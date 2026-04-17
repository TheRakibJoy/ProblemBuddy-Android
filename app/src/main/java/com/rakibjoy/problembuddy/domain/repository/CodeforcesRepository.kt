package com.rakibjoy.problembuddy.domain.repository

import com.rakibjoy.problembuddy.domain.model.RatingChange
import com.rakibjoy.problembuddy.domain.model.Submission
import com.rakibjoy.problembuddy.domain.model.UserInfo

interface CodeforcesRepository {
    suspend fun userInfo(handle: String): Result<UserInfo>
    suspend fun userRating(handle: String): Result<List<RatingChange>>
    suspend fun userStatus(handle: String, from: Int, count: Int): Result<List<Submission>>
}
