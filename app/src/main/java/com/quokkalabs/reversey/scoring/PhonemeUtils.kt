package com.quokkalabs.reversey.scoring

import android.content.Context
import android.util.Log

/**
 * Phoneme utilities using CMU Pronouncing Dictionary
 * Converts words to phonemes for bag-of-phonemes scoring
 *
 * Updated Dec 2025: Added fuzzy matching and alignment for difficulty modes
 */
object PhonemeUtils {

    private const val TAG = "PhonemeUtils"
    private const val CMU_DICT_ASSET = "cmudict.txt"

    // Word -> Phonemes map (e.g., "HELLO" -> ["HH", "AH", "L", "OW"])
    private val dictionary = mutableMapOf<String, List<String>>()
    private var isLoaded = false

    /**
     * Similar phoneme groups for FUZZY matching (Easy mode)
     * Phonemes in the same group are considered equivalent
     */
    private val similarPhonemeGroups = listOf(
        // Voiced/unvoiced pairs
        setOf("P", "B"),        // bilabial stops
        setOf("T", "D"),        // alveolar stops
        setOf("K", "G"),        // velar stops
        setOf("F", "V"),        // labiodental fricatives
        setOf("S", "Z"),        // alveolar fricatives
        setOf("SH", "ZH"),      // post-alveolar fricatives
        setOf("CH", "JH"),      // affricates
        setOf("TH", "DH"),      // dental fricatives

        // Similar vowels (reduced precision for Easy mode)
        setOf("IY", "IH"),      // high front vowels
        setOf("EY", "EH"),      // mid front vowels
        setOf("AE", "EH"),      // low-mid front vowels
        setOf("AA", "AO"),      // low back vowels
        setOf("OW", "AO"),      // mid-low back vowels
        setOf("UW", "UH"),      // high back vowels
        setOf("AH", "AX"),      // schwa variants
        setOf("ER", "AXR"),     // r-colored schwa

        // Nasals (similar place of articulation)
        setOf("M", "N"),        // labial/alveolar nasals

        // Liquids
        setOf("L", "R"),        // liquids (often confused)
    )

    // Build lookup: phoneme -> canonical phoneme (first in its group alphabetically)
    // CRITICAL FIX: Use sorted().first() for deterministic ordering
    // Previous implementation used Set.first() which has undefined ordering,
    // causing scores to vary between app restarts
    private val fuzzyPhonemeMap: Map<String, String> by lazy {
        val map = mutableMapOf<String, String>()
        for (group in similarPhonemeGroups) {
            val canonical = group.sorted().first()  // Deterministic: alphabetically first
            for (phoneme in group) {
                map[phoneme] = canonical
            }
        }
        map
    }

    /**
     * Load CMU dictionary from assets
     * Call once at app startup
     */
    fun initialize(context: Context): Boolean {
        if (isLoaded) return true

        return try {
            val startTime = System.currentTimeMillis()

            context.assets.open(CMU_DICT_ASSET).bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    parseDictLine(line)
                }
            }

