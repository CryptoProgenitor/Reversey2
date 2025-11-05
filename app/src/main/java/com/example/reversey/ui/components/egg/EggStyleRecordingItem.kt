package com.example.reversey.ui.components.egg

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import com.example.reversey.data.models.Recording
import com.example.reversey.ui.theme.AestheticThemeData
import com.example.reversey.data.models.ChallengeType
import com.example.reversey.utils.formatFileName

/**
 * EGG-THEMED RECORDING ITEM - PROPER HAND-DRAWN STYLE MATCHING MOCKUP! 🥚🍳
 */
@Composable
fun EggStyleRecordingItem(
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

    // Hand-drawn card style matching mockup
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(
                elevation = 0.dp,
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 4.dp,
                color = Color(0xFF2E2E2E),
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBF0)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Decorative fried eggs + recording title
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
                        modifier = Modifier.clickable { showRenameDialog = true } // 🔧 ADD THIS LINE
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

            // Progress bar with traveling egg! 🥚✨
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, Color(0xFF2E2E2E), RoundedCornerShape(4.dp))
                    .padding(2.dp) // Inner padding so progress bar doesn't touch border
            ) {
                EggTravelProgressBar(
                    progress = if (isPlaying) progress else 0f,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 🥚 HAND-DRAWN 6-BUTTON LAYOUT MATCHING MOCKUP! 🥚
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. SHARE BUTTON (purple like mockup)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    HandDrawnEggButton(
                        onClick = { showShareDialog = true },
                        backgroundColor = Color(0xFF9C27B0),
                        size = 50.dp
                    ) {
                        HandDrawnShareIcon()
                    }
                    Text(
                        text = "Share",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF2E2E2E)
                    )
                }

                // 2. PLAY BUTTON (orange like mockup)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    HandDrawnEggButton(
                        onClick = {
                            if (isPlaying && !isPaused) {
                                onPause()
                            } else if (isPlaying && isPaused) {
                                onPlay(recording.originalPath ?: "")
                            } else {
                                onPlay(recording.originalPath ?: "")
                            }
                        },
                        backgroundColor = Color(0xFFFF8A65),
                        size = 50.dp
                    ) {
                        if (isPlaying && !isPaused) {
                            HandDrawnPauseIcon()
                        } else {
                            HandDrawnPlayIcon()
                        }
                    }
                    Text(
                        text = if (isPlaying && !isPaused) "Pause" else "Play",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF2E2E2E)
                    )
                }

                // 3. REWIND BUTTON (orange like mockup)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    HandDrawnEggButton(
                        onClick = { onPlay(recording.reversedPath ?: "") },
                        backgroundColor = Color(0xFFFF8A65),
                        enabled = recording.reversedPath != null,
                        size = 50.dp
                    ) {
                        HandDrawnRewindIcon()
                    }
                    Text(
                        text = "Rewind",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF2E2E2E)
                    )
                }

                // 4. FWD BUTTON (orange like mockup) - only if game mode
                if (isGameModeEnabled) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        HandDrawnEggButton(
                            //onClick = { onStartAttempt(recording, ChallengeType.values().getOrNull(0) ?: return@HandDrawnEggButton) }, // broken -reversed sense!
                            onClick = { onStartAttempt(recording, ChallengeType.FORWARD) }, //fixed - uses explicit enum value
                            backgroundColor = Color(0xFFFF8A65),
                            size = 50.dp
                        ) {
                            HandDrawnMicIcon()
                        }
                        Text(
                            text = "Fwd",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF2E2E2E)
                        )
                    }
                }

                // 5. REV BUTTON (orange like mockup) - only if game mode
                if (isGameModeEnabled) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        HandDrawnEggButton(
                            //onClick = { onStartAttempt(recording, ChallengeType.values().getOrNull(1) ?: return@HandDrawnEggButton) }, // broken -reversed sense!
                            onClick = { onStartAttempt(recording, ChallengeType.REVERSE) }, //fixed - uses explicit enum value
                            backgroundColor = Color(0xFFFF8A65),
                            size = 50.dp
                        ) {
                            HandDrawnReverseIcon()
                        }
                        Text(
                            text = "Rev",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF2E2E2E)
                        )
                    }
                }

                // 6. DELETE BUTTON (red like mockup) with CRACKED EGG ICON!
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    HandDrawnEggButton(
                        onClick = { showDeleteDialog = true },
                        backgroundColor = Color(0xFFFF5722),
                        size = 50.dp
                    ) {
                        CrackedEggIcon()
                    }
                    Text(
                        text = "Del",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF2E2E2E)
                    )
                }
            }
        }
    }

    // EGG-THEMED DIALOGS (same as before but with proper styling)
    if (showRenameDialog) {
        var newName by remember { mutableStateOf(recording.name) }
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Egg Recording 🥚", color = Color(0xFF2E2E2E), fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("New Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFF8A65),
                        focusedLabelColor = Color(0xFFFF8A65)
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotBlank()) {
                            onRename(recording.originalPath ?: "", newName)
                        }
                        showRenameDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8A65))
                ) { Text("Rename", color = Color.White, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                Button(
                    onClick = { showRenameDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E2E2E))
                ) { Text("Cancel", color = Color.White, fontWeight = FontWeight.Bold) }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Crack this egg? 🥚💥", color = Color(0xFF2E2E2E), fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to crack this egg? This action cannot be undone!", fontWeight = FontWeight.Bold) },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete(recording)
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722))
                ) { Text("Crack It!", color = Color.White, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                Button(
                    onClick = { showDeleteDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8A65))
                ) { Text("Keep Safe", color = Color.White, fontWeight = FontWeight.Bold) }
            }
        )
    }

    if (showShareDialog) {
        AlertDialog(
            onDismissRequest = { showShareDialog = false },
            title = { Text("Share Your Egg! 🥚", color = Color(0xFF2E2E2E), fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Which egg would you like to share?", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            onShare(recording.originalPath ?: "")
                            showShareDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8A65))
                    ) {
                        Text("Share Fresh Egg 🥚", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    if (recording.reversedPath != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                onShare(recording.reversedPath!!)
                                showShareDialog = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB74D))
                        ) {
                            Text("Share Scrambled Egg 🍳", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = { },
            dismissButton = {
                Button(
                    onClick = { showShareDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E2E2E))
                ) { Text("Cancel", color = Color.White, fontWeight = FontWeight.Bold) }
            }
        )
    }
}

