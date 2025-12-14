package com.quokkalabs.reversey.data.models

import androidx.compose.runtime.Immutable
// 🎯 DUAL PIPELINE CHANGE 1A: Add VocalAnalysis import
import com.quokkalabs.reversey.scoring.VocalAnalysis

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
    REVERSE
}

@Immutable
data class Recording(
    val name: String,
    val originalPath: String,
    val reversedPath: String? = null,
    val attempts: List<PlayerAttempt> = emptyList(),
    val vocalAnalysis: VocalAnalysis? = null,  // 🎯 DUAL PIPELINE CHANGE 1B: Stores vocal mode detected from original recording

    // 🗣️ PHASE 3: ASR Transcription for Forward Speech content scoring
    val referenceTranscription: String? = null,      // What the challenge phrase IS
    val transcriptionConfidence: Float? = null,      // ASR confidence (0.0-1.0)
    val transcriptionPending: Boolean = false,       // True if offline at record time, needs transcription

    // 🎯 PHASE 1: Trimmed duration for timed recording countdown
    val trimmedDurationMs: Long? = null              // Duration after silence trimming
)