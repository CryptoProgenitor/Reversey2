package com.example.reversey


import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontStyle
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

/**
 * Scrapbook Theme Extension - Add these properties to your existing AppTheme
 */
data class ScrapbookProperties(
    val useScrapbookStyle: Boolean = false,
    val stickyNoteColors: List<Color> = listOf(
        Color(0xFFFFEB3B), // Yellow
        Color(0xFFFF9800), // Orange
        Color(0xFFE91E63), // Pink
        Color(0xFF9C27B0), // Purple
        Color(0xFF4CAF50), // Green
        Color(0xFF2196F3), // Blue
        Color(0xFFFF5722)  // Red
    ),
    val useStarRatings: Boolean = false,
    val useHandDrawnIcons: Boolean = false,
    val useTapeEffects: Boolean = false
)

/**
 * Enhanced AppTheme with Scrapbook Support
 * Add this to your existing Theme.kt
 */
fun AppTheme.withScrapbook(scrapbook: ScrapbookProperties): AppTheme {
    return this.copy(
        // You would add scrapbook properties to your AppTheme data class
    )
}

/**
 * Scrapbook Attempt Item - Anti-Design Aesthetic
 */

// Dancing Script handwriting font family
private val dancingScriptFontFamily = FontFamily(
    Font(R.font.dancing_script_regular, FontWeight.Normal),
    Font(R.font.dancing_script_bold, FontWeight.Bold)
)

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
    theme: AppTheme,
    onJumpToParent: (() -> Unit)? = null,
) {
    val isPlayingThis = currentlyPlayingPath == attempt.attemptFilePath ||
            currentlyPlayingPath == attempt.reversedAttemptFilePath

    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }

    // Random sticky note properties for authenticity
    val stickyNoteColor = remember {
        listOf(
            Color(0xFFFFEB3B), // Yellow
            Color(0xFFFFCDD2), // Light Pink
            Color(0xFFC8E6C9), // Light Green
            Color(0xFFBBDEFB), // Light Blue
            Color(0xFFD1C4E9)  // Light Purple
        ).random()
    }

    val rotation = remember { Random.nextFloat() * 6f - 3f } // -3 to +3 degrees
    val tapeRotation = remember { Random.nextFloat() * 30f - 15f } // -15 to +15 degrees

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 30.dp, end = 10.dp, top = 8.dp, bottom = 8.dp)
    ) {
        // Main sticky note card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .rotate(rotation)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(4.dp),
                    spotColor = Color.Black.copy(alpha = 0.5f)
                ),
            colors = CardDefaults.cardColors(
                containerColor = stickyNoteColor
            ),
            shape = RoundedCornerShape(4.dp) // Squared corners for sticker feel
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header row with player name and star rating
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
                        val challengeIcon = if (attempt.challengeType == ChallengeType.REVERSE) "🔄" else "▶️"
                        Text(
                            text = challengeIcon,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(end = 8.dp)
                        )

                        Text(
                            text = attempt.playerName,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = dancingScriptFontFamily, // CHANGE THIS LINE
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.Black
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Star rating instead of percentage
                    StarRating(
                        score = attempt.score,
                        modifier = Modifier
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Hand-drawn style progress bar (if playing)
                if (isPlayingThis) {
                    HandDrawnProgressBar(
                        progress = progress,
                        color = Color.Black,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                } else {
                    // Doodle line separator
                    DoodleDivider(
                        color = Color.Black.copy(alpha = 0.3f),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Control buttons in scrapbook style
                ScrapbookControlButtons(
                    isPlayingThis = isPlayingThis,
                    isPaused = isPaused,
                    attempt = attempt,
                    onPlay = onPlay,
                    onPause = onPause,
                    onStop = onStop,
                    onJumpToParent = onJumpToParent,
                    onShare = { showShareDialog = true },
                    onDelete = { showDeleteDialog = true }
                )
            }
        }

        // Tape effect in top corner
        TapeEffect(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 8.dp, y = (-8).dp)
                .rotate(tapeRotation)
        )
    }

    // Scrapbook style dialogs
    ScrapbookDialogs(
        showRenameDialog = showRenameDialog,
        showDeleteDialog = showDeleteDialog,
        showShareDialog = showShareDialog,
        attempt = attempt,
        onRenamePlayer = onRenamePlayer,
        onDeleteAttempt = onDeleteAttempt,
        onShareAttempt = onShareAttempt,
        onDismissRename = { showRenameDialog = false },
        onDismissDelete = { showDeleteDialog = false },
        onDismissShare = { showShareDialog = false }
    )
}

