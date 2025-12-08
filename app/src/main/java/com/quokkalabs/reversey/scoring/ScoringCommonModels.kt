package com.quokkalabs.reversey.scoring

import com.quokkalabs.reversey.data.models.ChallengeType

/**
 * ✅ CANONICAL SCORING CORE (DUAL PIPELINE)
 *
 * This file is the SINGLE SOURCE OF TRUTH for:
 * - DifficultyLevel (Easy / Normal / Hard)
 * - ScoringParameters
 * - AudioProcessingParameters
 * - ContentDetectionParameters
 * - MelodicAnalysisParameters
 * - MusicalSimilarityParameters
 * - ScoreScalingParameters
 * - GarbageDetectionParameters
 * - ScoringResult / SimilarityMetrics
 * - MelodySignature / ContentMetrics
 * - Presets + ScoringPresets
 * - ScoreCalculationBreakdown (NEW - v21.6.0)
 *
 * Used by:
 * - SpeechScoringEngine
 * - SingingScoringEngine
 * - GarbageDetector
 * - AudioProcessor
 * - DifficultyConfig
 * - ScoreAcquisitionDataConcentrator
 * - ScoreExplanationDialog (NEW)
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
//  CORE PARAMETER SETS
// ============================================================

/**
 * Main scoring weights and thresholds.
 * All the fields Gemini listed as missing live here.
 */
data class ScoringParameters(
    // --- Weights (FORWARD) ---
    var pitchWeight: Float = 0.85f,      // pitch similarity weight
    var mfccWeight: Float = 0.15f,       // timbre/content weight

    // --- Weights (REVERSE SINGING) --- Phase 2.4: Direction-sensitive scoring
    // SINGING REVERSE: interval×0.5 + pitch×0.4 + mfcc×0.1
    var reverseIntervalWeight: Float = 0.50f,  // interval accuracy (direction-sensitive)
    var reversePitchWeight: Float = 0.40f,     // pitch similarity (weakly direction-sensitive)
    var reverseMfccWeight: Float = 0.10f,      // voice matching (NOT direction-sensitive)

    // --- Weights (REVERSE SPEECH) --- Phase 2.5: Speech direction-sensitive scoring
    // SPEECH REVERSE: interval×0.85 + pitch×0.1 + mfcc×0.05
    // Speech pitch is USELESS for direction (-0.8% delta), interval is ONLY signal
    var speechReverseIntervalWeight: Float = 0.85f,  // interval accuracy (ONLY reliable signal)
    var speechReversePitchWeight: Float = 0.10f,     // pitch (actively misleading for speech)
    var speechReverseMfccWeight: Float = 0.05f,      // voice matching (minimal)

    // --- Pitch analysis / DTW ---
    var pitchTolerance: Float = 15f,           // cents / semitone-ish tolerance
    var variancePenalty: Float = 0.5f,         // factor for variance-based penalties
    var dtwNormalizationFactor: Float = 1.0f,   // DEPRECATED: cosine distance is self-normalizing

    // --- Silence detection for attempts ---
    var silenceThreshold: Float = 0.01f,

    // --- Score scaling thresholds ---
    var minScoreThreshold: Float = 0.20f,      // raw score below this → 0 after scaling
    var perfectScoreThreshold: Float = 0.80f,  // raw score at/above this → 100 after scaling
    var reverseMinScoreThreshold: Float = minScoreThreshold * 0.8f,     // 20% easier floor
    var reversePerfectScoreThreshold: Float = perfectScoreThreshold * 0.9f, // 10% easier ceiling
    var scoreCurve: Float = 2.0f,             // exponent for curve shaping

    // --- Bonus weights ---
    var consistencyBonus: Float = 0.05f,
    var confidenceBonus: Float = 0.05f,

    // --- Vocal effort similarity (optional, used by some models) ---
    var effortWeight: Float = 0.35f,
    var intensityWeight: Float = 0.45f,
    var rangeWeight: Float = 0.20f,

    // --- Penalties for "wrong but loud" etc ---
    var intensityPenaltyThreshold: Float = 0.15f,
    var intensityPenaltyMultiplier: Float = 0.2f
)

