package dev.alimansour.mytasks.core.data.repository

import dev.alimansour.mytasks.core.data.local.db.dao.TaskDao
import dev.alimansour.mytasks.core.data.local.db.entity.TaskEntity
import dev.alimansour.mytasks.core.data.mappers.toTask
import dev.alimansour.mytasks.core.data.mappers.toTaskEntity
import dev.alimansour.mytasks.core.domain.model.DataError
import dev.alimansour.mytasks.core.domain.model.Result
import dev.alimansour.mytasks.core.domain.model.Task
import dev.alimansour.mytasks.core.domain.repository.TasksRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

@Single
class TasksRepositoryImpl(
    private val taskDao: TaskDao,
) : TasksRepository {
    override fun getTasks(): Flow<Result<List<Task>, DataError.Local>> =
        taskDao
            .getTasks()
            .map<List<TaskEntity>, Result<List<Task>, DataError.Local>> { tasks ->
                Result.Success(tasks.map { it.toTask() })
            }.catch {
                // Log the exception 'it' if needed
                emit(Result.Error(DataError.Local.DATABASE_READ_ERROR))
            }

    override fun addTask(task: Task): Flow<Result<Unit, DataError.Local>> =
        flow {
            runCatching {
                taskDao.insertTask(task.toTaskEntity())
                emit(Result.Success(Unit))
            }.onFailure { error ->
                when (error) {
                    is CancellationException -> throw error
                    else -> emit(Result.Error(DataError.Local.DATABASE_WRITE_ERROR))
                }
            }
        }

    override fun updateTask(task: Task): Flow<Result<Unit, DataError.Local>> =
        flow {
            runCatching {
                val affectedRows = taskDao.updateTask(task.toTaskEntity())
                if (affectedRows > 0) {
                    emit(Result.Success(Unit))
                } else {
                    emit(Result.Error(DataError.Local.DATABASE_WRITE_ERROR))
                }
            }.onFailure { error ->
                when (error) {
                    is CancellationException -> throw error
                    else -> emit(Result.Error(DataError.Local.DATABASE_WRITE_ERROR))
                }
            }
        }

    override fun deleteTask(task: Task): Flow<Result<Unit, DataError.Local>> =
        flow {
            runCatching {
                val affectedRows = taskDao.deleteTask(task.toTaskEntity())
                if (affectedRows > 0) {
                    emit(Result.Success(Unit))
                } else {
                    emit(Result.Error(DataError.Local.DATABASE_WRITE_ERROR))
                }
            }.onFailure { error ->
                when (error) {
                    is CancellationException -> throw error
                    else -> emit(Result.Error(DataError.Local.DATABASE_WRITE_ERROR))
                }
            }
        }
}
