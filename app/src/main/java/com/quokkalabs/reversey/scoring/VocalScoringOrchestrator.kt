package com.quokkalabs.reversey.scoring

import android.util.Log
import com.quokkalabs.reversey.data.models.ChallengeType
import javax.inject.Inject
import com.quokkalabs.reversey.audio.AudioConstants

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
        sampleRate: Int = AudioConstants.SAMPLE_RATE
    ): ScoringResult {

        Log.d("VSO", "=== ORCHESTRATOR ENTRY ===")
        val recordingId = "mem_${System.currentTimeMillis()}"

        // ⚡ FIX: Detect mode on the ATTEMPT (User's Voice)
        // We must analyze the human audio to decide if they are singing or speaking.
        val analysis = vocalModeDetector.classifyVocalMode(attemptAudio, sampleRate)

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