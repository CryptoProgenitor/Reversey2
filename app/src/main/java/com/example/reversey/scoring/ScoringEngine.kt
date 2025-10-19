package com.example.reversey.scoring

import android.content.Context
import android.util.Log
import com.example.reversey.audio.processing.AudioProcessor
import kotlin.math.*

class ScoringEngine(private val context: Context) {

    private val audioProcessor = AudioProcessor()
    private var parameters = ScoringParameters()
    private val prefs = context.getSharedPreferences("scoring_params", Context.MODE_PRIVATE)

    init {
        loadParameters()
    }

    fun scoreAttempt(
        reversedOriginal: FloatArray,
        playerAttempt: FloatArray,
        sampleRate: Int = 44100
    ): ScoringResult {
        val attemptRMS = audioProcessor.calculateRMS(playerAttempt)
        if (attemptRMS < parameters.silenceThreshold) {
            Log.d("ScoringEngine", "SILENCE DETECTED: RMS ($attemptRMS) < Threshold (${parameters.silenceThreshold})")
            return ScoringResult(
                score = 0,
                rawScore = 0f,
                metrics = SimilarityMetrics(0f, 0f),
                feedback = listOf("🎤 Silence was recorded. Please try singing next time!")
            )
        }

        val (alignedOriginal, alignedAttempt) = alignAudio(reversedOriginal, playerAttempt)

        if (alignedOriginal.isEmpty() || alignedAttempt.isEmpty()) {
            Log.d("ScoringEngine", "AUDIO ALIGNMENT FAILED: Resulting audio is empty.")
            return ScoringResult(score = 0, rawScore = 0f, metrics = SimilarityMetrics(0f, 0f), feedback = listOf("Error: Could not process short recording."))
        }

        val originalPitchSequence = getPitchSequence(alignedOriginal, sampleRate)
        val attemptPitchSequence = getPitchSequence(alignedAttempt, sampleRate)
        val originalMfccSequence = getMfccSequence(alignedOriginal, sampleRate)
        val attemptMfccSequence = getMfccSequence(alignedAttempt, sampleRate)

        val pitchSimilarity = calculatePitchSimilarity(originalPitchSequence, attemptPitchSequence)
        val mfccSimilarity = calculateMfccSimilarity(originalMfccSequence, attemptMfccSequence)

        val metrics = SimilarityMetrics(
            pitch = pitchSimilarity,
            mfcc = mfccSimilarity
        )

        var rawScore = with(parameters) {
            metrics.pitch * pitchWeight + metrics.mfcc * mfccWeight
        }

        Log.d("ScoringEngine", "--- METRIC SCORES ---")
        Log.d("ScoringEngine", String.format("Pitch: %.3f | MFCC: %.3f", metrics.pitch, metrics.mfcc))
        Log.d("ScoringEngine", String.format("Weighted Score: %.3f", rawScore))

        val variancePenalty = calculatePitchVariancePenalty(originalPitchSequence, attemptPitchSequence)
        rawScore *= variancePenalty
        Log.d("ScoringEngine", String.format("Score after Variance Penalty (%.2fx): %.3f", variancePenalty, rawScore))

        val consistency = (1 - abs(metrics.pitch - metrics.mfcc)) * parameters.consistencyBonus
        val confidence = min(1f, attemptRMS * 5) * parameters.confidenceBonus
        val scoreWithBonuses = rawScore * (1 + consistency + confidence)
        Log.d("ScoringEngine", String.format("Score after Bonuses (Cons: %.2f, Conf: %.2f): %.3f", consistency, confidence, scoreWithBonuses))

        val finalScore = scaleScore(scoreWithBonuses)
        Log.d("ScoringEngine", "--------------------")
        Log.d("ScoringEngine", "FINAL SCORE: $finalScore")
        Log.d("ScoringEngine", "--------------------")

        return ScoringResult(score = finalScore, rawScore = rawScore, metrics = metrics, feedback = generateFeedback(finalScore, metrics))
    }

