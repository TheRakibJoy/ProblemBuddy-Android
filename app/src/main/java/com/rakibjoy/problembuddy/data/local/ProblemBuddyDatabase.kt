package com.rakibjoy.problembuddy.data.local

import androidx.room.*
import com.rakibjoy.problembuddy.data.model.LocalProblem
import com.rakibjoy.problembuddy.data.model.LocalSubmission
import com.rakibjoy.problembuddy.data.model.LocalUser
import kotlinx.coroutines.flow.Flow

@Database(
    entities = [
        LocalProblem::class,
        LocalSubmission::class,
        LocalUser::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ProblemBuddyDatabase : RoomDatabase() {
    abstract fun problemDao(): ProblemDao
    abstract fun submissionDao(): SubmissionDao
    abstract fun userDao(): UserDao
}

@Dao
interface ProblemDao {
    @Query("SELECT * FROM local_problems WHERE skillLevel = :skillLevel")
    fun getProblemsBySkillLevel(skillLevel: String): Flow<List<LocalProblem>>

    @Query("SELECT * FROM local_problems WHERE skillLevel = :skillLevel AND tags LIKE '%' || :tag || '%'")
    fun getProblemsBySkillLevelAndTag(skillLevel: String, tag: String): Flow<List<LocalProblem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProblems(problems: List<LocalProblem>)

    @Query("DELETE FROM local_problems WHERE skillLevel = :skillLevel")
    suspend fun deleteProblemsBySkillLevel(skillLevel: String)

    @Query("SELECT COUNT(*) FROM local_problems WHERE skillLevel = :skillLevel")
    suspend fun getProblemCount(skillLevel: String): Int

    @Query("SELECT * FROM local_problems LIMIT 1")
    suspend fun getAnyProblem(): LocalProblem?
}

@Dao
interface SubmissionDao {
    @Query("SELECT * FROM local_submissions WHERE contestId = :contestId AND `index` = :index")
    suspend fun getSubmission(contestId: Int, index: String): LocalSubmission?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubmission(submission: LocalSubmission)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubmissions(submissions: List<LocalSubmission>)

    @Query("SELECT * FROM local_submissions WHERE handle = :handle")
    fun getSubmissionsByHandle(handle: String): Flow<List<LocalSubmission>>

    @Query("DELETE FROM local_submissions WHERE handle = :handle")
    suspend fun deleteSubmissionsByHandle(handle: String)
}

@Dao
interface UserDao {
    @Query("SELECT * FROM local_users WHERE handle = :handle")
    suspend fun getUserByHandle(handle: String): LocalUser?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: LocalUser)

    @Query("SELECT * FROM local_users ORDER BY lastUpdated DESC")
    fun getAllUsers(): Flow<List<LocalUser>>

    @Delete
    suspend fun deleteUser(user: LocalUser)
}