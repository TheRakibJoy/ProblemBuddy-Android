package com.rakibjoy.problembuddy.data.cache

import com.rakibjoy.problembuddy.domain.model.Problem
import com.rakibjoy.problembuddy.domain.model.Submission
import com.rakibjoy.problembuddy.domain.model.UserInfo
import kotlinx.serialization.Serializable

@Serializable
data class UserInfoPersisted(
    val handle: String,
    val rating: Int? = null,
    val maxRating: Int? = null,
    val rank: String? = null,
    val maxRank: String? = null,
    val avatarUrl: String? = null,
    val titlePhotoUrl: String? = null,
)

@Serializable
data class ProblemPersisted(
    val contestId: Int,
    val problemIndex: String,
    val name: String = "",
    val rating: Int? = null,
    val tags: List<String> = emptyList(),
)

@Serializable
data class SubmissionPersisted(
    val id: Long = 0L,
    val verdict: String? = null,
    val creationTimeSeconds: Long = 0L,
    val problem: ProblemPersisted,
    val programmingLanguage: String? = null,
    val participantType: String? = null,
)

fun UserInfo.toPersisted(): UserInfoPersisted =
    UserInfoPersisted(
        handle = handle,
        rating = rating,
        maxRating = maxRating,
        rank = rank,
        maxRank = maxRank,
        avatarUrl = avatarUrl,
        titlePhotoUrl = titlePhotoUrl,
    )

fun UserInfoPersisted.toDomain(): UserInfo =
    UserInfo(
        handle = handle,
        rating = rating,
        maxRating = maxRating,
        rank = rank,
        maxRank = maxRank,
        avatarUrl = avatarUrl,
        titlePhotoUrl = titlePhotoUrl,
    )

fun Problem.toPersisted(): ProblemPersisted =
    ProblemPersisted(
        contestId = contestId,
        problemIndex = problemIndex,
        name = name,
        rating = rating,
        tags = tags,
    )

fun ProblemPersisted.toDomain(): Problem =
    Problem(
        contestId = contestId,
        problemIndex = problemIndex,
        name = name,
        rating = rating,
        tags = tags,
    )

fun Submission.toPersisted(): SubmissionPersisted =
    SubmissionPersisted(
        id = id,
        verdict = verdict,
        creationTimeSeconds = creationTimeSeconds,
        problem = problem.toPersisted(),
        programmingLanguage = programmingLanguage,
        participantType = participantType,
    )

fun SubmissionPersisted.toDomain(): Submission =
    Submission(
        id = id,
        verdict = verdict,
        creationTimeSeconds = creationTimeSeconds,
        problem = problem.toDomain(),
        programmingLanguage = programmingLanguage,
        participantType = participantType,
    )
