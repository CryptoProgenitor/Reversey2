package com.example.reversey.ui.theme

import android.content.Context
import android.graphics.Paint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import com.example.reversey.data.models.ChallengeType
import com.example.reversey.data.models.PlayerAttempt
import com.example.reversey.data.models.Recording
import com.example.reversey.scoring.DifficultyConfig
import com.example.reversey.ui.components.DifficultySquircle
import com.example.reversey.ui.components.ScoreExplanationDialog
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * 🥚 EGG THEME - PRO
 * Hand-drawn aesthetic with fried eggs, sticky notes, and bouncing eggs!
 */
object EggTheme {
    const val THEME_ID = "egg"

    val data = AestheticThemeData(
        id = THEME_ID,
        name = "Egg Theme",
        description = "🥚 CPD's adorable egg-inspired design!",
        components = EggThemeComponents(),

        // Visuals
        primaryGradient = Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFFF8E1), // Cream background
                Color(0xFFFFF3E0), // Slightly warmer cream
                Color(0xFFFFE0B2)  // Light orange
            )
        ),
        cardBorder = Color(0xFF2E2E2E),
        primaryTextColor = Color(0xFF2E2E2E),
        secondaryTextColor = Color(0xFF424242),
        useGlassmorphism = false,
        glowIntensity = 0f,
        useSerifFont = false,
        useWideLetterSpacing = true,
        recordButtonEmoji = "🥚",
        scoreEmojis = mapOf(
            90 to "🍳", // Fried egg for perfect!
            80 to "🥚", // Whole egg for great
            70 to "🐣", // Hatching for good
            60 to "🐥", // Chick for okay
            0 to "🥀"   // Wilted for poor
        ),

        // M3 Overrides
        cardAlpha = 1f,
        shadowElevation = 6f,
        useHandDrawnBorders = true,
        borderWidth = 4f,

        // Interaction
        dialogCopy = DialogCopy(
            deleteTitle = { type -> if (type == DeletableItemType.RECORDING) "Crack this egg? 🥚" else "Scramble attempt? 🍳" },
            deleteMessage = { type, name -> "\"$name\" will be cracked forever! 💔" },
            deleteConfirmButton = "Crack It",
            deleteCancelButton = "Keep Egg",
            shareTitle = "Share Fresh Egg 📤",
            shareMessage = "How would you like to serve this?",
            renameTitle = { "Name Your Egg ✏️" },
            renameHint = "Eggy McEggface"
        ),
        scoreFeedback = ScoreFeedback(
            getTitle = { score ->
                when {
                    score >= 90 -> "Egg-straordinary! 🍳"
                    score >= 80 -> "Egg-cellent! 🥚"
                    score >= 70 -> "Well done! 🐣"
                    score >= 60 -> "Soft boiled! 🐥"
                    else -> "Still hatching... 🥀"
                }
            },
            getMessage = { score ->
                when {
                    score >= 90 -> "Chef's kiss! Perfection!"
                    score >= 80 -> "A very tasty performance."
                    score >= 70 -> "Coming out of your shell!"
                    score >= 60 -> "Needs a pinch of salt."
                    else -> "Don't walk on eggshells, try again!"
                }
            },
            getEmoji = { score ->
                when {
                    score >= 90 -> "🍳"
                    score >= 80 -> "🥚"
                    score >= 70 -> "🐣"
                    score >= 60 -> "🐥"
                    else -> "🥀"
                }
            }
        ),
        menuColors = MenuColors.fromColors(
            primaryText = Color(0xFF2E2E2E),
            secondaryText = Color(0xFF424242),
            border = Color(0xFF2E2E2E),
            gradient = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFFFF8E1),
                    Color(0xFFFFE0B2)
                )
            )
        )
    )
}

class EggThemeComponents : ThemeComponents {

    @Composable
    override fun RecordingItem(
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
        EggRecordingItem(
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
        onJumpToParent: (() -> Unit)?
    ) {
        EggAttemptItem(
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
            onJumpToParent = onJumpToParent
        )
    }

    @Composable
    override fun RecordButton(
        isRecording: Boolean,
        isProcessing: Boolean,
        aesthetic: AestheticThemeData,
        onStartRecording: () -> Unit,
        onStopRecording: () -> Unit
    ) {
        EggRecordButton(
            isRecording = isRecording,
            onClick = {
                if (isRecording) {
                    onStopRecording()
                } else {
                    onStartRecording()
                }
            }
        )
    }

    @Composable
    override fun AppBackground(
        aesthetic: AestheticThemeData,
        content: @Composable () -> Unit
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(aesthetic.primaryGradient)
        ) {
            // Bouncing eggs behind all content
            BouncingEggs(floorHeightOffset = 90.dp)
            content()
        }
    }

    // --- NEW INTERFACE METHODS ---

