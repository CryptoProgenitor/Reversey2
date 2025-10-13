package com.example.reversey.ui.theme


import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Define the color schemes for each theme
private val DarkPurpleColorScheme = darkColorScheme(primary = Purple80, secondary = PurpleGrey80, tertiary = Pink80)
private val LightPurpleColorScheme = lightColorScheme(primary = Purple40, secondary = PurpleGrey40, tertiary = Pink40)

private val DarkBlueColorScheme = darkColorScheme(primary = Blue80, secondary = BlueGrey80, tertiary = LightBlue80)
private val LightBlueColorScheme = lightColorScheme(primary = Blue40, secondary = BlueGrey40, tertiary = LightBlue40)

private val DarkGreenColorScheme = darkColorScheme(primary = Green80, secondary = GreenGrey80, tertiary = LightGreen80)
private val LightGreenColorScheme = lightColorScheme(primary = Green40, secondary = GreenGrey40, tertiary = LightGreen40)

private val DarkOrangeColorScheme = darkColorScheme(primary = Orange80, secondary = OrangeGrey80, tertiary = LightOrange80)
private val LightOrangeColorScheme = lightColorScheme(primary = Orange40, secondary = OrangeGrey40, tertiary = LightOrange40)

@Composable
fun ReVerseYTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeName: String, // NEW: The name of the theme to apply
    content: @Composable () -> Unit
) {
    // Select the appropriate color scheme based on the theme name and dark mode
    val colorScheme = when (themeName) {
        "Blue" -> if (darkTheme) DarkBlueColorScheme else LightBlueColorScheme
        "Green" -> if (darkTheme) DarkGreenColorScheme else LightGreenColorScheme
        "Orange" -> if (darkTheme) DarkOrangeColorScheme else LightOrangeColorScheme
        else -> if (darkTheme) DarkPurpleColorScheme else LightPurpleColorScheme // Default to Purple
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Set the status bar color to be transparent to allow edge-to-edge
            window.statusBarColor = Color.Transparent.toArgb()
            // Set the navigation bar color to be transparent
            window.navigationBarColor = Color.Transparent.toArgb()

            // Ensure the content can draw behind the system bars
            WindowCompat.setDecorFitsSystemWindows(window, false)

            // Set the icons on the status bar (like time and battery) to be light or dark
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            // Set the icons on the navigation bar (if any) to be light or dark
            androidx.compose.ui.window.Window.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}