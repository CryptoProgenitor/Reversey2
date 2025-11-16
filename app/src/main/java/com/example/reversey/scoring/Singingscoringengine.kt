package com.example.reversey.scoring

import android.content.Context
import android.util.Log
import com.example.reversey.data.models.ChallengeType
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import com.example.reversey.data.repositories.SettingsDataStore

/**
 * 🎵 SINGING SCORING ENGINE
 *
 * Specialized scoring engine optimized for musical/singing patterns.
 * Uses SingingScoringModels parameter sets and music-focused algorithms.
 *
 * SINGING OPTIMIZATIONS:
 * - Lower pitch tolerance (precise musical notes required)
 * - Melody-focused scoring (pitch accuracy dominates)
 * - Enhanced musical analysis (intervals, phrases, harmony)
 * - Strict garbage detection for non-musical attempts
 * - Reward musical complexity and vocal technique
 *
 * GLUTE Principle: Unified architecture with musical specialization
 */
@Singleton
class SingingScoringEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsDataStore: SettingsDataStore
) {

    private val audioProcessor = AudioProcessor()
    private val garbageDetector = GarbageDetector(audioProcessor)

    // Singing-optimized parameter instances
    private var parameters = ScoringParameters()
    private var audioParams = AudioProcessingParameters()
    private var contentParams = ContentDetectionParameters()
    private var melodicParams = MelodicAnalysisParameters()
    private var musicalParams = MusicalSimilarityParameters()
    private var scalingParams = ScoreScalingParameters()
    private var garbageParams = GarbageDetectionParameters()

    private val engineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Track current difficulty and initialization
    private val _currentDifficulty = MutableStateFlow(DifficultyLevel.NORMAL)
    val currentDifficultyFlow: StateFlow<DifficultyLevel> = _currentDifficulty.asStateFlow()

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    init {
        Log.d("HILT_VERIFY", "🎵 SingingScoringEngine created - Instance: ${this.hashCode()}")

        // Load saved difficulty and apply singing-optimized presets
        engineScope.launch {
            val savedDifficulty = settingsDataStore.getDifficultyLevel.first()
            val difficulty = try {
                DifficultyLevel.valueOf(savedDifficulty)
            } catch (e: Exception) {
                DifficultyLevel.NORMAL
            }
            _currentDifficulty.value = difficulty

            // Apply singing-optimized preset parameters
            val preset = when(difficulty) {
                DifficultyLevel.EASY -> SingingScoringModels.easyModeSinging()
                DifficultyLevel.NORMAL -> SingingScoringModels.normalModeSinging()
                DifficultyLevel.HARD -> SingingScoringModels.hardModeSinging()
                DifficultyLevel.EXPERT -> SingingScoringModels.expertModeSinging()
                DifficultyLevel.MASTER -> SingingScoringModels.masterModeSinging()
            }
            applyPreset(preset)

            _isInitialized.value = true
            Log.d("SINGING_ENGINE", "🎵 Singing engine initialized with difficulty: ${difficulty.displayName}")
        }
    }

    /**
     * Apply singing-optimized preset parameters
     */
    private fun applyPreset(preset: Presets) {
        parameters = preset.scoring
        audioParams = preset.audio
        contentParams = preset.content
        melodicParams = preset.melodic
        musicalParams = preset.musical
        scalingParams = preset.scaling
        garbageParams = preset.garbage

        Log.d("SINGING_ENGINE", "🎵 Applied singing preset for ${preset.difficulty.displayName}")
        Log.d("SINGING_ENGINE", "   🎼 Pitch tolerance: ${parameters.pitchTolerance}f (strict for music)")
        Log.d("SINGING_ENGINE", "   🎼 Pitch weight: ${parameters.pitchWeight}f (melody dominates)")
        Log.d("SINGING_ENGINE", "   🎼 Monotone threshold: ${garbageParams.pitchMonotoneThreshold}f")
    }

    /**
     * Main singing scoring method
     * Optimized for musical patterns and melodic accuracy
     */
    fun scoreAttempt(
        originalAudio: FloatArray,
        playerAttempt: FloatArray,
        challengeType: ChallengeType,
        sampleRate: Int = 44100
    ): ScoringResult {

        Log.d("SINGING_ENGINE", "=== SINGING SCORING ENGINE ===")
        Log.d("SINGING_ENGINE", "🎵 Difficulty: ${_currentDifficulty.value.displayName}")
        Log.d("SINGING_ENGINE", "🎼 Pitch tolerance: ${parameters.pitchTolerance}f (music-precise)")
        Log.d("SINGING_ENGINE", "🎶 Melody focus: ${parameters.pitchWeight}f pitch weight")
        Log.d("SINGING_ENGINE", "🎤 Challenge type: $challengeType")

        // Silence detection
        val attemptRMS = audioProcessor.calculateRMS(playerAttempt)
        if (attemptRMS < parameters.silenceThreshold) {
            Log.d("SINGING_ENGINE", "🔇 Silence detected: RMS ($attemptRMS) < threshold")
            return ScoringResult(
                score = 0,
                rawScore = 0f,
                metrics = SimilarityMetrics(0f, 0f),
                feedback = listOf("🎵 Please sing louder - we need to hear your beautiful voice!"),
                isGarbage = false
            )
        }

        // Audio alignment with musical precision
        val (alignedOriginal, alignedAttempt) = alignAudioMusical(originalAudio, playerAttempt)
        if (alignedOriginal.isEmpty() || alignedAttempt.isEmpty()) {
            Log.d("SINGING_ENGINE", "❌ Musical audio alignment failed")
            return ScoringResult(
                score = 0,
                rawScore = 0f,
                metrics = SimilarityMetrics(0f, 0f),
                feedback = listOf("Recording too short for musical analysis - sing longer!"),
                isGarbage = false
            )
        }

        // Extract musical features
        val originalPitchSequence = getPitchSequence(alignedOriginal, sampleRate)
        val attemptPitchSequence = getPitchSequence(alignedAttempt, sampleRate)
        val originalMfccSequence = getMfccSequence(alignedOriginal, sampleRate)
        val attemptMfccSequence = getMfccSequence(alignedAttempt, sampleRate)

        // Singing-optimized garbage detection (strict for musical quality)
        val garbageAnalysis = performSingingGarbageDetection(
            alignedAttempt, attemptPitchSequence, attemptMfccSequence, sampleRate
        )

        if (garbageAnalysis.isGarbage) {
            Log.d("SINGING_ENGINE", "🗑️ Singing attempt flagged as non-musical garbage")
            return ScoringResult(
                score = garbageParams.garbageScoreMax,
                rawScore = garbageParams.garbageScoreMax / 100f,
                metrics = SimilarityMetrics(0f, 0f),
                feedback = listOf("Please sing with clear musical notes - that didn't sound like singing!"),
                isGarbage = true
            )
        }

        // Calculate singing-optimized similarity metrics
        val pitchSimilarity = calculateMusicalPitchSimilarity(originalPitchSequence, attemptPitchSequence)
        val mfccSimilarity = calculateMfccSimilarity(originalMfccSequence, attemptMfccSequence)

        // Advanced musical analysis
        val musicalComplexityBonus = calculateMusicalComplexityBonus(attemptPitchSequence)
        val intervalAccuracy = calculateIntervalAccuracy(originalPitchSequence, attemptPitchSequence)
        val harmonicRichness = calculateHarmonicRichness(attemptMfccSequence)

        Log.d("SINGING_ENGINE", "🎼 Musical metrics:")
        Log.d("SINGING_ENGINE", "   Pitch: $pitchSimilarity, MFCC: $mfccSimilarity")
        Log.d("SINGING_ENGINE", "   Complexity bonus: $musicalComplexityBonus")
        Log.d("SINGING_ENGINE", "   Interval accuracy: $intervalAccuracy")
        Log.d("SINGING_ENGINE", "   Harmonic richness: $harmonicRichness")

        val metrics = SimilarityMetrics(pitchSimilarity, mfccSimilarity)

        // Music-focused weighted score (heavily emphasizes melody)
        var rawScore = (pitchSimilarity * parameters.pitchWeight) + (mfccSimilarity * parameters.mfccWeight)

        // Apply musical bonuses
        rawScore += musicalComplexityBonus * 0.1f  // Up to 10% bonus for complexity
        rawScore += intervalAccuracy * 0.15f        // Up to 15% bonus for accurate intervals
        rawScore += harmonicRichness * 0.05f        // Up to 5% bonus for rich harmonics

        Log.d("SINGING_ENGINE", "🎯 Raw score with musical bonuses: $rawScore")

        // Musical variance penalty (strict for singing)
        val variancePenalty = calculateMusicalVariancePenalty(originalPitchSequence, attemptPitchSequence)
        rawScore *= variancePenalty
        Log.d("SINGING_ENGINE", "📉 After musical variance penalty (${variancePenalty}x): $rawScore")

        // Musical performance bonuses
        val consistency = (1 - abs(pitchSimilarity - mfccSimilarity)) * parameters.consistencyBonus
        val confidence = min(1f, attemptRMS * scalingParams.rmsConfidenceMultiplier) * parameters.confidenceBonus
        val scoreWithBonuses = rawScore * (1 + consistency + confidence)

        Log.d("SINGING_ENGINE", "🎁 After performance bonuses: $scoreWithBonuses")

        // Humming detection (more nuanced for singing)
        val hummingDetected = garbageAnalysis.filterResults["humming_detected"] == 1f
        val adjustedScore = if (hummingDetected && !garbageAnalysis.isGarbage) {
            // For singing, humming might be intentional - lighter penalty than speech
            Log.d("SINGING_ENGINE", "🎵 Musical humming detected - applying moderate penalty")
            scoreWithBonuses * 0.8f  // Less harsh than speech for musical context
        } else {
            scoreWithBonuses
        }

        // Final musical scaling
        val finalScore = scaleMusicalScore(adjustedScore, challengeType)

        Log.d("SINGING_ENGINE", "🏁 FINAL MUSICAL SCORE: $finalScore")
        Log.d("SINGING_ENGINE", "==========================")

        return ScoringResult(
            score = finalScore,
            rawScore = rawScore,
            metrics = metrics,
            feedback = generateMusicalFeedback(finalScore, metrics, challengeType,
                musicalComplexityBonus, intervalAccuracy, harmonicRichness),
            isGarbage = false
        )
    }

    /**
     * Musical pitch similarity - strict requirements for note accuracy
     */
    private fun calculateMusicalPitchSimilarity(
        originalPitches: List<Float>,
        attemptPitches: List<Float>
    ): Float {
        if (originalPitches.size < 2 || attemptPitches.size < 2) return 0f

        Log.d("SINGING_ENGINE", "🎼 Musical pitch analysis - lengths: orig=${originalPitches.size}, attempt=${attemptPitches.size}")

        val minLen = min(originalPitches.size, attemptPitches.size)
        var totalSimilarity = 0f
        var validComparisons = 0

        // Musical pitch comparison (precise note matching required)
        for (i in 0 until minLen) {
            val origPitch = originalPitches[i]
            val attemptPitch = attemptPitches[i]

            if (origPitch > 0f && attemptPitch > 0f) {
                val pitchDifference = abs(origPitch - attemptPitch)

                // Musical: Use tight tolerance and steep decay for accuracy
                val similarity = when {
                    pitchDifference <= parameters.pitchTolerance * 0.3f -> 1f  // Perfect musical range (tighter)
                    pitchDifference <= parameters.pitchTolerance -> {
                        // Steep decay for musical precision
                        val decayFactor = exp(-pitchDifference / (parameters.pitchTolerance * 0.2f))
                        decayFactor.coerceIn(0.1f, 1f)  // Steeper falloff for musical accuracy
                    }
                    else -> 0.05f  // Very low credit for off-pitch singing
                }

                totalSimilarity += similarity
                validComparisons++
            } else {
                // Musical: Silence-to-silence less generous than speech
                totalSimilarity += melodicParams.silenceToSilenceScore
                validComparisons++
            }
        }

        val result = if (validComparisons > 0) totalSimilarity / validComparisons else 0f
        Log.d("SINGING_ENGINE", "🎼 Musical pitch similarity: $result")
        return result
    }

    /**
     * Musical variance penalty - strict about monotone singing
     */
    private fun calculateMusicalVariancePenalty(
        originalPitches: List<Float>,
        attemptPitches: List<Float>
    ): Float {
        val originalVariance = calculatePitchVariance(originalPitches)
        val attemptVariance = calculatePitchVariance(attemptPitches)

        Log.d("SINGING_ENGINE", "🎼 Musical variance analysis - Original: $originalVariance, Attempt: $attemptVariance")

        // Musical: Strict about maintaining melodic variation
        return if (originalVariance > melodicParams.monotoneDetectionThreshold &&
            attemptVariance < melodicParams.flatSpeechThreshold) {
            val penalty = melodicParams.monotonePenalty
            Log.d("SINGING_ENGINE", "📉 Applying musical monotone penalty: $penalty")
            penalty
        } else {
            1.0f  // No penalty for proper musical variation
        }
    }

    /**
     * Calculate musical complexity bonus
     */
    private fun calculateMusicalComplexityBonus(pitches: List<Float>): Float {
        val validPitches = pitches.filter { it > 0f }
        if (validPitches.size < 4) return 0f

        // Analyze pitch range and transitions for musical complexity
        val pitchRange = validPitches.maxOrNull()!! - validPitches.minOrNull()!!
        val transitions = validPitches.zipWithNext { a, b -> abs(b - a) }.average().toFloat()

        val rangeBonus = min(pitchRange / 24f, 1f)  // 24 semitones = 2 octaves max bonus
        val transitionBonus = min(transitions / 6f, 1f)  // Moderate transition reward

        val complexityBonus = (rangeBonus * melodicParams.melodicRangeWeight +
                transitionBonus * melodicParams.melodicTransitionWeight) /
                (melodicParams.melodicRangeWeight + melodicParams.melodicTransitionWeight)

        Log.d("SINGING_ENGINE", "🎼 Complexity analysis - Range: $pitchRange, Transitions: $transitions, Bonus: $complexityBonus")
        return complexityBonus
    }

    /**
     * Calculate interval accuracy for musical assessment
     */
    private fun calculateIntervalAccuracy(
        originalPitches: List<Float>,
        attemptPitches: List<Float>
    ): Float {
        if (originalPitches.size < 3 || attemptPitches.size < 3) return 0f

        // Extract musical intervals (pitch differences between adjacent notes)
        val originalIntervals = originalPitches.zipWithNext { a, b -> b - a }.filter { abs(it) > 0.1f }
        val attemptIntervals = attemptPitches.zipWithNext { a, b -> b - a }.filter { abs(it) > 0.1f }

        if (originalIntervals.isEmpty() || attemptIntervals.isEmpty()) return 0f

        val minLen = min(originalIntervals.size, attemptIntervals.size)
        var intervalMatches = 0f

        // Compare musical intervals with strict tolerance
        for (i in 0 until minLen) {
            val originalInterval = originalIntervals[i]
            val attemptInterval = attemptIntervals[i]
            val intervalDifference = abs(originalInterval - attemptInterval)

            val match = when {
                intervalDifference <= musicalParams.sameIntervalThreshold -> musicalParams.sameIntervalScore
                intervalDifference <= musicalParams.closeIntervalThreshold -> musicalParams.closeIntervalScore
                intervalDifference <= musicalParams.similarIntervalThreshold -> musicalParams.similarIntervalScore
                else -> musicalParams.differentIntervalScore
            }

            intervalMatches += match
        }

        val accuracy = intervalMatches / minLen
        Log.d("SINGING_ENGINE", "🎼 Interval accuracy: $accuracy")
        return accuracy
    }

    /**
     * Calculate harmonic richness from MFCC data
     */
    private fun calculateHarmonicRichness(mfccFrames: List<FloatArray>): Float {
        if (mfccFrames.isEmpty()) return 0f

        // Analyze spectral richness from MFCC coefficients
        val avgSpectralSpread = mfccFrames.map { frame ->
            frame.drop(1).take(6).map { abs(it) }.sum() // Focus on harmonic content coefficients
        }.average().toFloat()

        // Normalize to 0-1 range (typical MFCC values)
        val richness = min(avgSpectralSpread / 10f, 1f)

        Log.d("SINGING_ENGINE", "🎼 Harmonic richness: $richness")
        return richness
    }

    /**
     * Musical-optimized garbage detection
     */
    private fun performSingingGarbageDetection(
        audio: FloatArray,
        pitches: List<Float>,
        mfccFrames: List<FloatArray>,
        sampleRate: Int
    ): GarbageAnalysis {

        // Convert to frames for garbage detector
        val frameSize = audioParams.mfccFrameSize
        val hopSize = audioParams.mfccHopSize
        val audioFrames = mutableListOf<FloatArray>()
        var i = 0
        while (i + frameSize <= audio.size) {
            audioFrames.add(audio.sliceArray(i until i + frameSize))
            i += hopSize
        }

        // Use singing-optimized parameters for strict musical garbage detection
        return garbageDetector.detectGarbage(
            audioFrames = audioFrames,
            pitches = pitches,
            mfccFrames = mfccFrames,
            sampleRate = sampleRate
        )
    }

    /**
     * Musical score scaling
     */
    private fun scaleMusicalScore(rawScore: Float, challengeType: ChallengeType): Int {
        var scaledScore = rawScore

        // Apply challenge type adjustments for singing (more demanding)
        if (challengeType == ChallengeType.REVERSE) {
            scaledScore *= scalingParams.reversePerfectScoreAdjustment
            // Additional reverse singing difficulty
            scaledScore *= 0.95f  // 5% additional difficulty for reverse singing
            Log.d("SINGING_ENGINE", "🔄 Applied reverse singing adjustments")
        }

        // Musical scaling curve (more demanding than speech)
        val normalizedScore = (scaledScore - parameters.minScoreThreshold) /
                (parameters.perfectScoreThreshold - parameters.minScoreThreshold)

        val curveAdjustedScore = if (normalizedScore > 0) {
            normalizedScore.pow(1f / parameters.scoreCurve)
        } else {
            0f
        }

        val finalScore = (curveAdjustedScore * 100f).roundToInt().coerceIn(0, 100)
        Log.d("SINGING_ENGINE", "🎼 Musical scaling: raw=$rawScore, normalized=$normalizedScore, curved=$curveAdjustedScore, final=$finalScore")

        return finalScore
    }

    /**
     * Generate musical feedback
     */
    private fun generateMusicalFeedback(
        score: Int,
        metrics: SimilarityMetrics,
        challengeType: ChallengeType,
        complexityBonus: Float,
        intervalAccuracy: Float,
        harmonicRichness: Float
    ): List<String> {
        val feedback = mutableListOf<String>()

        // Musical score feedback
        when {
            score >= scalingParams.incredibleFeedbackThreshold -> {
                feedback.add("🎵 Incredible musical performance! You have real singing talent!")
            }
            score >= scalingParams.greatJobFeedbackThreshold -> {
                feedback.add("🎶 Beautiful singing! Your pitch accuracy was impressive!")
            }
            score >= scalingParams.goodEffortFeedbackThreshold -> {
                feedback.add("🎤 Good musical effort! Your singing is improving!")
            }
            else -> {
                feedback.add("🎼 Keep practicing! Focus on hitting the right notes.")
            }
        }

        // Musical technique feedback
        if (complexityBonus > 0.7f) {
            feedback.add("🎶 Excellent vocal range and musical expression!")
        }

        if (intervalAccuracy > 0.8f) {
            feedback.add("🎼 Outstanding interval accuracy - you really understand the melody!")
        }

        if (harmonicRichness > 0.6f) {
            feedback.add("🎵 Beautiful harmonic richness in your voice!")
        }

        // Musical improvement suggestions
        if (metrics.pitch < 0.6f) {
            feedback.add("💡 Focus on pitch accuracy - try to match each note precisely.")
        }

        if (complexityBonus < 0.3f) {
            feedback.add("🎯 Try to use more vocal range and expression in your singing.")
        }

        // Challenge type specific musical advice
        if (challengeType == ChallengeType.REVERSE) {
            feedback.add("🔄 Reverse singing is musically challenging - listen for the melodic patterns!")
        }

        return feedback
    }

    // Utility methods (similar to SpeechScoringEngine but music-optimized)

    private fun alignAudioMusical(original: FloatArray, attempt: FloatArray): Pair<FloatArray, FloatArray> {
        Log.d("SINGING_ENGINE", "🎼 Aligning musical audio with precision...")

        // Musical alignment considers both energy and pitch characteristics
        val originalStart = findMusicalStart(original)
        val attemptStart = findMusicalStart(attempt)

        val alignedLength = min(original.size - originalStart, attempt.size - attemptStart)
        if (alignedLength <= 0) return Pair(FloatArray(0), FloatArray(0))

        return Pair(
            original.sliceArray(originalStart until originalStart + alignedLength),
            attempt.sliceArray(attemptStart until attemptStart + alignedLength)
        )
    }

    private fun findMusicalStart(audio: FloatArray): Int {
        // Musical start detection - looks for sustained energy (like vocal onset)
        val windowSize = 1024
        val threshold = 0.015f  // Higher threshold for musical content

        for (i in 0 until audio.size - windowSize step 256) {
            val window = audio.sliceArray(i until i + windowSize)
            val rms = sqrt(window.map { it * it }.average()).toFloat()
            if (rms > threshold) return i
        }
        return 0
    }

    private fun calculatePitchVariance(pitches: List<Float>): Float {
        val validPitches = pitches.filter { it > 0f }
        if (validPitches.size < 2) return 0f

        val mean = validPitches.average().toFloat()
        val variance = validPitches.map { (it - mean) * (it - mean) }.average().toFloat()
        return sqrt(variance)
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

    private fun calculateMfccSimilarity(originalMfccs: List<FloatArray>, attemptMfccs: List<FloatArray>): Float {
        if (originalMfccs.isEmpty() || attemptMfccs.isEmpty()) return 0f

        return dtw(originalMfccs, attemptMfccs) { a, b ->
            euclideanDistance(a, b) / getCurrentScoringParameters().dtwNormalizationFactor
        }
    }

    private fun <T> dtw(seq1: List<T>, seq2: List<T>, costFunc: (T, T) -> Float): Float {
        val m = seq1.size
        val n = seq2.size
        val dp = Array(m + 1) { FloatArray(n + 1) { Float.MAX_VALUE } }
        dp[0][0] = 0f

        for (i in 1..m) {
            for (j in 1..n) {
                val cost = costFunc(seq1[i - 1], seq2[j - 1])
                dp[i][j] = cost + minOf(dp[i - 1][j], dp[i][j - 1], dp[i - 1][j - 1])
            }
        }

        val maxDtwCost = (m + n) * 2f
        val normalizedDtw = (maxDtwCost - dp[m][n]) / maxDtwCost
        return normalizedDtw.coerceIn(0f, 1f)
    }

    private fun euclideanDistance(vec1: FloatArray, vec2: FloatArray): Float {
        val minLen = min(vec1.size, vec2.size)
        return sqrt((0 until minLen).map { (vec1[it] - vec2[it]).let { diff -> diff * diff } }.sum())
    }

    /**
     * Get current difficulty level
     */
    fun getCurrentDifficulty(): DifficultyLevel = _currentDifficulty.value

    /**
     * Update difficulty and apply new singing preset
     */
    fun updateDifficulty(newDifficulty: DifficultyLevel) {
        _currentDifficulty.value = newDifficulty
        val preset = when(newDifficulty) {
            DifficultyLevel.EASY -> SingingScoringModels.easyModeSinging()
            DifficultyLevel.NORMAL -> SingingScoringModels.normalModeSinging()
            DifficultyLevel.HARD -> SingingScoringModels.hardModeSinging()
            DifficultyLevel.EXPERT -> SingingScoringModels.expertModeSinging()
            DifficultyLevel.MASTER -> SingingScoringModels.masterModeSinging()
        }
        applyPreset(preset)
        Log.d("SINGING_ENGINE", "🎵 Updated to ${newDifficulty.displayName} singing preset")
    }

    private fun getCurrentScoringParameters(): ScoringParameters {
        // Simple fallback - use default ScoringParameters with proper dtwNormalizationFactor
        return ScoringParameters()
    }

}