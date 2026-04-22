package dev.alimansour.mytasks.feature.task.add.screen

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.alimansour.mytasks.R
import dev.alimansour.mytasks.core.domain.model.Task
import dev.alimansour.mytasks.core.domain.model.onError
import dev.alimansour.mytasks.core.domain.model.onSuccess
import dev.alimansour.mytasks.core.domain.usecase.AddTaskUseCase
import dev.alimansour.mytasks.core.ui.utils.UiText
import dev.alimansour.mytasks.core.ui.utils.toUiText
import dev.alimansour.mytasks.feature.task.NewTaskEvent
import dev.alimansour.mytasks.feature.task.TaskEffect
import dev.alimansour.mytasks.feature.task.TaskState
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
class NewTaskViewModel(
    private val dispatcher: CoroutineDispatcher,
    private val addTaskUseCase: AddTaskUseCase,
) : ViewModel() {
    private var addTaskJob: Job? = null
    private val _uiState = MutableStateFlow(TaskState())
    val uiState =
        _uiState
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000L),
                initialValue = _uiState.value,
            )
    private val _effect = Channel<TaskEffect>(capacity = Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun processEvent(event: NewTaskEvent) {
        viewModelScope.launch {
            when (event) {
                is NewTaskEvent.UpdateTitle -> {
                    _uiState.update { it.copy(title = event.title) }
                }

                is NewTaskEvent.UpdateDescription -> {
                    _uiState.update { it.copy(description = event.description) }
                }

                is NewTaskEvent.UpdateDueDate -> {
                    _uiState.update {
                        it.copy(dueDate = event.dueDate)
                    }
                }

                is NewTaskEvent.Proceed -> {
                    when {
                        _uiState.value.title.isBlank() -> {
                            _effect.send(TaskEffect.ShowError(message = UiText.StringResourceId(R.string.title_cannot_be_empty)))
                        }

                        _uiState.value.description.isBlank() -> {
                            _effect.send(TaskEffect.ShowError(message = UiText.StringResourceId(R.string.description_cannot_be_empty)))
                        }

                        else -> {
                            addTaskJob?.cancel()
                            addTaskJob = launchAddNewTask()
                        }
                    }
                }
            }
        }
    }

    private fun launchAddNewTask() =
        viewModelScope.launch(dispatcher) {
            _uiState.update { it.copy(isLoading = true) }
            val task =
                Task(
                    title = _uiState.value.title,
                    description = _uiState.value.description,
                    dueDate = _uiState.value.dueDate,
                )
            addTaskUseCase(task).collect { result ->
                result
                    .onSuccess {
                        _uiState.update {
                            it.copy(isLoading = false)
                        }
                        _effect.send(TaskEffect.ShowSuccess)
                    }.onError { error ->
                        _uiState.update {
                            it.copy(isLoading = false)
                        }
                        _effect.send(TaskEffect.ShowError(message = error.toUiText()))
                    }
            }
        }
}
