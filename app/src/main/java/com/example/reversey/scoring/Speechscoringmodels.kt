package com.example.reversey.scoring

import com.example.reversey.scoring.SpeechScoringModels.easyModeSpeech
import com.example.reversey.scoring.SpeechScoringModels.hardModeSpeech
import com.example.reversey.scoring.SpeechScoringModels.normalModeSpeech

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

            // --------------------------- 🎯 SCORING ---------------------------
            /**
             * For speech:
             *
             * - pitchWeight is LOWER than singing
             * - mfccWeight is HIGHER because timbre/formants carry more meaning
             * - pitchTolerance is LARGE because speech pitch is less stable
             */
            scoring = ScoringParameters(
                pitchWeight = 0.65f,
                mfccWeight = 0.35f,
                pitchTolerance = 50f,         // Very forgiving: speech varies wildly
                minScoreThreshold = 0.08f,
                perfectScoreThreshold = 0.75f,
                reverseMinScoreThreshold = 0.06f,      // 80% of 0.08f (easier floor)
                reversePerfectScoreThreshold = 0.68f,  // 90% of 0.75f (easier ceiling)
                scoreCurve = 3.0f             // Larger curve = more forgiving scoring drop-off
            ),

            // --------------------------- 📝 CONTENT ---------------------------
            /**
             * Content detection is more important for speech than singing.
             * However, EASY mode significantly eases the thresholds.
             */
            content = ContentDetectionParameters(
                contentDetectionBestThreshold = 0.20f,  // You only need a modestly good match
                contentDetectionAvgThreshold = 0.10f,
                rightContentFlatPenalty = 0.05f,        // Flat speech is totally fine
                rightContentDifferentMelodyPenalty = 0.02f, // Melody barely matters in speech
                wrongContentStandardPenalty = 0.35f     // Wrong words still matter
            ),

            // --------------------------- 🎶 MELODIC ---------------------------
            /**
             * Speech does NOT require melodic shape.
             * But some variation is still good (to avoid robotic monotone).
             */
            melodic = MelodicAnalysisParameters(
                monotoneDetectionThreshold = 0.5f, // Low threshold = easy acceptance
                flatSpeechThreshold = 1.5f,        // Very forgiving of monotone delivery
                monotonePenalty = 0.05f,          // Almost no penalty for monotone speech
                melodicRangeWeight = 0.05f,       // Speech doesn't require range
                melodicTransitionWeight = 0.05f,  // Transitions not important
                melodicVarianceWeight = 0.90f     // ANY variation is rewarded
            ),

            // --------------------------- 🎼 MUSICAL ---------------------------
            /**
             * Speech is not musical, so musical rules are extremely relaxed.
             */
            musical = MusicalSimilarityParameters(
                sameIntervalScore = 0.8f,
                closeIntervalScore = 0.7f,
                emptyPhrasesPenalty = 0.10f,
                emptyRhythmPenalty = 0.05f
            ),

            // --------------------------- 🔊 AUDIO -----------------------------
            audio = AudioProcessingParameters(),

            // --------------------------- 📈 SCALING ---------------------------
            scaling = ScoreScalingParameters(
                incredibleFeedbackThreshold = 75,
                greatJobFeedbackThreshold = 55,
                goodEffortFeedbackThreshold = 35,
                //reversePerfectScoreAdjustment = 1.05f // Reverse easier for speech
            ),

            // --------------------------- 🚫 GARBAGE ---------------------------
            /**
             * In EASY mode, almost anything that resembles speech is accepted.
             * Only extreme cases (static, pure tones, long silence) get filtered.
             */
            garbage = GarbageDetectionParameters(
                enableGarbageDetection = true,
                mfccVarianceThreshold = 0.10f,  // Low = speech has low timbre variance
                pitchMonotoneThreshold = 3f,    // Speech is allowed to be fairly monotone
                pitchOscillationRate = 1.0f,    // High = big natural pitch movements are OK
                spectralEntropyThreshold = 0.25f,
                zcrMinThreshold = 0.005f,
                zcrMaxThreshold = 0.45f,         // Wide range of consonant/unvoiced sounds allowed
                silenceRatioMin = 0.02f,
                garbageScoreMax = 30             // Very lenient
            )
        )
    }

    // -------------------------------------------------------------------------
    // 🎤 NORMAL MODE — Balanced and realistic
    // -------------------------------------------------------------------------
    /**
     * NORMAL MODE
     * - More realistic speech expectations.
     * - Tighter content matching.
     * - Less pitch tolerance (still far more than singing).
     * - Garbage filter less lenient, but still appropriate for speech.
     */
    fun normalModeSpeech(): Presets {
        return Presets(
            difficulty = DifficultyLevel.NORMAL,

            scoring = ScoringParameters(
                pitchWeight = 0.85f,       // UP from 0.70f - more content/timing weight
                mfccWeight = 0.15f,        // DOWN from 0.30f - less voice similarity bias
                pitchTolerance = 40f,
                minScoreThreshold = 0.12f,
                perfectScoreThreshold = 0.80f,   // DOWN from 0.85f - easier to get high scores
                reverseMinScoreThreshold = 0.10f,      // 80% of 0.12f
                reversePerfectScoreThreshold = 0.77f,  // 90% of 0.85f
                scoreCurve = 3.2f,               // UP from 2.8f - more forgiving curve
            ),

            content = ContentDetectionParameters(
                // We set the bar at 0.80 to BLOCK it, while remaining safer than 0.85.
                contentDetectionBestThreshold = 0.80f,
                contentDetectionAvgThreshold = 0.45f,   // Adjusted ratio
                rightContentFlatPenalty = 0.08f,
                rightContentDifferentMelodyPenalty = 0.04f,

                // 🚨 PENALTY CALIBRATION 🚨
                // This is a MULTIPLIER.
                // 0.70 = retain 70% of score (Soft penalty).
                // 0.50 = retain 50% of score (Hard penalty).
                wrongContentStandardPenalty = 0.20f // ⬇️ Set to 0.50 to halve the score on wrong words
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
                //reversePerfectScoreAdjustment = 1.0f
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
    /**
     * HARD MODE
     * - High speech clarity required.
     * - Content detection thresholds significantly higher.
     * - Garbage detector is much stricter (detects monotone attempts).
     * - Pitch matters more — but still nothing like singing mode.
     */
    fun hardModeSpeech(): Presets {
        return Presets(
            difficulty = DifficultyLevel.HARD,

            scoring = ScoringParameters(
                pitchWeight = 0.75f,
                mfccWeight = 0.25f,
                pitchTolerance = 30f,
                minScoreThreshold = 0.18f,
                perfectScoreThreshold = 0.80f,
                reverseMinScoreThreshold = 0.14f,      // 80% of 0.18f
                reversePerfectScoreThreshold = 0.72f,  // 90% of 0.80f
                scoreCurve = 2.3f
            ),

            content = ContentDetectionParameters(
                contentDetectionBestThreshold = 0.35f,
                contentDetectionAvgThreshold = 0.25f,
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
                //reversePerfectScoreAdjustment = 0.98f
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