/**
 * Technical audio processing parameters.
 * Used by AudioProcessor, Speech/Singing engines, and MFCC logic.
 */
data class AudioProcessingParameters(
    // Pitch analysis
    var pitchFrameSize: Int = 4096,
    var pitchHopSize: Int = 1024,

    // MFCC analysis
    var mfccFrameSize: Int = 2048,
    var mfccHopSize: Int = 1024,

    // Musical pitch conversion
    var semitonesPerOctave: Float = 12f,
    var pitchReferenceFreq: Float = 440f,      // A4

    // Alignment sensitivity
    var audioAlignmentThreshold: Float = 0.005f
)

/**
 * Content detection & melodic requirement thresholds.
 * These cover all the contentDetection* and *Penalty fields.
 */
data class ContentDetectionParameters(
    // Content recognition thresholds
    var contentDetectionBestThreshold: Float = 0.35f,
    var contentDetectionAvgThreshold: Float = 0.25f,
    // 🚨 NEW: Reverse Handicap (Default 0.0f = Disabled/Speech Safe)
    // Singing Engine will override this in its presets.
    var reverseHandicap: Float = 0.0f,

    // Melodic requirement thresholds
    var highMelodicThreshold: Float = 0.6f,
    var mediumMelodicThreshold: Float = 0.4f,
    var lowMelodicThreshold: Float = 0.3f,
    var insufficientMelodyThreshold: Float = 0.5f,

    // RIGHT CONTENT penalties (light)
    var rightContentFlatPenalty: Float = 0.2f,
    var rightContentDifferentMelodyPenalty: Float = 0.1f,

    // WRONG CONTENT penalties (harsh)
    var wrongContentFlatPenalty: Float = 0.75f,
    var wrongContentInsufficientPenalty: Float = 0.6f,
    var wrongContentStandardPenalty: Float = 0.5f
)

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

/**
 * Melodic analysis configuration: used for variance / monotone checks etc.
 * Contains all melodic* and monotone* fields Gemini listed.
 */
data class MelodicAnalysisParameters(
    // Melodic variation scoring weights
    var melodicRangeWeight: Float = 0.4f,
    var melodicTransitionWeight: Float = 0.35f,
    var melodicVarianceWeight: Float = 0.25f,

    // Melodic analysis thresholds
    var melodicRangeSemitones: Float = 12f,
    var melodicVarianceThreshold: Float = 10f,
    var melodicTransitionThreshold: Float = 0.5f,

    // Pitch similarity calculation
    var pitchDifferenceDecayRate: Float = 5f,
    var silenceToSilenceScore: Float = 0.7f,

    // Monotone detection (used by speech/singing variance penalty)
    var monotoneDetectionThreshold: Float = 2.0f,
    var flatSpeechThreshold: Float = 0.5f,
    var monotonePenalty: Float = 0.3f
)

/**
 * Musical similarity parameters.
 * Covers the interval, phrase, and rhythm thresholds & scores.
 */
data class MusicalSimilarityParameters(
    // Interval analysis
    var sameIntervalThreshold: Float = 0.5f,
    var sameIntervalScore: Float = 1f,
    var closeIntervalThreshold: Float = 1f,
    var closeIntervalScore: Float = 0.8f,
    var similarIntervalThreshold: Float = 2f,
    var similarIntervalScore: Float = 0.5f,
    var differentIntervalScore: Float = 0.1f,

    // Phrase structure analysis
    var emptyPhrasesPenalty: Float = 0.3f,
    var phraseCountDifferenceThreshold: Float = 0.5f,
    var phraseCountPenaltyMultiplier: Float = 0.5f,
    var phraseWeightBalance: Float = 2f,

    // Rhythm analysis
    var emptyRhythmPenalty: Float = 0.2f,
    var rhythmDifferenceSoftening: Float = 0.5f,
    var segmentCountSoftening: Float = 0.5f
)

