package com.example.reversey.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.reversey.ui.theme.SakuraSerenityComponents
import com.example.reversey.ui.theme.SnowyOwlComponents


// ADD THIS LINE:
import com.example.reversey.ui.theme.ThemeComponents
import com.example.reversey.ui.theme.DefaultThemeComponents
import com.example.reversey.ui.theme.EggThemeComponents
import com.example.reversey.ui.theme.ScrapbookThemeComponents
import com.example.reversey.ui.theme.GuitarComponents
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
    val components: ThemeComponents = DefaultThemeComponents(),

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

    // Background gradients (used for non-M3 backgrounds)
    val primaryGradient: Brush,
    val cardBorder: Color,


    // 🎨 GLUTE PRINCIPLE: Contrast-aware text colors for gradient backgrounds
    val primaryTextColor: Color,      // Main title text
    val secondaryTextColor: Color,    // Description text

    // Additional M3 overrides
    val cardAlpha: Float = 1f, // For glassmorphism effect
    val shadowElevation: Float = 0f, // For glow effects
    val cardRotation: Float = 0f,     // Card rotation in degrees
    val useHandDrawnBorders: Boolean = false,  // Hand-drawn style borders
    val borderWidth: Float = 2f,        // Border thickness
    val maxCardRotation: Float = 0f,


)
/**
 * Predefined Aesthetic Themes
 * Each corresponds to Material 3 color scheme + aesthetic properties
 */
object AestheticThemes {

