package com.example.reversey.scoring

import android.util.Log
import com.example.reversey.data.models.ChallengeType
import com.example.reversey.data.models.PlayerAttempt
import com.example.reversey.scoring.DifficultyLevel
import com.example.reversey.scoring.VocalMode
import javax.inject.Inject
import javax.inject.Singleton
// ADD THESE IMPORTS:
import com.example.reversey.scoring.VocalAnalysis
import com.example.reversey.scoring.ScoringEngineType
import com.example.reversey.scoring.VocalFeatures

/**
 * 🎯 SCORE ACQUISITION DATA CONCENTRATOR
 *
 * PURE DATA COLLECTOR - NO ROUTING OR ANALYSIS!
 *
 * ROLE: Takes completed ScoringResult and packages into EnhancedPlayerAttempt
 *
 * RESPONSIBILITIES:
 * 1. RECEIVE ScoringResult from whichever engine was used
 * 2. COLLECT metadata (engine type, attempt info, etc.)
 * 3. PACKAGE into EnhancedPlayerAttempt with NO DATA LOSS
 * 4. RETURN to AudioViewModel
 *
 * GLUTE PRINCIPLE: Single purpose data aggregation
 */
@Singleton
class ScoreAcquisitionDataConcentrator @Inject constructor() {

    /**
     * Collect and concentrate scoring data into enhanced format
     *
     * @param scoringResult Already completed scoring from engine
     * @param engineType Which engine produced the result
     * @param challengeType Forward or reverse challenge
     * @param attemptFilePath Path to attempt audio file
     * @param reversedAttemptFilePath Path to reversed attempt (if exists)
     * @param parentRecordingName Name of parent recording
     * @param vocalMode Classification result from VocalModeDetector
     * @param difficulty Current difficulty level
     * @return EnhancedPlayerAttempt with all data preserved
     */
    fun acquireAndConcentrateScore(
        scoringResult: ScoringResult,
        engineType: ScoringEngineType,
        challengeType: ChallengeType,
        attemptFilePath: String,
        reversedAttemptFilePath: String? = null,
        parentRecordingName: String,
        actualVocalAnalysis: VocalAnalysis,
        difficulty: DifficultyLevel
    ): EnhancedPlayerAttempt {

        Log.d("SCORE_CONCENTRATOR", "🎯 Collecting data from ${engineType.name}")

        // Pure data collection - no analysis, just packaging
        return EnhancedPlayerAttempt(
            // Core PlayerAttempt fields - from scoring result
            playerName = "Player", // Will be set by AudioViewModel
            attemptFilePath = attemptFilePath,
            reversedAttemptFilePath = reversedAttemptFilePath,
            score = scoringResult.score,
            pitchSimilarity = scoringResult.metrics.pitch,
            mfccSimilarity = scoringResult.metrics.mfcc,
            rawScore = scoringResult.rawScore,
            challengeType = challengeType,
            difficulty = difficulty,

            // Enhanced fields - preserved scoring intelligence
            feedback = scoringResult.feedback,
            isGarbage = scoringResult.isGarbage,
            vocalAnalysis = actualVocalAnalysis,

            selectedEngine = engineType,
            audioQualityMetrics = AudioQualityMetrics(
                rms = 0.5f, // Simple placeholder values
                snr = 20f
            ),
            performanceInsights = PerformanceInsights(
                feedback = scoringResult.feedback // Reuse engine feedback
            ),
            debuggingData = null
        ).also {
            Log.d("SCORE_CONCENTRATOR", "✅ Data collection complete - Score: ${scoringResult.score}")
        }
    }
}

/**
 * Enhanced PlayerAttempt with preserved scoring intelligence
 * NO DATA LOSS - all analysis preserved for future use
 */
data class EnhancedPlayerAttempt(
    // Core PlayerAttempt fields for backward compatibility
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
    val feedback: List<String>,
    val isGarbage: Boolean,
    val vocalAnalysis: VocalAnalysis,
    val selectedEngine: ScoringEngineType,
    val audioQualityMetrics: AudioQualityMetrics,
    val performanceInsights: PerformanceInsights,
    val debuggingData: DebuggingData? = null
) {
    /**
     * Convert to basic PlayerAttempt for backward compatibility
     */
    fun toBasicPlayerAttempt(): PlayerAttempt {
        return PlayerAttempt(
            playerName = playerName,
            attemptFilePath = attemptFilePath,
            reversedAttemptFilePath = reversedAttemptFilePath,
            score = score,
            pitchSimilarity = pitchSimilarity,
            mfccSimilarity = mfccSimilarity,
            rawScore = rawScore,
            challengeType = challengeType,
            difficulty = difficulty
        )
    }
}
data class AudioQualityMetrics(val rms: Float, val snr: Float)
data class PerformanceInsights(val feedback: List<String>)
data class DebuggingData(val debugInfo: String)