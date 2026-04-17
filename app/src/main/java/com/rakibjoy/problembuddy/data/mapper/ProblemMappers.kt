package com.rakibjoy.problembuddy.data.mapper

import com.rakibjoy.problembuddy.core.database.entity.ProblemEntity
import com.rakibjoy.problembuddy.domain.model.Problem

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
