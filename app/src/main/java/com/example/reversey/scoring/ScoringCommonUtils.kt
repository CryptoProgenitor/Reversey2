package com.example.reversey.scoring

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * SCORING COMMON UTILS
 *
 * Shared DSP/math utilities used by:
 *   ✔ SpeechScoringEngine
 *   ✔ SingingScoringEngine
 *
 * Contains:
 *   - Clamp/lerp/smoothing
 *   - RMS / energy similarity
 *   - Cosine similarity
 *   - Normalisation helpers
 *
 * Contains NO scoring logic and NO presets.
 */

object ScoringCommonUtils {

    // ------------------------------------------------------------
    // General Math Utilities
    // ------------------------------------------------------------

    fun clamp(value: Float, minValue: Float, maxValue: Float): Float =
        max(minValue, min(value, maxValue))

    fun clampInt(value: Int, minValue: Int, maxValue: Int): Int =
        max(minValue, min(value, maxValue))

    fun lerp(a: Float, b: Float, t: Float): Float =
        a + (b - a) * t

    fun smoothStep(value: Float, e0: Float, e1: Float): Float {
        val t = clamp((value - e0) / (e1 - e0), 0f, 1f)
        return t * t * (3f - 2f * t)
    }

    // ------------------------------------------------------------
    // Audio: Normalisation, RMS, and Energy Similarity
    // ------------------------------------------------------------

    fun normalise(samples: FloatArray): FloatArray {
        val peak = samples.maxOfOrNull { abs(it) } ?: return samples
        if (peak == 0f) return samples
        return FloatArray(samples.size) { i -> samples[i] / peak }
    }

    fun rms(samples: FloatArray): Float {
        if (samples.isEmpty()) return 0f
        val sumSq = samples.fold(0f) { acc, x -> acc + x * x }
        return sqrt(sumSq / samples.size)
    }

    fun energySimilarity(a: FloatArray, b: FloatArray): Float {
        if (a.size != b.size) return 0f
        val ea = rms(a)
        val eb = rms(b)
        if (ea == 0f || eb == 0f) return 0f
        return 1f - abs(ea - eb) / max(ea, eb)
    }

    // ------------------------------------------------------------
    // MFCC / Vector Similarity
    // ------------------------------------------------------------

    fun cosineSimilarity(a: FloatArray, b: FloatArray): Float {
        if (a.size != b.size) return 0f

        var dot = 0f
        var na = 0f
        var nb = 0f

        for (i in a.indices) {
            dot += a[i] * b[i]
            na += a[i] * a[i]
            nb += b[i] * b[i]
        }

        if (na == 0f || nb == 0f) return 0f
        return dot / (sqrt(na) * sqrt(nb))
    }

    // ------------------------------------------------------------
    // Pitch Helpers
    // ------------------------------------------------------------

    fun normalisePitch(diffInCents: Float, maxCents: Float): Float {
        if (diffInCents.isNaN()) return 0f
        return clamp(1f - (abs(diffInCents) / maxCents), 0f, 1f)
    }
}
