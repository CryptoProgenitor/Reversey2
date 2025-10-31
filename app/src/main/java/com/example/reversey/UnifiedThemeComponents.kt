package com.example.reversey

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.reversey.ui.theme.aestheticTheme
import com.example.reversey.ui.theme.materialColors
import com.example.reversey.ui.components.ScoreExplanationDialog
import com.example.reversey.ui.icons.EggIcons
import com.example.reversey.ui.theme.EggButtonStyle
import com.example.reversey.ui.theme.AestheticThemeData
import com.example.reversey.ui.components.egg.EggStylePlayerCard
import com.example.reversey.ui.components.unified.UnifiedRecordButton

// Dancing Script handwriting font family for scrapbook theme
private val dancingScriptFontFamily = FontFamily(
    Font(R.font.dancing_script_regular, FontWeight.Normal),
    Font(R.font.dancing_script_bold, FontWeight.Bold)
)

/**
 * 🎯 GLUTE: Grand Luxurious Unified Theme Engine
 * Single component that adapts to all aesthetic themes
 */
@Composable
fun UnifiedAttemptItem(
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

    // Shared state management
    var showScoreDialog by remember { mutableStateOf(false) }
    val isPlayingThis = currentlyPlayingPath == attempt.attemptFilePath ||
            currentlyPlayingPath == attempt.reversedAttemptFilePath
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }

    // Route to appropriate theme implementation
    // 🥚 GLUTE: Route to appropriate theme implementation
    when {
        // EGG THEME DETECTION
        aesthetic.id == "egg" -> {
            EggStylePlayerCard(
                playerName = attempt.playerName,
                score = "${attempt.score.toInt()}%",
                eggEmoji = aesthetic.scoreEmojis.entries
                    .sortedByDescending { it.key }
                    .firstOrNull { attempt.score.toInt() >= it.key }?.value ?: "🥚",
                isPlaying = isPlayingThis, // 🔧 ADD THIS LINE to show progress bar on attempt card
                isPaused = isPaused,       // 🔧 ADD THIS LINE to show progress bar on attempt card
                progress = progress,       // 🔧 ADD THIS LINE to show progress bar on attempt card
                //onShare = {
                //    println("🥚 SHARE: Calling onShareAttempt")
                //    onShareAttempt?.invoke(attempt.attemptFilePath ?: "")  // ← Correct: passing String path
                //},

                onShare = {//<---GEMINI CODE
                    println("🥚 SHARE: Setting showShareDialog = true")
                    showShareDialog = true
                },

                onPlay = {
                    println("🥚 PLAY BUTTON CLICKED!") // ← Add this debug line
                    if (isPlayingThis && !isPaused) onPause()
                    else onPlay(attempt.attemptFilePath ?: "")
                },
                onReverse = {
                    println("🥚 REVERSE BUTTON CLICKED!")
                    attempt.reversedAttemptFilePath?.let { onPlay(it) }
                },
                onNavigateToParent = {
                    onJumpToParent?.invoke()
                },
                onShowRenameDialog = {
                    showRenameDialog = true
                },
                onShowDeleteDialog = {
                    println("🥚 DELETE DIALOG: Setting showDeleteDialog to $it")
                    showDeleteDialog = it
                },
                onScoreClick = { showScoreDialog = true }
            )

            // Score dialog for egg theme
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
                    onDismiss = { showScoreDialog = false }
                )
            }

            // ✅ Share dialog for egg theme //<---GEMINI CODE
            if (showShareDialog && onShareAttempt != null) {
                AlertDialog(
                    onDismissRequest = { showShareDialog = false },
                    title = { Text("Share Attempt 📤") },
                    text = {
                        Column {
                            Text("Which version would you like to share?")
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    onShareAttempt(attempt.attemptFilePath)
                                    showShareDialog = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Share Fresh Egg 🥚")
                            }
                            if (attempt.reversedAttemptFilePath != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        onShareAttempt(attempt.reversedAttemptFilePath!!)
                                        showShareDialog = false
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Share Scrambled Egg🍳")
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

            // DELETE DIALOG FOR EGG THEME - FIXED!
            if (showDeleteDialog && onDeleteAttempt != null) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Delete Egg Attempt? 🥚💔", color = Color(0xFF2E2E2E), fontWeight = FontWeight.Bold) },
                    text = { Text("Are you sure you want to crack ${attempt.playerName}'s attempt? This cannot be undone!", color = Color(0xFF2E2E2E)) },
                    confirmButton = {
                        Button(
                            onClick = {
                                onDeleteAttempt(attempt)
                                showDeleteDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722))
                        ) { Text("Crack It! 🔨", color = Color.White, fontWeight = FontWeight.Bold) }
                    },
                    dismissButton = {
                        Button(
                            onClick = { showDeleteDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E2E2E))
                        ) { Text("Keep Egg Safe 🥚", color = Color.White, fontWeight = FontWeight.Bold) }
                    }
                )
            }

            // ✅ RENAME DIALOG FOR EGG THEME
            if (showRenameDialog && onRenamePlayer != null) {
                var newName by remember { mutableStateOf(attempt.playerName) }
                AlertDialog(
                    onDismissRequest = { showRenameDialog = false },
                    title = { Text("Rename Player 🥚✏️", color = Color(0xFF2E2E2E), fontWeight = FontWeight.Bold) },
                    text = {
                        OutlinedTextField(
                            value = newName,
                            onValueChange = { newName = it },
                            label = { Text("Player Name") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFFD54F),
                                focusedLabelColor = Color(0xFF2E2E2E)
                            )
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (newName.isNotBlank()) {
                                    onRenamePlayer(attempt, newName)
                                }
                                showRenameDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD54F))
                        ) { Text("Rename Egg 🥚", color = Color(0xFF2E2E2E), fontWeight = FontWeight.Bold) }
                    },
                    dismissButton = {
                        Button(
                            onClick = { showRenameDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E2E2E))
                        ) { Text("Cancel", color = Color.White) }
                    }
                )
            }
        }
        // SCRAPBOOK THEMES
        aesthetic.useScrapbookElements -> {
            ScrapbookStyleAttemptItem(
                aesthetic = aesthetic,
                attempt = attempt,
                isPlayingThis = isPlayingThis,
                progress = progress,
                showScoreDialog = showScoreDialog,
                showRenameDialog = showRenameDialog,
                showDeleteDialog = showDeleteDialog,
                showShareDialog = showShareDialog,
                onPlay = onPlay,
                onPause = onPause,
                onStop = onStop,
                onRenamePlayer = onRenamePlayer,
                onDeleteAttempt = onDeleteAttempt,
                onShareAttempt = onShareAttempt,
                onJumpToParent = onJumpToParent,
                onShowScoreDialog = { showScoreDialog = it },
                onShowRenameDialog = { showRenameDialog = it },
                onShowDeleteDialog = { showDeleteDialog = it },
                onShowShareDialog = { showShareDialog = it },
                isPaused = isPaused
            )
        }
        // MODERN THEMES
        else -> {
            ModernStyleAttemptItem(
                attempt = attempt,
                isPlayingThis = isPlayingThis,
                progress = progress,
                showScoreDialog = showScoreDialog,
                showRenameDialog = showRenameDialog,
                showDeleteDialog = showDeleteDialog,
                showShareDialog = showShareDialog,
                onPlay = onPlay,
                onPause = onPause,
                onStop = onStop,
                onRenamePlayer = onRenamePlayer,
                onDeleteAttempt = onDeleteAttempt,
                onShareAttempt = onShareAttempt,
                onJumpToParent = onJumpToParent,
                onShowScoreDialog = { showScoreDialog = it },
                onShowRenameDialog = { showRenameDialog = it },
                onShowDeleteDialog = { showDeleteDialog = it },
                onShowShareDialog = { showShareDialog = it },
                isPaused = isPaused
            )
        }
    }
}

