package com.example.reversey.scoring

/**
 * 🎵 SINGING SCORING MODELS
 *
 * Singing-optimized parameter sets for all difficulty levels.
 * Tuned for musical challenges where:
 * - Melodic accuracy is paramount
 * - Lower pitch tolerance (singing requires precise notes)
 * - Higher melodic and musical requirements
 * - Strict garbage detection for non-musical attempts
 *
 * GLUTE Principle: Same structure as base ScoringModels but optimized for singing characteristics
 */
object SingingScoringModels {

    // Easy Mode Singing - Beginner-friendly but still musical
    fun easyModeSinging(): Presets {
        return Presets(
            difficulty = DifficultyLevel.EASY,
            scoring = ScoringParameters(
                pitchWeight = 0.85f,              // SINGING: Increased from 0.75f - melody is key
                mfccWeight = 0.15f,               // SINGING: Reduced from 0.25f - pitch more important
                pitchTolerance = 25f,             // SINGING: Reduced from 35f - more melodic precision
                minScoreThreshold = 0.15f,        // SINGING: Increased from 0.12f - higher musical standards
                perfectScoreThreshold = 0.85f,    // SINGING: Increased from 0.8f - musical perfection harder
                scoreCurve = 2.0f                 // SINGING: Reduced from 2.5f - less generous, more musical standards
            ),
            content = ContentDetectionParameters(
                contentDetectionBestThreshold = 0.30f,    // SINGING: Increased from 0.25f - clearer melody required
                contentDetectionAvgThreshold = 0.20f,     // SINGING: Increased from 0.15f - better average needed
                rightContentFlatPenalty = 0.25f,          // SINGING: Increased from 0.1f - flat singing penalized
                rightContentDifferentMelodyPenalty = 0.15f, // SINGING: Increased from 0.05f - melody deviation matters
                wrongContentStandardPenalty = 0.20f       // SINGING: Reduced from 0.25f - wrong words less critical if melody good
            ),
            melodic = MelodicAnalysisParameters(
                monotoneDetectionThreshold = 2.5f,        // SINGING: ALIGNED - expect variation but not extreme
                flatSpeechThreshold = 0.5f,               // SINGING: ALIGNED - low tolerance for flatness
                monotonePenalty = 0.3f,                   // SINGING: Moderate penalty - some monotone acceptable in easy
                melodicRangeWeight = 0.35f,               // SINGING: ALIGNED - range important but not dominant
                melodicTransitionWeight = 0.4f,           // SINGING: ALIGNED - smooth transitions rewarded
                melodicVarianceWeight = 0.25f             // SINGING: Standard - overall variation matters
            ),
            musical = MusicalSimilarityParameters(
                sameIntervalScore = 1.0f,                 // SINGING: Increased from 0.8f - exact intervals rewarded
                closeIntervalScore = 0.85f,               // SINGING: Increased from 0.7f - close intervals good
                similarIntervalScore = 0.6f,              // SINGING: Increased - similar intervals acceptable
                emptyPhrasesPenalty = 0.4f,               // SINGING: Increased from 0.1f - phrase structure matters
                emptyRhythmPenalty = 0.3f                 // SINGING: Increased from 0.05f - rhythm important
            ),
            audio = AudioProcessingParameters(),
            scaling = ScoreScalingParameters(
                incredibleFeedbackThreshold = 85,         // SINGING: Increased from 75 - higher musical standards
                greatJobFeedbackThreshold = 65,           // SINGING: Increased from 55 - good singing takes skill
                goodEffortFeedbackThreshold = 45,         // SINGING: Increased from 35 - effort recognition appropriate
                reversePerfectScoreAdjustment = 0.92f     // SINGING: Reduced from 1.05f - reverse singing is harder
            ),
            garbage = GarbageDetectionParameters(
                enableGarbageDetection = true,
                mfccVarianceThreshold = 0.35f,            // SINGING: Increased from 0.2f - expect more vocal variety
                pitchMonotoneThreshold = 12f,             // SINGING: Increased from 8f - stricter monotone detection
                pitchOscillationRate = 0.4f,              // SINGING: Reduced from 0.6f - less tolerance for oscillation
                spectralEntropyThreshold = 0.6f,          // SINGING: Increased from 0.4f - expect richer spectral content
                zcrMinThreshold = 0.02f,                  // SINGING: Increased from 0.015f - clearer vocal boundaries
                zcrMaxThreshold = 0.18f,                  // SINGING: Reduced from 0.25f - controlled vocal quality
                silenceRatioMin = 0.12f,                  // SINGING: Increased from 0.08f - sustained notes expected
                garbageScoreMax = 15                      // SINGING: Reduced from 20 - stricter garbage standards
            )
        )
    }

