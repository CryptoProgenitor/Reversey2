# 🎨 ReVerseY Professional Theme Installation Guide

**How to Install a Complete Theme into ReVerseY**

This guide assumes you already have a complete theme file (like `EggThemeComponents.kt`) ready to install. If you're creating a theme from scratch, use the AI template first, then follow this guide to wire it in.

---

## Prerequisites

✅ You have a complete theme file (e.g., `CoolThemeComponents.kt`)  
✅ The theme implements the `ThemeComponents` interface  
✅ All theme assets are self-contained in the file  
✅ Android Studio open with ReVerseY project loaded  

---

## Installation Steps

### Step 1: Place Theme File in Project

**Location:**
```
app/src/main/java/com/example/reversey/ui/theme/
```

**Action:**
1. Copy your theme file (e.g., `CoolThemeComponents.kt`)
2. Paste it into the `ui/theme/` directory
3. Verify package declaration is correct:
   ```kotlin
   package com.quokkalabs.reversey.ui.theme
   ```

**Expected Result:**
Your file should appear alongside other theme files like:
```
ui/theme/
├── AestheticThemeData.kt
├── EggThemeComponents.kt
├── SnowyOwlComponents.kt
└── CoolThemeComponents.kt  ← Your new theme
```

---

### Step 2: Register Theme in AestheticThemeData.kt

**File:** `app/src/main/java/com/example/reversey/ui/theme/AestheticThemeData.kt`

#### 2.1 Find the Theme Definitions Section

Look for where other themes are defined. You'll see entries like:

```kotlin
val Egg = AestheticThemeData(
    id = "egg",
    name = "Egg Theme",
    description = "🥚 CPD's adorable egg-inspired design!",
    components = EggThemeComponents(),
    primaryGradient = Brush.verticalGradient(...),
    cardBorder = Color(0xFF2E2E2E),
    primaryTextColor = Color(0xFF2E2E2E),
    secondaryTextColor = Color(0xFF424242),
    ...
)
```

#### 2.2 Add Your Theme Entry

**Add AFTER existing themes:**

```kotlin
val CoolTheme = AestheticThemeData(
    id = "cool_theme",                    // Unique ID (lowercase, underscores)
    name = "Cool Theme",                  // Display name
    description = "😎 A really cool theme!", // User-facing description
    components = CoolThemeComponents(),   // YOUR theme class instance
    primaryGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1A1A1A),           // Top color
            Color(0xFF2D2D2D),           // Middle color
            Color(0xFF404040)            // Bottom color
        )
    ),
    cardBorder = Color(0xFF00FF00),      // Card outline color
    primaryTextColor = Color(0xFFFFFFFF), // Main text color
    secondaryTextColor = Color(0xFFCCCCCC), // Secondary text color
    accentColor = Color(0xFF00FF00),      // Accent/highlight color
    isDark = true                         // true for dark themes, false for light
)
```