/**
 * Star Rating Display - converts percentage to stars
 */
@Composable
fun StarRating(
    score: Int,
    modifier: Modifier = Modifier,
    maxStars: Int = 5
) {
    val filledStars = (score / 20).coerceIn(0, maxStars) // 0-100% to 0-5 stars
    val hasHalfStar = (score % 20) >= 10

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        repeat(maxStars) { index ->
            val starIcon = when {
                index < filledStars -> "★"
                index == filledStars && hasHalfStar -> "☆"
                else -> "☆"
            }
            Text(
                text = starIcon,
                fontSize = 18.sp,
                color = if (index < filledStars || (index == filledStars && hasHalfStar)) {
                    Color(0xFFFFD700) // Gold
                } else {
                    Color.Gray
                }
            )
        }
        // Show percentage in small text
        Text(
            text = " ${score}%",
            fontSize = 12.sp,
            color = Color.Black,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

/**
 * Hand-drawn style progress bar
 */
@Composable
fun HandDrawnProgressBar(
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.height(8.dp)) {
        val width = size.width
        val height = size.height
        val progressWidth = width * progress

        // Draw wavy background line
        val path = Path().apply {
            moveTo(0f, height / 2)
            var x = 0f
            while (x < width) {
                val nextX = x + 20f
                val waveY = height / 2 + kotlin.math.sin(x / 10f) * 2f
                lineTo(nextX.coerceAtMost(width), waveY)
                x = nextX
            }
        }

        drawPath(
            path = path,
            color = color.copy(alpha = 0.3f),
            style = Stroke(width = 3.dp.toPx())
        )

        // Draw progress portion with different color
        val progressPath = Path().apply {
            moveTo(0f, height / 2)
            var x = 0f
            while (x < progressWidth) {
                val nextX = x + 20f
                val waveY = height / 2 + kotlin.math.sin(x / 10f) * 2f
                lineTo(nextX.coerceAtMost(progressWidth), waveY)
                x = nextX
            }
        }

        drawPath(
            path = progressPath,
            color = color,
            style = Stroke(width = 4.dp.toPx())
        )
    }
}

/**
 * Doodle-style divider line
 */
@Composable
fun DoodleDivider(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.height(4.dp)) {
        val width = size.width
        val height = size.height

        val path = Path().apply {
            moveTo(0f, height / 2)
            var x = 0f
            while (x < width) {
                val nextX = x + 15f
                val waveY = height / 2 + kotlin.math.sin(x / 8f) * 1.5f
                lineTo(nextX.coerceAtMost(width), waveY)
                x = nextX
            }
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 2.dp.toPx())
        )
    }
}

/**
 * Scrapbook style control buttons
 */
