package com.example.reversey.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// --- COMPLETE, EXPLICIT LIGHT THEMES ---
private val LightPurpleColorScheme = lightColorScheme(
    primary = Purple40,
    onPrimary = Color.White,
    secondary = PurpleGrey40,
    onSecondary = Color.White,
    tertiary = Pink40,
    onTertiary = Color.White,
    background = Color(0xFFFFFBFE),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFBFE),
    onSurface = Color(0xFF1C1B1F)
)
private val LightBlueColorScheme = lightColorScheme(
    primary = Blue40,
    onPrimary = Color.White,
    secondary = BlueGrey40,
    onSecondary = Color.White,
    tertiary = LightBlue40,
    onTertiary = Color.White,
    background = Color(0xFFF8F9FF),
    onBackground = Color(0xFF191C23),
    surface = Color(0xFFF8F9FF),
    onSurface = Color(0xFF191C23)
)
private val LightGreenColorScheme = lightColorScheme(
    primary = Green40,
    onPrimary = Color.White,
    secondary = GreenGrey40,
    onSecondary = Color.White,
    tertiary = LightGreen40,
    onTertiary = Color.White,
    background = Color(0xFFF7FBF7),
    onBackground = Color(0xFF1A1C1A),
    surface = Color(0xFFF7FBF7),
    onSurface = Color(0xFF1A1C1A)
)
private val LightOrangeColorScheme = lightColorScheme(
    primary = Orange40,
    onPrimary = Color.White,
    secondary = OrangeGrey40,
    onSecondary = Color.White,
    tertiary = LightOrange40,
    onTertiary = Color.White,
    background = Color(0xFFFFF8F5),
    onBackground = Color(0xFF211A14),
    surface = Color(0xFFFFF8F5),
    onSurface = Color(0xFF211A14)
)

// --- COMPLETE, EXPLICIT DARK THEMES ---
private val DarkPurpleColorScheme = darkColorScheme(
    primary = Purple80,
    onPrimary = Color(0xFF381E72),
    secondary = PurpleGrey80,
    onSecondary = Color(0xFF332D41),
    tertiary = Pink80,
    onTertiary = Color(0xFF492532),
    background = Color(0xFF1C1B1F),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1C1B1F),
    onSurface = Color(0xFFE6E1E5)
)
private val DarkBlueColorScheme = darkColorScheme(
    primary = Blue80,
    onPrimary = Color(0xFF002E69),
    secondary = BlueGrey80,
    onSecondary = Color(0xFF2D313C),
    tertiary = LightBlue80,
    onTertiary = Color(0xFF003355),
    background = Color(0xFF111418),
    onBackground = Color(0xFFE1E2E8),
    surface = Color(0xFF111418),
    onSurface = Color(0xFFE1E2E8)
)
private val DarkGreenColorScheme = darkColorScheme(
    primary = Green80,
    onPrimary = Color(0xFF0A3911),
    secondary = GreenGrey80,
    onSecondary = Color(0xFF2C352D),
    tertiary = LightGreen80,
    onTertiary = Color(0xFF203624),
    background = Color(0xFF121411),
    onBackground = Color(0xFFE1E3DE),
    surface = Color(0xFF121411),
    onSurface = Color(0xFFE1E3DE)
)
private val DarkOrangeColorScheme = darkColorScheme(
    primary = Orange80,
    onPrimary = Color(0xFF4F2F00),
    secondary = OrangeGrey80,
    onSecondary = Color(0xFF3E3024),
    tertiary = LightOrange80,
    onTertiary = Color(0xFF512A00),
    background = Color(0xFF1A120B),
    onBackground = Color(0xFFF0DFD1),
    surface = Color(0xFF1A120B),
    onSurface = Color(0xFFF0DFD1)
)

@Composable
fun ReVerseYTheme(
    darkTheme: Boolean,
    themeName: String,
    content: @Composable () -> Unit
) {
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
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}