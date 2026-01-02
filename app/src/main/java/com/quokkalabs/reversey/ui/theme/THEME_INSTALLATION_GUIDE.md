# 🎨 ReVerseY Theme Installation Guide

**How to add a new theme to ReVerseY in 3 steps**

---

## Two Types of Themes

### 1. **Material Theme** (Uses Default Components)
Just define colors and settings. Uses the standard Material card layout.
Colors are injected via `materialPrimary` and card backgrounds.

**Example:** Cottage, Y2K, Vaporwave, Dark Academia, Cyberpunk, Graphite, Jeoseung, Steampunk

### 2. **Pro Theme** (Custom Components)
Custom card designs, special effects, unique layouts.
Fully self-contained with hardcoded colors - ignores Material theming.

**Example:** Strange Planet, Weird World, Egg, Snowy Owl, Sakura, Scrapbook, Guitar, Christmas

---

## Installation: Material Theme

### Step 1: Create Theme File

**Location:** `app/src/main/java/com/quokkalabs/reversey/ui/theme/`

**File:** `YourThemeComponents.kt`

```kotlin
package com.quokkalabs.reversey.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object YourTheme {
    const val THEME_ID = "your_theme_id"  // lowercase, underscores

    val data = AestheticThemeData(
        id = THEME_ID,
        name = "Your Theme Name",
        description = "🎨 Your theme description",
        components = DefaultThemeComponents(),  // ← Uses default Material cards
        
        // Required colors
        primaryGradient = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF123456),  // Top
                Color(0xFF234567),  // Middle  
                Color(0xFF345678)   // Bottom
            )
        ),
        accentColor = Color(0xFFFF00FF),        // Theme accent (waveforms, glows, decorations)
        materialPrimary = Color(0xFFFF00FF),    // Material3 seed (buttons, dialogs, ripples)
        primaryTextColor = Color(0xFFFFFFFF),   // Main text (must be readable!)
        secondaryTextColor = Color(0xFFCCCCCC), // Secondary text
        
        // Card backgrounds (nullable - falls back to Material surface if not set)
        cardBackgroundLight = Color(0xFFF5F5F5),  // Card color in light mode
        cardBackgroundDark = Color(0xFF2A2A2A),   // Card color in dark mode
        
        // Optional (customize if needed)
        recordButtonEmoji = "🎤",
        scoreEmojis = mapOf(
            90 to "🔥",
            80 to "💕", 
            70 to "✨",
            60 to "👍",
            0 to "💪"
        ),
        useSerifFont = false,
        useWideLetterSpacing = false,
        
        // Auto-defaults to accentColor (override if needed)
        scrollGlowColor = accentColor,  // Optional
        waveformColor = accentColor,    // Optional
        
        // Dialog/menu customization (optional)
        dialogCopy = DialogCopy.default(),
        scoreFeedback = ScoreFeedback.default(),
        menuColors = MenuColors.fromColors(
            primaryText = Color(0xFFFFFFFF),
            secondaryText = Color(0xFFCCCCCC),
            border = Color(0xFFFF00FF),
            gradient = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF123456),
                    Color(0xFF234567),
                    Color(0xFF345678)
                )
            )
        )
    )
}
```

### Step 2: Register in AestheticThemeData.kt

**File:** `app/src/main/java/com/quokkalabs/reversey/ui/theme/AestheticThemeData.kt`

Find the `allThemes` map:

```kotlin
object AestheticThemes {
    val allThemes = mapOf(
        Y2KTheme.THEME_ID to Y2KTheme.data,
        CottageTheme.THEME_ID to CottageTheme.data,
        // ... other themes
        
        YourTheme.THEME_ID to YourTheme.data  // ← Add this line
    )
}
```

### Step 3: Build & Test

1. Build project (`Ctrl+F9`)
2. Run app
3. Go to Settings → Theme
4. Select your theme
5. Test in **both light and dark mode**
6. Verify colors, text readability, cards, buttons

**Done!** ✅

---

## Installation: Pro Theme (Custom Components)

### Step 1: Create Theme File with Custom Components

**File:** `YourProThemeComponents.kt`

