package com.example.reversey

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.reversey.ui.components.ScoreExplanationDialog
import com.example.reversey.scoring.ScoringResult
import com.example.reversey.scoring.SimilarityMetrics
import kotlin.random.Random
import com.example.reversey.ui.theme.aestheticTheme
import com.example.reversey.ui.theme.materialColors

// Dancing Script handwriting font family
private val dancingScriptFontFamily = FontFamily(
    Font(R.font.dancing_script_regular, FontWeight.Normal),
    Font(R.font.dancing_script_bold, FontWeight.Bold)
)

/**
 * ⭐ STAR RATING COMPONENT - Restored from original design
 */
@Composable
fun StarRating(
    score: Double,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val fullStars = score.toInt() / 20 // Convert percentage to stars (0-5)
    val percentage = "${score.toInt()}%"

    Row(
        modifier = modifier.clickable { onClick?.invoke() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Draw 5 stars
        repeat(5) { index ->
            Icon(
                imageVector = if (index < fullStars) Icons.Filled.Star else Icons.Outlined.StarBorder,
                contentDescription = null,
                tint = if (index < fullStars) Color(0xFFFFD700) else Color.Gray,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = percentage,
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = dancingScriptFontFamily,
                fontWeight = FontWeight.Medium
            ),
            color = Color.Black.copy(alpha = 0.7f)
        )
    }
}

/**
 * 🎨 HANDWRITTEN DECORATIVE LINE - Restored from original design
 */
@Composable
fun HandwrittenLine(
    modifier: Modifier = Modifier,
    color: Color = Color.Black.copy(alpha = 0.3f)
) {
    Canvas(modifier = modifier.height(2.dp)) {
        val path = Path().apply {
            val y = size.height / 2
            moveTo(0f, y)

            var x = 0f
            while (x < size.width) {
                val wobble = kotlin.math.sin(x / 20) * 1.5f + kotlin.math.cos(x / 15) * 0.8f
                lineTo(x, y + wobble)
                x += 3f
            }
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 1.5.dp.toPx())
        )
    }
}

/**
 * 📎 PROPER TAPE CORNER - Like v10, extends beyond card boundaries
 */
@Composable
private fun ProperTapeCorner(
    modifier: Modifier = Modifier,
    offsetX: androidx.compose.ui.unit.Dp = 0.dp,
    offsetY: androidx.compose.ui.unit.Dp = 0.dp,
    rotation: Float = 0f
) {
    Box(
        modifier = modifier
            .offset(x = offsetX, y = offsetY)
            .rotate(rotation)
            .size(width = 24.dp, height = 16.dp)
            .background(
                color = Color.Gray.copy(alpha = 0.5f),
                shape = RoundedCornerShape(2.dp)
            )
            //.shadow(
            //    elevation = 2.dp,
            //    shape = RoundedCornerShape(2.dp)
            //)
    )
}

/**
 * 🎨 COMPACT SCRAPBOOK BUTTON - Exact match to original design
 */
@Composable
private fun CompactScrapbookButton(
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    iconColor: Color,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clickable(enabled = enabled) { onClick() }
            .padding(2.dp)
    ) {
        // Square icon button with border (matches screenshot exactly)
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(4.dp)
                )
                .border(
                    width = 2.dp,
                    color = Color.Gray.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(4.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(3.dp))

        // Label underneath
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = dancingScriptFontFamily,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            ),
            color = Color.Black.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

/**
 * 🎨 MAIN SCRAPBOOK ATTEMPT ITEM - Proper tape corners like v10
 */
