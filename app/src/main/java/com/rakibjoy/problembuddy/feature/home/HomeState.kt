package com.rakibjoy.problembuddy.feature.home

import com.rakibjoy.problembuddy.core.ui.components.UpsolveBadge
import com.rakibjoy.problembuddy.domain.model.Problem
import com.rakibjoy.problembuddy.domain.model.Tier
import com.rakibjoy.problembuddy.domain.model.TrainingJob
import com.rakibjoy.problembuddy.domain.model.UpcomingContest

data class HomeState(
    val handle: String? = null,
    val greeting: String = "",
    val rating: Int? = null,
    val maxRating: Int? = null,
    val avatarUrl: String? = null,
    val hasCorpus: Boolean = false,
    val latestJob: TrainingJob? = null,
    // Redesign additions
    val ratingDelta: Int? = null,
    val problemsSolved: Int? = null,
    val streakDays: Int = 0,
    val todayHasAc: Boolean = false,
    val upcomingContest: UpcomingContest? = null,
    val nextTier: Tier? = null,
    val ratingToNextTier: Int? = null,
    val nextTierProgress: Float = 0f,
    val weeklyGoal: Int = 10,
    val weeklySolved: Int = 0,
    // count of AC'd problems in last 7 days
    val weakTagTrend: WeakTagTrend? = null,
    val upsolve: List<UpsolveProblem> = emptyList(),
    val todayPicks: List<TodayPick> = emptyList(),
)

data class WeakTagTrend(
    val tag: String,
    val trendLabel: String,
    val declining: Boolean,
    val points: List<Float>,
)

data class UpsolveProblem(
    val name: String,
    val meta: String,
    val badge: UpsolveBadge,
)

data class TodayPick(
    val problem: Problem,
    val tier: Tier,
    val isSolved: Boolean = false,
    val isSkipped: Boolean = false,
    val weakTags: Set<String> = emptySet(),
    val isActive: Boolean = false,
)
