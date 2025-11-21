package com.example.reversey.ui.theme

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
import com.example.reversey.ui.components.ScoreExplanationDialog
import com.example.reversey.ui.components.UnifiedRecordButton

/**
 * 🌸 SAKURA SERENITY THEME
 * Cherry blossoms and sunset gradients.
 */
object SakuraSerenityTheme {
    const val THEME_ID = "sakura_serenity"

    val data = AestheticThemeData(
        id = THEME_ID,
        name = "Sakura Serenity",
        description = "🌸 Cherry blossoms and sunset gradients",
        components = SakuraSerenityComponents(),

        // Visuals
        primaryTextColor = Color.White,
        secondaryTextColor = Color(0xFFFFC0CB),
        primaryGradient = Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFFB6C1), // Light pink
                Color(0xFFFF69B4), // Hot pink
                Color(0xFFFFA07A), // Light coral sunset
                Color(0xFFFF7F50)  // Coral orange
            )
        ),
        cardBorder = Color(0xFFFF69B4),
        useGlassmorphism = false,
        glowIntensity = 0.3f,
        recordButtonEmoji = "🌸",
        scoreEmojis = mapOf(
            90 to "⭐",
            80 to "🌸",
            70 to "🌙",
            60 to "✨",
            0 to "🌱"
        ),

        // M3 Overrides
        cardAlpha = 0.95f,
        shadowElevation = 8f,
        useHandDrawnBorders = false,
        borderWidth = 2f,
        maxCardRotation = 0f,

        // Interaction
        dialogCopy = DialogCopy(
            deleteTitle = { type -> if (type == DeletableItemType.RECORDING) "Prune this branch?" else "Scatter petals?" },
            deleteMessage = { type, name -> "Shall we let '$name' fade away? It cannot bloom again." },
            deleteConfirmButton = "Prune",
            deleteCancelButton = "Preserve",
            shareTitle = "Share the Bloom 🌸",
            shareMessage = "Spread these petals to:",
            renameTitle = { "Name this Blossom" },
            renameHint = "New Name"
        ),
        scoreFeedback = ScoreFeedback(
            getTitle = { score ->
                when {
                    score >= 90 -> "Full Bloom!"
                    score >= 80 -> "Blossoming Beautifully"
                    score >= 70 -> "Budding Potential"
                    score >= 60 -> "Sprouting"
                    else -> "Planting Seeds"
                }
            },
            getMessage = { score ->
                when {
                    score >= 90 -> "A perfect spring day!"
                    score >= 80 -> "The garden is proud."
                    score >= 70 -> "Growing stronger every day."
                    score >= 60 -> "Needs a little more sunlight."
                    else -> "Patience is the key to growth."
                }
            },
            getEmoji = { score ->
                when {
                    score >= 90 -> "🌸"
                    score >= 80 -> "🌺"
                    score >= 70 -> "🌷"
                    score >= 60 -> "🌱"
                    else -> "🍂"
                }
            }
        ),
        menuColors = MenuColors.fromColors(
            primaryText = Color.White,
            secondaryText = Color(0xFFFFC0CB),
            border = Color(0xFFFF69B4),
            gradient = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFFF69B4),
                    Color(0xFFFFA07A)
                )
            )
        )
    )
}

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
                Canvas(
                    modifier = Modifier.size(120.dp).align(Alignment.TopEnd)
                ) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0x30FFB6C1), Color(0x10FF69B4), Color.Transparent)
                        ),
                        center = Offset(size.width * 0.8f, size.height * 0.2f),
                        radius = size.width * 0.6f
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f).clickable { showRenameDialog = true }
                        ) {
                            Text(text = "🌸", fontSize = 20.sp, modifier = Modifier.padding(end = 8.dp))
                            Text(
                                text = recording.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFFFF69B4)),
                                maxLines = 1, overflow = TextOverflow.Ellipsis
                            )
                        }

                        IconButton(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Delete, "Delete", tint = Color(0xFFFF69B4), modifier = Modifier.size(20.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    SakuraWaveform(isPlaying = isPlaying, progress = progress)

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
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
                            icon = if (isPlaying && !isPaused) Icons.Default.Pause else Icons.Default.PlayArrow,
                            label = if (isPlaying && !isPaused) "Pause" else "Play",
                            modifier = Modifier.weight(1f)
                        )

                        if (isGameModeEnabled) {
                            SakuraButton(
                                onClick = { onStartAttempt(recording, ChallengeType.REVERSE) },
                                icon = Icons.Default.Mic,
                                label = "Try",
                                modifier = Modifier.weight(1f),
                                isSecondary = true
                            )
                        }

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

        if (showRenameDialog) RenameDialog(RenamableItemType.RECORDING, recording.name, aesthetic, { onRename(recording.originalPath, it) }, { showRenameDialog = false })
        if (showDeleteDialog) DeleteDialog(DeletableItemType.RECORDING, recording, aesthetic, { onDelete(recording) }, { showDeleteDialog = false })
        if (showShareDialog) ShareDialog(recording, null, aesthetic, onShare, { showShareDialog = false })
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
        // Using Default Material card for attempts currently, but styled via Theme logic if we wanted specific Sakura attempt cards
        // For now, delegated to SharedDefaultComponents to keep it simple, or custom logic if needed.
        // Let's reuse SharedDefaultComponents.MaterialAttemptCard but injecting it here explicitly for clarity
        // Actually, let's implement a custom Sakura Attempt Card to match the style!

        var showRenameDialog by remember { mutableStateOf(false) }
        var showDeleteDialog by remember { mutableStateOf(false) }
        var showShareDialog by remember { mutableStateOf(false) }
        var showScoreDialog by remember { mutableStateOf(false) }
        val isPlayingThis = currentlyPlayingPath == attempt.attemptFilePath || currentlyPlayingPath == attempt.reversedAttemptFilePath

        Card(
            modifier = Modifier.fillMaxWidth().padding(start = 32.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFAFC)),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onJumpToParent != null) {
                        IconButton(onClick = onJumpToParent, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Home, "Parent", tint = Color(0xFFFF69B4))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = attempt.playerName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF69B4),
                        modifier = Modifier.weight(1f).clickable { showRenameDialog = true }
                    )
                    Surface(color = Color(0xFFFFB6C1).copy(alpha = 0.3f), shape = RoundedCornerShape(8.dp), modifier = Modifier.clickable { showScoreDialog = true }) {
                        Text("${attempt.score}%", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = Color(0xFFFF1493), fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = { showShareDialog = true }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Share, "Share", tint = Color(0xFFFF69B4))
                    }
                    IconButton(onClick = { if (isPlayingThis && !isPaused) onPause() else onPlay(attempt.attemptFilePath) }, modifier = Modifier.size(32.dp)) {
                        Icon(if (isPlayingThis && !isPaused) Icons.Default.Pause else Icons.Default.PlayArrow, "Play", tint = Color(0xFFFF69B4))
                    }
                    if (attempt.reversedAttemptFilePath != null) {
                        IconButton(onClick = { onPlay(attempt.reversedAttemptFilePath) }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Replay, "Reverse", tint = Color(0xFFFF69B4))
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    if (onDeleteAttempt != null) {
                        IconButton(onClick = { showDeleteDialog = true }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Delete, "Delete", tint = Color(0xFFFF7F50))
                        }
                    }
                }

                if (isPlayingThis) {
                    LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp), color = Color(0xFFFF69B4), trackColor = Color(0xFFFFB6C1).copy(0.3f))
                }
            }
        }

        if (showRenameDialog && onRenamePlayer != null) RenameDialog(RenamableItemType.PLAYER, attempt.playerName, aesthetic, { onRenamePlayer(attempt, it) }, { showRenameDialog = false })
        if (showDeleteDialog && onDeleteAttempt != null) DeleteDialog(DeletableItemType.ATTEMPT, attempt, aesthetic, { onDeleteAttempt(attempt) }, { showDeleteDialog = false })
        if (showShareDialog && onShareAttempt != null) ShareDialog(null, attempt, aesthetic, onShareAttempt, { showShareDialog = false })
        if (showScoreDialog) ScoreCard(attempt, aesthetic, { showScoreDialog = false })
    }

    @Composable
    override fun RecordButton(
        isRecording: Boolean,
        isProcessing: Boolean,
        aesthetic: AestheticThemeData,
        onStartRecording: () -> Unit,
        onStopRecording: () -> Unit
    ) {
        UnifiedRecordButton(isRecording = isRecording, onClick = { if (isRecording) onStopRecording() else onStartRecording() })
    }

    @Composable
    override fun AppBackground(aesthetic: AestheticThemeData, content: @Composable () -> Unit) {
        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(colors = listOf(Color(0xFF191970), Color(0xFF4B0082), Color(0xFF663399), Color(0xFFFF69B4), Color(0xFFFFB6C1))))) {
            TwinklingStars()
            CherryBlossomTrees()
            content()
        }
    }

    // --- NEW INTERFACE METHODS ---

    @Composable
    override fun ScoreCard(attempt: PlayerAttempt, aesthetic: AestheticThemeData, onDismiss: () -> Unit) {
        ScoreExplanationDialog(attempt, onDismiss)
    }

    @Composable
    override fun DeleteDialog(itemType: DeletableItemType, item: Any, aesthetic: AestheticThemeData, onConfirm: () -> Unit, onDismiss: () -> Unit) {
        val copy = aesthetic.dialogCopy
        val name = if (item is Recording) item.name else if (item is PlayerAttempt) item.playerName else "Item"

        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = Color(0xFFFFF0F5),
            title = { Text(copy.deleteTitle(itemType), color = Color(0xFFC71585), fontWeight = FontWeight.Bold) },
            text = { Text(copy.deleteMessage(itemType, name), color = Color(0xFF8B008B)) },
            confirmButton = {
                Button(onClick = { onConfirm(); onDismiss() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF1493))) {
                    Text(copy.deleteConfirmButton)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text(copy.deleteCancelButton, color = Color(0xFFC71585)) }
            }
        )
    }

    @Composable
    override fun ShareDialog(recording: Recording?, attempt: PlayerAttempt?, aesthetic: AestheticThemeData, onShare: (String) -> Unit, onDismiss: () -> Unit) {
        val copy = aesthetic.dialogCopy
        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = Color(0xFFFFF0F5),
            title = { Text(copy.shareTitle, color = Color(0xFFC71585), fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(copy.shareMessage, color = Color(0xFF8B008B))
                    Spacer(modifier = Modifier.height(16.dp))
                    val path = recording?.originalPath ?: attempt?.attemptFilePath ?: ""
                    Button(onClick = { onShare(path); onDismiss() }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF69B4))) {
                        Text("Share Blossom 🌸")
                    }
                    val revPath = recording?.reversedPath ?: attempt?.reversedAttemptFilePath
                    if (revPath != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { onShare(revPath); onDismiss() }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDB7093))) {
                            Text("Share Petals 🍃")
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = Color(0xFFC71585)) } }
        )
    }

    @Composable
    override fun RenameDialog(itemType: RenamableItemType, currentName: String, aesthetic: AestheticThemeData, onRename: (String) -> Unit, onDismiss: () -> Unit) {
        var name by remember { mutableStateOf(currentName) }
        val copy = aesthetic.dialogCopy
        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = Color(0xFFFFF0F5),
            title = { Text(copy.renameTitle(itemType), color = Color(0xFFC71585), fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = name, onValueChange = { name = it }, singleLine = true,
                    label = { Text(copy.renameHint) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFFF69B4), focusedLabelColor = Color(0xFFFF69B4))
                )
            },
            confirmButton = {
                Button(onClick = { onRename(name); onDismiss() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF69B4))) {
                    Text("Bloom")
                }
            },
            dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = Color(0xFFC71585)) } }
        )
    }
}

