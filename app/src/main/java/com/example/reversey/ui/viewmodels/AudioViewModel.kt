package com.example.reversey.ui.viewmodels

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.reversey.AudioConstants
import com.example.reversey.data.models.ChallengeType
import com.example.reversey.data.models.PlayerAttempt
import com.example.reversey.data.models.Recording
import com.example.reversey.data.repositories.AttemptsRepository
import com.example.reversey.data.repositories.RecordingRepository
import com.example.reversey.data.repositories.SettingsDataStore
import com.example.reversey.scoring.Presets
import com.example.reversey.scoring.ScoringEngine
import com.example.reversey.scoring.ScoringResult
import com.example.reversey.scoring.SimilarityMetrics
import dagger.hilt.android.lifecycle.HiltViewModel
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
import javax.inject.Inject
import kotlin.math.sqrt

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

    // SURGICAL FIX #1: Add error message helper
    private fun showUserMessage(message: String) {
        _uiState.update { it.copy(statusText = message) }
    }

    // SURGICAL FIX #2: Add file validation helper
    private fun validateRecordedFile(file: File?): Boolean {
        return file != null && file.exists() && file.length() > 100
    }

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
        //val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date()) //manky old format!
        val now = Date()
        val timeFormat = SimpleDateFormat("h-mm-ssa", Locale.US).format(now).lowercase()
        val dateFormat = SimpleDateFormat("dMMMYY", Locale.US).format(now) // "2Nov24"
        val humanTimeStamp = "Rec-${timeFormat}-${dateFormat}" // // Result: Rec 4-03-11pm 2Nov25.wav
        val storageDir = if (isAttempt) {
            File(context.filesDir, "recordings/attempts")
        } else {
            File(context.filesDir, "recordings")
        }
        storageDir.mkdirs()
        return File(storageDir, "${humanTimeStamp}.wav")
    }

    // SURGICAL FIX #3: Improved startRecording with user feedback and permission checking
    fun startRecording() {
        // Check if already recording
        if (uiState.value.isRecording || recordingJob != null) {
            showUserMessage("Recording already in progress")
            return
        }

        // Check permissions
        if (ActivityCompat.checkSelfPermission(getApplication(), Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            showUserMessage("Microphone permission is required to record")
            return
        }

        _uiState.update { it.copy(isRecording = true, statusText = "Recording...", amplitudes = emptyList()) }
        val file = createAudioFile(getApplication(), isAttempt = false)
        recordingJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.startRecording(file) { amplitude ->
                    _uiState.update {
                        val newAmplitudes = it.amplitudes + amplitude
                        it.copy(amplitudes = newAmplitudes.takeLast(AudioConstants.MAX_WAVEFORM_SAMPLES))
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showUserMessage("Recording failed: ${e.message}")
                    _uiState.update { it.copy(isRecording = false) }
                }
            }
        }
    }

    // SURGICAL FIX #4: Improved startAttemptRecording with user feedback and permission checking
    fun startAttemptRecording(recording: Recording, challengeType: ChallengeType) {
        // Check if already recording
        if (uiState.value.isRecording || recordingJob != null) {
            showUserMessage("Recording already in progress")
            return
        }

        // Check permissions
        if (ActivityCompat.checkSelfPermission(getApplication(), Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            showUserMessage("Microphone permission is required to record")
            return
        }

        _uiState.update {
            it.copy(
                isRecordingAttempt = true,
                parentRecordingPath = recording.originalPath,
                pendingChallengeType = challengeType,
                isRecording = true,
                statusText = "Recording ${challengeType.name} challenge...",
                amplitudes = emptyList()
            )
        }
        val file = createAudioFile(getApplication(), isAttempt = true)
        recordingJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.startRecording(file) { amplitude ->
                    _uiState.update {
                        val newAmplitudes = it.amplitudes + amplitude
                        it.copy(amplitudes = newAmplitudes.takeLast(AudioConstants.MAX_WAVEFORM_SAMPLES))
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showUserMessage("Recording failed: ${e.message}")
                    _uiState.update {
                        it.copy(
                            isRecording = false,
                            isRecordingAttempt = false,
                            parentRecordingPath = null,
                            pendingChallengeType = null
                        )
                    }
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

    // SURGICAL FIX #5: Improved readAudioFile with retry logic
    private suspend fun readAudioFile(filePath: String): FloatArray = withContext(Dispatchers.IO) {
        // Retry logic for file operations
        repeat(2) { attempt ->
            try {
                delay(if (attempt == 0) 0 else 100) // Small delay on retry

                val file = File(filePath)
                if (!file.exists() || file.length() < 44) {
                    if (attempt == 0) return@repeat // Retry once
                    return@withContext floatArrayOf()
                }

                val bytes = file.readBytes()
                val pcmData = bytes.drop(44).toByteArray()
                if (pcmData.isEmpty()) {
                    if (attempt == 0) return@repeat // Retry once
                    return@withContext floatArrayOf()
                }

                val samples = ShortArray(pcmData.size / 2)

                for (i in samples.indices) {
                    val low = pcmData[i * 2].toInt() and 0xFF
                    val high = pcmData[i * 2 + 1].toInt() and 0xFF
                    samples[i] = ((high shl 8) or low).toShort()
                }

                // Convert to FloatArray and normalize
                return@withContext samples.map { it.toFloat() / Short.MAX_VALUE }.toFloatArray()
            } catch (e: Exception) {
                Log.e(
                    "AudioViewModel",
                    "Error reading audio file (attempt ${attempt + 1}): $filePath",
                    e
                )
                if (attempt == 1) return@withContext floatArrayOf() // Last attempt failed
            }
        }
        return@withContext floatArrayOf()
    }

    //Detect and reject silent originals
    private fun calculateRMS(audio: FloatArray): Float {
        if (audio.isEmpty()) return 0f
        val sumSquares = audio.map { it * it }.sum()
        return sqrt(sumSquares / audio.size)
    }

    // Helper function for getting recordings directory
    private fun getRecordingsDir(context: Application): File {
        return File(context.filesDir, "recordings").apply { mkdirs() }
    }

    // SURGICAL FIX #6: Improved stopRecording with better file validation
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
                // IMPROVED PARENT RECORDING LOGIC with validation
                if (validateRecordedFile(latestFile)) {
                    val now = Date()
                    val timeFormat = SimpleDateFormat("h-mm-ssa", Locale.US).format(now).lowercase()
                    val dateFormat = SimpleDateFormat("dMMMYY", Locale.US).format(now)
                    val recordingName = "Rec ${timeFormat} ${dateFormat}.wav" // Result: Rec 4-03-11pm 2Nov25.wav



                    // Check original recording quality before processing
                    val originalAudio = readAudioFile(latestFile!!.absolutePath)
                    val originalRMS = calculateRMS(originalAudio)
                    val qualityThreshold = scoringEngine.getParameters().silenceThreshold * 2f

                    Log.d("AudioViewModel", "Original recording quality check: RMS=$originalRMS, Threshold=$qualityThreshold")

                    val isLowQuality = originalRMS < qualityThreshold

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
                            val finalReversedFile =
                                File(finalFile.absolutePath.replace(".wav", "_reversed.wav"))
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
                                    statusText = if (isLowQuality) "Recording saved (⚠️ quality warning)" else "Recording saved!",
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
                            showUserMessage("Error saving recording: ${e.message}")
                            _uiState.update { it.copy(isRecording = false) }
                        }
                    }
                } else {
                    // Handle recording validation failure
                    withContext(Dispatchers.Main) {
                        showUserMessage("Recording failed - file was not created properly. Please try again.")
                        _uiState.update { it.copy(isRecording = false) }
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
                    //FROM HERE
                    if (parentRecording != null) {
                        //scoringEngine.updateParameters(ScoringParameters()) // Force update  <-- DELETE - or comment out -  THIS LINE (GEMINI PRESET BUG HYPOTHESIS)
                        val scoringResult = when (challengeType) {
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
                                            Log.d("AudioViewModel", "Scoring result: ${result.score}, pitch: ${result.metrics.pitch}, mfcc: ${result.metrics.mfcc}")
                                            result
                                        } else {
                                            Log.d("AudioViewModel", "One or both audio arrays are empty for REVERSE score")
                                            ScoringResult(
                                                score = 0,
                                                rawScore = 0f,
                                                metrics = SimilarityMetrics(pitch = 0f, mfcc = 0f),
                                                feedback = emptyList()
                                            )
                                        }
                                    } catch (e: Exception) {
                                        Log.e("AudioViewModel", "Exception in REVERSE scoring: ${e.message}", e)
                                        ScoringResult(
                                            score = 0,
                                            rawScore = 0f,
                                            metrics = SimilarityMetrics(pitch = 0f, mfcc = 0f),
                                            feedback = emptyList()
                                        )
                                    }
                                } ?: run {
                                    Log.d("AudioViewModel", "Parent reversed path is null")
                                    ScoringResult(
                                        score = 0,
                                        rawScore = 0f,
                                        metrics = SimilarityMetrics(pitch = 0f, mfcc = 0f),
                                        feedback = emptyList()
                                    )
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
                                    Log.d("AudioViewModel", "Scoring result: ${result.score}, pitch: ${result.metrics.pitch}, mfcc: ${result.metrics.mfcc}")
                                    result
                                } else {
                                    Log.d("AudioViewModel", "One or both audio arrays are empty for FORWARD score")
                                    ScoringResult(
                                        score = 0,
                                        rawScore = 0f,
                                        metrics = SimilarityMetrics(pitch = 0f, mfcc = 0f),
                                        feedback = emptyList()
                                    )
                                }
                            }
                        }

                        // Create player attempt WITH the challengeType AND real scoring metrics
                        val attempt = PlayerAttempt(
                            playerName = "Player ${parentRecording.attempts.size + 1}",
                            attemptFilePath = attemptFile.absolutePath,
                            reversedAttemptFilePath = reversedAttemptFile?.absolutePath,
                            score = scoringResult.score,
                            pitchSimilarity = scoringResult.metrics.pitch,
                            mfccSimilarity = scoringResult.metrics.mfcc,
                            rawScore = scoringResult.rawScore,
                            challengeType = challengeType // <-- SAVE THE TYPE
                        )
                        //TO HERE
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
                                statusText = "Attempt scored: ${scoringResult.score}%",
                                scrollToIndex = scrollToIndex,
                                attemptToRename = if (scoringResult.score > 70) Pair(parentPath, attempt) else null
                            )
                        }
                    }
                } catch (e: Exception) {
                    Log.e("AudioViewModel", "Error processing attempt", e)
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

    // SURGICAL FIX #7: Add playback state cleanup
    private fun clearAllPlaybackStates() {
        _uiState.update { it.copy(
            currentlyPlayingPath = null,
            isPaused = false,
            playbackProgress = 0f
        )}
    }

    // SURGICAL FIX #8: Improved play method with state cleanup
    fun play(path: String) {
        clearAllPlaybackStates() // Clear any previous playback state
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
                showUserMessage("Error: Could not play file.")
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
    fun updateScoringEngine(preset: Presets) {
        scoringEngine.applyPreset(preset)
    }
}