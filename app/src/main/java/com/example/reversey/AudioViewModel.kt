package com.example.reversey

import android.app.Application
import android.media.MediaPlayer
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.reversey.scoring.ScoringEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


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
    val scrollToIndex: Int? = null
)


class AudioViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = RecordingRepository(application)
    private val attemptsRepository = AttemptsRepository(application)
    private val scoringEngine = ScoringEngine(application)
    private var mediaPlayer: MediaPlayer? = null
    private var recordingJob: Job? = null
    private var playbackJob: Job? = null

    private val _uiState = MutableStateFlow(AudioUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadRecordings()
        viewModelScope.launch {
            val settingsDataStore = SettingsDataStore(getApplication())
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
            val mergedRecordings = loadedRecordingsFromDisk.map { diskRecording ->
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

    fun startAttemptRecording(recording: Recording) {
        _uiState.update {
            it.copy(
                isRecordingAttempt = true,
                parentRecordingPath = recording.originalPath,
                isRecording = true,
                statusText = "Recording attempt...",
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

    fun stopRecording() {
        val wasRecordingAttempt = _uiState.value.isRecordingAttempt
        val parentPath = _uiState.value.parentRecordingPath

        _uiState.update {
            it.copy(
                isRecording = false,
                isRecordingAttempt = false,
                parentRecordingPath = null,
                statusText = "Processing..."
            )
        }

        viewModelScope.launch(Dispatchers.IO) {
            recordingJob?.cancel()
            recordingJob?.join()
            delay(200)

            val lastRecordingFile = repository.getLatestFile(isAttempt = wasRecordingAttempt)
            if (lastRecordingFile == null) {
                _uiState.update { it.copy(statusText = "Error: No recording file found.") }
                return@launch
            }

            if (wasRecordingAttempt && parentPath != null) {
                // --- ADDED SAFETY "CIRCUIT BREAKER" ---
                try {
                    val reversedAttemptFile = repository.reverseWavFile(lastRecordingFile)
                    val parentRecording = _uiState.value.recordings.find { it.originalPath == parentPath }

                    val score = if (parentRecording?.reversedPath != null) {
                        val parentReversedFile = File(parentRecording.reversedPath)
                        if (parentReversedFile.exists() && lastRecordingFile.exists()) {
                            val originalAudioBytes = parentReversedFile.readBytes().drop(44).toByteArray()
                            val attemptAudioBytes = lastRecordingFile.readBytes().drop(44).toByteArray()
                            val originalAudioFloats = convertByteArrayToFloatArray(originalAudioBytes)
                            val attemptAudioFloats = convertByteArrayToFloatArray(attemptAudioBytes)
                            scoringEngine.scoreAttempt(
                                reversedOriginal = originalAudioFloats,
                                playerAttempt = attemptAudioFloats
                            ).score
                        } else { 0 }
                    } else { 0 }

                    val existingPlayerNumbers = parentRecording?.attempts?.mapNotNull { it.playerName.removePrefix("Player ").toIntOrNull() }?.maxOrNull() ?: 0
                    val nextPlayerNumber = existingPlayerNumbers + 1
                    val newAttempt = PlayerAttempt(
                        playerName = "Player $nextPlayerNumber",
                        attemptFilePath = lastRecordingFile.absolutePath,
                        reversedAttemptFilePath = reversedAttemptFile?.absolutePath,
                        score = score
                    )

                    val updatedRecordings = _uiState.value.recordings.map { recording ->
                        if (recording.originalPath == parentPath) {
                            recording.copy(attempts = recording.attempts + newAttempt)
                        } else {
                            recording
                        }
                    }

                    val attemptsMap = updatedRecordings.associate { it.originalPath to it.attempts }.filterValues { it.isNotEmpty() }
                    attemptsRepository.saveAttempts(attemptsMap)

                    var scrollIndex = updatedRecordings.indexOfFirst { it.originalPath == parentPath }
                        .takeIf { it != -1 }
                        ?.let { parentIndex ->
                            updatedRecordings.take(parentIndex).sumOf { 1 + it.attempts.size } + 1 + (updatedRecordings.getOrNull(parentIndex)?.attempts?.size ?: 0) -1
                        }

                    _uiState.update {
                        it.copy(
                            recordings = updatedRecordings,
                            statusText = "Attempt Saved! Score: $score%",
                            attemptToRename = Pair(parentPath, newAttempt),
                            scrollToIndex = scrollIndex
                        )
                    }
                } catch (e: Exception) {
                    Log.e("AudioViewModel", "Scoring failed", e)
                    _uiState.update { it.copy(statusText = "Error: Scoring failed. See logs.") }
                }
                // --- END OF SAFETY "CIRCUIT BREAKER" ---
            } else {
                val reversedFile = repository.reverseWavFile(lastRecordingFile)
                val newStatus = if (reversedFile != null) "Reversed successfully!" else "Error: Reversing failed."
                loadRecordings()
                _uiState.update {
                    it.copy(
                        statusText = newStatus,
                        scrollToIndex = 0
                    )
                }
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
            val settingsDataStore = SettingsDataStore(getApplication())
            settingsDataStore.setTutorialCompleted(true)
            _uiState.update { it.copy(showTutorial = false) }
        }
    }

    fun dismissTutorial() {
        viewModelScope.launch {
            val settingsDataStore = SettingsDataStore(getApplication())
            settingsDataStore.setTutorialCompleted(true)
            _uiState.update { it.copy(showTutorial = false) }
        }
    }

    fun clearScrollToIndex() {
        _uiState.update { it.copy(scrollToIndex = null) }
    }

}
