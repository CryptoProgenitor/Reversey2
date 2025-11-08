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
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import kotlin.math.abs
import kotlin.math.sqrt
import com.example.reversey.data.models.ChallengeType
import com.example.reversey.data.models.PlayerAttempt
import com.example.reversey.data.models.Recording
import com.example.reversey.ui.components.ScoreExplanationDialog
import com.example.reversey.scoring.ScoringResult
import com.example.reversey.scoring.SimilarityMetrics
import com.example.reversey.utils.formatFileName
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.random.Random

/**
 * 🥚 EGG THEME COMPONENTS - MONOLITHIC SELF-CONTAINED THEME
 *
 * Hand-drawn aesthetic with fried eggs, sticky notes, and bouncing eggs!
 * Inspired by Taylor Swift's Folklore/Evermore albums.
 *
 * Architecture: Self-contained like SnowyOwlComponents - everything in one file.
 * No external routing, all composables inline.
 */
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
            // Adjust floorHeightOffset to control where eggs settle (default 200.dp leaves space for card list)
            BouncingEggs(floorHeightOffset = 90.dp)  // Increase if eggs overlap cards, decrease to use more space

            // Content on top
            content()
        }
    }
}

// ============================================
// RECORDING ITEM
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
                        enabled = recording.reversedPath != null,
                        size = 50.dp
                    ) { EggRewindIcon(Color(0xFF2E2E2E)) }
                    Text("Rewind", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFF2E2E2E))
                }

                if (isGameModeEnabled) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        HandDrawnEggButton(
                            onClick = { onStartAttempt(recording, ChallengeType.FORWARD) },
                            backgroundColor = Color(0xFFFF8A65),
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

    if (showRenameDialog) {
        var newName by remember { mutableStateOf(recording.name) }
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Recording 🥚") },
            text = {
                OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("Recording Name") }, singleLine = true)
            },
            confirmButton = { Button(onClick = { onRename(recording.originalPath ?: "", newName); showRenameDialog = false }) { Text("Save") } },
            dismissButton = { TextButton(onClick = { showRenameDialog = false }) { Text("Cancel") } }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Recording? 🥚💔") },
            text = { Text("This will crack the egg forever! Are you sure?") },
            confirmButton = {
                Button(onClick = { onDelete(recording); showDeleteDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722))) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Keep") } }
        )
    }

    if (showShareDialog) {
        AlertDialog(
            onDismissRequest = { showShareDialog = false },
            title = { Text("Share Recording 📤") },
            text = {
                Column {
                    Text("Which version would you like to share?")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { onShare(recording.originalPath ?: ""); showShareDialog = false }, modifier = Modifier.fillMaxWidth()) { Text("Share Original 🥚") }
                    if (recording.reversedPath != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { onShare(recording.reversedPath ?: ""); showShareDialog = false }, modifier = Modifier.fillMaxWidth()) { Text("Share Reversed 🔄") }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showShareDialog = false }) { Text("Cancel") } }
        )
    }
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
            // TOP ROW: Sticky note (left) and Score circle (right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Sticky note with player name + house icon
                Box(
                    modifier = Modifier
                        .rotate(-8f)
                        .background(Color(0xFFFFF176), RoundedCornerShape(4.dp))
                        .border(2.dp, Color(0xFF2E2E2E), RoundedCornerShape(4.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.combinedClickable(
                            onClick = { onJumpToParent?.invoke() },
                            onLongClick = { showRenameDialog = true }
                        )
                    ) {
                        HandDrawnHouseIcon(modifier = Modifier.size(16.dp))
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .rotate(if (attempt.challengeType == ChallengeType.REVERSE) 180f else 0f)
                        ) {
                            EggPlayIcon(Color(0xFF2E2E2E))
                        }
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

                // Right: Score circle
                EggScoreCircle(score = "${attempt.score.toInt()}%", eggEmoji = eggEmoji, size = 80.dp, onClick = { showScoreDialog = true })
            }

            Spacer(modifier = Modifier.height(12.dp))

            // BOTTOM ROW: 4 buttons equally spaced across width
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onShareAttempt != null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        HandDrawnEggButton(onClick = { showShareDialog = true }, backgroundColor = Color(0xFF9C27B0), size = 45.dp) { EggShareIcon(Color(0xFF2E2E2E)) }
                        Text("Share", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFF2E2E2E))
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    HandDrawnEggButton(
                        onClick = {
                            if (isPlayingThis && !isPaused) onPause() else onPlay(attempt.attemptFilePath)
                        },
                        backgroundColor = Color(0xFFFF8A65),
                        size = 45.dp
                    ) { EggPlayIcon(Color(0xFF2E2E2E)) }
                    Text("Play", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFF2E2E2E))
                }

                if (attempt.reversedAttemptFilePath != null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        HandDrawnEggButton(onClick = { onPlay(attempt.reversedAttemptFilePath!!) }, backgroundColor = Color(0xFFFF8A65), size = 45.dp) { EggReverseIcon(Color(0xFF2E2E2E)) }
                        Text("Rev", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFF2E2E2E))
                    }
                }

                if (onDeleteAttempt != null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        HandDrawnEggButton(onClick = { showDeleteDialog = true }, backgroundColor = Color(0xFFFF5722), size = 45.dp) { CrackedEggIcon() }
                        Text("Del", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFF2E2E2E))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, Color(0xFF2E2E2E), RoundedCornerShape(4.dp))
                    .padding(2.dp)
            ) {
                EggTravelProgressBar(progress = if (isPlayingThis) progress else 0f, modifier = Modifier.fillMaxWidth())
            }
        }
    }

    if (showRenameDialog && onRenamePlayer != null) {
        var newName by remember { mutableStateOf(attempt.playerName) }
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Player 🥚") },
            text = { OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("Player Name") }, singleLine = true) },
            confirmButton = { Button(onClick = { onRenamePlayer(attempt, newName); showRenameDialog = false }) { Text("Save") } },
            dismissButton = { TextButton(onClick = { showRenameDialog = false }) { Text("Cancel") } }
        )
    }

    if (showDeleteDialog && onDeleteAttempt != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Attempt? 🥚💔") },
            text = { Text("Delete ${attempt.playerName}'s attempt?") },
            confirmButton = {
                Button(onClick = { onDeleteAttempt(attempt); showDeleteDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722))) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Keep") } }
        )
    }

    if (showShareDialog && onShareAttempt != null) {
        AlertDialog(
            onDismissRequest = { showShareDialog = false },
            title = { Text("Share Attempt 📤") },
            text = {
                Column {
                    Text("Which version would you like to share?")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { onShareAttempt(attempt.attemptFilePath); showShareDialog = false }, modifier = Modifier.fillMaxWidth()) { Text("Share Fresh Egg 🥚") }
                    if (attempt.reversedAttemptFilePath != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { onShareAttempt(attempt.reversedAttemptFilePath!!); showShareDialog = false }, modifier = Modifier.fillMaxWidth()) { Text("Share Scrambled 🔄") }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showShareDialog = false }) { Text("Cancel") } }
        )
    }

    if (showScoreDialog) {
        EggScoreDialog(
            attempt = attempt,
            onDismiss = { showScoreDialog = false }
        )
    }
}

