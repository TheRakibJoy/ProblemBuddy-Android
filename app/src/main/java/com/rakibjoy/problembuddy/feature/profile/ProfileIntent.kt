package com.rakibjoy.problembuddy.feature.profile

sealed interface ProfileIntent {
    data object Refresh : ProfileIntent
    data class SetCompareHandle(val handle: String) : ProfileIntent
}
