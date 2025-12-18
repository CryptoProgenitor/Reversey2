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
 * Pro themes (Egg, Scrapbook, etc.) can ignore these and implement their own.
 *
 * 🔧 POLYMORPHIC BUTTONS: Play/Rewind buttons now independently track their
 * playback state via currentlyPlayingPath, enabling proper Pause/Resume per button.
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
        currentlyPlayingPath: String?,  // 🔧 NEW: Which specific file is playing
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

        // 🔧 POLYMORPHIC: Track which button owns the current playback
        val isPlayingForward = currentlyPlayingPath == recording.originalPath
        val isPlayingReversed = currentlyPlayingPath == recording.reversedPath

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
                            .clickable { showRenameDialog = true }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Progress bar - show when either forward or reversed is playing
                LinearProgressIndicator(
                    progress = { if (isPlayingForward || isPlayingReversed) progress else 0f },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Buttons - 🔧 POLYMORPHIC: Each button tracks its own path
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
                        isPrimary = true
                    )

                    // 2. Play (Forward) - 🔧 POLYMORPHIC
                    SimpleGlowButton(
                        onClick = {
                            when {
                                isPlayingForward -> onPause()  // Toggle pause/resume
                                else -> onPlay(recording.originalPath)
                            }
                        },
                        size = 50.dp,
                        label = when {
                            isPlayingForward && !isPaused -> "Pause"
                            isPlayingForward && isPaused -> "Resume"
                            else -> "Play"
                        },
                        isPrimary = true,
                        icon = when {
                            isPlayingForward && !isPaused -> Icons.Default.Pause
                            else -> Icons.Default.PlayArrow
                        }
                    )

                    // 3. Rewind (Reversed) - 🔧 POLYMORPHIC
                    SimpleGlowButton(
                        onClick = {
                            when {
                                isPlayingReversed -> onPause()  // Toggle pause/resume
                                else -> recording.reversedPath?.let { onPlay(it) }
                            }
                        },
                        enabled = recording.reversedPath != null,
                        size = 50.dp,
                        label = when {
                            isPlayingReversed && !isPaused -> "Pause"
                            isPlayingReversed && isPaused -> "Resume"
                            else -> "Rewind"
                        },
                        icon = when {
                            isPlayingReversed && !isPaused -> Icons.Default.Pause
                            else -> Icons.Default.Replay
                        },
                        isPrimary = true
                    )

                    // 4. Game Mode Button
                    if (isGameModeEnabled) {
                        SimpleGlowButton(
                            onClick = { onStartAttempt(recording, ChallengeType.REVERSE) },
                            size = 50.dp,
                            label = "Try",
                            icon = Icons.Default.Mic,
                            isPrimary = true
                        )
                    }

                    // 5. Delete
                    SimpleGlowButton(
                        onClick = { showDeleteDialog = true },
                        size = 50.dp,
                        label = "Del",
                        icon = Icons.Default.Delete,
                        isPrimary = false
                    )
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
        onOverrideScore: ((Int) -> Unit)? = null,
        onResetScore: (() -> Unit)? = null
    ) {
        var showRenameDialog by remember { mutableStateOf(false) }
        var showDeleteDialog by remember { mutableStateOf(false) }
        var showShareDialog by remember { mutableStateOf(false) }
        var showScoreDialog by remember { mutableStateOf(false) }

        // 🔧 POLYMORPHIC: Track which specific file is playing
        val isPlayingForward = currentlyPlayingPath == attempt.attemptFilePath
        val isPlayingReversed = currentlyPlayingPath == attempt.reversedAttemptFilePath
        val isPlayingThis = isPlayingForward || isPlayingReversed

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
                    verticalAlignment = Alignment.Top,
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
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { showRenameDialog = true }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Controls Row - 🔧 POLYMORPHIC: Each button tracks its own path
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
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

                            // 2. Play (Forward) - 🔧 POLYMORPHIC
                            SimpleGlowButton(
                                onClick = {
                                    when {
                                        isPlayingForward -> onPause()  // Toggle pause/resume
                                        else -> onPlay(attempt.attemptFilePath)
                                    }
                                },
                                size = 40.dp,
                                label = when {
                                    isPlayingForward && !isPaused -> "Pause"
                                    isPlayingForward && isPaused -> "Resume"
                                    else -> "Play"
                                },
                                icon = when {
                                    isPlayingForward && !isPaused -> Icons.Default.Pause
                                    else -> Icons.Default.PlayArrow
                                },
                                isPrimary = true
                            )

                            // 3. Reverse - 🔧 POLYMORPHIC
                            attempt.reversedAttemptFilePath?.let { reversedPath ->
                                SimpleGlowButton(
                                    onClick = {
                                        when {
                                            isPlayingReversed -> onPause()  // Toggle pause/resume
                                            else -> onPlay(reversedPath)
                                        }
                                    },
                                    size = 40.dp,
                                    label = when {
                                        isPlayingReversed && !isPaused -> "Pause"
                                        isPlayingReversed && isPaused -> "Resume"
                                        else -> "Rev"
                                    },
                                    icon = when {
                                        isPlayingReversed && !isPaused -> Icons.Default.Pause
                                        else -> Icons.Default.Replay
                                    },
                                    isPrimary = true
                                )
                            }

                            // 4. Delete
                            if (onDeleteAttempt != null) {
                                SimpleGlowButton(
                                    onClick = { showDeleteDialog = true },
                                    size = 40.dp,
                                    label = "Del",
                                    icon = Icons.Default.Delete,
                                    isPrimary = false
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Right Column: Score Badge
                    val displayScore = attempt.finalScore ?: attempt.score
                    DifficultySquircle(
                        score = displayScore,
                        difficulty = attempt.difficulty,
                        challengeType = attempt.challengeType,
                        emoji = aesthetic.scoreEmojis.entries.firstOrNull { displayScore >= it.key }?.value ?: "🎤",
                        isOverridden = attempt.finalScore != null,
                        width = 85.dp,
                        height = 110.dp,
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
                onOverrideScore = onOverrideScore ?: { },
                onResetScore = onResetScore ?: { }
            )
        }
    }

    // --- RECORD BUTTON ---

    @Composable
    fun MaterialRecordButton(
        isRecording: Boolean,
        countdownProgress: Float = 1f,
        onClick: () -> Unit
    ) {
        Box(
            modifier = Modifier.size(80.dp),
            contentAlignment = Alignment.Center
        ) {
            // Background arc (gray track)
            Canvas(modifier = Modifier.size(80.dp)) {
                drawArc(
                    color = Color.Gray.copy(alpha = 0.3f),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // Progress arc (depleting red arc during timed recording)
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
        onOverrideScore: (Int) -> Unit = { },
        onResetScore: () -> Unit = { }
    ) {
        ScoreExplanationDialog(
            attempt = attempt,
            onDismiss = onDismiss,
            onOverrideScore = onOverrideScore,
            onResetScore = onResetScore
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
                        android.util.Log.d("RENAME_DEBUG", "Dialog calling onRename with: $name")
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
                    disabledContainerColor = disabledColor,
                    contentColor = contentColor,
                    disabledContentColor = disabledContentColor
                )
            ) {
                Icon(icon, null)
            }

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

// =============================================================================
// 🎨 DEFAULT THEME COMPONENTS
// =============================================================================
/**
 * DRY Implementation for vanilla themes.
 *
 * All non-pro themes (Cottage, Cyberpunk, DarkAcademia, Graphite, Jeoseung,
 * Steampunk, Vaporwave, Y2K) use this single implementation instead of
 * duplicating ~100 lines of delegation boilerplate each.
 *
 * Pro themes implement ThemeComponents directly with custom UI.
 */
class DefaultThemeComponents : ThemeComponents {

    @Composable
    override fun RecordingItem(
        recording: Recording,
        aesthetic: AestheticThemeData,
        isPlaying: Boolean,
        isPaused: Boolean,
        progress: Float,
        currentlyPlayingPath: String?,
        onPlay: (String) -> Unit,
        onPause: () -> Unit,
        onStop: () -> Unit,
        onDelete: (Recording) -> Unit,
        onShare: (String) -> Unit,
        onRename: (String, String) -> Unit,
        isGameModeEnabled: Boolean,
        onStartAttempt: (Recording, ChallengeType) -> Unit
    ) {
        SharedDefaultComponents.MaterialRecordingCard(
            recording = recording,
            aesthetic = aesthetic,
            isPlaying = isPlaying,
            isPaused = isPaused,
            progress = progress,
            currentlyPlayingPath = currentlyPlayingPath,
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

    @Composable
    override fun AttemptItem(
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
        onOverrideScore: ((Int) -> Unit)?,
        onResetScore: (() -> Unit)?
    ) {
        SharedDefaultComponents.MaterialAttemptCard(
            attempt = attempt,
            aesthetic = aesthetic,
            currentlyPlayingPath = currentlyPlayingPath,
            isPaused = isPaused,
            progress = progress,
            onPlay = onPlay,
            onPause = onPause,
            onStop = onStop,
            onRenamePlayer = onRenamePlayer,
            onDeleteAttempt = onDeleteAttempt,
            onShareAttempt = onShareAttempt,
            onJumpToParent = onJumpToParent,
            onOverrideScore = onOverrideScore,
            onResetScore = onResetScore
        )
    }

    @Composable
    override fun RecordButton(
        isRecording: Boolean,
        isProcessing: Boolean,
        aesthetic: AestheticThemeData,
        onStartRecording: () -> Unit,
        onStopRecording: () -> Unit,
        countdownProgress: Float
    ) {
        SharedDefaultComponents.MaterialRecordButton(isRecording, countdownProgress) {
            if (isRecording) onStopRecording() else onStartRecording()
        }
    }

    @Composable
    override fun AppBackground(
        aesthetic: AestheticThemeData,
        content: @Composable () -> Unit
    ) {
        SharedDefaultComponents.GradientBackground(aesthetic, content)
    }

    @Composable
    override fun ScoreCard(
        attempt: PlayerAttempt,
        aesthetic: AestheticThemeData,
        onDismiss: () -> Unit,
        onOverrideScore: (Int) -> Unit
    ) {
        SharedDefaultComponents.MaterialScoreCard(attempt, aesthetic, onDismiss, onOverrideScore)
    }

    @Composable
    override fun DeleteDialog(
        itemType: DeletableItemType,
        item: Any,
        aesthetic: AestheticThemeData,
        onConfirm: () -> Unit,
        onDismiss: () -> Unit
    ) {
        SharedDefaultComponents.MaterialDeleteDialog(itemType, item, aesthetic, onConfirm, onDismiss)
    }

    @Composable
    override fun ShareDialog(
        recording: Recording?,
        attempt: PlayerAttempt?,
        aesthetic: AestheticThemeData,
        onShare: (String) -> Unit,
        onDismiss: () -> Unit
    ) {
        SharedDefaultComponents.MaterialShareDialog(recording, attempt, aesthetic, onShare, onDismiss)
    }

    @Composable
    override fun RenameDialog(
        itemType: RenamableItemType,
        currentName: String,
        aesthetic: AestheticThemeData,
        onRename: (String) -> Unit,
        onDismiss: () -> Unit
    ) {
        SharedDefaultComponents.MaterialRenameDialog(itemType, currentName, aesthetic, onRename, onDismiss)
    }
}