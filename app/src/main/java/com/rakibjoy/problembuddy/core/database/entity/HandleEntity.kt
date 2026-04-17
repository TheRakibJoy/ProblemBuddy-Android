package com.rakibjoy.problembuddy.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "handles")
data class HandleEntity(
    @PrimaryKey val handle: String,
)