/**
 * Hand-drawn style button matching the mockup design
 */
@Composable
fun HandDrawnEggButton(
    onClick: () -> Unit,
    backgroundColor: Color,
    enabled: Boolean = true,
    size: Dp,
    content: @Composable () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .size(size)
            .shadow(
                elevation = 0.dp,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 3.dp,
                color = Color(0xFF2E2E2E),
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            disabledContainerColor = backgroundColor.copy(alpha = 0.3f)
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        content()
    }
}

/**
 * Fried egg decoration matching mockup
 */
@Composable
fun FriedEggDecoration(
    size: Dp,
    rotation: Float,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .size(size)
            .rotate(rotation)
    ) {
        val center = this.center
        val radius = this.size.minDimension / 2

        // Draw irregular egg white (matching mockup path)
        val eggWhitePath = Path().apply {
            moveTo(center.x - radius * 0.7f, center.y)
            quadraticBezierTo(
                center.x - radius * 0.9f, center.y - radius * 0.6f,
                center.x - radius * 0.3f, center.y - radius * 0.8f
            )
            quadraticBezierTo(
                center.x + radius * 0.2f, center.y - radius * 0.9f,
                center.x + radius * 0.6f, center.y - radius * 0.4f
            )
            quadraticBezierTo(
                center.x + radius * 0.8f, center.y + radius * 0.2f,
                center.x + radius * 0.4f, center.y + radius * 0.7f
            )
            quadraticBezierTo(
                center.x - radius * 0.1f, center.y + radius * 0.8f,
                center.x - radius * 0.6f, center.y + radius * 0.3f
            )
            close()
        }

        drawPath(
            path = eggWhitePath,
            color = Color(0xFFFFF8E1)
        )
        drawPath(
            path = eggWhitePath,
            color = Color(0xFF2E2E2E),
            style = Stroke(width = 2.dp.toPx())
        )

        // Draw yolk
        drawCircle(
            color = Color(0xFFFFD700),
            radius = radius * 0.35f,
            center = center
        )
        drawCircle(
            color = Color(0xFF2E2E2E),
            radius = radius * 0.35f,
            center = center,
            style = Stroke(width = 2.dp.toPx())
        )
    }
}

// CUSTOM HAND-DRAWN ICONS MATCHING MOCKUP

