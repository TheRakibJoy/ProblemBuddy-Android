package com.rakibjoy.problembuddy.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "reviews", indices = [Index(value = ["problemId"], unique = true)])
data class ReviewEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val problemId: Long,
    val contestId: Int,
    val problemIndex: String,
    val box: Int,
    val lastReviewAt: Long,
    val nextReviewAt: Long,
    val lastOutcome: String,
)
