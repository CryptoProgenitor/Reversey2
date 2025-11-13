package com.example.reversey.ui.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.reversey.data.models.ChallengeType
import com.example.reversey.data.models.PlayerAttempt
import com.example.reversey.data.models.Recording
import com.example.reversey.ui.components.RecordingDeleteDialog
import com.example.reversey.ui.components.RecordingRenameDialog
import com.example.reversey.ui.components.RecordingShareDialog
import com.example.reversey.ui.components.UnifiedRecordButton

/**
 * 🌸 SAKURA SERENITY THEME COMPONENTS
 *
 * Cherry blossom theme with serene pink/coral gradients.
 * Implements ThemeComponents interface for drop-in theming.
 */
class SakuraSerenityComponents : ThemeComponents {

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
        var showRenameDialog by remember { mutableStateOf(false) }
        var showDeleteDialog by remember { mutableStateOf(false) }
        var showShareDialog by remember { mutableStateOf(false) }

        // 🌸 SAKURA CARD DESIGN
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(24.dp),
                    spotColor = Color(0x40FF69B4)
                ),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFFFAFC) // Almost white with pink tint
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Box {
                // Decorative pink glow in corner
                Canvas(
                    modifier = Modifier
                        .size(120.dp)
                        .align(Alignment.TopEnd)
                ) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0x30FFB6C1),
                                Color(0x10FF69B4),
                                Color.Transparent
                            )
                        ),
                        center = Offset(size.width * 0.8f, size.height * 0.2f),
                        radius = size.width * 0.6f
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // 🌸 Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Title with cherry blossom emoji
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { showRenameDialog = true }
                        ) {
                            Text(
                                text = "🌸",
                                fontSize = 20.sp,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = recording.name,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF69B4) // Hot pink
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Delete button
                        IconButton(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = Color(0xFFFF69B4),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 🎵 SAKURA WAVEFORM
                    SakuraWaveform(
                        isPlaying = isPlaying,
                        progress = progress
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 🔘 ACTION BUTTONS
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Play/Pause Button
                        SakuraButton(
                            onClick = {
                                recording.reversedPath?.let { path ->
                                    if (isPlaying) {
                                        if (isPaused) onPlay(path) else onPause()
                                    } else {
                                        onPlay(path)
                                    }
                                }
                            },
                            icon = when {
                                isPlaying && !isPaused -> Icons.Default.Pause
                                else -> Icons.Default.PlayArrow
                            },
                            label = when {
                                isPlaying && !isPaused -> "Pause"
                                else -> "Play"
                            },
                            modifier = Modifier.weight(1f)
                        )

                        // Retry Challenge Button (if game mode)
                        if (isGameModeEnabled) {
                            SakuraButton(
                                onClick = { onStartAttempt(recording, ChallengeType.REVERSE) },
                                icon = Icons.Default.Replay,
                                label = "Retry",
                                modifier = Modifier.weight(1f),
                                isSecondary = true
                            )
                        }

                        // Share Button
                        SakuraButton(
                            onClick = { showShareDialog = true },
                            icon = Icons.Default.Share,
                            label = "Share",
                            modifier = Modifier.weight(1f),
                            isSecondary = true
                        )
                    }
                }
            }
        }

        // ✨ SHARED DIALOGS
        if (showRenameDialog) {
            RecordingRenameDialog(
                recording = recording,
                aesthetic = aesthetic,
                onDismiss = { showRenameDialog = false },
                onRename = onRename
            )
        }

        if (showDeleteDialog) {
            RecordingDeleteDialog(
                recording = recording,
                aesthetic = aesthetic,
                onDismiss = { showDeleteDialog = false },
                onDelete = onDelete
            )
        }

        if (showShareDialog) {
            RecordingShareDialog(
                recording = recording,
                aesthetic = aesthetic,
                onDismiss = { showShareDialog = false },
                onShare = onShare
            )
        }
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
        // Use default implementation (can customize later!)
        com.example.reversey.ui.components.UnifiedAttemptItem(
            attempt = attempt,
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
        // Use existing unified button (has good styling)
        UnifiedRecordButton(
            isRecording = isRecording,
            onClick = {
                if (isRecording) onStopRecording()
                else onStartRecording()
            }
        )
    }

    // 🌸 SAKURA APP BACKGROUND - Full immersive experience!
    @Composable
    override fun AppBackground(
        aesthetic: AestheticThemeData,
        content: @Composable () -> Unit
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF191970),  // Midnight blue at top
                            Color(0xFF4B0082),  // Indigo
                            Color(0xFF663399),  // Purple
                            Color(0xFFFF69B4),  // Hot pink
                            Color(0xFFFFB6C1)   // Light pink at bottom
                        )
                    )
                )
        ) {
            // ⭐ TWINKLING STARS in upper sky
            TwinklingStars()

            // 🌸 CHERRY BLOSSOM TREES scattered randomly
            CherryBlossomTrees()

            // App content on top
            content()
        }
    }
}
// 🎵 SAKURA WAVEFORM COMPONENT
@Composable
private fun SakuraWaveform(
    isPlaying: Boolean,
    progress: Float
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0x15FFB6C1),
                        Color(0x20FF69B4),
                        Color(0x15FFB6C1)
                    )
                )
            )
            .border(
                width = 2.dp,
                color = Color(0x40FFB6C1),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 8 waveform bars with animation
            repeat(8) { index ->
                val targetHeight = when (index) {
                    0 -> 0.4f
                    1 -> 0.7f
                    2 -> 0.5f
                    3 -> 0.85f
                    4 -> 0.45f
                    5 -> 0.75f
                    6 -> 0.6f
                    else -> 0.7f
                }

                // Animate bars when playing
                val animatedHeight by animateFloatAsState(
                    targetValue = if (isPlaying) targetHeight else targetHeight * 0.6f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(800 + index * 100, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "waveBar$index"
                )

                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight(animatedHeight)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFFF69B4),
                                    Color(0xFFFFB6C1)
                                )
                            )
                        )
                )
            }
        }
    }
}

