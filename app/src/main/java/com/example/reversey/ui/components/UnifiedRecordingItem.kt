package com.example.reversey.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.Text
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
import com.example.reversey.data.models.Recording
import com.example.reversey.ui.theme.AestheticThemeData
import com.example.reversey.data.models.ChallengeType

/**
 * UNIFIED RECORDING ITEM - GLUTE COMPONENT COMPOSITION! ✨
 *
 * Uses aesthetic.components for egg/scrapbook themes.
 * Keeps default implementation inline to avoid circular dependency.
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
    // 🎨 GLUTE: Route themed implementations through components
    when {
        aesthetic.id == "egg" -> {
            // Egg theme uses its component
            aesthetic.components.RecordingItem(
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
        }
        aesthetic.id == "scrapbook" -> {
            // Scrapbook theme uses its component
            aesthetic.components.RecordingItem(
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
        }
        aesthetic.id == "guitar" -> {  // ← ADD THIS!
            aesthetic.components.RecordingItem(
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
        }
        else -> {
            // Default Material 3 implementation (inline to avoid recursion)
            DefaultRecordingItemImpl(
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
        }
    }
}

/**
 * Default Material 3 Recording Item Implementation
 * Extracted to avoid circular dependency with DefaultThemeComponents
 */
@Composable
private fun DefaultRecordingItemImpl(
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
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }

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
            // Title row with delete button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp)
                ) {
                    Text(
                        text = recording.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.clickable { showRenameDialog = true }
                    )
                }

                IconButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.size(36.dp)
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

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Top
            ) {
                // Share
                SimpleGlowButton(
                    onClick = { showShareDialog = true },
                    isPrimary = true,
                    size = 50.dp,
                    label = "Share"
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share Recording",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Play/Pause
                if (isPlaying) {
                    SimpleGlowButton(
                        onClick = { onPause() },
                        isPrimary = true,
                        size = 50.dp,
                        label = if (isPaused) "Resume" else "Pause"
                    ) {
                        Icon(
                            imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
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

                // Rewind/Stop
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

                // Forward challenge
                if (isGameModeEnabled) {
                    SimpleGlowButton(
                        onClick = { onStartAttempt(recording, ChallengeType.FORWARD) },
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
                    Spacer(modifier = Modifier.size(50.dp))
                }

                // Reverse challenge
                if (isGameModeEnabled) {
                    SimpleGlowButton(
                        onClick = { onStartAttempt(recording, ChallengeType.REVERSE) },
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
                    Spacer(modifier = Modifier.size(50.dp))
                }
            }
        }
    }

    // ✨ SHARED DIALOGS
    if (showRenameDialog) {
        RecordingRenameDialog(
            recording = recording,
            aesthetic = aesthetic,
            onDismiss = { showRenameDialog = false },
            onRename = onRename
        )
    }

    if (showDeleteDialog) {
        RecordingDeleteDialog(
            recording = recording,
            aesthetic = aesthetic,
            onDismiss = { showDeleteDialog = false },
            onDelete = onDelete
        )
    }

    if (showShareDialog) {
        RecordingShareDialog(
            recording = recording,
            aesthetic = aesthetic,
            onDismiss = { showShareDialog = false },
            onShare = onShare
        )
    }
}

/**
 * SimpleGlowButton helper
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