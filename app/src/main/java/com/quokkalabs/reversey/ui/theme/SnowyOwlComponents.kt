package com.quokkalabs.reversey.ui.theme

import android.content.Context
import android.media.SoundPool
import androidx.compose.animation.core.Animatable
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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quokkalabs.reversey.data.models.ChallengeType
import com.quokkalabs.reversey.data.models.PlayerAttempt
import com.quokkalabs.reversey.data.models.Recording
import com.quokkalabs.reversey.ui.components.DifficultySquircle
import com.quokkalabs.reversey.ui.components.ScoreExplanationDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
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
        ),
        isPro = true
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
            SnowyOwlAurora()
            SnowyOwlStarfield()
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
// ❄️ FALLING SNOWFLAKES and STARFIELD
// ============================================

data class SnowflakeData(
    var x: Float,
    var y: Float,
    val size: Float,
    val speed: Float,
    val drift: Float,
    val emoji: String = listOf("❄️", "❅", "❆").random()
)

data class StarData(
    val x: Float,
    val y: Float,
    val size: Float,
    val baseAlpha: Float,
    val twinkleSpeed: Float,  // How fast it twinkles
    val twinklePhase: Float   // Starting phase offset
)

data class ShootingStarData(
    val id: Int,
    val startX: Float,
    val startY: Float,
    val angle: Float,         // Direction in radians
    val speed: Float,
    val length: Float,
    val createdAt: Long
)

// ============================================
// ✨ DYNAMIC STARFIELD WITH SHOOTING STARS
// ============================================

@Composable
fun SnowyOwlStarfield() {
    var stars by remember { mutableStateOf(listOf<StarData>()) }
    var shootingStars by remember { mutableStateOf(listOf<ShootingStarData>()) }
    var nextShootingId by remember { mutableStateOf(0) }
    var time by remember { mutableStateOf(0f) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = constraints.maxWidth.toFloat()
        val screenHeight = constraints.maxHeight.toFloat()

        // Initialize stars once
        LaunchedEffect(screenWidth, screenHeight) {
            stars = List(80) {
                StarData(
                    x = Random.nextFloat() * screenWidth,
                    y = Random.nextFloat() * screenHeight * 0.7f, // Stars in upper 70%
                    size = Random.nextFloat() * 2.5f + 0.8f,
                    baseAlpha = Random.nextFloat() * 0.4f + 0.3f,
                    twinkleSpeed = Random.nextFloat() * 2f + 1f,
                    twinklePhase = Random.nextFloat() * 6.28f
                )
            }
        }

        // Animate time for twinkling
        LaunchedEffect(Unit) {
            while (isActive) {
                withFrameMillis {
                    time += 0.016f // ~60fps
                }
            }
        }

        // Spawn shooting stars randomly
        LaunchedEffect(Unit) {
            while (isActive) {
                delay(Random.nextLong(4000, 12000)) // Every 4-12 seconds
                val newStar = ShootingStarData(
                    id = nextShootingId++,
                    startX = Random.nextFloat() * screenWidth * 0.8f + screenWidth * 0.1f,
                    startY = Random.nextFloat() * screenHeight * 0.3f,
                    angle = Random.nextFloat() * 0.5f + 0.3f, // Roughly diagonal down-right
                    speed = Random.nextFloat() * 400f + 300f,
                    length = Random.nextFloat() * 60f + 40f,
                    createdAt = System.currentTimeMillis()
                )
                shootingStars = shootingStars + newStar
            }
        }

        // Clean up old shooting stars
        LaunchedEffect(shootingStars) {
            if (shootingStars.isNotEmpty()) {
                delay(100)
                val now = System.currentTimeMillis()
                shootingStars = shootingStars.filter { now - it.createdAt < 1500L }
            }
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw twinkling stars
            stars.forEach { star ->
                val twinkle = sin(time * star.twinkleSpeed + star.twinklePhase)
                val alpha = (star.baseAlpha + twinkle * 0.25f).coerceIn(0.1f, 1f)

                // Main star
                drawCircle(
                    color = Color.White.copy(alpha = alpha),
                    radius = star.size,
                    center = Offset(star.x, star.y)
                )

                // Subtle glow for brighter stars
                if (star.size > 2f) {
                    drawCircle(
                        color = Color(0xFFADD8E6).copy(alpha = alpha * 0.3f),
                        radius = star.size * 2.5f,
                        center = Offset(star.x, star.y)
                    )
                }
            }

            // Draw shooting stars
            val now = System.currentTimeMillis()
            shootingStars.forEach { shooting ->
                val elapsed = (now - shooting.createdAt) / 1000f
                val distance = elapsed * shooting.speed
                val progress = (elapsed / 1.5f).coerceIn(0f, 1f)

                // Fade in then out
                val alpha = when {
                    progress < 0.2f -> progress / 0.2f
                    progress > 0.7f -> (1f - progress) / 0.3f
                    else -> 1f
                }

                val headX = shooting.startX + cos(shooting.angle) * distance
                val headY = shooting.startY + sin(shooting.angle) * distance
                val tailX = headX - cos(shooting.angle) * shooting.length
                val tailY = headY - sin(shooting.angle) * shooting.length

                // Draw the streak with gradient effect
                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = alpha * 0.5f),
                            Color.White.copy(alpha = alpha)
                        ),
                        start = Offset(tailX, tailY),
                        end = Offset(headX, headY)
                    ),
                    start = Offset(tailX, tailY),
                    end = Offset(headX, headY),
                    strokeWidth = 2f
                )

                // Bright head
                drawCircle(
                    color = Color.White.copy(alpha = alpha),
                    radius = 2.5f,
                    center = Offset(headX, headY)
                )
            }
        }
    }
}

