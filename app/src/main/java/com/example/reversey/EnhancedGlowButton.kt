package com.example.reversey

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EnhancedGlowButton(
    onClick: () -> Unit,
    theme: AppTheme,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    isPrimary: Boolean = false,
    isDestructive: Boolean = false,
    enabled: Boolean = true,
    label: String? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val buttonColor = when {
        isDestructive -> Color.Red
        isPrimary -> theme.accentColor
        else -> {
            // Make secondary buttons (Share, Parent) more opaque for better visibility
            when (theme.name) {
                "Cottagecore Dreams" -> Color(0xFFE91E63).copy(alpha = 0.8f) // More opaque pink
                "Neon Vaporwave" -> Color(0xFF00BCD4).copy(alpha = 0.7f) // More opaque cyan
                else -> theme.cardBackground
            }
        }
    }

    val shape = getButtonShapeForTheme(theme)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        // Button
        Box(
            modifier = Modifier
                .size(size)
                .then(
                    if (theme.glowIntensity > 0 && enabled) {
                        Modifier.shadow(
                            elevation = (theme.glowIntensity * 8).dp,
                            shape = shape,
                            spotColor = buttonColor.copy(alpha = 0.4f)
                        )
                    } else Modifier
                )
                .background(
                    color = if (enabled) buttonColor else buttonColor.copy(alpha = 0.5f),
                    shape = shape
                )
                .then(
                    if (theme.useGlassmorphism) {
                        Modifier
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.2f),
                                        Color.Transparent
                                    )
                                ),
                                shape = shape
                            )
                            .border(
                                width = 1.dp,
                                color = Color.White.copy(alpha = 0.3f),
                                shape = shape
                            )
                    } else {
                        Modifier.border(
                            width = 1.dp,
                            color = theme.cardBorder.copy(alpha = if (enabled) 1f else 0.5f),
                            shape = shape
                        )
                    }
                )
                .clickable(enabled = enabled) { onClick() },
            contentAlignment = Alignment.Center,
            content = content
        )

        // Label
        label?.let { labelText ->
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = labelText,
                fontSize = getLabelFontSizeForTheme(theme),
                color = if (enabled) getLabelColorForTheme(theme) else getLabelColorForTheme(theme).copy(alpha = 0.5f),
                fontWeight = getLabelFontWeightForTheme(theme),
                textAlign = TextAlign.Center,
                maxLines = 1,
                letterSpacing = if (theme.useWideLetterSpacing) 0.5.sp else 0.sp
            )
        }
    }
}

// Theme-specific styling functions
@Composable
fun getButtonShapeForTheme(theme: AppTheme): Shape {
    return when (theme.name) {
        "Y2K Cyber Pop" -> RoundedCornerShape(12.dp) // Y2K loves rounded rectangles
        "Cottagecore Dreams" -> RoundedCornerShape(16.dp) // Soft, organic shapes
        "Dark Academia Glam" -> RoundedCornerShape(8.dp) // Scholarly, precise
        "Neon Vaporwave" -> RoundedCornerShape(24.dp) // Very rounded, dreamy
        "Jeoseung Shadows" -> RoundedCornerShape(6.dp) // Sharp, dramatic
        else -> CircleShape // Default
    }
}

@Composable
fun getLabelFontSizeForTheme(theme: AppTheme) = when (theme.name) {
    "Y2K Cyber Pop" -> 9.sp // Slightly smaller for better fit
    "Cottagecore Dreams" -> 8.sp // Smaller to prevent truncation
    "Dark Academia Glam" -> 9.sp
    "Neon Vaporwave" -> 9.sp // Smaller for better fit
    "Jeoseung Shadows" -> 9.sp
    else -> 9.sp // Smaller default
}

@Composable
fun getLabelColorForTheme(theme: AppTheme): Color {
    return when {
        // Force white text for cyan/blue accent colors (Vaporwave)
        theme.accentColor == Color(0xFF00BCD4) -> Color.White
        theme.name.contains("Vaporwave", ignoreCase = true) -> Color.White
        theme.name == "Y2K Cyber Pop" -> theme.accentColor
        theme.name == "Cottagecore Dreams" -> Color(0xFF4A4A4A)
        theme.name.contains("Cottagecore", ignoreCase = true) -> Color(0xFF4A4A4A)
        theme.name == "Dark Academia Glam" -> Color(0xFF4A4A4A)
        theme.name == "Jeoseung Shadows" -> Color(0xFF2E2E2E) // Dark text for yellow theme
        theme.name.contains("Jeoseung", ignoreCase = true) -> Color(0xFF2E2E2E) // Fallback
        else -> {
            // If accent color is light (like yellow), use dark text
            if (theme.accentColor == Color(0xFFFFC107) || theme.accentColor == Color.Yellow) {
                Color(0xFF2E2E2E) // Dark text for yellow themes
            } else {
                theme.textPrimary
            }
        }
    }
}

@Composable
fun getLabelFontWeightForTheme(theme: AppTheme) = when (theme.name) {
    "Y2K Cyber Pop" -> FontWeight.Bold
    "Cottagecore Dreams" -> FontWeight.Medium
    "Dark Academia Glam" -> FontWeight.Bold
    "Neon Vaporwave" -> FontWeight.Light
    "Jeoseung Shadows" -> FontWeight.Bold
    else -> FontWeight.Medium
}

// Keep the original GlowButton for backward compatibility
@Composable
fun GlowButton(
    onClick: () -> Unit,
    theme: AppTheme,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    isPrimary: Boolean = false,
    isDestructive: Boolean = false,
    enabled: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    EnhancedGlowButton(
        onClick = onClick,
        theme = theme,
        modifier = modifier,
        size = size,
        isPrimary = isPrimary,
        isDestructive = isDestructive,
        enabled = enabled,
        label = null, // No label for backward compatibility
        content = content
    )
}

@Composable
fun getThemeAwareTextColor(theme: AppTheme, textType: TextType = TextType.PRIMARY): Color {
    return when (textType) {
        TextType.PRIMARY -> when (theme.name) {
            "Cottagecore Dreams" -> Color(0xFFE91E63) // Pink recording names
            "Y2K Cyber Pop" -> Color(0xFFE91E63) // Pink recording names
            "Dark Academia Glam" -> Color(0xFF2E2E2E) // Black recording names
            "Neon Vaporwave" -> Color.White // White recording names
            "Jeoseung Shadows" -> Color(0xFF2E2E2E) // Black recording names
            else -> theme.textPrimary
        }
        TextType.ACCENT -> when (theme.name) {
            "Cottagecore Dreams" -> Color(0xFFE91E63) // Pink player names
            "Y2K Cyber Pop" -> Color(0xFFE91E63) // Pink player names
            "Dark Academia Glam" -> Color(0xFF2E2E2E) // Black player names
            "Neon Vaporwave" -> Color.White // White player names
            "Jeoseung Shadows" -> Color(0xFF2E2E2E) // Black player names
            else -> theme.accentColor
        }
    }
}

enum class TextType {
    PRIMARY,
    ACCENT
}