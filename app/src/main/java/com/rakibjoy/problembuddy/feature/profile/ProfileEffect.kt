package com.rakibjoy.problembuddy.feature.profile

sealed interface ProfileEffect {
    data class ShowToast(val message: String) : ProfileEffect
}
