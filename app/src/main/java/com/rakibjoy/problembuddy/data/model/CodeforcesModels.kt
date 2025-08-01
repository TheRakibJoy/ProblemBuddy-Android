package com.rakibjoy.problembuddy.data.model

import com.google.gson.annotations.SerializedName

// User Info Response
data class UserInfoResponse(
    val status: String,
    val result: List<UserInfo>
)

data class UserInfo(
    val handle: String,
    @SerializedName("maxRating")
    val maxRating: Int,
    @SerializedName("maxRank")
    val maxRank: String,
    @SerializedName("titlePhoto")
    val titlePhoto: String,
    val rating: Int? = null,
    val rank: String? = null
)

// User Rating Response
data class UserRatingResponse(
    val status: String,
    val result: List<RatingChange>
)

data class RatingChange(
    @SerializedName("contestId")
    val contestId: Int,
    @SerializedName("contestName")
    val contestName: String,
    @SerializedName("handle")
    val handle: String,
    @SerializedName("rank")
    val rank: Int,
    @SerializedName("ratingUpdateTimeSeconds")
    val ratingUpdateTimeSeconds: Long,
    @SerializedName("oldRating")
    val oldRating: Int,
    @SerializedName("newRating")
    val newRating: Int
)

// User Status Response
data class UserStatusResponse(
    val status: String,
    val result: List<Submission>
)

data class Submission(
    @SerializedName("id")
    val id: Long,
    @SerializedName("contestId")
    val contestId: Int,
    @SerializedName("creationTimeSeconds")
    val creationTimeSeconds: Long,
    @SerializedName("relativeTimeSeconds")
    val relativeTimeSeconds: Long,
    @SerializedName("problem")
    val problem: Problem,
    @SerializedName("author")
    val author: Author,
    @SerializedName("programmingLanguage")
    val programmingLanguage: String,
    @SerializedName("verdict")
    val verdict: String,
    @SerializedName("testset")
    val testset: String,
    @SerializedName("passedTestCount")
    val passedTestCount: Int,
    @SerializedName("timeConsumedMillis")
    val timeConsumedMillis: Int,
    @SerializedName("memoryConsumedBytes")
    val memoryConsumedBytes: Int
)

data class Problem(
    @SerializedName("contestId")
    val contestId: Int,
    @SerializedName("index")
    val index: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("rating")
    val rating: Int?,
    @SerializedName("tags")
    val tags: List<String>
)

data class Author(
    @SerializedName("contestId")
    val contestId: Int,
    @SerializedName("members")
    val members: List<Member>,
    @SerializedName("participantType")
    val participantType: String,
    @SerializedName("ghost")
    val ghost: Boolean,
    @SerializedName("startTimeSeconds")
    val startTimeSeconds: Long
)

data class Member(
    val handle: String
)

// Contest Standings Response
data class ContestStandingsResponse(
    val status: String,
    val result: ContestStandings
)

data class ContestStandings(
    @SerializedName("contest")
    val contest: Contest,
    @SerializedName("problems")
    val problems: List<ContestProblem>,
    @SerializedName("rows")
    val rows: List<RanklistRow>
)

data class Contest(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("phase")
    val phase: String,
    @SerializedName("frozen")
    val frozen: Boolean,
    @SerializedName("durationSeconds")
    val durationSeconds: Int,
    @SerializedName("startTimeSeconds")
    val startTimeSeconds: Long,
    @SerializedName("relativeTimeSeconds")
    val relativeTimeSeconds: Long
)

data class ContestProblem(
    @SerializedName("contestId")
    val contestId: Int,
    @SerializedName("index")
    val index: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("rating")
    val rating: Int?,
    @SerializedName("tags")
    val tags: List<String>
)

data class RanklistRow(
    @SerializedName("party")
    val party: Author,
    @SerializedName("rank")
    val rank: Int,
    @SerializedName("points")
    val points: Double,
    @SerializedName("penalty")
    val penalty: Int,
    @SerializedName("successfulHackCount")
    val successfulHackCount: Int,
    @SerializedName("unsuccessfulHackCount")
    val unsuccessfulHackCount: Int,
    @SerializedName("problemResults")
    val problemResults: List<ProblemResult>
)

data class ProblemResult(
    @SerializedName("points")
    val points: Double,
    @SerializedName("rejectedAttemptCount")
    val rejectedAttemptCount: Int,
    @SerializedName("type")
    val type: String,
    @SerializedName("bestSubmissionTimeSeconds")
    val bestSubmissionTimeSeconds: Int?
)