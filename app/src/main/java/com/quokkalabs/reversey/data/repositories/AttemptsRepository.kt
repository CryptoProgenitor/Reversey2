package com.quokkalabs.reversey.data.repositories

import android.content.Context
import android.util.Log
import com.quokkalabs.reversey.data.models.PlayerAttempt
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class AttemptsRepository(private val context: Context) {
    private val gson = Gson()
    private val attemptsFile = File(context.filesDir, "attempts.json")

    // Data structure to save: Map of parent recording path to list of attempts
    data class AttemptsData(
        val attemptsMap: Map<String, List<PlayerAttempt>> = emptyMap()
    )

    suspend fun saveAttempts(attemptsMap: Map<String, List<PlayerAttempt>>) =
        withContext(Dispatchers.IO) {
            try {
                // Filter out any null or empty values
                val cleanedMap = attemptsMap.filterValues { it.isNotEmpty() }

                val data = AttemptsData(cleanedMap)
                val json = gson.toJson(data)

                // Write to a temp file first, then rename to avoid corruption
                val tempFile = File(context.filesDir, "attempts_temp.json")
                tempFile.writeText(json)

                // Delete old file if it exists
                if (attemptsFile.exists()) {
                    attemptsFile.delete()
                }

                // Rename temp file to actual file
                tempFile.renameTo(attemptsFile)

                Log.d(
                    "AttemptsRepository",
                    "Saved ${cleanedMap.size} parent recordings with attempts"
                )
            } catch (e: Exception) {
                Log.e("AttemptsRepository", "Error saving attempts", e)
            }
        }

    suspend fun loadAttempts(): Map<String, List<PlayerAttempt>> = withContext(Dispatchers.IO) {
        try {
            if (!attemptsFile.exists()) {
                Log.e("AttemptsRepository", "No attempts file found")
                return@withContext emptyMap()
            }

            val json = attemptsFile.readText()
            Log.e("AttemptsRepository", "📄 JSON length: ${json.length}")
            Log.e("AttemptsRepository", "📄 JSON preview: ${json.take(300)}")

            if (json.isBlank()) {
                Log.e("AttemptsRepository", "Attempts file is empty")
                return@withContext emptyMap()
            }

            val type = object : TypeToken<AttemptsData>() {}.type
            val data: AttemptsData? = gson.fromJson(json, type)

            Log.e("AttemptsRepository", "📦 Parsed data null? ${data == null}")
            Log.e("AttemptsRepository", "📦 attemptsMap size: ${data?.attemptsMap?.size ?: -1}")

            if (data == null) {
                Log.e("AttemptsRepository", "❌ Gson returned null!")
                return@withContext emptyMap()
            }

            // data is now guaranteed non-null
            data.attemptsMap.forEach { (path, attempts) ->
                Log.e("AttemptsRepository", "🎵 Recording: $path has ${attempts.size} attempts")
                attempts.forEachIndexed { i, attempt ->
                    Log.e("AttemptsRepository", "   [$i] file exists? ${File(attempt.attemptFilePath).exists()} - ${attempt.attemptFilePath}")
                }
            }

            // Filter out attempts whose files no longer exist
            val result = data.attemptsMap.mapValues { (_, attempts) ->
                attempts.filter { File(it.attemptFilePath).exists() }
            }.filterValues { it.isNotEmpty() }

            Log.e("AttemptsRepository", "✅ Final result: ${result.size} recordings")
            return@withContext result
        } catch (e: Exception) {
            Log.e("AttemptsRepository", "💥 Error loading attempts", e)
            return@withContext emptyMap()
        }
    }
}