package com.rakibjoy.problembuddy.domain.model

data class Filters(
    val count: Int = 10,
    val minRating: Int? = null,
    val maxRating: Int? = null,
    val includeTags: Set<String> = emptySet(),
    val excludeTags: Set<String> = emptySet(),
    val weakOnly: Boolean = true,
)