/**
 * Final score scaling + feedback thresholds.
 * Contains reverse* adjustments and *FeedbackThreshold values.
 */
data class ScoreScalingParameters(
    // Reverse challenge adjustments
    var reverseMinScoreAdjustment: Float = 0.9f,
    var reversePerfectScoreAdjustment: Float = 0.95f,
    var reverseCurveAdjustment: Float = 1.1f,
    var minimumCurveProtection: Float = 0.1f,

    // Feedback thresholds (used in both engines)
    var incredibleFeedbackThreshold: Int = 90,
    var greatJobFeedbackThreshold: Int = 75,
    var goodEffortFeedbackThreshold: Int = 50,
    var additionalFeedbackThreshold: Float = 0.6f,

    // Confidence calculation
    var rmsConfidenceMultiplier: Float = 5f
)

// ============================================================
//  RESULT & METRICS TYPES
// ============================================================

data class ScoringResult(
    val score: Int,              // 0–100
    val rawScore: Float,         // 0–1 (pre-scaled)
    val metrics: SimilarityMetrics,
    val feedback: List<String>,
    val isGarbage: Boolean = false,
    val debugPresets: Presets? = null, // <--- ADDED FOR LOGGING
    // DEBUG FIELDS:
    val debugMinThreshold: Float = 0f,
    val debugPerfectThreshold: Float = 0f,
    val debugNormalizedScore: Float = 0f,
    // NEW: Full calculation breakdown for UI tooltip
    val calculationBreakdown: ScoreCalculationBreakdown? = null,
    // NEW: Vocal analysis for UI display (v21.7.1)
    val vocalAnalysis: VocalAnalysis? = null

)

data class SimilarityMetrics(
    val pitch: Float,
    val mfcc: Float
)

/**
 * Represents melodic "DNA" for advanced analysis.
 * Used by some experimental / musical logic.
 */
data class MelodySignature(
    val pitchContour: List<Float>,
    val intervalSequence: List<Float>,
    val phraseBreaks: List<Int>,
    val rhythmPattern: List<Float>,
    val vocalDensity: Float
) {
    val pitchStdDev: Float
        get() = if (pitchContour.isEmpty()) 0f else {
            val mean = pitchContour.average().toFloat()
            val variance = pitchContour
                .map { (it - mean) * (it - mean) }
                .average()
                .toFloat()
            kotlin.math.sqrt(variance)
        }
}

/**
 * Content similarity metrics, mostly for debugging/logging.
 */
data class ContentMetrics(
    val contourSimilarity: Float,
    val intervalSimilarity: Float,
    val phraseSimilarity: Float,
    val rhythmSimilarity: Float,
    val overallContentScore: Float
)

// ============================================================
//  SCORE CALCULATION BREAKDOWN (NEW - v21.6.0)
// ============================================================

/**
 * 🧮 SCORE CALCULATION BREAKDOWN
 *
 * Captures the complete calculation journey for the scorecard tooltip.
 * Supports all 4 scoring paths:
 *   - Speech Forward
 *   - Speech Reverse
 *   - Singing Forward
 *   - Singing Reverse
 *
 * Usage: Built in scoring engines, passed through ScoringResult → PlayerAttempt → UI
 */