/**
 * 🥚 EGG-THEMED SCORE DIALOG
 * Legacy style with cream background and hand-drawn borders
 */
@Composable
fun EggScoreDialog(
    attempt: PlayerAttempt,
    onDismiss: () -> Unit
) {
    val eggEmoji = when {
        attempt.score >= 90 -> "🍳"
        attempt.score >= 80 -> "🥚"
        attempt.score >= 70 -> "🐣"
        attempt.score >= 60 -> "🐥"
        else -> "🥀"
    }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Box(modifier = Modifier.fillMaxWidth(0.9f).clickable(enabled = false) { }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 0.dp, shape = RoundedCornerShape(16.dp))
                        .border(width = 4.dp, color = Color(0xFF2E2E2E), shape = RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBF0)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Close button
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopEnd) {
                            IconButton(onClick = onDismiss) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = Color(0xFF2E2E2E)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Score circle with green arc
                        EggScoreCircle(
                            score = "${attempt.score.toInt()}%",
                            eggEmoji = eggEmoji,
                            size = 120.dp,
                            onClick = {}
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Performance breakdown
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(2.dp, Color(0xFF2E2E2E), RoundedCornerShape(8.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Performance Breakdown",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF2E2E2E),
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                // Pitch Similarity
                                EggMetricRow("Pitch Similarity", attempt.pitchSimilarity)
                                Spacer(modifier = Modifier.height(8.dp))
                                // Voice Matching
                                EggMetricRow("Voice Matching", attempt.mfccSimilarity)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Encouragement tips
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(2.dp, Color(0xFF2E2E2E), RoundedCornerShape(8.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE0B2)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = when {
                                        attempt.score >= 90 -> "Egg-cellent! 🍳"
                                        attempt.score >= 80 -> "Crack-ing good! 🥚"
                                        attempt.score >= 70 -> "Hatching progress! 🐣"
                                        attempt.score >= 60 -> "Keep pecking away! 🐥"
                                        else -> "Practice makes perfect! 🥀"
                                    },
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF2E2E2E)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Egg-themed metric row with progress bar
 */
@Composable
fun EggMetricRow(label: String, value: Float) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF2E2E2E)
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            // Egg-themed progress bar
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .height(8.dp)
                    .border(2.dp, Color(0xFF2E2E2E), RoundedCornerShape(4.dp))
                    .background(Color(0xFFFFE0B2), RoundedCornerShape(4.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(value.coerceIn(0f, 1f))
                        .background(Color(0xFFFF8A65), RoundedCornerShape(4.dp))
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${(value * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF2E2E2E)
            )
        }
    }
}


// ============================================
// HELPERS & ICONS
// ============================================

@Composable
fun EggScoreCircle(score: String, eggEmoji: String, size: Dp, onClick: () -> Unit = {}, modifier: Modifier = Modifier) {
    // Extract numeric score for arc calculation
    val scoreValue = score.replace("%", "").toFloatOrNull() ?: 0f
    val sweepAngle = (scoreValue / 100f) * 360f

    Box(
        modifier = modifier.size(size).clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // Draw the circular progress arc
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 8.dp.toPx()
            val radius = (size.toPx() - strokeWidth) / 2f

            // Gray background circle
            drawArc(
                color = Color(0xFFE0E0E0),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                size = Size(radius * 2, radius * 2)
            )

            // Green progress arc (proportional to score)
            drawArc(
                color = Color(0xFF4CAF50),
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                size = Size(radius * 2, radius * 2)
            )
        }

        // Center content: egg emoji + score
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = eggEmoji,
                style = MaterialTheme.typography.titleMedium,
                fontSize = 20.sp
            )
            Text(
                text = score.replace("%", ""),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                ),
                color = Color(0xFF2E2E2E)
            )
        }
    }
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
    // Share Icon - Branching Rune
    Canvas(modifier = Modifier.size(32.dp)) {
        val strokeWidth = 2.dp.toPx()
        val radius = size.width * 0.083f

        // Three circles (nodes)
        drawCircle(
            color = color,
            radius = radius,
            center = Offset(size.width * 0.25f, size.height * 0.5f),
            style = Stroke(width = strokeWidth)
        )
        drawCircle(
            color = color,
            radius = radius,
            center = Offset(size.width * 0.75f, size.height * 0.25f),
            style = Stroke(width = strokeWidth)
        )
        drawCircle(
            color = color,
            radius = radius,
            center = Offset(size.width * 0.75f, size.height * 0.75f),
            style = Stroke(width = strokeWidth)
        )

        // Connecting lines
        drawLine(
            color = color,
            start = Offset(size.width * 0.33f, size.height * 0.5f),
            end = Offset(size.width * 0.67f, size.height * 0.29f),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.33f, size.height * 0.5f),
            end = Offset(size.width * 0.67f, size.height * 0.71f),
            strokeWidth = strokeWidth
        )
    }
}

