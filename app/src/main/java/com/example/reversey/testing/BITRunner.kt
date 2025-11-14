package com.example.reversey.testing

import android.content.Context
import android.util.Log
import com.example.reversey.scoring.ScoringEngine
import com.example.reversey.scoring.DifficultyLevel
import com.example.reversey.scoring.ScoringPresets
import com.example.reversey.data.models.ChallengeType
import com.example.reversey.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Built-In Test (BIT) Runner - Multi-File Testing
 *
 * Tests all 15 synthetic WAV files to validate scoring parameters
 */
@Singleton
class BITRunner @Inject constructor(
    private val context: Context,
    private val scoringEngine: ScoringEngine
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

    suspend fun runAllTests(onProgress: (Int, Int) -> Unit): Result<String> = withContext(Dispatchers.IO) {
        try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val outputFile = File("/storage/emulated/0/Download/BIT_MultiFile_Results_$timestamp.txt")

            // Write header
            FileWriter(outputFile, false).use { writer ->
                writer.write("╔══════════════════════════════════════════════════════════════════╗\n")
                writer.write("║         ReVerseY Multi-File Parameter Validation                ║\n")
                writer.write("╚══════════════════════════════════════════════════════════════════╝\n")
                writer.write("\n")
                writer.write("Started: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())}\n")
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

            // Apply NORMAL difficulty preset
            val preset = ScoringPresets.normalMode()
            preset.garbage.enableGarbageDetection = false
            scoringEngine.applyPreset(preset)

            // 🎯 FIXED: Load the "Original" baseline audio ONCE
            Log.d(TAG, "Loading baseline comparison audio: bit_test_baseline.wav")
            val originalAudio = loadWavFile(R.raw.bit_test_baseline)

            var testNumber = 1
            val totalTests = testFiles.size
            val results = mutableListOf<TestResult>()

            // Run tests
            testFiles.forEach { testFile ->
                onProgress(testNumber, totalTests)

                try {
                    // 🎯 FIXED: Load the "Attempt" audio
                    val attemptAudio = loadWavFile(testFile.resourceId)

                    // 🎯 FIXED: Pass BOTH audio arrays to the test
                    val result = runSingleTest(originalAudio, attemptAudio, testFile, ChallengeType.FORWARD, outputFile)
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

    private fun runSingleTest(
        originalAudio: FloatArray,  // 🎯 CHANGED: The baseline audio
        attemptAudio: FloatArray,   // 🎯 CHANGED: The test file audio
        testFile: TestFile,
        challengeType: ChallengeType,
        outputFile: File
    ): TestResult {
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())

        // 🎯 FIXED: Run scoring comparing baseline (original) vs test file (attempt)
        val result = scoringEngine.scoreAttempt(
            originalAudio = originalAudio,   // The baseline: bit_test_baseline.wav
            playerAttempt = attemptAudio,    // The test file: bit_test_pitch_025off.wav, etc.
            challengeType = challengeType,
            sampleRate = 44100
        )

        val target = testFile.targetForward
        val error = result.score - target
        val passed = Math.abs(error) <= 10  // ±10 points tolerance

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
            val avgError = results.map { Math.abs(it.error) }.average()

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
            val problemTests = results.filter { !it.passed }.sortedByDescending { Math.abs(it.error) }
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
            writer.write("Completed: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())}\n")
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