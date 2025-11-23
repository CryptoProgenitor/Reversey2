package com.quokkalabs.reversey.testing

import android.content.Context
import android.os.Environment
import android.util.Log
import com.quokkalabs.reversey.data.models.ChallengeType
import com.quokkalabs.reversey.scoring.DifficultyLevel
import com.quokkalabs.reversey.scoring.VocalScoringOrchestrator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.quokkalabs.reversey.scoring.Presets
import com.quokkalabs.reversey.scoring.SpeechScoringModels

/**
 * FULLY SELF-CONTAINED STRESS TESTER
 * -----------------------------------
 * Feature: Automatic Test File Discovery based on asset folder structure.
 * * New Structure (MUST be set up):
 * assets/bit_audio/
 * ├── baselines/
 * │   ├── baseline_speech.wav
 * │   └── baseline_singing.wav
 * └── test_files/
 * ├── speech/ <-- CONTAINS speech_fwd_clean.wav etc.
 * └── singing/ <-- CONTAINS sing_fwd_clean.wav etc.
 */

object ScoringStressTester {

    private const val TAG = "StressTester"
    private const val PASSES = 1

    // --- NEW CONSTANTS FOR FILE PATHS ---
    private const val BASE_ASSET_PATH = "bit_audio"
    private const val TEST_FILES_PATH = "$BASE_ASSET_PATH/test_files"
    private const val BASELINE_PATH = "$BASE_ASSET_PATH/baselines"

    // The hardcoded list is now REMOVED
    // private val testFilenames = listOf(...)

    private val difficulties = listOf(
        DifficultyLevel.EASY,
        DifficultyLevel.NORMAL,
        DifficultyLevel.HARD
    )

    data class CachedAudio(
        val name: String,
        val samples: FloatArray,
        val sampleRate: Int,
        val isSpeech: Boolean,
        val direction: ChallengeType
    )

    // -----------------------------------------------------
    // UTILITY: FLATTEN PRESETS (REQUIRED FOR CSV LOGGING)
    // -----------------------------------------------------
    private fun Presets.getFlattenedParameters(): Map<String, Any> {
        val params = mutableMapOf<String, Any>()

        // SCORING PARAMETERS
        params["scoring_pitchWeight"] = this.scoring.pitchWeight
        params["scoring_mfccWeight"] = this.scoring.mfccWeight
        params["scoring_pitchTolerance"] = this.scoring.pitchTolerance
        params["scoring_minScoreThreshold"] = this.scoring.minScoreThreshold
        params["scoring_perfectScoreThreshold"] = this.scoring.perfectScoreThreshold
        params["scoring_reverseMinScoreThreshold"] = this.scoring.reverseMinScoreThreshold
        params["scoring_reversePerfectScoreThreshold"] = this.scoring.reversePerfectScoreThreshold
        params["scoring_scoreCurve"] = this.scoring.scoreCurve
        params["scoring_consistencyBonus"] = this.scoring.consistencyBonus
        params["scoring_confidenceBonus"] = this.scoring.confidenceBonus

        // CONTENT DETECTION PARAMETERS
        params["content_bestThreshold"] = this.content.contentDetectionBestThreshold
        params["content_avgThreshold"] = this.content.contentDetectionAvgThreshold
        params["content_rightFlatPenalty"] = this.content.rightContentFlatPenalty
        params["content_rightMelodyPenalty"] = this.content.rightContentDifferentMelodyPenalty
        params["content_wrongStandardPenalty"] = this.content.wrongContentStandardPenalty

        // MELODIC ANALYSIS PARAMETERS
        params["melodic_monotoneDetectionThreshold"] = this.melodic.monotoneDetectionThreshold
        params["melodic_flatSpeechThreshold"] = this.melodic.flatSpeechThreshold
        params["melodic_monotonePenalty"] = this.melodic.monotonePenalty
        params["melodic_rangeWeight"] = this.melodic.melodicRangeWeight
        params["melodic_transitionWeight"] = this.melodic.melodicTransitionWeight
        params["melodic_varianceWeight"] = this.melodic.melodicVarianceWeight
        params["melodic_silenceToSilenceScore"] = this.melodic.silenceToSilenceScore

        // MUSICAL SIMILARITY PARAMETERS
        params["musical_sameIntervalScore"] = this.musical.sameIntervalScore
        params["musical_closeIntervalScore"] = this.musical.closeIntervalScore
        params["musical_emptyPhrasesPenalty"] = this.musical.emptyPhrasesPenalty
        params["musical_emptyRhythmPenalty"] = this.musical.emptyRhythmPenalty

        // SCALING PARAMETERS
        params["scaling_incredibleThreshold"] = this.scaling.incredibleFeedbackThreshold
        params["scaling_greatJobThreshold"] = this.scaling.greatJobFeedbackThreshold
        params["scaling_goodEffortThreshold"] = this.scaling.goodEffortFeedbackThreshold

        // GARBAGE DETECTION PARAMETERS
        params["garbage_mfccVarianceThreshold"] = this.garbage.mfccVarianceThreshold
        params["garbage_pitchMonotoneThreshold"] = this.garbage.pitchMonotoneThreshold
        params["garbage_pitchOscillationRate"] = this.garbage.pitchOscillationRate
        params["garbage_spectralEntropyThreshold"] = this.garbage.spectralEntropyThreshold
        params["garbage_zcrMinThreshold"] = this.garbage.zcrMinThreshold
        params["garbage_zcrMaxThreshold"] = this.garbage.zcrMaxThreshold
        params["garbage_silenceRatioMin"] = this.garbage.silenceRatioMin
        params["garbage_scoreMax"] = this.garbage.garbageScoreMax

        return params
    }