    private fun getPitchSequence(audio: FloatArray, sampleRate: Int): List<Float> {
        val frameSize = 4096
        val hopSize = 1024
        val pitches = mutableListOf<Float>()
        val paddedAudio = if (audio.size < frameSize) audio.plus(FloatArray(frameSize - audio.size)) else audio

        var i = 0
        while (i + frameSize <= paddedAudio.size) {
            val frame = paddedAudio.sliceArray(i until i + frameSize)
            val pitch = audioProcessor.extractPitchYIN(frame, sampleRate)
            pitches.add(if (pitch > 0) 12 * log2(pitch / 440f) else 0f)
            i += hopSize
        }
        return pitches
    }

    private fun getMfccSequence(audio: FloatArray, sampleRate: Int): List<FloatArray> {
        val frameSize = 2048
        val hopSize = 1024
        val mfccs = mutableListOf<FloatArray>()
        val paddedAudio = if (audio.size < frameSize) audio.plus(FloatArray(frameSize - audio.size)) else audio

        var i = 0
        while (i + frameSize <= paddedAudio.size) {
            val frame = paddedAudio.sliceArray(i until i + frameSize)
            mfccs.add(audioProcessor.extractMFCC(frame, sampleRate))
            i += hopSize
        }
        return mfccs
    }

    private fun calculatePitchSimilarity(originalPitches: List<Float>, attemptPitches: List<Float>): Float {
        if (originalPitches.size < 2 || attemptPitches.size < 2) return 0.0f

        val minLen = min(originalPitches.size, attemptPitches.size)
        val pitchDifferences = (0 until minLen)
            .filter { originalPitches[it] > 0 && attemptPitches[it] > 0 }
            .map {
                val diff = abs(originalPitches[it] - attemptPitches[it])
                val toleratedDiff = max(0f, diff - parameters.pitchTolerance)
                exp(-toleratedDiff / 5f)
            }
        val absoluteSimilarity = pitchDifferences.average().toFloat().takeIf { !it.isNaN() } ?: 0f

        val dtwDistance = dtw(originalPitches, attemptPitches) { p1, p2 -> abs(p1 - p2) }
        val avgDist = dtwDistance / max(originalPitches.size, attemptPitches.size)
        val dtwSimilarity = exp(-avgDist / parameters.dtwNormalizationFactor)

        Log.d("ScoringEngine", "--- PITCH ANALYSIS ---")
        Log.d("ScoringEngine", String.format("Absolute Similarity: %.3f", absoluteSimilarity))
        Log.d("ScoringEngine", String.format("DTW Avg Distance: %.2f (Similarity: %.3f)", avgDist, dtwSimilarity))

        val blendedScore = (absoluteSimilarity * (1 - parameters.pitchContourWeight) + dtwSimilarity * parameters.pitchContourWeight)
        Log.d("ScoringEngine", String.format("Blended Pitch Score (Contour W: %.2f): %.3f", parameters.pitchContourWeight, blendedScore))
        return blendedScore
    }

    private fun calculateMfccSimilarity(originalMfccs: List<FloatArray>, attemptMfccs: List<FloatArray>): Float {
        if (originalMfccs.size < 2 || attemptMfccs.size < 2) return 0.0f

        val dtwDistance = dtw(originalMfccs, attemptMfccs) { v1, v2 -> euclideanDistance(v1, v2) }
        val avgDist = dtwDistance / max(originalMfccs.size, attemptMfccs.size)
        // --- CALIBRATION: Stricter normalization for MFCC distance ---
        val similarity = exp(-avgDist / 15f) // OLD: 7.5

        Log.d("ScoringEngine", "--- MFCC ANALYSIS ---")
        Log.d("ScoringEngine", String.format("DTW Avg Distance: %.2f (Similarity: %.3f)", avgDist, similarity))
        return similarity
    }

