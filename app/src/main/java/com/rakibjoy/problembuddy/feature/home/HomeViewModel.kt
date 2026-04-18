package com.rakibjoy.problembuddy.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rakibjoy.problembuddy.core.database.dao.ProblemDao
import com.rakibjoy.problembuddy.core.datastore.SettingsStore
import com.rakibjoy.problembuddy.domain.model.Tier
import com.rakibjoy.problembuddy.domain.repository.CodeforcesRepository
import com.rakibjoy.problembuddy.domain.repository.TrainingJobRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val settingsStore: SettingsStore,
    private val codeforces: CodeforcesRepository,
    private val trainingJobRepository: TrainingJobRepository,
    private val problemDao: ProblemDao,
) : ViewModel() {

    // TODO(redesign): wire real data for ratingDelta, problemsSolved, streakDays,
    //   weakTagTrend, upsolve, todayPicks. Defaults (null / empty list) render
    //   an acceptable empty state today.
    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private val _effects = Channel<HomeEffect>(Channel.BUFFERED)
    val effects: Flow<HomeEffect> = _effects.receiveAsFlow()

    private var lastHandleFetched: String? = null

    init {
        viewModelScope.launch {
            combine(
                settingsStore.cfHandle.distinctUntilChanged(),
                trainingJobRepository.observeLatest(),
            ) { handle, job -> handle to job }
                .collect { (handle, job) ->
                    val greeting = if (handle != null) "Welcome back, $handle" else "Welcome"
                    _state.update {
                        it.copy(
                            handle = handle,
                            greeting = greeting,
                            latestJob = job,
                            // keep existing rating if handle unchanged; clear if handle removed
                            rating = if (handle == null) null else it.rating,
                            maxRating = if (handle == null) null else it.maxRating,
                        )
                    }
                    refreshCorpus()
                    if (handle != null && handle != lastHandleFetched) {
                        lastHandleFetched = handle
                        fetchUserInfo(handle)
                    } else if (handle == null) {
                        lastHandleFetched = null
                    }
                }
        }
    }

    fun onIntent(intent: HomeIntent) {
        when (intent) {
            HomeIntent.RecommendClicked -> onRecommend()
            HomeIntent.TrainClicked -> emit(HomeEffect.NavigateToTrain)
            HomeIntent.ProfileClicked -> emit(HomeEffect.NavigateToProfile)
            HomeIntent.SettingsClicked -> emit(HomeEffect.NavigateToSettings)
        }
    }

    private fun onRecommend() {
        if (!_state.value.hasCorpus) {
            emit(HomeEffect.ShowToast("Run training first"))
        } else {
            emit(HomeEffect.NavigateToRecommend)
        }
    }

    private fun emit(effect: HomeEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }

    private fun fetchUserInfo(handle: String) {
        viewModelScope.launch {
            codeforces.userInfo(handle).onSuccess { info ->
                _state.update {
                    if (it.handle == handle) {
                        it.copy(
                            rating = info.rating,
                            maxRating = info.maxRating,
                            avatarUrl = info.titlePhotoUrl ?: info.avatarUrl,
                        )
                    } else it
                }
            }
        }
    }

    private fun refreshCorpus() {
        viewModelScope.launch {
            val total = runCatching {
                Tier.entries.sumOf { problemDao.countByTier(it.name.lowercase()) }
            }.getOrDefault(0)
            _state.update { it.copy(hasCorpus = total > 0) }
        }
    }
}
