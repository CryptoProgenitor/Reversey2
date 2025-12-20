package com.quokkalabs.reversey.ui.theme

import android.content.Context
import android.media.SoundPool
import android.util.Log
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.withFrameMillis
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quokkalabs.reversey.R
import com.quokkalabs.reversey.data.models.ChallengeType
import com.quokkalabs.reversey.data.models.PlayerAttempt
import com.quokkalabs.reversey.data.models.Recording
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.text.style.TextAlign
import com.quokkalabs.reversey.ui.components.DifficultySquircle
import com.quokkalabs.reversey.ui.components.ScoreExplanationDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

// =============================================================================
// 🎁 PRESENT DROP TUNING CONSTANTS - TWEAK THESE!
// =============================================================================

private const val CHIMNEY_SCALE_FACTOR = 0.50f   // Final size when entering chimney
private const val CHIMNEY_DEPTH_PERCENT = 0.08f  // How far down chimney before disappearing
private const val TOSS_SPEED = 12f               // Initial upward velocity
private const val GRAVITY = 0.4f                 // Gravity acceleration per frame
private const val STAGGER_PERCENT = 0.05f        // 5% screen width between presents
private const val SPARKLE_COUNT = 8              // Particles per chimney landing
private const val SPARKLE_DURATION_MS = 600L     // How long sparkles last

private const val CHIMNEY_X_OFFSET_DP = 18f      // dp right of house center (+ = right)
private const val CHIMNEY_Y_OFFSET_DP = -70f     // dp from roof peak (+ = down)

// Snowdrift tuning
private const val SNOWDRIFT_HEIGHT = 55f         // Max height of drifts in dp
private const val SNOWDRIFT_WIDTH_FACTOR = 0.4f  // Width as fraction of house width


// Santa speed

private const val SANTA_SPEED = 0.7f             // Santa's flight speed (pixels/frame)

/**
 * 🎄 CHRISTMAS THEME
 * Festive holiday theme with flying Santa, snowy landscape, and twinkling lights.
 */
object ChristmasTheme {
    const val THEME_ID = "ChristmasTheme"

    val christmasRed = Color(0xFFC41E3A)
    val christmasGreen = Color(0xFF165B33)
    val christmasGold = Color(0xFFFFD700)
    val snowWhite = Color(0xFFFFFAFA)
    val warmCream = Color(0xFFFFF8DC)
    val berryRed = Color(0xFF8B0000)
    val frostBlue = Color(0xFFB0E0E6)

    val reindeerBrown = Color(0xFF5D4037)

    val data = AestheticThemeData(
        id = THEME_ID,
        name = "Christmas",
        description = "🎄 Festive holiday cheer with Santa, snow and twinkling lights",
        components = ChristmasComponents(),
        primaryGradient = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF0C1445),
                Color(0xFF1A237E),
                Color(0xFF1B3A26),
                Color(0xFF2E1A1A)
            )
        ),
        cardBorder = christmasGold.copy(alpha = 0.5f),
        primaryTextColor = snowWhite,
        secondaryTextColor = warmCream.copy(alpha = 0.9f),
        useGlassmorphism = false,
        glowIntensity = 0.3f,
        recordButtonEmoji = "🎄",
        scoreEmojis = mapOf(90 to "⭐", 80 to "🎄", 70 to "🎁", 60 to "❄️", 0 to "🦌"),
        cardAlpha = 0.92f,
        shadowElevation = 6f,
        dialogCopy = DialogCopy(
            deleteTitle = { type -> if (type == DeletableItemType.RECORDING) "Return this gift?" else "Unwrap and discard?" },
            deleteMessage = { _, name -> "Shall we send '$name' back to the North Pole? This cannot be undone." },
            deleteConfirmButton = "Send Back",
            deleteCancelButton = "Keep Gift",
            shareTitle = "Share Holiday Cheer! 🎁",
            shareMessage = "Spread the joy!",
            renameTitle = { "Write a New Gift Tag" },
            renameHint = "Gift Tag"
        ),
        scoreFeedback = ScoreFeedback(
            getTitle = { score ->
                when {
                    score >= 90 -> "Santa's Favorite!"; score >= 80 -> "Nice List!"; score >= 70 -> "Merry & Bright!"; score >= 60 -> "Holiday Spirit!"; else -> "Still Unwrapping!"
                }
            },
            getMessage = { score ->
                when {
                    score >= 90 -> "The elves are impressed!"; score >= 80 -> "Jingle all the way!"; score >= 70 -> "Deck the halls!"; score >= 60 -> "Ho ho ho, keep practicing!"; else -> "Even Rudolph had to learn!"
                }
            },
            getEmoji = { score ->
                when {
                    score >= 90 -> "⭐"; score >= 80 -> "🎄"; score >= 70 -> "🎁"; score >= 60 -> "❄️"; else -> "🦌"
                }
            }
        ),
        menuColors = MenuColors(
            menuBackground = Brush.verticalGradient(listOf(Color(0xFF165B33), Color(0xFF0C1445))),
            menuCardBackground = Color(0xFF1A237E).copy(alpha = 0.95f),
            menuItemBackground = Color(0xFF2E1A1A).copy(alpha = 0.6f),
            menuTitleText = christmasGold,
            menuItemText = snowWhite,
            menuDivider = christmasGold.copy(alpha = 0.3f),
            menuBorder = christmasGold.copy(alpha = 0.5f),
            toggleActive = christmasRed,
            toggleInactive = Color.Gray
        ),
        isPro = true
    )
}

class ChristmasComponents : ThemeComponents {
    @Composable
    override fun RecordingItem(
        recording: Recording,
        aesthetic: AestheticThemeData,
        isPlaying: Boolean,
        isPaused: Boolean,
        progress: Float,
        currentlyPlayingPath: String?,
        onPlay: (String) -> Unit,
        onPause: () -> Unit,
        onStop: () -> Unit,
        onDelete: (Recording) -> Unit,
        onShare: (String) -> Unit,
        onRename: (String, String) -> Unit,
        isGameModeEnabled: Boolean,
        onStartAttempt: (Recording, ChallengeType) -> Unit,
    ) {
        ChristmasRecordingItem(
            recording, aesthetic, isPlaying, isPaused, progress, currentlyPlayingPath,
            onPlay, onPause, onStop, onDelete, onShare, onRename, isGameModeEnabled, onStartAttempt
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
        onJumpToParent: (() -> Unit)?,
        onOverrideScore: ((Int) -> Unit)?,
        onResetScore: (() -> Unit)?,
    ) {
        ChristmasAttemptItem(
            attempt, aesthetic, currentlyPlayingPath, isPaused, progress,
            onPlay, onPause, onStop, onRenamePlayer, onDeleteAttempt, onShareAttempt,
            onJumpToParent, onOverrideScore, onResetScore
        )
    }

    @Composable
    override fun RecordButton(
        isRecording: Boolean,
        isProcessing: Boolean,
        aesthetic: AestheticThemeData,
        onStartRecording: () -> Unit,
        onStopRecording: () -> Unit,
        countdownProgress: Float,
    ) {
        ChristmasRecordButton(isRecording, countdownProgress) {
            if (isRecording) onStopRecording() else onStartRecording()
        }
    }

    @Composable
    override fun AppBackground(aesthetic: AestheticThemeData, content: @Composable () -> Unit) {
        Box(modifier = Modifier
            .fillMaxSize()
            .background(aesthetic.primaryGradient)) {
            ChristmasStars()
            ChristmasShootingStars()
            ChristmasSnowflakes()
            ChristmasLandscape()
            content()
            ChristmasSantaFlight()

        }
    }

    @Composable
    override fun ScoreCard(
        attempt: PlayerAttempt,
        aesthetic: AestheticThemeData,
        onDismiss: () -> Unit,
        onOverrideScore: (Int) -> Unit,
    ) {
        ScoreExplanationDialog(attempt, onDismiss, onOverrideScore = onOverrideScore)
    }

    @Composable
    override fun DeleteDialog(
        itemType: DeletableItemType,
        item: Any,
        aesthetic: AestheticThemeData,
        onConfirm: () -> Unit,
        onDismiss: () -> Unit,
    ) {
        val copy = aesthetic.dialogCopy
        val name =
            if (item is Recording) item.name else if (item is PlayerAttempt) item.playerName else "Item"
        AlertDialog(
            onDismissRequest = onDismiss, containerColor = Color(0xFF165B33),
            title = {
                Text(
                    copy.deleteTitle(itemType),
                    color = ChristmasTheme.christmasGold,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    copy.deleteMessage(itemType, name),
                    color = ChristmasTheme.snowWhite.copy(alpha = 0.9f)
                )
            },
            confirmButton = {
                Button(
                    onClick = { onConfirm(); onDismiss() },
                    colors = ButtonDefaults.buttonColors(containerColor = ChristmasTheme.christmasRed)
                ) { Text(copy.deleteConfirmButton) }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(
                        copy.deleteCancelButton,
                        color = ChristmasTheme.snowWhite
                    )
                }
            }
        )
    }

