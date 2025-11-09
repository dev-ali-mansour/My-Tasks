package dev.alimansour.mytasks

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import dev.alimansour.mytasks.core.ui.navigation.AppNavHost
import kotlinx.coroutines.launch

@Composable
fun MainApp() {
    var topBarContent by remember { mutableStateOf<(@Composable () -> Unit)?>(null) }
    var fabContent by remember { mutableStateOf<(@Composable () -> Unit)?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            topBarContent?.invoke()
        },
        floatingActionButton = {
            fabContent?.invoke()
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { innerPadding ->
        AppNavHost(
            modifier = Modifier.padding(innerPadding),
            onSetTopBar = {
                topBarContent = it
            },
            onSetFab = {
                fabContent = it
            },
            onSuccess = { uiText ->
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = uiText.asString(context),
                        duration = SnackbarDuration.Long,
                    )
                }
            },
            showError = { uiText ->
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = uiText.asString(context),
                        duration = SnackbarDuration.Long,
                    )
                }
            },
        )
    }
}
