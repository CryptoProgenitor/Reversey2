package com.example.reversey.scoring

import com.example.reversey.scoring.SpeechScoringModels.easyModeSpeech
import com.example.reversey.scoring.SpeechScoringModels.hardModeSpeech
import com.example.reversey.scoring.SpeechScoringModels.normalModeSpeech

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
object SingingScoringModels {

    // -------------------------------------------------------------------------
    // 🎵 EASY MODE — Beginner-friendly
    // -------------------------------------------------------------------------
    /**
     * EASY MODE
     * - Makes it fun for beginners.
     * - High tolerance for pitch errors (you don't have to be in tune).
     * - Melodic analysis is softer.
     * - Garbage detector is forgiving.
     */
    fun easyModeSinging(): Presets {
        return Presets(
            difficulty = DifficultyLevel.EASY,

            // ----------------------- 🎯 SCORING ------------------------
            /**
             * SCORING PARAMETERS:
             *
             * - pitchWeight:     How important pitch accuracy is to the score.
             * - mfccWeight:      How much timbre (voice character) matters.
             * - pitchTolerance:  How far off pitch you can be before scoring drops.
             * - minScoreThreshold:  Below this, almost no points rewarded.
             * - perfectScoreThreshold:  When raw score counts as “perfect”.
             * - scoreCurve:      Higher = more forgiving; lower = stricter.
             */
            scoring = ScoringParameters(
                pitchWeight = 0.85f,
                mfccWeight = 0.15f,
                pitchTolerance = 25f,
                minScoreThreshold = 0.15f,
                perfectScoreThreshold = 0.85f,
                reverseMinScoreThreshold = 0.12f,      // 80% of 0.15f (easier floor)
                reversePerfectScoreThreshold = 0.77f,  // 90% of 0.85f (easier ceiling)
                scoreCurve = 2.0f
            ),

            // ----------------------- 📝 CONTENT ------------------------
            /**
             * CONTENT DETECTION:
             *
             * These measure how well the words match the reference.
             * For singing, melody matters more than exact pronunciation.
             */
            content = ContentDetectionParameters(
                contentDetectionBestThreshold = 0.30f,
                contentDetectionAvgThreshold = 0.20f,
                rightContentFlatPenalty = 0.25f,           // If the word is right but sung flat
                rightContentDifferentMelodyPenalty = 0.15f,// Word right, melody wrong
                wrongContentStandardPenalty = 0.20f        // Incorrect syllable/word
            ),

            // ----------------------- 🎶 MELODIC ------------------------
            /**
             * MELODIC ANALYSIS:
             *
             * Looks at how the pitch moves:
             * - Is the singer monotone?
             * - Do they follow the general shape of the melody?
             * - Do they have some range?
             */
            melodic = MelodicAnalysisParameters(
                monotoneDetectionThreshold = 2.5f,
                flatSpeechThreshold = 0.5f,
                monotonePenalty = 0.3f,
                melodicRangeWeight = 0.35f,
                melodicTransitionWeight = 0.4f,
                melodicVarianceWeight = 0.25f
            ),

            // ----------------------- 🎼 MUSICAL ------------------------
            /**
             * MUSICAL SIMILARITY:
             *
             * - Checks interval correctness (distance between notes).
             * - Checks rhythmic structure (empty or missing phrases).
             * - Ensures the melodic shape follows the reference.
             */
            musical = MusicalSimilarityParameters(
                sameIntervalScore = 1.0f,
                closeIntervalScore = 0.85f,
                similarIntervalScore = 0.6f,
                emptyPhrasesPenalty = 0.40f,
                emptyRhythmPenalty = 0.30f
            ),

            // ----------------------- 🔊 AUDIO --------------------------
            audio = AudioProcessingParameters(), // defaults kept

            // ----------------------- 📈 SCALING ------------------------
            /**
             * SCORE SCALING:
             * Converts internal score (0–1 float) into 0–100 for UI,
             * and determines when to show positive feedback.
             */
            scaling = ScoreScalingParameters(
                incredibleFeedbackThreshold = 85,
                greatJobFeedbackThreshold = 65,
                goodEffortFeedbackThreshold = 45,
                //reversePerfectScoreAdjustment = 0.92f
            ),

            // ----------------------- 🚫 GARBAGE ------------------------
            /**
             * GARBAGE DETECTION:
             * Detects when the user is NOT really singing:
             *
             * - humming
             * - static
             * - monotone buzzing
             * - barely any vocal variety
             */
            garbage = GarbageDetectionParameters(
                enableGarbageDetection = true,
                mfccVarianceThreshold = 0.35f,
                pitchMonotoneThreshold = 12f,
                pitchOscillationRate = 0.4f,
                spectralEntropyThreshold = 0.6f,
                zcrMinThreshold = 0.02f,
                zcrMaxThreshold = 0.18f,
                silenceRatioMin = 0.12f,
                garbageScoreMax = 15
            )
        )
    }

