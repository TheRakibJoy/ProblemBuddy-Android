package com.rakibjoy.problembuddy.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProblemDto(
    val contestId: Int? = null,
    val index: String,
    val name: String,
    val rating: Int? = null,
    val tags: List<String> = emptyList(),
)
