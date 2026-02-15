package dev.alimansour.mytasks.core.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.alimansour.mytasks.core.ui.utils.UiText
import dev.alimansour.mytasks.feature.home.HomeScreen
import dev.alimansour.mytasks.feature.task.SelectedTaskViewModel
import dev.alimansour.mytasks.feature.task.UpdateTaskEvent
import dev.alimansour.mytasks.feature.task.add.NewTaskScreen
import dev.alimansour.mytasks.feature.task.details.TaskDetailsEvent
import dev.alimansour.mytasks.feature.task.details.TaskDetailsScreen
import dev.alimansour.mytasks.feature.task.details.TaskDetailsViewModel
import dev.alimansour.mytasks.feature.task.update.UpdateTaskScreen
import dev.alimansour.mytasks.feature.task.update.UpdateTaskViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    selectedTaskViewModel: SelectedTaskViewModel = koinViewModel(),
    onSuccess: (message: UiText) -> Unit,
    showError: (message: UiText) -> Unit,
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Route.Home,
        modifier = modifier.fillMaxSize(),
    ) {
        composable<Route.Home> {
            HomeScreen(
                navigateToRoute = {
                    navController.navigate(it)
                },
                navigateToTaskDetails = {
                    selectedTaskViewModel.onSelectTask(it)
                    navController.navigate(Route.TaskDetails)
                },
                onFabClick = { navController.navigate(Route.NewTask) },
                showError = showError,
            )
        }

        composable<Route.NewTask> {
            NewTaskScreen(
                onNavigationIconClicked = { navController.navigateUpSafely() },
                onSuccess = {
                    onSuccess(it)
                    navController.navigateUpSafely()
                },
                showError = showError,
            )
        }

        composable<Route.TaskDetails> {
            val viewModel: TaskDetailsViewModel = koinViewModel()
            val selectedTask by selectedTaskViewModel.selectedTask.collectAsStateWithLifecycle()

            LaunchedEffect(selectedTask) {
                selectedTask?.let { task ->
                    viewModel.processEvent(TaskDetailsEvent.LoadTask(task))
                }
            }

            TaskDetailsScreen(
                onNavigationIconClicked = { navController.navigateUpSafely() },
                onUpdateTaskClicked = {
                    selectedTaskViewModel.onSelectTask(it)
                    navController.navigate(Route.UpdateTask)
                },
                onSuccess = {
                    onSuccess(it)
                    navController.navigateUpSafely()
                },
                showError = showError,
            )
        }

        composable<Route.UpdateTask> {
            val viewModel: UpdateTaskViewModel = koinViewModel()
            val selectedTask by selectedTaskViewModel.selectedTask.collectAsStateWithLifecycle()

            LaunchedEffect(selectedTask) {
                selectedTask?.let { task ->
                    viewModel.processEvent(UpdateTaskEvent.LoadTask(task))
                }
            }

            UpdateTaskScreen(
                onNavigationIconClicked = { navController.navigateUpSafely() },
                onSuccess = {
                    onSuccess(it)
                    navController.navigate(Route.Home) {
                        popUpTo(navController.graph.id) {
                            inclusive = true
                        }
                    }
                },
                showError = showError,
            )
        }
    }
}

private fun NavHostController.navigateUpSafely() {
    if (previousBackStackEntry == null) {
        navigate(Route.Home)
    } else {
        navigateUp()
    }
}
