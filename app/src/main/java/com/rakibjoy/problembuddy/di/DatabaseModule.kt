package com.rakibjoy.problembuddy.di

import android.content.Context
import androidx.room.Room
import com.rakibjoy.problembuddy.data.local.ProblemBuddyDatabase
import com.rakibjoy.problembuddy.data.model.LocalProblem
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ProblemBuddyDatabase {
        return Room.databaseBuilder(
            context,
            ProblemBuddyDatabase::class.java,
            "problembuddy_database"
        )
            .fallbackToDestructiveMigration()
            .addCallback(object : androidx.room.RoomDatabase.Callback() {
                override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                    super.onCreate(db)
                    // Insert sample data when database is created
                    CoroutineScope(Dispatchers.IO).launch {
                        insertSampleData(context)
                    }
                }
            })
            .build()
    }

    @Provides
    fun provideProblemDao(database: ProblemBuddyDatabase) = database.problemDao()

    @Provides
    fun provideSubmissionDao(database: ProblemBuddyDatabase) = database.submissionDao()

    @Provides
    fun provideUserDao(database: ProblemBuddyDatabase) = database.userDao()

    private suspend fun insertSampleData(context: Context) {
        // Create a temporary database instance for sample data
        val tempDatabase = Room.databaseBuilder(
            context,
            ProblemBuddyDatabase::class.java,
            "temp_database"
        ).build()

        val problemDao = tempDatabase.problemDao()

        // Insert sample problems for different skill levels
        val sampleProblems = listOf(
            // Pupil level problems (1200-1399)
            LocalProblem(
                contestId = 1,
                index = "A",
                rating = 1200,
                tags = "implementation,math",
                skillLevel = "pupil"
            ),
            LocalProblem(
                contestId = 1,
                index = "B",
                rating = 1300,
                tags = "greedy,implementation",
                skillLevel = "pupil"
            ),
            LocalProblem(
                contestId = 2,
                index = "A",
                rating = 1250,
                tags = "math,implementation",
                skillLevel = "pupil"
            ),

            // Specialist level problems (1400-1599)
            LocalProblem(
                contestId = 3,
                index = "A",
                rating = 1400,
                tags = "greedy,implementation",
                skillLevel = "specialist"
            ),
            LocalProblem(
                contestId = 3,
                index = "B",
                rating = 1500,
                tags = "dp,implementation",
                skillLevel = "specialist"
            ),
            LocalProblem(
                contestId = 4,
                index = "A",
                rating = 1450,
                tags = "math,greedy",
                skillLevel = "specialist"
            ),

            // Expert level problems (1600-1899)
            LocalProblem(
                contestId = 5,
                index = "A",
                rating = 1600,
                tags = "dp,greedy",
                skillLevel = "expert"
            ),
            LocalProblem(
                contestId = 5,
                index = "B",
                rating = 1700,
                tags = "graphs,implementation",
                skillLevel = "expert"
            ),
            LocalProblem(
                contestId = 6,
                index = "A",
                rating = 1650,
                tags = "math,dp",
                skillLevel = "expert"
            ),

            // Candidate Master level problems (1900-2099)
            LocalProblem(
                contestId = 7,
                index = "A",
                rating = 1900,
                tags = "graphs,dp",
                skillLevel = "candidate_master"
            ),
            LocalProblem(
                contestId = 7,
                index = "B",
                rating = 2000,
                tags = "trees,implementation",
                skillLevel = "candidate_master"
            ),
            LocalProblem(
                contestId = 8,
                index = "A",
                rating = 1950,
                tags = "math,graphs",
                skillLevel = "candidate_master"
            ),

            // Master level problems (2100+)
            LocalProblem(
                contestId = 9,
                index = "A",
                rating = 2100,
                tags = "advanced,dp",
                skillLevel = "master"
            ),
            LocalProblem(
                contestId = 9,
                index = "B",
                rating = 2200,
                tags = "graphs,advanced",
                skillLevel = "master"
            ),
            LocalProblem(
                contestId = 10,
                index = "A",
                rating = 2150,
                tags = "math,advanced",
                skillLevel = "master"
            )
        )

        problemDao.insertProblems(sampleProblems)
        tempDatabase.close()
    }
}