@Composable
fun ScrapbookAttemptItem(
    attempt: PlayerAttempt,
    currentlyPlayingPath: String?,
    isPaused: Boolean,
    progress: Float,
    onPlay: (String) -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit,
    onRenamePlayer: ((PlayerAttempt, String) -> Unit)? = null,
    onDeleteAttempt: ((PlayerAttempt) -> Unit)? = null,
    onShareAttempt: ((String) -> Unit)? = null,
    onJumpToParent: (() -> Unit)? = null,
) {
    val aesthetic = aestheticTheme()
    val colors = materialColors()

    val isPlayingThis = currentlyPlayingPath == attempt.attemptFilePath ||
            currentlyPlayingPath == attempt.reversedAttemptFilePath

    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    var showScoreDialog by remember { mutableStateOf(false) }

    // Stable ID for consistent colors and rotation
    val stableId = attempt.attemptFilePath.hashCode()

    // Sticky note color selection
    val stickyNoteColor = remember(stableId) {
        val colors = listOf(
            Color(0xFFFFF59D), // Soft Yellow
            Color(0xFFF8BBD9), // Soft Pink
            Color(0xFFC8E6C9), // Soft Green
            Color(0xFFBBDEFB), // Soft Blue
            Color(0xFFE1BEE7), // Soft Purple
            Color(0xFFFFCCBC), // Soft Orange
        )
        colors[stableId.mod(colors.size)]
    }

    val rotation = remember(stableId) {
        (stableId % 7 - 3).toFloat() // -3 to +3 degrees
    }

    // Tape corner rotations (independent of card rotation)
    val tapeRotation1 = remember(stableId) { (stableId % 31 - 15).toFloat() }
    val tapeRotation2 = remember(stableId) { ((stableId * 17) % 31 - 15).toFloat() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 48.dp, end = 16.dp, top = 12.dp, bottom = 12.dp) // More padding for tape visibility
    ) {
        // Main sticky note card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .rotate(rotation)
                .shadow(
                    elevation = 6.dp,
                    shape = RoundedCornerShape(8.dp),
                    spotColor = Color.Black.copy(alpha = 0.3f),
                    ambientColor = Color.Black.copy(alpha = 0.1f)
                ),
            colors = CardDefaults.cardColors(
                containerColor = stickyNoteColor
            ),
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // Header with player name and star rating
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Player name with challenge icon
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showRenameDialog = true }
                    ) {
                        val challengeIcon =
                            if (attempt.challengeType == ChallengeType.REVERSE) "🔄" else "▶️"
                        Text(
                            text = challengeIcon,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(end = 6.dp)
                        )

                        Text(
                            text = attempt.playerName,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = dancingScriptFontFamily,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.Black.copy(alpha = 0.87f),
                            maxLines = 1
                        )
                    }

                    // Star rating component
                    StarRating(
                        score = attempt.score.toDouble(),
                        onClick = { showScoreDialog = true }
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Handwritten decorative line
                HandwrittenLine(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Black.copy(alpha = 0.4f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Progress indication if playing
                if (isPlayingThis) {
                    Text(
                        text = "♪ Playing... ♪",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = dancingScriptFontFamily,
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color.Black.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Simple progress bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .background(
                                Color.Black.copy(alpha = 0.2f),
                                RoundedCornerShape(2.dp)
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress)
                                .height(3.dp)
                                .background(
                                    Color.Black.copy(alpha = 0.6f),
                                    RoundedCornerShape(2.dp)
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Compact button layout - 5 buttons matching green note in screenshot
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Original button (up arrow, gray) - Navigation to parent recording
                    CompactScrapbookButton(
                        onClick = {
                            onJumpToParent?.invoke()
                        },
                        icon = Icons.Default.Upload, // Up arrow for "go up" navigation
                        label = "Original",
                        iconColor = Color.Gray
                    )

                    // Share button (envelope, purple/pink)
                    CompactScrapbookButton(
                        onClick = { showShareDialog = true },
                        icon = Icons.Default.Share,
                        label = "Share",
                        iconColor = Color(0xFFE91E63)
                    )

                    // Play button (play triangle, yellow/orange)
                    CompactScrapbookButton(
                        onClick = {
                            if (isPlayingThis && !isPaused) {
                                onPause()
                            } else {
                                onPlay(attempt.attemptFilePath)
                            }
                        },
                        icon = Icons.Default.PlayArrow,
                        label = "Play",
                        iconColor = Color(0xFFFF9800)
                    )

                    // Reverse button (curved arrow, yellow/orange)
                    CompactScrapbookButton(
                        onClick = {
                            if (isPlayingThis && !isPaused) {
                                onPause()
                            } else {
                                attempt.reversedAttemptFilePath?.let { onPlay(it) }
                            }
                        },
                        icon = Icons.Default.Repeat,
                        label = "Reverse",
                        iconColor = Color(0xFFFF9800)
                    )

                    // Delete button (trash, gray)
                    CompactScrapbookButton(
                        onClick = { showDeleteDialog = true },
                        icon = Icons.Default.Delete,
                        label = "Delete",
                        iconColor = Color.Gray
                    )
                }
            }
        }

        // PROPER TAPE CORNERS: Positioned outside card, extend to background, rotate with card
        ProperTapeCorner(
            offsetX = (-8).dp,
            offsetY = (-8).dp,
            rotation = rotation + tapeRotation1
        )

        ProperTapeCorner(
            modifier = Modifier.align(Alignment.TopEnd),
            offsetX = 8.dp,
            offsetY = (-8).dp,
            rotation = rotation + tapeRotation2
        )
    }

    // Dialogs
    ScrapbookAttemptDialogs(
        showRenameDialog = showRenameDialog,
        showDeleteDialog = showDeleteDialog,
        showShareDialog = showShareDialog,
        showScoreDialog = showScoreDialog,
        attempt = attempt,
        onRenamePlayer = onRenamePlayer,
        onDeleteAttempt = onDeleteAttempt,
        onShareAttempt = onShareAttempt,
        onDismissRename = { showRenameDialog = false },
        onDismissDelete = { showDeleteDialog = false },
        onDismissShare = { showShareDialog = false },
        onDismissScore = { showScoreDialog = false }
    )
}

/**
 * 🎨 SCRAPBOOK RECORDING ITEM - Proper tape corners like v10
 */
@Composable
fun ScrapbookRecordingItem(
    recording: Recording,
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
    // Use isPlaying directly since it indicates if this recording is playing
    val isPlayingThis = isPlaying

    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }

    // Stable ID for consistent colors and rotation
    val stableId = recording.originalPath.hashCode()

    // Orange sticky note color for recordings (like in screenshot)
    val stickyNoteColor = Color(0xFFFFB74D)

    val rotation = remember(stableId) {
        (stableId % 5 - 2).toFloat() // -2 to +2 degrees for recordings
    }

    // Tape corner rotations (independent of card rotation)
    val tapeRotation1 = remember(stableId) { (stableId % 23 - 11).toFloat() }
    val tapeRotation2 = remember(stableId) { ((stableId * 13) % 23 - 11).toFloat() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp) // More padding for tape visibility
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .rotate(rotation)
                .shadow(
                    elevation = 6.dp,
                    shape = RoundedCornerShape(8.dp),
                    spotColor = Color.Black.copy(alpha = 0.3f),
                    ambientColor = Color.Black.copy(alpha = 0.1f)
                ),
            colors = CardDefaults.cardColors(
                containerColor = stickyNoteColor
            ),
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // Recording name
                Text(
                    text = recording.name.removeSuffix(".wav"),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = dancingScriptFontFamily,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.Black.copy(alpha = 0.87f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showRenameDialog = true }
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Handwritten decorative line
                HandwrittenLine(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Black.copy(alpha = 0.4f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Progress indication if playing
                if (isPlayingThis) {
                    Text(
                        text = "♪ Playing... ♪",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = dancingScriptFontFamily,
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color.Black.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(2.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress)
                                .height(3.dp)
                                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(2.dp))
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Compact button layout - 6 buttons matching screenshot exactly
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Share button (envelope icon, gray)
                    CompactScrapbookButton(
                        onClick = { showShareDialog = true },
                        icon = Icons.Default.Share,
                        label = "Share",
                        iconColor = Color.Gray
                    )

                    // Play button (play triangle, yellow/orange)
                    CompactScrapbookButton(
                        onClick = {
                            if (isPlayingThis && !isPaused) {
                                onPause()
                            } else {
                                onPlay(recording.originalPath)
                            }
                        },
                        icon = Icons.Default.PlayArrow,
                        label = "Play",
                        iconColor = Color(0xFFFF9800)
                    )

                    // Reversed button (curved arrow, yellow/orange)
                    recording.reversedPath?.let { reversedPath ->
                        CompactScrapbookButton(
                            onClick = {
                                if (isPlayingThis && !isPaused) {
                                    onPause()
                                } else {
                                    onPlay(reversedPath)
                                }
                            },
                            icon = Icons.Default.Repeat,
                            label = "Reversed",
                            iconColor = Color(0xFFFF9800)
                        )
                    }

                    // FWD button (microphone icon, gray) - for starting forward challenge
                    if (isGameModeEnabled) {
                        CompactScrapbookButton(
                            onClick = { onStartAttempt(recording, ChallengeType.FORWARD) },
                            icon = Icons.Default.Mic,
                            label = "FWD",
                            iconColor = Color.Gray
                        )
                    }

                    // REV button (music note icon, purple) - for starting reverse challenge
                    if (isGameModeEnabled) {
                        CompactScrapbookButton(
                            onClick = { onStartAttempt(recording, ChallengeType.REVERSE) },
                            icon = Icons.Default.Star, // Using star as closest to music note
                            label = "REV",
                            iconColor = Color(0xFF9C27B0)
                        )
                    }

                    // Delete button (trash icon, gray)
                    CompactScrapbookButton(
                        onClick = { showDeleteDialog = true },
                        icon = Icons.Default.Delete,
                        label = "Delete",
                        iconColor = Color.Gray
                    )
                }
            }
        }

        // PROPER TAPE CORNERS: Positioned outside card, extend to background, rotate with card
        ProperTapeCorner(
            offsetX = (-8).dp,
            offsetY = (-8).dp,
            rotation = rotation + tapeRotation1
        )

        ProperTapeCorner(
            modifier = Modifier.align(Alignment.TopEnd),
            offsetX = 8.dp,
            offsetY = (-8).dp,
            rotation = rotation + tapeRotation2
        )
    }

    // Dialogs
    ScrapbookRecordingDialogs(
        showRenameDialog = showRenameDialog,
        showDeleteDialog = showDeleteDialog,
        showShareDialog = showShareDialog,
        recording = recording,
        onRename = onRename,
        onDelete = onDelete,
        onShare = onShare,
        onDismissRename = { showRenameDialog = false },
        onDismissDelete = { showDeleteDialog = false },
        onDismissShare = { showShareDialog = false }
    )
}