data class ScoreCalculationBreakdown(

    // ═══════════════════════════════════════════════════════════════════
    // METADATA - Which path was taken?
    // ═══════════════════════════════════════════════════════════════════

    val scoringEngineType: ScoringEngineType,   // SPEECH_ENGINE or SINGING_ENGINE
    val challengeType: ChallengeType,            // FORWARD or REVERSE
    val difficultyLevel: DifficultyLevel,        // EASY, NORMAL, HARD

    // ═══════════════════════════════════════════════════════════════════
    // STEP 1: INPUT SIMILARITIES (from feature extraction)
    // ═══════════════════════════════════════════════════════════════════

    val pitchSimilarity: Float,     // 0-1, from DTW alignment
    val mfccSimilarity: Float,      // 0-1, from cosine distance

    // ═══════════════════════════════════════════════════════════════════
    // STEP 2: WEIGHTED COMBINATION
    // ═══════════════════════════════════════════════════════════════════

    val pitchWeight: Float,         // 0.85 (speech) or 0.90 (singing)
    val mfccWeight: Float,          // 0.15 (speech) or 0.10 (singing)
    val baseWeightedScore: Float,   // pitch * pitchWeight + mfcc * mfccWeight

    // ═══════════════════════════════════════════════════════════════════
    // STEP 3: MUSICAL BONUSES (Singing only - null for Speech)
    // ═══════════════════════════════════════════════════════════════════

    val musicalBonuses: MusicalBonusBreakdown? = null,

    // ═══════════════════════════════════════════════════════════════════
    // STEP 4: CONTENT DETECTION PENALTY
    // ═══════════════════════════════════════════════════════════════════

    val contentThresholdBase: Float,      // Original threshold from preset
    val reverseHandicap: Float,           // 0.0 for forward, 0.15 for normal reverse, etc.
    val contentThresholdEffective: Float, // contentThresholdBase - reverseHandicap
    val contentPenaltyTriggered: Boolean, // Did mfcc < threshold?
    val contentPenaltyMultiplier: Float,  // e.g., 0.20 ("The Crusher")
    val scoreAfterContentPenalty: Float,

    // ═══════════════════════════════════════════════════════════════════
    // STEP 5: VARIANCE & PERFORMANCE ADJUSTMENTS
    // ═══════════════════════════════════════════════════════════════════

    val variancePenaltyTriggered: Boolean,
    val variancePenaltyMultiplier: Float,     // 1.0 if no penalty, 0.4-0.7 if triggered

    val consistencyBonus: Float,              // (1 - |pitch - mfcc|) * 0.05
    val confidenceBonus: Float,               // min(1, RMS * multiplier) * 0.05
    val maxConsistencyBonus: Float,           // from parameters.consistencyBonus (typically 0.05)
    val maxConfidenceBonus: Float,            // from parameters.confidenceBonus (typically 0.05)
    val totalPerformanceMultiplier: Float,    // (1 + consistency + confidence)

    val hummingDetected: Boolean,
    val hummingPenaltyMultiplier: Float,      // 1.0 (none), 0.8 (singing), 0.7 (speech)

    val rawScoreFinal: Float,                 // Score going into normalization

    // ═══════════════════════════════════════════════════════════════════
    // STEP 6: THRESHOLD NORMALIZATION
    // ═══════════════════════════════════════════════════════════════════

    val minThresholdBase: Float,              // From preset
    val perfectThresholdBase: Float,          // From preset
    val reverseMinMultiplier: Float,          // 1.0 for forward, 0.8 for reverse
    val reversePerfectMultiplier: Float,      // 1.0 for forward, 0.9 for reverse
    val minThresholdEffective: Float,         // base * multiplier
    val perfectThresholdEffective: Float,     // base * multiplier
    val normalizedScore: Float,               // (raw - min) / (perfect - min)

    // ═══════════════════════════════════════════════════════════════════
    // STEP 7: CURVE APPLICATION
    // ═══════════════════════════════════════════════════════════════════

    val scoreCurve: Float,                    // 3.2 (speech) or 1.0 (singing linear)
    val curvedScore: Float,                   // normalized ^ (1/scoreCurve)

    // ═══════════════════════════════════════════════════════════════════
    // STEP 8: FINAL SCORE
    // ═══════════════════════════════════════════════════════════════════

    val finalScore: Int                       // (curvedScore * 100).roundToInt().coerceIn(0, 100)
)

/**
 * 🎵 MUSICAL BONUS BREAKDOWN (Singing Engine only)
 *
 * Captures the three musical analysis bonuses:
 * - Complexity: Pitch range and transitions
 * - Interval Accuracy: Note-to-note jump matching
 * - Harmonic Richness: Spectral spread from MFCC
 */
