package com.example.reversey.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.reversey.ui.components.egg.EggStyleRecordingItem
import com.example.reversey.Recording
import com.example.reversey.ui.theme.AestheticThemeData
import com.example.reversey.ChallengeType

/**
 * UNIFIED RECORDING ITEM - NEW DELETE-IN-TITLE LAYOUT! 💡
 * Egg theme unchanged, GenZUI/Scrapbook get the new design
 */
@Composable
fun UnifiedRecordingItem(
    recording: Recording,
    aesthetic: AestheticThemeData,
    isPlaying: Boolean,
    isPaused: Boolean,
    progress: Float,
    onPlay: (String) -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit,
    onDelete: (Recording) -> Unit,
    onShare: (String) -> Unit,
    onRename: (String, String) -> Unit,
    isGameModeEnabled: Boolean,
    onStartAttempt: (Recording, ChallengeType) -> Unit
) {
    val isEggTheme = aesthetic.id == "egg"
    val isScrapbookTheme = aesthetic.useScrapbookElements

    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }

    if (isEggTheme) {
        // 🥚 EGG THEME: Leave completely unchanged!
        EggStyleRecordingItem(
            recording = recording,
            aesthetic = aesthetic,
            isPlaying = isPlaying,
            isPaused = isPaused,
            progress = progress,
            onPlay = onPlay,
            onPause = onPause,
            onStop = onStop,
            onDelete = onDelete,
            onShare = onShare,
            onRename = onRename,
            isGameModeEnabled = isGameModeEnabled,
            onStartAttempt = onStartAttempt
        )



    } else if (isScrapbookTheme) {
        com.example.reversey.ScrapbookRecordingItem(
            recording = recording,
            isPlaying = isPlaying,
            isPaused = isPaused,
            progress = progress,
            onPlay = onPlay,
            onPause = onPause,
            onStop = onStop,
            onDelete = onDelete,
            onShare = onShare,
            onRename = onRename,
            isGameModeEnabled = isGameModeEnabled,
            onStartAttempt = onStartAttempt
        )
    } else {
        // 🎨 GENZUI & SCRAPBOOK: New delete-in-title layout!
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // 💡 NEW: Title row with delete button on the right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // Left side: Filename and subtitle (takes remaining space)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 12.dp) // Space before delete button
                    ) {
                        Text(
                            text = recording.originalPath?.substringAfterLast("/")?.removeSuffix(".wav") ?: "Recording",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.clickable { showRenameDialog = true } // 🔧 FIXED: Click to rename!// 🔧 Truncate long filenames

                        )
                        Text(
                            text = "Audio recording",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Right side: Delete button (compact size)
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.size(36.dp) // Compact but touchable
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Recording",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Progress bar
                LinearProgressIndicator(
                    progress = if (isPlaying) progress else 0f,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 🎊 IMPROVED: 5-button layout (no delete!) with perfect spacing
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly, // Perfect distribution!
                    verticalAlignment = Alignment.Top
                ) {
                    // 1. SHARE BUTTON
                    SimpleGlowButton(
                        onClick = { showShareDialog = true },
                        isPrimary = true,
                        size = 50.dp, // Larger buttons now that we have space!
                        label = "Share"
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Recording",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // 2. PLAY/PAUSE BUTTON (dynamic)
                    if (isPlaying) {
                        SimpleGlowButton(
                            onClick = { onPause() },
                            isPrimary = true,
                            size = 50.dp,
                            label = if (isPaused) "Resume" else "Pause"
                        ) {
                            val pauseIcon = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause
                            Icon(
                                imageVector = pauseIcon,
                                contentDescription = "Pause/Resume",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    } else {
                        SimpleGlowButton(
                            onClick = { onPlay(recording.originalPath ?: "") },
                            isPrimary = true,
                            size = 50.dp,
                            label = "Play"
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play Original",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    // 3. REWIND/STOP BUTTON (dynamic)
                    if (isPlaying) {
                        SimpleGlowButton(
                            onClick = { onStop() },
                            isDestructive = true,
                            size = 50.dp,
                            label = "Stop"
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Stop",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    } else {
                        SimpleGlowButton(
                            onClick = { onPlay(recording.reversedPath ?: "") },
                            enabled = recording.reversedPath != null,
                            isPrimary = true,
                            size = 50.dp,
                            label = "Rewind"
                        ) {
                            Icon(
                                imageVector = Icons.Default.Replay,
                                contentDescription = "Play Reversed",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    // 4. FWD CHALLENGE (if game mode enabled)
                    if (isGameModeEnabled) {
                        SimpleGlowButton(
                            onClick = { onStartAttempt(recording, ChallengeType.values().getOrNull(0) ?: return@SimpleGlowButton) },
                            isPrimary = true,
                            size = 50.dp,
                            label = "Fwd"
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Forward Challenge",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    } else {
                        // Show a placeholder if game mode is off (keeps layout consistent)
                        Spacer(modifier = Modifier.size(50.dp))
                    }

                    // 5. REV CHALLENGE (if game mode enabled)
                    if (isGameModeEnabled) {
                        SimpleGlowButton(
                            onClick = { onStartAttempt(recording, ChallengeType.values().getOrNull(1) ?: return@SimpleGlowButton) },
                            isPrimary = true,
                            size = 50.dp,
                            label = "Rev"
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Reverse Challenge",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    } else {
                        // Show a placeholder if game mode is off (keeps layout consistent)
                        Spacer(modifier = Modifier.size(50.dp))
                    }
                }
            }
        }
    }

    // DIALOGS (unchanged)
    if (showRenameDialog) {
        var newName by remember { mutableStateOf(recording.originalPath?.substringAfterLast("/")?.removeSuffix(".wav") ?: "Recording") }
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Recording") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("New Name") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (newName.isNotBlank()) {
                        val finalName = if (newName.endsWith(".wav")) newName else "$newName.wav"
                        onRename(recording.originalPath ?: "", finalName)
                    }
                    showRenameDialog = false
                }) { Text("Rename") }
            },
            dismissButton = {
                Button(onClick = { showRenameDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Recording?", color = MaterialTheme.colorScheme.error) },
            text = { Text("Are you sure you want to delete this recording? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete(recording)
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete", color = Color.White) }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showShareDialog) {
        AlertDialog(
            onDismissRequest = { showShareDialog = false },
            title = { Text("Share Recording") },
            text = {
                Column {
                    Text("Which version would you like to share?")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            onShare(recording.originalPath ?: "")
                            showShareDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Share Original", color = Color.White)
                    }
                    if (recording.reversedPath != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                onShare(recording.reversedPath!!)
                                showShareDialog = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Share Reversed", color = Color.White)
                        }
                    }
                }
            },
            confirmButton = { },
            dismissButton = {
                Button(onClick = { showShareDialog = false }) { Text("Cancel") }
            }
        )
    }
}

/**
 * 🔧 PERFECTED: SimpleGlowButton with optimal spacing
 */
@Composable
fun SimpleGlowButton(
    onClick: () -> Unit,
    isPrimary: Boolean = false,
    isDestructive: Boolean = false,
    enabled: Boolean = true,
    size: Dp,
    label: String,
    content: @Composable () -> Unit
) {
    val containerColor = when {
        isDestructive -> MaterialTheme.colorScheme.error
        isPrimary -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.secondary
    }

    // Perfect button + label layout
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier.size(size),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = containerColor,
                disabledContainerColor = containerColor.copy(alpha = 0.3f)
            ),
            contentPadding = PaddingValues(0.dp)
        ) {
            content()
        }

        // Perfectly centered label
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            ),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}