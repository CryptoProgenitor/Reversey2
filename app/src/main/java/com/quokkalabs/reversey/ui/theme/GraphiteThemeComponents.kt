package com.quokkalabs.reversey.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * ✏️ GRAPHITE SKETCH THEME
 * Hand-drawn art, pencil textures, paper vibes.
 */
object GraphiteTheme {
    const val THEME_ID = "graphite_sketch"

    val data = AestheticThemeData(
        id = THEME_ID,
        name = "Graphite Sketch",
        description = "✏️ Hand-drawn art, pencil textures, paper vibes",
        components = DefaultThemeComponents(),  // 🔧 DRY: No more boilerplate!

        // Visuals
        primaryGradient = Brush.verticalGradient(
            colors = listOf(
                Color(0xFFF8F8F8),
                Color(0xFFEEEEEE),
                Color(0xFFE0E0E0)
            )
        ),
        accentColor = Color(0xFF2A2A2A),
        materialPrimary = Color(0xFF2A2A2A),
        cardBackgroundLight = Color(0xFFF5F5F5),
        cardBackgroundDark = Color(0xFF3A3A3A),
        primaryTextColor = Color(0xFF2A2A2A),
        secondaryTextColor = Color(0xFF505050),
        useGlassmorphism = false,
        glowIntensity = 0f,
        useSerifFont = false,
        useWideLetterSpacing = false,
        recordButtonEmoji = "✏️",
        scoreEmojis = mapOf(
            90 to "⭐",
            80 to "😊",
            70 to "👍",
            60 to "😐",
            0 to "😔"
        ),

        // M3 Overrides
        shadowElevation = 4f,

        // Interaction
        dialogCopy = DialogCopy(
            deleteTitle = { "Erase Sketch?" },
            deleteMessage = { type, name -> "Are you sure you want to erase '$name'? This artwork cannot be restored." },
            deleteConfirmButton = "Erase",
            deleteCancelButton = "Keep Art",
            shareTitle = "Exhibit Work",
            shareMessage = "Choose a gallery for your sketch:",
            renameTitle = { "Title Artwork" },
            renameHint = "Untitled Sketch"
        ),
        scoreFeedback = ScoreFeedback(
            getTitle = { score ->
                when {
                    score >= 90 -> "Masterpiece!"
                    score >= 80 -> "Beautiful Composition!"
                    score >= 70 -> "Strong Outline!"
                    score >= 60 -> "Rough Sketch"
                    else -> "Back to Basics"
                }
            },
            getMessage = { score ->
                when {
                    score >= 90 -> "Frame this immediately. It's perfect."
                    score >= 80 -> "Excellent shading and depth."
                    score >= 70 -> "Good form, needs a bit more detail."
                    score >= 60 -> "Keep sharpening your pencils."
                    else -> "Every artist starts somewhere. Try again!"
                }
            },
            getEmoji = { score ->
                when {
                    score >= 90 -> "🎨"
                    score >= 80 -> "🖼️"
                    score >= 70 -> "✏️"
                    score >= 60 -> "📝"
                    else -> "🗑️"
                }
            }
        ),
        menuColors = MenuColors.fromColors(
            primaryText = Color(0xFF2A2A2A),
            secondaryText = Color(0xFF505050),
            border = Color(0xFF2A2A2A),
            gradient = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFF8F8F8),
                    Color(0xFFEEEEEE),
                    Color(0xFFE0E0E0)
                )
            )
        )
    )
}
