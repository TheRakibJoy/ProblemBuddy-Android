package com.rakibjoy.problembuddy.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.rakibjoy.problembuddy.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private object PreferenceKeys {
    val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
    val KEY_CF_HANDLE = stringPreferencesKey("cf_handle")
    val KEY_RECS_PER_LOAD = intPreferencesKey("recs_per_load")
    val KEY_DIFFICULTY_OFFSET = intPreferencesKey("difficulty_offset")
    val KEY_COMPARE_HANDLE = stringPreferencesKey("compare_handle")
    val KEY_WEEKLY_GOAL = intPreferencesKey("weekly_goal")
    val KEY_LAST_SUBMISSION_ID_BY_HANDLE = stringPreferencesKey("last_submission_id_by_handle")
}

private val submissionMapSerializer = MapSerializer(String.serializer(), Long.serializer())
private val submissionMapJson = Json { ignoreUnknownKeys = true }

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

    val compareHandle: Flow<String?> = dataStore.data.map { prefs ->
        prefs[PreferenceKeys.KEY_COMPARE_HANDLE]
    }

    val weeklyGoal: Flow<Int> = dataStore.data.map { prefs ->
        prefs[PreferenceKeys.KEY_WEEKLY_GOAL] ?: 10
    }

    val lastSubmissionIdByHandle: Flow<Map<String, Long>> = dataStore.data.map { prefs ->
        val raw = prefs[PreferenceKeys.KEY_LAST_SUBMISSION_ID_BY_HANDLE] ?: return@map emptyMap()
        try {
            submissionMapJson.decodeFromString(submissionMapSerializer, raw)
        } catch (_: Exception) {
            emptyMap()
        }
    }

    suspend fun setLastSubmissionId(handle: String, id: Long) {
        dataStore.edit { prefs ->
            val raw = prefs[PreferenceKeys.KEY_LAST_SUBMISSION_ID_BY_HANDLE]
            val current = if (raw.isNullOrEmpty()) {
                emptyMap()
            } else {
                try {
                    submissionMapJson.decodeFromString(submissionMapSerializer, raw)
                } catch (_: Exception) {
                    emptyMap()
                }
            }
            val updated = current.toMutableMap().apply { put(handle, id) }
            prefs[PreferenceKeys.KEY_LAST_SUBMISSION_ID_BY_HANDLE] =
                submissionMapJson.encodeToString(submissionMapSerializer, updated)
        }
    }

    suspend fun clearSubmissionCheckpoints() {
        dataStore.edit { prefs ->
            prefs.remove(PreferenceKeys.KEY_LAST_SUBMISSION_ID_BY_HANDLE)
        }
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

    suspend fun setCompareHandle(handle: String?) {
        dataStore.edit { prefs ->
            if (handle == null) {
                prefs.remove(PreferenceKeys.KEY_COMPARE_HANDLE)
            } else {
                prefs[PreferenceKeys.KEY_COMPARE_HANDLE] = handle
            }
        }
    }

    suspend fun setWeeklyGoal(value: Int) {
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.KEY_WEEKLY_GOAL] = value
        }
    }

    suspend fun clearAll() {
        dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
