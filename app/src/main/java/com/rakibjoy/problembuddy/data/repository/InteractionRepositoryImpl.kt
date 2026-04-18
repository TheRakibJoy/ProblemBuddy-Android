package com.rakibjoy.problembuddy.data.repository

import com.rakibjoy.problembuddy.core.database.dao.InteractionDao
import com.rakibjoy.problembuddy.data.mapper.toDomain
import com.rakibjoy.problembuddy.data.mapper.toEntity
import com.rakibjoy.problembuddy.domain.model.Interaction
import com.rakibjoy.problembuddy.domain.repository.InteractionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InteractionRepositoryImpl @Inject constructor(
    private val dao: InteractionDao,
) : InteractionRepository {

    override fun observeAll(): Flow<List<Interaction>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun upsert(interaction: Interaction) {
        dao.upsert(interaction.toEntity())
    }

    override suspend fun getAll(): List<Interaction> =
        dao.getAll().map { it.toDomain() }

    override suspend fun clear() {
        dao.deleteAll()
    }
}
