package com.example.reversey.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.reversey.R
import com.example.reversey.data.models.ChallengeType
import com.example.reversey.data.models.PlayerAttempt
import com.example.reversey.data.models.Recording
import com.example.reversey.scoring.DifficultyConfig
import com.example.reversey.ui.components.ScoreExplanationDialog
import com.example.reversey.ui.viewmodels.AudioViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random
import kotlinx.coroutines.isActive


// Handwriting font
private val dancingScriptFontFamily = FontFamily(
    Font(R.font.dancing_script_regular, FontWeight.Normal),
    Font(R.font.dancing_script_bold, FontWeight.Bold)
)

// 🎨 VIBRANT PASTEL PALETTE (Northern Lights Style)
private val AuroraPink = Color(0xFFFFB7C5)
private val AuroraCyan = Color(0xFF80DEEA)
private val AuroraLavender = Color(0xFFCE93D8)
private val AuroraPeach = Color(0xFFFFCC80)
private val AuroraMint = Color(0xFFA5D6A7)

// 🎨 HIGH SATURATION PALETTE (Recording State)
private val VibrantPink = Color(0xFFFF4081)
private val VibrantCyan = Color(0xFF00BCD4)
private val VibrantLavender = Color(0xFF9C27B0)
private val VibrantPeach = Color(0xFFFF9800)
private val VibrantMint = Color(0xFF4CAF50)

/**
 * 📝 SCRAPBOOK THEME
 */
object ScrapbookTheme {
    const val THEME_ID = "scrapbook"

    val data = AestheticThemeData(
        id = THEME_ID,
        name = "Scrapbook Vibes",
        description = "📝 Sticky notes, hand-drawn fun, and playful chaos",
        components = ScrapbookThemeComponents(),

        // Visuals
        primaryGradient = Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFFF3E0),
                Color(0xFFFFE0B2),
                Color(0xFFFFCCBC)
            )
        ),
        cardBorder = Color(0xFF795548),
        primaryTextColor = Color(0xFF3E2723),
        secondaryTextColor = Color(0xFF5D4037),
        useGlassmorphism = false,
        glowIntensity = 0f,
        useSerifFont = true,
        recordButtonEmoji = "📝",
        scoreEmojis = mapOf(
            90 to "⭐",
            80 to "😊",
            70 to "👍",
            60 to "😐",
            0 to "😔"
        ),

        // M3 Overrides
        cardAlpha = 1f,
        shadowElevation = 0f,
        maxCardRotation = 3f,
        borderWidth = 3f,

        // Interaction
        dialogCopy = DialogCopy.default(),
        scoreFeedback = ScoreFeedback.default(),
        menuColors = MenuColors.fromColors(
            primaryText = Color(0xFF3E2723),
            secondaryText = Color(0xFF5D4037),
            border = Color(0xFF8B4513),
            gradient = Brush.verticalGradient(colors = listOf(Color(0xFFFFF3E0), Color(0xFFFFE0B2)))
        )
    )
}

class ScrapbookThemeComponents : ThemeComponents {

    companion object {
        private val _scrollVelocity = MutableStateFlow(0f)
        val scrollVelocity = _scrollVelocity.asStateFlow()

        // 🔹 NEW: event counter to force a pop every time
        private val _scrollPopId = MutableStateFlow(0L)
        val scrollPopId = _scrollPopId.asStateFlow()

        fun triggerScrollPop(velocity: Float) {
            val v = velocity.coerceIn(0f, 1f)
            _scrollVelocity.value = v

            // 🔹 Increment ID so LaunchedEffect always re-runs
            _scrollPopId.value = _scrollPopId.value + 1L
        }
    }


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

        // RESTORED COLORS: Lighter Orange
        val stickyNoteColor = Color(0xFFFFB74D)
        val contentColor = Color.Black.copy(alpha = 0.8f)

        val stableId = recording.originalPath.hashCode()
        val rotation = remember(stableId) { (stableId % 5 - 2).toFloat() }
        val tapeRotation1 = remember(stableId) { (stableId % 25 - 12).toFloat() }
        val tapeRotation2 = remember(stableId) { ((stableId * 13) % 25 - 12).toFloat() }
        val tornShape = remember(stableId) { TornPaperShape(stableId) }

