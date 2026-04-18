package com.rakibjoy.problembuddy.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class ContestDto(
    val id: Int,
    val name: String,
    val type: String? = null,
    val phase: String? = null,
    val frozen: Boolean = false,
    val durationSeconds: Long,
    val startTimeSeconds: Long? = null,
    val relativeTimeSeconds: Long? = null,
)
