package com.rakibjoy.problembuddy.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rakibjoy.problembuddy.core.database.dao.CounterDao
import com.rakibjoy.problembuddy.core.database.dao.HandleDao
import com.rakibjoy.problembuddy.core.database.dao.InteractionDao
import com.rakibjoy.problembuddy.core.database.dao.ProblemDao
import com.rakibjoy.problembuddy.core.database.dao.TrainingJobDao
import com.rakibjoy.problembuddy.core.datastore.SettingsStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsStore: SettingsStore,
    private val problemDao: ProblemDao,
    private val counterDao: CounterDao,
    private val handleDao: HandleDao,
    private val interactionDao: InteractionDao,
    private val trainingJobDao: TrainingJobDao,
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    private val _effects = Channel<SettingsEffect>(Channel.BUFFERED)
    val effects: Flow<SettingsEffect> = _effects.receiveAsFlow()

    init {
        settingsStore.themeMode
            .onEach { mode -> _state.update { it.copy(theme = mode) } }
            .launchIn(viewModelScope)
        settingsStore.recsPerLoad
            .onEach { v -> _state.update { it.copy(recsPerLoad = v) } }
            .launchIn(viewModelScope)
        settingsStore.difficultyOffset
            .onEach { v -> _state.update { it.copy(difficultyOffset = v) } }
            .launchIn(viewModelScope)
    }

    fun onIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.SetTheme -> viewModelScope.launch {
                settingsStore.setThemeMode(intent.mode)
            }
            is SettingsIntent.SetRecsPerLoad -> viewModelScope.launch {
                settingsStore.setRecsPerLoad(intent.value)
            }
            is SettingsIntent.SetDifficultyOffset -> viewModelScope.launch {
                settingsStore.setDifficultyOffset(intent.value)
            }
            SettingsIntent.RequestResetCorpus ->
                _state.update { it.copy(showResetCorpusConfirm = true) }
            SettingsIntent.DismissResetCorpusConfirm ->
                _state.update { it.copy(showResetCorpusConfirm = false) }
            SettingsIntent.ConfirmResetCorpus -> confirmResetCorpus()
            SettingsIntent.RequestDeleteAll ->
                _state.update { it.copy(showDeleteAllConfirm = true) }
            SettingsIntent.DismissDeleteAllConfirm ->
                _state.update { it.copy(showDeleteAllConfirm = false) }
            SettingsIntent.ConfirmDeleteAll -> confirmDeleteAll()
        }
    }

    private fun confirmResetCorpus() {
        _state.update {
            it.copy(resetCorpusBusy = true, showResetCorpusConfirm = false)
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                problemDao.deleteAll()
                counterDao.deleteAll()
                handleDao.deleteAll()
                trainingJobDao.deleteAll()
            }
            _state.update { it.copy(resetCorpusBusy = false) }
            _effects.send(SettingsEffect.ShowToast("Corpus cleared"))
        }
    }

    private fun confirmDeleteAll() {
        _state.update { it.copy(showDeleteAllConfirm = false) }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                problemDao.deleteAll()
                counterDao.deleteAll()
                handleDao.deleteAll()
                interactionDao.deleteAll()
                trainingJobDao.deleteAll()
            }
            settingsStore.clearAll()
            _effects.send(SettingsEffect.NavigateToOnboarding)
            _effects.send(SettingsEffect.ShowToast("All data deleted"))
        }
    }
}
