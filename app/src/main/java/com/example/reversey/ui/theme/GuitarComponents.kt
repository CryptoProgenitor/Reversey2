package com.example.reversey.ui.theme

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.reversey.data.models.ChallengeType
import com.example.reversey.data.models.PlayerAttempt
import com.example.reversey.data.models.Recording
import com.example.reversey.ui.components.DifficultySquircle

/**
 * 🎸 GUITAR THEME COMPONENTS
 * Taylor Swift Folklore/Evermore inspired theme for CPD!
 */
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
}

// ============================================
// 🎸 GUITAR RECORD BUTTON
// ============================================

@Composable
fun FloatingMusicNote(
    note: String,
    position: Offset,
    delay: Float,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sway")

    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset((delay * 1000).toInt())
        ),
        label = "swayY"
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset((delay * 1000).toInt())
        ),
        label = "swayRotation"
    )

    Text(
        text = note,
        fontSize = 32.sp,
        color = Color(0xFF5d4a36),
        modifier = modifier
            .offset(x = position.x.dp, y = (position.y + offsetY).dp)
            .graphicsLayer(rotationZ = rotation)
    )
}

@Composable
fun GuitarRecordButton(
    isRecording: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
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
            .height(140.dp)  // Compact height!
            .scale(scale)
            .clickable(
                indication = null,  // No ripple!
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() },
        contentAlignment = Alignment.TopCenter  // Changed to TopCenter!
    ) {
        // Floating music notes (20 notes with sway animation) - STAY VERTICAL!
        val musicNotes = listOf(
            // Top area
            Triple("♪", Offset(20f, 10f), 0f),
            Triple("♬", Offset(70f, 5f), 0.5f),
            Triple("♫", Offset(120f, 15f), 1f),
            Triple("♪", Offset(170f, 8f), 1.5f),
            Triple("♬", Offset(220f, 12f), 0.3f),
            Triple("♫", Offset(260f, 18f), 0.8f),

            // Left side
            Triple("♪", Offset(5f, 60f), 1.2f),
            Triple("♬", Offset(15f, 110f), 0.6f),
            Triple("♫", Offset(10f, 160f), 1.4f),
            Triple("♪", Offset(20f, 210f), 0.2f),
            Triple("♬", Offset(8f, 260f), 0.9f),

            // Right side
            Triple("♫", Offset(265f, 50f), 0.4f),
            Triple("♪", Offset(270f, 100f), 1.1f),
            Triple("♬", Offset(275f, 150f), 0.7f),
            Triple("♫", Offset(268f, 200f), 1.3f),
            Triple("♪", Offset(272f, 250f), 0.1f),

            // Bottom area
            Triple("♬", Offset(40f, 290f), 1.6f),
            Triple("♫", Offset(90f, 295f), 0.35f),
            Triple("♪", Offset(140f, 288f), 0.85f),
            Triple("♬", Offset(190f, 292f), 1.45f),
            Triple("♫", Offset(240f, 297f), 0.65f),

            // Center scattered
            Triple("♪", Offset(50f, 130f), 1.25f),
            Triple("♬", Offset(230f, 140f), 0.55f),
            Triple("♫", Offset(140f, 180f), 0.95f),
            Triple("♪", Offset(95f, 90f), 1.35f)
        )

        musicNotes.forEach { (note, position, delay) ->
            FloatingMusicNote(note = note, position = position, delay = delay)
        }

        // Pulsing glow when recording
       /* if (isRecording) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRoundRect(
                    color = Color(0xFFE8A87C).copy(alpha = pulseAlpha * 0.4f),
                    size = size,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx())
                )
            }
        }*/

        // Guitar drawing (rotated 80° clockwise, 50% smaller)
        Box(
            modifier = Modifier
                .width(110.dp)  // Scaled down
                .height(200.dp)  // Scaled down from 260dp
                .offset(x = (-24).dp, y = (-30).dp),  // Scaled offsets
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                // Save and rotate canvas 80° clockwise for jaunty angle
                drawContext.canvas.save()
                drawContext.transform.rotate(80f, pivot = Offset(canvasWidth / 2, canvasHeight / 2))

                // SVG viewBox: 166, 400, 382, 630 (scaled down 50%)
                val svgWidth = 382f
                val svgHeight = 630f
                val scaleX = canvasWidth / svgWidth
                val scaleY = canvasHeight / svgHeight

                // Helper to scale SVG coordinates to Canvas
                fun scaleX(x: Float): Float = (x - 166f) * scaleX
                fun scaleY(y: Float): Float = (y - 400f) * scaleY
                fun scalePoint(x: Float, y: Float) = Offset(scaleX(x), scaleY(y))

                // Colors
                val woodBrown = Color(0xFF8B4513)
                val darkBrown = Color(0xFF5d4a36)
                val soundHoleCream = Color(0xFFF5F1E8)

                // Stripe colors
                val stripePurple = Color(0xFFB8A8C8)
                val stripeOrange = Color(0xFFE8A87C)
                val stripeTeal = Color(0xFF7DB9A8)

                // === GUITAR NECK (ACTUALLY 50% LONGER NOW!) ===
                val neckRect = androidx.compose.ui.geometry.Rect(
                    scalePoint(335f, 190f),  // ← WAY UP! (was 345f)
                    scalePoint(379f, 555f)   // ← Bottom stays same
                )
// Neck is now 365 pixels instead of 210 = 74% LONGER!

                drawRect(
                    color = woodBrown,
                    topLeft = neckRect.topLeft,
                    size = neckRect.size
                )
                drawRect(
                    color = darkBrown,
                    topLeft = neckRect.topLeft,
                    size = neckRect.size,
                    style = Stroke(width = 2.dp.toPx())
                )

// === HEADSTOCK ===
                val headstockPath = Path().apply {
                    moveTo(scaleX(335f), scaleY(190f))
                    lineTo(scaleX(330f), scaleY(160f))
                    lineTo(scaleX(384f), scaleY(160f))
                    lineTo(scaleX(379f), scaleY(190f))
                    close()
                }
                drawPath(headstockPath, color = woodBrown)
                drawPath(headstockPath, color = darkBrown, style = Stroke(width = 2.dp.toPx()))

// === FRETS (11 FRETS!) ===
                listOf(220f, 255f, 290f, 325f, 360f, 395f, 430f, 465f, 500f, 530f, 550f).forEach { y ->
                    drawLine(
                        color = darkBrown,
                        start = scalePoint(335f, y),
                        end = scalePoint(379f, y),
                        strokeWidth = 1.5.dp.toPx()
                    )
                }

// === TUNING PEGS ===
                listOf(
                    340f to 165f,
                    340f to 175f,
                    374f to 165f,
                    374f to 175f
                ).forEach { (x, y) ->
                    drawCircle(
                        color = darkBrown,
                        radius = 4.dp.toPx(),
                        center = scalePoint(x, y)
                    )
                }

// === STRINGS ===
                listOf(350f, 355f, 360f, 365f).forEach { x ->
                    drawLine(
                        color = darkBrown.copy(alpha = 0.7f),
                        start = scalePoint(x, 160f),
                        end = scalePoint(x, 1030f),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // === EXACT SVG BODY PATH ===
                val guitarBodyPath = Path().apply {
                    moveTo(scaleX(351.936f), scaleY(553.04658f))

                    cubicTo(
                        scaleX(228.59838f), scaleY(554.76093f),
                        scaleX(209.69151f), scaleY(578.67358f),
                        scaleX(209.69151f), scaleY(620.67456f)
                    )

                    cubicTo(
                        scaleX(209.69149f), scaleY(681.43044f),
                        scaleX(222.0788f), scaleY(677.13503f),
                        scaleX(220.05686f), scaleY(726.85048f)
                    )

                    cubicTo(
                        scaleX(218.25094f), scaleY(771.25513f),
                        scaleX(167.1142f), scaleY(842.64339f),
                        scaleX(166.11718f), scaleY(910.19743f)
                    )

                    cubicTo(
                        scaleX(166.10737f), scaleY(910.86212f),
                        scaleX(166.07731f), scaleY(911.52479f),
                        scaleX(166.07731f), scaleY(912.1887f)
                    )

                    cubicTo(
                        scaleX(166.07731f), scaleY(912.37991f),
                        scaleX(166.0767f), scaleY(912.56168f),
                        scaleX(166.07731f), scaleY(912.75227f)
                    )

                    cubicTo(
                        scaleX(166.0761f), scaleY(912.96527f),
                        scaleX(166.07731f), scaleY(913.17806f),
                        scaleX(166.07731f), scaleY(913.39097f)
                    )

                    cubicTo(
                        scaleX(166.08697f), scaleY(914.80906f),
                        scaleX(166.11398f), scaleY(916.21556f),
                        scaleX(166.15705f), scaleY(917.59894f)
                    )

                    cubicTo(
                        scaleX(166.34207f), scaleY(924.64759f),
                        scaleX(166.95918f), scaleY(931.25469f),
                        scaleX(168.03078f), scaleY(937.43648f)
                    )

                    cubicTo(
                        scaleX(168.04993f), scaleY(937.55015f),
                        scaleX(168.0911f), scaleY(937.66122f),
                        scaleX(168.11051f), scaleY(937.77462f)
                    )

                    cubicTo(
                        scaleX(168.33277f), scaleY(939.03533f),
                        scaleX(168.57095f), scaleY(940.2682f),
                        scaleX(168.82812f), scaleY(941.49416f)
                    )

                    cubicTo(
                        scaleX(168.88861f), scaleY(941.7927f),
                        scaleX(168.92519f), scaleY(942.09936f),
                        scaleX(168.98758f), scaleY(942.39586f)
                    )

                    cubicTo(
                        scaleX(169.19522f), scaleY(943.34788f),
                        scaleX(169.43683f), scaleY(944.28258f),
                        scaleX(169.66532f), scaleY(945.21369f)
                    )

                    cubicTo(
                        scaleX(169.87126f), scaleY(946.0859f),
                        scaleX(170.08048f), scaleY(946.95206f),
                        scaleX(170.30318f), scaleY(947.8061f)
                    )

                    cubicTo(
                        scaleX(191.18154f), scaleY(1027.8721f),
                        scaleX(285.02482f), scaleY(1028.55f),
                        scaleX(348.18853f), scaleY(1028.5464f)
                    )

                    cubicTo(
                        scaleX(349.41853f), scaleY(1028.5463f),
                        scaleX(350.73012f), scaleY(1028.5464f),
                        scaleX(351.936f), scaleY(1028.5464f)
                    )

                    cubicTo(
                        scaleX(355.39903f), scaleY(1028.5484f),
                        scaleX(358.88595f), scaleY(1028.5464f),
                        scaleX(362.14188f), scaleY(1028.5464f)
                    )

                    cubicTo(
                        scaleX(363.41347f), scaleY(1028.5457f),
                        scaleX(364.59299f), scaleY(1028.5495f),
                        scaleX(365.88935f), scaleY(1028.5464f)
                    )

                    cubicTo(
                        scaleX(429.05306f), scaleY(1028.55f),
                        scaleX(522.89635f), scaleY(1027.8721f),
                        scaleX(543.77469f), scaleY(947.8061f)
                    )

                    cubicTo(
                        scaleX(544.15335f), scaleY(946.35404f),
                        scaleX(544.52115f), scaleY(944.87747f),
                        scaleX(544.8511f), scaleY(943.37271f)
                    )

                    cubicTo(
                        scaleX(545.03608f), scaleY(942.52906f),
                        scaleX(545.19998f), scaleY(941.67832f),
                        scaleX(545.36936f), scaleY(940.81788f)
                    )

                    cubicTo(
                        scaleX(546.82341f), scaleY(933.67372f),
                        scaleX(547.70152f), scaleY(925.95355f),
                        scaleX(547.92083f), scaleY(917.59894f)
                    )

                    cubicTo(
                        scaleX(547.97045f), scaleY(916.00565f),
                        scaleX(547.99535f), scaleY(914.39165f),
                        scaleX(548.00057f), scaleY(912.75227f)
                    )

                    cubicTo(
                        scaleX(547.99571f), scaleY(911.90008f),
                        scaleX(547.98468f), scaleY(911.05122f),
                        scaleX(547.9607f), scaleY(910.19743f)
                    )

                    cubicTo(
                        scaleX(546.96368f), scaleY(842.64339f),
                        scaleX(495.93495f), scaleY(773.91053f),
                        scaleX(494.02102f), scaleY(726.85048f)
                    )

                    cubicTo(
                        scaleX(492.10709f), scaleY(679.79043f),
                        scaleX(513.09095f), scaleY(639.23367f),
                        scaleX(500.04323f), scaleY(615.15984f)
                    )

                    cubicTo(
                        scaleX(486.407f), scaleY(590.0002f),
                        scaleX(460.66471f), scaleY(587.59929f),
                        scaleX(428.32564f), scaleY(612.6618f)
                    )

                    cubicTo(
                        scaleX(425.00447f), scaleY(615.23568f),
                        scaleX(383.4949f), scaleY(640.06295f),
                        scaleX(385.32321f), scaleY(560.89991f)
                    )

                    cubicTo(
                        scaleX(369.47384f), scaleY(558.74568f),
                        scaleX(353.6854f), scaleY(553.07089f),
                        scaleX(351.936f), scaleY(553.04658f)
                    )

                    close()
                }

                // Draw body with diagonal stripe pattern
                val stripeWidth = 6.67.dp.toPx() // Half of original 13.33

                drawContext.canvas.save()
                drawContext.canvas.clipPath(guitarBodyPath)

                // Draw rotated stripes (45° for pattern, on top of guitar's 80° rotation)
                val stripeRotation = 45f
                drawContext.transform.rotate(stripeRotation, pivot = Offset(canvasWidth / 2, canvasHeight / 2))

                var x = -canvasWidth * 2
                var colorIndex = 0
                val colors = listOf(stripePurple, stripeOrange, stripeTeal)

                while (x < canvasWidth * 3) {
                    drawRect(
                        color = colors[colorIndex % 3],
                        topLeft = Offset(x, -canvasHeight * 2),
                        size = Size(stripeWidth, canvasHeight * 4)
                    )
                    x += stripeWidth
                    colorIndex++
                }

                drawContext.canvas.restore()

                // Body border
                drawPath(
                    guitarBodyPath,
                    color = darkBrown,
                    style = Stroke(width = 2.dp.toPx())
                )

                // === SOUND HOLE (20% LARGER: ~21.6dp radius) ===
                val soundHoleCenter = scalePoint(357f, 720f)
                val soundHoleRadius = 21.6.dp.toPx() // 20% larger than 18dp!

                // Outer border
                drawCircle(
                    color = darkBrown,
                    radius = soundHoleRadius,
                    center = soundHoleCenter,
                    style = Stroke(width = 2.dp.toPx())
                )

                // Inner fill
                drawCircle(
                    color = soundHoleCream.copy(alpha = 0.9f),
                    radius = soundHoleRadius - 3.dp.toPx(),
                    center = soundHoleCenter
                )

                // === STRINGS (FROM NEW HEADSTOCK TO BOTTOM) ===
                listOf(350f, 355f, 360f, 365f).forEach { x ->
                    drawLine(
                        color = darkBrown.copy(alpha = 0.7f),
                        start = scalePoint(x, 315f),  // Start from new headstock position
                        end = scalePoint(x, 1030f),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                drawContext.canvas.restore() // Restore from 80° rotation

            } // End Canvas

            // === REC/STOP TEXT (STAYS UPRIGHT - NOT ROTATED!) ===
            Text(
                text = if (isRecording) "STOP" else "REC",
                fontSize = 16.sp,
                color = Color(0xFF5d4a36),
                fontWeight = FontWeight.Bold
            )
        } // End Box for guitar
    } // End outer Box
}

// ============================================
// 🎸 GUITAR RECORDING ITEM (CPD's Style!)
// ============================================

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
    val beigeBg = Color(0xFFC4B4A0)
    val lavenderBox = Color(0xFFB8A8C8)
    val darkBrown = Color(0xFF5d4a36)
    val tealGreen = Color(0xFF7DB9A8)
    val peachOrange = Color(0xFFE8A87C)

    var showRenameDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var newNameText by remember { mutableStateOf(recording.name) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(beigeBg, RoundedCornerShape(15.dp))
                .border(4.dp, darkBrown, RoundedCornerShape(15.dp))
                .padding(16.dp)
        ) {
            // Header: Title + Delete button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(lavenderBox, RoundedCornerShape(10.dp))
                        .border(3.dp, darkBrown, RoundedCornerShape(10.dp))
                        .clickable { showRenameDialog = true }
                        .padding(12.dp)
                ) {
                    Text(
                        text = recording.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = darkBrown,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.size(48.dp)
                ) {
                    GuitarDeleteIcon(darkBrown)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // PROGRESS BAR - CRITICAL!
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = tealGreen,  // Always visible!
                trackColor = Color(0xFFE8DCC8)  // Lighter beige for contrast
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ALL BUTTONS IN ONE ROW!
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                GuitarControlButton(
                    color = tealGreen,
                    label = "Share",
                    onClick = { showShareDialog = true }
                ) {
                    GuitarShareIcon(darkBrown)
                }

                GuitarControlButton(
                    color = peachOrange,
                    label = if (isPlaying && !isPaused) "Pause" else "Play",
                    onClick = {
                        when {
                            isPlaying && !isPaused -> onPause()
                            isPlaying && isPaused -> onPlay(recording.originalPath)
                            else -> onPlay(recording.originalPath)
                        }
                    }
                ) {
                    if (isPlaying && !isPaused) {
                        Canvas(modifier = Modifier.size(20.dp)) {
                            drawRect(
                                color = darkBrown,
                                topLeft = Offset(size.width * 0.3f, size.height * 0.2f),
                                size = Size(size.width * 0.15f, size.height * 0.6f)
                            )
                            drawRect(
                                color = darkBrown,
                                topLeft = Offset(size.width * 0.55f, size.height * 0.2f),
                                size = Size(size.width * 0.15f, size.height * 0.6f)
                            )
                        }
                    } else {
                        Canvas(modifier = Modifier.size(20.dp)) {
                            val path = Path().apply {
                                moveTo(size.width * 0.3f, size.height * 0.2f)
                                lineTo(size.width * 0.8f, size.height * 0.5f)
                                lineTo(size.width * 0.3f, size.height * 0.8f)
                                close()
                            }
                            drawPath(path = path, color = darkBrown)
                        }
                    }
                }

                GuitarControlButton(
                    color = tealGreen,
                    label = "Rev",
                    onClick = {
                        recording.reversedPath?.let { onPlay(it) }
                    }
                ) {
                    Canvas(modifier = Modifier.size(20.dp)) {
                        val path1 = Path().apply {
                            moveTo(size.width * 0.55f, size.height * 0.2f)
                            lineTo(size.width * 0.25f, size.height * 0.5f)
                            lineTo(size.width * 0.55f, size.height * 0.8f)
                            close()
                        }
                        val path2 = Path().apply {
                            moveTo(size.width * 0.8f, size.height * 0.2f)
                            lineTo(size.width * 0.5f, size.height * 0.5f)
                            lineTo(size.width * 0.8f, size.height * 0.8f)
                            close()
                        }
                        drawPath(path = path1, color = darkBrown)
                        drawPath(path = path2, color = darkBrown)
                    }
                }

                if (isGameModeEnabled) {
                    GuitarControlButton(
                        color = peachOrange,
                        label = "Fwd",
                        onClick = {
                            onStartAttempt(recording, ChallengeType.FORWARD)
                        }
                    ) {
                        // Mic + Right Arrow
                        Canvas(modifier = Modifier.size(24.dp)) {
                            // Mic body
                            drawRoundRect(
                                color = darkBrown,
                                topLeft = Offset(size.width * 0.25f, size.height * 0.15f),
                                size = Size(size.width * 0.25f, size.height * 0.3f),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx()),
                                style = Stroke(width = 1.5.dp.toPx())
                            )
                            // Mic stand
                            drawLine(
                                color = darkBrown,
                                start = Offset(size.width * 0.375f, size.height * 0.45f),
                                end = Offset(size.width * 0.375f, size.height * 0.6f),
                                strokeWidth = 1.5.dp.toPx()
                            )
                            // Mic base
                            drawLine(
                                color = darkBrown,
                                start = Offset(size.width * 0.3f, size.height * 0.6f),
                                end = Offset(size.width * 0.45f, size.height * 0.6f),
                                strokeWidth = 1.5.dp.toPx()
                            )
                            // Right Arrow
                            val arrowPath = Path().apply {
                                moveTo(size.width * 0.55f, size.height * 0.35f)
                                lineTo(size.width * 0.85f, size.height * 0.35f)
                                lineTo(size.width * 0.75f, size.height * 0.25f)
                                moveTo(size.width * 0.85f, size.height * 0.35f)
                                lineTo(size.width * 0.75f, size.height * 0.45f)
                            }
                            drawPath(
                                path = arrowPath,
                                color = darkBrown,
                                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                    }

                    GuitarControlButton(
                        color = tealGreen,
                        label = "Rev",
                        onClick = {
                            onStartAttempt(recording, ChallengeType.REVERSE)
                        }
                    ) {
                        // Mic + Left Arrow
                        Canvas(modifier = Modifier.size(24.dp)) {
                            // Mic body
                            drawRoundRect(
                                color = darkBrown,
                                topLeft = Offset(size.width * 0.5f, size.height * 0.15f),
                                size = Size(size.width * 0.25f, size.height * 0.3f),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx()),
                                style = Stroke(width = 1.5.dp.toPx())
                            )
                            // Mic stand
                            drawLine(
                                color = darkBrown,
                                start = Offset(size.width * 0.625f, size.height * 0.45f),
                                end = Offset(size.width * 0.625f, size.height * 0.6f),
                                strokeWidth = 1.5.dp.toPx()
                            )
                            // Mic base
                            drawLine(
                                color = darkBrown,
                                start = Offset(size.width * 0.55f, size.height * 0.6f),
                                end = Offset(size.width * 0.7f, size.height * 0.6f),
                                strokeWidth = 1.5.dp.toPx()
                            )
                            // Left Arrow
                            val arrowPath = Path().apply {
                                moveTo(size.width * 0.45f, size.height * 0.35f)
                                lineTo(size.width * 0.15f, size.height * 0.35f)
                                lineTo(size.width * 0.25f, size.height * 0.25f)
                                moveTo(size.width * 0.15f, size.height * 0.35f)
                                lineTo(size.width * 0.25f, size.height * 0.45f)
                            }
                            drawPath(
                                path = arrowPath,
                                color = darkBrown,
                                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showShareDialog) {
        AlertDialog(
            onDismissRequest = { showShareDialog = false },
            title = { Text("Share Recording", fontFamily = FontFamily.Serif) },
            text = { Text("Which version would you like to share?") },
            confirmButton = {
                Column {
                    TextButton(onClick = {
                        onShare(recording.originalPath)
                        showShareDialog = false
                    }) {
                        Text("Share Original (Forward)", color = tealGreen)
                    }
                    recording.reversedPath?.let {
                        TextButton(onClick = {
                            onShare(it)
                            showShareDialog = false
                        }) {
                            Text("Share Reversed", color = peachOrange)
                        }
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showShareDialog = false }) {
                    Text("Cancel", color = darkBrown)
                }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Recording?", fontFamily = FontFamily.Serif) },
            text = { Text("This will permanently delete '${recording.name}' and all its attempts.") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(recording)
                    showDeleteDialog = false
                }) {
                    Text("Delete", color = peachOrange)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = darkBrown)
                }
            }
        )
    }

    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Recording", fontFamily = FontFamily.Serif) },
            text = {
                TextField(
                    value = newNameText,
                    onValueChange = { newNameText = it },
                    label = { Text("New name") }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRename(recording.originalPath, newNameText)
                        showRenameDialog = false
                    }
                ) {
                    Text("Save", color = tealGreen)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel", color = darkBrown)
                }
            }
        )
    }
}
@Composable
private fun GuitarAttemptItem(
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

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 34.dp, end = 16.dp, top = 8.dp, bottom = 8.dp) // MATCH EGG INDENTATION
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = lavenderPurple,
                    shape = RoundedCornerShape(15.dp)
                )
                .border(
                    width = 4.dp,
                    color = darkBrown,
                    shape = RoundedCornerShape(15.dp)
                )
                .padding(12.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    // Left side: Player name + buttons
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Spacer(modifier = Modifier.height(10.dp))

                        // Jump to parent icon OUTSIDE + Player name box
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Jump to parent icon - OUTSIDE LEFT
                            if (onJumpToParent != null) {
                                Icon(
                                    imageVector = Icons.Default.ArrowUpward,
                                    contentDescription = "Jump to recording",
                                    tint = darkBrown,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clickable { onJumpToParent() }
                                )
                            }

                            // Player name box
                            Box(
                                modifier = Modifier
                                    .background(peachOrange.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                    .border(2.dp, darkBrown, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                    .clickable { showRenameDialog = true } // SHORT CLICK for rename
                            ) {
                                Text(
                                    text = attempt.playerName,
                                    style = TextStyle(
                                        fontFamily = FontFamily.Serif,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = darkBrown
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.widthIn(max = 120.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(22.dp))

                        // Control buttons with labels
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (onShareAttempt != null) {
                                GuitarControlButton(
                                    onClick = { showShareDialog = true },
                                    color = tealGreen,
                                    label = "Share"
                                ) { GuitarShareIcon(color = darkBrown) }
                            }

                            GuitarControlButton(
                                onClick = {
                                    if (isPlayingThis && !isPaused) onPause() else onPlay(attempt.attemptFilePath)
                                },
                                color = peachOrange,
                                label = if (isPlayingThis && !isPaused) "Pause" else "Play"
                            ) {
                                Icon(
                                    imageVector = if (isPlayingThis && !isPaused) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Play",
                                    tint = darkBrown
                                )
                            }

                            if (attempt.reversedAttemptFilePath != null) {
                                GuitarControlButton(
                                    onClick = { onPlay(attempt.reversedAttemptFilePath!!) },
                                    color = tealGreen,
                                    label = "Rev"
                                ) {
                                    Canvas(modifier = Modifier.size(20.dp)) {
                                        val path1 = Path().apply {
                                            moveTo(size.width * 0.55f, size.height * 0.2f)
                                            lineTo(size.width * 0.25f, size.height * 0.5f)
                                            lineTo(size.width * 0.55f, size.height * 0.8f)
                                            close()
                                        }
                                        val path2 = Path().apply {
                                            moveTo(size.width * 0.8f, size.height * 0.2f)
                                            lineTo(size.width * 0.5f, size.height * 0.5f)
                                            lineTo(size.width * 0.8f, size.height * 0.8f)
                                            close()
                                        }
                                        drawPath(path = path1, color = darkBrown)
                                        drawPath(path = path2, color = darkBrown)
                                    }
                                }
                            }

                            if (onDeleteAttempt != null) {
                                GuitarControlButton(
                                    onClick = { showDeleteDialog = true },
                                    color = peachOrange,
                                    label = "Del"
                                ) { GuitarDeleteIcon(color = darkBrown) }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Right: DifficultySquircle (matching egg theme)
                    DifficultySquircle(
                        score = attempt.score.toInt(),
                        difficulty = attempt.difficulty,
                        challengeType = attempt.challengeType,
                        emoji = "🎸",
                        width = 100.dp,
                        height = 130.dp,
                        onClick = { showScoreDialog = true }
                    )
                }

                // Progress bar at bottom
                if (isPlayingThis) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = tealGreen,
                        trackColor = peachOrange.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }

    if (showShareDialog && onShareAttempt != null) {
        AlertDialog(
            onDismissRequest = { showShareDialog = false },
            title = { Text("Share Attempt", fontFamily = FontFamily.Serif) },
            text = { Text("Which version would you like to share?") },
            confirmButton = {
                Column {
                    TextButton(onClick = {
                        onShareAttempt(attempt.attemptFilePath)
                        showShareDialog = false
                    }) {
                        Text("Share Original Attempt", color = tealGreen)
                    }
                    attempt.reversedAttemptFilePath?.let {
                        TextButton(onClick = {
                            onShareAttempt(it)
                            showShareDialog = false
                        }) {
                            Text("Share Reversed Attempt", color = peachOrange)
                        }
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showShareDialog = false }) {
                    Text("Cancel", color = darkBrown)
                }
            }
        )
    }

    if (showScoreDialog) {
        AlertDialog(
            onDismissRequest = { showScoreDialog = false },
            title = { Text("Score Details", fontFamily = FontFamily.Serif) },
            text = {
                Column {
                    Text("Player: ${attempt.playerName}")
                    Text("Overall Score: ${attempt.score}%")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Pitch Similarity: ${(attempt.pitchSimilarity * 100).toInt()}%")
                    Text("MFCC Similarity: ${(attempt.mfccSimilarity * 100).toInt()}%")
                    Text("Raw Score: ${(attempt.rawScore * 100).toInt()}%")
                    Text("Challenge: ${if (attempt.challengeType == ChallengeType.FORWARD) "Forward" else "Reverse"}")
                }
            },
            confirmButton = {
                TextButton(onClick = { showScoreDialog = false }) {
                    Text("Close", color = tealGreen)
                }
            }
        )
    }

    if (showRenameDialog && onRenamePlayer != null) {
        var newName by remember { mutableStateOf(attempt.playerName) }
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Player", fontFamily = FontFamily.Serif) },
            text = {
                TextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Player Name") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onRenamePlayer(attempt, newName)
                    showRenameDialog = false
                }) {
                    Text("Save", color = tealGreen)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel", color = darkBrown)
                }
            }
        )
    }

    if (showDeleteDialog && onDeleteAttempt != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Attempt?", fontFamily = FontFamily.Serif) },
            text = { Text("This will permanently delete ${attempt.playerName}'s attempt.") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteAttempt(attempt)
                    showDeleteDialog = false
                }) {
                    Text("Delete", color = peachOrange)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = darkBrown)
                }
            }
        )
    }
}
@Composable
fun GuitarControlButton(
    color: Color,
    label: String,
    onClick: () -> Unit,
    icon: @Composable () -> Unit
) {
    val darkBrown = Color(0xFF5d4a36)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(color, RoundedCornerShape(10.dp))
                .border(3.dp, darkBrown, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }

        Spacer(modifier = Modifier.height(1.dp))

        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = darkBrown,
            textAlign = TextAlign.Center
        )
    }
}

