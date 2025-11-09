package dev.alimansour.mytasks.core.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.alimansour.mytasks.core.ui.utils.UiText
import dev.alimansour.mytasks.feature.home.HomeScreen

@Composable
fun AppNavHost(
    modifier: Modifier,
    onSetTopBar: (@Composable () -> Unit) -> Unit,
    onSetFab: (@Composable () -> Unit) -> Unit,
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
                },
                onSetTopBar = onSetTopBar,
                onSetFab = onSetFab,
                onFabClick = { navController.navigate(Route.NewTask) },
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
