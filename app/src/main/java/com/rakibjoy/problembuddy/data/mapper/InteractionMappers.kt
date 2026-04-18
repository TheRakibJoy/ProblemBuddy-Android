package com.rakibjoy.problembuddy.data.mapper

import com.rakibjoy.problembuddy.core.database.entity.InteractionEntity
import com.rakibjoy.problembuddy.domain.model.Interaction

private fun Interaction.Status.toWire(): String = when (this) {
    Interaction.Status.SOLVED -> "solved"
    Interaction.Status.NOT_INTERESTED -> "not_interested"
    Interaction.Status.HIDDEN -> "hidden"
    Interaction.Status.SAVED -> "saved"
}

private fun String.toStatus(): Interaction.Status = when (this) {
    "solved" -> Interaction.Status.SOLVED
    "not_interested" -> Interaction.Status.NOT_INTERESTED
    "hidden" -> Interaction.Status.HIDDEN
    "saved" -> Interaction.Status.SAVED
    else -> Interaction.Status.HIDDEN
}

fun InteractionEntity.toDomain(): Interaction = Interaction(
    problemId = problemId,
    status = status.toStatus(),
    createdAt = createdAt,
)

fun Interaction.toEntity(): InteractionEntity = InteractionEntity(
    problemId = problemId,
    status = status.toWire(),
    createdAt = createdAt,
)
