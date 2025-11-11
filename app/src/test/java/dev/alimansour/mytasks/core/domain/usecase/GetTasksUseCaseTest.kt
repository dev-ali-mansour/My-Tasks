package dev.alimansour.mytasks.core.domain.usecase

import dev.alimansour.mytasks.core.domain.model.DataError
import dev.alimansour.mytasks.core.domain.model.Result
import dev.alimansour.mytasks.core.domain.model.Task
import dev.alimansour.mytasks.core.domain.repository.TasksRepository
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetTasksUseCaseTest {
    private lateinit var getTasksUseCase: GetTasksUseCase
    private lateinit var tasksRepository: TasksRepository

    @BeforeEach
    fun setUp() {
        tasksRepository = mockk()
        getTasksUseCase = GetTasksUseCase(tasksRepository)
    }

    @Test
    fun `should emit Success with tasks when repository returns Success`() =
        runTest {
            // Given
            val tasks =
                listOf(
                    Task(id = 1, title = "Task 1", description = "Description 1", dueDate = System.currentTimeMillis()),
                    Task(id = 2, title = "Task 2", description = "Description 2", dueDate = System.currentTimeMillis()),
                )
            val expectedResult = Result.Success(tasks)
            coEvery { tasksRepository.getTasks() } returns flowOf(expectedResult)

            // When
            val result = getTasksUseCase().first()

            // Then
            assertEquals(expectedResult, result)
        }

    @Test
    fun `should emit Error when repository returns Error`() =
        runTest {
            // Given
            val expectedError = Result.Error(DataError.Local.UNKNOWN)
            coEvery { tasksRepository.getTasks() } returns flowOf(expectedError)

            // When
            val result = getTasksUseCase().first()

            // Then
            assertEquals(expectedError, result)
        }

    @Test
    fun `should emit empty list when repository returns Success with empty list`() =
        runTest {
            // Given
            val expectedResult = Result.Success(emptyList<Task>())
            coEvery { tasksRepository.getTasks() } returns flowOf(expectedResult)

            // When
            val result = getTasksUseCase().first()

            // Then
            assertEquals(expectedResult, result)
        }

    @Test
    fun `should propagate multiple emissions from repository unchanged`() =
        runTest {
            // Given
            val t1 = Task(id = 1, title = "Task 1", description = "D1", dueDate = 1L)
            val t2 = Task(id = 2, title = "Task 2", description = "D2", dueDate = 2L)
            val emissions =
                listOf(
                    Result.Success(emptyList()),
                    Result.Success(listOf(t1)),
                    Result.Success(listOf(t1, t2)),
                    Result.Error(DataError.Local.UNKNOWN),
                )
            coEvery { tasksRepository.getTasks() } returns flowOf(*emissions.toTypedArray())

            // When
            val collected = mutableListOf<Result<List<Task>, DataError.Local>>()
            getTasksUseCase().toList(collected)

            // Then
            assertEquals(emissions, collected)
        }

    @Test
    fun `should invoke repository exactly once when flow is collected`() =
        runTest {
            // Given
            coEvery { tasksRepository.getTasks() } returns flowOf(Result.Success(emptyList()))

            // When
            getTasksUseCase().first()

            // Then
            verify(exactly = 1) { tasksRepository.getTasks().let { } }
        }
}
