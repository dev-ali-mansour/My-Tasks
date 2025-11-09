package dev.alimansour.mytasks.core.domain.model

data class Task(
    val title: String = "",
    val description: String = "",
    val dueDate: Long = System.currentTimeMillis(),
    val status: TaskStatus = TaskStatus.Pending,
)
