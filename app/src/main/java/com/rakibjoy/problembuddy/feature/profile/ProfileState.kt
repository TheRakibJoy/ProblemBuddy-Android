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
)

data class WeakTagStat(val tag: String, val coverage: Float)

/** Activity-tab payload: heatmap cells + streak numbers + rating timeline. */
data class ActivityStats(
    /** Number of AC submissions per day-of-epoch (floor of epochSeconds / 86_400). */
    val solvedByDayEpoch: Map<Long, Int>,
    val currentStreakDays: Int,
    val longestStreakDays: Int,
    val solvedThisYear: Int,
    val ratingHistory: List<RatingPoint>,
) {
    companion object {
        val Empty = ActivityStats(
            solvedByDayEpoch = emptyMap(),
            currentStreakDays = 0,
            longestStreakDays = 0,
            solvedThisYear = 0,
            ratingHistory = emptyList(),
        )
    }
}

data class RatingPoint(val timeSeconds: Long, val rating: Int)
