package com.rakibjoy.problembuddy.feature.train

import com.rakibjoy.problembuddy.domain.model.Tier
import com.rakibjoy.problembuddy.domain.model.TrainingJob

data class TrainState(
    val handleInput: String = "",
    val handleValidation: HandleValidation = HandleValidation.Idle,
    val activeJob: TrainingJob? = null,
    val startEnabled: Boolean = false,
    val corpus: CorpusOverview = CorpusOverview.Empty,
    val handleHistory: List<TrainedHandle> = emptyList(),
)

sealed interface HandleValidation {
    data object Idle : HandleValidation
    data object Validating : HandleValidation
    data object Valid : HandleValidation
    data class Invalid(val reason: String) : HandleValidation
}

/** Summary for the "corpus overview" card at the top of Train. */
data class CorpusOverview(
    val totalProblems: Int,
    val distinctTags: Int,
    val handleCount: Int,
    val perTierCounts: Map<Tier, Int>,
    val lastSyncAtMillis: Long?,
) {
    companion object {
        val Empty = CorpusOverview(
            totalProblems = 0,
            distinctTags = 0,
            handleCount = 0,
            perTierCounts = emptyMap(),
            lastSyncAtMillis = null,
        )
    }
}

/** Row in the "handle history" list on Train. */
data class TrainedHandle(
    val handle: String,
    val lastRunAtMillis: Long?,
    val lastStatus: TrainingJob.Status?,
    val problemsAddedApprox: Int?,   // currently null — we don't record per-handle deltas yet
)
