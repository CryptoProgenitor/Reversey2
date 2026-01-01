package com.quokkalabs.reversey.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * 💀 JEOSEUNG SHADOWS THEME
 * Dark reaper energy, golden souls and mysteries.
 */
object JeoseungTheme {
    const val THEME_ID = "jeoseung_shadows"

    val data = AestheticThemeData(
        id = THEME_ID,
        name = "Jeoseung Shadows",
        description = "💀 Dark reaper energy, golden souls and mysteries",
        components = DefaultThemeComponents(),  // 🔧 DRY: No more boilerplate!

        // Visuals
        primaryGradient = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF0A0A0A),
                Color(0xFF1C1C1C),
                Color(0xFF2D2D2D)
            )
        ),
        accentColor = Color(0x66FFD700),
        materialPrimary = Color(0xFFFFD700),
        cardBackgroundLight = Color(0xFF3A3A4A),
        cardBackgroundDark = Color(0xFF1A1A28),
        primaryTextColor = Color(0xFFFFD700),
        secondaryTextColor = Color(0xFFCCCCCC),
        useGlassmorphism = true,
        glowIntensity = 0.7f,
        useSerifFont = true,
        useWideLetterSpacing = true,
        recordButtonEmoji = "💀",
        scoreEmojis = mapOf(
            90 to "🔥",
            80 to "👻",
            70 to "🌙",
            60 to "⚡",
            0 to "💀"
        ),

        // M3 Overrides
        cardAlpha = 0.1f,
        shadowElevation = 14f,

        // Interaction
        dialogCopy = DialogCopy.default(),
        scoreFeedback = ScoreFeedback.default(),
        menuColors = MenuColors.fromColors(
            primaryText = Color(0xFFFFD700),
            secondaryText = Color(0xFFCCCCCC),
            border = Color(0x66FFD700),
            gradient = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF0A0A0A),
                    Color(0xFF1C1C1C),
                    Color(0xFF2D2D2D)
                )
            )
        )
    )
}