```kotlin
package com.quokkalabs.reversey.ui.theme

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
// ... more imports

// Theme Object
object YourProTheme {
    const val THEME_ID = "your_pro_theme"
    
    val data = AestheticThemeData(
        id = THEME_ID,
        name = "Your Pro Theme",
        description = "🌟 Custom everything!",
        components = YourProThemeComponents(),  // ← Custom implementation
        
        // Colors (same as material theme)
        primaryGradient = Brush.verticalGradient(...),
        accentColor = Color(0xFFFF00FF),
        primaryTextColor = Color(0xFFFFFFFF),
        secondaryTextColor = Color(0xFFCCCCCC),
        
        // Note: materialPrimary and cardBackground are ignored in Pro themes
        // Pro themes hardcode their own colors in custom components
        
        // Mark as pro
        isPro = true
    )
}

// Custom Components Implementation
class YourProThemeComponents : ThemeComponents {
    
    @Composable
    override fun RecordingItem(
        recording: Recording,
        aesthetic: AestheticThemeData,
        isPaused: Boolean,
        progress: Float,
        currentlyPlayingPath: String?,
        onPlay: (String) -> Unit,
        onPause: () -> Unit,
        onStop: () -> Unit,
        onDelete: (Recording) -> Unit,
        onShare: (String) -> Unit,
        onRename: (String, String) -> Unit,
        isGameModeEnabled: Boolean,
        onStartAttempt: (Recording, ChallengeType) -> Unit,
        activeAttemptRecordingPath: String?,
        onStopAttempt: (() -> Unit)?
    ) {
        // Your custom recording card design here
        // Hardcode your own colors - don't use MaterialTheme.colorScheme
        // See StrangePlanetThemeComponents.kt or WeirdWorldThemeComponents.kt for examples
    }
    
    @Composable
    override fun AttemptItem(
        attempt: PlayerAttempt,
        aesthetic: AestheticThemeData,
        currentlyPlayingPath: String?,
        isPaused: Boolean,
        progress: Float,
        onPlay: (String) -> Unit,
        onPause: () -> Unit,
        onStop: () -> Unit,
        onRenamePlayer: ((PlayerAttempt, String) -> Unit)?,
        onDeleteAttempt: ((PlayerAttempt) -> Unit)?,
        onShareAttempt: ((String) -> Unit)?,
        onJumpToParent: (() -> Unit)?,
        onOverrideScore: ((Int) -> Unit)?,
        onResetScore: (() -> Unit)?
    ) {
        // Your custom attempt card design here
    }
    
    // Custom dialogs - hardcode containerColor
    @Composable
    override fun DeleteDialog(...) {
        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = Color(0xFFYOURCOLOR),  // ← Hardcode, don't use MaterialTheme
            // ...
        )
    }
    
    // Optional: Custom backgrounds, effects
    @Composable
    override fun AppBackground(
        aesthetic: AestheticThemeData,
        modifier: Modifier,
        content: @Composable () -> Unit
    ) {
        // Custom background effects (optional)
        Box(modifier = modifier) {
            // Your special effects here
            content()
        }
    }
}
```

### Step 2: Register (Same as Material Theme)

Add to `AestheticThemes.allThemes` map in `AestheticThemeData.kt`

### Step 3: Build & Test

Same as material theme.

---

## Field Reference

### Required Fields

```kotlin
id: String                    // Unique ID (lowercase_underscore)
name: String                  // Display name
description: String           // User-facing description
components: ThemeComponents   // DefaultThemeComponents() or custom
primaryGradient: Brush        // Background gradient
accentColor: Color            // Accent/highlight color (decorations)
primaryTextColor: Color       // Main text color
secondaryTextColor: Color     // Secondary text color
```

### Material Theme Fields (for DefaultThemeComponents)

```kotlin
materialPrimary: Color = Color(0xFFFF6EC7)  // Material3 seed color for buttons/dialogs
                                             // Defaults to Hot Pink if not set

cardBackgroundLight: Color? = null  // Card color in light mode (null = Material surface)
cardBackgroundDark: Color? = null   // Card color in dark mode (null = Material surface)
```

### Optional Fields (Auto-Default)

