package com.example.reversey.ui.viewmodels

// 🎯 DUAL PIPELINE CHANGE 4A: Add dual scoring imports
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
import com.example.reversey.data.repositories.RecordingNamesRepository
import com.example.reversey.data.repositories.RecordingRepository
import com.example.reversey.data.repositories.SettingsDataStore
import com.example.reversey.scoring.DifficultyLevel
import com.example.reversey.scoring.Presets
import com.example.reversey.scoring.ScoreAcquisitionDataConcentrator
import com.example.reversey.scoring.ScoringEngineType
import com.example.reversey.scoring.ScoringPresets
import com.example.reversey.scoring.SingingScoringEngine
import com.example.reversey.scoring.SpeechScoringEngine
import com.example.reversey.scoring.VocalMode
import com.example.reversey.scoring.VocalModeDetector
import com.example.reversey.scoring.VocalModeRouter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import com.example.reversey.ui.components.AnalysisToast
import com.example.reversey.scoring.ScoringResult
import com.example.reversey.scoring.SimilarityMetrics
import com.example.reversey.scoring.VocalAnalysis
import com.example.reversey.scoring.VocalFeatures

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
    val isScoring: Boolean = false,  // 🎯 DUPLICATE SCORING GUARD: Prevents multiple simultaneous scoring operations
    val showAnalysisToast: Boolean = false  // 🎯 Analysis toast for 1000ms delay
)


