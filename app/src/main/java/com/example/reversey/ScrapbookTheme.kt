package com.example.reversey

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Add this new theme to your ThemeRepository.kt
 * Insert this into the ThemeRepository object alongside your other themes
 */

// Anti-Design Scrapbook Theme
val scrapbookTheme = AppTheme(
    id = "scrapbook",
    name = "Scrapbook Vibes",
    description = "📝 Sticky notes, hand-drawn fun, and playful chaos",
    primaryGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFFFF3E0), // Light Orange
            Color(0xFFFFE0B2), // Lighter Orange
            Color(0xFFFFCCBC)  // Peach
        )
    ),
    cardBackground = Color(0xFFFFEB3B), // Default sticky note yellow
    cardBorder = Color(0xFF795548), // Brown border for hand-drawn look
    textPrimary = Color(0xFF212121), // Dark text for readability on bright colors
    textSecondary = Color(0xFF424242),
    buttonGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFFFFCDD2), // Light Pink
            Color(0xFFC8E6C9)  // Light Green
        )
    ),
    accentColor = Color(0xFFFF5722), // Bold orange accent
    useGlassmorphism = false, // No glassmorphism for scrapbook
    glowIntensity = 0f, // No glow effects
    useSerifFont = true, // Comic Sans substitute
    useWideLetterSpacing = false,
    recordButtonEmoji = "📝",
    scoreEmojis = mapOf(
        90 to "⭐",  // Star for excellent
        80 to "😊",  // Smiley for good
        70 to "👍",  // Thumbs up for okay
        60 to "😐",  // Neutral for average
        0 to "😔"   // Sad for poor
    )
)

/**
 * Scrapbook Mode Detector
 * Add this extension function to easily check if scrapbook mode is active
 */
fun AppTheme.isScrapbookMode(): Boolean {
    return this.id == "scrapbook"
}

/**
 * Enhanced AppTheme with scrapbook properties
 * You can add these optional properties to your existing AppTheme data class:
 *
 * data class AppTheme(
 *     // ... existing properties ...
 *     val useScrapbookStyle: Boolean = false,
 *     val useStarRatings: Boolean = false,
 *     val useHandDrawnElements: Boolean = false
 * )
 */

/**
 * Scrapbook Theme Configuration
 * Use this to configure scrapbook-specific behavior
 */
object ScrapbookConfig {
    val stickyNoteColors = listOf(
        Color(0xAAFFE3A4), // Yellow
        Color(0xFFFFCDD2), // Light Pink
        Color(0xFFBFFFD8), // Light Green
        Color(0xFFA4AAFF), // Light Blue
        Color(0xFFD1C4E9), // Light Purple
        Color(0xFFFFCC8F), // Peach
        Color(0xFFFAA4FF)  // Pink
    )

    const val maxRotationDegrees = 3f
    const val useComicSansFont = true
    const val showTapeEffects = true
    const val useStarRatings = true
}