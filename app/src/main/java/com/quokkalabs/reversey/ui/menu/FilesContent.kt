package com.quokkalabs.reversey.ui.menu

import android.net.Uri
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.quokkalabs.reversey.data.backup.BackupManager
import com.quokkalabs.reversey.ui.viewmodels.AudioViewModel
import com.quokkalabs.reversey.ui.viewmodels.ImportWizardViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * FILES SCREEN - Backup, Restore, Import
 *
 * Main entry point for file management operations.
 * Routes to appropriate sub-screens based on user action.
 */
@Composable
fun FilesContent(
    backupManager: BackupManager,
    audioViewModel: AudioViewModel,
    incomingWavUri: Uri?,
    onNavigateBack: () -> Unit,
    onNavigateHome: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val wizardViewModel: ImportWizardViewModel = hiltViewModel()
    val wizardState by wizardViewModel.state.collectAsState()

    // Screen state
    var currentScreen by remember { mutableStateOf<FilesScreen>(FilesScreen.Menu) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var isExporting by remember { mutableStateOf(false) }
    var isExportingSelection by remember { mutableStateOf(false) }

    // Share dialog state for Back It Up
    var showBackupShareDialog by remember { mutableStateOf(false) }
    var backupExportedUri by remember { mutableStateOf<Uri?>(null) }
    var backupResultMessage by remember { mutableStateOf("") }

    // Handle incoming WAV file from external app
    LaunchedEffect(incomingWavUri) {
        if (incomingWavUri != null) {
            currentScreen = FilesScreen.SingleWavImport(incomingWavUri)
        }
    }

    // Export launcher
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val tempDir = File(context.cacheDir, "backup_temp").apply { mkdirs() }
                    val result = backupManager.exportFullBackup(tempDir)

                    if (result.success && result.zipFile != null) {
                        context.contentResolver.openOutputStream(uri)?.use { output ->
                            java.io.FileInputStream(result.zipFile).use { input ->
                                input.copyTo(output)
                            }
                        }

                        result.zipFile.delete()
                        result.zipFile.parentFile?.deleteRecursively()

                        // Show share dialog
                        backupExportedUri = uri
                        backupResultMessage = "${result.recordingsExported} recordings, ${result.attemptsExported} attempts"
                        showBackupShareDialog = true
                    } else {
                        Toast.makeText(context, "Export failed", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    isExporting = false
                }
            }
        } else {
            isExporting = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StaticMenuColors.backgroundGradient)
            .statusBarsPadding()
            .padding(20.dp)
    ) {
        when (val screen = currentScreen) {
            is FilesScreen.Menu -> {
                FilesMenuContent(
                    onNavigateBack = onNavigateBack,
                    onBackItUp = {
                        isExporting = true
                        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                        exportLauncher.launch("reversey_backup_$timestamp.zip")
                    },
                    onExportSelection = {
                        currentScreen = FilesScreen.ExportSelection
                    },
                    onRestoreBackup = {
                        wizardViewModel.reset()
                        currentScreen = FilesScreen.RestoreWizard
                    },
                    onAddRecording = {
                        currentScreen = FilesScreen.AddRecordingPicker
                    },
                    onStartFresh = {
                        showDeleteConfirmDialog = true
                    },
                    isExporting = isExporting,
                    isExportingSelection = isExportingSelection
                )
            }

            is FilesScreen.RestoreWizard -> {
                RestoreWizardContent(
                    wizardViewModel = wizardViewModel,
                    wizardState = wizardState,
                    onComplete = {
                        audioViewModel.loadRecordings()
                        currentScreen = FilesScreen.Menu
                        Toast.makeText(context, "Restore complete!", Toast.LENGTH_SHORT).show()
                    },
                    onBack = {
                        if (wizardState.step == ImportWizardViewModel.WizardStep.FileSelect) {
                            currentScreen = FilesScreen.Menu
                        } else {
                            wizardViewModel.goBack()
                        }
                    }
                )
            }

            is FilesScreen.AddRecordingPicker -> {
                AddRecordingPickerContent(
                    onFileSelected = { uri ->
                        currentScreen = FilesScreen.SingleWavImport(uri)
                    },
                    onBack = {
                        currentScreen = FilesScreen.Menu
                    }
                )
            }

            is FilesScreen.SingleWavImport -> {
                SingleWavImportDialog(
                    wavUri = screen.wavUri,
                    onImportComplete = { path, name ->
                        audioViewModel.loadRecordings()
                        Toast.makeText(context, "Added: $name", Toast.LENGTH_SHORT).show()
                        onNavigateHome()
                    },
                    onCancel = {
                        if (incomingWavUri != null) {
                            // User came from external app - close entirely
                            onNavigateBack()
                        } else {
                            currentScreen = FilesScreen.Menu
                        }

                    }
                )
            }

            is FilesScreen.ExportSelection -> {
                ExportSelectionContent(
                    audioViewModel = audioViewModel,
                    backupManager = backupManager,
                    onExportStarted = { isExportingSelection = true },
                    onExportComplete = { message ->
                        isExportingSelection = false
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        currentScreen = FilesScreen.Menu
                    },
                    onBack = {
                        currentScreen = FilesScreen.Menu
                    }
                )
            }

        }

        // Delete confirmation dialog
        if (showDeleteConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = false },
                title = {
                    Text(
                        "Start Fresh?",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        "This will delete ALL your recordings and attempts. Like, everything. Gone forever. No takesies-backsies. 😬",
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            audioViewModel.clearAllRecordings()
                            showDeleteConfirmDialog = false
                            Toast.makeText(context, "All gone! Fresh start 🌱", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEF4444)
                        )
                    ) {
                        Text("Delete Everything")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showDeleteConfirmDialog = false }) {
                        Text("Nope, keep it")
                    }
                }
            )
        }

        // Share dialog for Back It Up
        if (showBackupShareDialog) {
            AlertDialog(
                onDismissRequest = {
                    showBackupShareDialog = false
                    isExporting = false
                },
                title = {
                    Text(
                        "Backup Complete! ✅",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        "Exported $backupResultMessage",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            backupExportedUri?.let { uri ->
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/zip"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Backup"))
                            }
                            showBackupShareDialog = false
                            isExporting = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StaticMenuColors.toggleActive
                        )
                    ) {
                        Text("Share")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = {
                            showBackupShareDialog = false
                            isExporting = false
                        }
                    ) {
                        Text("Done")
                    }
                }
            )
        }
    }
}

