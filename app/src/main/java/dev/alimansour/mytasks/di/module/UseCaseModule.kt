package dev.alimansour.mytasks.di.module

import dev.alimansour.mytasks.core.domain.repository.TasksRepository
import dev.alimansour.mytasks.core.domain.usecase.AddTaskUseCase
import dev.alimansour.mytasks.core.domain.usecase.DeleteTaskUseCase
import dev.alimansour.mytasks.core.domain.usecase.GetTasksUseCase
import dev.alimansour.mytasks.core.domain.usecase.UpdateTaskUseCase
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module(includes = [ RoomModule::class])
@ComponentScan
class UseCaseModule {
    @Single
    fun provideAddTaskUseCase(repository: TasksRepository) = AddTaskUseCase(repository)

    @Single
    fun provideUpdateTaskUseCase(repository: TasksRepository) = UpdateTaskUseCase(repository)

    @Single
    fun provideDeleteTaskUseCase(repository: TasksRepository) = DeleteTaskUseCase(repository)

    @Single
    fun provideGetTasksUseCase(repository: TasksRepository) = GetTasksUseCase(repository)
}
