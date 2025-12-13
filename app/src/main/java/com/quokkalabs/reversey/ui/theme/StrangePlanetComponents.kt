package com.quokkalabs.reversey.ui.theme

import android.content.Context
import android.media.SoundPool
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
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
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
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
import com.quokkalabs.reversey.ui.components.DifficultySquircle
import com.quokkalabs.reversey.ui.components.ScoreExplanationDialog
import com.quokkalabs.reversey.ui.constants.UiConstants
import kotlinx.coroutines.isActive
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * 🛸 STRANGE PLANET THEME
 * Inspired by Nathan Pyle's Strange Planet comics.
 * Features floating alien family, creatures, and "fabric foot tubes"
 */

// Shared recording state for creature excitement
internal var strangePlanetRecordingState = mutableStateOf(false)

object StrangePlanetTheme {
    const val THEME_ID = "strange_planet"

    // Color palette from the comics
    private val cosmicBlue = Color(0xFF6B9FD4)
    private val cosmicPurple = Color(0xFF9B7FB8)
    private val alienPink = Color(0xFFE8B4C8)
    private val deepNavy = Color(0xFF2E2A4A)
    private val softPink = Color(0xFFC77DA3)
    private val cardPink = Color(0xFFD4A5B9)

    val data = AestheticThemeData(
        id = THEME_ID,
        name = "Strange Planet",
        description = "🛸 I have detected auditory vibrations!",
        components = StrangePlanetComponents(),

        // Visuals - blue to purple gradient like the comics
        primaryGradient = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF5B8FC4),  // Top - lighter blue
                Color(0xFF6B9FD4),  // Mid blue
                Color(0xFF8B8FC8),  // Transition
                Color(0xFF9B7FB8)   // Bottom - purple
            )
        ),
        cardBorder = deepNavy.copy(alpha = 0.6f),
        primaryTextColor = deepNavy,
        secondaryTextColor = deepNavy.copy(alpha = 0.7f),
        useGlassmorphism = false,
        glowIntensity = 0f,
        recordButtonEmoji = "🪐",
        scoreEmojis = mapOf(
            90 to "🛸",
            80 to "⭐",
            70 to "🪐",
            60 to "🌙",
            0 to "💫"
        ),

        // M3 Overrides - semi-transparent cards
        cardAlpha = 0.85f,
        shadowElevation = 4f,

        // Strange Planet dialog copy!
        dialogCopy = DialogCopy(
            deleteTitle = { type ->
                if (type == DeletableItemType.RECORDING)
                    "Terminate sound capture?"
                else
                    "Erase vocal replication?"
            },
            deleteMessage = { type, name ->
                "Shall we permanently remove '$name' from existence? This action cannot be reversed."
            },
            deleteConfirmButton = "Terminate",
            deleteCancelButton = "Preserve",
            shareTitle = "Transmit to Other Beings 🛸",
            shareMessage = "Select vibration pattern for transmission:",
            renameTitle = { "Assign New Designation" },
            renameHint = "New designation"
        ),
        scoreFeedback = ScoreFeedback(
            getTitle = { score ->
                when {
                    score >= 90 -> "Exceptional Mimicry!"
                    score >= 80 -> "Satisfactory!"
                    score >= 70 -> "Acceptable!"
                    score >= 60 -> "Modest Attempt"
                    else -> "Interesting Try"
                }
            },
            getMessage = { score ->
                when {
                    score >= 90 -> "Your mouth sounds are nearly identical to the original vibrations."
                    score >= 80 -> "Adequate vocal duplication has been detected."
                    score >= 70 -> "The beings would recognize this sound pattern."
                    score >= 60 -> "Continue exercising your sound production organ."
                    else -> "Perhaps more practice with generating mouth noises?"
                }
            },
            getEmoji = { score ->
                when {
                    score >= 90 -> "🛸"
                    score >= 80 -> "⭐"
                    score >= 70 -> "🪐"
                    score >= 60 -> "🌙"
                    else -> "💫"
                }
            }
        ),
        menuColors = MenuColors(
            menuBackground = Brush.verticalGradient(
                colors = listOf(cosmicBlue, cosmicPurple)
            ),
            menuCardBackground = alienPink.copy(alpha = 0.9f),
            menuItemBackground = Color.White.copy(alpha = 0.3f),
            menuTitleText = deepNavy,
            menuItemText = deepNavy.copy(alpha = 0.8f),
            menuDivider = deepNavy.copy(alpha = 0.2f),
            menuBorder = deepNavy.copy(alpha = 0.4f),
            toggleActive = softPink,
            toggleInactive = Color.Gray
        ),
        isPro = true
    )
}

// ============================================
// 🛸 THEME COMPONENTS CLASS
// ============================================

class StrangePlanetComponents : ThemeComponents {

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
        StrangePlanetRecordingItem(
            recording = recording,
            aesthetic = aesthetic,
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
        onJumpToParent: (() -> Unit)?,
        onOverrideScore: ((Int) -> Unit)?,
onResetScore: (() -> Unit)?
    ) {
        StrangePlanetAttemptItem(
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
            onJumpToParent = onJumpToParent,
            onOverrideScore = onOverrideScore
        )
    }

    @Composable
    override fun RecordButton(
        isRecording: Boolean,
        isProcessing: Boolean,
        aesthetic: AestheticThemeData,
        onStartRecording: () -> Unit,
        onStopRecording: () -> Unit,
        countdownProgress: Float  // 🎯 PHASE 3
    ) {
        StrangePlanetRecordButton(
            isRecording = isRecording,
            countdownProgress = countdownProgress,  // 🎯 PHASE 3
            onClick = {
                if (isRecording) onStopRecording() else onStartRecording()
            }
        )
    }

