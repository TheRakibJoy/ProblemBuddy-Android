package com.rakibjoy.problembuddy.core.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteTable
import androidx.room.RoomDatabase
import androidx.room.migration.AutoMigrationSpec
import com.rakibjoy.problembuddy.core.database.dao.CachedPayloadDao
import com.rakibjoy.problembuddy.core.database.dao.CounterDao
import com.rakibjoy.problembuddy.core.database.dao.HandleDao
import com.rakibjoy.problembuddy.core.database.dao.InteractionDao
import com.rakibjoy.problembuddy.core.database.dao.ProblemDao
import com.rakibjoy.problembuddy.core.database.dao.TrainingJobDao
import com.rakibjoy.problembuddy.core.database.entity.CachedPayloadEntity
import com.rakibjoy.problembuddy.core.database.entity.CounterEntity
import com.rakibjoy.problembuddy.core.database.entity.HandleEntity
import com.rakibjoy.problembuddy.core.database.entity.InteractionEntity
import com.rakibjoy.problembuddy.core.database.entity.ProblemEntity
import com.rakibjoy.problembuddy.core.database.entity.TrainingJobEntity

@Database(
    version = 4,
    exportSchema = true,
    entities = [
        ProblemEntity::class,
        CounterEntity::class,
        HandleEntity::class,
        InteractionEntity::class,
        TrainingJobEntity::class,
        CachedPayloadEntity::class,
    ],
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4, spec = ProblemBuddyDatabase.DropReviewsMigration::class),
    ],
)
abstract class ProblemBuddyDatabase : RoomDatabase() {
    abstract fun problemDao(): ProblemDao
    abstract fun counterDao(): CounterDao
    abstract fun handleDao(): HandleDao
    abstract fun interactionDao(): InteractionDao
    abstract fun trainingJobDao(): TrainingJobDao
    abstract fun cachedPayloadDao(): CachedPayloadDao

    @DeleteTable(tableName = "reviews")
    class DropReviewsMigration : AutoMigrationSpec
}
