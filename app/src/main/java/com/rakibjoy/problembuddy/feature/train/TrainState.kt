package com.rakibjoy.problembuddy.feature.train

import com.rakibjoy.problembuddy.domain.model.TrainingJob

data class TrainState(
    val handleInput: String = "",
    val handleValidation: HandleValidation = HandleValidation.Idle,
    val activeJob: TrainingJob? = null,
    val startEnabled: Boolean = false,
)

sealed interface HandleValidation {
    data object Idle : HandleValidation
    data object Validating : HandleValidation
    data object Valid : HandleValidation
    data class Invalid(val reason: String) : HandleValidation
}
