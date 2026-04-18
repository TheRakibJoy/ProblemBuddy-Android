package com.rakibjoy.problembuddy.domain.repository

import com.rakibjoy.problembuddy.domain.model.TrainingJob
import kotlinx.coroutines.flow.Flow

interface TrainingJobRepository {
    fun observeLatest(): Flow<TrainingJob?>
    fun observeAll(): Flow<List<TrainingJob>>
    suspend fun upsert(job: TrainingJob): Long
    suspend fun deleteByHandle(handle: String)
    suspend fun clearAll()
}
