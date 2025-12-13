package com.quokkalabs.reversey.scoring

import android.util.Log
import kotlin.math.sqrt
import kotlin.math.pow
import kotlin.math.abs

/**
 * ReVerseY Scoring Engine - EXPERIMENTAL MODELS
 *
 * Switch models by changing ACTIVE_MODEL
 */
object ReverseScoringEngine {

    private const val TAG = "ScoringEngine"
    private const val TAG_PHONEME = "PHONEME_SCORE"

    // ═══════════════════════════════════════════════════════════════
    //  🎮 CHANGE THIS TO TEST DIFFERENT MODELS
    // ═══════════════════════════════════════════════════════════════

    enum class ScoringModel {
        ADDITIVE_SQRT,      // Original: √phoneme×0.45 + √duration×0.55 (BROKEN)
        MULTIPLICATIVE,     // phoneme × duration_factor
        PHONEME_PRIMARY,    // phoneme×0.85 + duration_bonus×0.15
        GATE_MODEL,         // phoneme score, duration is pass/fail gate
        PHONEME_DOMINANT    // phoneme×0.80, duration penalty only
    }

    // 👇 SWITCH MODEL HERE 👇
    private val ACTIVE_MODEL = ScoringModel.MULTIPLICATIVE

    // ═══════════════════════════════════════════════════════════════
    //  DIFFICULTY CONFIG
    // ═══════════════════════════════════════════════════════════════

    enum class PhonemeMatch { FUZZY, EXACT, ORDERED }

    data class DifficultyConfig(
        val durationMin: Float,      // e.g., 0.5 = 50%
        val durationMax: Float,      // e.g., 1.5 = 150%
        val phonemeLeniency: PhonemeMatch,
        val label: String
    )

    private val DIFFICULTY_CONFIGS = mapOf(
        DifficultyLevel.EASY to DifficultyConfig(0.50f, 1.50f, PhonemeMatch.FUZZY, "Wide duration window, fuzzy phonemes"),
        DifficultyLevel.NORMAL to DifficultyConfig(0.66f, 1.33f, PhonemeMatch.EXACT, "Moderate duration window, exact phonemes required"),
        DifficultyLevel.HARD to DifficultyConfig(0.80f, 1.20f, PhonemeMatch.ORDERED, "Tight duration window, ordered phonemes required")
    )

    // Auto-accept thresholds
    const val AUTO_ACCEPT_HIGH = 90
    const val AUTO_ACCEPT_LOW = 15

    // ═══════════════════════════════════════════════════════════════
    //  MAIN ENTRY POINT
    // ═══════════════════════════════════════════════════════════════

