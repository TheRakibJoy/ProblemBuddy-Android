package com.rakibjoy.problembuddy.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class CfEnvelope<T>(
    val status: String,
    val result: T? = null,
    val comment: String? = null,
)
