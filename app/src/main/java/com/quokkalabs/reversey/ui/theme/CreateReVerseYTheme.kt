package com.quokkalabs.reversey.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb

/**
 * CompositionLocal for accessing aesthetic theme data
 */
val LocalAestheticTheme = compositionLocalOf<AestheticThemeData> {
    AestheticThemes.getThemeById("y2k_cyber")
}

/**
 * Unified ReVerseY Theme Provider
 * Combines Material 3 with aesthetic theme properties
 * Replaces the competing theme systems with a single, unified approach
 */
@Composable
fun ReVerseYTheme(
    aestheticThemeId: String = "y2k_cyber",
    customAccentColor: Color? = null,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Get the aesthetic theme data
    val aestheticTheme = AestheticThemes.getThemeById(aestheticThemeId)

    // Determine the accent color priority:
    // 1. Custom accent (from color picker)
    // 2. Theme's default accent (from aesthetic theme)
    val accentColor = customAccentColor ?: getThemeAccentColor(aestheticThemeId)

    // Generate Material 3 ColorScheme from the accent color
    val colorScheme = createDynamicColorScheme(
        accentColor = accentColor,
        darkTheme = darkTheme
    )

    // Provide both Material 3 theme and aesthetic theme
    CompositionLocalProvider(LocalAestheticTheme provides aestheticTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography(),
            content = content
        )
    }
}

/**
 * Get the default accent color for each aesthetic theme
 */
private fun getThemeAccentColor(themeId: String): Color {
    return when (themeId) {
        "y2k_cyber" -> Color(0xFFFF6EC7)           // Hot pink
        "scrapbook" -> Color(0xFFFF5722)           // Deep orange
        "cottagecore" -> Color(0xFFF8BBD0)         // Light pink
        "dark_academia" -> Color(0xFFFFD700)       // Gold
        "vaporwave" -> Color(0xFF00FFFF)           // Cyan
        "jeoseung_shadows" -> Color(0xFFFFD700)    // Gold
        "steampunk" -> Color(0xFFD4AF37)           // Brass gold
        "cyberpunk" -> Color(0xFF00FFFF)           // Neon cyan
        "graphite_sketch" -> Color(0xFF2A2A2A)     // Graphite gray
        "egg" -> Color(0xFFFF8A65)                 // 🥚 Coral orange (your daughter's design!)
        "sakura_serenity" -> Color(0xFFFF69B4)     // 🌸 ADD THIS LINE!
        else -> Color(0xFFFF6EC7)                  // Default to Y2K pink
    }
}

/**
 * Creates a complete Material3 ColorScheme from an accent color
 * This ensures all Material Design elements use the accent color
 */
private fun createDynamicColorScheme(
    accentColor: Color,
    darkTheme: Boolean
): ColorScheme {
    // Generate variants of the accent color
    if (darkTheme) {
        accentColor.lighten(0.2f)
    } else {
        accentColor.darken(0.1f)
    }

    val secondaryColor = accentColor.adjustHue(30f).desaturate(0.3f)

    return if (darkTheme) {
        darkColorScheme(
            primary = accentColor,
            primaryContainer = accentColor.darken(0.3f),
            secondary = secondaryColor,
            secondaryContainer = secondaryColor.darken(0.3f),
            tertiary = accentColor.adjustHue(-30f).desaturate(0.4f),
            surface = Color(0xFF121212),
            background = Color(0xFF121212),
            onPrimary = if (accentColor.luminance() > 0.5f) Color.Black else Color.White,
            onPrimaryContainer = Color.White,
            onSecondary = if (secondaryColor.luminance() > 0.5f) Color.Black else Color.White,
            onSurface = Color.White,
            onBackground = Color.White
        )
    } else {
        lightColorScheme(
            primary = accentColor,
            primaryContainer = accentColor.lighten(0.8f),
            secondary = secondaryColor,
            secondaryContainer = secondaryColor.lighten(0.8f),
            tertiary = accentColor.adjustHue(-30f).desaturate(0.4f),
            surface = Color.White,
            background = Color.White,
            onPrimary = if (accentColor.luminance() > 0.5f) Color.Black else Color.White,
            onPrimaryContainer = accentColor.darken(0.2f),
            onSecondary = if (secondaryColor.luminance() > 0.5f) Color.Black else Color.White,
            onSurface = Color.Black,
            onBackground = Color.Black
        )
    }
}

/**
 * Color manipulation utilities
 */
private fun Color.lighten(amount: Float): Color {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(this.toArgb(), hsv)
    hsv[2] = (hsv[2] + amount).coerceIn(0f, 1f) // Increase value/brightness
    return Color(android.graphics.Color.HSVToColor(hsv))
}

private fun Color.darken(amount: Float): Color {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(this.toArgb(), hsv)
    hsv[2] = (hsv[2] - amount).coerceIn(0f, 1f) // Decrease value/brightness
    return Color(android.graphics.Color.HSVToColor(hsv))
}

private fun Color.adjustHue(degrees: Float): Color {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(this.toArgb(), hsv)
    hsv[0] = (hsv[0] + degrees) % 360f // Adjust hue
    return Color(android.graphics.Color.HSVToColor(hsv))
}

private fun Color.desaturate(amount: Float): Color {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(this.toArgb(), hsv)
    hsv[1] = (hsv[1] - amount).coerceIn(0f, 1f) // Decrease saturation
    return Color(android.graphics.Color.HSVToColor(hsv))
}

/**
 * Utility composables for accessing theme data
 */
@Composable
fun AestheticTheme(): AestheticThemeData {
    return LocalAestheticTheme.current
}

@Composable
fun MaterialColors(): ColorScheme {
    return MaterialTheme.colorScheme
}

/**
 * Legacy compatibility - converts old AppTheme IDs to new system
 */
fun mapLegacyThemeId(oldId: String): String {
    return when (oldId) {
        "Purple", "Blue", "Green", "Orange" -> "y2k_cyber" // Default fallback
        else -> oldId // New IDs pass through unchanged
    }
}
