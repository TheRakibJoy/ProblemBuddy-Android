package com.rakibjoy.problembuddy.domain.repository

import com.rakibjoy.problembuddy.domain.model.Fresh
import com.rakibjoy.problembuddy.domain.model.RatingChange
import com.rakibjoy.problembuddy.domain.model.Submission
import com.rakibjoy.problembuddy.domain.model.UpcomingContest
import com.rakibjoy.problembuddy.domain.model.UserInfo

interface CodeforcesRepository {
    suspend fun userInfo(handle: String): Result<UserInfo>
    suspend fun userRating(handle: String): Result<List<RatingChange>>
    suspend fun userStatus(handle: String, from: Int, count: Int): Result<List<Submission>>
    suspend fun upcomingContests(): Result<List<UpcomingContest>>

    suspend fun userInfoWithFallback(handle: String): Result<Fresh<UserInfo>>
    suspend fun userStatusWithFallback(
        handle: String,
        from: Int,
        count: Int,
    ): Result<Fresh<List<Submission>>>
}
