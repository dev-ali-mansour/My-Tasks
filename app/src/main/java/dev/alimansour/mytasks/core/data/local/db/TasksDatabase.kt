package dev.alimansour.mytasks.core.data.local.db

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteColumn
import androidx.room.RoomDatabase
import androidx.room.migration.AutoMigrationSpec
import androidx.sqlite.db.SupportSQLiteDatabase
import dev.alimansour.mytasks.core.data.local.db.dao.TaskDao
import dev.alimansour.mytasks.core.data.local.db.entity.TaskEntity

@Database(
    entities = [TaskEntity::class],
    version = 2,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2, spec = TasksDatabase.AutoMigrationSpec2::class),
    ],
)
abstract class TasksDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    @DeleteColumn.Entries(
        DeleteColumn(tableName = "tasks", columnName = "status"),
    )
    class AutoMigrationSpec2 : AutoMigrationSpec {
        @Override
        override fun onPostMigrate(db: SupportSQLiteDatabase) = Unit
    }
}