    // -----------------------------------------------------
    // UNIVERSAL WAV DECODER (UNCHANGED)
    // -----------------------------------------------------
    private fun decodeWav(bytes: ByteArray): Pair<FloatArray, Int> {
        val stream = ByteArrayInputStream(bytes)

        // Skip 44-byte WAV header
        val header = ByteArray(44)
        stream.read(header)

        val sampleRate =
            (header[27].toInt() and 0xFF shl 24) or
                    (header[26].toInt() and 0xFF shl 16) or
                    (header[25].toInt() and 0xFF shl 8) or
                    (header[24].toInt() and 0xFF)

        val pcmData = stream.readBytes()
        val sampleCount = pcmData.size / 2
        val floats = FloatArray(sampleCount)

        var i = 0
        var j = 0
        while (i < pcmData.size - 1) {
            val lo = pcmData[i].toInt() and 0xFF
            val hi = pcmData[i + 1].toInt()
            val sample = (hi shl 8) or lo
            floats[j] = sample / 32768f
            i += 2
            j++
        }

        return floats to sampleRate
    }

    // -----------------------------------------------------
    // LOAD ALL ASSETS INTO RAM (AUTO-DISCOVERY IMPLEMENTATION)
    // -----------------------------------------------------
    private suspend fun loadAllTestAudio(context: Context): List<CachedAudio> =
        withContext(Dispatchers.IO) {

            val list = mutableListOf<CachedAudio>()

            // List the sub-folders inside test_files: speech and singing
            val modeDirs = context.assets.list(TEST_FILES_PATH) ?: emptyArray()

            for (modeDir in modeDirs) {
                // Determine if it's speech or singing based on the folder name itself
                val isSpeech = modeDir.equals("speech", ignoreCase = true)
                val modePath = "$TEST_FILES_PATH/$modeDir"

                // List all WAV files within the specific mode directory
                val modeFiles = context.assets.list(modePath)
                    ?.filter { it.endsWith(".wav", ignoreCase = true) }
                    ?: emptyList()

                for (fname in modeFiles) {
                    val fullPath = "$modePath/$fname"

                    // Determine direction (fwd or rev) from the filename based on convention
                    val direction = when {
                        fname.contains("_fwd_", ignoreCase = true) -> ChallengeType.FORWARD
                        fname.contains("_rev_", ignoreCase = true) -> ChallengeType.REVERSE
                        else -> {
                            Log.w(TAG, "Skipping file $fname in $modePath: Direction (fwd/rev) not clearly identified.")
                            continue
                        }
                    }

                    try {
                        // Load the audio file
                        context.assets.open(fullPath).use { input ->
                            val bytes = input.readBytes()
                            val (samples, sr) = decodeWav(bytes)

                            list += CachedAudio(
                                name = fname,
                                samples = samples,
                                sampleRate = sr,
                                isSpeech = isSpeech,
                                direction = direction
                            )
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to load audio file: $fullPath", e)
                    }
                }
            }
            list
        }

    // -----------------------------------------------------
    // PUBLIC ENTRY POINT (UPDATED FOR NEW BASELINE LOADING AND LOGGING)
    // -----------------------------------------------------
    suspend fun runAll(
        context: Context,
        orchestrator: VocalScoringOrchestrator,
        onProgress: (Progress) -> Unit
    ): File = withContext(Dispatchers.IO) {

        val cached = loadAllTestAudio(context)

        if (cached.isEmpty()) {
            Log.w(TAG, "No test audio files found in $TEST_FILES_PATH. Aborting test.")
            throw IllegalStateException("No test audio files found. Check asset folder structure.")
        }

        // Load baselines from the BASKETLINES folder
        val speechBaseline = loadBaseline(context, "baseline_speech.wav")
        val singingBaseline = loadBaseline(context, "baseline_singing.wav")

        if (speechBaseline == null || singingBaseline == null) {
            throw IllegalStateException("Missing one or both baseline files in $BASELINE_PATH.")
        }

        val total = cached.size * difficulties.size * PASSES
        var done = 0

        val report = StringBuilder()

        // Use an arbitrary preset (EASY Speech) to generate the full, ordered list of parameter keys for the header
        val headerParamsKeys = SpeechScoringModels.easyModeSpeech().getFlattenedParameters().keys.toList()

        // 1. Build the CSV Header
        report.append("file,pass,difficulty,mode,direction,score")
        for (paramName in headerParamsKeys) {
            report.append(",$paramName")
        }
        report.append("\n")

        for (difficulty in difficulties) {
            for (audio in cached) {

                repeat(PASSES) { pass ->
                    val reference = if (audio.isSpeech) speechBaseline else singingBaseline
                    val attempt = audio.samples  // Test the variation against baseline

                    // NOTE: The orchestrator will use the difficulty set above, and the scoring
                    // engine will retrieve the correct preset and attach it to the result.
                    val result = orchestrator.scoreAttempt(
                        referenceAudio = reference,
                        attemptAudio = attempt,
                        challengeType = audio.direction,
                        difficulty = difficulty,
                        sampleRate = audio.sampleRate
                    )

                    done++

                    onProgress(
                        Progress(
                            current = done,
                            total = total,
                            file = audio.name,
                            difficulty = difficulty,
                            pass = pass + 1
                        )
                    )

                    // Extract and flatten the parameters for this specific score
                    val flattenedParams = result.debugPresets?.getFlattenedParameters() ?: emptyMap()

                    // 2. Start the Data Row
                    report.append(
                        "${audio.name},${pass + 1},${difficulty.displayName},${if (audio.isSpeech) "speech" else "singing"},${audio.direction},${result.score}"
                    )

                    // 3. Append all parameter values in the correct order
                    for (paramName in headerParamsKeys) {
                        // Use toString() to ensure floats are written correctly. Use the map key to ensure column order matches header.
                        report.append(",${flattenedParams[paramName]?.toString() ?: ""}")
                    }
                    report.append("\n")
                }
            }
        }

        val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        // Generate DTG (Date Time Group) for unique filename
        val dtgFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
        val dtg = dtgFormat.format(Date())

        val outFile = File(downloads, "reversey_scoring_stress_report_${dtg}.csv")

        outFile.writeText(report.toString())
        Log.d(TAG, "WROTE REPORT → ${outFile.absolutePath}")

        outFile
    }

    /**
     * Helper function to load a single baseline audio file.
     */
    private fun loadBaseline(context: Context, filename: String): FloatArray? {
        val fullPath = "$BASELINE_PATH/$filename"
        return try {
            context.assets.open(fullPath).use { input ->
                val bytes = input.readBytes()
                val (samples, _) = decodeWav(bytes)
                samples
            }
        } catch (e: Exception) {
            Log.e(TAG, "CRITICAL: Failed to load baseline file: $fullPath", e)
            null
        }
    }

    data class Progress(
        val current: Int,
        val total: Int,
        val file: String,
        val difficulty: DifficultyLevel,
        val pass: Int
    )
}