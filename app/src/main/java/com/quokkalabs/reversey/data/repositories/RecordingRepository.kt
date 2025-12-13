package com.quokkalabs.reversey.data.repositories

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.core.app.ActivityCompat
import com.quokkalabs.reversey.audio.AudioConstants
import com.quokkalabs.reversey.data.models.Recording
import com.quokkalabs.reversey.scoring.VocalAnalysis
import com.quokkalabs.reversey.scoring.VocalFeatures
import com.quokkalabs.reversey.scoring.VocalMode
import com.quokkalabs.reversey.utils.formatFileName
import com.quokkalabs.reversey.utils.getRecordingsDir
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.experimental.and
import kotlin.math.abs


// 🎤 PHASE 3: Removed SpeechRecognitionService from constructor - live transcription handled by AudioRecorderHelper
// 🔄 REFACTOR: Removed VocalModeDetector - dual pipeline eliminated (Dec 2025)
class RecordingRepository(
    private val context: Context,
) {
    companion object {
        private const val TAG = "RecordingRepository"
    }

    // ⚡ PERFORMANCE OPTIMIZED: Removed delay, added caching
    suspend fun loadRecordings(): List<Recording> = withContext(Dispatchers.IO) {
        val dir = getRecordingsDir(context)
        val originalFiles =
            dir.listFiles { _, name -> name.endsWith(".wav") && !name.contains("_reversed") }
                ?: emptyArray()

        originalFiles
            .sortedByDescending { it.lastModified() }
            .mapNotNull { file ->
                try {
                    val reversedFile = File(dir, file.name.replace(".wav", "_reversed.wav"))

                    // 🛑 CRITICAL FIX: Removed strict WAV validation
                    // Only do basic sanity check - file exists and has some content
                    if (file.length() < 44) {
                        Log.d("RecordingRepository", "Skipping too-small file: ${file.name}")
                        return@mapNotNull null
                    }

                    // 🔄 REFACTOR: Hardcoded neutral analysis - dual pipeline eliminated
                    val vocalAnalysis = VocalAnalysis(
                        mode = VocalMode.UNKNOWN,
                        confidence = 0f,
                        features = VocalFeatures(0f, 0f, 0f, 0f)
                    )

                    Log.d("RecordingRepository", "LOADED: ${file.name}")

                    // 🗣️ PHASE 3: Get cached transcription (or null if not yet transcribed)
                    val transcriptionData = getCachedTranscription(file)

                    Recording(
                        name = formatFileName(file.name),
                        originalPath = file.absolutePath,
                        reversedPath = if (reversedFile.exists()) reversedFile.absolutePath else null,
                        attempts = emptyList(),
                        vocalAnalysis = vocalAnalysis,
                        referenceTranscription = transcriptionData?.text,
                        transcriptionConfidence = transcriptionData?.confidence,
                        transcriptionPending = transcriptionData?.pending ?: false,
                        // 🎯 PHASE 1: Compute trimmed duration from cached sample count
                        trimmedDurationMs = transcriptionData?.trimmedSampleCount?.let { samples ->
                            if (samples > 0) (samples * 1000L) / AudioConstants.SAMPLE_RATE else null
                        }
                    )

                } catch (e: Exception) {
                    Log.w(
                        "RecordingRepository",
                        "Error loading recording ${file.name}: ${e.message}"
                    )
                    null // Skip invalid files
                }
            }
    }

    suspend fun deleteRecording(originalPath: String, reversedPath: String?) = withContext(
        Dispatchers.IO
    ) {
        try {
            File(originalPath).let { if (it.exists()) it.delete() }
            reversedPath?.let { File(it).let { f -> if (f.exists()) f.delete() } }
        } catch (_: Exception) {
            // Log error if needed
        }
    }

    suspend fun clearAllRecordings() = withContext(Dispatchers.IO) {
        try {
            val recordingsDir = getRecordingsDir(context)
            recordingsDir.listFiles()?.forEach { file ->
                if (file.isFile) {
                    file.delete()
                }
            }
        } catch (_: Exception) {
            // Log error if needed
        }
    }

    suspend fun renameRecording(oldPath: String, newName: String): Boolean =
        withContext(Dispatchers.IO) {
            if (newName.isBlank() || !newName.endsWith(".wav")) return@withContext false

            try {
                val oldFile = File(oldPath)
                if (!oldFile.exists()) return@withContext false
                val newFile = File(oldFile.parent, newName)
                if (newFile.exists()) return@withContext false

                val renameSuccess = oldFile.renameTo(newFile)
                if (renameSuccess) {
                    val oldReversedPath = oldPath.replace(".wav", "_reversed.wav")
                    val oldReversedFile = File(oldReversedPath)
                    if (oldReversedFile.exists()) {
                        val newReversedName = newName.replace(".wav", "_reversed.wav")
                        val newReversedFile = File(oldReversedFile.parent, newReversedName)
                        oldReversedFile.renameTo(newReversedFile)
                    }
                }
                return@withContext renameSuccess
            } catch (e: Exception) {
                Log.e(TAG, "Failed to rename recording: $oldPath -> $newName", e)
                return@withContext false
            }
        }

    suspend fun startRecording(file: File, onAmplitudeUpdate: (Float) -> Unit) {
        val bufferSize = AudioRecord.getMinBufferSize(
            AudioConstants.SAMPLE_RATE,
            AudioConstants.CHANNEL_CONFIG,
            AudioConstants.AUDIO_FORMAT
        )

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            AudioConstants.SAMPLE_RATE,
            AudioConstants.CHANNEL_CONFIG,
            AudioConstants.AUDIO_FORMAT,
            bufferSize
        )

        audioRecord.startRecording()

        withContext(Dispatchers.IO) {
            try {
                FileOutputStream(file).use { fos ->
                    val buffer = ShortArray(bufferSize)
                    while (currentCoroutineContext().isActive) {
                        val read = audioRecord.read(buffer, 0, bufferSize)
                        if (read > 0) {
                            val byteBuffer = ByteArray(read * 2)
                            for (i in 0 until read) {
                                byteBuffer[i * 2] = (buffer[i] and 0xFF).toByte()
                                byteBuffer[i * 2 + 1] =
                                    ((buffer[i].toInt() shr 8) and 0xFF).toByte()
                            }
                            fos.write(byteBuffer)

                            // Calculate amplitude (0-1 range)
                            var maxAmp = 0
                            for (i in 0 until read) {
                                val amplitude = abs(buffer[i].toInt())
                                if (amplitude > maxAmp) maxAmp = amplitude
                            }
                            val normalizedAmp = maxAmp / 32768f
                            withContext(Dispatchers.Main) {
                                onAmplitudeUpdate(normalizedAmp)
                            }
                        }
                    }
                }
            } catch (e: IOException) {
                Log.e("RecordingRepository", "Error during recording", e)
            } finally {
                audioRecord.stop()
                audioRecord.release()
            }
        }
    }

    suspend fun reverseWavFile(inputFile: File): File? {
        return withContext(Dispatchers.IO) {
            try {
                val bytes = inputFile.readBytes()
                if (bytes.size <= 44) return@withContext null

                val header = bytes.sliceArray(0 until 44)
                val audioData = bytes.sliceArray(44 until bytes.size)

                // Reverse as 16-bit samples
                val reversedAudioData = ByteArray(audioData.size)
                for (i in audioData.indices step 2) {
                    val targetIndex = audioData.size - 2 - i
                    if (targetIndex >= 0 && targetIndex + 1 < audioData.size) {
                        reversedAudioData[i] = audioData[targetIndex]
                        reversedAudioData[i + 1] = audioData[targetIndex + 1]
                    }
                }

                val reversedFile = File(
                    inputFile.parent,
                    inputFile.name.replace(".wav", "_reversed.wav")
                )
                FileOutputStream(reversedFile).use { fos ->
                    fos.write(header)
                    fos.write(reversedAudioData)
                }

                reversedFile

            } catch (e: Exception) {
                Log.e("RecordingRepository", "Error reversing WAV", e)
                null
            }
        }
    }

    // ============================================================
    //  ⚡ VOCAL MODE CACHING SYSTEM (DEPRECATED - Dec 2025)
    //  Dual pipeline eliminated. Keeping cleanupAnalysisCache as no-op
    //  for backward compatibility with existing cache directories.
    // ============================================================

    /**
     * Clean up old analysis cache files (call periodically)
     * Now a no-op but kept for API compatibility
     */
    suspend fun cleanupAnalysisCache() = withContext(Dispatchers.IO) {
        try {
            val recordingsDir = getRecordingsDir(context)
            val cacheDir = File(recordingsDir, ".analysis_cache")

            if (!cacheDir.exists()) return@withContext

            // Clean up orphaned cache files
            val cacheFiles = cacheDir.listFiles() ?: return@withContext
            var cleaned = 0

            for (cacheFile in cacheFiles) {
                val audioFileName = cacheFile.nameWithoutExtension + ".wav"
                val audioFile = File(recordingsDir, audioFileName)

                if (!audioFile.exists()) {
                    cacheFile.delete()
                    cleaned++
                }
            }

            if (cleaned > 0) {
                Log.d("RecordingRepository", "Cleaned $cleaned orphaned analysis cache files")
            }
        } catch (e: Exception) {
            Log.w("RecordingRepository", "Error cleaning analysis cache: ${e.message}")
        }
    }

    // ============================================================
    //  🗣️ PHASE 3: TRANSCRIPTION CACHING SYSTEM (LIVE ASR)
    // ============================================================

    data class CachedTranscription(
        val text: String?,
        val confidence: Float,
        val pending: Boolean,
        val trimmedSampleCount: Int = 0,  // 🎯 PHASE 1: For timed recording countdown
    )

    private fun getTranscriptionCacheFile(audioFile: File): File {
        val cacheDir = File(audioFile.parent, ".transcription_cache").apply { mkdirs() }
        val baseName = audioFile.nameWithoutExtension
        return File(cacheDir, "$baseName.transcription")
    }

    private fun getCachedTranscription(audioFile: File): CachedTranscription? {
        val cacheFile = getTranscriptionCacheFile(audioFile)

        if (cacheFile.exists() && cacheFile.lastModified() > audioFile.lastModified()) {
            return try {
                val json = JSONObject(cacheFile.readText())
                CachedTranscription(
                    text = if (json.has("text") && !json.isNull("text")) json.getString("text") else null,
                    confidence = json.optDouble("confidence", 0.0).toFloat(),
                    pending = json.optBoolean("pending", false),
                    trimmedSampleCount = json.optInt("trimmedSampleCount", 0)  // 🎯 PHASE 1
                )
            } catch (e: Exception) {
                Log.w("RecordingRepository", "Failed to load cached transcription: ${e.message}")
                null
            }
        }
        return null
    }

    /**
     * 🎤 PHASE 3: Cache a LIVE transcription result.
     * Called by AudioViewModel after recording stops with live ASR result.
     * 🎯 PHASE 1: Now also caches trimmed sample count for timed recording.
     */
    suspend fun cacheTranscription(
        audioFile: File,
        text: String,
        confidence: Float,
        trimmedSampleCount: Int = 0,
    ) = withContext(Dispatchers.IO) {
        Log.d(
            "RecordingRepository",
            "🎤 Caching live transcription: '${text.take(50)}...' (trimmed=$trimmedSampleCount samples)"
        )
        val cached = CachedTranscription(
            text,
            confidence,
            pending = false,
            trimmedSampleCount = trimmedSampleCount
        )
        saveCachedTranscription(audioFile, cached)
    }

    /**
     * 🎤 PHASE 3: Mark transcription as pending (when offline at record time).
     * Can be retried later when device comes online.
     */
    suspend fun markTranscriptionPending(audioFile: File) = withContext(Dispatchers.IO) {
        Log.d("RecordingRepository", "🎤 Marking transcription pending for: ${audioFile.name}")
        val pending = CachedTranscription(null, 0f, pending = true)
        saveCachedTranscription(audioFile, pending)
    }

    private fun saveCachedTranscription(audioFile: File, transcription: CachedTranscription) {
        try {
            val cacheFile = getTranscriptionCacheFile(audioFile)
            val json = JSONObject().apply {
                put("text", transcription.text)
                put("confidence", transcription.confidence.toDouble())
                put("pending", transcription.pending)
                put("trimmedSampleCount", transcription.trimmedSampleCount)  // 🎯 PHASE 1
                put("timestamp", System.currentTimeMillis())
            }
            cacheFile.writeText(json.toString())
        } catch (e: Exception) {
            Log.e("RecordingRepository", "Failed to save transcription cache: ${e.message}")
        }
    }
}