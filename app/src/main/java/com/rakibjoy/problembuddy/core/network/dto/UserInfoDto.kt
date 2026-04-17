package com.rakibjoy.problembuddy.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserInfoDto(
    val handle: String,
    val rating: Int? = null,
    val maxRating: Int? = null,
    val rank: String? = null,
    val maxRank: String? = null,
    val avatar: String? = null,
    val titlePhoto: String? = null,
)
