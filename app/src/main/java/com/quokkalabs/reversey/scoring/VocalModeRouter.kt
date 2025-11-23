package com.quokkalabs.reversey.scoring

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

/**
 * VocalModeRouter - PURE ROUTING SWITCH
 *
 * Takes classified vocal mode and routes to appropriate engine type.
 * NO AUDIO PROCESSING - just pure routing logic.
 *
 * GLUTE Compliant: Single point of routing logic, no scattered conditionals
 */
@Singleton
class VocalModeRouter @Inject constructor() {

    /**
     * Determine appropriate scoring engine based on vocal mode analysis
     *
     * @param vocalAnalysis Already classified vocal mode result
     * @return Routing decision with engine selection
     */
    fun getRoutingDecision(
        vocalAnalysis: VocalAnalysis
    ): VocalModeRoutingDecision {

        Log.d("VocalModeRouter", "=== VOCAL MODE ROUTING ===")
        Log.d("VocalModeRouter", "Routing for: mode=${vocalAnalysis.mode}, confidence=${vocalAnalysis.confidence}")

        try {
            // Pure routing logic - select engine based on classification
            val engineType = selectScoringEngine(vocalAnalysis.mode)

            Log.d("VocalModeRouter", "Engine selection: ${engineType} for ${vocalAnalysis.mode}")
            Log.d("VocalModeRouter", "========================")

            return VocalModeRoutingDecision(
                vocalAnalysis = vocalAnalysis,
                selectedEngine = engineType,
                routedMode = vocalAnalysis.mode
            )

        } catch (e: Exception) {
            Log.e("VocalModeRouter", "Error in vocal mode routing: ${e.message}")

            return VocalModeRoutingDecision(
                vocalAnalysis = VocalAnalysis(VocalMode.UNKNOWN, 0f, VocalFeatures(0f, 0f, 0f, 0f)),
                selectedEngine = ScoringEngineType.SPEECH_ENGINE,
                routedMode = VocalMode.UNKNOWN
            )
        }
    }

    /**
     * TRUE ENGINE SWITCH - Select different scoring engines based on vocal mode
     */
    private fun selectScoringEngine(vocalMode: VocalMode): ScoringEngineType {
        return when (vocalMode) {
            VocalMode.SPEECH -> {
                Log.d("VocalModeRouter", "✅ ENGINE SELECTION → SpeechScoringEngine")
                ScoringEngineType.SPEECH_ENGINE
            }

            VocalMode.SINGING -> {
                Log.d("VocalModeRouter", "✅ ENGINE SELECTION → SingingScoringEngine")
                ScoringEngineType.SINGING_ENGINE
            }

            VocalMode.UNKNOWN -> {
                Log.d("VocalModeRouter", "⚠️ ERROR/UNCLEAR CASE → Falling back to SpeechScoringEngine (same as detector)")
                ScoringEngineType.SPEECH_ENGINE
            }
        }
    }
}

/**
 * Engine selector for true separation architecture
 */
enum class ScoringEngineType {
    SPEECH_ENGINE,      // Optimized for speech patterns + fallback for errors
    SINGING_ENGINE      // Optimized for musical patterns
}

/**
 * True separation routing decision - selects which ENGINE to use
 */
data class VocalModeRoutingDecision(
    val vocalAnalysis: VocalAnalysis,
    val selectedEngine: ScoringEngineType,
    val routedMode: VocalMode
)