            isLoaded = true
            val elapsed = System.currentTimeMillis() - startTime
            Log.d(TAG, "Loaded ${dictionary.size} words in ${elapsed}ms")
            true

        } catch (e: Exception) {
            Log.e(TAG, "Failed to load CMU dictionary", e)
            false
        }
    }

    private fun parseDictLine(line: String) {
        // Skip comments and empty lines
        if (line.isBlank() || line.startsWith(";;;")) return

        // Format: WORD  P1 P2 P3 (two spaces between word and phonemes)
        // Or: WORD(1)  P1 P2 P3 (alternate pronunciations)
        val parts = line.split(" ", limit = 2)
        if (parts.size != 2) return

        // Remove variant number like HELLO(2)
        val word = parts[0].replace(Regex("\\(\\d+\\)$"), "").uppercase()

        // Strip stress markers (0,1,2) from vowels: AH0 -> AH
        val phonemes = parts[1].trim()
            .split(" ")
            .map { it.replace(Regex("[012]$"), "") }

        // Keep first pronunciation only
        if (!dictionary.containsKey(word)) {
            dictionary[word] = phonemes
        }
    }

    /**
     * Convert a sentence to phonemes
     * Returns flat list of all phonemes
     */
    fun textToPhonemes(text: String): List<String> {
        val words = text.uppercase()
            .replace(Regex("[^A-Z' ]"), "") // Keep letters, apostrophes, spaces
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }

        val phonemes = mutableListOf<String>()

        for (word in words) {
            val wordPhonemes = dictionary[word]
            if (wordPhonemes != null) {
                phonemes.addAll(wordPhonemes)
            } else {
                // Fallback: use letters as pseudo-phonemes
                Log.w(TAG, "Word not in dictionary: $word")
                phonemes.addAll(word.map { "?$it" })
            }
        }

        return phonemes
    }

    /**
     * Convert a sentence to phonemes WITH word boundaries preserved
     * Returns list of WordPhonemes for UI visualization
     */
    fun textToWordPhonemes(text: String): List<WordPhonemes> {
        val words = text.uppercase()
            .replace(Regex("[^A-Z' ]"), "")
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }

        return words.map { word ->
            val phonemes = dictionary[word]
            if (phonemes != null) {
                WordPhonemes(word.lowercase(), phonemes)
            } else {
                Log.w(TAG, "Word not in dictionary: $word")
                WordPhonemes(word.lowercase(), word.map { "?$it" })
            }
        }
    }

    /**
     * Get phoneme bag (multiset) for overlap calculation
     * Returns map of phoneme -> count
     */
    fun phonemeBag(phonemes: List<String>): Map<String, Int> {
        return phonemes.groupingBy { it }.eachCount()
    }

    /**
     * Get FUZZY phoneme bag - maps similar phonemes to canonical form
     * Used for Easy mode scoring
     */
    fun fuzzyPhonemeBag(phonemes: List<String>): Map<String, Int> {
        return phonemes
            .map { fuzzyPhonemeMap[it] ?: it }  // Map to canonical or keep original
            .groupingBy { it }
            .eachCount()
    }

    /**
     * Calculate bag-of-phonemes overlap (Jaccard-style)
     * Returns 0.0 to 1.0
     */
    fun bagOverlap(bag1: Map<String, Int>, bag2: Map<String, Int>): Float {
        if (bag1.isEmpty() && bag2.isEmpty()) return 1f
        if (bag1.isEmpty() || bag2.isEmpty()) return 0f

        val allKeys = bag1.keys + bag2.keys

        var intersection = 0
        var union = 0

        for (key in allKeys) {
            val count1 = bag1[key] ?: 0
            val count2 = bag2[key] ?: 0
            intersection += minOf(count1, count2)
            union += maxOf(count1, count2)
        }

        return if (union > 0) intersection.toFloat() / union else 0f
    }

    /**
     * Match phonemes with alignment for visualization
     * Returns list of booleans indicating which TARGET phonemes were matched
     *
     * @param targetPhonemes The expected phonemes (from reference)
     * @param attemptPhonemes The attempted phonemes (from user)
     * @param leniency How strictly to match
     * @return Pair of (match list for target, overlap score)
     */
    fun matchPhonemesWithAlignment(
        targetPhonemes: List<String>,
        attemptPhonemes: List<String>,
        leniency: PhonemeLeniency
    ): PhonemeMatchResult {

        if (targetPhonemes.isEmpty()) {
            return PhonemeMatchResult(
                targetMatches = emptyList(),
                attemptMatches = emptyList(),
                matchedCount = 0,
                totalTarget = 0,
                overlapScore = if (attemptPhonemes.isEmpty()) 1f else 0f
            )
        }

        return when (leniency) {
            PhonemeLeniency.FUZZY -> matchFuzzy(targetPhonemes, attemptPhonemes)
            PhonemeLeniency.EXACT -> matchExact(targetPhonemes, attemptPhonemes)
            PhonemeLeniency.SEQUENCE -> matchSequence(targetPhonemes, attemptPhonemes)
        }
    }

    /**
     * FUZZY matching: similar phonemes count, order doesn't matter
     * Uses bag-of-phonemes with fuzzy equivalence
     */
    private fun matchFuzzy(
        targetPhonemes: List<String>,
        attemptPhonemes: List<String>
    ): PhonemeMatchResult {
        // Convert both to fuzzy (canonical) form
        val targetFuzzy = targetPhonemes.map { fuzzyPhonemeMap[it] ?: it }
        val attemptFuzzy = attemptPhonemes.map { fuzzyPhonemeMap[it] ?: it }

        // Build attempt bag for consumption
        val attemptBag = attemptFuzzy.groupingBy { it }.eachCount().toMutableMap()

        // Match each target phoneme against attempt bag
        val targetMatches = targetFuzzy.map { phoneme ->
            val available = attemptBag[phoneme] ?: 0
            if (available > 0) {
                attemptBag[phoneme] = available - 1
                true
            } else {
                false
            }
        }

        // For attempt matches, rebuild which were consumed
        val targetBag = targetFuzzy.groupingBy { it }.eachCount().toMutableMap()
        val attemptMatches = attemptFuzzy.map { phoneme ->
            val available = targetBag[phoneme] ?: 0
            if (available > 0) {
                targetBag[phoneme] = available - 1
                true
            } else {
                false
            }
        }

        val matchedCount = targetMatches.count { it }
        val overlapScore = bagOverlap(
            fuzzyPhonemeBag(targetPhonemes),
            fuzzyPhonemeBag(attemptPhonemes)
        )

        return PhonemeMatchResult(
            targetMatches = targetMatches,
            attemptMatches = attemptMatches,
            matchedCount = matchedCount,
            totalTarget = targetPhonemes.size,
            overlapScore = overlapScore
        )
    }

    /**
     * EXACT matching: exact phonemes required, order doesn't matter
     * Uses bag-of-phonemes with exact equality
     */
    private fun matchExact(
        targetPhonemes: List<String>,
        attemptPhonemes: List<String>
    ): PhonemeMatchResult {
        // Build attempt bag for consumption
        val attemptBag = attemptPhonemes.groupingBy { it }.eachCount().toMutableMap()

        // Match each target phoneme against attempt bag
        val targetMatches = targetPhonemes.map { phoneme ->
            val available = attemptBag[phoneme] ?: 0
            if (available > 0) {
                attemptBag[phoneme] = available - 1
                true
            } else {
                false
            }
        }

        // For attempt matches
        val targetBag = targetPhonemes.groupingBy { it }.eachCount().toMutableMap()
        val attemptMatches = attemptPhonemes.map { phoneme ->
            val available = targetBag[phoneme] ?: 0
            if (available > 0) {
                targetBag[phoneme] = available - 1
                true
            } else {
                false
            }
        }

        val matchedCount = targetMatches.count { it }
        val overlapScore = bagOverlap(
            phonemeBag(targetPhonemes),
            phonemeBag(attemptPhonemes)
        )

        return PhonemeMatchResult(
            targetMatches = targetMatches,
            attemptMatches = attemptMatches,
            matchedCount = matchedCount,
            totalTarget = targetPhonemes.size,
            overlapScore = overlapScore
        )
    }

    /**
     * SEQUENCE matching: exact phonemes in order (using LCS)
     * Strictest mode - phonemes must appear in correct sequence
     */
    private fun matchSequence(
        targetPhonemes: List<String>,
        attemptPhonemes: List<String>
    ): PhonemeMatchResult {
        // Use Longest Common Subsequence to find matching sequence
        val n = targetPhonemes.size
        val m = attemptPhonemes.size

        // DP table for LCS length
        val dp = Array(n + 1) { IntArray(m + 1) }

        for (i in 1..n) {
            for (j in 1..m) {
                dp[i][j] = if (targetPhonemes[i - 1] == attemptPhonemes[j - 1]) {
                    dp[i - 1][j - 1] + 1
                } else {
                    maxOf(dp[i - 1][j], dp[i][j - 1])
                }
            }
        }

        // Backtrack to find which phonemes matched
        val targetMatches = MutableList(n) { false }
        val attemptMatches = MutableList(m) { false }

        var i = n
        var j = m
        while (i > 0 && j > 0) {
            when {
                targetPhonemes[i - 1] == attemptPhonemes[j - 1] -> {
                    targetMatches[i - 1] = true
                    attemptMatches[j - 1] = true
                    i--
                    j--
                }
                dp[i - 1][j] > dp[i][j - 1] -> i--
                else -> j--
            }
        }

        val matchedCount = targetMatches.count { it }
        // For sequence mode: LCS length / max(target, attempt) length
        // Using max() is intentional anti-gaming protection:
        // - Prevents users from gaming by saying target + extra garbage words
        // - Example: "hello" (5 phonemes) vs "hello blah blah blah" (20 phonemes)
        //   LCS=5, score = 5/20 = 25% (penalizes verbosity)
        val overlapScore = if (maxOf(n, m) > 0) {
            dp[n][m].toFloat() / maxOf(n, m)
        } else 1f

        return PhonemeMatchResult(
            targetMatches = targetMatches,
            attemptMatches = attemptMatches,
            matchedCount = matchedCount,
            totalTarget = targetPhonemes.size,
            overlapScore = overlapScore
        )
    }

    /**
     * Quick check if dictionary is loaded
     */
    fun isReady(): Boolean = isLoaded

    /**
     * Get dictionary size (for debugging)
     */
    fun dictionarySize(): Int = dictionary.size
}

/**
 * Result of phoneme matching with alignment info
 * Used for visualization in Phase 4 UI
 */
data class PhonemeMatchResult(
    val targetMatches: List<Boolean>,   // Which target phonemes were matched
    val attemptMatches: List<Boolean>,  // Which attempt phonemes were matched
    val matchedCount: Int,              // Number of target phonemes matched
    val totalTarget: Int,               // Total target phonemes
    val overlapScore: Float             // 0-1 overlap score
) {
    val matchRatio: Float
        get() = if (totalTarget > 0) matchedCount.toFloat() / totalTarget else 1f
}

/**
 * Word with its phonemes for UI visualization
 */
data class WordPhonemes(
    val word: String,
    val phonemes: List<String>
)