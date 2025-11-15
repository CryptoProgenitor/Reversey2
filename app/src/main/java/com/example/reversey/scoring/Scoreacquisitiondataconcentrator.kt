package com.example.reversey.scoring

import android.util.Log
import com.example.reversey.data.models.ChallengeType
import javax.inject.Inject
import javax.inject.Singleton
import com.example.reversey.BuildConfig

/**
 * 🎯 SCORE ACQUISITION DATA CONCENTRATOR
 *
 * Central orchestrator that collects scoring results + analysis data from separate engines.
 * Outputs Enhanced PlayerAttempt with full analysis preservation.
 *
 * GLUTE PRINCIPLE: Single point of data collection and enrichment
 *
 * RESPONSIBILITIES:
 * 1. Route to appropriate scoring engine based on vocal mode
 * 2. Collect comprehensive scoring results and analysis data
 * 3. Aggregate additional metadata (vocal analysis, performance insights)
 * 4. Output Enhanced PlayerAttempt with preserved intelligence
 * 5. Handle error states gracefully
 *
 * PREVENTS DATA LOSS: Unlike current system that discards feedback/analysis,
 * this preserves ALL scoring intelligence for UI and future analysis.
 */
@Singleton
class ScoreAcquisitionDataConcentrator @Inject constructor(
    private val speechScoringEngine: SpeechScoringEngine,
    private val singingScoringEngine: SingingScoringEngine,
    private val vocalModeRouter: VocalModeRouter
) {

    /**
     * Main orchestration method - coordinates entire scoring pipeline
     *
     * @param originalAudio Reference audio to match against
     * @param playerAttempt User's audio attempt
     * @param challengeType Forward or Reverse challenge
     * @param attemptFilePath File path for attempt audio
     * @param reversedAttemptFilePath File path for reversed attempt (if created)
     * @param parentRecordingName Name of parent recording for context
     * @param sampleRate Audio sample rate
     * @return Enhanced PlayerAttempt with complete analysis data
     */
    fun acquireAndConcentrateScore(
        originalAudio: FloatArray,
        playerAttempt: FloatArray,
        challengeType: ChallengeType,
        attemptFilePath: String,
        reversedAttemptFilePath: String? = null,
        parentRecordingName: String = "Recording",
        sampleRate: Int = 44100
    ): EnhancedPlayerAttempt {

        Log.d("SCORE_CONCENTRATOR", "=== SCORE ACQUISITION PIPELINE ===")
        Log.d("SCORE_CONCENTRATOR", "🎯 Challenge: $challengeType")
        Log.d("SCORE_CONCENTRATOR", "📁 Attempt file: $attemptFilePath")
        Log.d("SCORE_CONCENTRATOR", "🔄 Reversed file: $reversedAttemptFilePath")

        try {
            // STEP 1: Perform vocal mode analysis and routing
            val vocalAnalysis = performVocalModeAnalysis(playerAttempt, sampleRate)
            val routingDecision = vocalModeRouter.getRoutingDecision(vocalAnalysis)

            Log.d("SCORE_CONCENTRATOR", "🎤 Vocal mode: ${routingDecision.routedMode}")
            Log.d("SCORE_CONCENTRATOR", "🎯 Selected engine: ${routingDecision.selectedEngine}")

            // STEP 2: Route to appropriate scoring engine
            val scoringResult = when (routingDecision.selectedEngine) {
                ScoringEngineType.SPEECH_ENGINE -> {
                    Log.d("SCORE_CONCENTRATOR", "📢 Routing to Speech Engine")
                    speechScoringEngine.scoreAttempt(originalAudio, playerAttempt, challengeType, sampleRate)
                }
                ScoringEngineType.SINGING_ENGINE -> {
                    Log.d("SCORE_CONCENTRATOR", "🎵 Routing to Singing Engine")
                    singingScoringEngine.scoreAttempt(originalAudio, playerAttempt, challengeType, sampleRate)
                }
            }

            Log.d("SCORE_CONCENTRATOR", "📊 Scoring complete - Score: ${scoringResult.score}%")

            // STEP 3: Collect additional analysis data
            val audioQualityMetrics = calculateAudioQualityMetrics(playerAttempt)
            val performanceInsights = generatePerformanceInsights(scoringResult, vocalAnalysis, challengeType)
            val debuggingData = collectDebuggingData(scoringResult, vocalAnalysis, routingDecision)

            // STEP 4: Get current difficulty from active engine
            val currentDifficulty = when (routingDecision.selectedEngine) {
                ScoringEngineType.SPEECH_ENGINE -> speechScoringEngine.getCurrentDifficulty()
                ScoringEngineType.SINGING_ENGINE -> singingScoringEngine.getCurrentDifficulty()
            }

            // STEP 5: Create Enhanced PlayerAttempt with ALL data preserved
            val enhancedAttempt = EnhancedPlayerAttempt(
                // Core PlayerAttempt fields
                playerName = generatePlayerName(parentRecordingName),
                attemptFilePath = attemptFilePath,
                reversedAttemptFilePath = reversedAttemptFilePath,
                score = scoringResult.score,
                pitchSimilarity = scoringResult.metrics.pitch,
                mfccSimilarity = scoringResult.metrics.mfcc,
                rawScore = scoringResult.rawScore,
                challengeType = challengeType,
                difficulty = currentDifficulty,

                // ENHANCED FIELDS - Preserved scoring intelligence
                feedback = scoringResult.feedback,
                isGarbage = scoringResult.isGarbage,
                vocalAnalysis = vocalAnalysis,
                selectedEngine = routingDecision.selectedEngine,
                audioQualityMetrics = audioQualityMetrics,
                performanceInsights = performanceInsights,
                debuggingData = if (BuildConfig.DEBUG) debuggingData else null
            )

            Log.d("SCORE_CONCENTRATOR", "✅ Enhanced PlayerAttempt created successfully")
            Log.d("SCORE_CONCENTRATOR", "   📊 Score: ${enhancedAttempt.score}%")
            Log.d("SCORE_CONCENTRATOR", "   🎤 Vocal mode: ${enhancedAttempt.vocalAnalysis.mode}")
            Log.d("SCORE_CONCENTRATOR", "   🎯 Engine: ${enhancedAttempt.selectedEngine}")
            Log.d("SCORE_CONCENTRATOR", "   💬 Feedback lines: ${enhancedAttempt.feedback.size}")
            Log.d("SCORE_CONCENTRATOR", "=====================================")

            return enhancedAttempt

        } catch (e: Exception) {
            Log.e("SCORE_CONCENTRATOR", "❌ Error in score acquisition pipeline: ${e.message}", e)

            // Return safe fallback Enhanced PlayerAttempt
            return createFallbackEnhancedAttempt(
                attemptFilePath, reversedAttemptFilePath, challengeType, parentRecordingName, e
            )
        }
    }

    /**
     * Analyze vocal mode characteristics of the attempt
     * This is a simplified version - in production would use more sophisticated ML
     */
    private fun performVocalModeAnalysis(audio: FloatArray, sampleRate: Int): VocalAnalysis {
        Log.d("SCORE_CONCENTRATOR", "🔍 Analyzing vocal mode characteristics...")

        // Simple heuristic-based vocal analysis
        // In production, this would use ML models or more sophisticated DSP

        // Calculate basic audio features
        val rmsEnergy = calculateRMS(audio)
        val pitchVariation = calculatePitchVariation(audio, sampleRate)
        val spectralCentroid = calculateSpectralCentroid(audio)
        val harmonicContent = calculateHarmonicContent(audio)

        val vocalFeatures = VocalFeatures(
            rmsEnergy = rmsEnergy,
            pitchVariation = pitchVariation,
            spectralCentroid = spectralCentroid,
            harmonicContent = harmonicContent
        )

        // Simple classification logic
        val (vocalMode, confidence) = classifyVocalMode(vocalFeatures)

        Log.d("SCORE_CONCENTRATOR", "🎤 Vocal analysis complete:")
        Log.d("SCORE_CONCENTRATOR", "   Mode: $vocalMode (confidence: ${confidence * 100}%)")
        Log.d("SCORE_CONCENTRATOR", "   RMS: $rmsEnergy, Pitch var: $pitchVariation")
        Log.d("SCORE_CONCENTRATOR", "   Spectral centroid: $spectralCentroid")

        return VocalAnalysis(
            mode = vocalMode,
            confidence = confidence,
            features = vocalFeatures
        )
    }

    /**
     * Simple vocal mode classification based on audio characteristics
     */
    private fun classifyVocalMode(features: VocalFeatures): Pair<VocalMode, Float> {
        // Simple heuristic classification
        // In production, this would use trained ML models

        val speechIndicators = listOf(
            features.pitchVariation < 8f,           // Lower pitch variation
            features.harmonicContent < 0.6f,        // Less harmonic complexity
            features.spectralCentroid < 2000f       // Lower spectral centroid
        ).count { it }

        val singingIndicators = listOf(
            features.pitchVariation > 12f,          // Higher pitch variation
            features.harmonicContent > 0.7f,        // More harmonic complexity
            features.spectralCentroid > 1500f,      // Higher spectral centroid
            features.rmsEnergy > 0.1f               // Generally louder
        ).count { it }

        return when {
            singingIndicators > speechIndicators -> {
                val confidence = (singingIndicators * 0.25f).coerceIn(0.5f, 1f)
                VocalMode.SINGING to confidence
            }
            speechIndicators > singingIndicators -> {
                val confidence = (speechIndicators * 0.33f).coerceIn(0.5f, 1f)
                VocalMode.SPEECH to confidence
            }
            else -> {
                VocalMode.UNKNOWN to 0.3f  // Low confidence for unclear cases
            }
        }
    }

    /**
     * Calculate audio quality metrics
     */
    private fun calculateAudioQualityMetrics(audio: FloatArray): AudioQualityMetrics {
        val rmsLevel = calculateRMS(audio)
        val peakLevel = audio.maxOfOrNull { kotlin.math.abs(it) } ?: 0f
        val dynamicRange = peakLevel - rmsLevel
        val noiseFloor = calculateNoiseFloor(audio)
        val snr = if (noiseFloor > 0f) 20 * kotlin.math.log10(rmsLevel / noiseFloor) else 60f

        return AudioQualityMetrics(
            rmsLevel = rmsLevel,
            peakLevel = peakLevel,
            dynamicRange = dynamicRange,
            signalToNoiseRatio = snr,
            qualityRating = when {
                snr > 40f && rmsLevel > 0.05f -> QualityRating.EXCELLENT
                snr > 25f && rmsLevel > 0.02f -> QualityRating.GOOD
                snr > 15f -> QualityRating.FAIR
                else -> QualityRating.POOR
            }
        )
    }

    /**
     * Generate performance insights based on results
     */
    private fun generatePerformanceInsights(
        result: ScoringResult,
        vocalAnalysis: VocalAnalysis,
        challengeType: ChallengeType
    ): PerformanceInsights {

        val strengths = mutableListOf<String>()
        val improvements = mutableListOf<String>()
        val tips = mutableListOf<String>()

        // Analyze performance strengths
        when {
            result.score >= 90 -> strengths.add("Exceptional ${vocalAnalysis.mode.name.lowercase()} performance")
            result.score >= 75 -> strengths.add("Strong ${vocalAnalysis.mode.name.lowercase()} technique")
            result.score >= 60 -> strengths.add("Good ${vocalAnalysis.mode.name.lowercase()} foundation")
        }

        if (result.metrics.pitch > 0.8f) strengths.add("Excellent pitch accuracy")
        if (result.metrics.mfcc > 0.8f) strengths.add("Great vocal tone matching")

        // Identify areas for improvement
        if (result.metrics.pitch < 0.6f) improvements.add("Pitch accuracy needs work")
        if (result.metrics.mfcc < 0.6f) improvements.add("Focus on vocal tone and clarity")

        // Generate mode-specific tips
        when (vocalAnalysis.mode) {
            VocalMode.SPEECH -> {
                tips.add("Focus on clear pronunciation and natural speech rhythm")
                if (challengeType == ChallengeType.REVERSE) {
                    tips.add("Listen carefully to the reversed speech patterns")
                }
            }
            VocalMode.SINGING -> {
                tips.add("Work on pitch accuracy and musical expression")
                if (challengeType == ChallengeType.REVERSE) {
                    tips.add("Reverse singing requires careful attention to melody direction")
                }
            }
            VocalMode.UNKNOWN -> {
                tips.add("Try to be more expressive - either speak clearly or sing melodically")
            }
        }

        return PerformanceInsights(
            strengths = strengths,
            areasForImprovement = improvements,
            practiceRecommendations = tips,
            overallAssessment = when {
                result.score >= 85 -> "Outstanding performance with ${vocalAnalysis.mode.name.lowercase()}"
                result.score >= 70 -> "Good performance, showing ${vocalAnalysis.mode.name.lowercase()} skills"
                result.score >= 50 -> "Decent attempt, room for improvement in ${vocalAnalysis.mode.name.lowercase()}"
                else -> "Keep practicing! Focus on basic ${vocalAnalysis.mode.name.lowercase()} techniques"
            }
        )
    }

    /**
     * Collect debugging data for development
     */
    private fun collectDebuggingData(
        result: ScoringResult,
        vocalAnalysis: VocalAnalysis,
        routingDecision: VocalModeRoutingDecision
    ): DebuggingData {
        return DebuggingData(
            engineRoutingReason = "Vocal mode ${vocalAnalysis.mode} → ${routingDecision.selectedEngine}",
            rawScoringData = mapOf(
                "raw_score" to result.rawScore.toString(),
                "pitch_similarity" to result.metrics.pitch.toString(),
                "mfcc_similarity" to result.metrics.mfcc.toString(),
                "is_garbage" to result.isGarbage.toString()
            ),
            vocalModeConfidence = vocalAnalysis.confidence,
            processingTimeMs = System.currentTimeMillis(), // Would measure actual processing time
            algorithmVersion = "1.0.0"
        )
    }

    /**
     * Create fallback Enhanced PlayerAttempt for error cases
     */
    private fun createFallbackEnhancedAttempt(
        attemptFilePath: String,
        reversedAttemptFilePath: String?,
        challengeType: ChallengeType,
        parentRecordingName: String,
        error: Exception
    ): EnhancedPlayerAttempt {
        Log.w("SCORE_CONCENTRATOR", "Creating fallback Enhanced PlayerAttempt due to error")

        return EnhancedPlayerAttempt(
            playerName = generatePlayerName(parentRecordingName),
            attemptFilePath = attemptFilePath,
            reversedAttemptFilePath = reversedAttemptFilePath,
            score = 0,
            pitchSimilarity = 0f,
            mfccSimilarity = 0f,
            rawScore = 0f,
            challengeType = challengeType,
            difficulty = DifficultyLevel.NORMAL, // Safe default

            // Enhanced fields with error info
            feedback = listOf("❌ Processing error occurred - please try again!"),
            isGarbage = false,
            vocalAnalysis = VocalAnalysis(
                mode = VocalMode.UNKNOWN,
                confidence = 0f,
                features = VocalFeatures(0f, 0f, 0f, 0f)
            ),
            selectedEngine = ScoringEngineType.SPEECH_ENGINE,
            audioQualityMetrics = AudioQualityMetrics(0f, 0f, 0f, 0f, QualityRating.POOR),
            performanceInsights = PerformanceInsights(
                strengths = emptyList(),
                areasForImprovement = listOf("Technical issue encountered"),
                practiceRecommendations = listOf("Please try recording again"),
                overallAssessment = "Processing error - attempt could not be analyzed"
            ),
            debuggingData = if (BuildConfig.DEBUG) {
                DebuggingData(
                    engineRoutingReason = "Error fallback",
                    rawScoringData = mapOf("error" to error.message.orEmpty()),
                    vocalModeConfidence = 0f,
                    processingTimeMs = 0,
                    algorithmVersion = "1.0.0-fallback"
                )
            } else null
        )
    }

    // Utility calculation methods

    private fun generatePlayerName(parentRecordingName: String): String {
        return "Player ${System.currentTimeMillis() % 1000}"
    }

    private fun calculateRMS(audio: FloatArray): Float {
        if (audio.isEmpty()) return 0f
        val sumOfSquares = audio.fold(0f) { acc, sample -> acc + sample * sample }
        return kotlin.math.sqrt(sumOfSquares / audio.size)
    }

    private fun calculatePitchVariation(audio: FloatArray, sampleRate: Int): Float {
        // Simplified pitch variation calculation
        // In production would use proper pitch tracking
        if (audio.size < 1024) return 0f

        val frameSize = 1024
        val pitches = mutableListOf<Float>()

        for (i in 0 until audio.size - frameSize step frameSize / 2) {
            val frame = audio.sliceArray(i until i + frameSize)
            val energy = calculateRMS(frame)
            if (energy > 0.01f) {
                // Simplified pitch estimate based on zero crossing rate
                val zcr = frame.zipWithNext { a, b -> if ((a >= 0f) != (b >= 0f)) 1 else 0 }.sum()
                val pitch = (zcr * sampleRate) / (2f * frameSize)
                if (pitch > 80f && pitch < 800f) pitches.add(pitch)
            }
        }

        return if (pitches.size >= 2) {
            val mean = pitches.average().toFloat()
            val variance = pitches.map { (it - mean) * (it - mean) }.average().toFloat()
            kotlin.math.sqrt(variance)
        } else 0f
    }

    private fun calculateSpectralCentroid(audio: FloatArray): Float {
        // Simplified spectral centroid calculation
        // In production would use proper FFT analysis
        return calculateRMS(audio) * 2000f // Placeholder
    }

    private fun calculateHarmonicContent(audio: FloatArray): Float {
        // Simplified harmonic content estimation
        // In production would use proper harmonic analysis
        val rms = calculateRMS(audio)
        return if (rms > 0.02f) 0.7f else 0.3f // Placeholder
    }

    private fun calculateNoiseFloor(audio: FloatArray): Float {
        // Estimate noise floor from quieter segments
        val sortedAmplitudes = audio.map { kotlin.math.abs(it) }.sorted()
        val noiseFloorIndex = (sortedAmplitudes.size * 0.1).toInt()
        return if (noiseFloorIndex < sortedAmplitudes.size) {
            sortedAmplitudes[noiseFloorIndex]
        } else 0.001f
    }
}

