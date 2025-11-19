package dev.alimansour.mytasks.feature.home

import androidx.compose.runtime.Stable
import dev.alimansour.mytasks.core.domain.model.Task
import dev.alimansour.mytasks.core.ui.navigation.Route
import dev.alimansour.mytasks.core.ui.utils.UiText

@Stable
data class HomeState(
    val isLoading: Boolean = false,
    val isFabExpanded: Boolean = false,
    val openDialog: Boolean = false,
    val tasks: List<Task> = emptyList(),
    val effect: HomeEffect? = null,
)

sealed interface HomeEffect {
    data class NavigateToRoute(
        val route: Route,
    ) : HomeEffect

    data class NavigateToTaskDetails(
        val task: Task,
    ) : HomeEffect

    data class ShowError(
        val message: UiText,
    ) : HomeEffect

    object ExitApp : HomeEffect
}

sealed interface HomeEvent {
    data class OnExpandStateChanged(
        val isExpanded: Boolean,
    ) : HomeEvent

    object OnBackPress : HomeEvent

    object OnExitDialogConfirmed : HomeEvent

    object OnExitDialogCancelled : HomeEvent

    object NavigateToNewTaskScreen : HomeEvent

    data class NavigateToTaskDetailsScreen(
        val task: Task,
    ) : HomeEvent

    data class OnTaskCheckChanged(
        val task: Task,
    ) : HomeEvent

    object ConsumeEffect : HomeEvent
}
