package com.quokkalabs.reversey.scoring

import android.util.Log
import com.quokkalabs.reversey.data.models.ChallengeType
import com.quokkalabs.reversey.data.models.PlayerAttempt

/**
 * 🎯 SCORE ACQUISITION DATA CONCENTRATOR
 *
 * Collects and consolidates scoring metadata into an enhanced structure.
 * Does not perform scoring or routing – only packages results.
 */
class ScoreAcquisitionDataConcentrator {

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

        Log.d(
            "SCORE_CONCENTRATOR",
            "Collecting score from engine=${engineType.name}, parent='${parentRecordingName}', diff=${difficulty.name}"
        )

        return EnhancedPlayerAttempt(
            playerName = "Player",  // Actual name assigned in AudioViewModel
            attemptFilePath = attemptFilePath,
            reversedAttemptFilePath = reversedAttemptFilePath,
            score = scoringResult.score,
            pitchSimilarity = scoringResult.metrics.pitch,
            mfccSimilarity = scoringResult.metrics.mfcc,
            rawScore = scoringResult.rawScore,
            challengeType = challengeType,
            difficulty = difficulty,

            feedback = scoringResult.feedback,
            isGarbage = scoringResult.isGarbage,
            vocalAnalysis = actualVocalAnalysis,
            selectedEngine = engineType,
            performanceInsights = PerformanceInsights(
                feedback = scoringResult.feedback
            ),
            debuggingData = null
        )
    }
}

/**
 * Enhanced score object containing rich metadata.
 * Converted to PlayerAttempt for storage/UI.
 */
data class EnhancedPlayerAttempt(
    val playerName: String,
    val attemptFilePath: String,
    val reversedAttemptFilePath: String?,
    val score: Int,
    val pitchSimilarity: Float,
    val mfccSimilarity: Float,
    val rawScore: Float,
    val challengeType: ChallengeType,
    val difficulty: DifficultyLevel,

    val feedback: List<String>,
    val isGarbage: Boolean,
    val vocalAnalysis: VocalAnalysis,
    val selectedEngine: ScoringEngineType,
    val performanceInsights: PerformanceInsights,
    val debuggingData: DebuggingData? = null
) {
    fun toPlayerAttempt(): PlayerAttempt {
        return PlayerAttempt(
            playerName = playerName,
            attemptFilePath = attemptFilePath,
            reversedAttemptFilePath = reversedAttemptFilePath,
            score = score,
            pitchSimilarity = pitchSimilarity,
            mfccSimilarity = mfccSimilarity,
            rawScore = rawScore,
            challengeType = challengeType,
            difficulty = difficulty,
            scoringEngine = selectedEngine,
            feedback = feedback,
            isGarbage = isGarbage,
            vocalAnalysis = vocalAnalysis,
            performanceInsights = performanceInsights,
            debuggingData = debuggingData
        )
    }
}

/** Simple value objects for metadata **/
data class PerformanceInsights(val feedback: List<String>)
data class DebuggingData(val debugInfo: String)
