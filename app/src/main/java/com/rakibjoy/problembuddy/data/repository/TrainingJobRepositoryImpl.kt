package com.rakibjoy.problembuddy.data.repository

import com.rakibjoy.problembuddy.core.database.dao.TrainingJobDao
import com.rakibjoy.problembuddy.data.mapper.toDomain
import com.rakibjoy.problembuddy.data.mapper.toEntity
import com.rakibjoy.problembuddy.domain.model.TrainingJob
import com.rakibjoy.problembuddy.domain.repository.TrainingJobRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrainingJobRepositoryImpl @Inject constructor(
    private val dao: TrainingJobDao,
) : TrainingJobRepository {

    override fun observeLatest(): Flow<TrainingJob?> =
        dao.observeLatest().map { it?.toDomain() }

    override suspend fun upsert(job: TrainingJob): Long =
        dao.upsert(job.toEntity())

    override suspend fun clearAll() {
        dao.deleteAll()
    }
}
