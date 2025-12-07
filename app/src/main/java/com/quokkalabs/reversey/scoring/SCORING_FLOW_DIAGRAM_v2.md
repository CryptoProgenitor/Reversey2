# ReVerseY Scoring System - Visual Flow Diagrams

## Complete System Flow

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        USER INITIATES RECORDING                          │
│                     (Taps microphone button)                             │
└──────────────────────────────┬──────────────────────────────────────────┘
                               ↓
┌─────────────────────────────────────────────────────────────────────────┐
│                    RECORDING PHASE (0-118 seconds)                       │
│ ┌─────────────────────────────────────────────────────────────────────┐ │
│ │ AudioRecorderHelper                                                  │ │
│ │ • AudioRecord captures PCM data (44.1kHz, 16-bit, Mono)             │ │
│ │ • Writes to file (~10 buffer writes/second)                         │ │
│ │ • Calculates amplitude for waveform (real-time)                     │ │
│ │ • Checks duration limits (1/second, throttled)                      │ │
│ │                                                                      │ │
│ │ Events:                                                              │ │
│ │   107s → RecorderEvent.Warning ("Approaching limit")                │ │
│ │   118s → RecorderEvent.Stop (Auto-stop)                             │ │
│ └─────────────────────────────────────────────────────────────────────┘ │
└──────────────────────────────┬──────────────────────────────────────────┘
                               ↓
┌─────────────────────────────────────────────────────────────────────────┐
│                      USER STOPS RECORDING                                │
│                     AudioViewModel.stopRecording()                       │
└──────────────────────────────┬──────────────────────────────────────────┘
                               ↓
┌─────────────────────────────────────────────────────────────────────────┐
│                    POST-RECORDING PROCESSING                             │
│ ┌─────────────────────────────────────────────────────────────────────┐ │
│ │ 1. Stop AudioRecord                                                  │ │
│ │ 2. Add WAV Header (RIFF format)                                     │ │
│ │ 3. Validate file (> 1KB)                                            │ │
│ │ 4. Create reversed version (FFmpeg)                                 │ │
│ │ 5. Vocal Mode Analysis                                              │ │
│ │    VocalModeDetector.classifyVocalMode()                            │ │
│ │    → Extracts: Pitch, MFCC, Features                                │ │
│ │    → Returns: SPEECH or SINGING                                     │ │
│ │ 6. Save to RecordingRepository                                      │ │
│ │    • Original audio file                                            │ │
│ │    • Reversed audio file                                            │ │
│ │    • Vocal analysis metadata  ◄─── STORED FOR ENGINE SELECTION      │ │
│ └─────────────────────────────────────────────────────────────────────┘ │
└──────────────────────────────┬──────────────────────────────────────────┘
                               ↓
┌─────────────────────────────────────────────────────────────────────────┐
│                       RECORDING SAVED ✅                                 │
│               Now available for challenges                               │
│          VocalMode stored: Recording.vocalAnalysis.mode                  │
└─────────────────────────────────────────────────────────────────────────┘


═══════════════════════════════════════════════════════════════════════════
                           CHALLENGE PHASE
═══════════════════════════════════════════════════════════════════════════

┌─────────────────────────────────────────────────────────────────────────┐
│                  USER STARTS CHALLENGE                                   │
│      (Taps game button on recording card)                                │
└──────────────────────────────┬──────────────────────────────────────────┘
                               ↓
                    ┌──────────────────────┐
                    │  Select Challenge    │
                    │       Type           │
                    └──────┬───────────┬───┘
                           ↓           ↓
                    ┌──────────┐  ┌──────────┐
                    │ FORWARD  │  │ REVERSE  │
                    │ Original │  │ Reversed │
                    └──────┬───┘  └────┬─────┘
                           └───────────┘
                                 ↓
┌─────────────────────────────────────────────────────────────────────────┐
│                  ATTEMPT RECORDING                                       │
│  (Same as normal recording but isRecordingAttempt = true)                │
│  • No audio reversal                                                     │
│  • No vocal analysis                                                     │
│  • Parent recording path stored                                          │
└──────────────────────────────┬──────────────────────────────────────────┘
                               ↓