@Composable
fun EggPlayIcon(color: Color) {
    // Play Icon - Runic Arrow
    Canvas(modifier = Modifier.size(32.dp)) {
        val path = Path().apply {
            moveTo(size.width * 0.29f, size.height * 0.17f)
            lineTo(size.width * 0.29f, size.height * 0.83f)
            lineTo(size.width * 0.79f, size.height * 0.5f)
            close()
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 2.dp.toPx(), join = StrokeJoin.Miter)
        )
    }
}

@Composable
fun EggPauseIcon(color: Color) {
    // Pause Icon - Two Vertical Bars
    Canvas(modifier = Modifier.size(32.dp)) {
        val barWidth = size.width * 0.12f
        val barHeight = size.height * 0.6f
        val topY = size.height * 0.2f

        // Left bar
        drawRect(
            color = color,
            topLeft = Offset(size.width * 0.3f, topY),
            size = Size(barWidth, barHeight),
            style = Stroke(width = 2.dp.toPx())
        )
        // Right bar
        drawRect(
            color = color,
            topLeft = Offset(size.width * 0.58f, topY),
            size = Size(barWidth, barHeight),
            style = Stroke(width = 2.dp.toPx())
        )
    }
}

@Composable
fun EggRewindIcon(color: Color) {
    // Rewind Icon - Double Back Runes
    Canvas(modifier = Modifier.size(32.dp)) {
        val strokeWidth = 2.dp.toPx()

        // First back arrow
        val path1 = Path().apply {
            moveTo(size.width * 0.54f, size.height * 0.71f)
            lineTo(size.width * 0.29f, size.height * 0.5f)
            lineTo(size.width * 0.54f, size.height * 0.29f)
        }
        drawPath(
            path = path1,
            color = color,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Square)
        )

        // Second back arrow
        val path2 = Path().apply {
            moveTo(size.width * 0.83f, size.height * 0.71f)
            lineTo(size.width * 0.58f, size.height * 0.5f)
            lineTo(size.width * 0.83f, size.height * 0.29f)
        }
        drawPath(
            path = path2,
            color = color,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Square)
        )
    }
}