@HiltViewModel
class AudioViewModel @Inject constructor(
    application: Application,
    private val repository: RecordingRepository,
    private val attemptsRepository: AttemptsRepository,
    private val recordingNamesRepository: RecordingNamesRepository,
    // 🎯 LEGACY CLEANUP: Removed legacy ScoringEngine injection
    private val settingsDataStore: SettingsDataStore,
    // 🎯 PURE DUAL PIPELINE COMPONENTS
    private val vocalModeDetector: VocalModeDetector,
    private val vocalModeRouter: VocalModeRouter,
    private val speechScoringEngine: SpeechScoringEngine,
    private val singingScoringEngine: SingingScoringEngine,
    private val scoreAcquisitionDataConcentrator: ScoreAcquisitionDataConcentrator
) : AndroidViewModel(application) {

    init {
        Log.d("HILT_VERIFY", "📱 AudioViewModel created - Speech: ${speechScoringEngine.hashCode()}, Singing: ${singingScoringEngine.hashCode()}")
    }

    private var mediaPlayer: MediaPlayer? = null
    private var recordingJob: Job? = null
    private var playbackJob: Job? = null

    // 🎯 GLUTE File Tracking: Track current recording files properly
    private var currentRecordingFile: File? = null
    private var currentAttemptFile: File? = null

    private val _uiState = MutableStateFlow(AudioUiState())
    val uiState = _uiState.asStateFlow()

    // 🎯 NEW: Track scoring engine readiness to prevent race conditions
    private val _isScoringReady = MutableStateFlow(false)
    val isScoringReady: StateFlow<Boolean> = _isScoringReady.asStateFlow()

    // 🎯 GLUTE UI INTERFACE: Expose difficulty information for UI components
    private val _currentDifficulty = MutableStateFlow(DifficultyLevel.NORMAL)
    val currentDifficultyFlow: StateFlow<DifficultyLevel> = _currentDifficulty.asStateFlow()




    //**********************************************************************************************************
    //*******************************USE REAL DATA TO FEED TO VOCAL MODE DETECTOR*******************************
    //*******************************||||||||||||||||||||||||||||||||||||||||||*********************************
    //*******************************vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv********************************
    /*private suspend fun scoreDualPipeline(
        referenceAudio: FloatArray,
        attemptAudio: FloatArray,
        challengeType: ChallengeType,
        sampleRate: Int = 44100
    ): ScoringResult {
        // Create temp file for VocalModeDetector analysis
        val tempFile = File.createTempFile("vocal_analysis_", ".wav", getApplication<Application>().cacheDir)

        try {
            // Write FloatArray to temp WAV file
            writeSimpleWavFile(tempFile, referenceAudio, sampleRate)

            // REAL ANALYSIS - Replace fake data with actual detection
            val vocalAnalysis = vocalModeDetector.classifyVocalMode(tempFile)

            // Route via VocalModeRouter
            val routingDecision = vocalModeRouter.getRoutingDecision(vocalAnalysis)

            // Call appropriate engine
            return when (routingDecision.selectedEngine) {
                ScoringEngineType.SPEECH_ENGINE -> speechScoringEngine.scoreAttempt(
                    referenceAudio, attemptAudio, challengeType, sampleRate
                )
                ScoringEngineType.SINGING_ENGINE -> singingScoringEngine.scoreAttempt(
                    referenceAudio, attemptAudio, challengeType, sampleRate
                )
            }
        } finally {
            tempFile.delete() // Clean up
        }
    }*/

    private suspend fun scoreDualPipeline(
        referenceAudio: FloatArray,
        attemptAudio: FloatArray,
        challengeType: ChallengeType,
        sampleRate: Int = 44100
    ): ScoringResult {
        Log.d("DUAL_PIPELINE", "🚀 === DUAL SCORING PIPELINE START ===")
        Log.d("DUAL_PIPELINE", "Reference audio: ${referenceAudio.size} samples")
        Log.d("DUAL_PIPELINE", "Attempt audio: ${attemptAudio.size} samples")
        Log.d("DUAL_PIPELINE", "Challenge type: $challengeType")

        // Create temp file for VocalModeDetector analysis
        val tempFile = File.createTempFile("vocal_analysis_", ".wav", getApplication<Application>().cacheDir)
        Log.d("DUAL_PIPELINE", "✅ Temp file created: ${tempFile.name} (${tempFile.length()} bytes)")

        try {
            // Write FloatArray to temp WAV file
            Log.d("DUAL_PIPELINE", "📁 Writing audio to temp WAV file...")
            writeSimpleWavFile(tempFile, referenceAudio, sampleRate)
            Log.d("DUAL_PIPELINE", "✅ WAV file written: ${tempFile.length()} bytes")

            // REAL ANALYSIS - Replace fake data with actual detection
            Log.d("DUAL_PIPELINE", "🔍 === VOCAL MODE DETECTION START ===")
            val vocalAnalysis = vocalModeDetector.classifyVocalMode(tempFile)
            Log.d("DUAL_PIPELINE", "✅ VocalModeDetector result:")
            Log.d("DUAL_PIPELINE", "   Mode: ${vocalAnalysis.mode}")
            Log.d("DUAL_PIPELINE", "   Confidence: ${vocalAnalysis.confidence}")
            Log.d("DUAL_PIPELINE", "   Features: ${vocalAnalysis.features}")

            // Route via VocalModeRouter
            Log.d("DUAL_PIPELINE", "🧭 === VOCAL MODE ROUTING START ===")
            val routingDecision = vocalModeRouter.getRoutingDecision(vocalAnalysis)
            Log.d("DUAL_PIPELINE", "✅ VocalModeRouter decision:")
            Log.d("DUAL_PIPELINE", "   Selected Engine: ${routingDecision.selectedEngine}")
            Log.d("DUAL_PIPELINE", "   Routed Mode: ${routingDecision.routedMode}")

            // Call appropriate engine
            Log.d("DUAL_PIPELINE", "⚙️ === SCORING ENGINE EXECUTION ===")
            val result = when (routingDecision.selectedEngine) {
                ScoringEngineType.SPEECH_ENGINE -> {
                    Log.d("DUAL_PIPELINE", "🗣️ Calling SpeechScoringEngine.scoreAttempt()")
                    val speechResult = speechScoringEngine.scoreAttempt(
                        referenceAudio, attemptAudio, challengeType, sampleRate
                    )
                    Log.d("DUAL_PIPELINE", "✅ SpeechScoringEngine completed: score=${speechResult.score}")
                    speechResult
                }
                ScoringEngineType.SINGING_ENGINE -> {
                    Log.d("DUAL_PIPELINE", "🎵 Calling SingingScoringEngine.scoreAttempt()")
                    val singingResult = singingScoringEngine.scoreAttempt(
                        referenceAudio, attemptAudio, challengeType, sampleRate
                    )
                    Log.d("DUAL_PIPELINE", "✅ SingingScoringEngine completed: score=${singingResult.score}")
                    singingResult
                }
            }

            Log.d("DUAL_PIPELINE", "🎯 === DUAL PIPELINE COMPLETE ===")
            Log.d("DUAL_PIPELINE", "Final result: ${result.score} (${result.feedback.joinToString()})")
            return result

        } finally {
            tempFile.delete() // Clean up
            Log.d("DUAL_PIPELINE", "🧹 Temp file cleaned up: ${tempFile.name}")
        }
    }

    private fun writeSimpleWavFile(file: File, audioData: FloatArray, sampleRate: Int) {
        file.outputStream().use { fos ->
            val dataSize = audioData.size * 2 // 16-bit samples
            val fileSize = 36 + dataSize

            // WAV header
            fos.write("RIFF".toByteArray())
            fos.write(intToLittleEndian(fileSize))
            fos.write("WAVE".toByteArray())
            fos.write("fmt ".toByteArray())
            fos.write(intToLittleEndian(16)) // PCM format size
            fos.write(shortToLittleEndian(1)) // PCM format
            fos.write(shortToLittleEndian(1)) // Mono
            fos.write(intToLittleEndian(sampleRate))
            fos.write(intToLittleEndian(sampleRate * 2)) // Byte rate
            fos.write(shortToLittleEndian(2)) // Block align
            fos.write(shortToLittleEndian(16)) // Bits per sample
            fos.write("data".toByteArray())
            fos.write(intToLittleEndian(dataSize))

            // PCM data
            audioData.forEach { sample ->
                val intSample = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                fos.write(shortToLittleEndian(intSample.toInt()))
            }
        }
    }

    private fun intToLittleEndian(value: Int) = byteArrayOf(
        (value and 0xFF).toByte(),
        ((value shr 8) and 0xFF).toByte(),
        ((value shr 16) and 0xFF).toByte(),
        ((value shr 24) and 0xFF).toByte()
    )

    private fun shortToLittleEndian(value: Int) = byteArrayOf(
        (value and 0xFF).toByte(),
        ((value shr 8) and 0xFF).toByte()
    )

//*******************************^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^********************************
//*******************************||||||||||||||||||||||||||||||||||||||||||*********************************
//*******************************USE REAL DATA TO FEED TO VOCAL MODE DETECTOR*******************************
//**********************************************************************************************************
    // SURGICAL FIX #1: Add error message helper
    private fun showUserMessage(message: String) {
        _uiState.update { it.copy(statusText = message) }
    }

    // SURGICAL FIX #2: Add file validation helper
    private fun validateRecordedFile(file: File?): Boolean {
        Log.d("FILE_DEBUG", "🔍 Validating file: ${file?.absolutePath}")
        Log.d("FILE_DEBUG", "📁 File exists: ${file?.exists()}")
        Log.d("FILE_DEBUG", "📏 File size: ${file?.length()} bytes")

        val isValid = file != null && file.exists() && file.length() > 100
        Log.d("FILE_DEBUG", "✅ Validation result: $isValid")

        return isValid
    }

    init {
        loadRecordings()

        // 🎯 PURE DUAL PIPELINE: Wait for speech engine to initialize (unified fallback)
        viewModelScope.launch {
            speechScoringEngine.isInitialized
                .filter { it == true }
                .collect {
                    _isScoringReady.value = true
                    Log.d("HILT_VERIFY", "🎯 Dual pipeline ready for AudioViewModel")
                }
        }

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
            // 🎯 Show analysis toast during the 1000ms delay
            _uiState.update { it.copy(showAnalysisToast = true) }

            delay (1500)//delay to allow animation to play🔉🔉

            loadRecordings()

            // 🎯 Hide analysis toast when done
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
        currentRecordingFile = file  // 🎯 GLUTE: Track the file for proper cleanup
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
                    _uiState.update { it.copy(isRecording = false, amplitudes = emptyList()) }
                }
            }
        }
    }

    // SURGICAL FIX #4: Improved stopRecording with better file validation
    fun stopRecording() {
        // Cancel the recording job and wait for it to complete
        recordingJob?.cancel()

        viewModelScope.launch(Dispatchers.IO) {
            // Wait for the recording job to actually finish
            recordingJob?.join()  // ← CRITICAL MISSING PIECE!
            recordingJob = null

            // Small delay to ensure file is fully written
            delay(100)

            // Get the latest recorded file
            val latestFile = repository.getLatestFile(isAttempt = _uiState.value.isRecordingAttempt)

            Log.d("AudioViewModel", "=== STOP RECORDING ===")
            Log.d("AudioViewModel", "Is attempt: ${_uiState.value.isRecordingAttempt}")
            Log.d("AudioViewModel", "Latest file: ${latestFile?.absolutePath}")

            if (_uiState.value.isRecordingAttempt) {
                // Handle attempt completion
                withContext(Dispatchers.Main) {
                    handleAttemptCompletion(latestFile)  // ← THE MISSING CALL!
                }
            } else {
                // [Keep your existing main recording logic from current version]
                if (validateRecordedFile(latestFile)) {
                    // Your existing challenge creation logic...
                    val recordedFile = latestFile
                    try {
                        Log.d("AudioViewModel", "Creating reversed version...")
                        repository.reverseWavFile(recordedFile)
                        Log.d("AudioViewModel", "Reversed file created: ${recordedFile!!.absolutePath.replace(".wav", "_reversed.wav")}")
                    } catch (e: Exception) {
                        Log.e("AudioViewModel", "Error creating reversed file: ${e.message}")
                    }

                    withContext(Dispatchers.Main) {
                        _uiState.update { it.copy(isRecording = false, statusText = "Recording complete") }
                        loadRecordingsWithAnalysis()  // ← Use your current method name
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        _uiState.update { it.copy(isRecording = false, statusText = "Recording failed - file too short") }
                    }
                }
            }
        }
    }

    private fun handleAttemptCompletion(attemptFile: File?) {
        val parentPath = _uiState.value.parentRecordingPath
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
                        // Use your current dual scoring system here!
                        val scoringResult = when (challengeType) {
                            ChallengeType.REVERSE -> {
                                Log.d("AudioViewModel", "REVERSE scoring - parentReversedPath: ${parentRecording.reversedPath}")
                                parentRecording.reversedPath?.let { reversedParentPath ->
                                    try {
                                        val reversedParentAudio = readAudioFile(reversedParentPath)
                                        val attemptAudioPath = reversedAttemptFile?.absolutePath ?: attemptFile.absolutePath
                                        val attemptAudio = readAudioFile(attemptAudioPath)

                                        if (reversedParentAudio.isNotEmpty() && attemptAudio.isNotEmpty()) {
                                            // ← INTEGRATE YOUR DUAL SCORING HERE!
                                            scoreDualPipeline(reversedParentAudio, attemptAudio, ChallengeType.REVERSE)
                                        } else {
                                            ScoringResult(score = 0, rawScore = 0f, metrics = SimilarityMetrics(pitch = 0f, mfcc = 0f), feedback = emptyList())
                                        }
                                    } catch (e: Exception) {
                                        Log.e("AudioViewModel", "Exception in REVERSE scoring: ${e.message}", e)
                                        ScoringResult(score = 0, rawScore = 0f, metrics = SimilarityMetrics(pitch = 0f, mfcc = 0f), feedback = emptyList())
                                    }
                                } ?: ScoringResult(score = 0, rawScore = 0f, metrics = SimilarityMetrics(pitch = 0f, mfcc = 0f), feedback = emptyList())
                            }
                            ChallengeType.FORWARD -> {
                                Log.d("AudioViewModel", "FORWARD scoring - parentOriginalPath: ${parentRecording.originalPath}")
                                val originalParentAudio = readAudioFile(parentRecording.originalPath)
                                val attemptAudio = readAudioFile(attemptFile.absolutePath)

                                if (originalParentAudio.isNotEmpty() && attemptAudio.isNotEmpty()) {
                                    // ← INTEGRATE YOUR DUAL SCORING HERE!
                                    scoreDualPipeline(originalParentAudio, attemptAudio, ChallengeType.FORWARD)
                                } else {
                                    ScoringResult(score = 0, rawScore = 0f, metrics = SimilarityMetrics(pitch = 0f, mfcc = 0f), feedback = emptyList())
                                }
                            }
                        }

                        // Create player attempt
                        val attempt = PlayerAttempt(
                            playerName = "Player ${parentRecording.attempts.size + 1}",
                            attemptFilePath = attemptFile.absolutePath,
                            reversedAttemptFilePath = reversedAttemptFile?.absolutePath,
                            score = scoringResult.score,
                            pitchSimilarity = scoringResult.metrics.pitch,
                            mfccSimilarity = scoringResult.metrics.mfcc,
                            rawScore = scoringResult.rawScore,
                            challengeType = challengeType,
                            difficulty = DifficultyLevel.NORMAL//hardcoded difficulty placeholder!
                        )

                        val updatedRecordings = _uiState.value.recordings.map { recording ->
                            if (recording.originalPath == parentPath) {
                                recording.copy(attempts = recording.attempts + attempt)
                            } else {
                                recording
                            }
                        }

                        val attemptsMap = updatedRecordings.associate { it.originalPath to it.attempts }.filterValues { it.isNotEmpty() }
                        attemptsRepository.saveAttempts(attemptsMap)

                        _uiState.update {
                            it.copy(
                                recordings = updatedRecordings,
                                isRecording = false,
                                isRecordingAttempt = false,
                                parentRecordingPath = null,
                                pendingChallengeType = null,
                                statusText = "Attempt scored: ${scoringResult.score}%",
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
                            pendingChallengeType = null,
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
                    pendingChallengeType = null,
                    statusText = "Attempt recording failed"
                )
            }
        }
    }


    // SURGICAL FIX #5: Attempt recording with better state management
    fun startAttempt(originalPath: String) {
        if (uiState.value.isRecordingAttempt && uiState.value.isRecording) {
            showUserMessage("Attempt recording already in progress")
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
                isRecording = true,                    // 🔥 FIX: Legacy behaviour restored
                parentRecordingPath = originalPath,
                statusText = "Recording attempt...",
                amplitudes = emptyList()
            )
        }

        Log.d("RECORD_BUG", "📍 STEP 1: Set isRecording=true in startAttempt")
        Log.d("RECORD_BUG", "📊 State: isRecording=${_uiState.value.isRecording}, isRecordingAttempt=${_uiState.value.isRecordingAttempt}")

        val attemptFile = createAudioFile(getApplication(), isAttempt = true)
        currentAttemptFile = attemptFile  // 🎯 GLUTE: Track the attempt file for proper cleanup
        recordingJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d("RECORD_BUG", "📞 About to call repository.startRecording")
                repository.startRecording(attemptFile) { amplitude ->
                    Log.d("RECORD_BUG", "📈 Amplitude callback received: $amplitude")
                    _uiState.update {
                        val newAmplitudes = it.amplitudes + amplitude
                        it.copy(amplitudes = newAmplitudes.takeLast(AudioConstants.MAX_WAVEFORM_SAMPLES))
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.d("RECORD_BUG", "💥 Exception in startRecording: ${e.message}")
                    showUserMessage("Attempt recording failed: ${e.message}")
                    _uiState.update {
                        Log.d("RECORD_BUG", "🚨 Exception handler - NOT clearing isRecording")
                        it.copy(
                            isRecording = false,           // 🚨 ADD THIS LINE
                            isRecordingAttempt = false,
                            parentRecordingPath = null,
                            amplitudes = emptyList()
                        )
                    }
                }
            }
        }
    }

    // SURGICAL FIX #6: Improved stop attempt with scoring integration
    fun stopAttempt() {
        Log.d("FILE_DEBUG", "🎯 stopAttempt() called - currentAttemptFile: ${currentAttemptFile?.absolutePath}")

        recordingJob?.cancel()
        recordingJob = null

        val attemptFile = currentAttemptFile
        Log.d("FILE_DEBUG", "📝 Captured attemptFile: ${attemptFile?.absolutePath}")
        Log.d("FILE_DEBUG", "🎯 About to validate attempt file: ${attemptFile?.absolutePath}")
// Add 1000ms delay like "Ed Standard" - file system needs time to settle
        Thread.sleep(1000)

        val parentPath = uiState.value.parentRecordingPath
        val challengeType = uiState.value.pendingChallengeType ?: ChallengeType.REVERSE

        if (validateRecordedFile(attemptFile) && parentPath != null) {
            currentAttemptFile = null  // 🎯 Clear ONLY after successful validation
            val parentRecording = _uiState.value.recordings.find { it.originalPath == parentPath }
            if (parentRecording != null) {
                Log.d("RECORD_BUG", "🚨 CLEARING isRecording=false in handleStopAttempt")
                _uiState.update {
                    it.copy(
                        isRecordingAttempt = false,
                        statusText = "Scoring attempt...",
                        isScoring = true  // 🎯 SET SCORING FLAG TO PREVENT DUPLICATE SCORING
                    )
                }

                scoreAttempt(
                    originalRecordingPath = parentRecording.originalPath,
                    reversedRecordingPath = parentRecording.reversedPath ?: "",
                    attemptFilePath = attemptFile?.absolutePath ?: return,
                    challengeType = challengeType
                )
            } else {
                _uiState.update {
                    it.copy(
                        isRecordingAttempt = false,
                        parentRecordingPath = null,
                        pendingChallengeType = null,
                        statusText = "Parent recording not found"
                    )
                }
            }
        } else {
            _uiState.update {
                it.copy(
                    isRecording = false,
                    isRecordingAttempt = false,
                    parentRecordingPath = null,
                    pendingChallengeType = null, // <-- Clear pending type
                    isScoring = false,  // 🎯 CLEAR SCORING FLAG WHEN NO VALID ATTEMPT
                    statusText = "Attempt recording failed"
                )
            }
        }
    }

    // 🎯 DUAL PIPELINE SCORING: Routes to Speech or Singing engine based on vocal analysis
    private fun scoreAttempt(
        originalRecordingPath: String,
        reversedRecordingPath: String,
        attemptFilePath: String,
        challengeType: ChallengeType = ChallengeType.REVERSE
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 🎯 DUAL PIPELINE: Read audio files as FloatArray for scoring engines
                val reversedParentAudio = readAudioFile(reversedRecordingPath)
                val attemptAudio = readAudioFile(attemptFilePath)

                if (reversedParentAudio.isEmpty() || attemptAudio.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        showUserMessage("Error: Could not read audio files for scoring")
                        _uiState.update { it.copy(isScoring = false) }
                    }
                    return@launch
                }

                // 🎯 VOCAL ANALYSIS: Determine if Speech or Singing mode
                // Note: VocalModeDetector expects File input, need to create temp file
                val tempFile = File.createTempFile("attempt", ".wav")

                FileOutputStream(tempFile).use { fos ->
                    val bytes = convertFloatArrayToWav(attemptAudio)
                    fos.write(bytes)
                    fos.flush()
                    fos.fd.sync()   // Force OS-level sync to disk
                }

