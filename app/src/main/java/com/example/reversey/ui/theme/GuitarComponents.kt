package com.example.reversey.ui.theme

import android.media.MediaPlayer
import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.reversey.R
import com.example.reversey.data.models.ChallengeType
import com.example.reversey.data.models.PlayerAttempt
import com.example.reversey.data.models.Recording
import com.example.reversey.ui.components.DifficultySquircle
import com.example.reversey.ui.components.ScoreExplanationDialog
import kotlinx.coroutines.delay
import kotlin.random.Random

// Rainbow colors for excited notes
private val rainbowColors = listOf(
    Color(0xFFFF0000), // Red
    Color(0xFFFF7F00), // Orange
    Color(0xFFFFFF00), // Yellow
    Color(0xFF00FF00), // Green
    Color(0xFF0000FF), // Blue
    Color(0xFF4B0082), // Indigo
    Color(0xFF9400D3)  // Violet
)

/**
 * 🎸 GUITAR ACOUSTIC THEME
 * Taylor Swift Folklore/Evermore inspired theme.
 */
object GuitarTheme {
    const val THEME_ID = "guitar"

    val data = AestheticThemeData(
        id = THEME_ID,
        name = "Guitar Acoustic",
        description = "🎸 Taylor Swift Folklore vibes for CPD!",
        components = GuitarComponents(),

        // Visuals
        primaryGradient = Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFFF8E1),  // Cream
                Color(0xFFFFF3E0),  // Warm beige
                Color(0xFFFFE0B2),  // Soft peach
                Color(0xFFFFDFC1)   // Peachy bottom
            )
        ),
        cardBorder = Color(0xFF5d4a36),
        primaryTextColor = Color(0xFF3E2723),
        secondaryTextColor = Color(0xFF5D4037),
        useGlassmorphism = false,
        glowIntensity = 0f,
        useSerifFont = true,
        useWideLetterSpacing = false,
        recordButtonEmoji = "🎸",
        scoreEmojis = mapOf(
            90 to "⭐",
            80 to "🎵",
            70 to "🎶",
            60 to "🎤",
            0 to "🎻"
        ),

        // M3 Overrides
        cardAlpha = 1f,
        shadowElevation = 6f,
        maxCardRotation = 0f,
        borderWidth = 3f,

        // Interaction
        dialogCopy = DialogCopy(
            deleteTitle = { type -> if (type == DeletableItemType.RECORDING) "Retire Track?" else "Scrap Take?" },
            deleteMessage = { type, name -> "Are you sure you want to delete '$name'? The melody will be lost." },
            deleteConfirmButton = "Delete",
            deleteCancelButton = "Keep Playing",
            shareTitle = "Share Session 🎸",
            shareMessage = "How would you like to share this track?",
            renameTitle = { "Title Track 🎵" },
            renameHint = "New Song Title"
        ),
        scoreFeedback = ScoreFeedback(
            getTitle = { score ->
                when {
                    score >= 90 -> "Standing Ovation!"
                    score >= 80 -> "Chart Topper!"
                    score >= 70 -> "Radio Ready!"
                    score >= 60 -> "Good Demo!"
                    else -> "Rehearsal Needed"
                }
            },
            getMessage = { score ->
                when {
                    score >= 90 -> "A platinum record performance!"
                    score >= 80 -> "The crowd goes wild!"
                    score >= 70 -> "Solid rhythm and flow."
                    score >= 60 -> "Keep practicing your scales."
                    else -> "Don't fret, try again!"
                }
            },
            getEmoji = { score ->
                when {
                    score >= 90 -> "🤩"
                    score >= 80 -> "🎸"
                    score >= 70 -> "🎵"
                    score >= 60 -> "🎶"
                    else -> "🎻"
                }
            }
        ),
        menuColors = MenuColors.fromColors(
            primaryText = Color(0xFF3E2723),
            secondaryText = Color(0xFF5D4037),
            border = Color(0xFF5d4a36),
            gradient = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFFFF8E1),
                    Color(0xFFFFF3E0)
                )
            )
        )
    )
}

