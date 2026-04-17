package com.rakibjoy.problembuddy.feature.profile

import com.rakibjoy.problembuddy.domain.model.Tier

data class ProfileState(
    val loading: Boolean = true,
    val handle: String? = null,
    val rating: Int? = null,
    val maxRating: Int? = null,
    val currentTier: Tier? = null,
    val weakTags: List<WeakTagStat> = emptyList(),
    val error: String? = null,
)

data class WeakTagStat(val tag: String, val coverage: Float)
