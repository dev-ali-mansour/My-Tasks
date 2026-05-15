package dev.alimansour.mytasks.core.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import dev.alimansour.mytasks.core.ui.utils.UiText
import dev.alimansour.mytasks.feature.home.screen.HomeScreen
import dev.alimansour.mytasks.feature.task.add.screen.NewTaskScreen
import dev.alimansour.mytasks.feature.task.details.screen.TaskDetailsScreen
import dev.alimansour.mytasks.feature.task.details.screen.TaskDetailsViewModel
import dev.alimansour.mytasks.feature.task.update.screen.UpdateTaskScreen
import dev.alimansour.mytasks.feature.task.update.screen.UpdateTaskViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    onSuccess: (message: UiText) -> Unit,
    showError: (message: UiText) -> Unit,
) {
    val navigationState = rememberNavigationState(
        startRoute = Route.Home,
        topLevelRoutes = setOf(Route.Home)
    )
    val navigator = remember { Navigator(navigationState) }

    val entryProvider: (NavKey) -> NavEntry<NavKey> = entryProvider {
        entry<Route.Home> {
            HomeScreen(
                navigateToRoute = { navigator.navigate(it) },
                navigateToTaskDetails = { navigator.navigate(Route.TaskDetails(it.id)) },
                onFabClick = { navigator.navigate(Route.NewTask) },
                showError = showError,
            )
        }

        entry<Route.NewTask> {
            NewTaskScreen(
                onNavigationIconClicked = { navigator.goBack() },
                onSuccess = {
                    onSuccess(it)
                    navigator.goBack()
                },
                showError = showError,
            )
        }

        entry<Route.TaskDetails> { key ->
            val viewModel: TaskDetailsViewModel = org.koin.androidx.compose.koinViewModel { org.koin.core.parameter.parametersOf(key.taskId) }
            TaskDetailsScreen(
                viewModel = viewModel,
                onNavigationIconClicked = { navigator.goBack() },
                onUpdateTaskClicked = { navigator.navigate(Route.UpdateTask(it.id)) },
                onSuccess = {
                    onSuccess(it)
                    navigator.goBack()
                },
                showError = showError,
            )
        }

        entry<Route.UpdateTask> { key ->
            val viewModel: UpdateTaskViewModel = org.koin.androidx.compose.koinViewModel { org.koin.core.parameter.parametersOf(key.taskId) }
            UpdateTaskScreen(
                viewModel = viewModel,
                onNavigationIconClicked = { navigator.goBack() },
                onSuccess = {
                    onSuccess(it)
                    // Clear backstack and go home
                    navigator.goBack()
                    navigator.goBack()
                },
                showError = showError,
            )
        }
    }

    NavDisplay(
        modifier = modifier.fillMaxSize(),
        entries = navigationState.toEntries(entryProvider),
        onBack = { navigator.goBack() }
    )
}
