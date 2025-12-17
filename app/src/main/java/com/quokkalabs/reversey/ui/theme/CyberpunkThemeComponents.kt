package com.quokkalabs.reversey.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * 🤖 CYBERPUNK 2099 THEME
 * Neon lights, digital underground, matrix vibes.
 */
object CyberpunkTheme {
    const val THEME_ID = "cyberpunk"

    val data = AestheticThemeData(
        id = THEME_ID,
        name = "Cyberpunk 2099",
        description = "🤖 Neon lights, digital underground, matrix vibes",
        components = DefaultThemeComponents(),  // 🔧 DRY: No more boilerplate!

        // Visuals
        primaryGradient = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF0A0A0A),
                Color(0xFF1A0033),
                Color(0xFF000A1A)
            )
        ),
        cardBorder = Color(0xFF00FFFF),
        primaryTextColor = Color(0xFF00FFFF),
        secondaryTextColor = Color(0xFF80FF80),
        useGlassmorphism = true,
        glowIntensity = 0.9f,
        useSerifFont = false,
        useWideLetterSpacing = true,
        recordButtonEmoji = "🤖",
        scoreEmojis = mapOf(
            90 to "👑",
            80 to "🤖",
            70 to "⚡",
            60 to "🔥",
            0 to "💻"
        ),

        // M3 Overrides
        cardAlpha = 0f,
        shadowElevation = 18f,

        // Interaction
        dialogCopy = DialogCopy(
            deleteTitle = { "ERASE_DATA_FRAGMENT?" },
            deleteMessage = { type, name -> "Confirm deletion of '$name'. Data recovery will be impossible." },
            deleteConfirmButton = "[CONFIRM_DELETION]",
            deleteCancelButton = "[ABORT]",
            shareTitle = "UPLINK_ESTABLISHED",
            shareMessage = "Select network protocol for transmission:",
            renameTitle = { "MODIFY_METADATA" },
            renameHint = "Enter_New_ID"
        ),
        scoreFeedback = ScoreFeedback(
            getTitle = { score ->
                when {
                    score >= 90 -> "SYSTEM_HACKED!"
                    score >= 80 -> "ACCESS_GRANTED"
                    score >= 70 -> "FIREWALL_BYPASSED"
                    score >= 60 -> "CONNECTION_UNSTABLE"
                    else -> "ACCESS_DENIED"
                }
            },
            getMessage = { score ->
                when {
                    score >= 90 -> "Root access obtained. Flawless execution."
                    score >= 80 -> "Security protocols neutralized."
                    score >= 70 -> "Data packet integrity acceptable."
                    score >= 60 -> "Signal noise detected. Optimize algorithm."
                    else -> "Critical failure. Reboot and retry."
                }
            },
            getEmoji = { score ->
                when {
                    score >= 90 -> "👑"
                    score >= 80 -> "🤖"
                    score >= 70 -> "⚡"
                    score >= 60 -> "⚠️"
                    else -> "🚫"
                }
            }
        ),
        menuColors = MenuColors.fromColors(
            primaryText = Color(0xFF00FFFF),
            secondaryText = Color(0xFF80FF80),
            border = Color(0xFF00FFFF),
            gradient = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF0A0A0A),
                    Color(0xFF1A0033),
                    Color(0xFF000A1A)
                )
            )
        )
    )
}