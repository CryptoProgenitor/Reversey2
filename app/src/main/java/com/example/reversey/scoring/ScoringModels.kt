package com.example.reversey.scoring

data class ScoringParameters(
    // --- NEW SIMPLIFIED WEIGHTS ---
    var pitchWeight: Float = 0.85f, //was 0.6f
    var mfccWeight: Float = 0.15f, //was 0.4f

    // Pitch analysis parameters
    var pitchTolerance: Float = 1.5f,        // semitones
    var pitchContourWeight: Float = 0.7f,    // 0-1 range, how much to prioritise melodic shape
    var variancePenalty: Float = 0.5f,       // 0-1 range, penalty for unnaturally flat tones
    var dtwNormalizationFactor: Float = 15f,  //  from 20f Lower is stricter on gibberish

    // Detection
    var silenceThreshold: Float = 0.01f,     // RMS level below which audio is considered silent - was 0.02f

    // Score scaling
    var minScoreThreshold: Float = 0.47f,  // from 0.42f ##
    var perfectScoreThreshold: Float = 0.85f,
    var scoreCurve: Float = 2.3f,  // Down from 1.2f

    // Bonuses (de-emphasized, keep them small)
    var consistencyBonus: Float = 0.05f,
    var confidenceBonus: Float = 0.05f
)

data class ScoringResult(
    val score: Int,           // 0-100
    val rawScore: Float,      // 0-1
    val metrics: SimilarityMetrics,
    val feedback: List<String>
)

data class SimilarityMetrics(
    // --- NEW SIMPLIFIED METRICS ---
    val pitch: Float,
    val mfcc: Float
)

