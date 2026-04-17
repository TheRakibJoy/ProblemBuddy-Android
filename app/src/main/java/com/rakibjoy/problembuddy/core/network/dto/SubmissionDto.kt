package com.rakibjoy.problembuddy.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class SubmissionDto(
    val id: Long,
    val problem: ProblemDto,
    val verdict: String? = null,
    val creationTimeSeconds: Long,
)
