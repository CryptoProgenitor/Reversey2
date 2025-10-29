package com.example.reversey

import android.app.Application
import android.media.MediaPlayer
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.reversey.scoring.ScoringEngine
import com.example.reversey.scoring.ScoringParameters
import com.example.reversey.scoring.applyPreset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


data class AudioUiState(
    val recordings: List<Recording> = emptyList(),
    val isRecording: Boolean = false,
    val statusText: String = "Ready to record",
    val currentlyPlayingPath: String? = null,
    val playbackProgress: Float = 0f,
    val isPaused: Boolean = false,
    val amplitudes: List<Float> = emptyList(),
    val showEasterEgg: Boolean = false,
    val showTutorial: Boolean = false,
    val cpdTaps: Int = 0,
    val isRecordingAttempt: Boolean = false,
    val parentRecordingPath: String? = null,
    val attemptToRename: Pair<String, PlayerAttempt>? = null,
    val scrollToIndex: Int? = null,
    val pendingChallengeType: ChallengeType? = null,
    val showQualityWarning: Boolean = false,
    val qualityWarningMessage: String = ""
)


@HiltViewModel
class AudioViewModel @Inject constructor(
    application: Application,
    private val repository: RecordingRepository,
    private val attemptsRepository: AttemptsRepository,
    val scoringEngine: ScoringEngine,
    private val settingsDataStore: SettingsDataStore  // ← ADD THIS LINE
) : AndroidViewModel(application) {

    init {
        Log.d("HILT_VERIFY", "📱 AudioViewModel created - ScoringEngine instance: ${scoringEngine.hashCode()}")
    }

    private var mediaPlayer: MediaPlayer? = null
    private var recordingJob: Job? = null
    private var playbackJob: Job? = null

    private val _uiState = MutableStateFlow(AudioUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadRecordings()
        viewModelScope.launch {
            settingsDataStore.tutorialCompleted.collect { completed ->
                if (!completed) {
                    _uiState.update { it.copy(showTutorial = true) }
                }
            }
        }
    }

    fun loadRecordings() {
        viewModelScope.launch(Dispatchers.IO) {
            val loadedRecordingsFromDisk = repository.loadRecordings()
            val attemptsMap = attemptsRepository.loadAttempts()
            val mergedRecordings = loadedRecordingsFromDisk.map { diskRecording: Recording ->
                attemptsMap[diskRecording.originalPath]?.let { savedAttempts ->
                    diskRecording.copy(attempts = savedAttempts)
                } ?: diskRecording
            }
            _uiState.update { it.copy(recordings = mergedRecordings) }
        }
    }


    fun onCpdTapped() {
        val newTaps = _uiState.value.cpdTaps + 1
        if (newTaps >= 5) {
            _uiState.update { it.copy(showEasterEgg = true, cpdTaps = 0) }
        } else {
            _uiState.update { it.copy(cpdTaps = newTaps) }
        }
    }

    private fun createAudioFile(context: Application, isAttempt: Boolean = false): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir = if (isAttempt) {
            File(context.filesDir, "recordings/attempts")
        } else {
            File(context.filesDir, "recordings")
        }
        storageDir.mkdirs()
        return File(storageDir, "REC_${timeStamp}.wav")
    }

    fun startRecording() {
        // --- ADD THIS GUARD ---
        if (uiState.value.isRecording || recordingJob != null) {
            Log.w("AudioViewModel", "Refusing to start new recording while one is active.")
            return
        }
        // --- END GUARD ---
        _uiState.update { it.copy(isRecording = true, statusText = "Recording...", amplitudes = emptyList()) }
        val file = createAudioFile(getApplication(), isAttempt = false)
        recordingJob = viewModelScope.launch(Dispatchers.IO) {
            repository.startRecording(file) { amplitude ->
                _uiState.update {
                    val newAmplitudes = it.amplitudes + amplitude
                    it.copy(amplitudes = newAmplitudes.takeLast(AudioConstants.MAX_WAVEFORM_SAMPLES))
                }
            }
        }
    }

    fun startAttemptRecording(recording: Recording, challengeType: ChallengeType) {
        // --- ADD THIS GUARD ---
        if (uiState.value.isRecording || recordingJob != null) {
            Log.w("AudioViewModel", "Refusing to start new recording while one is active.")
            return
        }
        // --- END GUARD ---
        _uiState.update {
            it.copy(
                isRecordingAttempt = true,
                parentRecordingPath = recording.originalPath,
                pendingChallengeType = challengeType, // <-- ADD THIS LINE
                isRecording = true,
                statusText = "Recording ${challengeType.name} challenge...", // <-- UPDATE THIS LINE
                amplitudes = emptyList()
            )
        }
        val file = createAudioFile(getApplication(), isAttempt = true)
        recordingJob = viewModelScope.launch(Dispatchers.IO) {
            repository.startRecording(file) { amplitude ->
                _uiState.update {
                    val newAmplitudes = it.amplitudes + amplitude
                    it.copy(amplitudes = newAmplitudes.takeLast(AudioConstants.MAX_WAVEFORM_SAMPLES))
                }
            }
        }
    }

    private fun convertByteArrayToFloatArray(bytes: ByteArray): FloatArray {
        val shorts = ShortArray(bytes.size / 2)
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts)
        val floats = FloatArray(shorts.size)
        for (i in shorts.indices) {
            floats[i] = shorts[i] / 32768.0f
        }
        return floats
    }

    // ADD THE NEW METHOD HERE:
    private suspend fun readAudioFile(filePath: String): FloatArray = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            if (!file.exists() || file.length() < 44) return@withContext floatArrayOf()

            val bytes = file.readBytes()
            val pcmData = bytes.drop(44).toByteArray()
            val samples = ShortArray(pcmData.size / 2)

            for (i in samples.indices) {
                val low = pcmData[i * 2].toInt() and 0xFF
                val high = pcmData[i * 2 + 1].toInt() and 0xFF
                samples[i] = ((high shl 8) or low).toShort()
            }

            // Convert to FloatArray and normalize
            samples.map { it.toFloat() / Short.MAX_VALUE }.toFloatArray()
        } catch (e: Exception) {
            Log.e("AudioViewModel", "Error reading audio file: $filePath", e)
            floatArrayOf()
        }
    }
    //Detect and reject silent originals
    private fun calculateRMS(audio: FloatArray): Float {
        if (audio.isEmpty()) return 0f
        val sumSquares = audio.map { it * it }.sum()
        return kotlin.math.sqrt(sumSquares / audio.size)
    }

    // AudioViewModel.kt

    fun stopRecording() {
        // Cancel the recording job and wait for it to complete
        recordingJob?.cancel()

        viewModelScope.launch(Dispatchers.IO) {
            // Wait for the recording job to actually finish
            recordingJob?.join()
            recordingJob = null

            // Small delay to ensure file is fully written
            delay(100)

            // Get the latest recorded file
            val latestFile = repository.getLatestFile(isAttempt = _uiState.value.isRecordingAttempt)

            Log.d("AudioViewModel", "=== STOP RECORDING ===")
            Log.d("AudioViewModel", "Is attempt: ${_uiState.value.isRecordingAttempt}")
            Log.d("AudioViewModel", "Latest file: ${latestFile?.absolutePath}")

            if (_uiState.value.isRecordingAttempt) {
                // Handle attempt completion (this logic is in a later step)
                withContext(Dispatchers.Main) {
                    handleAttemptCompletion(latestFile)
                }
            } else {
                // --- NEW PARENT RECORDING LOGIC ---
                if (latestFile != null && latestFile.exists()) {
                    val recordingName = "Recording_${SimpleDateFormat("dd-MMM-yyyy_HH-mm-ss", Locale.UK).format(Date())}.wav"

                    // Check original recording quality before processing
                    val originalAudio = readAudioFile(latestFile.absolutePath)
                    val originalRMS = calculateRMS(originalAudio)
                    val qualityThreshold = scoringEngine.getParameters().silenceThreshold * 2f  // 2x the silence threshold from scoring parameters

                    Log.d("AudioViewModel", "Original recording quality check: RMS=$originalRMS, Threshold=$qualityThreshold")

                    val isLowQuality = originalRMS < qualityThreshold

                    // ADD QUALITY CHECK HERE:

                    try {
                        // Step 1: Create reversed version
                        Log.d("AudioViewModel", "Creating reversed version...")
                        val reversedFile = repository.reverseWavFile(latestFile)
                        Log.d("AudioViewModel", "Reversed file created: ${reversedFile?.absolutePath}")

                        // Step 2: Rename original file to final name
                        val recordingsDir = getRecordingsDir(getApplication())
                        val finalFile = File(recordingsDir, recordingName)

                        val renameSuccess = latestFile.renameTo(finalFile)
                        if (!renameSuccess || !finalFile.exists()) {
                            throw IOException("Failed to rename or move original file to final destination.")
                        }

                        // Step 3: Rename reversed file to match
                        val updatedReversedPath = if (reversedFile != null) {
                            val finalReversedFile = File(finalFile.absolutePath.replace(".wav", "_reversed.wav"))
                            if (reversedFile.renameTo(finalReversedFile)) {
                                finalReversedFile.absolutePath
                            } else {
                                reversedFile.absolutePath // Fallback
                            }
                        } else null

                        Log.d("AudioViewModel", "Recording saved: ${finalFile.absolutePath}")
                        Log.d("AudioViewModel", "Reversed recording saved: $updatedReversedPath")

                        // Step 4: Update UI and reload
                        withContext(Dispatchers.Main) {
                            stopPlayback()
                            _uiState.update { currentState ->
                                currentState.copy(
                                    isRecording = false,
                                    statusText = "Recording saved!",
                                    scrollToIndex = 0,
                                    currentlyPlayingPath = null,
                                    isPaused = false,
                                    playbackProgress = 0f,
                                    showQualityWarning = isLowQuality,
                                    qualityWarningMessage = if (isLowQuality) "⚠️ Recording seems quite quiet. Consider re-recording for better challenge quality!" else ""
                                )
                            }
                            delay(200) // Delay for UI update
                            loadRecordings()
                        }
                    } catch (e: Exception) {
                        Log.e("AudioViewModel", "Error saving new recording", e)
                        withContext(Dispatchers.Main) {
                            _uiState.update { it.copy(
                                isRecording = false,
                                statusText = "Error saving recording: ${e.message}"
                            )}
                        }
                    }
                } else {
                    // Handle recording failure
                    withContext(Dispatchers.Main) {
                        _uiState.update { it.copy(
                            isRecording = false,
                            statusText = "Recording failed - no file created"
                        )}
                    }
                }
            }
        }
    }

    private fun handleAttemptCompletion(attemptFile: File?) {
        val parentPath = _uiState.value.parentRecordingPath
        // Get the challenge type we stored when recording started
        val challengeType = _uiState.value.pendingChallengeType

        if (attemptFile != null && parentPath != null && challengeType != null) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    // Create reversed version of attempt
                    Log.d("AudioViewModel", "Creating reversed version of attempt: ${attemptFile.absolutePath}")
                    val reversedAttemptFile = repository.reverseWavFile(attemptFile)
                    Log.d("AudioViewModel", "Reversed attempt file created: ${reversedAttemptFile?.absolutePath}")

                    val parentRecording = _uiState.value.recordings.find { it.originalPath == parentPath }

                    if (parentRecording != null) {
                        //scoringEngine.updateParameters(ScoringParameters()) // Force update  <-- DELETE - or comment out -  THIS LINE (GEMINI PRESET BUG HYPOTHESIS)
                        val score = when (challengeType) {
                            ChallengeType.REVERSE -> {
                                Log.d("AudioViewModel", "REVERSE scoring - parentReversedPath: ${parentRecording.reversedPath}")
                                parentRecording.reversedPath?.let { reversedParentPath ->
                                    try {
                                        val reversedParentAudio = readAudioFile(reversedParentPath)
                                        // NOTE: We compare to the *attempt's* reversed file for REVERSE challenge
                                        val attemptAudioPath = reversedAttemptFile?.absolutePath ?: attemptFile.absolutePath
                                        val attemptAudio = readAudioFile(attemptAudioPath)

                                        if (reversedParentAudio.isNotEmpty() && attemptAudio.isNotEmpty()) {
                                            // Pass challengeType to scoring engine
                                            val result = scoringEngine.scoreAttempt(reversedParentAudio, attemptAudio, ChallengeType.REVERSE)
                                            Log.d("AudioViewModel", "Scoring result: ${result.score}")
                                            result.score
                                        } else {
                                            Log.d("AudioViewModel", "One or both audio arrays are empty for REVERSE score")
                                            0
                                        }
                                    } catch (e: Exception) {
                                        Log.e("AudioViewModel", "Exception in REVERSE scoring: ${e.message}", e)
                                        0
                                    }
                                } ?: run {
                                    Log.d("AudioViewModel", "Parent reversed path is null")
                                    0
                                }
                            }
                            ChallengeType.FORWARD -> {
                                Log.d("AudioViewModel", "FORWARD scoring - parentOriginalPath: ${parentRecording.originalPath}")
                                val originalParentAudio = readAudioFile(parentRecording.originalPath)
                                // NOTE: We compare to the *attempt's* original file for FORWARD challenge
                                val attemptAudio = readAudioFile(attemptFile.absolutePath)

                                if (originalParentAudio.isNotEmpty() && attemptAudio.isNotEmpty()) {
                                    // Pass challengeType to scoring engine
                                    val result = scoringEngine.scoreAttempt(originalParentAudio, attemptAudio, ChallengeType.FORWARD)
                                    Log.d("AudioViewModel", "Scoring result: ${result.score}")
                                    result.score
                                } else {
                                    Log.d("AudioViewModel", "One or both audio arrays are empty for FORWARD score")
                                    0
                                }
                            }
                        }

                        // Create player attempt WITH the challengeType
                        val attempt = PlayerAttempt(
                            playerName = "Player ${parentRecording.attempts.size + 1}",
                            attemptFilePath = attemptFile.absolutePath,
                            reversedAttemptFilePath = reversedAttemptFile?.absolutePath,
                            score = score,
                            challengeType = challengeType // <-- SAVE THE TYPE
                        )

                        Log.d("AudioViewModel", "Created attempt with reversedPath: ${attempt.reversedAttemptFilePath}")

                        val updatedRecordings = _uiState.value.recordings.map { recording ->
                            if (recording.originalPath == parentPath) {
                                recording.copy(attempts = recording.attempts + attempt)
                            } else {
                                recording
                            }
                        }

                        val attemptsMap = updatedRecordings.associate { it.originalPath to it.attempts }.filterValues { it.isNotEmpty() }
                        attemptsRepository.saveAttempts(attemptsMap)

                        val parentIndex = updatedRecordings.indexOfFirst { it.originalPath == parentPath }
                        val scrollToIndex = if (parentIndex >= 0) {
                            parentIndex + updatedRecordings[parentIndex].attempts.size
                        } else null

                        _uiState.update {
                            it.copy(
                                recordings = updatedRecordings,
                                isRecording = false,
                                isRecordingAttempt = false,
                                parentRecordingPath = null,
                                pendingChallengeType = null, // <-- Clear pending type
                                statusText = "Attempt scored: ${score}%",
                                scrollToIndex = scrollToIndex,
                                attemptToRename = if (score > 70) Pair(parentPath, attempt) else null
                            )
                        }
                    }
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            isRecording = false,
                            isRecordingAttempt = false,
                            parentRecordingPath = null,
                            pendingChallengeType = null, // <-- Clear pending type
                            statusText = "Error processing attempt: ${e.message}"
                        )
                    }
                }
            }
        } else {
            _uiState.update {
                it.copy(
                    isRecording = false,
                    isRecordingAttempt = false,
                    parentRecordingPath = null,
                    pendingChallengeType = null, // <-- Clear pending type
                    statusText = "Attempt recording failed"
                )
            }
        }
    }

    fun play(path: String) {
        mediaPlayer?.release()
        playbackJob?.cancel()
        _uiState.update { it.copy(isPaused = false, playbackProgress = 0f) }

        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(path)
                prepare()
                start()
                _uiState.update { it.copy(currentlyPlayingPath = path) }
                setOnCompletionListener { stopPlayback() }
                playbackJob = viewModelScope.launch {
                    while (isActive) {
                        val currentPos = currentPosition.toFloat()
                        val totalDuration = duration.toFloat()
                        if (totalDuration > 0) {
                            _uiState.update { it.copy(playbackProgress = currentPos / totalDuration) }
                        }
                        delay(100)
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(statusText = "Error: Could not play file.") }
            }
        }
    }

    fun pause() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
            playbackJob?.cancel()
            _uiState.update { it.copy(isPaused = true) }
        } else if (mediaPlayer != null && _uiState.value.isPaused) {
            mediaPlayer?.start()
            _uiState.update { it.copy(isPaused = false) }
            playbackJob = viewModelScope.launch {
                while (isActive) {
                    val currentPos = mediaPlayer?.currentPosition?.toFloat() ?: 0f
                    val totalDuration = mediaPlayer?.duration?.toFloat() ?: 1f
                    if (totalDuration > 0) {
                        _uiState.update { it.copy(playbackProgress = currentPos / totalDuration) }
                    }
                    delay(100)
                }
            }
        }
    }

    fun stopPlayback() {
        mediaPlayer?.release()
        mediaPlayer = null
        playbackJob?.cancel()
        _uiState.update { it.copy(currentlyPlayingPath = null, isPaused = false, playbackProgress = 0f) }
    }

    fun deleteRecording(recording: Recording) {
        viewModelScope.launch {
            repository.deleteRecording(recording.originalPath, recording.reversedPath)
            recording.attempts.forEach { attempt ->
                try { File(attempt.attemptFilePath).delete() } catch (e: Exception) { Log.e("AudioViewModel", "Error deleting attempt file", e) }
                attempt.reversedAttemptFilePath?.let {
                    try { File(it).delete() } catch (e: Exception) { Log.e("AudioViewModel", "Error deleting reversed attempt file", e) }
                }
            }
            loadRecordings()
        }
    }

    fun clearAllRecordings() {
        viewModelScope.launch {
            repository.clearAllRecordings()
            val attemptsDir = File(getApplication<Application>().filesDir, "recordings/attempts")
            attemptsDir.listFiles()?.forEach { it.delete() }
            attemptsRepository.saveAttempts(emptyMap())
            loadRecordings()
        }
    }

    fun renameRecording(oldPath: String, newName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val oldFile = File(oldPath)
            val newPath = File(oldFile.parent, newName).absolutePath
            val success = repository.renameRecording(oldPath, newName)
            if (success) {
                val attemptsMap = attemptsRepository.loadAttempts().toMutableMap()
                attemptsMap[oldPath]?.let { attempts ->
                    attemptsMap.remove(oldPath)
                    attemptsMap[newPath] = attempts
                    attemptsRepository.saveAttempts(attemptsMap)
                }
            }
            loadRecordings()
        }
    }

    fun dismissEasterEgg() {
        _uiState.update { it.copy(showEasterEgg = false) }
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.release()
    }

    fun renamePlayer(parentRecordingPath: String, oldAttempt: PlayerAttempt, newPlayerName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedRecordings = _uiState.value.recordings.map { recording ->
                if (recording.originalPath == parentRecordingPath) {
                    recording.copy(attempts = recording.attempts.map { attempt ->
                        if (attempt.attemptFilePath == oldAttempt.attemptFilePath) {
                            attempt.copy(playerName = newPlayerName)
                        } else {
                            attempt
                        }
                    })
                } else {
                    recording
                }
            }
            val attemptsMap = updatedRecordings.associate { it.originalPath to it.attempts }.filterValues { it.isNotEmpty() }
            attemptsRepository.saveAttempts(attemptsMap)
            _uiState.update { it.copy(recordings = updatedRecordings) }
        }
    }

    fun deleteAttempt(parentRecordingPath: String, attemptToDelete: PlayerAttempt) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                File(attemptToDelete.attemptFilePath).delete()
                attemptToDelete.reversedAttemptFilePath?.let { File(it).delete() }
            } catch (e: Exception) {
                Log.e("AudioViewModel", "Error deleting attempt files", e)
            }
            val updatedRecordings = _uiState.value.recordings.map { recording ->
                if (recording.originalPath == parentRecordingPath) {
                    recording.copy(attempts = recording.attempts.filter { it.attemptFilePath != attemptToDelete.attemptFilePath })
                } else {
                    recording
                }
            }
            val attemptsMap = updatedRecordings.associate { it.originalPath to it.attempts }.filterValues { it.isNotEmpty() }
            attemptsRepository.saveAttempts(attemptsMap)
            _uiState.update { it.copy(recordings = updatedRecordings) }
        }
    }

    fun clearAttemptToRename() {
        _uiState.update { it.copy(attemptToRename = null) }
    }

    fun completeTutorial() {
        viewModelScope.launch {
            settingsDataStore.setTutorialCompleted(true)
            _uiState.update { it.copy(showTutorial = false) }
        }
    }

    fun dismissTutorial() {
        viewModelScope.launch {
            settingsDataStore.setTutorialCompleted(true)
            _uiState.update { it.copy(showTutorial = false) }
        }
    }

    fun clearScrollToIndex() {
        _uiState.update { it.copy(scrollToIndex = null) }
    }

    fun dismissQualityWarning() {
        _uiState.update {
            it.copy(
                showQualityWarning = false,
                qualityWarningMessage = ""
            )
        }
    }

    // Sync scoring parameters from settings
    fun updateScoringEngine(preset: com.example.reversey.scoring.Presets) {
        scoringEngine.applyPreset(preset)
    }
}
