package com.rakibjoy.problembuddy.domain.model

data class Submission(
    val id: Long,
    val problem: Problem,
    val verdict: String?,
    val creationTimeSeconds: Long,
    val programmingLanguage: String? = null,
    val participantType: String? = null,
)
