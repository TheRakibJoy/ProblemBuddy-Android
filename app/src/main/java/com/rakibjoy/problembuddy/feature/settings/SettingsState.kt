package com.rakibjoy.problembuddy.feature.settings

import com.rakibjoy.problembuddy.domain.model.ThemeMode

data class SettingsState(
    val theme: ThemeMode = ThemeMode.SYSTEM,
    val recsPerLoad: Int = 10,
    val difficultyOffset: Int = 0,
    val compareHandle: String = "",
    val weeklyGoal: Int = 10,
    val resetCorpusBusy: Boolean = false,
    val showResetCorpusConfirm: Boolean = false,
    val showDeleteAllConfirm: Boolean = false,
    val dailyNotificationEnabled: Boolean = false,
    val dailyNotificationHour: Int = 10,
    val dailyNotificationMinute: Int = 0,
)
