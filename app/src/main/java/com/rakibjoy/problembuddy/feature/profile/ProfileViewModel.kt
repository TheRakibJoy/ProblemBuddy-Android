package com.rakibjoy.problembuddy.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rakibjoy.problembuddy.core.database.dao.CounterDao
import com.rakibjoy.problembuddy.core.datastore.SettingsStore
import com.rakibjoy.problembuddy.domain.model.RatingChange
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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.ceil

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val settingsStore: SettingsStore,
    private val codeforces: CodeforcesRepository,
    private val computeWeakTags: ComputeWeakTagsUseCase,
    private val counterDao: CounterDao,
    private val problemDao: com.rakibjoy.problembuddy.core.database.dao.ProblemDao,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    private val _effects = Channel<ProfileEffect>(Channel.BUFFERED)
    val effects: Flow<ProfileEffect> = _effects.receiveAsFlow()

    private var compareFetchJob: Job? = null

    init {
        onIntent(ProfileIntent.Refresh)
        settingsStore.compareHandle
            .distinctUntilChanged()
            .onEach { rawHandle ->
                val handle = rawHandle?.takeIf { it.isNotBlank() }
                _state.update {
                    it.copy(
                        compareHandle = handle,
                        compareRating = if (handle == null) null else it.compareRating,
                        compareTier = if (handle == null) null else it.compareTier,
                        compareError = if (handle == null) null else it.compareError,
                    )
                }
                fetchCompareProfile(handle)
            }
            .launchIn(viewModelScope)
    }

    private fun fetchCompareProfile(handle: String?) {
        compareFetchJob?.cancel()
        if (handle == null) return
        compareFetchJob = viewModelScope.launch {
            codeforces.userInfo(handle)
                .onSuccess { info ->
                    val tier = Tier.forMaxRating(info.maxRating ?: info.rating ?: 0)
                    _state.update {
                        if (it.compareHandle == handle) {
                            it.copy(
                                compareRating = info.rating,
                                compareTier = tier,
                                compareError = null,
                            )
                        } else it
                    }
                }
                .onFailure { err ->
                    _state.update {
                        if (it.compareHandle == handle) {
                            it.copy(
                                compareRating = null,
                                compareTier = null,
                                compareError = err.message ?: "Failed to load",
                            )
                        } else it
                    }
                }
        }
    }

    fun onIntent(intent: ProfileIntent) {
        when (intent) {
            ProfileIntent.Refresh -> refresh()
            is ProfileIntent.SetCompareHandle -> {
                viewModelScope.launch {
                    val trimmed = intent.handle.trim()
                    settingsStore.setCompareHandle(trimmed.ifBlank { null })
                }
            }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            val existingCompare = _state.value.compareHandle
            val existingCompareRating = _state.value.compareRating
            val existingCompareTier = _state.value.compareTier
            val existingCompareError = _state.value.compareError
            _state.value = ProfileState(
                loading = true,
                compareHandle = existingCompare,
                compareRating = existingCompareRating,
                compareTier = existingCompareTier,
                compareError = existingCompareError,
            )
            try {
                val handle = settingsStore.cfHandle.first()
                if (handle.isNullOrBlank()) {
                    _state.value = ProfileState(
                        loading = false,
                        error = "No handle",
                        compareHandle = existingCompare,
                        compareRating = existingCompareRating,
                        compareTier = existingCompareTier,
                        compareError = existingCompareError,
                    )
                    return@launch
                }

                val userInfoResult = codeforces.userInfoWithFallback(handle)
                val userInfoFresh = userInfoResult.getOrElse { e ->
                    _state.value = ProfileState(
                        loading = false,
                        handle = handle,
                        error = e.message ?: "Failed to load profile",
                        compareHandle = existingCompare,
                        compareRating = existingCompareRating,
                        compareTier = existingCompareTier,
                        compareError = existingCompareError,
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

                val ratingChanges = codeforces.userRating(handle).getOrNull().orEmpty()
                val ratingHistory = ratingChanges
                    .map { RatingPoint(timeSeconds = it.ratingUpdateTimeSeconds, rating = it.newRating) }

                val activity = buildActivityStats(
                    submissions = submissions,
                    ratingHistory = ratingHistory,
                    ratingChanges = ratingChanges,
                    userTier = tier,
                )

                // Profile hero stats: total problems solved (unique key) + current-tier coverage.
                val solvedKeys: Set<Pair<Int, String>> = submissions.asSequence()
                    .filter { it.verdict == "OK" }
                    .filter { it.problem.contestId != 0 && it.problem.problemIndex.isNotBlank() }
                    .map { it.problem.contestId to it.problem.problemIndex }
                    .toSet()
                val problemsSolved = solvedKeys.size
                val solvedInTier = submissions.asSequence()
                    .filter { it.verdict == "OK" }
                    .filter { it.problem.contestId != 0 && it.problem.problemIndex.isNotBlank() }
                    .filter { sub ->
                        val r = sub.problem.rating ?: return@filter false
                        Tier.forMaxRating(r) == tier
                    }
                    .map { it.problem.contestId to it.problem.problemIndex }
                    .toSet()
                    .size
                val corpusInTier = runCatching {
                    problemDao.countByTier(tier.name.lowercase())
                }.getOrDefault(0)
                val coveragePct = if (corpusInTier > 0) {
                    ((solvedInTier.toFloat() / corpusInTier.toFloat()) * 100f)
                        .toInt()
                        .coerceIn(0, 100)
                } else {
                    null
                }

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
                    problemsSolved = problemsSolved,
                    coveragePct = coveragePct,
                    compareHandle = existingCompare,
                    compareRating = existingCompareRating,
                    compareTier = existingCompareTier,
                    compareError = existingCompareError,
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
        ratingChanges: List<RatingChange>,
        userTier: Tier,
    ): ActivityStats {
        if (submissions.isEmpty() && ratingHistory.isEmpty()) return ActivityStats.Empty

        val zone = ZoneId.systemDefault()
        val nowSeconds = System.currentTimeMillis() / 1000L

        // Sort once, ascending by time. Cheaper and enables chronological walks.
        val sortedSubs: List<Submission> = submissions.sortedBy { it.creationTimeSeconds }
        val validSubs: List<Submission> = sortedSubs.filter {
            it.problem.contestId != 0 && it.problem.problemIndex.isNotBlank()
        }

        // Heatmap — AC per day, deduped by (problem, day).
        val acceptedByProblemByDay: Map<Long, Int> = validSubs.asSequence()
            .filter { it.verdict == "OK" }
            .distinctBy { Triple(it.problem.contestId, it.problem.problemIndex, dayEpoch(it.creationTimeSeconds)) }
            .groupingBy { dayEpoch(it.creationTimeSeconds) }
            .eachCount()

        val today = LocalDate.now(zone).toEpochDay()
        val currentStreak = computeCurrentStreak(acceptedByProblemByDay, todayEpoch = today)
        val longestStreak = computeLongestStreak(acceptedByProblemByDay)

        val yearStart = LocalDate.now(zone).withDayOfYear(1).toEpochDay()
        val solvedThisYear = acceptedByProblemByDay.filterKeys { it >= yearStart }.values.sum()

        // First AC times per (contestId, index). Walk once, keep earliest OK.
        val firstAcTime = HashMap<Pair<Int, String>, Long>()
        // First submission time per problem — for first-attempt AC rate.
        val firstSubTime = HashMap<Pair<Int, String>, Long>()
        val firstSubWasOk = HashMap<Pair<Int, String>, Boolean>()

        for (s in validSubs) {
            val key = s.problem.contestId to s.problem.problemIndex
            if (key !in firstSubTime) {
                firstSubTime[key] = s.creationTimeSeconds
                firstSubWasOk[key] = (s.verdict == "OK")
            }
            if (s.verdict == "OK" && key !in firstAcTime) {
                firstAcTime[key] = s.creationTimeSeconds
            }
        }

        // Failed queue — attempts grouped by problem with no AC.
        val attemptsByProblem = HashMap<Pair<Int, String>, FailedAccumulator>()
        for (s in validSubs) {
            val key = s.problem.contestId to s.problem.problemIndex
            if (key in firstAcTime) continue
            val verdict = s.verdict ?: "UNKNOWN"
            if (verdict == "OK") continue // safety; shouldn't happen given firstAcTime check
            val acc = attemptsByProblem.getOrPut(key) {
                FailedAccumulator(
                    contestId = s.problem.contestId,
                    problemIndex = s.problem.problemIndex,
                    name = s.problem.name,
                    rating = s.problem.rating,
                    tags = s.problem.tags,
                )
            }
            acc.attempts += 1
            if (s.creationTimeSeconds >= acc.lastAttemptSeconds) {
                acc.lastAttemptSeconds = s.creationTimeSeconds
                acc.lastVerdict = verdict
            }
        }
        val failedQueue = attemptsByProblem.values
            .sortedByDescending { it.lastAttemptSeconds }
            .take(10)
            .map {
                FailedProblem(
                    contestId = it.contestId,
                    problemIndex = it.problemIndex,
                    name = it.name,
                    rating = it.rating,
                    tags = it.tags,
                    lastVerdict = it.lastVerdict,
                    attempts = it.attempts,
                    lastAttemptSeconds = it.lastAttemptSeconds,
                )
            }

        // Contest history from rating changes.
        val contestHistory = ratingChanges
            .sortedBy { it.ratingUpdateTimeSeconds }
            .map {
                ContestResult(
                    contestId = it.contestId,
                    name = it.contestName,
                    rank = it.rank,
                    oldRating = it.oldRating,
                    newRating = it.newRating,
                    timeSeconds = it.ratingUpdateTimeSeconds,
                    division = parseDivision(it.contestName),
                )
            }

        // Tier progression — cumulative first-AC count per tier, bucketed by month.
        val tierProgression = buildTierProgression(firstAcTime, validSubs, zone)

        // Verdict breakdown within last 90 days.
        val cutoff90d = nowSeconds - 90L * 86_400L
        val verdictBreakdown: Map<String, Int> = validSubs.asSequence()
            .filter { it.creationTimeSeconds >= cutoff90d }
            .groupingBy { it.verdict ?: "UNKNOWN" }
            .eachCount()

        // Tag radar — count solved problems within user's tier by canonical tag.
        // Build a problem-meta map in one pass so we don't scan validSubs per solved key.
        val radarTagSet = ActivityStats.RadarTags.toSet()
        val tagCounts = HashMap<String, Int>().apply { ActivityStats.RadarTags.forEach { put(it, 0) } }
        val metaByKey = HashMap<Pair<Int, String>, Submission>()
        for (s in validSubs) {
            if (s.verdict != "OK") continue
            val key = s.problem.contestId to s.problem.problemIndex
            if (key !in metaByKey) metaByKey[key] = s
        }
        for (key in firstAcTime.keys) {
            val sub = metaByKey[key] ?: continue
            val pr = sub.problem.rating ?: continue
            if (Tier.forMaxRating(pr) != userTier) continue
            for (t in sub.problem.tags) {
                if (t in radarTagSet) {
                    tagCounts[t] = (tagCounts[t] ?: 0) + 1
                }
            }
        }
        val tagRadar = ActivityStats.RadarTags.map { TagScore(it, tagCounts[it] ?: 0) }

        // First-attempt AC rate.
        val solvedKeys = firstAcTime.keys
        val totalSolved = solvedKeys.size
        val firstAttemptAcRate: Float? = if (totalSolved < 10) {
            null
        } else {
            val firstTryOk = solvedKeys.count { firstSubWasOk[it] == true }
            firstTryOk.toFloat() / totalSolved.toFloat()
        }

        // Division deltas.
        val divisionDeltas = contestHistory.asSequence()
            .filter { it.division != null }
            .groupBy { it.division!! }
            .map { (div, list) ->
                val avg = list.map { (it.newRating - it.oldRating).toFloat() }.average().toFloat()
                DivisionAverage(division = div, averageDelta = avg, contestCount = list.size)
            }
            .sortedByDescending { abs(it.averageDelta) }

        // Day-of-week + hour-of-day matrices (AC only).
        val dayCounts = IntArray(7)
        val hourCounts = IntArray(24)
        for (ts in firstAcTime.values) {
            val ldt = Instant.ofEpochSecond(ts).atZone(zone).toLocalDateTime()
            // DayOfWeek: MONDAY=1..SUNDAY=7, we want Monday=0..Sunday=6.
            val dow = ldt.dayOfWeek.value - 1
            dayCounts[dow] = dayCounts[dow] + 1
            hourCounts[ldt.hour] = hourCounts[ldt.hour] + 1
        }

        // Language counts — all submissions (not just AC), drop nulls.
        val languageCounts: Map<String, Int> = validSubs.asSequence()
            .mapNotNull { it.programmingLanguage }
            .groupingBy { it }
            .eachCount()

        // Virtual vs rated contest participation — distinct contest IDs.
        val virtualContestIds = HashSet<Int>()
        val ratedContestIds = HashSet<Int>()
        for (s in validSubs) {
            val cid = s.problem.contestId
            when (s.participantType) {
                "VIRTUAL" -> virtualContestIds.add(cid)
                "CONTESTANT" -> ratedContestIds.add(cid)
            }
        }

        // Milestones.
        val milestones = buildMilestones(validSubs, firstAcTime, contestHistory, ratingChanges)

        // Projection.
        val projection = buildProjection(contestHistory)

        // One-year-ago snapshot.
        val oneYearAgo = buildSnapshotOneYearAgo(
            nowSeconds = nowSeconds,
            firstAcTime = firstAcTime,
            ratingChanges = ratingChanges,
        )

        return ActivityStats(
            solvedByDayEpoch = acceptedByProblemByDay,
            currentStreakDays = currentStreak,
            longestStreakDays = longestStreak,
            solvedThisYear = solvedThisYear,
            ratingHistory = ratingHistory,
            failedQueue = failedQueue,
            contestHistory = contestHistory,
            tierProgression = tierProgression,
            verdictBreakdown = verdictBreakdown,
            tagRadar = tagRadar,
            firstAttemptAcRate = firstAttemptAcRate,
            divisionDeltas = divisionDeltas,
            dayOfWeekCounts = dayCounts,
            hourOfDayCounts = hourCounts,
            languageCounts = languageCounts,
            virtualParticipations = virtualContestIds.size,
            ratedParticipations = ratedContestIds.size,
            milestones = milestones,
            projection = projection,
            oneYearAgo = oneYearAgo,
            upsolveByContest = emptyList(),
        )
    }

    private class FailedAccumulator(
        val contestId: Int,
        val problemIndex: String,
        val name: String,
        val rating: Int?,
        val tags: List<String>,
        var lastVerdict: String = "UNKNOWN",
        var attempts: Int = 0,
        var lastAttemptSeconds: Long = 0L,
    )

    private fun buildTierProgression(
        firstAcTime: Map<Pair<Int, String>, Long>,
        validSubs: List<Submission>,
        zone: ZoneId,
    ): List<TierMonthPoint> {
        if (firstAcTime.isEmpty()) return emptyList()
        // Pull problem rating by looking up each solved key's first-AC submission.
        val ratingByKey = HashMap<Pair<Int, String>, Int?>()
        for (s in validSubs) {
            if (s.verdict != "OK") continue
            val key = s.problem.contestId to s.problem.problemIndex
            if (key in ratingByKey) continue
            ratingByKey[key] = s.problem.rating
        }

        // Events: (ym, tier) in chronological order.
        data class Event(val ym: YearMonth, val tier: Tier)
        val fmt = DateTimeFormatter.ofPattern("yyyy-MM")
        val events = firstAcTime.entries
            .mapNotNull { (key, ts) ->
                val r = ratingByKey[key] ?: return@mapNotNull null
                val ym = YearMonth.from(Instant.ofEpochSecond(ts).atZone(zone))
                Event(ym, Tier.forMaxRating(r))
            }
            .sortedBy { it.ym }

        if (events.isEmpty()) return emptyList()

        // Cumulative per tier across months. Emit points at each month that has activity.
        val cumulative = HashMap<Tier, Int>()
        val out = ArrayList<TierMonthPoint>()
        var currentYm: YearMonth? = null
        val monthAdds = HashMap<Tier, Int>()

        fun flushMonth(ym: YearMonth) {
            for ((tier, add) in monthAdds) {
                cumulative[tier] = (cumulative[tier] ?: 0) + add
            }
            for ((tier, cum) in cumulative.toSortedMap(compareBy { it.ordinal })) {
                out.add(TierMonthPoint(yearMonth = ym.format(fmt), tier = tier, cumulativeCount = cum))
            }
            monthAdds.clear()
        }

        for (e in events) {
            if (currentYm == null) currentYm = e.ym
            if (e.ym != currentYm) {
                flushMonth(currentYm!!)
                currentYm = e.ym
            }
            monthAdds[e.tier] = (monthAdds[e.tier] ?: 0) + 1
        }
        if (currentYm != null) flushMonth(currentYm!!)
        return out
    }

    private fun buildMilestones(
        validSubs: List<Submission>,
        firstAcTime: Map<Pair<Int, String>, Long>,
        contestHistory: List<ContestResult>,
        ratingChanges: List<RatingChange>,
    ): List<Milestone> {
        val out = ArrayList<Milestone>()

        // First AC ever + ordinal milestones.
        val acSorted = firstAcTime.entries.sortedBy { it.value }
        if (acSorted.isNotEmpty()) {
            out.add(
                Milestone(
                    timeSeconds = acSorted.first().value,
                    title = "First accepted solution",
                    subtitle = "${acSorted.first().key.first}${acSorted.first().key.second}",
                ),
            )
            for (n in listOf(100, 500, 1000, 2500)) {
                if (acSorted.size >= n) {
                    out.add(
                        Milestone(
                            timeSeconds = acSorted[n - 1].value,
                            title = "${n}th accepted solution",
                            subtitle = "Reached $n solves",
                        ),
                    )
                }
            }
        }

        // First problem at each tier.
        val ratingByKey = HashMap<Pair<Int, String>, Int?>()
        for (s in validSubs) {
            if (s.verdict != "OK") continue
            val key = s.problem.contestId to s.problem.problemIndex
            if (key in ratingByKey) continue
            ratingByKey[key] = s.problem.rating
        }
        val firstByTier = HashMap<Tier, Pair<Long, Pair<Int, String>>>()
        for (entry in acSorted) {
            val key = entry.key
            val ts = entry.value
            val r = ratingByKey[key] ?: continue
            val tier = Tier.forMaxRating(r)
            if (tier !in firstByTier) firstByTier[tier] = ts to key
        }
        for ((tier, pair) in firstByTier) {
            out.add(
                Milestone(
                    timeSeconds = pair.first,
                    title = "First ${tier.label}-tier problem",
                    subtitle = "${pair.second.first}${pair.second.second}",
                ),
            )
        }

        // First rated contest.
        if (contestHistory.isNotEmpty()) {
            val firstContest = contestHistory.minBy { it.timeSeconds }
            out.add(
                Milestone(
                    timeSeconds = firstContest.timeSeconds,
                    title = "First rated contest",
                    subtitle = firstContest.name,
                ),
            )
            // First Div 1.
            val firstDiv1 = contestHistory.filter { it.division == "Div 1" }.minByOrNull { it.timeSeconds }
            if (firstDiv1 != null) {
                out.add(
                    Milestone(
                        timeSeconds = firstDiv1.timeSeconds,
                        title = "First Div 1 contest",
                        subtitle = firstDiv1.name,
                    ),
                )
            }
            // Peak rating.
            val peak = ratingChanges.maxByOrNull { it.newRating }
            if (peak != null) {
                out.add(
                    Milestone(
                        timeSeconds = peak.ratingUpdateTimeSeconds,
                        title = "Peak rating ${peak.newRating}",
                        subtitle = peak.contestName,
                    ),
                )
            }
        }

        return out.sortedBy { it.timeSeconds }.take(20)
    }

    private fun buildProjection(contestHistory: List<ContestResult>): TierProjection? {
        if (contestHistory.size < 3) return null
        val recent = contestHistory.takeLast(minOf(10, contestHistory.size))
        val avgDelta = recent.map { (it.newRating - it.oldRating).toFloat() }.average().toFloat()
        val currentRating = contestHistory.last().newRating
        val currentTier = Tier.forMaxRating(currentRating)
        if (currentTier == Tier.LEGENDARY) return null
        val nextTier = Tier.entries.getOrNull(currentTier.ordinal + 1) ?: return null
        val ratingNeeded = nextTier.floor - currentRating
        val estimated = if (avgDelta > 0f) ceil(ratingNeeded.toDouble() / avgDelta.toDouble()).toInt() else null
        return TierProjection(
            nextTier = nextTier,
            ratingNeeded = ratingNeeded,
            currentRating = currentRating,
            averageDeltaLast10 = avgDelta,
            estimatedContestsToReach = estimated,
        )
    }

    private fun buildSnapshotOneYearAgo(
        nowSeconds: Long,
        firstAcTime: Map<Pair<Int, String>, Long>,
        ratingChanges: List<RatingChange>,
    ): ProfileSnapshot? {
        val cutoff = nowSeconds - 365L * 86_400L
        val acAtOrBeforeCutoff = firstAcTime.values.filter { it <= cutoff }
        if (acAtOrBeforeCutoff.isEmpty() && ratingChanges.none { it.ratingUpdateTimeSeconds <= cutoff }) {
            return null
        }
        val solvedCount = acAtOrBeforeCutoff.size
        val snapshotTs = acAtOrBeforeCutoff.maxOrNull() ?: cutoff
        val ratingAtTime = ratingChanges
            .filter { it.ratingUpdateTimeSeconds <= cutoff }
            .maxByOrNull { it.ratingUpdateTimeSeconds }
            ?.newRating
        val tier = ratingAtTime?.let { Tier.forMaxRating(it) }
        return ProfileSnapshot(
            timeSeconds = snapshotTs,
            rating = ratingAtTime,
            solvedCount = solvedCount,
            tier = tier,
        )
    }

    private fun parseDivision(contestName: String): String? {
        return when {
            "(Div. 1)" in contestName -> "Div 1"
            "(Div. 2)" in contestName -> "Div 2"
            "(Div. 3)" in contestName -> "Div 3"
            "(Div. 4)" in contestName -> "Div 4"
            contestName.startsWith("Educational", ignoreCase = true) -> "Edu"
            contestName.contains("Global", ignoreCase = true) -> "Global"
            else -> null
        }
    }

    private fun dayEpoch(epochSeconds: Long): Long {
        return Instant.ofEpochSecond(epochSeconds)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .toEpochDay()
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
