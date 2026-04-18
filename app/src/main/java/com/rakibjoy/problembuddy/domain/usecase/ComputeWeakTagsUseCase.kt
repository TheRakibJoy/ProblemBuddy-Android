package com.rakibjoy.problembuddy.domain.usecase

import com.rakibjoy.problembuddy.domain.model.Tier
import com.rakibjoy.problembuddy.domain.repository.CounterRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Computes the "weakest" tags for the given tier by comparing a user's
 * solved-per-tag counts against the reference corpus counters.
 *
 * Algorithm:
 *  1. Load corpus counters for the tier.
 *  2. For every tag whose corpus count is >= [minCorpusCount], compute the
 *     coverage ratio `solvedCount / corpusCount`.
 *  3. Sort ascending by coverage; break ties by descending corpus count so
 *     weaker tags with a larger corpus surface first.
 *  4. Return the top [topN] tag names.
 */
class ComputeWeakTagsUseCase @Inject constructor(
    private val counterRepository: CounterRepository,
) {
    suspend operator fun invoke(
        tier: Tier,
        solvedTagCounts: Map<String, Int>,
        topN: Int = 10,
        minCorpusCount: Int = 5,
    ): List<String> = withContext(Dispatchers.IO) {
        val counters = counterRepository.getByTier(tier)
        if (counters.isEmpty()) return@withContext emptyList()

        val corpus: Map<String, Int> = counters.associate { it.tagName to it.count }

        corpus.asSequence()
            .filter { (_, corpusCount) -> corpusCount >= minCorpusCount }
            .map { (tag, corpusCount) ->
                val solved = solvedTagCounts[tag] ?: 0
                val coverage = solved.toDouble() / corpusCount.toDouble()
                Triple(tag, coverage, corpusCount)
            }
            .sortedWith(
                compareBy<Triple<String, Double, Int>> { it.second }
                    .thenByDescending { it.third },
            )
            .take(topN)
            .map { it.first }
            .toList()
    }
}
