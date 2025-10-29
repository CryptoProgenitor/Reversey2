package com.example.reversey

import androidx.compose.runtime.Immutable

/**
 * Represents a single player's attempt to match a reversed recording.
 *
 * @param playerName The name of the player (e.g., "Player 1").
 * @param attemptFilePath The file path to the audio they recorded.
 * @param reversedAttemptFilePath The file path to the reversed version of their attempt.
 * @param score The percentage score of how well their attempt matched the original.
 * @param pitchSimilarity The pitch similarity metric (0.0 to 1.0) from ScoringEngine.
 * @param mfccSimilarity The MFCC similarity metric (0.0 to 1.0) from ScoringEngine.
 * @param rawScore The raw score value before percentage conversion from ScoringEngine.
 * @param challengeType The type of challenge (REVERSE or FORWARD).
 */
@Immutable
data class PlayerAttempt(
    val playerName: String,
    val attemptFilePath: String,
    val reversedAttemptFilePath: String? = null,
    val score: Int = 0, // Default score to 0 until it's calculated
    val pitchSimilarity: Float = 0f, // Real pitch similarity from ScoringEngine
    val mfccSimilarity: Float = 0f,  // Real MFCC similarity from ScoringEngine
    val rawScore: Float = 0f,        // Real raw score from ScoringEngine
    val challengeType: ChallengeType // Challenge type
)