/**
 * 📝 SCRAPBOOK STYLE - Sticky notes, tape corners, handwritten elements
 */
@Composable
private fun ScrapbookStyleAttemptItem(
    aesthetic: AestheticThemeData,
    attempt: PlayerAttempt,
    isPlayingThis: Boolean,
    progress: Float,
    showScoreDialog: Boolean,
    showRenameDialog: Boolean,
    showDeleteDialog: Boolean,
    showShareDialog: Boolean,
    onPlay: (String) -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit,
    onRenamePlayer: ((PlayerAttempt, String) -> Unit)?,
    onDeleteAttempt: ((PlayerAttempt) -> Unit)?,
    onShareAttempt: ((String) -> Unit)?,
    onJumpToParent: (() -> Unit)?,
    onShowScoreDialog: (Boolean) -> Unit,
    onShowRenameDialog: (Boolean) -> Unit,
    onShowDeleteDialog: (Boolean) -> Unit,
    onShowShareDialog: (Boolean) -> Unit,
    isPaused: Boolean
) {
    val colors = materialColors()

    // Stable ID for consistent colors and rotation
    val stableId = attempt.attemptFilePath.hashCode()

    // Sticky note color selection
    val stickyNoteColor = remember(stableId) {
        val noteColors = listOf(
            Color(0xFFFFF59D), // Soft Yellow
            Color(0xFFF8BBD9), // Soft Pink
            Color(0xFFC8E6C9), // Soft Green
            Color(0xFFBBDEFB), // Soft Blue
            Color(0xFFE1BEE7), // Soft Purple
            Color(0xFFFFCCBC), // Soft Orange
        )
        noteColors[stableId.mod(noteColors.size)]
    }

    val rotation = remember(stableId) {
        (stableId % 7 - 3).toFloat() // -3 to +3 degrees
    }

    // Tape corner rotations
    val tapeRotation1 = remember(stableId) { (stableId % 31 - 15).toFloat() }
    val tapeRotation2 = remember(stableId) { ((stableId * 17) % 31 - 15).toFloat() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 48.dp, end = 16.dp, top = 12.dp, bottom = 12.dp)
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
                            .clickable { onShowRenameDialog(true) }
                    ) {
                        val challengeIcon = if (attempt.challengeType == ChallengeType.REVERSE) "🔄" else "▶️"
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
                            color = Color.Black.copy(alpha = 0.8f)
                        )
                    }

                    // Star rating
                    Row(
                        modifier = Modifier.clickable { onShowScoreDialog(true) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val fullStars = attempt.score.toInt() / 20 // Convert percentage to stars (0-5)
                        val percentage = "${attempt.score.toInt()}%"

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

                Spacer(modifier = Modifier.height(8.dp))

                // Control buttons - Original first, then Share!
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Original button (FIRST) - Go to parent recording
                    if (onJumpToParent != null) {
                        ScrapbookButton(
                            onClick = onJumpToParent,
                            icon = Icons.Default.Home,
                            label = "Original",
                            iconColor = Color(0xFF607D8B)
                        )
                    }

                    // Share button (SECOND)
                    ScrapbookButton(
                        onClick = { onShowShareDialog(true) },
                        icon = Icons.Default.Share,
                        label = "Share",
                        iconColor = Color(0xFF9C27B0)
                    )

                    // Play controls (THIRD)
                    if (isPlayingThis && !isPaused) {
                        if (aesthetic.useEggElements) {
                            EggScrapbookButton(
                                onClick = onPause,
                                eggType = "cracked",
                                label = "Pause",
                                iconColor = Color(0xFF4CAF50)
                            )
                        } else {
                            ScrapbookButton(
                                onClick = onPause,
                                icon = Icons.Default.Pause,
                                label = "Pause",
                                iconColor = Color(0xFF4CAF50)
                            )
                        }
                    } else {
                        if (aesthetic.useEggElements) {
                            EggScrapbookButton(
                                onClick = { onPlay(attempt.attemptFilePath) },
                                eggType = "whole",
                                label = "Play",
                                iconColor = Color(0xFF2196F3)
                            )
                        } else {
                            ScrapbookButton(
                                onClick = { onPlay(attempt.attemptFilePath) },
                                icon = Icons.Default.PlayArrow,
                                label = "Play",
                                iconColor = Color(0xFF2196F3)
                            )
                        }
                    }

                    // Rev button (FOURTH)
                    if (attempt.reversedAttemptFilePath != null) {
                        if (aesthetic.useEggElements) {
                            EggScrapbookButton(
                                onClick = { onPlay(attempt.reversedAttemptFilePath!!) },
                                eggType = "fried",
                                label = "Reverse",
                                iconColor = Color(0xFFFF9800)
                            )
                        } else {
                            ScrapbookButton(
                                onClick = { onPlay(attempt.reversedAttemptFilePath!!) },
                                icon = Icons.Default.FastForward,
                                label = "Reverse",
                                iconColor = Color(0xFFFF9800)
                            )
                        }
                    }

                    // Delete button (LAST)
                    if (aesthetic.useEggElements) {
                        EggScrapbookButton(
                            onClick = { onShowDeleteDialog(true) },
                            eggType = "cracked",
                            label = "Del",
                            iconColor = Color(0xFFFF1744)
                        )
                    } else {
                        ScrapbookButton(
                            onClick = { onShowDeleteDialog(true) },
                            icon = Icons.Default.Delete,
                            label = "Del",
                            iconColor = Color(0xFFFF1744)
                        )
                    }
                }

                // Progress indicator
                if (isPlayingThis) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF4CAF50)
                    )
                }
            }
        }

        // Tape corners
        TapeCorner(
            modifier = Modifier.align(Alignment.TopStart),
            offsetX = (-12).dp,
            offsetY = 8.dp,
            rotation = tapeRotation1
        )
        TapeCorner(
            modifier = Modifier.align(Alignment.BottomEnd),
            offsetX = 12.dp,
            offsetY = (-8).dp,
            rotation = tapeRotation2
        )
    }

    // Dialogs
    ScrapbookDialogs(
        showRenameDialog = showRenameDialog,
        showDeleteDialog = showDeleteDialog,
        showShareDialog = showShareDialog,
        showScoreDialog = showScoreDialog,
        attempt = attempt,
        onRenamePlayer = onRenamePlayer,
        onDeleteAttempt = onDeleteAttempt,
        onShareAttempt = onShareAttempt,
        onDismissRename = { onShowRenameDialog(false) },
        onDismissDelete = { onShowDeleteDialog(false) },
        onDismissShare = { onShowShareDialog(false) },
        onDismissScore = { onShowScoreDialog(false) }
    )
}

