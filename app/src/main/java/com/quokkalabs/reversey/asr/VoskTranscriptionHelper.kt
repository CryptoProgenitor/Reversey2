package com.quokkalabs.reversey.asr

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.StorageService
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 🎤 VOSK TRANSCRIPTION HELPER - Proof of Concept
 * 
 * Transcribes WAV files AFTER recording completes.
 * Includes resampling from 44100Hz → 16000Hz.
 * No mic conflicts - reads from file!
 */
@Singleton
class VoskTranscriptionHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "VOSK_ASR"
        private const val MODEL_PATH = "model-small-en-us"
        private const val INPUT_SAMPLE_RATE = 44100
        private const val VOSK_SAMPLE_RATE = 16000
        private const val WAV_HEADER_SIZE = 44
    }

    private var model: Model? = null
    private var isInitialized = false

    /**
     * Initialize Vosk model (call once at app startup)
     * This copies the model from assets to internal storage on first run.
     */
    suspend fun initialize(): Boolean = withContext(Dispatchers.IO) {
        if (isInitialized) {
            Log.d(TAG, "✅ Already initialized")
            return@withContext true
        }

        try {
            Log.d(TAG, "📦 Loading Vosk model...")
            
            StorageService.unpack(context, MODEL_PATH, "model",
                { loadedModel ->
                    model = loadedModel
                    isInitialized = true
                    Log.d(TAG, "✅ Model loaded successfully")
                },
                { error ->
                    Log.e(TAG, "❌ Model load failed: ${error.message}")
                }
            )
            
            // Wait for async unpack (simple polling for POC)
            var attempts = 0
            while (!isInitialized && attempts < 30) {
                kotlinx.coroutines.delay(500)
                attempts++
            }
            
            isInitialized
        } catch (e: Exception) {
            Log.e(TAG, "❌ Initialize failed: ${e.message}")
            false
        }
    }

    /**
     * Transcribe a WAV file to text
     * 
     * @param wavFile The recorded WAV file (44100Hz mono 16-bit PCM)
     * @return TranscriptionResult with text or error
     */
    suspend fun transcribeFile(wavFile: File): TranscriptionResult = withContext(Dispatchers.IO) {
        if (!isInitialized || model == null) {
            Log.e(TAG, "❌ Model not initialized")
            return@withContext TranscriptionResult.error("Vosk not initialized")
        }

        if (!wavFile.exists()) {
            Log.e(TAG, "❌ File not found: ${wavFile.path}")
            return@withContext TranscriptionResult.error("File not found")
        }

        Log.d(TAG, "🎤 Transcribing: ${wavFile.name} (${wavFile.length()} bytes)")

        try {
            // Read and resample audio
            val resampledBytes = resampleWavTo16k(wavFile)
            
            if (resampledBytes.isEmpty()) {
                return@withContext TranscriptionResult.error("Failed to resample audio")
            }
            
            Log.d(TAG, "🔄 Resampled: ${resampledBytes.size} bytes at 16kHz")

            // Create recognizer for 16kHz audio
            val recognizer = Recognizer(model, VOSK_SAMPLE_RATE.toFloat())
            
            // Feed resampled audio in chunks
            var offset = 0
            val chunkSize = 4096
            while (offset < resampledBytes.size) {
                val end = minOf(offset + chunkSize, resampledBytes.size)
                val chunk = resampledBytes.copyOfRange(offset, end)
                recognizer.acceptWaveForm(chunk, chunk.size)
                offset = end
            }
            
            // Get final result
            val resultJson = recognizer.finalResult
            recognizer.close()
            
            // Parse JSON: {"text": "hello world"}
            val text = parseVoskResult(resultJson)
            
            Log.d(TAG, "✅ Result: '$text'")
            
            if (text.isBlank()) {
                TranscriptionResult.error("No speech detected")
            } else {
                TranscriptionResult.success(text, 0.9f)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Transcription failed: ${e.message}", e)
            TranscriptionResult.error(e.message ?: "Transcription failed")
        }
    }

    /**
     * Resample WAV from 44100Hz to 16000Hz
     * Uses linear interpolation for decent quality
     */
    private fun resampleWavTo16k(wavFile: File): ByteArray {
        try {
            val inputBytes = wavFile.readBytes()
            
            if (inputBytes.size <= WAV_HEADER_SIZE) {
                Log.e(TAG, "❌ WAV file too small")
                return ByteArray(0)
            }
            
            // Skip WAV header, get raw PCM
            val pcmBytes = inputBytes.copyOfRange(WAV_HEADER_SIZE, inputBytes.size)
            
            // Convert bytes to shorts (16-bit samples)
            val inputSamples = ShortArray(pcmBytes.size / 2)
            ByteBuffer.wrap(pcmBytes)
                .order(ByteOrder.LITTLE_ENDIAN)
                .asShortBuffer()
                .get(inputSamples)
            
            // Calculate output size
            val ratio = INPUT_SAMPLE_RATE.toDouble() / VOSK_SAMPLE_RATE
            val outputLength = (inputSamples.size / ratio).toInt()
            val outputSamples = ShortArray(outputLength)
            
            Log.d(TAG, "🔄 Resampling: ${inputSamples.size} samples → $outputLength samples")
            
            // Linear interpolation resampling
            for (i in 0 until outputLength) {
                val srcPos = i * ratio
                val srcIndex = srcPos.toInt()
                val fraction = srcPos - srcIndex
                
                val sample1 = inputSamples[srcIndex]
                val sample2 = if (srcIndex + 1 < inputSamples.size) {
                    inputSamples[srcIndex + 1]
                } else {
                    sample1
                }
                
                // Interpolate
                outputSamples[i] = (sample1 + fraction * (sample2 - sample1)).toInt().toShort()
            }
            
            // Convert shorts back to bytes
            val outputBytes = ByteArray(outputSamples.size * 2)
            ByteBuffer.wrap(outputBytes)
                .order(ByteOrder.LITTLE_ENDIAN)
                .asShortBuffer()
                .put(outputSamples)
            
            return outputBytes
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Resample failed: ${e.message}", e)
            return ByteArray(0)
        }
    }

    private fun parseVoskResult(json: String): String {
        // Simple JSON parsing for {"text": "..."}
        return try {
            val regex = """"text"\s*:\s*"([^"]*)"""".toRegex()
            regex.find(json)?.groupValues?.get(1) ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    fun isReady(): Boolean = isInitialized && model != null
}