@Composable
fun ScrapbookControlButtons(
    isPlayingThis: Boolean,
    isPaused: Boolean,
    attempt: PlayerAttempt,
    onPlay: (String) -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit,
    onJumpToParent: (() -> Unit)?,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Jump to parent - hand-drawn up arrow
        StickerButton(
            emoji = "↑",
            label = "Original",
            backgroundColor = Color(0xFF616161),
            onClick = { onJumpToParent?.invoke() },
            size = 28.dp
        )

        // Share - envelope emoji
        StickerButton(
            emoji = "✉️",
            label = "Share",
            backgroundColor = Color(0xFFF3E5F5),
            onClick = onShare,
            size = 28.dp
        )

        Spacer(modifier = Modifier.weight(1f))

        // Play controls
        if (isPlayingThis) {
            StickerButton(
                emoji = if (isPaused) "▶️" else "⏸️",
                label = if (isPaused) "Play" else "Pause",
                backgroundColor = Color(0xFFE8F5E8),
                onClick = onPause,
                size = 36.dp
            )
            StickerButton(
                emoji = "⏹️",
                label = "Stop",
                backgroundColor = Color(0xFFFFEBEE),
                onClick = onStop,
                size = 36.dp
            )
        } else {
            // Play original
            StickerButton(
                emoji = "▶️",
                label = "Play",
                backgroundColor = Color(0xFFE8F5E8),
                onClick = { onPlay(attempt.attemptFilePath) },
                size = 36.dp
            )
            // Play reversed - loop emoji
            StickerButton(
                emoji = "🔁",
                label = "Reverse",
                backgroundColor = Color(0xFFE8F5E8),
                onClick = {
                    attempt.reversedAttemptFilePath?.let { onPlay(it) }
                },
                enabled = attempt.reversedAttemptFilePath != null,
                size = 36.dp
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Delete - trash emoji
        StickerButton(
            emoji = "🗑️",
            label = "Delete",
            backgroundColor = Color(0xFFFFEBEE),
            onClick = onDelete,
            size = 28.dp
        )
    }
}

/**
 * Sticker-style button component WITH LABELS UNDERNEATH
 * NEW: Added label parameter and Column layout for text underneath icons
 */
@Composable
fun StickerButton(
    emoji: String,
    label: String,
    backgroundColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: androidx.compose.ui.unit.Dp = 32.dp
) {
    val rotation = remember { Random.nextFloat() * 10f - 5f } // -5 to +5 degrees

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Sticker button
        Box(
            modifier = Modifier
                .size(size)
                .rotate(rotation)
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(4.dp),
                    spotColor = Color.Black.copy(alpha = 0.3f)
                )
                .background(
                    color = if (enabled) backgroundColor else backgroundColor.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(4.dp)
                )
                .border(
                    width = 2.dp,
                    color = Color.Black.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(4.dp)
                )
                .clickable(enabled = enabled) { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = emoji,
                fontSize = (size.value * 0.6).sp,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Label underneath
        Text(
            text = label,
            fontSize = 9.sp, // Slightly larger for Dancing Script readability
            color = Color(0xFF212121),
            fontWeight = FontWeight.Bold,
            fontFamily = dancingScriptFontFamily,  // ADD THIS LINE
            textAlign = TextAlign.Center,
            modifier = Modifier.rotate(rotation * 0.3f)
        )
    }
}

/**
 * Tape effect for corners of sticky notes
 */
@Composable
fun TapeEffect(
    modifier: Modifier = Modifier,
    tapeColor: Color = Color(0xFFFAF0E6) // Off-white tape
) {
    Box(
        modifier = modifier
            .size(width = 30.dp, height = 15.dp)
            .background(
                color = tapeColor,
                shape = RoundedCornerShape(2.dp)
            )
            .border(
                width = 1.dp,
                color = Color.Gray.copy(alpha = 0.3f),
                shape = RoundedCornerShape(2.dp)
            )
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(2.dp)
            )
    ) {
        // Add subtle tape texture lines
        Canvas(modifier = Modifier.fillMaxSize()) {
            val lineColor = Color.Gray.copy(alpha = 0.2f)
            val lineWidth = 0.5.dp.toPx()

            // Draw horizontal lines to simulate tape texture
            for (i in 1..2) {
                val y = size.height * (i / 3f)
                drawLine(
                    color = lineColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = lineWidth
                )
            }
        }
    }
}

/**
 * Scrapbook Recording Item - Parent card with sticky note style
 */
@OptIn(ExperimentalFoundationApi::class)
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
    onStartAttempt: (Recording, ChallengeType) -> Unit,
    theme: AppTheme
) {
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }

    // Random properties for this recording's sticky note
    val stickyNoteColor = remember {
        listOf(
            Color(0xFFFFEB3B), // Bright Yellow
            Color(0xFFFF9800), // Orange
            Color(0xFFE91E63), // Pink
            Color(0xFF9C27B0)  // Purple
        ).random()
    }

    val rotation = remember { Random.nextFloat() * 4f - 2f } // -2 to +2 degrees
    val tapeRotation1 = remember { Random.nextFloat() * 30f - 15f }
    val tapeRotation2 = remember { Random.nextFloat() * 30f - 15f }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        // Main recording card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .rotate(rotation)
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(8.dp),
                    spotColor = Color.Black.copy(alpha = 0.6f)
                ),
            colors = CardDefaults.cardColors(
                containerColor = stickyNoteColor
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Recording name with hand-drawn style
                Text(
                    text = recording.name,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = dancingScriptFontFamily, // CHANGE THIS LINE
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    ),
                    color = Color.Black,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showRenameDialog = true }
                        .padding(bottom = 12.dp)
                )

                // Hand-drawn progress bar
                if (isPlaying) {
                    HandDrawnProgressBar(
                        progress = progress,
                        color = Color.Black,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    DoodleDivider(
                        color = Color.Black.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Control buttons in scrapbook style
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Share button
                    StickerButton(
                        emoji = "✉️",
                        label = "Share",
                        backgroundColor = Color(0xFFF3E5F5),
                        onClick = { showShareDialog = true },
                        size = 40.dp
                    )

                    // Play controls
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isPlaying) {
                            StickerButton(
                                emoji = if (isPaused) "▶️" else "⏸️",
                                label = if (isPaused) "Play" else "Pause",
                                backgroundColor = Color(0xFFE8F5E8),
                                onClick = { onPause() },
                                size = 50.dp
                            )
                            StickerButton(
                                emoji = "⏹️",
                                label = "Stop",
                                backgroundColor = Color(0xFFFFEBEE),
                                onClick = { onStop() },
                                size = 50.dp
                            )
                        } else {
                            StickerButton(
                                emoji = "▶️",
                                label = "Play",
                                backgroundColor = Color(0xFFE8F5E8),
                                onClick = { onPlay(recording.originalPath) },
                                size = 50.dp
                            )
                            StickerButton(
                                emoji = "🔁",
                                label = "Reversed",
                                backgroundColor = Color(0xFFE8F5E8),
                                onClick = { recording.reversedPath?.let { onPlay(it) } },
                                enabled = recording.reversedPath != null,
                                size = 50.dp
                            )
                        }
                    }

                    // Game mode and delete
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isGameModeEnabled) {
                            // Forward challenge
                            StickerButton(
                                emoji = "🎤",
                                label = "FWD",
                                backgroundColor = Color(0xFFE1F5FE),
                                onClick = { onStartAttempt(recording, ChallengeType.FORWARD) },
                                size = 45.dp
                            )
                            // Reverse challenge
                            StickerButton(
                                emoji = "🎵",
                                label = "REV",
                                backgroundColor = Color(0xFFE1F5FE),
                                onClick = { onStartAttempt(recording, ChallengeType.REVERSE) },
                                size = 45.dp
                            )
                        }
                        StickerButton(
                            emoji = "🗑️",
                            label = "Delete",
                            backgroundColor = Color(0xFFFFEBEE),
                            onClick = { showDeleteDialog = true },
                            size = 40.dp
                        )
                    }
                }
            }
        }

        // Tape effects in corners
        TapeEffect(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-5).dp, y = (-5).dp)
                .rotate(tapeRotation1)
        )
        TapeEffect(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 5.dp, y = (-5).dp)
                .rotate(tapeRotation2)
        )
    }

    // Scrapbook style dialogs (same as attempt item)
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
 * Scrapbook style dialogs for attempts
 */
