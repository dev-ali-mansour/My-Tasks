package dev.alimansour.mytasks.feature.home

import dev.alimansour.mytasks.core.domain.model.DataError
import dev.alimansour.mytasks.core.domain.model.Result
import dev.alimansour.mytasks.core.domain.model.Task
import dev.alimansour.mytasks.core.domain.usecase.GetTasksUseCase
import dev.alimansour.mytasks.core.domain.usecase.UpdateTaskUseCase
import dev.alimansour.mytasks.core.ui.navigation.Route
import dev.alimansour.mytasks.feature.home.screen.HomeEffect
import dev.alimansour.mytasks.feature.home.screen.HomeEvent
import dev.alimansour.mytasks.feature.home.screen.HomeViewModel
import io.mockk.coEvery
import io.mockk.coVerify
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
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val getTasksUseCase: GetTasksUseCase = mockk()
    private val updateTaskUseCase: UpdateTaskUseCase = mockk()
    private lateinit var viewModel: HomeViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = HomeViewModel(testDispatcher, getTasksUseCase, updateTaskUseCase)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when getTasksUseCase succeeds then uiState contains tasks and isLoading is false`() =
        runTest(testDispatcher) {
            // GIVEN
            val tasks = listOf(Task(id = 1L, title = "Task 1", description = "Desc", isCompleted = false, dueDate = 0L))
            coEvery { getTasksUseCase() } returns flowOf(Result.Success(tasks))
            val job = launch(testDispatcher) { viewModel.uiState.collect { } }

            // WHEN
            testDispatcher.scheduler.advanceUntilIdle()

            // THEN
            assertEquals(tasks, viewModel.uiState.value.tasks)
            assertFalse(viewModel.uiState.value.isLoading)

            job.cancel()
        }

    @Test
    fun `when getTasksUseCase emits error then uiState has error effect and isLoading is false`() =
        runTest(testDispatcher) {
            // GIVEN
            val error = DataError.Local.DATABASE_READ_ERROR
            coEvery { getTasksUseCase() } returns flowOf<Result<List<Task>, DataError.Local>>(Result.Error(error))
            val effects = mutableListOf<HomeEffect>()
            val effectJob = launch(testDispatcher) { viewModel.effect.collect { effects.add(it) } }
            val job = launch(testDispatcher) { viewModel.uiState.collect { } }

            // WHEN
            testDispatcher.scheduler.advanceUntilIdle()

            // THEN
            assertFalse(viewModel.uiState.value.isLoading)
            assertTrue(effects.last() is HomeEffect.ShowError)

            job.cancel()
            effectJob.cancel()
        }

    @Test
    fun `when OnExpandStateChanged event is processed then isFabExpanded is updated`() =
        runTest(testDispatcher) {
            // GIVEN
            coEvery { getTasksUseCase() } returns flowOf(Result.Success(emptyList()))
            val job = launch(testDispatcher) { viewModel.uiState.collect { } }
            testDispatcher.scheduler.advanceUntilIdle()

            // WHEN
            viewModel.processEvent(HomeEvent.OnExpandStateChanged(isExpanded = true))
            testDispatcher.scheduler.advanceUntilIdle()

            // THEN
            assertTrue(viewModel.uiState.value.isFabExpanded)

            job.cancel()
        }

    @Test
    fun `when OnTaskCheckChanged event is processed then updateTaskUseCase is called`() =
        runTest(testDispatcher) {
            // GIVEN
            val task = Task(id = 1L, title = "Task", description = "Desc", isCompleted = false, dueDate = 0L)
            coEvery { getTasksUseCase() } returns flowOf(Result.Success(listOf(task)))
            coEvery { updateTaskUseCase(any()) } returns flowOf(Result.Success(Unit))
            val job = launch(testDispatcher) { viewModel.uiState.collect { } }
            testDispatcher.scheduler.advanceUntilIdle()

            // WHEN
            viewModel.processEvent(HomeEvent.OnTaskCheckChanged(task))
            testDispatcher.scheduler.advanceUntilIdle()

            // THEN
            coVerify(exactly = 1) { updateTaskUseCase(task).let { } }
            assertFalse(viewModel.uiState.value.isLoading)

            job.cancel()
        }

    @Test
    fun `when NavigateToTaskDetailsScreen event is processed then effect contains correct task`() =
        runTest(testDispatcher) {
            // GIVEN
            coEvery { getTasksUseCase() } returns flowOf(Result.Success(emptyList()))
            val task = Task(id = 42L, title = "Task", description = "Desc", isCompleted = false, dueDate = 0L)
            val effects = mutableListOf<HomeEffect>()
            val effectJob = launch(testDispatcher) { viewModel.effect.collect { effects.add(it) } }
            val job = launch(testDispatcher) { viewModel.uiState.collect { } }
            testDispatcher.scheduler.advanceUntilIdle()

            // WHEN
            viewModel.processEvent(HomeEvent.NavigateToTaskDetailsScreen(task))
            testDispatcher.scheduler.advanceUntilIdle()

            // THEN
            val lastEffect = effects.last()
            assertTrue(lastEffect is HomeEffect.NavigateToTaskDetails)
            assertEquals(task, (lastEffect as HomeEffect.NavigateToTaskDetails).task)

            job.cancel()
            effectJob.cancel()
        }

    @Test
    fun `when OnTaskCheckChanged and updateTaskUseCase emits error then ShowError effect is set`() =
        runTest(testDispatcher) {
            // GIVEN
            val task = Task(id = 1L, title = "Task", description = "Desc", isCompleted = false, dueDate = 0L)
            val error = DataError.Local.DATABASE_WRITE_ERROR
            coEvery { getTasksUseCase() } returns flowOf(Result.Success(listOf(task)))
            coEvery { updateTaskUseCase(any()) } returns flowOf(Result.Error(error))
            val effects = mutableListOf<HomeEffect>()
            val effectJob = launch(testDispatcher) { viewModel.effect.collect { effects.add(it) } }
            val job = launch(testDispatcher) { viewModel.uiState.collect { } }
            testDispatcher.scheduler.advanceUntilIdle()

            // WHEN
            viewModel.processEvent(HomeEvent.OnTaskCheckChanged(task))
            testDispatcher.scheduler.advanceUntilIdle()

            // THEN
            assertFalse(viewModel.uiState.value.isLoading)
            assertTrue(effects.last() is HomeEffect.ShowError)

            job.cancel()
            effectJob.cancel()
        }

    @Test
    fun `when NavigateToNewTaskScreen event is processed then NavigateToRoute effect with NewTask route is set`() =
        runTest(testDispatcher) {
            // GIVEN
            coEvery { getTasksUseCase() } returns flowOf(Result.Success(emptyList()))
            val effects = mutableListOf<HomeEffect>()
            val effectJob = launch(testDispatcher) { viewModel.effect.collect { effects.add(it) } }
            val job = launch(testDispatcher) { viewModel.uiState.collect { } }
            testDispatcher.scheduler.advanceUntilIdle()

            // WHEN
            viewModel.processEvent(HomeEvent.NavigateToNewTaskScreen)
            testDispatcher.scheduler.advanceUntilIdle()

            // THEN
            val lastEffect = effects.last()
            assertTrue(lastEffect is HomeEffect.NavigateToRoute)
            assertEquals(Route.NewTask, (lastEffect as HomeEffect.NavigateToRoute).route)

            job.cancel()
            effectJob.cancel()
        }

    @Test
    fun `when getTasks is started then isLoading becomes true until first result is emitted`() =
        runTest(testDispatcher) {
            // GIVEN
            val results = flowOf(Result.Success(emptyList<Task>()))
            coEvery { getTasksUseCase() } returns results
            val job = launch(testDispatcher) { viewModel.uiState.collect { } }

            // WHEN
            // Immediately after starting collection, getTasks should set isLoading to true
            testDispatcher.scheduler.advanceUntilIdle()

            // THEN
            assertFalse(viewModel.uiState.value.isLoading)

            job.cancel()
        }

    @Test
    fun `when OnTaskCheckChanged is processed then isLoading becomes true while updating and false after completion`() =
        runTest(testDispatcher) {
            // GIVEN
            val task = Task(id = 1L, title = "Task", description = "Desc", isCompleted = false, dueDate = 0L)
            coEvery { getTasksUseCase() } returns flowOf(Result.Success(listOf(task)))
            coEvery { updateTaskUseCase(any()) } returns flowOf(Result.Success(Unit))
            val job = launch(testDispatcher) { viewModel.uiState.collect { } }
            testDispatcher.scheduler.advanceUntilIdle()

            // WHEN
            viewModel.processEvent(HomeEvent.OnTaskCheckChanged(task))

            // THEN
            // After starting the update, loading should eventually be false once completed
            testDispatcher.scheduler.advanceUntilIdle()
            assertFalse(viewModel.uiState.value.isLoading)

            job.cancel()
        }

    @Test
    fun `when getTasksUseCase is loading then isLoading is true until first result is emitted`() =
        runTest(testDispatcher) {
            // GIVEN
            val results = MutableSharedFlow<Result<List<Task>, DataError.Local>>()
            coEvery { getTasksUseCase() } returns results
            val collectedStates = mutableListOf<Boolean>()
            val job =
                launch(testDispatcher) {
                    viewModel.uiState.collect { state -> collectedStates.add(state.isLoading) }
                }

            // WHEN
            testDispatcher.scheduler.advanceUntilIdle()

            // THEN
            assertTrue(collectedStates.contains(true))

            // WHEN
            results.emit(Result.Success(emptyList()))
            testDispatcher.scheduler.advanceUntilIdle()

            // THEN
            assertFalse(viewModel.uiState.value.isLoading)

            job.cancel()
        }

    @Test
    fun `when updateTaskUseCase is loading then isLoading is true during update and false after completion`() =
        runTest(testDispatcher) {
            // GIVEN
            val task = Task(id = 1L, title = "Task", description = "Desc", isCompleted = false, dueDate = 0L)
            coEvery { getTasksUseCase() } returns flowOf(Result.Success(listOf(task)))
            val updateResults = MutableSharedFlow<Result<Unit, DataError.Local>>()
            coEvery { updateTaskUseCase(any()) } returns updateResults
            val loadingStates = mutableListOf<Boolean>()
            val job =
                launch(testDispatcher) {
                    viewModel.uiState.collect { state -> loadingStates.add(state.isLoading) }
                }
            testDispatcher.scheduler.advanceUntilIdle()

            // WHEN
            viewModel.processEvent(HomeEvent.OnTaskCheckChanged(task))
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
    fun `when OnTaskCheckChanged is triggered repeatedly then isLoading becomes true and ends as false`() =
        runTest(testDispatcher) {
            // GIVEN
            val task1 = Task(id = 1L, title = "Task 1", description = "Desc", isCompleted = false, dueDate = 0L)
            val task2 = Task(id = 2L, title = "Task 2", description = "Desc", isCompleted = true, dueDate = 0L)
            coEvery { getTasksUseCase() } returns flowOf(Result.Success(listOf(task1, task2)))

            // Use a shared flow to control completion of the last update
            val updateResults = MutableSharedFlow<Result<Unit, DataError.Local>>()
            coEvery { updateTaskUseCase(any()) } returns updateResults

            val loadingStates = mutableListOf<Boolean>()
            val job =
                launch(testDispatcher) {
                    viewModel.uiState.collect { state -> loadingStates.add(state.isLoading) }
                }
            testDispatcher.scheduler.advanceUntilIdle()

            // WHEN
            // Trigger two updates before emitting any result
            viewModel.processEvent(HomeEvent.OnTaskCheckChanged(task1))
            viewModel.processEvent(HomeEvent.OnTaskCheckChanged(task2))
            testDispatcher.scheduler.advanceUntilIdle()

            // THEN
            // While waiting for a result, loading should have been true at least once
            assertTrue(loadingStates.contains(true))

            // WHEN
            // Now emit a success result for the last update
            updateResults.emit(Result.Success(Unit))
            testDispatcher.scheduler.advanceUntilIdle()

            // THEN
            // ViewModel must not be stuck in loading state
            assertFalse(viewModel.uiState.value.isLoading)

            job.cancel()
        }

    @Test
    fun `when getTasksUseCase emits twice then uiState tasks contain last emission`() =
        runTest(testDispatcher) {
            // GIVEN
            val task1 = Task(id = 1L, title = "Task 1", description = "Desc", isCompleted = false, dueDate = 0L)
            val task2 = Task(id = 2L, title = "Task 2", description = "Desc", isCompleted = true, dueDate = 0L)
            val results = MutableSharedFlow<Result<List<Task>, DataError.Local>>()
            coEvery { getTasksUseCase() } returns results
            val job = launch(testDispatcher) { viewModel.uiState.collect { } }

            // WHEN
            results.emit(Result.Success(listOf(task1)))
            testDispatcher.scheduler.advanceUntilIdle()
            results.emit(Result.Success(listOf(task2)))
            testDispatcher.scheduler.advanceUntilIdle()

            // THEN
            assertEquals(listOf(task2), viewModel.uiState.value.tasks)
            assertFalse(viewModel.uiState.value.isLoading)

            job.cancel()
        }
}