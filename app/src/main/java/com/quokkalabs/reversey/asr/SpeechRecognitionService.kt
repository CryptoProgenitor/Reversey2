package com.quokkalabs.reversey.asr

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * 🗣️ PHASE 3: Android SpeechRecognizer wrapper for Forward Speech content scoring.
 * 
 * Uses Google's online ASR - excellent accuracy, zero APK bloat.
 * Gracefully degrades to acoustic-only scoring when offline.
 * 
 * GLUTE Principle: Simple integration, no external dependencies
 */
@Singleton
class SpeechRecognitionService @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val TAG = "ASR_SERVICE"
    }

    /**
     * Check if speech recognition is available on this device
     */
    fun isAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    /**
     * Check if device is online (ASR requires network)
     */
    fun isOnline(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /**
     * Transcribe audio from FloatArray.
     * Returns transcription result with text or error.
     */
    suspend fun transcribe(
        audioData: FloatArray,
        sampleRate: Int = 44100
    ): TranscriptionResult = withContext(Dispatchers.IO) {
        
        if (!isAvailable()) {
            Log.w(TAG, "Speech recognition not available on device")
            return@withContext TranscriptionResult.unavailable("Speech recognition not available")
        }

        if (!isOnline()) {
            Log.w(TAG, "Device offline - ASR requires network")
            return@withContext TranscriptionResult.offline()
        }

        // Convert FloatArray to WAV file (SpeechRecognizer needs audio file)
        val tempFile = createTempWavFile(audioData, sampleRate)
        
        try {
            transcribeFile(tempFile)
        } finally {
            tempFile.delete()
        }
    }

    /**
     * Transcribe from existing WAV file
     */
    suspend fun transcribeFile(audioFile: File): TranscriptionResult {
        if (!audioFile.exists()) {
            return TranscriptionResult.error("Audio file not found")
        }

        if (!isOnline()) {
            return TranscriptionResult.offline()
        }

        return withContext(Dispatchers.Main) {
            suspendCancellableCoroutine { continuation ->
                val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
                
                recognizer.setRecognitionListener(object : RecognitionListener {
                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val confidences = results?.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)
                        
                        val text = matches?.firstOrNull()?.trim()
                        val confidence = confidences?.firstOrNull() ?: 0f
                        
                        Log.d(TAG, "ASR Result: '$text' (confidence: ${(confidence * 100).toInt()}%)")
                        
                        recognizer.destroy()
                        
                        if (text.isNullOrBlank()) {
                            continuation.resume(TranscriptionResult.error("No speech detected"))
                        } else {
                            continuation.resume(TranscriptionResult.success(text, confidence))
                        }
                    }

                    override fun onError(error: Int) {
                        val errorMsg = when (error) {
                            SpeechRecognizer.ERROR_NETWORK -> "Network error"
                            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                            SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected"
                            SpeechRecognizer.ERROR_AUDIO -> "Audio error"
                            SpeechRecognizer.ERROR_SERVER -> "Server error"
                            SpeechRecognizer.ERROR_CLIENT -> "Client error"
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout"
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permission denied"
                            else -> "Recognition error ($error)"
                        }
                        Log.e(TAG, "ASR Error: $errorMsg")
                        recognizer.destroy()
                        
                        // Network errors = offline, other errors = failed
                        if (error == SpeechRecognizer.ERROR_NETWORK || 
                            error == SpeechRecognizer.ERROR_NETWORK_TIMEOUT) {
                            continuation.resume(TranscriptionResult.offline())
                        } else {
                            continuation.resume(TranscriptionResult.error(errorMsg))
                        }
                    }

                    override fun onReadyForSpeech(params: Bundle?) {
                        Log.d(TAG, "ASR ready for speech")
                    }
                    override fun onBeginningOfSpeech() {
                        Log.d(TAG, "ASR detected speech start")
                    }
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {
                        Log.d(TAG, "ASR detected speech end")
                    }
                    override fun onPartialResults(partialResults: Bundle?) {}
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
                }

                Log.d(TAG, "Starting ASR for: ${audioFile.name}")
                recognizer.startListening(intent)
                
                continuation.invokeOnCancellation {
                    Log.d(TAG, "ASR cancelled")
                    recognizer.cancel()
                    recognizer.destroy()
                }
            }
        }
    }

    /**
     * Convert FloatArray audio to temporary WAV file
     */
    private fun createTempWavFile(audioData: FloatArray, sampleRate: Int): File {
        val tempFile = File.createTempFile("asr_temp_", ".wav", context.cacheDir)
        
        FileOutputStream(tempFile).use { fos ->
            // Convert float [-1.0, 1.0] to 16-bit PCM
            val pcmData = ShortArray(audioData.size) { i ->
                (audioData[i] * 32767f).toInt().coerceIn(-32768, 32767).toShort()
            }
            
            // Write WAV header
            val dataSize = pcmData.size * 2
            val header = createWavHeader(sampleRate, 1, 16, dataSize)
            fos.write(header)
            
            // Write PCM data (little-endian)
            val buffer = ByteArray(pcmData.size * 2)
            for (i in pcmData.indices) {
                buffer[i * 2] = (pcmData[i].toInt() and 0xFF).toByte()
                buffer[i * 2 + 1] = (pcmData[i].toInt() shr 8 and 0xFF).toByte()
            }
            fos.write(buffer)
        }
        
        return tempFile
    }

    private fun createWavHeader(sampleRate: Int, channels: Int, bitsPerSample: Int, dataSize: Int): ByteArray {
        val byteRate = sampleRate * channels * bitsPerSample / 8
        val blockAlign = channels * bitsPerSample / 8
        val fileSize = dataSize + 36
        
        return ByteArray(44).apply {
            // RIFF chunk
            this[0] = 'R'.code.toByte(); this[1] = 'I'.code.toByte()
            this[2] = 'F'.code.toByte(); this[3] = 'F'.code.toByte()
            writeInt(this, 4, fileSize)
            this[8] = 'W'.code.toByte(); this[9] = 'A'.code.toByte()
            this[10] = 'V'.code.toByte(); this[11] = 'E'.code.toByte()
            
            // fmt chunk
            this[12] = 'f'.code.toByte(); this[13] = 'm'.code.toByte()
            this[14] = 't'.code.toByte(); this[15] = ' '.code.toByte()
            writeInt(this, 16, 16) // chunk size
            writeShort(this, 20, 1) // PCM format
            writeShort(this, 22, channels)
            writeInt(this, 24, sampleRate)
            writeInt(this, 28, byteRate)
            writeShort(this, 32, blockAlign)
            writeShort(this, 34, bitsPerSample)
            
            // data chunk
            this[36] = 'd'.code.toByte(); this[37] = 'a'.code.toByte()
            this[38] = 't'.code.toByte(); this[39] = 'a'.code.toByte()
            writeInt(this, 40, dataSize)
        }
    }

    private fun writeInt(array: ByteArray, offset: Int, value: Int) {
        array[offset] = (value and 0xFF).toByte()
        array[offset + 1] = (value shr 8 and 0xFF).toByte()
        array[offset + 2] = (value shr 16 and 0xFF).toByte()
        array[offset + 3] = (value shr 24 and 0xFF).toByte()
    }

    private fun writeShort(array: ByteArray, offset: Int, value: Int) {
        array[offset] = (value and 0xFF).toByte()
        array[offset + 1] = (value shr 8 and 0xFF).toByte()
    }
}