    // Normal Mode Singing - Balanced musical expectations
    fun normalModeSinging(): Presets {
        return Presets(
            difficulty = DifficultyLevel.NORMAL,
            scoring = ScoringParameters(
                pitchWeight = 0.90f,              // SINGING: Increased from 0.85f - pitch dominance
                mfccWeight = 0.10f,               // SINGING: Reduced from 0.15f - melody over voice character
                pitchTolerance = 20f,             // SINGING: Reduced from 25f - tighter pitch requirements
                minScoreThreshold = 0.22f,        // SINGING: Increased from 0.18f - higher musical floor
                perfectScoreThreshold = 0.92f,    // SINGING: Increased from 0.9f - musical perfection demanding
                scoreCurve = 1.8f                 // SINGING: Reduced from 2.5f - more demanding curve
            ),
            content = ContentDetectionParameters(
                contentDetectionBestThreshold = 0.45f,    // SINGING: Increased from 0.4f - clearer melody lines
                contentDetectionAvgThreshold = 0.35f,     // SINGING: Increased from 0.3f - consistent quality needed
                rightContentFlatPenalty = 0.30f,          // SINGING: Increased from 0.15f - flat singing penalized more
                rightContentDifferentMelodyPenalty = 0.20f, // SINGING: Increased from 0.08f - melody accuracy important
                wrongContentStandardPenalty = 0.40f       // SINGING: Reduced from 0.6f - melody can compensate for words
            ),
            melodic = MelodicAnalysisParameters(
                monotoneDetectionThreshold = 3.0f,        // SINGING: ALIGNED - expect melodic variation
                flatSpeechThreshold = 0.4f,               // SINGING: ALIGNED - low tolerance for flat delivery
                monotonePenalty = 0.4f,                   // SINGING: ALIGNED - penalty for monotone in singing
                melodicRangeWeight = 0.35f,               // SINGING: ALIGNED - range important for melody
                melodicTransitionWeight = 0.4f,           // SINGING: ALIGNED - melodic contour important
                melodicVarianceWeight = 0.25f             // SINGING: ALIGNED - overall variation important
            ),
            musical = MusicalSimilarityParameters(
                sameIntervalScore = 1.0f,                 // SINGING: Perfect interval matching rewarded
                closeIntervalScore = 0.85f,               // SINGING: Close intervals good
                similarIntervalScore = 0.6f,              // SINGING: Similar intervals acceptable
                emptyPhrasesPenalty = 0.35f,              // SINGING: Increased from 0.15f - structure matters
                emptyRhythmPenalty = 0.25f                // SINGING: Increased from 0.10f - rhythm important
            ),
            audio = AudioProcessingParameters(),
            scaling = ScoreScalingParameters(
                incredibleFeedbackThreshold = 88,         // SINGING: Increased from 80 - high musical standards
                greatJobFeedbackThreshold = 70,           // SINGING: Increased from 60 - good singing is skilled
                goodEffortFeedbackThreshold = 50,         // SINGING: Increased from 40 - effort threshold higher
                reversePerfectScoreAdjustment = 0.95f     // SINGING: Reduced from 1.0f - reverse singing challenging
            ),
            garbage = GarbageDetectionParameters(
                enableGarbageDetection = true,
                mfccVarianceThreshold = 0.45f,            // SINGING: Increased from 0.4f - expect vocal richness
                pitchMonotoneThreshold = 15f,             // SINGING: Increased from 1f (!!) - much stricter
                pitchOscillationRate = 0.35f,             // SINGING: Reduced from 0.5f - controlled vocal delivery
                spectralEntropyThreshold = 0.70f,         // SINGING: Increased from 0.6f - richer harmonic content
                zcrMinThreshold = 0.025f,                 // SINGING: Increased from 0.02f - clearer articulation
                zcrMaxThreshold = 0.15f,                  // SINGING: Reduced from 0.2f - controlled delivery
                silenceRatioMin = 0.15f,                  // SINGING: Increased from 0.1f - sustained musical phrases
                garbageScoreMax = 12                      // SINGING: Increased from 10 but controlled
            )
        )
    }