    @Composable
    override fun AppBackground(
        aesthetic: AestheticThemeData,
        content: @Composable () -> Unit
    ) {
        val context = LocalContext.current
        val soundManager = remember { CreatureSoundManager(context) }

        DisposableEffect(Unit) {
            onDispose { soundManager.release() }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(aesthetic.primaryGradient)
        ) {
            StrangePlanetStars()
            StrangePlanetFloatingCreatures()
            content()

            // Invisible tap zones for creature sounds (left and right of planet)
            val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = statusBarHeight + UiConstants.TOP_APP_BAR_HEIGHT + UiConstants.SPACER_ABOVE_RECORD_BUTTON)
                    .height(UiConstants.RECORD_BUTTON_SIZE)
            ) {
                val sideWidth = (maxWidth - UiConstants.RECORD_BUTTON_SIZE) / 2

                Row(modifier = Modifier.fillMaxSize()) {
                    // Left hitbox
                    Box(
                        modifier = Modifier
                            .width(sideWidth)
                            .fillMaxHeight()
                            .pointerInput(Unit) {
                                detectTapGestures { soundManager.playRandomSound() }
                            }
                    )
                    // Center gap (planet/button area)
                    Spacer(modifier = Modifier.width(UiConstants.RECORD_BUTTON_SIZE))
                    // Right hitbox
                    Box(
                        modifier = Modifier
                            .width(sideWidth)
                            .fillMaxHeight()
                            .pointerInput(Unit) {
                                detectTapGestures { soundManager.playRandomSound() }
                            }
                    )
                }
            }
        }
    }

    // --- DIALOG IMPLEMENTATIONS ---

    @Composable
    override fun ScoreCard(attempt: PlayerAttempt, aesthetic: AestheticThemeData, onDismiss: () -> Unit, onOverrideScore: ((Int) -> Unit)) {
        ScoreExplanationDialog(attempt, onDismiss, onOverrideScore = onOverrideScore)
    }

    @Composable
    override fun DeleteDialog(
        itemType: DeletableItemType,
        item: Any,
        aesthetic: AestheticThemeData,
        onConfirm: () -> Unit,
        onDismiss: () -> Unit
    ) {
        val copy = aesthetic.dialogCopy
        val name = when (item) {
            is Recording -> item.name
            is PlayerAttempt -> item.playerName
            else -> "Item"
        }
        val cardPink = Color(0xFFE8B4C8)
        val deepNavy = Color(0xFF2E2A4A)
        val softPink = Color(0xFFC77DA3)

        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = cardPink,
            title = {
                Text(
                    copy.deleteTitle(itemType),
                    color = deepNavy,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    copy.deleteMessage(itemType, name),
                    color = deepNavy.copy(alpha = 0.8f)
                )
            },
            confirmButton = {
                Button(
                    onClick = { onConfirm(); onDismiss() },
                    colors = ButtonDefaults.buttonColors(containerColor = softPink)
                ) {
                    Text(copy.deleteConfirmButton, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(copy.deleteCancelButton, color = deepNavy)
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
        onDismiss: () -> Unit
    ) {
        val copy = aesthetic.dialogCopy
        val cardPink = Color(0xFFE8B4C8)
        val deepNavy = Color(0xFF2E2A4A)
        val softPink = Color(0xFFC77DA3)
        val cosmicPurple = Color(0xFF9B7FB8)

        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = cardPink,
            title = {
                Text(copy.shareTitle, color = deepNavy, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(copy.shareMessage, color = deepNavy.copy(alpha = 0.8f))
                    Spacer(modifier = Modifier.height(16.dp))

                    val path = recording?.originalPath ?: attempt?.attemptFilePath ?: ""
                    Button(
                        onClick = { onShare(path); onDismiss() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = softPink)
                    ) {
                        Text("Original Vibrations", color = Color.White)
                    }

                    val revPath = recording?.reversedPath ?: attempt?.reversedAttemptFilePath
                    if (revPath != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { onShare(revPath); onDismiss() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = cosmicPurple)
                        ) {
                            Text("Reversed Vibrations", color = Color.White)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Abort Transmission", color = deepNavy)
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
        onDismiss: () -> Unit
    ) {
        var name by remember { mutableStateOf(currentName) }
        val copy = aesthetic.dialogCopy
        val cardPink = Color(0xFFE8B4C8)
        val deepNavy = Color(0xFF2E2A4A)
        val softPink = Color(0xFFC77DA3)

        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = cardPink,
            title = {
                Text(copy.renameTitle(itemType), color = deepNavy, fontWeight = FontWeight.Bold)
            },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    singleLine = true,
                    label = { Text(copy.renameHint) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = softPink,
                        focusedLabelColor = softPink,
                        unfocusedBorderColor = deepNavy.copy(alpha = 0.5f),
                        unfocusedLabelColor = deepNavy.copy(alpha = 0.5f),
                        focusedTextColor = deepNavy,
                        unfocusedTextColor = deepNavy,
                        cursorColor = softPink
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = { onRename(name); onDismiss() },
                    colors = ButtonDefaults.buttonColors(containerColor = softPink)
                ) {
                    Text("Confirm", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Abort", color = deepNavy)
                }
            }
        )
    }
}

// ============================================
// 🪐 RECORD BUTTON - THE PLANET
// ============================================

@Composable
fun StrangePlanetRecordButton(
    isRecording: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    countdownProgress: Float = 1f  // 🎯 PHASE 3
) {
    // Sync to shared state so floating creatures can see it
    LaunchedEffect(isRecording) {
        strangePlanetRecordingState.value = isRecording
    }

    val planetColor = Color(0xFFE8B4C8)  // Pink planet
    val ringColor = Color(0xFF9B7FB8)     // Purple rings
    val glowColor = Color(0xFFC77DA3)     // Soft pink glow

    // Animation for ring ripples
    val infiniteTransition = rememberInfiniteTransition(label = "planetAnim")

    // Ring rotation
    val ringRotation by infiniteTransition.animateFloat(
        initialValue = -15f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            tween(3000, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "ringRotation"
    )

    // Ripple effect when recording
    val ripple1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(1500, easing = LinearEasing),
            RepeatMode.Restart
        ),
        label = "ripple1"
    )

    val ripple2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(1500, easing = LinearEasing, delayMillis = 500),
            RepeatMode.Restart
        ),
        label = "ripple2"
    )

    // Glow pulse when recording (50% larger for bigger planet)
    val glowRadius by infiniteTransition.animateFloat(
        initialValue = 105f,
        targetValue = 150f,
        animationSpec = infiniteRepeatable(
            tween(800, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "glow"
    )

    val glowAlpha by animateFloatAsState(
        targetValue = if (isRecording) 0.6f else 0.2f,
        animationSpec = tween(300),
        label = "glowAlpha"
    )

    Box(
        modifier = modifier.size(210.dp),
        contentAlignment = Alignment.Center
    ) {
        // 🎯 PHASE 3: Countdown arc
        Canvas(modifier = Modifier.size(140.dp)) {
            drawArc(
                color = Color.Gray.copy(alpha = 0.3f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        if (isRecording && countdownProgress < 1f) {
            Canvas(modifier = Modifier.size(140.dp)) {
                drawArc(
                    color = Color.Red,
                    startAngle = -90f,
                    sweepAngle = 360f * countdownProgress,
                    useCenter = false,
                    style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onClick)
        ) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            val planetRadius = size.minDimension * 0.25f  // ~52dp planet in 210dp container

            // Cosmic glow behind planet
            if (isRecording) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            glowColor.copy(alpha = glowAlpha),
                            glowColor.copy(alpha = glowAlpha * 0.5f),
                            Color.Transparent
                        ),
                        radius = glowRadius
                    ),
                    radius = glowRadius,
                    center = Offset(centerX, centerY)
                )

                // Ring ripples emanating outward
                listOf(ripple1, ripple2).forEach { progress ->
                    val rippleRadius = planetRadius + (planetRadius * 1.5f * progress)
                    val rippleAlpha = (1f - progress) * 0.5f
                    drawCircle(
                        color = ringColor.copy(alpha = rippleAlpha),
                        radius = rippleRadius,
                        center = Offset(centerX, centerY),
                        style = Stroke(width = 3.dp.toPx())
                    )
                }
            }

            // Ring dimensions (defined once, used for back and front arcs)
            val outerRingWidth = planetRadius * 2.3f
            val outerRingHeight = planetRadius * 0.5f
            val middleRingWidth = planetRadius * 1.9f
            val middleRingHeight = planetRadius * 0.4f
            val innerRingWidth = planetRadius * 1.5f
            val innerRingHeight = planetRadius * 0.32f

            // STEP 1: Draw BACK arcs (behind planet)
            rotate(degrees = ringRotation, pivot = Offset(centerX, centerY)) {
                drawArc(
                    color = ringColor,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(centerX - outerRingWidth, centerY - outerRingHeight),
                    size = androidx.compose.ui.geometry.Size(outerRingWidth * 2, outerRingHeight * 2),
                    style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                )
                drawArc(
                    color = glowColor.copy(alpha = 0.8f),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(centerX - middleRingWidth, centerY - middleRingHeight),
                    size = androidx.compose.ui.geometry.Size(middleRingWidth * 2, middleRingHeight * 2),
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                )
                drawArc(
                    color = ringColor.copy(alpha = 0.5f),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(centerX - innerRingWidth, centerY - innerRingHeight),
                    size = androidx.compose.ui.geometry.Size(innerRingWidth * 2, innerRingHeight * 2),
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // STEP 2: Draw planet body
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        planetColor,
                        planetColor.copy(alpha = 0.9f),
                        Color(0xFFD4A5B9)
                    ),
                    center = Offset(centerX - planetRadius * 0.3f, centerY - planetRadius * 0.3f),
                    radius = planetRadius * 1.5f
                ),
                radius = planetRadius,
                center = Offset(centerX, centerY)
            )

            // STEP 3: Draw FRONT arcs (in front of planet)
            rotate(degrees = ringRotation, pivot = Offset(centerX, centerY)) {
                drawArc(
                    color = ringColor,
                    startAngle = 0f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(centerX - outerRingWidth, centerY - outerRingHeight),
                    size = androidx.compose.ui.geometry.Size(outerRingWidth * 2, outerRingHeight * 2),
                    style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                )
                drawArc(
                    color = glowColor.copy(alpha = 0.8f),
                    startAngle = 0f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(centerX - middleRingWidth, centerY - middleRingHeight),
                    size = androidx.compose.ui.geometry.Size(middleRingWidth * 2, middleRingHeight * 2),
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                )
                drawArc(
                    color = ringColor.copy(alpha = 0.5f),
                    startAngle = 0f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(centerX - innerRingWidth, centerY - innerRingHeight),
                    size = androidx.compose.ui.geometry.Size(innerRingWidth * 2, innerRingHeight * 2),
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // STEP 4: Three cute craters on planet
            drawCircle(
                color = Color(0xFFD4A5B9),
                radius = planetRadius * 0.17f,
                center = Offset(centerX + planetRadius * 0.35f, centerY - planetRadius * 0.3f)
            )
            drawCircle(
                color = Color(0xFFD4A5B9).copy(alpha = 0.7f),
                radius = planetRadius * 0.12f,
                center = Offset(centerX + planetRadius * 0.45f, centerY + planetRadius * 0.15f)
            )
            drawCircle(
                color = Color(0xFFD4A5B9).copy(alpha = 0.6f),
                radius = planetRadius * 0.09f,
                center = Offset(centerX - planetRadius * 0.25f, centerY + planetRadius * 0.4f)
            )
        }
    }
}

// ============================================
// ⭐ BACKGROUND STARS
// ============================================

@Composable
fun StrangePlanetStars() {
    val infiniteTransition = rememberInfiniteTransition(label = "stars")
    val density = LocalDensity.current

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = constraints.maxWidth.toFloat()
        val screenHeight = constraints.maxHeight.toFloat()

        // 10 stars at fixed positions
        val starPositions = remember(screenWidth, screenHeight) {
            listOf(
                Offset(screenWidth * 0.08f, screenHeight * 0.12f),
                Offset(screenWidth * 0.88f, screenHeight * 0.08f),
                Offset(screenWidth * 0.45f, screenHeight * 0.18f),
                /*Offset(screenWidth * 0.72f, screenHeight * 0.28f),
                Offset(screenWidth * 0.15f, screenHeight * 0.38f),
                Offset(screenWidth * 0.92f, screenHeight * 0.42f),
                Offset(screenWidth * 0.35f, screenHeight * 0.06f),*/
                Offset(screenWidth * 0.58f, screenHeight * 0.32f),
                Offset(screenWidth * 0.05f, screenHeight * 0.25f),
                Offset(screenWidth * 0.78f, screenHeight * 0.15f)
            )
        }

        // Twinkle animation
        val twinkle by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                tween(10000, easing = LinearEasing),
                RepeatMode.Restart  // ← changed from Reverse
            ),
            label = "twinkle"
        )

        starPositions.forEachIndexed { index, offset ->
            val phase = (twinkle + index * 0.1f) % 1f
            val pulse = (kotlin.math.sin(phase * 2f * kotlin.math.PI.toFloat()) + 1f) / 2f * 0.7f + 0.3f
            val starScale = 0.8f + (pulse * 0.4f)      // Pulsing size
            val auraScale = 1.2f + (pulse * 0.6f)      // Aura pulses bigger
            val auraAlpha = 0.5f + (pulse * 0.4f)      // Strong aura

            // Aura glow
            Box(
                modifier = Modifier
                    .offset(
                        x = with(density) { (offset.x - 30.dp.toPx()).toDp() },
                        y = with(density) { (offset.y - 30.dp.toPx()).toDp() }
                    )
                    .size((60 * auraScale).dp)
                    .alpha(auraAlpha)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.9f),
                                Color.White.copy(alpha = 0.4f),
                                Color.Transparent
                            )
                        ),
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
            )

            // Star image - pulses in size, constant opacity
            Image(
                painter = painterResource(id = R.drawable.sp_star),
                contentDescription = null,
                modifier = Modifier
                    .offset(
                        x = with(density) { offset.x.toDp() },
                        y = with(density) { offset.y.toDp() }
                    )
                    .size((48 * starScale).dp)
                    .alpha(0.95f)
            )
        }
    }
}

