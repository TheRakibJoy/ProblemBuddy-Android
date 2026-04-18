package com.rakibjoy.problembuddy.core.network

import com.rakibjoy.problembuddy.core.network.dto.CfEnvelope
import com.rakibjoy.problembuddy.core.network.dto.ContestDto
import com.rakibjoy.problembuddy.core.network.dto.RatingChangeDto
import com.rakibjoy.problembuddy.core.network.dto.SubmissionDto
import com.rakibjoy.problembuddy.core.network.dto.UserInfoDto
import retrofit2.http.GET
import retrofit2.http.Query

interface CodeforcesApi {
    @GET("user.info")
    suspend fun userInfo(@Query("handles") handles: String): CfEnvelope<List<UserInfoDto>>

    @GET("user.rating")
    suspend fun userRating(@Query("handle") handle: String): CfEnvelope<List<RatingChangeDto>>

    @GET("user.status")
    suspend fun userStatus(
        @Query("handle") handle: String,
        @Query("from") from: Int,
        @Query("count") count: Int,
    ): CfEnvelope<List<SubmissionDto>>

    @GET("contest.list")
    suspend fun contestList(@Query("gym") gym: Boolean = false): CfEnvelope<List<ContestDto>>
}
