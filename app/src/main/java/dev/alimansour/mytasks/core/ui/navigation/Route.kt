package dev.alimansour.mytasks.core.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Route {
    @Serializable
    data object Home : Route()

    @Serializable
    data object NewTask : Route()

    @Serializable
    data class TaskDetails(val taskId: Long) : Route()

    @Serializable
    data class UpdateTask(val taskId: Long) : Route()
}
