package com.example.reversey.scoring

import android.util.Log
import com.example.reversey.data.models.ChallengeType
import javax.inject.Inject

class VocalScoringOrchestrator @Inject constructor(
    private val vocalModeDetector: VocalModeDetector,
    private val vocalModeRouter: VocalModeRouter,
    private val speechScoringEngine: SpeechScoringEngine,
    private val singingScoringEngine: SingingScoringEngine
) {

    suspend fun scoreAttempt(
        referenceAudio: FloatArray,
        attemptAudio: FloatArray,
        challengeType: ChallengeType,
        difficulty: DifficultyLevel,
        sampleRate: Int = 44100
    ): ScoringResult {

        Log.d("VSO", "=== ORCHESTRATOR ENTRY ===")
        // Virtual ID for logging (timestamp-based to satisfy Claude's suggestion)
        val recordingId = "mem_${System.currentTimeMillis()}"

        // 1) Detect speech/singing mode DIRECTLY from memory
        // ⚡ IO FIX: Eliminates disk write/read cycle
        val analysis = vocalModeDetector.classifyVocalMode(referenceAudio, sampleRate)

        Log.d("VSO", "Detector → mode=${analysis.mode}, confidence=${analysis.confidence}")
        ScoringDebugLogger.logDetectorDecision(recordingId, analysis.mode)

        // 2) Route to SPEECH or SINGING engine
        val decision = vocalModeRouter.getRoutingDecision(analysis)
        ScoringDebugLogger.logRouterSelection(
            recordingId = recordingId,
            detectedMode = analysis.mode,
            selectedEngine = decision.selectedEngine
        )

        // 3) Pick the correct engine
        val result = when (decision.selectedEngine) {
            ScoringEngineType.SPEECH_ENGINE -> {
                ScoringDebugLogger.logOrchestratorEngine(recordingId, ScoringEngineType.SPEECH_ENGINE)
                speechScoringEngine.scoreAttempt(
                    originalAudio = referenceAudio,
                    playerAttempt = attemptAudio,
                    challengeType = challengeType,
                    difficulty = difficulty,
                    sampleRate = sampleRate
                )
            }

            ScoringEngineType.SINGING_ENGINE -> {
                ScoringDebugLogger.logOrchestratorEngine(recordingId, ScoringEngineType.SINGING_ENGINE)
                singingScoringEngine.scoreAttempt(
                    originalAudio = referenceAudio,
                    playerAttempt = attemptAudio,
                    challengeType = challengeType,
                    difficulty = difficulty,
                    sampleRate = sampleRate
                )
            }
        }

        Log.d("VSO", "=== ORCHESTRATOR EXIT → score=${result.score} ===")
        return result
    }
}