// ============================================
// 🌌 AURORA BOREALIS HINTS
// ============================================

@Composable
fun SnowyOwlAurora() {
    val infiniteTransition = rememberInfiniteTransition(label = "aurora")

    val driftPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(40000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "drift"
    )

    val rayPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rays"
    )

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val width = constraints.maxWidth.toFloat()
        val height = constraints.maxHeight.toFloat()

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(Color.Black) // Deep space background

            // 1. PALETTE ADJUSTMENT: STRICT GREEN & PURPLE
            // Removed Cyans/Teals to stop the "Blue" wash
            val auroraColors = listOf(
                Color(0xFF2DEB90), // Classic Aurora Green
                Color(0xFF2DEB90), // Double Green to make it dominant
                Color(0xFF8A2BE2), // Deep Violet
                Color(0xFFD040FF)  // Magenta/Purple (for the high altitude nitrogen look)
            )

            val rayCount = 50
            val step = width / rayCount
            val rayWidth = step * 12f

            for (r in 0 until rayCount) {
                val baseX = step * r
                val colorIndex = r % auroraColors.size
                val color = auroraColors[colorIndex]

                val shimmer = sin(rayPhase + r * 0.6f)
                val rayHeight = (height * 0.45f) * (0.8f + 0.2f * shimmer)

                // 2. DIMMING THE LIGHTS
                // Previous was 0.2f -> Dropped to 0.1f range.
                // This prevents the additive blend from turning white/cyan too fast.
                val rayAlpha = 0.08f + 0.04f * shimmer

                val path = Path().apply {
                    moveTo(baseX, 0f)

                    // Left edge
                    for (y in 0..rayHeight.toInt() step 15) {
                        val yNorm = y / rayHeight
                        val wave = sin(yNorm * 3 + driftPhase + r * 0.2f) * 25f
                        lineTo(baseX + wave, y.toFloat())
                    }

                    // Bottom
                    val bottomY = rayHeight
                    quadraticBezierTo(
                        baseX + rayWidth / 2, bottomY + 50f,
                        baseX + rayWidth, bottomY
                    )

                    // Right edge
                    for (y in rayHeight.toInt() downTo 0 step 15) {
                        val yNorm = y / rayHeight
                        val wave = sin(yNorm * 3 + driftPhase + r * 0.2f + 0.5f) * 25f
                        lineTo(baseX + rayWidth + wave, y.toFloat())
                    }
                    close()
                }

                drawPath(
                    path = path,
                    brush = Brush.radialGradient(
                        colors = listOf(
                            color.copy(alpha = rayAlpha),        // Core
                            color.copy(alpha = rayAlpha * 0.5f), // Soft Mids
                            Color.Transparent                    // Fade
                        ),
                        center = Offset(baseX + rayWidth / 2, 0f),
                        // 3. TIGHTER FOCUS
                        // Reducing radius slightly to keep colours separated
                        radius = rayHeight * 0.9f
                    ),
                    blendMode = BlendMode.Plus
                )
            }
        }
    }
}

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
                drawOwl(
                    wingAngle = sin(phase * 2.5f) * 25f, // Slower, bigger flaps
                    wingY = 0f, // No vertical movement needed now
                    facingRight = facingRight // Pass the facing direction
                )
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

// ============================================
// 🦉 ARTICULATED OWL WING SYSTEM (DROP-IN REPLACEMENT)
// ============================================

