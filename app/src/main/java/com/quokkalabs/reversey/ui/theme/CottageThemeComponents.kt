package com.quokkalabs.reversey.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * 🌸 COTTAGECORE DREAMS THEME
 * Soft pastels, florals, and cozy vibes.
 */
object CottageTheme {
    const val THEME_ID = "cottagecore"

    val data = AestheticThemeData(
        id = THEME_ID,
        name = "Cottagecore Dreams",
        description = "🌸 Soft pastels, florals, and cozy vibes",
        components = DefaultThemeComponents(),  // 🔧 DRY: No more boilerplate!

        // Visuals
        primaryGradient = Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFEEEF8),
                Color(0xFFFFF4E6),
                Color(0xFFE8F5E9)
            )
        ),
        accentColor = Color(0xFFF8BBD0),
        primaryTextColor = Color(0xFF2E2E2E),
        secondaryTextColor = Color(0xFF424242),
        useGlassmorphism = false,
        glowIntensity = 0f,
        useSerifFont = true,
        recordButtonEmoji = "🌸",
        scoreEmojis = mapOf(
            90 to "🦋",
            80 to "🌷",
            70 to "🌼",
            60 to "🌿",
            0 to "🌱"
        ),

        // Interaction
        dialogCopy = DialogCopy.default(),
        scoreFeedback = ScoreFeedback.default(),
        menuColors = MenuColors.fromColors(
            primaryText = Color(0xFF2E2E2E),
            secondaryText = Color(0xFF424242),
            border = Color(0xFFF8BBD0),
            gradient = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFFEEEF8),
                    Color(0xFFFFF4E6),
                    Color(0xFFE8F5E9)
                )
            )
        )
    )
}
