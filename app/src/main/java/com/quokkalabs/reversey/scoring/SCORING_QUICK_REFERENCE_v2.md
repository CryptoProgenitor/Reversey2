# ReVerseY Scoring System - Quick Reference Guide

## 🎯 Core Concepts

### Dual-Pipeline Architecture
- **Speech Engine**: Optimized for spoken word (±40 semitones tolerance, 85% pitch / 15% voice)
- **Singing Engine**: Optimized for music (±20 semitones tolerance, 90% pitch / 10% voice)
- **Reference-Mode Binding**: Engine selected based on reference recording's stored mode (v22.1.0)

### Engine Selection Logic (v22.1.0)
```kotlin
// Engine is chosen based on REFERENCE, not attempt
val engineMode = referenceVocalMode?.takeIf { it != VocalMode.UNKNOWN }
    ?: attemptAnalysis.mode  // fallback for legacy recordings
```

**Why**: Prevents cross-engine contamination when reference and attempt are classified differently due to edge-case detection near the 0.4 singing threshold.

### Challenge Types
- **FORWARD**: Replicate the original recording
- **REVERSE**: Replicate the reversed recording (inherently harder)

### Difficulty Levels
- **EASY** 😊: Very forgiving (±55 semis speech, ±30 semis singing)
- **NORMAL** 🎵: Balanced (±40 semis speech, ±20 semis singing)
- **HARD** 🔥: Challenging (±25 semis speech, ±12 semis singing)

---

## 📊 Score Calculation Formula

```
1. Extract Features:
   - Pitch sequence (YIN algorithm)
   - MFCC sequence (13 coefficients)

2. Compute Similarity:
   - Pitch similarity (DTW alignment + tolerance)
   - MFCC similarity (cosine distance)

3. Weighted Combination:
   Speech:  rawScore = pitch * 0.85 + mfcc * 0.15
   Singing: rawScore = pitch * 0.90 + mfcc * 0.10

4. Threshold Normalization:
   normalizedScore = (rawScore - minThreshold) / (perfectThreshold - minThreshold)

5. Apply Curve:
   curvedScore = normalizedScore ^ scoreCurve

6. Scale to 0-100:
   finalScore = (curvedScore * 100).roundToInt()
```

---

## 🎤 Vocal Mode Detection

### Classification Features

| Feature             | Speech Characteristics      | Singing Characteristics |
|---------------------|-----------------------------|-------------------------|
| **Pitch Stability** | Low (0.2-0.4)               | High (0.7-0.9)          |
| **Pitch Contour**   | Flat (0.2-0.4)              | Melodic (0.6-0.8)       |
| **MFCC Spread**     | High (varied pronunciation) | Low (sustained vowels ) |
| **Voiced Ratio**    | Medium (pauses, consonants) | High (continuous)       |

### Classification Logic
```kotlin
speechScore = (1 - pitchStability) * 0.4 +
              (1 - pitchContour) * 0.3 +
              mfccSpread * 0.1

singingScore = pitchStability * 0.2 +
               pitchContour * 0.3 +
               voicedRatio * 0.5

if (singingScore > speechScore && singingScore > 0.4)
    → SINGING
else
    → SPEECH
```

### When Detection is Used (v22.1.0)

