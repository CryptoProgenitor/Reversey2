package com.example.reversey.ui.menu

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Soft Gradient Menu Color Palette
 * Purple-to-pink gradient with glassmorphism cards
 */
object StaticMenuColors {
    // Gradient Background
    val backgroundGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF667EEA),  // Purple
            Color(0xFF764BA2)   // Pink-purple
        )
    )

    // Alternative vertical gradient for variety
    val backgroundGradientVertical = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF667EEA),
            Color(0xFF764BA2)
        )
    )

    // Card Backgrounds
    val cardSelected = Color.White.copy(alpha = 0.95f)
    val cardUnselected = Color.White.copy(alpha = 0.15f)
    val cardHover = Color.White.copy(alpha = 0.25f)

    // Header (Glassmorphism)
    val headerBackground = Color.White.copy(alpha = 0.20f)
    val headerBorder = Color.White.copy(alpha = 0.30f)

    // Text Colors
    val textOnGradient = Color.White
    val textOnCard = Color(0xFF5B21B6)         // Purple-700
    val textSecondary = Color.White.copy(alpha = 0.85f)
    val textMuted = Color.White.copy(alpha = 0.60f)

    // Interactive States
    val selectedAccent = Color(0xFF7C3AED)     // Violet-600
    val checkmark = Color(0xFF6D28D9)          // Violet-700

    // Danger Zone
    val deleteBackground = Color(0xFFEF4444).copy(alpha = 0.20f)
    val deleteBorder = Color(0xFFEF4444).copy(alpha = 0.30f)
    val deleteText = Color(0xFFFECACA)         // Red-200

    // Dividers
    val divider = Color.White.copy(alpha = 0.20f)

    // Shadows (for elevation effect descriptions)
    val shadowColor = Color.Black.copy(alpha = 0.20f)

    // Settings Screen Specific
    val settingsCardBackground = Color.White.copy(alpha = 0.90f)
    val settingsInputBackground = Color.White.copy(alpha = 0.50f)
    val toggleActive = Color(0xFF8B5CF6)       // Violet-500
    val toggleInactive = Color.White.copy(alpha = 0.40f)
}
