package dev.alimansour.mytasks.feature.task.add

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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

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
    val effect = _uiState.map { it.effect }

    fun processEvent(event: NewTaskEvent) {
        when (event) {
            is NewTaskEvent.UpdateTitle ->
                _uiState.update { it.copy(title = event.title) }

            is NewTaskEvent.UpdateDescription ->
                _uiState.update { it.copy(description = event.description) }

            is NewTaskEvent.UpdateDueDate ->
                _uiState.update {
                    it.copy(dueDate = event.dueDate)
                }

            is NewTaskEvent.Proceed -> {
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
                        addTaskJob?.cancel()
                        addTaskJob = launchAddNewTask()
                    }
                }
            }

            is NewTaskEvent.ConsumeEffect -> _uiState.update { it.copy(effect = null) }
        }
    }

    private fun launchAddNewTask() =
        viewModelScope.launch(dispatcher) {
            _uiState.update { it.copy(isLoading = true) }
            val task =
                Task(
                    title = uiState.value.title,
                    description = uiState.value.description,
                    dueDate = uiState.value.dueDate,
                )
            addTaskUseCase(task).collect { result ->
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
