package com.example.reversey.scoring

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