┌─────────────────────────────────────────────────────────────────────────┐
│                     USER STOPS ATTEMPT                                   │
│                  AudioViewModel.stopAttempt()                            │
└──────────────────────────────┬──────────────────────────────────────────┘
                               ↓
┌─────────────────────────────────────────────────────────────────────────┐
│                        SCORING PIPELINE                                  │
│                                                                           │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │ Step 1: Load Audio Files                                           │ │
│  │ ────────────────────────────────────────────────────────────────── │ │
│  │ • Reference Audio (original OR reversed, based on challenge type)  │ │
│  │ • Attempt Audio (user's recording)                                 │ │
│  │ • Convert WAV → FloatArray in memory                               │ │
│  │ • Safety check: file size < 10MB                                   │ │
│  └────────────────────────────────────────────────────────────────────┘ │
│                               ↓                                           │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │ Step 2: Get Reference Recording Metadata  ◄─── v22.1.0 FIX         │ │
│  │ ────────────────────────────────────────────────────────────────── │ │
│  │ parentRecording = recordings.find { it.originalPath == path }      │ │
│  │ referenceVocalMode = parentRecording?.vocalAnalysis?.mode          │ │
│  │                                                                     │ │
│  │ ⚡ EFFICIENCY: No audio analysis needed - uses stored metadata     │ │
│  └────────────────────────────────────────────────────────────────────┘ │
│                               ↓                                           │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │ Step 3: Vocal Mode Detection (on ATTEMPT - for UI feedback only)   │ │
│  │ ────────────────────────────────────────────────────────────────── │ │
│  │ VocalModeDetector.classifyVocalMode(attemptAudio, sampleRate)      │ │
│  │                                                                     │ │
│  │   ┌─────────────────────────────────────────────────────────┐     │ │
│  │   │ a) Preprocess                                            │     │ │
│  │   │    • Trim leading silence                                │     │ │
│  │   │    • Skip first 100ms (mic pop)                          │     │ │
│  │   │    • Validate length (>2048 samples)                     │     │ │
│  │   └─────────────────────────────────────────────────────────┘     │ │
│  │                         ↓                                           │ │
│  │   ┌─────────────────────────────────────────────────────────┐     │ │
│  │   │ b) Extract Features (frame-by-frame)                     │     │ │
│  │   │    • Pitch (YIN algorithm)                               │     │ │
│  │   │    • MFCC (13 coefficients, 40 mel bands)                │     │ │
│  │   └─────────────────────────────────────────────────────────┘     │ │
│  │                         ↓                                           │ │
│  │   ┌─────────────────────────────────────────────────────────┐     │ │
│  │   │ c) Analyze Vocal Features                                │     │ │
│  │   │    • Pitch Stability (0-1)                               │     │ │
│  │   │    • Pitch Contour (0-1)                                 │     │ │
│  │   │    • MFCC Spread (0-1)                                   │     │ │
│  │   │    • Voiced Ratio (0-1)                                  │     │ │
│  │   └─────────────────────────────────────────────────────────┘     │ │
│  │                         ↓                                           │ │
│  │   ┌─────────────────────────────────────────────────────────┐     │ │
│  │   │ d) Classify                                              │     │ │
│  │   │    speechScore = (1-stability)*0.4 + (1-contour)*0.3 +   │     │ │
│  │   │                  mfccSpread*0.1                          │     │ │
│  │   │    singingScore = stability*0.2 + contour*0.3 +          │     │ │
│  │   │                   voicedRatio*0.5                        │     │ │
│  │   │                                                           │     │ │
│  │   │    Result: SPEECH, SINGING, or UNKNOWN                   │     │ │
│  │   │    ⚠️  Used for UI feedback ONLY, not engine selection   │     │ │
│  │   └─────────────────────────────────────────────────────────┘     │ │
│  │                                                                     │ │
│  │ Returns: attemptAnalysis (for UI display of user's vocal style)    │ │
│  └────────────────────────────────────────────────────────────────────┘ │
│                               ↓                                           │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │ Step 4: Engine Selection (Reference-Mode Binding)  ◄─── v22.1.0    │ │
│  │ ────────────────────────────────────────────────────────────────── │ │
│  │                                                                     │ │
│  │   ┌─────────────────────────────────────────────────────────────┐ │ │
│  │   │ 🎯 REFERENCE-MODE BINDING LOGIC                              │ │ │
│  │   │                                                               │ │ │
│  │   │   referenceVocalMode provided and not UNKNOWN?               │ │ │
│  │   │         │                                                     │ │ │
│  │   │    YES  │   NO (null or UNKNOWN)                              │ │ │
│  │   │         │         │                                           │ │ │
│  │   │         ▼         ▼                                           │ │ │
│  │   │   ┌─────────┐   ┌─────────────────────┐                      │ │ │
│  │   │   │   USE   │   │  FALLBACK:          │                      │ │ │
│  │   │   │REFERENCE│   │  Use attemptAnalysis│                      │ │ │
│  │   │   │  MODE   │   │  (legacy behavior)  │                      │ │ │
│  │   │   └────┬────┘   └──────────┬──────────┘                      │ │ │
│  │   │        │                   │                                  │ │ │
│  │   │        └─────────┬─────────┘                                  │ │ │
│  │   │                  │                                            │ │ │
│  │   │                  ▼                                            │ │ │
│  │   │            engineMode                                         │ │ │
│  │   └─────────────────────────────────────────────────────────────┘ │ │
│  │                                                                     │ │
│  │   WHY: Prevents cross-engine contamination when reference and      │ │
│  │   attempt are classified differently (edge case detection)         │ │
│  │                                                                     │ │
│  │        ┌──────────────────────────────────┐                        │ │
│  │        │       Engine Mode Result         │                        │ │
│  │        └──────────┬────────────────┬──────┘                        │ │
│  │                   ↓                ↓                                │ │
│  │            ┌──────────┐    ┌──────────────┐                        │ │
│  │            │ SPEECH   │    │   SINGING    │                        │ │
│  │            └────┬─────┘    └──────┬───────┘                        │ │
│  │                 ↓                  ↓                                │ │
│  │        ┌─────────────────┐ ┌──────────────────┐                   │ │
│  │        │ SpeechEngine    │ │ SingingEngine    │                   │ │
│  │        │ (SPEECH_ENGINE) │ │ (SINGING_ENGINE) │                   │ │
│  │        └─────────────────┘ └──────────────────┘                   │ │
│  │                                                                     │ │
│  └────────────────────────────────────────────────────────────────────┘ │
│                               ↓                                           │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │ Step 5: Scoring Engine Execution                                   │ │
│  │ ────────────────────────────────────────────────────────────────── │ │
│  │                                                                     │ │
│  │ engine.scoreAttempt(referenceAudio, attemptAudio, challenge, diff) │ │
│  │                                                                     │ │
│  │   ┌─────────────────────────────────────────────────────────┐     │ │
│  │   │ 5a) Garbage Detection                                    │     │ │
│  │   │     GarbageDetector.detectGarbage()                      │     │ │
│  │   │     • MFCC Variance (repetition)                         │     │ │
│  │   │     • Pitch Contour (monotone/oscillation)               │     │ │
│  │   │     • Spectral Entropy (complexity)                      │     │ │
│  │   │     • Zero Crossing Rate (hum/noise)                     │     │ │
│  │   │     • Silence Ratio (pauses)                             │     │ │
│  │   │                                                           │     │ │
│  │   │     If garbage detected → Return score = 0               │     │ │
│  │   └─────────────────────────────────────────────────────────┘     │ │
│  │                         ↓                                           │ │
│  │   ┌─────────────────────────────────────────────────────────┐     │ │
│  │   │ 5b) Feature Extraction (both audios)                     │     │ │
│  │   │     • Extract pitch sequences                            │     │ │
│  │   │     • Extract MFCC sequences                             │     │ │
│  │   │     • Extract melody signatures (singing only)           │     │ │
│  │   │                                                           │     │ │
│  │   │  ⚡ SAME ENGINE = SAME ALIGNMENT ALGORITHM                │     │ │
│  │   │     Speech:  alignAudio() with threshold 0.01f           │     │ │
│  │   │     Singing: alignAudioMusical() with threshold 0.015f   │     │ │
│  │   └─────────────────────────────────────────────────────────┘     │ │
│  │                         ↓                                           │ │
│  │   ┌─────────────────────────────────────────────────────────┐     │ │
│  │   │ 5c) Similarity Computation                               │     │ │
│  │   │                                                           │     │ │
│  │   │  Pitch Similarity:                                       │     │ │
│  │   │    • DTW alignment of pitch sequences                    │     │ │
│  │   │    • Tolerance-based comparison                          │     │ │
│  │   │    • Speech: ±40 semitones (Normal)                      │     │ │
│  │   │    • Singing: ±20 semitones (Normal)                     │     │ │
│  │   │    → pitchSimilarity (0-1)                               │     │ │
│  │   │                                                           │     │ │
│  │   │  MFCC Similarity:                                        │     │ │
│  │   │    • Cosine distance between MFCC vectors                │     │ │
│  │   │    • Frame-by-frame comparison                           │     │ │
│  │   │    → mfccSimilarity (0-1)                                │     │ │
│  │   │                                                           │     │ │
│  │   └─────────────────────────────────────────────────────────┘     │ │
│  │                         ↓                                           │ │
│  │   ... [continued scoring steps 5d-5j unchanged] ...                │ │
│  │                                                                     │ │
│  └────────────────────────────────────────────────────────────────────┘ │
│                               ↓                                           │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │ Step 6: Result Assembly                                            │ │
│  │ ────────────────────────────────────────────────────────────────── │ │
│  │                                                                     │ │
│  │  ScoringResult {                                                   │ │
│  │    score: 85                    // Final score                     │ │
│  │    metrics: { pitch, mfcc }     // Similarity breakdown            │ │
│  │    vocalAnalysis: attemptAnalysis  // ◄─── USER's mode for UI     │ │
│  │    ...                                                             │ │
│  │  }                                                                 │ │
│  │                                                                     │ │
│  │  NOTE: UI shows what USER did, not what engine was used            │ │
│  └────────────────────────────────────────────────────────────────────┘ │
│                               ↓                                           │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │ Step 7: Save and Display                                           │ │
│  │ ────────────────────────────────────────────────────────────────── │ │
│  │ • Create PlayerAttempt with score                                  │ │
│  │ • Add to parent recording's attempts list                          │ │
│  │ • Update UI with new attempt                                       │ │
│  │ • Display score feedback                                           │ │
│  └────────────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────┘
```


═══════════════════════════════════════════════════════════════════════════
                  REFERENCE-MODE BINDING EXPLAINED (v22.1.0)
═══════════════════════════════════════════════════════════════════════════

```
┌─────────────────────────────────────────────────────────────────────────────┐
│  WHY THIS FIX WAS NEEDED                                                    │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  BEFORE (broken - v22.0.0):                                                 │
│                                                                             │
│    Challenge "hello hello" ──► Stored as SPEECH (singingScore=0.35)         │
│    Attempt "hello hello"   ──► Detected as SINGING (singingScore=0.48)      │
│                                       │                                     │
│                   (both near the 0.4 threshold - edge case!)                │
│                                       │                                     │
│                                       ▼                                     │
│                               SINGING ENGINE selected                       │
│                               (uses alignAudioMusical)                      │
│                                       │                                     │
│                                       ▼                                     │
│                               MFCC = 48.5% ──► Content penalty ──► 0%       │
│                                                                             │
│  ─────────────────────────────────────────────────────────────────────────  │
│                                                                             │
│  AFTER (fixed - v22.1.0):                                                   │
│                                                                             │
│    Challenge "hello hello" ──► Stored as SPEECH ─────┐                      │
│    Attempt "hello hello"   ──► Detected as SINGING   │ (ignored for engine) │
│                                                      │                      │
│                                       ┌──────────────┘                      │
│                                       ▼                                     │
│                               SPEECH ENGINE selected                        │
│                               (same alignment as reference)                 │
│                                       │                                     │
│                                       ▼                                     │
│                               MFCC = ~95% ──► No penalty ──► High score     │
│                                                                             │
│  ─────────────────────────────────────────────────────────────────────────  │
│                                                                             │
│  KEY INSIGHT: The 0.4 singing threshold is arbitrary. Two identical         │
│  recordings can land on opposite sides due to tiny variations in            │
│  delivery. Forcing same-engine processing eliminates this fragility.        │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```


═══════════════════════════════════════════════════════════════════════════
                        CODE CHANGE SUMMARY (v22.1.0)
═══════════════════════════════════════════════════════════════════════════

```
┌─────────────────────────────────────────────────────────────────────────────┐
│  FILES MODIFIED                                                             │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  1. VocalScoringOrchestrator.kt                                             │
│     ─────────────────────────                                               │
│     ADDED: referenceVocalMode: VocalMode? = null parameter                  │
│                                                                             │
│     suspend fun scoreAttempt(                                               │
│         referenceAudio: FloatArray,                                         │
│         attemptAudio: FloatArray,                                           │
│         challengeType: ChallengeType,                                       │
│         difficulty: DifficultyLevel,                                        │
│         referenceVocalMode: VocalMode? = null,  // ◄─── NEW                 │
│         sampleRate: Int = AudioConstants.SAMPLE_RATE                        │
│     )                                                                       │
│                                                                             │
│     CHANGED: Engine selection logic                                         │
│                                                                             │
│     val engineMode = referenceVocalMode?.takeIf { it != VocalMode.UNKNOWN } │
│         ?: attemptAnalysis.mode  // fallback to old behavior                │
│                                                                             │
│  ───────────────────────────────────────────────────────────────────────    │
│                                                                             │
│  2. AudioViewModel.kt                                                       │
│     ──────────────────                                                      │
│     MOVED: parentRecording lookup BEFORE scoring call                       │
│                                                                             │
│     // 3. Get parent recording for reference vocal mode (BEFORE scoring)    │
│     val parentRecording = uiState.value.recordings.find { ... }             │
│     val referenceVocalMode = parentRecording?.vocalAnalysis?.mode           │
│                                                                             │
│     // 4. Score via Orchestrator (with reference mode)                      │
│     val scoringResult = scoreDualPipeline(                                  │
│         referenceAudio = reversedParentAudio,                               │
│         attemptAudio = attemptAudio,                                        │
│         challengeType = challengeType,                                      │
│         referenceVocalMode = referenceVocalMode,  // ◄─── NEW               │
│         sampleRate = AudioConstants.SAMPLE_RATE                             │
│     )                                                                       │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```


═══════════════════════════════════════════════════════════════════════════
                          FALLBACK BEHAVIOR
═══════════════════════════════════════════════════════════════════════════

```
┌─────────────────────────────────────────────────────────────────────────────┐
│  WHEN FALLBACK OCCURS                                                       │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  The system falls back to attempt-based engine selection when:              │
│                                                                             │
│  1. parentRecording is null (deleted during scoring race condition)         │
│  2. vocalAnalysis is null (legacy recordings before v20.0.0)                │
│  3. vocalAnalysis.mode is UNKNOWN (detection failed when recorded)          │
│                                                                             │
│  In these cases, behavior is identical to v22.0.0 (no regression).          │
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                                                                     │   │
│  │   referenceVocalMode?.takeIf { it != VocalMode.UNKNOWN }            │   │
│  │       ?: attemptAnalysis.mode  // ◄─── FALLBACK PATH                │   │
│  │                                                                     │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```


═══════════════════════════════════════════════════════════════════════════
                    4-PATH SCORING MATRIX (unchanged)
═══════════════════════════════════════════════════════════════════════════

```
                      ┌─────────────────────────────────────────────────────────┐
                      │                   CHALLENGE TYPE                        │
                      │                                                         │
                      │          FORWARD              REVERSE                   │
                      │     (replicate original)  (replicate reversed)          │
      ┌───────────────┼─────────────────────────┬─────────────────────────────┤
      │               │                         │                              │
      │   SPEECH      │  Path A                 │  Path B                      │
