package com.rakibjoy.problembuddy.domain.model

data class IngestProgress(
    val handle: String,
    val tier: Tier?,
    val done: Int,
    val total: Int,
    val phase: Phase,
) {
    enum class Phase { FETCHING_SUBMISSIONS, WRITING_CORPUS, COMPLETED, FAILED }
}
