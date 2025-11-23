package com.quokkalabs.reversey.scoring

/**
 * 🎤 SPEECH SCORING MODELS — FULLY COMMENTED
 *
 * This file defines how ReVerseY evaluates SPEECH (spoken, not sung)
 * across the three supported difficulty levels:
 *
 *   • EASY   – friendly, highly forgiving, great for beginners
 *   • NORMAL – realistic spoken accuracy expectations
 *   • HARD   – demanding but still speech-appropriate
 *
 * Speech scoring is fundamentally different from singing scoring:
 *
 * SINGING cares about:
 *    ✓ pitch accuracy
 *    ✓ melodic intervals
 *    ✓ musical phrasing
 *
 * SPEECH cares about:
 *    ✓ syllable correctness
 *    ✓ spectral match with reference (MFCCs)
 *    ✓ natural variation (not monotone robot voice)
 *    ✓ timing/phrasing
 *
 * Pitch still matters (speech has pitch contours),
 * but MFCC/timbre is **far more important**.
 *
 * These presets work with the same “Presets” structure as singing,
 * but their values are tuned for speech-appropriate behaviour.
 */
object SpeechScoringModels {

    // -------------------------------------------------------------------------
    // 🎤 EASY MODE — Maximum forgiveness
    // -------------------------------------------------------------------------
    /**
     * EASY MODE
     * - Designed to help new users have fun immediately.
     * - Very forgiving pitch tolerance.
     * - Content detection is lenient.
     * - Garbage detector allows wide vocal variation.
     */
    fun easyModeSpeech(): Presets {
        return Presets(
            difficulty = DifficultyLevel.EASY,

            scoring = ScoringParameters(
                pitchWeight = 0.65f,
                mfccWeight = 0.35f,
                pitchTolerance = 50f,
                minScoreThreshold = 0.08f,
                perfectScoreThreshold = 0.75f,
                reverseMinScoreThreshold = 0.06f,
                reversePerfectScoreThreshold = 0.68f,
                scoreCurve = 3.0f
            ),

            content = ContentDetectionParameters(
                // 🟢 EASY CALIBRATION: Very lenient. Allows easy passing.
                contentDetectionBestThreshold = 0.65f,  // ⬆️ Adjusted from 0.20f for stability
                contentDetectionAvgThreshold = 0.35f,
                reverseHandicap = 0.20f, // ⬆️ Max Help: 0.65 - 0.20 = 0.45 (Very Safe)
                rightContentFlatPenalty = 0.05f,
                rightContentDifferentMelodyPenalty = 0.02f,
                wrongContentStandardPenalty = 0.35f
            ),

            melodic = MelodicAnalysisParameters(
                monotoneDetectionThreshold = 0.5f,
                flatSpeechThreshold = 1.5f,
                monotonePenalty = 0.05f,
                melodicRangeWeight = 0.05f,
                melodicTransitionWeight = 0.05f,
                melodicVarianceWeight = 0.90f
            ),

            musical = MusicalSimilarityParameters(
                sameIntervalScore = 0.8f,
                closeIntervalScore = 0.7f,
                emptyPhrasesPenalty = 0.10f,
                emptyRhythmPenalty = 0.05f
            ),

            audio = AudioProcessingParameters(),

            scaling = ScoreScalingParameters(
                incredibleFeedbackThreshold = 75,
                greatJobFeedbackThreshold = 55,
                goodEffortFeedbackThreshold = 35,
            ),

            garbage = GarbageDetectionParameters(
                enableGarbageDetection = true,
                mfccVarianceThreshold = 0.10f,
                pitchMonotoneThreshold = 3f,
                pitchOscillationRate = 1.0f,
                spectralEntropyThreshold = 0.25f,
                zcrMinThreshold = 0.005f,
                zcrMaxThreshold = 0.45f,
                silenceRatioMin = 0.02f,
                garbageScoreMax = 30
            )
        )
    }