class GuitarComponents : ThemeComponents {

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
        GuitarRecordingItem(
            recording = recording,
            isPlaying = isPlaying,
            isPaused = isPaused,
            progress = progress,
            onPlay = onPlay,
            onPause = onPause,
            onStop = onStop,
            onDelete = onDelete,
            onShare = onShare,
            onRename = onRename,
            isGameModeEnabled = isGameModeEnabled,
            onStartAttempt = onStartAttempt
        )
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
        GuitarAttemptItem(
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

    @Composable
    override fun RecordButton(
        isRecording: Boolean,
        isProcessing: Boolean,
        aesthetic: AestheticThemeData,
        onStartRecording: () -> Unit,
        onStopRecording: () -> Unit
    ) {
        GuitarRecordButton(
            isRecording = isRecording,
            onClick = {
                if (isRecording) {
                    onStopRecording()
                } else {
                    onStartRecording()
                }
            }
        )
    }

    @Composable
    override fun AppBackground(
        aesthetic: AestheticThemeData,
        content: @Composable () -> Unit
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(aesthetic.primaryGradient)
        ) {
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
        val darkBrown = Color(0xFF5d4a36)
        val peachOrange = Color(0xFFE8A87C)

        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = Color(0xFFC4B4A0),
            title = { Text(copy.deleteTitle(itemType), fontFamily = FontFamily.Serif, color = darkBrown, fontWeight = FontWeight.Bold) },
            text = { Text(copy.deleteMessage(itemType, name), color = darkBrown) },
            confirmButton = {
                TextButton(onClick = { onConfirm(); onDismiss() }) {
                    Text(copy.deleteConfirmButton, color = peachOrange, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(copy.deleteCancelButton, color = darkBrown)
                }
            }
        )
    }

    @Composable
    override fun ShareDialog(recording: Recording?, attempt: PlayerAttempt?, aesthetic: AestheticThemeData, onShare: (String) -> Unit, onDismiss: () -> Unit) {
        val copy = aesthetic.dialogCopy
        val darkBrown = Color(0xFF5d4a36)
        val tealGreen = Color(0xFF7DB9A8)
        val peachOrange = Color(0xFFE8A87C)

        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = Color(0xFFC4B4A0),
            title = { Text(copy.shareTitle, fontFamily = FontFamily.Serif, color = darkBrown, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(copy.shareMessage, color = darkBrown)
                    Spacer(modifier = Modifier.height(16.dp))
                    val path = recording?.originalPath ?: attempt?.attemptFilePath ?: ""
                    TextButton(onClick = { onShare(path); onDismiss() }) {
                        Text("Share Original (Forward)", color = tealGreen, fontWeight = FontWeight.Bold)
                    }
                    val revPath = recording?.reversedPath ?: attempt?.reversedAttemptFilePath
                    if (revPath != null) {
                        TextButton(onClick = { onShare(revPath); onDismiss() }) {
                            Text("Share Reversed", color = peachOrange, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = darkBrown) } }
        )
    }

    @Composable
    override fun RenameDialog(itemType: RenamableItemType, currentName: String, aesthetic: AestheticThemeData, onRename: (String) -> Unit, onDismiss: () -> Unit) {
        var name by remember { mutableStateOf(currentName) }
        val copy = aesthetic.dialogCopy
        val darkBrown = Color(0xFF5d4a36)
        val tealGreen = Color(0xFF7DB9A8)

        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = Color(0xFFC4B4A0),
            title = { Text(copy.renameTitle(itemType), fontFamily = FontFamily.Serif, color = darkBrown, fontWeight = FontWeight.Bold) },
            text = {
                TextField(
                    value = name, onValueChange = { name = it },
                    label = { Text(copy.renameHint) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(0.5f),
                        unfocusedContainerColor = Color.White.copy(0.3f)
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = { onRename(name); onDismiss() }) {
                    Text("Save", color = tealGreen, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = darkBrown) } }
        )
    }
}

// ============================================
// 🎸 GUITAR COMPONENTS IMPLEMENTATION
// ============================================

@Composable
fun FloatingMusicNote(
    note: String,
    position: Offset,
    delay: Float,
    isExcited: Boolean = false,
    rainbowMode: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "float")

    val swayAmount = if (isExcited) 15f else 8f
    val swayDuration = if (isExcited) 600 else 3000

    val offsetX by infiniteTransition.animateFloat(
        initialValue = position.x,
        targetValue = position.x + swayAmount,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = swayDuration,
                delayMillis = (delay * 1000).toInt(),
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sway"
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = if (isExcited) 0f else -5f,
        targetValue = if (isExcited) 360f else 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isExcited) 400 else 2000,
                easing = if (isExcited) LinearEasing else FastOutSlowInEasing
            ),
            repeatMode = if (isExcited) RepeatMode.Restart else RepeatMode.Reverse
        ),
        label = "tilt"
    )

    val colorIndex = ((position.x + position.y) / 50f).toInt() % rainbowColors.size
    val noteColor = if (rainbowMode) rainbowColors[colorIndex] else Color(0xFF5d4a36)

    Text(
        text = note,
        fontSize = if (isExcited) 22.sp else 18.sp,
        color = noteColor,
        modifier = Modifier
            .offset(x = offsetX.dp, y = position.y.dp)
            .rotate(rotation)
    )
}