    @Composable
    override fun ShareDialog(
        recording: Recording?,
        attempt: PlayerAttempt?,
        aesthetic: AestheticThemeData,
        onShare: (String) -> Unit,
        onDismiss: () -> Unit,
    ) {
        val copy = aesthetic.dialogCopy
        AlertDialog(
            onDismissRequest = onDismiss, containerColor = Color(0xFF165B33),
            title = {
                Text(
                    copy.shareTitle,
                    color = ChristmasTheme.christmasGold,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(copy.shareMessage, color = ChristmasTheme.snowWhite.copy(alpha = 0.9f))
                    Spacer(Modifier.height(16.dp))
                    val path = recording?.originalPath ?: attempt?.attemptFilePath ?: ""
                    Button(
                        onClick = { onShare(path); onDismiss() }, Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = ChristmasTheme.christmasRed)
                    ) { Text("🎁 Share Original") }
                    val revPath = recording?.reversedPath ?: attempt?.reversedAttemptFilePath
                    if (revPath != null) {
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = { onShare(revPath); onDismiss() }, Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = ChristmasTheme.berryRed)
                        ) { Text("🔄 Share Reversed") }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(
                        "Cancel",
                        color = ChristmasTheme.snowWhite
                    )
                }
            }
        )
    }

    @Composable
    override fun RenameDialog(
        itemType: RenamableItemType,
        currentName: String,
        aesthetic: AestheticThemeData,
        onRename: (String) -> Unit,
        onDismiss: () -> Unit,
    ) {
        var name by remember { mutableStateOf(currentName) }
        val copy = aesthetic.dialogCopy
        AlertDialog(
            onDismissRequest = onDismiss, containerColor = Color(0xFF165B33),
            title = {
                Text(
                    copy.renameTitle(itemType),
                    color = ChristmasTheme.christmasGold,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                OutlinedTextField(
                    value = name, onValueChange = { name = it }, singleLine = true,
                    label = { Text(copy.renameHint) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ChristmasTheme.christmasGold,
                        focusedLabelColor = ChristmasTheme.christmasGold,
                        unfocusedBorderColor = ChristmasTheme.snowWhite.copy(alpha = 0.5f),
                        unfocusedLabelColor = ChristmasTheme.snowWhite.copy(alpha = 0.5f),
                        focusedTextColor = ChristmasTheme.snowWhite,
                        unfocusedTextColor = ChristmasTheme.snowWhite
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = { onRename(name); onDismiss() },
                    colors = ButtonDefaults.buttonColors(containerColor = ChristmasTheme.christmasRed)
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(
                        "Cancel",
                        color = ChristmasTheme.snowWhite
                    )
                }
            }
        )
    }
}

// =============================================================================
// 🎅 FLYING SANTA WITH REINDEER + PRESENT BOMBING RUN
// =============================================================================

class HoHoSoundManager(private val context: Context) {
    private var isSoundLoaded = false
    private val soundPool = SoundPool.Builder().setMaxStreams(3).build().apply {
        setOnLoadCompleteListener { _, _, status -> if (status == 0) isSoundLoaded = true }
    }
    private var hohoSoundId = 0

    init {
        try {
            hohoSoundId = soundPool.load(
                context,
                context.resources.getIdentifier("ho_ho_ho", "raw", context.packageName), 1
            )
        } catch (_: Exception) {
        }
    }

    fun playHoHo() {
        if (isSoundLoaded && hohoSoundId != 0) soundPool.play(hohoSoundId, 0.8f, 0.8f, 1, 0, 1f)
    }

    fun release() {
        soundPool.release()
    }
}

// Data classes for presents and sparkles
data class PendingPresent(
    val spawnAtFrame: Int,
    val spawnX: Float,
    val spawnY: Float,
    val sleighVelocityX: Float,
)

data class FallingPresent(
    val id: Int,
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val startY: Float,
    val spinRate: Float,
    var rotation: Float = 0f,
    var sparkleTriggered: Boolean = false,
)

data class ChimneySparkle(
    val id: Int,
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val color: Color,
    val createdAt: Long,
)

@Composable
fun ChristmasSantaFlight() {
    val context = LocalContext.current
    val hohoManager = remember { HoHoSoundManager(context) }
    DisposableEffect(Unit) { onDispose { hohoManager.release() } }

    val density = LocalDensity.current
    var santaX by remember { mutableFloatStateOf(-400f) }
    var santaY by remember { mutableFloatStateOf(200f) }
    var baseY by remember { mutableFloatStateOf(200f) }
    val baseVelocity = with(density) { SANTA_SPEED.dp.toPx() }  // dp per frame
    var velocityX by remember { mutableFloatStateOf(baseVelocity) }
    var facingRight by remember { mutableStateOf(true) }
    var phase by remember { mutableFloatStateOf(0f) }
    var frameCount by remember { mutableIntStateOf(0) }

    // Present bombing state
    var pendingPresents by remember { mutableStateOf(listOf<PendingPresent>()) }
    var fallingPresents by remember { mutableStateOf(listOf<FallingPresent>()) }
    var sparkles by remember { mutableStateOf(listOf<ChimneySparkle>()) }
    var nextPresentId by remember { mutableIntStateOf(0) }
    var nextSparkleId by remember { mutableIntStateOf(0) }

    val infiniteTransition = rememberInfiniteTransition(label = "rudolphNose")
    val noseGlow by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(800, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "noseGlow"
    )

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = constraints.maxWidth.toFloat()
        val screenHeight = constraints.maxHeight.toFloat()
        val sleighWidth = 100.dp
        val sleighHeight = 80.dp
        val reindeerWidth = 55.dp
        val reindeerHeight = 45.dp
        val reindeerSpacing = 40.dp

        // House position (must match ChristmasLandscape)
        val houseSizeFactor = 1.5f
        val houseHeightDp = 120.dp * houseSizeFactor
        val houseX = screenWidth * 0.75f
        val houseBottomY = screenHeight - with(density) { 40.dp.toPx() }
        val houseHeightPx = with(density) { houseHeightDp.toPx() }

        // Chimney target = roof peak + offset
        val chimneyX = houseX + with(density) { CHIMNEY_X_OFFSET_DP.dp.toPx() }
        val chimneyY = houseBottomY - houseHeightPx * 0.7f + with(density) { CHIMNEY_Y_OFFSET_DP.dp.toPx() }
        val chimneyBottomY = chimneyY + screenHeight * CHIMNEY_DEPTH_PERCENT

        // Calculate full train length for proper screen exit
        val trainLength = with(density) {
            sleighWidth.toPx() + (reindeerSpacing.toPx() * 4) + reindeerWidth.toPx()
        }

        LaunchedEffect(Unit) {
            while (isActive) {
                withFrameMillis {
                    frameCount++
                    phase += 0.05f
                    santaY = baseY + sin(phase) * 15f
                    santaX += velocityX

                    // Train exit/flip logic
                    if (santaX > screenWidth + trainLength) {
                        velocityX = -baseVelocity; facingRight = false
                        baseY = Random.nextFloat() * (screenHeight * 0.3f) + 80f; phase = 0f
                        santaX = screenWidth + trainLength
                    } else if (santaX < -trainLength) {
                        velocityX = baseVelocity; facingRight = true
                        baseY = Random.nextFloat() * (screenHeight * 0.3f) + 80f; phase = 0f
                        santaX = -trainLength
                    }

                    // Spawn pending presents when their frame arrives
                    val toSpawn = pendingPresents.filter { it.spawnAtFrame <= frameCount }
                    pendingPresents = pendingPresents.filter { it.spawnAtFrame > frameCount }

                    toSpawn.forEach { pending ->
                        // Calculate ballistic trajectory to hit chimney
                        val startX = pending.spawnX
                        val startY = pending.spawnY

                        // Time to reach chimney (approximate using simple physics)
                        // Using quadratic formula: chimneyY = startY - tossSpeed*t + 0.5*gravity*t^2
                        val a = 0.5f * GRAVITY
                        val b = -TOSS_SPEED
                        val c = startY - chimneyY
                        val discriminant = b * b - 4 * a * c
                        val flightTime = if (discriminant > 0) {
                            (-b + sqrt(discriminant)) / (2 * a)
                        } else 50f // fallback

                        // Calculate required Vx to hit chimney
                        val deltaX = chimneyX - startX
                        val requiredVx = deltaX / flightTime

                        fallingPresents = fallingPresents + FallingPresent(
                            id = nextPresentId++,
                            x = startX,
                            y = startY,
                            vx = requiredVx,
                            vy = -TOSS_SPEED + Random.nextFloat() * 2f - 1f, // slight variation
                            startY = startY,
                            spinRate = Random.nextFloat() * 8f + 4f * (if (Random.nextBoolean()) 1f else -1f)
                        )
                    }

                    // Update falling presents physics
                    fallingPresents = fallingPresents.mapNotNull { present ->
                        present.vy += GRAVITY
                        present.x += present.vx
                        present.y += present.vy
                        present.rotation += present.spinRate

                        // Trigger sparkle when entering chimney
                        if (!present.sparkleTriggered && present.y >= chimneyY) {
                            present.sparkleTriggered = true
                            // Spawn sparkles
                            repeat(SPARKLE_COUNT) {
                                val angle = Random.nextFloat() * 2 * PI.toFloat()
                                val speed = Random.nextFloat() * 3f + 2f
                                sparkles = sparkles + ChimneySparkle(
                                    id = nextSparkleId++,
                                    x = chimneyX,
                                    y = chimneyY,
                                    vx = cos(angle) * speed,
                                    vy = sin(angle) * speed - 2f, // bias upward
                                    color = listOf(
                                        ChristmasTheme.christmasGold,
                                        ChristmasTheme.christmasRed,
                                        Color.White
                                    ).random(),
                                    createdAt = System.currentTimeMillis()
                                )
                            }
                        }

                        // Remove when past chimney bottom
                        if (present.y > chimneyBottomY) null else present
                    }

                    // Update sparkles (fade out and remove old ones)
                    val now = System.currentTimeMillis()
                    sparkles = sparkles.filter { now - it.createdAt < SPARKLE_DURATION_MS }
                }
            }
        }

        val spacingFactor = 0.75f
        val sleighXDp = with(density) { santaX.toDp() }
        val sleighYDp = with(density) { santaY.toDp() }
        val sleighWidthPx = with(density) { sleighWidth.toPx() }
        val sleighHeightPx = with(density) { sleighHeight.toPx() }
        val reindeerWidthPx = with(density) { reindeerWidth.toPx() }
        val reindeerHeightPx = with(density) { reindeerHeight.toPx() }

        val reindeerOffsets = if (facingRight) listOf(
            sleighWidth * spacingFactor + reindeerSpacing * 4,
            sleighWidth * spacingFactor + reindeerSpacing * 3,
            sleighWidth * spacingFactor + reindeerSpacing * 2,
            sleighWidth * spacingFactor + reindeerSpacing * 1
        ) else listOf(
            -reindeerSpacing * 4 - sleighWidth * spacingFactor,
            -reindeerSpacing * 3 - sleighWidth * spacingFactor,
            -reindeerSpacing * 2 - sleighWidth * spacingFactor,
            -reindeerSpacing * 1 - sleighWidth * spacingFactor
        )

        fun getYOffset(index: Int): Float =
            sin(phase + index * 0.8f) * 10f + if (index < 5) 20f else 0f

        // Draw reins
        Canvas(modifier = Modifier.fillMaxSize()) {
            val lastReindeerIdx = 3
            val reindeerBackX = santaX + with(density) {
                if (facingRight) reindeerOffsets[lastReindeerIdx].toPx() + reindeerWidthPx * 0.55f
                else reindeerOffsets[lastReindeerIdx].toPx() + reindeerWidthPx * 0.35f
            }
            val sleighFrontX = santaX + with(density) {
                if (facingRight) sleighWidthPx * 0.75f else sleighWidthPx * 0.21f
            }
            val reindeerBackY = santaY + getYOffset(lastReindeerIdx + 1) + reindeerHeightPx * 0.6f
            val sleighFrontY = santaY + getYOffset(5) + sleighHeightPx * 0.55f
            val sagAmount = 15f + sin(phase * 1.5f) * 8f
            val midX = (reindeerBackX + sleighFrontX) / 2
            val midY = maxOf(reindeerBackY, sleighFrontY) + sagAmount
            val path = Path().apply {
                moveTo(reindeerBackX, reindeerBackY)
                quadraticBezierTo(midX, midY, sleighFrontX, sleighFrontY)
            }
            drawPath(
                path,
                ChristmasTheme.reindeerBrown,
                style = Stroke(width = 2f, cap = StrokeCap.Round)
            )
        }

        // Rudolph with glowing nose
        val rudolphYOffset = with(density) { getYOffset(1).toDp() }
        Box(
            modifier = Modifier
                .offset(x = sleighXDp + reindeerOffsets[0], y = sleighYDp + rudolphYOffset)
                .size(reindeerWidth, reindeerHeight)
                .graphicsLayer(scaleX = if (facingRight) 1f else -1f)
        ) {
            Image(
                painter = painterResource(id = R.drawable.rudolph), contentDescription = "Rudolph",
                modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit
            )
            Canvas(modifier = Modifier.fillMaxSize()) {
                val noseX = size.width * 0.85f;
                val noseY = size.height * 0.32f
                drawCircle(
                    Color.Red.copy(alpha = noseGlow * 0.5f),
                    radius = 12f + (noseGlow * 6f),
                    center = Offset(noseX, noseY)
                )
                drawCircle(Color.Red.copy(alpha = 0.9f), radius = 5f, center = Offset(noseX, noseY))
            }
        }

        // Regular reindeer
        for (i in 1..3) {
            val reindeerYOffset = with(density) { getYOffset(i + 1).toDp() }
            Image(
                painter = painterResource(id = R.drawable.reindeer),
                contentDescription = "Reindeer",
                modifier = Modifier
                    .offset(x = sleighXDp + reindeerOffsets[i], y = sleighYDp + reindeerYOffset)
                    .size(reindeerWidth, reindeerHeight)
                    .graphicsLayer(scaleX = if (facingRight) 1f else -1f),
                contentScale = ContentScale.Fit
            )
        }

        // Santa's Sleigh with tap hitbox
        val sleighYOffset = with(density) { getYOffset(5).toDp() }
        Box(
            modifier = Modifier
                .offset(x = sleighXDp, y = sleighYDp + sleighYOffset)
                .size(sleighWidth, sleighHeight)
                .pointerInput(Unit) {
                    detectTapGestures {
                        hohoManager.playHoHo()

                        // Spawn 2-3 presents with time stagger
                        val count = (2..3).random()
                        val staggerFrames =
                            ((screenWidth * STAGGER_PERCENT) / abs(velocityX)).toInt()
                                .coerceAtLeast(1)
                        val sleighCenterX = santaX + sleighWidthPx / 2
                        val sleighCenterY = santaY + sleighHeightPx / 2

                        repeat(count) { i ->
                            pendingPresents = pendingPresents + PendingPresent(
                                spawnAtFrame = frameCount + (i * staggerFrames),
                                spawnX = sleighCenterX,
                                spawnY = sleighCenterY,
                                sleighVelocityX = velocityX
                            )
                        }
                    }
                }
        ) {
            Image(
                painter = painterResource(id = R.drawable.santa_sleigh),
                contentDescription = "Santa's Sleigh",
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(scaleX = if (facingRight) 1f else -1f),
                contentScale = ContentScale.Fit
            )
        }

        // Draw falling presents
        fallingPresents.forEach { present ->
            // Calculate scale based on Y progress toward chimney
            val scaleProgress =
                ((present.y - present.startY) / (chimneyBottomY - present.startY)).coerceIn(0f, 1f)
            val scale = 1f - (scaleProgress * (1f - CHIMNEY_SCALE_FACTOR))

            // Gradually guide X toward chimney as it descends
            val xProgress =
                ((present.y - present.startY) / (chimneyY - present.startY)).coerceIn(0f, 1f)
            val drawX = present.x + (chimneyX - present.x) * xProgress * xProgress

            Canvas(
                modifier = Modifier
                    .offset(x = with(density) { drawX.toDp() } - 15.dp * scale,
                        y = with(density) { present.y.toDp() } - 15.dp * scale)
                    .size(30.dp * scale)
                    .graphicsLayer(rotationZ = present.rotation)
            ) {
                // Draw present box
                val boxSize = size.width * 0.8f
                val boxOffset = (size.width - boxSize) / 2
                drawRect(
                    ChristmasTheme.christmasRed,
                    topLeft = Offset(boxOffset, boxOffset),
                    size = Size(boxSize, boxSize)
                )
                // Ribbon horizontal
                drawRect(
                    ChristmasTheme.christmasGold,
                    topLeft = Offset(boxOffset, size.height * 0.4f),
                    size = Size(boxSize, size.height * 0.2f)
                )
                // Ribbon vertical
                drawRect(
                    ChristmasTheme.christmasGold,
                    topLeft = Offset(size.width * 0.4f, boxOffset),
                    size = Size(size.width * 0.2f, boxSize)
                )
                // Bow (simple circles)
                drawCircle(
                    ChristmasTheme.christmasGold,
                    radius = size.width * 0.12f,
                    center = Offset(size.width * 0.35f, size.height * 0.25f)
                )
                drawCircle(
                    ChristmasTheme.christmasGold,
                    radius = size.width * 0.12f,
                    center = Offset(size.width * 0.65f, size.height * 0.25f)
                )
                drawCircle(
                    ChristmasTheme.christmasGold,
                    radius = size.width * 0.08f,
                    center = Offset(size.width * 0.5f, size.height * 0.3f)
                )
            }
        }

        // Draw sparkles
        val now = System.currentTimeMillis()
        sparkles.forEach { sparkle ->
            val age = (now - sparkle.createdAt).toFloat()
            val progress = (age / SPARKLE_DURATION_MS).coerceIn(0f, 1f)
            val alpha = 1f - progress
            val currentX = sparkle.x + sparkle.vx * progress * 20f
            val currentY =
                sparkle.y + sparkle.vy * progress * 20f + progress * progress * 30f // gravity on sparkles

            Canvas(
                modifier = Modifier
                    .offset(x = with(density) { currentX.toDp() } - 4.dp,
                        y = with(density) { currentY.toDp() } - 4.dp)
                    .size(8.dp)
            ) {
                drawCircle(sparkle.color.copy(alpha = alpha * 0.5f), radius = size.width / 2)
                drawCircle(sparkle.color.copy(alpha = alpha), radius = size.width / 4)
            }
        }
    }
}

