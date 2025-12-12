package com.quokkalabs.reversey.scoring

import androidx.compose.ui.graphics.Color

/**
 * 🎯 SINGLE SOURCE OF TRUTH - DIFFICULTY CONFIGURATION
 * GLUTE Principle: Unified config for all difficulty-related UI & logic
 * Ed's 3-Level System: Easy, Normal, Hard
 *
 * Updated Dec 2025: Added ReverseScoringConfig for phoneme-based scoring
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
     * 🎯 REVERSE SCORING CONFIGS - Difficulty → ReverseScoringConfig
     * Used by ReverseScoringEngine for duration gates and phoneme leniency
     */
    private val reverseScoringConfigs = mapOf(
        DifficultyLevel.EASY to ReverseScoringConfig(
            minDurationRatio = 0.50f,   // 50% of reference duration
            maxDurationRatio = 1.50f,   // 150% of reference duration
            phonemeLeniency = PhonemeLeniency.FUZZY,
            description = "Wide duration window, similar phonemes accepted"
        ),
        DifficultyLevel.NORMAL to ReverseScoringConfig(
            minDurationRatio = 0.66f,   // 66% of reference duration
            maxDurationRatio = 1.33f,   // 133% of reference duration
            phonemeLeniency = PhonemeLeniency.EXACT,
            description = "Moderate duration window, exact phonemes required"
        ),
        DifficultyLevel.HARD to ReverseScoringConfig(
            minDurationRatio = 0.80f,   // 80% of reference duration
            maxDurationRatio = 1.20f,   // 120% of reference duration
            phonemeLeniency = PhonemeLeniency.SEQUENCE,
            description = "Tight duration window, exact phonemes in order"
        )
    )

    /**
     * 🎛️ GET REVERSE SCORING CONFIG - For ReverseScoringEngine
     */
    fun getReverseScoringConfig(difficulty: DifficultyLevel): ReverseScoringConfig {
        return reverseScoringConfigs[difficulty] ?: reverseScoringConfigs[DifficultyLevel.NORMAL]!!
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

/**
 * 🎚️ PHONEME LENIENCY MODES
 * Determines how strictly phonemes must match
 */
enum class PhonemeLeniency {
    FUZZY,      // Similar phonemes count (T≈D, P≈B, etc.) - Easy mode
    EXACT,      // Exact phoneme match required - Normal mode
    SEQUENCE    // Exact match + sequence order matters - Hard mode
}

/**
 * 📊 REVERSE SCORING CONFIG
 * Duration gates and phoneme matching rules per difficulty
 */
data class ReverseScoringConfig(
    val minDurationRatio: Float,    // Minimum attempt/reference ratio (fail below)
    val maxDurationRatio: Float,    // Maximum attempt/reference ratio (penalize above)
    val phonemeLeniency: PhonemeLeniency,
    val description: String
) {
    /**
     * Check if duration ratio passes the gate
     */
    fun isDurationInRange(ratio: Float): Boolean {
        return ratio in minDurationRatio..maxDurationRatio
    }

    /**
     * Calculate duration penalty (0 = no penalty, 1 = max penalty)
     */
    fun durationPenalty(ratio: Float): Float {
        return when {
            ratio < minDurationRatio -> (minDurationRatio - ratio) / minDurationRatio
            ratio > maxDurationRatio -> (ratio - maxDurationRatio) / maxDurationRatio
            else -> 0f
        }.coerceIn(0f, 1f)
    }
}