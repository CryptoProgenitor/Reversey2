package com.example.reversey.testing

import android.util.Log
import com.example.reversey.audio.processing.AudioProcessor
import com.example.reversey.scoring.*
import java.io.File
import kotlin.math.sqrt
import com.example.reversey.audio.AudioConstants

/**
 * VocalModeDetector Auto-Tuner BIT Module
 *
 * Systematically optimizes VocalModeDetector parameters using labeled training data
 * to achieve optimal speech/singing classification accuracy.
 *
 * Ed's ReVerseY Project - GLUTE Architecture
 * ENHANCED: Audio caching for 10-50x performance improvement
 */

data class TuningParameters(
    val speechThreshold: Float,
    val singingThreshold: Float,
    val stabilityWeight: Float,    // singing stability contribution weight
    val contourWeight: Float,      // singing contour contribution weight
    val voicedWeight: Float,       // singing voiced contribution weight
    val contourNormalizer: Float,  // pitch contour normalization factor
    val mfccNormalizer: Float      // MFCC variance normalization factor
) {
    override fun toString(): String =
        "speech=${speechThreshold}, singing=${singingThreshold}, " +
                "weights=[${stabilityWeight},${contourWeight},${voicedWeight}], " +
                "norm=[${contourNormalizer},${mfccNormalizer}]"
}

data class TrainingSample(
    val file: File,
    val expectedMode: VocalMode,
    val description: String
)

data class TuningResult(
    val parameters: TuningParameters,
    val accuracy: Float,          // 0.0-1.0
    val correctClassifications: Int,
    val totalSamples: Int,
    val detailReport: String
)

/**
 * Cached audio analysis data to avoid reprocessing same files 6400+ times
 */
data class CachedAudioData(
    val audioFrames: List<FloatArray>,
    val pitches: List<Float>,
    val mfccFrames: List<FloatArray>
)

