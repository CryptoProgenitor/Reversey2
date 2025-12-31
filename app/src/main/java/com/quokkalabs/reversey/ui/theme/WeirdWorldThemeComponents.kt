package com.quokkalabs.reversey.ui.theme

import android.content.Context
import android.media.SoundPool
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
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
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * 🔬 WEIRD WORLD THEME
 * A legally safe, original theme inspired by "Scientific Observation."
 */

// ✅ SCOPED STATE: Uses CompositionLocal to avoid global singletons.
val LocalWeirdWorldExcitement = compositionLocalOf { mutableStateOf(false) }

object WeirdWorldTheme {
    const val THEME_ID = "weird_world"

    // 🎨 COLOUR PALETTE
    private val slateGrey = Color(0xFF2C3E50)
    private val seafoamGreen = Color(0xFFA8E6CF)
    private val sterileWhite = Color(0xFFF5F7FA)

    // VISIBILITY FIXES
    private val darkVoid = Color(0xFF151E27)   // Darker top for gradient contrast
    private val lightSlate = Color(0xFF4B6375) // Lighter bottom for gradient contrast
    private val brightWhite = Color(0xFFFFFFFF) // High contrast for Title/Menu

    val data = AestheticThemeData(
        id = THEME_ID,
        name = "Weird World",
        description = "🔬 Monitoring local organic vibrations...",
        components = WeirdWorldComponents(),

        // FIXED: Visible Gradient (Dark Void -> Light Slate)
        primaryGradient = Brush.verticalGradient(colors = listOf(darkVoid, lightSlate)),

        accentColor = seafoamGreen.copy(alpha = 0.3f),

        // FIXED: White text for headers/menu to be visible against dark background
        primaryTextColor = brightWhite,
        secondaryTextColor = slateGrey.copy(alpha = 0.8f),

        useGlassmorphism = false,
        glowIntensity = 0.1f,
        recordButtonEmoji = "🔬",
        scoreEmojis = mapOf(90 to "🧬", 80 to "🔭", 70 to "🧫", 60 to "📋", 0 to "🧪"),
        cardAlpha = 0.95f,
        shadowElevation = 2f,

        dialogCopy = DialogCopy(
            deleteTitle = { type -> if (type == DeletableItemType.RECORDING) "Discard auditory specimen?" else "Erase vocal profile?" },
            deleteMessage = { _, name -> "Permanently de-materialise '$name' from the archive?" },
            deleteConfirmButton = "De-materialise",
            deleteCancelButton = "Retain",
            shareTitle = "Broadcast Signal 📡",
            shareMessage = "Select frequency modulation:",
            renameTitle = { "Modify Subject Identifier" },
            renameHint = "New identifier"
        ),
        scoreFeedback = ScoreFeedback(
            getTitle = { score -> if (score >= 90) "Peak Replication!" else "Analysis Complete" },
            getMessage = { "Vocal output analyzed. Resonance efficiency calculated." },
            getEmoji = { "🔬" }
        ),
        menuColors = MenuColors(
            menuBackground = Brush.verticalGradient(colors = listOf(darkVoid, lightSlate)),
            menuCardBackground = sterileWhite,
            menuItemBackground = Color.Black.copy(alpha = 0.05f),

            // FIXED: Menu title text white for visibility
            menuTitleText = brightWhite,
            menuItemText = slateGrey.copy(alpha = 0.9f),

            menuDivider = slateGrey.copy(alpha = 0.1f),
            menuBorder = slateGrey.copy(alpha = 0.2f),
            toggleActive = seafoamGreen,
            toggleInactive = Color.Gray
        ),
        isPro = true
    )
}

class WeirdWorldComponents : ThemeComponents {

    @Composable
    override fun RecordingItem(
        recording: Recording, aesthetic: AestheticThemeData, isPaused: Boolean, progress: Float, currentlyPlayingPath: String?,
        onPlay: (String) -> Unit, onPause: () -> Unit, onStop: () -> Unit, onDelete: (Recording) -> Unit, onShare: (String) -> Unit,
        onRename: (String, String) -> Unit, isGameModeEnabled: Boolean, onStartAttempt: (Recording, ChallengeType) -> Unit,
        activeAttemptRecordingPath: String?, onStopAttempt: (() -> Unit)?
    ) {
        WeirdWorldRecordingItem(recording, aesthetic, isPaused, progress, currentlyPlayingPath, onPlay, onPause, onStop, onDelete, onShare, onRename, isGameModeEnabled, onStartAttempt, activeAttemptRecordingPath, onStopAttempt)
    }

    @Composable
    override fun AttemptItem(
        attempt: PlayerAttempt, aesthetic: AestheticThemeData, currentlyPlayingPath: String?, isPaused: Boolean, progress: Float,
        onPlay: (String) -> Unit, onPause: () -> Unit, onStop: () -> Unit, onRenamePlayer: ((PlayerAttempt, String) -> Unit)?,
        onDeleteAttempt: ((PlayerAttempt) -> Unit)?, onShareAttempt: ((String) -> Unit)?, onJumpToParent: (() -> Unit)?,
        onOverrideScore: ((Int) -> Unit)?, onResetScore: (() -> Unit)?
    ) {
        WeirdWorldAttemptItem(attempt, aesthetic, currentlyPlayingPath, isPaused, progress, onPlay, onPause, onStop, onRenamePlayer, onDeleteAttempt, onShareAttempt, onJumpToParent, onOverrideScore, onResetScore)
    }

    @Composable
    override fun RecordButton(
        isRecording: Boolean, isProcessing: Boolean, aesthetic: AestheticThemeData,
        onStartRecording: () -> Unit, onStopRecording: () -> Unit
    ) {
        WeirdWorldRecordButton(
            isRecording = isRecording,
            onClick = { if (isRecording) onStopRecording() else onStartRecording() }
        )
    }

