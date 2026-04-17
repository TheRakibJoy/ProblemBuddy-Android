package com.rakibjoy.problembuddy.domain.model

data class Fresh<T>(val value: T, val stale: Boolean, val fetchedAt: Long)
