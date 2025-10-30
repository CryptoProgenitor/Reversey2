package com.example.reversey

import com.example.reversey.ui.components.ScoreExplanationDialog
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin
import com.example.reversey.ui.theme.aestheticTheme
import com.example.reversey.ui.theme.materialColors

// Helper function to create ScoringResult from PlayerAttempt - USES REAL METRICS
private fun createScoringResultFromAttempt(attempt: PlayerAttempt): com.example.reversey.scoring.ScoringResult {
    return com.example.reversey.scoring.ScoringResult(
        score = attempt.score,
        rawScore = attempt.rawScore,
        metrics = com.example.reversey.scoring.SimilarityMetrics(
            pitch = attempt.pitchSimilarity,
            mfcc = attempt.mfccSimilarity
        ),
        feedback = emptyList() // Dialog will generate its own feedback
    )
}

/**
 * Gen Z Enhanced Attempt Item with Dopamine UI Strategy
 * Features: Radial progress circles, emoji medals, glassmorphism, glow effects
 */
@Composable
fun EnhancedAttemptItem(
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
    var showScoreDialog by remember { mutableStateOf(false) }
    val isPlayingThis = currentlyPlayingPath == attempt.attemptFilePath ||
            currentlyPlayingPath == attempt.reversedAttemptFilePath

    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }

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
                // Left side: Player info with challenge icon
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { showRenameDialog = true }
                    ) {
                        // Go to Parent button (only show if onJumpToParent is provided)
                        if (onJumpToParent != null) {
                            IconButton(
                                onClick = onJumpToParent,
                                modifier = Modifier.size(32.dp)  // Compact size
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Home,
                                    contentDescription = "Go to Parent",
                                    tint = colors.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Challenge type icon with glow
                        val challengeIcon = if (attempt.challengeType == ChallengeType.REVERSE) "🔄" else "▶️"
                        Text(
                            text = challengeIcon,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .then(
                                    if (aesthetic.glowIntensity > 0) {
                                        Modifier.shadow(
                                            elevation = (aesthetic.glowIntensity * 10).dp,
                                            shape = CircleShape,
                                            spotColor = colors.primary
                                        )
                                    } else Modifier
                                )
                        )

                        // Player name with theme typography
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

                    // Control buttons row
                    ControlButtonsRow(
                        isPlayingThis = isPlayingThis,
                        isPaused = isPaused,
                        attempt = attempt,
                        onPlay = onPlay,
                        onPause = onPause,
                        onStop = onStop,
                        onShare = { showShareDialog = true },
                        onDelete = { showDeleteDialog = true }
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Right side: Radial progress score with emoji medal
                RadialScoreDisplay(
                    score = attempt.score,
                    isAnimated = true,
                    onClick = { showScoreDialog = true }
                )
            }

            // Progress indicator
            if (isPlayingThis) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = colors.primary,
                    trackColor = colors.surfaceVariant.copy(alpha = 0.3f)
                )
            }
        }
    }

    // Score explanation dialog
    if (showScoreDialog) {
        ScoreExplanationDialog(
            score = createScoringResultFromAttempt(attempt),
            challengeType = attempt.challengeType,
            onDismiss = { showScoreDialog = false }
        )
    }

    // Other dialogs
    EnhancedDialogs(
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
 * Radial Score Display with Glass Effect
 */
@Composable
fun RadialScoreDisplay(
    score: Int,
    isAnimated: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    val aesthetic = aestheticTheme()
    val colors = materialColors()

    val animatedProgress by animateFloatAsState(
        targetValue = if (isAnimated) score / 100f else score / 100f,
        animationSpec = tween(durationMillis = 1500, easing = LinearOutSlowInEasing),
        label = "score_progress"
    )

    val scoreEmoji = when {
        score >= 90 -> aesthetic.recordButtonEmoji
        score >= 80 -> "😊"
        score >= 70 -> "👍"
        score >= 60 -> "😐"
        else -> "😔"
    }

    Box(
        modifier = Modifier
            .size(80.dp)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        contentAlignment = Alignment.Center
    ) {
        // Radial progress background
        Canvas(modifier = Modifier.size(80.dp)) {
            val center = this.center
            val radius = 30.dp.toPx()
            val strokeWidth = 6.dp.toPx()

            // Background circle
            drawCircle(
                color = colors.surfaceVariant,
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth)
            )

            // Progress arc
            val sweepAngle = animatedProgress * 360f
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        colors.primary,
                        colors.secondary,
                        colors.tertiary
                    )
                ),
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        // Score content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = scoreEmoji,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "$score",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = colors.onSurface
            )
        }
    }
}

