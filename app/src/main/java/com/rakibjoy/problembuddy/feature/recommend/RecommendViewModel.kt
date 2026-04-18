package com.rakibjoy.problembuddy.feature.recommend

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rakibjoy.problembuddy.core.datastore.SettingsStore
import com.rakibjoy.problembuddy.domain.model.Interaction
import com.rakibjoy.problembuddy.domain.model.Problem
import com.rakibjoy.problembuddy.domain.model.Tier
import com.rakibjoy.problembuddy.domain.repository.InteractionRepository
import com.rakibjoy.problembuddy.domain.repository.ProblemRepository
import com.rakibjoy.problembuddy.domain.usecase.GetRecommendationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecommendViewModel @Inject constructor(
    private val getRecommendations: GetRecommendationsUseCase,
    private val interactionRepository: InteractionRepository,
    private val problemRepository: ProblemRepository,
    private val settingsStore: SettingsStore,
) : ViewModel() {

    private val _state = MutableStateFlow(RecommendState())
    val state: StateFlow<RecommendState> = _state.asStateFlow()

    private val _effects = Channel<RecommendEffect>(Channel.BUFFERED)
    val effects: Flow<RecommendEffect> = _effects.receiveAsFlow()

    init {
        viewModelScope.launch {
            val recsPerLoad = runCatching { settingsStore.recsPerLoad.first() }.getOrDefault(10)
            val hasCorpus = runCatching {
                Tier.entries.sumOf { problemRepository.countByTier(it) } > 0
            }.getOrDefault(true)
            _state.update {
                it.copy(
                    filters = it.filters.copy(count = recsPerLoad),
                    hasCorpus = hasCorpus,
                )
            }
            refresh()
        }
    }

    fun onIntent(intent: RecommendIntent) {
        when (intent) {
            RecommendIntent.Refresh -> viewModelScope.launch { refresh() }
            RecommendIntent.OpenFilters -> _state.update { it.copy(filterSheetOpen = true) }
            RecommendIntent.CloseFilters -> _state.update { it.copy(filterSheetOpen = false) }
            is RecommendIntent.ApplyFilters -> viewModelScope.launch {
                _state.update { it.copy(filters = intent.filters, filterSheetOpen = false) }
                refresh()
            }
            is RecommendIntent.MarkSolved -> viewModelScope.launch {
                recordInteraction(intent.problem, status = Interaction.Status.SOLVED)
                _state.update { it.copy(problems = it.problems.filterNot { p -> p.sameAs(intent.problem) }) }
                _effects.send(RecommendEffect.Toast("Marked solved"))
            }
            is RecommendIntent.Skip -> viewModelScope.launch {
                recordInteraction(intent.problem, status = Interaction.Status.NOT_INTERESTED)
                _state.update { it.copy(problems = it.problems.filterNot { p -> p.sameAs(intent.problem) }) }
                _effects.send(RecommendEffect.Toast("Skipped"))
            }
            is RecommendIntent.OpenUrl -> viewModelScope.launch {
                val url = "https://codeforces.com/problemset/problem/${intent.problem.contestId}/${intent.problem.problemIndex}"
                _effects.send(RecommendEffect.OpenUrl(url))
            }
        }
    }

    private suspend fun refresh() {
        _state.update { it.copy(loading = true, error = null) }
        getRecommendations(_state.value.filters).fold(
            onSuccess = { result ->
                _state.update {
                    it.copy(
                        loading = false,
                        problems = result.problems,
                        stale = result.stale,
                        error = null,
                    )
                }
            },
            onFailure = { e ->
                _state.update { it.copy(loading = false, error = e.message) }
                _effects.send(RecommendEffect.Toast("Couldn't load: ${e.message}"))
            },
        )
    }

    private suspend fun recordInteraction(problem: Problem, status: Interaction.Status) {
        val id = problemRepository.findId(problem.contestId, problem.problemIndex) ?: return
        interactionRepository.upsert(
            Interaction(
                problemId = id,
                status = status,
                createdAt = System.currentTimeMillis(),
            ),
        )
    }

    private fun Problem.sameAs(other: Problem): Boolean =
        contestId == other.contestId && problemIndex == other.problemIndex
}
