package com.rakibjoy.problembuddy.domain.util

object HandleValidator {
    private val PATTERN = Regex("^[A-Za-z0-9_.-]+$")

    sealed interface Result {
        data object Valid : Result
        data class Invalid(val reason: String) : Result
    }

    fun validate(input: String): Result {
        val trimmed = input.trim()
        if (trimmed.length < 3) return Result.Invalid("Too short")
        if (!PATTERN.matches(trimmed)) return Result.Invalid("Invalid characters")
        return Result.Valid
    }
}
