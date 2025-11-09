package dev.alimansour.mytasks

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dev.alimansour.mytasks.core.ui.navigation.AppNavHost

@Composable
fun MainApp() {
    var topBarContent by remember { mutableStateOf<(@Composable () -> Unit)?>(null) }
    var fabContent by remember { mutableStateOf<(@Composable () -> Unit)?>(null) }

    Scaffold(
        topBar = {
            topBarContent?.invoke()
        },
        floatingActionButton = {
            fabContent?.invoke()
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
        )
    }
}
