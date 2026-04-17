package com.rakibjoy.problembuddy.feature.home

sealed interface HomeEffect {
    data object NavigateToRecommend : HomeEffect
    data object NavigateToTrain : HomeEffect
    data object NavigateToProfile : HomeEffect
    data object NavigateToSettings : HomeEffect
    data class ShowToast(val message: String) : HomeEffect
}
