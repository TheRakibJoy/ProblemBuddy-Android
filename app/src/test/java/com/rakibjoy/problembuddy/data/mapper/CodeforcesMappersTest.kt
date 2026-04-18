package com.rakibjoy.problembuddy.data.mapper

import com.rakibjoy.problembuddy.core.network.dto.ContestDto
import com.rakibjoy.problembuddy.core.network.dto.ProblemDto
import com.rakibjoy.problembuddy.core.network.dto.RatingChangeDto
import com.rakibjoy.problembuddy.core.network.dto.SubmissionDto
import com.rakibjoy.problembuddy.core.network.dto.UserInfoDto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class CodeforcesMappersTest {

    @Test
    fun `UserInfoDto maps every field`() {
        val dto = UserInfoDto(
            handle = "tourist",
            rating = 3700,
            maxRating = 3800,
            rank = "legendary grandmaster",
            maxRank = "legendary grandmaster",
        )
        val domain = dto.toDomain()
        assertEquals("tourist", domain.handle)
        assertEquals(3700, domain.rating)
        assertEquals(3800, domain.maxRating)
        assertEquals("legendary grandmaster", domain.rank)
        assertEquals("legendary grandmaster", domain.maxRank)
    }

    @Test
    fun `UserInfoDto with null rating maps through as null`() {
        val dto = UserInfoDto(handle = "newbie")
        val domain = dto.toDomain()
        assertEquals("newbie", domain.handle)
        assertNull(domain.rating)
        assertNull(domain.maxRating)
        assertNull(domain.rank)
        assertNull(domain.maxRank)
        assertNull(domain.avatarUrl)
        assertNull(domain.titlePhotoUrl)
    }

    @Test
    fun `UserInfoDto protocol-relative avatar URLs get https prefix`() {
        val dto = UserInfoDto(
            handle = "tourist",
            avatar = "//userpic.codeforces.org/no-avatar.jpg",
            titlePhoto = "//userpic.codeforces.org/tourist/title/x.jpg",
        )
        val domain = dto.toDomain()
        assertEquals("https://userpic.codeforces.org/no-avatar.jpg", domain.avatarUrl)
        assertEquals("https://userpic.codeforces.org/tourist/title/x.jpg", domain.titlePhotoUrl)
    }

    @Test
    fun `UserInfoDto http avatar URLs upgraded to https`() {
        val dto = UserInfoDto(handle = "h", avatar = "http://example.com/a.jpg")
        assertEquals("https://example.com/a.jpg", dto.toDomain().avatarUrl)
    }

    @Test
    fun `ProblemDto maps every field and preserves tag list as-is`() {
        val dto = ProblemDto(
            contestId = 1234,
            index = "B1",
            name = "Some Problem",
            rating = 1600,
            tags = listOf("dp", "graphs", "math"),
        )
        val domain = dto.toDomain()
        assertEquals(1234, domain.contestId)
        assertEquals("B1", domain.problemIndex)
        assertEquals("Some Problem", domain.name)
        assertEquals(1600, domain.rating)
        assertEquals(listOf("dp", "graphs", "math"), domain.tags)
    }

    @Test
    fun `ProblemDto with null contestId defaults to 0 and null rating passes through`() {
        val dto = ProblemDto(
            contestId = null,
            index = "A",
            name = "Gym Problem",
            rating = null,
            tags = emptyList(),
        )
        val domain = dto.toDomain()
        assertEquals(0, domain.contestId)
        assertNull(domain.rating)
        assertEquals(emptyList<String>(), domain.tags)
    }

    @Test
    fun `SubmissionDto maps verdict passthrough and nested problem`() {
        val dto = SubmissionDto(
            id = 42L,
            problem = ProblemDto(
                contestId = 1500,
                index = "C",
                name = "Nested",
                rating = 2100,
                tags = listOf("dp"),
            ),
            verdict = "OK",
            creationTimeSeconds = 1_700_000_000L,
        )
        val domain = dto.toDomain()
        assertEquals(42L, domain.id)
        assertEquals("OK", domain.verdict)
        assertEquals(1_700_000_000L, domain.creationTimeSeconds)
        assertEquals(1500, domain.problem.contestId)
        assertEquals("C", domain.problem.problemIndex)
        assertEquals(listOf("dp"), domain.problem.tags)
    }

    @Test
    fun `SubmissionDto with null verdict maps to null`() {
        val dto = SubmissionDto(
            id = 7L,
            problem = ProblemDto(contestId = 1, index = "A", name = "x"),
            verdict = null,
            creationTimeSeconds = 0L,
        )
        val domain = dto.toDomain()
        assertNull(domain.verdict)
    }

    @Test
    fun `RatingChangeDto maps every field`() {
        val dto = RatingChangeDto(
            contestId = 999,
            contestName = "Round #999",
            handle = "tourist",
            rank = 3,
            ratingUpdateTimeSeconds = 1_600_000_000L,
            oldRating = 3500,
            newRating = 3600,
        )
        val domain = dto.toDomain()
        assertEquals(999, domain.contestId)
        assertEquals("Round #999", domain.contestName)
        assertEquals("tourist", domain.handle)
        assertEquals(3, domain.rank)
        assertEquals(1_600_000_000L, domain.ratingUpdateTimeSeconds)
        assertEquals(3500, domain.oldRating)
        assertEquals(3600, domain.newRating)
    }

    @Test
    fun `ContestDto phase BEFORE maps to UpcomingContest with parsed division`() {
        val dto = ContestDto(
            id = 1234,
            name = "Codeforces Round 999 (Div. 2)",
            phase = "BEFORE",
            durationSeconds = 7200L,
            startTimeSeconds = 1_700_000_000L,
        )
        val upcoming = dto.toUpcoming()
        assertNotNull(upcoming)
        assertEquals(1234, upcoming!!.id)
        assertEquals(1_700_000_000L, upcoming.startTimeSeconds)
        assertEquals("Div 2", upcoming.division)
    }

    @Test
    fun `ContestDto phase FINISHED returns null`() {
        val dto = ContestDto(
            id = 999,
            name = "Codeforces Round 1 (Div. 1)",
            phase = "FINISHED",
            durationSeconds = 7200L,
            startTimeSeconds = 1_500_000_000L,
        )
        assertNull(dto.toUpcoming())
    }
}