// Screen navigation state
sealed class FilesScreen {
    object Menu : FilesScreen()
    object RestoreWizard : FilesScreen()
    object AddRecordingPicker : FilesScreen()
    object ExportSelection : FilesScreen()
    data class SingleWavImport(val wavUri: Uri) : FilesScreen()
}

// ============================================================
//  FILES MENU (Entry Point)
// ============================================================

@Composable
private fun FilesMenuContent(
    onNavigateBack: () -> Unit,
    onBackItUp: () -> Unit,
    onExportSelection: () -> Unit,
    onRestoreBackup: () -> Unit,
    onAddRecording: () -> Unit,
    onStartFresh: () -> Unit,
    isExporting: Boolean,
    isExportingSelection: Boolean
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Glass Header
        GlassHeader(
            title = "FILES",
            onBack = onNavigateBack
        )

        Spacer(modifier = Modifier.height(20.dp))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // SAVE Section
            SectionHeader("SAVE")

            MenuCard(
                emoji = "💾",
                title = "Back It Up",
                subtitle = if (isExporting) "Exporting..." else "Save everything to a zip",
                onClick = onBackItUp,
                enabled = !isExporting,
                showProgress = isExporting
            )

            MenuCard(
                emoji = "☑️",
                title = "Export Selection",
                subtitle = if (isExportingSelection) "Exporting..." else "Choose recordings to export",
                onClick = onExportSelection,
                enabled = !isExporting && !isExportingSelection,
                showProgress = isExportingSelection
            )

            // RESTORE Section
            SectionHeader("RESTORE")

            MenuCard(
                emoji = "📦",
                title = "Import Backup",
                subtitle = "Add recordings from a zip file",
                onClick = onRestoreBackup
            )

            MenuCard(
                emoji = "🎵",
                title = "Add Recording",
                subtitle = "Import a sound file",
                onClick = onAddRecording
            )

            // DANGER ZONE Section
            SectionHeader("DANGER ZONE")

            DangerCard(
                emoji = "🗑️",
                title = "Start Fresh",
                subtitle = "Delete everything (can't undo this!)",
                onClick = onStartFresh
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ============================================================
//  RESTORE WIZARD ROUTER
// ============================================================

@Composable
private fun RestoreWizardContent(
    wizardViewModel: ImportWizardViewModel,
    wizardState: ImportWizardViewModel.WizardState,
    onComplete: () -> Unit,
    onBack: () -> Unit
) {
    when (val step = wizardState.step) {
        is ImportWizardViewModel.WizardStep.FileSelect -> {
            RestoreStep1_FilePicker(
                onFileSelected = { uri, fileName ->
                    wizardViewModel.selectBackupFile(uri, fileName)
                },
                onBack = onBack
            )
        }

        is ImportWizardViewModel.WizardStep.Analyzing -> {
            RestoreStep_Analyzing()
        }

        is ImportWizardViewModel.WizardStep.Analysis -> {
            val analysis = wizardState.analysis
            val filtered = wizardState.filteredAnalysis

            if (analysis != null && filtered != null) {
                RestoreStep2_Analysis(
                    analysis = analysis,
                    filteredAnalysis = filtered,
                    dateChipOptions = wizardViewModel.getDateChipOptions(),
                    activeDateChips = wizardState.activeDateChips,
                    selectedNewRecordings = wizardState.selectedNewRecordings,
                    selectedConflicts = wizardState.selectedConflicts,
                    customNames = analysis.manifest.customNames,
                    onDateChipClick = { option ->
                        if (option.startMs == null) {
                            wizardViewModel.clearDateFilter()
                        } else {
                            wizardViewModel.setDateFilter(option.startMs, option.endMs, option.label)
                        }
                    },
                    onToggleNewRecording = { wizardViewModel.toggleNewRecording(it) },
                    onToggleConflict = { wizardViewModel.toggleConflict(it) },
                    onProceed = { wizardViewModel.proceedToConflicts() },
                    onBack = onBack,
                    formatDate = { wizardViewModel.formatDate(it) },
                    formatSize = { wizardViewModel.formatFileSize(it) }
                )
            }
        }

        is ImportWizardViewModel.WizardStep.Conflicts -> {
            val filtered = wizardState.filteredAnalysis
            if (filtered != null) {
                RestoreStep3_Conflicts(
                    conflicts = filtered.conflictingRecordings,
                    selectedConflicts = wizardState.selectedConflicts,
                    globalStrategy = wizardState.conflictStrategy,
                    onSetGlobalStrategy = { wizardViewModel.setGlobalConflictStrategy(it) },
                    onProceed = { wizardViewModel.startImport() },
                    onBack = onBack,
                    formatSize = { wizardViewModel.formatFileSize(it) }
                )
            }
        }

        is ImportWizardViewModel.WizardStep.Importing -> {
            RestoreStep4_Importing(progress = wizardState.importProgress)
        }

        is ImportWizardViewModel.WizardStep.Complete -> {
            RestoreStep5_Complete(
                result = step.result,
                onDone = onComplete
            )
        }

        is ImportWizardViewModel.WizardStep.Error -> {
            RestoreStep_Error(
                message = step.message,
                onRetry = { wizardViewModel.reset() },
                onBack = onBack
            )
        }
    }
}

// ============================================================
//  ADD RECORDING PICKER
// ============================================================

@Composable
private fun AddRecordingPickerContent(
    onFileSelected: (Uri) -> Unit,
    onBack: () -> Unit
) {
    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { onFileSelected(it) }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        GlassHeader(
            title = "ADD RECORDING",
            onBack = onBack
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Big tap target
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .shadow(12.dp, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .background(StaticMenuColors.settingsCardBackground)
                .border(2.dp, StaticMenuColors.toggleActive.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                .clickable { filePicker.launch("audio/*") },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("🎵", fontSize = 56.sp)
                Text(
                    "Tap to pick an audio file",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = StaticMenuColors.textOnCard
                )
                Text(
                    "WAV files work best",
                    style = MaterialTheme.typography.bodyMedium,
                    color = StaticMenuColors.textOnCard.copy(alpha = 0.6f)
                )
            }
        }
    }
}

// ============================================================
//  REUSABLE COMPONENTS
// ============================================================

@Composable
private fun GlassHeader(
    title: String,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(StaticMenuColors.headerBackground)
            .border(
                width = 1.dp,
                color = StaticMenuColors.headerBorder,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = StaticMenuColors.textOnGradient
                )
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                ),
                color = StaticMenuColors.textOnGradient
            )

            Spacer(modifier = Modifier.width(32.dp))
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        ),
        color = StaticMenuColors.textMuted,
        modifier = Modifier.padding(start = 4.dp, top = 8.dp)
    )
}