// =============================================================================
// 🏠 CHRISTMAS LANDSCAPE
// =============================================================================

@Composable
fun ChristmasLandscape() {
    val infiniteTransition = rememberInfiniteTransition(label = "houseLights")
    val lightPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Restart),
        label = "lightPhase"
    )
    val lightColors = listOf(
        ChristmasTheme.christmasRed,
        ChristmasTheme.christmasGreen,
        ChristmasTheme.christmasGold,
        Color(0xFF4169E1),
        Color(0xFFFF69B4)
    )

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = constraints.maxWidth.toFloat()
        val screenHeight = constraints.maxHeight.toFloat()
        val houseSizeFactor = 1.5f
        val houseWidth = 140.dp * houseSizeFactor
        val houseHeight = 120.dp * houseSizeFactor
        val houseX = screenWidth * 0.75f
        val houseBottomY = screenHeight - with(LocalDensity.current) { 40.dp.toPx() }

        // Snow hills and trees - asymmetric rolling terrain
        val density = LocalDensity.current
        val houseWidthPx = with(density) { houseWidth.toPx() }
        val houseHeightPx = with(density) { houseHeight.toPx() }
        val snowdriftHeightPx = with(density) { SNOWDRIFT_HEIGHT.dp.toPx() }
        val snowdriftWidth = houseWidthPx * SNOWDRIFT_WIDTH_FACTOR

        Canvas(modifier = Modifier.fillMaxSize()) {
            // Back hill - gentle curves all the way, no straight lines
            val backHillPath = Path().apply {
                moveTo(0f, screenHeight)
                quadraticBezierTo(
                    0f, screenHeight * 0.62f,
                    screenWidth * 0.1f, screenHeight * 0.55f
                )
                quadraticBezierTo(
                    screenWidth * 0.25f, screenHeight * 0.48f,
                    screenWidth * 0.4f, screenHeight * 0.53f
                )
                quadraticBezierTo(
                    screenWidth * 0.6f, screenHeight * 0.50f,
                    screenWidth * 0.8f, screenHeight * 0.46f
                )
                quadraticBezierTo(
                    screenWidth * 0.92f, screenHeight * 0.44f,
                    screenWidth, screenHeight * 0.50f
                )
                quadraticBezierTo(
                    screenWidth, screenHeight * 0.75f,
                    screenWidth, screenHeight
                )
                close()
            }
            drawPath(backHillPath, Color(0xFFCCDDEE))

            // Front hill - gentle curves
            val frontHillPath = Path().apply {
                moveTo(0f, screenHeight)
                quadraticBezierTo(
                    0f, screenHeight * 0.68f,
                    screenWidth * 0.1f, screenHeight * 0.70f
                )
                quadraticBezierTo(
                    screenWidth * 0.25f, screenHeight * 0.64f,
                    screenWidth * 0.4f, screenHeight * 0.67f
                )
                quadraticBezierTo(
                    screenWidth * 0.55f, screenHeight * 0.58f,
                    screenWidth * 0.75f, screenHeight * 0.62f
                )
                quadraticBezierTo(
                    screenWidth * 0.9f, screenHeight * 0.66f,
                    screenWidth, screenHeight * 0.62f
                )
                quadraticBezierTo(
                    screenWidth, screenHeight * 0.81f,
                    screenWidth, screenHeight
                )
                close()
            }
            drawPath(frontHillPath, Color(0xFFE8F4FF))



            drawPineTree(this, screenWidth * 0.08f, screenHeight * 0.70f, 70f, Color(0xFF1B3A26).copy(alpha = 0.8f))
            drawPineTree(this, screenWidth * 0.18f, screenHeight * 0.64f, 90f, Color(0xFF165B33).copy(alpha = 0.9f))
            drawPineTree(this, screenWidth * 0.82f, screenHeight * 0.61f, 85f, Color(0xFF1B3A26).copy(alpha = 0.8f))
            drawPineTree(this, screenWidth * 0.92f, screenHeight * 0.64f, 60f, Color(0xFF165B33).copy(alpha = 0.7f))
        }


        Image(
            painter = painterResource(id = R.drawable.christmas_house),
            contentDescription = "House",
            modifier = Modifier
                .offset(x = with(density) { houseX.toDp() } - houseWidth / 2,
                    y = with(density) { houseBottomY.toDp() } - houseHeight)
                .size(houseWidth, houseHeight),
            contentScale = ContentScale.Fit
        )

        // Snowdrifts at base of house (drawn on top of house)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val driftCenterX = houseX
            val driftBaseY = houseBottomY - houseHeightPx * 0.22f  // Raise up into visible house area

            // Left drift
            val leftDriftPath = Path().apply {
                moveTo(driftCenterX - houseWidthPx * 0.5f, driftBaseY)
                quadraticBezierTo(
                    driftCenterX - houseWidthPx * 0.35f, driftBaseY - snowdriftHeightPx,
                    driftCenterX - houseWidthPx * 0.15f, driftBaseY
                )
                close()
            }
            drawPath(leftDriftPath, Color(0xFFF8FCFF))

            // Right drift
            val rightDriftPath = Path().apply {
                moveTo(driftCenterX + houseWidthPx * 0.1f, driftBaseY)
                quadraticBezierTo(
                    driftCenterX + houseWidthPx * 0.3f, driftBaseY - snowdriftHeightPx * 0.8f,
                    driftCenterX + houseWidthPx * 0.5f, driftBaseY
                )
                close()
            }
            drawPath(rightDriftPath, Color(0xFFF0F8FF))

            // Center small drift
            val centerDriftPath = Path().apply {
                moveTo(driftCenterX - houseWidthPx * 0.15f, driftBaseY)
                quadraticBezierTo(
                    driftCenterX, driftBaseY - snowdriftHeightPx * 0.5f,
                    driftCenterX + houseWidthPx * 0.12f, driftBaseY
                )
                close()
            }
            drawPath(centerDriftPath, Color.White)
        }

        val roofTopY = houseBottomY - houseHeightPx * 0.55f
        val roofLeftX = houseX - houseWidthPx * 0.27f
        val roofRightX = houseX + houseWidthPx * 0.3f
        val roofPeakX = houseX - houseWidthPx * 0.1f
        val roofPeakY = houseBottomY - houseHeightPx * 0.7f

        Canvas(modifier = Modifier.fillMaxSize()) {
            for (i in 0 until 6) {
                val t = i.toFloat() / 5f
                val x = roofLeftX + (roofPeakX - roofLeftX) * t
                val y = roofTopY + (roofPeakY - roofTopY) * t
                val brightness = (sin(lightPhase + i * 0.9f) + 1f) / 2f
                val color = lightColors[i % lightColors.size]
                drawCircle(
                    color.copy(alpha = brightness * 0.5f),
                    radius = 10f,
                    center = Offset(x, y)
                )
                drawCircle(
                    color.copy(alpha = 0.6f + brightness * 0.4f),
                    radius = 4f,
                    center = Offset(x, y)
                )
            }
            for (i in 0 until 6) {
                val t = i.toFloat() / 5f
                val x = roofPeakX + (roofRightX - roofPeakX) * t
                val y = roofPeakY + (roofTopY - roofPeakY) * t + 15f
                val brightness = (sin(lightPhase + i * 0.9f + 2f) + 1f) / 2f
                val color = lightColors[(i + 2) % lightColors.size]
                drawCircle(
                    color.copy(alpha = brightness * 0.5f),
                    radius = 10f,
                    center = Offset(x, y)
                )
                drawCircle(
                    color.copy(alpha = 0.6f + brightness * 0.4f),
                    radius = 4f,
                    center = Offset(x, y)
                )
            }
            val eavesY = roofTopY + 40f
            for (i in 0 until 10) {
                val t = i.toFloat() / 9f
                val x = (roofLeftX + 10f) + ((roofRightX - 10f) - (roofLeftX + 10f)) * t
                val brightness = (sin(lightPhase + i * 0.7f + 1f) + 1f) / 2f
                val color = lightColors[i % lightColors.size]
                drawCircle(
                    color.copy(alpha = brightness * 0.4f),
                    radius = 8f,
                    center = Offset(x, eavesY)
                )
                drawCircle(
                    color.copy(alpha = 0.5f + brightness * 0.5f),
                    radius = 3.5f,
                    center = Offset(x, eavesY)
                )
            }
        }
    }
}

