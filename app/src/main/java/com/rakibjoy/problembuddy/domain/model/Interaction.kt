package com.rakibjoy.problembuddy.domain.model

data class Interaction(
    val problemId: Long,
    val status: Status,
    val createdAt: Long,
) {
    enum class Status { SOLVED, NOT_INTERESTED, HIDDEN, SAVED }
}
