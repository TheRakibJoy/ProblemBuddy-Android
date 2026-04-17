package com.rakibjoy.problembuddy.feature.settings

sealed interface SettingsEffect {
    data class ShowToast(val message: String) : SettingsEffect
    data object NavigateToOnboarding : SettingsEffect
}
