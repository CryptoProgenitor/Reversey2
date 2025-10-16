package com.example.reversey

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jtransforms.fft.DoubleFFT_1D
import java.io.File
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sqrt

class AudioComparer {

    suspend fun compareAudioFiles(file1: File, file2: File): Int = withContext(Dispatchers.Default) {
        try {
            val audio1Data = readWavData(file1)
            val audio2Data = readWavData(file2)

            android.util.Log.d("AudioComparer", "Audio1 samples: ${audio1Data.size}")
            android.util.Log.d("AudioComparer", "Audio2 samples: ${audio2Data.size}")

            if (audio1Data.isEmpty() || audio2Data.isEmpty()) {
                return@withContext 0
            }

            // Normalize to same length
            val minLength = min(audio1Data.size, audio2Data.size)
            val normalized1 = audio1Data.copyOfRange(0, minLength)
            val normalized2 = audio2Data.copyOfRange(0, minLength)

            // Normalize volumes
            val norm1 = normalizeVolume(normalized1)
            val norm2 = normalizeVolume(normalized2)

            // Convert to frequency domain and compare
            val similarity = compareFrequencySpectrums(norm1, norm2)

            android.util.Log.d("AudioComparer", "FFT similarity: $similarity")

            val score = (similarity * 100).toInt().coerceIn(0, 100)

            android.util.Log.d("AudioComparer", "Comparison complete: $score%")
            return@withContext score

        } catch (e: Exception) {
            android.util.Log.e("AudioComparer", "Error comparing audio files", e)
            return@withContext 0
        }
    }

    private fun readWavData(file: File): ShortArray {
        try {
            val bytes = file.readBytes()
            if (bytes.size < 44) return shortArrayOf()

            val pcmData = bytes.drop(44).toByteArray()
            val samples = ShortArray(pcmData.size / 2)
            for (i in samples.indices) {
                val low = pcmData[i * 2].toInt() and 0xFF
                val high = pcmData[i * 2 + 1].toInt() and 0xFF
                samples[i] = ((high shl 8) or low).toShort()
            }

            return samples
        } catch (e: Exception) {
            android.util.Log.e("AudioComparer", "Error reading WAV data", e)
            return shortArrayOf()
        }
    }

    private fun normalizeVolume(samples: ShortArray): FloatArray {
        if (samples.isEmpty()) return floatArrayOf()

        val maxValue = samples.maxOfOrNull { abs(it.toFloat()) } ?: 1f
        return samples.map { it.toFloat() / maxValue }.toFloatArray()
    }

    private fun compareFrequencySpectrums(signal1: FloatArray, signal2: FloatArray): Float {
        if (signal1.isEmpty() || signal2.isEmpty()) {
            return 0f
        }

        val chunkSize = 2048
        val hopSize = chunkSize / 2

        val spectrum1 = getAverageSpectrum(signal1, chunkSize, hopSize)
        val spectrum2 = getAverageSpectrum(signal2, chunkSize, hopSize)

        val rawSimilarity = cosineSimilarity(spectrum1, spectrum2)

        // Apply squaring first
        val squared = (rawSimilarity * rawSimilarity)

        // Stricter rescale: 0.6 -> 0.0, 0.9 -> 1.0
        val rescaled = if (squared < 0.6f) {
            0f
        } else {
            (squared - 0.6f) / (0.9f - 0.6f)  // (x - 0.6) / 0.3
        }

        val finalScore = rescaled.coerceIn(0f, 1f)

        android.util.Log.d("AudioComparer", "Raw: $rawSimilarity, Squared: $squared, Final: $finalScore")

        return finalScore
    }

    private fun getAverageSpectrum(signal: FloatArray, chunkSize: Int, hopSize: Int): FloatArray {
        val spectrumSize = chunkSize / 2
        val avgSpectrum = FloatArray(spectrumSize)
        var chunkCount = 0

        var pos = 0
        while (pos + chunkSize <= signal.size) {
            val chunk = signal.copyOfRange(pos, pos + chunkSize)
            val spectrum = computeFFT(chunk)

            for (i in spectrum.indices) {
                avgSpectrum[i] += spectrum[i]
            }

            chunkCount++
            pos += hopSize
        }

        // Average the accumulated spectrums
        if (chunkCount > 0) {
            for (i in avgSpectrum.indices) {
                avgSpectrum[i] /= chunkCount
            }
        }

        return avgSpectrum
    }

    private fun computeFFT(signal: FloatArray): FloatArray {
        val n = signal.size

        // Apply Hamming window
        val windowed = FloatArray(n)
        for (i in 0 until n) {
            val window = 0.54 - 0.46 * kotlin.math.cos(2.0 * kotlin.math.PI * i / (n - 1))
            windowed[i] = (signal[i] * window).toFloat()
        }

        // Prepare data for FFT
        val fftData = DoubleArray(n * 2)
        for (i in 0 until n) {
            fftData[i * 2] = windowed[i].toDouble()
            fftData[i * 2 + 1] = 0.0
        }

        // Perform FFT
        val fft = DoubleFFT_1D(n.toLong())
        fft.complexForward(fftData)

        // Calculate magnitude spectrum
        val magnitude = FloatArray(n / 2)
        for (i in 0 until n / 2) {
            val real = fftData[i * 2]
            val imag = fftData[i * 2 + 1]
            magnitude[i] = sqrt(real * real + imag * imag).toFloat()
        }

        return magnitude
    }

    private fun cosineSimilarity(vec1: FloatArray, vec2: FloatArray): Float {
        if (vec1.size != vec2.size || vec1.isEmpty()) {
            return 0f
        }

        var dotProduct = 0.0
        var norm1 = 0.0
        var norm2 = 0.0

        for (i in vec1.indices) {
            dotProduct += vec1[i] * vec2[i]
            norm1 += vec1[i] * vec1[i]
            norm2 += vec2[i] * vec2[i]
        }

        val denominator = sqrt(norm1 * norm2)

        if (denominator < 0.0001) {
            return 0f
        }

        return (dotProduct / denominator).toFloat().coerceIn(0f, 1f)
    }
}