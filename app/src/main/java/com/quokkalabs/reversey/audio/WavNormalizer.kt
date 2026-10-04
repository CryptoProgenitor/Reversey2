package com.quokkalabs.reversey.audio

import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Converts an arbitrary PCM/float WAV into the app's canonical format:
 * 44-byte header, mono, 16-bit PCM, [AudioConstants.SAMPLE_RATE].
 *
 * The reverse, transcription and scoring code all assume that exact layout,
 * so imported files must go through here first. Without it, a WAV with extra
 * header chunks (LIST, fact, ...) gets header bytes reversed into the audio,
 * and a 48kHz or stereo file plays/transcribes at the wrong speed.
 */
object WavNormalizer {

    private const val TAG = "WavNormalizer"

    private const val FORMAT_PCM = 1
    private const val FORMAT_FLOAT = 3
    private const val FORMAT_EXTENSIBLE = 0xFFFE

    private data class Format(
        val formatTag: Int,
        val channels: Int,
        val sampleRate: Int,
        val bitsPerSample: Int
    )

    /**
     * Reads [input] and writes the canonical version to [output].
     * @return true on success; false if the file isn't a supported WAV.
     */
    fun normalize(input: File, output: File): Boolean {
        return try {
            val bytes = input.readBytes()
            val buf = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)

            if (bytes.size < 12 ||
                String(bytes, 0, 4, Charsets.US_ASCII) != "RIFF" ||
                String(bytes, 8, 4, Charsets.US_ASCII) != "WAVE"
            ) {
                Log.e(TAG, "Not a RIFF/WAVE file")
                return false
            }

            // Walk chunks to find "fmt " and "data" wherever they are
            var format: Format? = null
            var dataOffset = -1
            var dataSize = 0
            var pos = 12
            while (pos + 8 <= bytes.size) {
                val id = String(bytes, pos, 4, Charsets.US_ASCII)
                val size = buf.getInt(pos + 4)
                val body = pos + 8
                when (id) {
                    "fmt " -> {
                        var tag = buf.getShort(body).toInt() and 0xFFFF
                        val channels = buf.getShort(body + 2).toInt()
                        val rate = buf.getInt(body + 4)
                        val bits = buf.getShort(body + 14).toInt()
                        if (tag == FORMAT_EXTENSIBLE && size >= 26) {
                            // Real format is the first 2 bytes of the SubFormat GUID
                            tag = buf.getShort(body + 24).toInt() and 0xFFFF
                        }
                        format = Format(tag, channels, rate, bits)
                    }
                    "data" -> {
                        dataOffset = body
                        // Streamed writers may leave size as 0 or 0xFFFFFFFF: use what's actually there
                        val remaining = bytes.size - body
                        dataSize = if (size <= 0 || size > remaining) remaining else size
                    }
                }
                if (format != null && dataOffset >= 0) break
                if (size < 0) break
                pos = body + size + (size and 1) // chunks are word-aligned
            }

            val fmt = format
            if (fmt == null || dataOffset < 0) {
                Log.e(TAG, "Missing fmt or data chunk")
                return false
            }
            val supported = fmt.channels >= 1 && fmt.sampleRate > 0 && when (fmt.formatTag) {
                FORMAT_PCM -> fmt.bitsPerSample in setOf(8, 16, 24, 32)
                FORMAT_FLOAT -> fmt.bitsPerSample == 32
                else -> false
            }
            if (!supported) {
                Log.e(TAG, "Unsupported WAV format: $fmt")
                return false
            }

            // Already canonical: copy unchanged so app-made files round-trip byte for byte
            if (fmt.formatTag == FORMAT_PCM && fmt.channels == 1 && fmt.bitsPerSample == 16 &&
                fmt.sampleRate == AudioConstants.SAMPLE_RATE && dataOffset == AudioConstants.WAV_HEADER_SIZE
            ) {
                input.copyTo(output, overwrite = true)
                return true
            }

            val mono = decodeToMono(buf, dataOffset, dataSize, fmt)
            val resampled = resample(mono, fmt.sampleRate, AudioConstants.SAMPLE_RATE)
            writeCanonical(output, resampled)
            Log.d(TAG, "Normalized $fmt (${mono.size} frames) → mono 16-bit ${AudioConstants.SAMPLE_RATE}Hz (${resampled.size} frames)")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Normalize failed", e)
            false
        }
    }

    /** Decodes interleaved samples to mono floats in [-1, 1], averaging channels. */
    private fun decodeToMono(buf: ByteBuffer, offset: Int, size: Int, fmt: Format): FloatArray {
        val bytesPerSample = fmt.bitsPerSample / 8
        val frameSize = bytesPerSample * fmt.channels
        val frames = size / frameSize
        val out = FloatArray(frames)
        for (f in 0 until frames) {
            var sum = 0f
            val frameStart = offset + f * frameSize
            for (c in 0 until fmt.channels) {
                val p = frameStart + c * bytesPerSample
                sum += when {
                    fmt.formatTag == FORMAT_FLOAT -> buf.getFloat(p)
                    bytesPerSample == 1 -> ((buf.get(p).toInt() and 0xFF) - 128) / 128f // 8-bit is unsigned
                    bytesPerSample == 2 -> buf.getShort(p) / 32768f
                    bytesPerSample == 3 -> {
                        val v = (buf.get(p).toInt() and 0xFF) or
                                ((buf.get(p + 1).toInt() and 0xFF) shl 8) or
                                (buf.get(p + 2).toInt() shl 16) // sign-extends from top byte
                        v / 8388608f
                    }
                    else -> buf.getInt(p) / 2147483648f
                }
            }
            out[f] = sum / fmt.channels
        }
        return out
    }

    /** Linear-interpolation resample. Good enough for speech; matches the Vosk resampler's approach. */
    private fun resample(input: FloatArray, fromRate: Int, toRate: Int): FloatArray {
        if (fromRate == toRate || input.isEmpty()) return input
        val ratio = fromRate.toDouble() / toRate
        val outLen = (input.size / ratio).toInt()
        val out = FloatArray(outLen)
        for (i in 0 until outLen) {
            val src = i * ratio
            val idx = src.toInt()
            val frac = (src - idx).toFloat()
            val a = input[idx]
            val b = if (idx + 1 < input.size) input[idx + 1] else a
            out[i] = a + (b - a) * frac
        }
        return out
    }

    private fun writeCanonical(output: File, samples: FloatArray) {
        val dataSize = samples.size * 2
        val bb = ByteBuffer.allocate(AudioConstants.WAV_HEADER_SIZE + dataSize).order(ByteOrder.LITTLE_ENDIAN)
        val rate = AudioConstants.SAMPLE_RATE
        bb.put("RIFF".toByteArray(Charsets.US_ASCII))
        bb.putInt(36 + dataSize)
        bb.put("WAVE".toByteArray(Charsets.US_ASCII))
        bb.put("fmt ".toByteArray(Charsets.US_ASCII))
        bb.putInt(16)
        bb.putShort(FORMAT_PCM.toShort())
        bb.putShort(1) // mono
        bb.putInt(rate)
        bb.putInt(rate * 2) // byte rate
        bb.putShort(2) // block align
        bb.putShort(16) // bits per sample
        bb.put("data".toByteArray(Charsets.US_ASCII))
        bb.putInt(dataSize)
        for (s in samples) {
            bb.putShort((s.coerceIn(-1f, 1f) * 32767f).toInt().toShort())
        }
        FileOutputStream(output).use { it.write(bb.array()) }
    }
}
