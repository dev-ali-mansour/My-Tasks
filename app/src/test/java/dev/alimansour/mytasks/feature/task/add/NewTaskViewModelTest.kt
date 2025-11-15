package dev.alimansour.mytasks.feature.task.add

import dev.alimansour.mytasks.core.domain.model.DataError
import dev.alimansour.mytasks.core.domain.model.Result
import dev.alimansour.mytasks.core.domain.model.Task
import dev.alimansour.mytasks.core.domain.usecase.AddTaskUseCase
import dev.alimansour.mytasks.feature.task.NewTaskEvent
import dev.alimansour.mytasks.feature.task.TaskEffect
import io.mockk.coEvery
import io.mockk.mockk
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
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NewTaskViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val addTaskUseCase: AddTaskUseCase = mockk()
    private lateinit var viewModel: NewTaskViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = NewTaskViewModel(testDispatcher, addTaskUseCase)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is default TaskState`() =
        runTest(testDispatcher) {
            // THEN
            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertEquals(0, state.id)
            assertEquals("", state.title)
            assertEquals("", state.description)
            assertTrue(state.dueDate > 0L)
            assertNull(state.effect)
        }

    @Test
    fun `UpdateTitle updates title in state`() =
        runTest(testDispatcher) {
            // GIVEN
            val job = launch(testDispatcher) { viewModel.uiState.collect { } }

            // WHEN
            viewModel.processEvent(NewTaskEvent.UpdateTitle("New title"))
            testDispatcher.scheduler.advanceUntilIdle()

            // THEN
            assertEquals("New title", viewModel.uiState.value.title)

            job.cancel()
        }

    @Test
    fun `UpdateDescription updates description in state`() =
        runTest(testDispatcher) {
            // GIVEN
            val job = launch(testDispatcher) { viewModel.uiState.collect { } }

            // WHEN
            viewModel.processEvent(NewTaskEvent.UpdateDescription("New description"))
            testDispatcher.scheduler.advanceUntilIdle()

            // THEN
            assertEquals("New description", viewModel.uiState.value.description)

            job.cancel()
        }

    @Test
    fun `UpdateDueDate updates dueDate in state`() =
        runTest(testDispatcher) {
            // GIVEN
            val job = launch(testDispatcher) { viewModel.uiState.collect { } }
            val dueDate = 12345L

            // WHEN
            viewModel.processEvent(NewTaskEvent.UpdateDueDate(dueDate))
            testDispatcher.scheduler.advanceUntilIdle()

            // THEN
            assertEquals(dueDate, viewModel.uiState.value.dueDate)

            job.cancel()
        }

    @Test
    fun `Proceed with blank title emits ShowError with title message`() =
        runTest(testDispatcher) {
            // GIVEN
            val job = launch(testDispatcher) { viewModel.uiState.collect { } }
            viewModel.processEvent(NewTaskEvent.UpdateTitle(""))
            viewModel.processEvent(NewTaskEvent.UpdateDescription("Some description"))
            testDispatcher.scheduler.advanceUntilIdle()

            // WHEN
            viewModel.processEvent(NewTaskEvent.Proceed)
            testDispatcher.scheduler.advanceUntilIdle()

            // THEN
            val effect = viewModel.uiState.value.effect
            assertTrue(effect is TaskEffect.ShowError)

            job.cancel()
        }

    @Test
    fun `Proceed with blank description emits ShowError with description message`() =
        runTest(testDispatcher) {
            // GIVEN
            val job = launch(testDispatcher) { viewModel.uiState.collect { } }
            viewModel.processEvent(NewTaskEvent.UpdateTitle("Title"))
            viewModel.processEvent(NewTaskEvent.UpdateDescription(""))
            testDispatcher.scheduler.advanceUntilIdle()

            // WHEN
            viewModel.processEvent(NewTaskEvent.Proceed)
            testDispatcher.scheduler.advanceUntilIdle()

            // THEN
            val effect = viewModel.uiState.value.effect
            assertTrue(effect is TaskEffect.ShowError)

            job.cancel()
        }

    @Test
    fun `Proceed with valid data starts loading and on success emits ShowSuccess and stops loading`() =
        runTest(testDispatcher) {
            // GIVEN
            val task = Task(title = "Title", description = "Desc", dueDate = 123L)
            val results = flowOf<Result<Unit, DataError.Local>>(Result.Success(Unit))
            coEvery { addTaskUseCase(any()) } returns results
            val job = launch(testDispatcher) { viewModel.uiState.collect { } }

            viewModel.processEvent(NewTaskEvent.UpdateTitle(task.title))
            viewModel.processEvent(NewTaskEvent.UpdateDescription(task.description))
            viewModel.processEvent(NewTaskEvent.UpdateDueDate(task.dueDate))
            testDispatcher.scheduler.advanceUntilIdle()

            // WHEN
            viewModel.processEvent(NewTaskEvent.Proceed)
            testDispatcher.scheduler.advanceUntilIdle()

            // THEN
            assertFalse(viewModel.uiState.value.isLoading)
            assertTrue(viewModel.uiState.value.effect is TaskEffect.ShowSuccess)

            job.cancel()
        }

    @Test
    fun `Proceed with valid data and error result emits ShowError effect and stops loading`() =
        runTest(testDispatcher) {
            // GIVEN
            val task = Task(title = "Title", description = "Desc", dueDate = 123L)
            val results = flowOf<Result<Unit, DataError.Local>>(Result.Error(DataError.Local.DATABASE_WRITE_ERROR))
            coEvery { addTaskUseCase(any()) } returns results
            val job = launch(testDispatcher) { viewModel.uiState.collect { } }

            viewModel.processEvent(NewTaskEvent.UpdateTitle(task.title))
            viewModel.processEvent(NewTaskEvent.UpdateDescription(task.description))
            viewModel.processEvent(NewTaskEvent.UpdateDueDate(task.dueDate))
            testDispatcher.scheduler.advanceUntilIdle()

            // WHEN
            viewModel.processEvent(NewTaskEvent.Proceed)
            testDispatcher.scheduler.advanceUntilIdle()

            // THEN
            assertFalse(viewModel.uiState.value.isLoading)
            val effect = viewModel.uiState.value.effect
            assertTrue(effect is TaskEffect.ShowError)

            job.cancel()
        }

    @Test
    fun `ConsumeEffect clears current effect`() =
        runTest(testDispatcher) {
            // GIVEN
            val results = flowOf<Result<Unit, DataError.Local>>(Result.Success(Unit))
            coEvery { addTaskUseCase(any()) } returns results
            val job = launch(testDispatcher) { viewModel.uiState.collect { } }

            viewModel.processEvent(NewTaskEvent.UpdateTitle("Title"))
            viewModel.processEvent(NewTaskEvent.UpdateDescription("Desc"))
            viewModel.processEvent(NewTaskEvent.Proceed)
            testDispatcher.scheduler.advanceUntilIdle()
            assertTrue(viewModel.uiState.value.effect != null)

            // WHEN
            viewModel.processEvent(NewTaskEvent.ConsumeEffect)
            testDispatcher.scheduler.advanceUntilIdle()

            // THEN
            assertNull(viewModel.uiState.value.effect)

            job.cancel()
        }

    @Test
    fun `when addTaskUseCase is loading then isLoading is true during add and false after completion`() =
        runTest(testDispatcher) {
            // GIVEN
            val updateResults = MutableSharedFlow<Result<Unit, DataError.Local>>()
            coEvery { addTaskUseCase(any()) } returns updateResults
            val loadingStates = mutableListOf<Boolean>()
            val job =
                launch(testDispatcher) {
                    viewModel.uiState.collect { state -> loadingStates.add(state.isLoading) }
                }
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.processEvent(NewTaskEvent.UpdateTitle("Title"))
            viewModel.processEvent(NewTaskEvent.UpdateDescription("Desc"))
            viewModel.processEvent(NewTaskEvent.UpdateDueDate(1L))
            testDispatcher.scheduler.advanceUntilIdle()

            // WHEN
            viewModel.processEvent(NewTaskEvent.Proceed)
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
    fun `effect flow emits ShowSuccess after successful proceed`() =
        runTest(testDispatcher) {
            // GIVEN
            coEvery { addTaskUseCase(any()) } returns flowOf<Result<Unit, DataError.Local>>(Result.Success(Unit))
            val job = launch(testDispatcher) { viewModel.uiState.collect { } }

            viewModel.processEvent(NewTaskEvent.UpdateTitle("Title"))
            viewModel.processEvent(NewTaskEvent.UpdateDescription("Desc"))
            viewModel.processEvent(NewTaskEvent.UpdateDueDate(1L))
            testDispatcher.scheduler.advanceUntilIdle()

            // WHEN
            viewModel.processEvent(NewTaskEvent.Proceed)
            testDispatcher.scheduler.advanceUntilIdle()

            // THEN
            assertTrue(viewModel.uiState.value.effect is TaskEffect.ShowSuccess)

            job.cancel()
        }

    @Test
    fun `Proceed called twice cancels previous job and final result emits ShowSuccess`() =
        runTest(testDispatcher) {
            // GIVEN
            val updateResults = MutableSharedFlow<Result<Unit, DataError.Local>>()
            coEvery { addTaskUseCase(any()) } returns updateResults
            val job = launch(testDispatcher) { viewModel.uiState.collect { } }

            viewModel.processEvent(NewTaskEvent.UpdateTitle("Title"))
            viewModel.processEvent(NewTaskEvent.UpdateDescription("Desc"))
            viewModel.processEvent(NewTaskEvent.UpdateDueDate(1L))
            testDispatcher.scheduler.advanceUntilIdle()

            // WHEN
            viewModel.processEvent(NewTaskEvent.Proceed)
            viewModel.processEvent(NewTaskEvent.Proceed) // triggers addTaskJob?.cancel() branch
            testDispatcher.scheduler.advanceUntilIdle()

            // WHEN: emit success from the shared flow (for the latest job)
            updateResults.emit(Result.Success(Unit))
            testDispatcher.scheduler.advanceUntilIdle()

            // THEN: loading finished and success effect emitted
            assertFalse(viewModel.uiState.value.isLoading)
            assertTrue(viewModel.uiState.value.effect is TaskEffect.ShowSuccess)

            job.cancel()
        }
}
