package com.example.reversey.data.repositories

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File

/**
 * Repository for managing custom recording display names.
 *
 * Stores a JSON file mapping: originalPath -> customDisplayName
 * This allows users to rename recordings without touching the actual files.
 *
 * File location: /recordings/recording_names.json
 * Format: { "/path/to/file.wav": "My Custom Name" }
 */
class RecordingNamesRepository(private val context: Context) {
    private val namesFile: File
        get() = File(getRecordingsDir(), "recording_names.json")

    /**
     * Load all custom recording names from JSON
     */
    suspend fun loadCustomNames(): Map<String, String> = withContext(Dispatchers.IO) {
        try {
            if (!namesFile.exists()) {
                Log.d("RecordingNames", "No custom names file found, returning empty map")
                return@withContext emptyMap()
            }

            val jsonString = namesFile.readText()
            val jsonObject = JSONObject(jsonString)
            val namesMap = mutableMapOf<String, String>()

            jsonObject.keys().forEach { key ->
                namesMap[key] = jsonObject.getString(key)
            }

            Log.d("RecordingNames", "Loaded ${namesMap.size} custom names")
            namesMap
        } catch (e: Exception) {
            Log.e("RecordingNames", "Error loading custom names: ${e.message}", e)
            emptyMap()
        }
    }

    /**
     * Save all custom recording names to JSON
     */
    suspend fun saveCustomNames(namesMap: Map<String, String>) = withContext(Dispatchers.IO) {
        try {
            val jsonObject = JSONObject()
            namesMap.forEach { (path, name) ->
                jsonObject.put(path, name)
            }

            namesFile.writeText(jsonObject.toString(2)) // Pretty print with indent=2
            Log.d("RecordingNames", "Saved ${namesMap.size} custom names to ${namesFile.absolutePath}")
        } catch (e: Exception) {
            Log.e("RecordingNames", "Error saving custom names: ${e.message}", e)
        }
    }

    /**
     * Set a custom name for a specific recording
     */
    suspend fun setCustomName(originalPath: String, customName: String) {
        val namesMap = loadCustomNames().toMutableMap()
        namesMap[originalPath] = customName
        saveCustomNames(namesMap)
    }

    /**
     * Remove custom name for a recording (revert to default)
     */
    suspend fun removeCustomName(originalPath: String) {
        val namesMap = loadCustomNames().toMutableMap()
        namesMap.remove(originalPath)
        saveCustomNames(namesMap)
    }

    /**
     * Get custom name for a recording, or null if not set
     */
    suspend fun getCustomName(originalPath: String): String? {
        return loadCustomNames()[originalPath]
    }

    /**
     * Clean up custom names for files that no longer exist
     */
    suspend fun cleanupOrphanedNames() = withContext(Dispatchers.IO) {
        val namesMap = loadCustomNames().toMutableMap()
        val iterator = namesMap.iterator()
        var removedCount = 0

        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (!File(entry.key).exists()) {
                iterator.remove()
                removedCount++
            }
        }

        if (removedCount > 0) {
            saveCustomNames(namesMap)
            Log.d("RecordingNames", "Cleaned up $removedCount orphaned custom names")
        }
    }

    private fun getRecordingsDir(): File {
        return File(context.filesDir, "recordings").apply { mkdirs() }
    }
}