package com.quokkalabs.reversey.audio

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import com.quokkalabs.reversey.asr.TranscriptionResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
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

    // 🎯 PHASE 2: Countdown progress for timed recording (1.0 → 0.0)
    private val _countdownProgress = MutableStateFlow(1f)
    val countdownProgress = _countdownProgress.asStateFlow()

    // 🎯 Typed RecorderEvent
    private val _events = MutableSharedFlow<RecorderEvent>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events: SharedFlow<RecorderEvent> = _events.asSharedFlow()

    private var recorderJob: Job? = null
    private var countdownJob: Job? = null  // 🎯 PHASE 2: Separate job for countdown timer
    private var audioRecord: AudioRecord? = null
    private var currentFile: File? = null

    // Flag to prevent Toast spam during the warning phase
    private var hasShownSizeWarning = false

    // Timestamp for throttling checks
    private var lastCheckTime = 0L

    // Thread safety for start/stop operations
    private val recordingMutex = Mutex()

    // Scope tied to the Singleton (survives rotation, prevents leaks)
    private val helperScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Starts recording to the specified file.
     * Thread-safe: uses same mutex as stop() to prevent race conditions.
     * Fire-and-forget: returns immediately, recording starts async.
     */
    @SuppressLint("MissingPermission")
    fun start(outputFile: File, maxDurationMs: Long? = null) {
        helperScope.launch {
            recordingMutex.withLock {
                if (_isRecording.value) return@withLock

                // Reset warning flag for new recording
                hasShownSizeWarning = false
                lastCheckTime = 0L

                // 🎯 PHASE 2: Reset countdown progress
                _countdownProgress.value = 1f

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
                        return@withLock
                    }

                    // Start AudioRecord
                    audioRecord?.startRecording()
                    _isRecording.value = true
                    currentFile = outputFile
                    Log.d("AudioRecorder", "🎙️ AudioRecord STARTED (maxDurationMs=$maxDurationMs)")

                    // 🎤 REMOVED: LiveTranscriptionHelper.startListening() - caused beep and didn't work

                    // Launch the write loop in the helper's scope (outside mutex to avoid blocking)
                    recorderJob = helperScope.launch {
                        writeAudioDataToFile(outputFile, bufferSize)
                    }

                    // 🎯 PHASE 2: Start countdown timer if maxDurationMs is specified
                    if (maxDurationMs != null && maxDurationMs > 0) {
                        countdownJob = helperScope.launch {
                            val intervalMs = 50L
                            var elapsed = 0L
                            while (elapsed < maxDurationMs && _isRecording.value) {
                                delay(intervalMs)
                                elapsed += intervalMs
                                _countdownProgress.value = 1f - (elapsed.toFloat() / maxDurationMs)
                            }
                            // Auto-stop when countdown reaches zero
                            // Auto-stop when countdown reaches zero
                            if (_isRecording.value) {
                                Log.d("AudioRecorder", "🎯 Countdown complete - auto-stopping")
                                val emitted = _events.tryEmit(RecorderEvent.Stop)
                                Log.d("AudioRecorder", "🎯 Stop event tryEmit result: $emitted")
                            }
                        }
                    }

                } catch (e: Exception) {
                    Log.e("AudioRecorder", "Start failed", e)
                    cleanup()
                }
            }
        }
    }

    /**
     * Stops recording and waits for the file to be fully written.
     * Returns RecordingResult with file only (transcription handled by Vosk in AudioViewModel)
     * Thread-safe: uses mutex to prevent race conditions with start()
     */
    suspend fun stop(): RecordingResult = recordingMutex.withLock {
        if (!_isRecording.value) {
            return@withLock RecordingResult(null, null)
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
        withContext(Dispatchers.IO) {
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
                        // Line 226 - add gain boost
                        _amplitude.value = (maxAmp / Short.MAX_VALUE.toFloat() * 5.5f).coerceIn(0f, 1f)

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

    /**
     * CRITICAL FIX: Use streaming to add WAV header
     * Previous implementation loaded entire file into memory (up to 10MB)
     * which doubled memory usage during recording finalization
     */
    private fun addWavHeader(file: File) {
        if (!file.exists()) return

        val rawDataSize = file.length().toInt()
        val tempFile = File(file.parent, "${file.name}.tmp")

        try {
            FileOutputStream(tempFile).use { fos ->
                // Write WAV header (44 bytes) with correct data size
                writeWavHeaderStreaming(fos, rawDataSize, 1, AudioConstants.SAMPLE_RATE, 16)

                // Stream copy raw PCM data in chunks (no full-file load)
                FileInputStream(file).use { fis ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    while (fis.read(buffer).also { bytesRead = it } != -1) {
                        fos.write(buffer, 0, bytesRead)
                    }
                }
            }
            file.delete()
            tempFile.renameTo(file)
            Log.d("AudioRecorder", "✅ WAV header added via streaming")
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Header write failed", e)
            if (tempFile.exists()) tempFile.delete()
        }
    }

    /**
     * Write WAV header without requiring full data in memory
     */
    private fun writeWavHeaderStreaming(
        out: FileOutputStream,
        dataSize: Int,
        channels: Int,
        sampleRate: Int,
        bitsPerSample: Int
    ) {
        val byteRate = sampleRate * channels * bitsPerSample / 8
        val blockAlign = channels * bitsPerSample / 8
        val totalSize = dataSize + 36

        // RIFF header
        out.write("RIFF".toByteArray())
        out.write(intToLittleEndian(totalSize))
        out.write("WAVE".toByteArray())

        // fmt chunk
        out.write("fmt ".toByteArray())
        out.write(intToLittleEndian(16)) // chunk size
        out.write(shortToLittleEndian(1)) // audio format (PCM)
        out.write(shortToLittleEndian(channels.toShort()))
        out.write(intToLittleEndian(sampleRate))
        out.write(intToLittleEndian(byteRate))
        out.write(shortToLittleEndian(blockAlign.toShort()))
        out.write(shortToLittleEndian(bitsPerSample.toShort()))

        // data chunk
        out.write("data".toByteArray())
        out.write(intToLittleEndian(dataSize))
    }

    private fun intToLittleEndian(value: Int): ByteArray {
        return byteArrayOf(
            (value and 0xFF).toByte(),
            ((value shr 8) and 0xFF).toByte(),
            ((value shr 16) and 0xFF).toByte(),
            ((value shr 24) and 0xFF).toByte()
        )
    }

    private fun shortToLittleEndian(value: Short): ByteArray {
        return byteArrayOf(
            (value.toInt() and 0xFF).toByte(),
            ((value.toInt() shr 8) and 0xFF).toByte()
        )
    }

    fun cleanup() {
        // 🎯 PHASE 2: Cancel countdown job
        countdownJob?.cancel()
        countdownJob = null
        _countdownProgress.value = 1f

        try { audioRecord?.release() } catch (e: Exception) { }
        audioRecord = null
        recorderJob = null
        _isRecording.value = false
        _amplitude.value = 0f
        currentFile = null
    }

    /**
     * CRITICAL FIX: Cancel the helperScope to prevent coroutine leaks
     * Call this when the helper is being destroyed (e.g., app termination)
     * Previous implementation never canceled the scope, causing memory leaks
     */
    fun destroy() {
        cleanup()
        helperScope.cancel()
        Log.d("AudioRecorder", "✅ AudioRecorderHelper destroyed, scope cancelled")
    }
}