private fun drawPineTree(
    drawScope: DrawScope,
    x: Float,
    baseY: Float,
    height: Float,
    color: Color,
) {
    with(drawScope) {
        val path = Path().apply {
            moveTo(x, baseY); lineTo(x - height * 0.4f, baseY); lineTo(
            x,
            baseY - height
        ); lineTo(x + height * 0.4f, baseY); close()
        }
        drawPath(path, color)
        drawRect(Color(0xFF4A3728), topLeft = Offset(x - 5f, baseY), size = Size(10f, 15f))
    }
}

// =============================================================================
// 🌟 SHOOTING STARS
// =============================================================================

data class XmasShootingStarData(
    val id: Int, val startX: Float, val startY: Float,
    val angle: Float, val speed: Float, val length: Float, val createdAt: Long,
)

@Composable
fun ChristmasShootingStars() {
    var shootingStars by remember { mutableStateOf(listOf<XmasShootingStarData>()) }
    var nextId by remember { mutableIntStateOf(0) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = constraints.maxWidth.toFloat()
        val screenHeight = constraints.maxHeight.toFloat()

        LaunchedEffect(Unit) {
            while (isActive) {
                delay(Random.nextLong(3000, 8000))
                shootingStars = shootingStars + XmasShootingStarData(
                    nextId++,
                    Random.nextFloat() * screenWidth * 0.7f + screenWidth * 0.1f,
                    Random.nextFloat() * screenHeight * 0.25f,
                    Random.nextFloat() * 0.5f + 0.3f,
                    Random.nextFloat() * 350f + 250f,
                    Random.nextFloat() * 50f + 35f,
                    System.currentTimeMillis()
                )
            }
        }
        LaunchedEffect(shootingStars) {
            if (shootingStars.isNotEmpty()) {
                delay(100); shootingStars =
                    shootingStars.filter { System.currentTimeMillis() - it.createdAt < 1500L }
            }
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val now = System.currentTimeMillis()
            shootingStars.forEach { s ->
                val elapsed = (now - s.createdAt) / 1000f
                val distance = elapsed * s.speed
                val progress = (elapsed / 1.5f).coerceIn(0f, 1f)
                val alpha = when {
                    progress < 0.2f -> progress / 0.2f; progress > 0.7f -> (1f - progress) / 0.3f; else -> 1f
                }
                val headX = s.startX + cos(s.angle) * distance;
                val headY = s.startY + sin(s.angle) * distance
                val tailX = headX - cos(s.angle) * s.length;
                val tailY = headY - sin(s.angle) * s.length
                val starColors = listOf(
                    listOf(
                        Color.Transparent,
                        ChristmasTheme.christmasGold.copy(alpha = alpha * 0.6f),
                        Color.White.copy(alpha = alpha)
                    ),
                    listOf(
                        Color.Transparent,
                        ChristmasTheme.christmasRed.copy(alpha = alpha * 0.5f),
                        ChristmasTheme.christmasGold.copy(alpha = alpha)
                    ),
                    listOf(
                        Color.Transparent,
                        ChristmasTheme.christmasGreen.copy(alpha = alpha * 0.5f),
                        Color.White.copy(alpha = alpha)
                    )
                )
                val colorSet = starColors[s.id % 3]
                drawLine(
                    Brush.linearGradient(
                        colorSet,
                        start = Offset(tailX, tailY),
                        end = Offset(headX, headY)
                    ),
                    Offset(tailX, tailY), Offset(headX, headY), strokeWidth = 3f
                )
                drawCircle(
                    Color.White.copy(alpha = alpha),
                    radius = 4f,
                    center = Offset(headX, headY)
                )
                drawCircle(
                    ChristmasTheme.christmasGold.copy(alpha = alpha * 0.7f),
                    radius = 6f,
                    center = Offset(headX, headY)
                )
            }
        }
    }
}

