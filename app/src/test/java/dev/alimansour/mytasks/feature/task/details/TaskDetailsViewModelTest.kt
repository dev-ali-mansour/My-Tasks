package dev.alimansour.mytasks.feature.task.details

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import dev.alimansour.mytasks.core.domain.model.DataError
import dev.alimansour.mytasks.core.domain.model.Result
import dev.alimansour.mytasks.core.domain.model.Task
import dev.alimansour.mytasks.core.domain.usecase.DeleteTaskUseCase
import dev.alimansour.mytasks.core.domain.usecase.GetTaskByIdUseCase
import dev.alimansour.mytasks.core.ui.navigation.Route
import dev.alimansour.mytasks.core.ui.utils.toUiText
import dev.alimansour.mytasks.feature.task.details.screen.TaskDetailsEffect
import dev.alimansour.mytasks.feature.task.details.screen.TaskDetailsEvent
import dev.alimansour.mytasks.feature.task.details.screen.TaskDetailsViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TaskDetailsViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val deleteTaskUseCase: DeleteTaskUseCase = mockk()
    private val getTaskByIdUseCase: GetTaskByIdUseCase = mockk()
    private val savedStateHandle: SavedStateHandle = mockk()
    private lateinit var viewModel: TaskDetailsViewModel

    private val sampleTask = Task(id = 1, title = "Title", description = "Desc", dueDate = 123L, isCompleted = false)

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic("androidx.navigation.SavedStateHandleKt")
        every { savedStateHandle.toRoute<Route.TaskDetails>() } returns Route.TaskDetails(sampleTask.id)
        every { getTaskByIdUseCase(any()) } returns flowOf(Result.Success(sampleTask))
        viewModel = TaskDetailsViewModel(
            savedStateHandle = savedStateHandle,
            dispatcher = testDispatcher,
            deleteTaskUseCase = deleteTaskUseCase,
            getTaskByIdUseCase = getTaskByIdUseCase
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic("androidx.navigation.SavedStateHandleKt")
        Dispatchers.resetMain()
    }

    @Test
    fun `initialization loads task from database`() =
        runTest(testDispatcher) {
            val job = launch { viewModel.uiState.collect { } }
            testDispatcher.scheduler.advanceUntilIdle()

            // THEN
            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertEquals(sampleTask, state.task)

            job.cancel()
        }

    @Test
    fun `DeleteTask success sets loading then emits ShowSuccess effect`() =
        runTest(testDispatcher) {
            // GIVEN
            val effects = mutableListOf<TaskDetailsEffect>()
            val job = launch { viewModel.uiState.collect { } }
            val effectJob = launch { viewModel.effect.collect { effects.add(it) } }
            val successFlow = flowOf(Result.Success(Unit))
            coEvery { deleteTaskUseCase(sampleTask) } returns successFlow
            testDispatcher.scheduler.advanceUntilIdle()

            // WHEN
            viewModel.processEvent(TaskDetailsEvent.DeleteTask)
            testDispatcher.scheduler.advanceUntilIdle()

            // THEN
            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertTrue(effects.isNotEmpty())
            assertEquals(TaskDetailsEffect.ShowSuccess, effects.last())

            job.cancel()
            effectJob.cancel()
        }

    @Test
    fun `DeleteTask error sets loading then emits ShowError effect with correct message`() =
        runTest(testDispatcher) {
            // GIVEN
            val effects = mutableListOf<TaskDetailsEffect>()
            val job = launch { viewModel.uiState.collect { } }
            val effectJob = launch { viewModel.effect.collect { effects.add(it) } }
            val error = DataError.Local.DATABASE_WRITE_ERROR
            coEvery { deleteTaskUseCase(sampleTask) } returns flowOf(Result.Error(error))
            testDispatcher.scheduler.advanceUntilIdle()

            // WHEN
            viewModel.processEvent(TaskDetailsEvent.DeleteTask)
            testDispatcher.scheduler.advanceUntilIdle()

            // THEN
            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertTrue(effects.isNotEmpty())
            assertTrue(effects.last() is TaskDetailsEffect.ShowError)
            val effect = effects.last() as TaskDetailsEffect.ShowError

            val expected = error.toUiText()
            assertEquals(expected::class, effect.message::class)

            job.cancel()
            effectJob.cancel()
        }
}
