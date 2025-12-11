package com.quokkalabs.reversey.scoring

import android.util.Log
import kotlin.math.sqrt
import kotlin.math.min

/**
 * ReVerseY Scoring Engine
 * 
 * Uses validated formula:
 * score = ((√phonemeOverlap × 0.45) + (√durationRatio × 0.55)) × 100
 * 
 * Test results (n=8): r=0.76 correlation with human judgment
 */
object ReverseScoringEngine {
    
    private const val TAG = "ScoringEngine"
    
    // Validated weights from testing
    private const val PHONEME_WEIGHT = 0.45f
    private const val DURATION_WEIGHT = 0.55f
    
    // Apply sqrt to soften extremes
    private const val PHONEME_EXPONENT = 0.5f
    private const val DURATION_EXPONENT = 0.5f
    
    // Auto-accept thresholds
    const val AUTO_ACCEPT_HIGH = 90
    const val AUTO_ACCEPT_LOW = 15
    
    // Duration bounds
    private const val MIN_DURATION_RATIO = 0.3f
    private const val MAX_DURATION_RATIO = 2.0f
    
    /**
     * Main scoring function
     * 
     * @param targetText Original phrase (e.g., "happy birthday")
     * @param attemptText ASR transcription of reversed attempt
     * @param targetDurationMs Duration of original recording
     * @param attemptDurationMs Duration of attempt recording
     * @return ScoringResult with score and breakdown
     */
    fun score(
        targetText: String,
        attemptText: String,
        targetDurationMs: Long,
        attemptDurationMs: Long
    ): PhonemeScoreResult {
        
        Log.d(TAG, "Scoring: target='$targetText' attempt='$attemptText'")
        Log.d(TAG, "Durations: target=${targetDurationMs}ms attempt=${attemptDurationMs}ms")
        
        // 1. Phoneme overlap
        val targetPhonemes = PhonemeUtils.textToPhonemes(targetText)
        val attemptPhonemes = PhonemeUtils.textToPhonemes(attemptText)
        
        val targetBag = PhonemeUtils.phonemeBag(targetPhonemes)
        val attemptBag = PhonemeUtils.phonemeBag(attemptPhonemes)
        
        val phonemeOverlap = PhonemeUtils.bagOverlap(targetBag, attemptBag)
        
        Log.d(TAG, "Phonemes: target=$targetPhonemes")
        Log.d(TAG, "Phonemes: attempt=$attemptPhonemes")
        Log.d(TAG, "Overlap: $phonemeOverlap")
        
        // 2. Duration ratio (capped)
        val rawDurationRatio = if (targetDurationMs > 0) {
            attemptDurationMs.toFloat() / targetDurationMs.toFloat()
        } else 1f
        
        // Penalize if too short or too long
        val durationScore = when {
            rawDurationRatio < MIN_DURATION_RATIO -> rawDurationRatio / MIN_DURATION_RATIO * 0.5f
            rawDurationRatio > MAX_DURATION_RATIO -> 1f - (rawDurationRatio - MAX_DURATION_RATIO) * 0.2f
            rawDurationRatio > 1f -> 1f - (rawDurationRatio - 1f) * 0.1f // Slight penalty for longer
            else -> rawDurationRatio
        }.coerceIn(0f, 1f)
        
        Log.d(TAG, "Duration: ratio=$rawDurationRatio score=$durationScore")
        
        // 3. Combined score with sqrt softening
        val phonemeComponent = sqrt(phonemeOverlap) * PHONEME_WEIGHT
        val durationComponent = sqrt(durationScore) * DURATION_WEIGHT
        
        val rawScore = (phonemeComponent + durationComponent) * 100f
        val finalScore = rawScore.toInt().coerceIn(0, 100)
        
        Log.d(TAG, "Components: phoneme=$phonemeComponent duration=$durationComponent")
        Log.d(TAG, "Final score: $finalScore")
        
        return PhonemeScoreResult(
            score = finalScore,
            phonemeOverlap = phonemeOverlap,
            durationRatio = rawDurationRatio,
            durationScore = durationScore,
            targetPhonemes = targetPhonemes,
            attemptPhonemes = attemptPhonemes,
            shouldAutoAccept = finalScore >= AUTO_ACCEPT_HIGH,
            shouldAutoReject = finalScore <= AUTO_ACCEPT_LOW
        )
    }
    
    /**
     * Quick score using just text (no duration)
     * Uses phoneme overlap only, scaled to 100
     */
    fun scoreTextOnly(targetText: String, attemptText: String): Int {
        val targetPhonemes = PhonemeUtils.textToPhonemes(targetText)
        val attemptPhonemes = PhonemeUtils.textToPhonemes(attemptText)
        
        val overlap = PhonemeUtils.bagOverlap(
            PhonemeUtils.phonemeBag(targetPhonemes),
            PhonemeUtils.phonemeBag(attemptPhonemes)
        )
        
        return (sqrt(overlap) * 100).toInt().coerceIn(0, 100)
    }
}

/**
 * Detailed scoring result
 */
data class PhonemeScoreResult(
    val score: Int,                    // 0-100 final score
    val phonemeOverlap: Float,         // 0-1 raw phoneme overlap
    val durationRatio: Float,          // Raw duration ratio
    val durationScore: Float,          // 0-1 duration component
    val targetPhonemes: List<String>,  // For debugging
    val attemptPhonemes: List<String>, // For debugging
    val shouldAutoAccept: Boolean,     // Score >= 90
    val shouldAutoReject: Boolean      // Score <= 15
) {
    /**
     * Human-readable breakdown
     */
    fun breakdown(): String {
        return buildString {
            appendLine("Score: $score%")
            appendLine("Phoneme overlap: ${(phonemeOverlap * 100).toInt()}%")
            appendLine("Duration ratio: ${String.format("%.2f", durationRatio)}x")
            if (shouldAutoAccept) appendLine("→ Auto-accept!")
            if (shouldAutoReject) appendLine("→ Auto-reject!")
        }
    }
}
