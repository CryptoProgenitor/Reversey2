package com.quokkalabs.reversey.data.models

import androidx.compose.runtime.Immutable
import com.quokkalabs.reversey.scoring.DifficultyLevel
import com.quokkalabs.reversey.scoring.ScoringEngineType
import com.quokkalabs.reversey.scoring.VocalAnalysis
import com.quokkalabs.reversey.scoring.AudioQualityMetrics
import com.quokkalabs.reversey.scoring.PerformanceInsights
import com.quokkalabs.reversey.scoring.DebuggingData

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