// ============================================
// 🛸 FLOATING CREATURES SYSTEM - ZERO-G PHYSICS
// ============================================

enum class CreatureType {
    ALIEN_MUM, ALIEN_DAD, ALIEN_KID, CAT, DOG, SOCKS, UNICORN
}

data class FloatingCreature(
    val type: CreatureType,
    var x: Float,
    var y: Float,
    var vx: Float,              // Velocity X
    var vy: Float,              // Velocity Y
    val radius: Float,          // Collision radius
    val mass: Float,            // For momentum transfer
    val size: Float,            // Display size (dp)
    var rotation: Float,        // Current angle (degrees)
    var angularVelocity: Float  // Spin speed (degrees/frame)
)

class CreatureSoundManager(private val context: Context) {
    private var isSoundLoaded = false
    private val soundPool = SoundPool.Builder().setMaxStreams(4).build().apply {
        setOnLoadCompleteListener { _, _, status -> if (status == 0) isSoundLoaded = true }
    }

    private var beepBoopId = 0
    private var meowId = 0
    private var woofId = 0
    private var neighId = 0

    init {
        try {
            val res = context.resources
            val pkg = context.packageName
            beepBoopId = soundPool.load(context, res.getIdentifier("sp_beep_boop", "raw", pkg), 1)
            meowId = soundPool.load(context, res.getIdentifier("sp_meow", "raw", pkg), 1)
            woofId = soundPool.load(context, res.getIdentifier("sp_woof", "raw", pkg), 1)
            neighId = soundPool.load(context, res.getIdentifier("sp_neigh", "raw", pkg), 1)
        } catch (_: Exception) {}
    }

