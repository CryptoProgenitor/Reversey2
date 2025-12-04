package com.quokkalabs.reversey.ui.menu

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import java.util.*

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

                        Toast.makeText(
                            context,
                            "Backup saved! ${result.recordingsExported} recordings exported",
                            Toast.LENGTH_SHORT
                        ).show()
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
                    isExporting = isExporting
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
    }
}

// Screen navigation state
sealed class FilesScreen {
    object Menu : FilesScreen()
    object RestoreWizard : FilesScreen()
    object AddRecordingPicker : FilesScreen()
    data class SingleWavImport(val wavUri: Uri) : FilesScreen()
}

// ============================================================
//  FILES MENU (Entry Point)
// ============================================================

@Composable
private fun FilesMenuContent(
    onNavigateBack: () -> Unit,
    onBackItUp: () -> Unit,
    onRestoreBackup: () -> Unit,
    onAddRecording: () -> Unit,
    onStartFresh: () -> Unit,
    isExporting: Boolean
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

            // RESTORE Section
            SectionHeader("RESTORE")

            MenuCard(
                emoji = "📦",
                title = "Restore Backup",
                subtitle = "Bring back your stuff from a zip",
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