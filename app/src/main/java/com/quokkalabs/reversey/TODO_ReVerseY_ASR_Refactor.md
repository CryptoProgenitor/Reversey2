# ReVerseY ASR Refactor - TODO
## Last Updated: December 12, 2025
## Status: Steps 1-7 COMPLETE, Phases 2-5 PENDING

---

## ✅ COMPLETED (Dec 11-12, 2025)

### Step 1: Move Types to ScoringCommonModels.kt
- [x] VocalMode, VocalFeatures, VocalAnalysis
- [x] ScoringEngineType
- [x] DebuggingData, PerformanceInsights (stubs for backward compat)

### Step 2: Update All Imports
- [x] Recording.kt, PlayerAttempt.kt, BackupModels.kt

### Step 3: Gut RecordingRepository.kt
- [x] Removed VocalModeDetector dependency
- [x] Hardcoded neutral VocalAnalysis

### Step 4: Gut AudioViewModel.kt
- [x] Removed dual pipeline dependencies
- [x] Rewired to ReverseScoringEngine + Vosk ASR
- [x] Fixed init order bug (state before init block)

### Step 5: Gut CoreModule.kt
- [x] Removed VocalModeDetector provider
- [x] Removed VocalModeRouter provider
- [x] Removed VocalScoringOrchestrator provider
- [x] Removed SingingScoringEngine provider
- [x] Removed ScoreAcquisitionDataConcentrator provider

### Step 6: Delete Dead Files
- [x] VocalModeDetector.kt
- [x] VocalModeRouter.kt
- [x] VocalScoringOrchestrator.kt
- [x] Singingscoringengine.kt
- [x] Singingscoringmodels.kt
- [x] Scoreacquisitiondataconcentrator.kt
- [x] VocalModeDetectorTuner.kt
- [x] ScoringStressTester.kt

### Step 7: Dead-End Remaining Files
- [x] Deprecation notice on Speechscoringengine.kt
- [x] Deprecation notice on Speechscoringmodels.kt

---

## 🔄 PENDING PHASES

### Phase 2: Expand ReverseScoringEngine
- [ ] Add `phonemeMatches: List<Boolean>` to PhonemeScoreResult for visualization
- [ ] Add difficulty-aware duration gates:
  | Difficulty | Min | Max |
  |------------|-----|-----|
  | Easy | 50% | 150% |
  | Normal | 66% | 133% |
  | Hard | 80% | 120% |
- [ ] Add difficulty-aware phoneme leniency:
  | Difficulty | Behavior |
  |------------|----------|
  | Easy | Fuzzy matching (similar phonemes = partial credit) |
  | Normal | Exact phoneme match required |
  | Hard | Exact match + sequence order matters |

### Phase 3: Update PlayerAttempt
- [ ] Add `finalScore: Int? = null` for player override feature
- [ ] Add phoneme visualization fields (matched phonemes, total phonemes)
- [ ] Update AttemptsRepository serialization for new fields

### Phase 4: New ScoreExplanationDialog UI
- [ ] Replace 9-step breakdown with 3-step:
  1. Phoneme match (X/Y matched)
  2. Duration check (✓/✗)
  3. Final score
- [ ] Add phoneme visualization grid
- [ ] Add ACCEPT / OVERRIDE SCORE buttons
- [ ] Slider/buttons for manual score override (0/25/50/75/100)

### Phase 5: Cleanup
- [ ] Remove unused imports throughout codebase
- [ ] Remove vestigial SpeechScoringEngine from AudioViewModel (currently injected but unused for scoring)
- [ ] Remove SpeechScoringModels references from Difficultyconfig.kt
- [ ] Test all 15 themes
- [ ] Verify arc timer still works
- [ ] Test backup/restore with new scoring data

---

## 🐛 KNOWN ISSUES

### Scoring Problem
- Scores displaying but may not be accurate
- Need to debug with scorecard populated (Phase 4)

### Vestigial Code Still Present
- `SpeechScoringEngine` still injected into AudioViewModel
- `SpeechScoringModels.presetFor()` still called in AudioViewModel:802
- `speechScoringEngine.updateDifficulty()` still called on difficulty change
- These don't affect scoring (ReverseScoringEngine used) but waste resources

---

## 📁 FILE REFERENCE

### Active Scoring Files
```
scoring/
├── ReverseScoringEngine.kt     # PRIMARY - phoneme + duration scoring
├── PhonemeUtils.kt             # Phoneme extraction from text
├── ScoringCommonModels.kt      # All shared types
├── Difficultyconfig.kt         # Difficulty colors/emojis/presets
├── ScoreCalculationBreakdown.kt # Score breakdown for UI
├── ScoringDebugLogger.kt       # Debug logging
├── GarbageDetector.kt          # May use later
└── ScoringCommonUtils.kt       # Shared utilities
```

### Deprecated (kept for Forward Challenge future)
```
scoring/
├── Speechscoringengine.kt      # ⚠️ DEPRECATED
└── Speechscoringmodels.kt      # ⚠️ DEPRECATED
```

### ASR Integration
```
asr/
├── VoskTranscriptionHelper.kt  # Vosk model loading + transcription
├── SpeechRecognitionService.kt # TranscriptionResult type
└── WordAccuracyCalculator.kt   # Word comparison utilities
```

---

## 🎯 SCORING FORMULA (Current)

```
Final Score = √(phoneme_overlap) × 0.45 + √(duration_ratio) × 0.55

Where:
- phoneme_overlap = |intersection| / |union| (Jaccard similarity)
- duration_ratio = min(attempt_duration / reference_duration, 1.0)
```

---

## 📝 NOTES

- ADB path: `C:\android_sdk\platform-tools\adb`
- Vosk model: `model-small-en-us` (loaded from assets on startup)
- Recording reference transcription stored in `Recording.referenceTranscription`
- Attempt transcription stored in `PlayerAttempt.attemptTranscription`

---

*Place this file in project root or docs/ folder*
