package com.example.reversey.scoring

/**
 * 🎵 SINGING SCORING MODELS — FULLY COMMENTED
 *
 * This file defines the scoring behaviour when the user is SINGING.
 *
 * ReVerseY evaluates singing using a large set of parameters grouped into:
 *
 * • SCORING  — How raw correctness is evaluated (pitch, timbre, tolerance)
 * • CONTENT  — Whether the sung words/syllables match the reference audio
 * • MELODIC  — Shape, variation, vocal range, and movement of melody
 * • MUSICAL  — Intervals, phrasing, and rhythmic structure
 * • AUDIO    — Low-level audio preprocessing (defaults kept)
 * • SCALING  — Converts internal score into final 0–100 + feedback labels
 * • GARBAGE  — Detects non-singing (humming, static, monotone buzzing)
 *
 * SINGING MODE is stricter about:
 * - pitch accuracy
 * - melodic intervals
 * - musical phrasing
 *
 * And more forgiving about:
 * - minor phonetic errors when melody is correct
 *
 * This file implements ONLY the 3 supported difficulty levels:
 * EASY, NORMAL, HARD
 *
 * EXPERT and MASTER modes have been intentionally removed.
 */


/**
 * 🎵 SINGING SCORING MODELS — RE-CALIBRATED
 *
 * Calibrated Nov 2025 for "No Nonsense" Scoring.
 *
 * • EASY:   Forgiving (Curve 1.8). "Feel Good" mode.
 * • NORMAL: Linear (Curve 1.0). "Reality Check" mode.
 * • HARD:   Punishing (Curve 0.25). "Sniper" mode.
 */
object SingingScoringModels {

    // -------------------------------------------------------------------------
    // 🎵 EASY MODE — The "Feel Good" Mode
    // -------------------------------------------------------------------------
    fun easyModeSinging(): Presets {
        return Presets(
            difficulty = DifficultyLevel.EASY,

            scoring = ScoringParameters(
                pitchWeight = 0.90f,
                mfccWeight = 0.10f,
                pitchTolerance = 25f,          // Loose tolerance
                minScoreThreshold = 0.15f,
                perfectScoreThreshold = 0.92f, // Lower bar for perfection
                scoreCurve = 1.8f              // 🚀 Inflation Curve (B becomes A+)
            ),

            content = ContentDetectionParameters(
                // Trap Door 0.70: Safe for almost everyone, but stops humming.
                contentDetectionBestThreshold = 0.70f,
                contentDetectionAvgThreshold = 0.50f,

                rightContentFlatPenalty = 0.20f,
                rightContentDifferentMelodyPenalty = 0.10f,
                wrongContentStandardPenalty = 0.20f // Still crushes humming to 0
            ),

            melodic = MelodicAnalysisParameters(
                monotoneDetectionThreshold = 2.5f,
                flatSpeechThreshold = 0.5f,
                monotonePenalty = 0.3f
            ),

            musical = MusicalSimilarityParameters(
                sameIntervalScore = 1.0f,
                closeIntervalScore = 0.85f,
                similarIntervalScore = 0.6f,
                emptyPhrasesPenalty = 0.30f
            ),

            audio = AudioProcessingParameters(),

            scaling = ScoreScalingParameters(
                incredibleFeedbackThreshold = 85,
                greatJobFeedbackThreshold = 65,
                goodEffortFeedbackThreshold = 45,
            ),

            garbage = GarbageDetectionParameters(
                enableGarbageDetection = true,
                mfccVarianceThreshold = 0.35f,
                pitchMonotoneThreshold = 12f,
                spectralEntropyThreshold = 0.6f,
                garbageScoreMax = 15
            )
        )
    }

