package com.quokkalabs.reversey.testing


import android.content.Context
import android.util.Log
import com.quokkalabs.reversey.R
import com.quokkalabs.reversey.audio.AudioConstants
import com.quokkalabs.reversey.data.models.ChallengeType
import com.quokkalabs.reversey.scoring.DifficultyLevel
import com.quokkalabs.reversey.scoring.VocalScoringOrchestrator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext
import com.quokkalabs.reversey.scoring.SingingScoringEngine

/**
 * Built-In Test (BIT) Runner - Multi-File Testing
 * suitable only for testing scoring of singing tonal differences
 * Tests all 15 synthetic WAV files to validate scoring parameters
 */

/**
 * ══════════════════════════════════════════════════════════════════════════════
 * 🔧 BITRunner — Built-In Test Runner (SYNTHETIC VALIDATION)
 *    suitable only for testing scoring of singing tonal differences
 * ══════════════════════════════════════════════════════════════════════════════
 *
 * PURPOSE:
 *   Validates scoring algorithm correctness using SYNTHETIC audio files with
 *   KNOWN TARGET SCORES. Tests the math, not the vibe.
 *
 * KEY QUESTION:
 *   "If I shift pitch by exactly 0.5 semitones, does the score drop by the
 *    expected amount?"
 *
 * ──────────────────────────────────────────────────────────────────────────────
 * HOW IT DIFFERS FROM ScoringStressTester:
 * ──────────────────────────────────────────────────────────────────────────────
 *
 *   BITRunner (this file)          │  ScoringStressTester
 *   ───────────────────────────────┼────────────────────────────────────────
 *   Synthetic audio files          │  Human recordings (CPD's files)
 *   res/raw/ folder                │  assets/bit_audio/ folder
 *   Known target scores (85%, etc) │  No targets — exploratory
 *   Tests ALGORITHM correctness    │  Tests REAL-WORLD experience
 *   Output: .txt pass/fail report  │  Output: .csv for analysis
 *   Single mode (no speech/sing)   │  Dual mode (speech vs singing routing)
 *
 * ──────────────────────────────────────────────────────────────────────────────
 * TEST FILES (in res/raw/):
 * ──────────────────────────────────────────────────────────────────────────────
 *
 *   bit_test_baseline.wav      → 90%  (perfect reference)
 *   bit_test_pitch_025off.wav  → 85%  (pitch +0.25 semitones)
 *   bit_test_pitch_050off.wav  → 75%  (pitch +0.5 semitones)
 *   bit_test_pitch_100off.wav  → 60%  (pitch +1.0 semitones)
 *   bit_test_pitch_200off.wav  → 45%  (pitch +2.0 semitones)
 *   bit_test_monotone.wav      → 25%  (GARBAGE TEST - flat tone)
 *   bit_test_octave_up.wav     → 55%  (octave shift)
 *   bit_test_wrong_notes.wav   → 50%  (3 notes changed)
 *   bit_test_all_wrong.wav     → 35%  (completely different melody)
 *   bit_test_missing_notes.wav → 48%  (3 notes removed)
 *   bit_test_fast.wav          → 70%  (1.5x speed)
 *   bit_test_delayed_50ms.wav  → 85%  (timing offset)
 *   bit_test_delayed_100ms.wav → 75%  (larger timing offset)
 *   bit_test_noise_20db.wav    → 82%  (light noise)
 *   bit_test_noise_10db.wav    → 65%  (heavy noise)
 *
 * ──────────────────────────────────────────────────────────────────────────────
 * OUTPUT:
 * ──────────────────────────────────────────────────────────────────────────────
 *
 *   Location: /storage/emulated/0/Download/BIT_MultiFile_Results_[timestamp].txt
 *
 *   Contains:
 *     - Per-file breakdown (target vs actual, pitch/MFCC similarity)
 *     - Pass/fail status (±10 points tolerance)
 *     - Summary with tuning recommendations
 *
 * ──────────────────────────────────────────────────────────────────────────────
 * WHEN TO USE:
 * ──────────────────────────────────────────────────────────────────────────────
 *
 *   ✅ After changing scoring algorithms (DCT, cosine distance, etc.)
 *   ✅ After adjusting threshold parameters
 *   ✅ Quick sanity check that nothing is catastrophically broken
 *   ✅ Debugging unexpected score behavior
 *
 *   ❌ NOT for validating real-world user experience (use ScoringStressTester)
 *   ❌ NOT for tuning "feel" of scores (use human recordings)
 *
 * ──────────────────────────────────────────────────────────────────────────────
 * NOTE ON TARGET SCORES:
 * ──────────────────────────────────────────────────────────────────────────────
 *
 *   Target scores were calibrated for a specific algorithm version.
 *   After major algorithm changes (e.g., Phase 2 DCT+Cosine), expect scores
 *   to shift. The GRADIENT matters more than absolute values:
 *
 *     - Baseline should still be highest
 *     - Monotone should still be crushed
 *     - Worse variants should score lower than better variants
 *
 * ══════════════════════════════════════════════════════════════════════════════
 */
