package com.quokkalabs.reversey.data.repositories

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.quokkalabs.reversey.audio.AudioConstants
import com.quokkalabs.reversey.data.models.Recording
import com.quokkalabs.reversey.utils.formatFileName
import com.quokkalabs.reversey.utils.getRecordingsDir
import com.quokkalabs.reversey.utils.writeWavHeader
import com.quokkalabs.reversey.scoring.VocalModeDetector
import com.quokkalabs.reversey.scoring.VocalMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.experimental.and
import kotlin.math.abs
import org.json.JSONObject

// 🎤 PHASE 3: Removed SpeechRecognitionService import - no longer needed for file-based transcription

// 🎯 GLUTE: WAV header validation - check file is completely written
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
suspend fun isWavFileComplete(file: File): Boolean = withContext(Dispatchers.IO) {
    try {
        if (file.length() < 44) return@withContext false

        val headerBytes = file.inputStream().use {
            it.readNBytes(44)
        }

        // Parse WAV header - ChunkSize field at bytes 4-7
        val declaredChunkSize = ByteBuffer.wrap(headerBytes, 4, 4)
            .order(ByteOrder.LITTLE_ENDIAN)
            .int

        val expectedFileSize = declaredChunkSize + 8  // ChunkSize + 8 byte header
        val actualFileSize = file.length()

        Log.d("RecordingRepository", "WAV CHECK: ${file.name} - expected=$expectedFileSize, actual=$actualFileSize")

        return@withContext actualFileSize >= expectedFileSize

    } catch (e: Exception) {
        Log.w("RecordingRepository", "WAV header check failed for ${file.name}: ${e.message}")
        return@withContext false
    }
}

