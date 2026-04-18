package com.rakibjoy.problembuddy.data.mapper

import com.rakibjoy.problembuddy.core.database.entity.ProblemEntity
import com.rakibjoy.problembuddy.domain.model.Problem
import com.rakibjoy.problembuddy.domain.model.Tier

fun ProblemEntity.toDomain(): Problem = Problem(
    contestId = contestId,
    problemIndex = problemIndex,
    name = "",
    rating = rating,
    tags = tags.split(',')
        .asSequence()
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .toList(),
)

fun Problem.toEntity(tier: Tier): ProblemEntity = ProblemEntity(
    tier = tier.name.lowercase(),
    contestId = contestId,
    problemIndex = problemIndex,
    rating = rating,
    tags = tags.joinToString(","),
)
