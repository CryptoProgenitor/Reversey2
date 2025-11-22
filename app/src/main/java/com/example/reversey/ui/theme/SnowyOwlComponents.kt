package com.example.reversey.ui.theme

import android.content.Context
import android.media.SoundPool
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.reversey.data.models.ChallengeType
import com.example.reversey.data.models.PlayerAttempt
import com.example.reversey.data.models.Recording
import com.example.reversey.ui.components.DifficultySquircle
import com.example.reversey.ui.components.ScoreExplanationDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * 🦉 SNOWY OWL THEME
 * Arctic midnight theme with flying owl and falling snow.
 */
object SnowyOwlTheme {
    const val THEME_ID = "snowy_owl"

    val data = AestheticThemeData(
        id = THEME_ID,
        name = "Snowy Owl",
        description = "🦉 Arctic midnight with flying owl and falling snow",
        components = SnowyOwlComponents(),

        // Visuals
        primaryGradient = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF0a1128),
                Color(0xFF1c2541),
                Color(0xFF2d3a5f),
                Color(0xFF3a4a6d)
            )
        ),
        cardBorder = Color(0xFF787896).copy(alpha = 0.4f),
        primaryTextColor = Color.White,
        secondaryTextColor = Color.White.copy(alpha = 0.8f),
        useGlassmorphism = false,
        glowIntensity = 0f,
        recordButtonEmoji = "🦉",
        scoreEmojis = mapOf(
            90 to "⭐",
            80 to "❄️",
            70 to "🌙",
            60 to "🦉",
            0 to "💫"
        ),

        // M3 Overrides
        cardAlpha = 0.95f,
        shadowElevation = 8f,

        // Interaction
        dialogCopy = DialogCopy(
            deleteTitle = { type -> if (type == DeletableItemType.RECORDING) "Let this fly away?" else "Lost in the snow?" },
            deleteMessage = { type, name -> "Shall we release '$name' into the night? It cannot be called back." },
            deleteConfirmButton = "Release",
            deleteCancelButton = "Keep Close",
            shareTitle = "Send a Hoot 🦉",
            shareMessage = "Whom shall we tell of this?",
            renameTitle = { "Name this Fledgling" },
            renameHint = "New Name"
        ),
        scoreFeedback = ScoreFeedback(
            getTitle = { score ->
                when {
                    score >= 90 -> "Wise as an Owl!"
                    score >= 80 -> "Silent Flight!"
                    score >= 70 -> "Sharp Eyes!"
                    score >= 60 -> "Taking Wing"
                    else -> "Still a Fledgling"
                }
            },
            getMessage = { score ->
                when {
                    score >= 90 -> "The forest falls silent in awe."
                    score >= 80 -> "A masterful hunt in the moonlight."
                    score >= 70 -> "You navigate the dark well."
                    score >= 60 -> "Finding your bearings."
                    else -> "The night is dark and full of errors."
                }
            },
            getEmoji = { score ->
                when {
                    score >= 90 -> "🦉"
                    score >= 80 -> "🌙"
                    score >= 70 -> "❄️"
                    score >= 60 -> "🌲"
                    else -> "🥚"
                }
            }
        ),
        menuColors = MenuColors.fromColors(
            primaryText = Color.White,
            secondaryText = Color(0xFFB0C4DE),
            border = Color(0xFF787896),
            gradient = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF0a1128),
                    Color(0xFF1c2541)
                )
            )
        )
    )
}