E     │               │  Speech Forward         │  Speech Reverse              │
N     │               │  ────────────────────   │  ────────────────────        │
G     │               │  pitch × 0.85           │  pitch × 0.85                │
I     │               │  mfcc × 0.15            │  mfcc × 0.15                 │
N     │               │  tolerance: ±40 semi    │  tolerance: ±40 semi         │
E     │               │  min: 0.12, perf: 0.80  │  min: 0.10, perf: 0.77       │
      │               │  curve: 3.2             │  curve: 3.2                  │
M     │               │                         │  REVERSE HANDICAP: 0.08      │
O     ├───────────────┼─────────────────────────┼─────────────────────────────┤
D     │               │                         │                              │
E     │   SINGING     │  Path C                 │  Path D                      │
      │               │  Singing Forward        │  Singing Reverse             │
      │               │  ────────────────────   │  ────────────────────        │
      │               │  pitch × 0.90           │  pitch × 0.90                │
      │               │  mfcc × 0.10            │  mfcc × 0.10                 │
      │               │  tolerance: ±20 semi    │  tolerance: ±20 semi         │
      │               │  min: 0.22, perf: 0.98  │  min: 0.176, perf: 0.882     │
      │               │  curve: 1.0 (linear)    │  curve: 1.0 (linear)         │
      │               │  +Musical bonuses       │  +Musical bonuses            │
      │               │                         │  REVERSE HANDICAP: 0.15      │
      └───────────────┴─────────────────────────┴─────────────────────────────┘
