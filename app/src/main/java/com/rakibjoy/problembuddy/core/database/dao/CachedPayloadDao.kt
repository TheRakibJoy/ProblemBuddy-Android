package com.rakibjoy.problembuddy.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.rakibjoy.problembuddy.core.database.entity.CachedPayloadEntity

@Dao
interface CachedPayloadDao {
    @Query("SELECT * FROM cached_payloads WHERE cacheKey = :key LIMIT 1")
    suspend fun get(key: String): CachedPayloadEntity?

    @Upsert
    suspend fun upsert(entity: CachedPayloadEntity)

    @Query("DELETE FROM cached_payloads")
    suspend fun deleteAll()
}
