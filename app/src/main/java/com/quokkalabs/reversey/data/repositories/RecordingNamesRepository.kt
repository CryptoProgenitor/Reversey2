package com.quokkalabs.reversey.data.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RecordingNamesRepository @Inject constructor(
    private val threadSafeJsonRepository: ThreadSafeJsonRepository
) {
    suspend fun loadCustomNames(): Map<String, String> = withContext(Dispatchers.IO) {
        threadSafeJsonRepository.loadRecordingNamesJson()
    }

    suspend fun saveCustomNames(namesMap: Map<String, String>) = withContext(Dispatchers.IO) {
        threadSafeJsonRepository.saveRecordingNamesJson(namesMap)
    }

    suspend fun setCustomName(originalPath: String, customName: String) {
        val namesMap = loadCustomNames().toMutableMap()
        namesMap[originalPath] = customName
        saveCustomNames(namesMap)
    }

    suspend fun removeCustomName(originalPath: String) {
        val namesMap = loadCustomNames().toMutableMap()
        namesMap.remove(originalPath)
        saveCustomNames(namesMap)
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