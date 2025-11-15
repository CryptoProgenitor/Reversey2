package com.example.reversey.scoring

/**
 * 🎤 SPEECH SCORING MODELS
 *
 * Speech-optimized parameter sets for all difficulty levels.
 * Tuned for spoken word challenges where:
 * - Content accuracy matters more than melodic precision
 * - Higher pitch tolerance (speech is less melodic than singing)
 * - More forgiving melodic requirements
 * - Speech-appropriate garbage detection filters
 *
 * GLUTE Principle: Same structure as base ScoringModels but optimized for speech characteristics
 */
object SpeechScoringModels {

    // Easy Mode Speech - Maximum forgiveness for beginners
    fun easyModeSpeech(): Presets {
        return Presets(
            difficulty = DifficultyLevel.EASY,
            scoring = ScoringParameters(
                pitchWeight = 0.65f,              // SPEECH: Reduced from 0.75f - less pitch focus
                mfccWeight = 0.35f,               // SPEECH: Increased from 0.25f - voice characteristics matter more
                pitchTolerance = 50f,             // SPEECH: Increased from 35f - much more forgiving
                minScoreThreshold = 0.08f,        // SPEECH: Reduced from 0.12f - easier to get points
                perfectScoreThreshold = 0.75f,    // SPEECH: Reduced from 0.8f - achievable perfection
                scoreCurve = 3.0f                 // SPEECH: Increased from 2.5f - more generous scoring
            ),
            content = ContentDetectionParameters(
                contentDetectionBestThreshold = 0.20f,    // SPEECH: Reduced - easier content detection
                contentDetectionAvgThreshold = 0.10f,     // SPEECH: Reduced - more forgiving average
                rightContentFlatPenalty = 0.05f,          // SPEECH: Reduced - monotone OK for speech
                rightContentDifferentMelodyPenalty = 0.02f, // SPEECH: Minimal - melody variation not required
                wrongContentStandardPenalty = 0.35f       // SPEECH: Increased from 0.25f - wrong words hurt more
            ),
            melodic = MelodicAnalysisParameters(
                monotoneDetectionThreshold = 0.5f,        // SPEECH: ALIGNED - matches detector low-stability threshold
                flatSpeechThreshold = 1.5f,               // SPEECH: Very forgiving - monotone speech acceptable
                monotonePenalty = 0.05f,                  // SPEECH: Minimal penalty - near-monotone OK
                melodicRangeWeight = 0.05f,               // SPEECH: Minimal - range unimportant for speech
                melodicTransitionWeight = 0.05f,          // SPEECH: Minimal - transitions unimportant for speech
                melodicVarianceWeight = 0.9f              // SPEECH: Focus on any variation at all
            ),
            musical = MusicalSimilarityParameters(
                sameIntervalScore = 0.8f,                 // SPEECH: Reduced - exact intervals less critical
                closeIntervalScore = 0.7f,                // SPEECH: Slightly reduced but still good
                emptyPhrasesPenalty = 0.1f,               // SPEECH: Reduced - phrase structure less critical
                emptyRhythmPenalty = 0.05f                // SPEECH: Reduced - rhythm less critical
            ),
            audio = AudioProcessingParameters(),
            scaling = ScoreScalingParameters(
                incredibleFeedbackThreshold = 75,         // SPEECH: Reduced - easier to get "incredible"
                greatJobFeedbackThreshold = 55,           // SPEECH: Reduced - encouraging feedback sooner
                goodEffortFeedbackThreshold = 35,         // SPEECH: Reduced - recognition for basic effort
                reversePerfectScoreAdjustment = 1.05f     // SPEECH: Slightly easier reverse challenges
            ),
            garbage = GarbageDetectionParameters(
                enableGarbageDetection = true,
                mfccVarianceThreshold = 0.10f,            // SPEECH: ALIGNED - very low threshold for speech patterns
                pitchMonotoneThreshold = 3f,              // SPEECH: ALIGNED - matches detector monotone acceptance
                pitchOscillationRate = 1.0f,              // SPEECH: Very forgiving - natural speech patterns
                spectralEntropyThreshold = 0.25f,         // SPEECH: ALIGNED - simple speech patterns OK
                zcrMinThreshold = 0.005f,                 // SPEECH: Very low - accept quiet speech
                zcrMaxThreshold = 0.45f,                  // SPEECH: High - accept varied speech sounds
                silenceRatioMin = 0.02f,                  // SPEECH: Very low - pauses in speech OK
                garbageScoreMax = 30                      // SPEECH: High - very lenient for classified speech
            )
        )
    }