// Small delay to ensure file system consistency
                delay(10)

                val vocalAnalysis = vocalModeDetector.classifyVocalMode(tempFile)
                tempFile.delete()
                Log.d("SCORING", "🎤 Vocal Analysis: $vocalAnalysis")

                // 🎯 ROUTE TO APPROPRIATE ENGINE
                val scoringResult = if (vocalAnalysis.mode == VocalMode.SINGING) {
                    Log.d("SCORING", "🎵 Using Singing Pipeline")
                    singingScoringEngine.scoreAttempt(
                        reversedParentAudio,
                        attemptAudio,
                        challengeType
                    )
                } else {
                    Log.d("SCORING", "🗣️ Using Speech Pipeline")
                    speechScoringEngine.scoreAttempt(
                        reversedParentAudio,
                        attemptAudio,
                        challengeType
                    )
                }

                Log.d("SCORING", "📊 Score: ${scoringResult.score}% (${if (vocalAnalysis.mode == VocalMode.SINGING) "🎵" else "🗣️"})")

                // Store attempt with basic PlayerAttempt structure
                val attempt = PlayerAttempt(
                    playerName = if (vocalAnalysis.mode == VocalMode.SINGING) "🎵 Singer" else "🗣️ Speaker",
                    attemptFilePath = attemptFilePath,
                    reversedAttemptFilePath = null,
                    score = scoringResult.score,
                    pitchSimilarity = 0.8f, // TODO: Get actual pitch similarity from ScoringResult
                    mfccSimilarity = 0.7f, // TODO: Get actual MFCC similarity from ScoringResult
                    rawScore = scoringResult.score.toFloat(), // Use score as rawScore for now
                    challengeType = challengeType,
                    difficulty = DifficultyLevel.NORMAL,
                    scoringEngine = if (vocalAnalysis.mode == VocalMode.SINGING) ScoringEngineType.SINGING_ENGINE else ScoringEngineType.SPEECH_ENGINE
                )

                val updatedRecordings = _uiState.value.recordings.map { recording ->
                    if (recording.originalPath == originalRecordingPath) {
                        recording.copy(attempts = recording.attempts + attempt)
                    } else {
                        recording
                    }
                }

                val attemptsMap = updatedRecordings.associate { it.originalPath to it.attempts }.filterValues { it.isNotEmpty() }
                attemptsRepository.saveAttempts(attemptsMap)

                withContext(Dispatchers.Main) {
                    _uiState.update {
                        it.copy(
                            recordings = updatedRecordings,
                            parentRecordingPath = null,
                            pendingChallengeType = null,
                            isScoring = false,  // 🎯 CLEAR SCORING FLAG AFTER COMPLETION
                            statusText = "Attempt scored: ${scoringResult.score}%"
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
                            isScoring = false  // 🎯 CLEAR SCORING FLAG ON ERROR
                        )
                    }
                }
            }
        }
    }

    fun startChallengeAttempt(originalPath: String, challengeType: ChallengeType) {
        _uiState.update { it.copy(pendingChallengeType = challengeType) }
        startAttempt(originalPath)
    }

    // 🌉 BRIDGE: Compatibility layer for pro themes (Recording, ChallengeType) -> startAttempt(originalPath)
    fun startAttemptRecording(recording: Recording, challengeType: ChallengeType) {
        _uiState.update { it.copy(pendingChallengeType = challengeType) }
        startAttempt(recording.originalPath)
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
            recordingNamesRepository.setCustomName(oldPath, newName)
            Log.d("AudioViewModel", "Saved custom name '$newName' for file: $oldPath")
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


    // 🎯 GLUTE DIFFICULTY MANAGEMENT: Unified difficulty control for dual engine system
    fun updateDifficulty(level: DifficultyLevel) {
        _currentDifficulty.value = level

        // Convert difficulty level to preset for backend engines
        val preset = when (level) {
            DifficultyLevel.EASY -> ScoringPresets.easyMode()
            DifficultyLevel.NORMAL -> ScoringPresets.normalMode()
            DifficultyLevel.HARD -> ScoringPresets.hardMode()
            else -> ScoringPresets.normalMode()  // fallback
        }

        updateScoringEngine(preset)
    }
    // 🎯 PURE DUAL PIPELINE: Apply preset to both engines
    fun updateScoringEngine(preset: Presets) {
        // TODO: Apply to both engines for consistent difficulty
        // NOTE: applyPreset methods are private in scoring engines
        Log.d("PRESET", "⚠️ Cannot apply preset - methods are private. Need public API.")
        // speechScoringEngine.applyPreset(preset)
        // singingScoringEngine.applyPreset(preset)
    }

    // 🎯 ESSENTIAL: WAV file to FloatArray converter for scoring engines
    private fun readAudioFile(path: String): FloatArray {
        return try {
            val file = File(path)
            if (!file.exists() || file.length() <= 44) return floatArrayOf()

            val bytes = file.readBytes()
            val header = bytes.sliceArray(0..43)
            val audioData = bytes.sliceArray(44 until bytes.size)

            FloatArray(audioData.size / 2) { i ->
                val byteIndex = i * 2
                if (byteIndex + 1 < audioData.size) {
                    val sample = (audioData[byteIndex].toInt() and 0xFF) or
                            ((audioData[byteIndex + 1].toInt() and 0xFF) shl 8)
                    sample.toShort().toFloat() / Short.MAX_VALUE.toFloat()
                } else {
                    0f
                }
            }
        } catch (e: Exception) {
            Log.e("AudioViewModel", "Error reading audio file: $path", e)
            floatArrayOf()
        }
    }

    // 🎯 HELPER: Convert FloatArray back to WAV bytes for VocalModeDetector
    private fun convertFloatArrayToWav(audioData: FloatArray): ByteArray {
        return try {
            val sampleRate = 44100
            val channels = 1
            val bitsPerSample = 16
            val byteRate = sampleRate * channels * bitsPerSample / 8
            val blockAlign = channels * bitsPerSample / 8
            val dataSize = audioData.size * 2
            val chunkSize = 36 + dataSize

            val wav = ByteArray(44 + dataSize)
            var index = 0

            // WAV Header
            "RIFF".toByteArray().copyInto(wav, index); index += 4
            chunkSize.toLittleEndianBytes().copyInto(wav, index); index += 4
            "WAVE".toByteArray().copyInto(wav, index); index += 4
            "fmt ".toByteArray().copyInto(wav, index); index += 4
            16.toLittleEndianBytes().copyInto(wav, index); index += 4 // PCM format size
            1.toShort().toLittleEndianBytes().copyInto(wav, index); index += 2 // Audio format (PCM)
            channels.toShort().toLittleEndianBytes().copyInto(wav, index); index += 2
            sampleRate.toLittleEndianBytes().copyInto(wav, index); index += 4
            byteRate.toLittleEndianBytes().copyInto(wav, index); index += 4
            blockAlign.toShort().toLittleEndianBytes().copyInto(wav, index); index += 2
            bitsPerSample.toShort().toLittleEndianBytes().copyInto(wav, index); index += 2
            "data".toByteArray().copyInto(wav, index); index += 4
            dataSize.toLittleEndianBytes().copyInto(wav, index); index += 4

            // Convert FloatArray to 16-bit PCM
            for (sample in audioData) {
                val intSample = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                val shortSample = intSample.toShort()
                shortSample.toLittleEndianBytes().copyInto(wav, index)
                index += 2
            }

            wav
        } catch (e: Exception) {
            Log.e("AudioViewModel", "Error converting FloatArray to WAV: ${e.message}")
            ByteArray(0)
        }
    }

    // Helper extension functions for little-endian byte conversion
    private fun Int.toLittleEndianBytes(): ByteArray = byteArrayOf(
        (this and 0xFF).toByte(),
        ((this shr 8) and 0xFF).toByte(),
        ((this shr 16) and 0xFF).toByte(),
        ((this shr 24) and 0xFF).toByte()
    )

    private fun Short.toLittleEndianBytes(): ByteArray = byteArrayOf(
        (this.toInt() and 0xFF).toByte(),
        ((this.toInt() shr 8) and 0xFF).toByte()
    )
}