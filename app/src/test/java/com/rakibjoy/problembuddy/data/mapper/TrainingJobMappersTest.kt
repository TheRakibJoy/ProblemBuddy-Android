package com.rakibjoy.problembuddy.data.mapper

import com.rakibjoy.problembuddy.core.database.entity.TrainingJobEntity
import com.rakibjoy.problembuddy.domain.model.Tier
import com.rakibjoy.problembuddy.domain.model.TrainingJob
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class TrainingJobMappersTest {

    @ParameterizedTest
    @EnumSource(TrainingJob.Status::class)
    fun `status round trips`(status: TrainingJob.Status) {
        val job = TrainingJob(
            id = 7,
            handle = "tourist",
            status = status,
            currentTier = Tier.EXPERT,
            done = 5,
            total = 10,
            error = null,
            updatedAt = 1_000L,
        )
        val roundTripped = job.toEntity().toDomain()
        assertEquals(status, roundTripped.status)
    }

    @Test
    fun `empty tier string maps to null`() {
        val e = TrainingJobEntity(id = 1, handle = "h", status = "running", currentTier = "", done = 0, total = 0, error = "", updatedAt = 0)
        assertNull(e.toDomain().currentTier)
    }

    @Test
    fun `tier name maps to enum`() {
        val e = TrainingJobEntity(id = 1, handle = "h", status = "running", currentTier = "MASTER", done = 0, total = 0, error = "", updatedAt = 0)
        assertEquals(Tier.MASTER, e.toDomain().currentTier)
    }

    @Test
    fun `unknown tier string maps to null`() {
        val e = TrainingJobEntity(id = 1, handle = "h", status = "running", currentTier = "WIZARD", done = 0, total = 0, error = "", updatedAt = 0)
        assertNull(e.toDomain().currentTier)
    }

    @Test
    fun `empty error string maps to null in domain`() {
        val e = TrainingJobEntity(id = 1, handle = "h", status = "failed", currentTier = "", done = 0, total = 0, error = "", updatedAt = 0)
        assertNull(e.toDomain().error)
    }

    @Test
    fun `null error in domain maps to empty string in entity`() {
        val j = TrainingJob(id = 1, handle = "h", status = TrainingJob.Status.SUCCESS, currentTier = null, done = 0, total = 0, error = null, updatedAt = 0)
        assertEquals("", j.toEntity().error)
    }

    @Test
    fun `unknown status string defaults to QUEUED`() {
        val e = TrainingJobEntity(id = 1, handle = "h", status = "bogus", currentTier = "", done = 0, total = 0, error = "", updatedAt = 0)
        assertEquals(TrainingJob.Status.QUEUED, e.toDomain().status)
    }
}
