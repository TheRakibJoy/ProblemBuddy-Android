package com.rakibjoy.problembuddy.domain.model

data class Problem(
    val contestId: Int,
    val problemIndex: String,
    val name: String,
    val rating: Int?,
    val tags: List<String>,
)
