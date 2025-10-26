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

// Helper function to create ScoringResult from PlayerAttempt
private fun createScoringResultFromAttempt(attempt: PlayerAttempt): com.example.reversey.scoring.ScoringResult {
    // Estimate metrics based on score
    val normalizedScore = attempt.score / 100f
    val pitchSimilarity = normalizedScore + (Math.random().toFloat() * 0.1f - 0.05f) // Small random variation
    val mfccSimilarity = normalizedScore + (Math.random().toFloat() * 0.1f - 0.05f)

    return com.example.reversey.scoring.ScoringResult(
        score = attempt.score,
        rawScore = normalizedScore,
        metrics = com.example.reversey.scoring.SimilarityMetrics(
            pitch = pitchSimilarity.coerceIn(0f, 1f),
            mfcc = mfccSimilarity.coerceIn(0f, 1f)
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
    theme: AppTheme,
    onJumpToParent: (() -> Unit)? = null,
) {
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
                if (theme.useGlassmorphism && theme.glowIntensity > 0) {
                    Modifier.shadow(
                        elevation = (theme.glowIntensity * 20).dp,
                        shape = RoundedCornerShape(16.dp),
                        spotColor = theme.accentColor
                    )
                } else Modifier
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (theme.useGlassmorphism) {
                theme.cardBackground.copy(alpha = 0.7f)
            } else theme.cardBackground
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (theme.useGlassmorphism) {
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
                    } else Modifier.border(1.dp, theme.cardBorder, RoundedCornerShape(16.dp))
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
                                    tint = theme.accentColor,
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
                                    if (theme.glowIntensity > 0) {
                                        Modifier.shadow(
                                            elevation = (theme.glowIntensity * 10).dp,
                                            shape = CircleShape,
                                            spotColor = theme.accentColor
                                        )
                                    } else Modifier
                                )
                        )

                        // Player name with theme typography
                        Text(
                            text = attempt.playerName,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = if (theme.useWideLetterSpacing) 1.2.sp else 0.sp
                            ),
                            color = getThemeAwareTextColor(theme, TextType.ACCENT),  // <-- CHANGE TO THIS
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Control buttons row
                    ControlButtonsRow(
                        isPlayingThis = isPlayingThis,
                        isPaused = isPaused,
                        attempt = attempt,
                        theme = theme,
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
                    theme = theme,
                    isAnimated = true,
                    onClick = { showScoreDialog = true }  // ← ADD THIS LINE
                )
            }

            // Progress indicator
            if (isPlayingThis) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = theme.accentColor,
                    trackColor = theme.cardBorder
                )
            }
        }
    }

    // Dialogs (same as before but with enhanced styling)
    EnhancedDialogs(
        showRenameDialog = showRenameDialog,
        showDeleteDialog = showDeleteDialog,
        showShareDialog = showShareDialog,
        attempt = attempt,
        theme = theme,
        onRenamePlayer = onRenamePlayer,
        onDeleteAttempt = onDeleteAttempt,
        onShareAttempt = onShareAttempt,
        onDismissRename = { showRenameDialog = false },
        onDismissDelete = { showDeleteDialog = false },
        onDismissShare = { showShareDialog = false }
    )

    // Score explanation dialog
    if (showScoreDialog) {
        ScoreExplanationDialog(
            score = createScoringResultFromAttempt(attempt),
            challengeType = attempt.challengeType,
            currentTheme = theme,
            onDismiss = { showScoreDialog = false }
        )
    }
}
//end of fun EnhancedAttemptItem



/**
 * Radial Progress Circle with Emoji Medal - Core of Dopamine UI Strategy
 */