// =============================================================================
// ❄️ SNOWFLAKES & ✨ STARS
// =============================================================================

data class ChristmasSnowflakeData(
    var x: Float, var y: Float, val size: Float, val speed: Float, val drift: Float,
    val emoji: String = listOf("❄️", "❅", "❆", "✦").random(),
)

@Composable
fun ChristmasSnowflakes() {
    var snowflakes by remember { mutableStateOf(listOf<ChristmasSnowflakeData>()) }
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = constraints.maxWidth.toFloat();
        val screenHeight = constraints.maxHeight.toFloat()
        LaunchedEffect(screenWidth, screenHeight) {
            snowflakes = List(35) {
                ChristmasSnowflakeData(
                    Random.nextFloat() * screenWidth,
                    Random.nextFloat() * screenHeight,
                    Random.nextFloat() * 1.0f + 0.4f,
                    Random.nextFloat() * 1.2f + 0.4f,
                    (Random.nextFloat() - 0.5f) * 0.6f
                )
            }
        }
        LaunchedEffect(Unit) {
            while (isActive) {
                withFrameMillis {
                    snowflakes = snowflakes.map { f ->
                        var newY = f.y + f.speed * 0.5f;
                        var newX = f.x + f.drift
                        if (newY > screenHeight) {
                            newY = -20f; newX = Random.nextFloat() * screenWidth
                        }
                        if (newX < -20f) newX =
                            screenWidth + 20f; if (newX > screenWidth + 20f) newX = -20f
                        f.copy(x = newX, y = newY)
                    }
                }
            }
        }
        snowflakes.forEach { f ->
            Canvas(modifier = Modifier
                .offset(x = f.x.dp, y = f.y.dp)
                .size((f.size * 18).dp)) {
                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        f.emoji, 0f, size.height * 0.8f,
                        android.graphics.Paint()
                            .apply { textSize = f.size * 26f; isAntiAlias = true; alpha = 200 })
                }
            }
        }
    }
}