@Singleton
class BITRunner @Inject constructor(
    @ApplicationContext private val context: Context,
    private val orchestrator: VocalScoringOrchestrator,
    private val singingScoringEngine: SingingScoringEngine  // ← ADD
) {
    private val TAG = "BITRunner"

    // Define all 15 test files with their expected target scores
    private data class TestFile(
        val resourceId: Int,
        val filename: String,
        val targetForward: Int,
        val targetReverse: Int,
        val description: String
    )

    private val testFiles = listOf(
        TestFile(R.raw.bit_test_baseline, "bit_test_baseline.wav", 90, 85, "Perfect C major scale"),
        TestFile(R.raw.bit_test_pitch_025off, "bit_test_pitch_025off.wav", 85, 78, "Pitch +0.25 semitones off"),
        TestFile(R.raw.bit_test_pitch_050off, "bit_test_pitch_050off.wav", 75, 68, "Pitch +0.5 semitones off"),
        TestFile(R.raw.bit_test_pitch_100off, "bit_test_pitch_100off.wav", 60, 53, "Pitch +1.0 semitones off"),
        TestFile(R.raw.bit_test_pitch_200off, "bit_test_pitch_200off.wav", 45, 40, "Pitch +2.0 semitones off"),
        TestFile(R.raw.bit_test_wrong_notes, "bit_test_wrong_notes.wav", 50, 45, "Wrong notes (3 changed)"),
        TestFile(R.raw.bit_test_monotone, "bit_test_monotone.wav", 25, 22, "Monotone (flat tone - GARBAGE TEST)"),
        TestFile(R.raw.bit_test_octave_up, "bit_test_octave_up.wav", 55, 50, "Octave up (+12 semitones)"),
        TestFile(R.raw.bit_test_delayed_50ms, "bit_test_delayed_50ms.wav", 85, 80, "Delayed 50ms"),
        TestFile(R.raw.bit_test_delayed_100ms, "bit_test_delayed_100ms.wav", 75, 70, "Delayed 100ms"),
        TestFile(R.raw.bit_test_noise_20db, "bit_test_noise_20db.wav", 82, 77, "Noise (SNR 20dB)"),
        TestFile(R.raw.bit_test_noise_10db, "bit_test_noise_10db.wav", 65, 60, "Noise (SNR 10dB)"),
        TestFile(R.raw.bit_test_all_wrong, "bit_test_all_wrong.wav", 35, 32, "All wrong (different melody)"),
        TestFile(R.raw.bit_test_missing_notes, "bit_test_missing_notes.wav", 48, 43, "Missing notes (3 removed)"),
        TestFile(R.raw.bit_test_fast, "bit_test_fast.wav", 70, 65, "Fast (1.5x speed)")
    )

    suspend fun runAllTests(onProgress: (Int, Int) -> Unit): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                val outputFile =
                    File("/storage/emulated/0/Download/BIT_MultiFile_Results_$timestamp.txt")

                // Write header
                FileWriter(outputFile, false).use { writer ->
                    writer.write("╔══════════════════════════════════════════════════════════════════╗\n")
                    writer.write("║         ReVerseY Multi-File Parameter Validation                ║\n")
                    writer.write("╚══════════════════════════════════════════════════════════════════╝\n")
                    writer.write("\n")
                    writer.write("Started: ${
                        SimpleDateFormat(
                            "yyyy-MM-dd HH:mm:ss",
                            Locale.US
                        ).format(Date())
                    }\n")
                    writer.write("Test Type: Parameter validation across 15 synthetic test files\n")
                    writer.write("Mode: FORWARD only (REVERSE optional)\n")
                    writer.write("Difficulty: NORMAL preset\n")
                    writer.write("\n")
                    writer.write("Target Scores:\n")
                    writer.write("  • Perfect baseline: 90%\n")
                    writer.write("  • Good matches: 75-85%\n")
                    writer.write("  • Moderate errors: 50-70%\n")
                    writer.write("  • Garbage (monotone): 25%\n")
                    writer.write("\n")
                    writer.write("═".repeat(70) + "\n\n")
                }

                // 🔁 We no longer have ScoringPresets + ScoringEngine here.
                //    The orchestrator and engines apply difficulty internally.
                //    For BIT we standardise on NORMAL difficulty:
                val difficulty = DifficultyLevel.NORMAL

                // 🎯 Load the "Original" baseline audio ONCE
                Log.d(TAG, "Loading baseline comparison audio: bit_test_baseline.wav")
                val originalAudio = loadWavFile(R.raw.bit_test_baseline)

                var testNumber = 1
                val totalTests = testFiles.size
                val results = mutableListOf<TestResult>()

                // Run tests
                for (testFile in testFiles) {
                    onProgress(testNumber, totalTests)

                    try {
                        // 🎯 Load the "Attempt" audio
                        val attemptAudio = loadWavFile(testFile.resourceId)

                        // 🎯 Pass BOTH audio arrays into the orchestrated dual-lane scoring
                        val result = runSingleTest(
                            originalAudio = originalAudio,
                            attemptAudio = attemptAudio,
                            testFile = testFile,
                            challengeType = ChallengeType.FORWARD,
                            difficulty = difficulty,
                            outputFile = outputFile
                        )
                        results.add(result)
                    } catch (e: Exception) {
                        Log.e(TAG, "Test $testNumber failed", e)
                        FileWriter(outputFile, true).use { writer ->
                            writer.write("─".repeat(70) + "\n")
                            writer.write("TEST #$testNumber: ${testFile.filename}\n")
                            writer.write("❌ ERROR: ${e.message}\n\n")
                        }
                    }

                    testNumber++
                }

                // Write summary
                writeSummary(outputFile, results)

                Result.success("BIT Complete: ${results.size}/${totalTests} tests executed. Results: ${outputFile.name}")
            } catch (e: Exception) {
                Log.e(TAG, "BIT failed", e)
                Result.failure(e)
            }
        }

    private data class TestResult(
        val filename: String,
        val targetScore: Int,
        val actualScore: Int,
        val pitchSimilarity: Float,
        val mfccSimilarity: Float,
        val error: Int,
        val passed: Boolean
    )

    // ✅ NOW SUSPEND – because VocalScoringOrchestrator.scoreAttempt is suspend
    private suspend fun runSingleTest(
        originalAudio: FloatArray,   // The baseline audio
        attemptAudio: FloatArray,    // The test file audio
        testFile: TestFile,
        challengeType: ChallengeType,
        difficulty: DifficultyLevel,
        outputFile: File
    ): TestResult {
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())

        // BIT uses SingingEngine directly (synthetic tonal test files)
        val result = singingScoringEngine.scoreAttempt(
            originalAudio = originalAudio,
            playerAttempt = attemptAudio,
            challengeType = challengeType,
            difficulty = difficulty,
            sampleRate = AudioConstants.SAMPLE_RATE
        )

        val target = testFile.targetForward
        val error = result.score - target
        val passed = kotlin.math.abs(error) <= 10  // ±10 points tolerance

        // Write to file
        FileWriter(outputFile, true).use { writer ->
            writer.write("─".repeat(70) + "\n")
            writer.write("${testFile.filename}\n")
            writer.write("Description: ${testFile.description}\n")
            writer.write("─".repeat(70) + "\n")
            writer.write("Target Score:      ${target}%\n")
            writer.write("Actual Score:      ${result.score}%\n")
            writer.write("Error:             ${if (error >= 0) "+" else ""}${error} points\n")
            writer.write("Pitch Similarity:  ${String.format("%.4f", result.metrics.pitch)}\n")
            writer.write("MFCC Similarity:   ${String.format("%.4f", result.metrics.mfcc)}\n")
            writer.write("\n")

            val status = when {
                passed && result.score >= target - 5 -> "✅ EXCELLENT (within target range)"
                passed -> "✅ PASS (within tolerance)"
                error > 0 -> "⚠️  TOO HIGH (scoring too lenient)"
                else -> "⚠️  TOO LOW (scoring too strict)"
            }
            writer.write("Status: $status\n")
            writer.write("\n")
        }

        Log.d(TAG, "${testFile.filename}: Target=$target, Actual=${result.score}, Error=$error")

        return TestResult(
            filename = testFile.filename,
            targetScore = target,
            actualScore = result.score,
            pitchSimilarity = result.metrics.pitch,
            mfccSimilarity = result.metrics.mfcc,
            error = error,
            passed = passed
        )
    }

    private fun writeSummary(outputFile: File, results: List<TestResult>) {
        FileWriter(outputFile, true).use { writer ->
            writer.write("\n")
            writer.write("═".repeat(70) + "\n")
            writer.write("SUMMARY\n")
            writer.write("═".repeat(70) + "\n\n")

            // Overall stats
            val passed = results.count { it.passed }
            val total = results.size
            val avgError = results.map { kotlin.math.abs(it.error) }.average()

            writer.write("Tests Passed: $passed/$total (${passed * 100 / total}%)\n")
            writer.write("Average Error: ${String.format("%.1f", avgError)} points\n")
            writer.write("\n")

            // Critical tests
            writer.write("Critical Tests:\n")
            val baseline = results.find { it.filename.contains("baseline") }
            val monotone = results.find { it.filename.contains("monotone") }

            baseline?.let {
                val status = if (it.actualScore >= 85) "✅" else "⚠️ "
                writer.write("  ${status} Baseline: ${it.actualScore}% (target: ${it.targetScore}%)\n")
            }

            monotone?.let {
                val status = if (it.actualScore <= 35) "✅" else "⚠️ "
                writer.write("  ${status} Monotone: ${it.actualScore}% (target: ${it.targetScore}%) - GARBAGE TEST\n")
            }
            writer.write("\n")

            // Problem areas
            val problemTests = results.filter { !it.passed }.sortedByDescending { kotlin.math.abs(it.error) }
            if (problemTests.isNotEmpty()) {
                writer.write("Tests Needing Attention:\n")
                problemTests.take(5).forEach { test ->
                    val direction = if (test.error > 0) "TOO HIGH" else "TOO LOW"
                    writer.write("  ⚠️  ${test.filename}: ${if (test.error > 0) "+" else ""}${test.error} points ($direction)\n")
                }
                writer.write("\n")
            }

            // Recommendations
            writer.write("Parameter Tuning Recommendations:\n")

            val allTooHigh = results.count { it.error > 10 } > results.size / 2
            val allTooLow = results.count { it.error < -10 } > results.size / 2

            when {
                allTooHigh -> {
                    writer.write("  → Scoring is TOO LENIENT overall\n")
                    writer.write("  → Consider: Decreasing pitchTolerance\n")
                    writer.write("  → Consider: Increasing penalties\n")
                }
                allTooLow -> {
                    writer.write("  → Scoring is TOO STRICT overall\n")
                    writer.write("  → Consider: Increasing pitchTolerance\n")
                    writer.write("  → Consider: Decreasing penalties\n")
                }
                monotone?.actualScore ?: 0 > 35 -> {
                    writer.write("  → Garbage detection is WEAK\n")
                    writer.write("  → Consider: Decreasing monotonePenalty (make it harsher)\n")
                    writer.write("  → Consider: Checking mfccWeight (should be >0.35)\n")
                }
                else -> {
                    writer.write("  → Parameters are generally well-tuned!\n")
                    writer.write("  → Minor adjustments may improve specific tests\n")
                    writer.write("  → VALIDATE WITH REAL RECORDINGS!\n")
                }
            }

            writer.write("\n")
            writer.write("═".repeat(70) + "\n")
            writer.write(
                "Completed: ${
                    SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss",
                        Locale.US
                    ).format(Date())
                }\n"
            )
            writer.write("═".repeat(70) + "\n")
        }
    }

    private fun loadWavFile(resourceId: Int): FloatArray {
        val inputStream = context.resources.openRawResource(resourceId)

        // Skip WAV header (44 bytes)
        val header = ByteArray(44)
        inputStream.read(header)

        // Read audio data
        val audioBytes = inputStream.readBytes()
        inputStream.close()

        // Convert 16-bit PCM to FloatArray (-1.0 to 1.0)
        val samples = FloatArray(audioBytes.size / 2)
        val buffer = ByteBuffer.wrap(audioBytes).order(ByteOrder.LITTLE_ENDIAN)

        for (i in samples.indices) {
            val sample = buffer.short.toFloat() / 32768f
            samples[i] = sample.coerceIn(-1f, 1f)
        }

        return samples
    }
}
