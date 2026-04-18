package com.rakibjoy.problembuddy.domain.repository

import com.rakibjoy.problembuddy.domain.model.Problem
import com.rakibjoy.problembuddy.domain.model.Tier
import kotlinx.coroutines.flow.Flow

interface ProblemRepository {
    fun observeByTier(tier: Tier): Flow<List<Problem>>
    suspend fun insertAll(problems: List<Problem>)
    suspend fun countByTier(tier: Tier): Int
    fun observeTotalCount(): Flow<Int>
    suspend fun findId(contestId: Int, problemIndex: String): Long?
    suspend fun resolveKeys(ids: Set<Long>): Map<Long, Pair<Int, String>>
    suspend fun clear()
}