@Composable
private fun MenuCard(
    emoji: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    showProgress: Boolean = false
) {
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, shape)
            .clip(shape)
            .background(StaticMenuColors.settingsCardBackground)
            .border(1.dp, Color.White.copy(alpha = 0.3f), shape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(18.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (showProgress) {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    color = StaticMenuColors.toggleActive,
                    strokeWidth = 3.dp
                )
            } else {
                Text(text = emoji, fontSize = 28.sp)
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = StaticMenuColors.textOnCard.copy(alpha = if (enabled) 1f else 0.5f)
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = StaticMenuColors.textOnCard.copy(alpha = if (enabled) 0.7f else 0.4f)
                )
            }
            Text(
                text = "›",
                style = MaterialTheme.typography.headlineMedium,
                color = StaticMenuColors.textOnCard.copy(alpha = if (enabled) 0.4f else 0.2f)
            )
        }
    }
}

@Composable
private fun DangerCard(
    emoji: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(StaticMenuColors.deleteBackground)
            .border(1.dp, StaticMenuColors.deleteBorder, shape)
            .clickable(onClick = onClick)
            .padding(18.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = emoji, fontSize = 28.sp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = StaticMenuColors.deleteText
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = StaticMenuColors.deleteText.copy(alpha = 0.8f)
                )
            }
            Text(
                text = "›",
                style = MaterialTheme.typography.headlineMedium,
                color = StaticMenuColors.deleteText.copy(alpha = 0.5f)
            )
        }
    }
}

