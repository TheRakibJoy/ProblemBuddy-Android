package com.rakibjoy.problembuddy.feature.recommend

sealed interface RecommendEffect {
    data class OpenUrl(val url: String) : RecommendEffect
    data class Toast(val message: String) : RecommendEffect
}
