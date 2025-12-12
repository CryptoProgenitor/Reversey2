package com.quokkalabs.reversey.scoring

/**
 * ⚠️ DEPRECATED - December 2025
 * Speech presets no longer used in ReVerseY 2.0.
 * Kept for reference and potential future Forward Challenge mode.
 *
 * New system uses ReverseScoringEngine with Vosk ASR + phoneme matching.
 */

/**
 * 🎤 SPEECH SCORING MODELS — PHASE 2.5 CALIBRATION
 *
 * Recalibrated for DCT + Cosine Distance (v23.0.0)
 *
 * This file defines how ReVerseY evaluates SPEECH (spoken, not sung)
 * across the three supported difficulty levels:
 *
 *   • EASY   — friendly, highly forgiving, great for beginners
 *   • NORMAL — realistic spoken accuracy expectations
 *   • HARD   — demanding but still speech-appropriate
 *
 * Speech scoring is fundamentally different from singing scoring:
 *
 * SINGING cares about:
 *    ✔ pitch accuracy
 *    ✔ melodic intervals
 *    ✔ musical phrasing
 *
 * SPEECH cares about:
 *    ✔ syllable correctness
 *    ✔ spectral match with reference (MFCCs)
 *    ✔ natural variation (not monotone robot voice)
 *    ✔ timing/phrasing
 *
 * PHASE 2 CHANGES:
 *    ✔ Fixed weight progression bug (was 0.65→0.85→0.75, now 0.65→0.75→0.85)
 *    ✔ All thresholds raised to account for improved MFCC signal
 *    ✔ dtwNormalizationFactor now defaults to 1.0 in ScoringCommonModels
 *
 * PHASE 2.5 CHANGES (Speech REVERSE direction sensitivity):
 *    ✔ Added interval accuracy calculation for REVERSE challenges
 *    ✔ REVERSE uses 70/20/10 (interval/pitch/mfcc) - pitch is useless for speech direction
 *    ✔ Added speech-specific interval thresholds (wider than singing)
 *    ✔ FORWARD unchanged (validated)
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
                minScoreThreshold = 0.10f,
                perfectScoreThreshold = 0.79f,
                reverseMinScoreThreshold = 0.07f,
                reversePerfectScoreThreshold = 0.71f,
                scoreCurve = 3.0f,
                speechReverseIntervalWeight = 0.85f,
                speechReversePitchWeight = 0.10f,
                speechReverseMfccWeight = 0.05f
            ),

            content = ContentDetectionParameters(
                contentDetectionBestThreshold = 0.68f,
                contentDetectionAvgThreshold = 0.37f,
                reverseHandicap = 0.20f,
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
                emptyRhythmPenalty = 0.05f,
                sameIntervalThreshold = 2.0f,
                closeIntervalThreshold = 5.0f,
                similarIntervalThreshold = 10.0f
            ),

            audio = AudioProcessingParameters(),

            scaling = ScoreScalingParameters(
                incredibleFeedbackThreshold = 75,
                greatJobFeedbackThreshold = 55,
                goodEffortFeedbackThreshold = 35,
            ),

            garbage = GarbageDetectionParameters(
                enableGarbageDetection = true,
                mfccVarianceThreshold = 0.12f,
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
    // 🎤 NORMAL MODE — Balanced and realistic
    // -------------------------------------------------------------------------
    fun normalModeSpeech(): Presets {
        return Presets(
            difficulty = DifficultyLevel.NORMAL,

            scoring = ScoringParameters(
                pitchWeight = 0.75f,
                mfccWeight = 0.25f,
                pitchTolerance = 40f,
                minScoreThreshold = 0.15f,
                perfectScoreThreshold = 0.84f,
                reverseMinScoreThreshold = 0.12f,
                reversePerfectScoreThreshold = 0.81f,
                scoreCurve = 3.2f,
                speechReverseIntervalWeight = 0.85f,
                speechReversePitchWeight = 0.10f,
                speechReverseMfccWeight = 0.05f
            ),

            content = ContentDetectionParameters(
                contentDetectionBestThreshold = 0.84f,
                contentDetectionAvgThreshold = 0.47f,
                reverseHandicap = 0.15f,
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
                emptyRhythmPenalty = 0.10f,
                sameIntervalThreshold = 2.0f,
                closeIntervalThreshold = 5.0f,
                similarIntervalThreshold = 10.0f
            ),

            audio = AudioProcessingParameters(),

            scaling = ScoreScalingParameters(
                incredibleFeedbackThreshold = 80,
                greatJobFeedbackThreshold = 60,
                goodEffortFeedbackThreshold = 40,
            ),

            garbage = GarbageDetectionParameters(
                enableGarbageDetection = true,
                mfccVarianceThreshold = 0.30f,
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
                pitchWeight = 0.85f,
                mfccWeight = 0.15f,
                pitchTolerance = 30f,
                minScoreThreshold = 0.22f,
                perfectScoreThreshold = 0.84f,
                reverseMinScoreThreshold = 0.18f,
                reversePerfectScoreThreshold = 0.76f,
                scoreCurve = 2.3f,
                speechReverseIntervalWeight = 0.85f,
                speechReversePitchWeight = 0.10f,
                speechReverseMfccWeight = 0.05f
            ),

            content = ContentDetectionParameters(
                contentDetectionBestThreshold = 0.92f,
                contentDetectionAvgThreshold = 0.53f,
                reverseHandicap = 0.10f,
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
                emptyRhythmPenalty = 0.15f,
                sameIntervalThreshold = 2.0f,
                closeIntervalThreshold = 5.0f,
                similarIntervalThreshold = 10.0f
            ),

            audio = AudioProcessingParameters(),

            scaling = ScoreScalingParameters(
                incredibleFeedbackThreshold = 85,
                greatJobFeedbackThreshold = 65,
                goodEffortFeedbackThreshold = 45,
            ),

            garbage = GarbageDetectionParameters(
                enableGarbageDetection = true,
                mfccVarianceThreshold = 0.42f,
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

    // Helper function: allows orchestrator to call SpeechScoringModels.presetFor(difficulty)
    fun presetFor(level: DifficultyLevel): Presets {
        return when (level) {
            DifficultyLevel.EASY -> easyModeSpeech()
            DifficultyLevel.NORMAL -> normalModeSpeech()
            DifficultyLevel.HARD -> hardModeSpeech()
        }
    }
}