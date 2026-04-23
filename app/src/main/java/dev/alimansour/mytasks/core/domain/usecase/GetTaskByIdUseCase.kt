package dev.alimansour.mytasks.core.domain.usecase

import dev.alimansour.mytasks.core.domain.model.DataError
import dev.alimansour.mytasks.core.domain.model.Result
import dev.alimansour.mytasks.core.domain.model.Task
import dev.alimansour.mytasks.core.domain.repository.TasksRepository
import kotlinx.coroutines.flow.Flow

class GetTaskByIdUseCase(
    private val repository: TasksRepository,
) {
    operator fun invoke(id: Long): Flow<Result<Task, DataError.Local>> = repository.getTaskById(id)
}