```


═══════════════════════════════════════════════════════════════════════════
                         MEMORY SAFETY (unchanged)
═══════════════════════════════════════════════════════════════════════════

```
┌──────────────────────────────────────────────────────────────┐
│                   AUDIO FILE PROCESSING                       │
│                  (v20.0.0f Safety Improvements)               │
└──────────┬───────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│  Safety Check #1: File Size Validation                       │
│  ───────────────────────────────────────────────────────────  │
│  Location: AudioViewModel.readAudioFile()                     │
│                                                               │
│  if (file.length() > AudioConstants.MAX_LOADABLE_AUDIO_BYTES)│
│      ❌ REJECT (Log warning, return empty array)             │
│                                                               │
│  MAX_LOADABLE_AUDIO_BYTES = 10MB                             │
│  Equivalent to: ~2 minutes of Mono 44.1kHz audio             │
│  Peak RAM usage: ~30MB (10MB byte[] + 20MB FloatArray)       │
└──────────┬───────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│  Safety Check #2: Recording Duration Limits                  │
│  ───────────────────────────────────────────────────────────  │
│  Location: AudioRecorderHelper.checkDuration()                │
│                                                               │
│  MAX_RECORDING_DURATION_MS = 118,800ms (~1:59)               │
│  WARNING_DURATION_MS = 106,920ms (~1:47, 90% threshold)      │
└──────────┬───────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│  Safety Check #3: Minimum File Size                          │
│  ───────────────────────────────────────────────────────────  │
│  Location: AudioViewModel.validateRecordedFile()              │
│                                                               │
│  MIN_VALID_RECORDING_SIZE = 1024 bytes (1KB)                 │
└──────────┬───────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────────────────┐
│  ✅ FILE ACCEPTED - Safe to process                          │
└──────────────────────────────────────────────────────────────┘
```

---

**Legend:**
- `↓` Flow direction
- `✓` Feature/capability
- `✅` Successful outcome
- `❌` Rejection/failure
- `→` Alternative path
- `◄───` Annotation pointer
- `┌─┐` Box border

---

## 📝 Changelog

### 2025-12-07 - Reference-Mode Binding Fix (v22.1.0)
**Added reference-mode binding to prevent cross-engine contamination**

#### New Sections Added
- **Step 2: Get Reference Recording Metadata** - Shows metadata lookup before scoring
- **Step 4: Engine Selection (Reference-Mode Binding)** - New decision logic
- **REFERENCE-MODE BINDING EXPLAINED** - Visual explanation of the fix
- **CODE CHANGE SUMMARY** - Exact code modifications
- **FALLBACK BEHAVIOR** - Documents graceful degradation

#### Key Changes
| Aspect | Before (v22.0.0) | After (v22.1.0) |
|--------|------------------|-----------------|
| Engine selection based on | Attempt analysis | Reference's stored mode |
| Fallback when no metadata | N/A | Uses attempt analysis |
| UI feedback shows | Attempt's mode | Attempt's mode (unchanged) |
| Cross-engine contamination | Possible | Prevented |

#### Bug Fixed
- Edge-case recordings (near 0.4 singing threshold) could be classified differently between reference and attempt
- Different engines use different alignment algorithms
- Mismatched alignment caused MFCC similarity to drop (48.5% vs expected 95%)
- Content detection penalty then crushed score to 0%

### 2025-12-05 - Documentation Accuracy Audit
- Corrected all parameter values to match codebase

### 2025-11-22 - Initial Release
- v20.0.0f Production-Ready flow diagrams
