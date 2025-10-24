package com.example.reversey


import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.experimental.and

class RecordingRepository(private val context: Context) {


    suspend fun loadRecordings(): List<Recording> = withContext(Dispatchers.IO) {
        val dir = getRecordingsDir(context)
        val originalFiles = dir.listFiles { _, name -> name.endsWith(".wav") && !name.contains("_reversed") } ?: emptyArray()

        originalFiles
            .sortedByDescending { it.lastModified() }
            .mapNotNull { file ->
                try {
                    val reversedFile = File(dir, file.name.replace(".wav", "_reversed.wav"))

                    Recording(
                        name = formatFileName(file.name),
                        originalPath = file.absolutePath,
                        reversedPath = if (reversedFile.exists()) reversedFile.absolutePath else null,
                        attempts = emptyList() // Attempts will be loaded and merged in ViewModel
                    )
                } catch (_: Exception) {
                    null // Skip invalid files
                }
            }
    }

    suspend fun deleteRecording(originalPath: String, reversedPath: String?) = withContext(
        Dispatchers.IO) {
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

    suspend fun renameRecording(oldPath: String, newName: String): Boolean = withContext(Dispatchers.IO) {
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

        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
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

                        val maxAmplitude = buffer.maxOfOrNull { kotlin.math.abs(it.toFloat()) } ?: 0f
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

            val reversedFile = File(originalFile.parent, originalFile.name.replace(".wav", "_reversed.wav"))
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
}