**Color Selection Tips:**
- `primaryGradient`: Background gradient (top → bottom)
- `cardBorder`: Outline around cards
- `primaryTextColor`: Main text (make sure it's readable on your gradient!)
- `secondaryTextColor`: Less important text
- `accentColor`: Highlights, active states
- `isDark`: Set to `true` if background is dark, `false` if light

---

### Step 3: Add Theme to Available Themes List

**In the same file:** `AestheticThemeData.kt`

#### 3.1 Find the Themes List

Look for a list that contains all available themes:

```kotlin
val allThemes = listOf(
    Y2KCyberPop,
    CottageCoreDreams,
    DarkAcademia,
    Steampunk,
    Egg,
    SnowyOwl,
    Scrapbook,
    // ... more themes
)
```

**OR it might be called:**
```kotlin
val availableThemes = listOf(...)
```

**OR:**
```kotlin
object AestheticThemes {
    val all = listOf(...)
}
```

#### 3.2 Add Your Theme to the List

**Add your theme variable to the list:**

```kotlin
val allThemes = listOf(
    Y2KCyberPop,
    CottageCoreDreams,
    DarkAcademia,
    Steampunk,
    Egg,
    SnowyOwl,
    Scrapbook,
    CoolTheme,  // ← Add your theme here
)
```

**Important:** Make sure you're adding the variable name (e.g., `CoolTheme`), not a string!

---

## Verification Steps

### Build Check
1. Build the project: `Build → Make Project`
2. Fix any compilation errors
3. Verify no import errors

### Runtime Check
1. Run the app on device/emulator
2. Navigate to Settings
3. Look for "Theme" or "Aesthetic" option
4. Your theme should appear in the list!
5. Select your theme
6. Verify:
   - Colors look correct
   - Recording cards render properly
   - Attempt cards render properly
   - Text is readable
   - Icons display correctly

---

## Common Issues & Fixes

### Issue 1: Theme Doesn't Appear in Settings

**Possible Causes:**
- ❌ Forgot Step 3 (adding to themes list)
- ❌ Variable name typo in list

**Fix:**
Double-check Step 3. Make sure the variable name matches exactly.

---

### Issue 2: Build Errors - "Unresolved Reference"

**Error Example:**
```
Unresolved reference 'CoolThemeComponents'
```

**Possible Causes:**
- ❌ Theme file not in correct location
- ❌ Package declaration wrong
- ❌ Class name doesn't match

**Fix:**
1. Verify file is in `ui/theme/` directory
2. Check package declaration: `package com.example.reversey.ui.theme`
3. Verify class name matches: `class CoolThemeComponents : ThemeComponents`

---

### Issue 3: App Crashes When Selecting Theme

**Possible Causes:**
- ❌ Theme class doesn't implement `ThemeComponents` interface
- ❌ Missing required functions (RecordingItem, AttemptItem)
- ❌ Runtime error in theme code

**Fix:**
1. Check Logcat for error details
2. Verify theme implements both required functions
3. Test theme functions individually

---

### Issue 4: Colors Look Wrong

**Possible Causes:**
- ❌ Color values incorrect (forgot `0xFF` prefix)
- ❌ Text not readable on gradient background
- ❌ Wrong color format

**Fix:**
1. Colors should be in format: `Color(0xFFRRGGBB)`
   - `0xFF` = fully opaque
   - `RRGGBB` = red, green, blue hex values
2. Test contrast: dark text on light backgrounds, light text on dark backgrounds
3. Use online color picker to get hex values

---

### Issue 5: Custom Features Don't Work

**Examples:**
- Bouncing eggs don't appear
- Custom animations don't play
- Special effects missing

**Possible Causes:**
- ❌ Features not properly self-contained in theme file
- ❌ Missing dependencies/imports
- ❌ Feature relies on external code

**Fix:**
1. Verify all custom features are called within the theme file itself
2. Check that special effects are integrated into the theme's composables
3. Review reference implementation (EggThemeComponents.kt) for pattern

---

## Theme File Requirements Checklist

Before installing, verify your theme file has:

✅ **Package declaration:** `package com.example.reversey.ui.theme`  
✅ **Implements interface:** `class YourTheme : ThemeComponents`  
✅ **RecordingItem function:** Complete implementation  
✅ **AttemptItem function:** Complete implementation  
✅ **All imports:** No missing dependencies  
✅ **Self-contained:** All icons, helpers, dialogs inline  
✅ **No external dependencies:** No references to other theme files  

---

## Theme Customization After Installation

### Changing Colors

**Edit your theme entry in AestheticThemeData.kt:**

```kotlin
val CoolTheme = AestheticThemeData(
    id = "cool_theme",
    name = "Cool Theme",
    description = "😎 A really cool theme!",
    components = CoolThemeComponents(),
    primaryGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF000000),  // ← Change these
            Color(0xFF333333),  // ← Change these
            Color(0xFF666666)   // ← Change these
        )
    ),
    cardBorder = Color(0xFF00FF00),       // ← Change this
    primaryTextColor = Color(0xFFFFFFFF), // ← Change this
    // ... etc
)
```

**No need to modify the theme file itself** - just update the values in AestheticThemeData.kt.

### Changing Name/Description

**Edit in AestheticThemeData.kt:**

```kotlin
val CoolTheme = AestheticThemeData(
    id = "cool_theme",              // Don't change ID after release!
    name = "Super Cool Theme",      // ← Change display name
    description = "🌟 The coolest!", // ← Change description
    // ...
)
```

**Warning:** Don't change the `id` after users have selected the theme, or they'll lose their selection!

---

## Example: Full Installation Walkthrough

### Scenario
You want to install "Retro Arcade Theme" (`RetroArcadeComponents.kt`)

### Step-by-Step

**Step 1: Place File**
```
Copy: RetroArcadeComponents.kt
To: app/src/main/java/com/example/reversey/ui/theme/
```

**Step 2: Register Theme**

Open `AestheticThemeData.kt`, add:

```kotlin
val RetroArcade = AestheticThemeData(
    id = "retro_arcade",
    name = "Retro Arcade",
    description = "🕹️ Classic 8-bit arcade vibes!",
    components = RetroArcadeComponents(),
    primaryGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1A0033),  // Deep purple
            Color(0xFF330066),  // Purple
            Color(0xFF4D0099)   // Bright purple
        )
    ),
    cardBorder = Color(0xFFFF00FF),      // Neon pink border
    primaryTextColor = Color(0xFF00FFFF), // Cyan text
    secondaryTextColor = Color(0xFFFF00FF), // Pink text
    accentColor = Color(0xFFFFFF00),      // Yellow accent
    isDark = true
)
```

**Step 3: Add to List**

In same file, find:

```kotlin
val allThemes = listOf(
    Y2KCyberPop,
    CottageCoreDreams,
    // ... other themes
)
```

Change to:

```kotlin
val allThemes = listOf(
    Y2KCyberPop,
    CottageCoreDreams,
    // ... other themes
    RetroArcade,  // ← Added!
)
```

**Build & Run!**

Navigate to settings → Select "Retro Arcade" → Enjoy! 🕹️

---

## Advanced: Theme with Custom Record Button

Some themes (like Egg) have custom record buttons. These are **already wired automatically** if properly implemented in the theme file.

**No extra installation steps needed!**

The theme file should handle its own record button by:
1. Defining the button composable in the same file
2. The app's recording screen checks if the active theme has a custom button
3. Automatically uses it when that theme is active

**Example from Egg Theme:**
- `EggRecordButton()` defined in `EggThemeComponents.kt`
- No separate wiring needed
- Just works when egg theme active

---

## Advanced: Theme with Background Effects

Some themes (like Egg with bouncing eggs) have special background effects. These should also be **self-contained** in the theme file.

**Pattern:**
```kotlin
class CoolThemeComponents : ThemeComponents {
    
    @Composable
    override fun RecordingItem(...) {
        Box {
            // Background effect (if any)
            if (showBackgroundEffect) {
                CoolBackgroundEffect()
            }
            
            // Main content
            // ...
        }
    }
    
    // Effect defined in same file
    @Composable
    private fun CoolBackgroundEffect() {
        // Implementation
    }
}
```

**No separate installation steps needed** - it's all in the theme file!

---

## Quick Reference

### File Locations
```
Theme File:
  app/src/main/java/com/example/reversey/ui/theme/YourTheme.kt

Registration:
  app/src/main/java/com/example/reversey/ui/theme/AestheticThemeData.kt
```

### Installation Checklist
- [ ] Step 1: Place theme file in `ui/theme/`
- [ ] Step 2: Add theme entry in `AestheticThemeData.kt`
- [ ] Step 3: Add to `allThemes` list
- [ ] Build project
- [ ] Test in app
- [ ] Verify colors, text readability, icons
- [ ] Test all card types (recordings, attempts)
- [ ] Test theme switching

---

## Getting Help

**If you get stuck:**

1. **Check the reference:** Look at `EggThemeComponents.kt` or `SnowyOwlComponents.kt`
2. **Review logcat:** Error messages are your friend
3. **Verify checklist:** Did you complete all steps?
4. **Ask AI assistant:** Provide error messages and file snippets

---

## Summary

**Installing a theme = 3 steps:**

1. **Place** theme file in `ui/theme/`
2. **Register** theme in `AestheticThemeData.kt`  
3. **Add** to available themes list

**That's it!** Everything else (custom buttons, special effects, icons) should be self-contained in the theme file and work automatically.

---

**Happy theming!** 🎨🚀
