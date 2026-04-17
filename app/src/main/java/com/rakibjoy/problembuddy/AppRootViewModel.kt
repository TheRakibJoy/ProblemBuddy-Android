package com.rakibjoy.problembuddy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rakibjoy.problembuddy.core.datastore.SettingsStore
import com.rakibjoy.problembuddy.domain.model.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed interface StartDestination {
    data object Onboarding : StartDestination
    data object Home : StartDestination
}

@HiltViewModel
class AppRootViewModel @Inject constructor(
    settingsStore: SettingsStore,
) : ViewModel() {

    val startDestination: StateFlow<StartDestination?> = settingsStore.cfHandle
        .map { handle -> if (handle.isNullOrBlank()) StartDestination.Onboarding else StartDestination.Home }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null,
        )

    val themeMode: StateFlow<ThemeMode> = settingsStore.themeMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = ThemeMode.SYSTEM,
        )
}
