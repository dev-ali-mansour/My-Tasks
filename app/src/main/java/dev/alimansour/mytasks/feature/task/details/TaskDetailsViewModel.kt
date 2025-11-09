package dev.alimansour.mytasks.feature.task.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.alimansour.mytasks.core.domain.model.Task
import dev.alimansour.mytasks.core.domain.model.onError
import dev.alimansour.mytasks.core.domain.model.onSuccess
import dev.alimansour.mytasks.core.domain.usecase.DeleteTaskUseCase
import dev.alimansour.mytasks.core.ui.utils.toUiText
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class TaskDetailsViewModel(
    private val dispatcher: CoroutineDispatcher,
    private val deleteTaskUseCase: DeleteTaskUseCase,
) : ViewModel() {
    private var deleteTaskJob: Job? = null
    private val _uiState = MutableStateFlow(TaskDetailsState())
    val uiState =
        _uiState
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000L),
                initialValue = _uiState.value,
            )
    val effect = _uiState.map { it.effect }

    fun processEvent(event: TaskDetailsEvent) {
        when (event) {
            is TaskDetailsEvent.LoadTask ->
                _uiState.update { it.copy(task = event.task) }

            is TaskDetailsEvent.UpdateTask -> {
                _uiState.value.task?.let { task ->
                    _uiState.update {
                        it.copy(effect = TaskDetailsEffect.NavigateToUpdateScreen(task))
                    }
                }
            }

            is TaskDetailsEvent.DeleteTask -> {
                _uiState.value.task?.let { task ->
                    deleteTaskJob?.cancel()
                    deleteTaskJob = launchDeleteTask(task)
                }
            }

            is TaskDetailsEvent.ConsumeEffect -> _uiState.update { it.copy(effect = null) }
        }
    }

    private fun launchDeleteTask(task: Task) =
        viewModelScope.launch(dispatcher) {
            _uiState.update { it.copy(isLoading = true) }
            deleteTaskUseCase(task).collect { result ->
                result
                    .onSuccess {
                        _uiState.update {
                            it.copy(isLoading = false, effect = TaskDetailsEffect.ShowSuccess)
                        }
                    }.onError { error ->
                        _uiState.update {
                            it.copy(isLoading = false, effect = TaskDetailsEffect.ShowError(message = error.toUiText()))
                        }
                    }
            }
        }
}