// ============================================
// 🎸 HAND-DRAWN ICONS (CPD's Style!)
// ============================================

@Composable
fun GuitarShareIcon(color: Color) {
    Canvas(modifier = Modifier.size(24.dp)) {
        val radius = 4.dp.toPx()
        drawCircle(
            color = color,
            radius = radius,
            center = Offset(size.width * 0.25f, size.height * 0.5f)
        )
        drawCircle(
            color = color,
            radius = radius,
            center = Offset(size.width * 0.75f, size.height * 0.25f)
        )
        drawCircle(
            color = color,
            radius = radius,
            center = Offset(size.width * 0.75f, size.height * 0.75f)
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.25f, size.height * 0.5f),
            end = Offset(size.width * 0.75f, size.height * 0.25f),
            strokeWidth = 2.dp.toPx()
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.25f, size.height * 0.5f),
            end = Offset(size.width * 0.75f, size.height * 0.75f),
            strokeWidth = 2.dp.toPx()
        )
    }
}

@Composable
fun GuitarRewindIcon(color: Color) {
    Canvas(modifier = Modifier.size(24.dp)) {
        val path = Path().apply {
            arcTo(
                rect = androidx.compose.ui.geometry.Rect(
                    size.width * 0.2f, size.height * 0.2f,
                    size.width * 0.8f, size.height * 0.8f
                ),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 270f,
                forceMoveTo = true
            )
            lineTo(size.width * 0.3f, size.height * 0.3f)
            moveTo(size.width * 0.2f, size.height * 0.5f)
            lineTo(size.width * 0.1f, size.height * 0.4f)
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
fun GuitarMicIcon(color: Color) {
    Canvas(modifier = Modifier.size(24.dp)) {
        drawRoundRect(
            color = color,
            topLeft = Offset(size.width * 0.35f, size.height * 0.15f),
            size = Size(size.width * 0.3f, size.height * 0.35f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx()),
            style = Stroke(width = 2.dp.toPx())
        )
        val path = Path().apply {
            moveTo(size.width * 0.25f, size.height * 0.5f)
            quadraticBezierTo(
                size.width * 0.25f, size.height * 0.65f,
                size.width * 0.5f, size.height * 0.65f
            )
            quadraticBezierTo(
                size.width * 0.75f, size.height * 0.65f,
                size.width * 0.75f, size.height * 0.5f
            )
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 2.dp.toPx())
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.5f, size.height * 0.65f),
            end = Offset(size.width * 0.5f, size.height * 0.8f),
            strokeWidth = 2.dp.toPx()
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.35f, size.height * 0.8f),
            end = Offset(size.width * 0.65f, size.height * 0.8f),
            strokeWidth = 2.dp.toPx()
        )
    }
}

