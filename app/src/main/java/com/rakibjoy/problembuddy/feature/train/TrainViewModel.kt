package com.rakibjoy.problembuddy.feature.train

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rakibjoy.problembuddy.core.work.IngestScheduler
import com.rakibjoy.problembuddy.domain.model.CodeforcesException
import com.rakibjoy.problembuddy.domain.model.TrainingJob
import com.rakibjoy.problembuddy.domain.repository.CodeforcesRepository
import com.rakibjoy.problembuddy.domain.repository.TrainingJobRepository
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
class TrainViewModel @Inject constructor(
    private val codeforces: CodeforcesRepository,
    private val trainingJobRepository: TrainingJobRepository,
    private val ingestScheduler: IngestScheduler,
) : ViewModel() {

    private val _state = MutableStateFlow(TrainState())
    val state: StateFlow<TrainState> = _state.asStateFlow()

    private val _effects = Channel<TrainEffect>(Channel.BUFFERED)
    val effects: Flow<TrainEffect> = _effects.receiveAsFlow()

    private var validationJob: Job? = null

    init {
        viewModelScope.launch {
            trainingJobRepository.observeLatest().collect { job ->
                _state.update {
                    it.copy(
                        activeJob = job,
                        startEnabled = canStart(it.handleValidation, job),
                    )
                }
            }
        }
    }

    fun onIntent(intent: TrainIntent) {
        when (intent) {
            is TrainIntent.HandleChanged -> onHandleChanged(intent.value)
            TrainIntent.StartClicked -> onStartClicked()
            TrainIntent.CancelClicked -> onCancelClicked()
        }
    }

    private fun onHandleChanged(value: String) {
        validationJob?.cancel()
        _state.update {
            it.copy(
                handleInput = value,
                handleValidation = HandleValidation.Idle,
                startEnabled = canStart(HandleValidation.Idle, it.activeJob),
            )
        }
        validationJob = viewModelScope.launch {
            delay(300)
            val trimmed = value.trim()
            if (trimmed.isBlank() || trimmed.length < 3) {
                updateValidation(HandleValidation.Invalid("Too short"))
                return@launch
            }
            if (!HANDLE_REGEX.matches(trimmed)) {
                updateValidation(HandleValidation.Invalid("Invalid characters"))
                return@launch
            }
            updateValidation(HandleValidation.Validating)
            codeforces.userInfo(trimmed).fold(
                onSuccess = { updateValidation(HandleValidation.Valid) },
                onFailure = { e ->
                    val reason = when (e) {
                        is CodeforcesException.HandleNotFound -> "Handle not found"
                        is CodeforcesException.CodeforcesUnavailable -> "Couldn't reach Codeforces"
                        else -> "Couldn't validate handle"
                    }
                    updateValidation(HandleValidation.Invalid(reason))
                },
            )
        }
    }

    private fun updateValidation(validation: HandleValidation) {
        _state.update {
            it.copy(
                handleValidation = validation,
                startEnabled = canStart(validation, it.activeJob),
            )
        }
    }

    private fun onStartClicked() {
        val current = _state.value
        if (!current.startEnabled) return
        ingestScheduler.enqueue(listOf(current.handleInput.trim()))
        viewModelScope.launch { _effects.send(TrainEffect.ShowToast("Training started")) }
    }

    private fun onCancelClicked() {
        ingestScheduler.cancel()
        viewModelScope.launch { _effects.send(TrainEffect.ShowToast("Canceled")) }
    }

    private fun canStart(validation: HandleValidation, job: TrainingJob?): Boolean {
        val validHandle = validation is HandleValidation.Valid
        val jobIdle = job == null ||
            job.status == TrainingJob.Status.SUCCESS ||
            job.status == TrainingJob.Status.FAILED
        return validHandle && jobIdle
    }

    private companion object {
        val HANDLE_REGEX = Regex("^[A-Za-z0-9_.-]+$")
    }
}
