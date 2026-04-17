package com.rakibjoy.problembuddy.feature.recommend

import com.rakibjoy.problembuddy.domain.model.Filters
import com.rakibjoy.problembuddy.domain.model.Problem

data class RecommendState(
    val loading: Boolean = true,
    val problems: List<Problem> = emptyList(),
    val filters: Filters = Filters(),
    val error: String? = null,
    val filterSheetOpen: Boolean = false,
)
