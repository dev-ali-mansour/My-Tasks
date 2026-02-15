package dev.alimansour.mytasks

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import dev.alimansour.mytasks.core.ui.navigation.AppNavHost
import kotlinx.coroutines.launch

@Composable
fun MainApp() {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { innerPadding ->
        AppNavHost(
            modifier =
                Modifier
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding),
            onSuccess = { uiText ->
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = uiText.asString(context),
                        duration = SnackbarDuration.Short,
                    )
                }
            },
            showError = { uiText ->
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = uiText.asString(context),
                        duration = SnackbarDuration.Short,
                    )
                }
            },
        )
    }
}
