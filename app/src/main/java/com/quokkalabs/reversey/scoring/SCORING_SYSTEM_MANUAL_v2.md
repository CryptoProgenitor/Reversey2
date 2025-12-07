# ReVerseY Scoring System Manual
**Version 22.1.0** | Complete Technical Documentation

---

## Table of Contents

1. [System Overview](#system-overview)
2. [Recording Flow](#recording-flow)
3. [Challenge & Attempt Flow](#challenge--attempt-flow)
4. [Vocal Mode Detection](#vocal-mode-detection)
5. [Dual-Pipeline Scoring Architecture](#dual-pipeline-scoring-architecture)
6. [Speech Scoring Engine](#speech-scoring-engine)
7. [Singing Scoring Engine](#singing-scoring-engine)
8. [Difficulty Levels](#difficulty-levels)
9. [Score Calculation](#score-calculation)
10. [Garbage Detection](#garbage-detection)
11. [Technical Implementation](#technical-implementation)
12. [Debugging & Troubleshooting](#debugging--troubleshooting)

---

## System Overview

ReVerseY is an audio challenge app that tests users' ability to replicate recordings (either forward or reversed). The scoring system uses advanced audio analysis to evaluate attempts with **separate optimizations for speech and singing**.

### Core Principles

- **GLUTE Architecture**: Polymorphic component design with clear separation of concerns
- **Dual-Pipeline Scoring**: Separate engines optimized for speech vs. singing
- **Memory Safety**: 10MB file size limit to prevent OutOfMemory errors
- **Type-Safe Events**: Sealed classes for all internal communication
- **Real-Time Analysis**: In-memory processing without temporary files

### Key Components

```
User Recording → Audio Processing → Vocal Detection → Routing → Scoring Engine → Result
                                         ↓                ↓
                                  Speech/Singing    Speech Engine
                                                   Singing Engine
```

---

## Recording Flow

### 1. User Initiates Recording

**Location**: `AudioViewModel.kt:startRecording()`

```kotlin
// User taps record button
// Permission check performed
// AudioRecorderHelper.start() called
```

**Process**:
1. **Permission Check**: Verifies `RECORD_AUDIO` permission
2. **File Creation**: Creates temporary WAV file in app storage
3. **Recorder Start**: Initializes `AudioRecord` at 44.1kHz, 16-bit PCM, Mono
4. **UI Update**: Displays recording state with waveform visualizer

### 2. Recording in Progress

**Location**: `AudioRecorderHelper.kt:writeAudioDataToFile()`

**Real-Time Operations**:
- Reads audio buffer (~10 times/second)
- Calculates amplitude for waveform visualization
- Checks duration limits (throttled to 1/second)
- Writes PCM data to file

**Safety Limits**:
```kotlin
MAX_RECORDING_DURATION_MS = ~118.8 seconds (~2 minutes)
WARNING_DURATION_MS = ~107 seconds (90% threshold)
MAX_LOADABLE_AUDIO_BYTES = 10MB
```

**Events**:
- `RecorderEvent.Warning` → "Approaching recording limit" toast
- `RecorderEvent.Stop` → Automatic stop when limit reached

### 3. User Stops Recording

**Location**: `AudioViewModel.kt:stopRecording()`

**Process**:
1. **Stop AudioRecord**: Halts microphone input
2. **Add WAV Header**: Writes RIFF/WAV header to raw PCM data
3. **File Validation**: Checks file size > 1KB
4. **Audio Reversal**: Creates reversed version using FFmpeg
5. **Vocal Analysis**: Detects speech vs. singing
6. **Save to Repository**: Stores recording with metadata

**Optimistic UI**:
- Recording appears instantly with temporary key
- Replaced with actual recording after processing completes
- Prevents LazyColumn crashes from duplicate keys

---

## Challenge & Attempt Flow

### Challenge Types

```kotlin
enum class ChallengeType {
    FORWARD,  // Replicate the original recording
    REVERSE   // Replicate the reversed recording
}
```

### 1. Starting a Challenge

**Location**: `AudioViewModel.kt:startAttempt()`

**User Action**: Taps game mode button on a recording card

**Process**:
1. User selects challenge type (Forward/Reverse)
2. `startAttemptRecording()` called with parent recording path
3. State updated: `isRecordingAttempt = true`
4. Parent recording path stored for later scoring

### 2. Recording the Attempt

**Same flow as normal recording** but with `isRecordingAttempt = true`

- No audio reversal performed
- No vocal analysis on attempt (analyzed during scoring)
- Attempt file saved with parent reference

### 3. Stopping the Attempt

**Location**: `AudioViewModel.kt:stopAttempt()`

**Process**:
1. **Stop Recording**: AudioRecorderHelper.stop()
2. **Null Safety Check**: Validates attempt file exists and is valid
3. **Parent Validation**: Ensures parent recording still exists
4. **Score Attempt**: Immediately triggers scoring pipeline
5. **UI Update**: Shows "Scoring attempt..." status

**Critical Fix (v20.0.0e)**:
```kotlin
// ✅ BEFORE (dangerous):
attemptFilePath = attemptFile!!.absolutePath

// ✅ AFTER (safe):
if (attemptFile != null && validateRecordedFile(attemptFile) && parentPath != null) {
    attemptFilePath = attemptFile.absolutePath  // Smart cast!
}
```

---

## Vocal Mode Detection

**Location**: `VocalModeDetector.kt`

### Purpose

Automatically classifies audio as **SPEECH** or **SINGING** to route to the appropriate scoring engine.

### Detection Algorithm

#### Step 1: Audio Preprocessing

```kotlin
classifyVocalMode(rawAudioData: FloatArray, sampleRate: Int)
```

1. **Trim Leading Silence**: Removes quiet intro
2. **Skip Mic Pop**: Ignores first 100ms
3. **Size Validation**: Requires minimum 2048 samples

#### Step 2: Feature Extraction

Processes audio in frames (1024 samples, 512 hop):

**Features Extracted**:
- **Pitch (YIN Algorithm)**: Fundamental frequency for each frame
- **MFCC (Mel-Frequency Cepstral Coefficients)**: Voice timbre characteristics

#### Step 3: Vocal Feature Analysis

```kotlin
data class VocalFeatures(
    val pitchStability: Float,      // How consistent is the pitch?
    val pitchContour: Float,        // How much pitch movement?
    val mfccSpread: Float,          // How varied is the timbre?
    val voicedRatio: Float          // Percentage of voiced frames
)
```

**Pitch Stability** (0-1):
- High = singing (stable notes)
- Low = speech (varying pitch)

**Pitch Contour** (0-1):
- High = singing (melodic movement)
- Low = speech (flat intonation)

**MFCC Spread** (0-1):
- High = speech (varied pronunciation)
- Low = singing (consistent vowels)

**Voiced Ratio** (0-1):
- High = singing (continuous vocalization)
- Low = speech (pauses, consonants)

#### Step 4: Classification

```kotlin
speechScore = (1 - pitchStability) * 0.4 +
              (1 - pitchContour) * 0.3 +
              mfccSpread * 0.1

singingScore = pitchStability * 0.2 +
               pitchContour * 0.3 +
               voicedRatio * 0.5
```

**Thresholds**:
- `speechConfidenceThreshold = 0.2`
- `singingConfidenceThreshold = 0.4`

**Result**:
```kotlin
VocalAnalysis(
    mode: VocalMode,        // SPEECH, SINGING, or UNKNOWN
    confidence: Float,      // 0-1 confidence score
    features: VocalFeatures // Detailed analysis
)
```

### Example Classifications

| Audio Type | Pitch Stability | Pitch Contour | Voiced Ratio | Result |
|------------|----------------|---------------|--------------|--------|
| Normal Speech | 0.2 | 0.3 | 0.5 | **SPEECH** |
| Singing Scale | 0.8 | 0.7 | 0.9 | **SINGING** |
| Monotone Hum | 0.9 | 0.1 | 0.8 | SINGING (low confidence) |
| Shouting | 0.3 | 0.2 | 0.6 | **SPEECH** |

---

## Dual-Pipeline Scoring Architecture

### Overview

ReVerseY uses **two separate scoring engines** optimized for different vocal modes, with **reference-mode binding** to ensure consistent processing (v22.1.0):

```
Reference Recording
     │
     └──► Recording.vocalAnalysis.mode (stored at creation time)
                    │
                    ▼
         ┌─────────────────────────────────────────┐
         │     VocalScoringOrchestrator            │
         │                                         │
         │  1. Get referenceVocalMode from parent  │ ◄── Uses stored metadata
         │  2. Analyze attempt (UI feedback only)  │
         │  3. Select engine using REFERENCE mode  │ ◄── KEY FIX (v22.1.0)
         │  4. Route to appropriate engine         │
         └─────────────────────────────────────────┘
                           │
              ┌────────────┴────────────┐
              ↓                         ↓
     SpeechScoringEngine       SingingScoringEngine
     (speech-optimized)        (music-optimized)
              ↓                         ↓
              └────────────┬────────────┘
                           ↓
                      ScoringResult
                      (includes attemptAnalysis for UI)
```

### Reference-Mode Binding (v22.1.0)

**Problem Solved**: Edge-case recordings near the 0.4 singing threshold could be classified differently between reference and attempt, causing cross-engine contamination.

**Example**:
- Reference "hello hello" → singingScore = 0.35 → SPEECH
- Attempt "hello hello" → singingScore = 0.48 → SINGING (tiny delivery difference)
- Different engines use different alignment algorithms
- MFCC similarity drops from ~95% to ~48.5%
- Content penalty triggers → 0% score

**Solution**: Engine selection uses the **reference's stored mode**, not the attempt's detected mode.

### Vocal Mode Router

**Location**: `VocalModeRouter.kt`

**Purpose**: Pure routing logic—no audio processing

```kotlin
fun getRoutingDecision(vocalAnalysis: VocalAnalysis): VocalModeRoutingDecision {
    val engineType = when (vocalAnalysis.mode) {
        VocalMode.SPEECH  → SPEECH_ENGINE
        VocalMode.SINGING → SINGING_ENGINE
        VocalMode.UNKNOWN → SPEECH_ENGINE  // Fallback
    }

    return VocalModeRoutingDecision(
        selectedEngine = engineType,
        routedMode = vocalAnalysis.mode
    )
}
```

### Scoring Orchestrator

**Location**: `VocalScoringOrchestrator.kt`

**Main Entry Point** (v22.1.0):
```kotlin
suspend fun scoreAttempt(
    referenceAudio: FloatArray,           // Original/reversed recording
    attemptAudio: FloatArray,             // User's attempt
    challengeType: ChallengeType,         // FORWARD or REVERSE
    difficulty: DifficultyLevel,          // EASY, NORMAL, or HARD
    referenceVocalMode: VocalMode? = null, // ◄── NEW: Reference's stored mode
    sampleRate: Int = 44100
): ScoringResult
```

**Process** (v22.1.0):
1. **Analyze Attempt**: Classify user's vocal mode (for UI feedback)
2. **Select Engine Mode**: Use `referenceVocalMode` if available, else fallback to attempt analysis
3. **Route to Engine**: Select SpeechEngine or SingingEngine based on engine mode
4. **Score Attempt**: Engine compares reference vs. attempt
5. **Return Result**: Score + metrics + feedback + attemptAnalysis (for UI)

**Engine Selection Logic**:
```kotlin
// 🎯 FIX: Use reference's stored mode for engine selection
val engineMode = referenceVocalMode?.takeIf { it != VocalMode.UNKNOWN }
    ?: attemptAnalysis.mode  // fallback for legacy recordings

// Route to engine based on reference mode (or fallback)
val decision = vocalModeRouter.getRoutingDecision(
    VocalAnalysis(engineMode, attemptAnalysis.confidence, attemptAnalysis.features)
)
```

**Fallback Conditions**:
- `referenceVocalMode` is null (legacy recordings, race condition)
- `referenceVocalMode` is UNKNOWN (detection failed during recording)

In fallback cases, behavior is identical to v22.0.0 (no regression).

---

## Speech Scoring Engine

**Location**: `SpeechScoringEngine.kt`

### Philosophy

Optimized for **natural speech patterns** where:
- Content accuracy matters most (getting words right)
- Pitch varies naturally (not melodic)
- Prosody (rhythm/intonation) is important
- Monotone speech is acceptable

### Speech-Specific Optimizations

#### 1. Higher Pitch Tolerance
```kotlin
pitchTolerance = 40f  // Semitones (vs. 20f for singing)
```
Speech naturally has more pitch variation without being "wrong."

#### 2. Content-Focused Scoring
```kotlin
pitchWeight = 0.85f     // High emphasis on pitch
mfccWeight = 0.15f      // Less emphasis on phoneme similarity
```

#### 3. Relaxed Melodic Requirements
```kotlin
melodicParams = MelodicAnalysisParameters(
    intervalToleranceSemitones = 2.5f,  // Wide tolerance
    // Speech doesn't need precise musical intervals
)
```

#### 4. Speech-Appropriate Garbage Detection
```kotlin
garbageParams = GarbageDetectionParameters(
    pitchMonotoneThreshold = 5f,        // Allow monotone
    spectralEntropyThreshold = 0.3f,    // Lower complexity OK
)
```

### Scoring Process

**Entry Point**:
```kotlin
suspend fun scoreAttempt(
    originalAudio: FloatArray,
    playerAttempt: FloatArray,
    challengeType: ChallengeType,
    difficulty: DifficultyLevel,
    sampleRate: Int = 44100
): ScoringResult
```

**Steps**:
1. **Garbage Detection**: Check if attempt is valid
2. **Feature Extraction**: Extract pitch + MFCC from both audios
3. **Pitch Similarity**: Compare pitch sequences with DTW alignment
4. **MFCC Similarity**: Compare phoneme characteristics
5. **Weighted Combination**: `score = pitch * 0.85 + mfcc * 0.15`
6. **Threshold Mapping**: Map raw score to 0-100 scale
7. **Feedback Generation**: Create user-friendly feedback messages

---

## Singing Scoring Engine

**Location**: `SingingScoringEngine.kt`

### Philosophy

Optimized for **musical/singing patterns** where:
- Precise pitch accuracy is critical
- Melodic structure matters
- Musical intervals must be correct
- Vocal technique is rewarded

### Singing-Specific Optimizations

#### 1. Lower Pitch Tolerance
```kotlin
pitchTolerance = 20f  // Semitones (strict musical accuracy)
```

#### 2. Melody-Focused Scoring
```kotlin
pitchWeight = 0.90f     // Very high emphasis on pitch accuracy
mfccWeight = 0.10f      // Less emphasis on timbre
```

#### 3. Enhanced Musical Analysis
```kotlin
melodicParams = MelodicAnalysisParameters(
    intervalToleranceSemitones = 1.0f,  // Tight tolerance
    enableIntervalAnalysis = true,       // Check musical intervals
    enablePhraseDetection = true,        // Detect phrase structure
)
```

#### 4. Strict Garbage Detection
```kotlin
garbageParams = GarbageDetectionParameters(
    pitchMonotoneThreshold = 8f,         // Reject pure monotone
    spectralEntropyThreshold = 0.4f,     // Require complexity
    mfccVarianceThreshold = 250f,        // Require variation
)
```

### Musical Features Analyzed

**Melody Signature**:
```kotlin
data class MelodySignature(
    val pitchContour: List<Float>,      // Pitch sequence
    val intervalSequence: List<Float>,  // Musical intervals (semitones)
    val phraseBreaks: List<Int>,        // Where phrases start/end
    val rhythmPattern: List<Float>,     // Temporal structure
    val vocalDensity: Float             // Notes per second
)
```

**Interval Analysis**:
- Compares musical intervals between notes
- Perfect 5th = 7 semitones
- Major 3rd = 4 semitones
- Tolerance: ±1 semitone

**Phrase Detection**:
- Identifies melodic phrases using silence gaps
- Compares phrase structure between reference and attempt

---

## Difficulty Levels

**Location**: `ScoringCommonModels.kt:DifficultyLevel`

### Three-Level System

```kotlin
enum class DifficultyLevel(
    val displayName: String,
    val emoji: String,
    val description: String
) {
    EASY("Easy", "😊", "Very forgiving - great for beginners"),
    NORMAL("Normal", "🎵", "Balanced scoring - the default experience"),
    HARD("Hard", "🔥", "Challenging - for experienced users")
}
```

### Parameter Differences

#### EASY Mode (Forgiving)

**Speech**:
```kotlin
ScoringParameters(
    pitchWeight = 0.4f,                 // Less pitch emphasis
    mfccWeight = 0.6f,                  // More content emphasis
    pitchTolerance = 50f,               // Very wide tolerance
    minScoreThreshold = 0.08f,          // Low minimum
    perfectScoreThreshold = 0.7f,       // Easy to get 100
    scoreCurve = 3.0f                   // Generous curve
)
```

**Singing**:
```kotlin
ScoringParameters(
    pitchWeight = 0.6f,                 // Still melodic focus
    mfccWeight = 0.4f,
    pitchTolerance = 25f,               // Wider than normal
    minScoreThreshold = 0.1f,
    perfectScoreThreshold = 0.75f,
    scoreCurve = 2.8f
)
```

#### NORMAL Mode (Balanced)

**Speech**:
```kotlin
ScoringParameters(
    pitchWeight = 0.5f,
    mfccWeight = 0.5f,
    pitchTolerance = 35f,
    minScoreThreshold = 0.12f,
    perfectScoreThreshold = 0.8f,
    scoreCurve = 2.5f
)
```

**Singing**:
```kotlin
ScoringParameters(
    pitchWeight = 0.7f,
    mfccWeight = 0.3f,
    pitchTolerance = 15f,
    minScoreThreshold = 0.15f,
    perfectScoreThreshold = 0.85f,
    scoreCurve = 2.2f
)
```

#### HARD Mode (Challenging)

**Speech**:
```kotlin
ScoringParameters(
    pitchWeight = 0.6f,
    mfccWeight = 0.4f,
    pitchTolerance = 20f,               // Strict
    minScoreThreshold = 0.2f,           // High minimum
    perfectScoreThreshold = 0.9f,       // Hard to get 100
    scoreCurve = 1.8f                   // Steep curve
)
```

**Singing**:
```kotlin
ScoringParameters(
    pitchWeight = 0.8f,                 // Very melody-focused
    mfccWeight = 0.2f,
    pitchTolerance = 10f,               // Very strict
    minScoreThreshold = 0.25f,
    perfectScoreThreshold = 0.95f,
    scoreCurve = 1.5f
)
```

### Difficulty Impact

| Difficulty | Easy | Normal | Hard |
|------------|------|--------|------|
| **Pitch Tolerance** (Speech) | ±50 semitones | ±35 semitones | ±20 semitones |
| **Pitch Tolerance** (Singing) | ±25 semitones | ±15 semitones | ±10 semitones |
| **Min Score** | 8-10% | 12-15% | 20-25% |
| **Perfect Score** | 70-75% | 80-85% | 90-95% |
| **Score Curve** | Generous (3.0) | Balanced (2.5) | Steep (1.8) |

---

## Score Calculation

### Raw Score Computation

**Location**: Both engines use similar flow

#### Step 1: Feature Similarity

**Pitch Similarity** (DTW-based):
```kotlin
val pitchSimilarity = comparePitchSequences(
    referencePitches,
    attemptPitches,
    tolerance = parameters.pitchTolerance
)
// Returns 0.0 - 1.0
```

**MFCC Similarity**:
```kotlin
val mfccSimilarity = compareMFCCSequences(
    referenceMFCC,
    attemptMFCC
)
// Returns 0.0 - 1.0
```

#### Step 2: Weighted Combination

```kotlin
rawScore = (pitchSimilarity * pitchWeight) +
           (mfccSimilarity * mfccWeight)
```

**Examples**:
- Speech (0.5/0.5): `rawScore = pitch * 0.5 + mfcc * 0.5`
- Singing (0.7/0.3): `rawScore = pitch * 0.7 + mfcc * 0.3`

#### Step 3: Threshold Normalization

Maps raw score to 0-1 range using difficulty thresholds:

```kotlin
val minThreshold = when (challengeType) {
    FORWARD → parameters.minScoreThreshold
    REVERSE → parameters.reverseMinScoreThreshold
}

val perfectThreshold = when (challengeType) {
    FORWARD → parameters.perfectScoreThreshold
    REVERSE → parameters.reversePerfectScoreThreshold
}

normalizedScore = when {
    rawScore < minThreshold → 0f
    rawScore > perfectThreshold → 1f
    else → (rawScore - minThreshold) / (perfectThreshold - minThreshold)
}
```

#### Step 4: Score Curve Application

Applies exponential curve for feel:

```kotlin
val curvedScore = normalizedScore.pow(parameters.scoreCurve)
```

**Score Curve Effects**:
- `3.0` (Easy) → Generous, rewards small improvements
- `2.5` (Normal) → Balanced
- `1.8` (Hard) → Steep, requires excellence for high scores

#### Step 5: Final Score

```kotlin
finalScore = (curvedScore * 100).roundToInt().coerceIn(0, 100)
```

### Reverse Challenge Adjustments

Reverse challenges are harder, so thresholds are adjusted:

```kotlin
// Speech Normal Mode
minScoreThreshold = 0.12f          // Forward
reverseMinScoreThreshold = 0.08f   // Reverse (lower minimum)

perfectScoreThreshold = 0.8f       // Forward
reversePerfectScoreThreshold = 0.7f // Reverse (easier to get 100)
```

---

## Garbage Detection

**Location**: `GarbageDetector.kt`

### Purpose

Prevents users from gaming the system with:
- "Blah blah blah" repetition
- Monotone humming
- White noise
- Silent recordings
- Unnatural oscillations

### Detection Filters

#### 1. MFCC Variance Filter

Detects repetitive sounds:

```kotlin
val mfccVariance = calculateMFCCVariance(mfccFrames)

if (mfccVariance < parameters.mfccVarianceThreshold) {
    // TOO REPETITIVE
    failedFilters.add("MFCC_VARIANCE")
}
```

**Thresholds**:
- Speech: 150f (allow some repetition)
- Singing: 250f (require variation)

#### 2. Pitch Contour Filter

Detects monotone or unnatural oscillation:

```kotlin
data class PitchContourAnalysis(
    val stdDev: Float,              // Pitch variation
    val oscillationRate: Float,     // % of peaks/troughs
    val isMonotone: Boolean,        // Too flat
    val isOscillating: Boolean      // Too regular
)

if (isMonotone) failedFilters.add("MONOTONE")
if (isOscillating) failedFilters.add("OSCILLATING")
```

#### 3. Spectral Entropy Filter

Detects low-complexity noise:

```kotlin
val spectralEntropy = calculateSpectralEntropy(audioFrames)

if (spectralEntropy < parameters.spectralEntropyThreshold) {
    // TOO SIMPLE (white noise, hum)
    failedFilters.add("SPECTRAL_ENTROPY")
}
```

#### 4. Zero Crossing Rate Filter

Detects hums or white noise:

```kotlin
val zcr = calculateZeroCrossingRate(audioFrames)

if (zcr < parameters.zcrMinThreshold ||
    zcr > parameters.zcrMaxThreshold) {
    failedFilters.add("ZERO_CROSSING_RATE")
}
```

#### 5. Silence Ratio Filter

Detects continuous noise without natural pauses:

```kotlin
val silenceRatio = detectSilenceRatio(audioFrames)

if (silenceRatio < parameters.minSilenceRatio) {
    // NO PAUSES (unnatural)
    failedFilters.add("SILENCE_RATIO")
}
```

### Garbage Verdict

```kotlin
data class GarbageAnalysis(
    val isGarbage: Boolean,           // Final verdict
    val confidence: Float,             // 0-1 confidence
    val failedFilters: List<String>,   // Which filters failed
    val filterResults: Map<String, Float> // Detailed scores
)

// Garbage if confidence > 0.6 OR multiple filters failed
val isGarbage = (garbageConfidence > 0.6f) || (failedFilters.size >= 2)
```

### Garbage Result

If detected as garbage:
```kotlin
return ScoringResult(
    score = 0,
    rawScore = 0f,
    metrics = SimilarityMetrics(0f, 0f),
    feedback = listOf("❌ Invalid attempt detected"),
    isGarbage = true
)
```

---

## Technical Implementation

### Memory Safety

**10MB File Size Limit**:
```kotlin
// AudioConstants.kt
const val MAX_LOADABLE_AUDIO_BYTES = 10 * 1024 * 1024L

// Peak RAM usage: ~30MB
// - 10MB byte array
// - 20MB float array
```

**Enforced At**:
- AudioViewModel.readAudioFile()
- VocalModeDetector.readWavFile()
- AudioRecorderHelper.checkDuration()

### Performance Optimizations

#### In-Memory Processing
```kotlin
// ❌ OLD: Temporary file I/O
val tempFile = createTempFile()
tempFile.writeBytes(audioData)
vocalModeDetector.classifyVocalMode(tempFile)

// ✅ NEW: Direct memory processing
vocalModeDetector.classifyVocalMode(audioData, sampleRate)
```

**Impact**: Eliminated ~5 second recording lag

#### Duration Check Throttling
```kotlin
// ❌ OLD: Check every buffer write (~10 times/sec)
checkDuration(startTime)

// ✅ NEW: Throttled to 1/second
private var lastCheckTime = 0L

if (now - lastCheckTime < 1000) return
```

**Impact**: 90% reduction in timestamp checks

### Type Safety

**Sealed Class Events**:
```kotlin
sealed class RecorderEvent {
    object Warning : RecorderEvent()
    object Stop : RecorderEvent()
}

// Type-safe handling
when (event) {
    RecorderEvent.Warning → showWarning()
    RecorderEvent.Stop → stopRecording()
}
```

### Concurrency

**Mutex Locks**:
```kotlin
private val recordingProcessingMutex = Mutex()

viewModelScope.launch {
    recordingProcessingMutex.withLock {
        // Critical section: file flush + processing
        val file = audioRecorderHelper.stop()
        processRecording(file)
    }
}
```

---

## Debugging & Troubleshooting

### Debug Logging

Enable detailed logs by filtering:

```bash
# Vocal mode detection
adb logcat | grep "VocalModeDetector"

# Routing decisions
adb logcat | grep "VocalModeRouter"

# Scoring engine
adb logcat | grep "SPEECH_ENGINE\|SINGING_ENGINE"

# Orchestrator flow
adb logcat | grep "VSO"

# Garbage detection
adb logcat | grep "GarbageDetector"
```

### Common Issues

#### Issue: Score Always 0

**Possible Causes**:
1. Garbage detected
2. Audio file too short
3. File read error

**Debug**:
```kotlin
// Check logs for:
"❌ Garbage detected"
"File too large"
"Error reading audio file"
```

#### Issue: Wrong Engine Selected

**Possible Causes**:
1. Vocal mode detection classified incorrectly
2. Low confidence classification
3. **Cross-engine contamination** (fixed in v22.1.0)

**Debug**:
```kotlin
// Check logs for engine selection (v22.1.0 format):
"🎯 ENGINE SELECTION: referenceMode=SPEECH, attemptMode=SINGING, using=SPEECH"

// If referenceMode differs from attemptMode but using=referenceMode → FIX WORKING
// If referenceMode=null → fallback to attempt-based selection (legacy recording)
```

#### Issue: 0% Score on Legitimate Attempt

**Possible Causes** (pre-v22.1.0):
1. Cross-engine contamination (reference=SPEECH, attempt=SINGING)
2. Different alignment algorithms caused MFCC mismatch
3. Content detection penalty triggered on low MFCC (~48%)

**Solution**: Upgrade to v22.1.0 with reference-mode binding

**Debug**:
```kotlin
// Look for these symptoms in logs:
"MFCC similarity: 0.485"  // Should be ~0.95 for matching content
"📉 Content Mismatch! MFCC (0.485) < Threshold (0.75)"
"⬇️ New Score: 0.0"

// Check if engines mismatched:
"referenceMode=SPEECH, attemptMode=SINGING"  // Problem!
```

#### Issue: OutOfMemoryError

**Cause**: File exceeds 10MB limit

**Fix**: Already implemented in v20.0.0f
```kotlin
if (file.length() > AudioConstants.MAX_LOADABLE_AUDIO_BYTES) {
    Log.w("AudioViewModel", "File too large")
    return floatArrayOf()
}
```

### ScoringResult Debug Fields

```kotlin
data class ScoringResult(
    val score: Int,                      // Final 0-100 score
    val rawScore: Float,                 // Pre-scaled 0-1
    val metrics: SimilarityMetrics,      // Pitch/MFCC breakdown
    val feedback: List<String>,          // User messages
    val isGarbage: Boolean,              // Garbage verdict

    // DEBUG FIELDS:
    val debugMinThreshold: Float,        // Minimum threshold used
    val debugPerfectThreshold: Float,    // Perfect threshold used
    val debugNormalizedScore: Float,     // After threshold mapping
    val debugPresets: Presets?           // Full preset configuration
)
```

### Testing the System

**Built-In Test Runner**: `BITRunner.kt`

Tests 15 synthetic audio files with known target scores:
- Perfect C major scale (target: 90/85)
- Pitch +0.25 semitones off (target: 85/78)
- Pitch +0.5 semitones off (target: 75/68)
- Wrong notes (target: 50/45)
- Pure garbage (target: 0)

---

## Appendices

### A. Audio Processing Parameters

```kotlin
data class AudioProcessingParameters(
    val frameSize: Int = 1024,           // Samples per frame
    val hopSize: Int = 512,              // Frame overlap
    val windowType: String = "hann",     // Window function
    val fftSize: Int = 2048,             // FFT resolution
    val melBands: Int = 40,              // MFCC mel bands
    val mfccCoefficients: Int = 13       // MFCC features
)
```

### B. Challenge Type Adjustments

```kotlin
// REVERSE challenges are inherently harder
// Separate thresholds compensate for difficulty

FORWARD:
  minScoreThreshold = 0.12f
  perfectScoreThreshold = 0.8f

REVERSE:
  reverseMinScoreThreshold = 0.08f      // Lower minimum
  reversePerfectScoreThreshold = 0.7f   // Easier perfect
```

### C. File Formats

**Input**: WAV (RIFF), 44.1kHz, 16-bit PCM, Mono

**Processing**: In-memory Float arrays (-1.0 to 1.0)

**Storage**: WAV files with proper headers

### D. Version History

- **v22.1.0**: Reference-mode binding fix for cross-engine contamination
- **v22.0.0**: Content detection improvements, threshold calibration
- **v20.0.0f**: Sealed class events, throttling, duplicate constant removal
- **v20.0.0e**: Memory safety, type safety, null safety improvements
- **v20.0.0d**: Magic number extraction, constant centralization
- **v20.0.0c**: Dual-pipeline scoring complete
- **v19.0.0**: GLUTE theme refactor
- **v18.0.0**: Performance optimizations, I/O elimination

---

## Glossary

**DTW (Dynamic Time Warping)**: Algorithm for comparing sequences with different speeds

**MFCC (Mel-Frequency Cepstral Coefficients)**: Voice timbre features

**YIN Algorithm**: Pitch detection algorithm

**Semitone**: Musical interval (12 semitones = 1 octave)

**PCM (Pulse Code Modulation)**: Raw audio format

**GLUTE**: Polymorphic component architecture principle

**Garbage Detection**: Filter for invalid/gaming attempts

**Vocal Mode**: Classification as speech, singing, or unknown

---

**End of Manual** | For technical support, see issues at github.com/CryptoProgenitor/ReVerseY

---

## 📝 Changelog

### 2025-12-07 - Reference-Mode Binding Fix (v22.1.0)
**Added reference-mode binding to prevent cross-engine contamination**

#### New Feature: Reference-Mode Binding
- Engine selection now uses reference recording's stored `vocalAnalysis.mode`
- Attempt detection still runs but only for UI feedback
- Fallback to attempt-based selection for legacy recordings (null/UNKNOWN mode)

#### Files Modified

| File | Change |
|------|--------|
| `VocalScoringOrchestrator.kt` | Added `referenceVocalMode: VocalMode?` parameter; engine selection uses reference mode |
| `AudioViewModel.kt` | Moved `parentRecording` lookup before scoring; passes `parentRecording?.vocalAnalysis?.mode` |

#### Bug Fixed
- **Symptom**: 0% score on legitimate attempts when reference and attempt classified differently
- **Root Cause**: Edge-case recordings near 0.4 singing threshold classified inconsistently
- **Mechanism**: Different engines use different alignment algorithms (threshold 0.01f vs 0.015f)
- **Effect**: MFCC similarity dropped from ~95% to ~48.5%, triggering content penalty
- **Solution**: Force same engine for both by using reference's stored mode

#### Updated Sections
- "Dual-Pipeline Scoring Architecture" - Added reference-mode binding diagram
- "Scoring Orchestrator" - Updated signature and process
- "Debugging & Troubleshooting" - Added cross-engine issue and new log format

### 2025-12-05 - Documentation Accuracy Audit
**Corrected values to match actual codebase** (`SpeechScoringModels.kt`, `SingingScoringModels.kt`)

#### Speech Engine Corrections (Section: Speech Scoring Engine)

| Parameter | Previous (Wrong) | Corrected | Location in Code |
|-----------|------------------|-----------|------------------|
| pitchTolerance | 35f | **40f** | SpeechScoringModels.kt:58 |
| pitchWeight | 0.5f | **0.85f** | SpeechScoringModels.kt:55 |
| mfccWeight | 0.5f | **0.15f** | SpeechScoringModels.kt:56 |

#### Singing Engine Corrections (Section: Singing Scoring Engine)

| Parameter | Previous (Wrong) | Corrected | Location in Code |
|-----------|------------------|-----------|------------------|
| pitchTolerance | 15f | **20f** | SingingScoringModels.kt:52 |
| pitchWeight | 0.7f | **0.90f** | SingingScoringModels.kt:49 |
| mfccWeight | 0.3f | **0.10f** | SingingScoringModels.kt:50 |

#### Scoring Process Description Correction
- Section "Speech Scoring Engine > Scoring Process > Step 5":
  - Was: `score = pitch * 0.5 + mfcc * 0.5`
  - Now: `score = pitch * 0.85 + mfcc * 0.15`

#### Philosophy Section Clarification
- Speech engine description updated: pitch still dominates (85%) but with much higher tolerance (±40 semitones) making it forgiving for natural speech variation
- Singing engine description updated: pitch heavily dominates (90%) with strict tolerance (±20 semitones) for musical accuracy

**Root cause**: Original documentation was written from design specification documents, not verified against final implementation. Code evolved during development but docs were not updated.

**Verification method**: Direct inspection of `SpeechScoringModels.kt:normalModeSpeech()` and `SingingScoringModels.kt:normalModeSinging()` functions.

### 2025-11-22 - Initial Release  
- v20.0.0f Production-Ready documentation
- Complete technical manual for dual-pipeline scoring system
