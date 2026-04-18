package com.rakibjoy.problembuddy.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rakibjoy.problembuddy.core.database.entity.ProblemEntity
import kotlinx.coroutines.flow.Flow

data class ProblemKey(
    val id: Long,
    val contestId: Int,
    val problemIndex: String,
)

@Dao
interface ProblemDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(problems: List<ProblemEntity>)

    @Query("SELECT * FROM problems WHERE tier = :tier")
    fun observeByTier(tier: String): Flow<List<ProblemEntity>>

    @Query("SELECT COUNT(*) FROM problems WHERE tier = :tier")
    suspend fun countByTier(tier: String): Int

    @Query("SELECT COUNT(*) FROM problems")
    fun observeTotalCount(): Flow<Int>

    @Query("SELECT id FROM problems WHERE contestId = :contestId AND problemIndex = :problemIndex LIMIT 1")
    suspend fun findId(contestId: Int, problemIndex: String): Long?

    @Query("SELECT id, contestId, problemIndex FROM problems WHERE id IN (:ids)")
    suspend fun findKeysByIds(ids: List<Long>): List<ProblemKey>

    @Query("DELETE FROM problems")
    suspend fun deleteAll()
}
