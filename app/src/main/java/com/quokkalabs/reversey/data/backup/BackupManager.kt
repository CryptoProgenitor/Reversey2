package com.quokkalabs.reversey.data.backup

import android.content.Context
import android.content.pm.PackageInfo
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.quokkalabs.reversey.data.models.ChallengeType
import com.quokkalabs.reversey.data.models.PlayerAttempt
import com.quokkalabs.reversey.data.models.Recording
import com.quokkalabs.reversey.data.repositories.AttemptsRepository
import com.quokkalabs.reversey.data.repositories.RecordingNamesRepository
import com.quokkalabs.reversey.data.repositories.RecordingRepository
import com.quokkalabs.reversey.data.repositories.ThreadSafeJsonRepository
import com.quokkalabs.reversey.scoring.DifficultyLevel
import com.quokkalabs.reversey.security.SecurityUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject

class BackupManager @Inject constructor(
    private val context: Context,
    private val recordingRepository: RecordingRepository,
    private val attemptsRepository: AttemptsRepository,
    private val recordingNamesRepository: RecordingNamesRepository,
    private val threadSafeJsonRepo: ThreadSafeJsonRepository
) {

    private val securityUtils = SecurityUtils
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    // Progress tracking for export operations
    private val _exportProgress = MutableStateFlow<BackupProgress>(BackupProgress.Idle)
    val exportProgress: StateFlow<BackupProgress> = _exportProgress.asStateFlow()

    // Progress tracking for import operations
    private val _importProgress = MutableStateFlow<BackupProgress>(BackupProgress.Idle)
    val importProgress: StateFlow<BackupProgress> = _importProgress.asStateFlow()

    companion object {
        private const val TAG = "BackupManager"
        private const val MANIFEST_FILENAME = "manifest.json"
        // Zip internal structure
        private const val RECORDINGS_PATH = "recordings/"
        private const val ATTEMPTS_PATH = "attempts/"
    }

    // Helper to get consistent directories matching RecordingRepository expectations
    private fun getRecordingsDir(): File = File(context.filesDir, "recordings").apply { mkdirs() }
    private fun getAttemptsDir(): File = File(getRecordingsDir(), "attempts").apply { mkdirs() }

    // ============================================================
    //  EXPORT
    // ============================================================

    suspend fun exportFullBackup(outputDir: File): BackupResult = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting FULL backup export...")
            val recordings = recordingRepository.loadRecordings()
            Log.d(TAG, "Loaded ${recordings.size} recordings")
            val attemptsMap = threadSafeJsonRepo.loadAttemptsJson()
            Log.d(TAG, "Loaded ${attemptsMap.size} attempts")
            val customNames = threadSafeJsonRepo.loadRecordingNamesJson()
            Log.d(TAG, "Loaded ${customNames.size} custom names")

            val result = performExport(recordings, attemptsMap, customNames, outputDir, null)
            Log.d(TAG, "Export result: success=${result.success}, recordings=${result.recordingsExported}")
            result
        } catch (e: Exception) {
            Log.e(TAG, "Full backup export failed: ${e.message}", e)
            BackupResult(false, null, 0, 0, 0, e.message)
        }
    }

    suspend fun exportDateRangeBackup(fromMs: Long, toMs: Long, outputDir: File): BackupResult = withContext(Dispatchers.IO) {
        try {
            val allRecordings = recordingRepository.loadRecordings()
            val allAttemptsMap = threadSafeJsonRepo.loadAttemptsJson()
            val customNames = threadSafeJsonRepo.loadRecordingNamesJson()

            val filteredRecordings = allRecordings.filter {
                File(it.originalPath).lastModified() in fromMs..toMs
            }

            val filteredAttemptsMap = allAttemptsMap.filterKeys { path ->
                filteredRecordings.any { it.originalPath == path }
            }

            // Simplified DateRange for export metadata
            val dateRange = DateRange(fromMs, toMs, "", "")

            performExport(filteredRecordings, filteredAttemptsMap, customNames, outputDir, dateRange)
        } catch (e: Exception) {
            BackupResult(false, null, 0, 0, 0, e.message)
        }
    }

    suspend fun exportCustomSelection(recordingPaths: List<String>, outputDir: File): BackupResult = withContext(Dispatchers.IO) {
        try {
            val allRecordings = recordingRepository.loadRecordings()
            val allAttemptsMap = threadSafeJsonRepo.loadAttemptsJson()
            val customNames = threadSafeJsonRepo.loadRecordingNamesJson()

            val selectedRecordings = allRecordings.filter { it.originalPath in recordingPaths }
            val selectedAttemptsMap = allAttemptsMap.filterKeys { it in recordingPaths }

            performExport(selectedRecordings, selectedAttemptsMap, customNames, outputDir, null)
        } catch (e: Exception) {
            BackupResult(false, null, 0, 0, 0, e.message)
        }
    }

    // ============================================================
    //  GAME PACKAGE EXPORT (Remote Play)
    // ============================================================

    /**
     * Export a recording as a Challenge package for Remote Play.
     * Creates a ZIP containing the original WAV and a GamePackageManifest.
     *
     * @param recording The recording to share as a challenge
     * @param outputDir Directory to write the ZIP file
     * @return GamePackageResult with the ZIP file path
     */
    suspend fun exportChallenge(recording: Recording, outputDir: File): GamePackageResult = withContext(Dispatchers.IO) {
        try {
            val originalFile = File(recording.originalPath)
            if (!originalFile.exists()) {
                return@withContext GamePackageResult(false, null, null, null, "Recording file not found")
            }

            val gameId = originalFile.lastModified()
            val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val zipFile = File(outputDir, "reversey_challenge_$timestamp.rvy")

            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)

            // Build recording entry
            val recordingEntry = RecordingBackupEntry(
                filename = originalFile.name,
                reversedFilename = recording.reversedPath?.let { File(it).name },
                hash = calculateFileHash(originalFile),
                creationTimestampMs = gameId,
                lastModified = originalFile.lastModified(),
                fileSizeBytes = originalFile.length(),
                vocalMode = recording.vocalAnalysis?.mode?.name,
                vocalConfidence = recording.vocalAnalysis?.confidence,
                vocalFeatures = recording.vocalAnalysis?.toBackup()?.features
            )

            // Get custom name if exists
            val customNames = threadSafeJsonRepo.loadRecordingNamesJson()
            val customName = customNames[recording.originalPath]

            // Build manifest
            val manifest = GamePackageManifest(
                type = GamePackageType.CHALLENGE,
                gameId = gameId,
                exportTimestampMs = System.currentTimeMillis(),
                appVersionName = packageInfo.versionName ?: "1.0",
                appVersionCode = packageInfo.versionCode,
                recording = recordingEntry,
                attempt = null,
                customName = customName
            )

            // Create ZIP
            ZipOutputStream(FileOutputStream(zipFile)).use { zipOut ->
                // Add original WAV
                zipOut.putNextEntry(ZipEntry(originalFile.name))
                FileInputStream(originalFile).use { it.copyTo(zipOut) }
                zipOut.closeEntry()

                // Add manifest
                zipOut.putNextEntry(ZipEntry(MANIFEST_FILENAME))
                zipOut.write(gson.toJson(manifest).toByteArray())
                zipOut.closeEntry()
            }

            Log.d(TAG, "Exported challenge: gameId=$gameId, file=${zipFile.name}")
            GamePackageResult(true, zipFile, GamePackageType.CHALLENGE, gameId)

        } catch (e: Exception) {
            Log.e(TAG, "Failed to export challenge", e)
            GamePackageResult(false, null, null, null, e.message)
        }
    }

    /**
     * Export a recording + attempt as a Response package for Remote Play.
     * Creates a ZIP containing original WAV, attempt WAV, and GamePackageManifest.
     *
     * @param recording The parent recording
     * @param attempt The attempt to share as a response
     * @param outputDir Directory to write the ZIP file
     * @return GamePackageResult with the ZIP file path
     */
    suspend fun exportResponse(recording: Recording, attempt: PlayerAttempt, outputDir: File): GamePackageResult = withContext(Dispatchers.IO) {
        try {
            val originalFile = File(recording.originalPath)
            val attemptFile = File(attempt.attemptFilePath)

            if (!originalFile.exists()) {
                return@withContext GamePackageResult(false, null, null, null, "Recording file not found")
            }
            if (!attemptFile.exists()) {
                return@withContext GamePackageResult(false, null, null, null, "Attempt file not found")
            }

            val gameId = originalFile.lastModified()
            val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val zipFile = File(outputDir, "reversey_response_$timestamp.rvy")

            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)

            // Build recording entry
            val recordingEntry = RecordingBackupEntry(
                filename = originalFile.name,
                reversedFilename = recording.reversedPath?.let { File(it).name },
                hash = calculateFileHash(originalFile),
                creationTimestampMs = gameId,
                lastModified = originalFile.lastModified(),
                fileSizeBytes = originalFile.length(),
                vocalMode = recording.vocalAnalysis?.mode?.name,
                vocalConfidence = recording.vocalAnalysis?.confidence,
                vocalFeatures = recording.vocalAnalysis?.toBackup()?.features
            )

            // Build attempt entry
            val attemptEntry = AttemptBackupEntry(
                parentRecordingFilename = originalFile.name,
                attemptFilename = attemptFile.name,
                reversedAttemptFilename = attempt.reversedAttemptFilePath?.let { File(it).name },
                hash = calculateFileHash(attemptFile),
                metadata = attemptToBackupMetadata(attempt)
            )

            // Get custom name if exists
            val customNames = threadSafeJsonRepo.loadRecordingNamesJson()
            val customName = customNames[recording.originalPath]

            // Build manifest
            val manifest = GamePackageManifest(
                type = GamePackageType.RESPONSE,
                gameId = gameId,
                exportTimestampMs = System.currentTimeMillis(),
                appVersionName = packageInfo.versionName ?: "1.0",
                appVersionCode = packageInfo.versionCode,
                recording = recordingEntry,
                attempt = attemptEntry,
                customName = customName
            )

            // Create ZIP
            ZipOutputStream(FileOutputStream(zipFile)).use { zipOut ->
                // Add original WAV
                zipOut.putNextEntry(ZipEntry(originalFile.name))
                FileInputStream(originalFile).use { it.copyTo(zipOut) }
                zipOut.closeEntry()

                // Add attempt WAV
                zipOut.putNextEntry(ZipEntry("attempts/${attemptFile.name}"))
                FileInputStream(attemptFile).use { it.copyTo(zipOut) }
                zipOut.closeEntry()

                // Add manifest
                zipOut.putNextEntry(ZipEntry(MANIFEST_FILENAME))
                zipOut.write(gson.toJson(manifest).toByteArray())
                zipOut.closeEntry()
            }

            Log.d(TAG, "Exported response: gameId=$gameId, file=${zipFile.name}")
            GamePackageResult(true, zipFile, GamePackageType.RESPONSE, gameId)

        } catch (e: Exception) {
            Log.e(TAG, "Failed to export response", e)
            GamePackageResult(false, null, null, null, e.message)
        }
    }

    /**
     * Import a Game Package (Challenge or Response).
     * Uses game_id (timestamp) matching to merge with existing recordings.
     *
     * IMPORT LOGIC:
     * 1. Read game_id from manifest
     * 2. Scan local recordings for any with matching lastModified timestamp
     * 3. If match found: Merge attempt (for RESPONSE) or skip (for CHALLENGE)
     * 4. If no match: Import as new recording
     *
     * @param packageFile The .rvy ZIP file to import
     * @return RestoreResult with import statistics
     */
    suspend fun importGamePackage(packageFile: File): RestoreResult = withContext(Dispatchers.IO) {
        try {
            if (!securityUtils.isValidZipFile(packageFile)) {
                return@withContext RestoreResult(false, 0, 0, 0, 0, "Invalid package file")
            }

            // Try to extract as GamePackageManifest first
            val gameManifest = extractGamePackageManifest(packageFile)
            if (gameManifest == null) {
                // Not a game package - fall back to full backup import
                Log.d(TAG, "Not a game package, delegating to full backup import")
                return@withContext importBackup(packageFile, ConflictStrategy.SKIP_DUPLICATES)
            }

            Log.d(TAG, "Importing game package: type=${gameManifest.type}, gameId=${gameManifest.gameId}")

            val recordingsDir = getRecordingsDir()
            val attemptsDir = getAttemptsDir()

            // Load current state
            val localRecordings = recordingRepository.loadRecordings()
            val existingAttemptsMap = threadSafeJsonRepo.loadAttemptsJson().toMutableMap()
            val existingCustomNames = threadSafeJsonRepo.loadRecordingNamesJson().toMutableMap()

            // Find local recording matching game_id (by timestamp)
            val matchingRecording = localRecordings.find { recording ->
                val localFile = File(recording.originalPath)
                localFile.exists() && localFile.lastModified() == gameManifest.gameId
            }

            var importedRecs = 0
            var skippedRecs = 0
            var importedAttempts = 0
            var restoredNames = 0

            ZipInputStream(FileInputStream(packageFile)).use { zipIn ->
                var entry = zipIn.nextEntry
                while (entry != null) {
                    val entryName = entry.name

                    when {
                        entryName == MANIFEST_FILENAME -> { /* Skip manifest */ }

                        entryName.endsWith(".wav") && !entryName.startsWith("attempts/") -> {
                            // This is the original recording WAV
                            if (matchingRecording != null) {
                                // Recording exists locally - skip import
                                skippedRecs++
                                Log.d(TAG, "Recording exists locally, skipping: ${gameManifest.recording.filename}")
                            } else {
                                // New recording - import it
                                val targetFile = File(recordingsDir, gameManifest.recording.filename)
                                if (!targetFile.exists()) {
                                    FileOutputStream(targetFile).use { zipIn.copyTo(it) }
                                    // Restore original timestamp for game_id matching
                                    targetFile.setLastModified(gameManifest.gameId)
                                    importedRecs++
                                    Log.d(TAG, "Imported new recording: ${targetFile.name}")
                                } else {
                                    skippedRecs++
                                    Log.d(TAG, "Recording file already exists: ${targetFile.name}")
                                }
                            }
                        }

                        entryName.startsWith("attempts/") && entryName.endsWith(".wav") -> {
                            // This is an attempt WAV
                            val attemptFilename = entryName.removePrefix("attempts/")
                            val targetFile = File(attemptsDir, attemptFilename)

                            if (!targetFile.exists()) {
                                FileOutputStream(targetFile).use { zipIn.copyTo(it) }
                                Log.d(TAG, "Extracted attempt file: $attemptFilename")
                            }

                            // Determine parent path
                            val parentPath = matchingRecording?.originalPath
                                ?: File(recordingsDir, gameManifest.recording.filename).absolutePath

                            // Add attempt to JSON if not already present
                            gameManifest.attempt?.let { attemptEntry ->
                                val existingAttempts = existingAttemptsMap[parentPath] ?: emptyList()
                                val alreadyExists = existingAttempts.any {
                                    it.attemptFilePath == targetFile.absolutePath
                                }

                                if (!alreadyExists) {
                                    val playerAttempt = metadataToPlayerAttempt(
                                        attemptEntry.metadata,
                                        targetFile.absolutePath,
                                        attemptEntry.reversedAttemptFilename?.let {
                                            File(attemptsDir, it).absolutePath
                                        }
                                    )

                                    existingAttemptsMap.compute(parentPath) { _, list ->
                                        (list ?: emptyList()) + playerAttempt
                                    }
                                    importedAttempts++
                                    Log.d(TAG, "Added attempt to recording: $parentPath")
                                }
                            }
                        }
                    }

                    zipIn.closeEntry()
                    entry = zipIn.nextEntry
                }
            }

            // Restore custom name if provided and recording was imported
            gameManifest.customName?.let { name ->
                val recordingPath = matchingRecording?.originalPath
                    ?: File(recordingsDir, gameManifest.recording.filename).absolutePath
                existingCustomNames[recordingPath] = name
                restoredNames++
            }

            // Save updated metadata
            threadSafeJsonRepo.saveAttemptsJson(existingAttemptsMap)
            threadSafeJsonRepo.saveRecordingNamesJson(existingCustomNames)

            Log.d(TAG, "Game package import complete: recs=$importedRecs, attempts=$importedAttempts")
            RestoreResult(true, importedRecs, skippedRecs, importedAttempts, restoredNames)

        } catch (e: Exception) {
            Log.e(TAG, "Failed to import game package", e)
            RestoreResult(false, 0, 0, 0, 0, e.message)
        }
    }

    /**
     * Extract GamePackageManifest from a ZIP file.
     * Returns null if the manifest is not a game package format.
     */
    private fun extractGamePackageManifest(file: File): GamePackageManifest? {
        try {
            ZipInputStream(FileInputStream(file)).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    if (entry.name == MANIFEST_FILENAME) {
                        val jsonContent = zip.readBytes().toString(Charsets.UTF_8)
                        // Check if it's a game package by looking for "gameId" field
                        if (jsonContent.contains("\"gameId\"")) {
                            return gson.fromJson(jsonContent, GamePackageManifest::class.java)
                        }
                        return null // It's a regular backup manifest
                    }
                    zip.closeEntry()
                    entry = zip.nextEntry
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to extract game package manifest", e)
        }
        return null
    }

    private suspend fun performExport(
        recordings: List<Recording>,
        attemptsMap: Map<String, List<PlayerAttempt>>,
        customNames: Map<String, String>,
        outputDir: File,
        dateRange: DateRange?
    ): BackupResult = withContext(Dispatchers.IO) {
        try {
            _exportProgress.value = BackupProgress.InProgress(
                phase = BackupPhase.ANALYZING,
                currentItem = 0,
                totalItems = recordings.size,
                currentFileName = "",
                message = "Analyzing ${recordings.size} recordings..."
            )

            val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val zipFile = File(outputDir, "reversey_backup_$timestamp.zip")
            var totalSizeBytes = 0L

            val recordingEntries = mutableListOf<RecordingBackupEntry>()
            val attemptEntriesMap = mutableMapOf<String, MutableList<AttemptBackupEntry>>()
            val customNamesMap = mutableMapOf<String, String>()
            val addedFiles = mutableSetOf<String>()

            ZipOutputStream(FileOutputStream(zipFile)).use { zipOut ->
                // --- EXPORT RECORDINGS ---
                recordings.forEachIndexed { index, recording ->
                    val originalFile = File(recording.originalPath)
                    if (originalFile.exists()) {
                        val filename = originalFile.name

                        // Emit progress
                        _exportProgress.value = BackupProgress.InProgress(
                            phase = BackupPhase.EXPORTING_RECORDINGS,
                            currentItem = index + 1,
                            totalItems = recordings.size,
                            currentFileName = filename,
                            message = "Exporting recording ${index + 1}/${recordings.size}"
                        )

                        if (filename !in addedFiles) {
                            // Store in zip under recordings/
                            zipOut.putNextEntry(ZipEntry("$RECORDINGS_PATH$filename"))
                            FileInputStream(originalFile).use { it.copyTo(zipOut) }
                            zipOut.closeEntry()
                            addedFiles.add(filename)
                            totalSizeBytes += originalFile.length()
                        }

                        // Export reversed recording if exists
                        var reversedFilename: String? = null
                        recording.reversedPath?.let { revPath ->
                            val reversedFile = File(revPath)
                            if (reversedFile.exists()) {
                                val revName = reversedFile.name
                                reversedFilename = revName
                                if (revName !in addedFiles) {
                                    zipOut.putNextEntry(ZipEntry("$RECORDINGS_PATH$revName"))
                                    FileInputStream(reversedFile).use { it.copyTo(zipOut) }
                                    zipOut.closeEntry()
                                    addedFiles.add(revName)
                                    totalSizeBytes += reversedFile.length()
                                }
                            }
                        }

                        // Create Manifest Entry
                        recordingEntries.add(
                            RecordingBackupEntry(
                                filename = filename,
                                reversedFilename = reversedFilename,
                                hash = calculateFileHash(originalFile),
                                creationTimestampMs = originalFile.lastModified(),
                                lastModified = originalFile.lastModified(),
                                fileSizeBytes = originalFile.length(),
                                vocalMode = recording.vocalAnalysis?.mode?.name,
                                vocalConfidence = recording.vocalAnalysis?.confidence,
                                vocalFeatures = recording.vocalAnalysis?.toBackup()?.features
                            )
                        )

                        // Handle custom names
                        customNames[recording.originalPath]?.let { name ->
                            customNamesMap[filename] = name
                        }
                    }
                }

                // --- EXPORT ATTEMPTS ---
                val totalAttempts = attemptsMap.values.sumOf { it.size }
                var attemptCounter = 0

                attemptsMap.forEach { (parentPath, attempts) ->
                    val parentFilename = File(parentPath).name
                    val attemptsList = mutableListOf<AttemptBackupEntry>()

                    attempts.forEach { attempt ->
                        attemptCounter++
                        val attemptFile = File(attempt.attemptFilePath)
                        var reversedFilename: String? = null

                        // Emit progress
                        _exportProgress.value = BackupProgress.InProgress(
                            phase = BackupPhase.EXPORTING_ATTEMPTS,
                            currentItem = attemptCounter,
                            totalItems = totalAttempts,
                            currentFileName = attemptFile.name,
                            message = "Exporting attempt $attemptCounter/$totalAttempts"
                        )

                        // Export attempt file
                        if (attemptFile.exists()) {
                            val attemptFilename = attemptFile.name
                            if (attemptFilename !in addedFiles) {
                                zipOut.putNextEntry(ZipEntry("$ATTEMPTS_PATH$attemptFilename"))
                                FileInputStream(attemptFile).use { it.copyTo(zipOut) }
                                zipOut.closeEntry()
                                addedFiles.add(attemptFilename)
                                totalSizeBytes += attemptFile.length()
                            }

                            // Export reversed attempt file if exists
                            attempt.reversedAttemptFilePath?.let { reversedPath ->
                                val reversedFile = File(reversedPath)
                                if (reversedFile.exists()) {
                                    reversedFilename = reversedFile.name
                                    if (reversedFilename !in addedFiles) {
                                        zipOut.putNextEntry(ZipEntry("$ATTEMPTS_PATH$reversedFilename"))
                                        FileInputStream(reversedFile).use { it.copyTo(zipOut) }
                                        zipOut.closeEntry()
                                        addedFiles.add(reversedFilename)
                                        totalSizeBytes += reversedFile.length()
                                    }
                                }
                            }

                            attemptsList.add(
                                AttemptBackupEntry(
                                    parentRecordingFilename = parentFilename,
                                    attemptFilename = attemptFile.name,
                                    reversedAttemptFilename = reversedFilename,  // ✅ FIXED
                                    hash = calculateFileHash(attemptFile),
                                    metadata = attemptToBackupMetadata(attempt)
                                )
                            )
                        }
                    }
                    if (attemptsList.isNotEmpty()) {
                        attemptEntriesMap[parentFilename] = attemptsList
                    }
                }

                // --- MANIFEST ---
                _exportProgress.value = BackupProgress.InProgress(
                    phase = BackupPhase.CREATING_MANIFEST,
                    currentItem = 1,
                    totalItems = 1,
                    currentFileName = MANIFEST_FILENAME,
                    message = "Creating backup manifest..."
                )

                val packageInfo: PackageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                val summary = BackupSummary(recordingEntries.size, attemptEntriesMap.values.sumOf { it.size }, totalSizeBytes, null, null, customNamesMap.isNotEmpty())

                val manifest = BackupManifestV2(
                    version = "2.3",
                    exportTimestampMs = System.currentTimeMillis(),
                    appVersionName = packageInfo.versionName ?: "1.0",
                    appVersionCode = packageInfo.versionCode,
                    dateRange = dateRange,
                    summary = summary,
                    recordings = recordingEntries,
                    attempts = attemptEntriesMap,
                    customNames = customNamesMap
                )

                zipOut.putNextEntry(ZipEntry(MANIFEST_FILENAME))
                zipOut.write(gson.toJson(manifest).toByteArray())
                zipOut.closeEntry()
            }

            _exportProgress.value = BackupProgress.Complete(
                message = "Export complete!",
                recordingsProcessed = recordingEntries.size,
                attemptsProcessed = attemptEntriesMap.values.sumOf { it.size }
            )

            BackupResult(true, zipFile, recordingEntries.size, attemptEntriesMap.values.sumOf { it.size }, totalSizeBytes)
        } catch (e: Exception) {
            _exportProgress.value = BackupProgress.Error("Export failed: ${e.message}")
            throw e
        }
    }

    // ============================================================
    //  IMPORT
    // ============================================================

    suspend fun importBackup(
        backupZipFile: File,
        conflictStrategy: ConflictStrategy = ConflictStrategy.SKIP_DUPLICATES
    ): RestoreResult = withContext(Dispatchers.IO) {
        try {
            _importProgress.value = BackupProgress.InProgress(
                phase = BackupPhase.ANALYZING,
                currentItem = 0,
                totalItems = 1,
                currentFileName = backupZipFile.name,
                message = "Validating backup file..."
            )

            if (!securityUtils.isValidZipFile(backupZipFile)) {
                _importProgress.value = BackupProgress.Error("Invalid zip file")
                return@withContext RestoreResult(false, 0, 0, 0, 0, "Invalid Zip")
            }

            if (!securityUtils.isReasonableBackupSize(backupZipFile)) {
                _importProgress.value = BackupProgress.Error("Backup file too large (max 500MB)")
                return@withContext RestoreResult(false, 0, 0, 0, 0, "File Too Large")
            }

            val manifest = extractManifest(backupZipFile)
            if (manifest == null) {
                _importProgress.value = BackupProgress.Error("Invalid manifest")
                return@withContext RestoreResult(false, 0, 0, 0, 0, "Invalid Manifest")
            }

            val totalFiles = manifest.recordings.size + manifest.attempts.values.sumOf { it.size }
            _importProgress.value = BackupProgress.InProgress(
                phase = BackupPhase.EXTRACTING,
                currentItem = 0,
                totalItems = totalFiles,
                currentFileName = "",
                message = "Preparing to import $totalFiles files..."
            )

            val recordingsDir = getRecordingsDir()
            val attemptsDir = getAttemptsDir()

            val existingAttemptsMap = threadSafeJsonRepo.loadAttemptsJson().toMutableMap()
            val existingCustomNames = threadSafeJsonRepo.loadRecordingNamesJson().toMutableMap()

            var importedRecs = 0
            var skippedRecs = 0
            var importedAttempts = 0
            var restoredNames = 0

            // Track mapping from backup filename -> actual installed filename (handles renames)
            val filenameMapping = mutableMapOf<String, String>()
            // Track renamed reversed files (backup reversed name -> actual reversed name)
            val reversedRenameMapping = mutableMapOf<String, String>()

            ZipInputStream(FileInputStream(backupZipFile)).use { zipIn ->
                var entry = zipIn.nextEntry
                while (entry != null) {
                    val entryName = entry.name

                    // Validate path strictly using local recordings dir as root
                    val targetRoot = if (entryName.startsWith(ATTEMPTS_PATH)) attemptsDir else recordingsDir
                    try { securityUtils.validateZipEntryStrict(entry, targetRoot) }
                    catch (e: Exception) {
                        Log.w(TAG, "Skipping suspicious entry: $entryName")
                        zipIn.closeEntry(); entry = zipIn.nextEntry; continue
                    }

                    when {
                        entryName == MANIFEST_FILENAME -> { /* Skip */ }

                        entryName.startsWith(RECORDINGS_PATH) -> {
                            // Logic: Extract Recording
                            if (conflictStrategy != ConflictStrategy.MERGE_ATTEMPTS_ONLY) {
                                val filename = entryName.removePrefix(RECORDINGS_PATH)

                                // Check if this is a reversed file (handled separately)
                                val isReversedFile = filename.contains("_reversed")

                                if (isReversedFile) {
                                    // Reversed files: extract with rename if parent was renamed
                                    // Derive parent name: "song_reversed.wav" -> "song.wav"
                                    val parentFilename = filename.replace("_reversed.wav", ".wav")
                                    val actualParentName = filenameMapping[parentFilename]

                                    // Compute target name based on parent's actual name
                                    val targetFilename = if (actualParentName != null && actualParentName != parentFilename) {
                                        // Parent was renamed, match it: "song (1).wav" -> "song (1)_reversed.wav"
                                        actualParentName.replace(".wav", "_reversed.wav")
                                    } else {
                                        filename
                                    }

                                    val targetFile = File(recordingsDir, targetFilename)
                                    if (!targetFile.exists()) {
                                        FileOutputStream(targetFile).use { zipIn.copyTo(it) }
                                        // Restore original timestamp (use parent's timestamp)
                                        manifest.recordings.find { it.filename == parentFilename }?.let { entry ->
                                            targetFile.setLastModified(entry.creationTimestampMs)
                                        }
                                        Log.d(TAG, "Extracted reversed recording: $filename -> $targetFilename")
                                    } else {
                                        Log.d(TAG, "Reversed recording already exists: $targetFilename")
                                    }
                                } else {
                                    // Original recordings: full conflict detection logic

                                    // Emit progress
                                    _importProgress.value = BackupProgress.InProgress(
                                        phase = BackupPhase.IMPORTING_RECORDINGS,
                                        currentItem = importedRecs + skippedRecs + 1,
                                        totalItems = manifest.recordings.size,
                                        currentFileName = filename,
                                        message = "Importing recordings..."
                                    )

                                    val targetFile = File(recordingsDir, filename)

                                    if (targetFile.exists()) {
                                        val localHash = calculateFileHash(targetFile)
                                        val manifestHash = manifest.recordings.find { it.filename == filename }?.hash

                                        if (localHash == manifestHash) {
                                            // Exact duplicate - skip
                                            skippedRecs++
                                            filenameMapping[filename] = filename
                                            Log.d(TAG, "Skipped duplicate recording: $filename")
                                        } else {
                                            // Conflict: Same filename, different content
                                            when (conflictStrategy) {
                                                ConflictStrategy.KEEP_BOTH -> {
                                                    // Generate unique name and import
                                                    val uniqueName = generateUniqueName(filename, recordingsDir)
                                                    val uniqueFile = File(recordingsDir, uniqueName)
                                                    FileOutputStream(uniqueFile).use { zipIn.copyTo(it) }
                                                    // Restore original timestamp for correct sort order
                                                    manifest.recordings.find { it.filename == filename }?.let { entry ->
                                                        uniqueFile.setLastModified(entry.creationTimestampMs)
                                                    }
                                                    filenameMapping[filename] = uniqueName
                                                    importedRecs++
                                                    Log.d(TAG, "Renamed recording: $filename -> $uniqueName")
                                                }
                                                else -> {
                                                    // SKIP_DUPLICATES or MERGE_ATTEMPTS_ONLY
                                                    skippedRecs++
                                                    filenameMapping[filename] = filename
                                                    Log.d(TAG, "Skipped conflicting recording: $filename")
                                                }
                                            }
                                        }
                                    } else {
                                        // New file - import normally
                                        FileOutputStream(targetFile).use { zipIn.copyTo(it) }
                                        // Restore original timestamp for correct sort order
                                        manifest.recordings.find { it.filename == filename }?.let { entry ->
                                            targetFile.setLastModified(entry.creationTimestampMs)
                                        }
                                        filenameMapping[filename] = filename
                                        importedRecs++
                                        Log.d(TAG, "Imported new recording: $filename")
                                    }
                                }
                            }
                        }

                        entryName.startsWith(ATTEMPTS_PATH) -> {
                            val filename = entryName.removePrefix(ATTEMPTS_PATH)

                            // Only process forward attempt files (not reversed files)
                            val attemptEntry = manifest.attempts.values.flatten().find { it.attemptFilename == filename }

                            if (attemptEntry != null) {
                                // Emit progress
                                _importProgress.value = BackupProgress.InProgress(
                                    phase = BackupPhase.IMPORTING_ATTEMPTS,
                                    currentItem = importedAttempts + 1,
                                    totalItems = manifest.attempts.values.sumOf { it.size },
                                    currentFileName = filename,
                                    message = "Importing attempts..."
                                )

                                // Get the actual parent recording filename (may be renamed)
                                val parentBackupName = attemptEntry.parentRecordingFilename
                                val parentActualName = filenameMapping[parentBackupName] ?: parentBackupName
                                val parentLocalPath = File(recordingsDir, parentActualName).absolutePath

                                // Determine target filename for attempt
                                val targetFile = File(attemptsDir, filename)
                                val finalAttemptFile: File
                                val finalAttemptFilename: String
                                var fileWasRestored = false  // Track physical file restoration

                                if (targetFile.exists()) {
                                    val localHash = calculateFileHash(targetFile)

                                    if (localHash == attemptEntry.hash) {
                                        // Exact duplicate - skip extraction but may need to add to JSON
                                        finalAttemptFile = targetFile
                                        finalAttemptFilename = filename
                                        Log.d(TAG, "Attempt file already exists (same content): $filename")
                                    } else {
                                        // Different content - conflict
                                        if (conflictStrategy == ConflictStrategy.KEEP_BOTH) {
                                            // Rename the attempt
                                            val uniqueName = generateUniqueName(filename, attemptsDir)
                                            finalAttemptFile = File(attemptsDir, uniqueName)
                                            FileOutputStream(finalAttemptFile).use { zipIn.copyTo(it) }
                                            finalAttemptFilename = uniqueName
                                            fileWasRestored = true
                                            Log.d(TAG, "Renamed attempt: $filename -> $uniqueName")

                                            // Handle reversed attempt with matching rename
                                            attemptEntry.reversedAttemptFilename?.let { revName ->
                                                val uniqueRevName = uniqueName.replace(".wav", "_reversed.wav")
                                                // STORE MAPPING: Tell the reversed file entry to use the new name
                                                reversedRenameMapping[revName] = uniqueRevName
                                                Log.d(TAG, "Mapped reversed file: $revName -> $uniqueRevName")
                                            }
                                        } else {
                                            // SKIP or MERGE - skip this conflicting attempt
                                            finalAttemptFile = targetFile
                                            finalAttemptFilename = filename
                                            Log.d(TAG, "Skipped conflicting attempt: $filename")
                                            zipIn.closeEntry()
                                            entry = zipIn.nextEntry
                                            continue
                                        }
                                    }
                                } else {
                                    // New file - extract normally
                                    FileOutputStream(targetFile).use { zipIn.copyTo(it) }
                                    finalAttemptFile = targetFile
                                    finalAttemptFilename = filename
                                    fileWasRestored = true
                                    Log.d(TAG, "Extracted new attempt: $filename")
                                }

                                // Check if this attempt already exists in JSON
                                val existingAttempts = existingAttemptsMap[parentLocalPath] ?: emptyList()
                                val alreadyExists = existingAttempts.any { it.attemptFilePath == finalAttemptFile.absolutePath }

                                if (!alreadyExists) {
                                    // Reconstruct reversed path if it exists
                                    val reversedLocalPath = attemptEntry.reversedAttemptFilename?.let { revName ->
                                        // Use the same rename pattern as the forward file
                                        val actualRevName = if (finalAttemptFilename != filename) {
                                            finalAttemptFilename.replace(".wav", "_reversed.wav")
                                        } else {
                                            revName
                                        }
                                        File(attemptsDir, actualRevName).absolutePath
                                    }

                                    val playerAttempt = metadataToPlayerAttempt(
                                        attemptEntry.metadata,
                                        finalAttemptFile.absolutePath,
                                        reversedLocalPath
                                    )

                                    existingAttemptsMap.compute(parentLocalPath) { _, list ->
                                        (list ?: emptyList()) + playerAttempt
                                    }
                                    importedAttempts++
                                    Log.d(TAG, "Added attempt to JSON: $finalAttemptFilename -> parent: $parentActualName")
                                } else if (fileWasRestored) {
                                    // FIX: Count file restoration even when JSON entry already existed
                                    importedAttempts++
                                    Log.d(TAG, "Restored missing file for existing JSON entry: $finalAttemptFilename")
                                } else {
                                    Log.d(TAG, "Attempt already in JSON, skipped: $finalAttemptFilename")
                                }
                            } else {
                                // This is likely a reversed file - check if it needs renaming
                                // CHECK MAPPING: Did the forward file trigger a rename?
                                val targetFilename = reversedRenameMapping[filename] ?: filename
                                val targetFile = File(attemptsDir, targetFilename)

                                if (!targetFile.exists()) {
                                    FileOutputStream(targetFile).use { zipIn.copyTo(it) }
                                    Log.d(TAG, "Extracted reversed attempt file: $targetFilename")
                                } else {
                                    Log.d(TAG, "Reversed file already exists: $targetFilename")
                                }
                            }
                        }
                    }
                    zipIn.closeEntry()
                    entry = zipIn.nextEntry
                }
            }

            // Restore Names (using filename mapping for renamed recordings)
            manifest.customNames.forEach { (backupFilename, name) ->
                val actualFilename = filenameMapping[backupFilename] ?: backupFilename
                val localPath = File(recordingsDir, actualFilename).absolutePath
                existingCustomNames[localPath] = name
                restoredNames++
                if (actualFilename != backupFilename) {
                    Log.d(TAG, "Restored custom name for renamed recording: $backupFilename -> $actualFilename")
                }
            }

            // Save metadata
            _importProgress.value = BackupProgress.InProgress(
                phase = BackupPhase.MERGING_METADATA,
                currentItem = 1,
                totalItems = 1,
                currentFileName = "attempts.json",
                message = "Saving metadata..."
            )

            threadSafeJsonRepo.saveAttemptsJson(existingAttemptsMap)
            threadSafeJsonRepo.saveRecordingNamesJson(existingCustomNames)

            _importProgress.value = BackupProgress.Complete(
                message = "Import complete!",
                recordingsProcessed = importedRecs,
                attemptsProcessed = importedAttempts
            )

            RestoreResult(true, importedRecs, skippedRecs, importedAttempts, restoredNames)

        } catch (e: Exception) {
            Log.e(TAG, "Import failed", e)
            _importProgress.value = BackupProgress.Error("Import failed: ${e.message}")
            RestoreResult(false, 0, 0, 0, 0, e.message)
        }
    }

    // ============================================================
    //  WIZARD SUPPORT - ANALYSIS
    // ============================================================

    /**
     * Analyze backup before importing - categorize what will happen.
     * Includes Gemini's fix: checks local files for parent recordings.
     */
    suspend fun analyzeBackup(backupZipFile: File): ImportAnalysis? = withContext(Dispatchers.IO) {
        try {
            if (!securityUtils.isValidZipFile(backupZipFile)) {
                Log.e(TAG, "Invalid zip file")
                return@withContext null
            }

            val manifest = extractManifest(backupZipFile)
            if (manifest == null) {
                Log.e(TAG, "Invalid manifest")
                return@withContext null
            }

            val recordingsDir = getRecordingsDir()
            val attemptsDir = getAttemptsDir()

            // Categorize recordings
            val newRecordings = mutableListOf<RecordingBackupEntry>()
            val duplicateRecordings = mutableListOf<RecordingBackupEntry>()
            val conflictingRecordings = mutableListOf<RecordingBackupEntry>()

            manifest.recordings.forEach { backupRec ->
                val localFile = File(recordingsDir, backupRec.filename)

                if (!localFile.exists()) {
                    newRecordings.add(backupRec)
                } else {
                    val localHash = calculateFileHash(localFile)
                    if (localHash == backupRec.hash) {
                        duplicateRecordings.add(backupRec)
                    } else {
                        conflictingRecordings.add(backupRec)
                    }
                }
            }

            // Categorize attempts
            val newAttempts = mutableListOf<AttemptBackupEntry>()
            val duplicateAttempts = mutableListOf<AttemptBackupEntry>()
            val conflictingAttempts = mutableListOf<AttemptBackupEntry>()
            val orphanedAttempts = mutableListOf<AttemptBackupEntry>()

            manifest.attempts.values.flatten().forEach { backupAttempt ->
                val attemptFile = File(attemptsDir, backupAttempt.attemptFilename)

                // GEMINI'S FIX: Check backup AND local for parent
                val parentInBackup = manifest.recordings.any {
                    it.filename == backupAttempt.parentRecordingFilename
                }
                val parentLocalFile = File(recordingsDir, backupAttempt.parentRecordingFilename)
                val parentExistsLocally = parentLocalFile.exists()

                if (!parentInBackup && !parentExistsLocally) {
                    orphanedAttempts.add(backupAttempt)
                    Log.d(TAG, "Orphaned: ${backupAttempt.attemptFilename}")
                    return@forEach
                }

                if (!attemptFile.exists()) {
                    newAttempts.add(backupAttempt)
                } else {
                    val localHash = calculateFileHash(attemptFile)
                    if (localHash == backupAttempt.hash) {
                        duplicateAttempts.add(backupAttempt)
                    } else {
                        conflictingAttempts.add(backupAttempt)
                    }
                }
            }

            val timestamps = manifest.recordings.map { it.creationTimestampMs }
            val dateRange = if (timestamps.isNotEmpty()) {
                Pair(timestamps.minOrNull() ?: 0L, timestamps.maxOrNull() ?: 0L)
            } else null

            ImportAnalysis(
                manifest = manifest,
                newRecordings = newRecordings,
                duplicateRecordings = duplicateRecordings,
                conflictingRecordings = conflictingRecordings,
                newAttempts = newAttempts,
                duplicateAttempts = duplicateAttempts,
                conflictingAttempts = conflictingAttempts,
                orphanedAttempts = orphanedAttempts,
                totalSizeBytes = manifest.summary.totalAudioFileSizeBytes,
                dateRange = dateRange
            )
        } catch (e: Exception) {
            Log.e(TAG, "Analysis failed", e)
            null
        }
    }

    /**
     * Filter analysis by date range.
     * Note: Filters by recording date (not attempt date).
     */
    fun filterAnalysisByDate(
        analysis: ImportAnalysis,
        fromMs: Long,
        toMs: Long
    ): ImportAnalysis {
        val filteredRecordings = analysis.manifest.recordings.filter {
            it.creationTimestampMs in fromMs..toMs
        }
        val filteredFilenames = filteredRecordings.map { it.filename }.toSet()

        val filteredAttempts = analysis.manifest.attempts
            .filterKeys { it in filteredFilenames }
            .values.flatten()

        val newRecs = analysis.newRecordings.filter { it in filteredRecordings }
        val dupRecs = analysis.duplicateRecordings.filter { it in filteredRecordings }
        val conflictRecs = analysis.conflictingRecordings.filter { it in filteredRecordings }

        val newAtts = analysis.newAttempts.filter { it in filteredAttempts }
        val dupAtts = analysis.duplicateAttempts.filter { it in filteredAttempts }
        val conflictAtts = analysis.conflictingAttempts.filter { it in filteredAttempts }
        val orphanAtts = analysis.orphanedAttempts.filter { it in filteredAttempts }

        val filteredSizeBytes = filteredRecordings.sumOf { it.fileSizeBytes }

        return analysis.copy(
            newRecordings = newRecs,
            duplicateRecordings = dupRecs,
            conflictingRecordings = conflictRecs,
            newAttempts = newAtts,
            duplicateAttempts = dupAtts,
            conflictingAttempts = conflictAtts,
            orphanedAttempts = orphanAtts,
            totalSizeBytes = filteredSizeBytes,
            dateRange = Pair(fromMs, toMs)
        )
    }

    private fun extractManifest(file: File): BackupManifestV2? {
        ZipInputStream(FileInputStream(file)).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                if (entry.name == MANIFEST_FILENAME) {
                    return gson.fromJson(zip.readBytes().toString(Charsets.UTF_8), BackupManifestV2::class.java)
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }
        return null
    }

    /**
     * Generate a unique filename with (N) suffix pattern.
     * Example: "recording.wav" -> "recording (1).wav" -> "recording (2).wav"
     */
    private fun generateUniqueName(originalName: String, dir: File): String {
        val extension = ".wav"
        val baseName = originalName.removeSuffix(extension).removeSuffix("_reversed")

        // Check if original name is available
        if (!File(dir, originalName).exists()) {
            return originalName
        }

        // Try (1), (2), (3), etc.
        var counter = 1
        var newName = "$baseName ($counter)$extension"

        while (File(dir, newName).exists()) {
            counter++
            newName = "$baseName ($counter)$extension"
        }

        return newName
    }

    private fun calculateFileHash(file: File): String {
        return "${file.length()}_${file.lastModified()}"
    }

    // ============================================================
    //  PRIVATE CONVERSION HELPERS (Moved inside class to fix scope)
    // ============================================================

    private fun attemptToBackupMetadata(attempt: PlayerAttempt): AttemptMetadataBackup =
        AttemptMetadataBackup(
            playerName = attempt.playerName,
            score = attempt.score,
            challengeType = attempt.challengeType.toBackupString(),
            difficulty = attempt.difficulty.toBackupString(),
            feedback = attempt.feedback,
            isGarbage = attempt.isGarbage,
            vocalAnalysis = attempt.vocalAnalysis?.toBackup(),
            performanceInsights = attempt.performanceInsights?.toBackup(),
            debuggingData = attempt.debuggingData?.toBackup(),
            // v2.2: Scorecard fields
            finalScore = attempt.finalScore,
            attemptTranscription = attempt.attemptTranscription,
            targetPhonemes = attempt.targetPhonemes,
            attemptPhonemes = attempt.attemptPhonemes,
            phonemeMatches = attempt.phonemeMatches,
            targetWordPhonemes = attempt.targetWordPhonemes.map { it.toBackup() },
            attemptWordPhonemes = attempt.attemptWordPhonemes.map { it.toBackup() },
            durationRatio = attempt.durationRatio,
            wordAccuracy = attempt.wordAccuracy,
            // v2.3: Score breakdown
            scoreBreakdown = attempt.scoreBreakdown?.toBackup()
        )

    private fun metadataToPlayerAttempt(
        metadata: AttemptMetadataBackup,
        attemptFilePath: String,
        reversedAttemptFilePath: String?
    ): PlayerAttempt =
        PlayerAttempt(
            playerName = metadata.playerName,
            attemptFilePath = attemptFilePath,
            reversedAttemptFilePath = reversedAttemptFilePath,
            score = metadata.score,
            challengeType = ChallengeType.valueOf(metadata.challengeType),
            difficulty = DifficultyLevel.valueOf(metadata.difficulty),
            feedback = metadata.feedback,
            isGarbage = metadata.isGarbage,
            vocalAnalysis = null, // TODO: Restore if needed
            performanceInsights = null,
            debuggingData = null,
            // v2.2: Scorecard fields restored
            finalScore = metadata.finalScore,
            attemptTranscription = metadata.attemptTranscription,
            targetPhonemes = metadata.targetPhonemes,
            attemptPhonemes = metadata.attemptPhonemes,
            phonemeMatches = metadata.phonemeMatches,
            targetWordPhonemes = metadata.targetWordPhonemes.map { it.toWordPhonemes() },
            attemptWordPhonemes = metadata.attemptWordPhonemes.map { it.toWordPhonemes() },
            durationRatio = metadata.durationRatio,
            wordAccuracy = metadata.wordAccuracy,
            // v2.3: Score breakdown restored
            scoreBreakdown = metadata.scoreBreakdown?.toScoreBreakdown()
        )
}