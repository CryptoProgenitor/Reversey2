package com.example.reversey.scoring

import android.content.Context
import android.util.Log
import com.example.reversey.ChallengeType
import com.example.reversey.audio.processing.AudioProcessor
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.log2
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

class ScoringEngine(private val context: Context) {

    private val audioProcessor = AudioProcessor()
    private var parameters = ScoringParameters()

    fun scoreAttempt(
        originalAudio: FloatArray, // Renamed for clarity
        playerAttempt: FloatArray,
        challengeType: ChallengeType, // <-- ADD THIS
        sampleRate: Int = 44100
    ): ScoringResult {
        Log.d("ScoringEngine", "--- USING PARAMETERS ---")
        Log.d("ScoringEngine", "dtwNormalizationFactor: ${parameters.dtwNormalizationFactor}")
        Log.d("ScoringEngine", "scoreCurve: ${parameters.scoreCurve}")
        Log.d("ScoringEngine", "------------------------")

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

        val (alignedOriginal, alignedAttempt) = alignAudio(originalAudio, playerAttempt)

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

        val finalScore = scaleScore(scoreWithBonuses, challengeType)
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
            Log.d("ScoringEngine", "Frame ${i/hopSize}: Raw pitch=$pitch, Converted=${if (pitch > 0) 12 * log2(pitch / 440f) else 0f}")
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

    // ***** CRITICAL FIX #1: Fixed absoluteSimilarity calculation *****
    // start of private fun calculatePitchSimilarity
    private fun calculatePitchSimilarity(originalPitches: List<Float>, attemptPitches: List<Float>): Float {
        if (originalPitches.size < 2 || attemptPitches.size < 2) return 0.0f

        Log.d("ScoringEngine", "Original pitches (first 10): ${originalPitches.take(10)}")
        Log.d("ScoringEngine", "Attempt pitches (first 10): ${attemptPitches.take(10)}")
        Log.d("ScoringEngine", "Sequences length: orig=${originalPitches.size}, attempt=${attemptPitches.size}")

        val minLen = min(originalPitches.size, attemptPitches.size)

        // Calculate pitch variance to detect monotone attempts
        val originalVariance = calculatePitchVariance(originalPitches)
        val attemptVariance = calculatePitchVariance(attemptPitches)
        val monotonePenalty = if (originalVariance > 2.0f && attemptVariance < 0.5f) 0.3f else 1.0f
        Log.d("ScoringEngine", String.format("Variance: Orig=%.2f, Attempt=%.2f, Penalty=%.2f", originalVariance, attemptVariance, monotonePenalty))

        // Calculate similarity for ALL frames
        val pitchSimilarities = (0 until minLen).map { i ->
            val original = originalPitches[i]
            val attempt = attemptPitches[i]

            when {
                original > 0 && attempt > 0 -> {
                    val diff = abs(original - attempt)
                    val toleratedDiff = max(0f, diff - parameters.pitchTolerance)
                    exp(-toleratedDiff / 5f)
                }
                original == 0f && attempt == 0f -> 0.7f
                else -> 0.0f
            }
        }
        val absoluteSimilarity = pitchSimilarities.average().toFloat() * monotonePenalty

        Log.d("ScoringEngine", "--- PITCH ANALYSIS ---")
        Log.d("ScoringEngine", String.format("Absolute Similarity: %.3f", absoluteSimilarity))

        Log.d("ScoringEngine", "🔍 ABOUT TO CALL: calculateTemporalSimilarity")
        val contentSimilarity = calculateVocalEffortSimilarity(originalPitches, attemptPitches)
        Log.d("ScoringEngine", "🔍 RETURNED FROM: calculateTemporalSimilarity with result: $contentSimilarity")
        Log.d("ScoringEngine", "--- PITCH ANALYSIS ---")
        Log.d("ScoringEngine", String.format("Absolute Similarity: %.3f", absoluteSimilarity))
        Log.d("ScoringEngine", String.format("Content Similarity: %.3f", contentSimilarity))
        Log.d("ScoringEngine", String.format("Final Pitch Score: %.3f", contentSimilarity))
        return contentSimilarity
    }

    private fun calculatePitchVariance(pitches: List<Float>): Float {
        val activePitches = pitches.filter { it != 0f }
        if (activePitches.size < 2) return 0f
        val mean = activePitches.average().toFloat()
        return activePitches.sumOf { (it - mean).pow(2).toDouble() }.toFloat() / activePitches.size
    }

    // END of private fun calculatePitchSimilarity
    private fun calculateMfccSimilarity(originalMfccs: List<FloatArray>, attemptMfccs: List<FloatArray>): Float {
        if (originalMfccs.size < 2 || attemptMfccs.size < 2) return 0.0f

        val dtwDistance = dtw(originalMfccs, attemptMfccs) { v1, v2 -> euclideanDistance(v1, v2) }
        val avgDist = dtwDistance / max(originalMfccs.size, attemptMfccs.size)
        val similarity = exp(-avgDist / parameters.dtwNormalizationFactor) // Use the dynamic factor

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

        fun calculateVariance(pitches: List<Float>): Float {
            val activePitches = pitches.filter { it != 0f }
            if (activePitches.size < 2) return 0f

            val sorted = activePitches.sorted()
            val q1 = sorted[sorted.size / 4]
            val q3 = sorted[sorted.size * 3 / 4]
            val iqr = q3 - q1
            val lowerBound = q1 - 1.5f * iqr
            val upperBound = q3 + 1.5f * iqr

            val filteredPitches = activePitches.filter { it in lowerBound..upperBound }
            if (filteredPitches.size < 2) return 0f

            val mean = filteredPitches.average().toFloat()
            return filteredPitches.sumOf { (it - mean).pow(2).toDouble() }.toFloat() / filteredPitches.size
        }

        val originalVariance = calculateVariance(originalPitches)
        val attemptVariance = calculateVariance(attemptPitches)
        Log.d("ScoringEngine", String.format("Pitch Variance: Original=%.6f, Attempt=%.6f", originalVariance, attemptVariance))

        return 1f // Variance penalty is now disabled in the parameters
    }

    private fun calculateContentSimilarity(originalPitches: List<Float>, attemptPitches: List<Float>): Float {
        // 1. Vocal density: how much actual singing vs silence?
        val originalDensity = originalPitches.count { it != 0f } / originalPitches.size.toFloat()
        val attemptDensity = attemptPitches.count { it != 0f } / attemptPitches.size.toFloat()
        val densityScore = 1f - abs(originalDensity - attemptDensity)

        // 2. Pitch range similarity
        val originalActive = originalPitches.filter { it != 0f }
        val attemptActive = attemptPitches.filter { it != 0f }
        if (originalActive.isEmpty() || attemptActive.isEmpty()) return 0f

        val originalMean = originalActive.average().toFloat()
        val attemptMean = attemptActive.average().toFloat()
        val rangeScore = exp(-abs(originalMean - attemptMean) / 8f)

        // 3. Melodic complexity similarity
        val originalVariance = calculatePitchVariance(originalPitches)
        val attemptVariance = calculatePitchVariance(attemptPitches)
        val complexityScore = if (max(originalVariance, attemptVariance) > 0) {
            min(originalVariance, attemptVariance) / max(originalVariance, attemptVariance)
        } else 0f

        Log.d("ScoringEngine", String.format("Content Similarity - Density: %.3f, Range: %.3f, Complexity: %.3f",
            densityScore, rangeScore, complexityScore))

        return densityScore * 0.4f + rangeScore * 0.4f + complexityScore * 0.2f
    }

    private fun calculateVocalEffortSimilarity(originalPitches: List<Float>, attemptPitches: List<Float>): Float {
        // 1. Vocal effort: how much singing vs silence
        val originalDensity = originalPitches.count { it != 0f } / originalPitches.size.toFloat()
        val attemptDensity = attemptPitches.count { it != 0f } / attemptPitches.size.toFloat()
        val effortScore = 1f - abs(originalDensity - attemptDensity)

        // 2. Vocal intensity: pitch variation indicates engagement
        val originalVariance = calculatePitchVariance(originalPitches)
        val attemptVariance = calculatePitchVariance(attemptPitches)
        val maxVariance = max(originalVariance, attemptVariance)
        val intensityScore = if (maxVariance > 0f) {
            min(originalVariance, attemptVariance) / maxVariance
        } else 1f

        // 3. Vocal range: similar pitch ranges indicate similar vocal approach
        val originalActive = originalPitches.filter { it != 0f }
        val attemptActive = attemptPitches.filter { it != 0f }
        val rangeScore = if (originalActive.isNotEmpty() && attemptActive.isNotEmpty()) {
            val originalRange = originalActive.maxOrNull()!! - originalActive.minOrNull()!!
            val attemptRange = attemptActive.maxOrNull()!! - attemptActive.minOrNull()!!
            val maxRange = max(originalRange, attemptRange)
            if (maxRange > 0f) 1f - abs(originalRange - attemptRange) / maxRange else 1f
        } else 0f

        // 4. Apply harsh penalty for very low intensity (indicates wrong content)
        val intensityPenalty = if (intensityScore < parameters.intensityPenaltyThreshold) {
            intensityScore * parameters.intensityPenaltyMultiplier
        } else {
            intensityScore
        }

        Log.d("ScoringEngine", String.format("Vocal Similarity - Effort: %.3f, Intensity: %.3f (Penalized: %.3f), Range: %.3f",
            effortScore, intensityScore, intensityPenalty, rangeScore))

        return with(parameters) {
            effortScore * effortWeight + intensityPenalty * intensityWeight + rangeScore * rangeWeight
        }
    }

    //START private fun extractVocalSegments
    private fun extractVocalSegments(pitches: List<Float>): List<Pair<Int, Int>> {
        val segments = mutableListOf<Pair<Int, Int>>()
        var segmentStart = -1
        val minSegmentLength = 3  // Minimum 3 frames to be a real vocal segment
        val minGapLength = 5      // Minimum 5 frames of silence to separate segments

        var silenceCount = 0

        for (i in pitches.indices) {
            if (pitches[i] != 0f) {
                if (segmentStart == -1) {
                    segmentStart = i // Start new segment
                }
                silenceCount = 0
            } else {
                silenceCount++
                if (segmentStart != -1 && silenceCount >= minGapLength) {
                    // End current segment if it's long enough
                    val segmentEnd = i - silenceCount
                    if (segmentEnd - segmentStart + 1 >= minSegmentLength) {
                        segments.add(Pair(segmentStart, segmentEnd))
                    }
                    segmentStart = -1
                }
            }
        }

        // Handle case where audio ends with vocal segment
        if (segmentStart != -1) {
            val segmentEnd = pitches.size - 1 - silenceCount
            if (segmentEnd - segmentStart + 1 >= minSegmentLength) {
                segments.add(Pair(segmentStart, segmentEnd))
            }
        }

        Log.d("ScoringEngine", "Extracted ${segments.size} segments: $segments")
        return segments
    }
    //END private fun extractVocalSegments

    private fun calculateRhythmPattern(segments: List<Pair<Int, Int>>): List<Int> {
        return segments.map { (start, end) -> end - start + 1 } // Duration of each segment
    }

    private fun compareSegmentCounts(originalCount: Int, attemptCount: Int): Float {
        if (originalCount == 0 && attemptCount == 0) return 1f
        if (originalCount == 0 || attemptCount == 0) return 0f

        val ratio = min(originalCount, attemptCount).toFloat() / max(originalCount, attemptCount)
        return ratio.pow(0.5f) // Less harsh penalty for segment count differences
    }

    private fun compareRhythmPatterns(originalRhythm: List<Int>, attemptRhythm: List<Int>): Float {
        if (originalRhythm.isEmpty() || attemptRhythm.isEmpty()) return 0f

        // Compare rhythm patterns using normalized DTW
        val similarities = mutableListOf<Float>()
        val maxComparisons = min(originalRhythm.size, attemptRhythm.size)

        for (i in 0 until maxComparisons) {
            val originalDuration = originalRhythm[i].toFloat()
            val attemptDuration = attemptRhythm[i].toFloat()
            val maxDuration = max(originalDuration, attemptDuration)
            if (maxDuration > 0) {
                similarities.add(min(originalDuration, attemptDuration) / maxDuration)
            }
        }

        return if (similarities.isNotEmpty()) similarities.average().toFloat() else 0f
    }

    private fun compareDensity(originalPitches: List<Float>, attemptPitches: List<Float>): Float {
        val originalDensity = originalPitches.count { it != 0f } / originalPitches.size.toFloat()
        val attemptDensity = attemptPitches.count { it != 0f } / attemptPitches.size.toFloat()
        return 1f - abs(originalDensity - attemptDensity)
    }

    private fun alignAudio(original: FloatArray, attempt: FloatArray): Pair<FloatArray, FloatArray> {
        val threshold = 0.005f
        val originalStart = original.indexOfFirst { abs(it) > threshold }.takeIf { it != -1 } ?: 0
        val attemptStart = attempt.indexOfFirst { abs(it) > threshold }.takeIf { it != -1 } ?: 0
        val minLength = min(original.size - originalStart, attempt.size - attemptStart)
        if (minLength <= 0) return Pair(FloatArray(0), FloatArray(0))
        return Pair(original.sliceArray(originalStart until originalStart + minLength), attempt.sliceArray(attemptStart until attemptStart + minLength))
    }

    private fun scaleScore(rawScore: Float, challengeType: ChallengeType): Int {
        val clamped = rawScore.coerceIn(0f, 1f)
        val (min, perfect, curve) = if (challengeType == ChallengeType.REVERSE) {
            Triple(parameters.minScoreThreshold * 0.9f, parameters.perfectScoreThreshold * 0.95f, parameters.scoreCurve * 1.1f)
        } else {
            Triple(parameters.minScoreThreshold, parameters.perfectScoreThreshold, parameters.scoreCurve)
        }

        Log.d("ScoringEngine", "Scaling with type: $challengeType (Min: $min, Perf: $perfect, Curve: $curve)")

        return when {
            clamped < min -> 0
            clamped > perfect -> 100
            else -> {
                val range = perfect - min
                if (range <= 0f) return 0
                val normalized = (clamped - min) / range
                val curved = normalized.pow(1f / max(0.1f, curve))
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
    }

    fun getParameters(): ScoringParameters = parameters
}
