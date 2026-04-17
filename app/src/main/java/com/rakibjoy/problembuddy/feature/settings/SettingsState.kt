package com.rakibjoy.problembuddy.feature.settings

import com.rakibjoy.problembuddy.domain.model.ThemeMode

data class SettingsState(
    val theme: ThemeMode = ThemeMode.SYSTEM,
    val recsPerLoad: Int = 10,
    val difficultyOffset: Int = 0,
    val resetCorpusBusy: Boolean = false,
    val showResetCorpusConfirm: Boolean = false,
    val showDeleteAllConfirm: Boolean = false,
)
