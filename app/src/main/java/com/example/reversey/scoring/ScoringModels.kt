package com.example.reversey.scoring

// ===== PRESERVE EXISTING STRUCTURE EXACTLY =====
data class ScoringParameters(
    // --- PITCH-FOCUSED WEIGHTS (since pitch is more important than timbre) ---
    var pitchWeight: Float = 0.85f,    // Content similarity - DOMINANT
    var mfccWeight: Float = 0.15f,     // Voice similarity - MINIMAL
    // Pitch analysis parameters - MODERATELY STRICT
    var pitchTolerance: Float = 15f,              // was 10f - wider pitch tolerance
    var variancePenalty: Float = 0.5f,       // unchanged
    var dtwNormalizationFactor: Float = 35f,     // was 25f - more forgiving DTW
    // Detection
    var silenceThreshold: Float = 0.01f,     // unchanged
    // Score scaling - MORE FORGIVING
    var minScoreThreshold: Float = 0.20f,    // was 0.35f - much lower minimum
    var perfectScoreThreshold: Float = 0.80f, // was 0.85f - slightly easier to get perfect
    var scoreCurve: Float = 2.0f,                 // was 1.8f - MORE generous scaling
    // Bonuses (keep small)
    var consistencyBonus: Float = 0.05f,
    var confidenceBonus: Float = 0.05f,
    // Vocal effort similarity weights
    var effortWeight: Float = 0.35f,           // Vocal density similarity was 0.4
    var intensityWeight: Float = 0.45f,        // Pitch variation similarity was 0.4
    var rangeWeight: Float = 0.2f,            // Pitch range similarity
    // Intensity penalty for wrong content
    var intensityPenaltyThreshold: Float = 0.15f,  // Below this triggers penalty
    var intensityPenaltyMultiplier: Float = 0.2f  // Harsh penalty factor was 0.3
)

// ===== NEW EXTRACTED PARAMETER CLASSES =====

/**
 * Audio processing technical parameters
 * Controls how we analyze the raw audio data
 */
data class AudioProcessingParameters(
    // Pitch analysis frame settings
    var pitchFrameSize: Int = 4096,              // How much audio to analyze at once for pitch
    var pitchHopSize: Int = 1024,                // How often to check pitch (audio samples)

    // MFCC (voice characteristics) analysis settings
    var mfccFrameSize: Int = 2048,               // How much audio to analyze for voice timbre
    var mfccHopSize: Int = 1024,                 // How often to check voice characteristics

    // Musical conversion constants
    var semitonesPerOctave: Float = 12f,         // Musical constant: 12 semitones = 1 octave
    var pitchReferenceFreq: Float = 440f,        // Musical reference: A4 = 440Hz

    // Audio alignment settings
    var audioAlignmentThreshold: Float = 0.005f  // Volume level that counts as "start of audio"
)

/**
 * Content detection parameters (v8.0.0 breakthrough algorithm!)
 * Controls the smart system that knows if you sang the right words
 */
data class ContentDetectionParameters(
    // Content recognition thresholds - when to consider content "correct"
    var contentDetectionBestThreshold: Float = 0.35f,   // If any similarity > 35% = right content likely
    var contentDetectionAvgThreshold: Float = 0.25f,    // If average similarity > 25% = right content likely

    // Melodic requirement thresholds - how melodic the singing needs to be
    var highMelodicThreshold: Float = 0.6f,              // 60%+ variation = very melodic singing
    var mediumMelodicThreshold: Float = 0.4f,            // 40%+ variation = somewhat melodic
    var lowMelodicThreshold: Float = 0.3f,               // Below 30% variation = flat/monotone
    var insufficientMelodyThreshold: Float = 0.5f,       // Below 50% = needs more melodic effort

    // Smart penalty system - different punishments based on what went wrong

    // RIGHT CONTENT penalties (light - you got the words right!)
    var rightContentFlatPenalty: Float = 0.2f,           // 20% penalty: correct words but too flat
    var rightContentDifferentMelodyPenalty: Float = 0.1f, // 10% penalty: correct words, different tune

    // WRONG CONTENT penalties (harsh - you missed the point!)
    var wrongContentFlatPenalty: Float = 0.75f,          // 75% penalty: wrong words AND flat delivery
    var wrongContentInsufficientPenalty: Float = 0.6f,   // 60% penalty: wrong words, insufficient melody
    var wrongContentStandardPenalty: Float = 0.5f        // 50% penalty: wrong words, decent melody
)

