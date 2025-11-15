package com.example.reversey.scoring

import android.util.Log
import com.example.reversey.audio.processing.AudioProcessor
import kotlin.math.sqrt

/**
 * Vocal Mode Classification Results
 */
data class VocalAnalysis(
    val mode: VocalMode,
    val confidence: Float,        // 0.0-1.0 confidence in classification
    val features: VocalFeatures
)

/**
 * Classification categories for vocal input
 */
enum class VocalMode {
    SPEECH,   // Conversational patterns - route to SpeechScoringEngine
    SINGING,  // Melodic patterns - route to SingingScoringEngine
    UNKNOWN   // Unclear pattern - use fallback strategy
}

/**
 * Extracted vocal characteristics for classification
 */
data class VocalFeatures(
    val pitchStability: Float,    // 0.0-1.0: Low = speech, High = singing
    val pitchContour: Float,      // 0.0-1.0: Melodic movement intensity
    val mfccSpread: Float,        // 0.0-1.0: Timbre variation complexity
    val voicedRatio: Float        // 0.0-1.0: Percentage of voiced frames
)

/**
 * Detection thresholds for vocal mode classification
 */
data class VocalDetectionParameters(
    // Classification thresholds - LOWERED FOR TESTING
    val speechConfidenceThreshold: Float = 0.2f,  // Lowered from 0.3f for better speech detection
    val singingConfidenceThreshold: Float = 0.4f,  // Lowered from 0.4f for better singing detection

    // Feature analysis parameters
    val pitchStabilityThreshold: Float = 0.4f,
    val pitchContourThreshold: Float = 0.5f,
    val mfccSpreadThreshold: Float = 0.3f,
    val voicedRatioThreshold: Float = 0.6f
)

/**
 * Standalone Vocal Mode Detector
 *
 * Analyzes audio features to classify speech vs singing patterns.
 * Routes to appropriate scoring engine based on vocal characteristics.
 */
