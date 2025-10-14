package com.example.reversey

import androidx.compose.runtime.Immutable

/**
 * Represents a single recording session.
 *
 * This now includes a list of player attempts for the game mode.
 *
 * @param name The display name of the recording.
 * @param originalPath The absolute file path to the original audio recording.
 * @param reversedPath The absolute file path to the reversed version of the audio.
 * @param attempts A list of attempts made by players in game mode.
 */
@Immutable
data class Recording(
    val name: String,
    val originalPath: String,
    val reversedPath: String? = null,
    val attempts: List<PlayerAttempt> = emptyList()
)