/**
 * Melodic analysis parameters
 * Controls how we understand the musical aspects of singing
 */
data class MelodicAnalysisParameters(
    // Melodic variation scoring weights
    var melodicRangeWeight: Float = 0.4f,         // 40%: how wide your pitch range is
    var melodicTransitionWeight: Float = 0.35f,   // 35%: how often your pitch changes
    var melodicVarianceWeight: Float = 0.25f,     // 25%: how much overall variation

    // Melodic analysis thresholds
    var melodicRangeSemitones: Float = 12f,       // 12 semitones = full octave range
    var melodicVarianceThreshold: Float = 10f,    // Variance level that counts as "melodic"
    var melodicTransitionThreshold: Float = 0.5f, // Minimum pitch change to count as "movement"

    // Pitch similarity calculation
    var pitchDifferenceDecayRate: Float = 5f,     // How quickly we forgive pitch differences
    var silenceToSilenceScore: Float = 0.7f,      // Score when both recordings are quiet

    // Monotone detection (legacy system, still used)
    var monotoneDetectionThreshold: Float = 2.0f, // Original variance that triggers monotone check
    var flatSpeechThreshold: Float = 0.5f,        // Attempt variance that counts as flat speech
    var monotonePenalty: Float = 0.3f             // Penalty multiplier for monotone attempts
)

/**
 * Musical similarity comparison parameters
 * Controls how we compare musical elements between original and attempt
 */
data class MusicalSimilarityParameters(
    // Musical interval analysis - how we score note-to-note relationships
    var sameIntervalThreshold: Float = 0.5f,      // Within 0.5 semitones = same interval
    var sameIntervalScore: Float = 1f,            // Perfect score for matching intervals
    var closeIntervalThreshold: Float = 1f,       // Within 1 semitone = close interval
    var closeIntervalScore: Float = 0.8f,         // Good score for close intervals
    var similarIntervalThreshold: Float = 2f,     // Within 2 semitones = similar interval
    var similarIntervalScore: Float = 0.5f,       // OK score for similar intervals
    var differentIntervalScore: Float = 0.1f,     // Low score for very different intervals

    // Phrase structure analysis - how we compare melodic "sentences"
    var emptyPhrasesPenalty: Float = 0.3f,        // Penalty when phrase structures don't match
    var phraseCountDifferenceThreshold: Float = 0.5f, // When phrase counts are too different
    var phraseCountPenaltyMultiplier: Float = 0.5f,   // How harsh to be about phrase differences
    var phraseWeightBalance: Float = 2f,          // Balance between count and position similarity

    // Rhythm analysis - how we compare timing patterns
    var emptyRhythmPenalty: Float = 0.2f,         // Penalty when rhythm comparison fails
    var rhythmDifferenceSoftening: Float = 0.5f, // How forgiving about rhythm differences (power factor)
    var segmentCountSoftening: Float = 0.5f       // How forgiving about different segment counts
)

/**
 * Score scaling and feedback parameters
 * Controls final score calculation and user feedback
 */
data class ScoreScalingParameters(
    // Challenge type adjustments - different difficulty for reverse vs forward
    var reverseMinScoreAdjustment: Float = 0.9f,     // Make reverse challenges 10% easier (minimum)
    var reversePerfectScoreAdjustment: Float = 0.95f, // Make reverse challenges 5% easier (maximum)
    var reverseCurveAdjustment: Float = 1.1f,        // Make reverse challenges 10% more generous
    var minimumCurveProtection: Float = 0.1f,        // Never let scoring curve go below 10%

    // User feedback thresholds - what scores trigger encouraging messages
    var incredibleFeedbackThreshold: Int = 90,      // 90%+ = "Incredible! You're a reverse singing master!"
    var greatJobFeedbackThreshold: Int = 75,        // 75%+ = "Great job! You're really getting the hang of this!"
    var goodEffortFeedbackThreshold: Int = 50,      // 50%+ = "Good effort! Keep practicing!"
    var additionalFeedbackThreshold: Float = 0.6f,  // Below 60% = give extra helpful tips

    // Confidence calculation - how volume affects scoring
    var rmsConfidenceMultiplier: Float = 5f         // Multiplier for volume-based confidence bonus
)

