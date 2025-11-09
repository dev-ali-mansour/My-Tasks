package dev.alimansour.mytasks.core.data.local.db.converter

import androidx.room.TypeConverter
import dev.alimansour.mytasks.core.domain.model.TaskStatus

class TaskStatusConverter {
    @TypeConverter
    fun fromTaskStatus(status: TaskStatus?): String? = status?.name

    @TypeConverter
    fun toTaskStatus(value: String?): TaskStatus? {
        if (value == null) return null
        return try {
            TaskStatus.valueOf(value)
        } catch (e: IllegalArgumentException) {
            // If unknown value is stored in DB, default to Pending
            TaskStatus.Pending
        }
    }
}
