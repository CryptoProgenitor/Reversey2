package com.quokkalabs.reversey.scoring

import android.content.Context
import android.util.Log

/**
 * Phoneme utilities using CMU Pronouncing Dictionary
 * Converts words to phonemes for bag-of-phonemes scoring
 */
object PhonemeUtils {
    
    private const val TAG = "PhonemeUtils"
    private const val CMU_DICT_ASSET = "cmudict.txt"
    
    // Word -> Phonemes map (e.g., "HELLO" -> ["HH", "AH", "L", "OW"])
    private val dictionary = mutableMapOf<String, List<String>>()
    private var isLoaded = false
    
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
     * Get phoneme bag (multiset) for overlap calculation
     * Returns map of phoneme -> count
     */
    fun phonemeBag(phonemes: List<String>): Map<String, Int> {
        return phonemes.groupingBy { it }.eachCount()
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
     * Quick check if dictionary is loaded
     */
    fun isReady(): Boolean = isLoaded
    
    /**
     * Get dictionary size (for debugging)
     */
    fun dictionarySize(): Int = dictionary.size
}
