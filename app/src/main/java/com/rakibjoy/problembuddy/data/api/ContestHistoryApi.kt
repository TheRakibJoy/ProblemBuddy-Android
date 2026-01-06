package com.rakibjoy.problembuddy.data.api

import retrofit2.http.GET
import retrofit2.http.Query

interface ContestHistoryApi {
    @GET("user.rating")
    suspend fun getUserRating(
        @Query("handle") handle: String
    ): UserRatingResponse
}

data class UserRatingResponse(
    val status: String,
    val result: List<RatingChange>
)

data class RatingChange(
    val contestId: Int,
    val contestName: String,
    val handle: String,
    val rank: Int,
    val ratingUpdateTimeSeconds: Long,
    val oldRating: Int,
    val newRating: Int
)
