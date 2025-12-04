package com.quokkalabs.reversey.ui.viewmodels

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quokkalabs.reversey.data.backup.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * Import Wizard ViewModel - State machine for backup restore flow.
 *
 * WIZARD STEPS:
 * 1. FILE_SELECT - Pick backup zip file
 * 2. ANALYSIS - Show what's inside with date filter + checkboxes
 * 3. CONFLICTS - Resolve conflicts (Keep Mine / Keep Both / Use Backup)
 * 4. IMPORTING - Progress bar with phase messages
 * 5. COMPLETE - Summary of what was imported
 */
@HiltViewModel
class ImportWizardViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val backupManager: BackupManager
) : ViewModel() {

    companion object {
        private const val TAG = "ImportWizardVM"
    }

    // ============================================================
    //  WIZARD STATE
    // ============================================================

    sealed class WizardStep {
        object FileSelect : WizardStep()
        object Analyzing : WizardStep()
        object Analysis : WizardStep()
        object Conflicts : WizardStep()
        object Importing : WizardStep()
        data class Complete(val result: RestoreResult) : WizardStep()
        data class Error(val message: String) : WizardStep()
    }

    data class WizardState(
        val step: WizardStep = WizardStep.FileSelect,
        val selectedFileUri: Uri? = null,
        val selectedFileName: String? = null,
        val analysis: ImportAnalysis? = null,
        val filteredAnalysis: ImportAnalysis? = null,

        // Date filtering
        val dateFilterStart: Long? = null,
        val dateFilterEnd: Long? = null,
        val activeDateChips: Set<String> = emptySet(),

        // Item selection (keyed by filename)
        val selectedNewRecordings: Set<String> = emptySet(),
        val selectedConflicts: Set<String> = emptySet(),

        // Conflict resolution
        val conflictStrategy: ConflictStrategy = ConflictStrategy.SKIP_DUPLICATES,
        val perItemConflictStrategy: Map<String, ConflictStrategy> = emptyMap(),

        // Progress
        val importProgress: BackupProgress = BackupProgress.Idle,

        // Results
        val importResult: RestoreResult? = null
    )

    private val _state = MutableStateFlow(WizardState())
    val state: StateFlow<WizardState> = _state.asStateFlow()

    // ============================================================
    //  FILE SELECTION
    // ============================================================

    fun selectBackupFile(uri: Uri, fileName: String?) {
        _state.value = _state.value.copy(
            selectedFileUri = uri,
            selectedFileName = fileName ?: "backup.zip",
            step = WizardStep.Analyzing
        )
        analyzeBackup(uri)
    }

    private fun analyzeBackup(uri: Uri) {
        viewModelScope.launch {
            try {
                val tempFile = withContext(Dispatchers.IO) {
                    val temp = File(context.cacheDir, "analyze_temp.zip")
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        FileOutputStream(temp).use { output ->
                            input.copyTo(output)
                        }
                    }
                    temp
                }

                val analysis = backupManager.analyzeBackup(tempFile)
                tempFile.delete()

                if (analysis != null) {
                    // Pre-select all new recordings
                    val selectedNew = analysis.newRecordings.map { it.filename }.toSet()
                    val selectedConflicts = analysis.conflictingRecordings.map { it.filename }.toSet()

                    _state.value = _state.value.copy(
                        step = WizardStep.Analysis,
                        analysis = analysis,
                        filteredAnalysis = analysis,
                        selectedNewRecordings = selectedNew,
                        selectedConflicts = selectedConflicts
                    )
                } else {
                    _state.value = _state.value.copy(
                        step = WizardStep.Error("Failed to analyze backup file")
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Analysis failed", e)
                _state.value = _state.value.copy(
                    step = WizardStep.Error("Error: ${e.message}")
                )
            }
        }
    }

    // ============================================================
    //  DATE FILTERING
    // ============================================================

    fun setDateFilter(startMs: Long?, endMs: Long?, chipLabel: String?) {
        val analysis = _state.value.analysis ?: return

        val filtered = if (startMs != null && endMs != null) {
            backupManager.filterAnalysisByDate(analysis, startMs, endMs)
        } else {
            analysis
        }

        val chips = if (chipLabel != null) setOf(chipLabel) else emptySet()

        // Update selected items based on filtered results
        val selectedNew = filtered.newRecordings.map { it.filename }.toSet()
        val selectedConflicts = filtered.conflictingRecordings.map { it.filename }.toSet()

        _state.value = _state.value.copy(
            dateFilterStart = startMs,
            dateFilterEnd = endMs,
            activeDateChips = chips,
            filteredAnalysis = filtered,
            selectedNewRecordings = selectedNew,
            selectedConflicts = selectedConflicts
        )
    }

    fun clearDateFilter() {
        val analysis = _state.value.analysis ?: return
        val selectedNew = analysis.newRecordings.map { it.filename }.toSet()
        val selectedConflicts = analysis.conflictingRecordings.map { it.filename }.toSet()

        _state.value = _state.value.copy(
            dateFilterStart = null,
            dateFilterEnd = null,
            activeDateChips = emptySet(),
            filteredAnalysis = analysis,
            selectedNewRecordings = selectedNew,
            selectedConflicts = selectedConflicts
        )
    }

    // ============================================================
    //  ITEM SELECTION
    // ============================================================

    fun toggleNewRecording(filename: String) {
        val current = _state.value.selectedNewRecordings
        val updated = if (filename in current) {
            current - filename
        } else {
            current + filename
        }
        _state.value = _state.value.copy(selectedNewRecordings = updated)
    }

    fun toggleConflict(filename: String) {
        val current = _state.value.selectedConflicts
        val updated = if (filename in current) {
            current - filename
        } else {
            current + filename
        }
        _state.value = _state.value.copy(selectedConflicts = updated)
    }

    fun selectAllNew() {
        val all = _state.value.filteredAnalysis?.newRecordings?.map { it.filename }?.toSet() ?: emptySet()
        _state.value = _state.value.copy(selectedNewRecordings = all)
    }

    fun deselectAllNew() {
        _state.value = _state.value.copy(selectedNewRecordings = emptySet())
    }

    // ============================================================
    //  CONFLICT RESOLUTION
    // ============================================================

    fun setGlobalConflictStrategy(strategy: ConflictStrategy) {
        _state.value = _state.value.copy(conflictStrategy = strategy)
    }

    fun setPerItemConflictStrategy(filename: String, strategy: ConflictStrategy) {
        val updated = _state.value.perItemConflictStrategy + (filename to strategy)
        _state.value = _state.value.copy(perItemConflictStrategy = updated)
    }

    fun proceedToConflicts() {
        val hasConflicts = (_state.value.filteredAnalysis?.conflictingRecordings?.size ?: 0) > 0
        if (hasConflicts && _state.value.selectedConflicts.isNotEmpty()) {
            _state.value = _state.value.copy(step = WizardStep.Conflicts)
        } else {
            // Skip conflicts step if none selected
            startImport()
        }
    }

    // ============================================================
    //  IMPORT EXECUTION
    // ============================================================

    fun startImport() {
        _state.value = _state.value.copy(step = WizardStep.Importing)

        viewModelScope.launch {
            try {
                val uri = _state.value.selectedFileUri ?: throw Exception("No file selected")

                val tempFile = withContext(Dispatchers.IO) {
                    val temp = File(context.cacheDir, "import_temp.zip")
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        FileOutputStream(temp).use { output ->
                            input.copyTo(output)
                        }
                    }
                    temp
                }

                // Collect progress updates
                viewModelScope.launch {
                    backupManager.importProgress.collect { progress ->
                        _state.value = _state.value.copy(importProgress = progress)
                    }
                }

                val result = backupManager.importBackup(tempFile, _state.value.conflictStrategy)
                tempFile.delete()

                _state.value = _state.value.copy(
                    step = WizardStep.Complete(result),
                    importResult = result
                )

            } catch (e: Exception) {
                Log.e(TAG, "Import failed", e)
                _state.value = _state.value.copy(
                    step = WizardStep.Error("Import failed: ${e.message}")
                )
            }
        }
    }

    // ============================================================
    //  NAVIGATION
    // ============================================================

    fun goBack() {
        val currentStep = _state.value.step
        val newStep = when (currentStep) {
            is WizardStep.Analysis -> WizardStep.FileSelect
            is WizardStep.Conflicts -> WizardStep.Analysis
            is WizardStep.Complete -> WizardStep.FileSelect
            is WizardStep.Error -> WizardStep.FileSelect
            else -> currentStep
        }
        _state.value = _state.value.copy(step = newStep)
    }

    fun reset() {
        _state.value = WizardState()
    }

    // ============================================================
    //  HELPERS
    // ============================================================

    fun getDateChipOptions(): List<DateChipOption> {
        val analysis = _state.value.analysis ?: return listOf(
            DateChipOption("All", null, null)
        )

        val dateRange = analysis.dateRange
        val now = System.currentTimeMillis()
        val dayMs = 24 * 60 * 60 * 1000L

        // If no date range info, just show "All"
        if (dateRange == null) {
            return listOf(DateChipOption("All", null, null))
        }

        val (minDate, maxDate) = dateRange

        // Build list of options
        val options = mutableListOf(
            DateChipOption("All", null, null)
        )

        // Only add time-based filters if they would include some data
        val last7Days = now - 7 * dayMs
        if (last7Days <= maxDate && now >= minDate) {
            options.add(DateChipOption("Last 7 Days", last7Days, now))
        }

        val last30Days = now - 30 * dayMs
        if (last30Days <= maxDate && now >= minDate) {
            options.add(DateChipOption("Last 30 Days", last30Days, now))
        }

        val startOfYear = getStartOfYear()
        if (startOfYear <= maxDate && now >= minDate) {
            options.add(DateChipOption("This Year", startOfYear, now))
        }

        return options
    }

    private fun getStartOfYear(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.MONTH, Calendar.JANUARY)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun formatDate(timestampMs: Long): String {
        return SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date(timestampMs))
    }

    fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
        }
    }
}

data class DateChipOption(
    val label: String,
    val startMs: Long?,
    val endMs: Long?
)