package com.example.reversey.data.models

import androidx.compose.runtime.Immutable
import com.example.reversey.scoring.DifficultyLevel
import com.example.reversey.scoring.ScoringEngineType
import com.example.reversey.scoring.VocalAnalysis
import com.example.reversey.scoring.AudioQualityMetrics
import com.example.reversey.scoring.PerformanceInsights
import com.example.reversey.scoring.DebuggingData

/**
 * Represents a single player's attempt to match a recording (forward or reverse).
 * GLUTE-compliant, metadata-rich, and fully Compose-safe.
 */
@Immutable
data class PlayerAttempt(
    val playerName: String,
    val attemptFilePath: String,
    val reversedAttemptFilePath: String? = null,
    val score: Int = 0,
    val pitchSimilarity: Float = 0f,
    val mfccSimilarity: Float = 0f,
    val rawScore: Float = 0f,
    val challengeType: ChallengeType,
    val difficulty: DifficultyLevel = DifficultyLevel.NORMAL,
    val scoringEngine: ScoringEngineType? = null,

    // --- Rich Metadata ------------------------------------------------------
    val feedback: List<String> = emptyList(),
    val isGarbage: Boolean = false,
    val vocalAnalysis: VocalAnalysis? = null,
    val audioQualityMetrics: AudioQualityMetrics? = null,
    val performanceInsights: PerformanceInsights? = null,
    val debuggingData: DebuggingData? = null
)
