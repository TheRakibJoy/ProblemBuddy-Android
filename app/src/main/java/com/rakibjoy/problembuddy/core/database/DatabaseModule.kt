package com.rakibjoy.problembuddy.core.database

import android.content.Context
import androidx.room.Room
import com.rakibjoy.problembuddy.core.database.dao.CachedPayloadDao
import com.rakibjoy.problembuddy.core.database.dao.CounterDao
import com.rakibjoy.problembuddy.core.database.dao.HandleDao
import com.rakibjoy.problembuddy.core.database.dao.InteractionDao
import com.rakibjoy.problembuddy.core.database.dao.ProblemDao
import com.rakibjoy.problembuddy.core.database.dao.ReviewDao
import com.rakibjoy.problembuddy.core.database.dao.TrainingJobDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): ProblemBuddyDatabase =
        Room.databaseBuilder(
            context,
            ProblemBuddyDatabase::class.java,
            "problembuddy.db",
        ).build()

    @Provides
    @Singleton
    fun provideProblemDao(db: ProblemBuddyDatabase): ProblemDao = db.problemDao()

    @Provides
    @Singleton
    fun provideCounterDao(db: ProblemBuddyDatabase): CounterDao = db.counterDao()

    @Provides
    @Singleton
    fun provideHandleDao(db: ProblemBuddyDatabase): HandleDao = db.handleDao()

    @Provides
    @Singleton
    fun provideInteractionDao(db: ProblemBuddyDatabase): InteractionDao = db.interactionDao()

    @Provides
    @Singleton
    fun provideTrainingJobDao(db: ProblemBuddyDatabase): TrainingJobDao = db.trainingJobDao()

    @Provides
    @Singleton
    fun provideCachedPayloadDao(db: ProblemBuddyDatabase): CachedPayloadDao = db.cachedPayloadDao()

    @Provides
    @Singleton
    fun provideReviewDao(db: ProblemBuddyDatabase): ReviewDao = db.reviewDao()
}
