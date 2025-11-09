package dev.alimansour.mytasks

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.navigation.compose.rememberNavController
import dev.alimansour.mytasks.core.ui.navigation.NavGraph

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val layoutDirection = LocalLayoutDirection.current

    Scaffold { paddingValues ->
        Column(
            modifier =
                Modifier
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .fillMaxSize()
                    .padding(
                        start = paddingValues.calculateStartPadding(layoutDirection),
                        end = paddingValues.calculateEndPadding(layoutDirection),
                        bottom = paddingValues.calculateBottomPadding(),
                    ),
        ) {
            NavGraph(navController = navController)
        }
    }
}