// ===== EXISTING DATA STRUCTURES (UNCHANGED) =====

data class ScoringResult(
    val score: Int,           // 0-100
    val rawScore: Float,      // 0-1
    val metrics: SimilarityMetrics,
    val feedback: List<String>
)

data class SimilarityMetrics(
    val pitch: Float,
    val mfcc: Float
)

/**
 * Represents the melodic "DNA" of an audio recording
 */
data class MelodySignature(
    val pitchContour: List<Float>,       // Relative pitch changes (semitones)
    val intervalSequence: List<Float>,   // Musical intervals between notes
    val phraseBreaks: List<Int>,         // Indices where melodic phrases end
    val rhythmPattern: List<Float>,      // Duration ratios between vocal segments
    val vocalDensity: Float             // Percentage of audio that contains voice
)

/**
 * Content similarity metrics for debugging
 */
data class ContentMetrics(
    val contourSimilarity: Float,        // How similar are the melody shapes
    val intervalSimilarity: Float,       // How similar are the musical intervals
    val phraseSimilarity: Float,         // How similar are the phrase structures
    val rhythmSimilarity: Float,         // How similar are the timing patterns
    val overallContentScore: Float       // Combined content similarity score
)

// ===== NEW DIFFICULTY LEVEL ENUM =====

enum class DifficultyLevel(val displayName: String, val emoji: String, val description: String) {
    EASY("Easy", "😊", "Very forgiving - great for beginners"),
    NORMAL("Normal", "🎵", "Balanced scoring - the default experience"),
    HARD("Hard", "🔥", "Challenging - for experienced users"),
    EXPERT("Expert", "💎", "Very strict - only for advanced singers"),
    MASTER("Master", "🏆", "Perfection required - for the elite")
}

// ===== ENHANCED PRESET CONFIGURATIONS =====

object ScoringPresets {

    // Easy Mode - Forgiving scoring for beginners
    fun easyMode(): Presets {
        return Presets(
            difficulty = DifficultyLevel.EASY,
            scoring = ScoringParameters(
                pitchWeight = 0.75f,         // Less strict on pitch accuracy
                mfccWeight = 0.25f,
                pitchTolerance = 20f,         // Very forgiving pitch tolerance
                minScoreThreshold = 0.15f,    // Easy to get some points
                perfectScoreThreshold = 0.75f, // Easier to get high scores
                scoreCurve = 2.5f             // More generous curve
            ),
            content = ContentDetectionParameters(
                contentDetectionBestThreshold = 0.25f,  // Easier content detection
                contentDetectionAvgThreshold = 0.15f,
                rightContentFlatPenalty = 0.1f,         // Light penalties
                rightContentDifferentMelodyPenalty = 0.05f,
                wrongContentStandardPenalty = 0.3f      // Less harsh on wrong content
            ),
            melodic = MelodicAnalysisParameters(
                monotoneDetectionThreshold = 1.5f,      // Less strict monotone detection
                flatSpeechThreshold = 0.7f,
                monotonePenalty = 0.2f                   // Lighter monotone penalty
            )
        )
    }

    // Normal Mode - Balanced scoring (current defaults)
    fun normalMode(): Presets {
        return Presets(
            difficulty = DifficultyLevel.NORMAL,
            scoring = ScoringParameters(),
            content = ContentDetectionParameters(),
            melodic = MelodicAnalysisParameters(),
            musical = MusicalSimilarityParameters(),
            audio = AudioProcessingParameters(),
            scaling = ScoreScalingParameters()
        )
    }