data class ChristmasStarData(
    val x: Float, val y: Float, val size: Float, val baseAlpha: Float,
    val twinkleSpeed: Float, val twinklePhase: Float, val isGold: Boolean,
)

@Composable
fun ChristmasStars() {
    var stars by remember { mutableStateOf(listOf<ChristmasStarData>()) }
    var time by remember { mutableFloatStateOf(0f) }
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = constraints.maxWidth.toFloat();
        val screenHeight = constraints.maxHeight.toFloat()
        LaunchedEffect(screenWidth, screenHeight) {
            stars = List(50) {
                ChristmasStarData(
                    Random.nextFloat() * screenWidth,
                    Random.nextFloat() * screenHeight * 0.5f,
                    Random.nextFloat() * 2.2f + 0.8f,
                    Random.nextFloat() * 0.4f + 0.4f,
                    Random.nextFloat() * 2f + 1f,
                    Random.nextFloat() * 6.28f,
                    Random.nextFloat() > 0.75f
                )
            }
        }
        LaunchedEffect(Unit) {
            while (isActive) {
                withFrameMillis { time += 0.016f }
            }
        }
        Canvas(modifier = Modifier.fillMaxSize()) {
            stars.forEach { s ->
                val alpha =
                    (s.baseAlpha + sin(time * s.twinkleSpeed + s.twinklePhase) * 0.3f).coerceIn(
                        0.2f,
                        1f
                    )
                val color = if (s.isGold) ChristmasTheme.christmasGold else Color.White
                drawCircle(color.copy(alpha = alpha), radius = s.size, center = Offset(s.x, s.y))
                if (s.size > 1.8f) drawCircle(
                    color.copy(alpha = alpha * 0.3f),
                    radius = s.size * 2.5f,
                    center = Offset(s.x, s.y)
                )
            }
        }
    }
}

// =============================================================================
// 🎄 RECORD BUTTON
// =============================================================================

@Composable
fun ChristmasRecordButton(
    isRecording: Boolean,
    countdownProgress: Float = 1f,
    onClick: () -> Unit,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ornamentGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            tween(800, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "glow"
    )
    val ornamentColor =
        if (isRecording) ChristmasTheme.christmasRed else ChristmasTheme.christmasGreen

    Box(modifier = Modifier.size(130.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier
            .size(120.dp)
            .clickable { onClick() }) {
            val centerX = size.width / 2;
            val centerY = size.height / 2 + 10f
            val radius = size.minDimension / 2 - 20f
            drawArc(
                Color.Gray.copy(alpha = 0.3f),
                -90f,
                360f,
                false,
                style = Stroke(6f),
                topLeft = Offset(centerX - radius - 5, centerY - radius - 5),
                size = Size((radius + 5) * 2, (radius + 5) * 2)
            )
            if (isRecording) drawArc(
                ChristmasTheme.christmasGold,
                -90f,
                360f * countdownProgress,
                false,
                style = Stroke(6f),
                topLeft = Offset(centerX - radius - 5, centerY - radius - 5),
                size = Size((radius + 5) * 2, (radius + 5) * 2)
            )
            drawRoundRect(
                ChristmasTheme.christmasGold,
                topLeft = Offset(centerX - 10f, centerY - radius - 10f),
                size = Size(20f, 15f),
                cornerRadius = CornerRadius(4f, 4f)
            )
            drawArc(
                ChristmasTheme.christmasGold, 180f, 180f, false, style = Stroke(3f),
                topLeft = Offset(centerX - 8f, centerY - radius - 18f), size = Size(16f, 16f)
            )
            if (isRecording) drawCircle(
                ornamentColor.copy(alpha = glowAlpha * 0.4f),
                radius = radius + 15f,
                center = Offset(centerX, centerY)
            )
            drawCircle(
                Brush.radialGradient(
                    listOf(
                        ornamentColor.copy(alpha = 0.9f), ornamentColor,
                        ornamentColor.copy(
                            red = ornamentColor.red * 0.7f,
                            green = ornamentColor.green * 0.7f,
                            blue = ornamentColor.blue * 0.7f
                        )
                    ),
                    center = Offset(centerX - radius * 0.3f, centerY - radius * 0.3f),
                    radius = radius * 1.5f
                ), radius = radius, center = Offset(centerX, centerY)
            )
            drawCircle(
                Color.White.copy(alpha = 0.4f),
                radius = radius * 0.25f,
                center = Offset(centerX - radius * 0.35f, centerY - radius * 0.35f)
            )
            drawLine(
                ChristmasTheme.christmasGold.copy(alpha = 0.6f),
                Offset(centerX - radius * 0.8f, centerY),
                Offset(centerX + radius * 0.8f, centerY),
                strokeWidth = 3f
            )
            drawStar(Offset(centerX, centerY), 12f, 5f, ChristmasTheme.christmasGold)
        }
        Text(
            text = if (isRecording) "🔴" else "🎤",
            fontSize = 20.sp,
            modifier = Modifier.offset(y = 50.dp)
        )
    }
}

fun DrawScope.drawStar(
    center: Offset,
    outerRadius: Float,
    innerRadius: Float,
    color: Color,
    points: Int = 5,
) {
    val path = Path();
    val angleStep = PI.toFloat() / points
    for (i in 0 until points * 2) {
        val radius = if (i % 2 == 0) outerRadius else innerRadius
        val angle = i * angleStep - PI.toFloat() / 2
        if (i == 0) path.moveTo(center.x + cos(angle) * radius, center.y + sin(angle) * radius)
        else path.lineTo(center.x + cos(angle) * radius, center.y + sin(angle) * radius)
    }
    path.close(); drawPath(path, color, style = Fill)
}

