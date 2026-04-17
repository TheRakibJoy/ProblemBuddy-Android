package com.rakibjoy.problembuddy.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.rakibjoy.problembuddy.core.database.entity.InteractionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InteractionDao {
    @Upsert
    suspend fun upsert(interaction: InteractionEntity)

    @Query("SELECT * FROM interactions WHERE status = :status")
    fun observeByStatus(status: String): Flow<List<InteractionEntity>>

    @Query("SELECT * FROM interactions")
    fun observeAll(): Flow<List<InteractionEntity>>

    @Query("DELETE FROM interactions")
    suspend fun deleteAll()
}
