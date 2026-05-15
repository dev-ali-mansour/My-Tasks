package dev.alimansour.mytasks

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import dev.alimansour.mytasks.core.ui.navigation.AppNavHost
import kotlinx.coroutines.launch

@Composable
fun MainApp() {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        AppNavHost(
            modifier = Modifier,
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

        SnackbarHost(
            hostState = snackbarHostState,
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .safeDrawingPadding(),
        )
    }
}
