package com.example.reversey.data.repositories

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.example.reversey.audio.AudioConstants
import com.example.reversey.data.models.Recording
import com.example.reversey.utils.formatFileName
import com.example.reversey.utils.getRecordingsDir
import com.example.reversey.utils.writeWavHeader
import com.example.reversey.scoring.VocalModeDetector
import com.example.reversey.scoring.VocalMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.experimental.and
import kotlin.math.abs
import org.json.JSONObject

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

                    Recording(
                        name = vocalAnalysis.let { analysis ->
                            when(analysis.mode) {
                                VocalMode.SPEECH -> "💬🗣️ "
                                VocalMode.SINGING -> "🎵🎼 "
                                VocalMode.UNKNOWN -> "❓ "
                            } + formatFileName(file.name)
                        },
                        originalPath = file.absolutePath,
                        reversedPath = if (reversedFile.exists()) reversedFile.absolutePath else null,
                        attempts = emptyList(), // Attempts will be loaded and merged in ViewModel
                        vocalAnalysis = vocalAnalysis
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
        val buffer = ShortArray(bufferSize / 2)

        try {
            FileOutputStream(file).use { fos ->
                audioRecord.startRecording()
                while (currentCoroutineContext().isActive) {
                    val readSize = audioRecord.read(buffer, 0, buffer.size)
                    if (readSize > 0) {
                        val byteBuffer = ByteArray(readSize * 2)
                        for (i in 0 until readSize) {
                            byteBuffer[i * 2] = buffer[i].and(0xFF).toByte()
                            byteBuffer[i * 2 + 1] = (buffer[i].toInt() shr 8).toByte()
                        }
                        fos.write(byteBuffer)

                        val maxAmplitude = buffer.maxOfOrNull { abs(it.toFloat()) } ?: 0f
                        val normalizedAmplitude = maxAmplitude / Short.MAX_VALUE
                        onAmplitudeUpdate(normalizedAmplitude)
                    }
                }
            }
        } finally {
            if (audioRecord.state == AudioRecord.STATE_INITIALIZED) audioRecord.stop()
            audioRecord.release()
            addWavHeader(file)
        }
    }

    suspend fun reverseWavFile(originalFile: File?): File? = withContext(Dispatchers.IO) {
        if (originalFile == null || !originalFile.exists() || originalFile.length() < 44) {
            return@withContext null
        }
        try {
            val fileBytes = originalFile.readBytes()
            val rawPcmData = fileBytes.drop(44).toByteArray()
            if (rawPcmData.isEmpty()) return@withContext null

            val reversedPcmData = ByteArray(rawPcmData.size)
            var i = 0
            while (i < rawPcmData.size - 1) {
                reversedPcmData[i] = rawPcmData[rawPcmData.size - 2 - i]
                reversedPcmData[i + 1] = rawPcmData[rawPcmData.size - 1 - i]
                i += 2
            }

            val reversedFile =
                File(originalFile.parent, originalFile.name.replace(".wav", "_reversed.wav"))
            FileOutputStream(reversedFile).use { fos ->
                writeWavHeader(fos, reversedPcmData, 1, AudioConstants.SAMPLE_RATE, 16)
            }
            return@withContext reversedFile
        } catch (_: Exception) {
            return@withContext null
        }
    }

    private fun addWavHeader(file: File) {
        if (!file.exists() || file.length() == 0L) return
        val rawData = file.readBytes()
        try {
            val tempFile = File.createTempFile("temp_wav", ".tmp", file.parentFile)
            FileOutputStream(tempFile).use { fos ->
                writeWavHeader(fos, rawData, 1, AudioConstants.SAMPLE_RATE, 16)
            }
            file.delete()
            tempFile.renameTo(file)
        } catch (_: IOException) {
            // Log error if needed
        }
    }

    fun getLatestFile(isAttempt: Boolean = false): File? {
        val dir = if (isAttempt) {
            File(context.filesDir, "recordings/attempts")
        } else {
            getRecordingsDir(context)
        }
        return dir.listFiles { _, name -> name.endsWith(".wav") && !name.contains("_reversed") }?.maxByOrNull { it.lastModified() }
    }

    // ⚡ VOCAL ANALYSIS CACHING SYSTEM - ELIMINATES 5+ SECOND DELAYS!
    private data class CachedVocalAnalysis(
        val mode: VocalMode,
        val confidence: Float,
        val pitchStability: Float,
        val pitchContour: Float,
        val mfccSpread: Float,
        val voicedRatio: Float
    )

    /**
     * Get cached vocal analysis or analyze if cache is invalid/missing
     * This is the KEY performance fix - only analyze new files!
     */
    private suspend fun getCachedOrAnalyzeVocalMode(audioFile: File): com.example.reversey.scoring.VocalAnalysis {
        val cacheFile = getCacheFileForAudio(audioFile)

        // Check if cache exists and is newer than audio file
        if (cacheFile.exists() && cacheFile.lastModified() > audioFile.lastModified()) {
            try {
                val cached = loadCachedAnalysis(cacheFile)
                if (cached != null) {
                    Log.d("RecordingRepository", "Using cached analysis for ${audioFile.name}")
                    return com.example.reversey.scoring.VocalAnalysis(
                        mode = cached.mode,
                        confidence = cached.confidence,
                        features = com.example.reversey.scoring.VocalFeatures(
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
}