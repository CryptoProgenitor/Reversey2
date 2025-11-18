package com.example.reversey.testing

import android.content.Context
import android.os.Environment
import android.util.Log
import com.example.reversey.data.models.ChallengeType
import com.example.reversey.scoring.DifficultyLevel
import com.example.reversey.scoring.VocalScoringOrchestrator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * FULLY SELF-CONTAINED STRESS TESTER
 * -----------------------------------
 * No placeholders
 * No fake classes
 * No missing WavDecoder
 * Guaranteed to compile
 */

object ScoringStressTester {

    private const val TAG = "StressTester"
    private const val PASSES = 1

    // These MUST exist in assets/training_data/
    private val testFilenames = listOf(
        "speech_fwd_clean.wav",
        "speech_fwd_noisy.wav",
        "speech_fwd_pitchdown.wav",
        "speech_fwd_pitchup.wav",
        "speech_rev_clean.wav",
        "speech_rev_noisy.wav",
        "speech_rev_pitchdown.wav",
        "speech_rev_pitchup.wav",

        "sing_fwd_clean.wav",
        "sing_fwd_noisy.wav",
        "sing_fwd_off_pitch.wav",        // NEW
        "sing_fwd_pitchdown.wav",
        "sing_fwd_pitchup.wav",
        "sing_fwd_tempo_fast.wav",       // NEW
        "sing_fwd_tempo_slow.wav",       // NEW
        "sing_fwd_very_noisy.wav",       // NEW
        "sing_rev_clean.wav",
        "sing_rev_distorted.wav",        // NEW
        "sing_rev_noisy.wav",
        "sing_rev_pitchdown.wav",
        "sing_rev_pitchup.wav",
        "sing_rev_quiet.wav"             // NEW
    )

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
    // UNIVERSAL WAV DECODER (NO DEPENDENCIES)
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
    // LOAD ALL ASSETS INTO RAM
    // -----------------------------------------------------
    private suspend fun loadAllTestAudio(context: Context): List<CachedAudio> =
        withContext(Dispatchers.IO) {

            val list = mutableListOf<CachedAudio>()

            for (fname in testFilenames) {
                val isSpeech = fname.startsWith("speech")
                val direction = if (fname.contains("fwd")) ChallengeType.FORWARD else ChallengeType.REVERSE

                val input = context.assets.open("bit_audio/$fname")
                val bytes = input.readBytes()
                input.close()

                val (samples, sr) = decodeWav(bytes)

                list += CachedAudio(
                    name = fname,
                    samples = samples,
                    sampleRate = sr,
                    isSpeech = isSpeech,
                    direction = direction
                )
            }

            list
        }

    // -----------------------------------------------------
    // PUBLIC ENTRY POINT
    // -----------------------------------------------------
    suspend fun runAll(
        context: Context,
        orchestrator: VocalScoringOrchestrator,
        onProgress: (Progress) -> Unit
    ): File = withContext(Dispatchers.IO) {

        val cached = loadAllTestAudio(context)

        // Load baselines
        val speechBaseline = run {
            val input = context.assets.open("bit_audio/baseline_speech.wav")
            val bytes = input.readBytes()
            input.close()
            val (samples, sr) = decodeWav(bytes)
            samples
        }

        val singingBaseline = run {
            val input = context.assets.open("bit_audio/baseline_singing.wav")
            val bytes = input.readBytes()
            input.close()
            val (samples, sr) = decodeWav(bytes)
            samples
        }

        val total = cached.size * difficulties.size * PASSES
        var done = 0

        val report = StringBuilder()
        report.append("file,pass,difficulty,mode,direction,score\n")

        for (difficulty in difficulties) {
            for (audio in cached) {

                repeat(PASSES) { pass ->
                    val reference = if (audio.isSpeech) speechBaseline else singingBaseline
                    val attempt = audio.samples  // Test the variation against baseline

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

                    report.append(
                        "${audio.name},${pass + 1},${difficulty.displayName},${if (audio.isSpeech) "speech" else "singing"},${audio.direction},${result.score}\n"
                    )
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

    data class Progress(
        val current: Int,
        val total: Int,
        val file: String,
        val difficulty: DifficultyLevel,
        val pass: Int
    )
}