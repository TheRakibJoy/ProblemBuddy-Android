package com.rakibjoy.problembuddy.feature.train

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rakibjoy.problembuddy.core.database.dao.CounterDao
import com.rakibjoy.problembuddy.core.database.dao.HandleDao
import com.rakibjoy.problembuddy.core.database.dao.ProblemDao
import com.rakibjoy.problembuddy.core.database.dao.TrainingJobDao
import com.rakibjoy.problembuddy.core.work.IngestScheduler
import com.rakibjoy.problembuddy.domain.model.CodeforcesException
import com.rakibjoy.problembuddy.domain.model.Tier
import com.rakibjoy.problembuddy.domain.model.TrainingJob
import com.rakibjoy.problembuddy.domain.repository.CodeforcesRepository
import com.rakibjoy.problembuddy.domain.repository.TrainingJobRepository
import com.rakibjoy.problembuddy.domain.util.HandleValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrainViewModel @Inject constructor(
    private val codeforces: CodeforcesRepository,
    private val trainingJobRepository: TrainingJobRepository,
    private val ingestScheduler: IngestScheduler,
    private val problemDao: ProblemDao,
    private val counterDao: CounterDao,
    private val handleDao: HandleDao,
    private val trainingJobDao: TrainingJobDao,
) : ViewModel() {

    private val _state = MutableStateFlow(TrainState())
    val state: StateFlow<TrainState> = _state.asStateFlow()

    private val _effects = Channel<TrainEffect>(Channel.BUFFERED)
    val effects: Flow<TrainEffect> = _effects.receiveAsFlow()

    private var validationJob: Job? = null

    init {
        // Active job + startEnabled.
        viewModelScope.launch {
            trainingJobRepository.observeLatest().collect { job ->
                _state.update {
                    it.copy(
                        activeJob = job,
                        startEnabled = canStart(it.handleValidation, job),
                    )
                }
                // Any job state change may have grown the corpus. Recompute per-tier.
                refreshPerTierCounts()
            }
        }

        // Corpus overview — total problems + distinct tags stream.
        viewModelScope.launch {
            combine(
                problemDao.observeTotalCount(),
                counterDao.observeDistinctTagCount(),
                handleDao.observeAll(),
            ) { total, distinctTags, handles ->
                Triple(total, distinctTags, handles.size)
            }.collect { (total, distinctTags, handleCount) ->
                _state.update {
                    it.copy(
                        corpus = it.corpus.copy(
                            totalProblems = total,
                            distinctTags = distinctTags,
                            handleCount = handleCount,
                        ),
                    )
                }
            }
        }

        // Handle history — join handle list with training job info for timestamps.
        viewModelScope.launch {
            combine(
                handleDao.observeAll(),
                trainingJobDao.observeAll(),
            ) { handles, jobs ->
                val latestByHandle = jobs.groupBy { it.handle }
                    .mapValues { (_, list) -> list.maxByOrNull { it.updatedAt } }
                handles.map { h ->
                    val latest = latestByHandle[h.handle]
                    TrainedHandle(
                        handle = h.handle,
                        lastRunAtMillis = latest?.updatedAt,
                        lastStatus = latest?.let { parseStatus(it.status) },
                        problemsAddedApprox = null,
                    )
                }.sortedByDescending { it.lastRunAtMillis ?: 0 }
            }.collect { items ->
                _state.update { it.copy(handleHistory = items) }
            }
        }
    }

    fun onIntent(intent: TrainIntent) {
        when (intent) {
            is TrainIntent.HandleChanged -> onHandleChanged(intent.value)
            TrainIntent.StartClicked -> onStartClicked()
            TrainIntent.CancelClicked -> onCancelClicked()
            is TrainIntent.ReRunHandle -> onReRun(intent.handle)
            is TrainIntent.RemoveHandle -> onRemove(intent.handle)
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
            when (val r = HandleValidator.validate(value)) {
                is HandleValidator.Result.Invalid -> {
                    updateValidation(HandleValidation.Invalid(r.reason))
                    return@launch
                }
                HandleValidator.Result.Valid -> Unit
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

    private fun onReRun(handle: String) {
        val active = _state.value.activeJob
        val idle = active == null ||
            active.status == TrainingJob.Status.SUCCESS ||
            active.status == TrainingJob.Status.FAILED
        if (!idle) {
            viewModelScope.launch { _effects.send(TrainEffect.ShowToast("Finish the current training first")) }
            return
        }
        ingestScheduler.enqueue(listOf(handle))
        viewModelScope.launch { _effects.send(TrainEffect.ShowToast("Re-syncing $handle")) }
    }

    private fun onRemove(handle: String) {
        viewModelScope.launch {
            // We don't have a per-handle DELETE on HandleDao yet — so we clear
            // the training-job trace for this handle and leave the handle row
            // untouched. The row will disappear once the user resets the corpus.
            trainingJobDao.deleteByHandle(handle)
            _effects.send(TrainEffect.ShowToast("Cleared history for $handle"))
        }
    }

    private fun canStart(validation: HandleValidation, job: TrainingJob?): Boolean {
        val validHandle = validation is HandleValidation.Valid
        val jobIdle = job == null ||
            job.status == TrainingJob.Status.SUCCESS ||
            job.status == TrainingJob.Status.FAILED
        return validHandle && jobIdle
    }

    private suspend fun refreshPerTierCounts() {
        val counts = Tier.entries.associateWith { tier ->
            runCatching { problemDao.countByTier(tier.name.lowercase()) }.getOrDefault(0)
        }
        val lastSync = runCatching { trainingJobDao.observeAll() }.getOrNull()
        _state.update {
            it.copy(corpus = it.corpus.copy(perTierCounts = counts, lastSyncAtMillis = it.activeJob?.updatedAt))
        }
    }

    private fun parseStatus(raw: String): TrainingJob.Status? = when (raw.lowercase()) {
        "queued" -> TrainingJob.Status.QUEUED
        "running" -> TrainingJob.Status.RUNNING
        "success" -> TrainingJob.Status.SUCCESS
        "failed" -> TrainingJob.Status.FAILED
        else -> null
    }
}
