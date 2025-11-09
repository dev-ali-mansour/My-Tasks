package dev.alimansour.mytasks.core.domain.model

data class Task(
    val id: Long = 0,
    val title: String = "",
    val description: String = "",
    val dueDate: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false,
)
