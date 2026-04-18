package com.rakibjoy.problembuddy.feature.settings

import com.rakibjoy.problembuddy.domain.model.ThemeMode

sealed interface SettingsIntent {
    data class SetTheme(val mode: ThemeMode) : SettingsIntent
    data class SetRecsPerLoad(val value: Int) : SettingsIntent
    data class SetDifficultyOffset(val value: Int) : SettingsIntent
    data class SetCompareHandle(val handle: String) : SettingsIntent
    data class SetWeeklyGoal(val value: Int) : SettingsIntent
    data object RequestResetCorpus : SettingsIntent
    data object ConfirmResetCorpus : SettingsIntent
    data object DismissResetCorpusConfirm : SettingsIntent
    data object RequestDeleteAll : SettingsIntent
    data object ConfirmDeleteAll : SettingsIntent
    data object DismissDeleteAllConfirm : SettingsIntent
    data class SetDailyNotification(val enabled: Boolean) : SettingsIntent
    data class SetDailyNotificationHour(val hour: Int) : SettingsIntent
    data class SetDailyNotificationTime(val hour: Int, val minute: Int) : SettingsIntent
}
