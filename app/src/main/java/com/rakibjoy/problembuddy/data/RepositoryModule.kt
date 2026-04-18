package com.rakibjoy.problembuddy.data

import com.rakibjoy.problembuddy.data.repository.CodeforcesRepositoryImpl
import com.rakibjoy.problembuddy.data.repository.CounterRepositoryImpl
import com.rakibjoy.problembuddy.data.repository.HandleRepositoryImpl
import com.rakibjoy.problembuddy.data.repository.InteractionRepositoryImpl
import com.rakibjoy.problembuddy.data.repository.ProblemRepositoryImpl
import com.rakibjoy.problembuddy.data.repository.ReviewRepositoryImpl
import com.rakibjoy.problembuddy.data.repository.TrainingJobRepositoryImpl
import com.rakibjoy.problembuddy.domain.repository.CodeforcesRepository
import com.rakibjoy.problembuddy.domain.repository.CounterRepository
import com.rakibjoy.problembuddy.domain.repository.HandleRepository
import com.rakibjoy.problembuddy.domain.repository.InteractionRepository
import com.rakibjoy.problembuddy.domain.repository.ProblemRepository
import com.rakibjoy.problembuddy.domain.repository.ReviewRepository
import com.rakibjoy.problembuddy.domain.repository.TrainingJobRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCodeforcesRepository(
        impl: CodeforcesRepositoryImpl,
    ): CodeforcesRepository

    @Binds
    @Singleton
    abstract fun bindTrainingJobRepository(
        impl: TrainingJobRepositoryImpl,
    ): TrainingJobRepository

    @Binds
    @Singleton
    abstract fun bindReviewRepository(
        impl: ReviewRepositoryImpl,
    ): ReviewRepository

    @Binds
    @Singleton
    abstract fun bindProblemRepository(
        impl: ProblemRepositoryImpl,
    ): ProblemRepository

    @Binds
    @Singleton
    abstract fun bindCounterRepository(
        impl: CounterRepositoryImpl,
    ): CounterRepository

    @Binds
    @Singleton
    abstract fun bindInteractionRepository(
        impl: InteractionRepositoryImpl,
    ): InteractionRepository

    @Binds
    @Singleton
    abstract fun bindHandleRepository(
        impl: HandleRepositoryImpl,
    ): HandleRepository
}
