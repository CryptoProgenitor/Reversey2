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
        referenceVocalMode: VocalMode? = null,  // 🎯 FIX: Use reference's stored mode for engine selection
        referenceTranscription: String? = null,  // 🗣️ PHASE 3: What the challenge phrase IS
        attemptTranscription: String? = null,    // 🎤 PHASE 3: What the player SAID (live ASR)
        sampleRate: Int = AudioConstants.SAMPLE_RATE
    ): ScoringResult {

        Log.d("VSO", "=== ORCHESTRATOR ENTRY ===")
        Log.d("VSO", "🎤 Reference transcription: '${referenceTranscription?.take(30)}...'")
        Log.d("VSO", "🎤 Attempt transcription: '${attemptTranscription?.take(30)}...'")

        val recordingId = "mem_${System.currentTimeMillis()}"

        // Analyze attempt audio (for UI feedback showing user's actual vocal style)
        val attemptAnalysis = vocalModeDetector.classifyVocalMode(attemptAudio, sampleRate)
        Log.d("VSO", "Attempt analysis → mode=${attemptAnalysis.mode}, confidence=${attemptAnalysis.confidence}")

        // 🎯 FIX: Use reference's stored mode for engine selection (prevents cross-engine contamination)
        // Fallback to attempt analysis if reference mode unavailable (legacy recordings, null safety)
        val engineMode = referenceVocalMode?.takeIf { it != VocalMode.UNKNOWN }
            ?: attemptAnalysis.mode

        Log.d("VSO", "🎯 ENGINE SELECTION: referenceMode=$referenceVocalMode, attemptMode=${attemptAnalysis.mode}, using=$engineMode")
        ScoringDebugLogger.logDetectorDecision(recordingId, engineMode)

        // Route to engine based on reference mode (or fallback)
        val decision = vocalModeRouter.getRoutingDecision(
            VocalAnalysis(engineMode, attemptAnalysis.confidence, attemptAnalysis.features)
        )
        ScoringDebugLogger.logRouterSelection(
            recordingId = recordingId,
            detectedMode = engineMode,
            selectedEngine = decision.selectedEngine
        )

        // Pick the correct engine
        val result = when (decision.selectedEngine) {
            ScoringEngineType.SPEECH_ENGINE -> {
                ScoringDebugLogger.logOrchestratorEngine(recordingId, ScoringEngineType.SPEECH_ENGINE)
                speechScoringEngine.scoreAttempt(
                    originalAudio = referenceAudio,
                    playerAttempt = attemptAudio,
                    challengeType = challengeType,
                    difficulty = difficulty,
                    sampleRate = sampleRate,
                    referenceTranscription = referenceTranscription,
                    attemptTranscription = attemptTranscription  // 🎤 PHASE 3: Pass live transcription
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
        // Return attempt's actual analysis for UI feedback (shows what USER did, not what challenge requires)
        return result.copy(vocalAnalysis = attemptAnalysis)
    }
}