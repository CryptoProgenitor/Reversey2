package com.example.reversey.testing

import android.content.Context
import android.util.Log
import com.example.reversey.scoring.ScoringEngine
import com.example.reversey.scoring.DifficultyLevel
import com.example.reversey.scoring.ScoringPresets
import com.example.reversey.data.models.ChallengeType
import com.example.reversey.R // 🎯 ADDED THIS IMPORT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*
import java.io.InputStream // 🎯 CHANGED THIS IMPORT
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Built-In Test (BIT) Runner
 *
 * Validates scoring algorithm by running identical audio through both channels.
 * Expected results:
 * - Correlation = 1.0
 * - Interval Similarity = 1.0
 * - Final Score = 100%
 */
@Singleton
class BITRunner @Inject constructor(
    private val context: Context,
    private val scoringEngine: ScoringEngine
) {
    private val TAG = "BITRunner"

    suspend fun runAllTests(onProgress: (Int, Int) -> Unit): Result<String> = withContext(Dispatchers.IO) {
        try {
            // 🎯 CHANGED: Load the synthetic test file from res/raw
            Log.d(TAG, "✅ Loading synthetic test file: R.raw.bit_test_audio")
            val audioData = loadWavFile(R.raw.bit_test_audio)
            Log.d(TAG, "File size: ${audioData.size * 2} bytes (approx)") // *2 for 16-bit

            // Create single output file with timestamp
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val outputFile = File("/storage/emulated/0/Download/BIT_Results_$timestamp.txt")

            // Write header
            FileWriter(outputFile, false).use { writer ->
                writer.write("╔══════════════════════════════════════════════════════════════════╗\n")
                writer.write("║           ReVerseY Built-In Test (BIT) Results                  ║\n")
                writer.write("║              Scoring Algorithm Validation                        ║\n")
                writer.write("╚══════════════════════════════════════════════════════════════════╝\n")
                writer.write("\n")
                // 🎯 CHANGED: Hard-coded the test file name
                writer.write("Test File: bit_test_audio.wav\n")
                writer.write("Source: res/raw/bit_test_audio.wav\n")
                writer.write("File Size: ${audioData.size * 2} bytes (approx)\n")
                writer.write("Started: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())}\n")
                writer.write("Test Type: Identical audio scoring (Original = Attempt)\n")
                writer.write("Expected: All scores should be 100% or near-perfect\n")
                writer.write("\n")
                writer.write("Test Structure:\n")
                writer.write("  • 2 Challenge Types: NORMAL (forward) + REVERSE\n")
                writer.write("  • 5 Difficulty Levels: Easy → Normal → Hard → Expert → Master\n")
                writer.write("  • 5 Runs per difficulty\n")
                writer.write("  • Total Tests: 50\n")
                writer.write("\n")
                writer.write("═".repeat(70) + "\n\n")
            }

            val challengeTypes = listOf(
                ChallengeType.FORWARD,
                ChallengeType.REVERSE
            )

            val difficulties = listOf(
                DifficultyLevel.EASY,
                DifficultyLevel.NORMAL,
                DifficultyLevel.HARD,
                DifficultyLevel.EXPERT,
                DifficultyLevel.MASTER
            )

            var testNumber = 1
            val totalTests = challengeTypes.size * difficulties.size * 5 // 2 × 5 × 5 = 50

            challengeTypes.forEach { challengeType ->
                // Write challenge type section header
                FileWriter(outputFile, true).use { writer ->
                    writer.write("\n")
                    writer.write("▓".repeat(70) + "\n")
                    writer.write("▓▓▓  CHALLENGE TYPE: ${challengeType.name.uppercase()} ${if (challengeType == ChallengeType.FORWARD) "(FORWARD)" else ""}\n")
                    writer.write("▓".repeat(70) + "\n")
                }

                difficulties.forEach { difficulty ->
                    // Apply preset for this difficulty
                    val preset = when (difficulty) {
                        DifficultyLevel.EASY -> ScoringPresets.easyMode()
                        DifficultyLevel.NORMAL -> ScoringPresets.normalMode()
                        DifficultyLevel.HARD -> ScoringPresets.hardMode()
                        DifficultyLevel.EXPERT -> ScoringPresets.expertMode()
                        DifficultyLevel.MASTER -> ScoringPresets.masterMode()
                    }
                    scoringEngine.applyPreset(preset)

                    // Write difficulty section header
                    FileWriter(outputFile, true).use { writer ->
                        writer.write("\n")
                        writer.write("█".repeat(70) + "\n")
                        writer.write("  DIFFICULTY: ${difficulty.displayName.uppercase()}\n")
                        writer.write("█".repeat(70) + "\n\n")
                    }

                    repeat(5) { runNumber ->
                        onProgress(testNumber, totalTests)
                        // 🎯 CHANGED: Pass the audioData array directly
                        runSingleTest(audioData, challengeType, difficulty, runNumber + 1, testNumber, outputFile)
                        testNumber++
                    }
                }
            }

            // Write footer
            FileWriter(outputFile, true).use { writer ->
                writer.write("\n")
                writer.write("═".repeat(70) + "\n")
                writer.write("BIT Complete: $totalTests tests executed\n")
                writer.write("Completed: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())}\n")
                writer.write("═".repeat(70) + "\n")
            }

            Result.success("BIT Complete: Results saved to ${outputFile.name}")
        } catch (e: Exception) {
            Log.e(TAG, "BIT failed", e)
            Result.failure(e)
        }
    }

    private fun runSingleTest(
        // 🎯 CHANGED: This function now takes the audio data array
        audioData: FloatArray,
        challengeType: ChallengeType,
        difficulty: DifficultyLevel,
        runNumber: Int,
        testNumber: Int,
        outputFile: File
    ) {
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())

        try {
            // 🎯 REMOVED: No longer need to load file here

            // Run scoring with identical audio
            val result = scoringEngine.scoreAttempt(
                originalAudio = audioData,
                playerAttempt = audioData,
                challengeType = challengeType,
                sampleRate = 44100
            )

            // Append results to single file
            FileWriter(outputFile, true).use { writer ->
                writer.write("─".repeat(70) + "\n")
                writer.write("TEST #$testNumber - Run $runNumber/5\n")
                writer.write("Time: $timestamp | Type: ${challengeType.name}\n")
                writer.write("─".repeat(70) + "\n")
                writer.write("Raw Score:         ${String.format("%.4f", result.rawScore)}\n")
                writer.write("Final Score:       ${result.score}/100\n")
                writer.write("Pitch Similarity:  ${String.format("%.4f", result.metrics.pitch)}\n")
                writer.write("MFCC Similarity:   ${String.format("%.4f", result.metrics.mfcc)}\n")

                if (result.feedback.isNotEmpty()) {
                    writer.write("\nFeedback:\n")
                    result.feedback.forEach { line ->
                        writer.write("  • $line\n")
                    }
                }

                // Status indicator
                val status = when {
                    result.score >= 95 -> "✅ PASS (Excellent)"
                    result.score >= 80 -> "⚠️  PASS (Good)"
                    else -> "❌ FAIL (Score too low for identical audio)"
                }
                writer.write("\nStatus: $status\n")
                writer.write("\n")
            }

            Log.d(TAG, "Test $testNumber complete - Type: ${challengeType.name}, Raw: ${result.rawScore}, Final: ${result.score}")
        } catch (e: Exception) {
            Log.e(TAG, "Test $testNumber failed", e)

            // Write error to file
            FileWriter(outputFile, true).use { writer ->
                writer.write("─".repeat(70) + "\n")
                writer.write("TEST #$testNumber - Run $runNumber/5\n")
                writer.write("Time: $timestamp | Type: ${challengeType.name}\n")
                writer.write("─".repeat(70) + "\n")
                writer.write("❌ ERROR: ${e.message}\n")
                writer.write("\n")
            }
        }
    }

    /**
     * Load WAV file from res/raw into FloatArray
     * Handles 16-bit PCM WAV format
     */
    // 🎯 CHANGED: This function now takes a Resource ID
    private fun loadWavFile(resourceId: Int): FloatArray {
        // 🎯 CHANGED: Open the resource stream
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