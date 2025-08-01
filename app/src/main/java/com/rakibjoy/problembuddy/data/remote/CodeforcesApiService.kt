package com.rakibjoy.problembuddy.data.remote

import com.rakibjoy.problembuddy.data.model.*
import retrofit2.http.GET
import retrofit2.http.Query

interface CodeforcesApiService {

    @GET("user.info")
    suspend fun getUserInfo(
        @Query("handles") handle: String
    ): UserInfoResponse

    @GET("user.rating")
    suspend fun getUserRating(
        @Query("handle") handle: String
    ): UserRatingResponse

    @GET("user.status")
    suspend fun getUserStatus(
        @Query("handle") handle: String,
        @Query("from") from: Int = 1,
        @Query("count") count: Int = 1000
    ): UserStatusResponse

    @GET("contest.standings")
    suspend fun getContestStandings(
        @Query("contestId") contestId: Int,
        @Query("showUnofficial") showUnofficial: Boolean = true,
        @Query("handles") handles: String
    ): ContestStandingsResponse
}