package com.rakibjoy.problembuddy.domain.model

data class TrainingJob(
    val id: Long = 0,
    val handle: String,
    val status: Status,
    val currentTier: Tier?,
    val done: Int,
    val total: Int,
    val error: String?,
    val updatedAt: Long,
) {
    enum class Status { QUEUED, RUNNING, SUCCESS, FAILED }
}
