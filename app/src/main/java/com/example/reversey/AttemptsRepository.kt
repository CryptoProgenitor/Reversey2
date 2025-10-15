package com.example.reversey

import android.content.Context
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

    suspend fun saveAttempts(attemptsMap: Map<String, List<PlayerAttempt>>) = withContext(Dispatchers.IO) {
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

            android.util.Log.d("AttemptsRepository", "Saved ${cleanedMap.size} parent recordings with attempts")
        } catch (e: Exception) {
            android.util.Log.e("AttemptsRepository", "Error saving attempts", e)
        }
    }

    suspend fun loadAttempts(): Map<String, List<PlayerAttempt>> = withContext(Dispatchers.IO) {
        try {
            if (!attemptsFile.exists()) {
                android.util.Log.d("AttemptsRepository", "No attempts file found")
                return@withContext emptyMap()
            }

            val json = attemptsFile.readText()
            if (json.isBlank()) {
                android.util.Log.d("AttemptsRepository", "Attempts file is empty")
                return@withContext emptyMap()
            }

            val type = object : TypeToken<AttemptsData>() {}.type
            val data: AttemptsData = gson.fromJson(json, type)

            // Filter out attempts whose files no longer exist
            val result = data.attemptsMap.mapValues { (_, attempts) ->
                attempts.filter { File(it.attemptFilePath).exists() }
            }.filterValues { it.isNotEmpty() }

            android.util.Log.d("AttemptsRepository", "Loaded ${result.size} parent recordings with attempts")
            return@withContext result
        } catch (e: Exception) {
            android.util.Log.e("AttemptsRepository", "Error loading attempts", e)
            // If the file is corrupted, delete it
            try {
                attemptsFile.delete()
            } catch (_: Exception) {}
            return@withContext emptyMap()
        }
    }
}