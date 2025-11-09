package dev.alimansour.mytasks.feature.home

import dev.alimansour.mytasks.core.domain.model.Task
import dev.alimansour.mytasks.core.ui.navigation.Route
import dev.alimansour.mytasks.core.ui.utils.UiText

data class HomeState(
    val isLoading: Boolean = false,
    val tasks: List<Task> = emptyList(),
    val effect: HomeEffect? = null,
)

sealed interface HomeEffect {
    data class NavigateToRoute(
        val route: Route,
    ) : HomeEffect

    data class ShowError(
        val message: UiText,
    ) : HomeEffect
}

sealed interface HomeEvent {
    object NavigateToNewTaskScreen : HomeEvent

    data class NavigateToTaskDetailsScreen(
        val task: Task,
    ) : HomeEvent

    object ConsumeEffect : HomeEvent
}
