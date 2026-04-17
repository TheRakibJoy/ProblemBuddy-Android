package com.rakibjoy.problembuddy.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.rakibjoy.problembuddy.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private object PreferenceKeys {
    val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
    val KEY_CF_HANDLE = stringPreferencesKey("cf_handle")
    val KEY_RECS_PER_LOAD = intPreferencesKey("recs_per_load")
    val KEY_DIFFICULTY_OFFSET = intPreferencesKey("difficulty_offset")
}

@Singleton
class SettingsStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    val themeMode: Flow<ThemeMode> = dataStore.data.map { prefs ->
        val raw = prefs[PreferenceKeys.KEY_THEME_MODE] ?: return@map ThemeMode.SYSTEM
        try {
            ThemeMode.valueOf(raw)
        } catch (_: IllegalArgumentException) {
            ThemeMode.SYSTEM
        }
    }

    val cfHandle: Flow<String?> = dataStore.data.map { prefs ->
        prefs[PreferenceKeys.KEY_CF_HANDLE]
    }

    val recsPerLoad: Flow<Int> = dataStore.data.map { prefs ->
        prefs[PreferenceKeys.KEY_RECS_PER_LOAD] ?: 10
    }

    val difficultyOffset: Flow<Int> = dataStore.data.map { prefs ->
        prefs[PreferenceKeys.KEY_DIFFICULTY_OFFSET] ?: 0
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.KEY_THEME_MODE] = mode.name
        }
    }

    suspend fun setCfHandle(handle: String?) {
        dataStore.edit { prefs ->
            if (handle == null) {
                prefs.remove(PreferenceKeys.KEY_CF_HANDLE)
            } else {
                prefs[PreferenceKeys.KEY_CF_HANDLE] = handle
            }
        }
    }

    suspend fun setRecsPerLoad(n: Int) {
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.KEY_RECS_PER_LOAD] = n
        }
    }

    suspend fun setDifficultyOffset(n: Int) {
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.KEY_DIFFICULTY_OFFSET] = n
        }
    }

    suspend fun clearAll() {
        dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