@Composable
fun HandDrawnShareIcon() {
    Canvas(modifier = Modifier.size(20.dp)) {
        val strokeWidth = 2.5.dp.toPx()
        val color = Color.White

        // Three circles connected by lines (like mockup)
        drawCircle(color, 3.dp.toPx(), Offset(size.width * 0.75f, size.height * 0.25f), style = Stroke(strokeWidth))
        drawCircle(color, 3.dp.toPx(), Offset(size.width * 0.25f, size.height * 0.5f), style = Stroke(strokeWidth))
        drawCircle(color, 3.dp.toPx(), Offset(size.width * 0.75f, size.height * 0.75f), style = Stroke(strokeWidth))

        // Connection lines
        drawLine(color, Offset(size.width * 0.28f, size.height * 0.53f), Offset(size.width * 0.72f, size.height * 0.72f), strokeWidth)
        drawLine(color, Offset(size.width * 0.72f, size.height * 0.28f), Offset(size.width * 0.28f, size.height * 0.47f), strokeWidth)
    }
}

@Composable
fun HandDrawnPlayIcon() {
    Canvas(modifier = Modifier.size(20.dp)) {
        val path = Path().apply {
            moveTo(size.width * 0.3f, size.height * 0.2f)
            lineTo(size.width * 0.8f, size.height * 0.5f)
            lineTo(size.width * 0.3f, size.height * 0.8f)
            close()
        }
        drawPath(path, Color.White)
        drawPath(path, Color(0xFF2E2E2E), style = Stroke(2.dp.toPx()))
    }
}

@Composable
fun HandDrawnPauseIcon() {
    Canvas(modifier = Modifier.size(20.dp)) {
        val strokeWidth = 3.dp.toPx()
        drawLine(Color.White, Offset(size.width * 0.35f, size.height * 0.2f), Offset(size.width * 0.35f, size.height * 0.8f), strokeWidth)
        drawLine(Color.White, Offset(size.width * 0.65f, size.height * 0.2f), Offset(size.width * 0.65f, size.height * 0.8f), strokeWidth)
    }
}

@Composable
fun HandDrawnRewindIcon() {
    Canvas(modifier = Modifier.size(20.dp)) {
        val strokeWidth = 2.5.dp.toPx()
        // Double arrow pointing left
        val path1 = Path().apply {
            moveTo(size.width * 0.5f, size.height * 0.2f)
            lineTo(size.width * 0.2f, size.height * 0.5f)
            lineTo(size.width * 0.5f, size.height * 0.8f)
        }
        val path2 = Path().apply {
            moveTo(size.width * 0.8f, size.height * 0.2f)
            lineTo(size.width * 0.5f, size.height * 0.5f)
            lineTo(size.width * 0.8f, size.height * 0.8f)
        }
        drawPath(path1, Color.White, style = Stroke(strokeWidth))
        drawPath(path2, Color.White, style = Stroke(strokeWidth))
    }
}

@Composable
fun HandDrawnMicIcon() {
    Canvas(modifier = Modifier.size(20.dp)) {
        val strokeWidth = 2.5.dp.toPx()
        val color = Color.White

        // Microphone shape
        drawRoundRect(
            color,
            Offset(size.width * 0.35f, size.height * 0.1f),
            androidx.compose.ui.geometry.Size(size.width * 0.3f, size.height * 0.5f),
            androidx.compose.ui.geometry.CornerRadius(size.width * 0.15f),
            style = Stroke(strokeWidth)
        )

        // Stand
        drawLine(color, Offset(size.width * 0.5f, size.height * 0.6f), Offset(size.width * 0.5f, size.height * 0.85f), strokeWidth)
        drawLine(color, Offset(size.width * 0.3f, size.height * 0.85f), Offset(size.width * 0.7f, size.height * 0.85f), strokeWidth)
    }
}

@Composable
fun HandDrawnReverseIcon() {
    Canvas(modifier = Modifier.size(20.dp)) {
        val strokeWidth = 2.5.dp.toPx()
        val color = Color.White

        // Circular arrow
        drawArc(
            color,
            startAngle = 45f,
            sweepAngle = 270f,
            useCenter = false,
            style = Stroke(strokeWidth),
            topLeft = Offset(size.width * 0.1f, size.height * 0.1f),
            size = androidx.compose.ui.geometry.Size(size.width * 0.8f, size.height * 0.8f)
        )

        // Arrow head
        val path = Path().apply {
            moveTo(size.width * 0.15f, size.height * 0.15f)
            lineTo(size.width * 0.05f, size.height * 0.25f)
            lineTo(size.width * 0.25f, size.height * 0.25f)
        }
        drawPath(path, color, style = Stroke(strokeWidth))
    }
}