// ============================================
// 🌸 SAKURA COMPONENTS
// ============================================

@Composable
private fun SakuraWaveform(isPlaying: Boolean, progress: Float) {
    Box(
        modifier = Modifier.fillMaxWidth().height(70.dp).clip(RoundedCornerShape(16.dp))
            .background(Brush.horizontalGradient(colors = listOf(Color(0x15FFB6C1), Color(0x20FF69B4), Color(0x15FFB6C1))))
            .border(2.dp, Color(0x40FFB6C1), RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
            repeat(8) { index ->
                val targetHeight = when (index) { 0 -> 0.4f; 1 -> 0.7f; 2 -> 0.5f; 3 -> 0.85f; 4 -> 0.45f; 5 -> 0.75f; 6 -> 0.6f; else -> 0.7f }
                val animatedHeight by animateFloatAsState(
                    targetValue = if (isPlaying) targetHeight else targetHeight * 0.6f,
                    animationSpec = infiniteRepeatable(animation = tween(800 + index * 100, easing = LinearEasing), repeatMode = RepeatMode.Reverse),
                    label = "waveBar$index"
                )
                Box(modifier = Modifier.width(4.dp).fillMaxHeight(animatedHeight).clip(RoundedCornerShape(2.dp)).background(Brush.verticalGradient(colors = listOf(Color(0xFFFF69B4), Color(0xFFFFB6C1)))))
            }
        }
    }
}

