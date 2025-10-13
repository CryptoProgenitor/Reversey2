package com.example.reversey

import android.app.Application
import android.content.Context
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
    val amplitudes: List<Float> = emptyList()
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

    fun stopRecording() {
        _uiState.update { it.copy(isRecording = false, statusText = "Processing...") }
        viewModelScope.launch(Dispatchers.IO) {
            recordingJob?.cancel()
            val lastRecording = repository.getLatestFile()
            val reversedFile = repository.reverseWavFile(lastRecording)
            val newStatus = if (reversedFile != null) "Reversed successfully!" else "Error: Reversing failed."
            _uiState.update { it.copy(statusText = newStatus) }
            loadRecordings()
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

    fun playEasterEgg(context: Context) {
        val mediaPlayer = MediaPlayer.create(context, R.raw.sad_trombone)
        mediaPlayer.setOnCompletionListener { mp -> mp.release() }
        mediaPlayer.start()
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.release()
    }
}