@Composable
private fun ScrapbookDialogs(
    showRenameDialog: Boolean,
    showDeleteDialog: Boolean,
    showShareDialog: Boolean,
    attempt: PlayerAttempt,
    onRenamePlayer: ((PlayerAttempt, String) -> Unit)?,
    onDeleteAttempt: ((PlayerAttempt) -> Unit)?,
    onShareAttempt: ((String) -> Unit)?,
    onDismissRename: () -> Unit,
    onDismissDelete: () -> Unit,
    onDismissShare: () -> Unit
) {
    // Rename Dialog
    if (showRenameDialog) {
        var newPlayerName by remember { mutableStateOf(attempt.playerName) }
        AlertDialog(
            onDismissRequest = onDismissRename,
            title = {
                Text(
                    "Rename Player 📝",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = FontFamily.Serif
                    )
                )
            },
            text = {
                OutlinedTextField(
                    value = newPlayerName,
                    onValueChange = { newPlayerName = it },
                    label = { Text("Player Name") }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPlayerName.isNotBlank() && onRenamePlayer != null) {
                            onRenamePlayer(attempt, newPlayerName)
                        }
                        onDismissRename()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Text("Save ✏️", color = Color.White)
                }
            },
            dismissButton = {
                Button(onClick = onDismissRename) { Text("Cancel") }
            }
        )
    }

    // Delete Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = onDismissDelete,
            title = { Text("Delete Attempt? 🗑️") },
            text = {
                Text("Are you sure you want to delete ${attempt.playerName}'s attempt? This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteAttempt?.invoke(attempt)
                        onDismissDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF1744))
                ) {
                    Text("Delete 💥", color = Color.White)
                }
            },
            dismissButton = {
                Button(onClick = onDismissDelete) { Text("Keep It") }
            }
        )
    }

    // Share Dialog
    if (showShareDialog) {
        AlertDialog(
            onDismissRequest = onDismissShare,
            title = { Text("Share Attempt 📤") },
            text = {
                Column {
                    Text("Which version would you like to share?")
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            onShareAttempt?.invoke(attempt.attemptFilePath)
                            onDismissShare()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                    ) {
                        Text("Share Original 🎤", color = Color.White)
                    }

                    if (attempt.reversedAttemptFilePath != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                onShareAttempt?.invoke(attempt.reversedAttemptFilePath!!)
                                onDismissShare()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                        ) {
                            Text("Share Reversed 🔁", color = Color.White)
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

/**
 * Scrapbook style dialogs for recordings
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
    // Similar to the attempt dialogs but for recordings
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
                    if (recording.reversedPath != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                onShare(recording.reversedPath!!)
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