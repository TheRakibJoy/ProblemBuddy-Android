package com.rakibjoy.problembuddy.feature.home

sealed interface HomeIntent {
    data object RecommendClicked : HomeIntent
    data object TrainClicked : HomeIntent
    data object ProfileClicked : HomeIntent
    data object SettingsClicked : HomeIntent
}
