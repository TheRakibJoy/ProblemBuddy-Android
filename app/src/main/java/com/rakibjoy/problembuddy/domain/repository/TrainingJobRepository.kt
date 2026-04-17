package com.rakibjoy.problembuddy.domain.repository

import com.rakibjoy.problembuddy.domain.model.TrainingJob
import kotlinx.coroutines.flow.Flow

interface TrainingJobRepository {
    fun observeLatest(): Flow<TrainingJob?>
    suspend fun upsert(job: TrainingJob): Long
    suspend fun clearAll()
}
