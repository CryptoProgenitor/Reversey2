package com.quokkalabs.reversey.scoring

import android.util.Log
import com.quokkalabs.reversey.audio.AudioConstants
import com.quokkalabs.reversey.audio.processing.AudioProcessor
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject
import kotlin.math.sqrt

/**
 * Vocal Mode Classification Results
 */
data class VocalAnalysis(
    val mode: VocalMode,
    val confidence: Float,
    val features: VocalFeatures
)

enum class VocalMode { SPEECH, SINGING, UNKNOWN }

data class VocalFeatures(
    val pitchStability: Float,
    val pitchContour: Float,
    val mfccSpread: Float,
    val voicedRatio: Float
)

data class VocalDetectionParameters(
    val speechConfidenceThreshold: Float = 0.2f,
    val singingConfidenceThreshold: Float = 0.4f,
    val pitchStabilityThreshold: Float = 0.4f,
    val pitchContourThreshold: Float = 0.5f,
    val mfccSpreadThreshold: Float = 0.3f,
    val voicedRatioThreshold: Float = 0.6f,
    // 🎯 FIX: Extracted Magic Numbers
    val minAudioLengthSamples: Int = 2048,
    val mfccNormalizationFactor: Float = 350f
)

class VocalModeDetector @Inject constructor(
    private val audioProcessor: AudioProcessor,
    private val parameters: VocalDetectionParameters = VocalDetectionParameters()
) {

    // --- ⚡ NEW: In-Memory Entry Point (High Performance) ---
    fun classifyVocalMode(rawAudioData: FloatArray, sampleRate: Int): VocalAnalysis {
        try {
            // 1. Trim leading silence / transients
            val trimmed = trimLeadingSilence(rawAudioData)
            // Skip first 100ms to avoid mic pop
            val skip = (sampleRate * 0.1).toInt()
            val audioData = if (skip < trimmed.size) trimmed.copyOfRange(skip, trimmed.size) else trimmed

            // 🎯 FIX: Use parameter instead of magic number 2048
            if (audioData.size < parameters.minAudioLengthSamples) {
                Log.w("VocalModeDetector", "Too little audio after trimming")
                // Default to speech if too short, low confidence
                return VocalAnalysis(VocalMode.SPEECH, 0.2f, VocalFeatures(0f, 0f, 0f, 0f))
            }

            // 2. Process audio in frames
            val frameSize = 1024
            val hopSize = 512
            val audioFrames = mutableListOf<FloatArray>()
            val pitches = mutableListOf<Float>()
            val mfccFrames = mutableListOf<FloatArray>()

            var i = 0
            while (i + frameSize <= audioData.size) {
                val frame = audioData.sliceArray(i until i + frameSize)
                audioFrames.add(frame)

                // Extract pitch
                val pitch = audioProcessor.extractPitchYIN(frame, sampleRate)
                pitches.add(pitch)

                // Extract MFCC
                val mfcc = audioProcessor.extractMFCC(frame, sampleRate)
                mfccFrames.add(mfcc)

                i += hopSize
            }

            Log.d("VocalModeDetector", "InMemory Analysis: Processed ${audioFrames.size} frames")

            // 3. Classify using existing logic
            return classifyVocalMode(audioFrames, pitches, mfccFrames)

        } catch (e: Exception) {
            Log.e("VocalModeDetector", "Error analyzing memory buffer: ${e.message}")
            return VocalAnalysis(VocalMode.UNKNOWN, 0f, VocalFeatures(0f, 0f, 0f, 0f))
        }
    }

    // --- Legacy File Entry Point (Maintained for compatibility) ---
    fun classifyVocalMode(audioFile: File): VocalAnalysis {
        return try {
            val (rawAudioData, sampleRate) = readWavFile(audioFile)
            if (rawAudioData.isEmpty()) {
                Log.w("VocalModeDetector", "No audio data found in file")
                return VocalAnalysis(VocalMode.UNKNOWN, 0f, VocalFeatures(0f, 0f, 0f, 0f))
            }
            // Delegate to the new in-memory processing method
            classifyVocalMode(rawAudioData, sampleRate)
        } catch (e: Exception) {
            Log.e("VocalModeDetector", "Error analyzing file ${audioFile.name}: ${e.message}")
            VocalAnalysis(VocalMode.UNKNOWN, 0f, VocalFeatures(0f, 0f, 0f, 0f))
        }
    }

    // --- Internal Classification Logic ---
    fun classifyVocalMode(
        audioFrames: List<FloatArray>,
        pitches: List<Float>,
        mfccFrames: List<FloatArray>
    ): VocalAnalysis {
        val features = extractVocalFeatures(audioFrames, pitches, mfccFrames)
        val (mode, confidence) = classifyFeatures(features)
        return VocalAnalysis(mode, confidence, features)
    }

    private fun extractVocalFeatures(
        audioFrames: List<FloatArray>,
        pitches: List<Float>,
        mfccFrames: List<FloatArray>
    ): VocalFeatures {
        val validPitches = pitches.filter { it > 0f }

        // Pitch Stability
        val pitchStability = if (validPitches.size >= 3) {
            val pitchStdDev = validPitches.stdDev()
            1f - (pitchStdDev / 50f).coerceIn(0f, 1f)
        } else 0f

        // Pitch Contour
        val pitchContour = if (validPitches.size >= 3) {
            analyzePitchContour(validPitches)
        } else 0f

        // MFCC Spread
        val mfccSpread = if (mfccFrames.size >= 2) {
            // 🎯 FIX: Use parameter instead of magic number 350f
            (audioProcessor.calculateMFCCVariance(mfccFrames) / parameters.mfccNormalizationFactor).coerceIn(0f, 1f)
        } else 0f

        // Voiced Ratio
        val voicedRatio = if (pitches.isNotEmpty()) {
            validPitches.size.toFloat() / pitches.size
        } else 0f

        return VocalFeatures(pitchStability, pitchContour, mfccSpread, voicedRatio)
    }

    private fun classifyFeatures(features: VocalFeatures): Pair<VocalMode, Float> {
        val speechScore = (1f - features.pitchStability) * 0.4f +
                (1f - features.pitchContour) * 0.3f +
                features.mfccSpread * 0.1f

        val singingScore = features.pitchStability * 0.2f +
                features.pitchContour * 0.3f +
                features.voicedRatio * 0.5f

        return when {
            speechScore > parameters.speechConfidenceThreshold && singingScore > parameters.singingConfidenceThreshold -> {
                if (speechScore > singingScore) VocalMode.SPEECH to speechScore else VocalMode.SINGING to singingScore
            }
            speechScore > parameters.speechConfidenceThreshold -> VocalMode.SPEECH to speechScore
            singingScore > parameters.singingConfidenceThreshold -> VocalMode.SINGING to singingScore
            else -> VocalMode.SPEECH to 0.25f // Fallback
        }
    }

    // --- Helpers ---

    private fun analyzePitchContour(pitches: List<Float>): Float {
        val validPitches = pitches.filter { it > 0f }
        if (validPitches.size < 3) return 0f
        val intervals = validPitches.zipWithNext { a, b -> kotlin.math.abs(a - b) }
        val avgInterval = intervals.average().toFloat()
        return (avgInterval / 15f).coerceIn(0f, 1f)
    }

    private fun List<Float>.stdDev(): Float {
        if (isEmpty()) return 0f
        val mean = average().toFloat()
        // FIX: Convert calculation to Double for sumOf, then back to Float
        val variance = sumOf {
            val diff = it - mean
            (diff * diff).toDouble()
        }.toFloat() / size
        return sqrt(variance)
    }

    private fun trimLeadingSilence(
        data: FloatArray,
        threshold: Float = 0.003f,
        windowSize: Int = 1024
    ): FloatArray {
        var idx = 0
        val limit = data.size - windowSize
        while (idx < limit) {
            var maxAmp = 0f
            for (i in idx until idx + windowSize) {
                val v = kotlin.math.abs(data[i])
                if (v > maxAmp) maxAmp = v
            }
            if (maxAmp > threshold) break
            idx += windowSize
        }
        return data.copyOfRange(idx, data.size)
    }

    private fun readWavFile(file: File): Pair<FloatArray, Int> {
        try {
            // 🎯 FIX: Safety check against memory limit
            if (file.length() > AudioConstants.MAX_LOADABLE_AUDIO_BYTES) {
                Log.w("VocalModeDetector", "File too large for detection: ${file.length()}")
                return Pair(floatArrayOf(), AudioConstants.SAMPLE_RATE)
            }

            val bytes = FileInputStream(file).use { it.readBytes() }


            if (bytes.size < AudioConstants.WAV_HEADER_SIZE) return Pair(floatArrayOf(), AudioConstants.SAMPLE_RATE)

            val sampleRate = ByteBuffer.wrap(bytes, 24, 4)
                .order(ByteOrder.LITTLE_ENDIAN).int

            // 🎯 FIX: Use constant instead of 44
            val pcmData = bytes.sliceArray(AudioConstants.WAV_HEADER_SIZE until bytes.size)
            val audioData = FloatArray(pcmData.size / 2)
            for (i in audioData.indices) {
                val sample = ((pcmData[i * 2 + 1].toInt() shl 8) or (pcmData[i * 2].toInt() and 0xFF)).toShort()
                audioData[i] = sample.toFloat() / Short.MAX_VALUE
            }
            return Pair(audioData, sampleRate)
        } catch (e: Exception) {

            return Pair(floatArrayOf(), AudioConstants.SAMPLE_RATE)
        }
    }
}