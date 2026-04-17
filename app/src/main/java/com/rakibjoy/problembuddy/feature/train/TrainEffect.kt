package com.rakibjoy.problembuddy.feature.train

sealed interface TrainEffect {
    data class ShowToast(val message: String) : TrainEffect
}