    fun score(
        targetText: String,
        attemptText: String,
        targetDurationMs: Long,
        attemptDurationMs: Long,
        difficulty: DifficultyLevel = DifficultyLevel.NORMAL
    ): PhonemeScoreResult {

        val config = DIFFICULTY_CONFIGS[difficulty] ?: DIFFICULTY_CONFIGS[DifficultyLevel.NORMAL]!!

        Log.d(TAG, "Scoring [$difficulty]: target='$targetText' attempt='$attemptText'")
        Log.d(TAG, "Durations: target=${targetDurationMs}ms attempt=${attemptDurationMs}ms")
        Log.d(TAG, "Config: ${config.label}")

        // 1. Extract phonemes
        val targetPhonemes = PhonemeUtils.textToPhonemes(targetText)
        val attemptPhonemes = PhonemeUtils.textToPhonemes(attemptText)
        
        // 1b. Extract word-level phonemes for UI visualization
        val targetWordPhonemes = PhonemeUtils.textToWordPhonemes(targetText)
        val attemptWordPhonemes = PhonemeUtils.textToWordPhonemes(attemptText)

        Log.d(TAG, "Phonemes: target=$targetPhonemes")
        Log.d(TAG, "Phonemes: attempt=$attemptPhonemes")

        // 2. Calculate phoneme overlap based on leniency
        val matchDetail = calculatePhonemeScore(
            targetPhonemes, attemptPhonemes, config.phonemeLeniency
        )

        Log.d(TAG, "Match result: ${matchDetail.matchedCount}/${matchDetail.totalCount} matched")
        Log.d(TAG, "Overlap (${config.phonemeLeniency}): ${matchDetail.overlap}")

        // 3. Calculate duration score
        val durationRatio = if (targetDurationMs > 0) {
            attemptDurationMs.toFloat() / targetDurationMs.toFloat()
        } else 1f

        val durationInRange = durationRatio in config.durationMin..config.durationMax
        val durationPenalty = calculateDurationPenalty(durationRatio, config)
        val durationScore = 1f - durationPenalty

        Log.d(TAG, "Duration: ratio=$durationRatio inRange=$durationInRange penalty=$durationPenalty score=$durationScore")

        // 4. Compute final score using selected model
        val (finalScore, phonemeComponent, durationComponent) = computeScore(
            matchDetail.overlap, durationScore, durationInRange, ACTIVE_MODEL
        )

        Log.d(TAG, "Components: phoneme=$phonemeComponent duration=$durationComponent")
        Log.d(TAG, "Final score: $finalScore")

        // Logging for debugging
        val durationEmoji = if (durationInRange) "✓" else "✗"
        Log.d(TAG_PHONEME, "🎯 Score: $finalScore% | ${matchDetail.matchedCount}/${matchDetail.totalCount} phonemes | Duration $durationEmoji | $finalScore%")
        Log.d(TAG_PHONEME, "🔤 Target phonemes: ${targetPhonemes.joinToString(" ")}")
        Log.d(TAG_PHONEME, "🔤 Attempt phonemes: ${attemptPhonemes.joinToString(" ")}")
        Log.d(TAG_PHONEME, "🎚️ Difficulty: $difficulty | Leniency: ${config.phonemeLeniency}")
        Log.d(TAG_PHONEME, "🟢🔴 Matches: ${matchDetail.matches.map { if (it) "✓" else "✗" }}")

        return PhonemeScoreResult(
            score = finalScore,
            phonemeOverlap = matchDetail.overlap,
            durationRatio = durationRatio,
            durationScore = durationScore,
            durationInRange = durationInRange,
            targetPhonemes = targetPhonemes,
            attemptPhonemes = attemptPhonemes,
            matchedCount = matchDetail.matchedCount,
            totalCount = matchDetail.totalCount,
            difficulty = difficulty,
            model = ACTIVE_MODEL.name,
            phonemeLeniency = config.phonemeLeniency.name,
            shouldAutoAccept = finalScore >= AUTO_ACCEPT_HIGH,
            shouldAutoReject = finalScore <= AUTO_ACCEPT_LOW,
            phonemeMatches = matchDetail.matches,
            targetWordPhonemes = targetWordPhonemes,
            attemptWordPhonemes = attemptWordPhonemes
        )
    }

    // ═══════════════════════════════════════════════════════════════
    //  PHONEME MATCHING
    // ═══════════════════════════════════════════════════════════════

    private fun calculatePhonemeScore(
        target: List<String>,
        attempt: List<String>,
        leniency: PhonemeMatch
    ): PhonemeMatchDetail {

        if (target.isEmpty()) return PhonemeMatchDetail(0f, 0, 0, emptyList())

        return when (leniency) {
            PhonemeMatch.FUZZY -> fuzzyMatch(target, attempt)
            PhonemeMatch.EXACT -> exactMatch(target, attempt)
            PhonemeMatch.ORDERED -> orderedMatch(target, attempt)
        }
    }