    @Composable
    override fun ScoreCard(
        attempt: PlayerAttempt,
        aesthetic: AestheticThemeData,
        onDismiss: () -> Unit
    ) {
        ScoreExplanationDialog(attempt, onDismiss)
    }

    @Composable
    override fun DeleteDialog(
        itemType: DeletableItemType,
        item: Any,
        aesthetic: AestheticThemeData,
        onConfirm: () -> Unit,
        onDismiss: () -> Unit
    ) {
        val copy = aesthetic.dialogCopy
        val name = if (item is Recording) item.name else if (item is PlayerAttempt) item.playerName else "Item"

        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = Color(0xFFFFFBF0),
            title = { Text(copy.deleteTitle(itemType), color = Color(0xFF2E2E2E), fontWeight = FontWeight.Bold) },
            text = { Text(copy.deleteMessage(itemType, name), color = Color(0xFF2E2E2E)) },
            confirmButton = {
                Button(onClick = { onConfirm(); onDismiss() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722))) {
                    Text(copy.deleteConfirmButton)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text(copy.deleteCancelButton, color = Color(0xFF2E2E2E)) }
            }
        )
    }

    @Composable
    override fun ShareDialog(
        recording: Recording?,
        attempt: PlayerAttempt?,
        aesthetic: AestheticThemeData,
        onShare: (String) -> Unit,
        onDismiss: () -> Unit
    ) {
        val copy = aesthetic.dialogCopy
        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = Color(0xFFFFFBF0),
            title = { Text(copy.shareTitle, color = Color(0xFF2E2E2E), fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(copy.shareMessage, color = Color(0xFF2E2E2E))
                    Spacer(modifier = Modifier.height(16.dp))
                    val path = recording?.originalPath ?: attempt?.attemptFilePath ?: ""
                    Button(onClick = { onShare(path); onDismiss() }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0))) {
                        Text("Share Original 🥚")
                    }
                    val revPath = recording?.reversedPath ?: attempt?.reversedAttemptFilePath
                    if (revPath != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { onShare(revPath); onDismiss() }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8A65))) {
                            Text("Share Reversed 🍳")
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = Color(0xFF2E2E2E)) } }
        )
    }

    @Composable
    override fun RenameDialog(
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
            containerColor = Color(0xFFFFFBF0),
            title = { Text(copy.renameTitle(itemType), color = Color(0xFF2E2E2E), fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = name, onValueChange = { name = it }, singleLine = true,
                    label = { Text(copy.renameHint) }
                )
            },
            confirmButton = {
                Button(onClick = { onRename(name); onDismiss() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8A65))) {
                    Text("Save")
                }
            },
            dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = Color(0xFF2E2E2E)) } }
        )
    }
}

// ============================================
// EGG RECORDING ITEM
// ============================================

@Composable
fun EggRecordingItem(
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

    // 🛡️ FIX: Check if file is ready
    val isReady = recording.reversedPath != null

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(elevation = 0.dp, shape = RoundedCornerShape(16.dp))
            .border(width = 4.dp, color = Color(0xFF2E2E2E), shape = RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBF0)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FriedEggDecoration(size = 35.dp, rotation = 15f)
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = recording.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = Color(0xFF2E2E2E),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.clickable { showRenameDialog = true }
                    )
                    Text(
                        text = "Fresh from the nest! 🍳",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF2E2E2E).copy(alpha = 0.7f)
                    )
                }
                FriedEggDecoration(size = 35.dp, rotation = -20f)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, Color(0xFF2E2E2E), RoundedCornerShape(4.dp))
                    .padding(2.dp)
            ) {
                EggTravelProgressBar(
                    progress = if (isPlaying) progress else 0f,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    HandDrawnEggButton(
                        onClick = { showShareDialog = true },
                        backgroundColor = Color(0xFF9C27B0),
                        size = 50.dp
                    ) { EggShareIcon(Color(0xFF2E2E2E)) }
                    Text("Share", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFF2E2E2E))
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    HandDrawnEggButton(
                        onClick = {
                            if (isPlaying && !isPaused) onPause()
                            else onPlay(recording.originalPath ?: "")
                        },
                        backgroundColor = Color(0xFFFF8A65),
                        size = 50.dp
                    ) {
                        if (isPlaying && !isPaused) EggPauseIcon(Color(0xFF2E2E2E)) else EggPlayIcon(Color(0xFF2E2E2E))
                    }
                    Text(if (isPlaying && !isPaused) "Pause" else "Play", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFF2E2E2E))
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    HandDrawnEggButton(
                        onClick = { onPlay(recording.reversedPath ?: "") },
                        backgroundColor = Color(0xFFFF8A65),
                        enabled = isReady, // 🛡️ FIX: Protected
                        size = 50.dp
                    ) { EggRewindIcon(Color(0xFF2E2E2E)) }
                    Text("Rewind", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFF2E2E2E))
                }

                if (isGameModeEnabled) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        HandDrawnEggButton(
                            onClick = { onStartAttempt(recording, ChallengeType.FORWARD) },
                            backgroundColor = Color(0xFFFF8A65),
                            enabled = isReady, // 🛡️ FIX: Protected
                            size = 50.dp
                        ) { EggMicRightArrowIcon(Color(0xFF2E2E2E)) }
                        Text("Fwd", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFF2E2E2E))
                    }
                }

                if (isGameModeEnabled) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        HandDrawnEggButton(
                            onClick = { onStartAttempt(recording, ChallengeType.REVERSE) },
                            backgroundColor = Color(0xFFFF8A65),
                            enabled = isReady, // 🛡️ FIX: Protected
                            size = 50.dp
                        ) { EggMicLeftArrowIcon(Color(0xFF2E2E2E)) }
                        Text("Rev", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFF2E2E2E))
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    HandDrawnEggButton(
                        onClick = { showDeleteDialog = true },
                        backgroundColor = Color(0xFFFF5722),
                        size = 50.dp
                    ) { CrackedEggIcon() }
                    Text("Del", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFF2E2E2E))
                }
            }
        }
    }

    if (showRenameDialog) aesthetic.components.RenameDialog(RenamableItemType.RECORDING, recording.name, aesthetic, { onRename(recording.originalPath, it) }, { showRenameDialog = false })
    if (showDeleteDialog) aesthetic.components.DeleteDialog(DeletableItemType.RECORDING, recording, aesthetic, { onDelete(recording) }, { showDeleteDialog = false })
    if (showShareDialog) aesthetic.components.ShareDialog(recording, null, aesthetic, onShare, { showShareDialog = false })
}

