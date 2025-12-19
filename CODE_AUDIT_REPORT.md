# Code Audit Report: Reversey2 Project

**Date:** 2025-12-19
**Build:** 0.24 Alpha
**Branch:** `main_fix-final-audit`
**Commit:** `56f55d2`
**Reviewer:** Claude (Opus 4.5)

---

## Executive Summary

| Severity | Total | Fixed | Outstanding | % Fixed |
|----------|-------|-------|-------------|---------|
| 🔴 Critical | 12 | 10 | 2 | 83% |
| 🟠 High | 19 | ~4 | ~15 | ~21% |
| 🟡 Medium | 28 | ~4 | ~24 | ~14% |
| 🔵 Low | 18 | 0 | 18 | 0% |
| **Total** | **77** | **~18** | **~59** | **~23%** |

**Status:** Core data integrity and memory issues resolved. Two critical issues remain.

---

## 🔴 CRITICAL ISSUES

### ✅ FIXED (10 of 12)

| # | Issue | File | Fix Applied |
|---|-------|------|-------------|
| 1 | Non-atomic file rename → data loss | `ThreadSafeJsonRepository.kt` | `Files.move()` with `ATOMIC_MOVE` |
| 2 | Memory exhaustion in resampling | `VoskTranscriptionHelper.kt` | Streaming 8KB chunk processing |
| 3 | Memory exhaustion WAV header | `AudioRecorderHelper.kt` | Streaming header write |
| 4 | Uncanceled CoroutineScope leak | `AudioRecorderHelper.kt` | `destroy()` calls `helperScope.cancel()` |
| 5 | Vosk model never closed | `VoskTranscriptionHelper.kt` | `cleanup()` method added |
| 7 | LCS overlap denominator | `PhonemeUtils.kt` | `maxOf(n,m)` - intentional anti-gaming |
| 8 | Non-deterministic fuzzy map | `PhonemeUtils.kt` | `sorted().first()` for determinism |
| 9 | MediaPlayer leak in Composable | `MenuPages.kt` | `DisposableEffect` lifecycle |
| 11 | Race condition AudioPlayer | `AudioPlayerHelper.kt` | `@Synchronized` annotations |
| 12 | Race condition RecordingNames | `RecordingNamesRepository.kt` | Atomic read-modify-write with mutex |

### ❌ OUTSTANDING (2 of 12)

#### Issue #6: Weak Hash for File Deduplication

**File:** `app/src/main/java/com/quokkalabs/reversey/data/backup/BackupManager.kt:833-835`

```kotlin
private fun calculateFileHash(file: File): String {
    return "${file.length()}_${file.lastModified()}"  // NOT cryptographic!
}
```

**Impact:** Different files with same size/timestamp treated as identical → potential data loss in backups.

**Recommended Fix:**
```kotlin
import java.security.MessageDigest

private fun calculateFileHash(file: File): String {
    val digest = MessageDigest.getInstance("SHA-256")
    file.inputStream().use { input ->
        val buffer = ByteArray(8192)
        var read: Int
        while (input.read(buffer).also { read = it } > 0) {
            digest.update(buffer, 0, read)
        }
    }
    return digest.digest().joinToString("") { "%02x".format(it) }
}
```

**Priority:** HIGH - Affects backup integrity

---

#### Issue #10: God Object - AudioViewModel

**File:** `app/src/main/java/com/quokkalabs/reversey/ui/viewmodels/AudioViewModel.kt`

**Size:** 1171 lines (grew from 1135)

**Responsibilities (too many):**
- Recording management
- Playback control
- Scoring logic
- Transcription coordination
- File I/O operations
- UI state management
- Tutorial state
- Easter egg handling

**Impact:**
- Difficult to test in isolation
- High coupling between unrelated features
- Maintenance nightmare
- Risk of side effects when modifying one feature

**Recommended Fix:** Split into focused ViewModels:
- `RecordingViewModel` - recording lifecycle
- `PlaybackViewModel` - audio playback
- `ScoringViewModel` - score calculation and display
- `AudioViewModel` - coordinator/facade

**Priority:** MEDIUM - Technical debt, not immediate risk

---

## 🟠 HIGH SEVERITY ISSUES

### Thread Safety (6 issues)

| Location | Issue | Status |
|----------|-------|--------|
| `AudioRecorderHelper.kt:71-72` | start/stop race condition | ❌ Outstanding |
| `AudioRecorderHelper.kt:161-203` | AudioRecord multi-context access | ❌ Outstanding |
| `BackupManager.kt:363-634` | Import state not thread-safe | ❌ Outstanding |
| `LiveTranscriptionHelper.kt:34, 42-45` | Mutable vars unsynchronized | ❌ Outstanding |
| `AudioViewModel.kt:100-102` | Recording files unprotected | ❌ Outstanding |
| `AudioViewModel.kt:651-657` | State read-then-update race | ❌ Outstanding |

