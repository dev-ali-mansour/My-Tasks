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
import dev.alimansour.mytasks.feature.home.screen.HomeScreen
import dev.alimansour.mytasks.feature.task.add.screen.NewTaskScreen
import dev.alimansour.mytasks.feature.task.details.screen.TaskDetailsScreen
import dev.alimansour.mytasks.feature.task.update.screen.UpdateTaskScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
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
                    navController.navigate(Route.TaskDetails(it.id))
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
            TaskDetailsScreen(
                onNavigationIconClicked = { navController.navigateUpSafely() },
                onUpdateTaskClicked = {
                    navController.navigate(Route.UpdateTask(it.id))
                },
                onSuccess = {
                    onSuccess(it)
                    navController.navigateUpSafely()
                },
                showError = showError,
            )
        }

        composable<Route.UpdateTask> {
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