    private fun <T> dtw(seq1: List<T>, seq2: List<T>, costFunc: (T, T) -> Float): Float {
        val n = seq1.size
        val m = seq2.size
        if (n == 0 || m == 0) return Float.POSITIVE_INFINITY
        val dtw = Array(n + 1) { FloatArray(m + 1) { Float.POSITIVE_INFINITY } }
        dtw[0][0] = 0f

        for (i in 1..n) {
            for (j in 1..m) {
                val cost = costFunc(seq1[i - 1], seq2[j - 1])
                dtw[i][j] = cost + min(dtw[i - 1][j], min(dtw[i][j - 1], dtw[i - 1][j - 1]))
            }
        }
        return dtw[n][m]
    }

    private fun euclideanDistance(vec1: FloatArray, vec2: FloatArray): Float {
        return sqrt(vec1.zip(vec2).sumOf { (a, b) -> (a - b).pow(2).toDouble() }.toFloat())
    }

    private fun calculatePitchVariancePenalty(originalPitches: List<Float>, attemptPitches: List<Float>): Float {
        if (originalPitches.size < 5 || attemptPitches.size < 5) return 1f

        // Debug: Check what the pitch values actually are
        Log.d("ScoringEngine", "Original pitches sample: ${originalPitches.take(10)}")
        Log.d("ScoringEngine", "Attempt pitches sample: ${attemptPitches.take(10)}")
        Log.d("ScoringEngine", "Original range: ${originalPitches.minOrNull()} to ${originalPitches.maxOrNull()}")
        Log.d("ScoringEngine", "Attempt range: ${attemptPitches.minOrNull()} to ${attemptPitches.maxOrNull()}")

        // rest of your function...

        fun calculateVariance(pitches: List<Float>): Float {
            val activePitches = pitches.filter { it != 0f }
            if (activePitches.size < 2) return 0f

            Log.d("ScoringEngine", "Active pitches count: ${activePitches.size} / ${pitches.size}")

            // Add outlier filtering
            val sorted = activePitches.sorted()
            val q1 = sorted[sorted.size / 4]
            val q3 = sorted[sorted.size * 3 / 4]
            val iqr = q3 - q1
            val lowerBound = q1 - 1.5f * iqr
            val upperBound = q3 + 1.5f * iqr

            val filteredPitches = activePitches.filter { it in lowerBound..upperBound }
            Log.d("ScoringEngine", "Filtered outliers: ${activePitches.size} -> ${filteredPitches.size}")

            if (filteredPitches.size < 2) return 0f

            val mean = filteredPitches.average().toFloat()
            val variance = filteredPitches.sumOf { (it - mean).pow(2).toDouble() }.toFloat() / filteredPitches.size

            Log.d("ScoringEngine", "First 5 pitches: ${activePitches.take(5)}")
            Log.d("ScoringEngine", "Mean: $mean, Raw variance: $variance")
            return variance
        }

        val originalVariance = calculateVariance(originalPitches)
        val attemptVariance = calculateVariance(attemptPitches)
        Log.d("ScoringEngine", String.format("Pitch Variance: Original=%.6f, Attempt=%.6f", originalVariance, attemptVariance))

        // NEW LOGIC: Penalize DIFFERENCE in variance, not low variance
        val varianceRatio = when {
            originalVariance < 0.15f && attemptVariance < 0.15f -> 1f  // Both flat = OK
            originalVariance < 0.05f || attemptVariance < 0.05f -> 0.5f  // One truly flat = bad
            else -> minOf(attemptVariance / originalVariance, originalVariance / attemptVariance)
        }
        //EDITS HERE!!!
        // Apply penalty based on the variance penalty parameter
        // Temporarily disable variance penalty
        return 1f
    }

    private fun alignAudio(original: FloatArray, attempt: FloatArray): Pair<FloatArray, FloatArray> {
        val threshold = 0.005f  // Was 0.01f,
        val originalStart = original.indexOfFirst { abs(it) > threshold }.takeIf { it != -1 } ?: 0
        val attemptStart = attempt.indexOfFirst { abs(it) > threshold }.takeIf { it != -1 } ?: 0
        val minLength = min(original.size - originalStart, attempt.size - attemptStart)
        if (minLength <= 0) return Pair(FloatArray(0), FloatArray(0))
        return Pair(original.sliceArray(originalStart until originalStart + minLength), attempt.sliceArray(attemptStart until attemptStart + minLength))
    }