    // Hard Mode - Strict scoring for advanced users
    fun hardMode(): Presets {
        return Presets(
            difficulty = DifficultyLevel.HARD,
            scoring = ScoringParameters(
                pitchWeight = 0.9f,          // Very strict on pitch
                mfccWeight = 0.1f,
                pitchTolerance = 8f,          // Tight pitch tolerance
                minScoreThreshold = 0.3f,     // Hard to get points
                perfectScoreThreshold = 0.9f, // Very hard to get perfect
                scoreCurve = 1.5f             // Less generous curve
            ),
            content = ContentDetectionParameters(
                contentDetectionBestThreshold = 0.5f,   // Strict content detection
                contentDetectionAvgThreshold = 0.35f,
                rightContentFlatPenalty = 0.3f,         // Harsh penalties
                rightContentDifferentMelodyPenalty = 0.2f,
                wrongContentStandardPenalty = 0.7f      // Very harsh on wrong content
            ),
            melodic = MelodicAnalysisParameters(
                monotoneDetectionThreshold = 2.5f,      // Strict monotone detection
                flatSpeechThreshold = 0.3f,
                monotonePenalty = 0.5f                   // Heavy monotone penalty
            )
        )
    }

    // NEW: Expert Mode - Very strict for advanced singers
    fun expertMode(): Presets {
        return Presets(
            difficulty = DifficultyLevel.EXPERT,
            scoring = ScoringParameters(
                pitchWeight = 0.95f,          // Extremely strict on pitch
                mfccWeight = 0.05f,
                pitchTolerance = 5f,          // Very tight pitch tolerance (quarter-tone precision)
                minScoreThreshold = 0.4f,     // Very hard to get points
                perfectScoreThreshold = 0.95f, // Near-impossible to get perfect
                scoreCurve = 1.2f             // Much less generous curve
            ),
            content = ContentDetectionParameters(
                contentDetectionBestThreshold = 0.6f,   // Very strict content detection
                contentDetectionAvgThreshold = 0.45f,
                rightContentFlatPenalty = 0.4f,         // Very harsh penalties
                rightContentDifferentMelodyPenalty = 0.3f,
                wrongContentStandardPenalty = 0.8f      // Extremely harsh on wrong content
            ),
            melodic = MelodicAnalysisParameters(
                monotoneDetectionThreshold = 3.0f,      // Very strict monotone detection
                flatSpeechThreshold = 0.2f,
                monotonePenalty = 0.7f                   // Severe monotone penalty
            ),
            scaling = ScoreScalingParameters(
                incredibleFeedbackThreshold = 95,       // Requires 95%+ for "incredible"
                greatJobFeedbackThreshold = 85,         // Requires 85%+ for "great job"
                goodEffortFeedbackThreshold = 70        // Requires 70%+ for "good effort"
            )
        )
    }

    // NEW: Master Mode - Perfection required for the elite
    fun masterMode(): Presets {
        return Presets(
            difficulty = DifficultyLevel.MASTER,
            scoring = ScoringParameters(
                pitchWeight = 0.98f,          // Near-perfect pitch required
                mfccWeight = 0.02f,
                pitchTolerance = 3f,          // Micro-tonal precision required
                minScoreThreshold = 0.5f,     // Extremely hard to get any points
                perfectScoreThreshold = 0.98f, // Virtually impossible perfect score
                scoreCurve = 1.0f             // Linear, unforgiving curve
            ),
            content = ContentDetectionParameters(
                contentDetectionBestThreshold = 0.7f,   // Extreme content detection strictness
                contentDetectionAvgThreshold = 0.55f,
                rightContentFlatPenalty = 0.5f,         // Brutal penalties
                rightContentDifferentMelodyPenalty = 0.4f,
                wrongContentStandardPenalty = 0.9f      // Nearly complete penalty for wrong content
            ),
            melodic = MelodicAnalysisParameters(
                monotoneDetectionThreshold = 4.0f,      // Extreme monotone detection
                flatSpeechThreshold = 0.1f,
                monotonePenalty = 0.9f                   // Devastating monotone penalty
            ),
            scaling = ScoreScalingParameters(
                incredibleFeedbackThreshold = 98,       // Requires 98%+ for "incredible"
                greatJobFeedbackThreshold = 90,         // Requires 90%+ for "great job"
                goodEffortFeedbackThreshold = 80,       // Requires 80%+ for "good effort"
                reverseMinScoreAdjustment = 1.0f,       // No easier adjustments for reverse
                reversePerfectScoreAdjustment = 1.0f,
                reverseCurveAdjustment = 1.0f
            )
        )
    }

