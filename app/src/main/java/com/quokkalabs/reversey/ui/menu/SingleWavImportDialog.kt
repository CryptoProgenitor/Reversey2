package com.quokkalabs.reversey.ui.menu

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Single WAV Import Dialog
 *
 * Shown when user shares a WAV file to ReVerseY from external apps.
 * - Pre-fills name from filename
 * - Auto-suffixes on collision (e.g., "My Song" → "My Song (1)")
 * - Cancel closes app (user came from external app)
 * - Import copies file and navigates to home with glow animation
 */
@Composable
fun SingleWavImportDialog(
    wavUri: Uri,
    onImportComplete: (importedFilePath: String, displayName: String) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Extract filename and size from URI
    var fileName by remember { mutableStateOf("") }
    var fileSize by remember { mutableStateOf(0L) }
    var isImporting by remember { mutableStateOf(false) }
    var customName by remember { mutableStateOf("") }

    // Load file info on mount
    LaunchedEffect(wavUri) {
        val info = getFileInfo(context, wavUri)
        fileName = info.first
        fileSize = info.second
        // Pre-fill custom name (strip .wav extension)
        customName = fileName.removeSuffix(".wav").removeSuffix(".WAV")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StaticMenuColors.backgroundGradient)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(16.dp, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .background(StaticMenuColors.settingsCardBackground)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            Text(
                "ADD RECORDING",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                ),
                color = StaticMenuColors.textOnCard
            )

            // File preview card
            FilePreviewCard(
                fileName = fileName,
                fileSize = formatFileSize(fileSize)
            )

            // Name input
            OutlinedTextField(
                value = customName,
                onValueChange = { customName = it },
                label = { Text("Name your recording") },
                placeholder = { Text("My awesome recording") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = StaticMenuColors.toggleActive,
                    unfocusedBorderColor = StaticMenuColors.textOnCard.copy(alpha = 0.3f),
                    focusedLabelColor = StaticMenuColors.toggleActive,
                    unfocusedLabelColor = StaticMenuColors.textOnCard.copy(alpha = 0.6f),
                    cursorColor = StaticMenuColors.toggleActive,
                    focusedTextColor = StaticMenuColors.textOnCard,
                    unfocusedTextColor = StaticMenuColors.textOnCard,
                    focusedPlaceholderColor = StaticMenuColors.textOnCard.copy(alpha = 0.4f),
                    unfocusedPlaceholderColor = StaticMenuColors.textOnCard.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Nah button
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = StaticMenuColors.textOnCard
                    ),
                    border = BorderStroke(1.dp, StaticMenuColors.textOnCard.copy(alpha = 0.3f))
                ) {
                    Text(
                        "Nah",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Add It! button
                Button(
                    onClick = {
                        if (customName.isBlank()) {
                            Toast.makeText(context, "Give it a name first!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        isImporting = true
                        scope.launch {
                            try {
                                val result = importWavFile(context, wavUri, customName)
                                if (result != null) {
                                    onImportComplete(result.first, result.second)
                                } else {
                                    Toast.makeText(context, "Import failed", Toast.LENGTH_SHORT).show()
                                    isImporting = false
                                }
                            } catch (e: Exception) {
                                Log.e("SingleWavImport", "Failed to import", e)
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                isImporting = false
                            }
                        }
                    },
                    enabled = !isImporting && customName.isNotBlank(),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StaticMenuColors.toggleActive,
                        disabledContainerColor = StaticMenuColors.toggleInactive
                    )
                ) {
                    if (isImporting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            "Add It! ✨",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilePreviewCard(
    fileName: String,
    fileSize: String
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(StaticMenuColors.toggleActive.copy(alpha = 0.1f))
            .border(
                2.dp,
                StaticMenuColors.toggleActive.copy(alpha = 0.3f),
                RoundedCornerShape(16.dp)
            )
            .padding(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Animated music icon
            Box(
                modifier = Modifier
                    .size((48 * scale).dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(StaticMenuColors.toggleActive),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    fileName.ifEmpty { "audio file" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = StaticMenuColors.textOnCard,
                    maxLines = 1
                )
                Text(
                    fileSize,
                    style = MaterialTheme.typography.bodyMedium,
                    color = StaticMenuColors.textOnCard.copy(alpha = 0.6f)
                )
            }
        }
    }
}

// ============================================================
//  UTILITY FUNCTIONS
// ============================================================

private fun getFileInfo(context: Context, uri: Uri): Pair<String, Long> {
    var fileName = "recording.wav"
    var fileSize = 0L

    try {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)

                if (nameIndex >= 0) {
                    fileName = cursor.getString(nameIndex) ?: fileName
                }
                if (sizeIndex >= 0) {
                    fileSize = cursor.getLong(sizeIndex)
                }
            }
        }
    } catch (e: Exception) {
        Log.e("SingleWavImport", "Failed to get file info", e)
    }

    return Pair(fileName, fileSize)
}

private suspend fun importWavFile(
    context: Context,
    uri: Uri,
    customName: String
): Pair<String, String>? = withContext(Dispatchers.IO) {
    try {
        val recordingsDir = File(context.filesDir, "recordings").apply { mkdirs() }

        // Generate unique filename
        val baseFileName = sanitizeFileName(customName)
        val targetFileName = generateUniqueFileName(baseFileName, recordingsDir)
        val targetFile = File(recordingsDir, targetFileName)

        // Copy file
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(targetFile).use { output ->
                input.copyTo(output)
            }
        }

        Log.d("SingleWavImport", "Imported to: ${targetFile.absolutePath}")

        // Return path and display name
        Pair(targetFile.absolutePath, customName)
    } catch (e: Exception) {
        Log.e("SingleWavImport", "Import failed", e)
        null
    }
}

private fun sanitizeFileName(name: String): String {
    // Remove invalid characters and ensure .wav extension
    val sanitized = name
        .replace(Regex("[<>:\"/\\\\|?*]"), "_")
        .trim()
        .take(100) // Limit length

    return if (sanitized.lowercase().endsWith(".wav")) {
        sanitized
    } else {
        "$sanitized.wav"
    }
}

private fun generateUniqueFileName(baseName: String, dir: File): String {
    val extension = ".wav"
    val nameWithoutExt = baseName.removeSuffix(extension)

    // Check if original name is available
    if (!File(dir, baseName).exists()) {
        return baseName
    }

    // Try (1), (2), (3), etc.
    var counter = 1
    var newName = "$nameWithoutExt ($counter)$extension"

    while (File(dir, newName).exists()) {
        counter++
        newName = "$nameWithoutExt ($counter)$extension"
    }

    return newName
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
    }
}