    // -------------------------------------------------------------------------
    // 🎵 NORMAL MODE — Linear Reality (The Standard)
    // -------------------------------------------------------------------------
    fun normalModeSinging(): Presets {
        return Presets(
            difficulty = DifficultyLevel.NORMAL,

            scoring = ScoringParameters(
                pitchWeight = 0.90f,
                mfccWeight = 0.10f,
                pitchTolerance = 20f,          // Standard precision
                minScoreThreshold = 0.22f,
                perfectScoreThreshold = 0.98f, // ⬆️ Raised: Must be nearly perfect for 100
                scoreCurve = 1.0f              // 📏 Linear: What you get is what you score
            ),

            content = ContentDetectionParameters(
                // Catches humming (<0.60) but allows accents/gender diffs (0.78+).
                contentDetectionBestThreshold = 0.75f,
                contentDetectionAvgThreshold = 0.55f,

                rightContentFlatPenalty = 0.30f,
                rightContentDifferentMelodyPenalty = 0.20f,
                wrongContentStandardPenalty = 0.20f // The Crusher (Trap Door)
            ),

            melodic = MelodicAnalysisParameters(
                monotoneDetectionThreshold = 3.0f,
                flatSpeechThreshold = 0.4f,
                monotonePenalty = 0.4f
            ),

            musical = MusicalSimilarityParameters(
                sameIntervalScore = 1.0f,
                closeIntervalScore = 0.85f,
                similarIntervalScore = 0.6f,
                emptyPhrasesPenalty = 0.35f
            ),

            audio = AudioProcessingParameters(),

            scaling = ScoreScalingParameters(
                incredibleFeedbackThreshold = 88,
                greatJobFeedbackThreshold = 70,
                goodEffortFeedbackThreshold = 50,
            ),

            garbage = GarbageDetectionParameters(
                enableGarbageDetection = true,
                mfccVarianceThreshold = 0.45f,
                pitchMonotoneThreshold = 15f,
                spectralEntropyThreshold = 0.70f,
                garbageScoreMax = 12
            )
        )
    }

    // -------------------------------------------------------------------------
    // 🎵 HARD MODE — The Punisher (Nuclear Winter)
    // -------------------------------------------------------------------------
    fun hardModeSinging(): Presets {
        return Presets(
            difficulty = DifficultyLevel.HARD,

            scoring = ScoringParameters(
                pitchWeight = 0.95f,           // Melody is everything
                mfccWeight = 0.05f,
                pitchTolerance = 12f,          // 🎯 Sniper tolerance (12 cents)
                minScoreThreshold = 0.30f,
                perfectScoreThreshold = 0.99f, // Perfection required
                scoreCurve = 0.25f             // 📉 Nuclear Curve: Crushes anything < 90%
            ),

            content = ContentDetectionParameters(
                // 0.78 is the "Sniper" threshold.
                // Requires clear articulation and close pitch match.
                contentDetectionBestThreshold = 0.78f,
                contentDetectionAvgThreshold = 0.60f,

                rightContentFlatPenalty = 0.40f,
                rightContentDifferentMelodyPenalty = 0.25f,
                wrongContentStandardPenalty = 0.10f // Absolute annihilation (0.1x)
            ),

            melodic = MelodicAnalysisParameters(
                monotoneDetectionThreshold = 4.0f,
                flatSpeechThreshold = 0.3f,
                monotonePenalty = 0.6f
            ),

            musical = MusicalSimilarityParameters(
                sameIntervalScore = 1.0f,
                closeIntervalScore = 0.80f,
                similarIntervalScore = 0.4f,
                emptyPhrasesPenalty = 0.50f
            ),

            audio = AudioProcessingParameters(),

            scaling = ScoreScalingParameters(
                incredibleFeedbackThreshold = 92,
                greatJobFeedbackThreshold = 75,
                goodEffortFeedbackThreshold = 55,
            ),

            garbage = GarbageDetectionParameters(
                enableGarbageDetection = true,
                mfccVarianceThreshold = 0.50f,
                pitchMonotoneThreshold = 18f,
                spectralEntropyThreshold = 0.75f,
                garbageScoreMax = 5
            )
        )
    }

    // -------------------------------------------------------------------------
    // Helper Methods
    // -------------------------------------------------------------------------
    fun getAllSingingDifficultyPresets(): List<Pair<DifficultyLevel, () -> Presets>> {
        return listOf(
            DifficultyLevel.EASY to ::easyModeSinging,
            DifficultyLevel.NORMAL to ::normalModeSinging,
            DifficultyLevel.HARD to ::hardModeSinging
        )
    }

    fun presetFor(level: DifficultyLevel): Presets {
        return when (level) {
            DifficultyLevel.EASY -> easyModeSinging()
            DifficultyLevel.NORMAL -> normalModeSinging()
            DifficultyLevel.HARD -> hardModeSinging()
        }
    }
}