/**
 * ⚡ MODERN STYLE - Glassmorphism, glow effects, radial progress
 */
@Composable
private fun ModernStyleAttemptItem(
    attempt: PlayerAttempt,
    isPlayingThis: Boolean,
    progress: Float,
    showScoreDialog: Boolean,
    showRenameDialog: Boolean,
    showDeleteDialog: Boolean,
    showShareDialog: Boolean,
    onPlay: (String) -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit,
    onRenamePlayer: ((PlayerAttempt, String) -> Unit)?,
    onDeleteAttempt: ((PlayerAttempt) -> Unit)?,
    onShareAttempt: ((String) -> Unit)?,
    onJumpToParent: (() -> Unit)?,
    onShowScoreDialog: (Boolean) -> Unit,
    onShowRenameDialog: (Boolean) -> Unit,
    onShowDeleteDialog: (Boolean) -> Unit,
    onShowShareDialog: (Boolean) -> Unit,
    isPaused: Boolean
) {
    val aesthetic = aestheticTheme()
    val colors = materialColors()

    // Glassmorphism card with glow effect
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 25.dp, end = 0.dp, top = 4.dp, bottom = 4.dp)
            .then(
                if (aesthetic.useGlassmorphism && aesthetic.glowIntensity > 0) {
                    Modifier.shadow(
                        elevation = (aesthetic.glowIntensity * 20).dp,
                        shape = RoundedCornerShape(16.dp),
                        spotColor = colors.primary
                    )
                } else Modifier
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (aesthetic.useGlassmorphism) {
                colors.surface.copy(alpha = aesthetic.cardAlpha)
            } else colors.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (aesthetic.useGlassmorphism) {
                        Modifier
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.1f),
                                        Color.Transparent
                                    )
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = Color.White.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(16.dp)
                            )
                    } else Modifier.border(1.dp, aesthetic.cardBorder, RoundedCornerShape(16.dp))
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side: Player info
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onShowRenameDialog(true) }
                    ) {
                        // Go to Parent button
                        if (onJumpToParent != null) {
                            IconButton(
                                onClick = onJumpToParent,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Home,
                                    contentDescription = "Go to Parent",
                                    tint = colors.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Challenge type icon
                        val challengeIcon = if (attempt.challengeType == ChallengeType.REVERSE) "🔄" else "▶️"
                        Text(
                            text = challengeIcon,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(end = 8.dp)
                        )

                        // Player name
                        Text(
                            text = attempt.playerName,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = if (aesthetic.useWideLetterSpacing) 1.2.sp else 0.sp
                            ),
                            color = colors.onSurface,
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Control buttons - evenly spaced across full width
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Share button (first) - Secondary color (same as primary)
                        EnhancedGlowButton(
                            onClick = { onShowShareDialog(true) },
                            isSecondary = true,
                            label = "Share"
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Play/Pause button (second) - Primary color
                        if (isPlayingThis && !isPaused) {
                            EnhancedGlowButton(
                                onClick = onPause,
                                isPrimary = true,
                                label = "Pause"
                            ) {
                                if (aesthetic.useEggElements) {
                                    EggIcons.CrackedEggIcon(
                                        size = 16.dp,
                                        tint = Color.White
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Pause,
                                        contentDescription = "Pause",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        } else {
                            EnhancedGlowButton(
                                onClick = { onPlay(attempt.attemptFilePath) },
                                isPrimary = true,
                                label = "Play"
                            ) {
                                if (aesthetic.useEggElements) {
                                    EggIcons.WholeEggIcon(
                                        size = 16.dp,
                                        tint = Color.White
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Play",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        // Rev button (third) - Secondary color (same as primary)
                        if (attempt.reversedAttemptFilePath != null) {
                            EnhancedGlowButton(
                                onClick = { onPlay(attempt.reversedAttemptFilePath!!) },
                                isSecondary = true,
                                label = "Rev"
                            ) {
                                if (aesthetic.useEggElements) {
                                    EggIcons.FriedEggIcon(
                                        size = 16.dp,
                                        tint = Color.White
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Repeat,
                                        contentDescription = "Reverse",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        // Delete button (last) - Destructive color
                        EnhancedGlowButton(
                            onClick = { onShowDeleteDialog(true) },
                            isDestructive = true,
                            label = "Del"
                        ) {
                            if (aesthetic.useEggElements) {
                                EggIcons.CrackedEggIcon(
                                    size = 16.dp,
                                    tint = Color.White
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Right side: Radial progress score
                RadialScoreDisplay(
                    score = attempt.score,
                    isAnimated = true,
                    onClick = { onShowScoreDialog(true) }
                )
            }

            // Progress indicator
            if (isPlayingThis) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth(),
                    color = colors.primary
                )
            }
        }
    }

    // Dialogs
    ModernDialogs(
        showRenameDialog = showRenameDialog,
        showDeleteDialog = showDeleteDialog,
        showShareDialog = showShareDialog,
        showScoreDialog = showScoreDialog,
        attempt = attempt,
        onRenamePlayer = onRenamePlayer,
        onDeleteAttempt = onDeleteAttempt,
        onShareAttempt = onShareAttempt,
        onDismissRename = { onShowRenameDialog(false) },
        onDismissDelete = { onShowDeleteDialog(false) },
        onDismissShare = { onShowShareDialog(false) },
        onDismissScore = { onShowScoreDialog(false) }
    )
}

/**
 * 🎨 SCRAPBOOK RECORDING ITEM - For main recording cards
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
    val tapeRotation1 = remember(stableId) { (stableId % 25 - 12).toFloat() }
    val tapeRotation2 = remember(stableId) { ((stableId * 13) % 25 - 12).toFloat() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 32.dp, end = 16.dp, top = 16.dp, bottom = 16.dp)
    ) {
        // Main sticky note card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .rotate(rotation)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(12.dp),
                    spotColor = Color.Black.copy(alpha = 0.4f),
                    ambientColor = Color.Black.copy(alpha = 0.15f)
                ),
            colors = CardDefaults.cardColors(
                containerColor = stickyNoteColor
            ),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header with recording name AND delete button (like GenZ UI)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "🎤 ${recording.name}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = dancingScriptFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = Color.Black.copy(alpha = 0.9f),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showRenameDialog = true }
                            .padding(end = 12.dp), // Add padding so text doesn't touch delete button
                        overflow = TextOverflow.Ellipsis, // Add ellipsis for long names
                        maxLines = 1 // Ensure single line with ellipsis
                    )

                    // Delete button moved to header (GenZ UI style)
                    ScrapbookButton(
                        onClick = { showDeleteDialog = true },
                        icon = Icons.Default.Delete,
                        label = "Del",
                        iconColor = Color(0xFFFF1744)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Control buttons - IMPROVED SPACING like GenZ UI (no delete button here anymore)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp), // Increased from 12.dp to 16.dp for better spacing
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Share button (FIRST)
                    ScrapbookButton(
                        onClick = { showShareDialog = true },
                        icon = Icons.Default.Share,
                        label = "Share",
                        iconColor = Color(0xFF607D8B)
                    )

                    // Play controls (SECOND)
                    if (isPlayingThis && !isPaused) {
                        ScrapbookButton(
                            onClick = onPause,
                            icon = Icons.Default.Pause,
                            label = "Pause",
                            iconColor = Color(0xFF4CAF50)
                        )
                    } else {
                        ScrapbookButton(
                            onClick = { onPlay(recording.originalPath) },
                            icon = Icons.Default.PlayArrow,
                            label = "Play",
                            iconColor = Color(0xFF2196F3)
                        )
                    }

                    // Rev button (THIRD)
                    if (recording.reversedPath != null) {
                        ScrapbookButton(
                            onClick = { onPlay(recording.reversedPath!!) },
                            icon = Icons.Default.FastForward,
                            label = "Rev",
                            iconColor = Color(0xFFFF9800)
                        )
                    }

                    // Game mode buttons (FOURTH & FIFTH)
                    if (isGameModeEnabled) {
                        ScrapbookButton(
                            onClick = { onStartAttempt(recording, ChallengeType.FORWARD) },
                            icon = Icons.Default.Mic,
                            label = "Try",
                            iconColor = Color(0xFF9C27B0)
                        )

                        ScrapbookButton(
                            onClick = { onStartAttempt(recording, ChallengeType.REVERSE) },
                            icon = Icons.Default.Mic,
                            label = "REV",
                            iconColor = Color(0xFFE91E63)
                        )
                    }

                    // DELETE BUTTON REMOVED FROM HERE - Now in header row!
                }

                // Progress indicator
                if (isPlayingThis) {
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF4CAF50)
                    )
                }
            }
        }

        // Tape corners for recordings
        TapeCorner(
            modifier = Modifier.align(Alignment.TopStart),
            offsetX = (-16).dp,
            offsetY = 12.dp,
            rotation = tapeRotation1
        )
        TapeCorner(
            modifier = Modifier.align(Alignment.BottomEnd),
            offsetX = 16.dp,
            offsetY = (-12).dp,
            rotation = tapeRotation2
        )
    }

    // Dialogs for recording
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
 * ⚡ ENHANCED GLOW BUTTON - Used throughout the app
 */
@Composable
fun EnhancedGlowButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isPrimary: Boolean = false,
    isDestructive: Boolean = false,
    isSecondary: Boolean = false, // New flag for Share/Rev buttons
    size: androidx.compose.ui.unit.Dp = 40.dp,
    label: String? = null,
    content: @Composable () -> Unit
) {
    val aesthetic = aestheticTheme()
    val colors = materialColors()

    val backgroundColor = when {
        isDestructive -> colors.error
        isPrimary || isSecondary -> colors.primary  // Both primary and secondary use primary color
        else -> colors.surfaceVariant
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier
                .size(size)
                .then(
                    if (aesthetic.glowIntensity > 0) {
                        Modifier.shadow(
                            elevation = (aesthetic.glowIntensity * 12).dp,
                            shape = CircleShape,
                            spotColor = if (isPrimary || isSecondary) colors.primary else colors.surfaceVariant
                        )
                    } else Modifier
                ),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = backgroundColor,
                disabledContainerColor = backgroundColor.copy(alpha = 0.5f)
            ),
            contentPadding = PaddingValues(0.dp)
        ) {
            content()
        }

        // Text label below button
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

// Helper components...
@Composable
private fun TapeCorner(
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
    )
}

@Composable
private fun ScrapbookButton(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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

@Composable
private fun RadialScoreDisplay(
    score: Int,
    isAnimated: Boolean = false,
    onClick: () -> Unit
) {
    val colors = materialColors()
    val aesthetic = aestheticTheme()

    // Score-based emoji - use egg emojis when egg theme is active
    val emoji = if (aesthetic.useEggElements) {
        // Use egg emojis from aesthetic.scoreEmojis
        aesthetic.scoreEmojis.entries
            .sortedByDescending { it.key }
            .firstOrNull { score >= it.key }?.value ?: "🥚"
    } else {
        // Standard emojis
        when {
            score >= 90 -> "🤩"
            score >= 80 -> "😊"
            score >= 70 -> "🙂"
            score >= 60 -> "😐"
            score >= 50 -> "😕"
            else -> "😞"
        }
    }

    Box(
        modifier = Modifier
            .size(80.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // Background circle
        Canvas(
            modifier = Modifier.size(80.dp)
        ) {
            val strokeWidth = 6.dp.toPx()
            val center = Offset(size.width / 2, size.height / 2)
            val radius = (size.width - strokeWidth) / 2

            // Background ring
            drawCircle(
                color = Color.Gray.copy(alpha = 0.3f),
                radius = radius,
                center = center,
                style = Stroke(strokeWidth)
            )

            // Progress ring
            val sweepAngle = (score / 100f) * 360f
            drawArc(
                color = when {
                    score >= 80 -> Color(0xFF4CAF50) // Green
                    score >= 60 -> Color(0xFFFF9800) // Orange
                    else -> Color(0xFFFF5722) // Red
                },
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(strokeWidth, cap = StrokeCap.Round),
                topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                size = Size(size.width - strokeWidth, size.height - strokeWidth)
            )
        }

        // Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp)
            )
            Text(
                text = "$score",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                ),
                color = colors.onSurface
            )
        }
    }
}

// Dialog composables
@Composable
private fun ScrapbookDialogs(
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
                }) { Text("Rename") }
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
                    if (attempt.reversedAttemptFilePath != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                onShareAttempt(attempt.reversedAttemptFilePath!!)
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

@Composable
private fun ModernDialogs(
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
            title = { Text("Rename Player") },
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
                }) { Text("Rename") }
            },
            dismissButton = {
                Button(onClick = onDismissRename) { Text("Cancel") }
            }
        )
    }

    if (showDeleteDialog && onDeleteAttempt != null) {
        AlertDialog(
            onDismissRequest = onDismissDelete,
            title = { Text("Delete Attempt?") },
            text = { Text("Are you sure you want to delete ${attempt.playerName}'s attempt? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteAttempt(attempt)
                        onDismissDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = materialColors().error)
                ) { Text("Delete") }
            },
            dismissButton = {
                Button(onClick = onDismissDelete) { Text("Cancel") }
            }
        )
    }

    if (showShareDialog && onShareAttempt != null) {
        AlertDialog(
            onDismissRequest = onDismissShare,
            title = { Text("Share Attempt") },
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
                        Text("Share Original")
                    }
                    if (attempt.reversedAttemptFilePath != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                onShareAttempt(attempt.reversedAttemptFilePath!!)
                                onDismissShare()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Share Reversed")
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

/**
 * Egg-themed record button for when theme.useEggElements = true
 */
@Composable
fun EggThemedRecordButton(
    isRecording: Boolean,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    modifier: Modifier = Modifier
) {
    val aesthetic = aestheticTheme()

    Box(
        modifier = modifier
            .size(80.dp)
            .clickable {
                if (isRecording) onStopRecording() else onStartRecording()
            },
        contentAlignment = Alignment.Center
    ) {
        // Background circle with egg styling
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = size.minDimension / 2

            if (isRecording) {
                // Pulsing effect when recording
                drawCircle(
                    color = Color(0xFFFFD700).copy(alpha = 0.3f),
                    radius = radius * 1.2f
                )
            }

            // Main button circle with egg theme colors
            drawCircle(
                color = Color(0xFFFFF8E1),
                radius = radius * 0.9f,
                style = Stroke(
                    width = if (aesthetic.useHandDrawnBorders) 4.dp.toPx() else 2.dp.toPx()
                )
            )
        }

        // Egg icon in center
        when (aesthetic.eggButtonStyle) {
            EggButtonStyle.FriedEgg -> {
                EggIcons.FriedEggIcon(
                    size = 40.dp,
                    tint = if (isRecording) Color(0xFFFF6B6B) else Color(0xFF2E2E2E)
                )
            }
            EggButtonStyle.CrackedEgg -> {
                EggIcons.CrackedEggIcon(
                    size = 40.dp,
                    tint = if (isRecording) Color(0xFFFF6B6B) else Color(0xFF2E2E2E)
                )
            }
            else -> {
                EggIcons.EggMicIcon(
                    size = 40.dp,
                    tint = if (isRecording) Color(0xFFFF6B6B) else Color(0xFF2E2E2E)
                )
            }
        }
    }
}

/**
 * Egg-themed play button for attempt items
 */
@Composable
fun EggThemedPlayButton(
    isPlaying: Boolean,
    isPaused: Boolean,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(56.dp)
            .background(
                color = Color(0xFFFFF8E1),
                shape = CircleShape
            )
            .border(
                width = 2.dp,
                color = Color(0xFF2E2E2E),
                shape = CircleShape
            )
            .clickable {
                if (isPlaying && !isPaused) onPause() else onPlay()
            },
        contentAlignment = Alignment.Center
    ) {
        // Use egg icon instead of regular play/pause
        if (isPlaying && !isPaused) {
            EggIcons.CrackedEggIcon(
                size = 24.dp,
                tint = Color(0xFF2E2E2E)
            )
        } else {
            EggIcons.WholeEggIcon(
                size = 24.dp,
                tint = Color(0xFF2E2E2E)
            )
        }
    }
}

/**
 * Egg-themed score display
 */
@Composable
fun EggThemedScoreChip(
    score: Int,
    isAnimated: Boolean = false,
    onClick: () -> Unit
) {
    val aesthetic = aestheticTheme()

    // Get the appropriate egg emoji for the score
    val emoji = aesthetic.scoreEmojis.entries
        .sortedByDescending { it.key }
        .firstOrNull { score >= it.key }?.value ?: "🥚"

    Box(
        modifier = Modifier
            .background(
                color = Color(0xFFFFF8E1),
                shape = RoundedCornerShape(20.dp)
            )
            .border(
                width = 2.dp,
                color = Color(0xFF2E2E2E),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "$score%",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF2E2E2E),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Egg-themed scrapbook button
 */
@Composable
private fun EggScrapbookButton(
    onClick: () -> Unit,
    eggType: String, // "whole", "fried", "cracked"
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
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = Color(0xFFFFF8E1), // Egg shell color
                    shape = RoundedCornerShape(8.dp)
                )
                .border(
                    width = 2.dp,
                    color = Color(0xFF8D6E63).copy(alpha = 0.8f), // Brown border
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            when (eggType) {
                "whole" -> EggIcons.WholeEggIcon(
                    size = 24.dp,
                    tint = iconColor
                )
                "fried" -> EggIcons.FriedEggIcon(
                    size = 24.dp,
                    tint = iconColor
                )
                "cracked" -> EggIcons.CrackedEggIcon(
                    size = 24.dp,
                    tint = iconColor
                )
                else -> EggIcons.WholeEggIcon(
                    size = 24.dp,
                    tint = iconColor
                )
            }
        }

        Spacer(modifier = Modifier.height(3.dp))

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
 * GLUTE-compliant recording button for your daughter's egg theme! 🥚
 */
@Composable
fun UnifiedRecordingButton(
    isRecording: Boolean,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    modifier: Modifier = Modifier
) {
    UnifiedRecordButton(
        isRecording = isRecording,
        onClick = {
            if (isRecording) {
                onStopRecording()
            } else {
                onStartRecording()
            }
        },
        modifier = modifier
    )
}