    private fun scaleScore(rawScore: Float): Int {
        val clamped = rawScore.coerceIn(0f, 1f)
        return when {
            clamped < parameters.minScoreThreshold -> 0
            clamped > parameters.perfectScoreThreshold -> 100
            else -> {
                val range = parameters.perfectScoreThreshold - parameters.minScoreThreshold
                if (range <= 0f) return 0
                val normalized = (clamped - parameters.minScoreThreshold) / range
                val curved = normalized.pow(1f / max(0.1f, parameters.scoreCurve))
                (curved * 100).roundToInt()
            }
        }
    }

    private fun generateFeedback(score: Int, metrics: SimilarityMetrics): List<String> {
        val feedback = mutableListOf<String>()
        feedback.add(when {
            score >= 90 -> "🌟 Incredible! You're a reverse singing master!"
            score >= 75 -> "🎯 Great job! You're really getting the hang of this!"
            score >= 50 -> "👍 Good effort! Keep practicing!"
            else -> "💪 Nice try! Reverse singing is tough!"
        })
        val weakestMetric = if (metrics.pitch < metrics.mfcc) "pitch" else "timbre"
        if (min(metrics.pitch, metrics.mfcc) < 0.6f) {
            feedback.add(when(weakestMetric) {
                "pitch" -> "💡 Try to match the melody's shape more closely."
                else -> "💡 Work on matching the vocal sound and quality."
            })
        }
        return feedback
    }

    fun updateParameters(newParams: ScoringParameters) {
        parameters = newParams
        saveParameters()
    }

    fun getParameters(): ScoringParameters = parameters

    private fun saveParameters() {
        prefs.edit().apply {
            putFloat("pitchWeight", parameters.pitchWeight)
            putFloat("mfccWeight", parameters.mfccWeight)
            putFloat("pitchTolerance", parameters.pitchTolerance)
            putFloat("pitchContourWeight", parameters.pitchContourWeight)
            putFloat("variancePenalty", parameters.variancePenalty)
            putFloat("dtwNormalizationFactor", parameters.dtwNormalizationFactor)
            putFloat("silenceThreshold", parameters.silenceThreshold)
            putFloat("minScoreThreshold", parameters.minScoreThreshold)
            putFloat("perfectScoreThreshold", parameters.perfectScoreThreshold)
            putFloat("scoreCurve", parameters.scoreCurve)
            putFloat("consistencyBonus", parameters.consistencyBonus)
            putFloat("confidenceBonus", parameters.confidenceBonus)
            apply()
        }
    }

    private fun loadParameters() {
        val defaults = ScoringParameters()
        parameters = ScoringParameters(
            pitchWeight = prefs.getFloat("pitchWeight", defaults.pitchWeight),
            mfccWeight = prefs.getFloat("mfccWeight", defaults.mfccWeight),
            pitchTolerance = prefs.getFloat("pitchTolerance", defaults.pitchTolerance),
            pitchContourWeight = prefs.getFloat("pitchContourWeight", defaults.pitchContourWeight),
            variancePenalty = prefs.getFloat("variancePenalty", defaults.variancePenalty),
            dtwNormalizationFactor = prefs.getFloat("dtwNormalizationFactor", defaults.dtwNormalizationFactor),
            silenceThreshold = prefs.getFloat("silenceThreshold", defaults.silenceThreshold),
            minScoreThreshold = prefs.getFloat("minScoreThreshold", defaults.minScoreThreshold),
            perfectScoreThreshold = prefs.getFloat("perfectScoreThreshold", defaults.perfectScoreThreshold),
            scoreCurve = prefs.getFloat("scoreCurve", defaults.scoreCurve),
            consistencyBonus = prefs.getFloat("consistencyBonus", defaults.consistencyBonus),
            confidenceBonus = prefs.getFloat("confidenceBonus", defaults.confidenceBonus)
        )
    }
}

