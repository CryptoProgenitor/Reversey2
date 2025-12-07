package com.quokkalabs.reversey.ui.menu

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quokkalabs.reversey.data.backup.BackupManager
import com.quokkalabs.reversey.data.backup.ConflictStrategy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * 📦 Backup & Restore Section for Settings Screen
 * 
 * Provides user-facing backup controls:
 * - Export backup to Downloads
 * - Import backup from file picker
 * - Progress indicators
 * - Success/error messages
 */
@Composable
fun BackupSection(
    backupManager: BackupManager,
    onBackupComplete: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var isExporting by remember { mutableStateOf(false) }
    var isImporting by remember { mutableStateOf(false) }
    var showConflictDialog by remember { mutableStateOf(false) }
    var selectedBackupUri by remember { mutableStateOf<Uri?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }
    
    // File picker for import
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedBackupUri = uri
            showConflictDialog = true
        }
    }
    
    // Glassmorphic card matching your GLUTE theme style
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF667EEA).copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Text(
                text = "📦 Backup & Restore",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = "Save your recordings and attempts to a backup file",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Divider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            )
            
            // Export Button
            Button(
                onClick = {
                    scope.launch {
                        isExporting = true
                        statusMessage = null
                        try {
                            val result = exportBackup(context, backupManager)
                            statusMessage = result
                            isError = false
                        } catch (e: Exception) {
                            statusMessage = "Export failed. Please try again."
                            isError = true
                        } finally {
                            isExporting = false
                        }
                    }
                },
                enabled = !isExporting && !isImporting,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (isExporting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Exporting...")
                } else {
                    Icon(Icons.Default.CloudDownload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export Backup")
                }
            }
            
            // Import Button
            OutlinedButton(
                onClick = { importLauncher.launch("application/zip") },
                enabled = !isExporting && !isImporting,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.CloudUpload, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Import Backup")
            }
            
            // Status Message
            if (statusMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isError) {
                            Color(0xFFFF5252).copy(alpha = 0.1f)
                        } else {
                            Color(0xFF4CAF50).copy(alpha = 0.1f)
                        }
                    )
                ) {
                    Text(
                        text = statusMessage!!,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isError) {
                            Color(0xFFFF5252)
                        } else {
                            Color(0xFF4CAF50)
                        }
                    )
                }
            }
            
            // Import Progress
            if (isImporting) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Importing backup...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
    
    // Conflict Strategy Dialog
    if (showConflictDialog && selectedBackupUri != null) {
        AlertDialog(
            onDismissRequest = { showConflictDialog = false },
            title = { Text("Import Options") },
            text = {
                Column {
                    Text("How should conflicts be handled?")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Skip Duplicates: Don't overwrite existing files\n" +
                               "• Merge Attempts: Add new attempts, keep recordings",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            isImporting = true
                            showConflictDialog = false
                            statusMessage = null
                            try {
                                val result = importBackup(
                                    context,
                                    backupManager,
                                    selectedBackupUri!!,
                                    ConflictStrategy.SKIP_DUPLICATES
                                )
                                statusMessage = result
                                isError = false
                                onBackupComplete()
                            } catch (e: Exception) {
                                statusMessage = "Import failed. Please try again."
                                isError = true
                            } finally {
                                isImporting = false
                                selectedBackupUri = null
                            }
                        }
                    }
                ) {
                    Text("Skip Duplicates")
                }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = {
                            scope.launch {
                                isImporting = true
                                showConflictDialog = false
                                statusMessage = null
                                try {
                                    val result = importBackup(
                                        context,
                                        backupManager,
                                        selectedBackupUri!!,
                                        ConflictStrategy.MERGE_ATTEMPTS_ONLY
                                    )
                                    statusMessage = result
                                    isError = false
                                    onBackupComplete()
                                } catch (e: Exception) {
                                    statusMessage = "Import failed. Please try again."
                                    isError = true
                                } finally {
                                    isImporting = false
                                    selectedBackupUri = null
                                }
                            }
                        }
                    ) {
                        Text("Merge Attempts")
                    }
                    TextButton(onClick = { showConflictDialog = false }) {
                        Text("Cancel")
                    }
                }
            }
        )
    }
}

// ============================================================
//  HELPER FUNCTIONS
// ============================================================

/**
 * Export backup to Downloads folder
 */
private suspend fun exportBackup(
    context: Context,
    backupManager: BackupManager
): String = withContext(Dispatchers.IO) {
    // Create temp directory for export
    val tempDir = File(context.cacheDir, "backup_temp").apply {
        mkdirs()
    }
    
    // Export to temp
    val result = backupManager.exportFullBackup(tempDir)
    
    if (!result.success || result.zipFile == null) {
        throw Exception(result.error ?: "Export failed")
    }
    
    // Copy to Downloads
    val downloadsDir = File(
        android.os.Environment.getExternalStoragePublicDirectory(
            android.os.Environment.DIRECTORY_DOWNLOADS
        ),
        "ReVerseY"
    ).apply { mkdirs() }
    
    val finalFile = File(downloadsDir, result.zipFile.name)
    FileInputStream(result.zipFile).use { input ->
        FileOutputStream(finalFile).use { output ->
            input.copyTo(output)
        }
    }
    
    // Cleanup temp
    result.zipFile.delete()
    tempDir.deleteRecursively()
    
    "✅ Backup saved to Downloads/ReVerseY/${finalFile.name}\n" +
    "Recordings: ${result.recordingsExported}, Attempts: ${result.attemptsExported}"
}

/**
 * Import backup from user-selected file
 */
private suspend fun importBackup(
    context: Context,
    backupManager: BackupManager,
    uri: Uri,
    strategy: ConflictStrategy
): String = withContext(Dispatchers.IO) {
    // Copy URI to temp file (required for BackupManager)
    val tempFile = File(context.cacheDir, "import_temp.zip")
    
    context.contentResolver.openInputStream(uri)?.use { input ->
        FileOutputStream(tempFile).use { output ->
            input.copyTo(output)
        }
    } ?: throw Exception("Could not read backup file")
    
    // Import
    val result = backupManager.importBackup(tempFile, strategy)
    
    // Cleanup
    tempFile.delete()
    
    if (!result.success) {
        throw Exception(result.error ?: "Import failed")
    }
    
    "✅ Backup imported successfully\n" +
    "Recordings: ${result.recordingsImported}, " +
    "Attempts: ${result.attemptsImported}, " +
    "Skipped: ${result.recordingsSkipped}"
}