@Composable
private fun SakuraButton(onClick: () -> Unit, icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, modifier: Modifier = Modifier, isSecondary: Boolean = false) {
    Column(modifier = modifier.clickable(onClick = onClick), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Box(
            modifier = Modifier.size(48.dp)
                .shadow(4.dp, CircleShape, spotColor = if (isSecondary) Color(0x30FFA07A) else Color(0x40FF69B4))
                .clip(CircleShape)
                .background(if (isSecondary) Brush.linearGradient(colors = listOf(Color(0xFFFFA07A), Color(0xFFFF7F50))) else Brush.linearGradient(colors = listOf(Color(0xFFFFB6C1), Color(0xFFFF69B4)))),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, color = if (isSecondary) Color(0xFFFF7F50) else Color(0xFFFF69B4)), fontSize = 11.sp)
    }
}

@Composable
private fun TwinklingStars() {
    val infiniteTransition = rememberInfiniteTransition(label = "starTwinkle")
    val twinkle1 by infiniteTransition.animateFloat(0.3f, 1f, infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "twinkle1")
    val twinkle2 by infiniteTransition.animateFloat(0.5f, 1f, infiniteRepeatable(tween(1500, 200, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "twinkle2")

    Canvas(modifier = Modifier.fillMaxSize()) {
        val stars = listOf(
            Triple(Offset(size.width * 0.15f, size.height * 0.08f), 12.dp.toPx(), Color(0xFFFFD700) to twinkle1),
            Triple(Offset(size.width * 0.85f, size.height * 0.07f), 11.dp.toPx(), Color(0xFFFFFFFF) to twinkle2),
            Triple(Offset(size.width * 0.50f, size.height * 0.20f), 10.dp.toPx(), Color(0xFFFFFFFF) to twinkle1)
        )
        stars.forEach { (pos, baseSize, pair) ->
            val (color, alpha) = pair
            val size = baseSize * (0.7f + alpha * 0.3f)
            drawCircle(color.copy(alpha = alpha), size / 2, pos)
        }
    }
}

@Composable
private fun CherryBlossomTrees() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val trees = listOf(
            Triple(0.12f, 0.25f, 40.dp.toPx()), Triple(0.88f, 0.28f, 38.dp.toPx()), Triple(0.48f, 0.52f, 41.dp.toPx())
        )
        trees.forEach { (xPct, yPct, size) ->
            val x = this.size.width * xPct
            val y = this.size.height * yPct
            drawRect(Color(0xFF8B4513), topLeft = Offset(x - 8.dp.toPx(), y), size = Size(16.dp.toPx(), 70.dp.toPx()))
            drawCircle(Color(0xFFFFB6C1), size, Offset(x, y - size * 0.3f))
            drawCircle(Color(0xFFFF69B4), size * 0.7f, Offset(x - size * 0.4f, y - size * 0.5f))
        }
    }
}