    /**
     * FUZZY: Similar phonemes get partial credit
     * "AH" matches "AE" with 0.7, "AH" matches "AH" with 1.0
     */
    private fun fuzzyMatch(target: List<String>, attempt: List<String>): PhonemeMatchDetail {
        // Track which target phonemes are matched
        val targetMatched = BooleanArray(target.size) { false }
        val targetBag = target.groupingBy { it }.eachCount().toMutableMap()
        val attemptBag = attempt.groupingBy { it }.eachCount()

        var matchScore = 0f
        var matchCount = 0

        for ((phoneme, count) in attemptBag) {
            val exactMatch = minOf(targetBag[phoneme] ?: 0, count)
            matchScore += exactMatch
            matchCount += exactMatch
            targetBag[phoneme] = (targetBag[phoneme] ?: 0) - exactMatch

            // Mark matched target phonemes (first N unmatched instances)
            if (exactMatch > 0) {
                var marked = 0
                for (i in target.indices) {
                    if (!targetMatched[i] && target[i] == phoneme && marked < exactMatch) {
                        targetMatched[i] = true
                        marked++
                    }
                }
            }

            // Fuzzy: check similar phonemes
            if (exactMatch < count) {
                val remaining = count - exactMatch
                for ((similar, similarity) in getSimilarPhonemes(phoneme)) {
                    val available = targetBag[similar] ?: 0
                    if (available > 0) {
                        val fuzzyMatch = minOf(available, remaining)
                        matchScore += fuzzyMatch * similarity
                        targetBag[similar] = available - fuzzyMatch
                        
                        // Mark fuzzy-matched target phonemes
                        var marked = 0
                        for (i in target.indices) {
                            if (!targetMatched[i] && target[i] == similar && marked < fuzzyMatch) {
                                targetMatched[i] = true
                                marked++
                            }
                        }
                    }
                }
            }
        }

        val total = target.size
        return PhonemeMatchDetail(matchScore / total, matchCount, total, targetMatched.toList())
    }

    /**
     * EXACT: Bag overlap (order doesn't matter)
     */
    private fun exactMatch(target: List<String>, attempt: List<String>): PhonemeMatchDetail {
        // Track which target phonemes are matched
        val targetMatched = BooleanArray(target.size) { false }
        val targetBag = target.groupingBy { it }.eachCount().toMutableMap()
        val attemptBag = PhonemeUtils.phonemeBag(attempt)

        var intersection = 0
        for ((phoneme, count) in attemptBag) {
            val available = targetBag[phoneme] ?: 0
            val matched = minOf(count, available)
            intersection += matched
            
            // Mark matched target phonemes
            if (matched > 0) {
                var marked = 0
                for (i in target.indices) {
                    if (!targetMatched[i] && target[i] == phoneme && marked < matched) {
                        targetMatched[i] = true
                        marked++
                    }
                }
            }
        }

        val union = target.size + attempt.size - intersection
        val overlap = if (union > 0) intersection.toFloat() / union else 0f

        return PhonemeMatchDetail(overlap, intersection, target.size, targetMatched.toList())
    }

    /**
     * ORDERED: Longest common subsequence ratio
     */
    private fun orderedMatch(target: List<String>, attempt: List<String>): PhonemeMatchDetail {
        val (lcs, matchedIndices) = longestCommonSubsequenceWithIndices(target, attempt)
        val overlap = if (target.isNotEmpty()) lcs.toFloat() / target.size else 0f
        
        // Create match array from matched indices
        val targetMatched = BooleanArray(target.size) { false }
        for (i in matchedIndices) {
            targetMatched[i] = true
        }
        
        return PhonemeMatchDetail(overlap, lcs, target.size, targetMatched.toList())
    }

    private fun longestCommonSubsequence(a: List<String>, b: List<String>): Int {
        val m = a.size
        val n = b.size
        val dp = Array(m + 1) { IntArray(n + 1) }

        for (i in 1..m) {
            for (j in 1..n) {
                dp[i][j] = if (a[i-1] == b[j-1]) {
                    dp[i-1][j-1] + 1
                } else {
                    maxOf(dp[i-1][j], dp[i][j-1])
                }
            }
        }
        return dp[m][n]
    }

    /**
     * LCS with backtracking to find which target indices matched
     */
    private fun longestCommonSubsequenceWithIndices(a: List<String>, b: List<String>): Pair<Int, List<Int>> {
        val m = a.size
        val n = b.size
        if (m == 0 || n == 0) return Pair(0, emptyList())
        
        val dp = Array(m + 1) { IntArray(n + 1) }

        for (i in 1..m) {
            for (j in 1..n) {
                dp[i][j] = if (a[i-1] == b[j-1]) {
                    dp[i-1][j-1] + 1
                } else {
                    maxOf(dp[i-1][j], dp[i][j-1])
                }
            }
        }
        
        // Backtrack to find matched indices in 'a' (target)
        val matchedIndices = mutableListOf<Int>()
        var i = m
        var j = n
        while (i > 0 && j > 0) {
            when {
                a[i-1] == b[j-1] -> {
                    matchedIndices.add(i - 1)  // 0-indexed
                    i--
                    j--
                }
                dp[i-1][j] > dp[i][j-1] -> i--
                else -> j--
            }
        }
        
        return Pair(dp[m][n], matchedIndices.reversed())  // Reverse to get ascending order
    }

