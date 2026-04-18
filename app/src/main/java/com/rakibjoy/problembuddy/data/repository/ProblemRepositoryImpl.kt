package com.rakibjoy.problembuddy.data.repository

import com.rakibjoy.problembuddy.core.database.dao.ProblemDao
import com.rakibjoy.problembuddy.data.mapper.toDomain
import com.rakibjoy.problembuddy.data.mapper.toEntity
import com.rakibjoy.problembuddy.domain.model.Problem
import com.rakibjoy.problembuddy.domain.model.Tier
import com.rakibjoy.problembuddy.domain.repository.ProblemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProblemRepositoryImpl @Inject constructor(
    private val dao: ProblemDao,
) : ProblemRepository {

    override fun observeByTier(tier: Tier): Flow<List<Problem>> =
        dao.observeByTier(tier.name.lowercase()).map { list -> list.map { it.toDomain() } }

    override suspend fun insertAll(problems: List<Problem>) {
        val entities = problems.map { p ->
            p.toEntity(Tier.forMaxRating(p.rating ?: 0))
        }
        dao.insertAll(entities)
    }

    override suspend fun countByTier(tier: Tier): Int =
        dao.countByTier(tier.name.lowercase())

    override fun observeTotalCount(): Flow<Int> = dao.observeTotalCount()

    override suspend fun findId(contestId: Int, problemIndex: String): Long? =
        dao.findId(contestId, problemIndex)

    override suspend fun resolveKeys(ids: Set<Long>): Map<Long, Pair<Int, String>> {
        if (ids.isEmpty()) return emptyMap()
        return dao.findKeysByIds(ids.toList())
            .associate { it.id to (it.contestId to it.problemIndex) }
    }

    override suspend fun clear() {
        dao.deleteAll()
    }
}
