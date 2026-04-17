package com.rakibjoy.problembuddy.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class RatingChangeDto(
    val contestId: Int,
    val contestName: String,
    val handle: String,
    val rank: Int,
    val ratingUpdateTimeSeconds: Long,
    val oldRating: Int,
    val newRating: Int,
)
