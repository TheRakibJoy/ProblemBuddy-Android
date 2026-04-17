package com.rakibjoy.problembuddy.domain.model

data class UserInfo(
    val handle: String,
    val rating: Int?,
    val maxRating: Int?,
    val rank: String?,
    val maxRank: String?,
    val avatarUrl: String? = null,
    val titlePhotoUrl: String? = null,
)