    @Composable
    override fun AppBackground(
        aesthetic: AestheticThemeData,
        content: @Composable () -> Unit,
    ) {
        val context = LocalContext.current
        val soundManager = remember { SpecimenSoundManager(context) }

        // ✅ SCOPED STATE: Created here, survives recomposition, passed down via CompositionLocal
        val excitementState = remember { mutableStateOf(false) }

        DisposableEffect(Unit) { onDispose { soundManager.release() } }

        CompositionLocalProvider(LocalWeirdWorldExcitement provides excitementState) {
            Box(modifier = Modifier.fillMaxSize().background(aesthetic.primaryGradient)) {
                WeirdWorldFloatingMolecules()
                WeirdWorldFloatingCreatures(soundManager)
                content()

                val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
                BoxWithConstraints(
                    modifier = Modifier.fillMaxWidth()
                        .padding(top = statusBarHeight + UiConstants.TOP_APP_BAR_HEIGHT + UiConstants.SPACER_ABOVE_RECORD_BUTTON)
                        .height(UiConstants.RECORD_BUTTON_SIZE)
                ) {
                    val sideWidth = (maxWidth - UiConstants.RECORD_BUTTON_SIZE) / 2
                    Row(modifier = Modifier.fillMaxSize()) {
                        Box(modifier = Modifier.width(sideWidth).fillMaxHeight().pointerInput(Unit) { detectTapGestures { soundManager.playRandomSound() } })
                        Spacer(modifier = Modifier.width(UiConstants.RECORD_BUTTON_SIZE))
                        Box(modifier = Modifier.width(sideWidth).fillMaxHeight().pointerInput(Unit) { detectTapGestures { soundManager.playRandomSound() } })
                    }
                }
            }
        }
    }

    @Composable override fun ScoreCard(attempt: PlayerAttempt, aesthetic: AestheticThemeData, onDismiss: () -> Unit, onOverrideScore: ((Int) -> Unit)) = ScoreExplanationDialog(attempt, onDismiss, onOverrideScore = onOverrideScore)
    @Composable override fun DeleteDialog(itemType: DeletableItemType, item: Any, aesthetic: AestheticThemeData, onConfirm: () -> Unit, onDismiss: () -> Unit) = WWDeleteDialog(itemType, item, aesthetic, onConfirm, onDismiss)
    @Composable override fun ShareDialog(recording: Recording?, attempt: PlayerAttempt?, aesthetic: AestheticThemeData, onShare: (String) -> Unit, onDismiss: () -> Unit) = WWShareDialog(recording, attempt, aesthetic, onShare, onDismiss)
    @Composable override fun RenameDialog(itemType: RenamableItemType, currentName: String, aesthetic: AestheticThemeData, onRename: (String) -> Unit, onDismiss: () -> Unit) = WWRenameDialog(itemType, currentName, aesthetic, onRename, onDismiss)
}

// ============================================
// 🔬 RECORD BUTTON (Microscope Lens with Micro-Specimens)
// ============================================

// Simple data class for the button internals
private class MicroSpecimen(
    var x: Float, var y: Float,
    var dx: Float, var dy: Float,
    val size: Float,
    val color: Color
)

@Composable
fun WeirdWorldRecordButton(isRecording: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val excitementState = LocalWeirdWorldExcitement.current
    LaunchedEffect(isRecording) {
        excitementState.value = isRecording
    }

    val infiniteTransition = rememberInfiniteTransition(label = "lensAnim")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = if (isRecording) 1.05f else 1f,
        animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "pulse"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f, targetValue = if (isRecording) 1f else 0.6f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing), RepeatMode.Reverse), label = "pulseAlpha"
    )

    // 🦠 Micro-Specimens State
    val microSpecimens = remember {
        List(8) {
            MicroSpecimen(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                dx = (Random.nextFloat() - 0.5f) * 0.005f,
                dy = (Random.nextFloat() - 0.5f) * 0.005f,
                size = Random.nextFloat() * 0.15f + 0.05f,
                color = if (Random.nextBoolean()) Color(0xFFA8E6CF) else Color(0xFF34495E)
            )
        }
    }

    LaunchedEffect(Unit) {
        while (isActive) {
            withFrameMillis {
                microSpecimens.forEach { s ->
                    s.x += s.dx
                    s.y += s.dy
                    if (s.x < 0) s.x += 1f
                    if (s.x > 1) s.x -= 1f
                    if (s.y < 0) s.y += 1f
                    if (s.y > 1) s.y -= 1f
                }
            }
        }
    }

    Box(modifier = modifier.size(210.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize().clickable(onClick = onClick)) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            val currentRadius = (size.minDimension * 0.28f) * pulseScale

            drawCircle(color = Color(0xFF2C3E50), radius = currentRadius + 12.dp.toPx(), style = Stroke(width = 8.dp.toPx()))
            drawCircle(color = Color(0xFFA8E6CF).copy(alpha = pulseAlpha), radius = currentRadius, style = Stroke(width = 5.dp.toPx()))
            drawCircle(brush = Brush.radialGradient(colors = listOf(Color(0xFFA8E6CF).copy(alpha = 0.1f), Color(0xFF2C3E50).copy(alpha = 0.95f)), center = Offset(centerX, centerY), radius = currentRadius), radius = currentRadius - 2.dp.toPx())

            // Clip to lens and draw contents
            val lensPath = Path().apply {
                addOval(androidx.compose.ui.geometry.Rect(centerX - currentRadius, centerY - currentRadius, centerX + currentRadius, centerY + currentRadius))
            }

            clipPath(lensPath) {
                // Draw floating beings
                microSpecimens.forEach { s ->
                    val specX = centerX - currentRadius + (s.x * currentRadius * 2)
                    val specY = centerY - currentRadius + (s.y * currentRadius * 2)
                    val specSize = s.size * currentRadius * 0.8f

                    drawCircle(color = s.color.copy(alpha = 0.6f), radius = specSize, center = Offset(specX, specY))
                    drawCircle(color = Color.White.copy(alpha = 0.4f), radius = specSize * 0.3f, center = Offset(specX - specSize * 0.2f, specY - specSize * 0.2f))
                }

                // Scan Line
                if (isRecording) {
                    val scanProgress = (System.currentTimeMillis() % 2000) / 2000f
                    val scanY = centerY - currentRadius + (currentRadius * 2 * scanProgress)

                    // Calculate chord length to constrain line within circle
                    val dy = kotlin.math.abs(scanY - centerY)
                    if (dy < currentRadius) {
                        val halfChord = kotlin.math.sqrt((currentRadius * currentRadius) - (dy * dy))

                        drawLine(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color.Transparent, Color(0xFFF9D423).copy(alpha = 0.8f), Color.Transparent),
                                startX = centerX - halfChord,
                                endX = centerX + halfChord
                            ),
                            start = Offset(centerX - halfChord, scanY),
                            end = Offset(centerX + halfChord, scanY),
                            strokeWidth = 4.dp.toPx()
                        )
                    }
                }

                // Glass Glare
                val glarePath = Path().apply {
                    moveTo(centerX - currentRadius * 0.7f, centerY - currentRadius * 0.5f)
                    quadraticTo(centerX, centerY - currentRadius * 0.8f, centerX + currentRadius * 0.4f, centerY - currentRadius * 0.4f)
                    lineTo(centerX + currentRadius * 0.3f, centerY - currentRadius * 0.3f)
                    quadraticTo(centerX, centerY - currentRadius * 0.6f, centerX - currentRadius * 0.6f, centerY - currentRadius * 0.3f)
                    close()
                }
                drawPath(glarePath, Color.White.copy(alpha = 0.15f))
            }
        }
    }
}

