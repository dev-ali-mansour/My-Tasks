package dev.alimansour.mytasks.core.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed class Route : NavKey {
    @Serializable
    data object Home : Route()

    @Serializable
    data object NewTask : Route()

    @Serializable
    data class TaskDetails(val taskId: Long) : Route()

    @Serializable
    data class UpdateTask(val taskId: Long) : Route()
}
