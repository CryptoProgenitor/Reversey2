package com.example.reversey.audio

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import com.example.reversey.AudioConstants
import com.example.reversey.utils.writeWavHeader
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class AudioRecorderHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Simple state for the ViewModel to observe
    private val _isRecording = MutableStateFlow(false)
    val isRecording = _isRecording.asStateFlow()

    private val _amplitude = MutableStateFlow(0f)
    val amplitude = _amplitude.asStateFlow()

    private var recorderJob: Job? = null
    private var audioRecord: AudioRecord? = null
    private var currentFile: File? = null

    // Scope tied to the Singleton (survives rotation, prevents leaks)
    private val helperScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @SuppressLint("MissingPermission")
    fun start(outputFile: File) {
        if (_isRecording.value) return

        val bufferSize = AudioRecord.getMinBufferSize(
            AudioConstants.SAMPLE_RATE,
            AudioConstants.CHANNEL_CONFIG,
            AudioConstants.AUDIO_FORMAT
        )

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                AudioConstants.SAMPLE_RATE,
                AudioConstants.CHANNEL_CONFIG,
                AudioConstants.AUDIO_FORMAT,
                bufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                Log.e("AudioRecorder", "Microphone failed to initialize")
                return
            }

            audioRecord?.startRecording()
            _isRecording.value = true
            currentFile = outputFile

            // Launch the write loop in the helper's scope
            recorderJob = helperScope.launch {
                writeAudioDataToFile(outputFile, bufferSize)
            }

        } catch (e: Exception) {
            Log.e("AudioRecorder", "Start failed", e)
            cleanup()
        }
    }

    /**
     * Stops recording and waits for the file to be fully written.
     * Returns the File if successful, null otherwise.
     */
    suspend fun stop(): File? {
        if (!_isRecording.value) {
            return null
        }

        try {
            audioRecord?.stop()

        } catch (e: Exception) {
            Log.w("AudioRecorder", "Error stopping AudioRecord", e)
        }

        recorderJob?.join()


        return withContext(Dispatchers.IO) {
            val file = currentFile
            // Add log here to see file size


            if (file != null && file.exists() && file.length() > 44) {
                addWavHeader(file)
                cleanup()
                file
            } else {
                cleanup()
                null
            }
        }
    }

    private suspend fun writeAudioDataToFile(file: File, bufferSize: Int) = withContext(Dispatchers.IO) {
        val buffer = ShortArray(bufferSize / 2)

        try {
            FileOutputStream(file).use { fos ->
                while (isActive && audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                    val readResult = audioRecord?.read(buffer, 0, buffer.size) ?: 0

                    if (readResult > 0) {
                        // Write raw PCM
                        val bytes = ByteArray(readResult * 2)
                        for (i in 0 until readResult) {
                            bytes[i * 2] = (buffer[i].toInt() and 0x00FF).toByte()
                            bytes[i * 2 + 1] = (buffer[i].toInt() shr 8).toByte()
                        }
                        fos.write(bytes)

                        // Calculate Amplitude for Visualizer
                        var maxAmp = 0
                        for (i in 0 until readResult) {
                            val absVal = abs(buffer[i].toInt())
                            if (absVal > maxAmp) maxAmp = absVal
                        }
                        _amplitude.value = maxAmp / Short.MAX_VALUE.toFloat()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Write loop error", e)
        }
    }

    private fun addWavHeader(file: File) {
        if (!file.exists()) return
        val rawData = file.readBytes()
        val tempFile = File(file.parent, "${file.name}.tmp")

        try {
            FileOutputStream(tempFile).use { fos ->
                writeWavHeader(fos, rawData, 1, AudioConstants.SAMPLE_RATE, 16)
            }
            file.delete()
            tempFile.renameTo(file)
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Header write failed", e)
        }
    }

    fun cleanup() {
        try { audioRecord?.release() } catch (e: Exception) { }
        audioRecord = null
        recorderJob = null
        _isRecording.value = false
        _amplitude.value = 0f
        currentFile = null
    }
}