package com.rakibjoy.problembuddy.domain.repository

import com.rakibjoy.problembuddy.domain.model.TagCounter
import com.rakibjoy.problembuddy.domain.model.Tier
import kotlinx.coroutines.flow.Flow

interface CounterRepository {
    suspend fun getByTier(tier: Tier): List<TagCounter>
    fun observeByTier(tier: Tier): Flow<List<TagCounter>>
    suspend fun upsertAll(counters: List<TagCounter>)
    fun observeDistinctTagCount(): Flow<Int>
    suspend fun clear()
}