        val isReady = recording.reversedPath != null

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp)
        ) {
            Box(modifier = Modifier.matchParentSize().offset(y = 4.dp).rotate(rotation).background(Color.Black.copy(alpha = 0.15f), tornShape))

            Card(
                modifier = Modifier.fillMaxWidth().rotate(rotation).border(1.dp, Color.White.copy(alpha = 0.6f), tornShape),
                colors = CardDefaults.cardColors(containerColor = stickyNoteColor),
                shape = tornShape,
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "🎤 ${recording.name}",
                            style = MaterialTheme.typography.titleMedium.copy(fontFamily = dancingScriptFontFamily, fontWeight = FontWeight.Bold, fontSize = 18.sp),
                            color = contentColor,
                            modifier = Modifier.weight(1f).clickable { showRenameDialog = true },
                            maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                        ScrapbookButton(onClick = { showDeleteDialog = true }, icon = Icons.Default.Delete, label = "Del", iconColor = Color(0xFFFF1744))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        ScrapbookButton(onClick = { showShareDialog = true }, icon = Icons.Default.Share, label = "Share", iconColor = Color(0xFF607D8B))

                        if (isPlaying && !isPaused) {
                            ScrapbookButton(onClick = onPause, icon = Icons.Default.Pause, label = "Pause", iconColor = Color(0xFF4CAF50))
                        } else {
                            ScrapbookButton(onClick = { onPlay(recording.originalPath) }, icon = Icons.Default.PlayArrow, label = "Play", iconColor = Color(0xFF2196F3))
                        }

                        ScrapbookButton(onClick = { recording.reversedPath?.let { onPlay(it) } }, icon = Icons.Default.FastForward, label = "Rev", iconColor = Color(0xFFFF9800), enabled = isReady)

                        if (isGameModeEnabled) {
                            ScrapbookButton(onClick = { onStartAttempt(recording, ChallengeType.FORWARD) }, icon = Icons.Default.Mic, label = "Try", iconColor = Color(0xFF9C27B0), enabled = isReady)
                            ScrapbookButton(onClick = { onStartAttempt(recording, ChallengeType.REVERSE) }, icon = Icons.Default.Mic, label = "REV", iconColor = Color(0xFFE91E63), enabled = isReady)
                        }
                    }

                    if (isPlaying) {
                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth(), color = Color(0xFF4CAF50))
                    }
                }
            }
            TapeCorner(Modifier.align(Alignment.TopStart), (-8).dp, (-8).dp, tapeRotation1)
            TapeCorner(Modifier.align(Alignment.TopEnd), 8.dp, (-8).dp, tapeRotation2)
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
        var showRenameDialog by remember { mutableStateOf(false) }
        var showDeleteDialog by remember { mutableStateOf(false) }
        var showShareDialog by remember { mutableStateOf(false) }
        var showScoreDialog by remember { mutableStateOf(false) }

        val isPlayingThis = currentlyPlayingPath == attempt.attemptFilePath || currentlyPlayingPath == attempt.reversedAttemptFilePath
        val stableId = attempt.attemptFilePath.hashCode()
        val stickyNoteColor = remember(stableId) {
            listOf(Color(0xFFFFF59D), Color(0xFFF8BBD9), Color(0xFFC8E6C9), Color(0xFFBBDEFB), Color(0xFFE1BEE7), Color(0xFFFFCCBC))[stableId.mod(6)]
        }
        val contentColor = Color.Black.copy(alpha = 0.8f)
        val rotation = remember(stableId) { (stableId % 7 - 3).toFloat() }
        val tapeRotation1 = remember(stableId) { (stableId % 31 - 15).toFloat() }
        val tapeRotation2 = remember(stableId) { ((stableId * 17) % 31 - 15).toFloat() }
        val tornShape = remember(stableId) { TornPaperShape(stableId + 100) }

        Box(modifier = Modifier.fillMaxWidth().padding(start = 32.dp, end = 16.dp, top = 12.dp, bottom = 12.dp)) {
            Box(modifier = Modifier.matchParentSize().offset(y = 3.dp).rotate(rotation).background(Color.Black.copy(alpha = 0.15f), tornShape))

            Card(
                modifier = Modifier.fillMaxWidth().rotate(rotation).border(1.dp, Color.White.copy(alpha = 0.5f), tornShape),
                colors = CardDefaults.cardColors(containerColor = stickyNoteColor),
                shape = tornShape,
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = attempt.playerName, style = MaterialTheme.typography.bodyLarge.copy(fontFamily = dancingScriptFontFamily, fontWeight = FontWeight.Bold), color = contentColor, modifier = Modifier.weight(1f).clickable { showRenameDialog = true })
                        Row(modifier = Modifier.clickable { showScoreDialog = true }, verticalAlignment = Alignment.CenterVertically) {
                            val fullStars = attempt.score.toInt() / 20
                            repeat(5) { index -> Icon(if (index < fullStars) Icons.Filled.Star else Icons.Outlined.StarBorder, null, tint = if (index < fullStars) Color(0xFFFFD700) else Color.Gray, modifier = Modifier.size(16.dp)) }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${attempt.score}%", style = MaterialTheme.typography.bodySmall.copy(fontFamily = dancingScriptFontFamily), color = Color.Black.copy(0.7f))
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Text(if (attempt.challengeType == ChallengeType.REVERSE) "🔄" else "▶️", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(attempt.difficulty.displayName.uppercase(), style = MaterialTheme.typography.labelSmall.copy(fontFamily = dancingScriptFontFamily, fontWeight = FontWeight.Bold), color = DifficultyConfig.getColorForDifficulty(attempt.difficulty), modifier = Modifier.background(Color.Black.copy(0.1f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (onJumpToParent != null) ScrapbookButton(onClick = onJumpToParent, icon = Icons.Default.Home, label = "Original", iconColor = Color(0xFF607D8B))
                        if (onShareAttempt != null) ScrapbookButton(onClick = { showShareDialog = true }, icon = Icons.Default.Share, label = "Share", iconColor = Color(0xFF9C27B0))
                        ScrapbookButton(onClick = { if (isPlayingThis && !isPaused) onPause() else onPlay(attempt.attemptFilePath) }, icon = if (isPlayingThis && !isPaused) Icons.Default.Pause else Icons.Default.PlayArrow, label = if (isPlayingThis && !isPaused) "Pause" else "Play", iconColor = if (isPlayingThis && !isPaused) Color(0xFF4CAF50) else Color(0xFF2196F3))
                        if (attempt.reversedAttemptFilePath != null) ScrapbookButton(onClick = { onPlay(attempt.reversedAttemptFilePath) }, icon = Icons.Default.FastForward, label = "Rev", iconColor = Color(0xFFFF9800))
                        if (onDeleteAttempt != null) ScrapbookButton(onClick = { showDeleteDialog = true }, icon = Icons.Default.Delete, label = "Del", iconColor = Color(0xFFFF1744))
                    }
                    if (isPlayingThis) { Spacer(modifier = Modifier.height(8.dp)); LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth(), color = Color(0xFF4CAF50)) }
                }
            }
            TapeCorner(Modifier.align(Alignment.TopStart), (-8).dp, (-8).dp, tapeRotation1)
            TapeCorner(Modifier.align(Alignment.TopEnd), 8.dp, (-8).dp, tapeRotation2)
        }

        if (showRenameDialog && onRenamePlayer != null) RenameDialog(RenamableItemType.PLAYER, attempt.playerName, aesthetic, { onRenamePlayer(attempt, it) }, { showRenameDialog = false })
        if (showDeleteDialog && onDeleteAttempt != null) DeleteDialog(DeletableItemType.ATTEMPT, attempt, aesthetic, { onDeleteAttempt(attempt) }, { showDeleteDialog = false })
        if (showShareDialog && onShareAttempt != null) ShareDialog(null, attempt, aesthetic, onShareAttempt, { showShareDialog = false })
        if (showScoreDialog) ScoreCard(attempt, aesthetic, { showScoreDialog = false })
    }

    /*@Composable
    override fun RecordButton(isRecording: Boolean, isProcessing: Boolean, aesthetic: AestheticThemeData, onStartRecording: () -> Unit, onStopRecording: () -> Unit) {
        FloatingActionButton(
            onClick = { if (isRecording) onStopRecording() else onStartRecording() }, modifier = Modifier.size(72.dp),
            containerColor = if (isRecording) Color(0xFFFF6B6B) else Color(0xFFFF8C00), contentColor = Color.White
        ) { Icon(imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic, contentDescription = "Record", modifier = Modifier.size(32.dp)) }
    }*/

    @Composable
    override fun RecordButton(
        isRecording: Boolean,
        isProcessing: Boolean,
        aesthetic: AestheticThemeData,
        onStartRecording: () -> Unit,
        onStopRecording: () -> Unit
    ) {
        // SVG booklet as the base
        val notebookPainter = painterResource(id = R.drawable.spiral_notebook)

        // Subtle scale pulse when recording
        val scale by animateFloatAsState(
            targetValue = if (isRecording) 1.05f else 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
            label = "record_booklet_scale"
        )

        Box(
            modifier = Modifier
                .size(140.dp) // tweak to taste
                .graphicsLayer(scaleX = scale, scaleY = scale)
                .clickable {
                    if (isRecording) onStopRecording() else onStartRecording()
                },
            contentAlignment = Alignment.Center
        ) {
            // The booklet SVG
            Image(
                painter = notebookPainter,
                contentDescription = if (isRecording) "Stop recording" else "Start recording",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Bottom-right REC / STOP pill on top of the booklet
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(10.dp)
                    .background(
                        color = Color(0xCC000000),
                        shape = RoundedCornerShape(50)
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(
                            color = if (isRecording) Color(0xFFFF5252) else Color(0xFFB0BEC5),
                            shape = CircleShape
                        )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isRecording) "STOP" else "REC",
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }



    @Composable
    override fun AppBackground(aesthetic: AestheticThemeData, content: @Composable () -> Unit) {
        val audioViewModel: AudioViewModel = hiltViewModel()
        // Direct access to the real recording state
        val isRecording by audioViewModel.recordingState.collectAsState()
        val amplitude by audioViewModel.amplitudeState.collectAsState()

        SwirlingPastelBackground(
            audioLevel = amplitude,
            isRecording = isRecording,
            content = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectDragGestures { change, _ ->
                                val velocity = kotlin.math.sqrt(
                                    change.position.x * change.position.x +
                                            change.position.y * change.position.y
                                ) / 100f
                                if (velocity > 0.2f) {
                                    triggerScrollPop(1f)
                                }
                            }
                        }
                ) {
                    content()
                }
            }
        )
    }

    // --- DIALOGS ---
    @Composable override fun ScoreCard(attempt: PlayerAttempt, aesthetic: AestheticThemeData, onDismiss: () -> Unit) { ScoreExplanationDialog(attempt, onDismiss) }
    @Composable override fun DeleteDialog(itemType: DeletableItemType, item: Any, aesthetic: AestheticThemeData, onConfirm: () -> Unit, onDismiss: () -> Unit) {
        val copy = aesthetic.dialogCopy; val name = if (item is Recording) item.name else if (item is PlayerAttempt) item.playerName else "Item"
        AlertDialog(onDismissRequest = onDismiss, containerColor = Color(0xFFFFF3E0), title = { Text(copy.deleteTitle(itemType), fontFamily = dancingScriptFontFamily, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color(0xFF3E2723)) }, text = { Text(copy.deleteMessage(itemType, name), fontFamily = dancingScriptFontFamily, fontSize = 18.sp, color = Color(0xFF5D4037)) }, confirmButton = { Button(onClick = { onConfirm(); onDismiss() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722))) { Text(copy.deleteConfirmButton, fontFamily = dancingScriptFontFamily) } }, dismissButton = { TextButton(onClick = onDismiss) { Text(copy.deleteCancelButton, fontFamily = dancingScriptFontFamily, color = Color(0xFF795548)) } })
    }
    @Composable override fun ShareDialog(recording: Recording?, attempt: PlayerAttempt?, aesthetic: AestheticThemeData, onShare: (String) -> Unit, onDismiss: () -> Unit) {
        val copy = aesthetic.dialogCopy
        AlertDialog(onDismissRequest = onDismiss, containerColor = Color(0xFFFFF3E0), title = { Text(copy.shareTitle, fontFamily = dancingScriptFontFamily, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color(0xFF3E2723)) }, text = { Column { Text(copy.shareMessage, fontFamily = dancingScriptFontFamily, fontSize = 18.sp, color = Color(0xFF5D4037)); Spacer(modifier = Modifier.height(16.dp)); val path = recording?.originalPath ?: attempt?.attemptFilePath ?: ""; Button(onClick = { onShare(path); onDismiss() }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8D6E63))) { Text("Share Original 🎤", fontFamily = dancingScriptFontFamily) }; val revPath = recording?.reversedPath ?: attempt?.reversedAttemptFilePath; if (revPath != null) { Spacer(modifier = Modifier.height(8.dp)); Button(onClick = { onShare(revPath); onDismiss() }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB74D))) { Text("Share Reversed 🔁", fontFamily = dancingScriptFontFamily) } } } }, confirmButton = {}, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", fontFamily = dancingScriptFontFamily, color = Color(0xFF795548)) } })
    }
    @Composable override fun RenameDialog(itemType: RenamableItemType, currentName: String, aesthetic: AestheticThemeData, onRename: (String) -> Unit, onDismiss: () -> Unit) {
        var name by remember { mutableStateOf(currentName) }; val copy = aesthetic.dialogCopy
        AlertDialog(onDismissRequest = onDismiss, containerColor = Color(0xFFFFF3E0), title = { Text(copy.renameTitle(itemType), fontFamily = dancingScriptFontFamily, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color(0xFF3E2723)) }, text = { OutlinedTextField(value = name, onValueChange = { name = it }, singleLine = true, label = { Text(copy.renameHint, fontFamily = dancingScriptFontFamily) }, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF8D6E63), focusedLabelColor = Color(0xFF8D6E63))) }, confirmButton = { Button(onClick = { onRename(name); onDismiss() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8D6E63))) { Text("Save 💾", fontFamily = dancingScriptFontFamily) } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", fontFamily = dancingScriptFontFamily, color = Color(0xFF795548)) } })
    }

    @Composable
    private fun ScrapbookButton(onClick: () -> Unit, icon: ImageVector, label: String, iconColor: Color, enabled: Boolean = true) {
        val alpha = if (enabled) 1f else 0.4f
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(enabled = enabled) { onClick() }.padding(4.dp).alpha(alpha)) {
            Box(modifier = Modifier.size(40.dp).background(Color.White, RoundedCornerShape(4.dp)).border(2.dp, Color.Gray.copy(0.6f), RoundedCornerShape(4.dp)), contentAlignment = Alignment.Center) { Icon(icon, label, tint = iconColor, modifier = Modifier.size(24.dp)) }
            Spacer(modifier = Modifier.height(3.dp))
            Text(text = label, style = MaterialTheme.typography.labelSmall.copy(fontFamily = dancingScriptFontFamily, fontWeight = FontWeight.Medium, fontSize = 12.sp), color = Color.Black.copy(0.8f))
        }
    }

    @Composable
    private fun TapeCorner(modifier: Modifier, offsetX: Dp, offsetY: Dp, rotation: Float) {
        Box(modifier = modifier.offset(offsetX, offsetY).rotate(rotation).size(24.dp, 16.dp).background(Color.Gray.copy(0.5f), RoundedCornerShape(2.dp)))
    }
}



