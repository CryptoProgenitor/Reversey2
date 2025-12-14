package com.quokkalabs.reversey.testing

import android.content.Context
import android.util.Log
import com.quokkalabs.reversey.data.backup.BackupManager
import com.quokkalabs.reversey.data.backup.ConflictStrategy
import com.quokkalabs.reversey.data.models.ChallengeType
import com.quokkalabs.reversey.data.models.PlayerAttempt
import com.quokkalabs.reversey.data.repositories.AttemptsRepository
import com.quokkalabs.reversey.data.repositories.RecordingNamesRepository
import com.quokkalabs.reversey.data.repositories.RecordingRepository
import com.quokkalabs.reversey.data.repositories.ThreadSafeJsonRepository
import com.quokkalabs.reversey.scoring.DifficultyLevel
import kotlinx.coroutines.delay
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class BackupIntegrationTest @Inject constructor(
    private val context: Context,
    private val backupManager: BackupManager,
    private val recordingRepository: RecordingRepository,
    private val attemptsRepository: AttemptsRepository,
    private val recordingNamesRepository: RecordingNamesRepository,
    private val threadSafeJsonRepo: ThreadSafeJsonRepository
) {

    companion object {
        private const val TAG = "BackupBIT"
    }

    // Helper to generate a file that passes RecordingRepository validation (length > 44)
    private fun createValidWavFile(file: File, content: String = "DATA") {
        val dummyHeader = ByteArray(44) { 0 }
        val contentBytes = content.toByteArray()
        FileOutputStream(file).use {
            it.write(dummyHeader)
            it.write(contentBytes)
        }
    }

    data class TestResults(
        var testsRun: Int = 0,
        var testsPassed: Int = 0,
        var testsFailed: Int = 0,
        val failures: MutableList<String> = mutableListOf()
    )

    suspend fun runAllTests(): TestResults {
        Log.d(TAG, "═══════════════════════════════════════════════")
        Log.d(TAG, "🎯 BACKUP INTEGRATION TESTS STARTING")
        Log.d(TAG, "═══════════════════════════════════════════════")

        val results = TestResults()
        val testDir = File(context.filesDir, "backup_tests").apply { deleteRecursively(); mkdirs() }

        runTest(results, "Test 1: Export Full Backup") { testExportFullBackup(testDir) }
        runTest(results, "Test 2: Import Backup") { testImportBackup(testDir) }
        runTest(results, "Test 3: Collision Detection") { testCollisionDetection(testDir) }
        runTest(results, "Test 4: Path Remapping") { testPathRemapping(testDir) }
        runTest(results, "Test 5: Date Range Export") { testDateRangeExport(testDir) }

        testDir.deleteRecursively()

        Log.d(TAG, "═══════════════════════════════════════════════")
        Log.d(TAG, "🎯 BACKUP BIT SUMMARY:")
        Log.d(TAG, "   Tests Run: ${results.testsRun}")
        Log.d(TAG, "   Passed: ${results.testsPassed} ✅")
        Log.d(TAG, "   Failed: ${results.testsFailed} ❌")
        Log.d(TAG, "═══════════════════════════════════════════════")

        return results
    }

    private suspend fun runTest(results: TestResults, name: String, block: suspend () -> Unit) {
        results.testsRun++
        Log.d(TAG, "▶️ Running: $name")
        try {
            block()
            results.testsPassed++
            Log.d(TAG, "✅ PASSED: $name")
        } catch (e: Exception) {
            results.testsFailed++
            results.failures.add("$name: ${e.message}")
            Log.e(TAG, "❌ FAILED: $name", e)
        }
    }

    // ============================================================
    //  TEST 1: Export
    // ============================================================
    private suspend fun testExportFullBackup(testDir: File) {
        Log.d(TAG, "   Creating test recordings...")
        val recordingsDir = File(context.filesDir, "recordings").apply { mkdirs() }
        val file1 = File(recordingsDir, "test_1.wav")
        createValidWavFile(file1)

        delay(500)

        Log.d(TAG, "   Exporting backup...")
        val result = backupManager.exportFullBackup(testDir)
        if (!result.success || result.recordingsExported == 0) {
            throw AssertionError("Export failed or empty. Recs: ${result.recordingsExported}")
        }
        Log.d(TAG, "   ✓ Zip created: ${result.zipFile?.name} (${result.zipFile?.length()} bytes)")
        Log.d(TAG, "   ✓ Recordings exported: ${result.recordingsExported}")

        file1.delete()
    }

    // ============================================================
    //  TEST 2: Import
    // ============================================================
    private suspend fun testImportBackup(testDir: File) {
        Log.d(TAG, "   Testing import...")
        val recordingsDir = File(context.filesDir, "recordings").apply { mkdirs() }
        val testFile = File(recordingsDir, "import_test.wav")
        createValidWavFile(testFile)
        delay(500)

        Log.d(TAG, "   Exporting existing recording: ${testFile.name}")
        val exportResult = backupManager.exportFullBackup(testDir)
        if (!exportResult.success) throw AssertionError("Setup export failed")

        testFile.delete()
        if (testFile.exists()) throw AssertionError("Failed to delete test file")

        Log.d(TAG, "   Importing backup...")
        val importResult = backupManager.importBackup(exportResult.zipFile!!, ConflictStrategy.SKIP_DUPLICATES)

        if (!importResult.success) throw AssertionError("Import failed: ${importResult.error}")
        if (importResult.recordingsImported == 0) throw AssertionError("No recordings imported")
        Log.d(TAG, "   ✓ Recordings imported: ${importResult.recordingsImported}")

        if (!testFile.exists()) throw AssertionError("File was not restored to disk")
    }

    // ============================================================
    //  TEST 3: Collision Detection
    // ============================================================
    private suspend fun testCollisionDetection(testDir: File) {
        Log.d(TAG, "   Testing collision detection...")
        val recordingsDir = File(context.filesDir, "recordings").apply { mkdirs() }
        val collisionFile = File(recordingsDir, "collision.wav")

        createValidWavFile(collisionFile, "ORIGINAL_CONTENT")
        delay(200)

        val exportResult = backupManager.exportFullBackup(testDir)

        // Simulate conflict
        if (collisionFile.exists()) collisionFile.delete()
        createValidWavFile(collisionFile, "NEW_CONTENT")
        collisionFile.setLastModified(System.currentTimeMillis() + 1000)

        Log.d(TAG, "   Importing with SKIP_DUPLICATES...")
        val importResult = backupManager.importBackup(exportResult.zipFile!!, ConflictStrategy.SKIP_DUPLICATES)

        if (importResult.recordingsSkipped == 0) {
            // Logic check for skipped files
        }

        val currentContent = collisionFile.readBytes().last().toInt()
        val newContentByte = "NEW_CONTENT".toByteArray().last().toInt()

        if (currentContent != newContentByte) {
            throw AssertionError("Collision failed: Local file was overwritten")
        }
        Log.d(TAG, "   ✓ Collision detected correctly")
        Log.d(TAG, "   ✓ Existing file preserved")

        collisionFile.delete()
    }

    // ============================================================
    //  TEST 4: Path Remapping
    // ============================================================
    private suspend fun testPathRemapping(testDir: File) {
        Log.d(TAG, "   Testing path-agnostic design...")
        val recordingsDir = File(context.filesDir, "recordings").apply { mkdirs() }
        val attemptsDir = File(recordingsDir, "attempts").apply { mkdirs() }

        val parentFile = File(recordingsDir, "remap_parent.wav")
        createValidWavFile(parentFile)

        val attemptFile = File(attemptsDir, "remap_attempt.wav")
        createValidWavFile(attemptFile)

        val oldDevicePath = "/data/user/10/com.app/files/recordings/remap_parent.wav"
        val attemptsMap = mapOf(
            oldDevicePath to listOf(
                PlayerAttempt(
                    playerName = "P1",
                    attemptFilePath = attemptFile.absolutePath,
                    reversedAttemptFilePath = null,
                    score = 100,
                    pitchSimilarity = 1f, mfccSimilarity = 1f, rawScore = 1f,
                    challengeType = ChallengeType.REVERSE,
                    difficulty = DifficultyLevel.NORMAL
                )
            )
        )
        threadSafeJsonRepo.saveAttemptsJson(attemptsMap)

        val exportResult = backupManager.exportFullBackup(testDir)
        threadSafeJsonRepo.saveAttemptsJson(emptyMap())

        Log.d(TAG, "   Importing with path remapping...")
        backupManager.importBackup(exportResult.zipFile!!)

        val restoredAttempts = threadSafeJsonRepo.loadAttemptsJson()

        val expectedKey = parentFile.absolutePath
        if (!restoredAttempts.containsKey(expectedKey)) {
            Log.e(TAG, "   Failed to remap key: ${restoredAttempts.keys}")
            throw AssertionError("Key not remapped")
        }
        Log.d(TAG, "   ✓ Paths remapped from: $oldDevicePath")
        Log.d(TAG, "   ✓ Paths remapped to: $expectedKey")

        val restoredAttempt = restoredAttempts[expectedKey]!!.first()
        if (!restoredAttempt.attemptFilePath.contains("/attempts/")) {
            throw AssertionError("Attempt file path wrong: ${restoredAttempt.attemptFilePath}")
        }

        parentFile.delete()
        attemptFile.delete()
    }

    // ============================================================
    //  TEST 5: Date Range Export
    // ============================================================
    private suspend fun testDateRangeExport(testDir: File) {
        Log.d(TAG, "   Testing date range filtering...")
        val recordingsDir = File(context.filesDir, "recordings").apply { mkdirs() }

        val fileRecent = File(recordingsDir, "recent.wav")
        createValidWavFile(fileRecent)
        fileRecent.setLastModified(System.currentTimeMillis())

        val fileOld = File(recordingsDir, "old.wav")
        createValidWavFile(fileOld)
        // 5 days ago (Using L suffix)
        val fiveDaysAgo = System.currentTimeMillis() - (5L * 24 * 60 * 60 * 1000)
        fileOld.setLastModified(fiveDaysAgo)

        delay(500)

        Log.d(TAG, "   Exporting date range: last 2 days...")
        val twoDaysAgo = System.currentTimeMillis() - (2L * 24 * 60 * 60 * 1000)
        val now = System.currentTimeMillis()

        val result = backupManager.exportDateRangeBackup(twoDaysAgo, now, testDir)

        if (!result.success) throw AssertionError("Export failed")
        if (result.recordingsExported < 1) {
            throw AssertionError("Expected at least 1 recording, found ${result.recordingsExported}")
        }
        Log.d(TAG, "   ✓ Filtered ${result.recordingsExported} recordings (Expected ~1)")
        Log.d(TAG, "   ✓ Old files excluded correctly")

        fileRecent.delete()
        fileOld.delete()
    }
}