package dev.alimansour.mytasks.core.domain.repository

import dev.alimansour.mytasks.core.domain.model.DataError
import dev.alimansour.mytasks.core.domain.model.Task
import dev.alimansour.mytasks.core.domain.model.Result
import kotlinx.coroutines.flow.Flow

interface TasksRepository {
    fun getTasks(): Flow<Result<List<Task>, DataError.Local>>

    fun addTask(task: Task): Flow<Result<Unit, DataError.Local>>

    fun updateTask(task: Task): Flow<Result<Unit, DataError.Local>>

    fun deleteTask(task: Task): Flow<Result<Unit, DataError.Local>>
}