// --- 🌀 SWIRLING BACKGROUND ENGINE (FIXED LOOP & Z-INDEX) ---
// --- 🌀 SWIRLING BACKGROUND ENGINE ---
@Composable
private fun SwirlingPastelBackground(
    audioLevel: Float,
    isRecording: Boolean,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val config = LocalConfiguration.current
    val screenWidth = with(density) { config.screenWidthDp.dp.toPx() }
    val screenHeight = with(density) { config.screenHeightDp.dp.toPx() }

    // Animation State
    var time by remember { mutableFloatStateOf(0f) }
    val currentAudioLevel by rememberUpdatedState(audioLevel)
    val currentIsRecording by rememberUpdatedState(isRecording)

    // Scroll-triggered saturation
    val scrollSaturation by ScrapbookThemeComponents.scrollVelocity.collectAsState()

    val scrollSaturationAnimated = remember { Animatable(0f) }

    // 🔹 NEW: event ID – bumps every time triggerScrollPop() is called
    val scrollPopId by ScrapbookThemeComponents.scrollPopId.collectAsState()


    // Scroll decay animation - START at peak, then fade
    // Scroll pop: big burst → hold → smooth fade
    LaunchedEffect(scrollPopId) {
        if (scrollSaturation > 0f) {

            // 1) Snap to full pop instantly
            scrollSaturationAnimated.snapTo(1.0f)

            // 2) HOLD the pop so it's visible (250ms flash)
            delay(1000)

            // 3) Then fade out smoothly
            scrollSaturationAnimated.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = 3000,
                    easing = EaseOutCubic
                )
            )
        }
    }


    // Combined saturation (recording OR scroll)
    val recordingSaturation = if (isRecording) 1f else 0f
    // Pop is MUCH stronger (3×), but still capped at 1.0
    val boostedScroll = (scrollSaturationAnimated.value * 3f).coerceAtMost(1f)
    val targetSaturation = maxOf(recordingSaturation, boostedScroll)



    // Instant animation for scroll pop, smooth for recording changes
    val isScrollActive = scrollSaturationAnimated.value > 0f
    val saturation by animateFloatAsState(
        targetValue = targetSaturation,
        animationSpec = if (isScrollActive) tween(0) else tween(500), // Instant for scroll!
        label = "saturation"
    )

    // Drive the loop
    LaunchedEffect(audioLevel, isRecording) {
        while (isActive) {
            withFrameNanos {
                // Speed up time based on recording state
                val speed = if (currentIsRecording) 0.1f + (currentAudioLevel * 0.5f) else 0.02f
                time += speed
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFFFF3E0))) {

        // Layer 1: Blobs (Interpolated Colors)
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Helper to mix colors based on saturation state

            fun mix(c1: Color, c2: Color) = lerp(c1, c2, saturation)

            val blobs = listOf(
                mix(AuroraPink, VibrantPink) to 0.2f,
                mix(AuroraCyan, VibrantCyan) to 0.5f,
                mix(AuroraLavender, VibrantLavender) to 0.8f,
                mix(AuroraPeach, VibrantPeach) to 1.1f,
                mix(AuroraMint, VibrantMint) to 1.4f,
                mix(AuroraPink, VibrantPink) to 1.7f
            )

            blobs.forEachIndexed { i, (color, offset) ->
                val motionFactor = if (isScrollActive && !isRecording) 1.2f else 0.7f

                val driftX = sin(time * 0.02f + offset + i) * (screenWidth * motionFactor)
                val driftY = cos(time * 0.015f + offset + i) * (screenHeight * motionFactor)

                val x = (screenWidth / 2) + driftX
                val y = (screenHeight / 2) + driftY

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            color.copy(
                                alpha = when {
                                    isRecording -> 0.9f          // strong while recording
                                    isScrollActive -> 0.9f       // equally strong during scroll POP
                                    else -> 0.35f
                                }
                            ),
                            Color.Transparent
                        ),
                        center = Offset(x, y),
                        radius = screenWidth * if (isScrollActive) 1.4f else 1.2f
                    ),
                    center = Offset(x, y),
                    radius = screenWidth * if (isScrollActive) 1.4f else 1.2f
                )

            }
        }

        // Layer 2: Content
        content()

        // Layer 3: Sparkles (Force visible during recording)
        if (isRecording) {
            val sparkleCount = 5 + (audioLevel * 20).toInt()
            // Seed changes slowly so sparkles don't jitter frantically
            val seed = (time / 5).toInt()
            val random = Random(seed)

            Canvas(modifier = Modifier.fillMaxSize().zIndex(10f)) {
                repeat(sparkleCount) {
                    val sx = random.nextFloat() * screenWidth
                    val sy = random.nextFloat() * screenHeight
                    val size = 20f + random.nextFloat() * 30f

                    // High contrast colors
                    val sparkleColor = if (random.nextBoolean()) Color(0xFFFFD700) else Color(0xFFFF00FF) // Gold or Neon Pink

                    // Thick strokes for visibility
                    drawLine(sparkleColor, Offset(sx - size, sy), Offset(sx + size, sy), strokeWidth = 8f, cap = StrokeCap.Round)
                    drawLine(sparkleColor, Offset(sx, sy - size), Offset(sx, sy + size), strokeWidth = 8f, cap = StrokeCap.Round)
                    drawCircle(Color.White, radius = size * 0.4f, center = Offset(sx, sy))
                }
            }
        }
    }
}

class TornPaperShape(private val seed: Int) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path(); val random = Random(seed); val roughness = 4f; val frequency = 10f
        path.moveTo(0f, 0f)
        var x = 0f; while (x < size.width) { x += frequency; path.lineTo(x, (random.nextFloat() - 0.5f) * roughness) }
        var y = 0f; while (y < size.height) { y += frequency; path.lineTo(size.width + (random.nextFloat() - 0.5f) * roughness, y) }
        x = size.width; while (x > 0) { x -= frequency; path.lineTo(x, size.height + (random.nextFloat() - 0.5f) * roughness) }
        y = size.height; while (y > 0) { y -= frequency; path.lineTo((random.nextFloat() - 0.5f) * roughness, y) }
        path.close()
        return Outline.Generic(path)
    }
}