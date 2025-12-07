package com.quokkalabs.reversey.ui.viewmodels

import android.app.Application
import android.content.pm.PackageManager
import android.Manifest
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.quokkalabs.reversey.audio.AudioConstants
import com.quokkalabs.reversey.audio.AudioPlayerHelper
import com.quokkalabs.reversey.audio.AudioRecorderHelper
import com.quokkalabs.reversey.audio.RecorderEvent // 🎯 FIX: Import the sealed class
import com.quokkalabs.reversey.data.models.ChallengeType
import com.quokkalabs.reversey.data.models.PlayerAttempt
import com.quokkalabs.reversey.data.models.Recording
import com.quokkalabs.reversey.data.repositories.AttemptsRepository
import com.quokkalabs.reversey.data.repositories.RecordingNamesRepository
import com.quokkalabs.reversey.data.repositories.RecordingRepository
import com.quokkalabs.reversey.data.repositories.SettingsDataStore
import com.quokkalabs.reversey.scoring.DifficultyLevel
import com.quokkalabs.reversey.scoring.Presets
import com.quokkalabs.reversey.scoring.ScoreAcquisitionDataConcentrator
import com.quokkalabs.reversey.scoring.ScoringPresets
import com.quokkalabs.reversey.scoring.SingingScoringEngine
import com.quokkalabs.reversey.scoring.SpeechScoringEngine
import com.quokkalabs.reversey.scoring.VocalMode
import com.quokkalabs.reversey.scoring.VocalModeDetector
import com.quokkalabs.reversey.scoring.VocalModeRouter
import com.quokkalabs.reversey.scoring.VocalScoringOrchestrator
import com.quokkalabs.reversey.scoring.ScoringResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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
    val qualityWarningMessage: String = "",
    val isScoring: Boolean = false,
    val showAnalysisToast: Boolean = false
)

