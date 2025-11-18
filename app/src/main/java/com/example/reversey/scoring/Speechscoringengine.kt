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
 * 🎤 SPEECH SCORING ENGINE
 *
 * Specialized scoring engine optimized for speech patterns.
 * Uses SpeechScoringModels parameter sets and speech-appropriate algorithms.
 *
 * SPEECH OPTIMIZATIONS:
 * - Higher pitch tolerance (speech is naturally less melodic)
 * - Content-focused scoring (getting words right matters most)
 * - Reduced melodic requirements (monotone speech acceptable)
 * - Speech-appropriate garbage detection
 * - Voice characteristic emphasis over pure melody
 *
 * GLUTE Principle: Unified architecture with speech specialization
 */
@Singleton
class SpeechScoringEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsDataStore: SettingsDataStore
) {

    private val audioProcessor = AudioProcessor()
    private val garbageDetector = GarbageDetector(audioProcessor)

    // Speech-optimized parameter instances
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
        Log.d("HILT_VERIFY", "🎤 SpeechScoringEngine created - Instance: ${this.hashCode()}")

        // Load saved difficulty and apply speech-optimized presets
        engineScope.launch {
            val savedDifficulty = settingsDataStore.getDifficultyLevel.first()
            val difficulty = try {
                DifficultyLevel.valueOf(savedDifficulty)
            } catch (e: Exception) {
                DifficultyLevel.NORMAL
            }
            _currentDifficulty.value = difficulty

            // Apply speech-optimized preset parameters
            val preset = when(difficulty) {
                DifficultyLevel.EASY -> SpeechScoringModels.easyModeSpeech()
                DifficultyLevel.NORMAL -> SpeechScoringModels.normalModeSpeech()
                DifficultyLevel.HARD -> SpeechScoringModels.hardModeSpeech()
            }
            applyPreset(preset)

            _isInitialized.value = true
            Log.d("SPEECH_ENGINE", "🎤 Speech engine initialized with difficulty: ${difficulty.displayName}")
        }
    }

    /**
     * Apply speech-optimized preset parameters
     */
    private fun applyPreset(preset: Presets) {
        parameters = preset.scoring
        audioParams = preset.audio
        contentParams = preset.content
        melodicParams = preset.melodic
        musicalParams = preset.musical
        scalingParams = preset.scaling
        garbageParams = preset.garbage

        Log.d("SPEECH_ENGINE", "🎤 Applied speech preset for ${preset.difficulty.displayName}")
        Log.d("SPEECH_ENGINE", "   📊 Pitch tolerance: ${parameters.pitchTolerance}f")
        Log.d("SPEECH_ENGINE", "   📊 Content weight: ${parameters.mfccWeight}f (higher for speech)")
        Log.d("SPEECH_ENGINE", "   📊 Monotone threshold: ${garbageParams.pitchMonotoneThreshold}f")

        // 🔍 Full preset + integrity audit (speech)
        ScoringDebugLogger.logSpeechPresetApplied(
            presetName = "Speech.${preset.difficulty}",
            preset = preset,
            scoring = parameters,
            content = contentParams,
            melodic = melodicParams,
            musical = musicalParams,
            audio = audioParams,
            scaling = scalingParams,
            garbage = garbageParams
        )
    }

    /**
     * Main speech scoring method
     * Optimized for speech patterns and content accuracy
     */
    fun scoreAttempt(
        originalAudio: FloatArray,
        playerAttempt: FloatArray,
        challengeType: ChallengeType,
        difficulty: DifficultyLevel,
        sampleRate: Int = 44100
    ): ScoringResult {

        if (difficulty != _currentDifficulty.value) {
            updateDifficulty(difficulty)
        }

        Log.d("SPEECH_ENGINE", "=== SPEECH SCORING ENGINE ===")
        Log.d("SPEECH_ENGINE", "🎤 Difficulty: ${_currentDifficulty.value.displayName}")
        Log.d("SPEECH_ENGINE", "🎵 Pitch tolerance: ${parameters.pitchTolerance}f (speech-optimized)")
        Log.d("SPEECH_ENGINE", "📝 Content focus: ${parameters.mfccWeight}f MFCC weight")
        Log.d("SPEECH_ENGINE", "🗣️ Challenge type: $challengeType")

        // Silence detection
        val attemptRMS = audioProcessor.calculateRMS(playerAttempt)
        if (attemptRMS < parameters.silenceThreshold) {
            Log.d("SPEECH_ENGINE", "🔇 Silence detected: RMS ($attemptRMS) < threshold")
            return ScoringResult(
                score = 0,
                rawScore = 0f,
                metrics = SimilarityMetrics(0f, 0f),
                feedback = listOf("🎤 Please speak more clearly - we detected silence!"),
                isGarbage = false
            )
        }

        // Audio alignment
        val (alignedOriginal, alignedAttempt) = alignAudio(originalAudio, playerAttempt)
        if (alignedOriginal.isEmpty() || alignedAttempt.isEmpty()) {
            Log.d("SPEECH_ENGINE", "❌ Audio alignment failed")
            return ScoringResult(
                score = 0,
                rawScore = 0f,
                metrics = SimilarityMetrics(0f, 0f),
                feedback = listOf("Recording too short to analyze - please try again!"),
                isGarbage = false
            )
        }

        // Extract features for analysis
        val originalPitchSequence = getPitchSequence(alignedOriginal, sampleRate)
        val attemptPitchSequence = getPitchSequence(alignedAttempt, sampleRate)
        val originalMfccSequence = getMfccSequence(alignedOriginal, sampleRate)
        val attemptMfccSequence = getMfccSequence(alignedAttempt, sampleRate)

        // Speech-optimized garbage detection
        val garbageAnalysis = performSpeechGarbageDetection(
            alignedAttempt, attemptPitchSequence, attemptMfccSequence, sampleRate
        )

        if (garbageAnalysis.isGarbage) {
            Log.d("SPEECH_ENGINE", "🗑️ Speech attempt flagged as garbage")
            return ScoringResult(
                score = garbageParams.garbageScoreMax,
                rawScore = garbageParams.garbageScoreMax / 100f,
                metrics = SimilarityMetrics(0f, 0f),
                feedback = listOf("Please speak clearly with real words - that sounded like noise!"),
                isGarbage = true
            )
        }

        // Calculate speech-optimized similarity metrics
        val pitchSimilarity = calculateSpeechPitchSimilarity(originalPitchSequence, attemptPitchSequence)
        val mfccSimilarity = calculateMfccSimilarity(originalMfccSequence, attemptMfccSequence)

        Log.d("SPEECH_ENGINE", "📊 Speech similarities - Pitch: ${pitchSimilarity}, MFCC: ${mfccSimilarity}")

        val metrics = SimilarityMetrics(pitchSimilarity, mfccSimilarity)

        // Speech-focused weighted score (emphasizes content over pure melody)
        var rawScore = (pitchSimilarity * parameters.pitchWeight) + (mfccSimilarity * parameters.mfccWeight)
        Log.d("SPEECH_ENGINE", "🎯 Raw weighted score: $rawScore")

        // Speech-appropriate variance penalty (more forgiving)
        val variancePenalty = calculateSpeechVariancePenalty(originalPitchSequence, attemptPitchSequence)
        rawScore *= variancePenalty
        Log.d("SPEECH_ENGINE", "📉 After speech variance penalty (${variancePenalty}x): $rawScore")

        // Speech bonuses (consistency and confidence)
        val consistency = (1 - abs(pitchSimilarity - mfccSimilarity)) * parameters.consistencyBonus
        val confidence = min(1f, attemptRMS * scalingParams.rmsConfidenceMultiplier) * parameters.confidenceBonus
        val scoreWithBonuses = rawScore * (1 + consistency + confidence)

        Log.d("SPEECH_ENGINE", "🎁 After bonuses - Consistency: $consistency, Confidence: $confidence")
        Log.d("SPEECH_ENGINE", "📈 Score with bonuses: $scoreWithBonuses")

        // Apply humming detection (less harsh for speech)
        val hummingDetected = garbageAnalysis.filterResults["humming_detected"] == 1f
        val adjustedScore = if (hummingDetected && !garbageAnalysis.isGarbage) {
            Log.d("SPEECH_ENGINE", "🎤 Humming detected in speech - applying light penalty")
            scoreWithBonuses * 0.7f  // Less harsh than singing (0.5f)
        } else {
            scoreWithBonuses
        }

        // Final speech-optimized scaling
        val finalScore = scaleSpeechScore(adjustedScore, challengeType)

        Log.d("SPEECH_ENGINE", "🏁 FINAL SPEECH SCORE: $finalScore")
        Log.d("SPEECH_ENGINE", "========================")

        return ScoringResult(
            score = finalScore,
            rawScore = rawScore,
            metrics = metrics,
            feedback = generateSpeechFeedback(finalScore, metrics, challengeType),
            isGarbage = false
        )
    }

    /**
     * Speech-optimized pitch similarity calculation
     * More forgiving than singing, focuses on overall pattern rather than exact notes
     */
    private fun calculateSpeechPitchSimilarity(
        originalPitches: List<Float>,
        attemptPitches: List<Float>
    ): Float {
        if (originalPitches.size < 2 || attemptPitches.size < 2) return 0f

        Log.d("SPEECH_ENGINE", "🎵 Speech pitch analysis - lengths: orig=${originalPitches.size}, attempt=${attemptPitches.size}")

        val minLen = min(originalPitches.size, attemptPitches.size)
        var totalSimilarity = 0f
        var validComparisons = 0

        // Speech-optimized pitch comparison (more tolerant)
        for (i in 0 until minLen) {
            val origPitch = originalPitches[i]
            val attemptPitch = attemptPitches[i]

            if (origPitch > 0f && attemptPitch > 0f) {
                val pitchDifference = abs(origPitch - attemptPitch)

                // Speech: Use higher tolerance and more gradual decay
                val similarity = when {
                    pitchDifference <= parameters.pitchTolerance * 0.5f -> 1f  // Perfect range
                    pitchDifference <= parameters.pitchTolerance -> {
                        // Gradual decay for speech tolerance
                        val decayFactor = (parameters.pitchTolerance - pitchDifference) / (parameters.pitchTolerance * 0.5f)
                        decayFactor.coerceIn(0.3f, 1f)  // More forgiving minimum
                    }
                    else -> 0.2f  // Still give some credit for speech attempt
                }

                totalSimilarity += similarity
                validComparisons++
            } else {
                // Handle silence-to-silence as neutral for speech
                totalSimilarity += 0.5f
                validComparisons++
            }
        }

        val result = if (validComparisons > 0) totalSimilarity / validComparisons else 0f
        Log.d("SPEECH_ENGINE", "🎵 Speech pitch similarity: $result")
        return result
    }

    /**
     * Speech-appropriate variance penalty
     * Less harsh than singing - monotone speech is more acceptable
     */
    private fun calculateSpeechVariancePenalty(
        originalPitches: List<Float>,
        attemptPitches: List<Float>
    ): Float {
        val originalVariance = calculatePitchVariance(originalPitches)
        val attemptVariance = calculatePitchVariance(attemptPitches)

        Log.d("SPEECH_ENGINE", "📊 Variance analysis - Original: $originalVariance, Attempt: $attemptVariance")

        // Speech: Much more tolerant of monotone delivery
        return if (originalVariance > melodicParams.monotoneDetectionThreshold &&
            attemptVariance < melodicParams.flatSpeechThreshold) {
            val penalty = melodicParams.monotonePenalty
            Log.d("SPEECH_ENGINE", "📉 Applying speech monotone penalty: $penalty")
            penalty
        } else {
            1.0f  // No penalty for speech patterns
        }
    }

    /**
     * Calculate pitch variance
     */
    private fun calculatePitchVariance(pitches: List<Float>): Float {
        val validPitches = pitches.filter { it > 0f }
        if (validPitches.size < 2) return 0f

        val mean = validPitches.average().toFloat()
        val variance = validPitches.map { (it - mean) * (it - mean) }.average().toFloat()
        return sqrt(variance)
    }

    /**
     * Speech-optimized garbage detection
     */
    private fun performSpeechGarbageDetection(
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

        // Use speech-optimized parameters for garbage detection
        return garbageDetector.detectGarbage(
            audioFrames = audioFrames,
            pitches = pitches,
            mfccFrames = mfccFrames,
            sampleRate = sampleRate
        )
    }

    /**
     * Speech-optimized score scaling
     */
    private fun scaleSpeechScore(rawScore: Float, challengeType: ChallengeType): Int {
        var scaledScore = rawScore

        // Use challenge-appropriate thresholds
        val minThreshold = if (challengeType == ChallengeType.REVERSE) {
            parameters.reverseMinScoreThreshold
        } else {
            parameters.minScoreThreshold
        }

        val perfectThreshold = if (challengeType == ChallengeType.REVERSE) {
            parameters.reversePerfectScoreThreshold
        } else {
            parameters.perfectScoreThreshold
        }

        Log.d("SPEECH_ENGINE", "🎯 Using thresholds: min=$minThreshold, perfect=$perfectThreshold for $challengeType")

// Speech-optimized scaling curve
        val normalizedScore = (rawScore - minThreshold) / (perfectThreshold - minThreshold)

        val curveAdjustedScore = if (normalizedScore > 0) {
            normalizedScore.pow(1f / parameters.scoreCurve)
        } else {
            0f
        }

        val finalScore = (curveAdjustedScore * 100f).roundToInt().coerceIn(0, 100)
        Log.d("SPEECH_ENGINE", "📊 Speech scaling: raw=$rawScore, normalized=$normalizedScore, curved=$curveAdjustedScore, final=$finalScore")

        return finalScore
    }

    /**
     * Generate speech-appropriate feedback
     */
    private fun generateSpeechFeedback(
        score: Int,
        metrics: SimilarityMetrics,
        challengeType: ChallengeType
    ): List<String> {
        val feedback = mutableListOf<String>()

        // Speech-specific score feedback
        when {
            score >= scalingParams.incredibleFeedbackThreshold -> {
                feedback.add("🎤 Outstanding speech clarity! Your pronunciation was excellent!")
            }
            score >= scalingParams.greatJobFeedbackThreshold -> {
                feedback.add("🗣️ Great speech attempt! Clear and understandable!")
            }
            score >= scalingParams.goodEffortFeedbackThreshold -> {
                feedback.add("👍 Good effort! Your speech patterns are improving!")
            }
            else -> {
                feedback.add("🎯 Keep practicing! Focus on clear pronunciation.")
            }
        }

        // Speech-specific metric feedback
        if (metrics.pitch < 0.5f) {
            feedback.add("💡 Try to follow the rhythm and flow of the original speech.")
        }

        if (metrics.mfcc < 0.5f) {
            feedback.add("🎯 Work on matching the vocal tone and clarity.")
        }

        // Challenge type specific advice
        if (challengeType == ChallengeType.REVERSE) {
            feedback.add("🔄 Reverse speech is tricky - listen carefully to the pattern!")
        }

        return feedback
    }

    // Utility methods (similar to ScoringEngine but speech-optimized)

    private fun alignAudio(original: FloatArray, attempt: FloatArray): Pair<FloatArray, FloatArray> {
        Log.d("SPEECH_ENGINE", "🔧 Aligning speech audio...")

        // Simple energy-based alignment for speech
        val originalPeak = findSpeechStart(original)
        val attemptPeak = findSpeechStart(attempt)

        val alignedLength = min(original.size - originalPeak, attempt.size - attemptPeak)
        if (alignedLength <= 0) return Pair(FloatArray(0), FloatArray(0))

        return Pair(
            original.sliceArray(originalPeak until originalPeak + alignedLength),
            attempt.sliceArray(attemptPeak until attemptPeak + alignedLength)
        )
    }

    private fun findSpeechStart(audio: FloatArray): Int {
        val threshold = 0.01f
        for (i in audio.indices) {
            if (abs(audio[i]) > threshold) return i
        }
        return 0
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
     * Update difficulty and apply new speech preset
     */
    fun updateDifficulty(newDifficulty: DifficultyLevel) {
        _currentDifficulty.value = newDifficulty
        val preset = when(newDifficulty) {
            DifficultyLevel.EASY -> SpeechScoringModels.easyModeSpeech()
            DifficultyLevel.NORMAL -> SpeechScoringModels.normalModeSpeech()
            DifficultyLevel.HARD -> SpeechScoringModels.hardModeSpeech()
        }
        applyPreset(preset)
        Log.d("SPEECH_ENGINE", "🎤 Updated to ${newDifficulty.displayName} speech preset")
    }

    private fun getCurrentScoringParameters(): ScoringParameters {
        // Simple fallback - use default ScoringParameters with proper dtwNormalizationFactor
        return parameters
    }

}