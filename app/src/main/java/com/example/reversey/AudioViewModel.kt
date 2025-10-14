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

data class AudioUiState(
    val recordings: List<Recording> = emptyList(),
    val isRecording: Boolean = false,
    val statusText: String = "Ready to record",
    val currentlyPlayingPath: String? = null,
    val playbackProgress: Float = 0f,
    val isPaused: Boolean = false,
    val amplitudes: List<Float> = emptyList(),
    val showEasterEgg: Boolean = false,
    val cpdTaps: Int = 0, // <-- ADD THIS NEW STATE
    val isRecordingAttempt: Boolean = false, // Are we currently recording a player's attempt?
    val parentRecordingPath: String? = null // Which original recording are we making an attempt for?
)


class AudioViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = RecordingRepository(application)
    private var mediaPlayer: MediaPlayer? = null
    private var recordingJob: Job? = null
    private var playbackJob: Job? = null

    private val _uiState = MutableStateFlow(AudioUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadRecordings()
    }

    fun loadRecordings() {
        viewModelScope.launch {
            val newRecordings = repository.loadRecordings()
            _uiState.update { it.copy(recordings = newRecordings) }
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
    fun startAttemptRecording(parentRecording: Recording) {
        if (_uiState.value.isRecording || _uiState.value.isRecordingAttempt) return

        // Update the state to show we're recording an attempt
        _uiState.update {
            it.copy(
                isRecordingAttempt = true,
                parentRecordingPath = parentRecording.originalPath,
                isRecording = true, // Also set the main recording flag
                statusText = "Get ready to sing...",
                amplitudes = emptyList()
            )
        }

        // We can reuse the existing startRecording logic!
        val file = createAudioFile(getApplication())
        recordingJob = viewModelScope.launch(Dispatchers.IO) {
            repository.startRecording(file) { amplitude ->
                _uiState.update {
                    val newAmplitudes = it.amplitudes + amplitude
                    it.copy(amplitudes = newAmplitudes.takeLast(AudioConstants.MAX_WAVEFORM_SAMPLES))
                }
            }
        }
    }


    fun startRecording() {
        _uiState.update { it.copy(isRecording = true, statusText = "Recording...", amplitudes = emptyList()) }
        val file = createAudioFile(getApplication())
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
    fun stopRecording() {
        // First, check if this was a game attempt BEFORE we update the state
        val wasRecordingAttempt = _uiState.value.isRecordingAttempt
        val parentPath = _uiState.value.parentRecordingPath

        // Update UI immediately to stop showing the recording state
        _uiState.update {
            it.copy(
                isRecording = false,
                isRecordingAttempt = false,
                parentRecordingPath = null,
                statusText = "Processing..."
            )
        }

        viewModelScope.launch(Dispatchers.IO) {
            // Stop the recording job and get the most recent file
            recordingJob?.cancel()
            val lastRecordingFile = repository.getLatestFile() ?: return@launch

            if (wasRecordingAttempt && parentPath != null) {
                // This was a GAME ATTEMPT recording.
                val newAttempt = PlayerAttempt(
                    playerName = "Player 1",
                    attemptFilePath = lastRecordingFile.absolutePath,
                    score = 0 // We will calculate this later
                )

                // Find the parent recording in the current state and add the new attempt to its list
                val updatedRecordings = _uiState.value.recordings.map { recording ->
                    if (recording.originalPath == parentPath) {
                        recording.copy(attempts = recording.attempts + newAttempt)
                    } else {
                        recording
                    }
                }
                // Update the UI with the new list of recordings and a confirmation status
                // IMPORTANT: WE DO NOT CALL loadRecordings() HERE.
                _uiState.update {
                    it.copy(
                        recordings = updatedRecordings,
                        statusText = "Attempt Saved!"
                    )
                }

            } else {
                // This was a NORMAL recording, so we reverse it, just like before.
                val reversedFile = repository.reverseWavFile(lastRecordingFile)
                val newStatus =
                    if (reversedFile != null) "Reversed successfully!" else "Error: Reversing failed."
                _uiState.update { it.copy(statusText = newStatus) }
                // For a normal recording, we DO reload from disk to get the new file.
                loadRecordings()
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
            mediaPlayer?.pause()
            playbackJob?.cancel()
            _uiState.update { it.copy(isPaused = true) }
        } else {
            mediaPlayer?.start()
            _uiState.update { it.copy(isPaused = false) }
            // Resume progress tracking
            play(_uiState.value.currentlyPlayingPath ?: return)
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
            loadRecordings()
        }
    }

    fun clearAllRecordings() {
        viewModelScope.launch {
            repository.clearAllRecordings()
            loadRecordings()
        }
    }

    fun renameRecording(oldPath: String, newName: String) {
        viewModelScope.launch {
            repository.renameRecording(oldPath, newName)
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

} // This is the closing brace for the AudioViewModel class