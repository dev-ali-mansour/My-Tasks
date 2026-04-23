package dev.alimansour.mytasks.feature.task.update

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import dev.alimansour.mytasks.R
import dev.alimansour.mytasks.core.domain.model.DataError
import dev.alimansour.mytasks.core.domain.model.Result
import dev.alimansour.mytasks.core.domain.model.Task
import dev.alimansour.mytasks.core.domain.usecase.GetTaskByIdUseCase
import dev.alimansour.mytasks.core.domain.usecase.UpdateTaskUseCase
import dev.alimansour.mytasks.core.ui.navigation.Route
import dev.alimansour.mytasks.core.ui.utils.UiText
import dev.alimansour.mytasks.feature.task.TaskEffect
import dev.alimansour.mytasks.feature.task.UpdateTaskEvent
import dev.alimansour.mytasks.feature.task.update.screen.UpdateTaskViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
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
class UpdateTaskViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val updateTaskUseCase: UpdateTaskUseCase = mockk()
    private val getTaskByIdUseCase: GetTaskByIdUseCase = mockk()
    private val savedStateHandle: SavedStateHandle = mockk()
    private lateinit var viewModel: UpdateTaskViewModel

    private val taskId = 1L
    private val task = Task(id = taskId, title = "Initial title", description = "Initial desc", dueDate = 123L)

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic("androidx.navigation.SavedStateHandleKt")
        every { savedStateHandle.toRoute<Route.UpdateTask>() } returns Route.UpdateTask(taskId)
        every { getTaskByIdUseCase(any()) } returns flowOf(Result.Success(task))
        viewModel = UpdateTaskViewModel(
            savedStateHandle = savedStateHandle,
            dispatcher = testDispatcher,
            updateTaskUseCase = updateTaskUseCase,
            getTaskByIdUseCase = getTaskByIdUseCase
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic("androidx.navigation.SavedStateHandleKt")
        Dispatchers.resetMain()
    }

    @Test
    fun `initialization loads task and updates state`() =
        runTest(testDispatcher) {
            val job = launch { viewModel.uiState.collect { } }
            testDispatcher.scheduler.advanceUntilIdle()

            // THEN
            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertEquals(taskId, state.id)
            assertEquals(task.title, state.title)
            assertEquals(task.description, state.description)
            assertEquals(task.dueDate, state.dueDate)

            job.cancel()
        }

    @Test
    fun `UpdateTitle updates title in state`() =
        runTest(testDispatcher) {
            // GIVEN
            val job = launch(testDispatcher) { viewModel.uiState.collect { } }
            testDispatcher.scheduler.advanceUntilIdle()

            // WHEN
            viewModel.processEvent(UpdateTaskEvent.UpdateTitle("Updated title"))
            testDispatcher.scheduler.advanceUntilIdle()

            // THEN
            assertEquals("Updated title", viewModel.uiState.value.title)

            job.cancel()
        }

    @Test
    fun `UpdateDescription updates description in state`() =
        runTest(testDispatcher) {
            // GIVEN
            val job = launch(testDispatcher) { viewModel.uiState.collect { } }
            testDispatcher.scheduler.advanceUntilIdle()

            // WHEN
            viewModel.processEvent(UpdateTaskEvent.UpdateDescription("Updated description"))
            testDispatcher.scheduler.advanceUntilIdle()

            // THEN
            assertEquals("Updated description", viewModel.uiState.value.description)

            job.cancel()
        }

    @Test
    fun `UpdateDueDate updates dueDate in state`() =
        runTest(testDispatcher) {
            // GIVEN
            val job = launch(testDispatcher) { viewModel.uiState.collect { } }
            testDispatcher.scheduler.advanceUntilIdle()
            val dueDate = 99999L

            // WHEN
            viewModel.processEvent(UpdateTaskEvent.UpdateDueDate(dueDate))
            testDispatcher.scheduler.advanceUntilIdle()

            // THEN
            assertEquals(dueDate, viewModel.uiState.value.dueDate)

            job.cancel()
        }

    @Test
    fun `Proceed with blank title emits ShowError with title message`() =
        runTest(testDispatcher) {
            // GIVEN
            val effects = mutableListOf<TaskEffect>()
            val job = launch(testDispatcher) { viewModel.uiState.collect { } }
            val effectJob = launch(testDispatcher) { viewModel.effect.collect { effects.add(it) } }
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.processEvent(UpdateTaskEvent.UpdateTitle(""))
            viewModel.processEvent(UpdateTaskEvent.UpdateDescription("Some description"))
            testDispatcher.scheduler.advanceUntilIdle()

            // WHEN
            viewModel.processEvent(UpdateTaskEvent.Proceed)
            testDispatcher.scheduler.advanceUntilIdle()

            // THEN
            assertTrue(effects.last() is TaskEffect.ShowError)
            val effect = effects.last() as TaskEffect.ShowError
            assertTrue(effect.message is UiText.StringResourceId)
            assertEquals(R.string.title_cannot_be_empty, (effect.message as UiText.StringResourceId).id)

            job.cancel()
            effectJob.cancel()
        }

    @Test
    fun `Proceed with blank description emits ShowError with description message`() =
        runTest(testDispatcher) {
            // GIVEN
            val effects = mutableListOf<TaskEffect>()
            val job = launch(testDispatcher) { viewModel.uiState.collect { } }
            val effectJob = launch(testDispatcher) { viewModel.effect.collect { effects.add(it) } }
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.processEvent(UpdateTaskEvent.UpdateTitle("Title"))
            viewModel.processEvent(UpdateTaskEvent.UpdateDescription(""))
            testDispatcher.scheduler.advanceUntilIdle()

            // WHEN
            viewModel.processEvent(UpdateTaskEvent.Proceed)
            testDispatcher.scheduler.advanceUntilIdle()

            // THEN
            assertTrue(effects.last() is TaskEffect.ShowError)
            val effect = effects.last() as TaskEffect.ShowError
            assertTrue(effect.message is UiText.StringResourceId)
            assertEquals(R.string.description_cannot_be_empty, (effect.message as UiText.StringResourceId).id)

            job.cancel()
            effectJob.cancel()
        }

    @Test
    fun `Proceed with valid data and success result sets ShowSuccess effect and stops loading`() =
        runTest(testDispatcher) {
            // GIVEN
            val results = flowOf<Result<Unit, DataError.Local>>(Result.Success(Unit))
            coEvery { updateTaskUseCase(any()) } returns results
            val effects = mutableListOf<TaskEffect>()
            val job = launch(testDispatcher) { viewModel.uiState.collect { } }
            val effectJob = launch(testDispatcher) { viewModel.effect.collect { effects.add(it) } }
            testDispatcher.scheduler.advanceUntilIdle()

            // WHEN
            viewModel.processEvent(UpdateTaskEvent.Proceed)
            testDispatcher.scheduler.advanceUntilIdle()

            // THEN
            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertTrue(effects.last() is TaskEffect.ShowSuccess)

            job.cancel()
            effectJob.cancel()
        }

    @Test
    fun `Proceed with valid data and error result sets ShowError effect and stops loading`() =
        runTest(testDispatcher) {
            // GIVEN
            val error = DataError.Local.DATABASE_WRITE_ERROR
            val results = flowOf<Result<Unit, DataError.Local>>(Result.Error(error))
            coEvery { updateTaskUseCase(any()) } returns results
            val effects = mutableListOf<TaskEffect>()
            val job = launch(testDispatcher) { viewModel.uiState.collect { } }
            val effectJob = launch(testDispatcher) { viewModel.effect.collect { effects.add(it) } }
            testDispatcher.scheduler.advanceUntilIdle()

            // WHEN
            viewModel.processEvent(UpdateTaskEvent.Proceed)
            testDispatcher.scheduler.advanceUntilIdle()

            // THEN
            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertTrue(effects.last() is TaskEffect.ShowError)

            job.cancel()
            effectJob.cancel()
        }

    @Test
    fun `when updateTaskUseCase is loading then isLoading is true during update and false after completion`() =
        runTest(testDispatcher) {
            // GIVEN
            val updateResults = MutableSharedFlow<Result<Unit, DataError.Local>>()
            coEvery { updateTaskUseCase(any()) } returns updateResults
            val loadingStates = mutableListOf<Boolean>()
            val job = launch(testDispatcher) { viewModel.uiState.collect { state -> loadingStates.add(state.isLoading) } }
            testDispatcher.scheduler.advanceUntilIdle()

            // WHEN
            viewModel.processEvent(UpdateTaskEvent.Proceed)
            testDispatcher.scheduler.advanceUntilIdle()

            // THEN
            assertTrue(loadingStates.contains(true))

            // WHEN
            updateResults.emit(Result.Success(Unit))
            testDispatcher.scheduler.advanceUntilIdle()

            // THEN
            assertFalse(viewModel.uiState.value.isLoading)

            job.cancel()
        }

    @Test
    fun `Proceed with valid data passes correct Task to use case`() =
        runTest(testDispatcher) {
            // GIVEN
            val capturedTask = slot<Task>()
            coEvery { updateTaskUseCase(capture(capturedTask)) } returns flowOf(Result.Success(Unit))
            val job = launch(testDispatcher) { viewModel.uiState.collect { } }
            testDispatcher.scheduler.advanceUntilIdle()

            // WHEN
            viewModel.processEvent(UpdateTaskEvent.Proceed)
            testDispatcher.scheduler.advanceUntilIdle()

            // THEN
            coVerify(exactly = 1) { updateTaskUseCase(any()).let { } }
            assertEquals(task.id, capturedTask.captured.id)
            assertEquals(task.title, capturedTask.captured.title)
            assertEquals(task.description, capturedTask.captured.description)
            assertEquals(task.dueDate, capturedTask.captured.dueDate)

            job.cancel()
        }
}