/**
 * 🎨 HAND DRAWN PROGRESS BAR
 */
@Composable
fun HandDrawnProgressBar(
    progress: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val path = Path().apply {
            val startY = size.height / 2
            val progressWidth = size.width * progress

            moveTo(0f, startY)
            var x = 0f
            while (x < progressWidth) {
                val wobble = kotlin.math.sin(x / 10) * 2f
                lineTo(x, startY + wobble)
                x += 5f
            }
        }

        drawPath(
            path = path,
            color = Color(0xFF4CAF50),
            style = Stroke(width = 4.dp.toPx())
        )
    }
}

/**
 * 🎨 SCRAPBOOK ATTEMPT DIALOGS
 */
@Composable
private fun ScrapbookAttemptDialogs(
    showRenameDialog: Boolean,
    showDeleteDialog: Boolean,
    showShareDialog: Boolean,
    showScoreDialog: Boolean,
    attempt: PlayerAttempt,
    onRenamePlayer: ((PlayerAttempt, String) -> Unit)?,
    onDeleteAttempt: ((PlayerAttempt) -> Unit)?,
    onShareAttempt: ((String) -> Unit)?,
    onDismissRename: () -> Unit,
    onDismissDelete: () -> Unit,
    onDismissShare: () -> Unit,
    onDismissScore: () -> Unit
) {
    if (showRenameDialog && onRenamePlayer != null) {
        var newName by remember { mutableStateOf(attempt.playerName) }
        AlertDialog(
            onDismissRequest = onDismissRename,
            title = { Text("Rename Player 📝") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Player Name") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (newName.isNotBlank()) {
                        onRenamePlayer(attempt, newName)
                    }
                    onDismissRename()
                }) { Text("Rename ✏️") }
            },
            dismissButton = {
                Button(onClick = onDismissRename) { Text("Cancel") }
            }
        )
    }

    if (showDeleteDialog && onDeleteAttempt != null) {
        AlertDialog(
            onDismissRequest = onDismissDelete,
            title = { Text("Delete Attempt? 🗑️") },
            text = { Text("Are you sure you want to delete ${attempt.playerName}'s attempt? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteAttempt(attempt)
                        onDismissDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF1744))
                ) { Text("Delete 💥") }
            },
            dismissButton = {
                Button(onClick = onDismissDelete) { Text("Keep It") }
            }
        )
    }

    if (showShareDialog && onShareAttempt != null) {
        AlertDialog(
            onDismissRequest = onDismissShare,
            title = { Text("Share Attempt 📤") },
            text = {
                Column {
                    Text("Which version would you like to share?")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            onShareAttempt(attempt.attemptFilePath)
                            onDismissShare()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Share Original 🎤")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            attempt.reversedAttemptFilePath?.let { onShareAttempt(it) }
                            onDismissShare()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Share Reversed 🔁")
                    }
                }
            },
            confirmButton = { },
            dismissButton = {
                Button(onClick = onDismissShare) { Text("Cancel") }
            }
        )
    }

    if (showScoreDialog) {
        val scoringResult = remember {
            com.example.reversey.scoring.ScoringResult(
                score = attempt.score,
                rawScore = attempt.rawScore,
                metrics = com.example.reversey.scoring.SimilarityMetrics(
                    pitch = attempt.pitchSimilarity,
                    mfcc = attempt.mfccSimilarity
                ),
                feedback = emptyList()
            )
        }

        ScoreExplanationDialog(
            score = scoringResult,
            challengeType = attempt.challengeType,
            onDismiss = onDismissScore
        )
    }
}

