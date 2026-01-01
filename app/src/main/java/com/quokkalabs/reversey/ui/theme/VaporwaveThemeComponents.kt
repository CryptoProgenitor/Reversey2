package com.quokkalabs.reversey.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * 🌴 NEON VAPORWAVE THEME
 * Cyan and magenta, retro-futuristic aesthetic.
 */
object VaporwaveTheme {
    const val THEME_ID = "vaporwave"

    val data = AestheticThemeData(
        id = THEME_ID,
        name = "Neon Vaporwave",
        description = "🌴 Cyan and magenta, retro-futuristic aesthetic",
        components = DefaultThemeComponents(),

        // Visuals
        primaryGradient = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF2E003E),
                Color(0xFF3D0066),
                Color(0xFFFF6EC7)
            )
        ),
        accentColor = Color(0xFF00FFFF),
        materialPrimary = Color(0xFF00FFFF),
        cardBackgroundLight = Color(0xFF3D2050),
        cardBackgroundDark = Color(0xFF1E0028),
        primaryTextColor = Color(0xFF00FFFF),
        secondaryTextColor = Color(0xFFE0E0E0),
        useGlassmorphism = true,
        glowIntensity = 0f,
        useSerifFont = false,
        useWideLetterSpacing = true,
        recordButtonEmoji = "🌴",
        scoreEmojis = mapOf(
            90 to "💎",
            80 to "🌊",
            70 to "🎭",
            60 to "🎮",
            0 to "📼"
        ),

        // M3 Overrides
        cardAlpha = 0.1f,
        shadowElevation = 12f,

        // Interaction
        dialogCopy = DialogCopy.default(),
        scoreFeedback = ScoreFeedback.default(),
        menuColors = MenuColors.fromColors(
            primaryText = Color(0xFF00FFFF),
            secondaryText = Color(0xFFE0E0E0),
            border = Color(0xFF00FFFF),
            gradient = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF2E003E),
                    Color(0xFF3D0066),
                    Color(0xFFFF6EC7)
                )
            )
        )
    )
}
