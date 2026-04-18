package com.rakibjoy.problembuddy.data.repository

import com.rakibjoy.problembuddy.core.database.dao.HandleDao
import com.rakibjoy.problembuddy.core.database.entity.HandleEntity
import com.rakibjoy.problembuddy.domain.repository.HandleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HandleRepositoryImpl @Inject constructor(
    private val dao: HandleDao,
) : HandleRepository {

    override fun observeAll(): Flow<List<String>> =
        dao.observeAll().map { list -> list.map { it.handle } }

    override suspend fun insert(handle: String) {
        dao.insert(HandleEntity(handle = handle))
    }

    override suspend fun clear() {
        dao.deleteAll()
    }
}
