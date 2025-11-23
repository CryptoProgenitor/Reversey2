package com.quokkalabs.reversey.utils

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import android.util.Log

// Utility functions
fun getRecordingsDir(context: Context): File {
    return File(context.filesDir, "recordings").apply { mkdirs() }
}

fun formatFileName(fileName: String): String {
    Log.d("FORMAT_DEBUG", "Input: $fileName")

    val nameWithoutExt = fileName.removeSuffix(".wav")

    // 🛡️ SAFETY: If already formatted, return as-is
    if (nameWithoutExt.contains("•")) {
        Log.d("FORMAT_DEBUG", "Already formatted, returning as-is")
        return nameWithoutExt
    }

    // Parse ISO format: 2025-11-05_14-30-00
    val regex = """(\d{4})-(\d{2})-(\d{2})_(\d{2})-(\d{2})-(\d{2})""".toRegex()
    val match = regex.matchEntire(nameWithoutExt)

    if (match == null) {
        // Old format or unknown, return as-is
        Log.d("FORMAT_DEBUG", "Not ISO format, returning: $nameWithoutExt")
        return nameWithoutExt
    }

    val (year, month, day, hour24, minute, second) = match.destructured

    // Convert to 12-hour format
    val hourInt = hour24.toInt()
    val hour12 = if (hourInt == 0) 12 else if (hourInt > 12) hourInt - 12 else hourInt
    val ampm = if (hourInt < 12) "am" else "pm"

    // Month names
    val months = listOf("", "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    val monthName = months[month.toInt()]

    // Format: Rec • 2:30pm • 5 Nov 25
    val formatted = "Rec • ${hour12}:${minute}${ampm} • ${day.toInt()} $monthName ${year.takeLast(2)}"
    Log.d("FORMAT_DEBUG", "Output: $formatted")
    return formatted
}

@Throws(IOException::class)
fun writeWavHeader(out: FileOutputStream, audioData: ByteArray, channels: Int, sampleRate: Int, bitDepth: Int) {
    val audioDataLength = audioData.size
    val totalDataLength = audioDataLength + 36
    val byteRate = sampleRate * channels * bitDepth / 8
    val blockAlign = channels * bitDepth / 8
    val header = ByteArray(44)

    header[0] = 'R'.code.toByte(); header[1] = 'I'.code.toByte(); header[2] = 'F'.code.toByte(); header[3] = 'F'.code.toByte()
    header[4] = (totalDataLength and 0xff).toByte(); header[5] = (totalDataLength shr 8 and 0xff).toByte(); header[6] = (totalDataLength shr 16 and 0xff).toByte(); header[7] = (totalDataLength shr 24 and 0xff).toByte()
    header[8] = 'W'.code.toByte(); header[9] = 'A'.code.toByte(); header[10] = 'V'.code.toByte(); header[11] = 'E'.code.toByte()
    header[12] = 'f'.code.toByte(); header[13] = 'm'.code.toByte(); header[14] = 't'.code.toByte(); header[15] = ' '.code.toByte()
    header[16] = 16; header[17] = 0; header[18] = 0; header[19] = 0
    header[20] = 1; header[21] = 0
    header[22] = channels.toByte(); header[23] = 0
    header[24] = (sampleRate and 0xff).toByte(); header[25] = (sampleRate shr 8 and 0xff).toByte(); header[26] = (sampleRate shr 16 and 0xff).toByte(); header[27] = (sampleRate shr 24 and 0xff).toByte()
    header[28] = (byteRate and 0xff).toByte(); header[29] = (byteRate shr 8 and 0xff).toByte(); header[30] = (byteRate shr 16 and 0xff).toByte(); header[31] = (byteRate shr 24 and 0xff).toByte()
    header[32] = blockAlign.toByte(); header[33] = 0
    header[34] = bitDepth.toByte(); header[35] = 0
    header[36] = 'd'.code.toByte(); header[37] = 'a'.code.toByte(); header[38] = 't'.code.toByte(); header[39] = 'a'.code.toByte()
    header[40] = (audioDataLength and 0xff).toByte(); header[41] = (audioDataLength shr 8 and 0xff).toByte(); header[42] = (audioDataLength shr 16 and 0xff).toByte(); header[43] = (audioDataLength shr 24 and 0xff).toByte()

    out.write(header, 0, 44)
    out.write(audioData)
}