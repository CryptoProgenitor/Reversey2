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
import com.quokkalabs.reversey.audio.RecorderEvent
import com.quokkalabs.reversey.audio.RecordingResult
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
import com.quokkalabs.reversey.asr.TranscriptionResult
import com.quokkalabs.reversey.asr.TranscriptionStatus
import com.quokkalabs.reversey.asr.VoskTranscriptionHelper
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
import com.quokkalabs.reversey.testing.BITRunner
import com.quokkalabs.reversey.scoring.PhonemeUtils
import com.quokkalabs.reversey.scoring.ReverseScoringEngine

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
    private val scoreAcquisitionDataConcentrator: ScoreAcquisitionDataConcentrator,
    private val bitRunner: com.quokkalabs.reversey.testing.BITRunner,
    private val voskTranscriptionHelper: VoskTranscriptionHelper
) : AndroidViewModel(application) {

    // 🎯 RE-INTRODUCED: Mutex for strict serialization of I/O operations
    private val recordingProcessingMutex = Mutex()

    fun getOrchestrator(): VocalScoringOrchestrator {
        return vocalScoringOrchestrator
    }

    fun getSpeechEngine(): SpeechScoringEngine {
        return speechScoringEngine
    }

    fun getSingingEngine(): SingingScoringEngine {
        return singingScoringEngine
    }

    fun getBITRunner(): com.quokkalabs.reversey.testing.BITRunner {
        return bitRunner
    }

    init {
        Log.d("HILT_VERIFY", "📱 AudioViewModel created")

        // 🎤 VOSK: Initialize model at startup
        viewModelScope.launch {
            Log.d("AudioViewModel", "🎤 Initializing Vosk...")
            val success = voskTranscriptionHelper.initialize()
            Log.d("AudioViewModel", "🎤 Vosk init: $success")
        }
    }

    private var currentRecordingFile: File? = null
    private var currentAttemptFile: File? = null

    // 🎤 PHASE 3: Store pending transcription for main recordings
    private var pendingTranscription: TranscriptionResult? = null

    private val _uiState = MutableStateFlow(AudioUiState())
    val uiState = _uiState.asStateFlow()

    private val _isScoringReady = MutableStateFlow(false)
    val isScoringReady: StateFlow<Boolean> = _isScoringReady.asStateFlow()

    private val _currentDifficulty = MutableStateFlow(DifficultyLevel.NORMAL)

    // 🎯 FIX: Encapsulation - Assigned once instead of using custom getter
    val recordingState: StateFlow<Boolean> = audioRecorderHelper.isRecording
    val amplitudeState: StateFlow<Float> = audioRecorderHelper.amplitude
    val countdownProgress: StateFlow<Float> = audioRecorderHelper.countdownProgress  // 🎯 PHASE 3

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
    }

    // --- Init Data Loading ---
    init {
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
                // 🎯 FIX: Apply restored difficulty to engines (no persist to avoid loop)
                val level = runCatching { DifficultyLevel.valueOf(savedDifficultyName) }
                    .getOrElse { DifficultyLevel.NORMAL }
                applyDifficulty(level)
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
                // 🎤 PHASE 3: Stop recording
                val result = audioRecorderHelper.stop()
                val file = result.file

                // 🎤 VOSK: Transcribe the original recording
                val transcription = if (file != null && file.exists()) {
                    Log.d("AudioViewModel", "🎤 Starting Vosk transcription for original...")
                    val voskResult = voskTranscriptionHelper.transcribeFile(file)
                    Log.d("AudioViewModel", "🎤 Vosk original result: '${voskResult.text}' (${voskResult.status})")
                    voskResult
                } else {
                    Log.e("AudioViewModel", "🎤 No file to transcribe!")
                    null
                }

                // 🎯 CRITICAL FIX: Null Check & Smart Cast (removed !!)
                if (file != null && validateRecordedFile(file)) {

                    if (!_uiState.value.isRecordingAttempt) {
                        // 🎤 PHASE 3: Log transcription result
                        Log.d("AudioViewModel", "🎤 Live transcription: '${transcription?.text}' (${transcription?.status})")

                        // 🎯 CRITICAL FIX: Use OPTIMISTIC_KEY to prevent LazyColumn crash
                        val optimisticRecording = Recording(
                            name = "🎤 New Recording",
                            originalPath = "${file.absolutePath}_OPTIMISTIC",
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

                        // 🎤 PHASE 3: Store transcription for repository to use
                        pendingTranscription = transcription

                        // Background processing
                        withContext(Dispatchers.IO) {
                            try {
                                repository.reverseWavFile(file)

                                // 🎯 PHASE 1: Calculate trimmed duration for timed recording
                                val samples = readAudioFile(file.absolutePath)
                                val trimmedSamples = trimSilence(samples)
                                val trimmedSampleCount = trimmedSamples.size
                                Log.d("AudioViewModel", "🎯 Trimmed: ${samples.size} → $trimmedSampleCount samples")

                                // 🎤 PHASE 3: Save transcription to cache (live result, not file-based)
                                // 🎯 PHASE 1: Now also saves trimmedSampleCount
                                if (transcription?.isSuccess == true && transcription.text != null) {
                                    try {
                                        repository.cacheTranscription(
                                            file,
                                            transcription.text,
                                            transcription.confidence,
                                            trimmedSampleCount  // 🎯 PHASE 1
                                        )
                                        Log.d("AudioViewModel", "🎤 Cached live transcription: '${transcription.text}' (trimmed=$trimmedSampleCount)")
                                    } catch (e: Exception) {
                                        Log.w("AudioViewModel", "🎤 Failed to cache transcription: ${e.message}")
                                    }
                                } else if (transcription?.isOffline == true) {
                                    // Mark as pending for later transcription when online
                                    Log.d("AudioViewModel", "🎤 Offline - transcription pending")
                                    repository.markTranscriptionPending(file)
                                } else {
                                    // 🎯 PHASE 1: Cache trimmed duration even without transcription
                                    try {
                                        repository.cacheTranscription(file, "", 0f, trimmedSampleCount)
                                        Log.d("AudioViewModel", "🎯 Cached trimmed duration only: $trimmedSampleCount samples")
                                    } catch (e: Exception) {
                                        Log.w("AudioViewModel", "🎯 Failed to cache trimmed duration: ${e.message}")
                                    }
                                }

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
                        // 🎤 PHASE 3: Pass transcription to attempt handler
                        handleAttemptCompletion(file, transcription)
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
                val parentRecording = uiState.value.recordings.find { it.originalPath == originalPath }
                if (parentRecording == null) {
                    showUserMessage("Error: Cannot start challenge. Parent recording not found.")
                    return@withLock
                }

                // 🎯 FIX: Block REVERSE challenges if reversed audio isn't ready yet
                val challengeType = uiState.value.pendingChallengeType ?: ChallengeType.REVERSE
                if (challengeType == ChallengeType.REVERSE && parentRecording.reversedPath == null) {
                    showUserMessage("Reversed audio isn't ready yet. Please wait a moment and try again.")
                    _uiState.update { it.copy(pendingChallengeType = null) }
                    return@withLock
                }

                val attemptFile = createAudioFile(getApplication(), isAttempt = true)
                currentAttemptFile = attemptFile

                // 🎯 PHASE 2: Calculate max duration for timed recording
                val maxMs = parentRecording.trimmedDurationMs?.let { trimmedMs ->
                    (trimmedMs * AudioConstants.DURATION_GATE_MAX).toLong()
                }
                Log.d("AudioViewModel", "🎯 Starting attempt with maxMs=$maxMs (trimmedDurationMs=${parentRecording.trimmedDurationMs})")

                audioRecorderHelper.start(attemptFile, maxMs)

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
                val result = audioRecorderHelper.stop()
                val attemptFile = result.file

                // 🎤 VOSK: Transcribe the recorded WAV file
                val transcription = if (attemptFile != null && attemptFile.exists()) {
                    Log.d("AudioViewModel", "🎤 Starting Vosk transcription...")
                    val voskResult = voskTranscriptionHelper.transcribeFile(attemptFile)
                    Log.d("AudioViewModel", "🎤 Vosk result: '${voskResult.text}' (${voskResult.status})")
                    voskResult
                } else {
                    Log.e("AudioViewModel", "🎤 No file to transcribe!")
                    TranscriptionResult.error("No file to transcribe")
                }

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
                        // 🎤 PHASE 3: Pass live transcription to scoring
                        scoreAttempt(
                            originalRecordingPath = parentRecording.originalPath,
                            reversedRecordingPath = parentRecording.reversedPath ?: "",
                            attemptFilePath = attemptFile.absolutePath,
                            challengeType = challengeType,
                            attemptTranscription = transcription  // 🎤 NEW PARAMETER
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

    // 🎤 PHASE 3: Updated signature to accept transcription
    private suspend fun handleAttemptCompletion(attemptFile: File, transcription: TranscriptionResult?) {
        val parentPath = uiState.value.parentRecordingPath
        val challengeType = uiState.value.pendingChallengeType ?: ChallengeType.REVERSE

        if (parentPath != null) {
            scoreAttempt(
                originalRecordingPath = parentPath,
                reversedRecordingPath = uiState.value.recordings.find { it.originalPath == parentPath }?.reversedPath ?: "",
                attemptFilePath = attemptFile.absolutePath,
                challengeType = challengeType,
                attemptTranscription = transcription  // 🎤 NEW PARAMETER
            )
        } else {
            _uiState.update { it.copy(isRecordingAttempt = false, statusText = "Error: Parent not found") }
        }
    }

    // 🎯 IN-MEMORY SCORING
    // 🎤 PHASE 3: Added attemptTranscription parameter
    private fun scoreAttempt(
        originalRecordingPath: String,
        reversedRecordingPath: String,
        attemptFilePath: String,
        challengeType: ChallengeType = ChallengeType.REVERSE,
        attemptTranscription: TranscriptionResult? = null  // 🎤 NEW PARAMETER
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. Generate the Reversed Version of the Attempt (Crucial for "Rev" button!)
                val attemptFile = File(attemptFilePath)
                val reversedAttemptFile = repository.reverseWavFile(attemptFile)

                // 2. Load Audio Data (Disk -> Memory)
                // 🎯 FIX: Select correct reference audio based on Challenge Type (SAFE 'WHEN' LOGIC)
                val referenceAudioPath = when (challengeType) {
                    ChallengeType.FORWARD -> originalRecordingPath
                    ChallengeType.REVERSE -> reversedRecordingPath
                }

                Log.d("SCORING_DEBUG", "challengeType=$challengeType, loading reference=$referenceAudioPath")

                val referenceAudioRaw = readAudioFile(referenceAudioPath)
                val attemptAudioRaw = readAudioFile(attemptFilePath)
                val referenceAudio = trimSilence(referenceAudioRaw)
                val attemptAudio = trimSilence(attemptAudioRaw)

                if (referenceAudio.isEmpty() || attemptAudio.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        showUserMessage("Error: Could not read audio files for scoring")
                        _uiState.update { it.copy(isScoring = false) }
                    }
                    return@launch
                }

                // 3. Get parent recording for reference vocal mode (BEFORE scoring)
                val parentRecording = uiState.value.recordings.find { it.originalPath == originalRecordingPath }
                val referenceVocalMode = parentRecording?.vocalAnalysis?.mode

                // 🎤 PHASE 3: Extract transcription text for scoring
                val attemptTranscriptionText = if (attemptTranscription?.isSuccess == true) {
                    attemptTranscription.text
                } else {
                    null
                }

                Log.d("SCORING_DEBUG", "🎤 Attempt transcription for scoring: '$attemptTranscriptionText'")

                // 🎤 PHONEME SCORING (with duration gate)
                val phonemeScore = if (
                    parentRecording?.referenceTranscription != null &&
                    attemptTranscriptionText != null &&
                    PhonemeUtils.isReady()
                ) {
                    // Duration gate: 40-250% of target length (after silence trim)
                    val durationRatio = if (referenceAudio.isNotEmpty()) {
                        attemptAudio.size.toFloat() / referenceAudio.size.toFloat()
                    } else 1f

                    if (durationRatio < AudioConstants.DURATION_GATE_MIN) {
                        Log.d("PHONEME_SCORE", "❌ Too short: ${(durationRatio * 100).toInt()}% of target")
                        0
                    } else if (durationRatio > AudioConstants.DURATION_GATE_MAX) {
                        Log.d("PHONEME_SCORE", "❌ Too long: ${(durationRatio * 100).toInt()}% of target")
                        0
                    } else {
                        val score = ReverseScoringEngine.scoreTextOnly(
                            targetText = parentRecording.referenceTranscription!!,
                            attemptText = attemptTranscriptionText
                        )
                        Log.d("PHONEME_SCORE", "🎯 $score% | Dur: ${(durationRatio * 100).toInt()}% | Target: '${parentRecording.referenceTranscription}' | Attempt: '$attemptTranscriptionText'")
                        score
                    }
                } else {
                    Log.d("PHONEME_SCORE", "⚠️ Skipped: refTx=${parentRecording?.referenceTranscription != null}, attemptTx=${attemptTranscriptionText != null}, ready=${PhonemeUtils.isReady()}")
                    null
                }

                // 4. Score via Orchestrator (with reference mode to prevent cross-engine contamination)
                val scoringResult = scoreDualPipeline(
                    referenceAudio = referenceAudio,
                    attemptAudio = attemptAudio,
                    challengeType = challengeType,
                    referenceVocalMode = referenceVocalMode,
                    referenceTranscription = parentRecording?.referenceTranscription,
                    attemptTranscriptionText = attemptTranscriptionText,  // 🎤 NEW: Pass attempt transcription
                    sampleRate = AudioConstants.SAMPLE_RATE
                )

                // 5. Save Result to Repo
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
                    calculationBreakdown = scoringResult.calculationBreakdown,
                    // 🎤 PHASE 3: Use LIVE transcription (not from scoring engine re-transcription)
                    attemptTranscription = attemptTranscriptionText ?: scoringResult.attemptTranscription,
                    wordAccuracy = scoringResult.wordAccuracy
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

    // 🎤 PHASE 3: Added attemptTranscriptionText parameter
    private suspend fun scoreDualPipeline(
        referenceAudio: FloatArray,
        attemptAudio: FloatArray,
        challengeType: ChallengeType,
        referenceVocalMode: VocalMode? = null,
        referenceTranscription: String? = null,
        attemptTranscriptionText: String? = null,  // 🎤 NEW PARAMETER
        sampleRate: Int = AudioConstants.SAMPLE_RATE
    ): ScoringResult {
        return vocalScoringOrchestrator.scoreAttempt(
            referenceAudio = referenceAudio,
            attemptAudio = attemptAudio,
            challengeType = challengeType,
            difficulty = _currentDifficulty.value,
            referenceVocalMode = referenceVocalMode,
            referenceTranscription = referenceTranscription,
            attemptTranscription = attemptTranscriptionText,  // 🎤 Pass through
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

    /**
     * Apply difficulty to engines WITHOUT persisting to DataStore.
     * Used by init collector to avoid write-back loops.
     */
    private fun applyDifficulty(level: DifficultyLevel) {
        _currentDifficulty.value = level
        val preset = when (level) {
            DifficultyLevel.EASY -> ScoringPresets.easyMode()
            DifficultyLevel.NORMAL -> ScoringPresets.normalMode()
            DifficultyLevel.HARD -> ScoringPresets.hardMode()
            else -> ScoringPresets.normalMode()
        }
        updateScoringEngine(preset)
    }

    /**
     * Update difficulty AND persist to DataStore.
     * Called by UI when user changes difficulty.
     */
    fun updateDifficulty(level: DifficultyLevel) {
        applyDifficulty(level)
        viewModelScope.launch { settingsDataStore.saveDifficultyLevel(level.name) }
    }

    fun updateScoringEngine(preset: Presets) {
        val difficulty = _currentDifficulty.value
        speechScoringEngine.updateDifficulty(difficulty)
        singingScoringEngine.updateDifficulty(difficulty)
    }

    // 🔇 SILENCE TRIMMING: Strip leading/trailing silence for accurate duration gate
    private fun trimSilence(samples: FloatArray, threshold: Float = AudioConstants.SILENCE_TRIM_THRESHOLD): FloatArray {
        if (samples.size < 1024) return samples

        val windowSize = 512
        var startIdx = 0
        var endIdx = samples.size

        // Find first loud window from start
        for (i in 0 until samples.size - windowSize step windowSize / 2) {
            var sum = 0f
            for (j in i until i + windowSize) sum += samples[j] * samples[j]
            val rms = kotlin.math.sqrt(sum / windowSize)
            if (rms > threshold) {
                startIdx = maxOf(0, i - windowSize)
                break
            }
        }

        // Find first loud window from end
        for (i in samples.size - windowSize downTo windowSize step windowSize / 2) {
            var sum = 0f
            for (j in i until i + windowSize) sum += samples[j] * samples[j]
            val rms = kotlin.math.sqrt(sum / windowSize)
            if (rms > threshold) {
                endIdx = minOf(samples.size, i + windowSize * 2)
                break
            }
        }

        if (endIdx <= startIdx || endIdx - startIdx < 1000) return samples

        Log.d("AUDIO_TRIM", "Trimmed: ${samples.size} → ${endIdx - startIdx} samples")
        return samples.sliceArray(startIdx until endIdx)
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