// =============================================================================
// 🎄 RECORDING & ATTEMPT CARDS (Guitar-style buttons)
// =============================================================================

@Composable
fun ChristmasRecordingItem(
    recording: Recording,
    aesthetic: AestheticThemeData,
    isPlaying: Boolean,
    isPaused: Boolean,
    progress: Float,
    currentlyPlayingPath: String?,
    onPlay: (String) -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit,
    onDelete: (Recording) -> Unit,
    onShare: (String) -> Unit,
    onRename: (String, String) -> Unit,
    isGameModeEnabled: Boolean,
    onStartAttempt: (Recording, ChallengeType) -> Unit,
) {
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    val isPlayingForward = currentlyPlayingPath == recording.originalPath
    val isPlayingReversed = currentlyPlayingPath == recording.reversedPath

    val cardBg = ChristmasTheme.frostBlue
    val titleBoxBg = ChristmasTheme.christmasGold.copy(alpha = 0.3f)
    val borderColor = ChristmasTheme.christmasGreen
    val iconColor = ChristmasTheme.christmasGreen
    val goldButton = ChristmasTheme.christmasGold
    val redButton = ChristmasTheme.christmasRed

    Box(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBg.copy(alpha = 0.65f), RoundedCornerShape(15.dp))
                .border(4.dp, borderColor, RoundedCornerShape(15.dp))
                .padding(16.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(titleBoxBg, RoundedCornerShape(10.dp))
                        .border(3.dp, borderColor, RoundedCornerShape(10.dp))
                        .clickable { showRenameDialog = true }
                        .padding(12.dp)
                ) {
                    Text(
                        "🎁 ${recording.name}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = borderColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (isPlayingForward || isPlayingReversed) {
                Spacer(Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = ChristmasTheme.christmasRed,
                    trackColor = ChristmasTheme.christmasGreen.copy(alpha = 0.3f)
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                ChristmasControlButton(
                    goldButton,
                    "Share",
                    { showShareDialog = true }) { ChristmasShareIcon(iconColor) }
                ChristmasControlButton(
                    redButton, when {
                        isPlayingForward && !isPaused -> "Pause"; isPlayingForward && isPaused -> "Resume"; else -> "Play"
                    },
                    { if (isPlayingForward) onPause() else onPlay(recording.originalPath) }) {
                    if (isPlayingForward && !isPaused) ChristmasPauseIcon(iconColor) else ChristmasPlayIcon(
                        iconColor
                    )
                }
                ChristmasControlButton(
                    goldButton, when {
                        isPlayingReversed && !isPaused -> "Pause"; isPlayingReversed && isPaused -> "Resume"; else -> "Rev"
                    },
                    { if (isPlayingReversed) onPause() else recording.reversedPath?.let { onPlay(it) } }) {
                    if (isPlayingReversed && !isPaused) ChristmasPauseIcon(iconColor) else ChristmasRewindIcon(
                        iconColor
                    )
                }
                if (isGameModeEnabled) {
                    ChristmasControlButton(
                        goldButton, "Try",
                        {
                            if (recording.reversedPath != null) onStartAttempt(
                                recording,
                                ChallengeType.REVERSE
                            )
                        }) { ChristmasMicIcon(iconColor) }
                }
                ChristmasControlButton(
                    redButton,
                    "Del",
                    { showDeleteDialog = true }) { ChristmasDeleteIcon(iconColor) }
            }
        }
    }
    if (showRenameDialog) aesthetic.components.RenameDialog(
        RenamableItemType.RECORDING,
        recording.name,
        aesthetic,
        { onRename(recording.originalPath, it) },
        { showRenameDialog = false })
    if (showDeleteDialog) aesthetic.components.DeleteDialog(
        DeletableItemType.RECORDING,
        recording,
        aesthetic,
        { onDelete(recording) },
        { showDeleteDialog = false })
    if (showShareDialog) aesthetic.components.ShareDialog(
        recording,
        null,
        aesthetic,
        onShare,
        { showShareDialog = false })
}

