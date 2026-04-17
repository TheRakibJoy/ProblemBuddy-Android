package com.rakibjoy.problembuddy.data.recommender

import com.rakibjoy.problembuddy.domain.model.Problem
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TierIndexTest {

    private fun problem(id: Int, tags: List<String>): Problem =
        Problem(
            contestId = id,
            problemIndex = "A",
            name = "P$id",
            rating = 1200,
            tags = tags,
        )

    private val p1 = problem(1, listOf("dp", "graphs"))
    private val p2 = problem(2, listOf("dp", "math"))
    private val p3 = problem(3, listOf("graphs", "trees"))
    private val p4 = problem(4, listOf("strings"))
    private val fixture = listOf(p1, p2, p3, p4)

    @Test
    fun rank_weakDp_prioritizesDpProblems() {
        val index = TierIndex.build(fixture)
        val ranking = index.rank(listOf("dp"))

        // p1 and p2 should come before p3; p4 (no "dp", no "strings" in query) last.
        val rankOf = ranking.withIndex().associate { (pos, idx) -> idx to pos }
        assertTrue(rankOf.getValue(0) < rankOf.getValue(2)) // p1 before p3
        assertTrue(rankOf.getValue(1) < rankOf.getValue(2)) // p2 before p3
        assertTrue(rankOf.getValue(0) < rankOf.getValue(3)) // p1 before p4
        assertTrue(rankOf.getValue(1) < rankOf.getValue(3)) // p2 before p4
        // p4 has no overlapping tags with query -> score 0, ranked last.
        assertEquals(3, rankOf.getValue(3))
    }

    @Test
    fun rank_emptyWeakTags_returnsEmpty() {
        val index = TierIndex.build(fixture)
        assertEquals(emptyList<Int>(), index.rank(emptyList()))
    }

    @Test
    fun rank_unknownWeakTag_returnsEmpty() {
        val index = TierIndex.build(fixture)
        assertEquals(emptyList<Int>(), index.rank(listOf("unknown_tag")))
    }

    @Test
    fun rank_multipleWeakTags_ordersByCombinedSimilarity() {
        val index = TierIndex.build(fixture)
        val ranking = index.rank(listOf("dp", "graphs"))
        // p1 has both "dp" and "graphs" — should rank first.
        assertEquals(0, ranking.first())
    }

    @Test
    fun build_vocabularyIsDeterministic() {
        val a = TierIndex.build(fixture)
        val b = TierIndex.build(fixture)
        assertEquals(a.vocabulary, b.vocabulary)
        // Entry iteration order should match too (sorted alphabetically).
        assertEquals(a.vocabulary.entries.toList(), b.vocabulary.entries.toList())
    }
}
