package com.rakibjoy.problembuddy.feature.recommend

import com.rakibjoy.problembuddy.domain.model.Filters
import com.rakibjoy.problembuddy.domain.model.Problem

data class RecommendState(
    val loading: Boolean = true,
    val problems: List<Problem> = emptyList(),
    val filters: Filters = Filters(),
    val error: String? = null,
    val filterSheetOpen: Boolean = false,
    val hasCorpus: Boolean = true,
    val stale: Boolean = false,
    val fetchedAtMillis: Long? = null,
    val weakTags: Set<String> = emptySet(),
    val totalMatching: Int? = null,
    /** Problem keys (contestId-index) the user has solved in this session. */
    val solvedKeys: Set<String> = emptySet(),
    /** Problem keys the user has skipped / saved for later. */
    val skippedKeys: Set<String> = emptySet(),
)