class SnowyOwlComponents : ThemeComponents {

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
        SnowyOwlRecordingItem(
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
        SnowyOwlAttemptItem(
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
        SnowyOwlRecordButton(
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
            SnowyOwlSnowflakes()
            content()
            SnowyOwlFlying()
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
        val darkBlue = Color(0xFF1c2541)
        val lightBlue = Color(0xFF3a4a6d)
        val mysticPurple = Color(0xFF9B4F96)

        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = darkBlue,
            title = { Text(copy.deleteTitle(itemType), color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text(copy.deleteMessage(itemType, name), color = Color.White.copy(alpha = 0.8f)) },
            confirmButton = {
                Button(onClick = { onConfirm(); onDismiss() }, colors = ButtonDefaults.buttonColors(containerColor = mysticPurple)) {
                    Text(copy.deleteConfirmButton)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text(copy.deleteCancelButton, color = Color.White) }
            }
        )
    }

    @Composable
    override fun ShareDialog(recording: Recording?, attempt: PlayerAttempt?, aesthetic: AestheticThemeData, onShare: (String) -> Unit, onDismiss: () -> Unit) {
        val copy = aesthetic.dialogCopy
        val darkBlue = Color(0xFF1c2541)
        val mysticPurple = Color(0xFF9B4F96)
        val deepSlate = Color(0xFF6B4C7C)

        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = darkBlue,
            title = { Text(copy.shareTitle, color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(copy.shareMessage, color = Color.White.copy(alpha = 0.8f))
                    Spacer(modifier = Modifier.height(16.dp))
                    val path = recording?.originalPath ?: attempt?.attemptFilePath ?: ""
                    Button(onClick = { onShare(path); onDismiss() }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = mysticPurple)) {
                        Text("Share Original")
                    }
                    val revPath = recording?.reversedPath ?: attempt?.reversedAttemptFilePath
                    if (revPath != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { onShare(revPath); onDismiss() }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = deepSlate)) {
                            Text("Share Reversed")
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = Color.White) } }
        )
    }

    @Composable
    override fun RenameDialog(itemType: RenamableItemType, currentName: String, aesthetic: AestheticThemeData, onRename: (String) -> Unit, onDismiss: () -> Unit) {
        var name by remember { mutableStateOf(currentName) }
        val copy = aesthetic.dialogCopy
        val darkBlue = Color(0xFF1c2541)
        val mysticPurple = Color(0xFF9B4F96)

        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = darkBlue,
            title = { Text(copy.renameTitle(itemType), color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = name, onValueChange = { name = it }, singleLine = true,
                    label = { Text(copy.renameHint) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = mysticPurple,
                        focusedLabelColor = mysticPurple,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                        unfocusedLabelColor = Color.White.copy(alpha = 0.5f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            },
            confirmButton = {
                Button(onClick = { onRename(name); onDismiss() }, colors = ButtonDefaults.buttonColors(containerColor = mysticPurple)) {
                    Text("Save")
                }
            },
            dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = Color.White) } }
        )
    }
}

// ============================================
// 🌙 RECORD BUTTON
// ============================================

@Composable
fun SnowyOwlRecordButton(
    isRecording: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val moonColor = Color(0xFFe8e8e8)
    val eclipseColor = Color(0xFF1c2541)
    val eclipseProgress by animateFloatAsState(if (isRecording) 1f else 0f, tween(600), label = "eclipse")
    val infiniteTransition = rememberInfiniteTransition(label = "moonGlow")
    val glowRadius by infiniteTransition.animateFloat(
        initialValue = 80f, targetValue = 120f,
        animationSpec = infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glowRadius"
    )

    Box(modifier = modifier.size(120.dp), contentAlignment = Alignment.Center) {
        if (isRecording) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(brush = Brush.radialGradient(colors = listOf(Color(0xFF4a5568).copy(alpha = 0.6f), Color.Transparent), radius = glowRadius), radius = glowRadius, center = center)
            }
        }
        Canvas(modifier = Modifier.size(80.dp).clickable(onClick = onClick)) {
            val radius = size.minDimension / 2 * 0.9f
            drawCircle(color = moonColor, radius = radius, center = center, style = Fill)
            if (eclipseProgress > 0f) {
                drawCircle(color = eclipseColor, radius = radius * eclipseProgress, center = Offset(center.x - radius * 0.2f, center.y), style = Fill)
            }
            if (eclipseProgress < 0.8f) {
                val craterAlpha = (1f - eclipseProgress).coerceAtLeast(0f)
                drawCircle(color = Color(0xFFd0d0d0).copy(alpha = craterAlpha), radius = radius * 0.15f, center = Offset(center.x + radius * 0.3f, center.y - radius * 0.4f))
                drawCircle(color = Color(0xFFd0d0d0).copy(alpha = craterAlpha), radius = radius * 0.08f, center = Offset(center.x - radius * 0.2f, center.y + radius * 0.5f))
            }
        }
    }
}

// ============================================
// ❄️ FALLING SNOWFLAKES
// ============================================

data class SnowflakeData(
    var x: Float,
    var y: Float,
    val size: Float,
    val speed: Float,
    val drift: Float,
    val emoji: String = listOf("❄️", "❅", "❆").random()
)