@Composable
fun EggMicIcon(color: Color) {
    // Microphone Icon - Sound Wave Rune
    Canvas(modifier = Modifier.size(32.dp)) {
        val strokeWidth = 2.dp.toPx()

        // Mic capsule body
        drawRoundRect(
            color = color,
            topLeft = Offset(size.width * 0.375f, size.height * 0.17f),
            size = Size(size.width * 0.25f, size.height * 0.42f),
            cornerRadius = CornerRadius(size.width * 0.125f),
            style = Stroke(width = strokeWidth)
        )

        // Arc under mic
        val arcPath = Path().apply {
            moveTo(size.width * 0.21f, size.height * 0.46f)
            quadraticBezierTo(
                size.width * 0.21f, size.height * 0.67f,
                size.width * 0.5f, size.height * 0.67f
            )
            quadraticBezierTo(
                size.width * 0.79f, size.height * 0.67f,
                size.width * 0.79f, size.height * 0.46f
            )
        }
        drawPath(
            path = arcPath,
            color = color,
            style = Stroke(width = strokeWidth)
        )

        // Vertical stand
        drawLine(
            color = color,
            start = Offset(size.width * 0.5f, size.height * 0.67f),
            end = Offset(size.width * 0.5f, size.height * 0.83f),
            strokeWidth = strokeWidth
        )

        // Base horizontal line
        drawLine(
            color = color,
            start = Offset(size.width * 0.375f, size.height * 0.83f),
            end = Offset(size.width * 0.625f, size.height * 0.83f),
            strokeWidth = strokeWidth
        )
    }
}

