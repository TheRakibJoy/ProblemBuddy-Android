package com.rakibjoy.problembuddy.feature.onboarding

sealed interface OnboardingEffect {
    data object NavigateToHome : OnboardingEffect
    data class ShowToast(val message: String) : OnboardingEffect
}
