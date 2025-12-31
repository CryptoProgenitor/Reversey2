package com.quokkalabs.reversey.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * ⚡ Y2K CYBER POP THEME
 * Hot pink, chrome, and early 2000s vibes.
 */
object Y2KTheme {
    const val THEME_ID = "y2k_cyber"

    val data = AestheticThemeData(
        id = THEME_ID,
        name = "Y2K Cyber Pop",
        description = "⚡ Hot pink, chrome, and early 2000s vibes",
        components = DefaultThemeComponents(),

        // Visuals
        primaryGradient = Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFF6EC7),
                Color(0xFF7873F5),
                Color(0xFF4FACFE)
            )
        ),
        accentColor = Color(0x4DFFFFFF),
        primaryTextColor = Color.White,
        secondaryTextColor = Color(0xFFE0E0E0),
        useGlassmorphism = true,
        glowIntensity = 0.8f,
        useWideLetterSpacing = true,
        recordButtonEmoji = "⚡",

        // M3 Overrides
        cardAlpha = 0.7f,
        shadowElevation = 16f,

        // Interaction
        dialogCopy = DialogCopy.default(),
        scoreFeedback = ScoreFeedback.default(),
        menuColors = MenuColors.fromColors(
            primaryText = Color.White,
            secondaryText = Color(0xFFE0E0E0),
            border = Color(0x4DFFFFFF),
            gradient = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFFF6EC7),
                    Color(0xFF7873F5),
                    Color(0xFF4FACFE)
                )
            )
        )
    )
}
