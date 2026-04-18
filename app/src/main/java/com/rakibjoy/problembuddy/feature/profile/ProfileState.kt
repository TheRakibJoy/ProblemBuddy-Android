package com.rakibjoy.problembuddy.feature.profile

import com.rakibjoy.problembuddy.domain.model.Tier

data class ProfileState(
    val loading: Boolean = true,
    val handle: String? = null,
    val rating: Int? = null,
    val maxRating: Int? = null,
    val currentTier: Tier? = null,
    val weakTags: List<WeakTagStat> = emptyList(),
    val avatarUrl: String? = null,
    val error: String? = null,
    val stale: Boolean = false,
    val fetchedAtMillis: Long? = null,
    val problemsSolved: Int? = null,
    val coveragePct: Int? = null,
    val activity: ActivityStats? = null,
    val compareHandle: String? = null,
    val compareRating: Int? = null,
    val compareTier: Tier? = null,
    val compareError: String? = null,
)

data class WeakTagStat(val tag: String, val coverage: Float)

/**
 * Activity-tab payload. Wraps heatmap cells, streaks, and rich activity-history features
 * computed from submissions + rating history.
 *
 * - dayOfWeekCounts: index 0 = Monday, 6 = Sunday.
 * - hourOfDayCounts: 0..23 in the user's local zone.
 */
data class ActivityStats(
    // Existing
    val solvedByDayEpoch: Map<Long, Int>,
    val currentStreakDays: Int,
    val longestStreakDays: Int,
    val solvedThisYear: Int,
    val ratingHistory: List<RatingPoint>,

    // New features
    val failedQueue: List<FailedProblem>,
    val contestHistory: List<ContestResult>,
    val tierProgression: List<TierMonthPoint>,
    val verdictBreakdown: Map<String, Int>,
    val tagRadar: List<TagScore>,
    val firstAttemptAcRate: Float?,
    val divisionDeltas: List<DivisionAverage>,
    val dayOfWeekCounts: IntArray,
    val hourOfDayCounts: IntArray,
    val languageCounts: Map<String, Int>,
    val virtualParticipations: Int,
    val ratedParticipations: Int,
    val milestones: List<Milestone>,
    val projection: TierProjection?,
    val oneYearAgo: ProfileSnapshot?,
    /** TODO: needs contest.standings which we don't fetch yet. */
    val upsolveByContest: List<ContestUpsolve> = emptyList(),
) {
    companion object {
        /** Canonical tags surfaced in the tag radar chart. */
        val RadarTags: List<String> = listOf(
            "dp",
            "graphs",
            "math",
            "strings",
            "ds",
            "greedy",
            "constructive algorithms",
            "implementation",
        )

        val Empty = ActivityStats(
            solvedByDayEpoch = emptyMap(),
            currentStreakDays = 0,
            longestStreakDays = 0,
            solvedThisYear = 0,
            ratingHistory = emptyList(),
            failedQueue = emptyList(),
            contestHistory = emptyList(),
            tierProgression = emptyList(),
            verdictBreakdown = emptyMap(),
            tagRadar = emptyList(),
            firstAttemptAcRate = null,
            divisionDeltas = emptyList(),
            dayOfWeekCounts = IntArray(7),
            hourOfDayCounts = IntArray(24),
            languageCounts = emptyMap(),
            virtualParticipations = 0,
            ratedParticipations = 0,
            milestones = emptyList(),
            projection = null,
            oneYearAgo = null,
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ActivityStats) return false
        return solvedByDayEpoch == other.solvedByDayEpoch &&
            currentStreakDays == other.currentStreakDays &&
            longestStreakDays == other.longestStreakDays &&
            solvedThisYear == other.solvedThisYear &&
            ratingHistory == other.ratingHistory &&
            failedQueue == other.failedQueue &&
            contestHistory == other.contestHistory &&
            tierProgression == other.tierProgression &&
            verdictBreakdown == other.verdictBreakdown &&
            tagRadar == other.tagRadar &&
            firstAttemptAcRate == other.firstAttemptAcRate &&
            divisionDeltas == other.divisionDeltas &&
            dayOfWeekCounts.contentEquals(other.dayOfWeekCounts) &&
            hourOfDayCounts.contentEquals(other.hourOfDayCounts) &&
            languageCounts == other.languageCounts &&
            virtualParticipations == other.virtualParticipations &&
            ratedParticipations == other.ratedParticipations &&
            milestones == other.milestones &&
            projection == other.projection &&
            oneYearAgo == other.oneYearAgo &&
            upsolveByContest == other.upsolveByContest
    }

    override fun hashCode(): Int {
        var result = solvedByDayEpoch.hashCode()
        result = 31 * result + currentStreakDays
        result = 31 * result + longestStreakDays
        result = 31 * result + solvedThisYear
        result = 31 * result + ratingHistory.hashCode()
        result = 31 * result + failedQueue.hashCode()
        result = 31 * result + contestHistory.hashCode()
        result = 31 * result + tierProgression.hashCode()
        result = 31 * result + verdictBreakdown.hashCode()
        result = 31 * result + tagRadar.hashCode()
        result = 31 * result + (firstAttemptAcRate?.hashCode() ?: 0)
        result = 31 * result + divisionDeltas.hashCode()
        result = 31 * result + dayOfWeekCounts.contentHashCode()
        result = 31 * result + hourOfDayCounts.contentHashCode()
        result = 31 * result + languageCounts.hashCode()
        result = 31 * result + virtualParticipations
        result = 31 * result + ratedParticipations
        result = 31 * result + milestones.hashCode()
        result = 31 * result + (projection?.hashCode() ?: 0)
        result = 31 * result + (oneYearAgo?.hashCode() ?: 0)
        result = 31 * result + upsolveByContest.hashCode()
        return result
    }
}

data class RatingPoint(val timeSeconds: Long, val rating: Int)

data class FailedProblem(
    val contestId: Int,
    val problemIndex: String,
    val name: String,
    val rating: Int?,
    val tags: List<String>,
    val lastVerdict: String,
    val attempts: Int,
    val lastAttemptSeconds: Long,
)

data class ContestResult(
    val contestId: Int,
    val name: String,
    val rank: Int,
    val oldRating: Int,
    val newRating: Int,
    val timeSeconds: Long,
    val division: String?,
)

data class TierMonthPoint(
    val yearMonth: String,
    val tier: Tier,
    val cumulativeCount: Int,
)

data class TagScore(val tag: String, val count: Int)

data class DivisionAverage(val division: String, val averageDelta: Float, val contestCount: Int)

data class Milestone(
    val timeSeconds: Long,
    val title: String,
    val subtitle: String,
)

data class TierProjection(
    val nextTier: Tier,
    val ratingNeeded: Int,
    val currentRating: Int,
    val averageDeltaLast10: Float,
    val estimatedContestsToReach: Int?,
)

data class ProfileSnapshot(
    val timeSeconds: Long,
    val rating: Int?,
    val solvedCount: Int,
    val tier: Tier?,
)

data class ContestUpsolve(
    val contestId: Int,
    val name: String,
    val totalProblems: Int,
    val solvedInContest: Int,
    val upsolvedSince: Int,
)