| Context | Detection Used For |
|---------|-------------------|
| **Recording creation** | Stored in `Recording.vocalAnalysis` for future engine selection |
| **Attempt scoring** | UI feedback only (shows user's vocal style) |
| **Engine selection** | Uses **reference's stored mode**, not attempt's detected mode |

---

## ⚙️ Parameter Comparison

### Speech Engine (Normal Difficulty)

```yaml
Weights:
  pitch_weight: 0.85
  mfcc_weight: 0.15

Thresholds:
  pitch_tolerance: 40 semitones
  min_score_threshold: 0.12 (forward), 0.10 (reverse)
  perfect_score_threshold: 0.80 (forward), 0.77 (reverse)

Curve:
  score_curve: 3.2

Garbage Detection:
  pitch_monotone_threshold: 5.0
  mfcc_variance_threshold: 150.0
  spectral_entropy_threshold: 0.3
```

### Singing Engine (Normal Difficulty)

```yaml
Weights:
  pitch_weight: 0.90
  mfcc_weight: 0.10

Thresholds:
  pitch_tolerance: 20 semitones
  min_score_threshold: 0.22 (forward), 0.18 (reverse)
  perfect_score_threshold: 0.98 (forward), 0.85 (reverse)

Curve:
  score_curve: 1.0

Garbage Detection:
  pitch_monotone_threshold: 8.0
  mfcc_variance_threshold: 250.0
  spectral_entropy_threshold: 0.4
```

---

## 🛡️ Safety Limits

### Memory Protection
```
MAX_LOADABLE_AUDIO_BYTES = 10MB
- Enforced at: AudioViewModel, VocalModeDetector, AudioRecorderHelper
- Peak RAM: ~30MB (10MB ByteArray + 20MB FloatArray)
- Equivalent: ~2 minutes of Mono 44.1kHz audio
```

### Recording Limits
```
MAX_RECORDING_DURATION_MS = 118,800ms (~1:59)
WARNING_DURATION_MS = 106,920ms (~1:47)
MIN_VALID_RECORDING_SIZE = 1024 bytes (1KB)
```

### Audio Format
```
Sample Rate: 44,100 Hz
Bit Depth: 16-bit PCM
Channels: Mono (1)
Format: WAV (RIFF)
```

---

## 🗑️ Garbage Detection Filters

### 5 Detection Filters

|            Filter      |             Purpose               |          Threshold                 |
|------------------------|-----------------------------------|------------------------------------|
| **MFCC Variance**      | Repetitive sounds ("blah blah")   | <150 (speech), <250 (singing)      |
| **Pitch Contour**      | Monotone or unnatural oscillation | <5 StdDev (speech), <8 (singing)   |
| **Spectral Entropy**   | Low-complexity noise              | <0.3 (speech), <0.4 (singing)      |
| **Zero Crossing Rate** | Hums, white noise                 | Outside range                      |
| **Silence Ratio**      | No natural pauses                 | <10% silence                       |

### Verdict Logic
```
if (confidence > 0.6 || failedFilters.size >= 2) {
    return ScoringResult(score = 0, isGarbage = true)
}
```

---

## 📈 Score Interpretation

### Score Ranges

| Score | Rating | Feedback |
|-------|--------|----------|
| **90-100** | Perfect ⭐⭐⭐⭐⭐ | "Excellent! Almost identical!" |
| **80-89** | Great ⭐⭐⭐⭐ | "Great job! Very close match!" |
| **70-79** | Good ⭐⭐⭐ | "Good effort! Keep practicing!" |
| **60-69** | Fair ⭐⭐ | "Fair attempt. Try again!" |
| **0-59** | Poor ⭐ | "Needs improvement. Practice more!" |

### Metrics Breakdown

**ScoringResult Structure:**
```kotlin
score: Int               // 0-100 final score
rawScore: Float          // 0-1 pre-scaled
metrics: {
    pitch: Float         // 0-1 pitch similarity
    mfcc: Float          // 0-1 MFCC similarity
}
feedback: List<String>   // User-facing tips
isGarbage: Boolean       // Garbage detection verdict
vocalAnalysis: VocalAnalysis  // User's detected vocal mode (for UI)
```

---

## 🔧 Common Configuration Changes

### Making Speech Easier
```kotlin
SpeechScoringModels.easyModeSpeech().copy(
    scoring = ScoringParameters(
        pitchTolerance = 60f,        // More forgiving
        minScoreThreshold = 0.05f,   // Lower minimum
        perfectScoreThreshold = 0.65f, // Easier 100
        scoreCurve = 3.5f            // More generous
    )
)
```

### Making Singing Harder
```kotlin
SingingScoringModels.hardModeSinging().copy(
    scoring = ScoringParameters(
        pitchTolerance = 8f,         // Very strict
        minScoreThreshold = 0.3f,    // High minimum
        perfectScoreThreshold = 0.98f, // Very hard 100
        scoreCurve = 1.2f            // Steep curve
    )
)
```

### Adjusting Garbage Detection
```kotlin
GarbageDetectionParameters(
    enableGarbageDetection = true,
    pitchMonotoneThreshold = 10f,    // Higher = stricter
    mfccVarianceThreshold = 300f,    // Higher = stricter
    spectralEntropyThreshold = 0.5f  // Higher = stricter
)
```

---

## 🐛 Debug Commands

### Logcat Filters

```bash
# Full scoring flow
adb logcat | grep "VSO\|VocalModeDetector\|VocalModeRouter\|SPEECH_ENGINE\|SINGING_ENGINE"

# Vocal mode detection only
adb logcat | grep "VocalModeDetector"

# Garbage detection
adb logcat | grep "GarbageDetector"

# Score calculation
adb logcat | grep "SCORE_CALC"

# Memory/file issues
adb logcat | grep "AudioViewModel\|AudioRecorderHelper"
```

### Debug Output Example (v22.1.0)

```
D/VSO: === ORCHESTRATOR ENTRY ===
D/VSO: Attempt analysis → mode=SINGING, confidence=0.48
D/VSO: 🎯 ENGINE SELECTION: referenceMode=SPEECH, attemptMode=SINGING, using=SPEECH
D/VocalModeRouter: ENGINE SELECTION → SpeechScoringEngine
D/SPEECH_ENGINE: 🎤 THRESHOLDS: challengeType=FORWARD
D/SPEECH_ENGINE: 📊 Speech similarities - Pitch: 0.92, MFCC: 0.95
D/SPEECH_ENGINE: 📊 Speech scaling: raw=0.87, normalized=0.94, curved=0.98, final=98
D/VSO: === ORCHESTRATOR EXIT → score=98 ===
```

**Key log line**: `🎯 ENGINE SELECTION: referenceMode=SPEECH, attemptMode=SINGING, using=SPEECH`
- Shows reference mode was used even though attempt was detected as singing

---

## 📚 File Locations

### Core Components
```
Orchestrator:   scoring/VocalScoringOrchestrator.kt  ◄── MODIFIED v22.1.0
Detector:       scoring/VocalModeDetector.kt
Router:         scoring/VocalModeRouter.kt
Speech Engine:  scoring/SpeechScoringEngine.kt
Singing Engine: scoring/SingingScoringEngine.kt
Garbage:        scoring/GarbageDetector.kt
Models:         scoring/ScoringCommonModels.kt
```

### Configuration
```
Constants:      audio/AudioConstants.kt
Presets:        scoring/SpeechScoringModels.kt
                scoring/SingingScoringModels.kt
```

### UI Layer
```
ViewModel:      ui/viewmodels/AudioViewModel.kt  ◄── MODIFIED v22.1.0
Recorder:       audio/AudioRecorderHelper.kt
Player:         audio/AudioPlayerHelper.kt
```

---

## ⚡ Performance Tips

### Optimization 1: In-Memory Processing
```kotlin
// ✅ GOOD: Direct memory analysis
vocalModeDetector.classifyVocalMode(audioData: FloatArray, sampleRate: Int)

// ❌ BAD: File I/O
vocalModeDetector.classifyVocalMode(file: File)
```

### Optimization 2: Reference-Mode Metadata (v22.1.0)
```kotlin
// ✅ GOOD: Use stored metadata (no audio processing)
val referenceVocalMode = parentRecording?.vocalAnalysis?.mode

// ❌ BAD: Re-analyze reference audio every scoring call
val referenceAnalysis = vocalModeDetector.classifyVocalMode(referenceAudio, sampleRate)
```

### Optimization 3: Throttled Checks
```kotlin
// Duration checks throttled to 1/second
private var lastCheckTime = 0L
if (now - lastCheckTime < 1000) return
```

---

## 🎓 Algorithm References

### DTW (Dynamic Time Warping)
Used for pitch sequence alignment
- Handles different speaking/singing speeds
- Allows temporal warping
- O(n*m) complexity

### YIN Pitch Detection
Autocorrelation-based pitch estimation
- Accurate for human voice (50-400Hz)
- Handles noise well
- Returns 0 for unvoiced frames

### MFCC (Mel-Frequency Cepstral Coefficients)
Voice timbre representation
- 40 mel filterbanks
- 13 coefficients
- Captures phoneme characteristics

### Cosine Similarity
MFCC comparison metric
- Range: 0 (different) to 1 (identical)
- Robust to amplitude differences
- Fast computation

---

## 📞 Support

**GitHub Issues**: https://github.com/CryptoProgenitor/ReVerseY/issues

**Version**: 22.1.0 (Reference-Mode Binding Fix)

**Last Updated**: 2025-12-07

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
| `VocalScoringOrchestrator.kt` | Added `referenceVocalMode: VocalMode?` parameter |
| `AudioViewModel.kt` | Passes `parentRecording?.vocalAnalysis?.mode` to orchestrator |

#### Bug Fixed
- Edge-case recordings near 0.4 singing threshold could classify differently
- Different engines use different alignment algorithms (0.01f vs 0.015f threshold)
- Mismatched alignment caused MFCC to drop from ~95% to ~48.5%
- Content detection penalty then crushed score to 0%

#### Updated Sections
- "Dual-Pipeline Architecture" - Added reference-mode binding note
- "When Detection is Used" - New table explaining detection contexts
- "Debug Output Example" - Shows new log format with engine selection
- "File Locations" - Marked modified files
- "Performance Tips" - Added Optimization 2 for metadata usage

### 2025-12-05 - Documentation Accuracy Audit
**Corrected values to match actual codebase** (`SpeechScoringModels.kt`, `SingingScoringModels.kt`)

| Parameter | Previous (Wrong) | Corrected | Source File |
|-----------|------------------|-----------|-------------|
| Speech pitch_weight | 0.5 | **0.85** | SpeechScoringModels.kt:normalModeSpeech() |
| Speech mfcc_weight | 0.5 | **0.15** | SpeechScoringModels.kt:normalModeSpeech() |
| Speech pitch_tolerance | 35 | **40** | SpeechScoringModels.kt:normalModeSpeech() |
| Singing pitch_weight | 0.7 | **0.90** | SingingScoringModels.kt:normalModeSinging() |
| Singing mfcc_weight | 0.3 | **0.10** | SingingScoringModels.kt:normalModeSinging() |
| Singing pitch_tolerance | 15 | **20** | SingingScoringModels.kt:normalModeSinging() |

### 2025-11-22 - Initial Release
- v20.0.0f Production-Ready documentation
