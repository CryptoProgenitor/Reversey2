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
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.reversey.ui.components.ScoreExplanationDialog
import kotlin.random.Random
import com.example.reversey.ui.theme.aestheticTheme
import com.example.reversey.ui.theme.materialColors

// Dancing Script handwriting font family
private val dancingScriptFontFamily = FontFamily(
    Font(R.font.dancing_script_regular, FontWeight.Normal),
    Font(R.font.dancing_script_bold, FontWeight.Bold)
)

/**
 * 🎨 MAIN SCRAPBOOK ATTEMPT ITEM - Fixed compilation errors
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

    // 🎨 FIXED: Use attemptFilePath for stable ID instead of attempt.id
    val stableId = attempt.attemptFilePath.hashCode()

    // 🎨 IMPROVED: More stable colors and rotations to prevent visual artifacts
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
        // More consistent rotation based on stable ID
        (stableId % 7 - 3).toFloat() // -3 to +3 degrees
    }

    // 🎨 IMPROVED: Better container layout with proper spacing
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp) // More consistent spacing
    ) {
        // Main sticky note card with improved shadow and border
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
                    .padding(16.dp) // Consistent inner padding
            ) {
                // 🎨 IMPROVED: Better header layout
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Player name with challenge icon - better layout
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
                                fontFamily = dancingScriptFontFamily,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.Black.copy(alpha = 0.87f), // Better text contrast
                            maxLines = 1 // Prevent text overflow
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // 🎨 FIXED: Using the StarRating component
                    StarRating(
                        score = attempt.score,
                        modifier = Modifier,
                        onClick = { showScoreDialog = true }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 🎨 IMPROVED: Progress indication with better styling
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

                    Spacer(modifier = Modifier.height(8.dp))

                    // Simple progress bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(2.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress)
                                .height(4.dp)
                                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(2.dp))
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                // 🎨 IMPROVED: Better button layout with consistent spacing
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp), // Consistent spacing
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Share button
                    ImprovedScrapbookButton(
                        onClick = { showShareDialog = true },
                        backgroundColor = Color(0xFF4CAF50),
                        label = "📤",
                        modifier = Modifier.weight(1f)
                    )

                    // Play controls
                    if (isPlayingThis) {
                        ImprovedScrapbookButton(
                            onClick = onPause,
                            backgroundColor = Color(0xFF2196F3),
                            label = if (isPaused) "▶️" else "⏸️",
                            modifier = Modifier.weight(1f)
                        )
                        ImprovedScrapbookButton(
                            onClick = onStop,
                            backgroundColor = Color(0xFFFF5722),
                            label = "⏹️",
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        ImprovedScrapbookButton(
                            onClick = { onPlay(attempt.attemptFilePath) },
                            backgroundColor = Color(0xFF2196F3),
                            label = "▶️",
                            modifier = Modifier.weight(1f)
                        )

                        // 🎨 FIXED: Handle nullable reversedAttemptFilePath
                        ImprovedScrapbookButton(
                            onClick = {
                                attempt.reversedAttemptFilePath?.let { onPlay(it) }
                            },
                            backgroundColor = Color(0xFF9C27B0),
                            label = "🔄",
                            modifier = Modifier.weight(1f),
                            enabled = attempt.reversedAttemptFilePath != null
                        )
                    }

                    // Delete button
                    ImprovedScrapbookButton(
                        onClick = { showDeleteDialog = true },
                        backgroundColor = Color(0xFFFF1744),
                        label = "🗑️",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 🎨 IMPROVED: Better tape effect positioning
        ImprovedTapeEffect(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-8).dp, y = (-8).dp)
        )
    }

    // 🎨 IMPROVED: Dialogs with better styling
    ImprovedScrapbookDialogs(
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
 * 🎨 STAR RATING COMPONENT - Maps scores to emoji ratings
 */
