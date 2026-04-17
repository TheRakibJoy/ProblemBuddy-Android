package com.rakibjoy.problembuddy.feature.onboarding

data class OnboardingState(
    val handleInput: String = "",
    val validation: HandleValidation = HandleValidation.Idle,
    val submitting: Boolean = false,
) {
    val canSubmit: Boolean get() = validation is HandleValidation.Valid && !submitting
}

sealed interface HandleValidation {
    data object Idle : HandleValidation
    data object Validating : HandleValidation
    data class Valid(val handle: String) : HandleValidation
    data class Invalid(val reason: String) : HandleValidation
}
