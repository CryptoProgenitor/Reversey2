package com.quokkalabs.reversey.data.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import javax.inject.Inject

/**
 * Repository for managing custom recording names.
 * Uses atomic read-modify-write operations to prevent race conditions.
 */
class RecordingNamesRepository @Inject constructor(
    private val threadSafeJsonRepository: ThreadSafeJsonRepository
) {
    suspend fun loadCustomNames(): Map<String, String> = withContext(Dispatchers.IO) {
        threadSafeJsonRepository.loadRecordingNamesJson()
    }

    suspend fun saveCustomNames(namesMap: Map<String, String>) = withContext(Dispatchers.IO) {
        threadSafeJsonRepository.saveRecordingNamesJson(namesMap)
    }

    /**
     * Atomic set operation - read and write are both inside the mutex.
     *
     * CRITICAL: We read the actual file directly here, NOT via loadRecordingNamesJson(),
     * because that method tries to acquire the same mutex we already hold.
     * Kotlin's Mutex is NOT reentrant - calling a mutex-protected method from inside
     * a block that already holds that mutex will DEADLOCK.
     */
    suspend fun setCustomName(originalPath: String, customName: String) = withContext(Dispatchers.IO) {
        threadSafeJsonRepository.writeRecordingNamesJson { tempFile ->
            // Read current data DIRECTLY from actual file (not via loadRecordingNamesJson - that would deadlock!)
            val actualFile = File(tempFile.parent, "recording_names.json")
            val currentData = if (actualFile.exists()) {
                try {
                    val json = actualFile.readText()
                    val jsonObject = JSONObject(json)
                    val namesMap = mutableMapOf<String, String>()
                    jsonObject.keys().forEach { key ->
                        namesMap[key] = jsonObject.getString(key)
                    }
                    namesMap
                } catch (e: Exception) {
                    mutableMapOf()
                }
            } else {
                mutableMapOf()
            }

            // Modify
            currentData[originalPath] = customName

            // Write to temp file
            val jsonObject = JSONObject()
            currentData.forEach { (path, name) ->
                jsonObject.put(path, name)
            }
            tempFile.writeText(jsonObject.toString(2))
        }
    }

    /**
     * Atomic remove operation - read and write are both inside the mutex.
     */
    suspend fun removeCustomName(originalPath: String) = withContext(Dispatchers.IO) {
        threadSafeJsonRepository.writeRecordingNamesJson { tempFile ->
            // Read current data DIRECTLY from actual file (not via loadRecordingNamesJson - that would deadlock!)
            val actualFile = File(tempFile.parent, "recording_names.json")
            val currentData = if (actualFile.exists()) {
                try {
                    val json = actualFile.readText()
                    val jsonObject = JSONObject(json)
                    val namesMap = mutableMapOf<String, String>()
                    jsonObject.keys().forEach { key ->
                        namesMap[key] = jsonObject.getString(key)
                    }
                    namesMap
                } catch (e: Exception) {
                    mutableMapOf()
                }
            } else {
                mutableMapOf()
            }

            // Modify
            currentData.remove(originalPath)

            // Write to temp file
            val jsonObject = JSONObject()
            currentData.forEach { (path, name) ->
                jsonObject.put(path, name)
            }
            tempFile.writeText(jsonObject.toString(2))
        }
    }

    suspend fun getCustomName(originalPath: String): String? {
        return loadCustomNames()[originalPath]
    }

    // Disable cleanup to prevent data loss during refactor
    suspend fun cleanupOrphanedNames() {}

    suspend fun clearAllCustomNames() = withContext(Dispatchers.IO) {
        threadSafeJsonRepository.saveRecordingNamesJson(emptyMap())
    }
}