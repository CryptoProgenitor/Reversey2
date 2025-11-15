package com.example.reversey.testing

import android.util.Log
import com.example.reversey.audio.processing.AudioProcessor
import com.example.reversey.scoring.*
import java.io.File
import kotlin.math.sqrt

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

                // Process audio in frames (same as CustomVocalModeDetector)
                val frameSize = 1024
                val hopSize = 512
                val audioFrames = mutableListOf<FloatArray>()
                val pitches = mutableListOf<Float>()
                val mfccFrames = mutableListOf<FloatArray>()

                var i = 0
                while (i + frameSize <= audioData.size) {
                    val frame = audioData.sliceArray(i until i + frameSize)
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
     * Read WAV file - copied exactly from your implementation
     */
    private fun readWavFile(file: File): Pair<FloatArray, Int> {
        try {
            val bytes = file.readBytes()
            if (bytes.size < 44) {
                return Pair(floatArrayOf(), 44100)
            }

            val sampleRate = java.nio.ByteBuffer.wrap(bytes, 24, 4)
                .order(java.nio.ByteOrder.LITTLE_ENDIAN).int

            val pcmData = bytes.sliceArray(44 until bytes.size)
            val audioData = FloatArray(pcmData.size / 2)
            for (i in audioData.indices) {
                val sample = ((pcmData[i * 2 + 1].toInt() shl 8) or
                        (pcmData[i * 2].toInt() and 0xFF)).toShort()
                audioData[i] = sample.toFloat() / Short.MAX_VALUE
            }

            return Pair(audioData, sampleRate)

        } catch (e: Exception) {
            return Pair(floatArrayOf(), 44100)
        }
    }

    /**
     * Test parameter configuration against training data
     * OPTIMIZED: Uses cached audio data instead of reprocessing files
     */
    fun testParameters(params: TuningParameters): TuningResult {
        var correct = 0
        val results = mutableListOf<String>()

        // Create custom detector with test parameters
        val customDetector = CustomVocalModeDetector(audioProcessor, params)

        trainingSamples.forEach { sample ->
            try {
                // Use cached audio data - this is the MASSIVE speedup!
                val cachedData = cachedAudioData[sample.file.name]
                val analysis = if (cachedData != null) {
                    customDetector.classifyVocalModeFromCache(cachedData)
                } else {
                    // Fallback to file processing if cache miss (shouldn't happen)
                    customDetector.classifyVocalMode(sample.file)
                }

                val isCorrect = analysis.mode == sample.expectedMode
                if (isCorrect) correct++

                results.add(
                    "${sample.file.name}: ${sample.expectedMode} → ${analysis.mode} " +
                            "(${(analysis.confidence * 100).toInt()}%) ${if (isCorrect) "✓" else "✗"}"
                )

                Log.d(TAG, "Test ${sample.description}: expected ${sample.expectedMode}, " +
                        "got ${analysis.mode} (${analysis.confidence}) ${if (isCorrect) "PASS" else "FAIL"}")

            } catch (e: Exception) {
                Log.e(TAG, "Error testing ${sample.file.name}: ${e.message}")
                results.add("${sample.file.name}: ERROR - ${e.message}")
            }
        }

        val accuracy = correct.toFloat() / trainingSamples.size
        val detailReport = results.joinToString("\n")

        return TuningResult(params, accuracy, correct, trainingSamples.size, detailReport)
    }

    /**
     * Run grid search optimization to find best parameters
     * ENHANCED: Pre-processes audio once, then runs fast cached tests
     */
    fun runOptimization(
        progressCallback: ((Int, Int, Float) -> Unit)? = null,
        statusCallback: ((String) -> Unit)? = null
    ): TuningResult {
        Log.i(TAG, "Starting parameter optimization...")

        // Pre-process all audio files first - this takes time once, saves massive time later
        statusCallback?.invoke("Pre-processing audio files...")
        val preprocessed = preProcessTrainingData { status -> statusCallback?.invoke(status) }
        if (!preprocessed) {
            throw Exception("Failed to pre-process training audio")
        }

        var bestResult = TuningResult(
            TuningParameters(0f, 0f, 0f, 0f, 0f, 0f, 0f),
            0f, 0, 0, ""
        )

        val searchRanges = ParameterSearchRanges()
        var testCount = 0
        val startTime = System.currentTimeMillis()

        // Calculate total tests once
        val totalTests = searchRanges.speechThresholds.size *
                searchRanges.singingThresholds.size *
                searchRanges.stabilityWeights.size *
                searchRanges.contourWeights.size *
                searchRanges.voicedWeights.size *
                searchRanges.contourNormalizers.size *
                searchRanges.mfccNormalizers.size

        Log.i(TAG, "Will test $totalTests parameter combinations")
        progressCallback?.invoke(0, totalTests, 0.0f)
        statusCallback?.invoke("Testing parameters...")

        // Grid search across parameter space - now fast with cached audio!
        searchRanges.speechThresholds.forEach { speechThresh ->
            searchRanges.singingThresholds.forEach { singingThresh ->
                searchRanges.stabilityWeights.forEach { stabilityWeight ->
                    searchRanges.contourWeights.forEach { contourWeight ->
                        searchRanges.voicedWeights.forEach { voicedWeight ->
                            searchRanges.contourNormalizers.forEach { contourNorm ->
                                searchRanges.mfccNormalizers.forEach { mfccNorm ->

                                    val params = TuningParameters(
                                        speechThresh, singingThresh,
                                        stabilityWeight, contourWeight, voicedWeight,
                                        contourNorm, mfccNorm
                                    )

                                    val result = testParameters(params)
                                    testCount++

                                    // Progress callback - call every test for real-time progress
                                    progressCallback?.invoke(testCount, totalTests, (testCount.toFloat() / totalTests.toFloat()) * 100f)

                                    if (result.accuracy > bestResult.accuracy) {
                                        bestResult = result
                                        Log.i(TAG, "New best: ${result.accuracy * 100}% with $params")

                                        // Early termination if perfect accuracy found
                                        if (result.accuracy >= 1.0f) {
                                            Log.i(TAG, "Perfect accuracy achieved! Stopping early.")
                                            return@forEach
                                        }
                                    }

                                    if (testCount % 100 == 0) {
                                        Log.d(TAG, "Tested $testCount configurations, best: ${bestResult.accuracy * 100}%")
                                    }
                                }

                                // Early exit if perfect found
                                if (bestResult.accuracy >= 1.0f) return@forEach
                            }
                            if (bestResult.accuracy >= 1.0f) return@forEach
                        }
                        if (bestResult.accuracy >= 1.0f) return@forEach
                    }
                    if (bestResult.accuracy >= 1.0f) return@forEach
                }
                if (bestResult.accuracy >= 1.0f) return@forEach
            }
            if (bestResult.accuracy >= 1.0f) return@forEach
        }

        val endTime = System.currentTimeMillis()
        val durationSec = (endTime - startTime) / 1000f

        Log.i(TAG, "Optimization complete! Best accuracy: ${bestResult.accuracy * 100}% in ${durationSec}s")
        Log.i(TAG, "Best parameters: ${bestResult.parameters}")

        return bestResult
    }

    /**
     * Generate update code for VocalModeDetector.kt
     */
    fun generateUpdateCode(result: TuningResult): String {
        return """
//=== VocalModeDetector Optimal Configuration ===
// Generated by VocalModeDetectorTuner BIT
// Accuracy: ${(result.accuracy * 100).toInt()}% (${result.correctClassifications}/${result.totalSamples})
// Parameters: ${result.parameters}
// Date: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(java.util.Date())}

// 1. Update VocalDetectionParameters defaults:
data class VocalDetectionParameters(
    val speechConfidenceThreshold: Float = ${result.parameters.speechThreshold}f,
    val singingConfidenceThreshold: Float = ${result.parameters.singingThreshold}f,
    // ... other parameters unchanged
)

// 2. In classifyFeatures() method, update singing weights:
val singingStabilityContrib = features.pitchStability * ${result.parameters.stabilityWeight}f
val singingContourContrib = features.pitchContour * ${result.parameters.contourWeight}f  
val singingVoicedContrib = features.voicedRatio * ${result.parameters.voicedWeight}f

// 3. In analyzePitchContour() method, update normalization:
val contour = (avgInterval / ${result.parameters.contourNormalizer}f).coerceIn(0f, 1f)

// 4. In extractVocalFeatures() method, update MFCC normalization:
(audioProcessor.calculateMFCCVariance(mfccFrames) / ${result.parameters.mfccNormalizer}f).coerceIn(0f, 1f)

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
 * Custom VocalModeDetector for testing - NO INHERITANCE
 * ENHANCED: Supports cached audio data for massive speedup
 */
private class CustomVocalModeDetector(
    private val audioProcessor: AudioProcessor,
    private val customParams: TuningParameters
) {

    /**
     * NEW: Classify from cached audio data - this is the speedup!
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
     * Original file-based classification (fallback)
     */
    fun classifyVocalMode(audioFile: File): VocalAnalysis {
        try {
            // Read WAV file (copied from your implementation)
            val (audioData, sampleRate) = readWavFile(audioFile)
            if (audioData.isEmpty()) {
                return VocalAnalysis(VocalMode.UNKNOWN, 0f, VocalFeatures(0f, 0f, 0f, 0f))
            }

            // Process audio in frames (copied from your implementation)
            val frameSize = 1024
            val hopSize = 512
            val audioFrames = mutableListOf<FloatArray>()
            val pitches = mutableListOf<Float>()
            val mfccFrames = mutableListOf<FloatArray>()

            var i = 0
            while (i + frameSize <= audioData.size) {
                val frame = audioData.sliceArray(i until i + frameSize)
                audioFrames.add(frame)

                val pitch = audioProcessor.extractPitchYIN(frame, sampleRate)
                pitches.add(pitch)

                val mfcc = audioProcessor.extractMFCC(frame, sampleRate)
                mfccFrames.add(mfcc)

                i += hopSize
            }

            // Extract features with custom parameters
            val features = extractVocalFeaturesCustom(audioFrames, pitches, mfccFrames)

            // Classify with custom logic
            val (mode, confidence) = classifyFeaturesCustom(features)

            return VocalAnalysis(mode, confidence, features)

        } catch (e: Exception) {
            return VocalAnalysis(VocalMode.UNKNOWN, 0f, VocalFeatures(0f, 0f, 0f, 0f))
        }
    }

    /**
     * Read WAV file - copied exactly from your implementation
     */
    private fun readWavFile(file: File): Pair<FloatArray, Int> {
        try {
            val bytes = file.readBytes()
            if (bytes.size < 44) {
                return Pair(floatArrayOf(), 44100)
            }

            val sampleRate = java.nio.ByteBuffer.wrap(bytes, 24, 4)
                .order(java.nio.ByteOrder.LITTLE_ENDIAN).int

            val pcmData = bytes.sliceArray(44 until bytes.size)
            val audioData = FloatArray(pcmData.size / 2)
            for (i in audioData.indices) {
                val sample = ((pcmData[i * 2 + 1].toInt() shl 8) or
                        (pcmData[i * 2].toInt() and 0xFF)).toShort()
                audioData[i] = sample.toFloat() / Short.MAX_VALUE
            }

            return Pair(audioData, sampleRate)

        } catch (e: Exception) {
            return Pair(floatArrayOf(), 44100)
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

        // 1. Pitch Stability (copied from your implementation)
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

        // 4. Voiced Ratio (copied from your implementation)
        val voicedRatio = if (pitches.isNotEmpty()) {
            validPitches.size.toFloat() / pitches.size
        } else 0f

        return VocalFeatures(pitchStability, pitchContour, mfccSpread, voicedRatio)
    }

    /**
     * Classify features with custom weights and thresholds
     */
    private fun classifyFeaturesCustom(features: VocalFeatures): Pair<VocalMode, Float> {

        // Speech indicators (copied from your implementation)
        val speechStabilityContrib = (1f - features.pitchStability) * 0.4f
        val speechContourContrib = (1f - features.pitchContour) * 0.3f
        val speechMfccContrib = features.mfccSpread * 0.1f
        val speechScore = speechStabilityContrib + speechContourContrib + speechMfccContrib

        // Singing indicators (custom weights)
        val singingStabilityContrib = features.pitchStability * customParams.stabilityWeight
        val singingContourContrib = features.pitchContour * customParams.contourWeight
        val singingVoicedContrib = features.voicedRatio * customParams.voicedWeight
        val singingScore = singingStabilityContrib + singingContourContrib + singingVoicedContrib

        return when {
            speechScore > customParams.speechThreshold && singingScore > customParams.singingThreshold -> {
                if (speechScore > singingScore) VocalMode.SPEECH to speechScore else VocalMode.SINGING to singingScore
            }
            speechScore > customParams.speechThreshold -> VocalMode.SPEECH to speechScore
            singingScore > customParams.singingThreshold -> VocalMode.SINGING to singingScore
            else -> VocalMode.SPEECH to 0.25f
        }
    }

    /**
     * Standard deviation calculation (copied from your implementation)
     */
    private fun List<Float>.stdDev(): Float {
        if (isEmpty()) return 0f
        val mean = average().toFloat()
        val variance = sumOf { (it - mean).toDouble() * (it - mean).toDouble() }.toFloat() / size
        return sqrt(variance)
    }
}