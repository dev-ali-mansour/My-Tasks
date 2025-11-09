package dev.alimansour.mytasks.core.data.mappers

import dev.alimansour.mytasks.core.data.local.db.entity.TaskEntity
import dev.alimansour.mytasks.core.domain.model.Task

fun TaskEntity.toTask(): Task =
    Task(
        title = title,
        description = description,
        dueDate = dueDate,
        status = status,
    )

fun Task.toTaskEntity(): TaskEntity =
    TaskEntity(
        title = title,
        description = description,
        dueDate = dueDate,
        status = status,
    )
