package com.example.reversey

import android.app.Application
import android.media.MediaPlayer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
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
    val showTutorial: Boolean = false,  // ← ADD THIS LINE
    val cpdTaps: Int = 0, // <-- ADD THIS NEW STATE
    val isRecordingAttempt: Boolean = false, // Are we currently recording a player's attempt?
    val parentRecordingPath: String? = null, // Which original recording are we making an attempt for?
    val attemptToRename: Pair<String, PlayerAttempt>? = null,
    val scrollToIndex: Int? = null  // ← ADD THIS LINE
)


class AudioViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = RecordingRepository(application)
    private val attemptsRepository = AttemptsRepository(application)
    private var mediaPlayer: MediaPlayer? = null
    private var recordingJob: Job? = null
    private var playbackJob: Job? = null

    private val _uiState = MutableStateFlow(AudioUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadRecordings()

        // Check if tutorial should be shown
        viewModelScope.launch {
            val settingsDataStore = SettingsDataStore(getApplication())
            settingsDataStore.tutorialCompleted.collect { completed ->
                if (!completed) {
                    _uiState.update { it.copy(showTutorial = true) }
                }
            }
        }
    }

    //claude's loadRecordings
    fun loadRecordings() {
        viewModelScope.launch(Dispatchers.IO) {
            // 1. Load recordings from disk
            val loadedRecordingsFromDisk = repository.loadRecordings()

            // 2. Load attempts from JSON
            val attemptsMap = attemptsRepository.loadAttempts()

            // 3. Merge the disk data with the saved attempts
            val mergedRecordings = loadedRecordingsFromDisk.map { diskRecording ->
                val savedAttempts = attemptsMap[diskRecording.originalPath]
                if (savedAttempts != null && savedAttempts.isNotEmpty()) {
                    diskRecording.copy(attempts = savedAttempts)
                } else {
                    diskRecording
                }
            }

            // 4. Update the UI with the final, merged list
            _uiState.update { it.copy(recordings = mergedRecordings) }
        }
    }


    fun onCpdTapped() {
        val newTaps = _uiState.value.cpdTaps + 1
        if (newTaps >= 5) {
            // If we've reached 5 taps, show the egg and immediately reset the counter
            _uiState.update { it.copy(showEasterEgg = true, cpdTaps = 0) }
        } else {
            // Otherwise, just increment the counter
            _uiState.update { it.copy(cpdTaps = newTaps) }
        }
    }

    // This is the NEW, COMPLETE function to paste

    // In AudioViewModel.kt

    private fun createAudioFile(context: Application, isAttempt: Boolean = false): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir = if (isAttempt) {
            File(context.filesDir, "recordings/attempts")  // Remove parent= and child=
        } else {
            File(context.filesDir, "recordings")  // Remove parent= and child=
        }
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        return File(storageDir, "REC_${timeStamp}.wav")  // Remove parent= and child=
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

    // REPLACE your old stopRecording function with this one
    // Update stopRecording() in AudioViewModel to reverse attempts:
    //2. Update stopRecording() in AudioViewModel.kt to reverse attempts:
    //Replace your stopRecording() function with this:
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
                // This was a GAME ATTEMPT recording
                val reversedAttemptFile = repository.reverseWavFile(lastRecordingFile)

                val parentRecording = _uiState.value.recordings.find { it.originalPath == parentPath }
                val score = if (parentRecording?.reversedPath != null) {
                    val parentReversedFile = File(parentRecording.reversedPath)
                    val comparer = AudioComparer()
                    comparer.compareAudioFiles(parentReversedFile, lastRecordingFile)
                } else {
                    0
                }

                val existingPlayerNumbers = parentRecording?.attempts
                    ?.mapNotNull { it.playerName.removePrefix("Player ").toIntOrNull() }
                    ?.maxOrNull() ?: 0
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

                val attemptsMap = updatedRecordings.associate {
                    it.originalPath to it.attempts
                }.filterValues { it.isNotEmpty() }
                attemptsRepository.saveAttempts(attemptsMap)

                // Calculate scroll index for the new attempt
                var scrollIndex = 0
                for (recording in updatedRecordings) {
                    scrollIndex++ // Count the parent item
                    if (recording.originalPath == parentPath) {
                        // This is our parent, the new attempt is at the end of its attempts list
                        scrollIndex += recording.attempts.size - 1
                        break
                    }
                    scrollIndex += recording.attempts.size // Count all attempts for other parents
                }

                _uiState.update {
                    it.copy(
                        recordings = updatedRecordings,
                        statusText = "Attempt Saved!",
                        attemptToRename = Pair(parentPath, newAttempt),
                        scrollToIndex = scrollIndex  // ADD THIS
                    )
                }

            } else {
                // This was a NORMAL recording
                val reversedFile = repository.reverseWavFile(lastRecordingFile)
                val newStatus = if (reversedFile != null) "Reversed successfully!" else "Error: Reversing failed."

                loadRecordings()

                // Scroll to top (index 0) for new parent recording
                _uiState.update {
                    it.copy(
                        statusText = newStatus,
                        scrollToIndex = 0  // ADD THIS
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
                setOnCompletionListener {
                    stopPlayback()
                }
                playbackJob = viewModelScope.launch {
                    while (isActive) {
                        val currentPos = currentPosition.toFloat()
                        val totalDuration = duration.toFloat()
                        val progress = if (totalDuration > 0) currentPos / totalDuration else 0f
                        _uiState.update { it.copy(playbackProgress = progress) }
                        delay(100)
                    }
                }
            } catch (_: Exception) {
                _uiState.update { it.copy(statusText = "Error: Could not play file.") }
            }
        }
    }

    fun pause() {
        if (mediaPlayer?.isPlaying == true) {
            // Pause playback
            mediaPlayer?.pause()
            playbackJob?.cancel()
            _uiState.update { it.copy(isPaused = true) }
        } else if (mediaPlayer != null && _uiState.value.isPaused) {
            // Resume playback from current position
            mediaPlayer?.start()
            _uiState.update { it.copy(isPaused = false) }

            // Restart progress tracking job
            playbackJob = viewModelScope.launch {
                while (isActive) {
                    val currentPos = mediaPlayer?.currentPosition?.toFloat() ?: 0f
                    val totalDuration = mediaPlayer?.duration?.toFloat() ?: 1f
                    val progress = if (totalDuration > 0) currentPos / totalDuration else 0f
                    _uiState.update { it.copy(playbackProgress = progress) }
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

            // Delete attempt files
            recording.attempts.forEach { attempt ->
                try {
                    File(attempt.attemptFilePath).delete()
                } catch (e: Exception) {
                    android.util.Log.e("AudioViewModel", "Error deleting attempt file", e)
                }
            }

            loadRecordings()
        }
    }

    fun clearAllRecordings() {
        viewModelScope.launch {
            repository.clearAllRecordings()

            // Clear attempts directory
            val attemptsDir = File(getApplication<Application>().filesDir, "recordings/attempts")
            attemptsDir.listFiles()?.forEach { it.delete() }

            // Clear attempts JSON
            attemptsRepository.saveAttempts(emptyMap())

            loadRecordings()
        }
    }

    fun renameRecording(oldPath: String, newName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            // Get the new path after renaming
            val oldFile = File(oldPath)
            val newPath = File(oldFile.parent, newName).absolutePath

            // Rename the file
            val success = repository.renameRecording(oldPath, newName)

            if (success) {
                // Update attempts map with new parent path
                val attemptsMap = attemptsRepository.loadAttempts().toMutableMap()
                val attempts = attemptsMap[oldPath]

                if (attempts != null) {
                    // Remove old key and add with new key
                    attemptsMap.remove(oldPath)
                    attemptsMap[newPath] = attempts

                    // Save updated map
                    attemptsRepository.saveAttempts(attemptsMap)
                }
            }

            loadRecordings()
        }
    }

    // This function will be called to hide it-
    fun dismissEasterEgg() {
        _uiState.update { it.copy(showEasterEgg = false) }
    }
    // The 'override' function was outside the class, move it inside.
    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.release()
    }

    //Now add a new function (from 2.0.2.e) to your AudioViewModel to handle renaming:
    fun renamePlayer(parentRecordingPath: String, oldAttempt: PlayerAttempt, newPlayerName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            // Update the attempt with the new player name
            val updatedAttempt = oldAttempt.copy(playerName = newPlayerName)

            // Update the recordings list
            val updatedRecordings = _uiState.value.recordings.map { recording ->
                if (recording.originalPath == parentRecordingPath) {
                    val updatedAttempts = recording.attempts.map { attempt ->
                        if (attempt.attemptFilePath == oldAttempt.attemptFilePath) {
                            updatedAttempt
                        } else {
                            attempt
                        }
                    }
                    recording.copy(attempts = updatedAttempts)
                } else {
                    recording
                }
            }

            // Save to JSON
            val attemptsMap = updatedRecordings.associate {
                it.originalPath to it.attempts
            }.filterValues { it.isNotEmpty() }
            attemptsRepository.saveAttempts(attemptsMap)

            // Update UI
            _uiState.update { it.copy(recordings = updatedRecordings) }
        }
    }

    fun deleteAttempt(parentRecordingPath: String, attemptToDelete: PlayerAttempt) {
        viewModelScope.launch(Dispatchers.IO) {
            // Delete the attempt files from disk
            try {
                File(attemptToDelete.attemptFilePath).delete()
                attemptToDelete.reversedAttemptFilePath?.let { reversedPath ->
                    File(reversedPath).delete()
                }
            } catch (e: Exception) {
                android.util.Log.e("AudioViewModel", "Error deleting attempt files", e)
            }

            // Update the recordings list by removing this attempt
            val updatedRecordings = _uiState.value.recordings.map { recording ->
                if (recording.originalPath == parentRecordingPath) {
                    val updatedAttempts = recording.attempts.filter {
                        it.attemptFilePath != attemptToDelete.attemptFilePath
                    }
                    recording.copy(attempts = updatedAttempts)
                } else {
                    recording
                }
            }

            // Save updated attempts to JSON
            val attemptsMap = updatedRecordings.associate {
                it.originalPath to it.attempts
            }.filterValues { it.isNotEmpty() }
            attemptsRepository.saveAttempts(attemptsMap)

            // Update UI
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

} // This is the closing brace for the AudioViewModel class