    // Normal Mode Speech - Balanced speech scoring
    fun normalModeSpeech(): Presets {
        return Presets(
            difficulty = DifficultyLevel.NORMAL,
            scoring = ScoringParameters(
                pitchWeight = 0.70f,              // SPEECH: Reduced from 0.85f
                mfccWeight = 0.30f,               // SPEECH: Increased from 0.15f
                pitchTolerance = 40f,             // SPEECH: Increased from 25f
                minScoreThreshold = 0.12f,        // SPEECH: Reduced from 0.18f
                perfectScoreThreshold = 0.85f,    // SPEECH: Reduced from 0.9f
                scoreCurve = 2.8f                 // SPEECH: Increased from 2.5f
            ),
            content = ContentDetectionParameters(
                contentDetectionBestThreshold = 0.30f,    // SPEECH: Reduced from 0.4f
                contentDetectionAvgThreshold = 0.20f,     // SPEECH: Reduced from 0.3f
                rightContentFlatPenalty = 0.08f,          // SPEECH: Reduced from 0.15f
                rightContentDifferentMelodyPenalty = 0.04f, // SPEECH: Reduced from 0.08f
                wrongContentStandardPenalty = 0.50f       // SPEECH: Reduced from 0.6f but still significant
            ),
            melodic = MelodicAnalysisParameters(
                monotoneDetectionThreshold = 0.8f,        // SPEECH: ALIGNED - low threshold for speech variance
                flatSpeechThreshold = 1.0f,               // SPEECH: Forgiving - natural speech patterns
                monotonePenalty = 0.10f,                  // SPEECH: Low penalty for monotone speech
                melodicRangeWeight = 0.10f,               // SPEECH: Minimal range importance
                melodicTransitionWeight = 0.10f,          // SPEECH: Minimal transition importance
                melodicVarianceWeight = 0.80f             // SPEECH: Focus on any variation
            ),
            musical = MusicalSimilarityParameters(
                sameIntervalScore = 0.85f,                // SPEECH: Slightly reduced
                closeIntervalScore = 0.75f,               // SPEECH: Good score for close matches
                emptyPhrasesPenalty = 0.15f,              // SPEECH: Reduced phrase penalty
                emptyRhythmPenalty = 0.10f                // SPEECH: Reduced rhythm penalty
            ),
            audio = AudioProcessingParameters(),
            scaling = ScoreScalingParameters(
                incredibleFeedbackThreshold = 80,         // SPEECH: Reduced from 90
                greatJobFeedbackThreshold = 60,           // SPEECH: Reduced from 75
                goodEffortFeedbackThreshold = 40,         // SPEECH: Reduced from 50
                reversePerfectScoreAdjustment = 1.0f      // SPEECH: Standard reverse difficulty
            ),
            garbage = GarbageDetectionParameters(
                enableGarbageDetection = true,
                mfccVarianceThreshold = 0.25f,            // SPEECH: Reduced from 0.4f
                pitchMonotoneThreshold = 8f,              // SPEECH: Reduced from 1f - balance monotone detection
                pitchOscillationRate = 0.7f,              // SPEECH: Increased from 0.5f
                spectralEntropyThreshold = 0.45f,         // SPEECH: Reduced from 0.6f
                zcrMinThreshold = 0.015f,                 // SPEECH: Reduced from 0.02f
                zcrMaxThreshold = 0.30f,                  // SPEECH: Increased from 0.2f
                silenceRatioMin = 0.08f,                  // SPEECH: Reduced from 0.1f
                garbageScoreMax = 15                      // SPEECH: Increased from 10
            )
        )
    }