/**
 * Enhanced PlayerAttempt with preserved analysis data
 * Extends basic PlayerAttempt concept to include rich scoring intelligence
 */
data class EnhancedPlayerAttempt(
    // Core PlayerAttempt fields
    val playerName: String,
    val attemptFilePath: String,
    val reversedAttemptFilePath: String?,
    val score: Int,
    val pitchSimilarity: Float,
    val mfccSimilarity: Float,
    val rawScore: Float,
    val challengeType: ChallengeType,
    val difficulty: DifficultyLevel,

    // Enhanced fields - preserved scoring intelligence
    val feedback: List<String>,                    // Rich feedback from scoring engine
    val isGarbage: Boolean,                       // Garbage detection result
    val vocalAnalysis: VocalAnalysis,             // Speech vs singing analysis
    val selectedEngine: ScoringEngineType,        // Which engine was used
    val audioQualityMetrics: AudioQualityMetrics, // Technical audio quality
    val performanceInsights: PerformanceInsights, // Detailed analysis and recommendations
    val debuggingData: DebuggingData? = null      // Debug info (only in debug builds)
) {
    /**
     * Convert to basic PlayerAttempt for backwards compatibility
     * (if existing code expects the original PlayerAttempt structure)
     */
    fun toBasicPlayerAttempt(): Any {
        // This would return the original PlayerAttempt data class
        // Implementation depends on exact PlayerAttempt structure
        return mapOf(
            "playerName" to playerName,
            "attemptFilePath" to attemptFilePath,
            "reversedAttemptFilePath" to reversedAttemptFilePath,
            "score" to score,
            "pitchSimilarity" to pitchSimilarity,
            "mfccSimilarity" to mfccSimilarity,
            "rawScore" to rawScore,
            "challengeType" to challengeType,
            "difficulty" to difficulty
        )
    }
}

/**
 * Audio quality assessment metrics
 */
data class AudioQualityMetrics(
    val rmsLevel: Float,
    val peakLevel: Float,
    val dynamicRange: Float,
    val signalToNoiseRatio: Float,
    val qualityRating: QualityRating
)

enum class QualityRating {
    POOR, FAIR, GOOD, EXCELLENT
}

/**
 * Performance analysis and recommendations
 */
data class PerformanceInsights(
    val strengths: List<String>,
    val areasForImprovement: List<String>,
    val practiceRecommendations: List<String>,
    val overallAssessment: String
)

/**
 * Debugging and development data
 */
data class DebuggingData(
    val engineRoutingReason: String,
    val rawScoringData: Map<String, String>,
    val vocalModeConfidence: Float,
    val processingTimeMs: Long,
    val algorithmVersion: String
)