// ============================================
// ATTEMPT ITEM
// ============================================

@Composable
fun EggAttemptItem(
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
    onJumpToParent: (() -> Unit)?
) {
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    var showScoreDialog by remember { mutableStateOf(false) }

    val isPlayingThis = currentlyPlayingPath == attempt.attemptFilePath || currentlyPlayingPath == attempt.reversedAttemptFilePath
    val eggEmoji = aesthetic.scoreEmojis.entries.sortedByDescending { it.key }.firstOrNull { attempt.score.toInt() >= it.key }?.value ?: "🥚"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 34.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
            .shadow(elevation = 0.dp, shape = RoundedCornerShape(16.dp))
            .border(width = 4.dp, color = Color(0xFF2E2E2E), shape = RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBF0)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Left side: Sticky note + buttons
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HandDrawnHouseIcon(
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { onJumpToParent?.invoke() }
                        )

                        Box(
                            modifier = Modifier
                                .rotate(-8f)
                                .background(Color(0xFFFFF176), RoundedCornerShape(4.dp))
                                .border(2.dp, Color(0xFF2E2E2E), RoundedCornerShape(4.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .clickable { showRenameDialog = true }
                        ) {
                            Text(
                                text = attempt.playerName,
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp),
                                color = Color(0xFF2E2E2E),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.widthIn(max = 120.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (onShareAttempt != null) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                HandDrawnEggButton(onClick = { showShareDialog = true }, backgroundColor = Color(0xFF9C27B0), size = 40.dp) { EggShareIcon(Color(0xFF2E2E2E)) }
                                Text("Share", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp), color = Color(0xFF2E2E2E))
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            HandDrawnEggButton(
                                onClick = {
                                    if (isPlayingThis && !isPaused) onPause() else onPlay(attempt.attemptFilePath)
                                },
                                backgroundColor = Color(0xFFFF8A65),
                                size = 40.dp
                            ) { EggPlayIcon(Color(0xFF2E2E2E)) }
                            Text("Play", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp), color = Color(0xFF2E2E2E))
                        }

                        if (attempt.reversedAttemptFilePath != null) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                HandDrawnEggButton(onClick = { onPlay(attempt.reversedAttemptFilePath!!) }, backgroundColor = Color(0xFFFF8A65), size = 40.dp) { EggReverseIcon(Color(0xFF2E2E2E)) }
                                Text("Rev", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp), color = Color(0xFF2E2E2E))
                            }
                        }

                        if (onDeleteAttempt != null) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                HandDrawnEggButton(onClick = { showDeleteDialog = true }, backgroundColor = Color(0xFFFF5722), size = 40.dp) { CrackedEggIcon() }
                                Text("Del", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp), color = Color(0xFF2E2E2E))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                EggScoreCircle(
                    score = "${attempt.score.toInt()}%",
                    difficulty = attempt.difficulty,
                    challengeType = attempt.challengeType,
                    eggEmoji = eggEmoji,
                    size = 100.dp,
                    onClick = { showScoreDialog = true }
                )
            }

            if (isPlayingThis) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(2.dp, Color(0xFF2E2E2E), RoundedCornerShape(4.dp))
                            .padding(2.dp)
                    ) {
                        EggTravelProgressBar(progress = progress, modifier = Modifier.fillMaxWidth())
                    }
                    Spacer(modifier = Modifier.width(12.dp + 100.dp)) // Space for squircle
                }
            }
        }
    }

    if (showRenameDialog && onRenamePlayer != null) aesthetic.components.RenameDialog(RenamableItemType.PLAYER, attempt.playerName, aesthetic, { onRenamePlayer(attempt, it) }, { showRenameDialog = false })
    if (showDeleteDialog && onDeleteAttempt != null) aesthetic.components.DeleteDialog(DeletableItemType.ATTEMPT, attempt, aesthetic, { onDeleteAttempt(attempt) }, { showDeleteDialog = false })
    if (showShareDialog && onShareAttempt != null) aesthetic.components.ShareDialog(null, attempt, aesthetic, onShareAttempt, { showShareDialog = false })
    if (showScoreDialog) aesthetic.components.ScoreCard(attempt, aesthetic, { showScoreDialog = false })
}