@Composable
fun GuitarRecordButton(
    isRecording: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isStrumming by remember { mutableStateOf(false) }
    var strummedNotesCount by remember { mutableStateOf(0) }
    val infiniteTransition = rememberInfiniteTransition(label = "strum")
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    LaunchedEffect(isStrumming) {
        if (isStrumming) {
            delay(10000)
            isStrumming = false
            strummedNotesCount = 0
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (isRecording) 1.05f else 1f,
        animationSpec = tween(200),
        label = "scale"
    )

    val pulseAlpha by animateFloatAsState(
        targetValue = if (isRecording) 0.6f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = modifier
            .width(280.dp)
            .height(140.dp)
            .scale(scale)
            .pointerInput(isRecording) {
                if (!isRecording) {
                    detectDragGestures(
                        onDragStart = { offset -> },
                        onDrag = { change, dragAmount -> change.consume() },
                        onDragEnd = {
                            isStrumming = true
                            strummedNotesCount = 20
                            try {
                                mediaPlayer?.release()
                                mediaPlayer = MediaPlayer.create(context, R.raw.e_chord)
                                mediaPlayer?.setOnCompletionListener { mp ->
                                    mp.release()
                                    mediaPlayer = null
                                }
                                mediaPlayer?.start()
                            } catch (e: Exception) {
                                Log.e("GuitarStrum", "Error: ${e.message}")
                            }
                        }
                    )
                }
            }
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() },
        contentAlignment = Alignment.TopCenter
    ) {
        val musicNotes = listOf(
            Triple("♪", Offset(20f, 20f), 0f),
            Triple("♬", Offset(35f, 35f), 0.5f),
            Triple("♫", Offset(55f, 15f), 1f),
            Triple("♪", Offset(75f, 20f), 1.5f),
            Triple("♬", Offset(105f, 12f), 0.3f),
            Triple("♫", Offset(145f, 18f), 0.8f),
            Triple("♪", Offset(5f, 26f), 1.2f),
            Triple("♬", Offset(15f, 33f), 0.6f),
            Triple("♫", Offset(10f, 75f), 1.4f),
            Triple("♪", Offset(20f, 101f), 0.2f),
            Triple("♬", Offset(8f, 120f), 0.9f),
            Triple("♫", Offset(165f, 5f), 0.4f),
            Triple("♪", Offset(170f, 30f), 1.1f),
            Triple("♬", Offset(175f, 77f), 0.7f),
            Triple("♫", Offset(168f, 100f), 1.3f),
            Triple("♪", Offset(172f, 130f), 0.1f),
            Triple("♬", Offset(10f, 110f), 1.6f),
            Triple("♫", Offset(45f, 90f), 0.35f),
            Triple("♪", Offset(100f, 130f), 0.85f),
            Triple("♬", Offset(110f, 100f), 1.45f),
            Triple("♫", Offset(140f, 80f), 0.65f),
            Triple("♪", Offset(80f, 80f), 1.25f),
            Triple("♬", Offset(0f, 140f), 0.55f),
            Triple("♫", Offset(140f, 100f), 0.95f),
            Triple("♪", Offset(95f, 90f), 1.35f)
        )

        musicNotes.forEach { (note, position, delay) ->
            FloatingMusicNote(
                note = note,
                position = position,
                delay = delay,
                isExcited = isStrumming,
                rainbowMode = isStrumming
            )
        }

        if (isStrumming) {
            repeat(strummedNotesCount) { index ->
                val randomX = Random.nextFloat() * 340f - 300f
                val randomY = Random.nextFloat() * 340f - 0f
                val randomDelay = Random.nextFloat() * 2f
                val randomNote = listOf("♪", "♬", "♫").random()

                ExcitedRainbowNote(
                    note = randomNote,
                    startPosition = Offset(randomX, randomY),
                    colorIndex = index % rainbowColors.size
                )
            }
        }

        // Guitar drawing
        Box(
            modifier = Modifier
                .width(110.dp)
                .height(200.dp)
                .offset(x = (-24).dp, y = (-30).dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                drawContext.canvas.save()
                drawContext.transform.rotate(80f, pivot = Offset(canvasWidth / 2, canvasHeight / 2))

                val svgWidth = 382f
                val svgHeight = 630f
                val scaleX = canvasWidth / svgWidth
                val scaleY = canvasHeight / svgHeight

                fun scaleX(x: Float): Float = (x - 166f) * scaleX
                fun scaleY(y: Float): Float = (y - 400f) * scaleY
                fun scalePoint(x: Float, y: Float) = Offset(scaleX(x), scaleY(y))

                val woodBrown = Color(0x998B4513)
                val darkBrown = Color(0xFF5d4a36)
                val soundHoleCream = Color(0xFFF5F1E8)
                val goldenPegs = Color(0xFFFFE400)
                val stripePurple = Color(0xFFB8A8C8)
                val stripeOrange = Color(0xFFE8A87C)
                val stripeTeal = Color(0xFF7DB9A8)

                val neckRect = androidx.compose.ui.geometry.Rect(
                    scalePoint(335f, 90f),
                    scalePoint(379f, 555f)
                )

                drawRect(color = woodBrown, topLeft = neckRect.topLeft, size = neckRect.size)
                drawRect(color = darkBrown, topLeft = neckRect.topLeft, size = neckRect.size, style = Stroke(width = 2.dp.toPx()))

                val headstockPath = Path().apply {
                    moveTo(scaleX(335f), scaleY(200f))
                    lineTo(scaleX(330f), scaleY(90f))
                    lineTo(scaleX(384f), scaleY(90f))
                    lineTo(scaleX(379f), scaleY(200f))
                    close()
                }
                drawPath(headstockPath, color = woodBrown)
                drawPath(headstockPath, color = darkBrown, style = Stroke(width = 2.dp.toPx()))

                listOf(220f, 255f, 290f, 325f, 360f, 395f, 430f, 465f, 500f, 530f, 550f).forEach { y ->
                    drawLine(color = darkBrown, start = scalePoint(335f, y), end = scalePoint(379f, y), strokeWidth = 1.5.dp.toPx())
                }

                listOf(
                    330f to 110f, 330f to 180f, 330f to 145f,
                    380f to 110f, 380f to 180f, 380f to 145f,
                ).forEach { (x, y) ->
                    drawCircle(color = goldenPegs, radius = 2.dp.toPx(), center = scalePoint(x, y))
                }

                listOf(345f,350f, 355f, 360f, 365f,370f).forEach { x ->
                    drawLine(color = darkBrown.copy(alpha = 0.7f), start = scalePoint(x, 160f), end = scalePoint(x, 1030f), strokeWidth = 1.dp.toPx())
                }

                val guitarBodyPath = Path().apply {
                    moveTo(scaleX(351.936f), scaleY(553.04658f))
                    cubicTo(scaleX(228.59838f), scaleY(554.76093f), scaleX(209.69151f), scaleY(578.67358f), scaleX(209.69151f), scaleY(620.67456f))
                    cubicTo(scaleX(209.69149f), scaleY(681.43044f), scaleX(222.0788f), scaleY(677.13503f), scaleX(220.05686f), scaleY(726.85048f))
                    cubicTo(scaleX(218.25094f), scaleY(771.25513f), scaleX(167.1142f), scaleY(842.64339f), scaleX(166.11718f), scaleY(910.19743f))
                    cubicTo(scaleX(166.10737f), scaleY(910.86212f), scaleX(166.07731f), scaleY(911.52479f), scaleX(166.07731f), scaleY(912.1887f))
                    cubicTo(scaleX(166.07731f), scaleY(912.37991f), scaleX(166.0767f), scaleY(912.56168f), scaleX(166.07731f), scaleY(912.75227f))
                    cubicTo(scaleX(166.0761f), scaleY(912.96527f), scaleX(166.07731f), scaleY(913.17806f), scaleX(166.07731f), scaleY(913.39097f))
                    cubicTo(scaleX(166.08697f), scaleY(914.80906f), scaleX(166.11398f), scaleY(916.21556f), scaleX(166.15705f), scaleY(917.59894f))
                    cubicTo(scaleX(166.34207f), scaleY(924.64759f), scaleX(166.95918f), scaleY(931.25469f), scaleX(168.03078f), scaleY(937.43648f))
                    cubicTo(scaleX(168.04993f), scaleY(937.55015f), scaleX(168.0911f), scaleY(937.66122f), scaleX(168.11051f), scaleY(937.77462f))
                    cubicTo(scaleX(168.33277f), scaleY(939.03533f), scaleX(168.57095f), scaleY(940.2682f), scaleX(168.82812f), scaleY(941.49416f))
                    cubicTo(scaleX(168.88861f), scaleY(941.7927f), scaleX(168.92519f), scaleY(942.09936f), scaleX(168.98758f), scaleY(942.39586f))
                    cubicTo(scaleX(169.19522f), scaleY(943.34788f), scaleX(169.43683f), scaleY(944.28258f), scaleX(169.66532f), scaleY(945.21369f))
                    cubicTo(scaleX(169.87126f), scaleY(946.0859f), scaleX(170.08048f), scaleY(946.95206f), scaleX(170.30318f), scaleY(947.8061f))
                    cubicTo(scaleX(191.18154f), scaleY(1027.8721f), scaleX(285.02482f), scaleY(1028.55f), scaleX(348.18853f), scaleY(1028.5464f))
                    cubicTo(scaleX(349.41853f), scaleY(1028.5463f), scaleX(350.73012f), scaleY(1028.5464f), scaleX(351.936f), scaleY(1028.5464f))
                    cubicTo(scaleX(355.39903f), scaleY(1028.5484f), scaleX(358.88595f), scaleY(1028.5464f), scaleX(362.14188f), scaleY(1028.5464f))
                    cubicTo(scaleX(363.41347f), scaleY(1028.5457f), scaleX(364.59299f), scaleY(1028.5495f), scaleX(365.88935f), scaleY(1028.5464f))
                    cubicTo(scaleX(429.05306f), scaleY(1028.55f), scaleX(522.89635f), scaleY(1027.8721f), scaleX(543.77469f), scaleY(947.8061f))
                    cubicTo(scaleX(544.15335f), scaleY(946.35404f), scaleX(544.52115f), scaleY(944.87747f), scaleX(544.8511f), scaleY(943.37271f))
                    cubicTo(scaleX(545.03608f), scaleY(942.52906f), scaleX(545.19998f), scaleY(941.67832f), scaleX(545.36936f), scaleY(940.81788f))
                    cubicTo(scaleX(546.82341f), scaleY(933.67372f), scaleX(547.70152f), scaleY(925.95355f), scaleX(547.92083f), scaleY(917.59894f))
                    cubicTo(scaleX(547.97045f), scaleY(916.00565f), scaleX(547.99535f), scaleY(914.39165f), scaleX(548.00057f), scaleY(912.75227f))
                    cubicTo(scaleX(547.99571f), scaleY(911.90008f), scaleX(547.98468f), scaleY(911.05122f), scaleX(547.9607f), scaleY(910.19743f))
                    cubicTo(scaleX(546.96368f), scaleY(842.64339f), scaleX(495.93495f), scaleY(773.91053f), scaleX(494.02102f), scaleY(726.85048f))
                    cubicTo(scaleX(492.10709f), scaleY(679.79043f), scaleX(513.09095f), scaleY(639.23367f), scaleX(500.04323f), scaleY(615.15984f))
                    cubicTo(scaleX(486.407f), scaleY(590.0002f), scaleX(460.66471f), scaleY(587.59929f), scaleX(428.32564f), scaleY(612.6618f))
                    cubicTo(scaleX(425.00447f), scaleY(615.23568f), scaleX(383.4949f), scaleY(640.06295f), scaleX(385.32321f), scaleY(560.89991f))
                    cubicTo(scaleX(369.47384f), scaleY(558.74568f), scaleX(353.6854f), scaleY(553.07089f), scaleX(351.936f), scaleY(553.04658f))
                    close()
                }

                drawContext.canvas.save()
                drawContext.canvas.clipPath(guitarBodyPath)
                val stripeRotation = 45f
                drawContext.transform.rotate(stripeRotation, pivot = Offset(canvasWidth / 2, canvasHeight / 2))

                var x = -canvasWidth * 2
                var colorIndex = 0
                val colors = listOf(stripePurple, stripeOrange, stripeTeal)
                val stripeWidth = 6.67.dp.toPx()

                while (x < canvasWidth * 3) {
                    drawRect(color = colors[colorIndex % 3], topLeft = Offset(x, -canvasHeight * 2), size = Size(stripeWidth, canvasHeight * 4))
                    x += stripeWidth
                    colorIndex++
                }
                drawContext.canvas.restore()

                drawPath(guitarBodyPath, color = darkBrown, style = Stroke(width = 2.dp.toPx()))

                val soundHoleCenter = scalePoint(357f, 720f)
                val soundHoleRadius = 21.6.dp.toPx()
                drawCircle(color = darkBrown, radius = soundHoleRadius, center = soundHoleCenter, style = Stroke(width = 2.dp.toPx()))
                drawCircle(color = soundHoleCream.copy(alpha = 0.9f), radius = soundHoleRadius - 3.dp.toPx(), center = soundHoleCenter)

                listOf(345f,350f, 355f, 360f, 365f,370f).forEach { x ->
                    drawLine(color = darkBrown.copy(alpha = 0.7f), start = scalePoint(x, 100f), end = scalePoint(x, 920f), strokeWidth = 1.dp.toPx())
                }

                val bridgePath = Path().apply {
                    moveTo(scaleX(330f), scaleY(915f))
                    lineTo(scaleX(330f), scaleY(925f))
                    lineTo(scaleX(385f), scaleY(925f))
                    lineTo(scaleX(385f), scaleY(915f))
                    close()
                }
                drawPath(bridgePath, color = woodBrown)
                drawPath(bridgePath, color = darkBrown, style = Stroke(width = 2.dp.toPx()))
                drawContext.canvas.restore()
            }

            Text(
                text = if (isRecording) "STOP" else "REC",
                fontSize = 16.sp,
                color = Color(0xFF5d4a36),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ExcitedRainbowNote(note: String, startPosition: Offset, colorIndex: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "excited")
    val offsetX by infiniteTransition.animateFloat(
        initialValue = startPosition.x,
        targetValue = startPosition.x + Random.nextFloat() * 60f - 30f,
        animationSpec = infiniteRepeatable(animation = tween(300, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse), label = "x"
    )
    val offsetY by infiniteTransition.animateFloat(
        initialValue = startPosition.y,
        targetValue = startPosition.y + Random.nextFloat() * 60f - 30f,
        animationSpec = infiniteRepeatable(animation = tween(400, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse), label = "y"
    )
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(800, easing = LinearEasing)), label = "spin"
    )
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(animation = tween(250), repeatMode = RepeatMode.Reverse), label = "pulse"
    )

    Text(text = note, fontSize = 20.sp, color = rainbowColors[colorIndex], modifier = Modifier.offset(x = offsetX.dp, y = offsetY.dp).rotate(rotation).scale(scale))
}

@Composable
fun GuitarRecordingItem(
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
    val aesthetic = AestheticTheme() // Get access to dialogs
    var showRenameDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val beigeBg = Color(0xFFC4B4A0)
    val lavenderBox = Color(0xFFB8A8C8)
    val darkBrown = Color(0xFF5d4a36)
    val tealGreen = Color(0xFF7DB9A8)
    val peachOrange = Color(0xFFE8A87C)

    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Column(modifier = Modifier.fillMaxWidth().background(beigeBg, RoundedCornerShape(15.dp)).border(4.dp, darkBrown, RoundedCornerShape(15.dp)).padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.weight(1f).background(lavenderBox, RoundedCornerShape(10.dp)).border(3.dp, darkBrown, RoundedCornerShape(10.dp)).clickable { showRenameDialog = true }.padding(12.dp)) {
                    Text(text = recording.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = darkBrown, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = { showDeleteDialog = true }, modifier = Modifier.size(48.dp)) { GuitarDeleteIcon(darkBrown) }
            }
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)), color = tealGreen, trackColor = Color(0xFFE8DCC8))
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                GuitarControlButton(color = tealGreen, label = "Share", onClick = { showShareDialog = true }) { GuitarShareIcon(darkBrown) }
                GuitarControlButton(color = peachOrange, label = if (isPlaying && !isPaused) "Pause" else "Play", onClick = { if (isPlaying && !isPaused) onPause() else onPlay(recording.originalPath) }) {
                    if (isPlaying && !isPaused) {
                        Canvas(modifier = Modifier.size(20.dp)) {
                            drawRect(color = darkBrown, topLeft = Offset(size.width * 0.3f, size.height * 0.2f), size = Size(size.width * 0.15f, size.height * 0.6f))
                            drawRect(color = darkBrown, topLeft = Offset(size.width * 0.55f, size.height * 0.2f), size = Size(size.width * 0.15f, size.height * 0.6f))
                        }
                    } else {
                        Canvas(modifier = Modifier.size(20.dp)) {
                            val path = Path().apply { moveTo(size.width * 0.3f, size.height * 0.2f); lineTo(size.width * 0.8f, size.height * 0.5f); lineTo(size.width * 0.3f, size.height * 0.8f); close() }
                            drawPath(path = path, color = darkBrown)
                        }
                    }
                }
                GuitarControlButton(color = tealGreen, label = "Rev", onClick = { recording.reversedPath?.let { onPlay(it) } }) { GuitarRewindIcon(darkBrown) }
                if (isGameModeEnabled) {
                    // 🛡️ FIX: Check if reversedPath exists before starting
                    GuitarControlButton(color = peachOrange, label = "Fwd", onClick = { if (recording.reversedPath != null) onStartAttempt(recording, ChallengeType.FORWARD) }) { GuitarMicIcon(darkBrown) }
                    GuitarControlButton(color = tealGreen, label = "Rev", onClick = { if (recording.reversedPath != null) onStartAttempt(recording, ChallengeType.REVERSE) }) { GuitarMicIcon(darkBrown) }
                }
            }
        }
    }

    if (showRenameDialog) aesthetic.components.RenameDialog(RenamableItemType.RECORDING, recording.name, aesthetic, { onRename(recording.originalPath, it) }, { showRenameDialog = false })
    if (showDeleteDialog) aesthetic.components.DeleteDialog(DeletableItemType.RECORDING, recording, aesthetic, { onDelete(recording) }, { showDeleteDialog = false })
    if (showShareDialog) aesthetic.components.ShareDialog(recording, null, aesthetic, onShare, { showShareDialog = false })
}

