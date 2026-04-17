package com.rakibjoy.problembuddy.domain.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class HandleValidatorTest {

    @Test
    fun `too short returns Invalid with reason`() {
        val result = HandleValidator.validate("ab")
        assertTrue(result is HandleValidator.Result.Invalid)
        assertEquals("Too short", (result as HandleValidator.Result.Invalid).reason)
    }

    @Test
    fun `exact 3 characters is valid`() {
        assertEquals(HandleValidator.Result.Valid, HandleValidator.validate("abc"))
    }

    @Test
    fun `long valid handle is accepted`() {
        val handle = "a".repeat(128)
        assertEquals(HandleValidator.Result.Valid, HandleValidator.validate(handle))
    }

    @Test
    fun `valid characters produce Valid`() {
        assertEquals(HandleValidator.Result.Valid, HandleValidator.validate("Tourist_99"))
        assertEquals(HandleValidator.Result.Valid, HandleValidator.validate("user.name"))
        assertEquals(HandleValidator.Result.Valid, HandleValidator.validate("my-handle"))
        assertEquals(HandleValidator.Result.Valid, HandleValidator.validate("ABC123"))
    }

    @Test
    fun `at sign is invalid`() {
        val result = HandleValidator.validate("foo@bar")
        assertTrue(result is HandleValidator.Result.Invalid)
        assertEquals("Invalid characters", (result as HandleValidator.Result.Invalid).reason)
    }

    @Test
    fun `space in middle is invalid`() {
        val result = HandleValidator.validate("foo bar")
        assertTrue(result is HandleValidator.Result.Invalid)
        assertEquals("Invalid characters", (result as HandleValidator.Result.Invalid).reason)
    }

    @Test
    fun `bang is invalid`() {
        val result = HandleValidator.validate("foo!")
        assertTrue(result is HandleValidator.Result.Invalid)
        assertEquals("Invalid characters", (result as HandleValidator.Result.Invalid).reason)
    }

    @Test
    fun `leading and trailing whitespace is trimmed before validation`() {
        assertEquals(HandleValidator.Result.Valid, HandleValidator.validate("  tourist  "))
        assertEquals(HandleValidator.Result.Valid, HandleValidator.validate("\ttourist\n"))
    }

    @Test
    fun `whitespace only input is too short`() {
        val result = HandleValidator.validate("   ")
        assertTrue(result is HandleValidator.Result.Invalid)
        assertEquals("Too short", (result as HandleValidator.Result.Invalid).reason)
    }

    @Test
    fun `empty input is too short`() {
        val result = HandleValidator.validate("")
        assertTrue(result is HandleValidator.Result.Invalid)
        assertEquals("Too short", (result as HandleValidator.Result.Invalid).reason)
    }
}
