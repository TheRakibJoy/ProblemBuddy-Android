package com.rakibjoy.problembuddy.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "counters",
    indices = [Index(value = ["tagName", "tier"], unique = true)],
)
data class CounterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tagName: String,
    val tier: String,
    val count: Int,
)