### Error Handling Gaps (5 issues)

| Location | Issue | Status |
|----------|-------|--------|
| `RecordingRepository.kt:101-103, 114-116` | Empty catch blocks | ❌ Outstanding |
| `AttemptsRepository.kt:22-24` | Save failures not propagated | ❌ Outstanding |
| `AttemptsRepository.kt:38-40` | Load errors indistinguishable | ❌ Outstanding |
| `AudioPlayerHelper.kt:53-60` | pause/resume lack try-catch | ⚠️ Partial |
| `AudioViewModel.kt:467-694` | Inconsistent error handling | ❌ Outstanding |

### Security Gaps (4 issues)

| Location | Issue | Status |
|----------|-------|--------|
| `RecordingRepository.kt:229` | Path traversal risk | ❌ Outstanding |
| `RecordingRepository.kt:121` | Insufficient filename validation | ❌ Outstanding |
| `ThreadSafeJsonRepository.kt:267-279` | Security bypass methods exposed | ❌ Outstanding |
| `BackupManager.kt:383-387` | Suspicious zip entries silent | ❌ Outstanding |

### Missing Validation (4 issues)

| Location | Issue | Status |
|----------|-------|--------|
| `DifficultyConfig.kt:142-146` | Division by zero possible | ❌ Outstanding |
| `ScoringCommonModels.kt:107-152` | No weight/threshold validation | ❌ Outstanding |
| `RecordingRepository.kt:53` | Only checks size, not WAV header | ❌ Outstanding |
| `GarbageDetector.kt:94, 105, 126` | No null safety for audioProcessor | ❌ Outstanding |

---

## 🟡 MEDIUM SEVERITY ISSUES

### Performance (8 issues)

| Location | Issue | Status |
|----------|-------|--------|
| `RecordingRepository.kt:193` | Context switch per buffer | ❌ Outstanding |
| `PhonemeUtils.kt:252-272` | O(n²) fuzzy matching | ❌ Outstanding |
| `ReverseScoringEngine.kt:295` | O(m×n) space LCS | ❌ Outstanding |
| `Analysistoast.kt:109-129` | Infinite animation after dismiss | ❌ Outstanding |
| `AudioViewModel.kt:981-991` | File I/O on Main thread risk | ❌ Outstanding |
| `VoskTranscriptionHelper.kt:64-68` | Polling instead of callbacks | ❌ Outstanding |
| `ScoreExplanationDialog.kt:133-161` | Canvas redraws every phase | ✅ Improved |
| `AudioViewModel.kt:93-94, 478-481` | Hardcoded polling delays | ❌ Outstanding |

### Edge Cases (10 issues)

| Location | Issue | Status |
|----------|-------|--------|
| `RecordingRepository.kt:219-224` | WAV reversal odd byte count | ❌ Outstanding |
| `RecordingRepository.kt:136` | Rename failure unreported | ❌ Outstanding |
| `BackupManager.kt:220-232` | Counter increment on missing file | ❌ Outstanding |
| `BackupManager.kt:799` | Large manifest OOM | ❌ Outstanding |
| `PhonemeUtils.kt:219-227` | Empty target returns 0 | ❌ Outstanding |
| `ReverseScoringEngine.kt:425` | NaN/Infinity unhandled | ❌ Outstanding |
| `ScoringCommonUtils.kt:72-87` | cosine outside [-1, 1] | ❌ Outstanding |
| `RecordingRepository.kt:295-298` | Directory creation unchecked | ❌ Outstanding |
| `Recordingitemdialogs.kt:72-80` | No max input length | ❌ Outstanding |
| `ScoreExplanationDialog.kt:661` | Integer division precision | ✅ Fixed |

### Maintainability (10 issues)

| Location | Issue | Status |
|----------|-------|--------|
| `BackupManager.kt:123-316` | performExport 193 lines | ❌ Outstanding |
| `BackupManager.kt:322-648` | importBackup 326 lines | ❌ Outstanding |
| `GarbageDetector.kt:111, 124, 159-161` | Magic numbers | ❌ Outstanding |
| `ScoreExplanationDialog.kt:138-236` | 6+ nesting levels | ✅ Improved |
| `SettingsContent.kt:306-395` | 90 lines commented code | ❌ Outstanding |
| `SnowyOwlThemeComponents.kt:1303` | 140+ char one-liner | ❌ Outstanding |
| `ReverseScoringEngine.kt:27-29, 55` | Hardcoded scoring configs | ❌ Outstanding |
| `PhonemeUtils.kt:132-135` | Pseudo-phoneme fallback | ❌ Outstanding |
| `Recordingitemdialogs.kt:30-34` | remember without key | ❌ Outstanding |
| `ScoreExplanationDialog.kt:114-116` | State hoisting violation | ❌ Outstanding |