@Composable
fun EggReverseIcon(color: Color) {
    // Reverse/Loop Icon - Circular Rune
    Canvas(modifier = Modifier.size(32.dp)) {
        val strokeWidth = 2.dp.toPx()

        // Left arc
        val leftArc = Path().apply {
            moveTo(size.width * 0.5f, size.height * 0.17f)
            cubicTo(
                size.width * 0.29f, size.height * 0.17f,
                size.width * 0.17f, size.height * 0.29f,
                size.width * 0.17f, size.height * 0.5f
            )
            cubicTo(
                size.width * 0.17f, size.height * 0.71f,
                size.width * 0.29f, size.height * 0.83f,
                size.width * 0.5f, size.height * 0.83f
            )
        }
        drawPath(
            path = leftArc,
            color = color,
            style = Stroke(width = strokeWidth)
        )

        // Arrow at top
        val arrowPath = Path().apply {
            moveTo(size.width * 0.5f, size.height * 0.17f)
            lineTo(size.width * 0.5f, size.height * 0.33f)
            lineTo(size.width * 0.33f, size.height * 0.17f)
            lineTo(size.width * 0.5f, size.height * 0.17f)
        }
        drawPath(path = arrowPath, color = color, style = Fill)

        // Right arc
        val rightArc = Path().apply {
            moveTo(size.width * 0.5f, size.height * 0.83f)
            cubicTo(
                size.width * 0.71f, size.height * 0.83f,
                size.width * 0.83f, size.height * 0.71f,
                size.width * 0.83f, size.height * 0.5f
            )
            cubicTo(
                size.width * 0.83f, size.height * 0.29f,
                size.width * 0.71f, size.height * 0.17f,
                size.width * 0.5f, size.height * 0.17f
            )
        }
        drawPath(
            path = rightArc,
            color = color,
            style = Stroke(width = strokeWidth)
        )
    }
}