// ============================================
// 🧬 PHYSICS ENGINE
// ============================================

internal enum class SpecimenType(val drawableId: Int, val baseSize: Float, val mass: Float, val initialSpeed: Float) {
    RESEARCHER_LEAD(R.drawable.ww_image_asset_1, 1.1f, 1.2f, 0.5f),
    RESEARCHER_TECH(R.drawable.ww_image_asset_2, 1.0f, 1.0f, 0.6f),
    RESEARCHER_INTERN(R.drawable.ww_image_asset_3, 0.7f, 0.5f, 0.8f),
    SPECIMEN_DOG(R.drawable.ww_image_asset_4, 0.8f, 0.8f, 0.7f),
    SPECIMEN_CAT(R.drawable.ww_image_asset_5, 0.6f, 0.4f, 0.5f),
    ANOMALY(0, 0.9f, 0.9f, 1.0f),
    BOOTS(R.drawable.ww_image_asset_7, 0.5f, 0.3f, 0.4f)
}

// ✅ CONSISTENCY: All physics properties are now Observable
internal class FloatingSpecimen(
    initialX: Float, initialY: Float,
    initialDx: Float, initialDy: Float,
    val type: SpecimenType,
    val size: Float,
    initialRotation: Float,
    val rotationSpeed: Float
) {
    var x by mutableFloatStateOf(initialX)
    var y by mutableFloatStateOf(initialY)
    var dx by mutableFloatStateOf(initialDx)
    var dy by mutableFloatStateOf(initialDy)
    var rotation by mutableFloatStateOf(initialRotation)
}

