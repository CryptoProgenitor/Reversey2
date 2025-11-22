package com.example.reversey.audio

import android.media.AudioFormat // MANDATORY: Needed for CHANNEL_CONFIG/AUDIO_FORMAT

/**
 * Centralized configuration for Audio parameters.
 * Defines safety limits, formats, and thresholds to prevent OutOfMemory (OOM) errors.
 */
object AudioConstants {
    // Standard WAV header size (RIFF + fmt + data)
    const val WAV_HEADER_SIZE = 44

    // Minimum file size to be considered a valid recording (1KB)
    const val MIN_VALID_RECORDING_SIZE = 1024L

    // Max size to load into memory (10MB)
    const val MAX_LOADABLE_AUDIO_SIZE_MB = 10
    const val MAX_LOADABLE_AUDIO_BYTES = MAX_LOADABLE_AUDIO_SIZE_MB * 1024 * 1024L

    // Warning Threshold (90%)
    const val WARNING_THRESHOLD_BYTES = (MAX_LOADABLE_AUDIO_BYTES * 0.9).toLong()

    // Visualizer settings
    const val MAX_WAVEFORM_SAMPLES = 60

    // Audio Format Settings
    const val SAMPLE_RATE = 44100
    const val BYTES_PER_SAMPLE = 2 // 16-bit PCM
    const val CHANNELS = 1 // Mono

    // 🎯 CRITICAL FIX: The missing definitions needed by RecordingRepository.kt
    const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
    const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT

    // Calculated Durations
    val MAX_RECORDING_DURATION_MS: Long = (MAX_LOADABLE_AUDIO_BYTES * 1000) / (SAMPLE_RATE * CHANNELS * BYTES_PER_SAMPLE)
    val WARNING_DURATION_MS: Long = (WARNING_THRESHOLD_BYTES * 1000) / (SAMPLE_RATE * CHANNELS * BYTES_PER_SAMPLE)
}