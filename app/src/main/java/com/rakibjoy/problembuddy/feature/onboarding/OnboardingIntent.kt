package com.rakibjoy.problembuddy.feature.onboarding

sealed interface OnboardingIntent {
    data class HandleChanged(val value: String) : OnboardingIntent
    data object SubmitClicked : OnboardingIntent
}
