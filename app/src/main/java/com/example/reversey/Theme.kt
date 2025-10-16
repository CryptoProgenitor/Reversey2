package com.example.reversey

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Represents a complete visual theme for the app
 */
data class AppTheme(
    val id: String,
    val name: String,
    val description: String,

    // Colors
    val primaryGradient: Brush,
    val cardBackground: Color,
    val cardBorder: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val buttonGradient: Brush,
    val accentColor: Color,

    // Effects
    val useGlassmorphism: Boolean = false,
    val glowIntensity: Float = 0f,

    // Typography
    val useSerifFont: Boolean = false,
    val useWideLetterSpacing: Boolean = false,

    // Emojis/Decorations
    val recordButtonEmoji: String = "🎤",
    val scoreEmojis: Map<Int, String> = mapOf(
        90 to "🔥",
        80 to "💕",
        70 to "✨",
        60 to "👍",
        0 to "💪"
    )
)

/**
 * Repository of all available themes
 */
object ThemeRepository {

    fun getThemeById(id: String): AppTheme {
        return allThemes.find { it.id == id } ?: y2kCyberTheme
    }


    // Y2K Cyber Pop Theme
    val y2kCyberTheme = AppTheme(
        id = "y2k_cyber",
        name = "Y2K Cyber Pop",
        description = "⚡ Hot pink, chrome, and early 2000s vibes",
        primaryGradient = Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFF6EC7),
                Color(0xFF7873F5),
                Color(0xFF4FACFE)
            )
        ),
        cardBackground = Color(0x26FFFFFF), // 15% white
        cardBorder = Color(0x4DFFFFFF), // 30% white
        textPrimary = Color.White,
        textSecondary = Color(0xFFFFFFFF),
        buttonGradient = Brush.horizontalGradient(
            colors = listOf(
                Color(0xFFFF6EC7),
                Color(0xFFFF1493)
            )
        ),
        accentColor = Color(0xFFFF6EC7),
        useGlassmorphism = true,
        glowIntensity = 0.8f,
        useWideLetterSpacing = true,
        recordButtonEmoji = "⚡",
        scoreEmojis = mapOf(
            90 to "💖",
            80 to "✨",
            70 to "💫",
            60 to "🌟",
            0 to "💪"
        )
    )

    // Cottagecore Dreams Theme
    val cottagecoredTheme = AppTheme(
        id = "cottagecore",
        name = "Cottagecore Dreams",
        description = "🌸 Soft pastels, florals, and cozy vibes",
        primaryGradient = Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFEEEF8),
                Color(0xFFFFF4E6),
                Color(0xFFE8F5E9)
            )
        ),
        cardBackground = Color.White,
        cardBorder = Color(0xFFF8BBD0),
        textPrimary = Color(0xFFD81B60),
        textSecondary = Color(0xFF880E4F),
        buttonGradient = Brush.horizontalGradient(
            colors = listOf(
                Color(0xFFF8BBD0),
                Color(0xFFF48FB1)
            )
        ),
        accentColor = Color(0xFFF8BBD0),
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
        )
    )

    // Dark Academia Glam Theme
    val darkAcademiaTheme = AppTheme(
        id = "dark_academia",
        name = "Dark Academia Glam",
        description = "📖 Moody gold, mysterious and sophisticated",
        primaryGradient = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF1A1A2E),
                Color(0xFF16213E),
                Color(0xFF0F3460)
            )
        ),
        cardBackground = Color(0x0DFFFFFF), // 5% white
        cardBorder = Color(0x4DFFD700), // 30% gold
        textPrimary = Color(0xFFFFD700),
        textSecondary = Color.White,
        buttonGradient = Brush.horizontalGradient(
            colors = listOf(
                Color(0xFFFFD700),
                Color(0xFFFF6B35)
            )
        ),
        accentColor = Color(0xFFFFD700),
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
            0 to "📝"
        )
    )

    // Neon Vaporwave Theme
    val vaporwaveTheme = AppTheme(
        id = "vaporwave",
        name = "Neon Vaporwave",
        description = "🌴 Cyan and magenta, retro-futuristic aesthetic",
        primaryGradient = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF2E003E),
                Color(0xFF3D0066),
                Color(0xFFFF6EC7)
            )
        ),
        cardBackground = Color(0x1A00FFFF), // 10% cyan
        cardBorder = Color(0xFF00FFFF),
        textPrimary = Color(0xFF00FFFF),
        textSecondary = Color.White,
        buttonGradient = Brush.horizontalGradient(
            colors = listOf(
                Color(0xFF00FFFF),
                Color(0xFFFF6EC7)
            )
        ),
        accentColor = Color(0xFF00FFFF),
        useGlassmorphism = true,
        glowIntensity = 0.6f,
        useWideLetterSpacing = true,
        recordButtonEmoji = "🌴",
        scoreEmojis = mapOf(
            90 to "💎",
            80 to "🌊",
            70 to "🎭",
            60 to "🎮",
            0 to "📼"
        )
    )
    val allThemes = listOf(
        y2kCyberTheme,
        cottagecoredTheme,
        darkAcademiaTheme,
        vaporwaveTheme
    )
}