class VocalModeDetector(
    private val audioProcessor: AudioProcessor,
    private val parameters: VocalDetectionParameters = VocalDetectionParameters()
) {

    /**
     * Classify vocal input from audio file as SPEECH, SINGING, or UNKNOWN
     *
     * @param audioFile WAV file to analyze
     * @return VocalAnalysis with classification and confidence
     */
    fun classifyVocalMode(audioFile: java.io.File): VocalAnalysis {
        try {
            Log.d("VocalModeDetector", "Analyzing file: ${audioFile.name}")

            // Read WAV file and extract audio data
            val (audioData, sampleRate) = readWavFile(audioFile)
            if (audioData.isEmpty()) {
                Log.w("VocalModeDetector", "No audio data found in file")
                return VocalAnalysis(VocalMode.UNKNOWN, 0f, VocalFeatures(0f, 0f, 0f, 0f))
            }

            // Process audio in frames for analysis
            val frameSize = 1024
            val hopSize = 512
            val audioFrames = mutableListOf<FloatArray>()
            val pitches = mutableListOf<Float>()
            val mfccFrames = mutableListOf<FloatArray>()

            // Window the audio into frames
            var i = 0
            while (i + frameSize <= audioData.size) {
                val frame = audioData.sliceArray(i until i + frameSize)
                audioFrames.add(frame)

                // Extract pitch for this frame
                val pitch = audioProcessor.extractPitchYIN(frame, sampleRate)
                if (i % 50 == 0) Log.d("VocalModeDetector", "Frame $i: pitch=$pitch") // Debug every 50th frame
                pitches.add(pitch)

                // Extract MFCC for this frame
                val mfcc = audioProcessor.extractMFCC(frame, sampleRate)
                mfccFrames.add(mfcc)

                i += hopSize
            }

            Log.d("VocalModeDetector", "Processed ${audioFrames.size} frames, ${pitches.size} pitches")

            // Use existing classification method
            return classifyVocalMode(audioFrames, pitches, mfccFrames)

        } catch (e: Exception) {
            Log.e("VocalModeDetector", "Error analyzing file ${audioFile.name}: ${e.message}")
            return VocalAnalysis(
                VocalMode.UNKNOWN,
                0f,
                VocalFeatures(0f, 0f, 0f, 0f)
            )
        }
    }

    /**
     * Read WAV file and return audio data with sample rate
     */
    private fun readWavFile(file: java.io.File): Pair<FloatArray, Int> {
        try {
            val bytes = file.readBytes()
            if (bytes.size < 44) {
                return Pair(floatArrayOf(), 44100) // Empty with default sample rate
            }

            // Parse WAV header to get sample rate
            val sampleRate = java.nio.ByteBuffer.wrap(bytes, 24, 4)
                .order(java.nio.ByteOrder.LITTLE_ENDIAN).int

            // Extract PCM data (skip 44-byte header)
            val pcmData = bytes.sliceArray(44 until bytes.size)

            // Convert 16-bit PCM to FloatArray
            val audioData = FloatArray(pcmData.size / 2)
            for (i in audioData.indices) {
                val sample = ((pcmData[i * 2 + 1].toInt() shl 8) or
                        (pcmData[i * 2].toInt() and 0xFF)).toShort()
                audioData[i] = sample.toFloat() / Short.MAX_VALUE
            }

            return Pair(audioData, sampleRate)

        } catch (e: Exception) {
            Log.e("VocalModeDetector", "Error reading WAV file: ${e.message}")
            return Pair(floatArrayOf(), 44100)
        }
    }

    /**
     * Classify vocal input as SPEECH, SINGING, or UNKNOWN
     *
     * @param audioFrames Audio data frames
     * @param pitches Extracted pitch sequence
     * @param mfccFrames MFCC feature frames
     * @return VocalAnalysis with classification and confidence
     */
    fun classifyVocalMode(
        audioFrames: List<FloatArray>,
        pitches: List<Float>,
        mfccFrames: List<FloatArray>
    ): VocalAnalysis {

        Log.d("VocalModeDetector", "=== VOCAL MODE ANALYSIS ===")

        // Extract features for classification
        val features = extractVocalFeatures(audioFrames, pitches, mfccFrames)

        Log.d("VocalModeDetector", "Features: stability=${features.pitchStability}, contour=${features.pitchContour}, mfcc=${features.mfccSpread}, voiced=${features.voicedRatio}")

        // Apply classification logic
        val (mode, confidence) = classifyFeatures(features)

        Log.d("VocalModeDetector", "Classification: $mode (confidence: $confidence)")
        Log.d("VocalModeDetector", "==========================")

        return VocalAnalysis(mode, confidence, features)
    }

    /**
     * Extract vocal characteristics from audio data
     */
    private fun extractVocalFeatures(
        audioFrames: List<FloatArray>,
        pitches: List<Float>,
        mfccFrames: List<FloatArray>
    ): VocalFeatures {

        // 1. Pitch Stability (speech = unstable, singing = stable)
        val validPitches = pitches.filter { it > 0f }
        val pitchStability = if (validPitches.size >= 3) {
            val pitchStdDev = validPitches.stdDev()
            1f - (pitchStdDev / 50f).coerceIn(0f, 1f) // Normalize and invert
        } else 0f

        // 2. Pitch Contour (melodic movement patterns)
        val pitchContour = if (validPitches.size >= 3) {
            analyzePitchContour(validPitches)
        } else 0f

        // 3. MFCC Spread (timbre variation)
        val mfccSpread = if (mfccFrames.size >= 2) {
            (audioProcessor.calculateMFCCVariance(mfccFrames) / 350f).coerceIn(0f, 1f) // Fixed: /300f for proper 13-coeff MFCC normalization
        } else 0f

        // 4. Voiced Ratio (percentage of voiced frames)
        val voicedRatio = if (pitches.isNotEmpty()) {
            validPitches.size.toFloat() / pitches.size
        } else 0f

        Log.d("VocalModeDetector", "RAW FEATURES: Stability=$pitchStability | Contour=$pitchContour | MFCCSpread=$mfccSpread | VoicedRatio=$voicedRatio")
        return VocalFeatures(pitchStability, pitchContour, mfccSpread, voicedRatio)
    }

    /**
     * Classify features into vocal mode with confidence
     */
    private fun classifyFeatures(features: VocalFeatures): Pair<VocalMode, Float> {

        // Speech indicators: low stability, low contour, varied timbre
        val speechStabilityContrib = (1f - features.pitchStability) * 0.4f
        val speechContourContrib = (1f - features.pitchContour) * 0.3f
        val speechMfccContrib = features.mfccSpread * 0.1f
        val speechScore = speechStabilityContrib + speechContourContrib + speechMfccContrib

        // Singing indicators: high stability, melodic contour, sustained voicing
        val singingStabilityContrib = features.pitchStability * 0.2f
        val singingContourContrib = features.pitchContour * 0.3f
        val singingVoicedContrib = features.voicedRatio * 0.5f// was 0.5f
        val singingScore = singingStabilityContrib + singingContourContrib + singingVoicedContrib

        Log.d("VocalModeDetector", "DECISION PROCESS: SpeechScore=$speechScore | SingingScore=$singingScore | Speech≥${parameters.speechConfidenceThreshold}? ${speechScore >= parameters.speechConfidenceThreshold} | Singing≥${parameters.singingConfidenceThreshold}? ${singingScore >= parameters.singingConfidenceThreshold}")
        Log.d("VocalModeDetector", "SPEECH: ${speechStabilityContrib} + ${speechContourContrib} + ${speechMfccContrib} = $speechScore")
        Log.d("VocalModeDetector", "SINGING: ${singingStabilityContrib} + ${singingContourContrib} + ${singingVoicedContrib} = $singingScore")
        Log.d("VocalModeDetector", "THRESHOLDS: speech=${parameters.speechConfidenceThreshold}, singing=${parameters.singingConfidenceThreshold}")

        return when {
            speechScore > parameters.speechConfidenceThreshold && singingScore > parameters.singingConfidenceThreshold -> {
                val result = if (speechScore > singingScore) VocalMode.SPEECH to speechScore else VocalMode.SINGING to singingScore
                val margin = kotlin.math.abs(speechScore - singingScore)
                Log.d("VocalModeDetector", "BOTH QUALIFY: Selected ${result.first} with margin=$margin")
                result
            }
            speechScore > parameters.speechConfidenceThreshold -> {
                val margin = speechScore - parameters.speechConfidenceThreshold
                Log.d("VocalModeDetector", "SPEECH ONLY: confidence=$speechScore, margin above threshold=$margin")
                VocalMode.SPEECH to speechScore
            }
            singingScore > parameters.singingConfidenceThreshold -> {
                val margin = singingScore - parameters.singingConfidenceThreshold
                Log.d("VocalModeDetector", "SINGING ONLY: confidence=$singingScore, margin above threshold=$margin")
                VocalMode.SINGING to singingScore
            }
            else -> {
                val speechGap = parameters.speechConfidenceThreshold - speechScore
                val singingGap = parameters.singingConfidenceThreshold - singingScore
                Log.d("VocalModeDetector", "DEFAULTING TO SPEECH - speechGap=$speechGap, singingGap=$singingGap")
                VocalMode.SPEECH to 0.25f
            }
        }
    }

    /**
     * Analyze pitch movement patterns for melodic characteristics
     */
    private fun analyzePitchContour(pitches: List<Float>): Float {
        val validPitches = pitches.filter { it > 0f }
        if (validPitches.size < 3) return 0f

        Log.d("VocalModeDetector", "Valid pitches count: ${validPitches.size} out of ${pitches.size}")
        Log.d("VocalModeDetector", "First 10 valid pitches: ${validPitches.take(10)}")

        val intervals = validPitches.zipWithNext { a, b -> kotlin.math.abs(a - b) }
        val avgInterval = intervals.average().toFloat()

        Log.d("VocalModeDetector", "First 10 intervals: ${intervals.take(10)}")
        Log.d("VocalModeDetector", "Average interval: $avgInterval Hz")

        val contour = (avgInterval / 15f).coerceIn(0f, 1f)  // Boosted: /12f instead of /20f for better melodic sensitivity
        Log.d("VocalModeDetector", "Final contour: $contour")

        return contour
    }

    /**
     * Calculate standard deviation for pitch analysis
     */
    private fun List<Float>.stdDev(): Float {
        if (isEmpty()) return 0f
        val mean = average().toFloat()
        val variance = sumOf { (it - mean).toDouble() * (it - mean).toDouble() }.toFloat() / size
        return sqrt(variance)
    }
}