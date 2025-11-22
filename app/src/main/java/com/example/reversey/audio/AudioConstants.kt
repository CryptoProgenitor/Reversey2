package com.example.reversey.audio

import android.media.AudioFormat // 🎯 MANDATORY: Required to define CHANNEL_CONFIG/AUDIO_FORMAT

/**
 * Centralized configuration for Audio parameters.
 * Defines safety limits, formats, and thresholds to prevent OutOfMemory (OOM) errors.
 */
object AudioConstants {
    // Standard WAV header size (RIFF + fmt + data)
    const val WAV_HEADER_SIZE = 44

    // Minimum file size to be considered a valid recording (1KB)
    // Prevents processing empty or corrupted header-only files
    const val MIN_VALID_RECORDING_SIZE = 1024L

    // Max size to load into memory (10MB)
    // 10MB on disk ≈ 2 mins of Mono 44.1kHz audio
    // Peak RAM usage during load ≈ 30MB (10MB Byte Array + 20MB Float Array)
    const val MAX_LOADABLE_AUDIO_SIZE_MB = 10
    const val MAX_LOADABLE_AUDIO_BYTES = MAX_LOADABLE_AUDIO_SIZE_MB * 1024 * 1024L

    // Warning Threshold (90%) - Trigger "Approaching Limit" Toast
    const val WARNING_THRESHOLD_BYTES = (MAX_LOADABLE_AUDIO_BYTES * 0.9).toLong()

    // How many amplitude points to keep for the visualizer
    const val MAX_WAVEFORM_SAMPLES = 60

    // Audio Format Settings
    // CRITICAL: Ensure these match the AudioFormat in AudioRecorderHelper
    const val SAMPLE_RATE = 44100
    const val BYTES_PER_SAMPLE = 2 // 16-bit PCM
    const val CHANNELS = 1 // Mono (Change to 2 if recording Stereo)

    // 🎯 FIX: Added missing audio format constants for RecordingRepository
    const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
    const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT

    // Calculated Durations (Used for Timer checks)
    // Formula: Seconds = Bytes / (Rate * Channels * BytesPerSample)
    val MAX_RECORDING_DURATION_MS: Long = (MAX_LOADABLE_AUDIO_BYTES * 1000) / (SAMPLE_RATE * CHANNELS * BYTES_PER_SAMPLE)
    val WARNING_DURATION_MS: Long = (WARNING_THRESHOLD_BYTES * 1000) / (SAMPLE_RATE * CHANNELS * BYTES_PER_SAMPLE)
}