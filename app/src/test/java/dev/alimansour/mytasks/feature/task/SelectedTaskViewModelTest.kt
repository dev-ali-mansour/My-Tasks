package dev.alimansour.mytasks.feature.task

import dev.alimansour.mytasks.core.domain.model.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SelectedTaskViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: SelectedTaskViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = SelectedTaskViewModel()
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has null selectedTask`() =
        runTest(testDispatcher) {
            // THEN
            assertNull(viewModel.selectedTask.value)
        }

    @Test
    fun `onSelectTask with non null task updates selectedTask`() =
        runTest(testDispatcher) {
            // GIVEN
            val task = Task(id = 1L, title = "Task", description = "Desc", isCompleted = false, dueDate = 0L)

            // WHEN
            viewModel.onSelectTask(task)
            testDispatcher.scheduler.advanceUntilIdle()

            // THEN
            assertEquals(task, viewModel.selectedTask.value)
        }

    @Test
    fun `onSelectTask with null clears selectedTask`() =
        runTest(testDispatcher) {
            // GIVEN
            val task = Task(id = 1L, title = "Task", description = "Desc", isCompleted = false, dueDate = 0L)
            viewModel.onSelectTask(task)
            testDispatcher.scheduler.advanceUntilIdle()
            assertEquals(task, viewModel.selectedTask.value)

            // WHEN
            viewModel.onSelectTask(null)
            testDispatcher.scheduler.advanceUntilIdle()

            // THEN
            assertNull(viewModel.selectedTask.value)
        }
}