    // Hard Mode Singing - Demanding musical performance
    fun hardModeSinging(): Presets {
        return Presets(
            difficulty = DifficultyLevel.HARD,
            scoring = ScoringParameters(
                pitchWeight = 0.93f,              // SINGING: Increased from 0.9f - pitch precision critical
                mfccWeight = 0.07f,               // SINGING: Reduced from 0.1f - melody dominates
                pitchTolerance = 12f,             // SINGING: Reduced from 15f - tight pitch control required
                minScoreThreshold = 0.30f,        // SINGING: Increased from 0.25f - high musical floor
                perfectScoreThreshold = 0.90f,    // SINGING: Increased from 0.85f - excellence required
                scoreCurve = 1.5f                 // SINGING: Reduced from 2.5f - demanding musical standards
            ),
            content = ContentDetectionParameters(
                contentDetectionBestThreshold = 0.55f,    // SINGING: Increased from 0.45f - clear musical lines
                contentDetectionAvgThreshold = 0.40f,     // SINGING: Increased from 0.30f - consistent excellence
                rightContentFlatPenalty = 0.40f,          // SINGING: Increased from 0.25f - flatness heavily penalized
                rightContentDifferentMelodyPenalty = 0.25f, // SINGING: Increased from 0.15f - melody accuracy crucial
                wrongContentStandardPenalty = 0.45f       // SINGING: Reduced from 0.55f - great melody can offset words
            ),
            melodic = MelodicAnalysisParameters(
                monotoneDetectionThreshold = 5.0f,        // SINGING: Increased from 2.5f - expect rich variation
                flatSpeechThreshold = 0.2f,               // SINGING: Reduced - very low tolerance for flatness
                monotonePenalty = 0.6f,                   // SINGING: Increased - major penalty for monotone
                melodicRangeWeight = 0.45f,               // SINGING: Increased - range expansion expected
                melodicTransitionWeight = 0.35f,          // SINGING: Standard but important
                melodicVarianceWeight = 0.20f             // SINGING: Reduced - focus on range and transitions
            ),
            musical = MusicalSimilarityParameters(
                sameIntervalScore = 1.0f,                 // SINGING: Perfect intervals essential
                closeIntervalScore = 0.85f,               // SINGING: Close intervals acceptable but not perfect
                similarIntervalScore = 0.5f,              // SINGING: Similar intervals marginal
                emptyPhrasesPenalty = 0.40f,              // SINGING: Increased from 0.20f - structure critical
                emptyRhythmPenalty = 0.30f                // SINGING: Increased from 0.15f - rhythm essential
            ),
            audio = AudioProcessingParameters(),
            scaling = ScoreScalingParameters(
                incredibleFeedbackThreshold = 92,         // SINGING: Increased from 85 - very high standards
                greatJobFeedbackThreshold = 75,           // SINGING: Increased from 65 - skilled performance needed
                goodEffortFeedbackThreshold = 55,         // SINGING: Increased from 45 - effort bar higher
                reversePerfectScoreAdjustment = 0.90f     // SINGING: Reduced from 0.98f - reverse singing very challenging
            ),
            garbage = GarbageDetectionParameters(
                enableGarbageDetection = true,
                mfccVarianceThreshold = 0.50f,            // SINGING: Increased from 0.45f - expect rich vocal textures
                pitchMonotoneThreshold = 18f,             // SINGING: Increased from 14f - strict monotone rejection
                pitchOscillationRate = 0.30f,             // SINGING: Reduced from 0.42f - controlled, precise delivery
                spectralEntropyThreshold = 0.75f,         // SINGING: Increased from 0.65f - rich harmonic structure
                zcrMinThreshold = 0.028f,                 // SINGING: Increased from 0.028f - clear articulation
                zcrMaxThreshold = 0.12f,                  // SINGING: Reduced from 0.16f - controlled vocal quality
                silenceRatioMin = 0.18f,                  // SINGING: Increased from 0.14f - sustained musical delivery
                garbageScoreMax = 10                      // SINGING: Reduced from 15 - stricter standards
            )
        )
    }

