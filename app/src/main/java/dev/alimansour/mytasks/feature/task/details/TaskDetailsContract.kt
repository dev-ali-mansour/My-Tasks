package dev.alimansour.mytasks.feature.task.details

import androidx.compose.runtime.Stable
import dev.alimansour.mytasks.core.domain.model.Task
import dev.alimansour.mytasks.core.ui.utils.UiText

@Stable
data class TaskDetailsState(
    val isLoading: Boolean = false,
    val task: Task? = null,
    val effect: TaskDetailsEffect? = null,
)

sealed interface TaskDetailsEffect {
    data class NavigateToUpdateScreen(
        val task: Task,
    ) : TaskDetailsEffect

    object ShowSuccess : TaskDetailsEffect

    data class ShowError(
        val message: UiText,
    ) : TaskDetailsEffect
}

sealed interface TaskDetailsEvent {
    data class LoadTask(
        val task: Task,
    ) : TaskDetailsEvent

    object UpdateTask : TaskDetailsEvent

    object DeleteTask : TaskDetailsEvent

    object ConsumeEffect : TaskDetailsEvent
}