// ============================================
// HELPERS & ICONS
// ============================================

@Composable
fun EggScoreCircle(
    score: String,
    difficulty: com.example.reversey.scoring.DifficultyLevel,
    eggEmoji: String,
    challengeType: ChallengeType,
    size: Dp,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scoreValue = score.replace("%", "").toIntOrNull() ?: 0

    DifficultySquircle(
        score = scoreValue,
        difficulty = difficulty,
        emoji = eggEmoji,
        challengeType = challengeType,
        width = size,
        height = (size.value * 1.3f).dp,
        onClick = onClick,
        modifier = modifier
    )
}


@Composable
fun FriedEggDecoration(size: Dp, rotation: Float = 0f) {
    Canvas(modifier = Modifier.size(size).rotate(rotation)) {
        val center = this.center
        val radius = this.size.minDimension / 2
        drawCircle(color = Color(0xFFFFF8E1), radius = radius * 0.9f, center = center)
        drawCircle(color = Color(0xFF2E2E2E), radius = radius * 0.9f, center = center, style = Stroke(2.dp.toPx()))
        drawCircle(color = Color(0xFFFFD700), radius = radius * 0.4f, center = center)
        drawCircle(color = Color(0xFF2E2E2E), radius = radius * 0.4f, center = center, style = Stroke(1.dp.toPx()))
    }
}

@Composable
fun EggTravelProgressBar(progress: Float, modifier: Modifier = Modifier, height: Dp = 10.dp) {
    BoxWithConstraints(modifier = modifier.height(height)) {
        val trackWidth = maxWidth
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFFFFE0B2), RoundedCornerShape(height / 2)))
        Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(progress.coerceIn(0f, 1f)).background(Color(0xFFFF8A65), RoundedCornerShape(height / 2)))
        if (progress > 0f) {
            val eggSize = 120.dp
            val eggPosition = progress.coerceIn(0f, 1f).times(trackWidth - eggSize)
            Box(modifier = Modifier.size(eggSize).offset(x = eggPosition, y = -2.dp), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(90.dp)) {
                    val center = this.center
                    val radius = this.size.minDimension / 2
                    drawOval(color = Color(0xFFFFF8E1), topLeft = Offset(center.x - radius * 0.7f, center.y - radius * 0.9f), size = androidx.compose.ui.geometry.Size(radius * 1.4f, radius * 1.8f))
                    drawOval(color = Color(0xFF2E2E2E), topLeft = Offset(center.x - radius * 0.7f, center.y - radius * 0.9f), size = androidx.compose.ui.geometry.Size(radius * 1.4f, radius * 1.8f), style = Stroke(2.dp.toPx()))
                    drawCircle(color = Color(0xFFFFD700), radius = radius * 0.4f, center = center)
                    drawCircle(color = Color(0xFF2E2E2E), radius = radius * 0.4f, center = center, style = Stroke(1.dp.toPx()))
                }
            }
        }
    }
}

@Composable
fun HandDrawnEggButton(onClick: () -> Unit, backgroundColor: Color, size: Dp, enabled: Boolean = true, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier.size(size).background(if (enabled) backgroundColor else backgroundColor.copy(alpha = 0.4f), CircleShape).border(3.dp, Color(0xFF2E2E2E), CircleShape).clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) { content() }
}

// ============================================
// ICONS - Egg Theme Icons
// ============================================

