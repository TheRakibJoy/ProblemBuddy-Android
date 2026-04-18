package com.rakibjoy.problembuddy.domain.usecase

import com.rakibjoy.problembuddy.core.datastore.SettingsStore
import com.rakibjoy.problembuddy.domain.model.Filters
import com.rakibjoy.problembuddy.domain.model.Fresh
import com.rakibjoy.problembuddy.domain.model.Interaction
import com.rakibjoy.problembuddy.domain.model.Problem
import com.rakibjoy.problembuddy.domain.model.Submission
import com.rakibjoy.problembuddy.domain.model.TagCounter
import com.rakibjoy.problembuddy.domain.model.Tier
import com.rakibjoy.problembuddy.domain.model.UserInfo
import com.rakibjoy.problembuddy.domain.repository.CodeforcesRepository
import com.rakibjoy.problembuddy.domain.repository.CounterRepository
import com.rakibjoy.problembuddy.domain.repository.InteractionRepository
import com.rakibjoy.problembuddy.domain.repository.ProblemRepository
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
    private lateinit var counterRepository: CounterRepository
    private lateinit var problemRepository: ProblemRepository
    private lateinit var interactionRepository: InteractionRepository
    private lateinit var settingsStore: SettingsStore
    private lateinit var computeWeakTags: ComputeWeakTagsUseCase
    private lateinit var useCase: GetRecommendationsUseCase

    private val handle = "tester"
    private val specialistTier = Tier.SPECIALIST

    // 8 specialist problems. Ratings in [1400, 1600). Some already solved; one with
    // "ds" tag to be excluded; one marked "hidden" via interaction.
    // IDs 1-8 map to their index in the list + 1.
    private val problems = listOf(
        Problem(contestId = 100, problemIndex = "A", name = "", rating = 1400, tags = listOf("dp", "graphs")),
        Problem(contestId = 100, problemIndex = "B", name = "", rating = 1500, tags = listOf("dp", "math")),
        Problem(contestId = 101, problemIndex = "A", name = "", rating = 1500, tags = listOf("graphs")),
        Problem(contestId = 101, problemIndex = "B", name = "", rating = 1450, tags = listOf("dp", "ds")), // excluded tag
        Problem(contestId = 102, problemIndex = "A", name = "", rating = 1500, tags = listOf("dp")), // solved
        Problem(contestId = 102, problemIndex = "B", name = "", rating = 1550, tags = listOf("graphs", "math")), // solved
        Problem(contestId = 103, problemIndex = "A", name = "", rating = 1500, tags = listOf("dp", "graphs")), // hidden
        Problem(contestId = 104, problemIndex = "A", name = "", rating = 1420, tags = listOf("graphs", "math")),
    )

    private val counters = listOf(
        TagCounter(tagName = "dp", tier = specialistTier, count = 50),
        TagCounter(tagName = "graphs", tier = specialistTier, count = 40),
        TagCounter(tagName = "math", tier = specialistTier, count = 20),
        TagCounter(tagName = "ds", tier = specialistTier, count = 15),
    )

    @BeforeEach
    fun setUp() {
        codeforces = mockk()
        counterRepository = mockk()
        problemRepository = mockk()
        interactionRepository = mockk()
        settingsStore = mockk()
        computeWeakTags = ComputeWeakTagsUseCase(counterRepository)
        useCase = GetRecommendationsUseCase(
            codeforces = codeforces,
            computeWeakTags = computeWeakTags,
            problemRepository = problemRepository,
            interactionRepository = interactionRepository,
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
        every { settingsStore.difficultyOffset } returns flowOf(0)
        coEvery { codeforces.userInfoWithFallback(handle) } returns Result.success(
            Fresh(
                value = UserInfo(handle = handle, rating = 1500, maxRating = 1500, rank = null, maxRank = null),
                stale = false,
                fetchedAt = 0L,
            ),
        )
        // User solved (102,A) and (102,B). Math and ds solves keep those tags from
        // being flagged weak, leaving "dp" and "graphs" as the weakest.
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
        coEvery { counterRepository.getByTier(specialistTier) } returns counters
        every { problemRepository.observeByTier(specialistTier) } returns flowOf(problems)
        coEvery { interactionRepository.getAll() } returns listOf(
            Interaction(problemId = 7L, status = Interaction.Status.HIDDEN, createdAt = 0L),
        )
        // Entity id 7 maps to problems[6] = (103, "A").
        coEvery { problemRepository.resolveKeys(setOf(7L)) } returns mapOf(7L to (103 to "A"))

        val result = useCase(
            Filters(count = 3, weakOnly = true, excludeTags = setOf("ds")),
        )

        assertTrue(result.isSuccess, "expected success, got $result")
        val returned = result.getOrThrow().problems
        assertTrue(returned.size <= 3, "size=${returned.size}")
        // Not solved
        val solvedKeys = setOf(102 to "A", 102 to "B")
        assertTrue(returned.none { (it.contestId to it.problemIndex) in solvedKeys })
        // No "ds" tag
        assertTrue(returned.none { "ds" in it.tags })
        // Not the hidden one (103,A)
        assertTrue(returned.none { it.contestId == 103 && it.problemIndex == "A" })
        // weakOnly with weak tags including "dp" or "graphs" — every result has at least one
        assertTrue(returned.all { p -> p.tags.any { it == "dp" || it == "graphs" } })
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
