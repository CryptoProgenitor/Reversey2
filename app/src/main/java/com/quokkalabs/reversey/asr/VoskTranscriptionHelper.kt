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
     * CRITICAL FIX: Uses streaming to avoid OOM on large files
     * Previous implementation loaded entire file (up to 40MB) into memory at once
     */
    private fun resampleWavTo16k(wavFile: File): ByteArray {
        try {
            val fileSize = wavFile.length()
            if (fileSize <= WAV_HEADER_SIZE) {
                Log.e(TAG, "❌ WAV file too small")
                return ByteArray(0)
            }

            val pcmSize = fileSize - WAV_HEADER_SIZE
            val inputSampleCount = pcmSize / 2
            val ratio = INPUT_SAMPLE_RATE.toDouble() / VOSK_SAMPLE_RATE
            val outputSampleCount = (inputSampleCount / ratio).toInt()

            Log.d(TAG, "🔄 Streaming resample: $inputSampleCount samples → $outputSampleCount samples")

            // Process in chunks to limit memory usage
            // Each chunk: 8KB input buffer = 4K samples
            val inputChunkBytes = 8192
            inputChunkBytes / 2

            // Output buffer - we still need to collect output but it's smaller (36% of input)
            val outputStream = java.io.ByteArrayOutputStream(outputSampleCount * 2)

            wavFile.inputStream().use { input ->
                // Skip WAV header
                input.skip(WAV_HEADER_SIZE.toLong())

                val inputBuffer = ByteArray(inputChunkBytes)
                var totalInputSamples = 0L
                var totalOutputSamples = 0
                var prevSample: Short = 0

                while (true) {
                    val bytesRead = input.read(inputBuffer)
                    if (bytesRead <= 0) break

                    val samplesInChunk = bytesRead / 2
                    val chunkSamples = ShortArray(samplesInChunk)

                    ByteBuffer.wrap(inputBuffer, 0, bytesRead)
                        .order(ByteOrder.LITTLE_ENDIAN)
                        .asShortBuffer()
                        .get(chunkSamples)

                    // Process this chunk - determine output samples that fall within this input range
                    val chunkStartInput = totalInputSamples
                    val chunkEndInput = totalInputSamples + samplesInChunk

                    while (totalOutputSamples < outputSampleCount) {
                        val srcPos = totalOutputSamples * ratio

                        // Check if this output sample's source is within current chunk
                        if (srcPos >= chunkEndInput) break

                        val srcIndex = srcPos.toInt()
                        val localIndex = (srcIndex - chunkStartInput).toInt()
                        val fraction = srcPos - srcIndex

                        if (localIndex >= 0 && localIndex < samplesInChunk) {
                            val sample1 = if (localIndex == 0 && chunkStartInput > 0) {
                                prevSample
                            } else if (localIndex > 0) {
                                chunkSamples[localIndex - 1]
                            } else {
                                chunkSamples[0]
                            }

                            val sample2 = chunkSamples[localIndex]

                            val interpolated = (sample1 + fraction * (sample2 - sample1)).toInt()
                                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                                .toShort()

                            // Write as little-endian bytes
                            outputStream.write(interpolated.toInt() and 0xFF)
                            outputStream.write((interpolated.toInt() shr 8) and 0xFF)
                            totalOutputSamples++
                        } else {
                            totalOutputSamples++
                        }
                    }

                    // Save last sample for interpolation across chunks
                    if (samplesInChunk > 0) {
                        prevSample = chunkSamples[samplesInChunk - 1]
                    }
                    totalInputSamples += samplesInChunk
                }
            }

            Log.d(TAG, "✅ Resampled to ${outputStream.size()} bytes")
            return outputStream.toByteArray()

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

    /**
     * CRITICAL FIX: Release native Vosk model resources
     * Call this when the helper is no longer needed (e.g., in ViewModel.onCleared())
     * Previous implementation never released the model, causing native memory leaks
     */
    fun cleanup() {
        try {
            model?.close()
            Log.d(TAG, "✅ Vosk model released")
        } catch (e: Exception) {
            Log.e(TAG, "Error closing Vosk model", e)
        } finally {
            model = null
            isInitialized = false
        }
    }
}