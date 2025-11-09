package dev.alimansour.mytasks.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
    darkColorScheme(
        primary = BrandGreen,
        onPrimary = DarkOnPrimary,
        background = DarkGreenBackground,
        surface = DarkGreenBackground,
        onBackground = TextWhite,
        onSurface = TextWhite,
        onSurfaceVariant = TextGray,
        // For secondary text like "Due Today"
        // You can fill in the rest (secondary, tertiary, error) as needed
    )

private val LightColorScheme =
    lightColorScheme(
        primary = BrandGreen,
        onPrimary = LightOnPrimary,
        background = LightBackground,
        surface = LightBackground,
        onBackground = LightText,
        onSurface = LightText,
        onSurfaceVariant = LightSecondaryText,
        // For less important text// For secondary text
        // You can fill in the rest (secondary, tertiary, error) as needed
    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
     */
    )

@Composable
fun MyTasksTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }

            darkTheme -> DarkColorScheme
            else -> LightColorScheme
        }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