@Composable
fun GuitarAttemptItem(
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
    val darkBrown = Color(0xFF5d4a36)
    val tealGreen = Color(0xFF7DB9A8)
    val peachOrange = Color(0xFFE8A87C)
    val lavenderPurple = Color(0xFFB8A8C8)

    val isPlayingThis = currentlyPlayingPath == attempt.attemptFilePath || currentlyPlayingPath == attempt.reversedAttemptFilePath
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    var showScoreDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth().padding(start = 34.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)) {
        Box(modifier = Modifier.fillMaxWidth().background(color = lavenderPurple, shape = RoundedCornerShape(15.dp)).border(width = 4.dp, color = darkBrown, shape = RoundedCornerShape(15.dp)).padding(12.dp)) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                    Column(modifier = Modifier.weight(1f)) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (onJumpToParent != null) {
                                Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = "Jump to recording", tint = darkBrown, modifier = Modifier.size(20.dp).clickable { onJumpToParent() })
                            }
                            Box(modifier = Modifier.background(peachOrange.copy(alpha = 0.3f), RoundedCornerShape(8.dp)).border(2.dp, darkBrown, RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 6.dp).clickable { showRenameDialog = true }) {
                                Text(text = attempt.playerName, style = TextStyle(fontFamily = FontFamily.Serif, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = darkBrown), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                        Spacer(modifier = Modifier.height(22.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                            if (onShareAttempt != null) {
                                GuitarControlButton(onClick = { showShareDialog = true }, color = tealGreen, label = "Share") { GuitarShareIcon(color = darkBrown) }
                            }
                            GuitarControlButton(onClick = { if (isPlayingThis && !isPaused) onPause() else onPlay(attempt.attemptFilePath) }, color = peachOrange, label = if (isPlayingThis && !isPaused) "Pause" else "Play") {
                                Icon(imageVector = if (isPlayingThis && !isPaused) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = "Play", tint = darkBrown)
                            }
                            if (attempt.reversedAttemptFilePath != null) {
                                GuitarControlButton(onClick = { onPlay(attempt.reversedAttemptFilePath!!) }, color = tealGreen, label = "Rev") { GuitarRewindIcon(darkBrown) }
                            }
                            if (onDeleteAttempt != null) {
                                GuitarControlButton(onClick = { showDeleteDialog = true }, color = peachOrange, label = "Del") { GuitarDeleteIcon(color = darkBrown) }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    DifficultySquircle(score = attempt.score.toInt(), difficulty = attempt.difficulty, challengeType = attempt.challengeType, emoji = "🎸", width = 100.dp, height = 130.dp, onClick = { showScoreDialog = true })
                }
                if (isPlayingThis) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)), color = tealGreen, trackColor = peachOrange.copy(alpha = 0.3f))
                }
            }
        }
    }

    if (showRenameDialog && onRenamePlayer != null) aesthetic.components.RenameDialog(RenamableItemType.PLAYER, attempt.playerName, aesthetic, { onRenamePlayer(attempt, it) }, { showRenameDialog = false })
    if (showDeleteDialog && onDeleteAttempt != null) aesthetic.components.DeleteDialog(DeletableItemType.ATTEMPT, attempt, aesthetic, { onDeleteAttempt(attempt) }, { showDeleteDialog = false })
    if (showShareDialog && onShareAttempt != null) aesthetic.components.ShareDialog(null, attempt, aesthetic, onShareAttempt, { showShareDialog = false })
    if (showScoreDialog) aesthetic.components.ScoreCard(attempt, aesthetic, { showScoreDialog = false })
}

