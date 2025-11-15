package dev.alimansour.mytasks.feature.task.update

import dev.alimansour.mytasks.R
import dev.alimansour.mytasks.core.domain.model.DataError
import dev.alimansour.mytasks.core.domain.model.Result
import dev.alimansour.mytasks.core.domain.model.Task
import dev.alimansour.mytasks.core.domain.usecase.UpdateTaskUseCase
import dev.alimansour.mytasks.core.ui.utils.UiText
import dev.alimansour.mytasks.feature.task.TaskEffect
import dev.alimansour.mytasks.feature.task.UpdateTaskEvent
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
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
class UpdateTaskViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val updateTaskUseCase: UpdateTaskUseCase = mockk()
    private lateinit var viewModel: UpdateTaskViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = UpdateTaskViewModel(testDispatcher, updateTaskUseCase)
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
            val dueDate = 99999L

            // WHEN
            viewModel.processEvent(UpdateTaskEvent.UpdateDueDate(dueDate))
            testDispatcher.scheduler.advanceUntilIdle()

            // THEN
            assertEquals(dueDate, viewModel.uiState.value.dueDate)

            job.cancel()
        }

    @Test
    fun `LoadTask populates state from given task`() =
        runTest(testDispatcher) {
            // GIVEN
            val job = launch(testDispatcher) { viewModel.uiState.collect { } }
            val task = Task(id = 10L, title = "Title", description = "Desc", dueDate = 123L, isCompleted = false)

            // WHEN
            viewModel.processEvent(UpdateTaskEvent.LoadTask(task))
            testDispatcher.scheduler.advanceUntilIdle()

            // THEN
            val state = viewModel.uiState.value
            assertEquals(task.id, state.id)
            assertEquals(task.title, state.title)
            assertEquals(task.description, state.description)
            assertEquals(task.dueDate, state.dueDate)

            job.cancel()
        }

    @Test
    fun `Proceed with blank title emits ShowError with title message`() =
        runTest(testDispatcher) {
            // GIVEN
            val job = launch(testDispatcher) { viewModel.uiState.collect { } }
            viewModel.processEvent(UpdateTaskEvent.UpdateTitle(""))
            viewModel.processEvent(UpdateTaskEvent.UpdateDescription("Some description"))
            testDispatcher.scheduler.advanceUntilIdle()

            // WHEN
            viewModel.processEvent(UpdateTaskEvent.Proceed)
            testDispatcher.scheduler.advanceUntilIdle()

            // THEN
            val effect = viewModel.uiState.value.effect
            assertTrue(effect is TaskEffect.ShowError)
            effect as TaskEffect.ShowError
            assertTrue(effect.message is UiText.StringResourceId)
            assertEquals(R.string.title_cannot_be_empty, (effect.message as UiText.StringResourceId).id)

            job.cancel()
        }

    @Test
    fun `Proceed with blank description emits ShowError with description message`() =
        runTest(testDispatcher) {
            // GIVEN
            val job = launch(testDispatcher) { viewModel.uiState.collect { } }
            viewModel.processEvent(UpdateTaskEvent.UpdateTitle("Title"))
            viewModel.processEvent(UpdateTaskEvent.UpdateDescription(""))
            testDispatcher.scheduler.advanceUntilIdle()

            // WHEN
            viewModel.processEvent(UpdateTaskEvent.Proceed)
            testDispatcher.scheduler.advanceUntilIdle()

            // THEN
            val effect = viewModel.uiState.value.effect
            assertTrue(effect is TaskEffect.ShowError)
            effect as TaskEffect.ShowError
            assertTrue(effect.message is UiText.StringResourceId)
            assertEquals(R.string.description_cannot_be_empty, (effect.message as UiText.StringResourceId).id)

            job.cancel()
        }

    @Test
    fun `Proceed with valid data and success result sets ShowSuccess effect and stops loading`() =
        runTest(testDispatcher) {
            // GIVEN
            val task = Task(id = 1L, title = "Title", description = "Desc", dueDate = 123L)
            val results = flowOf<Result<Unit, DataError.Local>>(Result.Success(Unit))
            coEvery { updateTaskUseCase(any()) } returns results
            val job = launch(testDispatcher) { viewModel.uiState.collect { } }
            viewModel.processEvent(UpdateTaskEvent.LoadTask(task))
            testDispatcher.scheduler.advanceUntilIdle()

            // WHEN
            viewModel.processEvent(UpdateTaskEvent.Proceed)
            testDispatcher.scheduler.advanceUntilIdle()

            // THEN
            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertTrue(state.effect is TaskEffect.ShowSuccess)

            job.cancel()
        }

    @Test
    fun `Proceed with valid data and error result sets ShowError effect and stops loading`() =
        runTest(testDispatcher) {
            // GIVEN
            val task = Task(id = 1L, title = "Title", description = "Desc", dueDate = 123L)
            val error = DataError.Local.DATABASE_WRITE_ERROR
            val results = flowOf<Result<Unit, DataError.Local>>(Result.Error(error))
            coEvery { updateTaskUseCase(any()) } returns results
            val job = launch(testDispatcher) { viewModel.uiState.collect { } }
            viewModel.processEvent(UpdateTaskEvent.LoadTask(task))
            testDispatcher.scheduler.advanceUntilIdle()

            // WHEN
            viewModel.processEvent(UpdateTaskEvent.Proceed)
            testDispatcher.scheduler.advanceUntilIdle()

            // THEN
            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertTrue(state.effect is TaskEffect.ShowError)

            job.cancel()
        }

    @Test
    fun `ConsumeEffect clears existing effect`() =
        runTest(testDispatcher) {
            // GIVEN
            val task = Task(id = 1L, title = "Title", description = "Desc", dueDate = 123L)
            coEvery { updateTaskUseCase(any()) } returns flowOf(Result.Success(Unit))
            val job = launch(testDispatcher) { viewModel.uiState.collect { } }
            viewModel.processEvent(UpdateTaskEvent.LoadTask(task))
            viewModel.processEvent(UpdateTaskEvent.Proceed)
            testDispatcher.scheduler.advanceUntilIdle()
            assertTrue(viewModel.uiState.value.effect != null)

            // WHEN
            viewModel.processEvent(UpdateTaskEvent.ConsumeEffect)
            testDispatcher.scheduler.advanceUntilIdle()

            // THEN
            assertNull(viewModel.uiState.value.effect)

            job.cancel()
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

            val task = Task(id = 1L, title = "Title", description = "Desc", dueDate = 123L)
            viewModel.processEvent(UpdateTaskEvent.LoadTask(task))
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
    fun `effect flow emits null after ConsumeEffect`() =
        runTest(testDispatcher) {
            // GIVEN
            val task = Task(id = 1L, title = "Title", description = "Desc", dueDate = 123L)
            coEvery { updateTaskUseCase(any()) } returns flowOf<Result<Unit, DataError.Local>>(Result.Success(Unit))
            val collectedEffects = mutableListOf<TaskEffect?>()
            val job = launch(testDispatcher) { viewModel.effect.collect { collectedEffects.add(it) } }
            viewModel.processEvent(UpdateTaskEvent.LoadTask(task))
            viewModel.processEvent(UpdateTaskEvent.Proceed)
            testDispatcher.scheduler.advanceUntilIdle()

            // WHEN
            viewModel.processEvent(UpdateTaskEvent.ConsumeEffect)
            testDispatcher.scheduler.advanceUntilIdle()

            // THEN
            assertNull(viewModel.uiState.value.effect)
            assertNull(collectedEffects.lastOrNull())

            job.cancel()
        }

    @Test
    fun `Proceed with valid data passes correct Task to use case`() =
        runTest(testDispatcher) {
            // GIVEN
            val capturedTask = slot<Task>()
            coEvery { updateTaskUseCase(capture(capturedTask)) } returns flowOf(Result.Success(Unit))
            val job = launch(testDispatcher) { viewModel.uiState.collect { } }
            val task = Task(id = 5L, title = "My title", description = "My desc", dueDate = 42L)
            viewModel.processEvent(UpdateTaskEvent.LoadTask(task))
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
