package com.example.reversey.ui.components

//import com.example.reversey.ui.components.unified.UnifiedRecordButton

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.StarBorder
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.reversey.R
import com.example.reversey.data.models.ChallengeType
import com.example.reversey.data.models.PlayerAttempt
import com.example.reversey.scoring.DifficultyLevel
import com.example.reversey.data.models.Recording
import com.example.reversey.scoring.DifficultyConfig
import com.example.reversey.scoring.ScoringResult
import com.example.reversey.scoring.SimilarityMetrics
import com.example.reversey.ui.icons.EggIcons
import com.example.reversey.ui.theme.AestheticTheme
import com.example.reversey.ui.theme.AestheticThemeData
import com.example.reversey.ui.theme.MaterialColors

// Dancing Script handwriting font family for scrapbook theme
private val dancingScriptFontFamily = FontFamily(
    Font(R.font.dancing_script_regular, FontWeight.Companion.Normal),
    Font(R.font.dancing_script_bold, FontWeight.Companion.Bold)
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
    val aesthetic = AestheticTheme()
    val colors = MaterialColors()

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
        // SCRAPBOOK THEMES
        aesthetic.id == "scrapbook" -> {
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
        // 🎸 GUITAR THEME - ADD THIS WHOLE BLOCK!
        aesthetic.id == "guitar" -> {
            aesthetic.components.AttemptItem(
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
 

        // 🥚 EGG THEME
        aesthetic.id == "egg" -> {
            aesthetic.components.AttemptItem(
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

        aesthetic.id == "snowy_owl" -> {
            aesthetic.components.AttemptItem(
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
    val colors = MaterialColors()

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
        modifier = Modifier.Companion
            .fillMaxWidth()
            .padding(start = 48.dp, end = 16.dp, top = 12.dp, bottom = 12.dp)
    ) {
        // Main sticky note card
        Card(
            modifier = Modifier.Companion
                .fillMaxWidth()
                .rotate(rotation)
                .shadow(
                    elevation = 6.dp,
                    shape = RoundedCornerShape(8.dp),
                    spotColor = Color.Companion.Black.copy(alpha = 0.3f),
                    ambientColor = Color.Companion.Black.copy(alpha = 0.1f)
                ),
            colors = CardDefaults.cardColors(
                containerColor = stickyNoteColor
            ),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.Companion
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // Header with player name and star rating
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Player name - LEFT ALIGNED
                    Text(
                        text = attempt.playerName,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = dancingScriptFontFamily,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.Black.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onShowRenameDialog(true) }
                    )

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

                Spacer(modifier = Modifier.height(6.dp))

                // NEW ROW 2: Challenge type + Difficulty indicator (aligned under stars on right)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Challenge type icon
                    val challengeIcon = if (attempt.challengeType == ChallengeType.REVERSE) "🔄" else "▶️"
                    Text(
                        text = challengeIcon,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(end = 8.dp)
                    )

                    // Difficulty indicator
                    Text(
                        text = attempt.difficulty.displayName.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = dancingScriptFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        ),
                        color = DifficultyConfig.getColorForDifficulty(attempt.difficulty),

                                modifier = Modifier
                            .background(
                                color = Color.Black.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Control buttons - Original first, then Share!

                Spacer(modifier = Modifier.Companion.height(8.dp))

                // Control buttons - Original first, then Share!
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.Companion.fillMaxWidth()
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
                        if (aesthetic.id == "egg") {
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
                        if (aesthetic.id == "egg") {
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
                        if (aesthetic.id == "egg") {
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
                    if (aesthetic.id == "egg") {
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
                    Spacer(modifier = Modifier.Companion.height(8.dp))
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier.Companion.fillMaxWidth(),
                        color = Color(0xFF4CAF50)
                    )
                }
            }
        }

        // Tape corners
        TapeCorner(
            modifier = Modifier.Companion.align(Alignment.Companion.TopStart),
            offsetX = (-12).dp,
            offsetY = 8.dp,
            rotation = tapeRotation1
        )
        TapeCorner(
            modifier = Modifier.Companion.align(Alignment.Companion.BottomEnd),
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
    val aesthetic = AestheticTheme()
    val colors = MaterialColors()

    // Glassmorphism card with glow effect
    Card(
        modifier = Modifier.Companion
            .fillMaxWidth()
            .padding(start = 25.dp, end = 0.dp, top = 4.dp, bottom = 4.dp)
            .then(
                if (aesthetic.useGlassmorphism && aesthetic.glowIntensity > 0) {
                    Modifier.Companion.shadow(
                        elevation = (aesthetic.glowIntensity * 20).dp,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                        spotColor = colors.primary
                    )
                } else Modifier.Companion
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (aesthetic.useGlassmorphism) {
                colors.surface.copy(alpha = aesthetic.cardAlpha)
            } else colors.surface
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier.Companion
                .fillMaxWidth()
                .then(
                    if (aesthetic.useGlassmorphism) {
                        Modifier.Companion
                            .background(
                                brush = Brush.Companion.linearGradient(
                                    colors = listOf(
                                        Color.Companion.White.copy(alpha = 0.1f),
                                        Color.Companion.Transparent
                                    )
                                ),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = Color.Companion.White.copy(alpha = 0.2f),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                            )
                    } else Modifier.Companion.border(
                        1.dp,
                        aesthetic.cardBorder,
                        androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                    )
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.Companion.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.Companion.fillMaxWidth(),
                    verticalAlignment = Alignment.Companion.Top
                ) {
                    // Left side: Player info + buttons
                    Column(
                        modifier = Modifier.Companion.weight(1f)
                    ) {
                        Spacer(modifier = Modifier.height(10.dp))//Move player name tag down 10px
                        Row(
                            verticalAlignment = Alignment.Companion.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(9.dp), // Adds 9.dp of space ONLY between items
                            modifier = Modifier.Companion.clickable { onShowRenameDialog(true) }
                        ) {
                            // Go to Parent button
                            if (onJumpToParent != null) {
                                IconButton(
                                    onClick = onJumpToParent,
                                    modifier = Modifier.Companion.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Home,
                                        contentDescription = "Go to Parent",
                                        tint = colors.primary,
                                        modifier = Modifier.Companion.size(36.dp)
                                    )
                                }
                            }

                            // Player name
                            Text(
                                text = attempt.playerName,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Companion.Bold,
                                    letterSpacing = if (aesthetic.useWideLetterSpacing) 1.2.sp else 0.sp
                                ),
                                color = colors.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }

                        Spacer(modifier = Modifier.Companion.height(12.dp))

                        // Control buttons
                        Spacer(modifier = Modifier.height(10.dp))//Move control buttons tag down 10px
                        Row(
                            modifier = Modifier.Companion.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // Share button
                            EnhancedGlowButton(
                                onClick = { onShowShareDialog(true) },
                                isSecondary = true,
                                label = "Share"
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share",
                                    tint = Color.Companion.White,
                                    modifier = Modifier.Companion.size(16.dp)
                                )
                            }

                            // Play/Pause button
                            if (isPlayingThis && !isPaused) {
                                EnhancedGlowButton(
                                    onClick = onPause,
                                    isPrimary = true,
                                    label = "Pause"
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Pause,
                                        contentDescription = "Pause",
                                        tint = Color.Companion.White,
                                        modifier = Modifier.Companion.size(16.dp)
                                    )
                                }
                            } else {
                                EnhancedGlowButton(
                                    onClick = { onPlay(attempt.attemptFilePath) },
                                    isPrimary = true,
                                    label = "Play"
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Play",
                                        tint = Color.Companion.White,
                                        modifier = Modifier.Companion.size(16.dp)
                                    )
                                }
                            }

                            // Rev button
                            if (attempt.reversedAttemptFilePath != null) {
                                EnhancedGlowButton(
                                    onClick = { onPlay(attempt.reversedAttemptFilePath!!) },
                                    isSecondary = true,
                                    label = "Rev"
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Repeat,
                                        contentDescription = "Reverse",
                                        tint = Color.Companion.White,
                                        modifier = Modifier.Companion.size(16.dp)
                                    )
                                }
                            }

                            // Delete button
                            EnhancedGlowButton(
                                onClick = { onShowDeleteDialog(true) },
                                isDestructive = true,
                                label = "Del"
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = Color.Companion.White,
                                    modifier = Modifier.Companion.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.Companion.width(16.dp))

                    // Right side: Squircle
                    RadialScoreDisplay(
                        score = attempt.score,
                        difficulty = attempt.difficulty,
                        isAnimated = true,
                        challengeType = attempt.challengeType,
                        onClick = { onShowScoreDialog(true) }
                    )
                }

                // Progress bar at bottom - spans from left edge to squircle
                if (isPlayingThis) {
                    Spacer(modifier = Modifier.Companion.height(8.dp))
                    Row(
                        modifier = Modifier.Companion.fillMaxWidth()
                    ) {
                        LinearProgressIndicator(
                            progress = progress,
                            modifier = Modifier.Companion.weight(1f),
                            color = colors.primary
                        )
                        Spacer(modifier = Modifier.Companion.width(16.dp + 100.dp)) // Space for squircle
                    }
                }
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
        modifier = Modifier.Companion
            .fillMaxWidth()
            .padding(start = 32.dp, end = 16.dp, top = 16.dp, bottom = 16.dp)
    ) {
        // Main sticky note card
        Card(
            modifier = Modifier.Companion
                .fillMaxWidth()
                .rotate(rotation)
                .shadow(
                    elevation = 8.dp,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    spotColor = Color.Companion.Black.copy(alpha = 0.4f),
                    ambientColor = Color.Companion.Black.copy(alpha = 0.15f)
                ),
            colors = CardDefaults.cardColors(
                containerColor = stickyNoteColor
            ),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier.Companion
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header with recording name AND delete button (like GenZ UI)
                Row(
                    modifier = Modifier.Companion.fillMaxWidth(),
                    verticalAlignment = Alignment.Companion.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "🎤 ${recording.name}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = dancingScriptFontFamily,
                            fontWeight = FontWeight.Companion.Bold,
                            fontSize = 16.sp
                        ),
                        color = Color.Companion.Black.copy(alpha = 0.9f),
                        modifier = Modifier.Companion
                            .weight(1f)
                            .clickable { showRenameDialog = true }
                            .padding(end = 12.dp), // Add padding so text doesn't touch delete button
                        overflow = TextOverflow.Companion.Ellipsis, // Add ellipsis for long names
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

                Spacer(modifier = Modifier.Companion.height(12.dp))

                // Control buttons - IMPROVED SPACING like GenZ UI (no delete button here anymore)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp), // Increased from 12.dp to 16.dp for better spacing
                    modifier = Modifier.Companion.fillMaxWidth()
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
                    Spacer(modifier = Modifier.Companion.height(12.dp))
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier.Companion.fillMaxWidth(),
                        color = Color(0xFF4CAF50)
                    )
                }
            }
        }

        // Tape corners for recordings
        TapeCorner(
            modifier = Modifier.Companion.align(Alignment.Companion.TopStart),
            offsetX = (-16).dp,
            offsetY = 12.dp,
            rotation = tapeRotation1
        )
        TapeCorner(
            modifier = Modifier.Companion.align(Alignment.Companion.BottomEnd),
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
    modifier: Modifier = Modifier.Companion,
    enabled: Boolean = true,
    isPrimary: Boolean = false,
    isDestructive: Boolean = false,
    isSecondary: Boolean = false, // New flag for Share/Rev buttons
    size: Dp = 40.dp,
    label: String? = null,
    content: @Composable () -> Unit
) {
    val aesthetic = AestheticTheme()
    val colors = MaterialColors()

    val backgroundColor = when {
        isDestructive -> colors.error
        isPrimary || isSecondary -> colors.primary  // Both primary and secondary use primary color
        else -> colors.surfaceVariant
    }

    Column(
        horizontalAlignment = Alignment.Companion.CenterHorizontally
    ) {
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier
                .size(size)
                .then(
                    if (aesthetic.glowIntensity > 0) {
                        Modifier.Companion.shadow(
                            elevation = (aesthetic.glowIntensity * 12).dp,
                            shape = CircleShape,
                            spotColor = if (isPrimary || isSecondary) colors.primary else colors.surfaceVariant
                        )
                    } else Modifier.Companion
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
                textAlign = TextAlign.Companion.Center,
                modifier = Modifier.Companion.padding(top = 2.dp)
            )
        }
    }
}

// Helper components...
@Composable
private fun TapeCorner(
    modifier: Modifier = Modifier.Companion,
    offsetX: Dp = 0.dp,
    offsetY: Dp = 0.dp,
    rotation: Float = 0f
) {
    Box(
        modifier = modifier
            .offset(x = offsetX, y = offsetY)
            .rotate(rotation)
            .size(width = 24.dp, height = 16.dp)
            .background(
                color = Color.Companion.Gray.copy(alpha = 0.5f),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(2.dp)
            )
    )
}

@Composable
private fun ScrapbookButton(
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    iconColor: Color,
    enabled: Boolean = true,
    modifier: Modifier = Modifier.Companion
) {
    Column(
        horizontalAlignment = Alignment.Companion.CenterHorizontally,
        modifier = modifier
            .clickable(enabled = enabled) { onClick() }
            .padding(2.dp)
    ) {
        Box(
            modifier = Modifier.Companion
                .size(40.dp)
                .background(
                    color = Color.Companion.White,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                )
                .border(
                    width = 2.dp,
                    color = Color.Companion.Gray.copy(alpha = 0.6f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                ),
            contentAlignment = Alignment.Companion.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.Companion.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.Companion.height(3.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = dancingScriptFontFamily,
                fontSize = 10.sp,
                fontWeight = FontWeight.Companion.Medium
            ),
            color = Color.Companion.Black.copy(alpha = 0.8f),
            textAlign = TextAlign.Companion.Center,
            maxLines = 1
        )
    }
}

@Composable
private fun RadialScoreDisplay(
    score: Int,
    difficulty: DifficultyLevel,
    challengeType: ChallengeType,
    isAnimated: Boolean = false,
    onClick: () -> Unit
) {
    val aesthetic = AestheticTheme()

    // Score-based emoji - use egg emojis when egg theme is active
    val emoji = if (aesthetic.id == "egg") {
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

    // Use the new DifficultySquircle component
    DifficultySquircle(
        score = score,
        difficulty = difficulty,
        challengeType = challengeType,
        emoji = emoji,
        width = 100.dp,
        height = 130.dp,
        onClick = onClick
    )
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
                    Spacer(modifier = Modifier.Companion.height(16.dp))
                    Button(
                        onClick = {
                            onShareAttempt(attempt.attemptFilePath)
                            onDismissShare()
                        },
                        modifier = Modifier.Companion.fillMaxWidth()
                    ) {
                        Text("Share Original 🎤")
                    }
                    if (attempt.reversedAttemptFilePath != null) {
                        Spacer(modifier = Modifier.Companion.height(8.dp))
                        Button(
                            onClick = {
                                onShareAttempt(attempt.reversedAttemptFilePath!!)
                                onDismissShare()
                            },
                            modifier = Modifier.Companion.fillMaxWidth()
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
            ScoringResult(
                score = attempt.score,
                rawScore = attempt.rawScore,
                metrics = SimilarityMetrics(
                    pitch = attempt.pitchSimilarity,
                    mfcc = attempt.mfccSimilarity
                ),
                feedback = emptyList()
            )
        }

        ScoreExplanationDialog(
            attempt = attempt,
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
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialColors().error)
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
                    Spacer(modifier = Modifier.Companion.height(16.dp))
                    Button(
                        onClick = {
                            onShareAttempt(attempt.attemptFilePath)
                            onDismissShare()
                        },
                        modifier = Modifier.Companion.fillMaxWidth()
                    ) {
                        Text("Share Original")
                    }
                    if (attempt.reversedAttemptFilePath != null) {
                        Spacer(modifier = Modifier.Companion.height(8.dp))
                        Button(
                            onClick = {
                                onShareAttempt(attempt.reversedAttemptFilePath!!)
                                onDismissShare()
                            },
                            modifier = Modifier.Companion.fillMaxWidth()
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
            ScoringResult(
                score = attempt.score,
                rawScore = attempt.rawScore,
                metrics = SimilarityMetrics(
                    pitch = attempt.pitchSimilarity,
                    mfcc = attempt.mfccSimilarity
                ),
                feedback = emptyList()
            )
        }

        ScoreExplanationDialog(
            attempt = attempt,
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
                        onRename(recording.originalPath, newName)
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
                    Spacer(modifier = Modifier.Companion.height(16.dp))
                    Button(
                        onClick = {
                            onShare(recording.originalPath)
                            onDismissShare()
                        },
                        modifier = Modifier.Companion.fillMaxWidth()
                    ) {
                        Text("Share Original 🎤")
                    }
                    recording.reversedPath?.let { reversedPath ->
                        Spacer(modifier = Modifier.Companion.height(8.dp))
                        Button(
                            onClick = {
                                onShare(reversedPath)
                                onDismissShare()
                            },
                            modifier = Modifier.Companion.fillMaxWidth()
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


@Composable
private fun EggScrapbookButton(
    onClick: () -> Unit,
    eggType: String, // "whole", "fried", "cracked"
    label: String,
    iconColor: Color,
    enabled: Boolean = true,
    modifier: Modifier = Modifier.Companion
) {
    Column(
        horizontalAlignment = Alignment.Companion.CenterHorizontally,
        modifier = modifier
            .clickable(enabled = enabled) { onClick() }
            .padding(2.dp)
    ) {
        Box(
            modifier = Modifier.Companion
                .size(40.dp)
                .background(
                    color = Color(0xFFFFF8E1), // Egg shell color
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                )
                .border(
                    width = 2.dp,
                    color = Color(0xFF8D6E63).copy(alpha = 0.8f), // Brown border
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Companion.Center
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

        Spacer(modifier = Modifier.Companion.height(3.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = dancingScriptFontFamily,
                fontSize = 10.sp,
                fontWeight = FontWeight.Companion.Medium
            ),
            color = Color.Companion.Black.copy(alpha = 0.8f),
            textAlign = TextAlign.Companion.Center,
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
    modifier: Modifier = Modifier.Companion
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

/**
 * GLUTE-compliant recording button router
 * NOTE: Egg theme bypasses this - has its own inline button
 */
@Composable
fun UnifiedRecordButton(
    isRecording: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val aesthetic = AestheticTheme()

    // Route to appropriate button style based on theme ID
    when (aesthetic.id) {
        "scrapbook" -> {
            ScrapbookRecordButton(
                isRecording = isRecording,
                onClick = onClick,
                modifier = modifier
            )
        }
        else -> {
            ModernRecordButton(
                isRecording = isRecording,
                onClick = onClick,
                modifier = modifier
            )
        }
    }
}

/**
 * Scrapbook-style recording button for scrapbook theme
 */
@Composable
fun ScrapbookRecordButton(
    isRecording: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val aesthetic = AestheticTheme()

    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.size(72.dp),
        containerColor = if (isRecording)
            Color(0xFFFF6B6B) else Color(0xFFFF8C00),
        contentColor = Color.White
    ) {
        if (isRecording) {
            Icon(
                imageVector = Icons.Default.Stop,
                contentDescription = "Stop Recording",
                modifier = Modifier.size(32.dp)
            )
        } else {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "Start Recording",
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

/**
 * Modern Material Design recording button fallback
 */
@Composable
fun ModernRecordButton(
    isRecording: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = if (isRecording)
            MaterialTheme.colorScheme.error
        else
            MaterialTheme.colorScheme.primary
    ) {
        if (isRecording) {
            Icon(
                imageVector = Icons.Default.Stop,
                contentDescription = "Stop Recording"
            )
        } else {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "Start Recording"
            )
        }
    }
}