/**
 * REPLACES: The old drawOwl() function
 * NEW: Uses articulated wings with natural flapping motion
 * FIXED: Proper depth logic for mirrored canvas
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawOwl(
    wingAngle: Float,
    wingY: Float,
    facingRight: Boolean = true
) {
    val bodyColor = Color(0xFFf8f8f8)
    val wingColor = Color(0xFFf5f5f5)
    val eyeColor = Color(0xFF2d3a5f)
    val beakColor = Color(0xFFffb84d)
    val spotColor = Color(0xFF505050).copy(alpha = 0.85f)

    // Calculate articulated wing states (natural flapping, not rigid rotation)
    val flapPhase = wingAngle / 25f // Convert old wingAngle to phase
    val leftWingState = calculateWingState(flapPhase, isRightWing = false)
    val rightWingState = calculateWingState(flapPhase, isRightWing = true)

    // ---- CORRECTED DEPTH LOGIC FOR MIRRORED CANVAS ----


        drawArticulatedWing(
            side = 1f, // Body's RIGHT = Visual LEFT
            state = rightWingState,
            isNearWing = false, // Visual LEFT should be FAR (behind)
            wingColor = wingColor,
            spotColor = spotColor
        )

        drawBodyCore(bodyColor, spotColor)

        drawArticulatedWing(
            side = -1f, // Body's LEFT = Visual RIGHT
            state = leftWingState,
            isNearWing = true,//visual RIGHT should be NEAR (in front)
            wingColor = wingColor,
            spotColor = spotColor
        )


    // Head always on top (unchanged from your original)
    drawHead(eyeColor, beakColor, spotColor)
}

/**
 * REPLACES: The old drawWingAnimated() function
 * NEW: Draws articulated wing with shoulder-elbow-wrist joints
 */
