package com.rakibjoy.problembuddy.domain.model

data class Review(
    val id: Long,
    val problemId: Long,
    val contestId: Int,
    val problemIndex: String,
    val box: Int,
    val lastReviewAt: Long,
    val nextReviewAt: Long,
    val lastOutcome: Outcome,
) {
    enum class Outcome { CORRECT, MISSED, INITIAL }
}
