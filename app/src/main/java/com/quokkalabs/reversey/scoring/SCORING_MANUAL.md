# ReVerseY Scoring Engine Manual

This document explains how ReVerseY calculates scores for reverse speech attempts.

## Overview

The scoring engine evaluates how accurately a player reproduces reversed phonemes. It uses a weighted model called **PHONEME_PRIMARY**:

- **Phoneme Base (85%)** — How well the sounds match
- **Duration Bonus (15%)** — How well the timing matches

```
FinalScore = (PhonemeBase + DurationBonus) × 100
```

---

## Phoneme Matching (85%)

The engine converts text to phonemes using the CMU Pronouncing Dictionary (~126,000 words). The matching algorithm varies by difficulty.

### Difficulty Modes

| Difficulty | Mode | Formula | Description |
|------------|------|---------|-------------|
| **Easy** | FUZZY | `matchScore / target` | Similar phonemes get partial credit. Order doesn't matter. |
| **Normal** | EXACT | `intersection / union` | Jaccard similarity. Penalises extra "garbage" sounds. |
| **Hard** | ORDERED | `lcs / target` | Longest Common Subsequence. Correct sounds in correct order. |

### FUZZY (Easy) — Partial Credit

Similar-sounding phonemes receive partial credit:

| Phoneme | Similar | Credit |
|---------|---------|--------|
| T | D | 0.7 |
| P | B | 0.7 |
| AH | AE | 0.7 |
| IH | IY | 0.7 |
| S | Z | 0.7 |
| TH | DH | 0.8 |

Example: If target has "T" and player says "D", they get 0.7 instead of 0.

### EXACT (Normal) — Jaccard Index

```
intersection = phonemes in both target and attempt
union = target + attempt - intersection
overlap = intersection / union
```

This penalises players who pad their attempt with extra sounds.

### ORDERED (Hard) — Longest Common Subsequence

Phonemes must appear in the correct relative order. Uses dynamic programming to find the longest matching subsequence.

### The Forgiveness Curve

Raw overlap is transformed with a square root before weighting:

```
PhonemeBase = √(overlap) × 0.85
```

This makes low scores less punishing while keeping 100% difficult.

| Raw Overlap | After √ | PhonemeBase |
|-------------|---------|-------------|
| 25% | 50% | 42.5% |
| 50% | 71% | 60% |
| 75% | 87% | 74% |
| 100% | 100% | 85% |

---

## Duration Bonus (15%)

Timing is evaluated by comparing attempt duration to target duration. The bonus peaks at 1.0x (exact match) and falls off using a Gaussian curve:

```
DurationBonus = 0.15 × e^(-(ratio - 1)² / width)
```

### Width by Difficulty

| Difficulty | Width | Tolerance |
|------------|-------|-----------|
| Easy | 0.3 | Very forgiving |
| Normal | 0.2 | Moderate |
| Hard | 0.1 | Tight window |

### Example Values (Normal, width=0.2)

| Duration Ratio | Bonus |
|----------------|-------|
| 1.00x (perfect) | 15.0% |
| 1.10x (+10%) | 14.3% |
| 1.20x (+20%) | 12.3% |
| 1.50x (+50%) | 4.6% |
| 2.00x (double) | 0.1% |

---

## Worked Examples

### Example 1: Normal Difficulty

```
Target phonemes:  [HH, AE, N, AH, HH, AE, N, AH, HH, AE, N, AH]  (12)
Attempt phonemes: [HH, AE, N, AH, HH, AE, N, AH, B, L, AH]       (11)

EXACT matching (Jaccard):
  intersection = 8
  union = 12 + 11 - 8 = 15
  overlap = 8 / 15 = 0.533

PhonemeBase = √(0.533) × 0.85 = 0.620

Duration ratio = 1.10x
DurationBonus = 0.15 × e^(-(0.10)²/0.2) = 0.143

FinalScore = (0.620 + 0.143) × 100 = 76%
```

### Example 2: Easy Difficulty with Partial Credit

```
Target phonemes:  [T, AH, P]  (3)
Attempt phonemes: [D, AH, B]  (3)

FUZZY matching:
  D matches T with 0.7 credit
  AH matches AH with 1.0 credit
  B matches P with 0.7 credit
  matchScore = 0.7 + 1.0 + 0.7 = 2.4
  overlap = 2.4 / 3 = 0.80

PhonemeBase = √(0.80) × 0.85 = 0.760

Duration ratio = 1.05x
DurationBonus = 0.15 × e^(-(0.05)²/0.3) = 0.149

FinalScore = (0.760 + 0.149) × 100 = 90%
```

### Example 3: Hard Difficulty (Order Matters)

```
Target phonemes:  [K, AE, T]  (3)
Attempt phonemes: [T, AE, K]  (3) — reversed order!

ORDERED matching (LCS):
  Longest common subsequence = [AE] = 1
  overlap = 1 / 3 = 0.333

PhonemeBase = √(0.333) × 0.85 = 0.490

Duration ratio = 0.95x
DurationBonus = 0.15 × e^(-(−0.05)²/0.1) = 0.146

FinalScore = (0.490 + 0.146) × 100 = 63%
```

Note: Same phonemes, but wrong order = low score on Hard.

---

## Garbage Detection

The garbage detector filters out invalid attempts (humming, "blah blah blah", noise). It runs five filters:

1. **MFCC Variance** — Detects repetitive sounds
2. **Pitch Contour** — Detects monotone or unnatural oscillation
3. **Spectral Entropy** — Detects low-complexity noise
4. **Zero Crossing Rate** — Detects hums or electronic interference
5. **Silence Ratio** — Ensures natural speech pauses exist

### Rejection Criteria

An attempt is rejected when **both** conditions are met:

- Garbage score > 0.6 (weighted sum of filter failures)
- Two or more filters failed

### Configuration

Garbage detection can be disabled via `GarbageDetectionParameters.enableGarbageDetection`.

---

## Score Thresholds

| Threshold | Score | Effect |
|-----------|-------|--------|
| Auto-Accept | ≥ 90% | Triggers celebration |
| Auto-Reject | ≤ 15% | Flagged as invalid |

Players can override any score manually via the score explanation dialog.

---

## Empty Input Handling

- Empty target phonemes → overlap = 0
- Empty attempt phonemes → overlap = 0 (FUZZY/EXACT) or 0 (ORDERED via LCS)
- Duration ratio defaults to 1.0 if target duration is 0

---

## API Reference

### Main Scoring Function

/**```kotlin
ReverseScoringEngine.score(
    targetText: String,
    attemptText: String,
    targetDurationMs: Long,
    attemptDurationMs: Long,
    difficulty: DifficultyLevel = DifficultyLevel.NORMAL
): PhonemeScoreResult
```

### Formula Breakdown (for UI)

```kotlin
ReverseScoringEngine.calculateFormulaBreakdown(
    targetPhonemes: List<String>,
    attemptPhonemes: List<String>,
    difficulty: DifficultyLevel,
    durationRatio: Float
): FormulaBreakdown
```

### Text-Only Scoring (no duration)

```kotlin
ReverseScoringEngine.scoreTextOnly(
    targetText: String,
    attemptText: String
): Int
```*/