    // Hard Mode Speech - More demanding but still speech-appropriate
    fun hardModeSpeech(): Presets {
        return Presets(
            difficulty = DifficultyLevel.HARD,
            scoring = ScoringParameters(
                pitchWeight = 0.75f,              // SPEECH: Reduced from 0.9f
                mfccWeight = 0.25f,               // SPEECH: Increased from 0.1f
                pitchTolerance = 30f,             // SPEECH: Increased from 15f - still more forgiving
                minScoreThreshold = 0.18f,        // SPEECH: Reduced from 0.25f
                perfectScoreThreshold = 0.80f,    // SPEECH: Reduced from 0.85f
                scoreCurve = 2.3f                 // SPEECH: Reduced from 2.5f - slightly less generous
            ),
            content = ContentDetectionParameters(
                contentDetectionBestThreshold = 0.35f,    // SPEECH: Reduced from 0.45f
                contentDetectionAvgThreshold = 0.25f,     // SPEECH: Reduced from 0.30f
                rightContentFlatPenalty = 0.15f,          // SPEECH: Reduced from 0.25f
                rightContentDifferentMelodyPenalty = 0.08f, // SPEECH: Reduced from 0.15f
                wrongContentStandardPenalty = 0.65f       // SPEECH: Increased from 0.55f - accuracy important
            ),
            melodic = MelodicAnalysisParameters(
                monotoneDetectionThreshold = 1.5f,        // SPEECH: Reduced from 2.5f
                flatSpeechThreshold = 0.8f,               // SPEECH: Increased - still forgiving
                monotonePenalty = 0.20f,                  // SPEECH: Reduced penalty
                melodicRangeWeight = 0.20f,               // SPEECH: Some range expectation
                melodicTransitionWeight = 0.20f,          // SPEECH: Some transition expectation
                melodicVarianceWeight = 0.60f             // SPEECH: Focus on variation
            ),
            musical = MusicalSimilarityParameters(
                sameIntervalScore = 0.90f,                // SPEECH: Closer to original but still reduced
                closeIntervalScore = 0.80f,               // SPEECH: Good scoring for close matches
                emptyPhrasesPenalty = 0.20f,              // SPEECH: Moderate phrase penalty
                emptyRhythmPenalty = 0.15f                // SPEECH: Moderate rhythm penalty
            ),
            audio = AudioProcessingParameters(),
            scaling = ScoreScalingParameters(
                incredibleFeedbackThreshold = 85,         // SPEECH: Reduced from 95
                greatJobFeedbackThreshold = 65,           // SPEECH: Reduced from 85
                goodEffortFeedbackThreshold = 45,         // SPEECH: Reduced from 70
                reversePerfectScoreAdjustment = 0.98f     // SPEECH: Slightly easier reverse challenges
            ),
            garbage = GarbageDetectionParameters(
                enableGarbageDetection = true,
                mfccVarianceThreshold = 0.35f,            // SPEECH: Reduced from 0.45f
                pitchMonotoneThreshold = 10f,             // SPEECH: Reduced from 14f
                pitchOscillationRate = 0.6f,              // SPEECH: Increased from 0.42f
                spectralEntropyThreshold = 0.55f,         // SPEECH: Reduced from 0.65f
                zcrMinThreshold = 0.018f,                 // SPEECH: Reduced from 0.028f
                zcrMaxThreshold = 0.25f,                  // SPEECH: Increased from 0.16f
                silenceRatioMin = 0.10f,                  // SPEECH: Reduced from 0.14f
                garbageScoreMax = 18                      // SPEECH: Increased from 15
            )
        )
    }

