package com.quokkalabs.reversey.audio

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import com.quokkalabs.reversey.asr.TranscriptionResult
import com.quokkalabs.reversey.utils.writeWavHeader
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

// 🎯 Sealed class for type-safe events
sealed class RecorderEvent {
    object Warning : RecorderEvent()
    object Stop : RecorderEvent()
}

// 🎤 Result container for recording (transcription now handled separately by Vosk)
data class RecordingResult(
    val file: File?,
    val transcription: TranscriptionResult? = null  // Kept for interface compatibility, but unused
)

@Singleton
class AudioRecorderHelper @Inject constructor(
    @ApplicationContext private val context: Context
    // 🎤 REMOVED: LiveTranscriptionHelper - no longer needed (Vosk handles transcription)
) {
    // Simple state for the ViewModel to observe
    private val _isRecording = MutableStateFlow(false)
    val isRecording = _isRecording.asStateFlow()

    private val _amplitude = MutableStateFlow(0f)
    val amplitude = _amplitude.asStateFlow()

    // 🎯 Typed RecorderEvent
    private val _events = MutableSharedFlow<RecorderEvent>()
    val events: SharedFlow<RecorderEvent> = _events.asSharedFlow()

    private var recorderJob: Job? = null
    private var audioRecord: AudioRecord? = null
    private var currentFile: File? = null

    // Flag to prevent Toast spam during the warning phase
    private var hasShownSizeWarning = false

    // Timestamp for throttling checks
    private var lastCheckTime = 0L

    // Scope tied to the Singleton (survives rotation, prevents leaks)
    private val helperScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @SuppressLint("MissingPermission")
    fun start(outputFile: File) {
        if (_isRecording.value) return

        // Reset warning flag for new recording
        hasShownSizeWarning = false
        lastCheckTime = 0L

        // Use standard AudioFormat constants to ensure compatibility
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
        val sampleRate = AudioConstants.SAMPLE_RATE

        val bufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            channelConfig,
            audioFormat
        )

        try {
            // Use VOICE_RECOGNITION source for cleaner audio
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.VOICE_RECOGNITION,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                Log.e("AudioRecorder", "Microphone failed to initialize")
                return
            }

            // Start AudioRecord
            audioRecord?.startRecording()
            _isRecording.value = true
            currentFile = outputFile
            Log.d("AudioRecorder", "🎙️ AudioRecord STARTED")

            // 🎤 REMOVED: LiveTranscriptionHelper.startListening() - caused beep and didn't work

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
     * Returns RecordingResult with file only (transcription handled by Vosk in AudioViewModel)
     */
    suspend fun stop(): RecordingResult {
        if (!_isRecording.value) {
            return RecordingResult(null, null)
        }

        try {
            audioRecord?.stop()
        } catch (e: Exception) {
            Log.w("AudioRecorder", "Error stopping AudioRecord", e)
        }

        // Await the file writing job to ensure data is flushed
        recorderJob?.join()

        // 🎤 REMOVED: LiveTranscriptionHelper.stopAndGetResult() - Vosk handles transcription now

        // Perform final file operations and cleanup on the IO thread
        return withContext(Dispatchers.IO) {
            val file = currentFile

            if (file != null && file.exists() && file.length() > AudioConstants.WAV_HEADER_SIZE) {
                addWavHeader(file)
                cleanup()
                RecordingResult(file, null)  // Transcription handled separately by Vosk
            } else {
                cleanup()
                RecordingResult(null, null)
            }
        }
    }

    private suspend fun writeAudioDataToFile(file: File, bufferSize: Int) = withContext(Dispatchers.IO) {
        val buffer = ShortArray(bufferSize / 2)
        val startTime = System.currentTimeMillis()

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

                        // Check recording duration/size limits
                        checkDuration(startTime)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Write loop error", e)
        }
    }

    private suspend fun checkDuration(startTime: Long) {
        // Throttle check to once per second
        val now = System.currentTimeMillis()
        if (now - lastCheckTime < 1000) return
        lastCheckTime = now

        val currentDurationMs = now - startTime

        // Amber Alert (Approaching Limit)
        if (currentDurationMs > AudioConstants.WARNING_DURATION_MS && !hasShownSizeWarning) {
            hasShownSizeWarning = true
            _events.emit(RecorderEvent.Warning)
        }

        // Red Alert (Hard Stop)
        if (currentDurationMs >= AudioConstants.MAX_RECORDING_DURATION_MS) {
            _events.emit(RecorderEvent.Stop)
        }
    }

    private fun addWavHeader(file: File) {
        if (!file.exists()) return
        val rawData = file.readBytes()
        val tempFile = File(file.parent, "${file.name}.tmp")

        try {
            FileOutputStream(tempFile).use { fos ->
                // Ensure 1 channel (Mono) and 16 bits matches configuration above
                writeWavHeader(fos, rawData, 1, AudioConstants.SAMPLE_RATE, 16)
            }
            file.delete()
            tempFile.renameTo(file)
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Header write failed", e)
        }
    }

    fun cleanup() {
        // 🎤 REMOVED: liveTranscriptionHelper.cancel()

        try { audioRecord?.release() } catch (e: Exception) { }
        audioRecord = null
        recorderJob = null
        _isRecording.value = false
        _amplitude.value = 0f
        currentFile = null
    }
}
