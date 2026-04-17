package com.rakibjoy.problembuddy.data.mapper

import com.rakibjoy.problembuddy.core.database.entity.TrainingJobEntity
import com.rakibjoy.problembuddy.domain.model.Tier
import com.rakibjoy.problembuddy.domain.model.TrainingJob

fun TrainingJobEntity.toDomain(): TrainingJob = TrainingJob(
    id = id,
    handle = handle,
    status = when (status.lowercase()) {
        "queued" -> TrainingJob.Status.QUEUED
        "running" -> TrainingJob.Status.RUNNING
        "success" -> TrainingJob.Status.SUCCESS
        "failed" -> TrainingJob.Status.FAILED
        else -> TrainingJob.Status.QUEUED
    },
    currentTier = currentTier.takeIf { it.isNotEmpty() }?.let { name ->
        runCatching { Tier.valueOf(name) }.getOrNull()
    },
    done = done,
    total = total,
    error = error.takeIf { it.isNotEmpty() },
    updatedAt = updatedAt,
)

fun TrainingJob.toEntity(): TrainingJobEntity = TrainingJobEntity(
    id = id,
    handle = handle,
    status = when (status) {
        TrainingJob.Status.QUEUED -> "queued"
        TrainingJob.Status.RUNNING -> "running"
        TrainingJob.Status.SUCCESS -> "success"
        TrainingJob.Status.FAILED -> "failed"
    },
    currentTier = currentTier?.name.orEmpty(),
    done = done,
    total = total,
    error = error.orEmpty(),
    updatedAt = updatedAt,
)