// 🎤 PHASE 3: Removed SpeechRecognitionService from constructor - live transcription handled by AudioRecorderHelper
class RecordingRepository(
    private val context: Context,
    private val vocalModeDetector: VocalModeDetector
) {

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

                    // ⚡ PERFORMANCE FIX: Proper WAV validation instead of delay
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (!isWavFileComplete(file)) {
                            Log.d("RecordingRepository", "Skipping incomplete WAV: ${file.name}")
                            return@mapNotNull null
                        }
                    } else {
                        // Fallback for older Android versions
                        if (file.length() < 44) {
                            return@mapNotNull null
                        }
                    }

                    // ⚡ PERFORMANCE FIX: Cached vocal analysis (only analyze new files)
                    val vocalAnalysis = getCachedOrAnalyzeVocalMode(file)

                    Log.d("RecordingRepository", "LOADED: ${file.name} - mode=${vocalAnalysis.mode}")

                    // 🗣️ PHASE 3: Get cached transcription (or null if not yet transcribed)
                    val transcriptionData = getCachedTranscription(file)

                    Recording(
                        name = vocalAnalysis.let { analysis ->
                            when(analysis.mode) {
                                VocalMode.SPEECH -> "🗣️💬"
                                VocalMode.SINGING -> "🎵🎼 "
                                VocalMode.UNKNOWN -> "❓ "
                            } + formatFileName(file.name)
                        },
                        originalPath = file.absolutePath,
                        reversedPath = if (reversedFile.exists()) reversedFile.absolutePath else null,
                        attempts = emptyList(),
                        vocalAnalysis = vocalAnalysis,
                        referenceTranscription = transcriptionData?.text,
                        transcriptionConfidence = transcriptionData?.confidence,
                        transcriptionPending = transcriptionData?.pending ?: false
                    )

                } catch (e: Exception) {
                    Log.w("RecordingRepository", "Error loading recording ${file.name}: ${e.message}")
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
            } catch (_: Exception) {
                return@withContext false
            }
        }

    suspend fun startRecording(file: File, onAmplitudeUpdate: (Float) -> Unit) {
        val bufferSize = AudioRecord.getMinBufferSize(
            AudioConstants.SAMPLE_RATE,
            AudioConstants.CHANNEL_CONFIG,
            AudioConstants.AUDIO_FORMAT
        )

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
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
                                byteBuffer[i * 2 + 1] = ((buffer[i].toInt() shr 8) and 0xFF).toByte()
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
    //  ⚡ VOCAL MODE CACHING SYSTEM
    // ============================================================

    data class CachedVocalAnalysis(
        val mode: VocalMode,
        val confidence: Float,
        val pitchStability: Float,
        val pitchContour: Float,
        val mfccSpread: Float,
        val voicedRatio: Float
    )

    private suspend fun getCachedOrAnalyzeVocalMode(audioFile: File): com.quokkalabs.reversey.scoring.VocalAnalysis {
        val cacheFile = getCacheFileForAudio(audioFile)

        // Check if cache exists and is newer than audio file
        if (cacheFile.exists() && cacheFile.lastModified() > audioFile.lastModified()) {
            try {
                val cached = loadCachedAnalysis(cacheFile)
                if (cached != null) {
                    Log.d("RecordingRepository", "Using cached analysis for ${audioFile.name}")
                    return com.quokkalabs.reversey.scoring.VocalAnalysis(
                        mode = cached.mode,
                        confidence = cached.confidence,
                        features = com.quokkalabs.reversey.scoring.VocalFeatures(
                            pitchStability = cached.pitchStability,
                            pitchContour = cached.pitchContour,
                            mfccSpread = cached.mfccSpread,
                            voicedRatio = cached.voicedRatio
                        )
                    )
                }
            } catch (e: Exception) {
                Log.w("RecordingRepository", "Failed to load cached analysis for ${audioFile.name}: ${e.message}")
            }
        }

        // Analyze and cache on background thread (Dispatchers.Default for CPU-intensive work)
        return withContext(Dispatchers.Default) {
            Log.d("RecordingRepository", "Analyzing ${audioFile.name} (not cached)")
            val analysis = vocalModeDetector.classifyVocalMode(audioFile)

            // Save to cache
            try {
                saveCachedAnalysis(cacheFile, CachedVocalAnalysis(
                    mode = analysis.mode,
                    confidence = analysis.confidence,
                    pitchStability = analysis.features.pitchStability,
                    pitchContour = analysis.features.pitchContour,
                    mfccSpread = analysis.features.mfccSpread,
                    voicedRatio = analysis.features.voicedRatio
                ))
                Log.d("RecordingRepository", "Cached analysis for ${audioFile.name}")
            } catch (e: Exception) {
                Log.w("RecordingRepository", "Failed to cache analysis for ${audioFile.name}: ${e.message}")
            }

            analysis
        }
    }

    private fun getCacheFileForAudio(audioFile: File): File {
        val cacheDir = File(audioFile.parent, ".analysis_cache").apply { mkdirs() }
        val baseName = audioFile.nameWithoutExtension
        return File(cacheDir, "$baseName.analysis")
    }

    private fun loadCachedAnalysis(cacheFile: File): CachedVocalAnalysis? {
        return try {
            val json = JSONObject(cacheFile.readText())
            CachedVocalAnalysis(
                mode = VocalMode.valueOf(json.getString("mode")),
                confidence = json.getDouble("confidence").toFloat(),
                pitchStability = json.getDouble("pitchStability").toFloat(),
                pitchContour = json.getDouble("pitchContour").toFloat(),
                mfccSpread = json.getDouble("mfccSpread").toFloat(),
                voicedRatio = json.getDouble("voicedRatio").toFloat()
            )
        } catch (e: Exception) {
            Log.w("RecordingRepository", "Failed to parse cached analysis: ${e.message}")
            null
        }
    }

    private fun saveCachedAnalysis(cacheFile: File, analysis: CachedVocalAnalysis) {
        val json = JSONObject().apply {
            put("mode", analysis.mode.toString())
            put("confidence", analysis.confidence.toDouble())
            put("pitchStability", analysis.pitchStability.toDouble())
            put("pitchContour", analysis.pitchContour.toDouble())
            put("mfccSpread", analysis.mfccSpread.toDouble())
            put("voicedRatio", analysis.voicedRatio.toDouble())
            put("timestamp", System.currentTimeMillis())
        }
        cacheFile.writeText(json.toString())
    }

    /**
     * Clean up old analysis cache files (call periodically)
     */
    suspend fun cleanupAnalysisCache() = withContext(Dispatchers.IO) {
        try {
            val recordingsDir = getRecordingsDir(context)
            val cacheDir = File(recordingsDir, ".analysis_cache")

            if (!cacheDir.exists()) return@withContext

            val cacheFiles = cacheDir.listFiles() ?: return@withContext
            var cleaned = 0

            for (cacheFile in cacheFiles) {
                val audioFileName = cacheFile.nameWithoutExtension + ".wav"
                val audioFile = File(recordingsDir, audioFileName)

                // Remove cache if audio file no longer exists
                if (!audioFile.exists()) {
                    cacheFile.delete()
                    cleaned++
                }
            }

            Log.d("RecordingRepository", "Cleaned $cleaned orphaned analysis cache files")
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
        val pending: Boolean
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
                    pending = json.optBoolean("pending", false)
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
     */
    suspend fun cacheTranscription(audioFile: File, text: String, confidence: Float) = withContext(Dispatchers.IO) {
        Log.d("RecordingRepository", "🎤 Caching live transcription: '${text.take(50)}...'")
        val cached = CachedTranscription(text, confidence, pending = false)
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
                put("timestamp", System.currentTimeMillis())
            }
            cacheFile.writeText(json.toString())
        } catch (e: Exception) {
            Log.e("RecordingRepository", "Failed to save transcription cache: ${e.message}")
        }
    }

    // 🗑️ REMOVED: transcribeAndCache() - file-based transcription doesn't work on Android
    // Live transcription now handled by LiveTranscriptionHelper + AudioRecorderHelper
}