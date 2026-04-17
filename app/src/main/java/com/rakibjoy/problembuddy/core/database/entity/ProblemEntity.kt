package com.rakibjoy.problembuddy.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "problems",
    indices = [
        Index(value = ["tier", "rating"]),
        Index(value = ["tier", "contestId", "problemIndex"], unique = true),
    ],
)
data class ProblemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tier: String,
    val contestId: Int,
    val problemIndex: String,
    val rating: Int?,
    val tags: String,
)
