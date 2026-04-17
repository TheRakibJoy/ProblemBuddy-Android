package com.rakibjoy.problembuddy.data.mapper

import com.rakibjoy.problembuddy.core.database.entity.ProblemEntity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ProblemMappersTest {

    private fun entity(tags: String, rating: Int? = 1500) = ProblemEntity(
        id = 0,
        tier = "expert",
        contestId = 1,
        problemIndex = "A",
        rating = rating,
        tags = tags,
    )

    @Test
    fun `splits comma separated tags`() {
        assertEquals(listOf("dp", "graphs", "math"), entity("dp,graphs,math").toDomain().tags)
    }

    @Test
    fun `trims whitespace around tags`() {
        assertEquals(listOf("dp", "graphs", "math"), entity("dp, graphs ,math").toDomain().tags)
    }

    @Test
    fun `empty tag string yields empty list`() {
        assertEquals(emptyList<String>(), entity("").toDomain().tags)
    }

    @Test
    fun `single tag`() {
        assertEquals(listOf("dp"), entity("dp").toDomain().tags)
    }

    @Test
    fun `passes rating null through`() {
        val p = entity(tags = "dp", rating = null).toDomain()
        assertEquals(null, p.rating)
    }

    @Test
    fun `contestId and problemIndex preserved`() {
        val e = ProblemEntity(id = 0, tier = "expert", contestId = 1234, problemIndex = "B2", rating = 1800, tags = "dp")
        val d = e.toDomain()
        assertEquals(1234, d.contestId)
        assertEquals("B2", d.problemIndex)
    }
}
