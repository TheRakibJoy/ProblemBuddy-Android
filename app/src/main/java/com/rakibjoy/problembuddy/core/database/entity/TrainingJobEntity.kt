package com.rakibjoy.problembuddy.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "training_jobs")
data class TrainingJobEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val handle: String,
    val status: String,
    val currentTier: String,
    val done: Int,
    val total: Int,
    val error: String,
    val updatedAt: Long,
)
