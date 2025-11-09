package dev.alimansour.mytasks.feature.task

import androidx.compose.runtime.Stable
import dev.alimansour.mytasks.core.domain.model.Task
import dev.alimansour.mytasks.core.ui.utils.UiText

@Stable
data class TaskState(
    val isLoading: Boolean = false,
    val title: String = "",
    val description: String = "",
    val dueDate: Long = System.currentTimeMillis(),
    val effect: TaskEffect? = null,
)

sealed interface TaskEffect {
    object ShowSuccess : TaskEffect

    data class ShowError(
        val message: UiText,
    ) : TaskEffect
}

sealed interface NewTaskEvent {
    data class UpdateTitle(
        val title: String,
    ) : NewTaskEvent

    data class UpdateDescription(
        val description: String,
    ) : NewTaskEvent

    data class UpdateDueDate(
        val dueDate: Long,
    ) : NewTaskEvent

    object Proceed : NewTaskEvent

    object ConsumeEffect : NewTaskEvent
}

sealed interface UpdateTaskEvent {
    data class UpdateTitle(
        val title: String,
    ) : UpdateTaskEvent

    data class UpdateDescription(
        val description: String,
    ) : UpdateTaskEvent

    data class UpdateDueDate(
        val dueDate: Long,
    ) : UpdateTaskEvent

    data class LoadTask(
        val task: Task,
    ) : UpdateTaskEvent

    object Proceed : UpdateTaskEvent

    object ConsumeEffect : UpdateTaskEvent
}
