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
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScoringEngine @Inject constructor(@ApplicationContext private val context: Context) {
    init {
        Log.d("HILT_VERIFY", "🎯 ScoringEngine created - Instance: ${this.hashCode()}")
    }

    private val audioProcessor = AudioProcessor()
    private var parameters = ScoringParameters()
    private var audioParams = AudioProcessingParameters()
    private var contentParams = ContentDetectionParameters()
    private var melodicParams = MelodicAnalysisParameters()
    private var musicalParams = MusicalSimilarityParameters()
    private var scalingParams = ScoreScalingParameters()

    // NEW: Track current difficulty level for UI feedback
    private var currentDifficulty = DifficultyLevel.NORMAL

    fun scoreAttempt(
        originalAudio: FloatArray, // Renamed for clarity
        playerAttempt: FloatArray,
        challengeType: ChallengeType, // <-- ADD THIS
        sampleRate: Int = 44100
    ): ScoringResult {
        Log.d("ScoringEngine", "=== CRITICAL PARAMETER VERIFICATION ===")
        Log.d("ScoringEngine", "🎯 Current Difficulty: ${currentDifficulty.displayName}")
        Log.d("ScoringEngine", "🎵 1. PITCH TOLERANCE: ${parameters.pitchTolerance}f (Easy=20f, Master=3f)")
        Log.d("ScoringEngine", "📊 2. SCORE RANGE: ${parameters.minScoreThreshold} to ${parameters.perfectScoreThreshold} (Easy=0.15-0.75, Master=0.5-0.98)")
        Log.d("ScoringEngine", "📈 3. SCORE CURVE: ${parameters.scoreCurve}f (Easy=2.5f, Master=1.0f)")
        Log.d("ScoringEngine", "⚖️ 4. PITCH vs VOICE: ${parameters.pitchWeight}/${parameters.mfccWeight} (Easy=0.75/0.25, Master=0.98/0.02)")
        Log.d("ScoringEngine", "❌ 5. WRONG CONTENT PENALTY: ${contentParams.wrongContentStandardPenalty}f (Easy=0.3f, Master=0.9f)")
        Log.d("ScoringEngine", "✅ 6. CONTENT DETECTION: ${contentParams.contentDetectionBestThreshold}f (Easy=0.25f, Master=0.7f)")
        Log.d("ScoringEngine", "=======================================")
        Log.d("ScoringEngine", "🎯 Scoring with difficulty: ${currentDifficulty.displayName}")
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
        val confidence = min(1f, attemptRMS * scalingParams.rmsConfidenceMultiplier) * parameters.confidenceBonus
        val scoreWithBonuses = rawScore * (1 + consistency + confidence)
        Log.d("ScoringEngine", String.format("Score after Bonuses (Cons: %.2f, Conf: %.2f): %.3f", consistency, confidence, scoreWithBonuses))

        val finalScore = scaleScore(scoreWithBonuses, challengeType)
        Log.d("ScoringEngine", "--------------------")
        Log.d("ScoringEngine", "FINAL SCORE: $finalScore")
        Log.d("ScoringEngine", "--------------------")

        return ScoringResult(score = finalScore, rawScore = rawScore, metrics = metrics, feedback = generateFeedback(finalScore, metrics))
    }

    private fun getPitchSequence(audio: FloatArray, sampleRate: Int): List<Float> {
        val frameSize = audioParams.pitchFrameSize
        val hopSize = audioParams.pitchHopSize
        val pitches = mutableListOf<Float>()
        val paddedAudio = if (audio.size < frameSize) audio.plus(FloatArray(frameSize - audio.size)) else audio

        var i = 0
        while (i + frameSize <= paddedAudio.size) {
            val frame = paddedAudio.sliceArray(i until i + frameSize)
            val pitch = audioProcessor.extractPitchYIN(frame, sampleRate)
            Log.d("ScoringEngine", "Frame ${i/hopSize}: Raw pitch=$pitch, Converted=${if (pitch > 0) audioParams.semitonesPerOctave * log2(pitch / audioParams.pitchReferenceFreq) else 0f}")
            pitches.add(if (pitch > 0) audioParams.semitonesPerOctave * log2(pitch / audioParams.pitchReferenceFreq) else 0f)
            i += hopSize
        }
        return pitches
    }
    private fun getMfccSequence(audio: FloatArray, sampleRate: Int): List<FloatArray> {
        val frameSize = audioParams.mfccFrameSize
        val hopSize = audioParams.mfccHopSize
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
        val monotonePenalty = if (originalVariance > melodicParams.monotoneDetectionThreshold && attemptVariance < melodicParams.flatSpeechThreshold) melodicParams.monotonePenalty else 1.0f
        Log.d("ScoringEngine", String.format("Variance: Orig=%.2f, Attempt=%.2f, Penalty=%.2f", originalVariance, attemptVariance, monotonePenalty))

        // Calculate similarity for ALL frames
        val pitchSimilarities = (0 until minLen).map { i ->
            val original = originalPitches[i]
            val attempt = attemptPitches[i]

            when {
                original > 0 && attempt > 0 -> {
                    val diff = abs(original - attempt)
                    val toleratedDiff = max(0f, diff - parameters.pitchTolerance)
                    exp(-toleratedDiff / melodicParams.pitchDifferenceDecayRate)
                }
                original == 0f && attempt == 0f -> melodicParams.silenceToSilenceScore
                else -> 0.0f
            }
        }
        val absoluteSimilarity = pitchSimilarities.average().toFloat() * monotonePenalty

        Log.d("ScoringEngine", "--- PITCH ANALYSIS ---")
        Log.d("ScoringEngine", String.format("Absolute Similarity: %.3f", absoluteSimilarity))

        Log.d("ScoringEngine", "🔍 EXTRACTING MELODY SIGNATURES")
        val originalSignature = extractMelodySignature(originalPitches)
        val attemptSignature = extractMelodySignature(attemptPitches)

// NEW: Melodic Requirement Analysis
        val originalMelodicVariation = calculateMelodicVariation(originalPitches)
        val attemptMelodicVariation = calculateMelodicVariation(attemptPitches)

        Log.d("ScoringEngine", "🎵 MELODIC VARIATION ANALYSIS")
        Log.d("ScoringEngine", "Original melodic variation: ${String.format("%.3f", originalMelodicVariation)}")
        Log.d("ScoringEngine", "Attempt melodic variation: ${String.format("%.3f", attemptMelodicVariation)}")

        Log.d("ScoringEngine", "🔍 CALCULATING CONTENT SIMILARITY")
        val baseContentMetrics = calculateContentSimilarity(originalSignature, attemptSignature)


        // NEW: Content-Aware Melodic Penalty System
        val contentIndicators = listOf(
            baseContentMetrics.contourSimilarity,
            baseContentMetrics.intervalSimilarity,
            baseContentMetrics.phraseSimilarity,
            baseContentMetrics.rhythmSimilarity
        )
        val bestContentScore = contentIndicators.maxOrNull() ?: 0f
        val avgContentScore = contentIndicators.average().toFloat()

        val melodicRequirementPenalty = when {
            // CONTENT SEEMS RIGHT: Apply light melody-only penalties
            bestContentScore > contentParams.contentDetectionBestThreshold || avgContentScore > contentParams.contentDetectionAvgThreshold -> {
                when {
                    originalMelodicVariation > contentParams.highMelodicThreshold && attemptMelodicVariation < contentParams.lowMelodicThreshold -> {
                        Log.d("ScoringEngine", "📝 RIGHT CONTENT, FLAT DELIVERY: Light penalty (words correct, needs melody)")
                        contentParams.rightContentFlatPenalty
                    }
                    originalMelodicVariation > contentParams.mediumMelodicThreshold && attemptMelodicVariation < contentParams.insufficientMelodyThreshold -> {
                        Log.d("ScoringEngine", "🎵 RIGHT CONTENT, DIFFERENT MELODY: Minor penalty (words right, melody different)")
                        contentParams.rightContentDifferentMelodyPenalty
                    }
                    else -> {
                        Log.d("ScoringEngine", "✅ RIGHT CONTENT, GOOD MELODY: No penalty")
                        0f
                    }
                }
            }
            // CONTENT SEEMS WRONG: Apply harsh penalties
            originalMelodicVariation > contentParams.highMelodicThreshold && attemptMelodicVariation < contentParams.lowMelodicThreshold -> {
                Log.d("ScoringEngine", "🚨 WRONG CONTENT + FLAT DELIVERY: Severe penalty")
                contentParams.wrongContentFlatPenalty
            }
            originalMelodicVariation > contentParams.mediumMelodicThreshold && attemptMelodicVariation < contentParams.insufficientMelodyThreshold -> {
                Log.d("ScoringEngine", "❌ WRONG CONTENT + INSUFFICIENT MELODY: Heavy penalty")
                contentParams.wrongContentInsufficientPenalty
            }
            else -> {
                Log.d("ScoringEngine", "❌ WRONG CONTENT: Standard penalty for wrong words")
                contentParams.wrongContentStandardPenalty
            }
        }

        Log.d("ScoringEngine", "🧠 CONTENT ANALYSIS: Best=${String.format("%.3f", bestContentScore)}, Avg=${String.format("%.3f", avgContentScore)}")

// Apply penalty to content metrics
        val contentMetrics = ContentMetrics(
            contourSimilarity = baseContentMetrics.contourSimilarity,
            intervalSimilarity = baseContentMetrics.intervalSimilarity,
            phraseSimilarity = baseContentMetrics.phraseSimilarity,
            rhythmSimilarity = baseContentMetrics.rhythmSimilarity,
            overallContentScore = (baseContentMetrics.overallContentScore * (1f - melodicRequirementPenalty)).coerceIn(0f, 1f)
        )

        Log.d("ScoringEngine", "📊 MELODIC PENALTY: Base=${String.format("%.3f", baseContentMetrics.overallContentScore)}, Penalty=${String.format("%.1f", melodicRequirementPenalty * 100)}%, Final=${String.format("%.3f", contentMetrics.overallContentScore)}")

        Log.d("ScoringEngine", "🔍 CALCULATING VOCAL EFFORT SIMILARITY (for comparison)")
        val vocalEffortSimilarity = calculateVocalEffortSimilarity(originalPitches, attemptPitches)

// Combine melody signature content with vocal effort (weighted)
        val hybridContentScore = (contentMetrics.overallContentScore * 0.7f) + (vocalEffortSimilarity * 0.3f)

        Log.d("ScoringEngine", "--- PITCH ANALYSIS ---")
        Log.d("ScoringEngine", String.format("Absolute Similarity: %.3f", absoluteSimilarity))
        Log.d("ScoringEngine", String.format("Content Similarity (Melody): %.3f", contentMetrics.overallContentScore))
        Log.d("ScoringEngine", String.format("Content Similarity (Vocal Effort): %.3f", vocalEffortSimilarity))
        Log.d("ScoringEngine", String.format("Hybrid Content Score: %.3f", hybridContentScore))
        Log.d("ScoringEngine", String.format("Final Pitch Score: %.3f", hybridContentScore))

        Log.d("ScoringEngine", "--- DETAILED CONTENT METRICS ---")
        Log.d("ScoringEngine", String.format("Contour Similarity: %.3f", contentMetrics.contourSimilarity))
        Log.d("ScoringEngine", String.format("Interval Similarity: %.3f", contentMetrics.intervalSimilarity))
        Log.d("ScoringEngine", String.format("Phrase Similarity: %.3f", contentMetrics.phraseSimilarity))
        Log.d("ScoringEngine", String.format("Rhythm Similarity: %.3f", contentMetrics.rhythmSimilarity))

        return hybridContentScore
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

    // START: Melody Signature Extraction Functions
    private fun extractMelodySignature(pitches: List<Float>): MelodySignature {
        Log.d("ScoringEngine", "🎵 Extracting melody signature from ${pitches.size} pitch frames")

        // 1. Extract pitch contour (relative changes)
        val pitchContour = extractPitchContour(pitches)
        Log.d("ScoringEngine", "Pitch contour: ${pitchContour.take(10)}")

        // 2. Calculate musical intervals
        val intervalSequence = extractIntervalSequence(pitches)
        Log.d("ScoringEngine", "Intervals: ${intervalSequence.take(10)}")

        // 3. Detect phrase breaks
        val phraseBreaks = detectPhraseBreaks(pitches)
        Log.d("ScoringEngine", "Phrase breaks at indices: $phraseBreaks")

        // 4. Extract rhythm pattern
        val rhythmPattern = extractRhythmPattern(pitches)
        Log.d("ScoringEngine", "Rhythm pattern: $rhythmPattern")

        // 5. Calculate vocal density
        val vocalDensity = pitches.count { it != 0f }.toFloat() / pitches.size
        Log.d("ScoringEngine", "Vocal density: $vocalDensity")

        return MelodySignature(
            pitchContour = pitchContour,
            intervalSequence = intervalSequence,
            phraseBreaks = phraseBreaks,
            rhythmPattern = rhythmPattern,
            vocalDensity = vocalDensity
        )
    }


    private fun extractPitchContour(pitches: List<Float>): List<Float> {
        val activePitches = pitches.filter { it != 0f }
        if (activePitches.size < 2) return emptyList()

        // Convert to relative pitch changes (normalized to semitones)
        return activePitches.zipWithNext { current, next ->
            val change = next - current
            // Clamp large jumps to prevent octave errors from dominating
            change.coerceIn(-12f, 12f)
        }
    }

    private fun extractIntervalSequence(pitches: List<Float>): List<Float> {
        val activePitches = pitches.filter { it != 0f }
        if (activePitches.size < 2) return emptyList()

        // Calculate musical intervals between consecutive notes
        return activePitches.zipWithNext { current, next ->
            abs(next - current) // Absolute interval size (ignore direction)
        }.filter { it > 0.5f } // Ignore micro-variations, focus on real melodic movement
    }

    private fun detectPhraseBreaks(pitches: List<Float>): List<Int> {
        val phraseBreaks = mutableListOf<Int>()
        var silenceCount = 0
        val phraseBreakThreshold = 8 // Frames of silence to indicate phrase boundary

        for (i in pitches.indices) {
            if (pitches[i] == 0f) {
                silenceCount++
            } else {
                if (silenceCount >= phraseBreakThreshold) {
                    phraseBreaks.add(i) // Mark end of silence as phrase break
                }
                silenceCount = 0
            }
        }

        return phraseBreaks
    }

    private fun extractRhythmPattern(pitches: List<Float>): List<Float> {
        val segments = extractVocalSegments(pitches)
        if (segments.size < 2) return emptyList()

        // Calculate duration ratios between consecutive segments
        val durations = segments.map { (start, end) -> (end - start + 1).toFloat() }

        return durations.zipWithNext { current, next ->
            if (current > 0) next / current else 1f
        }.map { it.coerceIn(0.1f, 10f) } // Clamp extreme ratios
    }
// END: Melody Signature Extraction Functions

    // START: Content Similarity Comparison Functions
    private fun calculateContentSimilarity(
        originalSignature: MelodySignature,
        attemptSignature: MelodySignature
    ): ContentMetrics {
        Log.d("ScoringEngine", "🎯 Calculating content similarity between melody signatures")

        // 1. Compare pitch contours (melody shape)
        val contourSim = comparePitchContours(originalSignature.pitchContour, attemptSignature.pitchContour)
        Log.d("ScoringEngine", "Contour similarity: $contourSim")

        // 2. Compare musical intervals
        val intervalSim = compareIntervalSequences(originalSignature.intervalSequence, attemptSignature.intervalSequence)
        Log.d("ScoringEngine", "Interval similarity: $intervalSim")

        // 3. Compare phrase structures
        val phraseSim = comparePhraseStructures(originalSignature.phraseBreaks, attemptSignature.phraseBreaks)
        Log.d("ScoringEngine", "Phrase similarity: $phraseSim")

        // 4. Compare rhythm patterns
        val rhythmSim = compareMelodyRhythms(originalSignature.rhythmPattern, attemptSignature.rhythmPattern)
        Log.d("ScoringEngine", "Rhythm similarity: $rhythmSim")

        // 5. Calculate weighted overall content score
        val overallScore = (contourSim * 0.1f + intervalSim * 0.8f + phraseSim * 0.05f + rhythmSim * 0.05f)
        Log.d("ScoringEngine", "Overall content score: $overallScore")

        return ContentMetrics(
            contourSimilarity = contourSim,
            intervalSimilarity = intervalSim,
            phraseSimilarity = phraseSim,
            rhythmSimilarity = rhythmSim,
            overallContentScore = overallScore
        )
    }

    private fun comparePitchContours(contour1: List<Float>, contour2: List<Float>): Float {
        if (contour1.isEmpty() || contour2.isEmpty()) return 0f

        // Use Dynamic Time Warping on the contours to handle timing differences
        val minLength = min(contour1.size, contour2.size)
        val maxLength = max(contour1.size, contour2.size)

        // If length difference is too extreme, penalize heavily
        if (minLength.toFloat() / maxLength < 0.5f) return 0.2f

        // Compare corresponding points with tolerance for small differences
        val similarities = (0 until minLength).map { i ->
            val diff = abs(contour1[i] - contour2[i])
            exp(-diff / 3f) // Exponential decay for differences
        }

        return similarities.average().toFloat()
    }

    /**
     * Detects if an attempt has sufficient melodic variation for a melodic challenge
     * @param pitches The pitch sequence to analyze
     * @return Float between 0.0 (completely flat) and 1.0 (highly melodic)
     */
    private fun calculateMelodicVariation(pitches: List<Float>): Float {
        val vocalPitches = pitches.filter { it != 0f }
        if (vocalPitches.size < 3) return 0f

        // 1. Calculate pitch range (how much the voice moves up/down)
        val pitchRange = vocalPitches.maxOrNull()!! - vocalPitches.minOrNull()!!
        val rangeScore = (pitchRange / melodicParams.melodicRangeSemitones).coerceAtMost(1f)

        // 2. Calculate pitch transitions (how often pitch changes)
        var transitions = 0
        for (i in 1 until vocalPitches.size) {
            if (abs(vocalPitches[i] - vocalPitches[i - 1]) > melodicParams.melodicTransitionThreshold) {
                transitions++
            }
        }
        val transitionScore = (transitions.toFloat() / vocalPitches.size).coerceAtMost(1f)

        // 3. Calculate pitch variance (how much variation exists)
        val variance = calculatePitchVariance(pitches)
        val varianceScore = (variance / melodicParams.melodicVarianceThreshold).coerceAtMost(1f)

        // Weighted combination - all three factors must be present for melodic singing
        return (rangeScore * melodicParams.melodicRangeWeight + transitionScore * melodicParams.melodicTransitionWeight + varianceScore * melodicParams.melodicVarianceWeight).coerceIn(0f, 1f)
    }

    private fun compareIntervalSequences(intervals1: List<Float>, intervals2: List<Float>): Float {
        if (intervals1.isEmpty() || intervals2.isEmpty()) return 0f

        val minLength = min(intervals1.size, intervals2.size)
        if (minLength == 0) return 0f

        // Compare musical intervals with tolerance for microtonal differences
        val similarities = (0 until minLength).map { i ->
            val diff = abs(intervals1[i] - intervals2[i])
            when {
                diff < musicalParams.sameIntervalThreshold -> musicalParams.sameIntervalScore
                diff < musicalParams.closeIntervalThreshold -> musicalParams.closeIntervalScore
                diff < musicalParams.similarIntervalThreshold -> musicalParams.similarIntervalScore
                else -> musicalParams.differentIntervalScore
            }
        }

        return similarities.average().toFloat()
    }

    private fun comparePhraseStructures(phrases1: List<Int>, phrases2: List<Int>): Float {
        // If both have no phrases, they're similar in structure
        if (phrases1.isEmpty() && phrases2.isEmpty()) return 1f
        if (phrases1.isEmpty() || phrases2.isEmpty()) return musicalParams.emptyPhrasesPenalty

        // Compare number of phrases (phrase count similarity)
        val countSimilarity = min(phrases1.size, phrases2.size).toFloat() / max(phrases1.size, phrases2.size)

        // If phrase counts are very different, return early with low score
        if (countSimilarity < musicalParams.phraseCountDifferenceThreshold) return countSimilarity * musicalParams.phraseCountPenaltyMultiplier

        // Compare relative phrase positions (normalized)
        val normalizedPhrases1 = if (phrases1.isNotEmpty()) phrases1.map { it.toFloat() / phrases1.maxOrNull()!! } else emptyList()
        val normalizedPhrases2 = if (phrases2.isNotEmpty()) phrases2.map { it.toFloat() / phrases2.maxOrNull()!! } else emptyList()

        val minPhrases = min(normalizedPhrases1.size, normalizedPhrases2.size)
        val positionSimilarity = if (minPhrases > 0) {
            (0 until minPhrases).map { i ->
                1f - abs(normalizedPhrases1[i] - normalizedPhrases2[i])
            }.average().toFloat()
        } else 1f

        return (countSimilarity + positionSimilarity) / musicalParams.phraseWeightBalance
    }

    private fun compareMelodyRhythms(rhythm1: List<Float>, rhythm2: List<Float>): Float {
        if (rhythm1.isEmpty() && rhythm2.isEmpty()) return 1f
        if (rhythm1.isEmpty() || rhythm2.isEmpty()) return musicalParams.emptyRhythmPenalty

        val minLength = min(rhythm1.size, rhythm2.size)
        if (minLength == 0) return 0f

        // Compare rhythm ratios with tolerance
        val similarities = (0 until minLength).map { i ->
            val ratio = min(rhythm1[i], rhythm2[i]) / max(rhythm1[i], rhythm2[i])
            ratio.pow(musicalParams.rhythmDifferenceSoftening)
        }

        return similarities.average().toFloat()
    }
// END: Content Similarity Comparison Functions

    private fun calculateRhythmPattern(segments: List<Pair<Int, Int>>): List<Int> {
        return segments.map { (start, end) -> end - start + 1 } // Duration of each segment
    }

    private fun compareSegmentCounts(originalCount: Int, attemptCount: Int): Float {
        if (originalCount == 0 && attemptCount == 0) return 1f
        if (originalCount == 0 || attemptCount == 0) return 0f

        val ratio = min(originalCount, attemptCount).toFloat() / max(originalCount, attemptCount)
        return ratio.pow(musicalParams.segmentCountSoftening)
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
        val threshold = audioParams.audioAlignmentThreshold
        val originalStart = original.indexOfFirst { abs(it) > threshold }.takeIf { it != -1 } ?: 0
        val attemptStart = attempt.indexOfFirst { abs(it) > threshold }.takeIf { it != -1 } ?: 0
        val minLength = min(original.size - originalStart, attempt.size - attemptStart)
        if (minLength <= 0) return Pair(FloatArray(0), FloatArray(0))
        return Pair(original.sliceArray(originalStart until originalStart + minLength), attempt.sliceArray(attemptStart until attemptStart + minLength))
    }

    private fun scaleScore(rawScore: Float, challengeType: ChallengeType): Int {
        val clamped = rawScore.coerceIn(0f, 1f)

        Log.d("ScoringEngine", "🔢 SCORE SCALING VERIFICATION:")
        Log.d("ScoringEngine", "   Raw Score: $rawScore → Clamped: $clamped")
        Log.d("ScoringEngine", "   Using thresholds: ${parameters.minScoreThreshold} to ${parameters.perfectScoreThreshold}")
        Log.d("ScoringEngine", "   Using curve: ${parameters.scoreCurve}")

        val (min, perfect, curve) = if (challengeType == ChallengeType.REVERSE) {
            Triple(parameters.minScoreThreshold * scalingParams.reverseMinScoreAdjustment, parameters.perfectScoreThreshold * scalingParams.reversePerfectScoreAdjustment, parameters.scoreCurve * scalingParams.reverseCurveAdjustment)
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
                val curved = normalized.pow(1f / max(scalingParams.minimumCurveProtection, curve))
                (curved * 100).roundToInt()
            }
        }
    }

    private fun generateFeedback(score: Int, metrics: SimilarityMetrics): List<String> {
        val feedback = mutableListOf<String>()
        feedback.add(when {
            score >= scalingParams.incredibleFeedbackThreshold -> "🌟 Incredible! You're a reverse singing master!"
            score >= scalingParams.greatJobFeedbackThreshold -> "🎯 Great job! You're really getting the hang of this!"
            score >= scalingParams.goodEffortFeedbackThreshold -> "👍 Good effort! Keep practicing!"
            else -> "💪 Nice try! Reverse singing is tough!"
        })
        val weakestMetric = if (metrics.pitch < metrics.mfcc) "pitch" else "timbre"
        if (min(metrics.pitch, metrics.mfcc) < scalingParams.additionalFeedbackThreshold) {
            feedback.add(when(weakestMetric) {
                "pitch" -> "💡 Try to match the melody's shape more closely."
                else -> "💡 Work on matching the vocal sound and quality."
            })
        }
        return feedback
    }

    fun updateParameters(newParams: ScoringParameters) {
        Log.d("UPDATE_PARAMS", "=== STARTING updateParameters ===")
        Log.d("PARAM_CHANGE", "🔄 PARAMETERS CHANGING ON THREAD: ${Thread.currentThread().name}")

        if (newParams == null) {
            Log.e("UPDATE_PARAMS", "ERROR: newParams is null!")
            return
        }

        Log.d("UPDATE_PARAMS", "=== UPDATING PARAMETERS ===")
        Log.d("UPDATE_PARAMS", "OLD pitchTolerance: ${parameters.pitchTolerance}")
        Log.d("UPDATE_PARAMS", "NEW pitchTolerance: ${newParams.pitchTolerance}")
        Log.d("UPDATE_PARAMS", "OLD score range: ${parameters.minScoreThreshold} to ${parameters.perfectScoreThreshold}")
        Log.d("UPDATE_PARAMS", "NEW score range: ${newParams.minScoreThreshold} to ${newParams.perfectScoreThreshold}")

        // AGGRESSIVE CALLER DETECTION
        Log.d("PARAM_CHANGE", "🔍 FULL STACK TRACE (ALL FRAMES):")
        Thread.currentThread().stackTrace.forEachIndexed { index, element ->
            if (index > 1) { // Skip Thread.getStackTrace and this method
                Log.d("PARAM_CHANGE", "     [$index] ${element.className}.${element.methodName}(${element.fileName}:${element.lineNumber})")
            }
        }

        parameters = newParams

        Log.d("UPDATE_PARAMS", "AFTER UPDATE pitchTolerance: ${parameters.pitchTolerance}")
        Log.d("UPDATE_PARAMS", "=== PARAMETERS UPDATED ===")
    }

    fun updateAudioParameters(newParams: AudioProcessingParameters) {
        audioParams = newParams
    }

    fun updateContentParameters(newParams: ContentDetectionParameters) {
        contentParams = newParams
    }

    fun updateMelodicParameters(newParams: MelodicAnalysisParameters) {
        melodicParams = newParams
    }

    fun updateMusicalParameters(newParams: MusicalSimilarityParameters) {
        musicalParams = newParams
    }

    fun updateScalingParameters(newParams: ScoreScalingParameters) {
        scalingParams = newParams
    }

    fun getParameters(): ScoringParameters = parameters
    fun getAudioParameters(): AudioProcessingParameters = audioParams
    fun getContentParameters(): ContentDetectionParameters = contentParams
    fun getMelodicParameters(): MelodicAnalysisParameters = melodicParams
    fun getMusicalParameters(): MusicalSimilarityParameters = musicalParams
    fun getScalingParameters(): ScoreScalingParameters = scalingParams

    // NEW: Difficulty level tracking methods
    fun getCurrentDifficulty(): DifficultyLevel = currentDifficulty
    fun setCurrentDifficulty(difficulty: DifficultyLevel) {
        currentDifficulty = difficulty
    }
    // Apply a complete preset configuration
    fun applyPreset(preset: Presets) {
        Log.d("APPLY_PRESET", "=== APPLYING PRESET: ${preset.difficulty.displayName} ===")

        try {
            // Update all parameter categories
            Log.d("APPLY_PRESET", "About to call updateParameters...")
            updateParameters(preset.scoring)
            Log.d("APPLY_PRESET", "updateParameters completed successfully")

            Log.d("APPLY_PRESET", "About to call updateContentParameters...")
            updateContentParameters(preset.content)
            Log.d("APPLY_PRESET", "updateContentParameters completed successfully")

            updateMelodicParameters(preset.melodic)
            updateMusicalParameters(preset.musical)
            updateScalingParameters(preset.scaling)
            setCurrentDifficulty(preset.difficulty)
        } catch (e: Exception) {
            Log.e("APPLY_PRESET", "ERROR applying preset: ${e.message}")
            Log.e("APPLY_PRESET", "Stack trace: ${e.stackTrace.joinToString("\n")}")
        }

        Log.d("APPLY_PRESET", "=== PRESET APPLIED ===")
    }
}