@Composable
fun GuitarControlButton(color: Color, label: String, onClick: () -> Unit, icon: @Composable () -> Unit) {
    val darkBrown = Color(0xFF5d4a36)
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onClick)) {
        Box(modifier = Modifier.size(40.dp).background(color, RoundedCornerShape(10.dp)).border(3.dp, darkBrown, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) { icon() }
        Spacer(modifier = Modifier.height(1.dp))
        Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = darkBrown, textAlign = TextAlign.Center)
    }
}

// ============================================
// 🎸 HAND-DRAWN ICONS
// ============================================

@Composable
fun GuitarShareIcon(color: Color) {
    Canvas(modifier = Modifier.size(24.dp)) {
        val radius = 4.dp.toPx()
        drawCircle(color = color, radius = radius, center = Offset(size.width * 0.25f, size.height * 0.5f))
        drawCircle(color = color, radius = radius, center = Offset(size.width * 0.75f, size.height * 0.25f))
        drawCircle(color = color, radius = radius, center = Offset(size.width * 0.75f, size.height * 0.75f))
        drawLine(color = color, start = Offset(size.width * 0.25f, size.height * 0.5f), end = Offset(size.width * 0.75f, size.height * 0.25f), strokeWidth = 2.dp.toPx())
        drawLine(color = color, start = Offset(size.width * 0.25f, size.height * 0.5f), end = Offset(size.width * 0.75f, size.height * 0.75f), strokeWidth = 2.dp.toPx())
    }
}

