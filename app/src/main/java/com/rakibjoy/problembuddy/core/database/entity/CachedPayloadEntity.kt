package com.rakibjoy.problembuddy.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cached_payloads",
    indices = [Index(value = ["cacheKey"], unique = true)],
)
data class CachedPayloadEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cacheKey: String,
    val payloadJson: String,
    val fetchedAt: Long,
)
