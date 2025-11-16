package com.example.reversey.data.models

import androidx.compose.runtime.Immutable
import com.example.reversey.data.models.PlayerAttempt
// 🎯 DUAL PIPELINE CHANGE 1A: Add VocalAnalysis import
import com.example.reversey.scoring.VocalAnalysis

/**
 * Represents a single recording session.
 *
 * This now includes a list of player attempts for the game mode.
 *
 * @param name The display name of the recording.
 * @param originalPath The absolute file path to the original audio recording.
 * @param reversedPath The absolute file path to the reversed version of the audio.
 * @param attempts A list of attempts made by players in game mode.
 * @param vocalAnalysis The vocal mode analysis from the original recording for dual pipeline routing.
 */

enum class ChallengeType {
    REVERSE,
    FORWARD
}

@Immutable
data class Recording(
    val name: String,
    val originalPath: String,
    val reversedPath: String? = null,
    val attempts: List<PlayerAttempt> = emptyList(),
    val vocalAnalysis: VocalAnalysis? = null  // 🎯 DUAL PIPELINE CHANGE 1B: Stores vocal mode detected from original recording
)