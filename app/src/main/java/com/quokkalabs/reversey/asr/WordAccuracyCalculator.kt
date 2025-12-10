package com.quokkalabs.reversey.asr

import kotlin.math.max

/**
 * 🗣️ PHASE 3: Calculate word-level accuracy using Levenshtein distance.
 * 
 * Compares reference transcription against attempt transcription.
 * Returns 0.0 to 1.0 (percentage match).
 * 
 * GLUTE Principle: Pure function, no dependencies, fully testable
 */
object WordAccuracyCalculator {

    /**
     * Compare two transcriptions and return similarity (0.0 to 1.0)
     * 
     * @param reference What the challenge phrase IS (from recording)
     * @param attempt What the player SAID (from ASR)
     * @return Similarity score 0.0 (completely different) to 1.0 (identical)
     */
    fun calculate(reference: String, attempt: String): Float {
        val refWords = normalizeText(reference)
        val attWords = normalizeText(attempt)
        
        // Edge cases
        if (refWords.isEmpty() && attWords.isEmpty()) return 1f
        if (refWords.isEmpty() || attWords.isEmpty()) return 0f
        
        val distance = levenshteinDistance(refWords, attWords)
        val maxLen = max(refWords.size, attWords.size)
        
        return (1f - (distance.toFloat() / maxLen)).coerceIn(0f, 1f)
    }

    /**
     * Calculate percentage of reference words found in attempt
     * More lenient than Levenshtein - good for partial matches
     */
    fun calculateWordOverlap(reference: String, attempt: String): Float {
        val refWords = normalizeText(reference).toSet()
        val attWords = normalizeText(attempt).toSet()
        
        if (refWords.isEmpty()) return if (attWords.isEmpty()) 1f else 0f
        
        val matches = refWords.intersect(attWords).size
        return matches.toFloat() / refWords.size
    }

    /**
     * Get detailed comparison for debugging/display
     */
    fun getDetailedComparison(reference: String, attempt: String): ComparisonDetails {
        val refWords = normalizeText(reference)
        val attWords = normalizeText(attempt)
        
        val levenshteinScore = calculate(reference, attempt)
        val overlapScore = calculateWordOverlap(reference, attempt)
        
        // Find matching and missing words
        val refSet = refWords.toSet()
        val attSet = attWords.toSet()
        val matching = refSet.intersect(attSet)
        val missing = refSet - attSet
        val extra = attSet - refSet
        
        return ComparisonDetails(
            referenceWords = refWords,
            attemptWords = attWords,
            levenshteinScore = levenshteinScore,
            overlapScore = overlapScore,
            matchingWords = matching.toList(),
            missingWords = missing.toList(),
            extraWords = extra.toList()
        )
    }

    /**
     * Normalize text for comparison:
     * - Lowercase
     * - Remove punctuation
     * - Split to words
     * - Filter blanks
     */
    private fun normalizeText(text: String): List<String> {
        return text
            .lowercase()
            .replace(Regex("[^a-z0-9\\s]"), "")  // Remove punctuation
            .split(Regex("\\s+"))                 // Split on whitespace
            .filter { it.isNotBlank() }           // Remove empty entries
    }

    /**
     * Levenshtein distance between word lists
     * (Edit distance: minimum insertions, deletions, substitutions)
     */
    private fun levenshteinDistance(s1: List<String>, s2: List<String>): Int {
        val m = s1.size
        val n = s2.size
        
        // DP table
        val dp = Array(m + 1) { IntArray(n + 1) }

        // Base cases: transforming empty string
        for (i in 0..m) dp[i][0] = i
        for (j in 0..n) dp[0][j] = j

        // Fill table
        for (i in 1..m) {
            for (j in 1..n) {
                dp[i][j] = if (s1[i - 1] == s2[j - 1]) {
                    dp[i - 1][j - 1]  // No operation needed
                } else {
                    minOf(
                        dp[i - 1][j] + 1,      // Deletion
                        dp[i][j - 1] + 1,      // Insertion
                        dp[i - 1][j - 1] + 1   // Substitution
                    )
                }
            }
        }
        
        return dp[m][n]
    }
}

/**
 * Detailed comparison results for debugging/UI
 */
data class ComparisonDetails(
    val referenceWords: List<String>,
    val attemptWords: List<String>,
    val levenshteinScore: Float,   // 0.0-1.0 based on edit distance
    val overlapScore: Float,        // 0.0-1.0 based on word overlap
    val matchingWords: List<String>,
    val missingWords: List<String>,
    val extraWords: List<String>
) {
    /**
     * Combined score (weighted average)
     * Levenshtein is stricter, overlap is more lenient
     */
    val combinedScore: Float
        get() = (levenshteinScore * 0.7f) + (overlapScore * 0.3f)
    
    /**
     * Human-readable summary
     */
    fun toSummary(): String {
        return buildString {
            append("Match: ${(levenshteinScore * 100).toInt()}%")
            if (missingWords.isNotEmpty()) {
                append(" | Missing: ${missingWords.take(3).joinToString(", ")}")
                if (missingWords.size > 3) append("...")
            }
            if (extraWords.isNotEmpty()) {
                append(" | Extra: ${extraWords.take(3).joinToString(", ")}")
                if (extraWords.size > 3) append("...")
            }
        }
    }
}
