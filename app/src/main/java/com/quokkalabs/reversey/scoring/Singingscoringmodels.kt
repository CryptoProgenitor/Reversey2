package com.quokkalabs.reversey.scoring

/**
 * 🎵 SINGING SCORING MODELS — PHASE 2.2 CALIBRATION
 *
 * Recalibrated for DCT + Cosine Distance (v23.0.0)
 * Hard Mode pitch fix (v23.0.1) — Ed subjective calibration
 * Hard Mode Round 2 loosening (v23.0.2) — Linear gradient preservation
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
 * PHASE 2 CHANGES:
 *    ✔ minScoreThreshold raised +15% (floor raised for better MFCC signal)
 *    ✔ perfectScoreThreshold raised +3% (capped near 1.0)
 *    ✔ contentDetectionBestThreshold raised +5%
 *    ✔ contentDetectionAvgThreshold raised +5%
 *    ✔ mfccVarianceThreshold raised +20% (garbage detection recalibration)
 *    ✔ dtwNormalizationFactor now defaults to 1.0 in ScoringCommonModels
 *
 * PHASE 2.1 CHANGES (Hard Mode Pitch Fix):
 *    ✔ Hard pitchWeight 0.95→0.88 (less pitch dominance)
 *    ✔ Hard mfccWeight 0.05→0.12 (more MFCC contribution)
 *    ✔ Hard pitchTolerance 12→18 (less brutal on pitch shifts)
 *    ✔ Hard scoreCurve 0.25→0.5 (square curve, not 4th power)
 *
 * PHASE 2.2 CHANGES (Hard Mode Round 2 Loosening):
 *    ✔ Hard pitchTolerance 18→22 (more pitch forgiveness)
 *    ✔ Hard scoreCurve 0.5→0.65 (gentler curve, preserves E>N>H gradient)
 *
 * PHASE 2.3 CHANGES (70/30 Weighting — "Singing is singing"):
 *    ✔ All difficulties: pitchWeight 0.90→0.70 (pitch still dominant)
 *    ✔ All difficulties: mfccWeight 0.10→0.30 (words/rhythm matter too)
 *    ✔ Philosophy: Amateurs shouldn't be murdered for being off-key
 *
 * PHASE 2.4 CHANGES (REVERSE Direction Sensitivity):
 *    ✔ REVERSE challenges use different formula: interval×0.5 + pitch×0.4 + mfcc×0.1
 *    ✔ Interval accuracy promoted from bonus to core metric for REVERSE
 *    ✔ MFCC demoted (not direction-sensitive - same voice = same spectral fingerprint)
 *    ✔ FORWARD scoring unchanged (validated in Phase 2.3)
 */
object SingingScoringModels {

    // -------------------------------------------------------------------------
    // 🎵 EASY MODE — The "Feel Good" Mode
    // -------------------------------------------------------------------------
    fun easyModeSinging(): Presets {
        return Presets(
            difficulty = DifficultyLevel.EASY,

            scoring = ScoringParameters(
                pitchWeight = 0.70f,
                mfccWeight = 0.30f,
                pitchTolerance = 25f,
                minScoreThreshold = 0.17f,             // ← Was 0.15f (+15%)
                perfectScoreThreshold = 0.95f,         // ← Was 0.92f (+3%)
                scoreCurve = 1.8f
            ),

            content = ContentDetectionParameters(
                contentDetectionBestThreshold = 0.73f, // ← Was 0.70f (+5%)
                contentDetectionAvgThreshold = 0.53f,  // ← Was 0.50f (+5%)
                reverseHandicap = 0.20f,
                rightContentFlatPenalty = 0.20f,
                rightContentDifferentMelodyPenalty = 0.10f,
                wrongContentStandardPenalty = 0.20f
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
                mfccVarianceThreshold = 0.42f,         // ← Was 0.35f (+20%)
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
                pitchWeight = 0.70f,
                mfccWeight = 0.30f,
                pitchTolerance = 20f,
                minScoreThreshold = 0.25f,             // ← Was 0.22f (+15%)
                perfectScoreThreshold = 0.99f,         // ← Was 0.98f (+1%, capped)
                scoreCurve = 1.0f
            ),

            content = ContentDetectionParameters(
                contentDetectionBestThreshold = 0.79f, // ← Was 0.75f (+5%)
                contentDetectionAvgThreshold = 0.58f,  // ← Was 0.55f (+5%)
                reverseHandicap = 0.15f,
                rightContentFlatPenalty = 0.30f,
                rightContentDifferentMelodyPenalty = 0.20f,
                wrongContentStandardPenalty = 0.20f
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
                mfccVarianceThreshold = 0.54f,         // ← Was 0.45f (+20%)
                pitchMonotoneThreshold = 15f,
                spectralEntropyThreshold = 0.70f,
                garbageScoreMax = 12
            )
        )
    }

    // -------------------------------------------------------------------------
    // 🎵 HARD MODE — The Punisher (Recalibrated Round 2)
    // -------------------------------------------------------------------------
    fun hardModeSinging(): Presets {
        return Presets(
            difficulty = DifficultyLevel.HARD,

            scoring = ScoringParameters(
                pitchWeight = 0.70f,
                mfccWeight = 0.30f,                    // ← Was 0.05f (more MFCC contribution)
                pitchTolerance = 22f,                  // ← Was 18f → 22f (Round 2 loosening)
                minScoreThreshold = 0.35f,             // ← Was 0.30f (+15%)
                perfectScoreThreshold = 0.99f,         // ← Was 0.99f (already max)
                scoreCurve = 0.65f                     // ← Was 0.5f → 0.65f (gentler curve)
            ),

            content = ContentDetectionParameters(
                contentDetectionBestThreshold = 0.82f, // ← Was 0.78f (+5%)
                contentDetectionAvgThreshold = 0.63f,  // ← Was 0.60f (+5%)
                reverseHandicap = 0.08f,
                rightContentFlatPenalty = 0.40f,
                rightContentDifferentMelodyPenalty = 0.25f,
                wrongContentStandardPenalty = 0.10f
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
                mfccVarianceThreshold = 0.60f,         // ← Was 0.50f (+20%)
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