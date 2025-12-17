package com.quokkalabs.reversey.data.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import javax.inject.Inject

/**
 * Repository for managing custom recording names.
 * CRITICAL FIX: Uses atomic read-modify-write operations to prevent race conditions.
 * Previous implementation could lose updates when two concurrent calls modified the names.
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
     * CRITICAL FIX: Atomic set operation - read and write are both inside the mutex
     * Previous implementation: load() -> modify -> save() could lose concurrent updates
     */
    suspend fun setCustomName(originalPath: String, customName: String) = withContext(Dispatchers.IO) {
        threadSafeJsonRepository.writeRecordingNamesJson { tempFile ->
            // Read current data
            val currentData = threadSafeJsonRepository.loadRecordingNamesJson().toMutableMap()
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
     * CRITICAL FIX: Atomic remove operation - read and write are both inside the mutex
     */
    suspend fun removeCustomName(originalPath: String) = withContext(Dispatchers.IO) {
        threadSafeJsonRepository.writeRecordingNamesJson { tempFile ->
            // Read current data
            val currentData = threadSafeJsonRepository.loadRecordingNamesJson().toMutableMap()
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