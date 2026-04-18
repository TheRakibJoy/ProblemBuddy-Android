package com.rakibjoy.problembuddy.data.repository

import com.rakibjoy.problembuddy.core.database.dao.CounterDao
import com.rakibjoy.problembuddy.data.mapper.toDomain
import com.rakibjoy.problembuddy.data.mapper.toEntity
import com.rakibjoy.problembuddy.domain.model.TagCounter
import com.rakibjoy.problembuddy.domain.model.Tier
import com.rakibjoy.problembuddy.domain.repository.CounterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CounterRepositoryImpl @Inject constructor(
    private val dao: CounterDao,
) : CounterRepository {

    override suspend fun getByTier(tier: Tier): List<TagCounter> =
        dao.getByTier(tier.name.lowercase()).map { it.toDomain() }

    override fun observeByTier(tier: Tier): Flow<List<TagCounter>> =
        dao.observeByTier(tier.name.lowercase()).map { list -> list.map { it.toDomain() } }

    override suspend fun upsertAll(counters: List<TagCounter>) {
        dao.upsertAll(counters.map { it.toEntity() })
    }

    override fun observeDistinctTagCount(): Flow<Int> = dao.observeDistinctTagCount()

    override suspend fun clear() {
        dao.deleteAll()
    }
}
