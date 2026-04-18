package com.rakibjoy.problembuddy.domain.repository

import com.rakibjoy.problembuddy.domain.model.Review
import kotlinx.coroutines.flow.Flow

interface ReviewRepository {
    fun observeDue(nowSeconds: Long): Flow<List<Review>>
    suspend fun scheduleInitial(problemId: Long, contestId: Int, problemIndex: String)
    suspend fun markCorrect(review: Review)
    suspend fun markMissed(review: Review)
    suspend fun clearAll()
}