@Composable
fun CrackedEggIcon() {
    Canvas(modifier = Modifier.size(20.dp)) {
        val color = Color.White
        val strokeWidth = 2.5.dp.toPx()

        // Left half of cracked egg
        val leftPath = Path().apply {
            moveTo(size.width * 0.5f, size.height * 0.1f)
            quadraticBezierTo(size.width * 0.2f, size.height * 0.2f, size.width * 0.15f, size.height * 0.4f)
            quadraticBezierTo(size.width * 0.1f, size.height * 0.7f, size.width * 0.4f, size.height * 0.9f)
            lineTo(size.width * 0.5f, size.height * 0.1f)
        }

        // Right half of cracked egg (slightly offset)
        val rightPath = Path().apply {
            moveTo(size.width * 0.5f, size.height * 0.1f)
            quadraticBezierTo(size.width * 0.8f, size.height * 0.2f, size.width * 0.85f, size.height * 0.4f)
            quadraticBezierTo(size.width * 0.9f, size.height * 0.7f, size.width * 0.6f, size.height * 0.9f)
            lineTo(size.width * 0.5f, size.height * 0.1f)
        }

        // Draw both halves
        drawPath(leftPath, color)
        drawPath(leftPath, Color(0xFF2E2E2E), style = Stroke(strokeWidth))
        drawPath(rightPath, color)
        drawPath(rightPath, Color(0xFF2E2E2E), style = Stroke(strokeWidth))

        // Jagged crack line
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
/**
 * Custom progress bar with a traveling egg! 🥚✨
 */
@Composable
fun EggTravelProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 10.dp // Thinner progress bar for better proportion
) {
    BoxWithConstraints(modifier = modifier.height(height)) {
        val trackWidth = maxWidth

        // Background track (light orange)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Color(0xFFFFE0B2), // Light orange track
                    RoundedCornerShape(height / 2)
                )
        )

        // Progress fill (orange)
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .background(
                    Color(0xFFFF8A65), // Orange progress
                    RoundedCornerShape(height / 2)
                )
        )

        // Traveling egg! 🥚 (ORIGINAL WORKING VERSION!)
        if (progress > 0f) {
            val eggSize = 120.dp // 1.5x bigger than the working 80.dp
            val eggPosition = progress.coerceIn(0f, 1f).times(trackWidth - eggSize)

            Box(
                modifier = Modifier
                    .size(eggSize)
                    .offset(x = eggPosition, y = -2.dp),
                contentAlignment = Alignment.Center
            ) {
                // Original working hand-drawn egg! 🥚
                Canvas(
                    modifier = Modifier.size(90.dp) // 1.5x bigger than working 60.dp
                ) {
                    val center = this.center
                    val radius = this.size.minDimension / 2

                    // Egg white (oval shape) - ORIGINAL WORKING
                    drawOval(
                        color = Color(0xFFFFF8E1),
                        topLeft = Offset(center.x - radius * 0.7f, center.y - radius * 0.9f),
                        size = androidx.compose.ui.geometry.Size(radius * 1.4f, radius * 1.8f)
                    )

                    // Egg border - ORIGINAL WORKING
                    drawOval(
                        color = Color(0xFF2E2E2E),
                        topLeft = Offset(center.x - radius * 0.7f, center.y - radius * 0.9f),
                        size = androidx.compose.ui.geometry.Size(radius * 1.4f, radius * 1.8f),
                        style = Stroke(2.dp.toPx()) // Original working thickness
                    )

                    // Yolk (yellow circle) - ORIGINAL WORKING
                    drawCircle(
                        color = Color(0xFFFFD700),
                        radius = radius * 0.4f,
                        center = center
                    )

                    // Yolk border - ORIGINAL WORKING
                    drawCircle(
                        color = Color(0xFF2E2E2E),
                        radius = radius * 0.4f,
                        center = center,
                        style = Stroke(1.dp.toPx()) // Original working thickness
                    )
                }
            }
        }
    }
}