```kotlin
scrollGlowColor: Color = accentColor      // Scroll edge glow
waveformColor: Color = accentColor        // Waveform bars
recordButtonEmoji: String = "🎤"
scoreEmojis: Map<Int, String> = defaults
useGlassmorphism: Boolean = false
glowIntensity: Float = 0f
useSerifFont: Boolean = false
useWideLetterSpacing: Boolean = false
cardAlpha: Float = 1f
shadowElevation: Float = 0f
isPro: Boolean = false
```

---

## Understanding materialPrimary vs accentColor

These serve different purposes:

| Property | Used For | Used By |
|----------|----------|---------|
| `accentColor` | Waveforms, scroll glows, decorative elements | Theme visuals |
| `materialPrimary` | Buttons, dialogs, ripples, Material UI | Material3 system |

They **can** be the same color, but don't have to be:

```kotlin
// Same color (simple)
accentColor = Color(0xFFF8BBD0),
materialPrimary = Color(0xFFF8BBD0),

// Different colors (advanced)
accentColor = Color(0x4DFFD700),      // Semi-transparent gold for decorations
materialPrimary = Color(0xFFFFD700),  // Solid gold for buttons (needs full opacity)
```

---

## Understanding Card Backgrounds

Cards adapt to light/dark mode using `LocalIsDarkTheme`:

```kotlin
// Warm cozy theme
cardBackgroundLight = Color(0xFFFFF8F0),  // Cream in light mode
cardBackgroundDark = Color(0xFF2D2520),   // Warm brown in dark mode

// Dark aesthetic theme (dark cards in both modes)
cardBackgroundLight = Color(0xFF2A2A3E),  // Dark purple even in "light" mode
cardBackgroundDark = Color(0xFF1A1A28),   // Darker purple in dark mode

// Let Material decide (nullable)
cardBackgroundLight = null,  // Uses MaterialTheme.colorScheme.surface
cardBackgroundDark = null,
```

---

## Color Tips

### Choosing Colors

**primaryGradient:** App background (top → bottom)
- Make sure text is readable!
- Test with both light and dark text

**accentColor:** Decorative elements, waveforms, scroll glow
- Should pop against your gradient
- Can be semi-transparent for effects

**materialPrimary:** Buttons, dialogs, system UI
- Must be fully opaque (0xFF prefix)
- Material3 generates a full color scheme from this seed

**primaryTextColor:** Headers, titles, important text
- MUST be readable on your gradient
- Light text on dark gradient, dark text on light gradient

**secondaryTextColor:** Body text, less important info
- Slightly less contrast than primary
- Still readable

### Testing Contrast

```kotlin
// Dark gradient → Use light text
primaryGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF000000), Color(0xFF333333))
)
primaryTextColor = Color(0xFFFFFFFF)   // White
secondaryTextColor = Color(0xFFCCCCCC) // Light gray

// Light gradient → Use dark text  
primaryGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFFFFFFFF), Color(0xFFCCCCCC))
)
primaryTextColor = Color(0xFF000000)   // Black
secondaryTextColor = Color(0xFF666666) // Dark gray
```

---

## Common Issues

### Theme doesn't appear in settings
**Fix:** Check Step 2 - did you add it to `allThemes` map?

### Build error: "Unresolved reference"
**Fix:** 
- File in correct location? (`ui/theme/`)
- Package correct? (`package com.quokkalabs.reversey.ui.theme`)

### Text is unreadable
**Fix:** Wrong text color for your gradient
- Dark gradient → light text
- Light gradient → dark text

### Buttons are wrong color
**Fix:** Set `materialPrimary` to your desired button color

### Cards are white/black and boring
**Fix:** Set `cardBackgroundLight` and `cardBackgroundDark`

### Dark mode shows light card / light mode shows dark card
**Fix:** This was a bug with `isSystemInDarkTheme()`. Now uses `LocalIsDarkTheme` which respects the app's dark mode setting (Light/Dark/System).

### Cards look broken (Pro themes only)
**Fix:** Check your `RecordingItem`/`AttemptItem` implementations
- See `StrangePlanetThemeComponents.kt` for reference
- Make sure all buttons/features are implemented

---

## Examples

### Minimal Theme (Uses Defaults)

