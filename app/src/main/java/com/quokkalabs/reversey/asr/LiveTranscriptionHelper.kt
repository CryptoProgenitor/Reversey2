package com.quokkalabs.reversey.asr

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ðŸŽ¤ LIVE TRANSCRIPTION HELPER (v2 - Non-blocking)
 *
 * Fire-and-forget approach: start listening, results arrive via callback.
 * No blocking, no deadlocks.
 */
@Singleton
class LiveTranscriptionHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "LIVE_ASR"
    }

    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false

    // ðŸŽ¤ Store result when it arrives (non-blocking)
    private var lastResult: TranscriptionResult? = null

    fun isAvailable(): Boolean = SpeechRecognizer.isRecognitionAvailable(context)

    /**
     * Start listening - fire and forget.
     * Must be called from Main thread.
     */
    fun startListening() {
        if (isListening) {
            Log.w(TAG, "Already listening - ignoring")
            return
        }

        if (!isAvailable()) {
            Log.w(TAG, "SpeechRecognizer not available")
            return
        }

        Log.d(TAG, "ðŸŽ¤ Starting live transcription...")
        lastResult = null

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(createListener())
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        speechRecognizer?.startListening(intent)
        isListening = true
        Log.d(TAG, "âœ… SpeechRecognizer started")
    }

    /**
     * Stop and get whatever result we have (non-blocking).
     * Returns immediately with current result or null.
     */
    fun stopAndGetResult(): TranscriptionResult {
        Log.d(TAG, "ðŸ›‘ Stopping - returning current result")

        val result = lastResult ?: TranscriptionResult.error("No result yet")

        // Stop listening (must be on Main thread)
        android.os.Handler(android.os.Looper.getMainLooper()).post {
            try {
                speechRecognizer?.stopListening()
                speechRecognizer?.destroy()
                speechRecognizer = null
            } catch (e: Exception) {
                Log.w(TAG, "Stop error: ${e.message}")
            }
        }

        isListening = false

        Log.d(TAG, "🎤 Final: '${result.text}' (success=${result.isSuccess})")
        return result
    }

    fun cancel() {
        if (isListening) {
            Log.d(TAG, "ðŸš« Cancelling")
            // Must run on Main thread!
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                try {
                    speechRecognizer?.cancel()
                    speechRecognizer?.destroy()
                    speechRecognizer = null
                } catch (e: Exception) {
                    Log.w(TAG, "Cancel error: ${e.message}")
                }
            }
            isListening = false
        }
    }

    private fun cleanup() {
        // Must run on Main thread!
        android.os.Handler(android.os.Looper.getMainLooper()).post {
            try {
                speechRecognizer?.destroy()
                speechRecognizer = null
            } catch (e: Exception) {
                Log.w(TAG, "Cleanup error: ${e.message}")
            }
        }
        isListening = false
    }

    private fun createListener() = object : RecognitionListener {
        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val confidences = results?.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)

            val text = matches?.firstOrNull()?.trim()
            val confidence = confidences?.firstOrNull() ?: 0f

            Log.d(TAG, "âœ… Result: '$text' (${(confidence * 100).toInt()}%)")

            lastResult = if (text.isNullOrBlank()) {
                TranscriptionResult.error("No speech detected")
            } else {
                TranscriptionResult.success(text, confidence)
            }
        }

        override fun onError(error: Int) {
            val msg = when (error) {
                SpeechRecognizer.ERROR_NO_MATCH -> "No match"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Timeout"
                SpeechRecognizer.ERROR_NETWORK -> "Network"
                else -> "Error $error"
            }
            Log.e(TAG, "âŒ $msg")

            // Don't overwrite a good partial result with an error
            if (lastResult?.isSuccess != true) {
                lastResult = TranscriptionResult.error(msg)
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val partial = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val text = partial?.firstOrNull()
            if (!text.isNullOrBlank()) {
                Log.d(TAG, "ðŸ“ Partial: '$text'")
                // Save partial as a low-confidence result
                lastResult = TranscriptionResult.success(text, 0.5f)
            }
        }

        override fun onReadyForSpeech(params: Bundle?) { Log.d(TAG, "ðŸŽ¤ Ready") }
        override fun onBeginningOfSpeech() { Log.d(TAG, "ðŸ—£ï¸ Speech started") }
        override fun onEndOfSpeech() { Log.d(TAG, "ðŸ”‡ Speech ended") }
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEvent(eventType: Int, params: Bundle?) {}
    }
}