    /**
     * Phoneme similarity map for fuzzy matching
     */
    private fun getSimilarPhonemes(phoneme: String): List<Pair<String, Float>> {
        return SIMILAR_PHONEMES[phoneme] ?: emptyList()
    }

    private val SIMILAR_PHONEMES = mapOf(
        // Vowels
        "AH" to listOf("AE" to 0.7f, "EH" to 0.6f, "IH" to 0.5f),
        "AE" to listOf("AH" to 0.7f, "EH" to 0.7f),
        "EH" to listOf("AE" to 0.7f, "IH" to 0.6f),
        "IH" to listOf("EH" to 0.6f, "IY" to 0.7f),
        "IY" to listOf("IH" to 0.7f),
        "OW" to listOf("AO" to 0.7f, "UH" to 0.5f),
        "UW" to listOf("UH" to 0.7f),
        // Consonants
        "T" to listOf("D" to 0.7f, "TH" to 0.5f),
        "D" to listOf("T" to 0.7f, "DH" to 0.5f),
        "P" to listOf("B" to 0.7f),
        "B" to listOf("P" to 0.7f),
        "K" to listOf("G" to 0.7f),
        "G" to listOf("K" to 0.7f),
        "S" to listOf("Z" to 0.7f, "SH" to 0.5f),
        "Z" to listOf("S" to 0.7f),
        "F" to listOf("V" to 0.7f, "TH" to 0.5f),
        "V" to listOf("F" to 0.7f),
        "TH" to listOf("DH" to 0.8f, "F" to 0.5f),
        "DH" to listOf("TH" to 0.8f, "D" to 0.5f),
        "N" to listOf("M" to 0.6f, "NG" to 0.5f),
        "M" to listOf("N" to 0.6f),
        "L" to listOf("R" to 0.5f),
        "R" to listOf("L" to 0.5f)
    )

    // ═══════════════════════════════════════════════════════════════
    //  DURATION SCORING
    // ═══════════════════════════════════════════════════════════════

