package com.quokkalabs.reversey.data.repositories

import android.util.Log
import com.quokkalabs.reversey.data.models.PlayerAttempt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AttemptsRepository @Inject constructor(
    private val threadSafeJsonRepository: ThreadSafeJsonRepository
) {
    suspend fun saveAttempts(attemptsMap: Map<String, List<PlayerAttempt>>) =
        withContext(Dispatchers.IO) {
            try {
                // Filter out empty entries
                val cleanedMap = attemptsMap.filterValues { it.isNotEmpty() }

                // Delegate to the ThreadSafe repository
                threadSafeJsonRepository.saveAttemptsJson(cleanedMap)

                Log.d("AttemptsRepository", "✅ Saved attempts for ${cleanedMap.size} recordings")
            } catch (e: Exception) {
                Log.e("AttemptsRepository", "💥 Error saving attempts", e)
            }
        }

    suspend fun loadAttempts(): Map<String, List<PlayerAttempt>> = withContext(Dispatchers.IO) {
        try {
            // Use the ThreadSafe repository to load
            val attemptsMap = threadSafeJsonRepository.loadAttemptsJson()

            // 🛑 CRITICAL FIX: Removed the .exists() filter.
            // Do NOT hide attempts just because the file check fails.
            // We need to see the data in the UI to debug path issues.

            Log.d("AttemptsRepository", "✅ Loaded attempts for ${attemptsMap.size} recordings")
            return@withContext attemptsMap
        } catch (e: Exception) {
            Log.e("AttemptsRepository", "💥 Error loading attempts", e)
            return@withContext emptyMap()
        }
    }

    suspend fun clearAllAttempts() = withContext(Dispatchers.IO) {
        try {
            threadSafeJsonRepository.saveAttemptsJson(emptyMap())
            Log.d("AttemptsRepository", "✅ Cleared all attempts")
        } catch (e: Exception) {
            Log.e("AttemptsRepository", "💥 Error clearing attempts", e)
        }
    }

}