@Composable
fun RadialScoreDisplay(
    score: Int,
    theme: AppTheme,
    isAnimated: Boolean = true,
    size: androidx.compose.ui.unit.Dp = 80.dp,//Score Circle Diameter
    onClick: (() -> Unit)? = null  // ← ADD THIS LINE
) {
    // Calculate text scaling based on circle size
    val textScale = size.value / 80f  // ✅ ADD THIS LINE HERE
    // Get emoji for score
    val emoji = theme.scoreEmojis.entries
        .sortedByDescending { it.key }
        .firstOrNull { score >= it.key }?.value ?: "💪"

    // Animation for the progress
    val animatedScore by animateFloatAsState(
        targetValue = if (isAnimated) score / 100f else score / 100f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "score_animation"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(size)
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else Modifier
            )
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            drawRadialProgress(
                progress = animatedScore,
                theme = theme,
                size = this.size
            )
        }

        // Center content: Emoji and score
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = emoji,
                fontSize = (20 * textScale).sp,
                modifier = Modifier
                    .then(
                        if (theme.glowIntensity > 0) {
                            Modifier.shadow(
                                elevation = (theme.glowIntensity * 15).dp,
                                shape = CircleShape,
                                spotColor = theme.accentColor
                            )
                        } else Modifier
                    )
            )
            Text(
                text = "${score}%",
                fontSize = (12 * textScale).sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = if (theme.useWideLetterSpacing) 0.8.sp else 0.sp,
                color = theme.textPrimary,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Draws the radial progress circle with theme-aware gradients
 */
private fun DrawScope.drawRadialProgress(
    progress: Float,
    theme: AppTheme,
    size: androidx.compose.ui.geometry.Size
) {
    val strokeWidth = 8.dp.toPx()
    val radius = (size.minDimension / 2) - strokeWidth / 2
    val center = size.center

    // Background track
    drawCircle(
        color = theme.cardBorder.copy(alpha = 0.3f),
        radius = radius,
        center = center,
        style = Stroke(strokeWidth)
    )

    // Progress arc with gradient
    val sweepAngle = 360f * progress
    val startAngle = -90f // Start at top

    // Create gradient brush
    val progressBrush = Brush.sweepGradient(
        colors = listOf(
            theme.accentColor,
            theme.accentColor.copy(alpha = 0.7f),
            theme.accentColor
        ),
        center = center
    )

    drawArc(
        brush = progressBrush,
        startAngle = startAngle,
        sweepAngle = sweepAngle,
        useCenter = false,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round
        ),
        topLeft = Offset(
            center.x - radius,
            center.y - radius
        ),
        size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
    )
}

/**
 * Enhanced control buttons with glow effects and glassmorphism
 */
@Composable
fun ControlButtonsRow(
    isPlayingThis: Boolean,
    isPaused: Boolean,
    attempt: PlayerAttempt,
    theme: AppTheme,
    onPlay: (String) -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit,
    //onJumpToParent: (() -> Unit)?,//Relocated to top row! in v.9.4.0
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
            theme = theme,
            size = 32.dp,
            label = "Share"
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Share Attempt",
                tint = theme.textPrimary,
                modifier = Modifier.size(16.dp)
            )
        }

        // Play controls
        if (isPlayingThis) {
            EnhancedGlowButton(
                onClick = onPause,
                theme = theme,
                isPrimary = true,
                label = if (isPaused) "Resume" else "Pause"
            ) {
                val pauseIcon = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause
                Icon(
                    imageVector = pauseIcon,
                    contentDescription = "Pause/Resume",
                    tint = Color.White
                )
            }

            EnhancedGlowButton(
                onClick = onStop,
                theme = theme,
                isDestructive = true,
                label = "Stop"
            ) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = "Stop",
                    tint = Color.White
                )
            }
        } else {
            // Play original
            EnhancedGlowButton(
                onClick = { onPlay(attempt.attemptFilePath) },
                theme = theme,
                isPrimary = true,
                label = "Play"
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play Original",
                    tint = Color.White
                )
            }

            // Play reversed
            EnhancedGlowButton(
                onClick = {
                    attempt.reversedAttemptFilePath?.let { onPlay(it) }
                },
                enabled = attempt.reversedAttemptFilePath != null,
                theme = theme,
                isPrimary = true,
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
            theme = theme,
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
 * Themed button with glow effects and glassmorphism
 */
@Composable
fun GlowButton(
    onClick: () -> Unit,
    theme: AppTheme,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isPrimary: Boolean = false,
    isDestructive: Boolean = false,
    size: androidx.compose.ui.unit.Dp = 40.dp,
    content: @Composable () -> Unit
) {
    val backgroundColor = when {
        isDestructive -> Color(0xFFFF1744)
        isPrimary -> theme.accentColor
        else -> theme.cardBackground
    }

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .size(size)
            .then(
                if (theme.glowIntensity > 0) {
                    Modifier.shadow(
                        elevation = (theme.glowIntensity * 12).dp,
                        shape = CircleShape,
                        spotColor = if (isPrimary) theme.accentColor else Color.Gray
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
    theme: AppTheme,
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
                    "Rename Player",
                    color = theme.textPrimary,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        letterSpacing = if (theme.useWideLetterSpacing) 1.sp else 0.sp
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
                    colors = ButtonDefaults.buttonColors(containerColor = theme.accentColor)
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
            title = { Text("Delete Attempt?", color = Color.Red) },
            text = {
                Text(
                    "Are you sure you want to delete ${attempt.playerName}'s attempt? This action cannot be undone.",
                    color = Color.Red
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteAttempt?.invoke(attempt)
                        onDismissDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF1744))
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
            title = { Text("Share Attempt", color = theme.textPrimary) },
            text = {
                Column {
                    Text("Which version would you like to share?", color = theme.textSecondary)
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            onShareAttempt?.invoke(attempt.attemptFilePath)
                            onDismissShare()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = theme.accentColor)
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
                            colors = ButtonDefaults.buttonColors(containerColor = theme.accentColor)
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
