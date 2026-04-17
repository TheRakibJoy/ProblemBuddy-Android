package com.rakibjoy.problembuddy.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "interactions",
    indices = [Index(value = ["problemId"], unique = true)],
)
data class InteractionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val problemId: Long,
    val status: String,
    val createdAt: Long,
)
