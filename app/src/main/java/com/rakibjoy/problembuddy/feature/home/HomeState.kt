package com.rakibjoy.problembuddy.feature.home

import com.rakibjoy.problembuddy.domain.model.TrainingJob

data class HomeState(
    val handle: String? = null,
    val greeting: String = "",
    val rating: Int? = null,
    val maxRating: Int? = null,
    val hasCorpus: Boolean = false,
    val latestJob: TrainingJob? = null,
)
