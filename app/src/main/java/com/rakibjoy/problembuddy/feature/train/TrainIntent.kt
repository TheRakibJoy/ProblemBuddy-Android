package com.rakibjoy.problembuddy.feature.train

sealed interface TrainIntent {
    data class HandleChanged(val value: String) : TrainIntent
    data object StartClicked : TrainIntent
    data object CancelClicked : TrainIntent
}
