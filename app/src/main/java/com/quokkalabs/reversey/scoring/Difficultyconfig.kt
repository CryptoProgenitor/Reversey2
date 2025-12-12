package com.quokkalabs.reversey.scoring

import androidx.compose.ui.graphics.Color

/**
 * 🎯 SINGLE SOURCE OF TRUTH - DIFFICULTY CONFIGURATION
 * GLUTE Principle: Unified config for all difficulty-related UI & logic
 * Ed's 3-Level System: Easy, Normal, Hard
 */
object DifficultyConfig {

    /**
     * 🎨 DIFFICULTY COLORS - Used across all UI components
     */
    val colors = mapOf(
        DifficultyLevel.EASY to Color(0xFF4CAF50),    // Green - Forgiving
        DifficultyLevel.NORMAL to Color(0xFF2196F3),  // Blue - Balanced
        DifficultyLevel.HARD to Color(0xFFFF9800)     // Orange - Strict
    )

    /**
     * 🎭 DIFFICULTY EMOJIS - Ed's custom emoji set
     */
    val emojis = mapOf(
        DifficultyLevel.EASY to "💚",      // Green heart - Forgiving
        DifficultyLevel.NORMAL to "💎",    // Blue gem - Balanced
        DifficultyLevel.HARD to "🔥"       // Orange fire - Strict
    )

    /**
     * 📝 DIFFICULTY DESCRIPTIONS - Used in UI tooltips
     */
    val descriptions = mapOf(
        DifficultyLevel.EASY to "Forgiving",
        DifficultyLevel.NORMAL to "Balanced",
        DifficultyLevel.HARD to "Strict"
    )

    /**
     * 🎮 SUPPORTED LEVELS - Only these 3 levels for Ed's app
     */
    val supportedLevels = listOf(
        DifficultyLevel.EASY,
        DifficultyLevel.NORMAL,
        DifficultyLevel.HARD
    )

    /**
     * 🎛️ PRESET MAPPING - Difficulty → Speech preset
     *
     * 🎯 REFACTOR: Dual pipeline removed (Dec 2025)
     * Now returns only speech preset. Primary scoring uses ReverseScoringEngine.
     */
    fun getSpeechPresetForDifficulty(difficulty: DifficultyLevel): Presets {
        return when (difficulty) {
            DifficultyLevel.EASY -> SpeechScoringModels.easyModeSpeech()
            DifficultyLevel.NORMAL -> SpeechScoringModels.normalModeSpeech()
            DifficultyLevel.HARD -> SpeechScoringModels.hardModeSpeech()
        }
    }


    /**
     * 🎨 GET COLOR - Safe color retrieval with fallback
     */
    fun getColorForDifficulty(difficulty: DifficultyLevel): Color {
        return colors[difficulty] ?: Color(0xFF2196F3) // Fallback to blue
    }

    /**
     * 🎭 GET EMOJI - Safe emoji retrieval with fallback
     */
    fun getEmojiForDifficulty(difficulty: DifficultyLevel): String {
        return emojis[difficulty] ?: "💎" // Fallback to blue gem
    }

    /**
     * 📝 GET DESCRIPTION - Safe description retrieval with fallback
     */
    fun getDescriptionForDifficulty(difficulty: DifficultyLevel): String {
        return descriptions[difficulty] ?: "Unknown"
    }

    /**
     * ✅ IS SUPPORTED - Check if difficulty level is supported
     */
    fun isSupported(difficulty: DifficultyLevel): Boolean {
        return difficulty in supportedLevels
    }
}