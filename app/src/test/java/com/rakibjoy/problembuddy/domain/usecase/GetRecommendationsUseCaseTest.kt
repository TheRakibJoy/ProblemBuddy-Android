package com.rakibjoy.problembuddy.domain.usecase

import com.rakibjoy.problembuddy.core.database.dao.CounterDao
import com.rakibjoy.problembuddy.core.database.dao.InteractionDao
import com.rakibjoy.problembuddy.core.database.dao.ProblemDao
import com.rakibjoy.problembuddy.core.database.entity.CounterEntity
import com.rakibjoy.problembuddy.core.database.entity.InteractionEntity
import com.rakibjoy.problembuddy.core.database.entity.ProblemEntity
import com.rakibjoy.problembuddy.core.datastore.SettingsStore
import com.rakibjoy.problembuddy.domain.model.Filters
import com.rakibjoy.problembuddy.domain.model.Fresh
import com.rakibjoy.problembuddy.domain.model.Problem
import com.rakibjoy.problembuddy.domain.model.Submission
import com.rakibjoy.problembuddy.domain.model.UserInfo
import com.rakibjoy.problembuddy.domain.repository.CodeforcesRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetRecommendationsUseCaseTest {

    private lateinit var codeforces: CodeforcesRepository
    private lateinit var counterDao: CounterDao
    private lateinit var problemDao: ProblemDao
    private lateinit var interactionDao: InteractionDao
    private lateinit var settingsStore: SettingsStore
    private lateinit var computeWeakTags: ComputeWeakTagsUseCase
    private lateinit var useCase: GetRecommendationsUseCase

    private val handle = "tester"
    private val expertTier = "expert"

    // 8 expert entities. Ratings in [1400, 1600). Some already solved; one with
    // "ds" tag to be excluded; one marked "hidden" via interaction.
    private val entities = listOf(
        ProblemEntity(id = 1, tier = expertTier, contestId = 100, problemIndex = "A", rating = 1400, tags = "dp,graphs"),
        ProblemEntity(id = 2, tier = expertTier, contestId = 100, problemIndex = "B", rating = 1500, tags = "dp,math"),
        ProblemEntity(id = 3, tier = expertTier, contestId = 101, problemIndex = "A", rating = 1500, tags = "graphs"),
        ProblemEntity(id = 4, tier = expertTier, contestId = 101, problemIndex = "B", rating = 1450, tags = "dp,ds"), // excluded tag
        ProblemEntity(id = 5, tier = expertTier, contestId = 102, problemIndex = "A", rating = 1500, tags = "dp"), // solved
        ProblemEntity(id = 6, tier = expertTier, contestId = 102, problemIndex = "B", rating = 1550, tags = "graphs,math"), // solved
        ProblemEntity(id = 7, tier = expertTier, contestId = 103, problemIndex = "A", rating = 1500, tags = "dp,graphs"), // hidden
        ProblemEntity(id = 8, tier = expertTier, contestId = 104, problemIndex = "A", rating = 1420, tags = "graphs,math"),
    )

    private val counters = listOf(
        CounterEntity(id = 1, tagName = "dp", tier = expertTier, count = 50),
        CounterEntity(id = 2, tagName = "graphs", tier = expertTier, count = 40),
        CounterEntity(id = 3, tagName = "math", tier = expertTier, count = 20),
        CounterEntity(id = 4, tagName = "ds", tier = expertTier, count = 15),
    )

    @BeforeEach
    fun setUp() {
        codeforces = mockk()
        counterDao = mockk()
        problemDao = mockk()
        interactionDao = mockk()
        settingsStore = mockk()
        computeWeakTags = ComputeWeakTagsUseCase(counterDao)
        useCase = GetRecommendationsUseCase(
            codeforces = codeforces,
            computeWeakTags = computeWeakTags,
            problemDao = problemDao,
            interactionDao = interactionDao,
            settingsStore = settingsStore,
        )
    }

    private fun sub(contestId: Int, idx: String, rating: Int?, tags: List<String>, verdict: String = "OK"): Submission =
        Submission(
            id = (contestId * 1000L + idx.hashCode()),
            problem = Problem(
                contestId = contestId,
                problemIndex = idx,
                name = "",
                rating = rating,
                tags = tags,
            ),
            verdict = verdict,
            creationTimeSeconds = 0L,
        )

    @Test
    fun returnsFilteredRecommendations() = runTest {
        every { settingsStore.cfHandle } returns flowOf(handle)
        coEvery { codeforces.userInfoWithFallback(handle) } returns Result.success(
            Fresh(
                value = UserInfo(handle = handle, rating = 1500, maxRating = 1500, rank = null, maxRank = null),
                stale = false,
                fetchedAt = 0L,
            ),
        )
        // User solved (102,A) and (102,B). Also a non-expert problem that shouldn't affect tier weak tags.
        // To push "dp" and "graphs" as weakest, ensure low coverage for them.
        coEvery { codeforces.userStatusWithFallback(handle, 1, 100_000) } returns Result.success(
            Fresh(
                value = listOf(
                sub(102, "A", 1500, listOf("dp")),
                sub(102, "B", 1550, listOf("graphs", "math")),
                // Some math solves to make "math" not the weakest.
                sub(200, "A", 1500, listOf("math")),
                sub(201, "A", 1500, listOf("math")),
                sub(202, "A", 1500, listOf("math")),
                sub(203, "A", 1500, listOf("math")),
                sub(204, "A", 1500, listOf("math")),
                sub(205, "A", 1500, listOf("math")),
                sub(206, "A", 1500, listOf("math")),
                sub(207, "A", 1500, listOf("math")),
                sub(208, "A", 1500, listOf("math")),
                sub(209, "A", 1500, listOf("math")),
                // Some ds solves
                sub(300, "A", 1500, listOf("ds")),
                sub(301, "A", 1500, listOf("ds")),
                sub(302, "A", 1500, listOf("ds")),
                sub(303, "A", 1500, listOf("ds")),
                sub(304, "A", 1500, listOf("ds")),
                sub(305, "A", 1500, listOf("ds")),
                sub(306, "A", 1500, listOf("ds")),
                sub(307, "A", 1500, listOf("ds")),
                sub(308, "A", 1500, listOf("ds")),
                sub(309, "A", 1500, listOf("ds")),
                ),
                stale = false,
                fetchedAt = 0L,
            ),
        )
        coEvery { counterDao.getByTier(expertTier) } returns counters
        every { problemDao.observeByTier(expertTier) } returns flowOf(entities)
        every { interactionDao.observeAll() } returns flowOf(
            listOf(
                InteractionEntity(id = 1, problemId = 7L, status = "hidden", createdAt = 0L),
            ),
        )

        val result = useCase(
            Filters(count = 3, weakOnly = true, excludeTags = setOf("ds")),
        )

        assertTrue(result.isSuccess, "expected success, got $result")
        val problems = result.getOrThrow().problems
        assertTrue(problems.size <= 3, "size=${problems.size}")
        // Not solved
        val solvedKeys = setOf(102 to "A", 102 to "B")
        assertTrue(problems.none { (it.contestId to it.problemIndex) in solvedKeys })
        // No "ds" tag
        assertTrue(problems.none { "ds" in it.tags })
        // Not the hidden one (103,A)
        assertTrue(problems.none { it.contestId == 103 && it.problemIndex == "A" })
        // weakOnly with weak tags including "dp" or "graphs" — every result has at least one
        assertTrue(problems.all { p -> p.tags.any { it == "dp" || it == "graphs" } })
    }

    @Test
    fun noHandle_returnsFailure() = runTest {
        every { settingsStore.cfHandle } returns flowOf(null)

        val result = useCase(Filters())

        assertTrue(result.isFailure)
        val err = result.exceptionOrNull()
        assertNotNull(err)
        assertTrue(err is IllegalStateException)
    }

    @Test
    fun userInfoFailure_propagates() = runTest {
        every { settingsStore.cfHandle } returns flowOf(handle)
        val boom = RuntimeException("cf down")
        coEvery { codeforces.userInfoWithFallback(handle) } returns Result.failure(boom)

        val result = useCase(Filters())

        assertTrue(result.isFailure)
        assertEquals(boom, result.exceptionOrNull())
    }
}
