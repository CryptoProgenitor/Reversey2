# 🎨 ReVerseY Theme Installation Guide

**How to add a new theme to ReVerseY in 3 steps**

---

## Two Types of Themes

### 1. **Simple Theme** (Uses Default Components)
Just define colors and settings. Uses the standard card layout.

**Example:** Cottage, Y2K, Vaporwave, Dark Academia

### 2. **Pro Theme** (Custom Components)
Custom card designs, special effects, unique layouts.

**Example:** Strange Planet, Weird World, Egg, Snowy Owl

---

## Installation: Simple Theme

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
        components = DefaultThemeComponents(),  // ← Uses default cards
        
        // Required colors
        primaryGradient = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF123456),  // Top
                Color(0xFF234567),  // Middle  
                Color(0xFF345678)   // Bottom
            )
        ),
        accentColor = Color(0xFFFF00FF),        // Accent/highlight color
        primaryTextColor = Color(0xFFFFFFFF),   // Main text (must be readable!)
        secondaryTextColor = Color(0xFFCCCCCC), // Secondary text
        
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
5. Verify colors, text readability, cards

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
        
        // Colors (same as simple theme)
        primaryGradient = Brush.verticalGradient(...),
        accentColor = Color(0xFFFF00FF),
        primaryTextColor = Color(0xFFFFFFFF),
        secondaryTextColor = Color(0xFFCCCCCC),
        
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
    
    // Optional: Custom dialogs, backgrounds, effects
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

### Step 2: Register (Same as Simple Theme)

Add to `AestheticThemes.allThemes` map in `AestheticThemeData.kt`

### Step 3: Build & Test

Same as simple theme.

---

## Field Reference

### Required Fields

```kotlin
id: String                    // Unique ID (lowercase_underscore)
name: String                  // Display name
description: String           // User-facing description
components: ThemeComponents   // DefaultThemeComponents() or custom
primaryGradient: Brush        // Background gradient
accentColor: Color            // Accent/highlight color
primaryTextColor: Color       // Main text color
secondaryTextColor: Color     // Secondary text color
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

## Color Tips

### Choosing Colors

**primaryGradient:** App background (top → bottom)
- Make sure text is readable!
- Test with both light and dark text

**accentColor:** Buttons, highlights, borders, waveforms, scroll glow
- Should pop against your gradient
- Used for interactive elements

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

### Cards look broken (Pro themes only)
**Fix:** Check your `RecordingItem`/`AttemptItem` implementations
- See `StrangePlanetThemeComponents.kt` for reference
- Make sure all buttons/features are implemented

---

## Examples

### Minimal Theme (3 Required Colors)

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
        primaryTextColor = Color.Black,
        secondaryTextColor = Color.Gray
    )
}
```

### Fully Customized Theme

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
        primaryTextColor = Color(0xFFFFFFFF),
        secondaryTextColor = Color(0xFFB0B0B0),
        
        // Custom emojis
        recordButtonEmoji = "🎸",
        scoreEmojis = mapOf(
            90 to "🏆",
            80 to "🎯",
            70 to "⭐",
            60 to "👏",
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

**Simple Theme:**
- [ ] Create theme file in `ui/theme/`
- [ ] Set all required colors
- [ ] Use `DefaultThemeComponents()`
- [ ] Add to `allThemes` map
- [ ] Build & test

**Pro Theme:**
- [ ] Create theme file with custom `ThemeComponents`
- [ ] Implement `RecordingItem()`
- [ ] Implement `AttemptItem()`
- [ ] Set all colors
- [ ] Set `isPro = true`
- [ ] Add to `allThemes` map
- [ ] Build & test

---

## Reference Themes

**Simple Themes (copy these):**
- `CottageThemeComponents.kt`
- `Y2KThemeComponents.kt`
- `VaporwaveThemeComponents.kt`

**Pro Themes (study these):**
- `StrangePlanetThemeComponents.kt` - Full custom implementation
- `WeirdWorldThemeComponents.kt` - Custom cards with effects
- `EggThemeComponents.kt` - Animated background

---

**That's it! Most themes are just 30 lines of color definitions.** 🎨
