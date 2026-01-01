package com.quokkalabs.reversey.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * ⚙️ STEAMPUNK VICTORIAN THEME
 * Brass gears, copper pipes, and steam power.
 */
object SteampunkTheme {
    const val THEME_ID = "steampunk"

    val data = AestheticThemeData(
        id = THEME_ID,
        name = "Steampunk Victorian",
        description = "⚙️ Brass gears, copper pipes, and steam power",
        components = DefaultThemeComponents(),

        // Visuals
        primaryGradient = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF2C1810),
                Color(0xFF8B4513),
                Color(0xFFCD7F32)
            )
        ),
        accentColor = Color(0xFFD4AF37),
        materialPrimary = Color(0xFFD4AF37),
        cardBackgroundLight = Color(0xFFF5E6D3),
        cardBackgroundDark = Color(0xFF3D2A1E),
        primaryTextColor = Color(0xFFD4AF37),
        secondaryTextColor = Color(0xFFF5E6D3),
        useGlassmorphism = false,
        glowIntensity = 0.4f,
        useSerifFont = true,
        useWideLetterSpacing = true,
        recordButtonEmoji = "⚙️",
        scoreEmojis = mapOf(
            90 to "🏆",
            80 to "⚗️",
            70 to "🎩",
            60 to "⚙️",
            0 to "🔧"
        ),

        // M3 Overrides
        shadowElevation = 6f,

        // Interaction
        dialogCopy = DialogCopy(
            deleteTitle = { "Dismantle Mechanism?" },
            deleteMessage = { type, name -> "Are you certain you wish to scrap '$name'? The gears cannot be reassembled." },
            deleteConfirmButton = "Dismantle",
            deleteCancelButton = "Maintain",
            shareTitle = "Transmit via Telegraph",
            shareMessage = "Select frequency for transmission:",
            renameTitle = { "Re-label Blueprint" },
            renameHint = "New Specification"
        ),
        scoreFeedback = ScoreFeedback(
            getTitle = { score ->
                when {
                    score >= 90 -> "Master Engineer!"
                    score >= 80 -> "Splendid Invention!"
                    score >= 70 -> "Functional Prototype!"
                    score >= 60 -> "Needs Calibration!"
                    else -> "Steam Leak Detected!"
                }
            },
            getMessage = { score ->
                when {
                    score >= 90 -> "Precision engineering at its finest."
                    score >= 80 -> "The gears are turning smoothly."
                    score >= 70 -> "Operational, but noisy."
                    score >= 60 -> "Tighten the bolts and try again."
                    else -> "Back to the drawing board, old chap."
                }
            },
            getEmoji = { score ->
                when {
                    score >= 90 -> "🏆"
                    score >= 80 -> "🎩"
                    score >= 70 -> "⚙️"
                    score >= 60 -> "🔧"
                    else -> "💨"
                }
            }
        ),
        menuColors = MenuColors.fromColors(
            primaryText = Color(0xFFD4AF37),
            secondaryText = Color(0xFFF5E6D3),
            border = Color(0xFFD4AF37),
            gradient = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF2C1810),
                    Color(0xFF8B4513),
                    Color(0xFFCD7F32)
                )
            )
        )
    )
}
