package com.rakibjoy.problembuddy.domain.model

data class TagCounter(
    val tagName: String,
    val tier: Tier,
    val count: Int,
)
