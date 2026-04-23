package dev.alimansour.mytasks.feature.task

import androidx.compose.runtime.Stable
import dev.alimansour.mytasks.core.domain.model.Task
import dev.alimansour.mytasks.core.ui.utils.UiText

@Stable
data class TaskState(
    val isLoading: Boolean = false,
    val isInitialized: Boolean = false,
    val id: Long = 0,
    val title: String = "",
    val description: String = "",
    val dueDate: Long = System.currentTimeMillis(),
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

    object Proceed : UpdateTaskEvent
}