@Composable
fun EggMicRightArrowIcon(color: Color) {
    // Mic on left + Right arrow on right (for Fwd challenge)
    Canvas(modifier = Modifier.size(32.dp)) {
        val strokeWidth = 2.5.dp.toPx()

        // Mic body (left side) - scaled to match large chevron height
        drawRoundRect(
            color = color,
            topLeft = Offset(size.width * 0.10f, size.height * 0.15f),
            size = Size(size.width * 0.22f, size.height * 0.45f),
            cornerRadius = CornerRadius(4.dp.toPx()),
            style = Stroke(width = strokeWidth)
        )

        // Mic stand
        drawLine(
            color = color,
            start = Offset(size.width * 0.21f, size.height * 0.60f),
            end = Offset(size.width * 0.21f, size.height * 0.75f),
            strokeWidth = strokeWidth
        )

        // Mic base
        drawLine(
            color = color,
            start = Offset(size.width * 0.14f, size.height * 0.75f),
            end = Offset(size.width * 0.28f, size.height * 0.75f),
            strokeWidth = strokeWidth
        )

        // Right double chevron (75% larger - height matches mic)
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
    // Mic on right + Left arrow on left (for Rev challenge)
    Canvas(modifier = Modifier.size(32.dp)) {
        val strokeWidth = 2.5.dp.toPx()

        // Mic body (right side) - scaled to match large chevron height
        drawRoundRect(
            color = color,
            topLeft = Offset(size.width * 0.68f, size.height * 0.15f),
            size = Size(size.width * 0.22f, size.height * 0.45f),
            cornerRadius = CornerRadius(4.dp.toPx()),
            style = Stroke(width = strokeWidth)
        )

        // Mic stand
        drawLine(
            color = color,
            start = Offset(size.width * 0.79f, size.height * 0.60f),
            end = Offset(size.width * 0.79f, size.height * 0.75f),
            strokeWidth = strokeWidth
        )

        // Mic base
        drawLine(
            color = color,
            start = Offset(size.width * 0.72f, size.height * 0.75f),
            end = Offset(size.width * 0.86f, size.height * 0.75f),
            strokeWidth = strokeWidth
        )

        // Left double chevron (75% larger - height matches mic)
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
    // House icon - custom design by Ed's daughter (from legacy egg theme)
    Canvas(modifier = modifier) {
        val strokeWidth = 2.dp.toPx()
        val color = Color(0xFF2E2E2E)

        // House roof (triangle)
        val roofPath = Path().apply {
            moveTo(size.width * 0.5f, size.height * 0.1f)
            lineTo(size.width * 0.1f, size.height * 0.5f)
            lineTo(size.width * 0.9f, size.height * 0.5f)
            close()
        }
        drawPath(roofPath, color, style = Stroke(strokeWidth))

        // House body (rectangle)
        drawRect(
            color = color,
            topLeft = Offset(size.width * 0.2f, size.height * 0.45f),
            size = Size(size.width * 0.6f, size.height * 0.45f),
            style = Stroke(strokeWidth)
        )

        // Door
        drawRect(
            color = color,
            topLeft = Offset(size.width * 0.4f, size.height * 0.65f),
            size = Size(size.width * 0.2f, size.height * 0.25f),
            style = Stroke(strokeWidth)
        )
    }
}
// Data class for bouncing egg particles with rotation
data class EggParticle(
    var x: Float,
    var y: Float,
    var velocityX: Float,
    var velocityY: Float,
    var rotation: Float,  // Add rotation!
    val emoji: String,
    val size: Float
)
/**
 * Bouncing eggs with accelerometer physics! 🥚🍳🐣🐤
 * Eggs rotate as they bounce and settle above the card list.
 *
 * @param floorHeightOffset How much space to leave at bottom (dp) for card list
 */
@Composable

fun BouncingEggs(floorHeightOffset: Dp = 200.dp) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidthPx = constraints.maxWidth.toFloat()
        val screenHeightPx = constraints.maxHeight.toFloat()
        val floorOffsetPx = with(LocalDensity.current) { floorHeightOffset.toPx() }
        val effectiveFloorPx = screenHeightPx - floorOffsetPx  // Eggs can't go below this

        val context = LocalContext.current
        val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
        val accelerometer = remember { sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }
        var gravityX by remember { mutableFloatStateOf(0f) }
        var gravityY by remember { mutableFloatStateOf(5.0f) }
        var lastShakeTime by remember { mutableLongStateOf(0L) }
        var frameCount by remember { mutableIntStateOf(0) }  // Forces recomposition!

        val eggs = remember {
            List(8) { index ->
                EggParticle(
                    x = Random.nextFloat() * screenWidthPx,
                    y = Random.nextFloat() * screenHeightPx * 0.3f,
                    velocityX = (Random.nextFloat() - 0.5f) * 100f,
                    velocityY = Random.nextFloat() * 100f,
                    rotation = Random.nextFloat() * 360f,  // Random initial rotation
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

                        // Shake detection - jiggle those eggs! 😁
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
                frameCount++  // Trigger recomposition!
                val deltaTime = 0.016f
                val damping = 0.98f
                val restitution = 1.1f

                eggs.forEach { egg ->
                    // Apply gravity
                    egg.velocityX += gravityX * deltaTime * 10f
                    egg.velocityY += gravityY * deltaTime * 10f

                    // Apply damping
                    egg.velocityX *= damping
                    egg.velocityY *= damping

                    // Update position
                    egg.x += egg.velocityX * deltaTime
                    egg.y += egg.velocityY * deltaTime

                    // Update rotation based on velocityX (spin as they move)
                    egg.rotation += egg.velocityX * deltaTime * 2f

                    // Bounce off walls
                    if (egg.x < 0) {
                        egg.x = 0f
                        egg.velocityX = -egg.velocityX * restitution
                    } else if (egg.x > screenWidthPx - egg.size) {
                        egg.x = screenWidthPx - egg.size
                        egg.velocityX = -egg.velocityX * restitution
                    }

                    // Bounce off ceiling
                    if (egg.y < 0) {
                        egg.y = 0f
                        egg.velocityY = -egg.velocityY * restitution
                    }
                    // Bounce off floor (now uses effectiveFloorPx to stay above card list)
                    else if (egg.y > effectiveFloorPx - egg.size) {
                        egg.y = effectiveFloorPx - egg.size
                        egg.velocityY = -egg.velocityY * restitution

                        // Complete stop when settled (unless jiggled!)
                        if (abs(egg.velocityY) < 20f && abs(egg.velocityX) < 20f && abs(gravityX) < 5f) {
                            egg.velocityX = 0f
                            egg.velocityY = 0f
                            egg.rotation = (egg.rotation / 90f).roundToInt() * 90f  // Snap to nearest 90°
                        }
                    }
                }

                // Egg-to-egg collision detection (prevents clumping!)
                for (i in eggs.indices) {
                    for (j in i + 1 until eggs.size) {
                        val egg1 = eggs[i]
                        val egg2 = eggs[j]

                        // Calculate distance between egg centers
                        val dx = egg2.x - egg1.x
                        val dy = egg2.y - egg1.y
                        val distance = kotlin.math.sqrt(dx * dx + dy * dy)
                        val minDistance = (egg1.size + egg2.size) / 2f

                        // If eggs overlap, push them apart and bounce
                        if (distance < minDistance && distance > 0f) {
                            // Normalized direction vector
                            val nx = dx / distance
                            val ny = dy / distance

                            // Separate eggs to minimum distance
                            val overlap = minDistance - distance
                            val separationX = nx * overlap * 0.5f
                            val separationY = ny * overlap * 0.5f

                            egg1.x -= separationX
                            egg1.y -= separationY
                            egg2.x += separationX
                            egg2.y += separationY

                            // Calculate relative velocity
                            val relativeVelX = egg2.velocityX - egg1.velocityX
                            val relativeVelY = egg2.velocityY - egg1.velocityY

                            // Velocity along collision normal
                            val velAlongNormal = relativeVelX * nx + relativeVelY * ny

                            // Don't resolve if velocities are separating
                            if (velAlongNormal < 0) {
                                // Apply collision impulse (elastic collision)
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

        // Render eggs as actual emojis with rotation! 🥚
        // key(frameCount) forces recomposition every frame so UI updates!
        key(frameCount) {
            eggs.forEach { egg ->
                Text(
                    text = egg.emoji,
                    fontSize = (egg.size * 0.8f).sp,  // Scale emoji to egg size
                    modifier = Modifier
                        .offset { IntOffset(egg.x.toInt(), egg.y.toInt()) }
                        .graphicsLayer {
                            rotationZ = egg.rotation  // Rotate! 🔄
                        }
                )
            }
        }
    }
}

// ============================================
// RECORD BUTTON
// ============================================

/**
 * Beautiful fried egg recording button! 🥚🍳
 */
@Composable
fun EggRecordButton(
    isRecording: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Animation for recording state
    val scale by animateFloatAsState(
        targetValue = if (isRecording) 1.02f else 1f,
        animationSpec = tween(200),
        label = "scale"
    )

    // Pulsing animation for recording ring
    val pulseAlpha by animateFloatAsState(
        targetValue = if (isRecording) 0.8f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = modifier
            .size(182.dp, 156.dp)
            .scale(scale)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // Pulsing ring when recording
        if (isRecording) {
            Canvas(
                modifier = Modifier.size(208.dp, 182.dp)
            ) {
                drawOval(
                    color = Color(0xFFFF6B6B).copy(alpha = pulseAlpha * 0.6f),
                    style = Stroke(width = 4.dp.toPx()),
                    topLeft = Offset(8.dp.toPx(), 8.dp.toPx()),
                    size = androidx.compose.ui.geometry.Size(
                        size.width - 16.dp.toPx(),
                        size.height - 16.dp.toPx()
                    )
                )
            }
        }

        // Main fried egg button
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // Create the irregular fried egg white shape
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

            // Draw the egg white
            drawPath(path = eggWhitePath, color = Color(0xFFFFF8E1))

            // Draw thick hand-drawn border
            drawPath(
                path = eggWhitePath,
                color = Color(0xFF2E2E2E),
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Draw the yolk
            val yolkColor = if (isRecording) Color(0xFFFF6B6B) else Color(0xFFFFD700)
            val yolkCenter = Offset(canvasWidth * 0.5f, canvasHeight * 0.46f)
            val yolkRadius = canvasWidth * 0.13f

            drawCircle(color = yolkColor, radius = yolkRadius, center = yolkCenter)
            drawCircle(
                color = Color(0xFF2E2E2E),
                radius = yolkRadius,
                center = yolkCenter,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            // Draw text
            val text = if (isRecording) "STOP" else "REC"
            val textY = canvasHeight * 0.71f

            drawContext.canvas.nativeCanvas.drawText(
                text,
                canvasWidth * 0.5f,
                textY,
                Paint().apply {
                    color = Color(0xFF2E2E2E).toArgb()
                    textSize = 16.sp.toPx()
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                    textAlign = Paint.Align.CENTER
                }
            )
        }
    }
}