class VocalModeDetectorTuner(
    private val audioProcessor: AudioProcessor
) {

    companion object {
        private const val TAG = "VocalModeDetectorTuner"
    }

    private val trainingSamples = mutableListOf<TrainingSample>()
    private val cachedAudioData = mutableMapOf<String, CachedAudioData>()

    /**
     * Load training dataset from assets folder
     */
    fun loadTrainingDataFromAssets(context: android.content.Context): Boolean {
        trainingSamples.clear()
        cachedAudioData.clear()

        try {
            val assetManager = context.assets
            val trainingFiles = assetManager.list("training_data") ?: return false

            // Load speech samples
            for (i in 1..5) {
                val fileName = "speech_${i.toString().padStart(2, '0')}"
                val file = trainingFiles.find { it.startsWith(fileName) }
                if (file != null) {
                    val description = when(i) {
                        1 -> "quick brown fox"
                        2 -> "counting"
                        3 -> "ReVerseY description"
                        4 -> "don't like mondays"
                        5 -> "slowly clearly"
                        else -> "speech sample $i"
                    }
                    // Create temp file from assets
                    val tempFile = createTempFileFromAsset(context, "training_data/$file")
                    trainingSamples.add(TrainingSample(tempFile, VocalMode.SPEECH, description))
                    Log.d(TAG, "Loaded speech sample: $file ($description)")
                } else {
                    Log.w(TAG, "Missing speech sample: $fileName")
                }
            }

            // Load singing samples
            for (i in 1..5) {
                val fileName = "singing_${i.toString().padStart(2, '0')}"
                val file = trainingFiles.find { it.startsWith(fileName) }
                if (file != null) {
                    val description = when(i) {
                        1 -> "happy birthday"
                        2 -> "twinkle twinkle"
                        3 -> "do re mi"
                        4 -> "mary had lamb"
                        5 -> "humming"
                        else -> "singing sample $i"
                    }
                    // Create temp file from assets
                    val tempFile = createTempFileFromAsset(context, "training_data/$file")
                    trainingSamples.add(TrainingSample(tempFile, VocalMode.SINGING, description))
                    Log.d(TAG, "Loaded singing sample: $file ($description)")
                } else {
                    Log.w(TAG, "Missing singing sample: $fileName")
                }
            }

            Log.i(TAG, "Loaded ${trainingSamples.size} training samples")
            return trainingSamples.size == 10

        } catch (e: Exception) {
            Log.e(TAG, "Error loading training data from assets: ${e.message}")
            return false
        }
    }

    /**
     * Pre-process all training audio files and cache results
     * This is the KEY PERFORMANCE IMPROVEMENT - process once, use 6400+ times!
     */
    fun preProcessTrainingData(progressCallback: ((String) -> Unit)? = null): Boolean {
        Log.i(TAG, "Pre-processing ${trainingSamples.size} audio files for caching...")
        cachedAudioData.clear()

        trainingSamples.forEachIndexed { index, sample ->
            try {
                progressCallback?.invoke("Processing ${sample.description} (${index + 1}/${trainingSamples.size})")

                val (audioData, sampleRate) = readWavFile(sample.file)
                if (audioData.isEmpty()) {
                    Log.e(TAG, "Empty audio data for ${sample.file.name}")
                    return false
                }

                // 🔥 APPLY SILENCE TRIMMING TO TRAINING DATA
                val trimmed = trimLeadingSilence(audioData)
                val skip = (sampleRate * 0.1).toInt()
                val finalData = if (skip < trimmed.size) trimmed.copyOfRange(skip, trimmed.size) else trimmed

                // Process audio in frames (same as VocalModeDetector)
                val frameSize = 1024
                val hopSize = 512
                val audioFrames = mutableListOf<FloatArray>()
                val pitches = mutableListOf<Float>()
                val mfccFrames = mutableListOf<FloatArray>()

                var i = 0
                while (i + frameSize <= finalData.size) {
                    val frame = finalData.sliceArray(i until i + frameSize)
                    audioFrames.add(frame)

                    val pitch = audioProcessor.extractPitchYIN(frame, sampleRate)
                    pitches.add(pitch)

                    val mfcc = audioProcessor.extractMFCC(frame, sampleRate)
                    mfccFrames.add(mfcc)

                    i += hopSize
                }

                cachedAudioData[sample.file.name] = CachedAudioData(audioFrames, pitches, mfccFrames)
                Log.d(TAG, "Cached ${sample.file.name}: ${audioFrames.size} frames")

            } catch (e: Exception) {
                Log.e(TAG, "Error pre-processing ${sample.file.name}: ${e.message}")
                return false
            }
        }

        Log.i(TAG, "Pre-processing complete: ${cachedAudioData.size} files cached")
        return true
    }

    /**
     * 🔥 Trim leading silence (same as VocalModeDetector)
     */
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

    /**
     * Create temporary file from asset for processing
     */
    private fun createTempFileFromAsset(context: android.content.Context, assetPath: String): File {
        val tempFile = File(context.cacheDir, assetPath.replace("/", "_"))
        context.assets.open(assetPath).use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return tempFile
    }


    /**
     * Read WAV file - copied exactly from VocalModeDetector
     */
    private fun readWavFile(file: File): Pair<FloatArray, Int> {
        try {
            // 🎯 FIX: Safety check against memory limit (OOM protection)
            if (file.length() > AudioConstants.MAX_LOADABLE_AUDIO_BYTES) {
                Log.w(TAG, "File too large for processing: ${file.length()}")
                return Pair(floatArrayOf(), AudioConstants.SAMPLE_RATE)
            }

            val bytes = file.readBytes()

            // 🎯 FIX: Use constant instead of 44
            if (bytes.size < AudioConstants.WAV_HEADER_SIZE) {
                return Pair(floatArrayOf(), AudioConstants.SAMPLE_RATE)
            }

            val sampleRate = java.nio.ByteBuffer.wrap(bytes, 24, 4)
                .order(java.nio.ByteOrder.LITTLE_ENDIAN).int

            // 🎯 FIX: Use constant instead of 44
            val pcmData = bytes.sliceArray(AudioConstants.WAV_HEADER_SIZE until bytes.size)
            val audioData = FloatArray(pcmData.size / 2)
            for (i in audioData.indices) {
                val sample = ((pcmData[i * 2 + 1].toInt() shl 8) or
                        (pcmData[i * 2].toInt() and 0xFF)).toShort()
                audioData[i] = sample.toFloat() / Short.MAX_VALUE
            }

            return Pair(audioData, sampleRate)

        } catch (e: Exception) {
            Log.e(TAG, "Error reading WAV: ${e.message}")
            return Pair(floatArrayOf(), AudioConstants.SAMPLE_RATE)
        }
    }

    /**
     * Find optimal parameters through grid search
     */
    fun findOptimalParameters(progressCallback: ((String) -> Unit)? = null): TuningResult? {
        if (trainingSamples.isEmpty()) {
            Log.e(TAG, "No training samples loaded")
            return null
        }

        if (cachedAudioData.isEmpty()) {
            Log.e(TAG, "No cached audio data - call preProcessTrainingData() first")
            return null
        }

        Log.i(TAG, "Starting parameter grid search...")

        val ranges = ParameterSearchRanges()
        var bestResult: TuningResult? = null
        var totalCombinations = 0
        var testedCombinations = 0

        // Calculate total combinations for progress tracking
        totalCombinations = ranges.speechThresholds.size *
                ranges.singingThresholds.size *
                ranges.stabilityWeights.size *
                ranges.contourWeights.size *
                ranges.voicedWeights.size *
                ranges.contourNormalizers.size *
                ranges.mfccNormalizers.size

        Log.i(TAG, "Testing $totalCombinations parameter combinations...")

        for (speechThresh in ranges.speechThresholds) {
            for (singingThresh in ranges.singingThresholds) {
                for (stabilityWeight in ranges.stabilityWeights) {
                    for (contourWeight in ranges.contourWeights) {
                        for (voicedWeight in ranges.voicedWeights) {
                            for (contourNorm in ranges.contourNormalizers) {
                                for (mfccNorm in ranges.mfccNormalizers) {

                                    val params = TuningParameters(
                                        speechThresh, singingThresh,
                                        stabilityWeight, contourWeight, voicedWeight,
                                        contourNorm, mfccNorm
                                    )

                                    val result = evaluateParameters(params)
                                    testedCombinations++

                                    if (bestResult == null || result.accuracy > bestResult.accuracy) {
                                        bestResult = result
                                        Log.i(TAG, "New best accuracy: ${result.accuracy} with params: $params")
                                    }

                                    // Progress update every 100 tests
                                    if (testedCombinations % 100 == 0) {
                                        val progress = (testedCombinations * 100f / totalCombinations).toInt()
                                        progressCallback?.invoke("Tested $testedCombinations/$totalCombinations ($progress%) - Best: ${bestResult?.accuracy ?: 0f}")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Log.i(TAG, "Grid search complete. Best accuracy: ${bestResult?.accuracy}")
        return bestResult
    }

    /**
     * Evaluate a specific parameter set
     */
    private fun evaluateParameters(params: TuningParameters): TuningResult {
        val detector = CustomVocalModeDetector(audioProcessor, params)
        var correctClassifications = 0
        val results = mutableListOf<String>()

        trainingSamples.forEach { sample ->
            val cachedData = cachedAudioData[sample.file.name]
            if (cachedData != null) {
                val analysis = detector.classifyVocalModeFromCache(cachedData)
                val correct = analysis.mode == sample.expectedMode
                if (correct) correctClassifications++

                results.add("${sample.description}: expected=${sample.expectedMode}, got=${analysis.mode}, confidence=${analysis.confidence} ${if (correct) "✓" else "✗"}")
            }
        }

        val accuracy = correctClassifications.toFloat() / trainingSamples.size
        val detailReport = results.joinToString("\n")

        return TuningResult(params, accuracy, correctClassifications, trainingSamples.size, detailReport)
    }

    /**
     * Generate code snippet for applying the optimal parameters
     */
    fun generateOptimizedCode(result: TuningResult): String {
        return """
// OPTIMIZED PARAMETERS - Generated by VocalModeDetectorTuner
// Accuracy: ${result.accuracy} (${result.correctClassifications}/${result.totalSamples} correct)

// 1. In classifyFeatures() method, update thresholds:
val speechThreshold = ${result.parameters.speechThreshold}f
val singingThreshold = ${result.parameters.singingThreshold}f

// 2. In classifyFeatures() method, update singing score calculation:
val singingStabilityContrib = features.pitchStability * ${result.parameters.stabilityWeight}f
val singingContourContrib = features.pitchContour * ${result.parameters.contourWeight}f
val singingVoicedContrib = features.voicedRatio * ${result.parameters.voicedWeight}f

// 3. In extractVocalFeatures() method, update contour normalization:
val contour = (avgInterval / ${result.parameters.contourNormalizer}f).coerceIn(0f, 1f)

// 4. In extractVocalFeatures() method, update MFCC normalization:
(calculateMFCCVariance(mfccFrames) / ${result.parameters.mfccNormalizer}f).coerceIn(0f, 1f)

/* CLASSIFICATION RESULTS:
${result.detailReport}
*/
        """.trimIndent()
    }
}

/**
 * Parameter search ranges for grid search optimization
 */
private class ParameterSearchRanges {
    // Confidence thresholds
    val speechThresholds = listOf(0.20f, 0.25f, 0.30f, 0.35f)
    val singingThresholds = listOf(0.30f, 0.35f, 0.40f, 0.45f)

    // Singing score weights (must sum to reasonable total)
    val stabilityWeights = listOf(0.2f, 0.3f, 0.4f, 0.5f)
    val contourWeights = listOf(0.2f, 0.3f, 0.4f, 0.5f)
    val voicedWeights = listOf(0.2f, 0.3f, 0.4f, 0.5f)

    // Normalization factors
    val contourNormalizers = listOf(15f, 18f, 20f, 25f, 30f)
    val mfccNormalizers = listOf(200f, 250f, 300f, 350f, 400f)
}

/**
 * Custom VocalModeDetector for testing with silence trimming
 */
private class CustomVocalModeDetector(
    private val audioProcessor: AudioProcessor,
    private val customParams: TuningParameters
) {

    /**
     * Classify from cached audio data
     */
    fun classifyVocalModeFromCache(cachedData: CachedAudioData): VocalAnalysis {
        try {
            // Extract features with custom parameters using cached data
            val features = extractVocalFeaturesCustom(cachedData.audioFrames, cachedData.pitches, cachedData.mfccFrames)

            // Classify with custom logic
            val (mode, confidence) = classifyFeaturesCustom(features)

            return VocalAnalysis(mode, confidence, features)

        } catch (e: Exception) {
            return VocalAnalysis(VocalMode.UNKNOWN, 0f, VocalFeatures(0f, 0f, 0f, 0f))
        }
    }

    /**
     * Extract features with custom normalization parameters
     */
    private fun extractVocalFeaturesCustom(
        audioFrames: List<FloatArray>,
        pitches: List<Float>,
        mfccFrames: List<FloatArray>
    ): VocalFeatures {

        // 1. Pitch Stability
        val validPitches = pitches.filter { it > 0f }
        val pitchStability = if (validPitches.size >= 3) {
            val pitchStdDev = validPitches.stdDev()
            1f - (pitchStdDev / 50f).coerceIn(0f, 1f)
        } else 0f

        // 2. Pitch Contour (custom normalization)
        val pitchContour = if (validPitches.size >= 3) {
            val intervals = validPitches.zipWithNext { a, b -> kotlin.math.abs(a - b) }
            val avgInterval = intervals.average().toFloat()
            (avgInterval / customParams.contourNormalizer).coerceIn(0f, 1f)
        } else 0f

        // 3. MFCC Spread (custom normalization)
        val mfccSpread = if (mfccFrames.size >= 2) {
            (audioProcessor.calculateMFCCVariance(mfccFrames) / customParams.mfccNormalizer).coerceIn(0f, 1f)
        } else 0f

        // 4. Voiced Ratio
        val voicedRatio = if (pitches.isNotEmpty()) {
            validPitches.size.toFloat() / pitches.size
        } else 0f

        return VocalFeatures(pitchStability, pitchContour, mfccSpread, voicedRatio)
    }

    /**
     * 🔥 FIXED: Classify features with correct property names
     */
    private fun classifyFeaturesCustom(features: VocalFeatures): Pair<VocalMode, Float> {

        // Speech indicators - using CORRECT property names
        val speechStabilityContrib = (1f - features.pitchStability) * 0.4f
        val speechContourContrib = (1f - features.pitchContour) * 0.3f
        val speechMfccContrib = features.mfccSpread * 0.1f
        val speechScore = speechStabilityContrib + speechContourContrib + speechMfccContrib

        // Singing indicators (custom weights) - using CORRECT property names
        val singingStabilityContrib = features.pitchStability * customParams.stabilityWeight
        val singingContourContrib = features.pitchContour * customParams.contourWeight
        val singingVoicedContrib = features.voicedRatio * customParams.voicedWeight
        val singingScore = singingStabilityContrib + singingContourContrib + singingVoicedContrib

        // 🔥 FIXED: Simple comparison without operator issues
        // REPLACE the entire when block with:
        return when {
            speechScore >= customParams.speechThreshold && singingScore >= customParams.singingThreshold -> {
                if (speechScore >= singingScore) VocalMode.SPEECH to speechScore else VocalMode.SINGING to singingScore
            }
            speechScore >= customParams.speechThreshold -> VocalMode.SPEECH to speechScore
            singingScore >= customParams.singingThreshold -> VocalMode.SINGING to singingScore
            else -> VocalMode.SPEECH to 0.25f
        }
    }

    /**
     * Standard deviation calculation
     */
    private fun List<Float>.stdDev(): Float {
        if (isEmpty()) return 0f
        val mean = average().toFloat()
        val variance = sumOf { (it - mean).toDouble() * (it - mean).toDouble() }.toFloat() / size
        return sqrt(variance)
    }
}