// 🔘 SAKURA BUTTON COMPONENT
@Composable
private fun SakuraButton(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    isSecondary: Boolean = false
) {
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Circular button
        Box(
            modifier = Modifier
                .size(48.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = CircleShape,
                    spotColor = if (isSecondary) Color(0x30FFA07A) else Color(0x40FF69B4)
                )
                .clip(CircleShape)
                .background(
                    if (isSecondary) {
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFFFA07A),
                                Color(0xFFFF7F50)
                            )
                        )
                    } else {
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFFFB6C1),
                                Color(0xFFFF69B4)
                            )
                        )
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Label
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = if (isSecondary) Color(0xFFFF7F50) else Color(0xFFFF69B4)
            ),
            fontSize = 11.sp
        )
    }
}
// ⭐ TWINKLING STARS COMPONENT
@Composable
private fun TwinklingStars() {
    // Animate star twinkle
    val infiniteTransition = rememberInfiniteTransition(label = "starTwinkle")

    val twinkle1 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "twinkle1"
    )

    val twinkle2 by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, delayMillis = 200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "twinkle2"
    )

    val twinkle3 by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, delayMillis = 400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "twinkle3"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        // Stars ONLY in upper 30% of screen (the dark sky area)
        val stars = listOf(
            Triple(Offset(size.width * 0.15f, size.height * 0.08f), 12.dp.toPx(), Pair(Color(0xFFFFD700), twinkle1)),
            Triple(Offset(size.width * 0.25f, size.height * 0.12f), 10.dp.toPx(), Pair(Color(0xFFFFFFFF), twinkle2)),
            Triple(Offset(size.width * 0.35f, size.height * 0.06f), 11.dp.toPx(), Pair(Color(0xFFFFD700), twinkle3)),
            Triple(Offset(size.width * 0.45f, size.height * 0.10f), 9.dp.toPx(), Pair(Color(0xFFFFFFFF), twinkle1)),
            Triple(Offset(size.width * 0.55f, size.height * 0.14f), 13.dp.toPx(), Pair(Color(0xFFFFD700), twinkle2)),
            Triple(Offset(size.width * 0.65f, size.height * 0.08f), 10.dp.toPx(), Pair(Color(0xFFFFFFFF), twinkle3)),
            Triple(Offset(size.width * 0.75f, size.height * 0.11f), 12.dp.toPx(), Pair(Color(0xFFFFD700), twinkle1)),
            Triple(Offset(size.width * 0.85f, size.height * 0.07f), 11.dp.toPx(), Pair(Color(0xFFFFFFFF), twinkle2)),
            Triple(Offset(size.width * 0.92f, size.height * 0.13f), 10.dp.toPx(), Pair(Color(0xFFFFD700), twinkle3)),
            Triple(Offset(size.width * 0.10f, size.height * 0.15f), 9.dp.toPx(), Pair(Color(0xFFFFFFFF), twinkle1)),
            Triple(Offset(size.width * 0.30f, size.height * 0.18f), 11.dp.toPx(), Pair(Color(0xFFFFD700), twinkle2)),
            Triple(Offset(size.width * 0.50f, size.height * 0.20f), 10.dp.toPx(), Pair(Color(0xFFFFFFFF), twinkle3)),
            Triple(Offset(size.width * 0.70f, size.height * 0.17f), 12.dp.toPx(), Pair(Color(0xFFFFD700), twinkle1)),
            Triple(Offset(size.width * 0.88f, size.height * 0.22f), 11.dp.toPx(), Pair(Color(0xFFFFFFFF), twinkle2)),
        )

        stars.forEach { (position, baseSize, colorAlphaPair) ->
            val (color, alpha) = colorAlphaPair
            val starSize = baseSize * (0.7f + alpha * 0.3f)  // Size pulses!

            // Draw 5-pointed star
            val starPath = Path().apply {
                val outerRadius = starSize
                val innerRadius = starSize * 0.4f

                for (i in 0 until 10) {
                    val angle = (Math.PI / 5.0 * i - Math.PI / 2.0).toFloat()
                    val radius = if (i % 2 == 0) outerRadius else innerRadius
                    val x = position.x + radius * kotlin.math.cos(angle)
                    val y = position.y + radius * kotlin.math.sin(angle)

                    if (i == 0) moveTo(x, y) else lineTo(x, y)
                }
                close()
            }

            drawPath(
                path = starPath,
                color = color.copy(alpha = alpha)
            )
        }
    }
}

