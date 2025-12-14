package com.quokkalabs.reversey.data.backup

import android.content.Context
import android.content.pm.PackageInfo
import android.util.Log
import com.quokkalabs.reversey.data.models.ChallengeType
import com.quokkalabs.reversey.data.models.PlayerAttempt
import com.quokkalabs.reversey.data.models.Recording
import com.quokkalabs.reversey.data.repositories.AttemptsRepository
import com.quokkalabs.reversey.data.repositories.RecordingNamesRepository
import com.quokkalabs.reversey.data.repositories.RecordingRepository
import com.quokkalabs.reversey.data.repositories.ThreadSafeJsonRepository
import com.quokkalabs.reversey.security.SecurityUtils
import com.quokkalabs.reversey.scoring.DifficultyLevel
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*
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

                        // Create Manifest Entry
                        recordingEntries.add(
                            RecordingBackupEntry(
                                filename = filename,
                                reversedFilename = null,
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
                    version = "2.1",
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
                        Log.w(TAG, "Skipping suspicious entry: $entryName");
                        zipIn.closeEntry(); entry = zipIn.nextEntry; continue
                    }

                    when {
                        entryName == MANIFEST_FILENAME -> { /* Skip */ }

                        entryName.startsWith(RECORDINGS_PATH) -> {
                            // Logic: Extract Recording
                            if (conflictStrategy != ConflictStrategy.MERGE_ATTEMPTS_ONLY) {
                                val filename = entryName.removePrefix(RECORDINGS_PATH)

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
                                    filenameMapping[filename] = filename
                                    importedRecs++
                                    Log.d(TAG, "Imported new recording: $filename")
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
            pitchSimilarity = attempt.pitchSimilarity,
            mfccSimilarity = attempt.mfccSimilarity,
            rawScore = attempt.rawScore,
            challengeType = attempt.challengeType.toBackupString(),
            difficulty = attempt.difficulty.toBackupString(),
            feedback = attempt.feedback,
            isGarbage = attempt.isGarbage,
            vocalAnalysis = attempt.vocalAnalysis?.toBackup(),
            performanceInsights = attempt.performanceInsights?.toBackup(),
            debuggingData = attempt.debuggingData?.toBackup()
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
            pitchSimilarity = metadata.pitchSimilarity,
            mfccSimilarity = metadata.mfccSimilarity,
            rawScore = metadata.rawScore,
            challengeType = ChallengeType.valueOf(metadata.challengeType),
            difficulty = DifficultyLevel.valueOf(metadata.difficulty),
            feedback = metadata.feedback,
            isGarbage = metadata.isGarbage,
            vocalAnalysis = null, // TODO: Restore if needed
            performanceInsights = null,
            debuggingData = null
        )
}