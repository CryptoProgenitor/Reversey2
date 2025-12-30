package com.quokkalabs.reversey.scoring

import androidx.compose.runtime.Immutable
import com.quokkalabs.reversey.data.models.ChallengeType
import com.quokkalabs.reversey.scoring.WordPhonemes

/**
 * ✅ CANONICAL SCORING CORE
 *
 * This file is the SINGLE SOURCE OF TRUTH for:
 * - DifficultyLevel (Easy / Normal / Hard)
 * - VocalMode / VocalFeatures / VocalAnalysis
 * - GarbageDetectionParameters
 * - PerformanceInsights / DebuggingData (legacy, kept for backup compatibility)
 *
 * Used by:
 * - ReverseScoringEngine
 * - GarbageDetector
 * - AudioProcessor
 * - DifficultyConfig
 * - BackupManager
 */

// ============================================================
//  DIFFICULTY SYSTEM (3-LEVEL ONLY)
// ============================================================

enum class DifficultyLevel(
    val displayName: String,
    val emoji: String,
    val description: String
) {
    EASY("Easy", "😊", "Very forgiving - great for beginners"),
    NORMAL("Normal", "🎵", "Balanced scoring - the default experience"),
    HARD("Hard", "🔥", "Challenging - for experienced users")
}

// ============================================================
//  VOCAL MODE TYPES (Canonical - relocated from VocalModeDetector.kt)
// ============================================================

/**
 * Classification of vocal content type.
 * UNKNOWN used as safe default when analysis unavailable.
 */
enum class VocalMode { SPEECH, SINGING, UNKNOWN }

/**
 * Low-level audio features extracted during vocal analysis.
 * Retained for backward compatibility with cached data.
 */
data class VocalFeatures(
    val pitchStability: Float,
    val pitchContour: Float,
    val mfccSpread: Float,
    val voicedRatio: Float
)

/**
 * Result of vocal mode classification.
 * Stored in Recording and PlayerAttempt for historical reference.
 */
data class VocalAnalysis(
    val mode: VocalMode,
    val confidence: Float,
    val features: VocalFeatures
)


// ============================================================
//  LEGACY TYPES (relocated from Scoreacquisitiondataconcentrator.kt)
//  Kept for backward compatibility with stored PlayerAttempts
// ============================================================

/**
 * Performance insights from scoring analysis.
 * Now deprecated - kept for backup/restore compatibility.
 */
data class PerformanceInsights(
    val feedback: List<String> = emptyList()
)

/**
 * Debug data from scoring analysis.
 * Now deprecated - kept for backup/restore compatibility.
 */
data class DebuggingData(
    val debugInfo: String = ""
)

// ============================================================
//  CORE PARAMETER SETS
// ============================================================


/**
 * Garbage detection parameters.
 * Supports both the legacy noise thresholds AND the newer multi-filter system.
 */
data class GarbageDetectionParameters(

    // Master toggle
    var enableGarbageDetection: Boolean = true,

    // Legacy garbage config (you had these before)
    var noiseThreshold: Float = 0.02f,
    var garbageEnergyRatioThreshold: Float = 0.20f,
    var penaltyMultiplier: Float = 0.8f,

    // FILTER 1: MFCC variance
    var mfccVarianceThreshold: Float = 0.3f,

    // FILTER 2: Pitch contour (monotone / oscillation)
    var pitchMonotoneThreshold: Float = 10f,
    var pitchOscillationRate: Float = 0.5f,

    // FILTER 3: Spectral entropy
    var spectralEntropyThreshold: Float = 0.5f,

    // FILTER 4: Zero-crossing rate
    var zcrMinThreshold: Float = 0.02f,
    var zcrMaxThreshold: Float = 0.2f,

    // FILTER 5: Silence ratio
    var silenceRatioMin: Float = 0.1f,
    var silenceRatioMax: Float = 0.8f,
    var silenceThreshold: Float = 0.01f,

    // Penalty system
    var garbageScorePenalty: Float = 0f,
    var garbageScoreMax: Int = 10
)

// ============================================================
//  SCORE BREAKDOWN (Phoneme-based scoring audit trail)
// ============================================================

/**
 * Full scoring breakdown for audit/display.
 * Populated from PhonemeScoreResult after scoring.
 * Stored in PlayerAttempt for scorecard display and backup.
 *
 * Self-contained audit trail - includes all scoring data
 * even if some fields are duplicated at PlayerAttempt level.
 *
 * @since v2.3 - Replaces legacy ScoreCalculationBreakdown
 */
@Immutable
data class ScoreBreakdown(
    // Core result
    val score: Int,
    val phonemeOverlap: Float,
    val matchedCount: Int,
    val totalCount: Int,

    // Duration analysis
    val durationRatio: Float,
    val durationScore: Float,
    val durationInRange: Boolean,

    // Configuration used
    val difficulty: DifficultyLevel,
    val model: String,
    val phonemeLeniency: String,

    // Auto-decision flags
    val shouldAutoAccept: Boolean,
    val shouldAutoReject: Boolean,

    // Phoneme data (duplicated from PlayerAttempt for self-contained audit)
    val targetPhonemes: List<String>,
    val attemptPhonemes: List<String>,
    val phonemeMatches: List<Boolean>,
    val targetWordPhonemes: List<WordPhonemes>,
    val attemptWordPhonemes: List<WordPhonemes>
)