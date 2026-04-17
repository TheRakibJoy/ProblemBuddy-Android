package com.rakibjoy.problembuddy.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rakibjoy.problembuddy.core.datastore.SettingsStore
import com.rakibjoy.problembuddy.domain.model.CodeforcesException
import com.rakibjoy.problembuddy.domain.repository.CodeforcesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val codeforces: CodeforcesRepository,
    private val settingsStore: SettingsStore,
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    private val _effects = Channel<OnboardingEffect>(Channel.BUFFERED)
    val effects: Flow<OnboardingEffect> = _effects.receiveAsFlow()

    private var validationJob: Job? = null

    fun onIntent(intent: OnboardingIntent) {
        when (intent) {
            is OnboardingIntent.HandleChanged -> onHandleChanged(intent.value)
            OnboardingIntent.SubmitClicked -> onSubmitClicked()
        }
    }

    private fun onHandleChanged(value: String) {
        validationJob?.cancel()
        val trimmed = value.trim()
        val immediate: HandleValidation = when {
            trimmed.isBlank() || trimmed.length < 3 -> HandleValidation.Invalid("Too short")
            !HANDLE_REGEX.matches(trimmed) -> HandleValidation.Invalid("Invalid characters")
            else -> HandleValidation.Idle
        }
        _state.update { it.copy(handleInput = value, validation = immediate) }
        if (immediate is HandleValidation.Invalid) return

        validationJob = viewModelScope.launch {
            delay(300)
            _state.update { it.copy(validation = HandleValidation.Validating) }
            codeforces.userInfo(trimmed).fold(
                onSuccess = {
                    _state.update { it.copy(validation = HandleValidation.Valid(trimmed)) }
                },
                onFailure = { e ->
                    val reason = when (e) {
                        is CodeforcesException.HandleNotFound -> "Handle not found"
                        is CodeforcesException.CodeforcesUnavailable -> "Couldn't reach Codeforces"
                        else -> "Couldn't validate handle"
                    }
                    _state.update { it.copy(validation = HandleValidation.Invalid(reason)) }
                },
            )
        }
    }

    private fun onSubmitClicked() {
        val current = _state.value
        if (!current.canSubmit) return
        val valid = current.validation as? HandleValidation.Valid ?: return
        _state.update { it.copy(submitting = true) }
        viewModelScope.launch {
            try {
                settingsStore.setCfHandle(valid.handle)
                _effects.send(OnboardingEffect.NavigateToHome)
            } catch (e: Exception) {
                _effects.send(OnboardingEffect.ShowToast("Couldn't save handle"))
            } finally {
                _state.update { it.copy(submitting = false) }
            }
        }
    }

    private companion object {
        val HANDLE_REGEX = Regex("^[A-Za-z0-9_.-]+$")
    }
}
