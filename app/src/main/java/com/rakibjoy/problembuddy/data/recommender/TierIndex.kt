package com.rakibjoy.problembuddy.data.recommender

import com.rakibjoy.problembuddy.domain.model.Problem
import kotlin.math.sqrt

/**
 * Bag-of-words tag index for cosine-similarity ranking of problems against
 * a list of weak tags. Pure Kotlin (no Android/DI dependencies); intended to
 * be cached at a higher level per (tier, corpus version).
 *
 * Algorithm mirrors `app/Implementation Plan.md` §6.
 */
class TierIndex(
    val problems: List<Problem>,
    val vocabulary: Map<String, Int>,      // tag -> column
    val vectors: Array<IntArray>,          // row per problem
) {
    /**
     * Returns indices into [problems] sorted by descending cosine similarity
     * between the query vector (built from [weakTags]) and each problem's
     * tag vector. If the query vector has zero norm (no known weak tags in
     * the vocabulary), returns an empty list. Rows with zero norm score 0.0.
     */
    fun rank(weakTags: List<String>): List<Int> {
        val query = IntArray(vocabulary.size)
        for (tag in weakTags) vocabulary[tag]?.let { query[it]++ }
        val qNorm = query.norm().takeIf { it > 0 } ?: return emptyList()
        return vectors.indices.sortedByDescending { i ->
            val row = vectors[i]
            val rNorm = row.norm()
            if (rNorm == 0.0) 0.0 else dot(query, row) / (qNorm * rNorm)
        }
    }

    companion object {
        fun build(problems: List<Problem>): TierIndex {
            val allTags = problems.flatMap { it.tags }.toSortedSet()
            val vocabulary: Map<String, Int> =
                allTags.withIndex().associate { (i, tag) -> tag to i }
            val width = vocabulary.size
            val vectors = Array(problems.size) { i ->
                val row = IntArray(width)
                for (tag in problems[i].tags) {
                    vocabulary[tag]?.let { col -> row[col] = 1 }
                }
                row
            }
            return TierIndex(problems, vocabulary, vectors)
        }
    }
}

private fun IntArray.norm(): Double = sqrt(sumOf { (it * it).toDouble() })

private fun dot(a: IntArray, b: IntArray): Int {
    var s = 0
    for (i in a.indices) s += a[i] * b[i]
    return s
}
