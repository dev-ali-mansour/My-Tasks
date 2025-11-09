package dev.alimansour.mytasks.core.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import dev.alimansour.mytasks.core.data.local.db.converter.TaskStatusConverter
import dev.alimansour.mytasks.core.domain.model.TaskStatus

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val dueDate: Long = System.currentTimeMillis(),
    @field:TypeConverters(TaskStatusConverter::class)
    val status: TaskStatus = TaskStatus.Pending,
)