    // Expert Mode Singing - Advanced musical performance
    fun expertModeSinging(): Presets {
        return Presets(
            difficulty = DifficultyLevel.EXPERT,
            scoring = ScoringParameters(
                pitchWeight = 0.96f,              // SINGING: Increased from 0.95f - near-perfect pitch required
                mfccWeight = 0.04f,               // SINGING: Reduced from 0.05f - pitch absolutely dominant
                pitchTolerance = 8f,              // SINGING: Same as original - very tight pitch control
                minScoreThreshold = 0.35f,        // SINGING: Increased from 0.30f - expert floor
                perfectScoreThreshold = 0.95f,    // SINGING: Increased from 0.92f - near-perfection required
                scoreCurve = 1.2f                 // SINGING: Reduced from 2.5f - very demanding
            ),
            content = ContentDetectionParameters(
                contentDetectionBestThreshold = 0.60f,    // SINGING: Increased from 0.50f - excellent musical clarity
                contentDetectionAvgThreshold = 0.45f,     // SINGING: Increased from 0.35f - sustained excellence
                rightContentFlatPenalty = 0.50f,          // SINGING: Increased from 0.35f - flatness major flaw
                rightContentDifferentMelodyPenalty = 0.35f, // SINGING: Increased from 0.25f - melody precision crucial
                wrongContentStandardPenalty = 0.60f       // SINGING: Reduced from 0.75f - exceptional melody can compensate
            ),
            melodic = MelodicAnalysisParameters(
                monotoneDetectionThreshold = 6.0f,        // SINGING: Increased from 3.0f - expert variation expected
                flatSpeechThreshold = 0.15f,              // SINGING: Reduced from 0.6f - minimal tolerance for flatness
                monotonePenalty = 0.7f,                   // SINGING: Increased from 0.4f - severe monotone penalty
                melodicRangeWeight = 0.45f,               // SINGING: High range expectations
                melodicTransitionWeight = 0.35f,          // SINGING: Smooth transitions required
                melodicVarianceWeight = 0.20f             // SINGING: Focus on range and transitions over raw variance
            ),
            musical = MusicalSimilarityParameters(
                sameIntervalScore = 1.0f,                 // SINGING: Perfect intervals mandatory
                closeIntervalScore = 0.80f,               // SINGING: Reduced from 0.85f - closer to perfect expected
                similarIntervalScore = 0.4f,              // SINGING: Reduced from 0.5f - less tolerance
                emptyPhrasesPenalty = 0.45f,              // SINGING: Increased from 0.25f - structure essential
                emptyRhythmPenalty = 0.35f                // SINGING: Increased from 0.18f - rhythm mastery required
            ),
            audio = AudioProcessingParameters(),
            scaling = ScoreScalingParameters(
                incredibleFeedbackThreshold = 95,         // SINGING: Same as original - expert standards
                greatJobFeedbackThreshold = 80,           // SINGING: Increased from 70 - higher threshold
                goodEffortFeedbackThreshold = 60,         // SINGING: Increased from 50 - expert effort recognition
                reversePerfectScoreAdjustment = 0.88f     // SINGING: Reduced from 0.96f - reverse singing at expert level very hard
            ),
            garbage = GarbageDetectionParameters(
                enableGarbageDetection = true,
                mfccVarianceThreshold = 0.55f,            // SINGING: Increased from 0.45f - expect sophisticated vocals
                pitchMonotoneThreshold = 20f,             // SINGING: Increased from 14f - expert-level variation required
                pitchOscillationRate = 0.25f,             // SINGING: Reduced from 0.42f - very controlled delivery
                spectralEntropyThreshold = 0.80f,         // SINGING: Increased from 0.65f - sophisticated harmonic content
                zcrMinThreshold = 0.030f,                 // SINGING: Increased from 0.028f - precise articulation
                zcrMaxThreshold = 0.10f,                  // SINGING: Reduced from 0.16f - masterful control
                silenceRatioMin = 0.20f,                  // SINGING: Increased from 0.14f - sustained musical mastery
                garbageScoreMax = 8                       // SINGING: Reduced from 15 - expert standards
            )
        )
    }

