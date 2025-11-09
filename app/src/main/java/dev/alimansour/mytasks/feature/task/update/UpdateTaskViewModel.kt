package dev.alimansour.mytasks.feature.task.update

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.alimansour.mytasks.R
import dev.alimansour.mytasks.core.domain.model.Task
import dev.alimansour.mytasks.core.domain.model.onError
import dev.alimansour.mytasks.core.domain.model.onSuccess
import dev.alimansour.mytasks.core.domain.usecase.UpdateTaskUseCase
import dev.alimansour.mytasks.core.ui.utils.UiText
import dev.alimansour.mytasks.core.ui.utils.toUiText
import dev.alimansour.mytasks.feature.task.TaskEffect
import dev.alimansour.mytasks.feature.task.TaskState
import dev.alimansour.mytasks.feature.task.UpdateTaskEvent
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
class UpdateTaskViewModel(
    private val dispatcher: CoroutineDispatcher,
    private val updateTaskUseCase: UpdateTaskUseCase,
) : ViewModel() {
    private var updateTaskJob: Job? = null
    private val _uiState = MutableStateFlow(TaskState())
    val uiState =
        _uiState
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000L),
                initialValue = _uiState.value,
            )
    val effect = _uiState.map { it.effect }

    fun processEvent(event: UpdateTaskEvent) {
        when (event) {
            is UpdateTaskEvent.UpdateTitle ->
                _uiState.update { it.copy(title = event.title) }

            is UpdateTaskEvent.UpdateDescription ->
                _uiState.update { it.copy(description = event.description) }

            is UpdateTaskEvent.UpdateDueDate ->
                _uiState.update {
                    it.copy(dueDate = event.dueDate)
                }

            is UpdateTaskEvent.LoadTask ->
                _uiState.update {
                    it.copy(
                        id = event.task.id,
                        title = event.task.title,
                        description = event.task.description,
                        dueDate = event.task.dueDate,
                    )
                }

            is UpdateTaskEvent.Proceed -> {
                when {
                    uiState.value.title.isBlank() ->
                        _uiState.update {
                            it.copy(
                                effect =
                                    TaskEffect.ShowError(message = UiText.StringResourceId(R.string.title_cannot_be_empty)),
                            )
                        }

                    uiState.value.description.isBlank() ->
                        _uiState.update {
                            it.copy(
                                effect =
                                    TaskEffect.ShowError(message = UiText.StringResourceId(R.string.description_cannot_be_empty)),
                            )
                        }

                    else -> {
                        updateTaskJob?.cancel()
                        updateTaskJob = launchUpdateTask()
                    }
                }
            }

            is UpdateTaskEvent.ConsumeEffect -> _uiState.update { it.copy(effect = null) }
        }
    }

    private fun launchUpdateTask() =
        viewModelScope.launch(dispatcher) {
            _uiState.update { it.copy(isLoading = true) }
            val task =
                Task(
                    id = uiState.value.id,
                    title = uiState.value.title,
                    description = uiState.value.description,
                    dueDate = uiState.value.dueDate,
                )
            updateTaskUseCase(task).collect { result ->
                result
                    .onSuccess {
                        _uiState.update {
                            it.copy(isLoading = false, effect = TaskEffect.ShowSuccess)
                        }
                    }.onError { error ->
                        _uiState.update {
                            it.copy(isLoading = false, effect = TaskEffect.ShowError(message = error.toUiText()))
                        }
                    }
            }
        }
}
