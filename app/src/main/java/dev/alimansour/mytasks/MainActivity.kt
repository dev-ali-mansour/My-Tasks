package dev.alimansour.mytasks

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import dev.alimansour.mytasks.core.ui.theme.MyTasksTheme
import dev.alimansour.mytasks.core.ui.view.DebugCheckerBanner
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var isLoadingSplash = true
        lifecycleScope.launch {
            delay(timeMillis = 1500L)
            isLoadingSplash = false
        }

        installSplashScreen().setKeepOnScreenCondition {
            isLoadingSplash
        }

        enableEdgeToEdge(
            navigationBarStyle =
                SystemBarStyle.light(
                    Color.TRANSPARENT,
                    Color.TRANSPARENT,
                ),
        )

        setContent {
            MyTasksTheme {
                MainApp()
                if (BuildConfig.DEBUG) {
                    DebugCheckerBanner()
                }
            }
        }
    }
}