@Composable
fun EggShareIcon(color: Color) {
    Canvas(modifier = Modifier.size(32.dp)) {
        val strokeWidth = 2.dp.toPx()
        val radius = size.width * 0.083f
        drawCircle(color = color, radius = radius, center = Offset(size.width * 0.25f, size.height * 0.5f), style = Stroke(width = strokeWidth))
        drawCircle(color = color, radius = radius, center = Offset(size.width * 0.75f, size.height * 0.25f), style = Stroke(width = strokeWidth))
        drawCircle(color = color, radius = radius, center = Offset(size.width * 0.75f, size.height * 0.75f), style = Stroke(width = strokeWidth))
        drawLine(color = color, start = Offset(size.width * 0.33f, size.height * 0.5f), end = Offset(size.width * 0.67f, size.height * 0.29f), strokeWidth = strokeWidth)
        drawLine(color = color, start = Offset(size.width * 0.33f, size.height * 0.5f), end = Offset(size.width * 0.67f, size.height * 0.71f), strokeWidth = strokeWidth)
    }
}

@Composable
fun EggPlayIcon(color: Color) {
    Canvas(modifier = Modifier.size(32.dp)) {
        val path = Path().apply {
            moveTo(size.width * 0.29f, size.height * 0.17f)
            lineTo(size.width * 0.29f, size.height * 0.83f)
            lineTo(size.width * 0.79f, size.height * 0.5f)
            close()
        }
        drawPath(path = path, color = color, style = Stroke(width = 2.dp.toPx(), join = StrokeJoin.Miter))
    }
}

@Composable
fun EggPauseIcon(color: Color) {
    Canvas(modifier = Modifier.size(32.dp)) {
        val barWidth = size.width * 0.12f
        val barHeight = size.height * 0.6f
        val topY = size.height * 0.2f
        drawRect(color = color, topLeft = Offset(size.width * 0.3f, topY), size = Size(barWidth, barHeight), style = Stroke(width = 2.dp.toPx()))
        drawRect(color = color, topLeft = Offset(size.width * 0.58f, topY), size = Size(barWidth, barHeight), style = Stroke(width = 2.dp.toPx()))
    }
}

@Composable
fun EggRewindIcon(color: Color) {
    Canvas(modifier = Modifier.size(32.dp)) {
        val strokeWidth = 2.dp.toPx()
        val path1 = Path().apply {
            moveTo(size.width * 0.54f, size.height * 0.71f)
            lineTo(size.width * 0.29f, size.height * 0.5f)
            lineTo(size.width * 0.54f, size.height * 0.29f)
        }
        drawPath(path = path1, color = color, style = Stroke(width = strokeWidth, cap = StrokeCap.Square))
        val path2 = Path().apply {
            moveTo(size.width * 0.83f, size.height * 0.71f)
            lineTo(size.width * 0.58f, size.height * 0.5f)
            lineTo(size.width * 0.83f, size.height * 0.29f)
        }
        drawPath(path = path2, color = color, style = Stroke(width = strokeWidth, cap = StrokeCap.Square))
    }
}

@Composable
fun EggReverseIcon(color: Color) {
    Canvas(modifier = Modifier.size(32.dp)) {
        val strokeWidth = 2.dp.toPx()
        val leftArc = Path().apply {
            moveTo(size.width * 0.5f, size.height * 0.17f)
            cubicTo(size.width * 0.29f, size.height * 0.17f, size.width * 0.17f, size.height * 0.29f, size.width * 0.17f, size.height * 0.5f)
            cubicTo(size.width * 0.17f, size.height * 0.71f, size.width * 0.29f, size.height * 0.83f, size.width * 0.5f, size.height * 0.83f)
        }
        drawPath(path = leftArc, color = color, style = Stroke(width = strokeWidth))
        val arrowPath = Path().apply {
            moveTo(size.width * 0.5f, size.height * 0.17f)
            lineTo(size.width * 0.5f, size.height * 0.33f)
            lineTo(size.width * 0.33f, size.height * 0.17f)
            lineTo(size.width * 0.5f, size.height * 0.17f)
        }
        drawPath(path = arrowPath, color = color, style = Fill)
        val rightArc = Path().apply {
            moveTo(size.width * 0.5f, size.height * 0.83f)
            cubicTo(size.width * 0.71f, size.height * 0.83f, size.width * 0.83f, size.height * 0.71f, size.width * 0.83f, size.height * 0.5f)
            cubicTo(size.width * 0.83f, size.height * 0.29f, size.width * 0.71f, size.height * 0.17f, size.width * 0.5f, size.height * 0.17f)
        }
        drawPath(path = rightArc, color = color, style = Stroke(width = strokeWidth))
    }
}

