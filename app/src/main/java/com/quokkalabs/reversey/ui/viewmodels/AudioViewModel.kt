package com.quokkalabs.reversey.ui.viewmodels

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.quokkalabs.reversey.asr.VoskTranscriptionHelper
import com.quokkalabs.reversey.audio.AudioConstants
import com.quokkalabs.reversey.audio.AudioPlayerHelper
import com.quokkalabs.reversey.audio.AudioRecorderHelper
import com.quokkalabs.reversey.audio.RecorderEvent
import com.quokkalabs.reversey.data.models.ChallengeType
import com.quokkalabs.reversey.data.models.PlayerAttempt
import com.quokkalabs.reversey.data.models.Recording
import com.quokkalabs.reversey.data.repositories.AttemptsRepository
import com.quokkalabs.reversey.data.repositories.RecordingNamesRepository
import com.quokkalabs.reversey.data.repositories.RecordingRepository
import com.quokkalabs.reversey.data.repositories.SettingsDataStore
import com.quokkalabs.reversey.scoring.DifficultyLevel
import com.quokkalabs.reversey.scoring.PhonemeScoreResult
import com.quokkalabs.reversey.scoring.PhonemeUtils
import com.quokkalabs.reversey.scoring.ReverseScoringEngine
import com.quokkalabs.reversey.scoring.WordPhonemes
import com.quokkalabs.reversey.testing.BITRunner
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class AudioUiState(
    val recordings: List<Recording> = emptyList(),
    val isRecording: Boolean = false,
    val isRecordingAttempt: Boolean = false,
    val currentlyPlayingPath: String? = null,
    val isPaused: Boolean = false,
    val playbackProgress: Float = 0f,
    val recordingDuration: Long = 0L,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showVocalModeChooser: Boolean = false,
    val parentRecordingPath: String? = null,
    val statusText: String = "",
    val pendingChallengeType: ChallengeType? = null,
    val isScoring: Boolean = false,
    val userMessage: String? = null,
    val attemptToRename: Pair<String, PlayerAttempt>? = null,
    val showBitResults: Boolean = false,
    val bitResults: String = "",
    val bitProgress: Float = 0f,
    // 🎯 Custom names stored separately (Recording model doesn't have customName field)
    val customNames: Map<String, String> = emptyMap(),
    // 🎯 Additional UI state for MainActivity
    val scrollToIndex: Int? = null,
    val amplitudes: List<Float> = emptyList(),
    val showTutorial: Boolean = false,
    val showQualityWarning: Boolean = false,
    val qualityWarningMessage: String = "",
    val showAnalysisToast: Boolean = false,
    // 🥚 Easter egg state
    val showEasterEgg: Boolean = false,
    val cpdTaps: Int = 0,
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
    private val bitRunner: BITRunner,
    private val voskTranscriptionHelper: VoskTranscriptionHelper,
) : AndroidViewModel(application) {

    companion object {
        private const val PHONEME_LOAD_MAX_RETRIES = 20
        private const val PHONEME_LOAD_RETRY_DELAY_MS = 100L
    }

    // 🎯 RE-INTRODUCED: Mutex for strict serialization of I/O operations
    private val recordingProcessingMutex = Mutex()

    // 🎯 STATE: Must be declared BEFORE init block to avoid NPE
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
    val countdownProgress: StateFlow<Float> = audioRecorderHelper.countdownProgress

    val currentDifficultyFlow: StateFlow<DifficultyLevel> = _currentDifficulty.asStateFlow()

    fun getBITRunner(): BITRunner {
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

        // Listen for recorder events (Warning, Stop)
        viewModelScope.launch {
            audioRecorderHelper.events.collect { event ->
                when (event) {
                    is RecorderEvent.Warning -> {
                        showUserMessage("Recording approaching size limit...")
                    }

                    is RecorderEvent.Stop -> {
                        Log.d("AudioViewModel", "⏱️ Auto-stop event received")
                        // Auto-stop triggered by duration limit
                        handleAutoStop()
                    }
                }
            }
        }

        // Collect amplitude for waveform visualization
        viewModelScope.launch {
            audioRecorderHelper.amplitude.collect { amp ->
                if (_uiState.value.isRecording && amp > 0f) {
                    val currentAmps = _uiState.value.amplitudes.toMutableList()
                    currentAmps.add(amp)
                    // Keep last 100 samples for visualization
                    if (currentAmps.size > 100) currentAmps.removeAt(0)
                    _uiState.update { it.copy(amplitudes = currentAmps) }
                }
            }
        }

        // Collect playback progress for progress bars
        viewModelScope.launch {
            audioPlayerHelper.progress.collect { progress ->
                _uiState.update { it.copy(playbackProgress = progress) }
            }
        }

        loadRecordings()
    }

    private fun showUserMessage(message: String) {
        _uiState.update { it.copy(userMessage = message) }
        viewModelScope.launch {
            delay(3000)
            _uiState.update { it.copy(userMessage = null) }
        }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }

    /**
     * Handle auto-stop from duration limit
     */
    private fun handleAutoStop() {
        viewModelScope.launch {
            if (_uiState.value.isRecording) {
                stopRecording()
            }
        }
    }

    fun loadRecordings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // 🎯 FIX: Use correct method names
                val recordings = repository.loadRecordings()
                val savedAttempts = attemptsRepository.loadAttempts()
                val savedNames = recordingNamesRepository.loadCustomNames()

                val recordingsWithAttempts = recordings.map { recording ->
                    val attempts = savedAttempts[recording.originalPath] ?: emptyList()
                    val customName = savedNames[recording.originalPath]
                    recording.copy(
                        name = customName ?: recording.name,
                        attempts = attempts
                    )
                }

                _uiState.update {
                    it.copy(
                        recordings = recordingsWithAttempts,
                        customNames = savedNames,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    /**
     * Get display name for a recording (custom name if set, otherwise default name)
     */
    fun getDisplayName(recording: Recording): String {
        return _uiState.value.customNames[recording.originalPath] ?: recording.name
    }

    fun startRecording() {
        // 🎤 Check if Vosk is ready - show toast if still loading
        if (!voskTranscriptionHelper.isReady()) {
            showUserMessage("Voice transcription engine still loading, please wait...")
            return
        }

        val context = getApplication<Application>()

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            _uiState.update { it.copy(error = "Microphone permission not granted") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isRecording = true,
                    isRecordingAttempt = false,
                    statusText = "Recording...",
                    amplitudes = emptyList()
                )
            }

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val recordingFile = File(context.filesDir, "recordings/REC_$timestamp.wav")
            recordingFile.parentFile?.mkdirs()
            currentRecordingFile = recordingFile

            // 🎯 FIX: Use correct method signature - start(File, maxDurationMs?)
            audioRecorderHelper.start(recordingFile)
        }
    }

    fun stopRecording() {
        Log.d(
            "AudioViewModel",
            "🛑 stopRecording() called | isRecordingAttempt=${_uiState.value.isRecordingAttempt}"
        )

        viewModelScope.launch {
            val wasAttempt = _uiState.value.isRecordingAttempt

            // 🎯 FIX: stop() returns RecordingResult directly (not via events flow)
            val result = audioRecorderHelper.stop()

            Log.d("AudioViewModel", "📻 Recording stopped: file=${result.file?.absolutePath}")

            recordingProcessingMutex.withLock {
                if (result.file != null && result.file.exists()) {
                    if (wasAttempt) {
                        Log.d("AudioViewModel", "🎯 Processing ATTEMPT recording")
                        processAttemptResult(result.file)
                    } else {
                        Log.d("AudioViewModel", "🎵 Processing MAIN recording")
                        processMainRecordingResult(result.file)
                    }
                } else {
                    Log.e("AudioViewModel", "❌ Recording failed: no file produced")
                    _uiState.update {
                        it.copy(
                            isRecording = false,
                            isRecordingAttempt = false,
                            statusText = "Recording failed",
                            parentRecordingPath = null,
                            pendingChallengeType = null,
                            amplitudes = emptyList()
                        )
                    }
                }
            }
        }
    }

    private suspend fun processMainRecordingResult(recordingFile: File) {
        try {
            _uiState.update { it.copy(statusText = "Processing...") }

            val reversedFile = repository.reverseWavFile(recordingFile)

            if (reversedFile != null) {
                // 🎤 Transcribe ORIGINAL recording for reference text
                Log.d("AudioViewModel", "🎤 Transcribing reference audio...")
                val transcriptionResult = voskTranscriptionHelper.transcribeFile(recordingFile)
                val referenceTranscription = if (transcriptionResult.isSuccess) {
                    val text = transcriptionResult.text ?: run {
                        Log.w("AudioViewModel", "🎤 Transcription succeeded but text was null")
                        null
                    }
                    if (text != null) {
                        Log.d("AudioViewModel", "🎤 Reference transcription: '$text'")
                        repository.cacheTranscription(recordingFile, text, 1.0f)
                    }
                    text
                } else {
                    Log.w(
                        "AudioViewModel",
                        "🎤 Reference transcription failed: ${transcriptionResult.errorMessage}"
                    )
                    null
                }

                // 🎯 FIX: Recording model fields - no createdAt, no customName
                val newRecording = Recording(
                    name = recordingFile.nameWithoutExtension,
                    originalPath = recordingFile.absolutePath,
                    reversedPath = reversedFile.absolutePath,
                    referenceTranscription = referenceTranscription
                )

                val updatedRecordings = listOf(newRecording) + _uiState.value.recordings

                _uiState.update {
                    it.copy(
                        recordings = updatedRecordings,
                        isRecording = false,
                        statusText = if (referenceTranscription != null) "Saved! Reference: \"$referenceTranscription\"" else "Saved!",
                        amplitudes = emptyList()
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isRecording = false,
                        statusText = "Failed to reverse audio",
                        amplitudes = emptyList()
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("AudioViewModel", "Error processing recording", e)
            _uiState.update {
                it.copy(
                    isRecording = false,
                    error = "Processing failed: ${e.message}",
                    amplitudes = emptyList()
                )
            }
        }
    }

    private suspend fun processAttemptResult(attemptFile: File) {
        val parentPath = _uiState.value.parentRecordingPath
        val challengeType = _uiState.value.pendingChallengeType ?: ChallengeType.REVERSE

        Log.d(
            "AudioViewModel",
            "📝 processAttemptResult: parentPath=$parentPath, challengeType=$challengeType"
        )

        if (parentPath == null) {
            Log.e("AudioViewModel", "❌ No parent recording path!")
            _uiState.update {
                it.copy(
                    isRecording = false,
                    isRecordingAttempt = false,
                    statusText = "Error: No parent recording",
                    amplitudes = emptyList()
                )
            }
            return
        }

        val parentRecording = _uiState.value.recordings.find { it.originalPath == parentPath }
        if (parentRecording == null) {
            Log.e("AudioViewModel", "❌ Parent recording not found!")
            _uiState.update {
                it.copy(
                    isRecording = false,
                    isRecordingAttempt = false,
                    statusText = "Error: Parent recording not found",
                    amplitudes = emptyList()
                )
            }
            return
        }

        // Transcription moved to scoreAttempt() - happens AFTER reversal

        _uiState.update {
            it.copy(
                isRecording = false,
                isRecordingAttempt = false,
                isScoring = true,
                statusText = "Scoring...",
                amplitudes = emptyList()
            )
        }

        scoreAttempt(
            originalRecordingPath = parentPath,
            reversedRecordingPath = parentRecording.reversedPath,
            attemptFile = attemptFile,
            challengeType = challengeType
        )
    }

    fun startAttempt(recordingPath: String) {
        // 🎤 Check if Vosk is ready - show toast if still loading
        if (!voskTranscriptionHelper.isReady()) {
            showUserMessage("Voice transcription engine still loading, please wait...")
            return
        }

        val context = getApplication<Application>()

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            _uiState.update { it.copy(error = "Microphone permission not granted") }
            return
        }

        viewModelScope.launch {
            Log.d("RECORD_BUG", "🎯 startAttempt() | recordingPath=$recordingPath")

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val attemptFile = File(context.filesDir, "recordings/attempts/ATTEMPT_$timestamp.wav")
            attemptFile.parentFile?.mkdirs()
            currentAttemptFile = attemptFile

            Log.d(
                "RECORD_BUG",
                "📝 Setting flags: isRecording=true, isRecordingAttempt=true, parentPath=$recordingPath"
            )

            _uiState.update {
                it.copy(
                    isRecording = true,
                    isRecordingAttempt = true,
                    parentRecordingPath = recordingPath,
                    statusText = "Recording attempt...",
                    amplitudes = emptyList()
                )
            }

            Log.d("RECORD_BUG", "🎤 About to call audioRecorderHelper.start()")

            // 🎯 FIX: Use correct method signature
            audioRecorderHelper.start(attemptFile)

            Log.d("RECORD_BUG", "✅ audioRecorderHelper.start() returned")
        }
    }

    fun stopAttempt() {
        Log.d("RECORD_BUG", "🛑 stopAttempt() called | Delegating to stopRecording()")
        stopRecording()
    }

    // --- Scoring Logic (REFACTORED: ReverseScoringEngine only) ---

    private suspend fun scoreAttempt(
        originalRecordingPath: String,
        reversedRecordingPath: String?,
        attemptFile: File,
        challengeType: ChallengeType = ChallengeType.REVERSE,
    ) {
        withContext(Dispatchers.IO) {
            try {
                // 🛑 CRITICAL FIX: Wait for Phoneme Dictionary to load (max 2 seconds)
                var attempts = 0
                while (!PhonemeUtils.isReady() && attempts < PHONEME_LOAD_MAX_RETRIES) {
                    delay(PHONEME_LOAD_RETRY_DELAY_MS)
                    attempts++
                }

                if (!PhonemeUtils.isReady()) {
                    Log.e(
                        "AudioViewModel",
                        "⚠️ PhonemeUtils failed to load in time. Scoring might fail."
                    )
                }

                // 1. Generate the Reversed Version of the Attempt
                val reversedAttemptFile = repository.reverseWavFile(attemptFile)

                // 2. 🎤 Transcribe the REVERSED attempt (this is the key fix!)
                // User says "olleh olleh" → reversed = "hello hello" → Vosk hears "hello hello"
                val attemptTranscription = if (reversedAttemptFile != null) {
                    Log.d("AudioViewModel", "🎤 Transcribing REVERSED attempt audio...")
                    val result = voskTranscriptionHelper.transcribeFile(reversedAttemptFile)
                    Log.d(
                        "AudioViewModel",
                        "🎤 Reversed attempt transcription: '${result.text}' (success=${result.isSuccess})"
                    )
                    result
                } else {
                    Log.w(
                        "AudioViewModel",
                        "🎤 No reversed file - transcribing raw attempt as fallback"
                    )
                    voskTranscriptionHelper.transcribeFile(attemptFile)
                }

                // 3. Load Audio Data (Disk -> Memory)
                val referenceAudioPath = reversedRecordingPath ?: originalRecordingPath

                Log.d(
                    "SCORING_DEBUG",
                    "challengeType=$challengeType, loading reference=$referenceAudioPath"
                )

                val referenceAudioRaw = readAudioFile(referenceAudioPath)
                val attemptAudioRaw = readAudioFile(attemptFile.absolutePath)
                val referenceAudio = trimSilence(referenceAudioRaw)
                val attemptAudio = trimSilence(attemptAudioRaw)

                if (referenceAudio.isEmpty() || attemptAudio.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        showUserMessage("Error: Could not read audio files for scoring")
                        _uiState.update { it.copy(isScoring = false) }
                    }
                    return@withContext
                }

                // 4. Get parent recording for reference transcription
                val parentRecording =
                    uiState.value.recordings.find { it.originalPath == originalRecordingPath }

                // 5. Extract transcription texts
                val attemptTranscriptionText = if (attemptTranscription.isSuccess) {
                    attemptTranscription.text
                } else {
                    null
                }

                Log.d(
                    "SCORING_DEBUG",
                    "🎤 Attempt transcription for scoring: '$attemptTranscriptionText'"
                )
                Log.d(
                    "SCORING_DEBUG",
                    "🎤 Reference transcription: '${parentRecording?.referenceTranscription}'"
                )

                // 6. Score using ReverseScoringEngine (phoneme-based)
                // Capture in local val for smart cast (parentRecording is mutable property)
                val referenceText = parentRecording?.referenceTranscription
                val scoringOutput = if (
                    referenceText != null &&
                    attemptTranscriptionText != null &&
                    PhonemeUtils.isReady()
                ) {
                    // Calculate durations from trimmed audio
                    val targetDurationMs =
                        (referenceAudio.size * 1000L) / AudioConstants.SAMPLE_RATE
                    val attemptDurationMs = (attemptAudio.size * 1000L) / AudioConstants.SAMPLE_RATE

                    val result = ReverseScoringEngine.score(
                        targetText = referenceText,
                        attemptText = attemptTranscriptionText,
                        targetDurationMs = targetDurationMs,
                        attemptDurationMs = attemptDurationMs,
                        difficulty = _currentDifficulty.value
                    )

                    Log.d("PHONEME_SCORE", "🎯 Score: ${result.score}% | ${result.shortSummary()}")
                    Log.d(
                        "PHONEME_SCORE",
                        "🔤 Target phonemes: ${result.targetPhonemes.joinToString(" ")}"
                    )
                    Log.d(
                        "PHONEME_SCORE",
                        "🔤 Attempt phonemes: ${result.attemptPhonemes.joinToString(" ")}"
                    )
                    Log.d(
                        "PHONEME_SCORE",
                        "🎚️ Difficulty: ${result.difficulty} | Leniency: ${result.phonemeLeniency}"
                    )

                    val feedbackList = buildPhonemeFeedback(result)

                    ScoringOutput(
                        score = result.score,
                        phonemeOverlap = result.phonemeOverlap,
                        durationRatio = result.durationRatio,
                        feedback = feedbackList,
                        isGarbage = result.shouldAutoReject,
                        targetPhonemes = result.targetPhonemes,
                        attemptPhonemes = result.attemptPhonemes,
                        phonemeMatches = result.phonemeMatches,
                        targetWordPhonemes = result.targetWordPhonemes,
                        attemptWordPhonemes = result.attemptWordPhonemes
                    )
                } else {
                    // Missing transcription - can't score properly
                    Log.w(
                        "PHONEME_SCORE",
                        "⚠️ Cannot score: refTx=${parentRecording?.referenceTranscription != null}, attemptTx=${attemptTranscriptionText != null}, ready=${PhonemeUtils.isReady()}"
                    )

                    val reason = when {
                        parentRecording?.referenceTranscription == null -> "Reference transcription unavailable"
                        attemptTranscriptionText == null -> "Could not transcribe your attempt"
                        !PhonemeUtils.isReady() -> "Phoneme engine not ready"
                        else -> "Unknown scoring error"
                    }

                    ScoringOutput(
                        score = 0,
                        phonemeOverlap = 0f,
                        durationRatio = 0f,
                        feedback = listOf(reason),
                        isGarbage = true
                    )
                }

                // 7. Build PlayerAttempt
                val playerIndex = (parentRecording?.attempts?.size ?: 0) + 1

                val attempt = PlayerAttempt(
                    playerName = "Player $playerIndex",
                    attemptFilePath = attemptFile.absolutePath,
                    reversedAttemptFilePath = reversedAttemptFile?.absolutePath,
                    score = scoringOutput.score,
                    pitchSimilarity = 0f,  // Not used in phoneme scoring
                    mfccSimilarity = 0f,   // Not used in phoneme scoring
                    rawScore = scoringOutput.score.toFloat() / 100f,
                    challengeType = challengeType,
                    difficulty = _currentDifficulty.value,
                    feedback = scoringOutput.feedback,
                    isGarbage = scoringOutput.isGarbage,
                    vocalAnalysis = null,
                    calculationBreakdown = null,
                    attemptTranscription = attemptTranscriptionText,
                    wordAccuracy = scoringOutput.phonemeOverlap,
                    targetPhonemes = scoringOutput.targetPhonemes,
                    attemptPhonemes = scoringOutput.attemptPhonemes,
                    phonemeMatches = scoringOutput.phonemeMatches,
                    targetWordPhonemes = scoringOutput.targetWordPhonemes,
                    attemptWordPhonemes = scoringOutput.attemptWordPhonemes,
                    durationRatio = scoringOutput.durationRatio
                )

                val updatedRecordings = uiState.value.recordings.map { recording ->
                    if (recording.originalPath == originalRecordingPath) {
                        recording.copy(attempts = recording.attempts + attempt)
                    } else {
                        recording
                    }
                }

                val attemptsMap = updatedRecordings.associate { it.originalPath to it.attempts }
                    .filterValues { it.isNotEmpty() }
                attemptsRepository.saveAttempts(attemptsMap)

                // 7. Update UI
                withContext(Dispatchers.Main) {
                    _uiState.update {
                        it.copy(
                            recordings = updatedRecordings,
                            parentRecordingPath = null,
                            pendingChallengeType = null,
                            isScoring = false,
                            statusText = "Attempt scored: ${scoringOutput.score}%",
                            attemptToRename = if (scoringOutput.score > 70) Pair(
                                originalRecordingPath,
                                attempt
                            ) else null
                        )
                    }
                }

            } catch (e: Exception) {
                Log.e("SCORING", "Error during scoring", e)
                withContext(Dispatchers.Main) {
                    showUserMessage("Scoring failed: ${e.message}")
                    _uiState.update {
                        it.copy(
                            parentRecordingPath = null,
                            pendingChallengeType = null,
                            isScoring = false
                        )
                    }
                }
            }
        }
    }

    // Helper data class for scoring output
    private data class ScoringOutput(
        val score: Int,
        val phonemeOverlap: Float,
        val durationRatio: Float,
        val feedback: List<String>,
        val isGarbage: Boolean,
        // Phase 3: Phoneme visualization data
        val targetPhonemes: List<String> = emptyList(),
        val attemptPhonemes: List<String> = emptyList(),
        val phonemeMatches: List<Boolean> = emptyList(),
        val targetWordPhonemes: List<WordPhonemes> = emptyList(),
        val attemptWordPhonemes: List<WordPhonemes> = emptyList(),
    )

    // Build human-readable feedback from phoneme scoring
    private fun buildPhonemeFeedback(result: PhonemeScoreResult): List<String> {
        val feedback = mutableListOf<String>()

        // Phoneme match feedback (now using actual match count)
        val matchPercent = if (result.targetPhonemes.isNotEmpty()) {
            (result.matchedCount * 100) / result.targetPhonemes.size
        } else 100

        when {
            matchPercent >= 90 -> feedback.add("Excellent pronunciation! 🎯")
            matchPercent >= 70 -> feedback.add("Good attempt - ${result.matchedCount}/${result.targetPhonemes.size} sounds matched")
            matchPercent >= 50 -> feedback.add("Some sounds were off - ${result.matchedCount}/${result.targetPhonemes.size} matched")
            else -> feedback.add("Try to match the sounds more closely")
        }

        // Duration feedback (now using gate check)
        val durationPercent = (result.durationRatio * 100).toInt()
        when {
            result.durationInRange && durationPercent in 85..115 -> feedback.add("Great timing! ⏱️")
            result.durationInRange -> {} // In range but not perfect - no comment
            durationPercent < 60 -> feedback.add("Too fast - slow down a bit")
            durationPercent > 150 -> feedback.add("Too slow - try to match the pace")
            else -> feedback.add("Timing slightly off for ${result.difficulty.name.lowercase()} mode")
        }

        // Overall score feedback
        when {
            result.score >= 85 -> feedback.add("🔥 Amazing!")
            result.score >= 70 -> feedback.add("👍 Nice work!")
            result.score >= 50 -> feedback.add("Getting there...")
            else -> feedback.add("Keep practicing!")
        }

        return feedback
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
        _uiState.update { it.copy(isPaused = false, currentlyPlayingPath = path) }  // ADD: currentlyPlayingPath = path

        audioPlayerHelper.play(path) {
            _uiState.update {
                it.copy(
                    currentlyPlayingPath = null,
                    isPaused = false,
                    playbackProgress = 0f
                )
            }
        }
    }

    fun pause() {
        if (_uiState.value.isPaused) {
            audioPlayerHelper.resume()
            _uiState.update { it.copy(isPaused = false) }
        } else {
            audioPlayerHelper.pause()
            _uiState.update { it.copy(isPaused = true) }
        }
    }

    fun stopPlayback() {
        audioPlayerHelper.stop()
        _uiState.update {
            it.copy(
                currentlyPlayingPath = null,
                isPaused = false,
                playbackProgress = 0f
            )
        }
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
                try {
                    File(attempt.attemptFilePath).delete()
                } catch (e: Exception) {
                    Log.w(
                        "AudioViewModel",
                        "Failed to delete attempt file: ${attempt.attemptFilePath}"
                    )
                }
                attempt.reversedAttemptFilePath?.let { path ->
                    try {
                        File(path).delete()
                    } catch (e: Exception) {
                        Log.w("AudioViewModel", "Failed to delete reversed attempt: $path")
                    }
                }
            }
            loadRecordings()
        }
    }

    fun clearAllRecordings() {
        viewModelScope.launch {
            // Step 1: Clear attempts JSON first
            attemptsRepository.clearAllAttempts()
            // Step 2: Clear custom names JSON
            recordingNamesRepository.clearAllCustomNames()
            // Step 3: Delete physical files
            _uiState.value.recordings.forEach { recording ->
                deleteRecording(recording)
            }
            // Step 4: Reload state
            loadRecordings()
        }
    }

    fun deleteAttempt(recordingPath: String, attempt: PlayerAttempt) {
        viewModelScope.launch {
            try {
                File(attempt.attemptFilePath).delete()
            } catch (e: Exception) {
                Log.w("AudioViewModel", "Failed to delete attempt file")
            }
            attempt.reversedAttemptFilePath?.let { path ->
                try {
                    File(path).delete()
                } catch (e: Exception) {
                    Log.w("AudioViewModel", "Failed to delete reversed attempt")
                }
            }

            val updatedRecordings = _uiState.value.recordings.map { recording ->
                if (recording.originalPath == recordingPath) {
                    recording.copy(attempts = recording.attempts.filter { it != attempt })
                } else {
                    recording
                }
            }

            val attemptsMap = updatedRecordings.associate { it.originalPath to it.attempts }
                .filterValues { it.isNotEmpty() }
            attemptsRepository.saveAttempts(attemptsMap)

            _uiState.update { it.copy(recordings = updatedRecordings) }
        }
    }

    fun renamePlayer(recordingPath: String, attempt: PlayerAttempt, newName: String) {
        viewModelScope.launch {
            val updatedRecordings = _uiState.value.recordings.map { recording ->
                if (recording.originalPath == recordingPath) {
                    val updatedAttempts = recording.attempts.map {
                        if (it == attempt) it.copy(playerName = newName) else it
                    }
                    recording.copy(attempts = updatedAttempts)
                } else {
                    recording
                }
            }

            val attemptsMap = updatedRecordings.associate { it.originalPath to it.attempts }
                .filterValues { it.isNotEmpty() }
            attemptsRepository.saveAttempts(attemptsMap)

            _uiState.update { it.copy(recordings = updatedRecordings, attemptToRename = null) }
        }
    }

    /**
     * Override an attempt's score with a player-selected value
     */
    fun overrideAttemptScore(recordingPath: String, attempt: PlayerAttempt, overrideScore: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedRecordings = _uiState.value.recordings.map { recording ->
                if (recording.originalPath == recordingPath) {
                    val updatedAttempts = recording.attempts.map {
                        if (it == attempt) it.copy(finalScore = overrideScore) else it
                    }
                    recording.copy(attempts = updatedAttempts)
                } else {
                    recording
                }
            }

            val attemptsMap = updatedRecordings.associate { it.originalPath to it.attempts }
                .filterValues { it.isNotEmpty() }
            attemptsRepository.saveAttempts(attemptsMap)

            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(recordings = updatedRecordings) }
            }
        }
    }

    fun resetAttemptScore(recordingPath: String, attempt: PlayerAttempt) {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedRecordings = _uiState.value.recordings.map { recording ->
                if (recording.originalPath == recordingPath) {
                    val updatedAttempts = recording.attempts.map {
                        if (it == attempt) it.copy(finalScore = null) else it
                    }
                    recording.copy(attempts = updatedAttempts)
                } else {
                    recording
                }
            }

            val attemptsMap = updatedRecordings.associate { it.originalPath to it.attempts }
                .filterValues { it.isNotEmpty() }
            attemptsRepository.saveAttempts(attemptsMap)

            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(recordings = updatedRecordings) }
            }
        }
    }

    fun clearAttemptToRename() {
        _uiState.update { it.copy(attemptToRename = null) }
    }

    // --- Scroll/Tutorial/Warning UI Controls ---

    fun clearScrollToIndex() {
        _uiState.update { it.copy(scrollToIndex = null) }
    }

    fun dismissTutorial() {
        _uiState.update { it.copy(showTutorial = false) }
    }

    fun completeTutorial() {
        _uiState.update { it.copy(showTutorial = false) }
        // Could persist completion state to DataStore here if needed
    }

    fun dismissQualityWarning() {
        _uiState.update { it.copy(showQualityWarning = false, qualityWarningMessage = "") }
    }

    fun showQualityWarning(message: String) {
        _uiState.update { it.copy(showQualityWarning = true, qualityWarningMessage = message) }
    }

    fun dismissAnalysisToast() {
        _uiState.update { it.copy(showAnalysisToast = false) }
    }

    // 🥚 Easter Egg Methods
    fun dismissEasterEgg() {
        _uiState.update { it.copy(showEasterEgg = false) }
    }

    fun onCpdTapped() {
        val newTaps = _uiState.value.cpdTaps + 1
        if (newTaps >= 7) {
            // Trigger easter egg after 7 taps
            _uiState.update { it.copy(showEasterEgg = true, cpdTaps = 0) }
        } else {
            _uiState.update { it.copy(cpdTaps = newTaps) }
        }
    }

    fun renameRecording(recordingPath: String, newName: String) {
        Log.d("RENAME_DEBUG", "renameRecording called: path=$recordingPath, name=$newName")
        viewModelScope.launch {
            recordingNamesRepository.setCustomName(recordingPath, newName)

            _uiState.update { state ->
                val updatedRecordings = state.recordings.map { recording ->
                    if (recording.originalPath == recordingPath) {
                        recording.copy(name = newName)
                    } else recording
                }
                val updatedNames = state.customNames.toMutableMap()
                updatedNames[recordingPath] = newName

                state.copy(recordings = updatedRecordings, customNames = updatedNames)
            }
        }
    }

    fun ensureScoringReady() {
        viewModelScope.launch {
            if (!_isScoringReady.value) {
                _isScoringReady.value = PhonemeUtils.isReady()
            }
        }
    }

    /**
     * Apply difficulty level internally (no persistence)
     */
    fun applyDifficulty(level: DifficultyLevel) {
        _currentDifficulty.value = level
    }

    /**
     * Update difficulty AND persist to DataStore.
     * Called by UI when user changes difficulty.
     */
    fun updateDifficulty(level: DifficultyLevel) {
        applyDifficulty(level)
        viewModelScope.launch { settingsDataStore.saveDifficultyLevel(level.name) }
    }


    // 🔇 SILENCE TRIMMING: Strip leading/trailing silence for accurate duration gate
    private fun trimSilence(
        samples: FloatArray,
        threshold: Float = AudioConstants.SILENCE_TRIM_THRESHOLD,
    ): FloatArray {
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

        return if (startIdx < endIdx) samples.sliceArray(startIdx until endIdx) else samples
    }

    private fun readAudioFile(path: String): FloatArray {
        return try {
            val file = File(path)
            if (!file.exists()) {
                Log.e("AudioViewModel", "File not found: $path")
                return floatArrayOf()
            }

            val bytes = file.readBytes()
            if (bytes.size < 44) {
                Log.e("AudioViewModel", "File too small for WAV header: ${bytes.size}")
                return floatArrayOf()
            }

            // Skip WAV header (44 bytes) and convert to float
            val audioBytes = bytes.drop(44)
            val samples = FloatArray(audioBytes.size / 2)
            for (i in samples.indices) {
                val low = audioBytes[i * 2].toInt() and 0xFF
                val high = audioBytes[i * 2 + 1].toInt()
                val sample = (high shl 8) or low
                samples[i] = sample / 32768f
            }
            samples
        } catch (e: Exception) {
            Log.e("AudioViewModel", "Error reading audio file: $path", e)
            floatArrayOf()
        }
    }

    // 🧪 BIT (Built-In Test) Functions
    fun runBIT() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    showBitResults = true,
                    bitResults = "Running tests...",
                    bitProgress = 0f
                )
            }

            val results = StringBuilder()

            // Test 1: PhonemeUtils
            results.append("=== PhonemeUtils ===\n")
            val phonemeReady = PhonemeUtils.isReady()
            results.append("Ready: $phonemeReady\n")
            _uiState.update { it.copy(bitProgress = 0.25f) }

            // Test 2: Vosk
            results.append("\n=== Vosk ===\n")
            // 🎯 FIX: Use correct method name
            val voskReady = voskTranscriptionHelper.isReady()
            results.append("Model loaded: $voskReady\n")
            _uiState.update { it.copy(bitProgress = 0.5f) }

            // Test 3: ReverseScoringEngine
            results.append("\n=== ReverseScoringEngine ===\n")
            try {
                val testScore = ReverseScoringEngine.scoreTextOnly("hello", "olleh")
                results.append("Test score (hello/olleh): $testScore%\n")
            } catch (e: Exception) {
                results.append("ERROR: ${e.message}\n")
            }
            _uiState.update { it.copy(bitProgress = 0.75f) }

            // Test 4: Audio paths
            results.append("\n=== Storage ===\n")
            val context = getApplication<Application>()
            val recordingsDir = File(context.filesDir, "recordings")
            results.append("Recordings dir exists: ${recordingsDir.exists()}\n")
            results.append("Recording count: ${recordingsDir.listFiles()?.size ?: 0}\n")
            _uiState.update { it.copy(bitProgress = 1f) }

            _uiState.update { it.copy(bitResults = results.toString()) }
        }
    }

    fun dismissBitResults() {
        _uiState.update { it.copy(showBitResults = false, bitResults = "", bitProgress = 0f) }
    }
}