    // Master Mode Singing - Virtuoso-level musical mastery
    fun masterModeSinging(): Presets {
        return Presets(
            difficulty = DifficultyLevel.MASTER,
            scoring = ScoringParameters(
                pitchWeight = 0.98f,              // SINGING: Same as original - absolute pitch mastery
                mfccWeight = 0.02f,               // SINGING: Same as original - pitch is everything
                pitchTolerance = 3f,              // SINGING: Reduced from 5f - virtuoso precision required
                minScoreThreshold = 0.45f,        // SINGING: Increased from 0.42f - master-level floor
                perfectScoreThreshold = 0.98f,    // SINGING: Increased from 0.50f (!) - near-impossible perfection
                scoreCurve = 1.0f                 // SINGING: Reduced from 2.5f - completely unforgiving
            ),
            content = ContentDetectionParameters(
                contentDetectionBestThreshold = 0.70f,    // SINGING: Increased from 0.65f - flawless musical clarity
                contentDetectionAvgThreshold = 0.55f,     // SINGING: Increased from 0.50f - sustained perfection
                rightContentFlatPenalty = 0.60f,          // SINGING: Increased from 0.45f - any flatness is failure
                rightContentDifferentMelodyPenalty = 0.45f, // SINGING: Increased from 0.35f - melody perfection required
                wrongContentStandardPenalty = 0.70f       // SINGING: Reduced from 0.75f - perfect melody might save poor words
            ),
            melodic = MelodicAnalysisParameters(
                monotoneDetectionThreshold = 6.0f,        // SINGING: High expectation but achievable for classified singing
                flatSpeechThreshold = 0.1f,               // SINGING: Very low tolerance but not impossible
                monotonePenalty = 0.8f,                   // SINGING: High penalty but not completely disqualifying
                melodicRangeWeight = 0.4f,                // SINGING: High range expectations
                melodicTransitionWeight = 0.35f,          // SINGING: Smooth transitions critical
                melodicVarianceWeight = 0.25f             // SINGING: Rich overall variation required
            ),
            musical = MusicalSimilarityParameters(
                sameIntervalScore = 1.0f,                 // SINGING: Perfect intervals are minimum standard
                closeIntervalScore = 0.75f,               // SINGING: Reduced from 0.9f - perfection expected
                similarIntervalScore = 0.3f,              // SINGING: Reduced - very low tolerance
                emptyPhrasesPenalty = 0.50f,              // SINGING: Increased from 0.30f - structure must be flawless
                emptyRhythmPenalty = 0.40f                // SINGING: Increased from 0.20f - rhythmic mastery required
            ),
            audio = AudioProcessingParameters(),
            scaling = ScoreScalingParameters(
                incredibleFeedbackThreshold = 98,         // SINGING: Same as original - virtuoso standards
                greatJobFeedbackThreshold = 85,           // SINGING: Reduced from 90 - even "great" is rare
                goodEffortFeedbackThreshold = 70,         // SINGING: Reduced from 80 - effort recognition at master level
                reverseMinScoreAdjustment = 0.85f,        // SINGING: Reduced from 1.0f - reverse at master is brutally hard
                reversePerfectScoreAdjustment = 0.90f,    // SINGING: Reduced from 1.0f - reverse perfection nearly impossible
                reverseCurveAdjustment = 0.95f            // SINGING: Reduced from 1.0f - less generous than forward
            ),
            garbage = GarbageDetectionParameters(
                enableGarbageDetection = true,
                mfccVarianceThreshold = 0.65f,            // SINGING: Increased from 0.55f - expect vocal virtuosity
                pitchMonotoneThreshold = 25f,             // SINGING: Increased from 16f - master-level variation required
                pitchOscillationRate = 0.20f,             // SINGING: Reduced from 0.37f - absolute control required
                spectralEntropyThreshold = 0.85f,         // SINGING: Increased from 0.75f - sophisticated harmonic mastery
                zcrMinThreshold = 0.035f,                 // SINGING: Increased from 0.033f - perfect articulation
                zcrMaxThreshold = 0.08f,                  // SINGING: Reduced from 0.13f - complete vocal control
                silenceRatioMin = 0.22f,                  // SINGING: Increased from 0.17f - sustained musical perfection
                garbageScoreMax = 5                       // SINGING: Reduced from 10 - master standards, virtually no garbage tolerance
            )
        )
    }

    // Helper function to get all singing difficulty presets in order
    fun getAllSingingDifficultyPresets(): List<Pair<DifficultyLevel, () -> Presets>> {
        return listOf(
            DifficultyLevel.EASY to ::easyModeSinging,
            DifficultyLevel.NORMAL to ::normalModeSinging,
            DifficultyLevel.HARD to ::hardModeSinging,
            DifficultyLevel.EXPERT to ::expertModeSinging,
            DifficultyLevel.MASTER to ::masterModeSinging
        )
    }
}