@Composable
private fun WeirdWorldFloatingCreatures(soundManager: SpecimenSoundManager) {
    val config = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidth = with(density) { config.screenWidthDp.dp.toPx() }
    val screenHeight = with(density) { config.screenHeightDp.dp.toPx() }
    val baseIconSize = with(density) { 80.dp.toPx() }

    val excitementState = LocalWeirdWorldExcitement.current

    val specimens = remember(screenWidth, screenHeight) {
        mutableStateListOf<FloatingSpecimen>().apply {
            addAll(SpecimenType.values().map { createRandomSpecimen(it, screenWidth, screenHeight, baseIconSize) })
            add(createRandomSpecimen(SpecimenType.RESEARCHER_INTERN, screenWidth, screenHeight, baseIconSize))
        }
    }

    LaunchedEffect(Unit) {
        while (isActive) {
            withFrameMillis {
                val speedMult = if (excitementState.value) 5.0f else 1.0f

                for (i in specimens.indices) {
                    val s = specimens[i]
                    s.x += s.dx * speedMult
                    s.y += s.dy * speedMult
                    s.rotation += s.rotationSpeed * speedMult

                    if (s.x < 0 || s.x > screenWidth - s.size) {
                        s.dx *= -1
                        s.x = s.x.coerceIn(0f, screenWidth - s.size)
                    }
                    if (s.y < 0 || s.y > screenHeight - s.size) {
                        s.dy *= -1
                        s.y = s.y.coerceIn(0f, screenHeight - s.size)
                    }
                }
                for (i in specimens.indices) {
                    for (j in i + 1 until specimens.size) {
                        handleCollision(specimens[i], specimens[j])
                    }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        specimens.forEach { specimen ->
            Box(
                modifier = Modifier
                    .offset(x = with(density) { specimen.x.toDp() }, y = with(density) { specimen.y.toDp() })
                    .size(with(density) { specimen.size.toDp() })
                    .rotate(specimen.rotation)
                    .pointerInput(Unit) { detectTapGestures { soundManager.playRandomSound() } }
            ) {
                if (specimen.type == SpecimenType.ANOMALY) {
                    WeirdWorldDataSwarm(modifier = Modifier.fillMaxSize())
                } else {
                    Image(painter = painterResource(id = specimen.type.drawableId), contentDescription = null, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}

private fun createRandomSpecimen(type: SpecimenType, w: Float, h: Float, base: Float): FloatingSpecimen {
    val r = Random.Default
    val size = base * type.baseSize
    return FloatingSpecimen(
        initialX = r.nextFloat() * (w - size),
        initialY = r.nextFloat() * (h - size),
        initialDx = (r.nextFloat() - 0.5f) * 2f * type.initialSpeed,
        initialDy = (r.nextFloat() - 0.5f) * 2f * type.initialSpeed,
        type = type,
        size = size,
        initialRotation = 0f,
        rotationSpeed = (r.nextFloat() - 0.5f) * 2f
    )
}

private fun handleCollision(s1: FloatingSpecimen, s2: FloatingSpecimen) {
    val dx = s2.x - s1.x
    val dy = s2.y - s1.y
    val dist = sqrt(dx * dx + dy * dy)
    val minDist = (s1.size + s2.size) / 2.5f

    if (dist < minDist && dist > 0.01f) {
        val nx = dx / dist
        val ny = dy / dist
        val p = 2 * (s1.dx * nx + s1.dy * ny - s2.dx * nx - s2.dy * ny) / (s1.type.mass + s2.type.mass)
        s1.dx -= p * s2.type.mass * nx
        s1.dy -= p * s2.type.mass * ny
        s2.dx += p * s1.type.mass * nx
        s2.dy += p * s1.type.mass * ny
        val overlap = minDist - dist
        s1.x -= overlap * nx * 0.5f
        s1.y -= overlap * ny * 0.5f
        s2.x += overlap * nx * 0.5f
        s2.y += overlap * ny * 0.5f
    }
}

// ============================================
// 🧬 HELPERS & COMPONENTS
// ============================================

@Composable
fun WeirdWorldDataSwarm(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "swarm")
    val breatheScale by infiniteTransition.animateFloat(
        initialValue = 0.8f, targetValue = 1.1f,
        animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "breathe"
    )
    val randomOffsets = remember { List(20) { Random.nextFloat() * 2f * Math.PI } }

    Canvas(modifier = modifier) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val maxRadius = size.minDimension / 2.5f
        val time = System.currentTimeMillis() / 1000f

        randomOffsets.forEachIndexed { index, offset ->
            val angle = (time * (0.5f + (index % 3) * 0.2f)) + offset.toFloat()
            val radiusOscillation = sin(time * 3f + offset.toFloat()) * 8f
            val currentRadius = (maxRadius * (0.3f + (index % 5) * 0.1f) + radiusOscillation) * breatheScale
            val x = centerX + cos(angle) * currentRadius
            val y = centerY + sin(angle) * currentRadius
            val blockSize = if (index % 4 == 0) 10.dp.toPx() else 6.dp.toPx()
            val color = if (index % 2 == 0) Color(0xFFA8E6CF) else Color(0xFF2C3E50)
            drawRect(color = color, topLeft = Offset(x - blockSize / 2, y - blockSize / 2), size = Size(blockSize, blockSize))
        }
    }
}

@Composable
private fun WeirdWorldFloatingMolecules() {
    val density = LocalDensity.current
    val config = LocalConfiguration.current
    val screenWidth = with(density) { config.screenWidthDp.dp.toPx() }
    val screenHeight = with(density) { config.screenHeightDp.dp.toPx() }

    val molecules = remember(screenWidth, screenHeight) {
        List(6) {
            FloatingSpecimen(
                initialX = Random.nextFloat() * screenWidth, initialY = Random.nextFloat() * screenHeight,
                initialDx = (Random.nextFloat() - 0.5f) * 0.5f, initialDy = (Random.nextFloat() - 0.5f) * 0.5f,
                type = SpecimenType.BOOTS, size = 120f, initialRotation = 0f, rotationSpeed = (Random.nextFloat() - 0.5f) * 0.2f
            )
        }
    }

    LaunchedEffect(Unit) {
        while (isActive) {
            withFrameMillis {
                molecules.forEach { m ->
                    m.x += m.dx; m.y += m.dy; m.rotation += m.rotationSpeed
                    if (m.x < -200) m.x = screenWidth + 100
                    if (m.x > screenWidth + 200) m.x = -100f
                    if (m.y < -200) m.y = screenHeight + 100
                    if (m.y > screenHeight + 200) m.y = -100f
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        molecules.forEach { m ->
            Image(painter = painterResource(id = R.drawable.ww_image_asset_8), contentDescription = null, modifier = Modifier.offset(x = with(density){m.x.toDp()}, y = with(density){m.y.toDp()}).size(60.dp).alpha(0.3f).rotate(m.rotation))
        }
    }
}

// ============================================
// 📼 RECORDING ITEM CARD
// ============================================

@Composable
fun WeirdWorldRecordingItem(
    recording: Recording, aesthetic: AestheticThemeData, isPaused: Boolean, progress: Float, currentlyPlayingPath: String?,
    onPlay: (String) -> Unit, onPause: () -> Unit, onStop: () -> Unit, onDelete: (Recording) -> Unit, onShare: (String) -> Unit,
    onRename: (String, String) -> Unit, isGameModeEnabled: Boolean, onStartAttempt: (Recording, ChallengeType) -> Unit,
    activeAttemptRecordingPath: String?, onStopAttempt: (() -> Unit)?
) {
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }

    val isPlayingForward = currentlyPlayingPath == recording.originalPath
    val isPlayingReversed = currentlyPlayingPath == recording.reversedPath

    // Colors: Slate and Seafoam palette - now more transparent as requested
    val cardBg = Color(0xFFF5F7FA).copy(alpha = 0.6f)
    val cardInner = Color.White.copy(alpha = 0.6f)
    val borderColor = Color(0xFF2C3E50).copy(alpha = 0.2f)
    val buttonPrimary = Color(0xFFA8E6CF) // Seafoam
    val buttonSecondary = Color(0xFF2C3E50) // Slate
    val textColor = Color(0xFF34495E)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp)
            .background(cardBg, RoundedCornerShape(20.dp))
            .border(2.dp, borderColor, RoundedCornerShape(20.dp))
            .padding(6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardInner, RoundedCornerShape(15.dp))
                .padding(8.dp)
        ) {
            // Title row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.White.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
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
            }

            // Progress bar
            if (isPlayingForward || isPlayingReversed) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = buttonPrimary,
                    trackColor = Color(0xFF2C3E50).copy(alpha = 0.1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Control buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Share
                WWControlButton(
                    color = buttonPrimary,
                    label = "Share",
                    onClick = { showShareDialog = true }
                ) { WWShareGlyph(Color(0xFF2C3E50)) }

                // Play OR Stop (Reversed)
                if (isPlayingReversed) {
                    WWControlButton(
                        color = buttonPrimary,
                        label = "Halt",
                        onClick = onStop
                    ) { WWStopGlyph(Color(0xFF2C3E50)) }
                } else {
                    WWControlButton(
                        color = buttonSecondary,
                        label = if (isPlayingForward && !isPaused) "Pause" else "Play",
                        onClick = { if (isPlayingForward) onPause() else onPlay(recording.originalPath) }
                    ) {
                        if (isPlayingForward && !isPaused) WWPauseGlyph(Color.White) else WWPlayGlyph(Color.White)
                    }
                }

                // Rev OR Stop (Forward)
                if (isPlayingForward) {
                    WWControlButton(
                        color = buttonPrimary,
                        label = "Halt",
                        onClick = onStop
                    ) { WWStopGlyph(Color(0xFF2C3E50)) }
                } else {
                    WWControlButton(
                        color = buttonPrimary,
                        label = if (isPlayingReversed && !isPaused) "Pause" else "Rev",
                        onClick = { if (isPlayingReversed) onPause() else recording.reversedPath?.let { onPlay(it) } }
                    ) {
                        if (isPlayingReversed && !isPaused) WWPauseGlyph(Color(0xFF2C3E50)) else WWRewindGlyph(Color(0xFF2C3E50))
                    }
                }

                // Try / Stop Attempt
                if (isGameModeEnabled) {
                    val isAttemptingThis = activeAttemptRecordingPath == recording.originalPath
                    if (isAttemptingThis && onStopAttempt != null) {
                        val infiniteTransition = rememberInfiniteTransition(label = "wwBlink")
                        val blink by infiniteTransition.animateFloat(
                            initialValue = 1f, targetValue = 0.5f,
                            animationSpec = infiniteRepeatable(tween(500, easing = LinearEasing), RepeatMode.Reverse), label = "blink"
                        )
                        WWControlButton(
                            color = Color(0xFFF9D423).copy(alpha = blink),
                            label = "Stop",
                            onClick = { onStopAttempt() }
                        ) { WWStopGlyph(Color(0xFF2C3E50)) }
                    } else {
                        WWControlButton(
                            color = buttonPrimary,
                            label = "Try",
                            onClick = { onStartAttempt(recording, ChallengeType.REVERSE) }
                        ) { WWMicGlyph(Color(0xFF2C3E50)) }
                    }
                }

                // Delete
                WWControlButton(
                    color = buttonSecondary,
                    label = "Purge",
                    onClick = { showDeleteDialog = true }
                ) { WWDeleteGlyph(Color.White) }
            }
        }
    }

    if (showRenameDialog) WWRenameDialog(RenamableItemType.RECORDING, recording.name, aesthetic, { onRename(recording.originalPath, it) }, { showRenameDialog = false })
    if (showDeleteDialog) WWDeleteDialog(DeletableItemType.RECORDING, recording, aesthetic, { onDelete(recording) }, { showDeleteDialog = false })
    if (showShareDialog) WWShareDialog(recording, null, aesthetic, onShare, { showShareDialog = false })
}

// ============================================
// 🎯 ATTEMPT ITEM CARD
// ============================================

@Composable
fun WeirdWorldAttemptItem(
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
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    var showScoreDialog by remember { mutableStateOf(false) }

    val isPlayingForward = currentlyPlayingPath == attempt.attemptFilePath
    val isPlayingReversed = currentlyPlayingPath == attempt.reversedAttemptFilePath
    val isPlayingThis = isPlayingForward || isPlayingReversed

    val score = (attempt.finalScore ?: attempt.score).toInt()
    val scoreEmoji = if (score > 80) "🧬" else "🧪"

    // Colors - made transparent as requested
    val cardBg = Color(0xFFF5F7FA).copy(alpha = 0.6f)
    val cardInner = Color.White.copy(alpha = 0.6f)
    val borderColor = Color(0xFF2C3E50).copy(alpha = 0.2f)
    val buttonPrimary = Color(0xFFA8E6CF)
    val buttonSecondary = Color(0xFF2C3E50)
    val textColor = Color(0xFF34495E)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 8.dp, top = 6.dp, bottom = 6.dp)
            .background(cardBg, RoundedCornerShape(16.dp))
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
                    // Header Row: Home Button + Name
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (onJumpToParent != null) {
                            Box(modifier = Modifier.size(24.dp).clickable { onJumpToParent() }) {
                                WWHomeGlyph(textColor)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                                .clickable { showRenameDialog = true }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(attempt.playerName, color = textColor, fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Controls Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        if (onShareAttempt != null) {
                            WWControlButton(buttonPrimary, "Share", { showShareDialog = true }) { WWShareGlyph(Color(0xFF2C3E50)) }
                        }

                        if (isPlayingReversed) {
                            WWControlButton(buttonPrimary, "Halt", onStop) { WWStopGlyph(Color(0xFF2C3E50)) }
                        } else {
                            WWControlButton(
                                buttonSecondary,
                                if (isPlayingForward && !isPaused) "Pause" else "Play",
                                { if (isPlayingForward) onPause() else onPlay(attempt.attemptFilePath) }
                            ) {
                                if (isPlayingForward && !isPaused) WWPauseGlyph(Color.White) else WWPlayGlyph(Color.White)
                            }
                        }

                        attempt.reversedAttemptFilePath?.let { reversedPath ->
                            if (isPlayingForward) {
                                WWControlButton(buttonPrimary, "Halt", onStop) { WWStopGlyph(Color(0xFF2C3E50)) }
                            } else {
                                WWControlButton(
                                    buttonPrimary,
                                    if (isPlayingReversed && !isPaused) "Pause" else "Rev",
                                    { if (isPlayingReversed) onPause() else onPlay(reversedPath) }
                                ) {
                                    if (isPlayingReversed && !isPaused) WWPauseGlyph(Color(0xFF2C3E50)) else WWRewindGlyph(Color(0xFF2C3E50))
                                }
                            }
                        }

                        if (onDeleteAttempt != null) {
                            WWControlButton(buttonSecondary, "Purge", { showDeleteDialog = true }) { WWDeleteGlyph(Color.White) }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                DifficultySquircle(
                    score,
                    attempt.difficulty,
                    attempt.challengeType,
                    scoreEmoji,
                    attempt.finalScore != null,
                    85.dp,
                    110.dp,
                    { showScoreDialog = true }
                )
            }

            if (isPlayingThis) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    color = buttonPrimary,
                    trackColor = Color(0xFF2C3E50).copy(alpha = 0.1f)
                )
            }
        }
    }

    if (showRenameDialog && onRenamePlayer != null) WWRenameDialog(RenamableItemType.PLAYER, attempt.playerName, aesthetic, { onRenamePlayer(attempt, it) }, { showRenameDialog = false })
    if (showDeleteDialog && onDeleteAttempt != null) WWDeleteDialog(DeletableItemType.ATTEMPT, attempt, aesthetic, { onDeleteAttempt(attempt) }, { showDeleteDialog = false })
    if (showShareDialog && onShareAttempt != null) WWShareDialog(null, attempt, aesthetic, onShareAttempt, { showShareDialog = false })
    if (showScoreDialog) ScoreExplanationDialog(attempt, { showScoreDialog = false }, onOverrideScore = onOverrideScore ?: {}, onResetScore = onResetScore ?: {})
}

// ============================================
// 🔣 GLYPHS (Ported from Strange Planet)
// ============================================

@Composable fun WWControlButton(color: Color, label: String, onClick: () -> Unit, icon: @Composable () -> Unit) { Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onClick).padding(4.dp)) { Box(Modifier.size(44.dp).background(color, RoundedCornerShape(10.dp)).border(2.dp, Color(0xFF2C3E50).copy(alpha = 0.2f), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) { icon() }; Text(label, fontSize = 9.sp, color = Color(0xFF2C3E50), fontWeight = FontWeight.SemiBold) } }

@Composable fun WWPlayGlyph(color: Color) { Canvas(modifier = Modifier.size(28.dp)) { val strokeWidth = 2.5.dp.toPx(); val nodeRadius = 2.dp.toPx(); val path = Path().apply { moveTo(size.width * 0.2f, size.height * 0.15f); lineTo(size.width * 0.2f, size.height * 0.85f); lineTo(size.width * 0.85f, size.height * 0.5f); close() }; drawPath(path, color, style = Stroke(width = strokeWidth, join = StrokeJoin.Miter)); drawCircle(color, nodeRadius, Offset(size.width * 0.2f, size.height * 0.15f)); drawCircle(color, nodeRadius, Offset(size.width * 0.2f, size.height * 0.85f)); drawCircle(color, nodeRadius, Offset(size.width * 0.85f, size.height * 0.5f)) } }
@Composable fun WWPauseGlyph(color: Color) { Canvas(modifier = Modifier.size(28.dp)) { val strokeWidth = 3.dp.toPx(); val nodeRadius = 2.dp.toPx(); drawLine(color, Offset(size.width * 0.3f, size.height * 0.15f), Offset(size.width * 0.3f, size.height * 0.85f), strokeWidth = strokeWidth); drawLine(color, Offset(size.width * 0.7f, size.height * 0.15f), Offset(size.width * 0.7f, size.height * 0.85f), strokeWidth = strokeWidth); drawCircle(color, nodeRadius, Offset(size.width * 0.3f, size.height * 0.15f)); drawCircle(color, nodeRadius, Offset(size.width * 0.3f, size.height * 0.85f)); drawCircle(color, nodeRadius, Offset(size.width * 0.7f, size.height * 0.15f)); drawCircle(color, nodeRadius, Offset(size.width * 0.7f, size.height * 0.85f)) } }
@Composable fun WWStopGlyph(color: Color) { Canvas(modifier = Modifier.size(28.dp)) { val strokeWidth = 2.5.dp.toPx(); val nodeRadius = 2.dp.toPx(); drawRect(color, topLeft = Offset(size.width * 0.2f, size.height * 0.2f), size = Size(size.width * 0.6f, size.height * 0.6f), style = Stroke(width = strokeWidth, join = StrokeJoin.Miter)); drawCircle(color, nodeRadius, Offset(size.width * 0.2f, size.height * 0.2f)); drawCircle(color, nodeRadius, Offset(size.width * 0.8f, size.height * 0.2f)); drawCircle(color, nodeRadius, Offset(size.width * 0.2f, size.height * 0.8f)); drawCircle(color, nodeRadius, Offset(size.width * 0.8f, size.height * 0.8f)) } }
@Composable fun WWShareGlyph(color: Color) { Canvas(modifier = Modifier.size(28.dp)) { val strokeWidth = 2.dp.toPx(); val outerRadius = 5.dp.toPx(); val innerRadius = 2.dp.toPx(); drawCircle(color, outerRadius, Offset(size.width * 0.22f, size.height * 0.5f), style = Stroke(strokeWidth)); drawCircle(color, outerRadius, Offset(size.width * 0.78f, size.height * 0.22f), style = Stroke(strokeWidth)); drawCircle(color, outerRadius, Offset(size.width * 0.78f, size.height * 0.78f), style = Stroke(strokeWidth)); drawLine(color, Offset(size.width * 0.32f, size.height * 0.42f), Offset(size.width * 0.68f, size.height * 0.27f), strokeWidth = strokeWidth); drawLine(color, Offset(size.width * 0.32f, size.height * 0.58f), Offset(size.width * 0.68f, size.height * 0.73f), strokeWidth = strokeWidth); drawCircle(color, innerRadius, Offset(size.width * 0.22f, size.height * 0.5f)); drawCircle(color, innerRadius, Offset(size.width * 0.78f, size.height * 0.22f)); drawCircle(color, innerRadius, Offset(size.width * 0.78f, size.height * 0.78f)) } }
@Composable fun WWRewindGlyph(color: Color) { Canvas(modifier = Modifier.size(28.dp)) { val strokeWidth = 2.5.dp.toPx(); val nodeRadius = 2.dp.toPx(); val path1 = Path().apply { moveTo(size.width * 0.55f, size.height * 0.15f); lineTo(size.width * 0.25f, size.height * 0.5f); lineTo(size.width * 0.55f, size.height * 0.85f) }; val path2 = Path().apply { moveTo(size.width * 0.85f, size.height * 0.15f); lineTo(size.width * 0.55f, size.height * 0.5f); lineTo(size.width * 0.85f, size.height * 0.85f) }; drawPath(path1, color, style = Stroke(width = strokeWidth, join = StrokeJoin.Miter)); drawPath(path2, color, style = Stroke(width = strokeWidth, join = StrokeJoin.Miter)); drawCircle(color, nodeRadius, Offset(size.width * 0.55f, size.height * 0.15f)); drawCircle(color, nodeRadius, Offset(size.width * 0.25f, size.height * 0.5f)); drawCircle(color, nodeRadius, Offset(size.width * 0.55f, size.height * 0.85f)); drawCircle(color, nodeRadius, Offset(size.width * 0.85f, size.height * 0.15f)); drawCircle(color, nodeRadius, Offset(size.width * 0.85f, size.height * 0.85f)) } }
@Composable fun WWDeleteGlyph(color: Color) { Canvas(modifier = Modifier.size(24.dp)) { val strokeWidth = 2.5.dp.toPx(); val cornerRadius = 2.dp.toPx(); val centerRadius = 3.dp.toPx(); drawLine(color, Offset(size.width * 0.2f, size.height * 0.2f), Offset(size.width * 0.8f, size.height * 0.8f), strokeWidth = strokeWidth); drawLine(color, Offset(size.width * 0.8f, size.height * 0.2f), Offset(size.width * 0.2f, size.height * 0.8f), strokeWidth = strokeWidth); drawCircle(color, cornerRadius, Offset(size.width * 0.2f, size.height * 0.2f)); drawCircle(color, cornerRadius, Offset(size.width * 0.8f, size.height * 0.8f)); drawCircle(color, cornerRadius, Offset(size.width * 0.8f, size.height * 0.2f)); drawCircle(color, cornerRadius, Offset(size.width * 0.2f, size.height * 0.8f)); drawCircle(color, centerRadius, Offset(size.width * 0.5f, size.height * 0.5f)) } }
@Composable fun WWHomeGlyph(color: Color) { Canvas(modifier = Modifier.size(20.dp)) { val strokeWidth = 2.dp.toPx(); val nodeRadius = 2.dp.toPx(); val roofPath = Path().apply { moveTo(size.width * 0.1f, size.height * 0.5f); lineTo(size.width * 0.5f, size.height * 0.12f); lineTo(size.width * 0.9f, size.height * 0.5f) }; drawPath(roofPath, color, style = Stroke(width = strokeWidth, join = StrokeJoin.Miter)); val bodyPath = Path().apply { moveTo(size.width * 0.2f, size.height * 0.45f); lineTo(size.width * 0.2f, size.height * 0.88f); lineTo(size.width * 0.8f, size.height * 0.88f); lineTo(size.width * 0.8f, size.height * 0.45f) }; drawPath(bodyPath, color, style = Stroke(width = strokeWidth, join = StrokeJoin.Miter)); drawCircle(color, nodeRadius, Offset(size.width * 0.1f, size.height * 0.5f)); drawCircle(color, nodeRadius, Offset(size.width * 0.5f, size.height * 0.12f)); drawCircle(color, nodeRadius, Offset(size.width * 0.9f, size.height * 0.5f)); drawCircle(color, nodeRadius, Offset(size.width * 0.2f, size.height * 0.88f)); drawCircle(color, nodeRadius, Offset(size.width * 0.8f, size.height * 0.88f)) } }
@Composable fun WWMicGlyph(color: Color) { Canvas(modifier = Modifier.size(28.dp)) { val strokeWidth = 2.dp.toPx(); val nodeRadius = 2.dp.toPx(); drawRoundRect(color, topLeft = Offset(size.width * 0.35f, size.height * 0.08f), size = Size(size.width * 0.3f, size.height * 0.42f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx()), style = Stroke(width = strokeWidth)); val arcPath = Path().apply { moveTo(size.width * 0.22f, size.height * 0.42f); quadraticTo(size.width * 0.22f, size.height * 0.68f, size.width * 0.5f, size.height * 0.68f); quadraticTo(size.width * 0.78f, size.height * 0.68f, size.width * 0.78f, size.height * 0.42f) }; drawPath(arcPath, color, style = Stroke(width = strokeWidth)); drawLine(color, Offset(size.width * 0.5f, size.height * 0.68f), Offset(size.width * 0.5f, size.height * 0.92f), strokeWidth = strokeWidth); drawCircle(color, nodeRadius, Offset(size.width * 0.5f, size.height * 0.08f)); drawCircle(color, nodeRadius, Offset(size.width * 0.5f, size.height * 0.92f)); drawCircle(color, nodeRadius, Offset(size.width * 0.22f, size.height * 0.42f)); drawCircle(color, nodeRadius, Offset(size.width * 0.78f, size.height * 0.42f)) } }

class SpecimenSoundManager(val context: Context) {
    private var isSoundLoaded = false
    private val soundPool = SoundPool.Builder().setMaxStreams(4).build().apply {
        setOnLoadCompleteListener { _, _, status -> if (status == 0) isSoundLoaded = true }
    }

    private var beepId = 0
    private var chirpId = 0
    private var staticId = 0

    init {
        try {
            val res = context.resources
            val pkg = context.packageName
            // Fallback strategy: try ww_audio first, else fallback to sp_audio to ensure sound
            beepId = loadSound(res, pkg, "ww_audio_beep", "sp_beep_boop")
            chirpId = loadSound(res, pkg, "ww_audio_chirp", "sp_meow")
            staticId = loadSound(res, pkg, "ww_audio_static", "sp_woof")
        } catch (_: Exception) {
            // Failsafe silent catch
        }
    }

    private fun loadSound(res: android.content.res.Resources, pkg: String, name: String, fallback: String): Int {
        var id = res.getIdentifier(name, "raw", pkg)
        if (id == 0) id = res.getIdentifier(fallback, "raw", pkg)
        return if (id != 0) soundPool.load(context, id, 1) else 0
    }

    fun playRandomSound() {
        if (!isSoundLoaded) return
        val sounds = listOf(beepId, chirpId, staticId).filter { it != 0 }
        if (sounds.isNotEmpty()) {
            soundPool.play(sounds.random(), 0.5f, 0.5f, 1, 0, 1f)
        }
    }

    fun release() {
        soundPool.release()
    }
}

@Composable
fun WWDeleteDialog(itemType: DeletableItemType, item: Any, aesthetic: AestheticThemeData, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val name = if (item is Recording) item.name else "Specimen"
    AlertDialog(
        onDismissRequest = onDismiss, containerColor = Color(0xFFF5F7FA),
        title = { Text(aesthetic.dialogCopy.deleteTitle(itemType), color = Color(0xFF2C3E50), fontWeight = FontWeight.Bold) },
        text = { Text(aesthetic.dialogCopy.deleteMessage(itemType, name), color = Color(0xFF2C3E50).copy(alpha = 0.8f)) },
        confirmButton = { Button(onClick = { onConfirm(); onDismiss() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9D423))) { Text("De-materialise", color = Color(0xFF2C3E50)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Retain", color = Color(0xFF2C3E50)) } }
    )
}
@Composable fun WWShareDialog(recording: Recording?, attempt: PlayerAttempt?, aesthetic: AestheticThemeData, onShare: (String) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss, containerColor = Color(0xFFF5F7FA),
        title = { Text(aesthetic.dialogCopy.shareTitle, color = Color(0xFF2C3E50), fontWeight = FontWeight.Bold) },
        text = { Column {
            Text(aesthetic.dialogCopy.shareMessage, color = Color(0xFF2C3E50).copy(alpha = 0.8f));
            Spacer(modifier = Modifier.height(16.dp));
            Button(onClick = { onShare(recording?.originalPath ?: attempt?.attemptFilePath ?: ""); onDismiss() }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA8E6CF))) {
                Text("Original Frequency", color = Color(0xFF2C3E50))
            }
            val revPath = recording?.reversedPath ?: attempt?.reversedAttemptFilePath
            if (revPath != null) {
                Spacer(modifier = Modifier.height(8.dp));
                Button(onClick = { onShare(revPath); onDismiss() }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34495E))) {
                    Text("Inverted Frequency", color = Color.White)
                }
            }
        } },
        confirmButton = {}, dismissButton = { TextButton(onClick = onDismiss) { Text("Abort", color = Color(0xFF2C3E50)) } }
    )
}
@Composable fun WWRenameDialog(itemType: RenamableItemType, currentName: String, aesthetic: AestheticThemeData, onRename: (String) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf(currentName) }
    AlertDialog(
        onDismissRequest = onDismiss, containerColor = Color(0xFFF5F7FA),
        title = { Text(aesthetic.dialogCopy.renameTitle(itemType), color = Color(0xFF2C3E50), fontWeight = FontWeight.Bold) },
        text = { OutlinedTextField(value = name, onValueChange = { name = it }, singleLine = true, label = { Text("New identifier") }, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFA8E6CF), focusedLabelColor = Color(0xFFA8E6CF), focusedTextColor = Color(0xFF2C3E50), unfocusedTextColor = Color(0xFF2C3E50))) },
        confirmButton = { Button(onClick = { onRename(name); onDismiss() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA8E6CF))) { Text("Confirm", color = Color(0xFF2C3E50)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Abort", color = Color(0xFF2C3E50)) } }
    )
}