/**
 * 🎨 SCRAPBOOK RECORDING DIALOGS
 */
@Composable
private fun ScrapbookRecordingDialogs(
    showRenameDialog: Boolean,
    showDeleteDialog: Boolean,
    showShareDialog: Boolean,
    recording: Recording,
    onRename: (String, String) -> Unit,
    onDelete: (Recording) -> Unit,
    onShare: (String) -> Unit,
    onDismissRename: () -> Unit,
    onDismissDelete: () -> Unit,
    onDismissShare: () -> Unit
) {
    if (showRenameDialog) {
        var newName by remember { mutableStateOf(recording.name) }
        AlertDialog(
            onDismissRequest = onDismissRename,
            title = { Text("Rename Recording 📝") },
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
                        onRename(recording.originalPath, finalName)
                    }
                    onDismissRename()
                }) { Text("Rename ✏️") }
            },
            dismissButton = {
                Button(onClick = onDismissRename) { Text("Cancel") }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = onDismissDelete,
            title = { Text("Delete Recording? 🗑️") },
            text = { Text("Are you sure you want to delete '${recording.name}'? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete(recording)
                        onDismissDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF1744))
                ) { Text("Delete 💥") }
            },
            dismissButton = {
                Button(onClick = onDismissDelete) { Text("Keep It") }
            }
        )
    }

    if (showShareDialog) {
        AlertDialog(
            onDismissRequest = onDismissShare,
            title = { Text("Share Recording 📤") },
            text = {
                Column {
                    Text("Which version would you like to share?")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            onShare(recording.originalPath)
                            onDismissShare()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Share Original 🎤")
                    }
                    recording.reversedPath?.let { reversedPath ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                onShare(reversedPath)
                                onDismissShare()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Share Reversed 🔁")
                        }
                    }
                }
            },
            confirmButton = { },
            dismissButton = {
                Button(onClick = onDismissShare) { Text("Cancel") }
            }
        )
    }
}