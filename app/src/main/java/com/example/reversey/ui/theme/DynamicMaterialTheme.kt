package com.example.reversey.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb

/**
 * Dynamic Material Theme Provider
 * Applies custom accent colors to Material Design color scheme
 * This affects: radio buttons, switches, scroll glows, progress indicators, etc.
 */
@Composable
fun DynamicMaterialTheme(
    customAccentColor: Color?,
    fallbackAccentColor: Color, // NEW: actual theme's accent
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
){
    val colorScheme = if (customAccentColor != null) {
        // Generate dynamic color scheme from custom accent
        createDynamicColorScheme(customAccentColor, darkTheme)
    } else {
        // Use fallback accent color (current theme's accent)
        createDynamicColorScheme(fallbackAccentColor, darkTheme)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}

/**
 * Creates a complete Material3 ColorScheme from a custom accent color
 * This ensures all Material Design elements use the custom color
 */
private fun createDynamicColorScheme(
    accentColor: Color,
    darkTheme: Boolean
): ColorScheme {
    // Generate variants of the accent color
    val primaryVariant = if (darkTheme) {
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