    // Expert Mode Speech - High standards with speech accommodation
    fun expertModeSpeech(): Presets {
        return Presets(
            difficulty = DifficultyLevel.EXPERT,
            scoring = ScoringParameters(
                pitchWeight = 0.80f,              // SPEECH: Reduced from 0.95f
                mfccWeight = 0.20f,               // SPEECH: Increased from 0.05f
                pitchTolerance = 20f,             // SPEECH: Increased from 8f - more forgiving than singing
                minScoreThreshold = 0.22f,        // SPEECH: Reduced from 0.30f
                perfectScoreThreshold = 0.85f,    // SPEECH: Reduced from 0.92f
                scoreCurve = 2.0f                 // SPEECH: Reduced from 2.5f - more demanding
            ),
            content = ContentDetectionParameters(
                contentDetectionBestThreshold = 0.40f,    // SPEECH: Reduced from 0.50f
                contentDetectionAvgThreshold = 0.30f,     // SPEECH: Reduced from 0.35f
                rightContentFlatPenalty = 0.20f,          // SPEECH: Reduced from 0.35f
                rightContentDifferentMelodyPenalty = 0.12f, // SPEECH: Reduced from 0.25f
                wrongContentStandardPenalty = 0.70f       // SPEECH: Reduced from 0.75f
            ),
            melodic = MelodicAnalysisParameters(
                monotoneDetectionThreshold = 2.0f,        // SPEECH: Reduced from 3.0f
                flatSpeechThreshold = 0.6f,               // SPEECH: Increased - allow some flatness
                monotonePenalty = 0.25f,                  // SPEECH: Reduced from 0.4f
                melodicRangeWeight = 0.25f,               // SPEECH: Moderate expectation
                melodicTransitionWeight = 0.25f,          // SPEECH: Moderate expectation
                melodicVarianceWeight = 0.50f             // SPEECH: Balanced focus
            ),
            musical = MusicalSimilarityParameters(
                sameIntervalScore = 0.95f,                // SPEECH: Slightly reduced from 1.0f
                closeIntervalScore = 0.85f,               // SPEECH: Good scoring
                emptyPhrasesPenalty = 0.25f,              // SPEECH: Moderate penalty
                emptyRhythmPenalty = 0.18f                // SPEECH: Moderate penalty
            ),
            audio = AudioProcessingParameters(),
            scaling = ScoreScalingParameters(
                incredibleFeedbackThreshold = 90,         // SPEECH: Reduced from 95
                greatJobFeedbackThreshold = 70,           // SPEECH: Reduced from 85
                goodEffortFeedbackThreshold = 50,         // SPEECH: Reduced from 70
                reversePerfectScoreAdjustment = 0.96f     // SPEECH: Slightly easier reverse
            ),
            garbage = GarbageDetectionParameters(
                enableGarbageDetection = true,
                mfccVarianceThreshold = 0.40f,            // SPEECH: Reduced from 0.45f
                pitchMonotoneThreshold = 12f,             // SPEECH: Reduced from 14f
                pitchOscillationRate = 0.5f,              // SPEECH: Increased from 0.42f
                spectralEntropyThreshold = 0.60f,         // SPEECH: Reduced from 0.65f
                zcrMinThreshold = 0.022f,                 // SPEECH: Reduced from 0.028f
                zcrMaxThreshold = 0.20f,                  // SPEECH: Increased from 0.16f
                silenceRatioMin = 0.12f,                  // SPEECH: Reduced from 0.14f
                garbageScoreMax = 15                      // SPEECH: Same as original
            )
        )
    }