    private fun calculateDurationPenalty(ratio: Float, config: DifficultyConfig): Float {
        return when {
            ratio < config.durationMin -> {
                // Too short: linear penalty
                val shortfall = config.durationMin - ratio
                (shortfall / config.durationMin).coerceIn(0f, 0.5f)
            }
            ratio > config.durationMax -> {
                // Too long: linear penalty
                val excess = ratio - config.durationMax
                (excess / config.durationMax).coerceIn(0f, 0.5f)
            }
            else -> 0f // In range, no penalty
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  SCORING MODELS
    // ═══════════════════════════════════════════════════════════════

    private fun computeScore(
        phonemeOverlap: Float,
        durationScore: Float,
        durationInRange: Boolean,
        model: ScoringModel
    ): Triple<Int, Float, Float> {

        return when (model) {

            // ─────────────────────────────────────────────────────────
            // ORIGINAL (BROKEN): Duration contributes positively
            // Problem: "bombastic" gets 67% because duration is good
            // ─────────────────────────────────────────────────────────
            ScoringModel.ADDITIVE_SQRT -> {
                val p = sqrt(phonemeOverlap) * 0.45f
                val d = sqrt(durationScore) * 0.55f
                val score = ((p + d) * 100).toInt().coerceIn(0, 100)
                Triple(score, p, d)
            }

            // ─────────────────────────────────────────────────────────
            // MULTIPLICATIVE: phoneme × duration_factor
            // Good phonemes required. Duration is multiplier (0.5-1.0)
            // ─────────────────────────────────────────────────────────
            ScoringModel.MULTIPLICATIVE -> {
                val phonemeScore = phonemeOverlap * 100f
                val durationMultiplier = 0.5f + (durationScore * 0.5f) // 0.5 to 1.0
                val score = (phonemeScore * durationMultiplier).toInt().coerceIn(0, 100)
                Triple(score, phonemeScore / 100f, durationMultiplier)
            }

            // ─────────────────────────────────────────────────────────
            // PHONEME PRIMARY: 85% phoneme, 15% duration bonus
            // Duration can only ADD, never carry the score
            // ─────────────────────────────────────────────────────────
            ScoringModel.PHONEME_PRIMARY -> {
                val phonemeScore = sqrt(phonemeOverlap) * 0.85f
                val durationBonus = if (durationInRange) 0.15f else 0f
                val score = ((phonemeScore + durationBonus) * 100).toInt().coerceIn(0, 100)
                Triple(score, phonemeScore, durationBonus)
            }

            // ─────────────────────────────────────────────────────────
            // GATE MODEL: Full phoneme score if duration OK, else halved
            // ─────────────────────────────────────────────────────────
            ScoringModel.GATE_MODEL -> {
                val phonemeScore = sqrt(phonemeOverlap) * 100f
                val gatedScore = if (durationInRange) {
                    phonemeScore
                } else {
                    phonemeScore * 0.5f // Halve score if duration bad
                }
                val score = gatedScore.toInt().coerceIn(0, 100)
                Triple(score, phonemeScore / 100f, if (durationInRange) 1f else 0.5f)
            }

            // ─────────────────────────────────────────────────────────
            // PHONEME DOMINANT: 80% weight, duration penalty only
            // Duration can HURT but not HELP
            // ─────────────────────────────────────────────────────────
            ScoringModel.PHONEME_DOMINANT -> {
                val baseScore = sqrt(phonemeOverlap) * 100f
                val penalty = if (!durationInRange) {
                    baseScore * 0.2f * (1f - durationScore) // Up to 20% penalty
                } else 0f
                val score = (baseScore - penalty).toInt().coerceIn(0, 100)
                Triple(score, baseScore / 100f, -penalty / 100f)
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  UTILITY
    // ═══════════════════════════════════════════════════════════════

    fun scoreTextOnly(targetText: String, attemptText: String): Int {
        val targetPhonemes = PhonemeUtils.textToPhonemes(targetText)
        val attemptPhonemes = PhonemeUtils.textToPhonemes(attemptText)

        val overlap = PhonemeUtils.bagOverlap(
            PhonemeUtils.phonemeBag(targetPhonemes),
            PhonemeUtils.phonemeBag(attemptPhonemes)
        )

        return (sqrt(overlap) * 100).toInt().coerceIn(0, 100)
    }

    fun getActiveModel(): String = ACTIVE_MODEL.name
}

/**
 * Intermediate result from phoneme matching functions
 */
data class PhonemeMatchDetail(
    val overlap: Float,
    val matchedCount: Int,
    val totalCount: Int,
    val matches: List<Boolean>  // true for each target phoneme that was matched
)

/**
 * Detailed scoring result
 */
data class PhonemeScoreResult(
    val score: Int,
    val phonemeOverlap: Float,
    val durationRatio: Float,
    val durationScore: Float,
    val durationInRange: Boolean,
    val targetPhonemes: List<String>,
    val attemptPhonemes: List<String>,
    val matchedCount: Int,
    val totalCount: Int,
    val difficulty: DifficultyLevel,
    val model: String,
    val phonemeLeniency: String,  // "FUZZY", "EXACT", or "ORDERED"
    val shouldAutoAccept: Boolean,
    val shouldAutoReject: Boolean,
    val phonemeMatches: List<Boolean> = emptyList(),  // Per-target-phoneme match status for UI grid
    val targetWordPhonemes: List<WordPhonemes> = emptyList(),  // Word-grouped target phonemes
    val attemptWordPhonemes: List<WordPhonemes> = emptyList()  // Word-grouped attempt phonemes
) {
    /** Short summary for logging */
    fun shortSummary(): String = "$score% | $matchedCount/$totalCount phonemes | Duration ${if (durationInRange) "✓" else "✗"}"

    fun breakdown(): String {
        return buildString {
            appendLine("Score: $score% (Model: $model)")
            appendLine("Phonemes: $matchedCount/$totalCount (${(phonemeOverlap * 100).toInt()}%)")
            appendLine("Duration: ${String.format("%.2f", durationRatio)}x ${if (durationInRange) "✓" else "✗"}")
            appendLine("Difficulty: $difficulty")
            if (shouldAutoAccept) appendLine("→ Auto-accept!")
            if (shouldAutoReject) appendLine("→ Auto-reject!")
        }
    }
}