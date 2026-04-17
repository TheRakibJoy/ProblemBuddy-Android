package com.rakibjoy.problembuddy.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.rakibjoy.problembuddy.core.database.entity.CounterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CounterDao {
    @Upsert
    suspend fun upsert(counter: CounterEntity)

    @Upsert
    suspend fun upsertAll(counters: List<CounterEntity>)

    @Query("SELECT * FROM counters WHERE tier = :tier")
    fun observeByTier(tier: String): Flow<List<CounterEntity>>

    @Query("SELECT * FROM counters WHERE tier = :tier")
    suspend fun getByTier(tier: String): List<CounterEntity>

    @Query("DELETE FROM counters")
    suspend fun deleteAll()
}
