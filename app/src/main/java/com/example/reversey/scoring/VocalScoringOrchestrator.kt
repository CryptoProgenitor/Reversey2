package com.example.reversey.scoring

import android.util.Log
import com.example.reversey.data.models.ChallengeType
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

/**
 * VocalScoringOrchestrator
 *
 * Handles:
 *  - Running VocalModeDetector on the REFERENCE audio
 *  - Routing via VocalModeRouter
 *  - Calling SpeechScoringEngine or SingingScoringEngine correctly
 *
 * Uses FLOAT scoring path (no Files) because that matches your engines.
 */
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
        Log.d("VSO", "Ref size=${referenceAudio.size}, Attempt size=${attemptAudio.size}")

        // 1) Create temp WAV for the REFERENCE (detector only supports File)
        val tempRef = File.createTempFile("rvy_detector_ref_", ".wav")
        writeSimpleWavFile(tempRef, referenceAudio, sampleRate)

        val recordingId = ScoringDebugLogger.formatRecordingId(tempRef.absolutePath)

        // 2) Detect speech/singing mode
        val analysis = vocalModeDetector.classifyVocalMode(tempRef)
        Log.d("VSO", "Detector → mode=${analysis.mode}, confidence=${analysis.confidence}")
        ScoringDebugLogger.logDetectorDecision(recordingId, analysis.mode)

        // 3) Route to SPEECH or SINGING engine
        val decision = vocalModeRouter.getRoutingDecision(analysis)
        Log.d("VSO", "Routing → ${decision.selectedEngine}")
        ScoringDebugLogger.logRouterSelection(
            recordingId = recordingId,
            detectedMode = analysis.mode,
            selectedEngine = decision.selectedEngine
        )

        // 4) Pick the correct engine
        val result = when (decision.selectedEngine) {
            ScoringEngineType.SPEECH_ENGINE -> {
                ScoringDebugLogger.logOrchestratorEngine(recordingId, ScoringEngineType.SPEECH_ENGINE)
                ScoringDebugLogger.logSpeechEngineEnter(recordingId)

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
                ScoringDebugLogger.logSingingEngineEnter(recordingId)

                singingScoringEngine.scoreAttempt(
                    originalAudio = referenceAudio,
                    playerAttempt = attemptAudio,
                    challengeType = challengeType,
                    difficulty = difficulty,
                    sampleRate = sampleRate
                )
            }
        }

        // Cleanup temp file
        try { tempRef.delete() } catch (_: Exception) {}

        Log.d("VSO", "=== ORCHESTRATOR EXIT → score=${result.score} ===")
        return result
    }


    // WAV writer (16-bit PCM) for detector
    private fun writeSimpleWavFile(
        file: File,
        audioData: FloatArray,
        sampleRate: Int
    ) {
        val numChannels = 1
        val bitsPerSample = 16
        val byteRate = sampleRate * numChannels * bitsPerSample / 8
        val dataSize = audioData.size * 2
        val totalDataLen = 36 + dataSize

        FileOutputStream(file).use { fos ->

            // Header
            fos.write("RIFF".toByteArray())
            fos.write(intLE(totalDataLen))
            fos.write("WAVE".toByteArray())
            fos.write("fmt ".toByteArray())
            fos.write(intLE(16))
            fos.write(shortLE(1))
            fos.write(shortLE(numChannels))
            fos.write(intLE(sampleRate))
            fos.write(intLE(byteRate))
            fos.write(shortLE(numChannels * bitsPerSample / 8))
            fos.write(shortLE(bitsPerSample))

            // Data
            fos.write("data".toByteArray())
            fos.write(intLE(dataSize))

            audioData.forEach { sample ->
                val pcm = (sample * Short.MAX_VALUE)
                    .toInt()
                    .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                fos.write(shortLE(pcm))
            }
        }
    }

    private fun intLE(value: Int) = byteArrayOf(
        (value and 0xFF).toByte(),
        ((value shr 8) and 0xFF).toByte(),
        ((value shr 16) and 0xFF).toByte(),
        ((value shr 24) and 0xFF).toByte()
    )

    private fun shortLE(value: Int) = byteArrayOf(
        (value and 0xFF).toByte(),
        ((value shr 8) and 0xFF).toByte()
    )
}