data class MusicalBonusBreakdown(
    val complexityBonus: Float,          // 0-1 raw value
    val complexityWeight: Float,         // 0.10
    val complexityContribution: Float,   // bonus * weight

    val intervalAccuracy: Float,         // 0-1 raw value
    val intervalWeight: Float,           // 0.15
    val intervalContribution: Float,     // accuracy * weight

    val harmonicRichness: Float,         // 0-1 raw value
    val harmonicWeight: Float,           // 0.05
    val harmonicContribution: Float,     // richness * weight

    val totalMusicalBonus: Float         // Sum of all contributions (max 0.30)
)

// ============================================================
//  PRESET BUNDLE
// ============================================================

/**
 * Full preset bundle used by SpeechScoringModels & SingingScoringModels.
 */
data class Presets(
    val difficulty: DifficultyLevel = DifficultyLevel.NORMAL,
    val scoring: ScoringParameters = ScoringParameters(),
    val content: ContentDetectionParameters = ContentDetectionParameters(),
    val melodic: MelodicAnalysisParameters = MelodicAnalysisParameters(),
    val musical: MusicalSimilarityParameters = MusicalSimilarityParameters(),
    val audio: AudioProcessingParameters = AudioProcessingParameters(),
    val scaling: ScoreScalingParameters = ScoreScalingParameters(),
    val garbage: GarbageDetectionParameters = GarbageDetectionParameters()
)

// ============================================================
//  BASELINE PRESETS (used by DifficultyConfig, generic modes)
// ============================================================

object ScoringPresets {

    // Easy Mode – forgiving
    fun easyMode(): Presets {
        return Presets(
            difficulty = DifficultyLevel.EASY,
            scoring = ScoringParameters(
                pitchWeight = 0.75f,
                mfccWeight = 0.25f,
                pitchTolerance = 35f,
                minScoreThreshold = 0.12f,
                perfectScoreThreshold = 0.8f,
                scoreCurve = 2.5f
            ),
            content = ContentDetectionParameters(
                contentDetectionBestThreshold = 0.25f,
                contentDetectionAvgThreshold = 0.15f,
                rightContentFlatPenalty = 0.1f,
                rightContentDifferentMelodyPenalty = 0.05f,
                wrongContentStandardPenalty = 0.25f
            ),
            melodic = MelodicAnalysisParameters(
                monotoneDetectionThreshold = 1.5f,
                flatSpeechThreshold = 0.8f,
                monotonePenalty = 0.2f
            ),
            musical = MusicalSimilarityParameters(),
            audio = AudioProcessingParameters(),
            scaling = ScoreScalingParameters(
                incredibleFeedbackThreshold = 80,
                greatJobFeedbackThreshold = 60,
                goodEffortFeedbackThreshold = 40
            ),
            garbage = GarbageDetectionParameters(
                enableGarbageDetection = true,
                mfccVarianceThreshold = 0.2f,
                pitchMonotoneThreshold = 8f,
                pitchOscillationRate = 0.6f,
                spectralEntropyThreshold = 0.4f,
                zcrMinThreshold = 0.015f,
                zcrMaxThreshold = 0.25f,
                silenceRatioMin = 0.08f,
                garbageScoreMax = 20
            )
        )
    }

    // Normal Mode – balanced
    fun normalMode(): Presets {
        return Presets(
            difficulty = DifficultyLevel.NORMAL,
            scoring = ScoringParameters(
                pitchWeight = 0.85f,
                mfccWeight = 0.15f,
                pitchTolerance = 25f,
                minScoreThreshold = 0.18f,
                perfectScoreThreshold = 0.9f,
                scoreCurve = 2.5f
            ),
            content = ContentDetectionParameters(
                contentDetectionBestThreshold = 0.4f,
                contentDetectionAvgThreshold = 0.3f,
                rightContentFlatPenalty = 0.15f,
                rightContentDifferentMelodyPenalty = 0.08f,
                wrongContentStandardPenalty = 0.6f
            ),
            melodic = MelodicAnalysisParameters(),
            musical = MusicalSimilarityParameters(),
            audio = AudioProcessingParameters(),
            scaling = ScoreScalingParameters(),
            garbage = GarbageDetectionParameters(
                enableGarbageDetection = true,
                mfccVarianceThreshold = 0.4f,
                pitchMonotoneThreshold = 1f,
                pitchOscillationRate = 0.5f,
                spectralEntropyThreshold = 0.6f,
                zcrMinThreshold = 0.02f,
                zcrMaxThreshold = 0.2f,
                silenceRatioMin = 0.1f,
                garbageScoreMax = 10
            )
        )
    }

