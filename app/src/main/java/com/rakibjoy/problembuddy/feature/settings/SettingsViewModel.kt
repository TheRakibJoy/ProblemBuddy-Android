package com.rakibjoy.problembuddy.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rakibjoy.problembuddy.core.datastore.SettingsStore
import com.rakibjoy.problembuddy.core.work.DailyProblemScheduler
import com.rakibjoy.problembuddy.domain.repository.CounterRepository
import com.rakibjoy.problembuddy.domain.repository.HandleRepository
import com.rakibjoy.problembuddy.domain.repository.InteractionRepository
import com.rakibjoy.problembuddy.domain.repository.ProblemRepository
import com.rakibjoy.problembuddy.domain.repository.TrainingJobRepository
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
    private val problemRepository: ProblemRepository,
    private val counterRepository: CounterRepository,
    private val handleRepository: HandleRepository,
    private val interactionRepository: InteractionRepository,
    private val trainingJobRepository: TrainingJobRepository,
    private val dailyProblemScheduler: DailyProblemScheduler,
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
        settingsStore.compareHandle
            .onEach { v -> _state.update { it.copy(compareHandle = v.orEmpty()) } }
            .launchIn(viewModelScope)
        settingsStore.weeklyGoal
            .onEach { v -> _state.update { it.copy(weeklyGoal = v) } }
            .launchIn(viewModelScope)
        settingsStore.dailyNotificationEnabled
            .onEach { v -> _state.update { it.copy(dailyNotificationEnabled = v) } }
            .launchIn(viewModelScope)
        settingsStore.dailyNotificationHour
            .onEach { v -> _state.update { it.copy(dailyNotificationHour = v) } }
            .launchIn(viewModelScope)
        settingsStore.dailyNotificationMinute
            .onEach { v -> _state.update { it.copy(dailyNotificationMinute = v) } }
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
            is SettingsIntent.SetCompareHandle -> viewModelScope.launch {
                val trimmed = intent.handle.trim()
                settingsStore.setCompareHandle(trimmed.ifBlank { null })
            }
            is SettingsIntent.SetWeeklyGoal -> viewModelScope.launch {
                settingsStore.setWeeklyGoal(intent.value)
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
            is SettingsIntent.SetDailyNotification -> viewModelScope.launch {
                settingsStore.setDailyNotificationEnabled(intent.enabled)
                if (intent.enabled) {
                    dailyProblemScheduler.enqueue(
                        _state.value.dailyNotificationHour,
                        _state.value.dailyNotificationMinute,
                    )
                } else {
                    dailyProblemScheduler.cancel()
                }
            }
            is SettingsIntent.SetDailyNotificationHour -> viewModelScope.launch {
                val hour = intent.hour.coerceIn(0, 23)
                settingsStore.setDailyNotificationHour(hour)
                if (_state.value.dailyNotificationEnabled) {
                    dailyProblemScheduler.enqueue(hour, _state.value.dailyNotificationMinute)
                }
            }
            is SettingsIntent.SetDailyNotificationTime -> viewModelScope.launch {
                val hour = intent.hour.coerceIn(0, 23)
                val minute = intent.minute.coerceIn(0, 59)
                settingsStore.setDailyNotificationTime(hour, minute)
                if (_state.value.dailyNotificationEnabled) {
                    dailyProblemScheduler.enqueue(hour, minute)
                }
            }
        }
    }

    private fun confirmResetCorpus() {
        _state.update {
            it.copy(resetCorpusBusy = true, showResetCorpusConfirm = false)
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                problemRepository.clear()
                counterRepository.clear()
                handleRepository.clear()
                trainingJobRepository.clearAll()
            }
            _state.update { it.copy(resetCorpusBusy = false) }
            _effects.send(SettingsEffect.ShowToast("Corpus cleared"))
        }
    }

    private fun confirmDeleteAll() {
        _state.update { it.copy(showDeleteAllConfirm = false) }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                problemRepository.clear()
                counterRepository.clear()
                handleRepository.clear()
                interactionRepository.clear()
                trainingJobRepository.clearAll()
            }
            settingsStore.clearDailyProblem()
            settingsStore.clearAll()
            _effects.send(SettingsEffect.NavigateToOnboarding)
            _effects.send(SettingsEffect.ShowToast("All data deleted"))
        }
    }
}