    fun playSound(type: CreatureType) {
        if (!isSoundLoaded) return
        val soundId = when (type) {
            CreatureType.ALIEN_MUM, CreatureType.ALIEN_DAD, CreatureType.ALIEN_KID -> beepBoopId
            CreatureType.CAT -> meowId
            CreatureType.DOG -> woofId
            CreatureType.UNICORN -> neighId
            CreatureType.SOCKS -> 0  // Socks are silent... or are they?
        }
        if (soundId != 0) {
            soundPool.play(soundId, 0.7f, 0.7f, 1, 0, 1f)
        }
    }

    fun playRandomSound() {
        if (!isSoundLoaded) return
        val sounds = listOf(beepBoopId, meowId, woofId, neighId).filter { it != 0 }
        if (sounds.isNotEmpty()) {
            soundPool.play(sounds.random(), 0.7f, 0.7f, 1, 0, 1f)
        }
    }

    fun release() {
        soundPool.release()
    }
}

@Composable
fun StrangePlanetFloatingCreatures() {
    val density = LocalDensity.current
    val context = LocalContext.current
    val soundManager = remember { CreatureSoundManager(context) }

    var creatures by remember { mutableStateOf(listOf<FloatingCreature>()) }

    // Physics constants
    val baseSpeed = 2.5f
    val restitutionNormal = 1.0f      // 100% elastic - perpetual motion
    val restitutionExcited = 0.7f     // 70% - dampens over time

    // Read recording state - affects restitution and speed
    val isExcited = strangePlanetRecordingState.value
    val restitution = if (isExcited) restitutionExcited else restitutionNormal
    val speedMultiplier = if (isExcited) 2.5f else 1f

    DisposableEffect(Unit) {
        onDispose { soundManager.release() }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = constraints.maxWidth.toFloat()
        val screenHeight = constraints.maxHeight.toFloat()

        // Initialize creatures with random velocities
        LaunchedEffect(screenWidth, screenHeight) {
            fun randomVelocity(): Float = (Random.nextFloat() - 0.5f) * baseSpeed * 2f
            fun randomSpin(): Float = (Random.nextFloat() - 0.5f) * 3f  // -1.5 to +1.5 deg/frame

            creatures = listOf(
                // type, x, y, vx, vy, radius, mass, size, rotation, angularVelocity
                FloatingCreature(CreatureType.ALIEN_MUM, screenWidth * 0.15f, screenHeight * 0.35f, randomVelocity(), randomVelocity(), 45f, 1.2f, 90f, 0f, randomSpin()),
                FloatingCreature(CreatureType.ALIEN_DAD, screenWidth * 0.25f, screenHeight * 0.40f, randomVelocity(), randomVelocity(), 48f, 1.3f, 98f, 0f, randomSpin()),
                FloatingCreature(CreatureType.ALIEN_KID, screenWidth * 0.20f, screenHeight * 0.50f, randomVelocity(), randomVelocity(), 33f, 0.8f, 68f, 0f, randomSpin() * 1.5f),
                FloatingCreature(CreatureType.CAT, screenWidth * 0.70f, screenHeight * 0.30f, randomVelocity(), randomVelocity(), 25f, 0.9f, 50f, 0f, randomSpin()),
                FloatingCreature(CreatureType.DOG, screenWidth * 0.80f, screenHeight * 0.55f, randomVelocity(), randomVelocity(), 28f, 1.0f, 55f, 0f, randomSpin()),
                FloatingCreature(CreatureType.SOCKS, screenWidth * 0.50f, screenHeight * 0.20f, randomVelocity(), randomVelocity(), 20f, 0.5f, 40f, 0f, randomSpin() * 2f),
                FloatingCreature(CreatureType.UNICORN, screenWidth * 0.60f, screenHeight * 0.65f, randomVelocity(), randomVelocity(), 35f, 1.4f, 70f, 0f, randomSpin() * 0.7f)
            )
        }

        // Physics animation loop
        LaunchedEffect(Unit) {
            while (isActive) {
                withFrameMillis {
                    val currentRestitution = if (strangePlanetRecordingState.value) restitutionExcited else restitutionNormal
                    val currentSpeedMult = if (strangePlanetRecordingState.value) 2.5f else 1f

                    // Create mutable copy for physics updates
                    val updated = creatures.map { it.copy() }.toMutableList()

                    // Update positions and rotation based on velocities
                    updated.forEachIndexed { i, c ->
                        updated[i] = c.copy(
                            x = c.x + c.vx * currentSpeedMult,
                            y = c.y + c.vy * currentSpeedMult,
                            rotation = c.rotation + c.angularVelocity * currentSpeedMult
                        )
                    }

                    // Wall collisions (bounce off edges)
                    updated.forEachIndexed { i, c ->
                        var newVx = c.vx
                        var newVy = c.vy
                        var newX = c.x
                        var newY = c.y
                        var newAngVel = c.angularVelocity

                        // Left/right walls
                        if (c.x - c.radius < 0) {
                            newX = c.radius
                            newVx = kotlin.math.abs(c.vx) * currentRestitution
                            newAngVel = -c.angularVelocity * 0.8f + c.vy * 0.1f  // Spin from impact
                        } else if (c.x + c.radius > screenWidth) {
                            newX = screenWidth - c.radius
                            newVx = -kotlin.math.abs(c.vx) * currentRestitution
                            newAngVel = -c.angularVelocity * 0.8f - c.vy * 0.1f
                        }

                        // Top/bottom walls
                        if (c.y - c.radius < 0) {
                            newY = c.radius
                            newVy = kotlin.math.abs(c.vy) * currentRestitution
                            newAngVel = -c.angularVelocity * 0.8f - c.vx * 0.1f
                        } else if (c.y + c.radius > screenHeight) {
                            newY = screenHeight - c.radius
                            newVy = -kotlin.math.abs(c.vy) * currentRestitution
                            newAngVel = -c.angularVelocity * 0.8f + c.vx * 0.1f
                        }

                        updated[i] = c.copy(x = newX, y = newY, vx = newVx, vy = newVy, angularVelocity = newAngVel)
                    }

                    // Creature-creature collisions
                    for (i in updated.indices) {
                        for (j in i + 1 until updated.size) {
                            val a = updated[i]
                            val b = updated[j]

                            val dx = b.x - a.x
                            val dy = b.y - a.y
                            val dist = kotlin.math.sqrt(dx * dx + dy * dy)
                            val minDist = a.radius + b.radius

                            if (dist < minDist && dist > 0.001f) {
                                // Collision detected - calculate elastic response
                                val nx = dx / dist  // Normal vector
                                val ny = dy / dist

                                // Relative velocity along normal
                                val dvx = a.vx - b.vx
                                val dvy = a.vy - b.vy
                                val dvn = dvx * nx + dvy * ny

                                // Skip if moving apart
                                if (dvn > 0) continue

                                // Impulse based on masses
                                val impulse = (2f * dvn) / (a.mass + b.mass) * currentRestitution

                                // Apply impulse
                                val newAvx = a.vx - impulse * b.mass * nx
                                val newAvy = a.vy - impulse * b.mass * ny
                                val newBvx = b.vx + impulse * a.mass * nx
                                val newBvy = b.vy + impulse * a.mass * ny

                                // Angular momentum transfer (lighter spins more)
                                val tangentImpulse = dvx * (-ny) + dvy * nx  // Tangent component
                                val newAangVel = a.angularVelocity + tangentImpulse * (b.mass / (a.mass + b.mass)) * 0.5f
                                val newBangVel = b.angularVelocity - tangentImpulse * (a.mass / (a.mass + b.mass)) * 0.5f

                                // Separate overlapping creatures
                                val overlap = minDist - dist
                                val sepX = overlap * nx * 0.5f
                                val sepY = overlap * ny * 0.5f

                                updated[i] = a.copy(
                                    x = a.x - sepX,
                                    y = a.y - sepY,
                                    vx = newAvx,
                                    vy = newAvy,
                                    angularVelocity = newAangVel
                                )
                                updated[j] = b.copy(
                                    x = b.x + sepX,
                                    y = b.y + sepY,
                                    vx = newBvx,
                                    vy = newBvy,
                                    angularVelocity = newBangVel
                                )
                            }
                        }
                    }

                    // If creatures are nearly stopped (excited mode drained energy), give a nudge
                    if (!strangePlanetRecordingState.value) {
                        updated.forEachIndexed { i, c ->
                            val speed = kotlin.math.sqrt(c.vx * c.vx + c.vy * c.vy)
                            if (speed < 0.3f) {
                                val angle = Random.nextFloat() * 2f * kotlin.math.PI.toFloat()
                                updated[i] = c.copy(
                                    vx = kotlin.math.cos(angle) * baseSpeed,
                                    vy = kotlin.math.sin(angle) * baseSpeed
                                )
                            }
                        }
                    }

                    creatures = updated
                }
            }
        }

        // Render creatures
        creatures.forEach { creature ->
            Box(
                modifier = Modifier
                    .offset(
                        x = with(density) { (creature.x - creature.size / 2).toDp() },
                        y = with(density) { (creature.y - creature.size / 2).toDp() }
                    )
                    .size(creature.size.dp)
                    .rotate(creature.rotation)  // Use accumulated rotation
                    .pointerInput(creature.type) {
                        android.util.Log.d("SP_TAP", "Tapped ${creature.type}")
                        detectTapGestures {
                            soundManager.playSound(creature.type)
                        }
                    }
            ) {
                val drawableRes = when (creature.type) {
                    CreatureType.ALIEN_MUM -> R.drawable.sp_alien_mum
                    CreatureType.ALIEN_DAD -> R.drawable.sp_alien_dad
                    CreatureType.ALIEN_KID -> R.drawable.sp_alien_kid
                    CreatureType.CAT -> R.drawable.sp_cat
                    CreatureType.DOG -> R.drawable.sp_dog
                    CreatureType.SOCKS -> R.drawable.sp_socks
                    CreatureType.UNICORN -> R.drawable.sp_unicorn
                }

                Image(
                    painter = painterResource(id = drawableRes),
                    contentDescription = creature.type.name,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

// ============================================
// 📼 RECORDING ITEM CARD
// ============================================

@Composable
fun StrangePlanetRecordingItem(
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

    // Card colors - semi-transparent pink
    val cardOuter = Color(0xFFE8B4C8).copy(alpha = 0.7f)
    val cardInner = Color(0xFFD4A5B9).copy(alpha = 0.5f)
    val borderColor = Color(0xFF2E2A4A).copy(alpha = 0.4f)
    val buttonPrimary = Color(0xFFC77DA3)
    val buttonSecondary = Color(0xFF9B7FB8)
    val textColor = Color(0xFF2E2A4A)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp)
            .background(cardOuter, RoundedCornerShape(20.dp))
            .border(2.dp, borderColor, RoundedCornerShape(20.dp))
            .padding(6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardInner, RoundedCornerShape(15.dp))
                .padding(8.dp)
        ) {
            // Title row with DELETE button on right (like Owl)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.White.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .clickable { showRenameDialog = true }
                        .padding(12.dp)
                ) {
                    Text(
                        text = recording.name,
                        color = textColor,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                // Delete button in top right
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(buttonSecondary.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
                        .clickable { showDeleteDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    SPDeleteGlyph(Color.White)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress bar - ALWAYS visible (like Owl)
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = buttonPrimary,
                trackColor = Color.White.copy(alpha = 0.3f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Control buttons row - SpaceEvenly (like Owl)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Share
                SPControlButton(
                    color = buttonPrimary,
                    label = "Share",
                    onClick = { showShareDialog = true }
                ) {
                    SPShareGlyph(Color.White)
                }

                // Play/Pause
                SPControlButton(
                    color = buttonSecondary,
                    label = if (isPlaying && !isPaused) "Pause" else "Play",
                    onClick = {
                        if (isPlaying && !isPaused) onPause()
                        else onPlay(recording.originalPath)
                    }
                ) {
                    if (isPlaying && !isPaused) SPPauseGlyph(Color.White)
                    else SPPlayGlyph(Color.White)
                }

                // Reversed playback
                SPControlButton(
                    color = buttonPrimary,
                    label = "Rev",
                    onClick = { recording.reversedPath?.let { onPlay(it) } }
                ) {
                    SPRewindGlyph(Color.White)
                }

                // Game mode buttons (like Owl: Fwd, Rev)
                if (isGameModeEnabled) {
                    SPControlButton(
                        color = buttonSecondary,
                        label = "Fwd",
                        onClick = { onStartAttempt(recording, ChallengeType.FORWARD) }
                    ) {
                        SPMicGlyph(Color.White)
                    }

                    SPControlButton(
                        color = buttonPrimary,
                        label = "Rev",
                        onClick = { onStartAttempt(recording, ChallengeType.REVERSE) }
                    ) {
                        SPMicGlyph(Color.White)
                    }
                }
            }
        }
    }

    // Dialogs
    if (showRenameDialog) {
        aesthetic.components.RenameDialog(
            RenamableItemType.RECORDING,
            recording.name,
            aesthetic,
            { newName -> onRename(recording.originalPath, newName) },
            { showRenameDialog = false }
        )
    }
    if (showDeleteDialog) {
        aesthetic.components.DeleteDialog(
            DeletableItemType.RECORDING,
            recording,
            aesthetic,
            { onDelete(recording) },
            { showDeleteDialog = false }
        )
    }
    if (showShareDialog) {
        aesthetic.components.ShareDialog(
            recording,
            null,
            aesthetic,
            onShare,
            { showShareDialog = false }
        )
    }
}

// ============================================
// 🎯 ATTEMPT ITEM CARD
// ============================================

@Composable
fun StrangePlanetAttemptItem(
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
    onOverrideScore: ((Int) -> Unit)?
) {
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    var showScoreDialog by remember { mutableStateOf(false) }

    val isPlayingThis = currentlyPlayingPath == attempt.attemptFilePath ||
            currentlyPlayingPath == attempt.reversedAttemptFilePath

    // Card colors
    val cardOuter = Color(0xFFD4A5B9).copy(alpha = 0.6f)
    val cardInner = Color(0xFFE8B4C8).copy(alpha = 0.4f)
    val borderColor = Color(0xFF2E2A4A).copy(alpha = 0.35f)
    val buttonPrimary = Color(0xFFC77DA3)
    val buttonSecondary = Color(0xFF9B7FB8)
    val textColor = Color(0xFF2E2A4A)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 8.dp, top = 6.dp, bottom = 6.dp)
            .background(cardOuter, RoundedCornerShape(16.dp))
            .border(2.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardInner, RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    // Player name with home button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (onJumpToParent != null) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable { onJumpToParent() }
                            ) {
                                SPHomeGlyph(textColor)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .clickable { showRenameDialog = true }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                attempt.playerName,
                                color = textColor,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Control buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (onShareAttempt != null) {
                            SPControlButton(buttonPrimary, "Share", { showShareDialog = true }) {
                                SPShareGlyph(Color.White)
                            }
                        }

                        SPControlButton(
                            buttonSecondary,
                            if (isPlayingThis && !isPaused) "Pause" else "Play",
                            { if (isPlayingThis && !isPaused) onPause() else onPlay(attempt.attemptFilePath) }
                        ) {
                            if (isPlayingThis && !isPaused) SPPauseGlyph(Color.White)
                            else SPPlayGlyph(Color.White)
                        }

                        if (attempt.reversedAttemptFilePath != null) {
                            SPControlButton(buttonPrimary, "Rev", { onPlay(attempt.reversedAttemptFilePath!!) }) {
                                SPRewindGlyph(Color.White)
                            }
                        }

                        if (onDeleteAttempt != null) {
                            SPControlButton(buttonSecondary, "Del", { showDeleteDialog = true }) {
                                SPDeleteGlyph(Color.White)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Score squircle
                DifficultySquircle(
                    attempt.score,
                    attempt.difficulty,
                    attempt.challengeType,
                    "🛸",
                    attempt.finalScore != null,
                    100.dp,
                    130.dp,
                    { showScoreDialog = true }
                )
            }

            // Progress bar
            if (isPlayingThis) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    color = buttonPrimary,
                    trackColor = Color.White.copy(alpha = 0.3f)
                )
            }
        }
    }

    // Dialogs
    if (showRenameDialog && onRenamePlayer != null) {
        aesthetic.components.RenameDialog(
            RenamableItemType.PLAYER,
            attempt.playerName,
            aesthetic,
            { onRenamePlayer(attempt, it) },
            { showRenameDialog = false }
        )
    }
    if (showDeleteDialog && onDeleteAttempt != null) {
        aesthetic.components.DeleteDialog(
            DeletableItemType.ATTEMPT,
            attempt,
            aesthetic,
            { onDeleteAttempt(attempt) },
            { showDeleteDialog = false }
        )
    }
    if (showShareDialog && onShareAttempt != null) {
        aesthetic.components.ShareDialog(
            null,
            attempt,
            aesthetic,
            onShareAttempt,
            { showShareDialog = false }
        )
    }
    if (showScoreDialog) {
        aesthetic.components.ScoreCard(attempt, aesthetic, { showScoreDialog = false }, onOverrideScore ?: { })
    }
}

// ============================================
// 🔣 ALIEN GLYPH ICONS (Matrix-style with labels)
// ============================================

@Composable
fun SPControlButton(
    color: Color,
    label: String,
    onClick: () -> Unit,
    icon: @Composable () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(color, RoundedCornerShape(10.dp))
                .border(2.dp, Color(0xFF2E2A4A).copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
        Text(
            label,
            fontSize = 9.sp,
            color = Color(0xFF2E2A4A),
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ============================================
// STYLE A: MATRIX GLYPHS (Angular + Terminal Nodes)
// Strange Planet colors retained
// ============================================

@Composable
fun SPPlayGlyph(color: Color) {
    Canvas(modifier = Modifier.size(28.dp)) {
        val strokeWidth = 2.5.dp.toPx()
        val nodeRadius = 2.dp.toPx()

        // Sharp triangle
        val path = Path().apply {
            moveTo(size.width * 0.2f, size.height * 0.15f)
            lineTo(size.width * 0.2f, size.height * 0.85f)
            lineTo(size.width * 0.85f, size.height * 0.5f)
            close()
        }
        drawPath(path, color, style = Stroke(width = strokeWidth, join = StrokeJoin.Miter))

        // Terminal dots at vertices
        drawCircle(color, nodeRadius, Offset(size.width * 0.2f, size.height * 0.15f))
        drawCircle(color, nodeRadius, Offset(size.width * 0.2f, size.height * 0.85f))
        drawCircle(color, nodeRadius, Offset(size.width * 0.85f, size.height * 0.5f))
    }
}

@Composable
fun SPPauseGlyph(color: Color) {
    Canvas(modifier = Modifier.size(28.dp)) {
        val strokeWidth = 3.dp.toPx()
        val nodeRadius = 2.dp.toPx()

        // Two vertical bars
        drawLine(color, Offset(size.width * 0.3f, size.height * 0.15f), Offset(size.width * 0.3f, size.height * 0.85f), strokeWidth = strokeWidth)
        drawLine(color, Offset(size.width * 0.7f, size.height * 0.15f), Offset(size.width * 0.7f, size.height * 0.85f), strokeWidth = strokeWidth)

        // Terminal dots at all 4 ends
        drawCircle(color, nodeRadius, Offset(size.width * 0.3f, size.height * 0.15f))
        drawCircle(color, nodeRadius, Offset(size.width * 0.3f, size.height * 0.85f))
        drawCircle(color, nodeRadius, Offset(size.width * 0.7f, size.height * 0.15f))
        drawCircle(color, nodeRadius, Offset(size.width * 0.7f, size.height * 0.85f))
    }
}

@Composable
fun SPShareGlyph(color: Color) {
    Canvas(modifier = Modifier.size(28.dp)) {
        val strokeWidth = 2.dp.toPx()
        val outerRadius = 5.dp.toPx()
        val innerRadius = 2.dp.toPx()

        // Network node circles
        drawCircle(color, outerRadius, Offset(size.width * 0.22f, size.height * 0.5f), style = Stroke(strokeWidth))
        drawCircle(color, outerRadius, Offset(size.width * 0.78f, size.height * 0.22f), style = Stroke(strokeWidth))
        drawCircle(color, outerRadius, Offset(size.width * 0.78f, size.height * 0.78f), style = Stroke(strokeWidth))

        // Connecting lines
        drawLine(color, Offset(size.width * 0.32f, size.height * 0.42f), Offset(size.width * 0.68f, size.height * 0.27f), strokeWidth = strokeWidth)
        drawLine(color, Offset(size.width * 0.32f, size.height * 0.58f), Offset(size.width * 0.68f, size.height * 0.73f), strokeWidth = strokeWidth)

        // Center dots (filled nodes)
        drawCircle(color, innerRadius, Offset(size.width * 0.22f, size.height * 0.5f))
        drawCircle(color, innerRadius, Offset(size.width * 0.78f, size.height * 0.22f))
        drawCircle(color, innerRadius, Offset(size.width * 0.78f, size.height * 0.78f))
    }
}

@Composable
fun SPRewindGlyph(color: Color) {
    Canvas(modifier = Modifier.size(28.dp)) {
        val strokeWidth = 2.5.dp.toPx()
        val nodeRadius = 2.dp.toPx()

        // Double chevrons pointing left
        val path1 = Path().apply {
            moveTo(size.width * 0.55f, size.height * 0.15f)
            lineTo(size.width * 0.25f, size.height * 0.5f)
            lineTo(size.width * 0.55f, size.height * 0.85f)
        }
        val path2 = Path().apply {
            moveTo(size.width * 0.85f, size.height * 0.15f)
            lineTo(size.width * 0.55f, size.height * 0.5f)
            lineTo(size.width * 0.85f, size.height * 0.85f)
        }
        drawPath(path1, color, style = Stroke(width = strokeWidth, join = StrokeJoin.Miter))
        drawPath(path2, color, style = Stroke(width = strokeWidth, join = StrokeJoin.Miter))

        // Terminal dots
        drawCircle(color, nodeRadius, Offset(size.width * 0.55f, size.height * 0.15f))
        drawCircle(color, nodeRadius, Offset(size.width * 0.25f, size.height * 0.5f))
        drawCircle(color, nodeRadius, Offset(size.width * 0.55f, size.height * 0.85f))
        drawCircle(color, nodeRadius, Offset(size.width * 0.85f, size.height * 0.15f))
        drawCircle(color, nodeRadius, Offset(size.width * 0.85f, size.height * 0.85f))
    }
}

@Composable
fun SPDeleteGlyph(color: Color) {
    Canvas(modifier = Modifier.size(24.dp)) {
        val strokeWidth = 2.5.dp.toPx()
        val cornerRadius = 2.dp.toPx()
        val centerRadius = 3.dp.toPx()

        // X mark
        drawLine(color, Offset(size.width * 0.2f, size.height * 0.2f), Offset(size.width * 0.8f, size.height * 0.8f), strokeWidth = strokeWidth)
        drawLine(color, Offset(size.width * 0.8f, size.height * 0.2f), Offset(size.width * 0.2f, size.height * 0.8f), strokeWidth = strokeWidth)

        // Terminal dots at 4 corners
        drawCircle(color, cornerRadius, Offset(size.width * 0.2f, size.height * 0.2f))
        drawCircle(color, cornerRadius, Offset(size.width * 0.8f, size.height * 0.8f))
        drawCircle(color, cornerRadius, Offset(size.width * 0.8f, size.height * 0.2f))
        drawCircle(color, cornerRadius, Offset(size.width * 0.2f, size.height * 0.8f))

        // Center node
        drawCircle(color, centerRadius, Offset(size.width * 0.5f, size.height * 0.5f))
    }
}

@Composable
fun SPHomeGlyph(color: Color) {
    Canvas(modifier = Modifier.size(20.dp)) {
        val strokeWidth = 2.dp.toPx()
        val nodeRadius = 2.dp.toPx()

        // Angular roof
        val roofPath = Path().apply {
            moveTo(size.width * 0.1f, size.height * 0.5f)
            lineTo(size.width * 0.5f, size.height * 0.12f)
            lineTo(size.width * 0.9f, size.height * 0.5f)
        }
        drawPath(roofPath, color, style = Stroke(width = strokeWidth, join = StrokeJoin.Miter))

        // House body
        val bodyPath = Path().apply {
            moveTo(size.width * 0.2f, size.height * 0.45f)
            lineTo(size.width * 0.2f, size.height * 0.88f)
            lineTo(size.width * 0.8f, size.height * 0.88f)
            lineTo(size.width * 0.8f, size.height * 0.45f)
        }
        drawPath(bodyPath, color, style = Stroke(width = strokeWidth, join = StrokeJoin.Miter))

        // Terminal dots at key vertices
        drawCircle(color, nodeRadius, Offset(size.width * 0.1f, size.height * 0.5f))
        drawCircle(color, nodeRadius, Offset(size.width * 0.5f, size.height * 0.12f))
        drawCircle(color, nodeRadius, Offset(size.width * 0.9f, size.height * 0.5f))
        drawCircle(color, nodeRadius, Offset(size.width * 0.2f, size.height * 0.88f))
        drawCircle(color, nodeRadius, Offset(size.width * 0.8f, size.height * 0.88f))
    }
}

@Composable
fun SPMicGlyph(color: Color) {
    Canvas(modifier = Modifier.size(28.dp)) {
        val strokeWidth = 2.dp.toPx()
        val nodeRadius = 2.dp.toPx()

        // Angular mic body (rectangular with small corner radius)
        drawRoundRect(
            color,
            topLeft = Offset(size.width * 0.35f, size.height * 0.08f),
            size = androidx.compose.ui.geometry.Size(size.width * 0.3f, size.height * 0.42f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx()),
            style = Stroke(width = strokeWidth)
        )

        // Stand arc
        val arcPath = Path().apply {
            moveTo(size.width * 0.22f, size.height * 0.42f)
            quadraticTo(size.width * 0.22f, size.height * 0.68f, size.width * 0.5f, size.height * 0.68f)
            quadraticTo(size.width * 0.78f, size.height * 0.68f, size.width * 0.78f, size.height * 0.42f)
        }
        drawPath(arcPath, color, style = Stroke(width = strokeWidth))

        // Stand line
        drawLine(color, Offset(size.width * 0.5f, size.height * 0.68f), Offset(size.width * 0.5f, size.height * 0.92f), strokeWidth = strokeWidth)

        // Terminal dots
        drawCircle(color, nodeRadius, Offset(size.width * 0.5f, size.height * 0.08f))
        drawCircle(color, nodeRadius, Offset(size.width * 0.5f, size.height * 0.92f))
        drawCircle(color, nodeRadius, Offset(size.width * 0.22f, size.height * 0.42f))
        drawCircle(color, nodeRadius, Offset(size.width * 0.78f, size.height * 0.42f))
    }
}

@Composable
fun SPMicReverseGlyph(color: Color) {
    Canvas(modifier = Modifier.size(28.dp)) {
        val strokeWidth = 2.dp.toPx()
        val nodeRadius = 2.dp.toPx()

        // Smaller angular mic on left
        drawRoundRect(
            color,
            topLeft = Offset(size.width * 0.05f, size.height * 0.12f),
            size = androidx.compose.ui.geometry.Size(size.width * 0.25f, size.height * 0.35f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx()),
            style = Stroke(width = strokeWidth)
        )

        // Chevron pointing left (reverse indicator)
        val arrowPath = Path().apply {
            moveTo(size.width * 0.75f, size.height * 0.25f)
            lineTo(size.width * 0.45f, size.height * 0.5f)
            lineTo(size.width * 0.75f, size.height * 0.75f)
        }
        drawPath(arrowPath, color, style = Stroke(width = strokeWidth, join = StrokeJoin.Miter))

        // Terminal dots
        drawCircle(color, nodeRadius, Offset(size.width * 0.175f, size.height * 0.12f))
        drawCircle(color, nodeRadius, Offset(size.width * 0.75f, size.height * 0.25f))
        drawCircle(color, nodeRadius, Offset(size.width * 0.45f, size.height * 0.5f))
        drawCircle(color, nodeRadius, Offset(size.width * 0.75f, size.height * 0.75f))
    }
}