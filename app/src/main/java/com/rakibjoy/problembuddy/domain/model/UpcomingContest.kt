package com.rakibjoy.problembuddy.domain.model

data class UpcomingContest(
    val id: Int,
    val name: String,
    val startTimeSeconds: Long,
    val durationSeconds: Long,
    val division: String?,
)