@Composable
fun SnowyOwlSnowflakes() {
    var snowflakes by remember { mutableStateOf(listOf<SnowflakeData>()) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = constraints.maxWidth.toFloat()
        val screenHeight = constraints.maxHeight.toFloat()

        // Initialize snowflakes
        LaunchedEffect(screenWidth, screenHeight) {
            snowflakes = List(25) {
                SnowflakeData(
                    x = Random.nextFloat() * screenWidth,
                    y = Random.nextFloat() * screenHeight,
                    size = Random.nextFloat() * 8f + 2f,
                    speed = Random.nextFloat() * 3f + 1f,
                    drift = Random.nextFloat() * 2f - 1f
                )
            }
        }

        // Animate snowflakes
        LaunchedEffect(Unit) {
            while (true) {
                withFrameMillis {
                    snowflakes = snowflakes.map { snowflake ->
                        var newY = snowflake.y + snowflake.speed *0.3f
                        var newX = snowflake.x + snowflake.drift

                        if (newY > screenHeight) {
                            newY = -snowflake.size
                            newX = Random.nextFloat() * screenWidth
                        }

                        snowflake.copy(x = newX, y = newY)
                    }
                }
            }
        }

        // Render snowflakes
        snowflakes.forEach { snowflake ->
            AnimatedSnowflake(data = snowflake)
        }
    }
}

@Composable
fun AnimatedSnowflake(data: SnowflakeData) {
    Canvas(
        modifier = Modifier
            .offset(x = data.x.dp, y = data.y.dp)
            .size(data.size.dp)
    ) {
        drawContext.canvas.nativeCanvas.apply {
            val emoji = data.emoji
            val textSizePx = data.size * 22f
            val paint = android.graphics.Paint().apply {
                textSize = textSizePx
                isAntiAlias = true
                isSubpixelText = true
                isFilterBitmap = true
                alpha = 255
            }
            val drawX = size.width * 0.1f
            val drawY = size.height * 0.75f

            drawText(
                emoji,
                drawX,
                drawY,
                paint
            )
        }
    }
}

// ============================================
// 🦉 OWL FLIGHT LOGIC
// ============================================

class OwlHootManager(private val context: Context) {
    private var isSoundLoaded = false
    private val soundPool = SoundPool.Builder().setMaxStreams(3).build().apply {
        setOnLoadCompleteListener { _, _, status -> if (status == 0) isSoundLoaded = true }
    }
    private var hootSoundId = 0
    init {
        try { hootSoundId = soundPool.load(context, context.resources.getIdentifier("hoot", "raw", context.packageName), 1) } catch (_: Exception) {}
    }
    fun playHoot() { if (isSoundLoaded && hootSoundId != 0) soundPool.play(hootSoundId, 0.7f, 0.7f, 1, 0, 1f) }
    fun release() { soundPool.release() }
}

data class HeartBubble(val id: Int, val startX: Float, val startY: Float, val initialDriftX: Float, val createdAt: Long = System.currentTimeMillis())

@Composable
fun SnowyOwlFlying() {
    val density = LocalDensity.current
    val owlWidth = 150.dp; val owlHeight = 95.dp
    var owlX by remember { mutableStateOf(100f) }
    var owlY by remember { mutableStateOf(250f) }
    var baseY by remember { mutableStateOf(250f) }
    var velocityX by remember { mutableStateOf(1.7f) }
    var facingRight by remember { mutableStateOf(true) }
    var phase by remember { mutableStateOf(0f) }
    var hearts by remember { mutableStateOf(listOf<HeartBubble>()) }
    var nextId by remember { mutableStateOf(0) }
    val context = LocalContext.current
    val hootManager = remember { OwlHootManager(context) }
    val scope = rememberCoroutineScope()

    DisposableEffect(Unit) { onDispose { hootManager.release() } }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val width = constraints.maxWidth.toFloat()
        val height = constraints.maxHeight.toFloat()

        LaunchedEffect(Unit) {
            while (isActive) {
                withFrameMillis {
                    phase += 0.02f
                    owlY = baseY + sin(phase) * 70f
                    owlX += velocityX
                    if (owlX > width + 50f) { velocityX = -1.5f; facingRight = false; baseY = Random.nextFloat() * (height * 0.6f) + (height * 0.3f); phase = 0f }
                    else if (owlX < -200f) { velocityX = 1.5f; facingRight = true; baseY = Random.nextFloat() * (height * 0.6f) + (height * 0.3f); phase = 0f }
                }
            }
        }

        LaunchedEffect(hearts) {
            if (hearts.isNotEmpty()) {
                delay(120)
                val now = System.currentTimeMillis()
                hearts = hearts.filter { now - it.createdAt < 2500L }
            }
        }

        Box(
            modifier = Modifier
                .offset(x = with(density) { owlX.toDp() }, y = with(density) { owlY.toDp() })
                .size(owlWidth, owlHeight)
                .pointerInput(Unit) {
                    detectTapGestures {
                        hootManager.playHoot()
                        repeat(4) {
                            hearts = hearts + HeartBubble(nextId++, owlX + 100f, owlY + 50f, Random.nextFloat() * 60f * (if (Random.nextBoolean()) 1f else -1f))
                        }
                    }
                }
        ) {
            Canvas(
                modifier = Modifier.fillMaxSize().graphicsLayer(scaleX = if (facingRight) 1f else -1f, rotationZ = cos(phase) * 2f)
            ) {
                drawOwl(sin(phase * 5f) * 17f, sin(phase * 5f) * 8f)
            }
        }

        hearts.forEach { heart ->
            val yAnim = remember { Animatable(0f) }
            val xAnim = remember { Animatable(0f) }
            val alphaAnim = remember { Animatable(1f) }
            LaunchedEffect(heart.id) {
                launch { yAnim.animateTo(-250f, tween(2500, easing = LinearEasing)) }
                launch { xAnim.animateTo(heart.initialDriftX, tween(2500)) }
                launch { delay(1000); alphaAnim.animateTo(0f, tween(1500)) }
            }
            Text(
                "💖",
                modifier = Modifier.offset(
                    x = with(density) { heart.startX.toDp() + xAnim.value.dp },
                    y = with(density) { heart.startY.toDp() + yAnim.value.dp }
                ).graphicsLayer(alpha = alphaAnim.value),
                fontSize = 24.sp
            )
        }
    }
}

