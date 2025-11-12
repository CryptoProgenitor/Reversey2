package com.example.reversey.scoring

import android.util.Log
import com.example.reversey.audio.processing.AudioProcessor
import kotlin.math.sqrt

/**
 * Analysis result from garbage detection
 */
data class GarbageAnalysis(
    val isGarbage: Boolean,
    val confidence: Float,  // 0-1 scale, how confident we are it's garbage
    val failedFilters: List<String>,
    val filterResults: Map<String, Float>
)

/**
 * Pitch contour analysis for detecting monotone or unnatural oscillation
 */
data class PitchContourAnalysis(
    val stdDev: Float,           // Standard deviation of pitch
    val oscillationRate: Float,  // Percentage of frames that are peaks/troughs
    val isMonotone: Boolean,     // Too little pitch variation
    val isOscillating: Boolean   // Too much unnatural variation
)

/**
 * Garbage Detector - Filters out invalid attempts like "blah blah blah" or monotone humming
 *
 * This detector uses multiple audio analysis filters to identify attempts that are clearly
 * not genuine singing/speaking attempts. It's designed to prevent gaming the system while
 * minimizing false positives on legitimate attempts.
 *
 * Filters:
 * 1. MFCC Variance - Detects repetitive sounds
 * 2. Pitch Contour - Detects monotone or unnatural oscillation
 * 3. Spectral Entropy - Detects low-complexity noise
 * 4. Zero Crossing Rate - Detects hums or white noise
 * 5. Silence Ratio - Detects continuous noise without pauses
 */
class GarbageDetector(
    private val audioProcessor: AudioProcessor,
    private var parameters: GarbageDetectionParameters = GarbageDetectionParameters()
) {

    /**
     * Analyze audio to detect garbage input
     *
     * @param audioFrames List of audio frames for analysis
     * @param pitches Pitch sequence extracted from audio
     * @param mfccFrames MFCC feature frames
     * @param sampleRate Audio sample rate
     * @return GarbageAnalysis with verdict and details
     */
    fun detectGarbage(
        audioFrames: List<FloatArray>,
        pitches: List<Float>,
        mfccFrames: List<FloatArray>,
        sampleRate: Int
    ): GarbageAnalysis {

        // If garbage detection is disabled, always pass
        if (!parameters.enableGarbageDetection) {
            return GarbageAnalysis(
                isGarbage = false,
                confidence = 0f,
                failedFilters = emptyList(),
                filterResults = emptyMap()
            )
        }

        // ADD THIS DEBUG BLOCK RIGHT HERE:
        Log.d("GarbageDetector", "=== GARBAGE PARAMETERS ===")
        Log.d("GarbageDetector", "pitchMonotoneThreshold: ${parameters.pitchMonotoneThreshold}")
        Log.d("GarbageDetector", "spectralEntropyThreshold: ${parameters.spectralEntropyThreshold}")
        Log.d("GarbageDetector", "mfccVarianceThreshold: ${parameters.mfccVarianceThreshold}")
        Log.d("GarbageDetector", "garbageScoreThreshold: 0.4f")
        Log.d("GarbageDetector", "========================")

        val failedFilters = mutableListOf<String>()
        val filterResults = mutableMapOf<String, Float>()
        var garbageScore = 0f

        // === FILTER 1: MFCC VARIANCE (Repetition Detection) ===
        if (mfccFrames.size >= 2) {
            val mfccVariance = audioProcessor.calculateMFCCVariance(mfccFrames)
            filterResults["mfcc_variance"] = mfccVariance

            if (mfccVariance < parameters.mfccVarianceThreshold) {
                garbageScore += 0.25f
                failedFilters.add("Repetitive sound pattern detected")
            }
        }

        // === FILTER 2: PITCH CONTOUR (Monotone/Oscillation Detection) ===
        if (pitches.size >= 3) {
            val pitchAnalysis = audioProcessor.analyzePitchContour(pitches, parameters)
            filterResults["pitch_stddev"] = pitchAnalysis.stdDev
            filterResults["pitch_oscillation"] = pitchAnalysis.oscillationRate

            if (pitchAnalysis.isMonotone) {
                garbageScore += 0.25f
                failedFilters.add("Monotone/droning detected")
            }

            if (pitchAnalysis.isOscillating) {
                garbageScore += 0.15f
                failedFilters.add("Unnatural pitch oscillation")
            }
        }

        // === FILTER 3: SPECTRAL ENTROPY (Complexity Detection) ===
        if (audioFrames.isNotEmpty()) {
            // Sample first 10 frames for efficiency
            val entropyFrames = audioFrames.take(10)
            val entropies = entropyFrames.mapNotNull { frame ->
                if (frame.isNotEmpty()) {
                    audioProcessor.calculateSpectralEntropy(frame)
                } else null
            }

            if (entropies.isNotEmpty()) {
                val avgEntropy = entropies.average().toFloat()
                filterResults["spectral_entropy"] = avgEntropy

                if (avgEntropy < parameters.spectralEntropyThreshold) {
                    garbageScore += 0.20f
                    failedFilters.add("Low audio complexity (noise/hum)")
                }
            }
        }

        // === FILTER 4: ZERO CROSSING RATE (Voice Signature Detection) ===
        if (audioFrames.isNotEmpty()) {
            val zcr = audioProcessor.analyzeZeroCrossingRate(audioFrames)
            filterResults["zero_crossing_rate"] = zcr

            if (zcr < parameters.zcrMinThreshold || zcr > parameters.zcrMaxThreshold) {
                garbageScore += 0.15f
                failedFilters.add("Abnormal audio signature")
            }
        }

        // === FILTER 5: SILENCE RATIO (Natural Pause Detection) ===
        if (audioFrames.isNotEmpty()) {
            val silenceRatio = audioProcessor.calculateSilenceRatio(audioFrames, parameters.silenceThreshold)
            filterResults["silence_ratio"] = silenceRatio

            if (silenceRatio < parameters.silenceRatioMin) {
                garbageScore += 0.15f
                failedFilters.add("No natural speech pauses")
            }
        }

        // === FINAL VERDICT ===
        // garbageScore > 0.4 means multiple filters failed = high confidence it's garbage
        val isGarbage = garbageScore > 0.4f

        // Logging for debugging
        Log.d("GarbageDetector", "=== GARBAGE ANALYSIS ===")
        Log.d("GarbageDetector", "Filter Results: $filterResults")
        Log.d("GarbageDetector", "Failed Filters: $failedFilters")
        Log.d("GarbageDetector", "Garbage Score: $garbageScore (threshold: 0.5)")
        Log.d("GarbageDetector", "Verdict: ${if (isGarbage) "🚫 REJECT" else "✅ ACCEPT"}")
        Log.d("GarbageDetector", "========================")

        return GarbageAnalysis(
            isGarbage = isGarbage,
            confidence = garbageScore,
            failedFilters = failedFilters,
            filterResults = filterResults
        )
    }

    /**
     * Update garbage detection parameters
     */
    fun updateParameters(newParams: GarbageDetectionParameters) {
        parameters = newParams
        Log.d("GarbageDetector", "Parameters updated: enabled=${parameters.enableGarbageDetection}")
    }

    /**
     * Get current parameters
     */
    fun getParameters(): GarbageDetectionParameters = parameters
}