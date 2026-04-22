package dev.alimansour.mytasks.feature.home.screen

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.alimansour.mytasks.core.domain.model.Task
import dev.alimansour.mytasks.core.domain.model.onError
import dev.alimansour.mytasks.core.domain.model.onSuccess
import dev.alimansour.mytasks.core.domain.usecase.GetTasksUseCase
import dev.alimansour.mytasks.core.domain.usecase.UpdateTaskUseCase
import dev.alimansour.mytasks.core.ui.navigation.Route
import dev.alimansour.mytasks.core.ui.utils.toUiText
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@Stable
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
    private val _effect = Channel<HomeEffect>(capacity = Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun processEvent(event: HomeEvent) {
        viewModelScope.launch {
            when (event) {
                is HomeEvent.OnExpandStateChanged -> {
                    _uiState.update {
                        it.copy(isFabExpanded = event.isExpanded)
                    }
                }

                is HomeEvent.OnBackPress -> {
                    _uiState.update {
                        it.copy(openDialog = true)
                    }
                }

                is HomeEvent.OnExitDialogConfirmed -> {
                    _uiState.update { it.copy(openDialog = false) }
                    _effect.send(HomeEffect.ExitApp)
                }

                is HomeEvent.OnExitDialogCancelled -> {
                    _uiState.update {
                        it.copy(openDialog = false)
                    }
                }

                is HomeEvent.NavigateToNewTaskScreen -> {
                    _effect.send(HomeEffect.NavigateToRoute(route = Route.NewTask))
                }

                is HomeEvent.NavigateToTaskDetailsScreen -> {
                    _effect.send(HomeEffect.NavigateToTaskDetails(task = event.task))
                }

                is HomeEvent.OnTaskCheckChanged -> {
                    updateTaskJob?.cancel()
                    updateTaskJob = updateTask(event.task)
                }
            }
        }
    }

    private fun updateTask(task: Task) =
        viewModelScope.launch(dispatcher) {
            _uiState.update { it.copy(isLoading = true) }
            updateTaskUseCase(task).collect { result ->
                result
                    .onSuccess {
                        _uiState.update {
                            it.copy(isLoading = false)
                        }
                    }.onError { error ->
                        _effect.send(HomeEffect.ShowError(message = error.toUiText()))
                        _uiState.update { it.copy(isLoading = false) }
                    }
            }
        }

    private fun getTasks() =
        viewModelScope.launch(dispatcher) {
            _uiState.update { it.copy(isLoading = true) }
            getTasksUseCase().collect { result ->
                result
                    .onSuccess { tasks ->
                        _uiState.update {
                            it.copy(isLoading = false, tasks = tasks)
                        }
                    }.onError { error ->
                        _uiState.update { it.copy(isLoading = false) }
                        _effect.send(HomeEffect.ShowError(message = error.toUiText()))
                    }
            }
        }
}
