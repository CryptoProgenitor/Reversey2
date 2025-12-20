package com.quokkalabs.reversey.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Aesthetic Theme Data - Non-color properties for visual themes
 * Works alongside Material 3 ColorScheme for complete theming
 */
@Immutable
data class AestheticThemeData(
    val id: String,
    val name: String,
    val description: String,

    // 🎨 GLUTE: Component Composition Architecture
    // Delegates UI logic to specific theme implementations
    val components: ThemeComponents,

    // Visual Effects
    val useGlassmorphism: Boolean = false,
    val glowIntensity: Float = 0f,

    // Typography Style
    val useSerifFont: Boolean = false,
    val useWideLetterSpacing: Boolean = false,

    // Theme-specific decorations
    val recordButtonEmoji: String = "🎤",
    val scoreEmojis: Map<Int, String> = mapOf(
        90 to "🔥",
        80 to "💕",
        70 to "✨",
        60 to "👍",
        0 to "💪"
    ),

    // Background gradients
    val primaryGradient: Brush,
    val cardBorder: Color,

    // Contrast-aware text colors
    val primaryTextColor: Color,
    val secondaryTextColor: Color,

    // Material 3 overrides
    val cardAlpha: Float = 1f,
    val shadowElevation: Float = 0f,
    val cardRotation: Float = 0f,
    val useHandDrawnBorders: Boolean = false,
    val borderWidth: Float = 2f,
    val maxCardRotation: Float = 0f,

    // 🆕 GLUTE V2: Personality & Interactions
    val dialogCopy: DialogCopy = DialogCopy.default(),
    val scoreFeedback: ScoreFeedback = ScoreFeedback.default(),
    val menuColors: MenuColors = MenuColors.fromColors(
        primaryTextColor, secondaryTextColor, cardBorder, primaryGradient
    ),
    // 🎖️ Pro Theme Indicator
    val isPro: Boolean = false
)

// --- NEW DATA CLASSES ---

@Immutable
data class DialogCopy(
    val deleteTitle: (DeletableItemType) -> String,
    val deleteMessage: (DeletableItemType, String) -> String,
    val deleteConfirmButton: String,
    val deleteCancelButton: String,
    val shareTitle: String,
    val shareMessage: String,
    val renameTitle: (RenamableItemType) -> String,
    val renameHint: String
) {
    companion object {
        fun default() = DialogCopy(
            deleteTitle = { type -> if (type == DeletableItemType.RECORDING) "Delete Recording?" else "Delete Attempt?" },
            deleteMessage = { type, name -> "Are you sure you want to delete '$name'? This cannot be undone." },
            deleteConfirmButton = "Delete",
            deleteCancelButton = "Cancel",
            shareTitle = "Share Audio",
            shareMessage = "Which version would you like to share?",
            renameTitle = { type -> if (type == RenamableItemType.RECORDING) "Rename Recording" else "Rename Player" },
            renameHint = "New Name"
        )
    }
}

@Immutable
data class ScoreFeedback(
    val getTitle: (Int) -> String,
    val getMessage: (Int) -> String,
    val getEmoji: (Int) -> String
) {
    companion object {
        fun default() = ScoreFeedback(
            getTitle = { score ->
                when {
                    score >= 90 -> "Amazing!"
                    score >= 80 -> "Great Job!"
                    score >= 70 -> "Good Effort!"
                    score >= 60 -> "Not Bad!"
                    else -> "Keep Trying!"
                }
            },
            getMessage = { score ->
                when {
                    score >= 90 -> "You nailed it!"
                    score >= 80 -> "Very close to the original."
                    score >= 70 -> "You're getting there."
                    score >= 60 -> "Practice makes perfect."
                    else -> "Don't give up!"
                }
            },
            getEmoji = { score ->
                when {
                    score >= 90 -> "🏆"
                    score >= 80 -> "🔥"
                    score >= 70 -> "✨"
                    score >= 60 -> "👍"
                    else -> "💪"
                }
            }
        )
    }
}

@Immutable
data class MenuColors(
    val menuBackground: Brush,
    val menuCardBackground: Color,
    val menuItemBackground: Color, // ← Added this
    val menuTitleText: Color,
    val menuItemText: Color,
    val menuDivider: Color,
    val menuBorder: Color,
    val toggleActive: Color,
    val toggleInactive: Color
) {
    companion object {
        fun fromColors(
            primaryText: Color,
            secondaryText: Color,
            border: Color,
            gradient: Brush
        ): MenuColors {
            return MenuColors(
                menuBackground = gradient,
                menuCardBackground = Color.White.copy(alpha = 0.9f),
                menuItemBackground = Color.White.copy(alpha = 0.5f), // ← Added default
                menuTitleText = primaryText,
                menuItemText = secondaryText,
                menuDivider = border.copy(alpha = 0.3f),
                menuBorder = border,
                toggleActive = border,
                toggleInactive = Color.Gray
            )
        }
    }
}

/**
 * Predefined Aesthetic Themes Registry
 * Maps ID strings to the Theme Objects created in Phases 2 & 3.
 */
object AestheticThemes {

    val allThemes = mapOf(
        // Basic Themes (Delegated to SharedDefaultComponents)
        Y2KTheme.THEME_ID to Y2KTheme.data,
        CottageTheme.THEME_ID to CottageTheme.data,
        DarkAcademiaTheme.THEME_ID to DarkAcademiaTheme.data,
        VaporwaveTheme.THEME_ID to VaporwaveTheme.data,
        JeoseungTheme.THEME_ID to JeoseungTheme.data,
        SteampunkTheme.THEME_ID to SteampunkTheme.data,
        CyberpunkTheme.THEME_ID to CyberpunkTheme.data,
        GraphiteTheme.THEME_ID to GraphiteTheme.data,

        // Pro Themes (Custom Implementations)
        ScrapbookTheme.THEME_ID to ScrapbookTheme.data,
        EggTheme.THEME_ID to EggTheme.data,
        SakuraSerenityTheme.THEME_ID to SakuraSerenityTheme.data,
        GuitarTheme.THEME_ID to GuitarTheme.data,
        SnowyOwlTheme.THEME_ID to SnowyOwlTheme.data,
        StrangePlanetTheme.THEME_ID to StrangePlanetTheme.data,

        // 🎄 Seasonal Themes
        ChristmasTheme.THEME_ID to ChristmasTheme.data

    )

    fun getThemeById(id: String): AestheticThemeData {
        return allThemes[id] ?: Y2KTheme.data
    }
}