    // Hard Mode – strict
    fun hardMode(): Presets {
        return Presets(
            difficulty = DifficultyLevel.HARD,
            scoring = ScoringParameters(
                pitchWeight = 0.9f,
                mfccWeight = 0.1f,
                pitchTolerance = 15f,
                minScoreThreshold = 0.25f,
                perfectScoreThreshold = 0.85f,
                scoreCurve = 2.5f
            ),
            content = ContentDetectionParameters(
                contentDetectionBestThreshold = 0.45f,
                contentDetectionAvgThreshold = 0.30f,
                rightContentFlatPenalty = 0.25f,
                rightContentDifferentMelodyPenalty = 0.15f,
                wrongContentStandardPenalty = 0.55f
            ),
            melodic = MelodicAnalysisParameters(
                monotoneDetectionThreshold = 2.5f,
                flatSpeechThreshold = 0.6f,
                monotonePenalty = 0.4f
            ),
            musical = MusicalSimilarityParameters(),
            audio = AudioProcessingParameters(),
            scaling = ScoreScalingParameters(
                incredibleFeedbackThreshold = 90,
                greatJobFeedbackThreshold = 75,
                goodEffortFeedbackThreshold = 60
            ),
            garbage = GarbageDetectionParameters(
                enableGarbageDetection = true,
                mfccVarianceThreshold = 0.4f,
                pitchMonotoneThreshold = 12f,
                pitchOscillationRate = 0.45f,
                spectralEntropyThreshold = 0.6f,
                zcrMinThreshold = 0.025f,
                zcrMaxThreshold = 0.18f,
                silenceRatioMin = 0.12f,
                garbageScoreMax = 5
            )
        )
    }

    // Content-focused mode – rewards correct words
    fun contentFocusedMode(): Presets {
        return Presets(
            difficulty = DifficultyLevel.NORMAL,
            scoring = ScoringParameters(
                pitchWeight = 0.95f,
                mfccWeight = 0.05f,
                pitchTolerance = 25f
            ),
            content = ContentDetectionParameters(
                contentDetectionBestThreshold = 0.2f,
                contentDetectionAvgThreshold = 0.1f,
                rightContentFlatPenalty = 0.05f,
                rightContentDifferentMelodyPenalty = 0.02f,
                wrongContentStandardPenalty = 0.8f
            )
        )
    }

    // Melody-focused mode – rewards exact melody
    fun melodyFocusedMode(): Presets {
        return Presets(
            difficulty = DifficultyLevel.NORMAL,
            scoring = ScoringParameters(
                pitchWeight = 0.95f,
                mfccWeight = 0.05f,
                pitchTolerance = 5f
            ),
            content = ContentDetectionParameters(
                contentDetectionBestThreshold = 0.6f,
                contentDetectionAvgThreshold = 0.4f,
                rightContentFlatPenalty = 0.4f,
                rightContentDifferentMelodyPenalty = 0.3f,
                wrongContentStandardPenalty = 0.4f
            ),
            melodic = MelodicAnalysisParameters(
                melodicRangeWeight = 0.5f,
                melodicTransitionWeight = 0.4f,
                melodicVarianceWeight = 0.1f
            )
        )
    }

    fun getAllDifficultyPresets(): List<Pair<DifficultyLevel, () -> Presets>> {
        return listOf(
            DifficultyLevel.EASY to ::easyMode,
            DifficultyLevel.NORMAL to ::normalMode,
            DifficultyLevel.HARD to ::hardMode
        )
    }
}