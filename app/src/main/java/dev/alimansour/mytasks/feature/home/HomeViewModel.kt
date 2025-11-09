package dev.alimansour.mytasks.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.alimansour.mytasks.core.domain.model.Task
import dev.alimansour.mytasks.core.domain.model.onError
import dev.alimansour.mytasks.core.domain.model.onSuccess
import dev.alimansour.mytasks.core.domain.usecase.GetTasksUseCase
import dev.alimansour.mytasks.core.domain.usecase.UpdateTaskUseCase
import dev.alimansour.mytasks.core.ui.navigation.Route
import dev.alimansour.mytasks.core.ui.utils.toUiText
import dev.alimansour.mytasks.feature.home.HomeEffect.NavigateToRoute
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class HomeViewModel(
    private val dispatcher: CoroutineDispatcher,
    private val getTasksUseCase: GetTasksUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
) : ViewModel() {
    private var updateTaskJob: Job? = null
    private val _uiState = MutableStateFlow(HomeState())
    val uiState =
        _uiState
            .onStart {
                getTasks()
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000L),
                initialValue = _uiState.value,
            )
    val effect = _uiState.map { it.effect }

    fun processEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.OnExpandStateChanged ->
                _uiState.update {
                    it.copy(isFabExpanded = event.isExpanded)
                }
            is HomeEvent.NavigateToNewTaskScreen ->
                _uiState.update {
                    it.copy(effect = NavigateToRoute(route = Route.NewTask))
                }

            is HomeEvent.NavigateToTaskDetailsScreen ->
                _uiState.update {
                    it.copy(effect = NavigateToRoute(route = Route.TaskDetails))
                }

            is HomeEvent.OnTaskCheckChanged -> {
                updateTaskJob?.cancel()
                updateTaskJob = updateTask(event.task)
            }

            is HomeEvent.ConsumeEffect -> _uiState.update { it.copy(effect = null) }
        }
    }

    private fun updateTask(task: Task) =
        viewModelScope.launch(dispatcher) {
            updateTaskUseCase(task).collect { result ->
                result
                    .onError { error ->
                        _uiState.update {
                            it.copy(effect = HomeEffect.ShowError(message = error.toUiText()))
                        }
                    }
            }
        }

    private fun getTasks() =
        viewModelScope.launch(dispatcher) {
            getTasksUseCase().collect { result ->
                result
                    .onSuccess { tasks ->
                        _uiState.update {
                            it.copy(tasks = tasks)
                        }
                    }.onError { error ->
                        _uiState.update {
                            it.copy(effect = HomeEffect.ShowError(message = error.toUiText()))
                        }
                    }
            }
        }
}
