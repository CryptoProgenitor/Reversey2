package com.example.reversey.audio.processing

import org.jtransforms.fft.FloatFFT_1D
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.sqrt

class AudioProcessor {

    fun extractPitchYIN(audioFrame: FloatArray, sampleRate: Int, threshold: Float = 0.15f): Float {
        val bufferSize = audioFrame.size
        if (bufferSize == 0) return 0f
        val yinBuffer = FloatArray(bufferSize / 2)

        // Difference function
        for (tau in 1 until bufferSize / 2) {
            var squaredDifference = 0f
            for (j in 0 until bufferSize / 2) {
                if (j + tau < bufferSize) { // Boundary check
                    val diff = audioFrame[j] - audioFrame[j + tau]
                    squaredDifference += diff * diff
                }
            }
            yinBuffer[tau] = squaredDifference
        }

        // Cumulative mean normalized difference
        var runningSum = 0f
        yinBuffer[0] = 1f
        for (tau in 1 until bufferSize / 2) {
            runningSum += yinBuffer[tau]
            yinBuffer[tau] = if (runningSum != 0f) yinBuffer[tau] * tau / runningSum else 1f
        }

        // Absolute threshold to find the first dip
        for (tau in 2 until bufferSize / 2) {
            if (yinBuffer[tau] < threshold) {
                // --- CRITICAL BUG FIX: Replaced the faulty 'while' loop ---
                // This new implementation correctly and safely finds the local minimum.
                var minTau = tau
                while (minTau + 1 < bufferSize / 2 && yinBuffer[minTau + 1] < yinBuffer[minTau]) {
                    minTau++
                }
                // --- END OF BUG FIX ---

                // Parabolic interpolation for better accuracy
                return if (minTau > 0 && minTau < bufferSize / 2 - 1) {
                    val y1 = yinBuffer[minTau - 1]
                    val y2 = yinBuffer[minTau]
                    val y3 = yinBuffer[minTau + 1]
                    val denominator = 2 * (2 * y2 - y3 - y1)
                    if (abs(denominator) > 1e-6) {
                        val betterTau = minTau + (y3 - y1) / denominator
                        if(betterTau > 0) sampleRate / betterTau else 0f
                    } else {
                        sampleRate.toFloat() / minTau
                    }
                } else {
                    sampleRate.toFloat() / minTau
                }
            }
        }
        return 0f // No pitch detected
    }

    fun extractMFCC(audioFrame: FloatArray, sampleRate: Int, numCoefficients: Int = 13): FloatArray {
        val n = audioFrame.size
        if (n == 0) return FloatArray(numCoefficients)

        val emphasized = FloatArray(n)
        emphasized[0] = audioFrame[0]
        for (i in 1 until n) {
            emphasized[i] = audioFrame[i] - 0.97f * audioFrame[i - 1]
        }

        val windowed = applyHammingWindow(emphasized)
        val spectrum = getSpectrum(windowed)
        val powerSpectrum = spectrum.map { it * it }.toFloatArray()

        val numFilters = 26
        val melEnergies = applyMelFilterbank(powerSpectrum, sampleRate, numFilters)
        val logMelEnergies = melEnergies.map { ln(it.coerceAtLeast(1e-5f)) }.toFloatArray()

        val dct = FloatFFT_1D(numFilters.toLong())
        val mfccs = logMelEnergies.copyOf()
        dct.realForward(mfccs)

        return mfccs.copyOfRange(1, numCoefficients + 1)
    }

    private fun applyMelFilterbank(powerSpectrum: FloatArray, sampleRate: Int, numFilters: Int): FloatArray {
        val lowFreqMel = 0.0
        val highFreqMel = 2595 * ln(1 + (sampleRate / 2.0) / 700.0)
        val melPoints = FloatArray(numFilters + 2)
        val melSpacing = (highFreqMel - lowFreqMel) / (numFilters + 1)
        for (i in 0 until numFilters + 2) {
            melPoints[i] = (lowFreqMel + i * melSpacing).toFloat()
        }

        val fftSize = (powerSpectrum.size) * 2
        val freqPoints = melPoints.map { 700 * (exp(it / 1125.0) - 1) }
        val binPoints = freqPoints.map { floor((fftSize + 1) * it / sampleRate).toInt() }

        val filterEnergies = FloatArray(numFilters)
        for (i in 1..numFilters) {
            var energy = 0f
            val startBin = binPoints[i-1]
            val centerBin = binPoints[i]
            val endBin = binPoints[i+1]

            for (j in startBin..endBin) {
                if (j >= 0 && j < powerSpectrum.size) {
                    val weight = when {
                        j < centerBin -> if (centerBin == startBin) 1f else (j - startBin).toFloat() / (centerBin - startBin)
                        else -> if (endBin == centerBin) 1f else (endBin - j).toFloat() / (endBin - centerBin)
                    }
                    energy += powerSpectrum[j] * weight
                }
            }
            filterEnergies[i-1] = energy
        }
        return filterEnergies
    }

    fun applyHammingWindow(frame: FloatArray): FloatArray {
        val windowed = FloatArray(frame.size)
        val n = frame.size
        for (i in frame.indices) {
            windowed[i] = frame[i] * (0.54f - 0.46f * cos(2 * PI * i / (n - 1))).toFloat()
        }
        return windowed
    }

    fun getSpectrum(frame: FloatArray): FloatArray {
        val n = frame.size
        val fft = FloatFFT_1D(n.toLong())
        val data = frame.copyOf(n * 2)
        fft.realForwardFull(data)
        val spectrum = FloatArray(n / 2)
        for (i in spectrum.indices) {
            val real = data[i * 2]
            val imag = if (i * 2 + 1 < data.size) data[i * 2 + 1] else 0f
            spectrum[i] = sqrt(real * real + imag * imag)
        }
        return spectrum
    }

    fun calculateRMS(audio: FloatArray): Float {
        if (audio.isEmpty()) return 0f
        val sum = audio.fold(0f) { acc, sample -> acc + sample * sample }
        return sqrt(sum / audio.size)
    }
}
