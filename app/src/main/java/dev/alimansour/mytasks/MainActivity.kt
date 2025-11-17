package dev.alimansour.mytasks

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dev.alimansour.mytasks.core.ui.theme.MyTasksTheme
import dev.alimansour.mytasks.core.ui.view.DebugCheckerBanner

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