@Composable
fun StarRating(
    score: Int,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    // 🎨 SCRAPBOOK STAR RATING SYSTEM - matches ScrapbookTheme.kt
    val (emoji, description) = when {
        score >= 90 -> "⭐" to "Excellent!"
        score >= 80 -> "😊" to "Good job!"
        score >= 70 -> "👍" to "Nice try!"
        score >= 60 -> "😐" to "Keep practicing"
        else -> "😔" to "Try again"
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            )
            .padding(4.dp)
    ) {
        // Large emoji
        Text(
            text = emoji,
            fontSize = 24.sp,
            modifier = Modifier.padding(end = 4.dp)
        )

        // Score percentage (small)
        Text(
            text = "${score}%",
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * 🎨 IMPROVED SCRAPBOOK BUTTON - Consistent styling
 */
@Composable
private fun ImprovedScrapbookButton(
    onClick: () -> Unit,
    backgroundColor: Color,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor.copy(alpha = if (enabled) 0.9f else 0.5f),
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
            .height(36.dp) // Consistent height
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            ),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * 🎨 IMPROVED TAPE EFFECT - Cleaner rendering
 */
@Composable
private fun ImprovedTapeEffect(
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier.size(32.dp)
    ) {
        val path = Path().apply {
            moveTo(0f, 8f)
            lineTo(size.width - 8f, 0f)
            lineTo(size.width, 8f)
            lineTo(8f, size.height)
            close()
        }

        // Semi-transparent tape color
        drawPath(
            path = path,
            color = Color(0x66F5F5DC), // Beige tape color with transparency
            style = Stroke(width = 2.dp.toPx())
        )

        drawPath(
            path = path,
            color = Color(0x33F5F5DC) // Fill with lower opacity
        )
    }
}

/**
 * 🎨 IMPROVED SCRAPBOOK DIALOGS - Better UX
 */
@Composable
private fun ImprovedScrapbookDialogs(
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
    // Rename dialog
    if (showRenameDialog && onRenamePlayer != null) {
        var newName by remember { mutableStateOf(attempt.playerName) }
        AlertDialog(
            onDismissRequest = onDismissRename,
            title = { Text("Rename Player 📝") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Player Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (newName.isNotBlank()) {
                        onRenamePlayer(attempt, newName.trim())
                    }
                    onDismissRename()
                }) { Text("Rename ✏️") }
            },
            dismissButton = {
                Button(onClick = onDismissRename) { Text("Cancel") }
            }
        )
    }

    // Delete dialog
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

    // Share dialog
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
                        Text("Share Original Attempt 🎤")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    // 🎨 FIXED: Handle nullable reversedAttemptFilePath
                    attempt.reversedAttemptFilePath?.let { reversedPath ->
                        Button(
                            onClick = {
                                onShareAttempt(reversedPath)
                                onDismissShare()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Share Reversed Attempt 🔁")
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

    // 🎨 FIXED: Score explanation dialog with correct signature
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

        // Use the correct function signature based on what's available
        ScoreExplanationDialog(
            score = scoringResult,
            challengeType = attempt.challengeType,
            onDismiss = onDismissScore
        )
    }
}

/**
 * 🎨 SCRAPBOOK RECORDING ITEM - For recordings list
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
    onStartAttempt: (Recording, ChallengeType) -> Unit
) {
    val aesthetic = aestheticTheme()
    val colors = materialColors()

    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }

    // Random sticky note properties
    val stickyNoteColor = remember {
        listOf(
            Color(0xFFFFEB3B), // Yellow
            Color(0xFFFFCDD2), // Light Pink
            Color(0xFFC8E6C9), // Light Green
            Color(0xFFBBDEFB), // Light Blue
            Color(0xFFD1C4E9)  // Light Purple
        ).random()
    }

    val rotation = remember { Random.nextFloat() * 4f - 2f }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .rotate(rotation)
            .shadow(6.dp, RoundedCornerShape(4.dp)),
        colors = CardDefaults.cardColors(containerColor = stickyNoteColor),
        shape = RoundedCornerShape(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Recording name
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = recording.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = dancingScriptFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = Color.Black,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showRenameDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (isPlaying) {
                HandDrawnProgressBar(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Control buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Share button
                ScrapbookButton(
                    onClick = { showShareDialog = true },
                    backgroundColor = Color(0xFF4CAF50),
                    label = "📤 Share"
                )

                if (isPlaying) {
                    ScrapbookButton(
                        onClick = onPause,
                        backgroundColor = Color(0xFF2196F3),
                        label = if (isPaused) "▶️" else "⏸️"
                    )
                    ScrapbookButton(
                        onClick = onStop,
                        backgroundColor = Color(0xFFFF5722),
                        label = "⏹️"
                    )
                } else {
                    ScrapbookButton(
                        onClick = { onPlay(recording.originalPath) },
                        backgroundColor = Color(0xFF2196F3),
                        label = "▶️ Play"
                    )
                    // 🎨 FIXED: Handle nullable reversedPath
                    recording.reversedPath?.let { reversedPath ->
                        ScrapbookButton(
                            onClick = { onPlay(reversedPath) },
                            backgroundColor = Color(0xFF9C27B0),
                            label = "🔄"
                        )
                    }
                }

                if (isGameModeEnabled) {
                    ScrapbookButton(
                        onClick = { onStartAttempt(recording, ChallengeType.FORWARD) },
                        backgroundColor = Color(0xFFFF9800),
                        label = "🎯 FWD"
                    )
                    ScrapbookButton(
                        onClick = { onStartAttempt(recording, ChallengeType.REVERSE) },
                        backgroundColor = Color(0xFFE91E63),
                        label = "🎯 REV"
                    )
                }

                ScrapbookButton(
                    onClick = { showDeleteDialog = true },
                    backgroundColor = Color(0xFFFF1744),
                    label = "🗑️"
                )
            }
        }
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
 * 🎨 SCRAPBOOK BUTTON - For recording items
 */
@Composable
private fun ScrapbookButton(
    onClick: () -> Unit,
    backgroundColor: Color,
    label: String,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            disabledContainerColor = backgroundColor.copy(alpha = 0.5f)
        ),
        modifier = Modifier
            .height(32.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = dancingScriptFontFamily,
                fontSize = 10.sp
            ),
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }
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
            // Create a slightly wavy line for hand-drawn effect
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
                    // 🎨 FIXED: Handle nullable reversedPath
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