/**
 * Enhanced Control Buttons Row
 */
@Composable
private fun ControlButtonsRow(
    isPlayingThis: Boolean,
    isPaused: Boolean,
    attempt: PlayerAttempt,
    onPlay: (String) -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Share button
        EnhancedGlowButton(
            onClick = onShare,
            size = 32.dp,
            label = "Share"
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Share",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }

        // Play controls
        if (isPlayingThis) {
            // Pause/Resume button
            EnhancedGlowButton(
                onClick = onPause,
                isPrimary = true,
                size = 32.dp,
                label = if (isPaused) "Resume" else "Pause"
            ) {
                val pauseIcon = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause
                Icon(
                    imageVector = pauseIcon,
                    contentDescription = if (isPaused) "Resume" else "Pause",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Stop button
            EnhancedGlowButton(
                onClick = onStop,
                isDestructive = true,
                size = 32.dp,
                label = "Stop"
            ) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = "Stop",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        } else {
            // Play Original button
            EnhancedGlowButton(
                onClick = { onPlay(attempt.attemptFilePath) },
                isPrimary = true,
                size = 32.dp,
                label = "Play"
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play Original",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Play Reversed button
            EnhancedGlowButton(
                onClick = {
                    attempt.reversedAttemptFilePath?.let { onPlay(it) }
                },
                enabled = attempt.reversedAttemptFilePath != null,
                isPrimary = true,
                size = 32.dp,
                label = "Rev"
            ) {
                Icon(
                    imageVector = Icons.Default.Replay,
                    contentDescription = "Play Reversed",
                    tint = Color.White
                )
            }
        }

        // Delete button
        EnhancedGlowButton(
            onClick = onDelete,
            isDestructive = true,
            size = 32.dp,
            label = "Del"
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * Enhanced Glow Button - Material 3 Compatible
 */
@Composable
fun EnhancedGlowButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isPrimary: Boolean = false,
    isDestructive: Boolean = false,
    size: androidx.compose.ui.unit.Dp = 40.dp,
    label: String? = null,
    content: @Composable () -> Unit
) {
    val aesthetic = aestheticTheme()
    val colors = materialColors()

    val backgroundColor = when {
        isDestructive -> colors.error
        isPrimary -> colors.primary
        else -> colors.surface
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
                            spotColor = if (isPrimary) colors.primary else colors.surfaceVariant
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

/**
 * Enhanced dialogs with theme styling
 */
@Composable
private fun EnhancedDialogs(
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
    val aesthetic = aestheticTheme()
    val colors = materialColors()

    // Rename Dialog
    if (showRenameDialog) {
        var newPlayerName by remember { mutableStateOf(attempt.playerName) }
        AlertDialog(
            onDismissRequest = onDismissRename,
            title = {
                Text(
                    "Rename Player",
                    color = colors.onSurface,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        letterSpacing = if (aesthetic.useWideLetterSpacing) 1.sp else 0.sp
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
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                ) {
                    Text("Rename", color = Color.White)
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
            title = { Text("Delete Attempt?", color = colors.error) },
            text = {
                Text(
                    "Are you sure you want to delete ${attempt.playerName}'s attempt? This action cannot be undone.",
                    color = colors.error
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteAttempt?.invoke(attempt)
                        onDismissDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.error)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                Button(onClick = onDismissDelete) { Text("Cancel") }
            }
        )
    }

    // Share Dialog
    if (showShareDialog) {
        AlertDialog(
            onDismissRequest = onDismissShare,
            title = { Text("Share Attempt", color = colors.onSurface) },
            text = {
                Column {
                    Text("Which version would you like to share?", color = colors.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            onShareAttempt?.invoke(attempt.attemptFilePath)
                            onDismissShare()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                    ) {
                        Text("Share Original (What ${attempt.playerName} Sang)", color = Color.White)
                    }

                    if (attempt.reversedAttemptFilePath != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                onShareAttempt?.invoke(attempt.reversedAttemptFilePath)
                                onDismissShare()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                        ) {
                            Text("Share Reversed (How It Sounds)", color = Color.White)
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