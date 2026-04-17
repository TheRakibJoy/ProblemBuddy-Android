package com.rakibjoy.problembuddy.data.mapper

import com.rakibjoy.problembuddy.core.network.dto.ProblemDto
import com.rakibjoy.problembuddy.core.network.dto.RatingChangeDto
import com.rakibjoy.problembuddy.core.network.dto.SubmissionDto
import com.rakibjoy.problembuddy.core.network.dto.UserInfoDto
import com.rakibjoy.problembuddy.domain.model.Problem
import com.rakibjoy.problembuddy.domain.model.RatingChange
import com.rakibjoy.problembuddy.domain.model.Submission
import com.rakibjoy.problembuddy.domain.model.UserInfo

fun UserInfoDto.toDomain(): UserInfo = UserInfo(
    handle = handle,
    rating = rating,
    maxRating = maxRating,
    rank = rank,
    maxRank = maxRank,
)

fun RatingChangeDto.toDomain(): RatingChange = RatingChange(
    contestId = contestId,
    contestName = contestName,
    handle = handle,
    rank = rank,
    ratingUpdateTimeSeconds = ratingUpdateTimeSeconds,
    oldRating = oldRating,
    newRating = newRating,
)

fun ProblemDto.toDomain(): Problem = Problem(
    contestId = contestId ?: 0,
    problemIndex = index,
    name = name,
    rating = rating,
    tags = tags,
)

fun SubmissionDto.toDomain(): Submission = Submission(
    id = id,
    problem = problem.toDomain(),
    verdict = verdict,
    creationTimeSeconds = creationTimeSeconds,
)