private fun DrawScope.drawArticulatedWing(
    side: Float, // -1 for left, 1 for right
    state: WingState,
    isNearWing: Boolean,
    wingColor: Color,
    spotColor: Color
) {
    val shoulderX = 400f
    val shoulderY = 240f

    // --- ARTICULATED JOINT CALCULATIONS ---
    // Upper arm (shoulder to elbow)
    val upperLength = 75f
    val elbowX = shoulderX + cos(state.shoulderAngle) * upperLength * side
    val elbowY = shoulderY + sin(state.shoulderAngle) * upperLength

    // Forearm (elbow to wrist) - includes elbow bend
    val forearmLength = 65f
    val totalForearmAngle = state.shoulderAngle + state.elbowAngle
    val wristX = elbowX + cos(totalForearmAngle) * forearmLength * side
    val wristY = elbowY + sin(totalForearmAngle) * forearmLength

    // Hand (wrist to tip) - includes wrist adjustment
    val handLength = 55f
    val totalHandAngle = totalForearmAngle + state.wristAngle
    val tipX = wristX + cos(totalHandAngle) * handLength * side
    val tipY = wristY + sin(totalHandAngle) * handLength

    // --- DRAW WING WITH NATURAL CURVES ---
    val wingPath = Path().apply {
        moveTo(shoulderX, shoulderY)

        // Upper arm curve
        quadraticBezierTo(
            shoulderX + (elbowX - shoulderX) * 0.3f * side,
            shoulderY + (elbowY - shoulderY) * 0.3f,
            elbowX, elbowY
        )

        // Forearm curve with feather spread
        val featherSpreadX = state.featherSpread * 15f * side
        quadraticBezierTo(
            elbowX + (wristX - elbowX) * 0.5f + featherSpreadX,
            elbowY + (wristY - elbowY) * 0.5f,
            wristX, wristY
        )

        // Wing tip with finger feathers
        val featherCount = 5
        val featherStep = handLength / featherCount
        for (i in 0..featherCount) {
            val t = i.toFloat() / featherCount
            val curveX = wristX + (tipX - wristX) * t
            val curveY = wristY + (tipY - wristY) * t

            // Feather splay based on spread amount
            val featherSplay = sin(t * PI.toFloat()) * state.featherSpread * 12f * side
            lineTo(curveX + featherSplay, curveY)
        }

        // Close back to body with trailing edge curve
        quadraticBezierTo(
            wristX - featherSpreadX * 0.7f,
            wristY - 20f,
            shoulderX, shoulderY + 40f
        )
        close()
    }

    // Draw wing fill with gradient
    drawPath(
        wingPath,
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White,
                wingColor,
                Color(0xFFe8e8e8)
            ),
            start = Offset(shoulderX, shoulderY),
            end = Offset(tipX, tipY)
        ),
        style = Fill
    )

    // Draw wing spots (more on near wing for depth)
    val spotAlpha = if (isNearWing) 0.9f else 0.7f
    drawCircle(
        color = spotColor.copy(alpha = spotAlpha),
        radius = 8f,
        center = Offset(elbowX, elbowY)
    )
    drawCircle(
        color = spotColor.copy(alpha = spotAlpha),
        radius = 6f,
        center = Offset(
            elbowX + (wristX - elbowX) * 0.5f,
            elbowY + (wristY - elbowY) * 0.5f
        )
    )

    // Wing outline (thicker for near wing)
    val outlineWidth = if (isNearWing) 4f else 3f
    drawPath(
        wingPath,
        color = Color(0xFF909090),
        style = Stroke(width = outlineWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
}

/**
 * NEW: Helper data class for articulated wing state
 * (Add this ABOVE the drawOwl function in your file)
 */
private data class WingState(
    val shoulderAngle: Float,  // Main flap angle in radians
    val elbowAngle: Float,     // Secondary bend
    val wristAngle: Float,     // Minor adjustment
    val featherSpread: Float   // 0 = folded, 1 = spread
)

/**
 * NEW: Calculates natural wing motion (replaces rigid rotation)
 * Using Float-specific math operations
 */
private fun calculateWingState(phase: Float, isRightWing: Boolean): WingState {
    // 1. Explicitly type 'side' as Float to ensure it isn't inferred as Int or Number
    val side: Float = if (isRightWing) 1f else -1f

    val sinPhase = sin(phase.toDouble()).toFloat()
    val sinDoublePhase = sin((phase * 2f).toDouble()).toFloat()

    return WingState(
        shoulderAngle = sinPhase * 0.7f,
        elbowAngle = max(0f, -sinPhase) * 0.4f,
        // This calculation should now work because all operands are Floats
        wristAngle = sinDoublePhase * 0.15f * side,
        featherSpread = (sinPhase + 1f) / 2f
    )
}

private fun max(f: Float, f2: Float) {}

/**
 * NEW: Draws just the body (without head/wings logic)
 */
private fun DrawScope.drawBodyCore(bodyColor: Color, spotColor: Color) {
    // Body oval (same as your original but without head)
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFffffff), bodyColor, Color(0xFFe0e0e0)),
            center = Offset(380f, 260f),
            radius = 120f
        ),
        topLeft = Offset(300f, 208f),
        size = Size(200f, 144f),
        style = Fill
    )

    // Body spots (unchanged from your original)
    drawCircle(color = spotColor, radius = 8f, center = Offset(340f, 240f))
    drawCircle(color = spotColor, radius = 7f, center = Offset(460f, 260f))
    drawCircle(color = spotColor, radius = 9f, center = Offset(380f, 300f))
    drawCircle(color = spotColor, radius = 7f, center = Offset(420f, 290f))
    drawCircle(color = spotColor, radius = 8f, center = Offset(350f, 280f))
    drawCircle(color = spotColor, radius = 6f, center = Offset(440f, 250f))
    drawCircle(color = spotColor, radius = 7f, center = Offset(370f, 270f))

    // Body markings
    drawLine(color = spotColor, start = Offset(320f, 250f), end = Offset(370f, 252f), strokeWidth = 5f, cap = StrokeCap.Round)
    drawLine(color = spotColor, start = Offset(430f, 270f), end = Offset(480f, 268f), strokeWidth = 5f, cap = StrokeCap.Round)
    drawLine(color = spotColor, start = Offset(340f, 310f), end = Offset(390f, 312f), strokeWidth = 5f, cap = StrokeCap.Round)
}

/**
 * NEW: Draws just the head (separated from body)
 */
private fun DrawScope.drawHead(eyeColor: Color, beakColor: Color, spotColor: Color) {
    // Head circle
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFffffff), Color(0xFFf8f8f8), Color(0xFFe8e8e8)),
            center = Offset(450f, 230f),
            radius = 80f
        ),
        radius = 80f,
        center = Offset(460f, 240f)
    )

    // Head spots
    drawCircle(color = spotColor, radius = 5f, center = Offset(410f, 200f))
    drawCircle(color = spotColor, radius = 5f, center = Offset(510f, 200f))
    drawCircle(color = spotColor, radius = 4f, center = Offset(430f, 190f))
    drawCircle(color = spotColor, radius = 4f, center = Offset(490f, 190f))
    drawCircle(color = spotColor, radius = 4f, center = Offset(390f, 220f))
    drawCircle(color = spotColor, radius = 4f, center = Offset(530f, 220f))

    // Eyes
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

    // Eye highlights
    drawCircle(color = Color.White, radius = 6f, center = Offset(444f, 226f))
    drawCircle(color = Color.White, radius = 6f, center = Offset(484f, 226f))

    // Beak
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

/**
 * DELETE this old function from your file:
 * private fun DrawScope.drawWingAnimated(...)
 *
 * DELETE this old function from your file:
 * private fun DrawScope.drawWing(...)
 */

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