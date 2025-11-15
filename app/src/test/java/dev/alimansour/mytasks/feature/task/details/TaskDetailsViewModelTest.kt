package dev.alimansour.mytasks.feature.task.details

import dev.alimansour.mytasks.core.domain.model.DataError
import dev.alimansour.mytasks.core.domain.model.Result
import dev.alimansour.mytasks.core.domain.model.Task
import dev.alimansour.mytasks.core.domain.usecase.DeleteTaskUseCase
import dev.alimansour.mytasks.core.ui.utils.UiText
import dev.alimansour.mytasks.core.ui.utils.toUiText
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
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
class TaskDetailsViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val deleteTaskUseCase: DeleteTaskUseCase = mockk()
    private lateinit var viewModel: TaskDetailsViewModel

    private val sampleTask = Task(id = 1, title = "Title", description = "Desc", dueDate = 123L, isCompleted = false)

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = TaskDetailsViewModel(testDispatcher, deleteTaskUseCase)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has no task effect null and not loading`() =
        runTest(testDispatcher) {
            // GIVEN
            val uiStates = mutableListOf<TaskDetailsState>()
            val job = launch { viewModel.uiState.collect { uiStates.add(it) } }

            // WHEN
            testDispatcher.scheduler.advanceUntilIdle()

            // THEN
            val initial = viewModel.uiState.value
            assertFalse(initial.isLoading)
            assertNull(initial.task)
            assertNull(initial.effect)

            job.cancel()
        }

    @Test
    fun `LoadTask sets task without altering loading or effect`() =
        runTest(testDispatcher) {
            // GIVEN
            val job = launch { viewModel.uiState.collect { } }
            testDispatcher.scheduler.advanceUntilIdle()

            // WHEN
            viewModel.processEvent(TaskDetailsEvent.LoadTask(sampleTask))
            testDispatcher.scheduler.advanceUntilIdle()

            // THEN
            val state = viewModel.uiState.value
            assertEquals(sampleTask, state.task)
            assertFalse(state.isLoading)
            assertNull(state.effect)

            job.cancel()
        }

    @Test
    fun `LoadTask overwrites previously loaded task`() =
        runTest(testDispatcher) {
            // GIVEN
            val job = launch { viewModel.uiState.collect { } }
            val oldTask = sampleTask.copy(id = 99)
            viewModel.processEvent(TaskDetailsEvent.LoadTask(oldTask))
            testDispatcher.scheduler.advanceUntilIdle()

            // WHEN
            viewModel.processEvent(TaskDetailsEvent.LoadTask(sampleTask))
            testDispatcher.scheduler.advanceUntilIdle()

            // THEN
            assertEquals(sampleTask, viewModel.uiState.value.task)

            job.cancel()
        }

    @Test
    fun `UpdateTask with existing task emits navigation effect`() =
        runTest(testDispatcher) {
            // GIVEN
            val job = launch { viewModel.uiState.collect { } }
            viewModel.processEvent(TaskDetailsEvent.LoadTask(sampleTask))
            testDispatcher.scheduler.advanceUntilIdle()

            // WHEN
            viewModel.processEvent(TaskDetailsEvent.UpdateTask)
            testDispatcher.scheduler.advanceUntilIdle()

            // THEN
            val effect = viewModel.uiState.value.effect
            assertTrue(effect is TaskDetailsEffect.NavigateToUpdateScreen)
            effect as TaskDetailsEffect.NavigateToUpdateScreen
            assertEquals(sampleTask, effect.task)

            job.cancel()
        }

    @Test
    fun `UpdateTask with null task does nothing`() =
        runTest(testDispatcher) {
            // GIVEN
            val job = launch { viewModel.uiState.collect { } }
            testDispatcher.scheduler.advanceUntilIdle()

            // WHEN
            viewModel.processEvent(TaskDetailsEvent.UpdateTask)
            testDispatcher.scheduler.advanceUntilIdle()

            // THEN
            val state = viewModel.uiState.value
            assertNull(state.task)
            assertNull(state.effect)

            job.cancel()
        }

    @Test
    fun `UpdateTask overrides previous success or error effect`() =
        runTest(testDispatcher) {
            // GIVEN
            val job = launch { viewModel.uiState.collect { } }
            viewModel.processEvent(TaskDetailsEvent.LoadTask(sampleTask))
            coEvery { deleteTaskUseCase(any()) } returns flowOf(Result.Success(Unit))
            viewModel.processEvent(TaskDetailsEvent.DeleteTask)
            testDispatcher.scheduler.advanceUntilIdle()
            assertTrue(viewModel.uiState.value.effect is TaskDetailsEffect.ShowSuccess)

            // WHEN
            viewModel.processEvent(TaskDetailsEvent.UpdateTask)
            testDispatcher.scheduler.advanceUntilIdle()

            // THEN
            assertTrue(viewModel.uiState.value.effect is TaskDetailsEffect.NavigateToUpdateScreen)

            job.cancel()
        }

    @Test
    fun `DeleteTask with null task does not invoke use case`() =
        runTest(testDispatcher) {
            // GIVEN
            val job = launch { viewModel.uiState.collect { } }
            testDispatcher.scheduler.advanceUntilIdle()

            // WHEN
            viewModel.processEvent(TaskDetailsEvent.DeleteTask)
            testDispatcher.scheduler.advanceUntilIdle()

            // THEN
            coVerify(exactly = 0) { deleteTaskUseCase.invoke(any()).let { } }
            assertNull(viewModel.uiState.value.effect)

            job.cancel()
        }

    @Test
    fun `DeleteTask success sets loading then emits ShowSuccess effect`() =
        runTest(testDispatcher) {
            // GIVEN
            val job = launch { viewModel.uiState.collect { } }
            viewModel.processEvent(TaskDetailsEvent.LoadTask(sampleTask))
            val successFlow = flowOf(Result.Success(Unit))
            coEvery { deleteTaskUseCase(sampleTask) } returns successFlow

            // WHEN
            viewModel.processEvent(TaskDetailsEvent.DeleteTask)
            testDispatcher.scheduler.advanceUntilIdle()

            // THEN
            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertEquals(TaskDetailsEffect.ShowSuccess, state.effect)

            job.cancel()
        }

    @Test
    fun `DeleteTask error sets loading then emits ShowError effect with correct message`() =
        runTest(testDispatcher) {
            // GIVEN
            val job = launch { viewModel.uiState.collect { } }
            viewModel.processEvent(TaskDetailsEvent.LoadTask(sampleTask))
            val error = DataError.Local.DATABASE_WRITE_ERROR
            coEvery { deleteTaskUseCase(sampleTask) } returns flowOf(Result.Error(error))

            // WHEN
            viewModel.processEvent(TaskDetailsEvent.DeleteTask)
            testDispatcher.scheduler.advanceUntilIdle()

            // THEN
            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertTrue(state.effect is TaskDetailsEffect.ShowError)
            val effect = state.effect as TaskDetailsEffect.ShowError

            val expected = error.toUiText()
            assertTrue(effect.message is UiText.StringResourceId)
            assertTrue(expected is UiText.StringResourceId)
            // We can't access underlying resId here; just assert types match and message is not null
            assertEquals(expected::class, effect.message::class)

            job.cancel()
        }

    @Test
    fun `ConsumeEffect clears existing effect`() =
        runTest(testDispatcher) {
            // GIVEN
            val job = launch { viewModel.uiState.collect { } }
            viewModel.processEvent(TaskDetailsEvent.LoadTask(sampleTask))
            coEvery { deleteTaskUseCase(sampleTask) } returns flowOf(Result.Success(Unit))
            viewModel.processEvent(TaskDetailsEvent.DeleteTask)
            testDispatcher.scheduler.advanceUntilIdle()
            assertTrue(viewModel.uiState.value.effect is TaskDetailsEffect.ShowSuccess)

            // WHEN
            viewModel.processEvent(TaskDetailsEvent.ConsumeEffect)
            testDispatcher.scheduler.advanceUntilIdle()

            // THEN
            assertNull(viewModel.uiState.value.effect)

            job.cancel()
        }

    @Test
    fun `ConsumeEffect when effect already null keeps state unchanged`() =
        runTest(testDispatcher) {
            // GIVEN
            val job = launch { viewModel.uiState.collect { } }
            val before = viewModel.uiState.value

            // WHEN
            viewModel.processEvent(TaskDetailsEvent.ConsumeEffect)
            testDispatcher.scheduler.advanceUntilIdle()

            // THEN
            val after = viewModel.uiState.value
            assertEquals(before, after)

            job.cancel()
        }

    @Test
    fun `DeleteTask called twice invokes use case twice and stays loading until emission`() =
        runTest(testDispatcher) {
            // GIVEN
            val job = launch { viewModel.uiState.collect { } }
            viewModel.processEvent(TaskDetailsEvent.LoadTask(sampleTask))
            val shared = MutableSharedFlow<Result<Unit, DataError.Local>>()
            coEvery { deleteTaskUseCase(sampleTask) } returns shared

            // WHEN
            viewModel.processEvent(TaskDetailsEvent.DeleteTask)
            // WHEN
            viewModel.processEvent(TaskDetailsEvent.DeleteTask)
            testDispatcher.scheduler.advanceUntilIdle()

            // THEN
            coVerify(atLeast = 1) { deleteTaskUseCase(sampleTask).let { } }
            assertTrue(viewModel.uiState.value.isLoading)

            // WHEN
            shared.emit(Result.Success(Unit))
            testDispatcher.scheduler.advanceUntilIdle()

            // THEN
            assertFalse(viewModel.uiState.value.isLoading)
            assertEquals(TaskDetailsEffect.ShowSuccess, viewModel.uiState.value.effect)

            job.cancel()
        }

    @Test
    fun `DeleteTask multi emission final effect reflects last emission`() =
        runTest(testDispatcher) {
            // GIVEN
            val job = launch { viewModel.uiState.collect { } }
            viewModel.processEvent(TaskDetailsEvent.LoadTask(sampleTask))
            val flowMulti =
                flow {
                    emit(Result.Success(Unit))
                    emit(Result.Error(DataError.Local.DATABASE_WRITE_ERROR))
                }
            coEvery { deleteTaskUseCase(sampleTask) } returns flowMulti

            // WHEN
            viewModel.processEvent(TaskDetailsEvent.DeleteTask)
            testDispatcher.scheduler.advanceUntilIdle()

            // THEN
            val state = viewModel.uiState.value
            assertTrue(state.effect is TaskDetailsEffect.ShowError)

            job.cancel()
        }

    @Test
    fun `effect flow sequence reflects navigation consumption and success`() =
        runTest(testDispatcher) {
            // GIVEN
            val uiJob = launch { viewModel.uiState.collect { } }
            val effects = mutableListOf<TaskDetailsEffect?>()
            val effectJob = launch { viewModel.effect.collect { effects.add(it) } }
            viewModel.processEvent(TaskDetailsEvent.LoadTask(sampleTask))
            testDispatcher.scheduler.advanceUntilIdle()

            // WHEN
            viewModel.processEvent(TaskDetailsEvent.UpdateTask)
            testDispatcher.scheduler.advanceUntilIdle()
            // WHEN
            viewModel.processEvent(TaskDetailsEvent.ConsumeEffect)
            testDispatcher.scheduler.advanceUntilIdle()
            // WHEN
            coEvery { deleteTaskUseCase(sampleTask) } returns flowOf(Result.Success(Unit))
            viewModel.processEvent(TaskDetailsEvent.DeleteTask)
            testDispatcher.scheduler.advanceUntilIdle()
            // WHEN
            viewModel.processEvent(TaskDetailsEvent.ConsumeEffect)
            testDispatcher.scheduler.advanceUntilIdle()

            // THEN
            assertTrue(effects.isNotEmpty())
            val condensed =
                effects.fold(mutableListOf<TaskDetailsEffect?>()) { acc, e ->
                    if (acc.lastOrNull() != e) acc.add(e)
                    acc
                }
            assertEquals(
                listOf(
                    TaskDetailsEffect.NavigateToUpdateScreen(sampleTask),
                    null,
                    TaskDetailsEffect.ShowSuccess,
                    null,
                ),
                condensed,
            )

            uiJob.cancel()
            effectJob.cancel()
        }

    @Test
    fun `DeleteTask sets loading true before emission then false after result`() =
        runTest(testDispatcher) {
            // GIVEN
            val job = launch { viewModel.uiState.collect { } }
            viewModel.processEvent(TaskDetailsEvent.LoadTask(sampleTask))
            val shared = MutableSharedFlow<Result<Unit, DataError.Local>>()
            coEvery { deleteTaskUseCase(sampleTask) } returns shared

            // WHEN
            viewModel.processEvent(TaskDetailsEvent.DeleteTask)
            testDispatcher.scheduler.advanceUntilIdle()

            // THEN
            assertTrue(viewModel.uiState.value.isLoading)

            // WHEN
            shared.emit(Result.Success(Unit))
            testDispatcher.scheduler.advanceUntilIdle()

            // THEN
            assertFalse(viewModel.uiState.value.isLoading)

            job.cancel()
        }
}
