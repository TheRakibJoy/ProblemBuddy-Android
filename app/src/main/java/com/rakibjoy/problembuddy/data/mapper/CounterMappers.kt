package com.rakibjoy.problembuddy.data.mapper

import com.rakibjoy.problembuddy.core.database.entity.CounterEntity
import com.rakibjoy.problembuddy.domain.model.TagCounter
import com.rakibjoy.problembuddy.domain.model.Tier

internal fun parseTier(tierString: String): Tier =
    Tier.entries.firstOrNull { it.name.equals(tierString, ignoreCase = true) } ?: Tier.NEWBIE

fun CounterEntity.toDomain(): TagCounter = TagCounter(
    tagName = tagName,
    tier = parseTier(tier),
    count = count,
)

fun TagCounter.toEntity(): CounterEntity = CounterEntity(
    tagName = tagName,
    tier = tier.name.lowercase(),
    count = count,
)
