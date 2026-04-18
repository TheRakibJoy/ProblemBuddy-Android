package com.rakibjoy.problembuddy.domain.repository

import kotlinx.coroutines.flow.Flow

interface HandleRepository {
    fun observeAll(): Flow<List<String>>
    suspend fun insert(handle: String)
    suspend fun clear()
}
