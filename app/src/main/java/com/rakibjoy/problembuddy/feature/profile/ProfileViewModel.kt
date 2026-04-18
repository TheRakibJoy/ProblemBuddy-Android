package com.rakibjoy.problembuddy.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rakibjoy.problembuddy.core.database.dao.CounterDao
import com.rakibjoy.problembuddy.core.datastore.SettingsStore
import com.rakibjoy.problembuddy.domain.model.Submission
import com.rakibjoy.problembuddy.domain.model.Tier
import com.rakibjoy.problembuddy.domain.repository.CodeforcesRepository
import com.rakibjoy.problembuddy.domain.usecase.ComputeWeakTagsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val settingsStore: SettingsStore,
    private val codeforces: CodeforcesRepository,
    private val computeWeakTags: ComputeWeakTagsUseCase,
    private val counterDao: CounterDao,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    private val _effects = Channel<ProfileEffect>(Channel.BUFFERED)
    val effects: Flow<ProfileEffect> = _effects.receiveAsFlow()

    init {
        onIntent(ProfileIntent.Refresh)
    }

    fun onIntent(intent: ProfileIntent) {
        when (intent) {
            ProfileIntent.Refresh -> refresh()
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            _state.value = ProfileState(loading = true)
            try {
                val handle = settingsStore.cfHandle.first()
                if (handle.isNullOrBlank()) {
                    _state.value = ProfileState(loading = false, error = "No handle")
                    return@launch
                }

                val userInfoResult = codeforces.userInfoWithFallback(handle)
                val userInfoFresh = userInfoResult.getOrElse { e ->
                    _state.value = ProfileState(
                        loading = false,
                        handle = handle,
                        error = e.message ?: "Failed to load profile",
                    )
                    return@launch
                }
                val userInfo = userInfoFresh.value

                val rating = userInfo.rating
                val maxRating = userInfo.maxRating
                val tier = Tier.forMaxRating(maxRating ?: rating ?: 0)

                val submissionsFresh = codeforces.userStatusWithFallback(handle, 1, 100000).getOrNull()
                val submissions = submissionsFresh?.value.orEmpty()
                val stale = userInfoFresh.stale || (submissionsFresh?.stale ?: false)
                val fetchedAt = maxOf(
                    userInfoFresh.fetchedAt,
                    submissionsFresh?.fetchedAt ?: userInfoFresh.fetchedAt,
                )
                val solvedTagCounts: Map<String, Int> = submissions.asSequence()
                    .filter { it.verdict == "OK" }
                    .filter { sub ->
                        val r = sub.problem.rating ?: return@filter false
                        Tier.forMaxRating(r) == tier
                    }
                    .flatMap { it.problem.tags.asSequence() }
                    .groupingBy { it }
                    .eachCount()

                val weakTagNames = computeWeakTags(tier, solvedTagCounts)

                val corpus: Map<String, Int> = counterDao.getByTier(tier.name.lowercase())
                    .associate { it.tagName to it.count }

                val weakTags = weakTagNames.map { tag ->
                    val corpusCount = corpus[tag] ?: 0
                    val solved = solvedTagCounts[tag] ?: 0
                    val coverage = if (corpusCount > 0) {
                        (solved.toFloat() / corpusCount.toFloat())
                    } else {
                        0f
                    }
                    WeakTagStat(tag = tag, coverage = coverage.coerceIn(0f, 1f))
                }

                val ratingHistory = codeforces.userRating(handle).getOrNull()
                    ?.map { RatingPoint(timeSeconds = it.ratingUpdateTimeSeconds, rating = it.newRating) }
                    .orEmpty()

                val activity = buildActivityStats(submissions, ratingHistory)

                _state.value = ProfileState(
                    loading = false,
                    handle = userInfo.handle,
                    rating = rating,
                    maxRating = maxRating,
                    currentTier = tier,
                    weakTags = weakTags,
                    avatarUrl = userInfo.titlePhotoUrl ?: userInfo.avatarUrl,
                    error = null,
                    stale = stale,
                    fetchedAtMillis = fetchedAt,
                    activity = activity,
                )
            } catch (t: Throwable) {
                _state.value = _state.value.copy(
                    loading = false,
                    error = t.message ?: "Unexpected error",
                )
            }
        }
    }

    private fun buildActivityStats(
        submissions: List<Submission>,
        ratingHistory: List<RatingPoint>,
    ): ActivityStats {
        if (submissions.isEmpty() && ratingHistory.isEmpty()) return ActivityStats.Empty

        val acceptedByProblemByDay: Map<Long, Int> = submissions.asSequence()
            .filter { it.verdict == "OK" }
            .filter { it.problem.contestId != 0 && it.problem.problemIndex.isNotBlank() }
            // Dedup: first AC of a given problem only counts once per day.
            .distinctBy { Triple(it.problem.contestId, it.problem.problemIndex, dayEpoch(it.creationTimeSeconds)) }
            .groupingBy { dayEpoch(it.creationTimeSeconds) }
            .eachCount()

        val today = LocalDate.now(ZoneId.systemDefault()).toEpochDay()
        val currentStreak = computeCurrentStreak(acceptedByProblemByDay, todayEpoch = today)
        val longestStreak = computeLongestStreak(acceptedByProblemByDay)

        val yearStart = LocalDate.now(ZoneId.systemDefault())
            .withDayOfYear(1)
            .toEpochDay()
        val solvedThisYear = acceptedByProblemByDay
            .filterKeys { it >= yearStart }
            .values
            .sum()

        return ActivityStats(
            solvedByDayEpoch = acceptedByProblemByDay,
            currentStreakDays = currentStreak,
            longestStreakDays = longestStreak,
            solvedThisYear = solvedThisYear,
            ratingHistory = ratingHistory,
        )
    }

    private fun dayEpoch(epochSeconds: Long): Long {
        // Convert an epoch-seconds timestamp to the day index in the user's local zone.
        return java.time.Instant.ofEpochSecond(epochSeconds)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .toEpochDay()
    }

    private fun computeCurrentStreak(byDay: Map<Long, Int>, todayEpoch: Long): Int {
        if (byDay.isEmpty()) return 0
        // Allow the streak to still count if today is empty but yesterday was active.
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

    private fun computeLongestStreak(byDay: Map<Long, Int>): Int {
        if (byDay.isEmpty()) return 0
        val activeDays = byDay.keys.sorted()
        var longest = 1
        var running = 1
        for (i in 1 until activeDays.size) {
            running = if (activeDays[i] == activeDays[i - 1] + 1) running + 1 else 1
            if (running > longest) longest = running
        }
        return longest
    }
}
