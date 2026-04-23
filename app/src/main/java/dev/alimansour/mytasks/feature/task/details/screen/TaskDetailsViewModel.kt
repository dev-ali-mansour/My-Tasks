package dev.alimansour.mytasks.feature.task.details.screen

import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dev.alimansour.mytasks.core.domain.model.Task
import dev.alimansour.mytasks.core.domain.model.onError
import dev.alimansour.mytasks.core.domain.model.onSuccess
import dev.alimansour.mytasks.core.domain.usecase.DeleteTaskUseCase
import dev.alimansour.mytasks.core.domain.usecase.GetTaskByIdUseCase
import dev.alimansour.mytasks.core.ui.navigation.Route
import dev.alimansour.mytasks.core.ui.utils.toUiText
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@Stable
@KoinViewModel
class TaskDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val dispatcher: CoroutineDispatcher,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val getTaskByIdUseCase: GetTaskByIdUseCase,
) : ViewModel() {
    private val taskId = savedStateHandle.toRoute<Route.TaskDetails>().taskId
    private var loadTaskJob: Job? = null
    private var deleteTaskJob: Job? = null
    private val _uiState = MutableStateFlow(TaskDetailsState())
    val uiState =
        _uiState
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000L),
                initialValue = _uiState.value,
            )
    private val _effect = Channel<TaskDetailsEffect>(capacity = Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        loadTask()
    }

    private fun loadTask() {
        loadTaskJob?.cancel()
        loadTaskJob =
            viewModelScope.launch(dispatcher) {
                _uiState.update { it.copy(isLoading = true) }
                getTaskByIdUseCase(taskId).collect { result ->
                    result
                        .onSuccess { task ->
                            _uiState.update {
                                it.copy(isLoading = false, task = task)
                            }
                        }.onError { error ->
                            _uiState.update {
                                it.copy(isLoading = false)
                            }
                            _effect.send(TaskDetailsEffect.ShowError(message = error.toUiText()))
                        }
                }
            }
    }

    fun processEvent(event: TaskDetailsEvent) {
        viewModelScope.launch {
            when (event) {
                is TaskDetailsEvent.DeleteTask -> {
                    _uiState.value.task?.let { task ->
                        deleteTaskJob?.cancel()
                        deleteTaskJob = launchDeleteTask(task)
                    }
                }
            }
        }
    }

    private fun launchDeleteTask(task: Task) =
        viewModelScope.launch(dispatcher) {
            _uiState.update { it.copy(isLoading = true) }
            deleteTaskUseCase(task).collect { result ->
                result
                    .onSuccess {
                        _uiState.update {
                            it.copy(isLoading = false)
                        }
                        _effect.send(TaskDetailsEffect.ShowSuccess)
                    }.onError { error ->
                        _uiState.update {
                            it.copy(isLoading = false)
                        }
                        _effect.send(TaskDetailsEffect.ShowError(message = error.toUiText()))
                    }
            }
        }
}