@Composable
fun GuitarDeleteIcon(color: Color) {
    Canvas(modifier = Modifier.size(24.dp)) {
        drawLine(
            color = color,
            start = Offset(size.width * 0.2f, size.height * 0.25f),
            end = Offset(size.width * 0.8f, size.height * 0.25f),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
        val lidPath = Path().apply {
            moveTo(size.width * 0.35f, size.height * 0.25f)
            lineTo(size.width * 0.35f, size.height * 0.15f)
            quadraticBezierTo(
                size.width * 0.35f, size.height * 0.1f,
                size.width * 0.4f, size.height * 0.1f
            )
            lineTo(size.width * 0.6f, size.height * 0.1f)
            quadraticBezierTo(
                size.width * 0.65f, size.height * 0.1f,
                size.width * 0.65f, size.height * 0.15f
            )
            lineTo(size.width * 0.65f, size.height * 0.25f)
        }
        drawPath(
            path = lidPath,
            color = color,
            style = Stroke(width = 2.dp.toPx())
        )
        val canPath = Path().apply {
            moveTo(size.width * 0.25f, size.height * 0.25f)
            lineTo(size.width * 0.25f, size.height * 0.75f)
            quadraticBezierTo(
                size.width * 0.25f, size.height * 0.85f,
                size.width * 0.35f, size.height * 0.85f
            )
            lineTo(size.width * 0.65f, size.height * 0.85f)
            quadraticBezierTo(
                size.width * 0.75f, size.height * 0.85f,
                size.width * 0.75f, size.height * 0.75f
            )
            lineTo(size.width * 0.75f, size.height * 0.25f)
        }
        drawPath(
            path = canPath,
            color = color,
            style = Stroke(width = 2.dp.toPx())
        )
    }
}