---

## 🔵 LOW SEVERITY ISSUES

| Category | Count | Status |
|----------|-------|--------|
| Missing accessibility (contentDescription) | 1 | ❌ Outstanding |
| Hardcoded colors (not using theme) | 1 | ❌ Outstanding |
| Strings not localized | 1 | ❌ Outstanding |
| Unused/duplicate imports | 1 | ❌ Outstanding |
| Missing @Preview annotations | 1 | ❌ Outstanding |
| No loading state indicators | 1 | ❌ Outstanding |
| Inconsistent empty list handling | 1 | ❌ Outstanding |
| Test code in production | 1 | ❌ Outstanding |
| Magic padding numbers | 1 | ❌ Outstanding |
| Handler instead of coroutines | 1 | ❌ Outstanding |
| Documentation gaps | 4 | ⚠️ Partial (SCORING_MANUAL.md added) |
| Minor bugs | 4 | ❌ Outstanding |

---

## ✅ Fixes Applied in 0.23-0.24 Alpha

### 0.23 Alpha
- **DifficultySquircle:** `Arrangement.SpaceEvenly` fixes text cutoff
- **FormulaToast:** Uses `calculateFormulaBreakdown()` for correct formula display
- **Added:** `FormulaBreakdown` data class
- **Added:** `SCORING_MANUAL.md` documentation

### 0.24 Alpha
- **All 5 Pro Themes:**
  - AttemptItem Rev button polymorphic (icon/label switch)
  - RecordingItem Rev button icon polymorphic
  - Score override displays correctly (uses finalScore)
  - Squircle emoji uses `aesthetic.scoreEmojis` lookup
  - Reset to automatic score wired to ScoreExplanationDialog
  - Challenge progress bar hidden unless playing
- **Egg Theme Visual Refresh:**
  - Black lines → mid brown (#6B5344)
  - Wobbly hand-drawn borders (WobblyShape class)
  - Player name box: wobbly border + random rotation
- **Optimization:** gingham_cloth.png reduced 9MB → 3MB

---

## 🛡️ Security Status

### ✅ Good Practices (Unchanged)
- Zip Slip prevention in `SecurityUtils.kt`
- Path traversal validation
- Null byte detection
- File size limits (zip bomb prevention)
- Magic byte validation

### ⚠️ Gaps Remaining
- Weak file hash (size+timestamp instead of SHA-256)
- Filename sanitization inconsistent
- Direct file access methods bypass mutex
- Silent skip of suspicious zip entries

---

## 📋 Priority Action Plan

### Immediate (Next Release)
1. **Fix weak hash** in `BackupManager.kt` - use SHA-256
2. **Add @Synchronized** to `AudioRecorderHelper.kt` start/stop

### Short-Term (Next Sprint)
3. Add proper error propagation (use `Result<T>` type)
4. Split `AudioViewModel` into smaller ViewModels
5. Add comprehensive filename validation
6. Fix division by zero in `DifficultyConfig.kt`

### Medium-Term (Next Month)
7. Refactor `BackupManager` large methods
8. Add unit tests for repositories and scoring
9. Fix accessibility issues
10. Extract magic numbers to config objects

---

## Files Changed Since Original Audit

| File | Changes |
|------|---------|
| `ThreadSafeJsonRepository.kt` | Atomic file operations |
| `VoskTranscriptionHelper.kt` | Streaming + cleanup |
| `AudioRecorderHelper.kt` | Streaming + destroy |
| `PhonemeUtils.kt` | Determinism + documentation |
| `MenuPages.kt` | MediaPlayer lifecycle |
| `AudioPlayerHelper.kt` | Thread safety |
| `RecordingNamesRepository.kt` | Atomic operations |
| `DifficultySquircle.kt` | Layout fix |
| `ScoreExplanationDialog.kt` | Formula display fix |
| `ReverseScoringEngine.kt` | FormulaBreakdown |
| `*ThemeComponents.kt` (5 files) | Pro theme fixes |
| `SCORING_MANUAL.md` | New documentation |

---

## Conclusion

**Build 0.24 Alpha** has addressed the most critical data integrity and memory issues. The app is significantly more stable than v0.17, with 83% of critical issues resolved.

**Remaining Priorities:**
1. 🔴 Fix weak hash (backup integrity risk)
2. 🟠 Address remaining thread safety issues
3. 🟡 Refactor AudioViewModel (technical debt)

**Recommendation:** Safe for continued testing. The weak hash issue should be fixed before any production backup/restore scenarios are relied upon.

---

*Report generated by Claude Code Audit*
*Original audit: 2025-12-17 | Updated: 2025-12-19*
