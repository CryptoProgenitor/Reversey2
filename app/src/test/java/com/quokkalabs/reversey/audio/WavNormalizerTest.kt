package com.quokkalabs.reversey.audio

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

class WavNormalizerTest {

    @get:Rule
    val tmp = TemporaryFolder()

    /** Builds a WAV; [extraChunk] is inserted between fmt and data to mimic LIST/INFO metadata. */
    private fun wav(
        formatTag: Int,
        channels: Int,
        rate: Int,
        bits: Int,
        pcm: ByteArray,
        extraChunk: Boolean = false
    ): File {
        val out = ByteArrayOutputStream()
        fun le(size: Int, block: ByteBuffer.() -> Unit) =
            out.write(ByteBuffer.allocate(size).order(ByteOrder.LITTLE_ENDIAN).apply(block).array())

        val extra = if (extraChunk) 8 + 10 else 0
        out.write("RIFF".toByteArray()); le(4) { putInt(4 + 24 + extra + 8 + pcm.size) }
        out.write("WAVE".toByteArray())
        out.write("fmt ".toByteArray()); le(4) { putInt(16) }
        le(16) {
            putShort(formatTag.toShort()); putShort(channels.toShort()); putInt(rate)
            putInt(rate * channels * bits / 8); putShort((channels * bits / 8).toShort()); putShort(bits.toShort())
        }
        if (extraChunk) {
            out.write("LIST".toByteArray()); le(4) { putInt(10) }; out.write(ByteArray(10) { 0x7F })
        }
        out.write("data".toByteArray()); le(4) { putInt(pcm.size) }
        out.write(pcm)
        return tmp.newFile().apply { writeBytes(out.toByteArray()) }
    }

    private fun readCanonical(f: File): Pair<ByteBuffer, ShortArray> {
        val buf = ByteBuffer.wrap(f.readBytes()).order(ByteOrder.LITTLE_ENDIAN)
        val samples = ShortArray((f.length().toInt() - 44) / 2) { buf.getShort(44 + it * 2) }
        return buf to samples
    }

    @Test
    fun canonicalFileIsCopiedUnchanged() {
        val pcm = ByteBuffer.allocate(200).order(ByteOrder.LITTLE_ENDIAN)
            .apply { repeat(100) { putShort((it * 100).toShort()) } }.array()
        val input = wav(1, 1, 44100, 16, pcm)
        val output = tmp.newFile()

        assertTrue(WavNormalizer.normalize(input, output))
        assertArrayEquals(input.readBytes(), output.readBytes())
    }

    @Test
    fun stereo48k24bitWithListChunkIsConverted() {
        // 0.1s of a 440Hz tone, 24-bit stereo at 48kHz, with metadata before the data chunk
        val frames = 4800
        val pcm = ByteArray(frames * 6)
        for (i in 0 until frames) {
            val v = (sin(2 * PI * 440 * i / 48000) * 0.5 * 8388607).toInt()
            for (c in 0 until 2) {
                val p = i * 6 + c * 3
                pcm[p] = v.toByte(); pcm[p + 1] = (v shr 8).toByte(); pcm[p + 2] = (v shr 16).toByte()
            }
        }
        val output = tmp.newFile()

        assertTrue(WavNormalizer.normalize(wav(1, 2, 48000, 24, pcm, extraChunk = true), output))

        val (buf, samples) = readCanonical(output)
        assertEquals("RIFF", String(output.readBytes(), 0, 4))
        assertEquals(1, buf.getShort(22).toInt())          // mono
        assertEquals(44100, buf.getInt(24))                // sample rate
        assertEquals(16, buf.getShort(34).toInt())         // bits
        assertEquals(samples.size * 2, buf.getInt(40))     // data size
        assertEquals(4410, samples.size)                   // still 0.1s long
        // LIST bytes (0x7F7F) must not leak into the audio, and amplitude should be ~0.5
        val peak = samples.maxOf { abs(it.toInt()) }
        assertTrue("peak=$peak", peak in 15000..17500)
        assertTrue("first sample should be ~0, was ${samples[0]}", abs(samples[0].toInt()) < 200)
    }

    @Test
    fun rejectsNonWav() {
        val input = tmp.newFile().apply { writeBytes("ID3 not a wav file at all".toByteArray()) }
        assertFalse(WavNormalizer.normalize(input, tmp.newFile()))
    }
}
