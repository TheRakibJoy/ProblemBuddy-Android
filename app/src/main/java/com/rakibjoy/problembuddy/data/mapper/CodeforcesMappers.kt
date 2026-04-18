package com.rakibjoy.problembuddy.data.mapper

import com.rakibjoy.problembuddy.core.network.dto.ContestDto
import com.rakibjoy.problembuddy.core.network.dto.ProblemDto
import com.rakibjoy.problembuddy.core.network.dto.RatingChangeDto
import com.rakibjoy.problembuddy.core.network.dto.SubmissionDto
import com.rakibjoy.problembuddy.core.network.dto.UserInfoDto
import com.rakibjoy.problembuddy.domain.model.Problem
import com.rakibjoy.problembuddy.domain.model.RatingChange
import com.rakibjoy.problembuddy.domain.model.Submission
import com.rakibjoy.problembuddy.domain.model.UpcomingContest
import com.rakibjoy.problembuddy.domain.model.UserInfo

fun UserInfoDto.toDomain(): UserInfo = UserInfo(
    handle = handle,
    rating = rating,
    maxRating = maxRating,
    rank = rank,
    maxRank = maxRank,
    avatarUrl = avatar?.ensureHttps(),
    titlePhotoUrl = titlePhoto?.ensureHttps(),
)

private fun String.ensureHttps(): String = when {
    startsWith("//") -> "https:$this"
    startsWith("http://") -> "https://" + removePrefix("http://")
    else -> this
}

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

fun ContestDto.toUpcoming(): UpcomingContest? {
    if (phase != "BEFORE") return null
    val start = startTimeSeconds ?: return null
    return UpcomingContest(
        id = id,
        name = name,
        startTimeSeconds = start,
        durationSeconds = durationSeconds,
        division = parseContestDivision(name),
    )
}

private fun parseContestDivision(contestName: String): String? = when {
    "(Div. 1)" in contestName -> "Div 1"
    "(Div. 2)" in contestName -> "Div 2"
    "(Div. 3)" in contestName -> "Div 3"
    "(Div. 4)" in contestName -> "Div 4"
    contestName.startsWith("Educational", ignoreCase = true) -> "Edu"
    contestName.contains("Global", ignoreCase = true) -> "Global"
    else -> null
}

fun SubmissionDto.toDomain(): Submission = Submission(
    id = id,
    problem = problem.toDomain(),
    verdict = verdict,
    creationTimeSeconds = creationTimeSeconds,
    programmingLanguage = programmingLanguage,
    participantType = participantType,
)