//claude's luxurious owl
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawOwl(
    wingAngle: Float,
    wingY: Float
) {
    val bodyColor = Color(0xFFf8f8f8)
    val wingColor = Color(0xFFf5f5f5)
    val eyeColor = Color(0xFF2d3a5f)
    val beakColor = Color(0xFFffb84d)
    val spotColor = Color(0xFF505050).copy(alpha = 0.85f)

    // Wings with flapping animation (4x bigger positions)
    drawWingAnimated(-220f, 30f, wingColor, -wingAngle, wingY) // Left wing flaps down
    drawWingAnimated(220f, 30f, wingColor, wingAngle, wingY)   // Right wing flaps up

    // Body with gradient shading (4x bigger)
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFffffff),
                bodyColor,
                Color(0xFFe0e0e0)
            ),
            center = Offset(380f, 260f),
            radius = 120f
        ),
        topLeft = Offset(300f, 208f),
        size = Size(200f, 144f),
        style = Fill
    )

    // Body feather spots - MUCH MORE VISIBLE (bigger, darker)
    drawCircle(color = spotColor, radius = 8f, center = Offset(340f, 240f))
    drawCircle(color = spotColor, radius = 7f, center = Offset(460f, 260f))
    drawCircle(color = spotColor, radius = 9f, center = Offset(380f, 300f))
    drawCircle(color = spotColor, radius = 7f, center = Offset(420f, 290f))
    drawCircle(color = spotColor, radius = 8f, center = Offset(350f, 280f))
    drawCircle(color = spotColor, radius = 6f, center = Offset(440f, 250f))
    drawCircle(color = spotColor, radius = 7f, center = Offset(370f, 270f))

    // Horizontal bar markings on body - THICKER, DARKER
    drawLine(color = spotColor, start = Offset(320f, 250f), end = Offset(370f, 252f), strokeWidth = 5f, cap = StrokeCap.Round)
    drawLine(color = spotColor, start = Offset(430f, 270f), end = Offset(480f, 268f), strokeWidth = 5f, cap = StrokeCap.Round)
    drawLine(color = spotColor, start = Offset(340f, 310f), end = Offset(390f, 312f), strokeWidth = 5f, cap = StrokeCap.Round)

    // Head with subtle gradient (4x bigger)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFffffff), Color(0xFFf8f8f8), Color(0xFFe8e8e8)),
            center = Offset(450f, 230f),
            radius = 80f
        ),
        radius = 80f,
        center = Offset(460f, 240f)
    )

    // Head spots - BIGGER, MORE VISIBLE
    drawCircle(color = spotColor, radius = 5f, center = Offset(410f, 200f))
    drawCircle(color = spotColor, radius = 5f, center = Offset(510f, 200f))
    drawCircle(color = spotColor, radius = 4f, center = Offset(430f, 190f))
    drawCircle(color = spotColor, radius = 4f, center = Offset(490f, 190f))
    drawCircle(color = spotColor, radius = 4f, center = Offset(390f, 220f))
    drawCircle(color = spotColor, radius = 4f, center = Offset(530f, 220f))

    // Eyes with subtle gradient (4x bigger)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(eyeColor, eyeColor.copy(alpha = 0.8f), Color(0xFF1a2030)),
            center = Offset(440f, 232f), radius = 20f
        ),
        radius = 20f, center = Offset(440f, 232f)
    )
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(eyeColor, eyeColor.copy(alpha = 0.8f), Color(0xFF1a2030)),
            center = Offset(480f, 232f), radius = 20f
        ),
        radius = 20f, center = Offset(480f, 232f)
    )

    // Eye highlights (4x bigger)
    drawCircle(color = Color.White, radius = 6f, center = Offset(444f, 226f))
    drawCircle(color = Color.White, radius = 6f, center = Offset(484f, 226f))

    // Beak with gradient (4x bigger)
    val beakPath = Path().apply {
        moveTo(460f, 248f)
        lineTo(452f, 260f)
        lineTo(468f, 260f)
        close()
    }
    drawPath(beakPath, brush = Brush.linearGradient(
        colors = listOf(Color(0xFFffb84d), Color(0xFFffa033)),
        start = Offset(460f, 248f), end = Offset(460f, 260f)
    ))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawWing(
    xOffset: Float,
    yOffset: Float,
    color: Color
) {
    val wingPath = Path().apply {
        moveTo(400f, 240f)
        quadraticBezierTo(400f + xOffset * 0.5f, 160f + yOffset, 400f + xOffset, 140f + yOffset)
        quadraticBezierTo(400f + xOffset * 0.8f, 180f + yOffset, 400f + xOffset * 0.3f, 280f + yOffset)
        close()
    }
    drawPath(wingPath, color, style = Fill)
    drawPath(wingPath, Color(0xFFd0d0d0), style = Stroke(width = 8f))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawWingAnimated(
    xOffset: Float,
    yOffset: Float,
    color: Color,
    rotationAngle: Float,
    verticalOffset: Float
) {
    val spotColor = Color(0xFF505050).copy(alpha = 0.85f)

    rotate(degrees = rotationAngle, pivot = Offset(400f, 240f)) {
        val wingPath = Path().apply {
            moveTo(400f, 240f + verticalOffset)

            // Broad base connection to body
            cubicTo(
                400f + xOffset * 0.15f, 230f + yOffset + verticalOffset,
                400f + xOffset * 0.3f, 220f + yOffset + verticalOffset,
                400f + xOffset * 0.45f, 200f + yOffset + verticalOffset
            )

            // Finger feathers - 5 individual "fingers" at wing tip
            // Finger 1 (outermost)
            cubicTo(400f + xOffset * 0.65f, 170f + yOffset + verticalOffset, 400f + xOffset * 0.85f, 140f + yOffset + verticalOffset, 400f + xOffset * 0.95f, 125f + yOffset + verticalOffset)
            cubicTo(400f + xOffset * 0.93f, 132f + yOffset + verticalOffset, 400f + xOffset * 0.90f, 138f + yOffset + verticalOffset, 400f + xOffset * 0.88f, 145f + yOffset + verticalOffset)

            // Finger 2
            cubicTo(400f + xOffset * 0.90f, 140f + yOffset + verticalOffset, 400f + xOffset * 0.92f, 135f + yOffset + verticalOffset, 400f + xOffset * 0.93f, 132f + yOffset + verticalOffset)
            cubicTo(400f + xOffset * 0.90f, 140f + yOffset + verticalOffset, 400f + xOffset * 0.87f, 148f + yOffset + verticalOffset, 400f + xOffset * 0.84f, 155f + yOffset + verticalOffset)

            // Finger 3 (middle)
            cubicTo(400f + xOffset * 0.86f, 150f + yOffset + verticalOffset, 400f + xOffset * 0.88f, 145f + yOffset + verticalOffset, 400f + xOffset * 0.89f, 142f + yOffset + verticalOffset)
            cubicTo(400f + xOffset * 0.86f, 152f + yOffset + verticalOffset, 400f + xOffset * 0.83f, 160f + yOffset + verticalOffset, 400f + xOffset * 0.80f, 168f + yOffset + verticalOffset)

            // Finger 4
            cubicTo(400f + xOffset * 0.82f, 162f + yOffset + verticalOffset, 400f + xOffset * 0.84f, 157f + yOffset + verticalOffset, 400f + xOffset * 0.85f, 154f + yOffset + verticalOffset)
            cubicTo(400f + xOffset * 0.82f, 164f + yOffset + verticalOffset, 400f + xOffset * 0.78f, 174f + yOffset + verticalOffset, 400f + xOffset * 0.74f, 182f + yOffset + verticalOffset)

            // Finger 5 (innermost)
            cubicTo(400f + xOffset * 0.76f, 177f + yOffset + verticalOffset, 400f + xOffset * 0.78f, 172f + yOffset + verticalOffset, 400f + xOffset * 0.79f, 169f + yOffset + verticalOffset)

            // Scalloped trailing edge back to body
            cubicTo(400f + xOffset * 0.75f, 185f + yOffset + verticalOffset, 400f + xOffset * 0.65f, 215f + yOffset + verticalOffset, 400f + xOffset * 0.50f, 245f + yOffset + verticalOffset)
            cubicTo(400f + xOffset * 0.40f, 260f + yOffset + verticalOffset, 400f + xOffset * 0.25f, 270f + yOffset + verticalOffset, 400f + xOffset * 0.15f, 275f + yOffset + verticalOffset)
            cubicTo(400f + xOffset * 0.08f, 270f + yOffset + verticalOffset, 400f + xOffset * 0.03f, 255f + yOffset + verticalOffset, 400f, 240f + verticalOffset)
            close()
        }

        // Draw wing fill with gradient for depth
        drawPath(wingPath, brush = Brush.linearGradient(
            colors = listOf(Color(0xFFffffff), color, Color(0xFFd8d8d8)),
            start = Offset(400f, 240f + verticalOffset),
            end = Offset(400f + xOffset, 140f + yOffset + verticalOffset)
        ), style = Fill)

        // Dark spots along wing edge
        val spots = listOf(
            Pair(400f + xOffset * 0.88f, 145f + yOffset + verticalOffset),
            Pair(400f + xOffset * 0.84f, 155f + yOffset + verticalOffset),
            Pair(400f + xOffset * 0.80f, 168f + yOffset + verticalOffset),
            Pair(400f + xOffset * 0.74f, 182f + yOffset + verticalOffset),
            Pair(400f + xOffset * 0.55f, 235f + yOffset + verticalOffset),
            Pair(400f + xOffset * 0.45f, 250f + yOffset + verticalOffset),
            Pair(400f + xOffset * 0.60f, 210f + yOffset + verticalOffset),
            Pair(400f + xOffset * 0.50f, 225f + yOffset + verticalOffset),
        )

        spots.forEach { (x, y) ->
            drawCircle(color = spotColor, radius = 6f, center = Offset(x, y))
        }

        // Wing outline
        drawPath(wingPath, Color(0xFF909090), style = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

// ============================================
// UI ITEMS
// ============================================

@Composable
fun SnowyOwlRecordingItem(
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
    val aesthetic = AestheticTheme()
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }

    val cardOuter = Color(0xFF282832).copy(alpha = 0.6f)
    val cardInner = Color(0xFF14141e).copy(alpha = 0.6f)
    val borderColor = Color(0xFF787896).copy(alpha = 0.35f)
    val mysticPurple = Color(0xFF9B4F96).copy(alpha = 0.85f)
    val deepSlate = Color(0xFF6B4C7C).copy(alpha = 0.85f)

    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp, horizontal = 4.dp).background(cardOuter, RoundedCornerShape(20.dp)).border(2.dp, borderColor, RoundedCornerShape(20.dp)).padding(6.dp)) {
        Column(modifier = Modifier.fillMaxWidth().background(cardInner, RoundedCornerShape(15.dp)).padding(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.weight(1f).background(Color(0xFF505064).copy(0.5f), RoundedCornerShape(12.dp)).clickable { showRenameDialog = true }.padding(12.dp)) {
                    Text(recording.name, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.size(44.dp).background(Color(0xFF8B3A6F).copy(0.7f), RoundedCornerShape(10.dp)).clickable { showDeleteDialog = true }, contentAlignment = Alignment.Center) {
                    OwlDeleteIcon(Color.White)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth(), color = Color(0xFFADD8E6).copy(0.5f))
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                OwlControlButton(mysticPurple, "Share", { showShareDialog = true }) { OwlShareIcon(Color.White) }
                OwlControlButton(deepSlate, if (isPlaying && !isPaused) "Pause" else "Play", { if (isPlaying && !isPaused) onPause() else onPlay(recording.originalPath) }) {
                    if (isPlaying && !isPaused) OwlPauseIcon(Color.White) else OwlPlayIcon(Color.White)
                }
                OwlControlButton(mysticPurple, "Rev", { recording.reversedPath?.let { onPlay(it) } }) { OwlRewindIcon(Color.White) }
                if (isGameModeEnabled) {
                    OwlControlButton(deepSlate, "Fwd", { onStartAttempt(recording, ChallengeType.FORWARD) }) { OwlMicRightArrowIcon(Color.White) }
                    OwlControlButton(mysticPurple, "Rev", { onStartAttempt(recording, ChallengeType.REVERSE) }) { OwlMicLeftArrowIcon(Color.White) }
                }
            }
        }
    }

    if (showRenameDialog) aesthetic.components.RenameDialog(RenamableItemType.RECORDING, recording.name, aesthetic, { onRename(recording.originalPath, it) }, { showRenameDialog = false })
    if (showDeleteDialog) aesthetic.components.DeleteDialog(DeletableItemType.RECORDING, recording, aesthetic, { onDelete(recording) }, { showDeleteDialog = false })
    if (showShareDialog) aesthetic.components.ShareDialog(recording, null, aesthetic, onShare, { showShareDialog = false })
}

@Composable
fun SnowyOwlAttemptItem(
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

    val cardOuter = Color(0xFF282832).copy(alpha = 0.6f)
    val cardInner = Color(0xFF14141e).copy(alpha = 0.6f)
    val borderColor = Color(0xFF787896).copy(alpha = 0.35f)
    val mysticPurple = Color(0xFF9B4F96).copy(alpha = 0.85f)
    val deepSlate = Color(0xFF6B4C7C).copy(alpha = 0.85f)

    Box(modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 6.dp, top = 8.dp, bottom = 8.dp).background(cardOuter, RoundedCornerShape(20.dp)).border(2.dp, borderColor, RoundedCornerShape(20.dp)).padding(6.dp)) {
        Column(modifier = Modifier.fillMaxWidth().background(cardInner, RoundedCornerShape(15.dp)).padding(12.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (onJumpToParent != null) Box(modifier = Modifier.size(20.dp).clickable { onJumpToParent() }) { OwlHomeIcon(Color.White.copy(0.9f)) }
                        Box(modifier = Modifier.background(Color(0xFF505064).copy(0.5f), RoundedCornerShape(8.dp)).clickable { showRenameDialog = true }.padding(horizontal = 12.dp, vertical = 6.dp)) {
                            Text(attempt.playerName, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (onShareAttempt != null) OwlControlButton(mysticPurple, "Share", { showShareDialog = true }) { OwlShareIcon(Color.White) }
                        OwlControlButton(deepSlate, if (isPlayingThis && !isPaused) "Pause" else "Play", { if (isPlayingThis && !isPaused) onPause() else onPlay(attempt.attemptFilePath) }) {
                            if (isPlayingThis && !isPaused) OwlPauseIcon(Color.White) else OwlPlayIcon(Color.White)
                        }
                        if (attempt.reversedAttemptFilePath != null) OwlControlButton(mysticPurple, "Rev", { onPlay(attempt.reversedAttemptFilePath!!) }) { OwlRewindIcon(Color.White) }
                        if (onDeleteAttempt != null) OwlControlButton(deepSlate, "Del", { showDeleteDialog = true }) { OwlDeleteIcon(Color.White) }
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                DifficultySquircle(attempt.score, attempt.difficulty, attempt.challengeType, "🦉", 100.dp, 130.dp, { showScoreDialog = true })
            }
            if (isPlayingThis) LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp), color = Color(0xFFADD8E6).copy(0.5f))
        }
    }

    if (showRenameDialog && onRenamePlayer != null) aesthetic.components.RenameDialog(RenamableItemType.PLAYER, attempt.playerName, aesthetic, { onRenamePlayer(attempt, it) }, { showRenameDialog = false })
    if (showDeleteDialog && onDeleteAttempt != null) aesthetic.components.DeleteDialog(DeletableItemType.ATTEMPT, attempt, aesthetic, { onDeleteAttempt(attempt) }, { showDeleteDialog = false })
    if (showShareDialog && onShareAttempt != null) aesthetic.components.ShareDialog(null, attempt, aesthetic, onShareAttempt, { showShareDialog = false })
    if (showScoreDialog) aesthetic.components.ScoreCard(attempt, aesthetic, { showScoreDialog = false })
}

