package com.quokkalabs.reversey.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import androidx.compose.foundation.isSystemInDarkTheme
import com.quokkalabs.reversey.ui.theme.LocalIsDarkTheme


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
        onStartAttempt: (Recording, ChallengeType) -> Unit,
        activeAttemptRecordingPath: String? = null,  // 🎯 Which recording has active attempt
        onStopAttempt: (() -> Unit)? = null,         // 🎯 Stop attempt callback
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
                containerColor = if (LocalIsDarkTheme.current) {
                    aesthetic.cardBackgroundDark ?: MaterialTheme.colorScheme.surface
                } else {
                    aesthetic.cardBackgroundLight ?: MaterialTheme.colorScheme.surface
                }
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

                    // 2. Play (Forward) OR Stop (when Reversed is playing)
                    if (isPlayingReversed) {
                        // Reversed is playing - this button becomes Stop
                        SimpleGlowButton(
                            onClick = onStop,
                            size = 50.dp,
                            label = "Stop",
                            isPrimary = false,
                            icon = Icons.Default.Stop
                        )
                    } else {
                        // Normal Play/Pause button
                        SimpleGlowButton(
                            onClick = {
                                when {
                                    isPlayingForward -> onPause()
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
                    }

                    // 3. Rewind (Reversed) OR Stop (when Forward is playing)
                    if (isPlayingForward) {
                        // Forward is playing - this button becomes Stop
                        SimpleGlowButton(
                            onClick = onStop,
                            size = 50.dp,
                            label = "Stop",
                            isPrimary = false,
                            icon = Icons.Default.Stop
                        )
                    } else {
                        // Normal Rewind/Pause button
                        SimpleGlowButton(
                            onClick = {
                                when {
                                    isPlayingReversed -> onPause()
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
                    }

                    // 4. Game Mode Button - 🎯 POLYMORPHIC: Try → Stop when recording attempt
                    if (isGameModeEnabled) {
                        val isAttemptingThis = activeAttemptRecordingPath == recording.originalPath

                        if (isAttemptingThis && onStopAttempt != null) {
                            // 🛑 STOP BUTTON with pulsing icon
                            val infiniteTransition = rememberInfiniteTransition(label = "stopPulse")
                            val iconAlpha by infiniteTransition.animateFloat(
                                initialValue = 1f,
                                targetValue = 0.4f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(500),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "iconAlpha"
                            )

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Button(
                                    onClick = onStopAttempt,
                                    modifier = Modifier.size(50.dp),
                                    shape = CircleShape,
                                    contentPadding = PaddingValues(0.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Icon(
                                        Icons.Default.Stop,
                                        contentDescription = "Stop",
                                        modifier = Modifier.alpha(iconAlpha)
                                    )
                                }
                                Text(
                                    text = "Stop",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            // 🎤 TRY BUTTON (normal)
                            SimpleGlowButton(
                                onClick = { onStartAttempt(recording, ChallengeType.REVERSE) },
                                size = 50.dp,
                                label = "Try",
                                icon = Icons.Default.Mic,
                                isPrimary = true
                            )
                        }
                    }

                    // 5. Delete
                    SimpleGlowButton(
                        onClick = { showDeleteDialog = true },
                        size = 50.dp,
                        label = "Del",
                        icon = Icons.Default.Delete,
                        isDestructive = true
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
        onResetScore: (() -> Unit)? = null,
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
                containerColor = if (LocalIsDarkTheme.current) {
                    aesthetic.cardBackgroundDark ?: MaterialTheme.colorScheme.surface
                } else {
                    aesthetic.cardBackgroundLight ?: MaterialTheme.colorScheme.surface
                }
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
                                IconButton(
                                    onClick = onJumpToParent,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Home,
                                        "Parent",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                            }

                            Text(
                                text = attempt.playerName,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
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

                            // 2. Play (Forward) OR Stop (when Reversed is playing)
                            if (isPlayingReversed) {
                                // Reversed is playing - this button becomes Stop
                                SimpleGlowButton(
                                    onClick = onStop,
                                    size = 40.dp,
                                    label = "Stop",
                                    isPrimary = false,
                                    icon = Icons.Default.Stop
                                )
                            } else {
                                // Normal Play/Pause button
                                SimpleGlowButton(
                                    onClick = {
                                        when {
                                            isPlayingForward -> onPause()
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
                            }

                            // 3. Reverse OR Stop (when Forward is playing)
                            attempt.reversedAttemptFilePath?.let { reversedPath ->
                                if (isPlayingForward) {
                                    // Forward is playing - this button becomes Stop
                                    SimpleGlowButton(
                                        onClick = onStop,
                                        size = 40.dp,
                                        label = "Stop",
                                        isPrimary = false,
                                        icon = Icons.Default.Stop
                                    )
                                } else {
                                    // Normal Rev/Pause button
                                    SimpleGlowButton(
                                        onClick = {
                                            when {
                                                isPlayingReversed -> onPause()
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
                            }

                            // 4. Delete
                            if (onDeleteAttempt != null) {
                                SimpleGlowButton(
                                    onClick = { showDeleteDialog = true },
                                    size = 40.dp,
                                    label = "Del",
                                    icon = Icons.Default.Delete,
                                    isDestructive = true
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
                        emoji = aesthetic.scoreEmojis.entries.firstOrNull { displayScore >= it.key }?.value
                            ?: "🎤",
                        isOverridden = attempt.finalScore != null,
                        width = 85.dp,
                        height = 110.dp,
                        onClick = { showScoreDialog = true }
                    )
                }

                if (isPlayingThis) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
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
        onClick: () -> Unit,
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "recordPulse")
        val iconAlpha by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 0.4f,
            animationSpec = infiniteRepeatable(
                animation = tween(500),
                repeatMode = RepeatMode.Reverse
            ),
            label = "iconAlpha"
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier.size(80.dp),
                contentAlignment = Alignment.Center
            ) {
                FloatingActionButton(
                    onClick = onClick,
                    modifier = Modifier.size(64.dp),
                    containerColor = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = if (isRecording) "Stop" else "Record",
                        modifier = Modifier
                            .size(28.dp)
                            .alpha(if (isRecording) iconAlpha else 1f)
                    )
                }
            }
            Text(
                text = if (isRecording) "Stop" else "Record",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }

    // --- BACKGROUND ---

    @Composable
    fun GradientBackground(
        aesthetic: AestheticThemeData,
        content: @Composable () -> Unit,
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
        onResetScore: () -> Unit = { },
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
        onDismiss: () -> Unit,
    ) {
        val itemName = when (item) {
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
                TextButton(onClick = onDismiss) {
                    Text(copy.deleteCancelButton, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    @Composable
    fun MaterialShareDialog(
        recording: Recording?,
        attempt: PlayerAttempt?,
        aesthetic: AestheticThemeData,
        onShare: (String) -> Unit,
        onDismiss: () -> Unit,
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
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    @Composable
    fun MaterialRenameDialog(
        itemType: RenamableItemType,
        currentName: String,
        aesthetic: AestheticThemeData,
        onRename: (String) -> Unit,
        onDismiss: () -> Unit,
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
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    // --- MENU COMPONENTS ---

    @Composable
    fun ThemedSettingsCard(
        aesthetic: AestheticThemeData,
        title: String? = null,
        content: @Composable ColumnScope.() -> Unit,
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
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    colors.menuBorder.copy(alpha = 0.3f)
                )
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
        onCheckedChange: (Boolean) -> Unit,
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
        isDestructive: Boolean = false,
        enabled: Boolean = true,
        size: Dp,
        label: String,
        icon: androidx.compose.ui.graphics.vector.ImageVector,
    ) {
        val containerColor = when {
            isDestructive -> MaterialTheme.colorScheme.error
            isPrimary -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.secondaryContainer
        }

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
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(
                    alpha = 0.4f
                ),
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
        onStartAttempt: (Recording, ChallengeType) -> Unit,
        activeAttemptRecordingPath: String?,
        onStopAttempt: (() -> Unit)?,
    ) {
        SharedDefaultComponents.MaterialRecordingCard(
            recording = recording,
            aesthetic = aesthetic,

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
            onStartAttempt = onStartAttempt,
            activeAttemptRecordingPath = activeAttemptRecordingPath,
            onStopAttempt = onStopAttempt
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
        onResetScore: (() -> Unit)?,
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

        ) {
        SharedDefaultComponents.MaterialRecordButton(isRecording) {
            if (isRecording) onStopRecording() else onStartRecording()
        }
    }

    @Composable
    override fun AppBackground(
        aesthetic: AestheticThemeData,
        content: @Composable () -> Unit,
    ) {
        SharedDefaultComponents.GradientBackground(aesthetic, content)
    }

    @Composable
    override fun ScoreCard(
        attempt: PlayerAttempt,
        aesthetic: AestheticThemeData,
        onDismiss: () -> Unit,
        onOverrideScore: (Int) -> Unit,
    ) {
        SharedDefaultComponents.MaterialScoreCard(attempt, aesthetic, onDismiss, onOverrideScore)
    }

    @Composable
    override fun DeleteDialog(
        itemType: DeletableItemType,
        item: Any,
        aesthetic: AestheticThemeData,
        onConfirm: () -> Unit,
        onDismiss: () -> Unit,
    ) {
        SharedDefaultComponents.MaterialDeleteDialog(
            itemType,
            item,
            aesthetic,
            onConfirm,
            onDismiss
        )
    }

    @Composable
    override fun ShareDialog(
        recording: Recording?,
        attempt: PlayerAttempt?,
        aesthetic: AestheticThemeData,
        onShare: (String) -> Unit,
        onDismiss: () -> Unit,
    ) {
        SharedDefaultComponents.MaterialShareDialog(
            recording,
            attempt,
            aesthetic,
            onShare,
            onDismiss
        )
    }

    @Composable
    override fun RenameDialog(
        itemType: RenamableItemType,
        currentName: String,
        aesthetic: AestheticThemeData,
        onRename: (String) -> Unit,
        onDismiss: () -> Unit,
    ) {
        SharedDefaultComponents.MaterialRenameDialog(
            itemType,
            currentName,
            aesthetic,
            onRename,
            onDismiss
        )
    }
}