    // Content-Focused Mode - Rewards getting words right
    fun contentFocusedMode(): Presets {
        return Presets(
            difficulty = DifficultyLevel.NORMAL, // Special mode, not a difficulty level
            scoring = ScoringParameters(
                pitchWeight = 0.95f,         // Heavily prioritize content
                mfccWeight = 0.05f,
                pitchTolerance = 25f          // Very forgiving on pitch differences
            ),
            content = ContentDetectionParameters(
                contentDetectionBestThreshold = 0.2f,   // Easy content detection
                contentDetectionAvgThreshold = 0.1f,
                rightContentFlatPenalty = 0.05f,        // Minimal penalty for right content
                rightContentDifferentMelodyPenalty = 0.02f,
                wrongContentStandardPenalty = 0.8f      // Heavy penalty for wrong content
            )
        )
    }

    // Melody-Focused Mode - Rewards exact melody matching
    fun melodyFocusedMode(): Presets {
        return Presets(
            difficulty = DifficultyLevel.NORMAL, // Special mode, not a difficulty level
            scoring = ScoringParameters(
                pitchWeight = 0.95f,
                mfccWeight = 0.05f,
                pitchTolerance = 5f           // Very strict pitch matching
            ),
            content = ContentDetectionParameters(
                contentDetectionBestThreshold = 0.6f,   // Strict about melody similarity
                contentDetectionAvgThreshold = 0.4f,
                rightContentFlatPenalty = 0.4f,         // Penalty for not being melodic enough
                rightContentDifferentMelodyPenalty = 0.3f,
                wrongContentStandardPenalty = 0.4f      // Less focus on content accuracy
            ),
            melodic = MelodicAnalysisParameters(
                melodicRangeWeight = 0.5f,               // Emphasize range and transitions
                melodicTransitionWeight = 0.4f,
                melodicVarianceWeight = 0.1f
            )
        )
    }

    // Helper function to get all difficulty presets in order
    fun getAllDifficultyPresets(): List<Pair<DifficultyLevel, () -> Presets>> {
        return listOf(
            DifficultyLevel.EASY to ::easyMode,
            DifficultyLevel.NORMAL to ::normalMode,
            DifficultyLevel.HARD to ::hardMode,
            DifficultyLevel.EXPERT to ::expertMode,
            DifficultyLevel.MASTER to ::masterMode
        )
    }
}

// Helper data class to group all parameters (ENHANCED)
data class Presets(
    val difficulty: DifficultyLevel = DifficultyLevel.NORMAL,
    val scoring: ScoringParameters = ScoringParameters(),
    val content: ContentDetectionParameters = ContentDetectionParameters(),
    val melodic: MelodicAnalysisParameters = MelodicAnalysisParameters(),
    val musical: MusicalSimilarityParameters = MusicalSimilarityParameters(),
    val audio: AudioProcessingParameters = AudioProcessingParameters(),
    val scaling: ScoreScalingParameters = ScoreScalingParameters()
)

// Extension function to apply preset to ScoringEngine (ENHANCED)
fun ScoringEngine.applyPreset(preset: Presets) {
    updateParameters(preset.scoring)
    updateContentParameters(preset.content)
    updateMelodicParameters(preset.melodic)
    updateMusicalParameters(preset.musical)
    updateAudioParameters(preset.audio)
    updateScalingParameters(preset.scaling)
    // Store the current difficulty level for UI feedback
    setCurrentDifficulty(preset.difficulty)
}