// 🌸 CHERRY BLOSSOM TREES COMPONENT - Scattered artistically!
@Composable
private fun CherryBlossomTrees() {
    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        // Artistic tree placements - scattered at different heights
        val trees = listOf(
            // Format: (xPosition, yPosition from top, trunkHeight, blossom size)
            TreeData(0.12f, 0.25f, 70.dp.toPx(), 40.dp.toPx()),  // Left side, mid-high
            TreeData(0.88f, 0.28f, 65.dp.toPx(), 38.dp.toPx()),  // Right side, higher
            TreeData(0.30f, 0.45f, 75.dp.toPx(), 42.dp.toPx()),  // Left-center, lower
            TreeData(0.65f, 0.40f, 68.dp.toPx(), 39.dp.toPx()),  // Right-center, mid
            TreeData(0.48f, 0.52f, 72.dp.toPx(), 41.dp.toPx()),  // Center, lowest
        )

        trees.forEach { tree ->
            val treeX = size.width * tree.xPosition
            val treeY = size.height * tree.yPosition

            // Tree trunk
            drawRect(
                color = Color(0xFF8B4513),
                topLeft = Offset(treeX - 8.dp.toPx(), treeY),
                size = Size(16.dp.toPx(), tree.trunkHeight)
            )

            // Cherry blossom top (3 overlapping circles for fluffy effect)
            drawCircle(
                color = Color(0xFFFFB6C1),  // Light pink
                radius = tree.blossomSize,
                center = Offset(treeX, treeY - tree.blossomSize * 0.3f)
            )

            drawCircle(
                color = Color(0xFFFF69B4),  // Hot pink
                radius = tree.blossomSize * 0.7f,
                center = Offset(treeX - tree.blossomSize * 0.4f, treeY - tree.blossomSize * 0.5f)
            )

            drawCircle(
                color = Color(0xFFFFC0CB),  // Pink
                radius = tree.blossomSize * 0.65f,
                center = Offset(treeX + tree.blossomSize * 0.35f, treeY - tree.blossomSize * 0.2f)
            )
        }
    }
}

// Tree data class for positioning
private data class TreeData(
    val xPosition: Float,      // 0.0 to 1.0 (percentage across screen)
    val yPosition: Float,      // 0.0 to 1.0 (percentage down screen)
    val trunkHeight: Float,    // Height in pixels
    val blossomSize: Float     // Radius in pixels
)