/**
 * Result of a transcription attempt
 */
data class TranscriptionResult(
    val text: String?,
    val confidence: Float,
    val status: TranscriptionStatus,
    val errorMessage: String? = null
) {
    val isSuccess: Boolean get() = status == TranscriptionStatus.SUCCESS
    val isOffline: Boolean get() = status == TranscriptionStatus.OFFLINE
    
    /**
     * Get first N words for scorecard display
     */
    fun getFirstWords(n: Int = 50): String {
        if (text.isNullOrBlank()) return ""
        val words = text.split(Regex("\\s+"))
        return if (words.size <= n) text else words.take(n).joinToString(" ") + "..."
    }
    
    companion object {
        fun success(text: String, confidence: Float) = TranscriptionResult(
            text = text,
            confidence = confidence,
            status = TranscriptionStatus.SUCCESS
        )
        
        fun offline() = TranscriptionResult(
            text = null,
            confidence = 0f,
            status = TranscriptionStatus.OFFLINE,
            errorMessage = "Device offline"
        )
        
        fun error(message: String) = TranscriptionResult(
            text = null,
            confidence = 0f,
            status = TranscriptionStatus.ERROR,
            errorMessage = message
        )
        
        fun unavailable(message: String) = TranscriptionResult(
            text = null,
            confidence = 0f,
            status = TranscriptionStatus.UNAVAILABLE,
            errorMessage = message
        )
    }
}

enum class TranscriptionStatus {
    SUCCESS,    // Transcription completed
    OFFLINE,    // Device offline, pending transcription
    ERROR,      // Transcription failed
    UNAVAILABLE // ASR not available on device
}
