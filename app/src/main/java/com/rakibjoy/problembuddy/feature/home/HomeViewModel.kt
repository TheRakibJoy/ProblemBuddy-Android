package com.rakibjoy.problembuddy.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rakibjoy.problembuddy.core.database.dao.ProblemDao
import com.rakibjoy.problembuddy.core.datastore.SettingsStore
import com.rakibjoy.problembuddy.domain.model.Tier
import com.rakibjoy.problembuddy.domain.repository.CodeforcesRepository
import com.rakibjoy.problembuddy.domain.repository.ReviewRepository
import com.rakibjoy.problembuddy.domain.repository.TrainingJobRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

/**
 * Home surfaces a dashboard: greeting, rating / solved / streak stats, weak-tag
 * trend, today's picks, upsolve queue, and a primary CTA.
 *
 * `streakDays`, `weakTagTrend`, `upsolve`, and `todayPicks` are still computed
 * lazily — they require contest-standings lookups and a recommendation pass
 * per load, which haven't been implemented yet. Their defaults (null / empty)
 * hide the corresponding sections on the Home screen so the layout stays
 * correct in the meantime.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val settingsStore: SettingsStore,
    private val codeforces: CodeforcesRepository,
    private val trainingJobRepository: TrainingJobRepository,
    private val problemDao: ProblemDao,
    private val reviewRepository: ReviewRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private val _effects = Channel<HomeEffect>(Channel.BUFFERED)
    val effects: Flow<HomeEffect> = _effects.receiveAsFlow()

    private var lastHandleFetched: String? = null

    init {
        viewModelScope.launch {
            settingsStore.weeklyGoal.distinctUntilChanged().collect { goal ->
                _state.update { it.copy(weeklyGoal = goal) }
            }
        }
        viewModelScope.launch {
            observeReviewsDue()
                .collect { reviews ->
                    val due = reviews.take(5).map { r ->
                        DueReview(
                            id = r.id,
                            contestId = r.contestId,
                            problemIndex = r.problemIndex,
                            box = r.box,
                        )
                    }
                    _state.update { it.copy(reviewsDue = due) }
                }
        }
        viewModelScope.launch {
            combine(
                settingsStore.cfHandle.distinctUntilChanged(),
                trainingJobRepository.observeLatest(),
            ) { handle, job -> handle to job }
                .collect { (handle, job) ->
                    val greeting = if (handle != null) "Welcome back, $handle" else "Welcome"
                    _state.update {
                        it.copy(
                            handle = handle,
                            greeting = greeting,
                            latestJob = job,
                            rating = if (handle == null) null else it.rating,
                            maxRating = if (handle == null) null else it.maxRating,
                            ratingDelta = if (handle == null) null else it.ratingDelta,
                            problemsSolved = if (handle == null) null else it.problemsSolved,
                            streakDays = if (handle == null) 0 else it.streakDays,
                            todayHasAc = if (handle == null) false else it.todayHasAc,
                            upcomingContest = if (handle == null) null else it.upcomingContest,
                            nextTier = if (handle == null) null else it.nextTier,
                            ratingToNextTier = if (handle == null) null else it.ratingToNextTier,
                            nextTierProgress = if (handle == null) 0f else it.nextTierProgress,
                        )
                    }
                    refreshCorpus()
                    if (handle != null && handle != lastHandleFetched) {
                        lastHandleFetched = handle
                        fetchUserInfo(handle)
                        fetchRatingDelta(handle)
                        fetchSolvedCount(handle)
                        fetchUpcomingContests()
                    } else if (handle == null) {
                        lastHandleFetched = null
                    }
                }
        }
    }

    fun onIntent(intent: HomeIntent) {
        when (intent) {
            HomeIntent.RecommendClicked -> onRecommend()
            HomeIntent.TrainClicked -> emit(HomeEffect.NavigateToTrain)
            HomeIntent.ProfileClicked -> emit(HomeEffect.NavigateToProfile)
            HomeIntent.SettingsClicked -> emit(HomeEffect.NavigateToSettings)
        }
    }

    private fun onRecommend() {
        if (!_state.value.hasCorpus) {
            emit(HomeEffect.ShowToast("Run training first"))
        } else {
            emit(HomeEffect.NavigateToRecommend)
        }
    }

    private fun emit(effect: HomeEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private fun observeReviewsDue() =
        flow {
            while (true) {
                emit(System.currentTimeMillis() / 1000L)
                delay(60_000L)
            }
        }.flatMapLatest { nowSeconds -> reviewRepository.observeDue(nowSeconds) }

    private fun fetchUserInfo(handle: String) {
        viewModelScope.launch {
            codeforces.userInfo(handle).onSuccess { info ->
                val userRating = info.rating ?: info.maxRating ?: 0
                val currentTier = Tier.forMaxRating(userRating)
                val nextTier = Tier.entries.firstOrNull { it.floor > currentTier.floor }
                val ratingToNext: Int?
                val progress: Float
                if (nextTier != null) {
                    ratingToNext = (nextTier.floor - userRating).coerceAtLeast(0)
                    val span = (nextTier.floor - currentTier.floor).toFloat()
                    progress = if (span <= 0f) 1f
                    else ((userRating - currentTier.floor).toFloat() / span).coerceIn(0f, 1f)
                } else {
                    ratingToNext = null
                    progress = 1f
                }
                _state.update {
                    if (it.handle == handle) {
                        it.copy(
                            rating = info.rating,
                            maxRating = info.maxRating,
                            avatarUrl = info.titlePhotoUrl ?: info.avatarUrl,
                            nextTier = nextTier,
                            ratingToNextTier = ratingToNext,
                            nextTierProgress = progress,
                        )
                    } else it
                }
            }
        }
    }

    private fun fetchUpcomingContests() {
        viewModelScope.launch {
            codeforces.upcomingContests().onSuccess { list ->
                val nowSeconds = System.currentTimeMillis() / 1000L
                val next = list.asSequence()
                    .filter { it.startTimeSeconds >= nowSeconds }
                    .minByOrNull { it.startTimeSeconds }
                _state.update { it.copy(upcomingContest = next) }
            }
        }
    }

    private fun fetchRatingDelta(handle: String) {
        viewModelScope.launch {
            codeforces.userRating(handle).onSuccess { changes ->
                val last = changes.lastOrNull() ?: return@onSuccess
                val delta = last.newRating - last.oldRating
                _state.update {
                    if (it.handle == handle) it.copy(ratingDelta = delta) else it
                }
            }
        }
    }

    private fun fetchSolvedCount(handle: String) {
        viewModelScope.launch {
            codeforces.userStatus(handle, from = 1, count = 100_000).onSuccess { subs ->
                val acceptedSubs = subs.asSequence().filter { it.verdict == "OK" }.toList()
                val solvedKeys = acceptedSubs.asSequence()
                    .mapNotNull { sub ->
                        val p = sub.problem
                        if (p.contestId == 0 || p.problemIndex.isBlank()) null
                        else p.contestId to p.problemIndex
                    }
                    .toSet()
                val weekCutoff = (System.currentTimeMillis() / 1000L) - 7L * 86_400L
                val weeklyKeys = acceptedSubs.asSequence()
                    .filter { it.creationTimeSeconds >= weekCutoff }
                    .mapNotNull { sub ->
                        val p = sub.problem
                        if (p.contestId == 0 || p.problemIndex.isBlank()) null
                        else p.contestId to p.problemIndex
                    }
                    .toSet()

                val zone = ZoneId.systemDefault()
                val today = LocalDate.now(zone)
                val startOfTodaySec = today.atStartOfDay(zone).toEpochSecond()
                val todayHasAc = acceptedSubs.any { it.creationTimeSeconds >= startOfTodaySec }
                val acByDay: Map<Long, Int> = acceptedSubs.asSequence()
                    .filter { it.problem.contestId != 0 && it.problem.problemIndex.isNotBlank() }
                    .distinctBy {
                        Triple(
                            it.problem.contestId,
                            it.problem.problemIndex,
                            Instant.ofEpochSecond(it.creationTimeSeconds)
                                .atZone(zone).toLocalDate().toEpochDay(),
                        )
                    }
                    .groupingBy {
                        Instant.ofEpochSecond(it.creationTimeSeconds)
                            .atZone(zone).toLocalDate().toEpochDay()
                    }
                    .eachCount()
                val streakDays = computeCurrentStreak(acByDay, today.toEpochDay())

                _state.update {
                    if (it.handle == handle) it.copy(
                        problemsSolved = solvedKeys.size,
                        weeklySolved = weeklyKeys.size,
                        todayHasAc = todayHasAc,
                        streakDays = streakDays,
                    ) else it
                }
            }
        }
    }

    private fun computeCurrentStreak(byDay: Map<Long, Int>, todayEpoch: Long): Int {
        if (byDay.isEmpty()) return 0
        var cursor = if (byDay[todayEpoch] == null && byDay[todayEpoch - 1] != null) {
            todayEpoch - 1
        } else {
            todayEpoch
        }
        var streak = 0
        while ((byDay[cursor] ?: 0) > 0) {
            streak++
            cursor -= 1
        }
        return streak
    }

    private fun refreshCorpus() {
        viewModelScope.launch {
            val total = runCatching {
                Tier.entries.sumOf { problemDao.countByTier(it.name.lowercase()) }
            }.getOrDefault(0)
            _state.update { it.copy(hasCorpus = total > 0) }
        }
    }
}
