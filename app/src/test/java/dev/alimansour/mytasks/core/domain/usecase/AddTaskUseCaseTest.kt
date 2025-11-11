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

class AddTaskUseCaseTest {
    private lateinit var addTaskUseCase: AddTaskUseCase
    private lateinit var tasksRepository: TasksRepository
    private val task =
        Task(
            id = 0,
            title = "New Task",
            description = "Write unit tests",
            dueDate = 1_700_000_000_000L,
            isCompleted = false,
        )

    @BeforeEach
    fun setUp() {
        tasksRepository = mockk()
        addTaskUseCase = AddTaskUseCase(tasksRepository)
    }

    @Test
    fun `should emit Success when repository returns Success`() =
        runTest {
            // Given
            val expected = Result.Success(Unit)
            coEvery { tasksRepository.addTask(task) } returns flowOf(expected)

            // When
            val result = addTaskUseCase(task).first()

            // Then
            assertEquals(expected, result)
        }

    @Test
    fun `should emit Error when repository returns Error`() =
        runTest {
            // Given
            val expected = Result.Error(DataError.Local.DATABASE_WRITE_ERROR)
            coEvery { tasksRepository.addTask(task) } returns flowOf(expected)

            // When
            val result = addTaskUseCase(task).first()

            // Then
            assertEquals(expected, result)
        }

    @Test
    fun `should invoke repository exactly once with the same task when flow is collected`() =
        runTest {
            // Given
            coEvery { tasksRepository.addTask(task) } returns flowOf(Result.Success(Unit))

            // When
            addTaskUseCase(task).first()

            // Then
            verify(exactly = 1) { tasksRepository.addTask(task).let { } }
        }

    @Test
    fun `should propagate multiple emissions from repository unchanged`() =
        runTest {
            val emissions =
                listOf(
                    Result.Error(DataError.Local.DATABASE_WRITE_ERROR),
                    Result.Success(Unit),
                )
            coEvery { tasksRepository.addTask(task) } returns flowOf(*emissions.toTypedArray())

            val collected = mutableListOf<Result<Unit, DataError.Local>>()

            // When
            addTaskUseCase(task).toList(collected)

            // Then
            assertEquals(emissions, collected)
        }
}
