package dev.alimansour.mytasks.core.data.repository

import dev.alimansour.mytasks.core.data.local.db.dao.TaskDao
import dev.alimansour.mytasks.core.data.local.db.entity.TaskEntity
import dev.alimansour.mytasks.core.domain.model.DataError
import dev.alimansour.mytasks.core.domain.model.Result
import dev.alimansour.mytasks.core.domain.model.Task
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TasksRepositoryImplTest {
    private lateinit var taskDao: TaskDao
    private lateinit var repository: TasksRepositoryImpl

    @BeforeEach
    fun setup() {
        taskDao = mockk()
        repository = TasksRepositoryImpl(taskDao)
    }

    private fun sampleEntity(
        id: Long = 1L,
        title: String = "Title",
        description: String = "Desc",
        dueDate: Long = 1000L,
        isCompleted: Boolean = false,
    ) = TaskEntity(id, title, description, dueDate, isCompleted)

    private fun sampleTask(
        id: Long = 1L,
        title: String = "Title",
        description: String = "Desc",
        dueDate: Long = 1000L,
        isCompleted: Boolean = false,
    ) = Task(id, title, description, dueDate, isCompleted)

    @Test
    fun `getTasks should emit Success with mapped tasks when dao emits entities`() =
        runTest {
            // Given
            val entities =
                listOf(
                    sampleEntity(id = 1, title = "T1", description = "D1", dueDate = 10L, isCompleted = false),
                    sampleEntity(id = 2, title = "T2", description = "D2", dueDate = 20L, isCompleted = true),
                )
            every { taskDao.getTasks() } returns flowOf(entities)

            // When
            val result = repository.getTasks().first()

            // Then
            val expected =
                Result.Success(
                    listOf(
                        sampleTask(id = 1, title = "T1", description = "D1", dueDate = 10L, isCompleted = false),
                        sampleTask(id = 2, title = "T2", description = "D2", dueDate = 20L, isCompleted = true),
                    ),
                )
            assertEquals(expected, result)
            verify(exactly = 1) { taskDao.getTasks().let { } }
        }

    @Test
    fun `getTasks should emit Error when upstream throws before any emission`() =
        runTest {
            // Given
            every { taskDao.getTasks() } returns flow { throw RuntimeException("db error") }

            // When
            val result = repository.getTasks().first()

            // Then
            assertEquals(Result.Error(DataError.Local.DATABASE_READ_ERROR), result)
        }

    @Test
    fun `getTasks should emit mapped list then Error when upstream throws after first emission`() =
        runTest {
            // Given
            val entitiesFirst = listOf(sampleEntity(id = 1))
            every { taskDao.getTasks() } returns
                flow {
                    emit(entitiesFirst)
                    throw RuntimeException("boom after first emit")
                }

            // When
            val collected = mutableListOf<Result<List<Task>, DataError.Local>>()
            repository.getTasks().toList(collected)

            // Then
            val expectedFirst = Result.Success(listOf(sampleTask(id = 1)))
            val expectedSecond = Result.Error(DataError.Local.DATABASE_READ_ERROR)
            assertEquals(listOf(expectedFirst, expectedSecond), collected)
        }

    @Test
    fun `addTask should emit Success and pass mapped entity to dao`() =
        runTest {
            // Given
            val task = sampleTask(id = 0, title = "New", description = "N", dueDate = 999L)
            val expectedEntity = TaskEntity(id = 0, title = "New", description = "N", dueDate = 999L, isCompleted = false)
            coEvery { taskDao.insertTask(expectedEntity) } returns 10L

            // When
            val result = repository.addTask(task).first()

            // Then
            assertEquals(Result.Success(Unit), result)
            coVerify(exactly = 1) { taskDao.insertTask(expectedEntity) }
        }

    @Test
    fun `addTask should emit Error when dao throws`() =
        runTest {
            // Given
            val task = sampleTask()
            coEvery { taskDao.insertTask(any()) } throws RuntimeException("insert fail")

            // When
            val result = repository.addTask(task).first()

            // Then
            assertEquals(Result.Error(DataError.Local.DATABASE_WRITE_ERROR), result)
        }

    @Test
    fun `updateTask should emit Success when affectedRows greater than zero`() =
        runTest {
            // Given
            val task = sampleTask()
            coEvery { taskDao.updateTask(any()) } returns 1

            // When
            val result = repository.updateTask(task).first()

            // Then
            assertEquals(Result.Success(Unit), result)
            coVerify(exactly = 1) { taskDao.updateTask(match { it.id == task.id && it.title == task.title }) }
        }

    @Test
    fun `updateTask should emit Error when affectedRows equals zero`() =
        runTest {
            // Given
            val task = sampleTask()
            coEvery { taskDao.updateTask(any()) } returns 0

            // When
            val result = repository.updateTask(task).first()

            // Then
            assertEquals(Result.Error(DataError.Local.DATABASE_WRITE_ERROR), result)
        }

    @Test
    fun `updateTask should emit Error when dao throws`() =
        runTest {
            // Given
            val task = sampleTask()
            coEvery { taskDao.updateTask(any()) } throws RuntimeException("update fail")

            // When
            val result = repository.updateTask(task).first()

            // Then
            assertEquals(Result.Error(DataError.Local.DATABASE_WRITE_ERROR), result)
        }

    @Test
    fun `deleteTask should emit Success when affectedRows greater than zero`() =
        runTest {
            // Given
            val task = sampleTask()
            coEvery { taskDao.deleteTask(any()) } returns 1

            // When
            val result = repository.deleteTask(task).first()

            // Then
            assertEquals(Result.Success(Unit), result)
            coVerify(exactly = 1) { taskDao.deleteTask(match { it.id == task.id }) }
        }

    @Test
    fun `deleteTask should emit Error when affectedRows equals zero`() =
        runTest {
            // Given
            val task = sampleTask()
            coEvery { taskDao.deleteTask(any()) } returns 0

            // When
            val result = repository.deleteTask(task).first()

            // Then
            assertEquals(Result.Error(DataError.Local.DATABASE_WRITE_ERROR), result)
        }

    @Test
    fun `deleteTask should emit Error when dao throws`() =
        runTest {
            // Given
            val task = sampleTask()
            coEvery { taskDao.deleteTask(any()) } throws RuntimeException("delete fail")

            // When
            val result = repository.deleteTask(task).first()

            // Then
            assertEquals(Result.Error(DataError.Local.DATABASE_WRITE_ERROR), result)
        }
}