    // -------------------------------------------------------------------------
    // 🎤 NORMAL MODE — Balanced and realistic (LOCKED)
    // -------------------------------------------------------------------------
    fun normalModeSpeech(): Presets {
        return Presets(
            difficulty = DifficultyLevel.NORMAL,

            scoring = ScoringParameters(
                pitchWeight = 0.85f,
                mfccWeight = 0.15f,
                pitchTolerance = 40f,
                minScoreThreshold = 0.12f,
                perfectScoreThreshold = 0.80f,
                reverseMinScoreThreshold = 0.10f,
                reversePerfectScoreThreshold = 0.77f,
                scoreCurve = 3.2f,
            ),

            content = ContentDetectionParameters(
                // 🎯 BASELINE: 0.80 is the standard trap door.
                contentDetectionBestThreshold = 0.80f,
                contentDetectionAvgThreshold = 0.45f,
                reverseHandicap = 0.15f, // 🎯 Standard Help: 0.80 - 0.15 = 0.65
                rightContentFlatPenalty = 0.08f,
                rightContentDifferentMelodyPenalty = 0.04f,
                wrongContentStandardPenalty = 0.20f
            ),

            melodic = MelodicAnalysisParameters(
                monotoneDetectionThreshold = 0.8f,
                flatSpeechThreshold = 1.0f,
                monotonePenalty = 0.10f,
                melodicRangeWeight = 0.10f,
                melodicTransitionWeight = 0.10f,
                melodicVarianceWeight = 0.80f
            ),

            musical = MusicalSimilarityParameters(
                sameIntervalScore = 0.85f,
                closeIntervalScore = 0.75f,
                emptyPhrasesPenalty = 0.15f,
                emptyRhythmPenalty = 0.10f
            ),

            audio = AudioProcessingParameters(),

            scaling = ScoreScalingParameters(
                incredibleFeedbackThreshold = 80,
                greatJobFeedbackThreshold = 60,
                goodEffortFeedbackThreshold = 40,
            ),

            garbage = GarbageDetectionParameters(
                enableGarbageDetection = true,
                mfccVarianceThreshold = 0.25f,
                pitchMonotoneThreshold = 8f,
                pitchOscillationRate = 0.7f,
                spectralEntropyThreshold = 0.45f,
                zcrMinThreshold = 0.015f,
                zcrMaxThreshold = 0.30f,
                silenceRatioMin = 0.08f,
                garbageScoreMax = 15
            )
        )
    }

    // -------------------------------------------------------------------------
    // 🎤 HARD MODE — Demanding but speech-appropriate
    // -------------------------------------------------------------------------
    fun hardModeSpeech(): Presets {
        return Presets(
            difficulty = DifficultyLevel.HARD,

            scoring = ScoringParameters(
                pitchWeight = 0.75f,
                mfccWeight = 0.25f,
                pitchTolerance = 30f,
                minScoreThreshold = 0.18f,
                perfectScoreThreshold = 0.80f,
                reverseMinScoreThreshold = 0.14f,
                reversePerfectScoreThreshold = 0.72f,
                scoreCurve = 2.3f
            ),

            content = ContentDetectionParameters(
                // 🚨 FIX: Must be higher than Normal's 0.80
                contentDetectionBestThreshold = 0.88f, // ⬆️ Set to 0.88 for high demand
                contentDetectionAvgThreshold = 0.50f,
                reverseHandicap = 0.10f, // Minimal Help: 0.88 - 0.10 = 0.78
                rightContentFlatPenalty = 0.15f,
                rightContentDifferentMelodyPenalty = 0.08f,
                wrongContentStandardPenalty = 0.65f
            ),

            melodic = MelodicAnalysisParameters(
                monotoneDetectionThreshold = 1.5f,
                flatSpeechThreshold = 0.8f,
                monotonePenalty = 0.20f,
                melodicRangeWeight = 0.20f,
                melodicTransitionWeight = 0.20f,
                melodicVarianceWeight = 0.60f
            ),

            musical = MusicalSimilarityParameters(
                sameIntervalScore = 0.90f,
                closeIntervalScore = 0.80f,
                emptyPhrasesPenalty = 0.20f,
                emptyRhythmPenalty = 0.15f
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
                pitchMonotoneThreshold = 10f,
                pitchOscillationRate = 0.6f,
                spectralEntropyThreshold = 0.55f,
                zcrMinThreshold = 0.018f,
                zcrMaxThreshold = 0.25f,
                silenceRatioMin = 0.10f,
                garbageScoreMax = 18
            )
        )
    }

    // -------------------------------------------------------------------------
    // Return all 3 difficulty levels in order
    // -------------------------------------------------------------------------
    fun getAllSpeechDifficultyPresets(): List<Pair<DifficultyLevel, () -> Presets>> {
        return listOf(
            DifficultyLevel.EASY to ::easyModeSpeech,
            DifficultyLevel.NORMAL to ::normalModeSpeech,
            DifficultyLevel.HARD to ::hardModeSpeech
        )
    }

    //helper function This allows the orchestrator to call:
    //SpeechScoringModels.presetFor(difficulty)
    fun presetFor(level: DifficultyLevel): Presets {
        return when (level) {
            DifficultyLevel.EASY -> easyModeSpeech()
            DifficultyLevel.NORMAL -> normalModeSpeech()
            DifficultyLevel.HARD -> hardModeSpeech()
        }
    }
}
