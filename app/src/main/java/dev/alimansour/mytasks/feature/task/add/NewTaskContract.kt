package dev.alimansour.mytasks.feature.task.add

import androidx.compose.runtime.Stable
import dev.alimansour.mytasks.core.ui.utils.UiText

@Stable
data class NewTaskState(
    val isLoading: Boolean = false,
    val title: String = "",
    val description: String = "",
    val dueDate: Long = System.currentTimeMillis(),
    val effect: NewTaskEffect? = null,
)

sealed interface NewTaskEffect {
    object ShowSuccess : NewTaskEffect

    data class ShowError(
        val message: UiText,
    ) : NewTaskEffect
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
