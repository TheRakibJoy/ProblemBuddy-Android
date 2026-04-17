package com.rakibjoy.problembuddy.domain.model

data class RecommendationsResult(
    val problems: List<Problem>,
    val stale: Boolean,
)