@Composable
fun ChristmasAttemptItem(
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
    onJumpToParent: (() -> Unit)?,
    onOverrideScore: ((Int) -> Unit)?,
    onResetScore: (() -> Unit)?,
) {
    val isPlayingForward = currentlyPlayingPath == attempt.attemptFilePath
    val isPlayingReversed = currentlyPlayingPath == attempt.reversedAttemptFilePath
    val isPlayingThis = isPlayingForward || isPlayingReversed

    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    var showScoreDialog by remember { mutableStateOf(false) }

    val displayScore = (attempt.finalScore ?: attempt.score).toInt()
    val scoreEmoji = aesthetic.scoreEmojis.entries.sortedByDescending { it.key }
        .firstOrNull { displayScore >= it.key }?.value ?: "🎄"

    val cardBg = ChristmasTheme.christmasRed.copy(alpha = 0.15f)
    val nameBoxBg = ChristmasTheme.christmasGold.copy(alpha = 0.3f)
    val borderColor = ChristmasTheme.christmasGreen
    val iconColor = ChristmasTheme.christmasGreen
    val goldButton = ChristmasTheme.christmasGold
    val redButton = ChristmasTheme.christmasRed

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 34.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBg, RoundedCornerShape(15.dp))
                .border(4.dp, borderColor, RoundedCornerShape(15.dp))
                .padding(12.dp)
        ) {
            Column(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                    Column(Modifier.weight(1f)) {
                        Spacer(Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (onJumpToParent != null) {
                                Box(
                                    Modifier
                                        .size(28.dp)
                                        .background(
                                            goldButton.copy(alpha = 0.5f),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .border(2.dp, borderColor, RoundedCornerShape(6.dp))
                                        .clickable { onJumpToParent() },
                                    contentAlignment = Alignment.Center
                                ) { ChristmasParentIcon(iconColor) }
                            }
                            Box(
                                Modifier
                                    .background(nameBoxBg, RoundedCornerShape(8.dp))
                                    .border(2.dp, borderColor, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                    .clickable {
                                        if (onRenamePlayer != null) showRenameDialog = true
                                    }
                            ) {
                                Text(
                                    "🧑‍🎄 ${attempt.playerName}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = borderColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Spacer(Modifier.height(22.dp))
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (onShareAttempt != null) {
                                ChristmasControlButton(
                                    goldButton,
                                    "Share",
                                    { showShareDialog = true }) { ChristmasShareIcon(iconColor) }
                            }
                            ChristmasControlButton(
                                redButton, when {
                                    isPlayingForward && !isPaused -> "Pause"; isPlayingForward && isPaused -> "Resume"; else -> "Play"
                                },
                                { if (isPlayingForward) onPause() else onPlay(attempt.attemptFilePath) }) {
                                if (isPlayingForward && !isPaused) ChristmasPauseIcon(iconColor) else ChristmasPlayIcon(
                                    iconColor
                                )
                            }
                            attempt.reversedAttemptFilePath?.let { reversedPath ->
                                ChristmasControlButton(
                                    goldButton, when {
                                        isPlayingReversed && !isPaused -> "Pause"; isPlayingReversed && isPaused -> "Resume"; else -> "Rev"
                                    },
                                    { if (isPlayingReversed) onPause() else onPlay(reversedPath) }) {
                                    if (isPlayingReversed && !isPaused) ChristmasPauseIcon(iconColor) else ChristmasRewindIcon(
                                        iconColor
                                    )
                                }
                            }
                            if (onDeleteAttempt != null) {
                                ChristmasControlButton(
                                    redButton,
                                    "Del",
                                    { showDeleteDialog = true }) { ChristmasDeleteIcon(iconColor) }
                            }
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    DifficultySquircle(
                        score = displayScore,
                        difficulty = attempt.difficulty,
                        challengeType = attempt.challengeType,
                        emoji = scoreEmoji,
                        isOverridden = attempt.finalScore != null,
                        width = 85.dp,
                        height = 110.dp,
                        onClick = { showScoreDialog = true })
                }
                if (isPlayingThis) {
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = ChristmasTheme.christmasRed,
                        trackColor = ChristmasTheme.christmasGold.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
    if (showRenameDialog && onRenamePlayer != null) aesthetic.components.RenameDialog(
        RenamableItemType.PLAYER,
        attempt.playerName,
        aesthetic,
        { onRenamePlayer(attempt, it) },
        { showRenameDialog = false })
    if (showDeleteDialog && onDeleteAttempt != null) aesthetic.components.DeleteDialog(
        DeletableItemType.ATTEMPT,
        attempt,
        aesthetic,
        { onDeleteAttempt(attempt) },
        { showDeleteDialog = false })
    if (showShareDialog && onShareAttempt != null) aesthetic.components.ShareDialog(
        null,
        attempt,
        aesthetic,
        onShareAttempt,
        { showShareDialog = false })
    if (showScoreDialog) ScoreExplanationDialog(
        attempt,
        { showScoreDialog = false },
        onOverrideScore = onOverrideScore ?: {},
        onResetScore = onResetScore ?: {})
}

@Composable
fun ChristmasControlButton(
    color: Color,
    label: String,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
) {
    val borderColor = ChristmasTheme.christmasGreen
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            Modifier
                .size(40.dp)
                .background(color, RoundedCornerShape(10.dp))
                .border(3.dp, borderColor, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) { icon() }
        Spacer(Modifier.height(1.dp))
        Text(
            label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = ChristmasTheme.snowWhite,
            textAlign = TextAlign.Center
        )
    }
}

// =============================================================================
// 🎄 CHRISTMAS ICONS (Canvas-drawn, Guitar-style)
// =============================================================================

@Composable
fun ChristmasShareIcon(color: Color) {
    Canvas(modifier = Modifier.size(24.dp)) {
        val radius = 4.dp.toPx()
        drawCircle(color, radius, Offset(size.width * 0.25f, size.height * 0.5f))
        drawCircle(color, radius, Offset(size.width * 0.75f, size.height * 0.25f))
        drawCircle(color, radius, Offset(size.width * 0.75f, size.height * 0.75f))
        drawLine(
            color,
            Offset(size.width * 0.25f, size.height * 0.5f),
            Offset(size.width * 0.75f, size.height * 0.25f),
            2.dp.toPx()
        )
        drawLine(
            color,
            Offset(size.width * 0.25f, size.height * 0.5f),
            Offset(size.width * 0.75f, size.height * 0.75f),
            2.dp.toPx()
        )
    }
}

@Composable
fun ChristmasPlayIcon(color: Color) {
    Canvas(modifier = Modifier.size(24.dp)) {
        val path = Path().apply {
            moveTo(size.width * 0.25f, size.height * 0.15f)
            lineTo(size.width * 0.25f, size.height * 0.85f)
            lineTo(size.width * 0.85f, size.height * 0.5f); close()
        }
        drawPath(
            path,
            color,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
fun ChristmasPauseIcon(color: Color) {
    Canvas(modifier = Modifier.size(24.dp)) {
        drawRoundRect(
            color,
            Offset(size.width * 0.22f, size.height * 0.15f),
            Size(size.width * 0.2f, size.height * 0.7f),
            CornerRadius(2.dp.toPx()),
            style = Stroke(2.dp.toPx())
        )
        drawRoundRect(
            color,
            Offset(size.width * 0.58f, size.height * 0.15f),
            Size(size.width * 0.2f, size.height * 0.7f),
            CornerRadius(2.dp.toPx()),
            style = Stroke(2.dp.toPx())
        )
    }
}

@Composable
fun ChristmasRewindIcon(color: Color) {
    Canvas(modifier = Modifier.size(24.dp)) {
        val path = Path().apply {
            moveTo(size.width * 0.75f, size.height * 0.15f)
            lineTo(size.width * 0.75f, size.height * 0.85f)
            lineTo(size.width * 0.15f, size.height * 0.5f); close()
        }
        drawPath(
            path,
            color,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
fun ChristmasMicIcon(color: Color) {
    Canvas(modifier = Modifier.size(24.dp)) {
        drawRoundRect(
            color,
            Offset(size.width * 0.35f, size.height * 0.15f),
            Size(size.width * 0.3f, size.height * 0.35f),
            CornerRadius(8.dp.toPx()),
            style = Stroke(2.dp.toPx())
        )
        val path = Path().apply {
            moveTo(size.width * 0.25f, size.height * 0.5f)
            quadraticBezierTo(
                size.width * 0.25f,
                size.height * 0.65f,
                size.width * 0.5f,
                size.height * 0.65f
            )
            quadraticBezierTo(
                size.width * 0.75f,
                size.height * 0.65f,
                size.width * 0.75f,
                size.height * 0.5f
            )
        }
        drawPath(path, color, style = Stroke(2.dp.toPx()))
        drawLine(
            color,
            Offset(size.width * 0.5f, size.height * 0.65f),
            Offset(size.width * 0.5f, size.height * 0.8f),
            2.dp.toPx()
        )
        drawLine(
            color,
            Offset(size.width * 0.35f, size.height * 0.8f),
            Offset(size.width * 0.65f, size.height * 0.8f),
            2.dp.toPx()
        )
    }
}

@Composable
fun ChristmasDeleteIcon(color: Color) {
    Canvas(modifier = Modifier.size(24.dp)) {
        drawLine(
            color,
            Offset(size.width * 0.2f, size.height * 0.25f),
            Offset(size.width * 0.8f, size.height * 0.25f),
            2.dp.toPx(),
            StrokeCap.Round
        )
        val lidPath = Path().apply {
            moveTo(size.width * 0.35f, size.height * 0.25f); lineTo(
            size.width * 0.35f,
            size.height * 0.15f
        )
            quadraticBezierTo(
                size.width * 0.35f,
                size.height * 0.1f,
                size.width * 0.4f,
                size.height * 0.1f
            )
            lineTo(size.width * 0.6f, size.height * 0.1f)
            quadraticBezierTo(
                size.width * 0.65f,
                size.height * 0.1f,
                size.width * 0.65f,
                size.height * 0.15f
            )
            lineTo(size.width * 0.65f, size.height * 0.25f)
        }
        drawPath(lidPath, color, style = Stroke(2.dp.toPx()))
        val canPath = Path().apply {
            moveTo(size.width * 0.25f, size.height * 0.25f); lineTo(
            size.width * 0.25f,
            size.height * 0.75f
        )
            quadraticBezierTo(
                size.width * 0.25f,
                size.height * 0.85f,
                size.width * 0.35f,
                size.height * 0.85f
            )
            lineTo(size.width * 0.65f, size.height * 0.85f)
            quadraticBezierTo(
                size.width * 0.75f,
                size.height * 0.85f,
                size.width * 0.75f,
                size.height * 0.75f
            )
            lineTo(size.width * 0.75f, size.height * 0.25f)
        }
        drawPath(canPath, color, style = Stroke(2.dp.toPx()))
    }
}

@Composable
fun ChristmasParentIcon(color: Color) {
    Canvas(modifier = Modifier.size(24.dp)) {
        val path = Path().apply {
            moveTo(size.width * 0.5f, size.height * 0.15f); lineTo(
            size.width * 0.2f,
            size.height * 0.5f
        )
            moveTo(size.width * 0.5f, size.height * 0.15f); lineTo(
            size.width * 0.8f,
            size.height * 0.5f
        )
            moveTo(size.width * 0.5f, size.height * 0.15f); lineTo(
            size.width * 0.5f,
            size.height * 0.85f
        )
        }
        drawPath(
            path,
            color,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}