package com.rakibjoy.problembuddy.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.rakibjoy.problembuddy.core.database.entity.TrainingJobEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrainingJobDao {
    @Upsert
    suspend fun upsert(job: TrainingJobEntity): Long

    @Query("SELECT * FROM training_jobs ORDER BY updatedAt DESC LIMIT 1")
    fun observeLatest(): Flow<TrainingJobEntity?>

    @Query("DELETE FROM training_jobs")
    suspend fun deleteAll()
}
