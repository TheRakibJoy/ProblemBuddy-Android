package com.rakibjoy.problembuddy.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rakibjoy.problembuddy.core.database.entity.HandleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HandleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(handle: HandleEntity)

    @Query("SELECT * FROM handles")
    fun observeAll(): Flow<List<HandleEntity>>

    @Query("DELETE FROM handles")
    suspend fun deleteAll()
}
