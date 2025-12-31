package com.quokkalabs.reversey.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * 📖 DARK ACADEMIA GLAM THEME
 * Moody gold, mysterious and sophisticated.
 */
object DarkAcademiaTheme {
    const val THEME_ID = "dark_academia"

    val data = AestheticThemeData(
        id = THEME_ID,
        name = "Dark Academia Glam",
        description = "📖 Moody gold, mysterious and sophisticated",
        components = DefaultThemeComponents(),  // 🔧 DRY: No more boilerplate!

        // Visuals
        primaryGradient = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF1A1A2E),
                Color(0xFF16213E),
                Color(0xFF0F3460)
            )
        ),
        accentColor = Color(0x4DFFD700),
        primaryTextColor = Color(0xFFFFD700),
        secondaryTextColor = Color(0xFFE8E8E8),
        useGlassmorphism = true,
        glowIntensity = 0.5f,
        useSerifFont = true,
        useWideLetterSpacing = true,
        recordButtonEmoji = "📖",
        scoreEmojis = mapOf(
            90 to "⭐",
            80 to "✒️",
            70 to "📚",
            60 to "🕯️",
            0 to "📓"
        ),

        // M3 Overrides
        cardAlpha = 0.05f,
        shadowElevation = 8f,

        // Interaction
        dialogCopy = DialogCopy.default(),
        scoreFeedback = ScoreFeedback.default(),
        menuColors = MenuColors.fromColors(
            primaryText = Color(0xFFFFD700),
            secondaryText = Color(0xFFE8E8E8),
            border = Color(0x4DFFD700),
            gradient = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF1A1A2E),
                    Color(0xFF16213E),
                    Color(0xFF0F3460)
                )
            )
        )
    )
}
