package dev.alimansour.mytasks.di.module

import android.content.Context
import androidx.room.Room
import dev.alimansour.mytasks.core.data.local.db.TasksDatabase
import dev.alimansour.mytasks.core.data.local.db.dao.TaskDao
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@ComponentScan
class RoomModule {
    @Single
    fun provideTasksDatabase(context: Context): TasksDatabase =
        Room
            .databaseBuilder(
                context,
                TasksDatabase::class.java,
                "MyTasks.db",
            ).build()

    @Single
    fun provideTaskDao(db: TasksDatabase): TaskDao = db.taskDao()
}