@Composable
fun EggMicRightArrowIcon(color: Color) {
    Canvas(modifier = Modifier.size(32.dp)) {
        val strokeWidth = 2.5.dp.toPx()
        drawRoundRect(color = color, topLeft = Offset(size.width * 0.10f, size.height * 0.15f), size = Size(size.width * 0.22f, size.height * 0.45f), cornerRadius = CornerRadius(4.dp.toPx()), style = Stroke(width = strokeWidth))
        drawLine(color = color, start = Offset(size.width * 0.21f, size.height * 0.60f), end = Offset(size.width * 0.21f, size.height * 0.75f), strokeWidth = strokeWidth)
        drawLine(color = color, start = Offset(size.width * 0.14f, size.height * 0.75f), end = Offset(size.width * 0.28f, size.height * 0.75f), strokeWidth = strokeWidth)
        val arrow1 = Path().apply {
            moveTo(size.width * 0.48f, size.height * 0.15f)
            lineTo(size.width * 0.68f, size.height * 0.375f)
            lineTo(size.width * 0.48f, size.height * 0.60f)
        }
        val arrow2 = Path().apply {
            moveTo(size.width * 0.68f, size.height * 0.15f)
            lineTo(size.width * 0.88f, size.height * 0.375f)
            lineTo(size.width * 0.68f, size.height * 0.60f)
        }
        drawPath(arrow1, color, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawPath(arrow2, color, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
fun EggMicLeftArrowIcon(color: Color) {
    Canvas(modifier = Modifier.size(32.dp)) {
        val strokeWidth = 2.5.dp.toPx()
        drawRoundRect(color = color, topLeft = Offset(size.width * 0.68f, size.height * 0.15f), size = Size(size.width * 0.22f, size.height * 0.45f), cornerRadius = CornerRadius(4.dp.toPx()), style = Stroke(width = strokeWidth))
        drawLine(color = color, start = Offset(size.width * 0.79f, size.height * 0.60f), end = Offset(size.width * 0.79f, size.height * 0.75f), strokeWidth = strokeWidth)
        drawLine(color = color, start = Offset(size.width * 0.72f, size.height * 0.75f), end = Offset(size.width * 0.86f, size.height * 0.75f), strokeWidth = strokeWidth)
        val arrow1 = Path().apply {
            moveTo(size.width * 0.52f, size.height * 0.15f)
            lineTo(size.width * 0.32f, size.height * 0.375f)
            lineTo(size.width * 0.52f, size.height * 0.60f)
        }
        val arrow2 = Path().apply {
            moveTo(size.width * 0.32f, size.height * 0.15f)
            lineTo(size.width * 0.12f, size.height * 0.375f)
            lineTo(size.width * 0.32f, size.height * 0.60f)
        }
        drawPath(arrow1, color, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawPath(arrow2, color, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
fun CrackedEggIcon() {
    Canvas(modifier = Modifier.size(20.dp).rotate(180f)) {
        val color = Color.White
        val strokeWidth = 2.5.dp.toPx()
        val leftPath = Path().apply {
            moveTo(size.width * 0.5f, size.height * 0.1f)
            quadraticBezierTo(size.width * 0.2f, size.height * 0.2f, size.width * 0.15f, size.height * 0.4f)
            quadraticBezierTo(size.width * 0.1f, size.height * 0.7f, size.width * 0.4f, size.height * 0.9f)
            lineTo(size.width * 0.5f, size.height * 0.1f)
        }
        val rightPath = Path().apply {
            moveTo(size.width * 0.5f, size.height * 0.1f)
            quadraticBezierTo(size.width * 0.8f, size.height * 0.2f, size.width * 0.85f, size.height * 0.4f)
            quadraticBezierTo(size.width * 0.9f, size.height * 0.7f, size.width * 0.6f, size.height * 0.9f)
            lineTo(size.width * 0.5f, size.height * 0.1f)
        }
        drawPath(leftPath, color)
        drawPath(leftPath, Color(0xFF2E2E2E), style = Stroke(strokeWidth))
        drawPath(rightPath, color)
        drawPath(rightPath, Color(0xFF2E2E2E), style = Stroke(strokeWidth))
        val crackPath = Path().apply {
            moveTo(size.width * 0.45f, size.height * 0.2f)
            lineTo(size.width * 0.55f, size.height * 0.35f)
            lineTo(size.width * 0.4f, size.height * 0.5f)
            lineTo(size.width * 0.6f, size.height * 0.65f)
            lineTo(size.width * 0.45f, size.height * 0.8f)
        }
        drawPath(crackPath, Color(0xFF2E2E2E), style = Stroke(strokeWidth))
    }
}


@Composable
fun HandDrawnHouseIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val strokeWidth = 2.dp.toPx()
        val color = Color(0xFF2E2E2E)
        val roofPath = Path().apply {
            moveTo(size.width * 0.5f, size.height * 0.1f)
            lineTo(size.width * 0.1f, size.height * 0.5f)
            lineTo(size.width * 0.9f, size.height * 0.5f)
            close()
        }
        drawPath(roofPath, color, style = Stroke(strokeWidth))
        drawRect(color = color, topLeft = Offset(size.width * 0.2f, size.height * 0.45f), size = Size(size.width * 0.6f, size.height * 0.45f), style = Stroke(strokeWidth))
        drawRect(color = color, topLeft = Offset(size.width * 0.4f, size.height * 0.65f), size = Size(size.width * 0.2f, size.height * 0.25f), style = Stroke(strokeWidth))
    }
}

// Data class for bouncing egg particles
data class EggParticle(
    var x: Float,
    var y: Float,
    var velocityX: Float,
    var velocityY: Float,
    var rotation: Float,
    val emoji: String,
    val size: Float
)

/**
 * Bouncing eggs with accelerometer physics! 🥚🍳🐣🐤
 */
@Composable
fun BouncingEggs(floorHeightOffset: Dp = 200.dp) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidthPx = constraints.maxWidth.toFloat()
        val screenHeightPx = constraints.maxHeight.toFloat()
        val floorOffsetPx = with(LocalDensity.current) { floorHeightOffset.toPx() }
        val effectiveFloorPx = screenHeightPx - floorOffsetPx

        val context = LocalContext.current
        val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
        val accelerometer = remember { sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }
        var gravityX by remember { mutableFloatStateOf(0f) }
        var gravityY by remember { mutableFloatStateOf(5.0f) }
        var lastShakeTime by remember { mutableLongStateOf(0L) }
        var frameCount by remember { mutableIntStateOf(0) }

        val eggs = remember {
            List(8) { index ->
                EggParticle(
                    x = Random.nextFloat() * screenWidthPx,
                    y = Random.nextFloat() * screenHeightPx * 0.3f,
                    velocityX = (Random.nextFloat() - 0.5f) * 100f,
                    velocityY = Random.nextFloat() * 100f,
                    rotation = Random.nextFloat() * 360f,
                    emoji = listOf("🥚", "🍳", "🐣", "🐤","🧲","🍿")[index % 6],
                    size = Random.nextFloat() * 20f + 40f
                )
            }.toMutableList()
        }

        DisposableEffect(Unit) {
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {
                    event?.let {
                        gravityX = -it.values[0] * 50f
                        gravityY = it.values[1] * 50f
                        val totalAccel = abs(it.values[0]) + abs(it.values[1]) + abs(it.values[2])
                        if (totalAccel > 25f) {
                            val currentTime = System.currentTimeMillis()
                            if (currentTime - lastShakeTime > 500) {
                                lastShakeTime = currentTime
                                eggs.forEach { egg ->
                                    egg.velocityX += (Random.nextFloat() - 0.5f) * 400f
                                    egg.velocityY += (Random.nextFloat() - 0.5f) * 400f
                                }
                            }
                        }
                    }
                }
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }
            sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_GAME)
            onDispose { sensorManager.unregisterListener(listener) }
        }

        LaunchedEffect(Unit) {
            while (true) {
                delay(16)
                frameCount++
                val deltaTime = 0.016f
                val damping = 0.98f
                val restitution = 1.1f

                eggs.forEach { egg ->
                    egg.velocityX += gravityX * deltaTime * 10f
                    egg.velocityY += gravityY * deltaTime * 10f
                    egg.velocityX *= damping
                    egg.velocityY *= damping
                    egg.x += egg.velocityX * deltaTime
                    egg.y += egg.velocityY * deltaTime
                    egg.rotation += egg.velocityX * deltaTime * 2f

                    if (egg.x < 0) { egg.x = 0f; egg.velocityX = -egg.velocityX * restitution }
                    else if (egg.x > screenWidthPx - egg.size) { egg.x = screenWidthPx - egg.size; egg.velocityX = -egg.velocityX * restitution }

                    if (egg.y < 0) { egg.y = 0f; egg.velocityY = -egg.velocityY * restitution }
                    else if (egg.y > effectiveFloorPx - egg.size) {
                        egg.y = effectiveFloorPx - egg.size
                        egg.velocityY = -egg.velocityY * restitution
                        if (abs(egg.velocityY) < 20f && abs(egg.velocityX) < 20f && abs(gravityX) < 5f) {
                            egg.velocityX = 0f; egg.velocityY = 0f
                            egg.rotation = (egg.rotation / 90f).roundToInt() * 90f
                        }
                    }
                }

                for (i in eggs.indices) {
                    for (j in i + 1 until eggs.size) {
                        val egg1 = eggs[i]
                        val egg2 = eggs[j]
                        val dx = egg2.x - egg1.x
                        val dy = egg2.y - egg1.y
                        val distance = sqrt(dx * dx + dy * dy)
                        val minDistance = (egg1.size + egg2.size) / 2f

                        if (distance < minDistance && distance > 0f) {
                            val nx = dx / distance
                            val ny = dy / distance
                            val overlap = minDistance - distance
                            egg1.x -= nx * overlap * 0.5f
                            egg1.y -= ny * overlap * 0.5f
                            egg2.x += nx * overlap * 0.5f
                            egg2.y += ny * overlap * 0.5f
                            val relativeVelX = egg2.velocityX - egg1.velocityX
                            val relativeVelY = egg2.velocityY - egg1.velocityY
                            val velAlongNormal = relativeVelX * nx + relativeVelY * ny
                            if (velAlongNormal < 0) {
                                val impulse = velAlongNormal * restitution
                                egg1.velocityX += impulse * nx
                                egg1.velocityY += impulse * ny
                                egg2.velocityX -= impulse * nx
                                egg2.velocityY -= impulse * ny
                            }
                        }
                    }
                }
            }
        }

        key(frameCount) {
            eggs.forEach { egg ->
                Text(
                    text = egg.emoji,
                    fontSize = (egg.size * 0.8f).sp,
                    modifier = Modifier.offset { IntOffset(egg.x.toInt(), egg.y.toInt()) }.rotate(egg.rotation)
                )
            }
        }
    }
}

@Composable
fun EggRecordButton(
    isRecording: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(if (isRecording) 1.02f else 1f, tween(200), label = "scale")
    val pulseAlpha by animateFloatAsState(if (isRecording) 0.8f else 0f, infiniteRepeatable(tween(1500), RepeatMode.Reverse), label = "pulse")

    Box(
        modifier = modifier.size(182.dp, 156.dp).scale(scale).clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isRecording) {
            Canvas(modifier = Modifier.size(208.dp, 182.dp)) {
                drawOval(color = Color(0xFFFF6B6B).copy(alpha = pulseAlpha * 0.6f), style = Stroke(width = 4.dp.toPx()), topLeft = Offset(8.dp.toPx(), 8.dp.toPx()), size = androidx.compose.ui.geometry.Size(size.width - 16.dp.toPx(), size.height - 16.dp.toPx()))
            }
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val eggWhitePath = Path().apply {
                moveTo(canvasWidth * 0.18f, canvasHeight * 0.5f)
                quadraticBezierTo(canvasWidth * 0.14f, canvasHeight * 0.33f, canvasWidth * 0.29f, canvasHeight * 0.29f)
                quadraticBezierTo(canvasWidth * 0.39f, canvasHeight * 0.25f, canvasWidth * 0.46f, canvasHeight * 0.28f)
                quadraticBezierTo(canvasWidth * 0.57f, canvasHeight * 0.23f, canvasWidth * 0.68f, canvasHeight * 0.27f)
                quadraticBezierTo(canvasWidth * 0.79f, canvasHeight * 0.29f, canvasWidth * 0.84f, canvasHeight * 0.4f)
                quadraticBezierTo(canvasWidth * 0.89f, canvasHeight * 0.5f, canvasWidth * 0.82f, canvasHeight * 0.6f)
                quadraticBezierTo(canvasWidth * 0.84f, canvasHeight * 0.71f, canvasWidth * 0.75f, canvasHeight * 0.75f)
                quadraticBezierTo(canvasWidth * 0.64f, canvasHeight * 0.8f, canvasWidth * 0.54f, canvasHeight * 0.77f)
                quadraticBezierTo(canvasWidth * 0.43f, canvasHeight * 0.8f, canvasWidth * 0.32f, canvasHeight * 0.75f)
                quadraticBezierTo(canvasWidth * 0.21f, canvasHeight * 0.71f, canvasWidth * 0.18f, canvasHeight * 0.62f)
                quadraticBezierTo(canvasWidth * 0.13f, canvasHeight * 0.54f, canvasWidth * 0.18f, canvasHeight * 0.5f)
                close()
            }
            drawPath(path = eggWhitePath, color = Color(0xFFFFF8E1))
            drawPath(path = eggWhitePath, color = Color(0xFF2E2E2E), style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
            val yolkColor = if (isRecording) Color(0xFFFF6B6B) else Color(0xFFFFD700)
            val yolkCenter = Offset(canvasWidth * 0.5f, canvasHeight * 0.46f)
            val yolkRadius = canvasWidth * 0.13f
            drawCircle(color = yolkColor, radius = yolkRadius, center = yolkCenter)
            drawCircle(color = Color(0xFF2E2E2E), radius = yolkRadius, center = yolkCenter, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
            val text = if (isRecording) "STOP" else "REC"
            val textY = canvasHeight * 0.71f
            drawContext.canvas.nativeCanvas.drawText(text, canvasWidth * 0.5f, textY, Paint().apply { color = Color(0xFF2E2E2E).toArgb(); textSize = 16.sp.toPx(); typeface = android.graphics.Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER })
        }
    }
}