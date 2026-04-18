package com.rakibjoy.problembuddy.domain.repository

import com.rakibjoy.problembuddy.domain.model.Interaction
import kotlinx.coroutines.flow.Flow

interface InteractionRepository {
    fun observeAll(): Flow<List<Interaction>>
    suspend fun upsert(interaction: Interaction)
    suspend fun getAll(): List<Interaction>
    suspend fun clear()
}