// ============================================================
//  EXPORT SELECTION SCREEN
// ============================================================

@Composable
private fun ExportSelectionContent(
    audioViewModel: AudioViewModel,
    backupManager: BackupManager,
    onExportStarted: () -> Unit,
    onExportComplete: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uiState by audioViewModel.uiState.collectAsState()
    val recordings = uiState.recordings

    // Selection state - all selected by default
    var selectedPaths by remember(recordings) {
        mutableStateOf(recordings.map { it.originalPath }.toSet())
    }

    // Derived state for toggle
    val allSelected = selectedPaths.size == recordings.size && recordings.isNotEmpty()

    // Share dialog state
    var showShareDialog by remember { mutableStateOf(false) }
    var exportedUri by remember { mutableStateOf<Uri?>(null) }
    var exportResultMessage by remember { mutableStateOf("") }

    // Export launcher
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        if (uri != null && selectedPaths.isNotEmpty()) {
            onExportStarted()
            scope.launch {
                try {
                    val tempDir = File(context.cacheDir, "backup_temp").apply { mkdirs() }
                    val result = backupManager.exportCustomSelection(
                        selectedPaths.toList(),
                        tempDir
                    )

                    if (result.success && result.zipFile != null) {
                        context.contentResolver.openOutputStream(uri)?.use { output ->
                            java.io.FileInputStream(result.zipFile).use { input ->
                                input.copyTo(output)
                            }
                        }
                        result.zipFile.delete()
                        result.zipFile.parentFile?.deleteRecursively()

                        // Show share dialog instead of completing immediately
                        exportedUri = uri
                        exportResultMessage = "${result.recordingsExported} recordings, ${result.attemptsExported} attempts"
                        showShareDialog = true
                    } else {
                        onExportComplete("Export failed")
                    }
                } catch (e: Exception) {
                    onExportComplete("Error: ${e.message}")
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        GlassHeader(
            title = "EXPORT SELECTION",
            onBack = onBack
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Select All / None toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Select All",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = StaticMenuColors.textOnGradient
            )

            Switch(
                checked = allSelected,
                onCheckedChange = { checked ->
                    selectedPaths = if (checked) {
                        recordings.map { it.originalPath }.toSet()
                    } else {
                        emptySet()
                    }
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = StaticMenuColors.toggleActive,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = StaticMenuColors.textMuted.copy(alpha = 0.3f),
                    uncheckedBorderColor = Color.Transparent
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Summary
        val totalAttempts = recordings.filter { it.originalPath in selectedPaths }
            .sumOf { it.attempts.size }
        Text(
            text = "${selectedPaths.size} recordings, $totalAttempts attempts selected",
            style = MaterialTheme.typography.bodySmall,
            color = StaticMenuColors.textMuted.copy(alpha = 0.7f),
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Recording list with checkboxes
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            recordings.forEach { recording ->
                val isSelected = recording.originalPath in selectedPaths
                SelectableRecordingCard(
                    name = recording.name,
                    attemptCount = recording.attempts.size,
                    isSelected = isSelected,
                    onToggle = {
                        selectedPaths = if (isSelected) {
                            selectedPaths - recording.originalPath
                        } else {
                            selectedPaths + recording.originalPath
                        }
                    }
                )
            }

            if (recordings.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No recordings to export",
                        color = StaticMenuColors.textMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Export button
        Button(
            onClick = {
                val count = selectedPaths.size
                val total = recordings.size
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                val filename = if (count == total) {
                    "reversey_backup_$timestamp.zip"
                } else {
                    "reversey_backup_${count}of${total}_$timestamp.zip"
                }
                exportLauncher.launch(filename)
            },
            enabled = selectedPaths.isNotEmpty(),
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = StaticMenuColors.toggleActive
            )
        ) {
            Text("Export ${selectedPaths.size} Recording${if (selectedPaths.size != 1) "s" else ""}")
        }
    }

    // Share dialog after export
    if (showShareDialog) {
        AlertDialog(
            onDismissRequest = {
                showShareDialog = false
                onExportComplete("Exported $exportResultMessage")
            },
            title = {
                Text(
                    "Export Complete! ✅",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "Exported $exportResultMessage",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        exportedUri?.let { uri ->
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/zip"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Backup"))
                        }
                        showShareDialog = false
                        onExportComplete("Exported $exportResultMessage")
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StaticMenuColors.toggleActive
                    )
                ) {
                    Text("Share")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showShareDialog = false
                        onExportComplete("Exported $exportResultMessage")
                    }
                ) {
                    Text("Done")
                }
            }
        )
    }
}

@Composable
private fun SelectableRecordingCard(
    name: String,
    attemptCount: Int,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                if (isSelected) Color.White
                else StaticMenuColors.settingsCardBackground
            )
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) StaticMenuColors.toggleActive else Color.White.copy(alpha = 0.2f),
                shape = shape
            )
            .clickable(onClick = onToggle)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Checkbox indicator
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (isSelected) StaticMenuColors.toggleActive
                        else Color.Transparent
                    )
                    .border(
                        2.dp,
                        StaticMenuColors.toggleActive,
                        RoundedCornerShape(6.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Text("✓", color = Color.White, fontSize = 14.sp)
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (isSelected) Color.DarkGray else StaticMenuColors.textOnCard
                )
                Text(
                    text = "$attemptCount attempt${if (attemptCount != 1) "s" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) Color.Gray else StaticMenuColors.textOnCard.copy(alpha = 0.6f)
                )
            }
        }
    }
}