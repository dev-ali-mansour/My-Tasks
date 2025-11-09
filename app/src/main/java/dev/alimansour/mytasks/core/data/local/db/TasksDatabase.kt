package dev.alimansour.mytasks.core.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import dev.alimansour.mytasks.core.data.local.db.converter.TaskStatusConverter
import dev.alimansour.mytasks.core.data.local.db.dao.TaskDao
import dev.alimansour.mytasks.core.data.local.db.entity.TaskEntity

@Database(entities = [TaskEntity::class], version = 1, exportSchema = true)
@TypeConverters(TaskStatusConverter::class)
abstract class TasksDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
}
