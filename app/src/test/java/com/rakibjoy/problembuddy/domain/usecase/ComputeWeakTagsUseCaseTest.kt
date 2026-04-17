package com.rakibjoy.problembuddy.domain.usecase

import com.rakibjoy.problembuddy.core.database.dao.CounterDao
import com.rakibjoy.problembuddy.core.database.entity.CounterEntity
import com.rakibjoy.problembuddy.domain.model.Tier
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ComputeWeakTagsUseCaseTest {

    private lateinit var counterDao: CounterDao
    private lateinit var useCase: ComputeWeakTagsUseCase

    private val expertTier = "expert"

    private val fixture = listOf(
        CounterEntity(id = 1, tagName = "dp", tier = expertTier, count = 50),
        CounterEntity(id = 2, tagName = "graphs", tier = expertTier, count = 30),
        CounterEntity(id = 3, tagName = "math", tier = expertTier, count = 20),
        CounterEntity(id = 4, tagName = "strings", tier = expertTier, count = 10),
        CounterEntity(id = 5, tagName = "geometry", tier = expertTier, count = 3),
    )

    @BeforeEach
    fun setUp() {
        counterDao = mockk()
        useCase = ComputeWeakTagsUseCase(counterDao)
    }

    @Test
    fun weakTags_excludesLowCorpusTags() = runTest {
        coEvery { counterDao.getByTier(expertTier) } returns fixture

        val result = useCase(
            tier = Tier.EXPERT,
            solvedTagCounts = emptyMap(),
            topN = 10,
        )

        assertEquals(4, result.size)
        assertTrue(result.containsAll(listOf("dp", "graphs", "math", "strings")))
        assertTrue(!result.contains("geometry"))
    }

    @Test
    fun weakTags_sortsByCoverageAscending() = runTest {
        coEvery { counterDao.getByTier(expertTier) } returns fixture

        val result = useCase(
            tier = Tier.EXPERT,
            solvedTagCounts = mapOf(
                "dp" to 40,      // 0.80
                "graphs" to 5,   // 0.1667
                "math" to 10,    // 0.50
                "strings" to 5,  // 0.50
            ),
            topN = 10,
        )

        // graphs (lowest coverage), then math/strings tied at 0.5 — math wins via
        // higher corpus count — and finally dp at 0.8.
        assertEquals(listOf("graphs", "math", "strings", "dp"), result)
    }

    @Test
    fun weakTags_respectsTopN() = runTest {
        coEvery { counterDao.getByTier(expertTier) } returns fixture

        val result = useCase(
            tier = Tier.EXPERT,
            solvedTagCounts = emptyMap(),
            topN = 2,
        )

        assertEquals(2, result.size)
    }

    @Test
    fun weakTags_emptyCorpus_returnsEmpty() = runTest {
        coEvery { counterDao.getByTier(expertTier) } returns emptyList()

        val result = useCase(
            tier = Tier.EXPERT,
            solvedTagCounts = mapOf("dp" to 5),
            topN = 10,
        )

        assertEquals(emptyList<String>(), result)
    }
}