@HiltViewModel
class AudioViewModel @Inject constructor(
    application: Application,
    private val repository: RecordingRepository,
    private val attemptsRepository: AttemptsRepository,
    private val recordingNamesRepository: RecordingNamesRepository,
    private val settingsDataStore: SettingsDataStore,
    private val audioPlayerHelper: AudioPlayerHelper,
    private val audioRecorderHelper: AudioRecorderHelper,
    private val vocalModeDetector: VocalModeDetector,
    private val vocalModeRouter: VocalModeRouter,
    private val speechScoringEngine: SpeechScoringEngine,
    private val singingScoringEngine: SingingScoringEngine,
    private val vocalScoringOrchestrator: VocalScoringOrchestrator,
    private val scoreAcquisitionDataConcentrator: ScoreAcquisitionDataConcentrator
) : AndroidViewModel(application) {

    // 🎯 RE-INTRODUCED: Mutex for strict serialization of I/O operations
    private val recordingProcessingMutex = Mutex()

    fun getOrchestrator(): VocalScoringOrchestrator {
        return vocalScoringOrchestrator
    }

    init {
        Log.d("HILT_VERIFY", "📱 AudioViewModel created")
    }

    private var currentRecordingFile: File? = null
    private var currentAttemptFile: File? = null

    private val _uiState = MutableStateFlow(AudioUiState())
    val uiState = _uiState.asStateFlow()

    private val _isScoringReady = MutableStateFlow(false)
    val isScoringReady: StateFlow<Boolean> = _isScoringReady.asStateFlow()

    private val _currentDifficulty = MutableStateFlow(DifficultyLevel.NORMAL)

    // 🎯 FIX: Encapsulation - Assigned once instead of using custom getter
    val recordingState: StateFlow<Boolean> = audioRecorderHelper.isRecording
    val amplitudeState: StateFlow<Float> = audioRecorderHelper.amplitude

    val currentDifficultyFlow: StateFlow<DifficultyLevel> = _currentDifficulty.asStateFlow()

    private fun showUserMessage(message: String) {
        _uiState.update { it.copy(statusText = message) }
    }

    private fun validateRecordedFile(file: File?): Boolean {
        return file != null && file.exists() && file.length() > AudioConstants.MIN_VALID_RECORDING_SIZE
    }

    init {
        // --- Player State Observation ---
        viewModelScope.launch {
            audioPlayerHelper.progress.collect { progress ->
                _uiState.update { it.copy(playbackProgress = progress) }
            }
        }
        viewModelScope.launch {
            audioPlayerHelper.currentPath.collect { path ->
                _uiState.update { it.copy(currentlyPlayingPath = path) }
            }
        }
        viewModelScope.launch {
            audioPlayerHelper.isPlaying.collect { isPlaying ->
                val isPaused = _uiState.value.currentlyPlayingPath != null && !isPlaying
                _uiState.update { it.copy(isPaused = isPaused) }
            }
        }

        // --- Recorder State Observation ---
        viewModelScope.launch {
            audioRecorderHelper.amplitude.collect { amp ->
                if (_uiState.value.isRecording) {
                    _uiState.update {
                        val newAmplitudes = it.amplitudes + amp
                        it.copy(amplitudes = newAmplitudes.takeLast(AudioConstants.MAX_WAVEFORM_SAMPLES))
                    }
                }
            }
        }

        // --- NEW: Recorder Event Observation (Size Limits) ---
        viewModelScope.launch {
            audioRecorderHelper.events.collect { event ->
                // 🎯 FIX: Type-safe event handling with sealed class
                when (event) {
                    RecorderEvent.Warning -> _uiState.update {
                        it.copy(showQualityWarning = true, qualityWarningMessage = "Approaching recording limit")
                    }
                    RecorderEvent.Stop -> {
                        showUserMessage("Recording limit reached")
                        // Determine if we are stopping a normal recording or an attempt
                        if (_uiState.value.isRecordingAttempt) {
                            stopAttempt()
                        } else {
                            stopRecording()
                        }
                    }
                }
            }
        }

        // Safety: Sync UI if helper stops unexpectedly
        viewModelScope.launch {
            audioRecorderHelper.isRecording.collect { recording ->
                if (!recording && _uiState.value.isRecording && _uiState.value.statusText != "Processing...") {
                    _uiState.update { it.copy(isRecording = false) }
                }
            }
        }

        // --- Init Data Loading ---
        viewModelScope.launch(Dispatchers.IO) {
            try { repository.cleanupAnalysisCache() } catch (e: Exception) {
                Log.w("AudioViewModel", "Cache cleanup failed (non-critical)", e)
            }
            withContext(Dispatchers.Main) { loadRecordings() }
        }

        viewModelScope.launch {
            speechScoringEngine.isInitialized.filter { it == true }.collect { _isScoringReady.value = true }
        }

        viewModelScope.launch {
            // Get initial value immediately (fixes race condition)
            val initialCompleted = settingsDataStore.tutorialCompleted.first()
            _uiState.update { it.copy(showTutorial = !initialCompleted) }

            // Continue listening for changes (e.g., user completes tutorial)
            settingsDataStore.tutorialCompleted.collect { completed ->
                _uiState.update { it.copy(showTutorial = !completed) }
            }
        }

        viewModelScope.launch {
            settingsDataStore.getDifficultyLevel.collect { savedDifficultyName ->
                try {
                    val savedDifficulty = DifficultyLevel.valueOf(savedDifficultyName)
                    _currentDifficulty.value = savedDifficulty
                } catch (e: Exception) {
                    _currentDifficulty.value = DifficultyLevel.NORMAL
                }
            }
        }
    }

    fun loadRecordings() {
        viewModelScope.launch(Dispatchers.IO) {
            val loadedRecordingsFromDisk = repository.loadRecordings()
            val attemptsMap = attemptsRepository.loadAttempts()
            val customNamesMap = recordingNamesRepository.loadCustomNames()

            val mergedRecordings = loadedRecordingsFromDisk.map { diskRecording: Recording ->
                val withAttempts = attemptsMap[diskRecording.originalPath]?.let { savedAttempts ->
                    diskRecording.copy(attempts = savedAttempts)
                } ?: diskRecording

                customNamesMap[diskRecording.originalPath]?.let { customName ->
                    withAttempts.copy(name = customName)
                } ?: withAttempts
            }

            _uiState.update { it.copy(recordings = mergedRecordings) }
        }
    }

    fun loadRecordingsWithAnalysis() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(showAnalysisToast = true) }
            delay(1500)
            loadRecordings()
            _uiState.update { it.copy(showAnalysisToast = false) }
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
        val timeStamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US).format(Date())
        val storageDir = if (isAttempt) {
            File(context.filesDir, "recordings/attempts")
        } else {
            File(context.filesDir, "recordings")
        }
        storageDir.mkdirs()
        return File(storageDir, "${timeStamp}.wav")
    }

    // --- RECORDING ACTIONS ---

    fun startRecording() {
        Log.d("AudioViewModel", "Starting recording - setting isRecording = true")
        if (audioRecorderHelper.isRecording.value) return

        if (ActivityCompat.checkSelfPermission(getApplication(), Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            showUserMessage("Microphone permission is required")
            return
        }

        viewModelScope.launch {
            // 🎯 MUTEX: Lock operation before starting file creation
            recordingProcessingMutex.withLock {
                if (audioRecorderHelper.isRecording.value) return@withLock

                val file = createAudioFile(getApplication(), isAttempt = false)
                currentRecordingFile = file

                audioRecorderHelper.start(file)

                _uiState.update { it.copy(
                    isRecording = true,
                    isRecordingAttempt = false,        // Fix the bug - when rapid Main Record Button Pressing Triggers "Parent Not Found"
                    parentRecordingPath = null,        // Fix the bug - when rapid Main Record Button Pressing Triggers "Parent Not Found"
                    pendingChallengeType = null,       // Fix the bug - when rapid Main Record Button Pressing Triggers "Parent Not Found"
                    statusText = "Recording...",
                    amplitudes = emptyList()
                    // Everything else preserved!
                ) }
            }
        }
    }

    fun stopRecording() {
        // ⚡ FAST UI UPDATE: Disable button immediately
        _uiState.update { it.copy(isRecording = false, statusText = "Processing...") }

        viewModelScope.launch {
            // 🎯 MUTEX: Lock operation during file flush and processing
            recordingProcessingMutex.withLock {
                val file = audioRecorderHelper.stop()

                // 🎯 CRITICAL FIX: Null Check & Smart Cast (removed !!)
                if (file != null && validateRecordedFile(file)) {

                    if (!_uiState.value.isRecordingAttempt) {
                        // 🎯 CRITICAL FIX: Use OPTIMISTIC_KEY to prevent LazyColumn crash
                        val optimisticRecording = Recording(
                            name = "🎤 New Recording",
                            originalPath = "${file.absolutePath}_OPTIMISTIC", // 🎯 Smart cast used here
                            reversedPath = null,
                            attempts = emptyList(),
                            vocalAnalysis = com.quokkalabs.reversey.scoring.VocalAnalysis(
                                VocalMode.UNKNOWN, 0.0f, com.quokkalabs.reversey.scoring.VocalFeatures(0f, 0f, 0f, 0f)
                            )
                        )

                        _uiState.update { state ->
                            state.copy(
                                statusText = "Recording complete",
                                recordings = listOf(optimisticRecording) + state.recordings,
                                scrollToIndex = 0
                            )
                        }

                        // Background processing
                        withContext(Dispatchers.IO) {
                            try {
                                repository.reverseWavFile(file)
                                withContext(Dispatchers.Main) {
                                    // Remove the optimistic entry and reload the real entry
                                    _uiState.update { state ->
                                        state.copy(
                                            recordings = state.recordings.filterNot { it.originalPath == optimisticRecording.originalPath }
                                        )
                                    }
                                    loadRecordings()
                                }
                            } catch (e: Exception) {
                                Log.e("AudioViewModel", "Error processing file", e)
                            }
                        }
                    } else {
                        handleAttemptCompletion(file) // 🎯 Smart cast used here
                    }
                } else {
                    // Failure (File too short)
                    _uiState.update { it.copy(statusText = "Recording failed - file too short") }
                }
            }
        }
    }

    fun startAttempt(originalPath: String) {
        if (audioRecorderHelper.isRecording.value) return

        if (ActivityCompat.checkSelfPermission(getApplication(), Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            showUserMessage("Microphone permission is required")
            return
        }

        viewModelScope.launch {
            // 🎯 MUTEX: Lock operation before starting file creation
            recordingProcessingMutex.withLock {
                if (audioRecorderHelper.isRecording.value) return@withLock

                // 🎯 DEFENSIVE CHECK: Ensure parent still exists before starting
                val parentExists = uiState.value.recordings.any { it.originalPath == originalPath }
                if (!parentExists) {
                    showUserMessage("Error: Cannot start challenge. Parent recording not found.")
                    return@withLock
                }

                val attemptFile = createAudioFile(getApplication(), isAttempt = true)
                currentAttemptFile = attemptFile

                audioRecorderHelper.start(attemptFile)

                _uiState.update {
                    it.copy(
                        isRecordingAttempt = true,
                        isRecording = true,
                        parentRecordingPath = originalPath,
                        statusText = "Recording attempt...",
                        amplitudes = emptyList()
                    )
                }
            }
        }
    }

    fun stopAttempt() {
        // ⚡ CRITICAL FIX: Lock UI immediately to prevent double-click race condition
        _uiState.update { it.copy(
            isRecording = false,
            statusText = "Processing..."
        )}

        viewModelScope.launch {
            // 🎯 MUTEX: Lock operation during file flush and scoring
            recordingProcessingMutex.withLock {
                val attemptFile = audioRecorderHelper.stop()

                val parentPath = uiState.value.parentRecordingPath
                val challengeType = uiState.value.pendingChallengeType ?: ChallengeType.REVERSE

                // 🎯 CRITICAL FIX: Null Check & Smart Cast (removed !!)
                if (attemptFile != null && validateRecordedFile(attemptFile) && parentPath != null) {
                    _uiState.update { it.copy(
                        isRecordingAttempt = false,
                        statusText = "Scoring attempt...",
                        isScoring = true
                    )}

                    val parentRecording = uiState.value.recordings.find { it.originalPath == parentPath }
                    if (parentRecording != null) {
                        scoreAttempt(
                            originalRecordingPath = parentRecording.originalPath,
                            reversedRecordingPath = parentRecording.reversedPath ?: "",
                            attemptFilePath = attemptFile.absolutePath, // 🎯 Smart cast used here
                            challengeType = challengeType
                        )
                    } else {
                        // FINAL CLEANUP: If parent was deleted during recording, clear state
                        _uiState.update {
                            it.copy(
                                isRecordingAttempt = false,
                                parentRecordingPath = null,
                                pendingChallengeType = null,
                                isScoring = false,
                                statusText = "Error: Parent not found after recording."
                            )
                        }
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isRecordingAttempt = false,
                            parentRecordingPath = null,
                            pendingChallengeType = null,
                            isScoring = false,
                            statusText = "Attempt recording failed"
                        )
                    }
                }
            }
        }
    }

    private suspend fun handleAttemptCompletion(attemptFile: File) {
        val parentPath = uiState.value.parentRecordingPath
        val challengeType = uiState.value.pendingChallengeType ?: ChallengeType.REVERSE

        if (parentPath != null) {
            scoreAttempt(
                originalRecordingPath = parentPath,
                reversedRecordingPath = uiState.value.recordings.find { it.originalPath == parentPath }?.reversedPath ?: "",
                attemptFilePath = attemptFile.absolutePath,
                challengeType = challengeType
            )
        } else {
            _uiState.update { it.copy(isRecordingAttempt = false, statusText = "Error: Parent not found") }
        }
    }

    // 🎯 IN-MEMORY SCORING
    private fun scoreAttempt(
        originalRecordingPath: String,
        reversedRecordingPath: String,
        attemptFilePath: String,
        challengeType: ChallengeType = ChallengeType.REVERSE
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. Generate the Reversed Version of the Attempt (Crucial for "Rev" button!)
                val attemptFile = File(attemptFilePath)
                val reversedAttemptFile = repository.reverseWavFile(attemptFile)

                // 2. Load Audio Data (Disk -> Memory)
                val reversedParentAudio = readAudioFile(reversedRecordingPath)
                val attemptAudio = readAudioFile(attemptFilePath)

                if (reversedParentAudio.isEmpty() || attemptAudio.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        showUserMessage("Error: Could not read audio files for scoring")
                        _uiState.update { it.copy(isScoring = false) }
                    }
                    return@launch
                }

                // 3. Score via Orchestrator
                val scoringResult = scoreDualPipeline(
                    referenceAudio = reversedParentAudio,
                    attemptAudio = attemptAudio,
                    challengeType = challengeType,
                    sampleRate = AudioConstants.SAMPLE_RATE
                )

                // 4. Save Result to Repo
                val parentRecording = uiState.value.recordings.find { it.originalPath == originalRecordingPath }
                val playerIndex = (parentRecording?.attempts?.size ?: 0) + 1

                val attempt = PlayerAttempt(
                    playerName = "Player $playerIndex",
                    attemptFilePath = attemptFilePath,
                    reversedAttemptFilePath = reversedAttemptFile?.absolutePath,
                    score = scoringResult.score,
                    pitchSimilarity = scoringResult.metrics.pitch,
                    mfccSimilarity = scoringResult.metrics.mfcc,
                    rawScore = scoringResult.rawScore,
                    challengeType = challengeType,
                    difficulty = _currentDifficulty.value,
                    scoringEngine = scoringResult.calculationBreakdown?.scoringEngineType,
                    feedback = scoringResult.feedback,
                    isGarbage = scoringResult.isGarbage,
                    vocalAnalysis = scoringResult.vocalAnalysis,
                    // NEW: Pass the calculation breakdown through (v21.6.0)
                    calculationBreakdown = scoringResult.calculationBreakdown
                )

                val updatedRecordings = uiState.value.recordings.map { recording ->
                    if (recording.originalPath == originalRecordingPath) {
                        recording.copy(attempts = recording.attempts + attempt)
                    } else {
                        recording
                    }
                }

                val attemptsMap = updatedRecordings.associate { it.originalPath to it.attempts }.filterValues { it.isNotEmpty() }
                attemptsRepository.saveAttempts(attemptsMap)

                // 5. Update UI
                withContext(Dispatchers.Main) {
                    _uiState.update {
                        it.copy(
                            recordings = updatedRecordings,
                            parentRecordingPath = null,
                            pendingChallengeType = null,
                            isScoring = false,
                            statusText = "Attempt scored: ${scoringResult.score}%",
                            attemptToRename = if (scoringResult.score > 70) Pair(originalRecordingPath, attempt) else null
                        )
                    }
                }

            } catch (e: Exception) {
                Log.e("SCORING", "Error during scoring", e)
                withContext(Dispatchers.Main) {
                    showUserMessage("Scoring failed: ${e.message}")
                    _uiState.update { it.copy(parentRecordingPath = null, pendingChallengeType = null, isScoring = false) }
                }
            }
        }
    }

    private suspend fun scoreDualPipeline(
        referenceAudio: FloatArray,
        attemptAudio: FloatArray,
        challengeType: ChallengeType,
        sampleRate: Int = AudioConstants.SAMPLE_RATE
    ): ScoringResult {
        return vocalScoringOrchestrator.scoreAttempt(
            referenceAudio = referenceAudio,
            attemptAudio = attemptAudio,
            challengeType = challengeType,
            difficulty = _currentDifficulty.value,
            sampleRate = sampleRate
        )
    }

    // --- Boilerplate & Utils ---

    fun startChallengeAttempt(originalPath: String, challengeType: ChallengeType) {
        _uiState.update { it.copy(pendingChallengeType = challengeType) }
        startAttempt(originalPath)
    }

    fun startAttemptRecording(recording: Recording, challengeType: ChallengeType) {
        _uiState.update { it.copy(pendingChallengeType = challengeType) }
        startAttempt(recording.originalPath)
    }

    fun play(path: String) {
        // 🎯 FIX: Removed 'playbackProgress = 0f' to stop race condition/flickering
        _uiState.update { it.copy(isPaused = false) }

        audioPlayerHelper.play(path) {
            _uiState.update { it.copy(currentlyPlayingPath = null, isPaused = false, playbackProgress = 0f) }
        }
    }

    fun pause() {
        if (_uiState.value.isPaused) audioPlayerHelper.resume() else audioPlayerHelper.pause()
    }

    fun stopPlayback() {
        audioPlayerHelper.stop()
        _uiState.update { it.copy(currentlyPlayingPath = null, isPaused = false, playbackProgress = 0f) }
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayerHelper.cleanup()
        audioRecorderHelper.cleanup()
    }

    fun deleteRecording(recording: Recording) {
        viewModelScope.launch {
            repository.deleteRecording(recording.originalPath, recording.reversedPath)
            recording.attempts.forEach { attempt ->
                try { File(attempt.attemptFilePath).delete() } catch (e: Exception) {
                    Log.w("AudioViewModel", "Failed to delete attempt file: ${attempt.attemptFilePath}")
                }
                attempt.reversedAttemptFilePath?.let { path ->
                    try { File(path).delete() } catch (e: Exception) {
                        Log.w("AudioViewModel", "Failed to delete reversed attempt: $path")
                    }
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
            recordingNamesRepository.setCustomName(oldPath, newName)
            loadRecordings()
        }
    }

    fun dismissEasterEgg() {
        _uiState.update { it.copy(showEasterEgg = false) }
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
                Log.w("AudioViewModel", "Failed to delete attempt files", e)
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
        _uiState.update { it.copy(showQualityWarning = false, qualityWarningMessage = "") }
    }

    fun updateDifficulty(level: DifficultyLevel) {
        _currentDifficulty.update { level }
        viewModelScope.launch { settingsDataStore.saveDifficultyLevel(level.name) }
        val preset = when (level) {
            DifficultyLevel.EASY -> ScoringPresets.easyMode()
            DifficultyLevel.NORMAL -> ScoringPresets.normalMode()
            DifficultyLevel.HARD -> ScoringPresets.hardMode()
            else -> ScoringPresets.normalMode()
        }
        updateScoringEngine(preset)
    }

    fun updateScoringEngine(preset: Presets) {
        val difficulty = _currentDifficulty.value
        speechScoringEngine.updateDifficulty(difficulty)
        singingScoringEngine.updateDifficulty(difficulty)
    }

    private fun readAudioFile(path: String): FloatArray {
        val file = File(path)

        if (!file.exists()) {
            Log.e("AudioViewModel", "File not found: $path")
            return floatArrayOf()
        }

        // NEW: Check against AudioConstants
        if (file.length() > AudioConstants.MAX_LOADABLE_AUDIO_BYTES) {
            Log.w("AudioViewModel", "File too large (${file.length()} bytes). Limit: ${AudioConstants.MAX_LOADABLE_AUDIO_BYTES}")
            return floatArrayOf()
        }

        if (file.length() <= AudioConstants.WAV_HEADER_SIZE) {
            return floatArrayOf()
        }

        return try {
            val bytes = file.readBytes()
            if (bytes.size <= AudioConstants.WAV_HEADER_SIZE) return floatArrayOf()

            val audioData = bytes.sliceArray(AudioConstants.WAV_HEADER_SIZE until bytes.size)

            FloatArray(audioData.size / 2) { i ->
                val low = audioData[i * 2].toInt() and 0xFF
                val high = audioData[i * 2 + 1].toInt() shl 8
                (high or low).toShort() / 32768f
            }
        } catch (e: Exception) {
            Log.e("AudioViewModel", "Error reading audio file", e)
            floatArrayOf()
        }
    }
}