@Composable
fun GuitarRewindIcon(color: Color) {
    Canvas(modifier = Modifier.size(24.dp)) {
        val path = Path().apply {
            arcTo(androidx.compose.ui.geometry.Rect(size.width * 0.2f, size.height * 0.2f, size.width * 0.8f, size.height * 0.8f), 180f, 270f, true)
            lineTo(size.width * 0.3f, size.height * 0.3f)
            moveTo(size.width * 0.2f, size.height * 0.5f)
            lineTo(size.width * 0.1f, size.height * 0.4f)
        }
        drawPath(path = path, color = color, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
    }
}

@Composable
fun GuitarMicIcon(color: Color) {
    Canvas(modifier = Modifier.size(24.dp)) {
        drawRoundRect(color = color, topLeft = Offset(size.width * 0.35f, size.height * 0.15f), size = Size(size.width * 0.3f, size.height * 0.35f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx()), style = Stroke(width = 2.dp.toPx()))
        val path = Path().apply {
            moveTo(size.width * 0.25f, size.height * 0.5f)
            quadraticBezierTo(size.width * 0.25f, size.height * 0.65f, size.width * 0.5f, size.height * 0.65f)
            quadraticBezierTo(size.width * 0.75f, size.height * 0.65f, size.width * 0.75f, size.height * 0.5f)
        }
        drawPath(path = path, color = color, style = Stroke(width = 2.dp.toPx()))
        drawLine(color = color, start = Offset(size.width * 0.5f, size.height * 0.65f), end = Offset(size.width * 0.5f, size.height * 0.8f), strokeWidth = 2.dp.toPx())
        drawLine(color = color, start = Offset(size.width * 0.35f, size.height * 0.8f), end = Offset(size.width * 0.65f, size.height * 0.8f), strokeWidth = 2.dp.toPx())
    }
}

@Composable
fun GuitarDeleteIcon(color: Color) {
    Canvas(modifier = Modifier.size(24.dp)) {
        drawLine(color = color, start = Offset(size.width * 0.2f, size.height * 0.25f), end = Offset(size.width * 0.8f, size.height * 0.25f), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
        val lidPath = Path().apply {
            moveTo(size.width * 0.35f, size.height * 0.25f)
            lineTo(size.width * 0.35f, size.height * 0.15f)
            quadraticBezierTo(size.width * 0.35f, size.height * 0.1f, size.width * 0.4f, size.height * 0.1f)
            lineTo(size.width * 0.6f, size.height * 0.1f)
            quadraticBezierTo(size.width * 0.65f, size.height * 0.1f, size.width * 0.65f, size.height * 0.15f)
            lineTo(size.width * 0.65f, size.height * 0.25f)
        }
        drawPath(path = lidPath, color = color, style = Stroke(width = 2.dp.toPx()))
        val canPath = Path().apply {
            moveTo(size.width * 0.25f, size.height * 0.25f)
            lineTo(size.width * 0.25f, size.height * 0.75f)
            quadraticBezierTo(size.width * 0.25f, size.height * 0.85f, size.width * 0.35f, size.height * 0.85f)
            lineTo(size.width * 0.65f, size.height * 0.85f)
            quadraticBezierTo(size.width * 0.75f, size.height * 0.85f, size.width * 0.75f, size.height * 0.75f)
            lineTo(size.width * 0.75f, size.height * 0.25f)
        }
        drawPath(path = canPath, color = color, style = Stroke(width = 2.dp.toPx()))
    }
}