```kotlin
object MinimalTheme {
    const val THEME_ID = "minimal"
    
    val data = AestheticThemeData(
        id = THEME_ID,
        name = "Minimal",
        description = "Clean and simple",
        components = DefaultThemeComponents(),
        primaryGradient = Brush.verticalGradient(
            colors = listOf(Color.White, Color(0xFFF5F5F5))
        ),
        accentColor = Color.Black,
        materialPrimary = Color.Black,
        primaryTextColor = Color.Black,
        secondaryTextColor = Color.Gray
        // cardBackgroundLight/Dark not set = uses Material surface
    )
}
```

### Fully Customized Material Theme

```kotlin
object CustomTheme {
    const val THEME_ID = "custom"
    
    val data = AestheticThemeData(
        id = THEME_ID,
        name = "Custom Theme",
        description = "🎨 Everything customized!",
        components = DefaultThemeComponents(),
        
        primaryGradient = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF1A1A2E),
                Color(0xFF16213E),
                Color(0xFF0F3460)
            )
        ),
        accentColor = Color(0xFFE94560),
        materialPrimary = Color(0xFFE94560),
        primaryTextColor = Color(0xFFFFFFFF),
        secondaryTextColor = Color(0xFFB0B0B0),
        
        // Custom card colors for light/dark mode
        cardBackgroundLight = Color(0xFF2A2A3E),
        cardBackgroundDark = Color(0xFF1A1A28),
        
        // Custom emojis
        recordButtonEmoji = "🎸",
        scoreEmojis = mapOf(
            90 to "🏆",
            80 to "🎯",
            70 to "⭐",
            60 to "👍",
            0 to "🎵"
        ),
        
        // Custom waveform color (different from accent)
        waveformColor = Color(0xFF00D9FF),
        
        // Typography
        useSerifFont = true,
        useWideLetterSpacing = true,
        
        // Custom dialog text
        dialogCopy = DialogCopy(
            deleteTitle = { "Remove ${if (it == DeletableItemType.RECORDING) "Track" else "Performance"}?" },
            deleteMessage = { _, name -> "Delete '$name' permanently?" },
            deleteConfirmButton = "Remove",
            deleteCancelButton = "Keep",
            shareTitle = "Share Audio",
            shareMessage = "Choose version:",
            renameTitle = { "Rename ${if (it == RenamableItemType.RECORDING) "Track" else "Artist"}" },
            renameHint = "New name"
        )
    )
}
```

---

## Quick Checklist

**Material Theme:**
- [ ] Create theme file in `ui/theme/`
- [ ] Set all required colors
- [ ] Set `materialPrimary` for buttons/dialogs
- [ ] Set `cardBackgroundLight`/`cardBackgroundDark` (optional)
- [ ] Use `DefaultThemeComponents()`
- [ ] Add to `allThemes` map
- [ ] Build & test in **both light and dark mode**

**Pro Theme:**
- [ ] Create theme file with custom `ThemeComponents`
- [ ] Implement `RecordingItem()`
- [ ] Implement `AttemptItem()`
- [ ] Implement custom dialogs with hardcoded `containerColor`
- [ ] Set all colors (materialPrimary/cardBackground ignored)
- [ ] Set `isPro = true`
- [ ] Add to `allThemes` map
- [ ] Build & test

---

## Reference Themes

**Material Themes (copy these):**
- `CottageThemeComponents.kt` - Warm pastel aesthetic
- `CyberpunkThemeComponents.kt` - Dark neon aesthetic
- `DarkAcademiaThemeComponents.kt` - Moody gold aesthetic
- `Y2KThemeComponents.kt` - Glassmorphism with alpha cards

**Pro Themes (study these):**
- `StrangePlanetThemeComponents.kt` - Full custom with aliens
- `WeirdWorldThemeComponents.kt` - Custom cards with effects
- `SakuraSerenityThemeComponents.kt` - Custom shapes and animations
- `EggThemeComponents.kt` - Animated background

---

## Architecture Note

As of v0.2.8, the theme system uses a "Dumb Pipe" architecture:

- **Material themes** inject colors via `materialPrimary` → `CreateReVerseYTheme` → `MaterialTheme.colorScheme`
- **Pro themes** are self-contained with hardcoded colors throughout

There is no longer a `getThemeAccentColor()` switch statement. Each theme is the single source of truth for its own colors.

---

**That's it! Most themes are just 30-40 lines of color definitions.** 🎨