    // -------------------------------------------------------------------------
    // 🎵 NORMAL MODE — Balanced and fair
    // -------------------------------------------------------------------------
    /**
     * NORMAL MODE
     * - Requires reasonably accurate singing.
     * - Pitch must be closer.
     * - Melody needs to follow the reference more closely.
     * - Content detection is stricter.
     */
    fun normalModeSinging(): Presets {
        return Presets(
            difficulty = DifficultyLevel.NORMAL,

            scoring = ScoringParameters(
                pitchWeight = 0.90f,
                mfccWeight = 0.10f,
                pitchTolerance = 20f,
                minScoreThreshold = 0.22f,
                perfectScoreThreshold = 0.92f,
                reverseMinScoreThreshold = 0.18f,      // 80% of 0.22f
                reversePerfectScoreThreshold = 0.83f,  // 90% of 0.92f
                scoreCurve = 1.8f
            ),

            content = ContentDetectionParameters(
                contentDetectionBestThreshold = 0.45f,
                contentDetectionAvgThreshold = 0.35f,
                rightContentFlatPenalty = 0.30f,
                rightContentDifferentMelodyPenalty = 0.20f,
                wrongContentStandardPenalty = 0.40f
            ),

            melodic = MelodicAnalysisParameters(
                monotoneDetectionThreshold = 3.0f,
                flatSpeechThreshold = 0.4f,
                monotonePenalty = 0.4f,
                melodicRangeWeight = 0.35f,
                melodicTransitionWeight = 0.4f,
                melodicVarianceWeight = 0.25f
            ),

            musical = MusicalSimilarityParameters(
                sameIntervalScore = 1.0f,
                closeIntervalScore = 0.85f,
                similarIntervalScore = 0.6f,
                emptyPhrasesPenalty = 0.35f,
                emptyRhythmPenalty = 0.25f
            ),

            audio = AudioProcessingParameters(),

            scaling = ScoreScalingParameters(
                incredibleFeedbackThreshold = 88,
                greatJobFeedbackThreshold = 70,
                goodEffortFeedbackThreshold = 50,
                //reversePerfectScoreAdjustment = 0.95f
            ),

            garbage = GarbageDetectionParameters(
                enableGarbageDetection = true,
                mfccVarianceThreshold = 0.45f,
                pitchMonotoneThreshold = 15f,
                pitchOscillationRate = 0.35f,
                spectralEntropyThreshold = 0.70f,
                zcrMinThreshold = 0.025f,
                zcrMaxThreshold = 0.15f,
                silenceRatioMin = 0.15f,
                garbageScoreMax = 12
            )
        )
    }

    // -------------------------------------------------------------------------
    // 🎵 HARD MODE — Demanding musical precision
    // -------------------------------------------------------------------------
    /**
     * HARD MODE
     * - Designed for confident singers.
     * - Low pitch tolerance, high penalties for monotone or flat delivery.
     * - Intervals and musical phrasing matter a lot.
     * - Garbage detector is strict.
     */
    fun hardModeSinging(): Presets {
        return Presets(
            difficulty = DifficultyLevel.HARD,

            scoring = ScoringParameters(
                pitchWeight = 0.93f,
                mfccWeight = 0.07f,
                pitchTolerance = 12f,
                minScoreThreshold = 0.30f,
                perfectScoreThreshold = 0.90f,
                reverseMinScoreThreshold = 0.24f,      // 80% of 0.30f
                reversePerfectScoreThreshold = 0.81f,  // 90% of 0.90f
                scoreCurve = 1.5f
            ),

            content = ContentDetectionParameters(
                contentDetectionBestThreshold = 0.55f,
                contentDetectionAvgThreshold = 0.40f,
                rightContentFlatPenalty = 0.40f,
                rightContentDifferentMelodyPenalty = 0.25f,
                wrongContentStandardPenalty = 0.45f
            ),

            melodic = MelodicAnalysisParameters(
                monotoneDetectionThreshold = 5.0f,
                flatSpeechThreshold = 0.2f,
                monotonePenalty = 0.6f,
                melodicRangeWeight = 0.45f,
                melodicTransitionWeight = 0.35f,
                melodicVarianceWeight = 0.20f
            ),

            musical = MusicalSimilarityParameters(
                sameIntervalScore = 1.0f,
                closeIntervalScore = 0.85f,
                similarIntervalScore = 0.5f,
                emptyPhrasesPenalty = 0.40f,
                emptyRhythmPenalty = 0.30f
            ),

            audio = AudioProcessingParameters(),

            scaling = ScoreScalingParameters(
                incredibleFeedbackThreshold = 92,
                greatJobFeedbackThreshold = 75,
                goodEffortFeedbackThreshold = 55,
                //reversePerfectScoreAdjustment = 0.90f
            ),

            garbage = GarbageDetectionParameters(
                enableGarbageDetection = true,
                mfccVarianceThreshold = 0.50f,
                pitchMonotoneThreshold = 18f,
                pitchOscillationRate = 0.30f,
                spectralEntropyThreshold = 0.75f,
                zcrMinThreshold = 0.028f,
                zcrMaxThreshold = 0.12f,
                silenceRatioMin = 0.18f,
                garbageScoreMax = 10
            )
        )
    }

    // -------------------------------------------------------------------------
    // List of difficulty presets (only the 3 supported levels)
    // -------------------------------------------------------------------------
    fun getAllSingingDifficultyPresets(): List<Pair<DifficultyLevel, () -> Presets>> {
        return listOf(
            DifficultyLevel.EASY to ::easyModeSinging,
            DifficultyLevel.NORMAL to ::normalModeSinging,
            DifficultyLevel.HARD to ::hardModeSinging
        )
    }

    //helper function This allows the orchestrator to call:
    //SingingScoringModels.presetFor(difficulty)
    fun presetFor(level: DifficultyLevel): Presets {
        return when (level) {
            DifficultyLevel.EASY -> easyModeSinging() // <--- CORRECTED
            DifficultyLevel.NORMAL -> normalModeSinging() // <--- CORRECTED
            DifficultyLevel.HARD -> hardModeSinging() // <--- CORRECTED
        }
    }
}