    // Master Mode Speech - Perfection with speech realism
    fun masterModeSpeech(): Presets {
        return Presets(
            difficulty = DifficultyLevel.MASTER,
            scoring = ScoringParameters(
                pitchWeight = 0.85f,              // SPEECH: Reduced from 0.98f
                mfccWeight = 0.15f,               // SPEECH: Increased from 0.02f
                pitchTolerance = 15f,             // SPEECH: Increased from 5f - realistic for speech
                minScoreThreshold = 0.30f,        // SPEECH: Reduced from 0.42f
                perfectScoreThreshold = 0.88f,    // SPEECH: Increased from 0.50f but realistic
                scoreCurve = 1.8f                 // SPEECH: Reduced from 2.5f - demanding but fair
            ),
            content = ContentDetectionParameters(
                contentDetectionBestThreshold = 0.50f,    // SPEECH: Reduced from 0.65f
                contentDetectionAvgThreshold = 0.35f,     // SPEECH: Reduced from 0.50f
                rightContentFlatPenalty = 0.30f,          // SPEECH: Reduced from 0.45f
                rightContentDifferentMelodyPenalty = 0.20f, // SPEECH: Reduced from 0.35f
                wrongContentStandardPenalty = 0.75f       // SPEECH: Same - accuracy critical at master
            ),
            melodic = MelodicAnalysisParameters(
                monotoneDetectionThreshold = 3.0f,        // SPEECH: Reduced from 4.0f but still demanding
                flatSpeechThreshold = 0.3f,               // SPEECH: Increased from 0.1f - some variation expected
                monotonePenalty = 0.6f,                   // SPEECH: Reduced from 0.9f - significant but not crushing
                melodicRangeWeight = 0.30f,               // SPEECH: Moderate range expectation
                melodicTransitionWeight = 0.30f,          // SPEECH: Moderate transition expectation
                melodicVarianceWeight = 0.40f             // SPEECH: Balanced focus on variation
            ),
            musical = MusicalSimilarityParameters(
                sameIntervalScore = 1.0f,                 // SPEECH: Full score for exact matches
                closeIntervalScore = 0.90f,               // SPEECH: High score for close
                emptyPhrasesPenalty = 0.30f,              // SPEECH: Full penalty expected at master
                emptyRhythmPenalty = 0.20f                // SPEECH: Reduced rhythm penalty
            ),
            audio = AudioProcessingParameters(),
            scaling = ScoreScalingParameters(
                incredibleFeedbackThreshold = 95,         // SPEECH: Reduced from 98
                greatJobFeedbackThreshold = 75,           // SPEECH: Reduced from 90
                goodEffortFeedbackThreshold = 55,         // SPEECH: Reduced from 80
                reverseMinScoreAdjustment = 0.95f,        // SPEECH: Slightly easier reverse
                reversePerfectScoreAdjustment = 0.98f,    // SPEECH: Slightly easier reverse
                reverseCurveAdjustment = 1.05f            // SPEECH: Slightly more generous reverse
            ),
            garbage = GarbageDetectionParameters(
                enableGarbageDetection = true,
                mfccVarianceThreshold = 0.50f,            // SPEECH: Reduced from 0.55f
                pitchMonotoneThreshold = 14f,             // SPEECH: Reduced from 16f
                pitchOscillationRate = 0.45f,             // SPEECH: Increased from 0.37f
                spectralEntropyThreshold = 0.70f,         // SPEECH: Reduced from 0.75f
                zcrMinThreshold = 0.025f,                 // SPEECH: Reduced from 0.033f
                zcrMaxThreshold = 0.18f,                  // SPEECH: Increased from 0.13f
                silenceRatioMin = 0.15f,                  // SPEECH: Reduced from 0.17f
                garbageScoreMax = 12                      // SPEECH: Increased from 10 - slightly more lenient
            )
        )
    }

    // Helper function to get all speech difficulty presets in order
    fun getAllSpeechDifficultyPresets(): List<Pair<DifficultyLevel, () -> Presets>> {
        return listOf(
            DifficultyLevel.EASY to ::easyModeSpeech,
            DifficultyLevel.NORMAL to ::normalModeSpeech,
            DifficultyLevel.HARD to ::hardModeSpeech,
            DifficultyLevel.EXPERT to ::expertModeSpeech,
            DifficultyLevel.MASTER to ::masterModeSpeech
        )
    }
}