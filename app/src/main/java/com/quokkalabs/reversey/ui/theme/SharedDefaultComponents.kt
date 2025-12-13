package com.quokkalabs.reversey.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quokkalabs.reversey.data.models.ChallengeType
import com.quokkalabs.reversey.data.models.PlayerAttempt
import com.quokkalabs.reversey.data.models.Recording
import com.quokkalabs.reversey.ui.components.DifficultySquircle
import com.quokkalabs.reversey.ui.components.ScoreExplanationDialog

/**
 * 🛠️ SHARED DEFAULT COMPONENTS
 *
 * DRY Implementation:
 * These are reusable Material 3 components that "Basic" themes can delegate to.
 * Pro themes (Egg, Scrapbook) can ignore these and implement their own.
 */
object SharedDefaultComponents {

    // --- RECORDING CARD ---

    @Composable
    fun MaterialRecordingCard(
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
        // 🔧 FIX: Handle state internally so clicks actually work!
        var showRenameDialog by remember { mutableStateOf(false) }
        var showDeleteDialog by remember { mutableStateOf(false) }
        var showShareDialog by remember { mutableStateOf(false) }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Title row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = recording.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 12.dp)
                            .clickable { showRenameDialog = true } // 🔧 FIX: Click to rename
                    )

                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Progress bar
                LinearProgressIndicator(
                    progress = { if (isPlaying) progress else 0f },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Buttons - 🔧 FIX: Restore 5-button layout & Primary Colors
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // 1. Share
                    SimpleGlowButton(
                        onClick = { showShareDialog = true },
                        size = 50.dp,
                        label = "Share",
                        icon = Icons.Default.Share,
                        isPrimary = true // 🔧 FIX: Restore Pink color
                    )

                    // 2. Play/Pause
                    if (isPlaying) {
                        SimpleGlowButton(
                            onClick = { if (isPaused) onPlay(recording.originalPath) else onPause() },
                            size = 50.dp,
                            label = if (isPaused) "Resume" else "Pause",
                            isPrimary = true,
                            icon = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause
                        )
                    } else {
                        SimpleGlowButton(
                            onClick = { onPlay(recording.originalPath) },
                            size = 50.dp,
                            label = "Play",
                            isPrimary = true,
                            icon = Icons.Default.PlayArrow
                        )
                    }

                    // 3. Rewind
                    SimpleGlowButton(
                        onClick = { recording.reversedPath?.let { onPlay(it) } },
                        enabled = recording.reversedPath != null,
                        size = 50.dp,
                        label = "Rewind", // 🔧 FIX: Label restored
                        icon = Icons.Default.Replay,
                        isPrimary = true // 🔧 FIX: Restore Pink color
                    )

                    // 4 & 5. Game Mode Buttons (Fwd & Rev)
                    if (isGameModeEnabled) {
                        SimpleGlowButton(
                            onClick = { onStartAttempt(recording, ChallengeType.FORWARD) },
                            size = 50.dp,
                            label = "Fwd", // 🔧 FIX: Restored Fwd button
                            icon = Icons.Default.Mic,
                            isPrimary = true
                        )

                        SimpleGlowButton(
                            onClick = { onStartAttempt(recording, ChallengeType.REVERSE) },
                            size = 50.dp,
                            label = "Rev", // 🔧 FIX: Restored Rev button
                            icon = Icons.Default.Mic,
                            isPrimary = true
                        )
                    }
                }
            }
        }

        // Internal Dialog Routing
        if (showDeleteDialog) {
            MaterialDeleteDialog(
                itemType = DeletableItemType.RECORDING,
                item = recording,
                aesthetic = aesthetic,
                onConfirm = { onDelete(recording) },
                onDismiss = { showDeleteDialog = false }
            )
        }

        if (showShareDialog) {
            MaterialShareDialog(
                recording = recording,
                attempt = null,
                aesthetic = aesthetic,
                onShare = onShare,
                onDismiss = { showShareDialog = false }
            )
        }

        if (showRenameDialog) {
            MaterialRenameDialog(
                itemType = RenamableItemType.RECORDING,
                currentName = recording.name,
                aesthetic = aesthetic,
                onRename = { newName -> onRename(recording.originalPath, newName) },
                onDismiss = { showRenameDialog = false }
            )
        }
    }

    // --- ATTEMPT CARD ---

    @Composable
    fun MaterialAttemptCard(
        attempt: PlayerAttempt,
        aesthetic: AestheticThemeData,
        currentlyPlayingPath: String?,
        isPaused: Boolean,
        progress: Float,
        onPlay: (String) -> Unit,
        onPause: () -> Unit,
        onStop: () -> Unit,
        onRenamePlayer: ((PlayerAttempt, String) -> Unit)?,
        onDeleteAttempt: ((PlayerAttempt) -> Unit)?,
        onShareAttempt: ((String) -> Unit)?,
        onJumpToParent: (() -> Unit)?,
        onOverrideScore: ((Int) -> Unit)? = null  // Phase 4
    ) {
        // 🔧 FIX: Internalize state so dialogs actually open
        var showRenameDialog by remember { mutableStateOf(false) }
        var showDeleteDialog by remember { mutableStateOf(false) }
        var showShareDialog by remember { mutableStateOf(false) }
        var showScoreDialog by remember { mutableStateOf(false) }

        val isPlayingThis = currentlyPlayingPath == attempt.attemptFilePath ||
                currentlyPlayingPath == attempt.reversedAttemptFilePath

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 32.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    verticalAlignment = Alignment.Top, // Align top to accommodate tall squircle
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Left Column: Header + Buttons
                    Column(modifier = Modifier.weight(1f)) {
                        // Header Row: Home Icon + Name
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (onJumpToParent != null) {
                                IconButton(onClick = onJumpToParent, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Home, "Parent", tint = MaterialTheme.colorScheme.primary)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                            }

                            Text(
                                text = attempt.playerName,
                                style = MaterialTheme.typography.titleMedium, // Larger text
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { showRenameDialog = true }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp)) // More breathing room

                        // Controls Row - 🔧 FIX: Restored 4-button layout using SimpleGlowButton
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp) // Even spacing
                        ) {
                            // 1. Share
                            if (onShareAttempt != null) {
                                SimpleGlowButton(
                                    onClick = { showShareDialog = true },
                                    size = 40.dp,
                                    label = "Share",
                                    icon = Icons.Default.Share,
                                    isPrimary = true
                                )
                            }

                            // 2. Play/Pause
                            if (isPlayingThis && !isPaused) {
                                SimpleGlowButton(
                                    onClick = { onPause() },
                                    size = 40.dp,
                                    label = "Pause",
                                    icon = Icons.Default.Pause,
                                    isPrimary = true
                                )
                            } else {
                                SimpleGlowButton(
                                    onClick = { onPlay(attempt.attemptFilePath) },
                                    size = 40.dp,
                                    label = "Play",
                                    icon = Icons.Default.PlayArrow,
                                    isPrimary = true
                                )
                            }

                            // 3. Reverse
                            if (attempt.reversedAttemptFilePath != null) {
                                SimpleGlowButton(
                                    onClick = { onPlay(attempt.reversedAttemptFilePath!!) },
                                    size = 40.dp,
                                    label = "Rev",
                                    icon = Icons.Default.Replay,
                                    isPrimary = true
                                )
                            }

                            // 4. Delete - 🔧 FIX: Restored to main row, Pink (Secondary) color
                            if (onDeleteAttempt != null) {
                                SimpleGlowButton(
                                    onClick = { showDeleteDialog = true },
                                    size = 40.dp,
                                    label = "Del",
                                    icon = Icons.Default.Delete,
                                    isPrimary = false // Secondary color (faded pink)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Right Column: Score Badge - 🔧 FIX: Restored full size (100x130)
                    DifficultySquircle(
                        score = attempt.finalScore ?: attempt.score,
                        difficulty = attempt.difficulty,
                        challengeType = attempt.challengeType,
                        emoji = aesthetic.scoreEmojis.entries.firstOrNull { attempt.score >= it.key }?.value ?: "🎤",
                        width = 100.dp,  // Restored Width
                        height = 130.dp, // Restored Height
                        onClick = { showScoreDialog = true }
                    )
                }

                if (isPlayingThis) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Internal Dialog Routing
        if (showDeleteDialog && onDeleteAttempt != null) {
            MaterialDeleteDialog(
                itemType = DeletableItemType.ATTEMPT,
                item = attempt,
                aesthetic = aesthetic,
                onConfirm = { onDeleteAttempt(attempt) },
                onDismiss = { showDeleteDialog = false }
            )
        }

        if (showShareDialog && onShareAttempt != null) {
            MaterialShareDialog(
                recording = null,
                attempt = attempt,
                aesthetic = aesthetic,
                onShare = onShareAttempt,
                onDismiss = { showShareDialog = false }
            )
        }

        if (showRenameDialog && onRenamePlayer != null) {
            MaterialRenameDialog(
                itemType = RenamableItemType.PLAYER,
                currentName = attempt.playerName,
                aesthetic = aesthetic,
                onRename = { newName -> onRenamePlayer(attempt, newName) },
                onDismiss = { showRenameDialog = false }
            )
        }

        if (showScoreDialog) {
            ScoreExplanationDialog(
                attempt = attempt,
                onDismiss = { showScoreDialog = false },
                onOverrideScore = onOverrideScore ?: { }
            )
        }
    }

    // --- RECORD BUTTON ---

    /**
     * 🎯 PHASE 3: Record button with countdown arc for timed recording
     */
    @Composable
    fun MaterialRecordButton(
        isRecording: Boolean,
        countdownProgress: Float = 1f,  // 🎯 PHASE 3: 1.0 → 0.0
        onClick: () -> Unit
    ) {
        Box(
            modifier = Modifier.size(80.dp),
            contentAlignment = Alignment.Center
        ) {
            // 🎯 PHASE 3: Background arc (gray track)
            Canvas(modifier = Modifier.size(80.dp)) {
                drawArc(
                    color = Color.Gray.copy(alpha = 0.3f),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // 🎯 PHASE 3: Progress arc (depleting red arc during timed recording)
            if (isRecording && countdownProgress < 1f) {
                Canvas(modifier = Modifier.size(100.dp)) {
                    drawArc(
                        color = Color.Red,
                        startAngle = -90f,
                        sweepAngle = 360f * countdownProgress,
                        useCenter = false,
                        style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            // Record button FAB
            FloatingActionButton(
                onClick = onClick,
                modifier = Modifier.size(64.dp),
                containerColor = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = if (isRecording) "Stop" else "Record",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }

    // --- BACKGROUND ---

    @Composable
    fun GradientBackground(
        aesthetic: AestheticThemeData,
        content: @Composable () -> Unit
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(aesthetic.primaryGradient)
        ) {
            content()
        }
    }

    // --- SCORE CARD ---

    @Composable
    fun MaterialScoreCard(
        attempt: PlayerAttempt,
        aesthetic: AestheticThemeData,
        onDismiss: () -> Unit,
        onOverrideScore: (Int) -> Unit = { }
    ) {
        // Delegate to the shared ScoreExplanationDialog which is already theme-aware
        ScoreExplanationDialog(
            attempt = attempt,
            onDismiss = onDismiss,
            onOverrideScore = onOverrideScore
        )
    }

    // --- DIALOGS ---

    @Composable
    fun MaterialDeleteDialog(
        itemType: DeletableItemType,
        item: Any,
        aesthetic: AestheticThemeData,
        onConfirm: () -> Unit,
        onDismiss: () -> Unit
    ) {
        val itemName = when(item) {
            is Recording -> item.name
            is PlayerAttempt -> item.playerName
            else -> "Item"
        }

        val copy = aesthetic.dialogCopy

        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(copy.deleteTitle(itemType)) },
            text = { Text(copy.deleteMessage(itemType, itemName)) },
            confirmButton = {
                Button(
                    onClick = {
                        onConfirm()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text(copy.deleteConfirmButton) }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text(copy.deleteCancelButton) }
            }
        )
    }

    @Composable
    fun MaterialShareDialog(
        recording: Recording?,
        attempt: PlayerAttempt?,
        aesthetic: AestheticThemeData,
        onShare: (String) -> Unit,
        onDismiss: () -> Unit
    ) {
        val copy = aesthetic.dialogCopy

        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(copy.shareTitle) },
            text = {
                Column {
                    Text(copy.shareMessage)
                    Spacer(modifier = Modifier.height(16.dp))

                    val path = recording?.originalPath ?: attempt?.attemptFilePath ?: ""
                    Button(
                        onClick = { onShare(path); onDismiss() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (recording != null) "Share Original" else "Share Attempt")
                    }

                    val revPath = recording?.reversedPath ?: attempt?.reversedAttemptFilePath
                    if (revPath != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { onShare(revPath); onDismiss() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Text("Share Reversed")
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        )
    }

    @Composable
    fun MaterialRenameDialog(
        itemType: RenamableItemType,
        currentName: String,
        aesthetic: AestheticThemeData,
        onRename: (String) -> Unit,
        onDismiss: () -> Unit
    ) {
        var name by remember { mutableStateOf(currentName) }
        val copy = aesthetic.dialogCopy

        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(copy.renameTitle(itemType)) },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    singleLine = true,
                    label = { Text(copy.renameHint) }
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (name.isNotBlank()) {
                        onRename(name)
                        onDismiss()
                    }
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        )
    }

    // --- MENU COMPONENTS ---

    @Composable
    fun ThemedSettingsCard(
        aesthetic: AestheticThemeData,
        title: String? = null,
        content: @Composable ColumnScope.() -> Unit
    ) {
        val colors = aesthetic.menuColors

        Column(modifier = Modifier.fillMaxWidth()) {
            if (title != null) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.menuTitleText,
                    modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = colors.menuCardBackground
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.menuBorder.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    content()
                }
            }
        }
    }

    @Composable
    fun ThemedToggle(
        aesthetic: AestheticThemeData,
        label: String,
        checked: Boolean,
        onCheckedChange: (Boolean) -> Unit
    ) {
        val colors = aesthetic.menuColors

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = colors.menuItemText
            )

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = colors.toggleActive,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = colors.toggleInactive
                )
            )
        }
    }

    // --- HELPER ---

    @Composable
    private fun SimpleGlowButton(
        onClick: () -> Unit,
        isPrimary: Boolean = false,
        enabled: Boolean = true,
        size: Dp,
        label: String,
        icon: androidx.compose.ui.graphics.vector.ImageVector
    ) {
        val containerColor = if (isPrimary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer

        // 🔧 FIX: Explicitly set disabled color to Faded Pink instead of default Black/Grey
        val disabledColor = if (isPrimary)
            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        else
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)

        val contentColor = Color.White
        val disabledContentColor = Color.White.copy(alpha = 0.5f)

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Button(
                onClick = onClick,
                enabled = enabled,
                modifier = Modifier.size(size),
                shape = CircleShape,
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = containerColor,
                    disabledContainerColor = disabledColor, // 🎨 Restored styling
                    contentColor = contentColor,
                    disabledContentColor = disabledContentColor
                )
            ) {
                Icon(icon, null)
            }

            // Label styling
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}