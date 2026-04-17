package com.rakibjoy.problembuddy.feature.recommend

import com.rakibjoy.problembuddy.domain.model.Filters
import com.rakibjoy.problembuddy.domain.model.Problem

sealed interface RecommendIntent {
    data object Refresh : RecommendIntent
    data object OpenFilters : RecommendIntent
    data object CloseFilters : RecommendIntent
    data class ApplyFilters(val filters: Filters) : RecommendIntent
    data class MarkSolved(val problem: Problem) : RecommendIntent
    data class Skip(val problem: Problem) : RecommendIntent
    data class OpenUrl(val problem: Problem) : RecommendIntent
}
