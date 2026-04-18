package com.rakibjoy.problembuddy.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.rakibjoy.problembuddy.core.database.entity.ReviewEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewDao {
    @Upsert
    suspend fun upsert(review: ReviewEntity): Long

    @Query("SELECT * FROM reviews WHERE nextReviewAt <= :nowSeconds ORDER BY nextReviewAt ASC")
    fun observeDue(nowSeconds: Long): Flow<List<ReviewEntity>>

    @Query("SELECT * FROM reviews WHERE problemId = :problemId LIMIT 1")
    suspend fun getForProblem(problemId: Long): ReviewEntity?

    @Query("DELETE FROM reviews WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM reviews")
    suspend fun deleteAll()
}