    val Y2KCyber = AestheticThemeData(
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
        cardBorder = Color(0x4DFFFFFF),
        primaryTextColor = Color.White,        // ✅ High contrast on bright gradient
        secondaryTextColor = Color(0xFFE0E0E0), // ✅ Slightly dimmed for hierarchy
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
        ),
        cardAlpha = 0.7f,
        shadowElevation = 16f
    )

    val Scrapbook = AestheticThemeData(
        id = "scrapbook",
        name = "Scrapbook Vibes",
        description = "📝 Sticky notes, hand-drawn fun, and playful chaos",
        components = ScrapbookThemeComponents(),  // ADD THIS LINE
        primaryGradient = Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFFF3E0),
                Color(0xFFFFE0B2),
                Color(0xFFFFCCBC)
            )
        ),
        cardBorder = Color(0xFF795548),
        primaryTextColor = Color(0xFF3E2723),     // ✅ Dark brown on light background
        secondaryTextColor = Color(0xFF5D4037),   // ✅ Medium brown for description
        useGlassmorphism = false,
        glowIntensity = 0f,
        useSerifFont = true,
        useWideLetterSpacing = false,
        recordButtonEmoji = "📝",
        scoreEmojis = mapOf(
            90 to "⭐",
            80 to "😊",
            70 to "👍",
            60 to "😐",
            0 to "😔"
        ),
        cardAlpha = 1f,
        shadowElevation = 0f,
        maxCardRotation = 3f,    // Slight rotation for hand-placed look
        borderWidth = 3f         // Thicker borders for scrapbook feel
    )

    val Cottagecore = AestheticThemeData(
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
        cardBorder = Color(0xFFF8BBD0),
        primaryTextColor = Color(0xFF2E2E2E),     // ADD THIS
        secondaryTextColor = Color(0xFF424242),   // ADD THIS
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

    val DarkAcademia = AestheticThemeData(
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
        cardBorder = Color(0x4DFFD700),
        primaryTextColor = Color(0xFFFFD700),     // ADD THIS
        secondaryTextColor = Color(0xFFE8E8E8),   // ADD THIS
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
        ),
        cardAlpha = 0.05f,
        shadowElevation = 8f
    )

    val Vaporwave = AestheticThemeData(
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
        cardBorder = Color(0xFF00FFFF),
        primaryTextColor = Color(0xFF00FFFF),     // ADD THIS
        secondaryTextColor = Color(0xFFE0E0E0),   // ADD THIS
        useGlassmorphism = true,
        useWideLetterSpacing = true,
        recordButtonEmoji = "🌴",
        scoreEmojis = mapOf(
            90 to "💎",
            80 to "🌊",
            70 to "🎭",
            60 to "🎮",
            0 to "📼"
        ),
        cardAlpha = 0.1f,
        shadowElevation = 12f
    )

    val JeoseungShadows = AestheticThemeData(
        id = "jeoseung_shadows",
        name = "Jeoseung Shadows",
        description = "💀 Dark reaper energy, golden souls and mysteries",
        primaryGradient = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF0A0A0A),
                Color(0xFF1C1C1C),
                Color(0xFF2D2D2D)
            )
        ),
        cardBorder = Color(0x66FFD700),
        primaryTextColor = Color(0xFFFFD700),     // ADD THIS
        secondaryTextColor = Color(0xFFCCCCCC),   // ADD THIS
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
        cardAlpha = 0.1f,
        shadowElevation = 14f
    )

    val Steampunk = AestheticThemeData(
        id = "steampunk",
        name = "Steampunk Victorian",
        description = "⚙️ Brass gears, copper pipes, and steam power",
        primaryGradient = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF2C1810),
                Color(0xFF8B4513),
                Color(0xFFCD7F32)
            )
        ),
        cardBorder = Color(0xFFD4AF37),
        primaryTextColor = Color(0xFFD4AF37),     // ADD THIS
        secondaryTextColor = Color(0xFFF5E6D3),   // ADD THIS
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
        shadowElevation = 6f
    )

    val Cyberpunk = AestheticThemeData(
        id = "cyberpunk",
        name = "Cyberpunk 2099",
        description = "🤖 Neon lights, digital underground, matrix vibes",
        primaryGradient = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF0A0A0A),
                Color(0xFF1A0033),
                Color(0xFF000A1A)
            )
        ),
        cardBorder = Color(0xFF00FFFF),
        primaryTextColor = Color(0xFF00FFFF),     // ADD THIS
        secondaryTextColor = Color(0xFF80FF80),   // ADD THIS
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
        cardAlpha = 0f,
        shadowElevation = 18f
    )

    val GraphiteSketch = AestheticThemeData(
        id = "graphite_sketch",
        name = "Graphite Sketch",
        description = "✏️ Hand-drawn art, pencil textures, paper vibes",
        primaryGradient = Brush.verticalGradient(
            colors = listOf(
                Color(0xFFF8F8F8),
                Color(0xFFEEEEEE),
                Color(0xFFE0E0E0)
            )
        ),
        cardBorder = Color(0xFF2A2A2A),
        primaryTextColor = Color(0xFF2A2A2A),     // ADD THIS
        secondaryTextColor = Color(0xFF505050),   // ADD THIS
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
        )
    )

    val Egg = AestheticThemeData(
        id = "egg",
        name = "Egg Theme",
        description = "🥚 CPD's adorable egg-inspired design!",
        components = EggThemeComponents(),  // ADD THIS LINE
        primaryGradient = Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFFF8E1), // Cream background
                Color(0xFFFFF3E0), // Slightly warmer cream
                Color(0xFFFFE0B2)  // Light orange
            )
        ),
        cardBorder = Color(0xFF2E2E2E),
        primaryTextColor = Color(0xFF2E2E2E),     // ADD THIS
        secondaryTextColor = Color(0xFF424242),   // ADD THIS
        useGlassmorphism = false,
        glowIntensity = 0f,
        useSerifFont = false,
        useWideLetterSpacing = true,
        recordButtonEmoji = "🥚",
        scoreEmojis = mapOf(
            90 to "🍳", // Fried egg for perfect!
            80 to "🥚", // Whole egg for great
            70 to "🐣", // Hatching for good
            60 to "🐥", // Chick for okay
            0 to "🥀"   // Wilted for poor
        ),
        // Card styling
        cardAlpha = 1f,
        shadowElevation = 6f, // Nice shadow like her sketch
        useHandDrawnBorders = true,
        borderWidth = 4f // Thick black borders
    )

    // 🌸 ADD THIS TO AestheticThemeData.kt in the AestheticThemes object

    val SakuraSerenity = AestheticThemeData(
        id = "sakura_serenity",
        name = "Sakura Serenity",
        description = "🌸 Cherry blossoms and sunset gradients",
        components = SakuraSerenityComponents(),  // ← Your component!
        primaryTextColor = Color.White,           // ✨ WHITE for visibility on dark blue
        secondaryTextColor = Color(0xFFFFC0CB),   // ✨ Light pink for descriptions

        // Cherry blossom pink to coral sunset gradient
        primaryGradient = Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFFB6C1), // Light pink
                Color(0xFFFF69B4), // Hot pink
                Color(0xFFFFA07A), // Light coral sunset
                Color(0xFFFF7F50)  // Coral orange
            )
        ),
        cardBorder = Color(0xFFFF69B4),


        // Visual effects
        useGlassmorphism = false,
        glowIntensity = 0.3f,
        recordButtonEmoji = "🌸",
        scoreEmojis = mapOf(
            90 to "⭐",
            80 to "🌸",
            70 to "🌙",
            60 to "✨",
            0 to "🌱"
        ),

        // Material 3 overrides
        cardAlpha = 0.95f,
        shadowElevation = 8f,
        useHandDrawnBorders = false,
        borderWidth = 2f,

        // Sakura-specific settings
        maxCardRotation = 0f
    )

    val Guitar = AestheticThemeData(
        id = "guitar",
        name = "Guitar Acoustic",
        description = "🎸 Taylor Swift Folklore vibes for CPD!",
        components = GuitarComponents(),

        primaryGradient = Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFFF8E1),  // Cream
                Color(0xFFFFF3E0),  // Warm beige
                Color(0xFFFFE0B2),  // Soft peach
                Color(0xFFFFDFC1)   // Peachy bottom
            )
        ),
        cardBorder = Color(0xFF5d4a36),
        primaryTextColor = Color(0xFF3E2723),
        secondaryTextColor = Color(0xFF5D4037),

        useGlassmorphism = false,
        glowIntensity = 0f,
        useSerifFont = true,
        useWideLetterSpacing = false,

        recordButtonEmoji = "🎸",
        scoreEmojis = mapOf(
            90 to "⭐",
            80 to "🎵",
            70 to "🎶",
            60 to "🎤",
            0 to "🎻"
        ),

        cardAlpha = 1f,
        shadowElevation = 6f,
        maxCardRotation = 0f,
        borderWidth = 3f
    )

    val SnowyOwl = AestheticThemeData(
        id = "snowy_owl",
        name = "Snowy Owl",
        description = "🦉 Arctic midnight with flying owl and falling snow",
        components = SnowyOwlComponents(),

        primaryGradient = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF0a1128),
                Color(0xFF1c2541),
                Color(0xFF2d3a5f),
                Color(0xFF3a4a6d)
            )
        ),

        cardBorder = Color(0xFF787896).copy(alpha = 0.4f),
        primaryTextColor = Color.White,
        secondaryTextColor = Color.White.copy(alpha = 0.8f),

        useGlassmorphism = false,
        glowIntensity = 0f,
        recordButtonEmoji = "🦉",
        scoreEmojis = mapOf(
            90 to "⭐",
            80 to "❄️",
            70 to "🌙",
            60 to "🦉",
            0 to "💫"
        ),

        cardAlpha = 0.95f,
        shadowElevation = 8f
    )

    // Map for easy lookup
    val allThemes = mapOf(
        "y2k_cyber" to Y2KCyber,
        "scrapbook" to Scrapbook,
        "cottagecore" to Cottagecore,
        "dark_academia" to DarkAcademia,
        "vaporwave" to Vaporwave,
        "jeoseung_shadows" to JeoseungShadows,
        "steampunk" to Steampunk,
        "cyberpunk" to Cyberpunk,
        "graphite_sketch" to GraphiteSketch,
        "egg" to Egg,  // 🥚 ADD THIS LINE!
        "sakura_serenity" to SakuraSerenity,  // 🌸 ADD THIS LINE!
        "guitar" to Guitar,  // 🎸 CPD's theme!
        "snowy_owl" to SnowyOwl  // ← ADD THIS!
    )

    fun getThemeById(id: String): AestheticThemeData {
        return allThemes[id] ?: Y2KCyber
    }
}
