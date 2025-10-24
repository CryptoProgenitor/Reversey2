package com.example.reversey

import androidx.compose.runtime.Immutable

/**
 * Represents a single player's attempt to match a reversed recording.
 *
 * @param playerName The name of the player (e.g., "Player 1").
 * @param attemptFilePath The file path to the audio they recorded.
 * @param reversedAttemptFilePath The file path to the reversed version of their attempt.
 * @param score The percentage score of how well their attempt matched the original.
 */
@Immutable
data class PlayerAttempt(
    val playerName: String,
    val attemptFilePath: String,
    val reversedAttemptFilePath: String? = null,
    val score: Int = 0, // Default score to 0 until it's calculated
    val challengeType: ChallengeType // <-- ADD THIS LINE
)