package com.rakibjoy.problembuddy.data

import com.rakibjoy.problembuddy.data.repository.CodeforcesRepositoryImpl
import com.rakibjoy.problembuddy.data.repository.TrainingJobRepositoryImpl
import com.rakibjoy.problembuddy.domain.repository.CodeforcesRepository
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
}
