package com.quokkalabs.reversey.data.repositories

import android.content.Context
import android.util.Log
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thread-safe repository for JSON file operations.
 *
 * CRITICAL: Prevents race conditions when multiple threads access the same JSON file.
 *
 * USE CASES:
 * - Game saves score while backup imports → COLLISION prevented
 * - Multiple backup operations → SERIALIZED safely
 *
 * PATTERN: Mutex ensures only ONE writer at a time
 */
@Singleton
class ThreadSafeJsonRepository @Inject constructor(
    private val context: Context
) {
    // Mutex for attempts.json - only ONE operation at a time
    private val attemptsJsonMutex = Mutex()

    // Mutex for recording_names.json - only ONE operation at a time
    private val recordingNamesMutex = Mutex()

    companion object {
        private const val TAG = "ThreadSafeJsonRepo"
        private const val ATTEMPTS_FILE = "attempts.json"
        private const val ATTEMPTS_TEMP_FILE = "attempts_temp.json"
        private const val NAMES_FILE = "recording_names.json"
        private const val NAMES_TEMP_FILE = "recording_names_temp.json"
    }

    /**
     * Read attempts.json safely.
     *
     * THREAD-SAFE: Acquires mutex, blocks if another operation in progress.
     *
     * @param block Function to execute with the file (read operations)
     * @return Result of the block function
     */
    suspend fun <T> readAttemptsJson(block: (File) -> T): T {
        return attemptsJsonMutex.withLock {
            val file = File(context.filesDir, ATTEMPTS_FILE)

            if (!file.exists()) {
                Log.d(TAG, "attempts.json does not exist, creating empty")
                file.writeText("{\"attemptsMap\":{}}")
            }

            try {
                block(file)
            } catch (e: Exception) {
                Log.e(TAG, "Error reading attempts.json", e)
                throw e
            }
        }
    }

    /**
     * Write attempts.json safely with atomic rename.
     *
     * THREAD-SAFE: Acquires mutex, blocks if another operation in progress.
     * ATOMIC: Writes to temp file, then renames (either succeeds completely or fails completely).
     *
     * PATTERN:
     * 1. Write to temp file
     * 2. If successful, rename temp → actual
     * 3. If rename fails, temp file stays, actual file untouched
     *
     * @param block Function to execute that writes data (receives temp file)
     * @return Result of the block function
     */
    suspend fun <T> writeAttemptsJson(block: (File) -> T): T {
        return attemptsJsonMutex.withLock {
            val tempFile = File(context.filesDir, ATTEMPTS_TEMP_FILE)
            val actualFile = File(context.filesDir, ATTEMPTS_FILE)

            try {
                // Execute block on temp file
                val result = block(tempFile)

                // CRITICAL FIX: Use atomic move to prevent data loss
                // Previous implementation deleted first, then renamed - if rename failed, data was lost!
                // Files.move with ATOMIC_MOVE ensures either complete success or no change
                try {
                    Files.move(
                        tempFile.toPath(),
                        actualFile.toPath(),
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE
                    )
                    Log.d(TAG, "Successfully wrote attempts.json atomically")
                } catch (atomicError: java.nio.file.AtomicMoveNotSupportedException) {
                    // Fallback for filesystems that don't support atomic move
                    Log.w(TAG, "Atomic move not supported, using fallback with REPLACE_EXISTING")
                    Files.move(
                        tempFile.toPath(),
                        actualFile.toPath(),
                        StandardCopyOption.REPLACE_EXISTING
                    )
                    Log.d(TAG, "Successfully wrote attempts.json with fallback")
                }

                result

            } catch (e: Exception) {
                Log.e(TAG, "Error writing attempts.json", e)

                // Clean up temp file on failure
                if (tempFile.exists()) {
                    tempFile.delete()
                }

                throw e
            }
        }
    }

    /**
     * Read recording_names.json safely.
     *
     * THREAD-SAFE: Acquires mutex, blocks if another operation in progress.
     */
    suspend fun <T> readRecordingNamesJson(block: (File) -> T): T {
        return recordingNamesMutex.withLock {
            val recordingsDir = File(context.filesDir, "recordings")
            recordingsDir.mkdirs()

            val file = File(recordingsDir, NAMES_FILE)

            if (!file.exists()) {
                Log.d(TAG, "recording_names.json does not exist, creating empty")
                file.writeText("{}")
            }

            try {
                block(file)
            } catch (e: Exception) {
                Log.e(TAG, "Error reading recording_names.json", e)
                throw e
            }
        }
    }

    /**
     * Write recording_names.json safely with atomic rename.
     *
     * THREAD-SAFE: Acquires mutex, blocks if another operation in progress.
     * ATOMIC: Writes to temp file, then renames.
     */
    suspend fun <T> writeRecordingNamesJson(block: (File) -> T): T {
        return recordingNamesMutex.withLock {
            val recordingsDir = File(context.filesDir, "recordings")
            recordingsDir.mkdirs()

            val tempFile = File(recordingsDir, NAMES_TEMP_FILE)
            val actualFile = File(recordingsDir, NAMES_FILE)

            try {
                // Execute block on temp file
                val result = block(tempFile)

                // CRITICAL FIX: Use atomic move to prevent data loss
                try {
                    Files.move(
                        tempFile.toPath(),
                        actualFile.toPath(),
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE
                    )
                    Log.d(TAG, "Successfully wrote recording_names.json atomically")
                } catch (atomicError: java.nio.file.AtomicMoveNotSupportedException) {
                    // Fallback for filesystems that don't support atomic move
                    Log.w(TAG, "Atomic move not supported, using fallback with REPLACE_EXISTING")
                    Files.move(
                        tempFile.toPath(),
                        actualFile.toPath(),
                        StandardCopyOption.REPLACE_EXISTING
                    )
                    Log.d(TAG, "Successfully wrote recording_names.json with fallback")
                }

                result

            } catch (e: Exception) {
                Log.e(TAG, "Error writing recording_names.json", e)

                // Clean up temp file on failure
                if (tempFile.exists()) {
                    tempFile.delete()
                }

                throw e
            }
        }
    }

    // ============================================================
    //  HIGH-LEVEL METHODS (with automatic JSON parsing)
    // ============================================================

    /**
     * Load attempts as a Map structure (automatically parses JSON).
     */
    suspend fun loadAttemptsJson(): Map<String, List<com.quokkalabs.reversey.data.models.PlayerAttempt>> {
        return readAttemptsJson { file ->
            try {
                val json = file.readText()
                val gson = com.google.gson.Gson()
                val type = object : com.google.gson.reflect.TypeToken<AttemptsData>() {}.type
                val data: AttemptsData = gson.fromJson(json, type)
                data.attemptsMap
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing attempts.json", e)
                emptyMap()
            }
        }
    }

    /**
     * Save attempts Map structure (automatically serializes to JSON).
     */
    suspend fun saveAttemptsJson(attemptsMap: Map<String, List<com.quokkalabs.reversey.data.models.PlayerAttempt>>) {
        writeAttemptsJson { tempFile ->
            val gson = com.google.gson.Gson()
            val data = AttemptsData(attemptsMap)
            val json = gson.toJson(data)
            tempFile.writeText(json)
        }
    }

    /**
     * Load recording names as a Map structure (automatically parses JSON).
     */
    suspend fun loadRecordingNamesJson(): Map<String, String> {
        return readRecordingNamesJson { file ->
            try {
                val json = file.readText()
                val jsonObject = org.json.JSONObject(json)
                val namesMap = mutableMapOf<String, String>()
                jsonObject.keys().forEach { key ->
                    namesMap[key] = jsonObject.getString(key)
                }
                namesMap
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing recording_names.json", e)
                emptyMap()
            }
        }
    }

    /**
     * Save recording names Map structure (automatically serializes to JSON).
     */
    suspend fun saveRecordingNamesJson(namesMap: Map<String, String>) {
        writeRecordingNamesJson { tempFile ->
            val jsonObject = org.json.JSONObject()
            namesMap.forEach { (path, name) ->
                jsonObject.put(path, name)
            }
            tempFile.writeText(jsonObject.toString(2))
        }
    }

    /**
     * Data structure for attempts.json
     */
    data class AttemptsData(
        val attemptsMap: Map<String, List<com.quokkalabs.reversey.data.models.PlayerAttempt>> = emptyMap()
    )

    /**
     * Get direct file reference (USE WITH CAUTION).
     *
     * WARNING: Direct access bypasses mutex protection.
     * Only use for read-only operations where thread safety is guaranteed elsewhere.
     */
    fun getAttemptsFile(): File = File(context.filesDir, ATTEMPTS_FILE)

    /**
     * Get direct file reference (USE WITH CAUTION).
     *
     * WARNING: Direct access bypasses mutex protection.
     * Only use for read-only operations where thread safety is guaranteed elsewhere.
     */
    fun getRecordingNamesFile(): File {
        val recordingsDir = File(context.filesDir, "recordings")
        recordingsDir.mkdirs()
        return File(recordingsDir, NAMES_FILE)
    }
}