// ============================================
// 🎨 OWL ICONS
// ============================================

@Composable
fun OwlControlButton(color: Color, label: String, onClick: () -> Unit, icon: @Composable () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onClick)) {
        Box(modifier = Modifier.size(44.dp).background(color, RoundedCornerShape(10.dp)).border(2.dp, Color(0xFFC8508C).copy(0.6f), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) { icon() }
        Text(label, fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun OwlPlayIcon(color: Color) {
    Canvas(modifier = Modifier.size(32.dp)) {
        drawPath(Path().apply { moveTo(size.width * 0.29f, size.height * 0.17f); lineTo(size.width * 0.29f, size.height * 0.83f); lineTo(size.width * 0.79f, size.height * 0.5f); close() }, color, style = Stroke(width = 2.dp.toPx()))
    }
}

@Composable
fun OwlPauseIcon(color: Color) {
    Canvas(modifier = Modifier.size(32.dp)) {
        drawRect(color, topLeft = Offset(size.width * 0.3f, size.height * 0.2f), size = Size(size.width * 0.12f, size.height * 0.6f), style = Stroke(width = 2.dp.toPx()))
        drawRect(color, topLeft = Offset(size.width * 0.58f, size.height * 0.2f), size = Size(size.width * 0.12f, size.height * 0.6f), style = Stroke(width = 2.dp.toPx()))
    }
}

@Composable
fun OwlShareIcon(color: Color) {
    Canvas(modifier = Modifier.size(32.dp)) {
        val r = size.width * 0.083f
        drawCircle(color, r, Offset(size.width * 0.25f, size.height * 0.5f), style = Stroke(2.dp.toPx()))
        drawCircle(color, r, Offset(size.width * 0.75f, size.height * 0.25f), style = Stroke(2.dp.toPx()))
        drawCircle(color, r, Offset(size.width * 0.75f, size.height * 0.75f), style = Stroke(2.dp.toPx()))
        drawLine(color, Offset(size.width * 0.33f, size.height * 0.5f), Offset(size.width * 0.67f, size.height * 0.29f), strokeWidth = 2.dp.toPx())
        drawLine(color, Offset(size.width * 0.33f, size.height * 0.5f), Offset(size.width * 0.67f, size.height * 0.71f), strokeWidth = 2.dp.toPx())
    }
}

@Composable
fun OwlRewindIcon(color: Color) {
    Canvas(modifier = Modifier.size(32.dp)) {
        val stroke = 2.dp.toPx()
        drawPath(Path().apply { moveTo(size.width * 0.54f, size.height * 0.71f); lineTo(size.width * 0.29f, size.height * 0.5f); lineTo(size.width * 0.54f, size.height * 0.29f) }, color, style = Stroke(stroke))
        drawPath(Path().apply { moveTo(size.width * 0.83f, size.height * 0.71f); lineTo(size.width * 0.58f, size.height * 0.5f); lineTo(size.width * 0.83f, size.height * 0.29f) }, color, style = Stroke(stroke))
    }
}

@Composable
fun OwlDeleteIcon(color: Color) {
    Canvas(modifier = Modifier.size(24.dp)) {
        drawLine(color, Offset(size.width * 0.33f, size.height * 0.33f), Offset(size.width * 0.67f, size.height * 0.67f), strokeWidth = 2.5.dp.toPx())
        drawLine(color, Offset(size.width * 0.67f, size.height * 0.33f), Offset(size.width * 0.33f, size.height * 0.67f), strokeWidth = 2.5.dp.toPx())
        drawPath(Path().apply { moveTo(size.width * 0.25f, size.height * 0.75f); lineTo(size.width * 0.75f, size.height * 0.75f); lineTo(size.width * 0.79f, size.height * 0.83f); lineTo(size.width * 0.21f, size.height * 0.83f); close() }, color, style = Stroke(2.dp.toPx()))
    }
}

@Composable
fun OwlHomeIcon(color: Color) {
    Canvas(modifier = Modifier.size(24.dp)) {
        drawPath(Path().apply { moveTo(size.width * 0.125f, size.height * 0.5f); lineTo(size.width * 0.5f, size.height * 0.125f); lineTo(size.width * 0.875f, size.height * 0.5f) }, color, style = Stroke(2.dp.toPx()))
        drawPath(Path().apply { moveTo(size.width * 0.21f, size.height * 0.42f); lineTo(size.width * 0.21f, size.height * 0.83f); lineTo(size.width * 0.79f, size.height * 0.83f); lineTo(size.width * 0.79f, size.height * 0.42f) }, color, style = Stroke(2.dp.toPx()))
    }
}

@Composable
fun OwlMicRightArrowIcon(color: Color) {
    Canvas(modifier = Modifier.size(32.dp)) {
        drawRoundRect(color, topLeft = Offset(size.width * 0.1f, size.height * 0.15f), size = Size(size.width * 0.22f, size.height * 0.45f), cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()), style = Stroke(2.5.dp.toPx()))
        drawPath(Path().apply { moveTo(size.width * 0.48f, size.height * 0.15f); lineTo(size.width * 0.68f, size.height * 0.375f); lineTo(size.width * 0.48f, size.height * 0.60f) }, color, style = Stroke(2.5.dp.toPx()))
    }
}

@Composable
fun OwlMicLeftArrowIcon(color: Color) {
    Canvas(modifier = Modifier.size(32.dp)) {
        drawRoundRect(color, topLeft = Offset(size.width * 0.68f, size.height * 0.15f), size = Size(size.width * 0.22f, size.height * 0.45f), cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()), style = Stroke(2.5.dp.toPx()))
        drawPath(Path().apply { moveTo(size.width * 0.52f, size.height * 0.15f); lineTo(size.width * 0.32f, size.height * 0.375f); lineTo(size.width * 0.52f, size.height * 0.60f) }, color, style = Stroke(2.5.dp.toPx()))
    }
}