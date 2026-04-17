package com.rakibjoy.problembuddy.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rakibjoy.problembuddy.core.database.entity.ProblemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProblemDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(problems: List<ProblemEntity>)

    @Query("SELECT * FROM problems WHERE tier = :tier")
    fun observeByTier(tier: String): Flow<List<ProblemEntity>>

    @Query("SELECT COUNT(*) FROM problems WHERE tier = :tier")
    suspend fun countByTier(tier: String): Int

    @Query("SELECT id FROM problems WHERE contestId = :contestId AND problemIndex = :problemIndex LIMIT 1")
    suspend fun findId(contestId: Int, problemIndex: String): Long?

    @Query("DELETE FROM problems")
    suspend fun deleteAll()
}
