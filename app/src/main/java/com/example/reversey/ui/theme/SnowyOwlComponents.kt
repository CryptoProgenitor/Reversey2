package com.example.reversey.ui.theme

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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.reversey.data.models.ChallengeType
import com.example.reversey.data.models.PlayerAttempt
import com.example.reversey.data.models.Recording
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * 🦉 SNOWY OWL THEME COMPONENTS
 * Arctic midnight theme with flying owl and falling snow
 */
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
            // Falling snowflakes
            SnowyOwlSnowflakes()

            // Flying owl
            SnowyOwlFlying()

            // Content on top
            content()
        }
    }
}

// ============================================
// 🌙 MOON RECORD BUTTON
// ============================================

@Composable
fun SnowyOwlRecordButton(
    isRecording: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val moonColor = Color(0xFFe8e8e8) // Light gray moon
    val eclipseColor = Color(0xFF1c2541) // Dark blue eclipse

    val eclipseProgress by animateFloatAsState(
        targetValue = if (isRecording) 1f else 0f,
        animationSpec = tween(600),
        label = "eclipse"
    )

    // Pulsing glow animation when recording
    val infiniteTransition = rememberInfiniteTransition(label = "moonPulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // SUPER BRIGHT pulsing glow when recording (MAXIMUM VISIBILITY)
        if (isRecording) {
            // Bright outer glow layer - NOT CLIPPED NOW
            Canvas(modifier = Modifier.size(210.dp)) {
                val glowRadius = size.minDimension / 2f
                val center = Offset(size.width / 2f, size.height / 2f)

                drawCircle(
                    brush = Brush.radialGradient(
                        0.33f to Color.Transparent,
                        0.38f to Color(0xFFffffff).copy(alpha = glowAlpha),  // Full white
                        0.48f to Color(0xFFffffff).copy(alpha = glowAlpha * 0.9f),
                        0.60f to Color(0xFFf0f0f0).copy(alpha = glowAlpha * 0.75f),
                        0.75f to Color(0xFFe0e0e0).copy(alpha = glowAlpha * 0.55f),
                        0.90f to Color(0xFFd0d0d0).copy(alpha = glowAlpha * 0.3f),
                        1.0f to Color.Transparent,
                        center = center,
                        radius = glowRadius
                    ),
                    radius = glowRadius,
                    center = center
                )
            }

            // Extra bright inner ring at moon edge for visibility
            Canvas(modifier = Modifier.size(150.dp)) {
                val glowRadius = size.minDimension / 2f
                val center = Offset(size.width / 2f, size.height / 2f)

                drawCircle(
                    brush = Brush.radialGradient(
                        0.45f to Color.Transparent,
                        0.50f to Color(0xFFffffff).copy(alpha = glowAlpha * 0.95f),  // Very bright
                        0.70f to Color(0xFFffffff).copy(alpha = glowAlpha * 0.8f),
                        0.90f to Color(0xFFf8f8f8).copy(alpha = glowAlpha * 0.5f),
                        1.0f to Color.Transparent,
                        center = center,
                        radius = glowRadius
                    ),
                    radius = glowRadius,
                    center = center
                )
            }
        }

        // Text below moon button
        if (isRecording) {
            Text(
                text = "🔴 Recording",
                color = Color(0xFFFF69B4),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = 50.dp)
            )
        }

        // Moon button with clickable area
        Canvas(
            modifier = Modifier
                .size(140.dp)
                .clickable(
                    onClick = onClick,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                )
        ) {
            val radius = size.minDimension / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            // Moon background with craters
            drawCircle(
                color = moonColor,
                radius = radius,
                center = center,
                style = Fill
            )

            // Craters (always visible)
            drawCrater(center, 0.3f, 0.15f, radius)
            drawCrater(center, -0.25f, -0.2f, radius)
            drawCrater(center, 0.15f, -0.35f, radius)
            drawCrater(center, -0.4f, 0.25f, radius)

            // Semi-transparent eclipse shadow (allows craters to show through)
            if (eclipseProgress > 0f) {
                val eclipseOffset = Offset(
                    center.x + radius * 0.3f * (1f - eclipseProgress),
                    center.y
                )
                drawCircle(
                    color = eclipseColor.copy(alpha = 0.7f), // Semi-transparent to show textures
                    radius = radius * eclipseProgress,
                    center = eclipseOffset,
                    style = Fill
                )
            }

            // Moon border (extra bright when recording)
            drawCircle(
                color = if (isRecording) Color(0xFFe8e8e8) else Color(0xFFc0c0c0),
                radius = radius,
                center = center,
                style = Stroke(width = if (isRecording) 4.dp.toPx() else 3.dp.toPx())
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCrater(
    center: Offset,
    offsetXRatio: Float,
    offsetYRatio: Float,
    moonRadius: Float
) {
    val craterCenter = Offset(
        center.x + moonRadius * offsetXRatio,
        center.y + moonRadius * offsetYRatio
    )
    val craterRadius = moonRadius * 0.15f

    // Shadow
    drawCircle(
        color = Color(0xFFb0b0b0),
        radius = craterRadius,
        center = craterCenter
    )
    // Highlight
    drawCircle(
        color = Color(0xFFf0f0f0),
        radius = craterRadius * 0.6f,
        center = craterCenter.copy(
            x = craterCenter.x - craterRadius * 0.2f,
            y = craterCenter.y - craterRadius * 0.2f
        )
    )
}

// ============================================
// ❄️ FALLING SNOWFLAKES
// ============================================

@Composable
fun SnowyOwlSnowflakes() {
    val snowflakes = remember {
        List(15) { index ->
            SnowflakeData(
                x = Random.nextFloat(),
                initialY = Random.nextFloat() * -0.3f,
                speed = Random.nextFloat() * 0.5f + 0.3f,
                size = Random.nextFloat() * 8f + 8f,
                delay = index * 400
            )
        }
    }

    snowflakes.forEach { snowflake ->
        AnimatedSnowflake(snowflake)
    }
}

data class SnowflakeData(
    val x: Float,
    val initialY: Float,
    val speed: Float,
    val size: Float,
    val delay: Int
)

@Composable
fun AnimatedSnowflake(data: SnowflakeData) {
    val infiniteTransition = rememberInfiniteTransition(label = "snowfall")

    val yPosition by infiniteTransition.animateFloat(
        initialValue = data.initialY,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (20000 / data.speed).toInt(),
                easing = LinearEasing,
                delayMillis = data.delay
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "snowY"
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 8000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "snowRotation"
    )

    BoxWithConstraints {
        Text(
            text = "❄",
            color = Color.White.copy(alpha = 0.4f),
            fontSize = data.size.sp,
            modifier = Modifier
                .offset(
                    x = (maxWidth.value * data.x).dp,
                    y = (maxHeight.value * yPosition).dp
                )
                .graphicsLayer(rotationZ = rotation)
        )
    }
}

// ============================================
// 🦉 FLYING OWL
// ============================================

@Composable
fun SnowyOwlFlying() {
    var owlX by remember { mutableStateOf(100f) }
    var owlY by remember { mutableStateOf(250f) }
    var baseY by remember { mutableStateOf(250f) }
    var velocityX by remember { mutableStateOf(1.5f) }
    var facingRight by remember { mutableStateOf(true) }
    var flightPhase by remember { mutableStateOf(0f) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = constraints.maxWidth.toFloat()
        val screenHeight = constraints.maxHeight.toFloat()
        val owlWidth = 600f // Owl total width including wings

        LaunchedEffect(Unit) {
            while (true) {
                withFrameMillis { frameTimeMillis ->
                    // Update position
                    owlX += velocityX
                    flightPhase += 0.02f

                    // Sine wave vertical motion
                    val verticalOffset = sin(flightPhase) * 70f
                    owlY = baseY + verticalOffset

                    // Reverse completely off-screen (owl fully invisible before turning)
                    if (owlX > screenWidth + 100f) {
                        velocityX = -1.5f
                        facingRight = false
                        // Prefer top 30% of screen (70% chance), but allow full screen (30% chance)
                        baseY = if (Random.nextFloat() < 0.7f) {
                            // Top 30% range (with margin for sine wave)
                            Random.nextFloat() * (screenHeight * 0.3f - 140f) + 70f
                        } else {
                            // Full screen range
                            Random.nextFloat() * (screenHeight - 140f) + 70f
                        }
                        flightPhase = 0f
                    } else if (owlX < -owlWidth - 100f) {
                        velocityX = 1.5f
                        facingRight = true
                        // Prefer top 30% of screen (70% chance), but allow full screen (30% chance)
                        baseY = if (Random.nextFloat() < 0.7f) {
                            // Top 30% range (with margin for sine wave)
                            Random.nextFloat() * (screenHeight * 0.3f - 140f) + 70f
                        } else {
                            // Full screen range
                            Random.nextFloat() * (screenHeight - 140f) + 70f
                        }
                        flightPhase = 0f
                    }
                }
            }
        }

        // Calculate wing flap angle (smooth sine wave for graceful flapping)
        val wingFlapAngle = sin(flightPhase * 5f) * 17f  // Oscillate between -17° and +17° (like HTML: -8° to -25° and +8° to +25°)
        val wingVerticalOffset = sin(flightPhase * 5f) * 8f // Oscillate between -8px and +8px (like HTML: -3px to +5px)

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    translationX = owlX,
                    translationY = owlY,
                    scaleX = if (facingRight) 1f else -1f,
                    rotationZ = cos(flightPhase) * 70f * 0.01f // Subtle tilt
                )
        ) {
            drawOwl(wingFlapAngle, wingVerticalOffset)
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawOwl(
    wingFlapAngle: Float = 0f,
    wingVerticalOffset: Float = 0f
) {
    val bodyColor = Color(0xFFf8f8f8)
    val wingColor = Color(0xFFf5f5f5)
    val eyeColor = Color(0xFF2d3a5f)
    val beakColor = Color(0xFFffb84d)
    val spotColor = Color(0xFF505050).copy(alpha = 0.85f) // DARK gray spots - actually visible!

    // Wings with flapping animation (4x bigger positions)
    drawWingAnimated(-200f, 120f, wingColor, -wingFlapAngle, wingVerticalOffset) // Left wing flaps down
    drawWingAnimated(200f, 120f, wingColor, wingFlapAngle, wingVerticalOffset)   // Right wing flaps up

    // Body with gradient shading (4x bigger)
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFffffff),
                bodyColor,
                Color(0xFFe0e0e0) // Slightly darker edge
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
    drawLine(
        color = spotColor,
        start = Offset(320f, 250f),
        end = Offset(370f, 252f),
        strokeWidth = 5f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = spotColor,
        start = Offset(430f, 270f),
        end = Offset(480f, 268f),
        strokeWidth = 5f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = spotColor,
        start = Offset(340f, 310f),
        end = Offset(390f, 312f),
        strokeWidth = 5f,
        cap = StrokeCap.Round
    )

    // Head with subtle gradient (4x bigger)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFffffff),
                Color(0xFFf8f8f8),
                Color(0xFFe8e8e8) // Darker edge for definition
            ),
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
            colors = listOf(
                eyeColor,
                eyeColor.copy(alpha = 0.8f),
                Color(0xFF1a2030)
            ),
            center = Offset(440f, 232f),
            radius = 20f
        ),
        radius = 20f,
        center = Offset(440f, 232f)
    )
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                eyeColor,
                eyeColor.copy(alpha = 0.8f),
                Color(0xFF1a2030)
            ),
            center = Offset(480f, 232f),
            radius = 20f
        ),
        radius = 20f,
        center = Offset(480f, 232f)
    )

    // Eye highlights (4x bigger)
    drawCircle(
        color = Color.White,
        radius = 6f,
        center = Offset(444f, 226f)
    )
    drawCircle(
        color = Color.White,
        radius = 6f,
        center = Offset(484f, 226f)
    )

    // Beak with gradient (4x bigger)
    val beakPath = Path().apply {
        moveTo(460f, 248f)
        lineTo(452f, 260f)
        lineTo(468f, 260f)
        close()
    }
    drawPath(
        beakPath,
        brush = Brush.linearGradient(
            colors = listOf(
                Color(0xFFffb84d),
                Color(0xFFffa033)
            ),
            start = Offset(460f, 248f),
            end = Offset(460f, 260f)
        )
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawWing(
    xOffset: Float,
    yOffset: Float,
    color: Color
) {
    val wingPath = Path().apply {
        moveTo(400f, 240f)
        quadraticBezierTo(
            400f + xOffset * 0.5f, 160f + yOffset,
            400f + xOffset, 140f + yOffset
        )
        quadraticBezierTo(
            400f + xOffset * 0.8f, 180f + yOffset,
            400f + xOffset * 0.3f, 280f + yOffset
        )
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
    val spotColor = Color(0xFF505050).copy(alpha = 0.85f) // DARK gray - actually visible!

    // Save current state and apply rotation around wing pivot point (400f, 240f)
    rotate(degrees = rotationAngle, pivot = Offset(400f, 240f)) {
        // Create wing path with VERY rounded curves - smooth, organic feather shape
        val wingPath = Path().apply {
            moveTo(400f, 240f + verticalOffset)

            // First curve - outer edge with VERY rounded tip (extended control points for smoothness)
            cubicTo(
                400f + xOffset * 0.25f, 190f + yOffset + verticalOffset,  // Pull control point out more
                400f + xOffset * 0.8f, 145f + yOffset + verticalOffset,   // Higher control point
                400f + xOffset * 0.95f, 135f + yOffset + verticalOffset   // Rounder tip approach
            )

            // Small curve at tip for maximum roundness
            cubicTo(
                400f + xOffset * 0.98f, 138f + yOffset + verticalOffset,
                400f + xOffset, 142f + yOffset + verticalOffset,
                400f + xOffset * 0.98f, 148f + yOffset + verticalOffset   // Rounded wing tip
            )

            // Second curve - inner edge back to body with smooth connection
            cubicTo(
                400f + xOffset * 0.9f, 165f + yOffset + verticalOffset,
                400f + xOffset * 0.6f, 220f + yOffset + verticalOffset,
                400f + xOffset * 0.35f, 270f + yOffset + verticalOffset
            )

            // Final curve back to start - smooth connection to body
            cubicTo(
                400f + xOffset * 0.2f, 285f + yOffset + verticalOffset,
                400f + xOffset * 0.05f, 265f + yOffset + verticalOffset,
                400f, 240f + verticalOffset
            )

            close()
        }

        // Draw wing fill with gradient for depth
        drawPath(
            wingPath,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFFffffff),
                    color,
                    Color(0xFFd8d8d8) // Darker edge for more contrast
                ),
                start = Offset(400f, 240f + verticalOffset),
                end = Offset(400f + xOffset, 140f + yOffset + verticalOffset)
            ),
            style = Fill
        )

        // Wing feather markings - MUCH THICKER AND DARKER
        // Feather barring (horizontal lines across wing)
        drawLine(
            color = spotColor,
            start = Offset(400f + xOffset * 0.3f, 210f + yOffset + verticalOffset),
            end = Offset(400f + xOffset * 0.7f, 200f + yOffset + verticalOffset),
            strokeWidth = 5f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = spotColor,
            start = Offset(400f + xOffset * 0.4f, 240f + yOffset + verticalOffset),
            end = Offset(400f + xOffset * 0.8f, 225f + yOffset + verticalOffset),
            strokeWidth = 5f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = spotColor,
            start = Offset(400f + xOffset * 0.25f, 180f + yOffset + verticalOffset),
            end = Offset(400f + xOffset * 0.6f, 175f + yOffset + verticalOffset),
            strokeWidth = 4f,
            cap = StrokeCap.Round
        )

        // Wing spots - BIGGER
        drawCircle(
            color = spotColor,
            radius = 7f,
            center = Offset(400f + xOffset * 0.5f, 180f + yOffset + verticalOffset)
        )
        drawCircle(
            color = spotColor,
            radius = 6f,
            center = Offset(400f + xOffset * 0.7f, 210f + yOffset + verticalOffset)
        )
        drawCircle(
            color = spotColor,
            radius = 6f,
            center = Offset(400f + xOffset * 0.4f, 260f + yOffset + verticalOffset)
        )
        drawCircle(
            color = spotColor,
            radius = 5f,
            center = Offset(400f + xOffset * 0.6f, 250f + yOffset + verticalOffset)
        )

        // Draw wing outline with VERY rounded cap for maximum smoothness
        drawPath(
            wingPath,
            Color(0xFFb0b0b0), // Darker outline for better definition
            style = Stroke(width = 6f, cap = StrokeCap.Round, join = StrokeJoin.Round) // Thinner stroke for smoother look
        )
    }
}

// ============================================
// 📱 RECORDING CARD
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
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showGameDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }

    // Colors - Mysterious arctic night palette
    val cardOuter = Color(0xFF282832).copy(alpha = 0.6f) // 60% transparent for owl roaming
    val cardInner = Color(0xFF14141e).copy(alpha = 0.6f) // 60% transparent for owl roaming
    val headerBg = Color(0xFF505064).copy(alpha = 0.5f)
    val borderColor = Color(0xFF787896).copy(alpha = 0.35f)
    val mysteriousPurple = Color(0xFF9B4F96).copy(alpha = 0.85f)
    val deepSlatePurple = Color(0xFF6B4C7C).copy(alpha = 0.85f)
    val progressBlue = Color(0xFFADD8E6).copy(alpha = 0.5f)

    // Outer card - minimal screen gaps (4dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 4.dp, top = 4.dp, bottom = 2.dp) // Tight to screen edges
            .background(cardOuter, RoundedCornerShape(20.dp))
            .border(2.dp, borderColor, RoundedCornerShape(20.dp))
            .padding(6.dp)
    ) {
        // Inner dark box
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardInner, RoundedCornerShape(15.dp))
                .padding(8.dp) // Reduced from 12.dp
        ) {
            // Header row with separate name box and delete button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Recording name box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(headerBg, RoundedCornerShape(12.dp))
                        .border(2.dp, borderColor, RoundedCornerShape(12.dp))
                        .clickable { showRenameDialog = true }
                        .padding(12.dp)
                ) {
                    Text(
                        text = recording.name,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Delete button (separate box)
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color(0xFF8B3A6F).copy(alpha = 0.7f), RoundedCornerShape(10.dp))
                        .border(2.dp, Color(0xFF6B2C5C).copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                        .clickable { showDeleteDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    OwlDeleteIcon(Color.White)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress bar with metallic dot
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .background(progressBlue, RoundedCornerShape(10.dp))
                )

                // Metallic progress dot
                Canvas(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .offset(x = (progress * 100).coerceIn(0f, 100f).dp - 8.dp)
                        .size(16.dp)
                ) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFf0f0f0),
                                Color(0xFFd4d4d4),
                                Color(0xFFa8a8a8)
                            ),
                            center = Offset(size.width * 0.3f, size.height * 0.3f)
                        ),
                        radius = size.minDimension / 2f
                    )
                    drawCircle(
                        color = Color.White,
                        radius = size.minDimension / 2f,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Control buttons (matching Guitar theme pattern)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Share button - opens dialog
                OwlControlButton(
                    color = mysteriousPurple,
                    label = "Share",
                    onClick = { showShareDialog = true }
                ) {
                    OwlShareIcon(Color.White)
                }

                // Play/Pause button
                OwlControlButton(
                    color = deepSlatePurple,
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
                        OwlPauseIcon(Color.White)
                    } else {
                        OwlPlayIcon(Color.White)
                    }
                }

                // Rev button - plays reversed audio
                OwlControlButton(
                    color = mysteriousPurple,
                    label = "Rev",
                    onClick = {
                        recording.reversedPath?.let { path ->
                            onPlay(path)
                        }
                    }
                ) {
                    OwlRewindIcon(Color.White)
                }

                // Game mode buttons
                if (isGameModeEnabled) {
                    // Fwd challenge button
                    OwlControlButton(
                        color = deepSlatePurple,
                        label = "Fwd",
                        onClick = { onStartAttempt(recording, ChallengeType.FORWARD) }
                    ) {
                        OwlMicRightArrowIcon(Color.White)
                    }

                    // Rev challenge button
                    OwlControlButton(
                        color = mysteriousPurple,
                        label = "Rev",
                        onClick = { onStartAttempt(recording, ChallengeType.REVERSE) }
                    ) {
                        OwlMicLeftArrowIcon(Color.White)
                    }
                }
            }
        }
    }

    // Dialogs
    if (showRenameDialog) {
        var newName by remember { mutableStateOf(recording.name) }
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Recording", color = Color.White) },
            text = {
                TextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Recording Name") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = cardInner,
                        unfocusedContainerColor = cardInner
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onRename(recording.originalPath, newName)
                    showRenameDialog = false
                }) {
                    Text("Save", color = mysteriousPurple)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            },
            containerColor = cardOuter
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Recording?", color = Color.White) },
            text = { Text("This will permanently delete ${recording.name}", color = Color.White.copy(alpha = 0.8f)) },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(recording)
                    showDeleteDialog = false
                }) {
                    Text("Delete", color = mysteriousPurple)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            },
            containerColor = cardOuter
        )
    }

    if (showGameDialog) {
        AlertDialog(
            onDismissRequest = { showGameDialog = false },
            title = { Text("Start Challenge", color = Color.White) },
            text = {
                Column {
                    TextButton(
                        onClick = {
                            onStartAttempt(recording, ChallengeType.REVERSE)
                            showGameDialog = false
                        }
                    ) {
                        Text("🔄 Reverse Challenge", color = mysteriousPurple)
                    }
                    TextButton(
                        onClick = {
                            onStartAttempt(recording, ChallengeType.FORWARD)
                            showGameDialog = false
                        }
                    ) {
                        Text("▶️ Forward Challenge", color = deepSlatePurple)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showGameDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            },
            containerColor = cardOuter
        )
    }

    if (showShareDialog) {
        AlertDialog(
            onDismissRequest = { showShareDialog = false },
            title = { Text("Share Recording", color = Color.White) },
            text = { Text("Which version would you like to share?", color = Color.White.copy(alpha = 0.8f)) },
            confirmButton = {
                Column {
                    TextButton(onClick = {
                        onShare(recording.originalPath)
                        showShareDialog = false
                    }) {
                        Text("Share Original (Forward)", color = mysteriousPurple)
                    }
                    recording.reversedPath?.let {
                        TextButton(onClick = {
                            onShare(it)
                            showShareDialog = false
                        }) {
                            Text("Share Reversed", color = deepSlatePurple)
                        }
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showShareDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            },
            containerColor = cardOuter
        )
    }
}

// ============================================
// 🎮 ATTEMPT CARD
// ============================================

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

    val isPlaying = currentlyPlayingPath == attempt.attemptFilePath

    val cardOuter = Color(0xFF282832).copy(alpha = 0.6f) // 60% transparent for owl roaming
    val cardInner = Color(0xFF14141e).copy(alpha = 0.6f) // 60% transparent for owl roaming
    val headerBg = Color(0xFF505064).copy(alpha = 0.5f)
    val borderColor = Color(0xFF787896).copy(alpha = 0.35f)
    val mysteriousPurple = Color(0xFF9B4F96).copy(alpha = 0.85f)
    val deepSlatePurple = Color(0xFF6B4C7C).copy(alpha = 0.85f)
    val progressBlue = Color(0xFFADD8E6).copy(alpha = 0.5f)

    // Calculate moon face emoji based on score
    val scorePercent = if (attempt.score > 1f) attempt.score.toInt() else (attempt.score * 100).toInt()
    val moonFace = when {
        scorePercent >= 80 -> "🌝" // Happy full moon
        scorePercent >= 50 -> "🌗" // Neutral quarter moon
        else -> "🌚" // Sad new moon
    }

    // Outer card - right edge aligned with recording card (4dp), extra left indent
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 34.dp, end = 4.dp, top = 4.dp, bottom = 2.dp) // 4dp base + 30dp indent = 34dp left, 4dp right matches recording
            .background(cardOuter, RoundedCornerShape(20.dp))
            .border(2.dp, borderColor, RoundedCornerShape(20.dp))
            .padding(6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardInner, RoundedCornerShape(15.dp))
                .padding(8.dp) // Reduced from 12.dp
        ) {
            // Header row - "Go to Parent" button, Player name box, Delete button (all separate)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // "Go to Parent" button (separate box on left)
                if (onJumpToParent != null) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0xFF4A6FA5).copy(alpha = 0.7f), RoundedCornerShape(10.dp))
                            .border(2.dp, Color(0xFF3B5A8C).copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                            .clickable { onJumpToParent() },
                        contentAlignment = Alignment.Center
                    ) {
                        OwlHomeIcon(Color.White)
                    }
                }

                // Player name box (separate, clickable to rename)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(headerBg, RoundedCornerShape(12.dp))
                        .border(2.dp, borderColor, RoundedCornerShape(12.dp))
                        .clickable { if (onRenamePlayer != null) showRenameDialog = true }
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Challenge type icon - 50% sized version of playback buttons
                        Box(modifier = Modifier.size(16.dp)) {
                            if (attempt.challengeType == ChallengeType.REVERSE) {
                                OwlRewindIcon(Color.White.copy(alpha = 0.7f))
                            } else {
                                OwlPlayIcon(Color.White.copy(alpha = 0.7f))
                            }
                        }

                        // Player name
                        Text(
                            text = attempt.playerName,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Delete button (separate box on right)
                if (onDeleteAttempt != null) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0xFF8B3A6F).copy(alpha = 0.7f), RoundedCornerShape(10.dp))
                            .border(2.dp, Color(0xFF6B2C5C).copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                            .clickable { showDeleteDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        OwlDeleteIcon(Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // MIDDLE ROW: Progress bar - full width
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
            ) {
                if (isPlaying) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progress)
                            .background(progressBlue, RoundedCornerShape(10.dp))
                    )

                    // Metallic progress dot
                    Canvas(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .offset(x = (progress * 100).coerceIn(0f, 100f).dp - 8.dp)
                            .size(16.dp)
                    ) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFf0f0f0),
                                    Color(0xFFd4d4d4),
                                    Color(0xFFa8a8a8)
                                ),
                                center = Offset(size.width * 0.3f, size.height * 0.3f)
                            ),
                            radius = size.minDimension / 2f
                        )
                        drawCircle(
                            color = Color.White,
                            radius = size.minDimension / 2f,
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // BOTTOM ROW: Buttons + Score
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Share button
                if (onShareAttempt != null) {
                    OwlControlButton(
                        color = mysteriousPurple,
                        label = "share",
                        onClick = { showShareDialog = true }
                    ) {
                        OwlShareIcon(Color.White)
                    }
                }

                // Play/Pause button
                OwlControlButton(
                    color = deepSlatePurple,
                    label = if (isPlaying && !isPaused) "pause" else "play",
                    onClick = {
                        if (isPlaying && !isPaused) {
                            onPause()
                        } else {
                            onPlay(attempt.attemptFilePath)
                        }
                    }
                ) {
                    if (isPlaying && !isPaused) {
                        OwlPauseIcon(Color.White)
                    } else {
                        OwlPlayIcon(Color.White)
                    }
                }

                // Rev button
                if (attempt.reversedAttemptFilePath != null) {
                    OwlControlButton(
                        color = mysteriousPurple,
                        label = "rev",
                        onClick = { onPlay(attempt.reversedAttemptFilePath!!) }
                    ) {
                        OwlRewindIcon(Color.White)
                    }
                }

                // Moon face scoring circle (right side) - 90% size
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color(0xFFADD8E6).copy(alpha = 0.3f), CircleShape)
                        .border(3.dp, Color(0xFFADD8E6).copy(alpha = 0.6f), CircleShape)
                        .clickable { showScoreDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = moonFace,
                            fontSize = 25.sp
                        )
                        Text(
                            text = "${scorePercent}%",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // Share dialog - FORWARD vs REVERSED options
    if (showShareDialog && onShareAttempt != null) {
        AlertDialog(
            onDismissRequest = { showShareDialog = false },
            title = { Text("Share Attempt", color = Color.White) },
            text = {
                Column {
                    Text("Which version would you like to share?", color = Color.White.copy(alpha = 0.8f))
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            onShareAttempt(attempt.attemptFilePath)
                            showShareDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Share Original (Forward)")
                    }
                    if (attempt.reversedAttemptFilePath != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                onShareAttempt(attempt.reversedAttemptFilePath!!)
                                showShareDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Share Reversed")
                        }
                    }
                }
            },
            confirmButton = { },
            dismissButton = {
                TextButton(onClick = { showShareDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            },
            containerColor = Color(0xFF282832).copy(alpha = 0.75f)
        )
    }

    // Rename dialog
    if (showRenameDialog && onRenamePlayer != null) {
        var newName by remember { mutableStateOf(attempt.playerName) }
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Player", color = Color.White) },
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
                    Text("Save", color = mysteriousPurple)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            },
            containerColor = Color(0xFF282832).copy(alpha = 0.75f)
        )
    }

    // Delete dialog
    if (showDeleteDialog && onDeleteAttempt != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Attempt?", color = Color.White) },
            text = { Text("This will permanently delete ${attempt.playerName}'s attempt.", color = Color.White.copy(alpha = 0.8f)) },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteAttempt(attempt)
                    showDeleteDialog = false
                }) {
                    Text("Delete", color = mysteriousPurple)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            },
            containerColor = Color(0xFF282832).copy(alpha = 0.75f)
        )
    }

    // Score Explanation Dialog
    if (showScoreDialog) {
        OwlScoreDialog(
            attempt = attempt,
            scorePercent = scorePercent,
            moonFace = moonFace,
            onDismiss = { showScoreDialog = false }
        )
    }
}


// ============================================
// 🌙 OWL SCORE DIALOG - Mysterious & Detailed
// ============================================

@Composable
fun OwlScoreDialog(
    attempt: PlayerAttempt,
    scorePercent: Int,
    moonFace: String,
    onDismiss: () -> Unit
) {
    val mysticalPurple = Color(0xFF9B4F96)
    val deepNight = Color(0xFF14141e)
    val starBlue = Color(0xFFADD8E6)
    val mysteryShadow = Color(0xFF282832)

    // Mystical message based on score
    val mysticalMessage = when {
        scorePercent >= 90 -> "The owl spirits deem you LEGENDARY! 🦉✨"
        scorePercent >= 80 -> "A masterful performance under the moon! 🌝"
        scorePercent >= 70 -> "The night whispers of your growing skill... 🌙"
        scorePercent >= 60 -> "Progress echoes through the forest... 🌲"
        scorePercent >= 50 -> "The owl watches your journey with patience... 👁️"
        else -> "Even the wisest owl was once a fledgling... 🥚"
    }

    // Mystical tip based on weakest metric
    val mysticalTip = when {
        attempt.pitchSimilarity < attempt.mfccSimilarity ->
            "💫 The stars suggest: Focus on matching the pitch's rhythm..."
        attempt.mfccSimilarity < attempt.pitchSimilarity ->
            "💫 The moon advises: Listen deeply to voice tone patterns..."
        else ->
            "💫 The night whispers: Balance is the key to wisdom..."
    }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(deepNight.copy(alpha = 0.95f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            // Floating stars decoration
            Canvas(modifier = Modifier.fillMaxSize()) {
                val starPositions = listOf(
                    Offset(size.width * 0.1f, size.height * 0.15f),
                    Offset(size.width * 0.85f, size.height * 0.2f),
                    Offset(size.width * 0.15f, size.height * 0.8f),
                    Offset(size.width * 0.9f, size.height * 0.75f),
                    Offset(size.width * 0.5f, size.height * 0.1f),
                    Offset(size.width * 0.3f, size.height * 0.3f),
                    Offset(size.width * 0.7f, size.height * 0.85f),
                    Offset(size.width * 0.25f, size.height * 0.6f)
                )

                starPositions.forEach { pos ->
                    drawCircle(
                        color = starBlue.copy(alpha = 0.4f),
                        radius = 3f,
                        center = pos
                    )
                    // Twinkle effect
                    drawCircle(
                        color = Color.White.copy(alpha = 0.3f),
                        radius = 1.5f,
                        center = pos
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .clickable(enabled = false) { }
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 24.dp, shape = RoundedCornerShape(20.dp))
                        .border(
                            width = 3.dp,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    mysticalPurple.copy(alpha = 0.6f),
                                    starBlue.copy(alpha = 0.4f)
                                )
                            ),
                            shape = RoundedCornerShape(20.dp)
                        ),
                    colors = CardDefaults.cardColors(containerColor = mysteryShadow.copy(alpha = 0.95f)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Close button (X in top right)
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.TopEnd
                        ) {
                            IconButton(onClick = onDismiss) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = starBlue.copy(alpha = 0.8f)
                                )
                            }
                        }

                        // Player name with owl decoration
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Text(
                                text = "🦉",
                                fontSize = 20.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = attempt.playerName,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                style = MaterialTheme.typography.titleLarge
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "🦉",
                                fontSize = 20.sp
                            )
                        }

                        Text(
                            text = "◈ Moonlit Performance ◈",
                            fontSize = 14.sp,
                            color = starBlue.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Large moon score circle with glow effect
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            // Outer glow
                            Canvas(modifier = Modifier.size(140.dp)) {
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            starBlue.copy(alpha = 0.3f),
                                            Color.Transparent
                                        )
                                    ),
                                    radius = size.width / 2
                                )
                            }

                            // Moon circle
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                starBlue.copy(alpha = 0.5f),
                                                starBlue.copy(alpha = 0.2f)
                                            )
                                        ),
                                        shape = CircleShape
                                    )
                                    .border(
                                        width = 4.dp,
                                        color = starBlue.copy(alpha = 0.8f),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = moonFace,
                                        fontSize = 48.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "$scorePercent%",
                                        color = Color.White,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Performance Breakdown Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = 2.dp,
                                    color = mysticalPurple.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = deepNight.copy(alpha = 0.8f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                ) {
                                    Text(
                                        text = "✧",
                                        fontSize = 16.sp,
                                        color = starBlue
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Mystical Metrics",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "✧",
                                        fontSize = 16.sp,
                                        color = starBlue
                                    )
                                }

                                // Pitch Similarity
                                OwlMetricRow(
                                    label = "Pitch Harmony",
                                    value = attempt.pitchSimilarity,
                                    icon = "🎵"
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Voice Matching (MFCC)
                                OwlMetricRow(
                                    label = "Voice Echo",
                                    value = attempt.mfccSimilarity,
                                    icon = "🔮"
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Mystical Message Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = 2.dp,
                                    color = mysticalPurple.copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = mysticalPurple.copy(alpha = 0.2f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = mysticalMessage,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            deepNight.copy(alpha = 0.4f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = mysticalTip,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = starBlue.copy(alpha = 0.9f),
                                        textAlign = TextAlign.Center,
                                        fontStyle = FontStyle.Italic
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Owl-themed metric row with mystical progress visualization
 */
@Composable
fun OwlMetricRow(
    label: String,
    value: Float,
    icon: String
) {
    val starBlue = Color(0xFFADD8E6)
    val mysticalPurple = Color(0xFF9B4F96)
    val percentage = (value * 100).toInt()

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = icon,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }

            Text(
                text = "$percentage%",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = starBlue
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Mystical progress bar with gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.White.copy(alpha = 0.1f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(value)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                mysticalPurple.copy(alpha = 0.8f),
                                starBlue.copy(alpha = 0.9f)
                            )
                        )
                    )
            )

            // Sparkle effect at the end
            if (value > 0.1f) {
                Canvas(
                    modifier = Modifier
                        .fillMaxHeight()
                        .align(Alignment.CenterStart)
                        .padding(start = (value * 100).dp.coerceAtMost(100.dp) - 8.dp)
                ) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.6f),
                        radius = 4.dp.toPx()
                    )
                }
            }
        }
    }
}

// ============================================
// 🎨 CONTROL BUTTON COMPONENT
// ============================================

@Composable
fun OwlControlButton(
    color: Color,
    label: String,
    onClick: () -> Unit,
    icon: @Composable () -> Unit
) {
    val borderColor = Color(0xFFC8508C).copy(alpha = 0.6f)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(color, RoundedCornerShape(10.dp))
                .border(2.dp, borderColor, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

// ============================================
// 🎨 RUNIC-STYLE ICONS (EXACT FROM HTML MOCKUP)
// ============================================

@Composable
fun OwlPlayIcon(color: Color) {
    // Play Icon - Runic Arrow
    Canvas(modifier = Modifier.size(32.dp)) {
        val path = Path().apply {
            moveTo(size.width * 0.29f, size.height * 0.17f) // 7,4 scaled
            lineTo(size.width * 0.29f, size.height * 0.83f) // 7,20 scaled
            lineTo(size.width * 0.79f, size.height * 0.5f)  // 19,12 scaled
            close()
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 2.dp.toPx(), join = StrokeJoin.Miter)
        )
    }
}

@Composable
fun OwlPauseIcon(color: Color) {
    // Pause Icon - Two Vertical Bars (runic style)
    Canvas(modifier = Modifier.size(32.dp)) {
        val barWidth = size.width * 0.12f
        val barHeight = size.height * 0.6f
        val topY = size.height * 0.2f

        // Left bar
        drawRect(
            color = color,
            topLeft = Offset(size.width * 0.3f, topY),
            size = Size(barWidth, barHeight),
            style = Stroke(width = 2.dp.toPx())
        )
        // Right bar
        drawRect(
            color = color,
            topLeft = Offset(size.width * 0.58f, topY),
            size = Size(barWidth, barHeight),
            style = Stroke(width = 2.dp.toPx())
        )
    }
}

@Composable
fun OwlStopIcon(color: Color) {
    // Stop Icon - Square (runic style)
    Canvas(modifier = Modifier.size(32.dp)) {
        drawRect(
            color = color,
            topLeft = Offset(size.width * 0.25f, size.height * 0.25f),
            size = Size(size.width * 0.5f, size.height * 0.5f),
            style = Stroke(width = 2.5.dp.toPx())
        )
    }
}

@Composable
fun OwlShareIcon(color: Color) {
    // Share Icon - Branching Rune (exact from HTML)
    Canvas(modifier = Modifier.size(32.dp)) {
        val strokeWidth = 2.dp.toPx()
        val radius = size.width * 0.083f // 2/24 scaled up

        // Three circles (nodes)
        drawCircle(
            color = color,
            radius = radius,
            center = Offset(size.width * 0.25f, size.height * 0.5f), // 6,12
            style = Stroke(width = strokeWidth)
        )
        drawCircle(
            color = color,
            radius = radius,
            center = Offset(size.width * 0.75f, size.height * 0.25f), // 18,6
            style = Stroke(width = strokeWidth)
        )
        drawCircle(
            color = color,
            radius = radius,
            center = Offset(size.width * 0.75f, size.height * 0.75f), // 18,18
            style = Stroke(width = strokeWidth)
        )

        // Connecting lines
        drawLine(
            color = color,
            start = Offset(size.width * 0.33f, size.height * 0.5f), // 8,12
            end = Offset(size.width * 0.67f, size.height * 0.29f),   // 16,7
            strokeWidth = strokeWidth
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.33f, size.height * 0.5f), // 8,12
            end = Offset(size.width * 0.67f, size.height * 0.71f),   // 16,17
            strokeWidth = strokeWidth
        )
    }
}

@Composable
fun OwlRewindIcon(color: Color) {
    // Rewind Icon - Double Back Runes (exact from HTML)
    Canvas(modifier = Modifier.size(32.dp)) {
        val strokeWidth = 2.dp.toPx()

        // First back arrow
        val path1 = Path().apply {
            moveTo(size.width * 0.54f, size.height * 0.71f) // 13,17
            lineTo(size.width * 0.29f, size.height * 0.5f)  // 7,12
            lineTo(size.width * 0.54f, size.height * 0.29f) // 13,7
        }
        drawPath(
            path = path1,
            color = color,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Square)
        )

        // Second back arrow
        val path2 = Path().apply {
            moveTo(size.width * 0.83f, size.height * 0.71f) // 20,17
            lineTo(size.width * 0.58f, size.height * 0.5f)  // 14,12
            lineTo(size.width * 0.83f, size.height * 0.29f) // 20,7
        }
        drawPath(
            path = path2,
            color = color,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Square)
        )
    }
}

@Composable
fun OwlForwardIcon(color: Color) {
    // Forward Icon - Double Forward Runes (exact from HTML)
    Canvas(modifier = Modifier.size(32.dp)) {
        val strokeWidth = 2.dp.toPx()

        // First forward arrow
        val path1 = Path().apply {
            moveTo(size.width * 0.46f, size.height * 0.29f) // 11,7
            lineTo(size.width * 0.71f, size.height * 0.5f)  // 17,12
            lineTo(size.width * 0.46f, size.height * 0.71f) // 11,17
        }
        drawPath(
            path = path1,
            color = color,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Square)
        )

        // Second forward arrow
        val path2 = Path().apply {
            moveTo(size.width * 0.17f, size.height * 0.29f) // 4,7
            lineTo(size.width * 0.42f, size.height * 0.5f)  // 10,12
            lineTo(size.width * 0.17f, size.height * 0.71f) // 4,17
        }
        drawPath(
            path = path2,
            color = color,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Square)
        )
    }
}

@Composable
fun OwlMicIcon(color: Color) {
    // Microphone Icon - Sound Wave Rune (exact from HTML)
    Canvas(modifier = Modifier.size(32.dp)) {
        val strokeWidth = 2.dp.toPx()

        // Mic capsule body
        drawRoundRect(
            color = color,
            topLeft = Offset(size.width * 0.375f, size.height * 0.17f), // 9,4
            size = Size(size.width * 0.25f, size.height * 0.42f), // 6,10
            cornerRadius = CornerRadius(size.width * 0.125f), // rx=3
            style = Stroke(width = strokeWidth)
        )

        // Arc under mic
        val arcPath = Path().apply {
            moveTo(size.width * 0.21f, size.height * 0.46f) // 5,11
            quadraticBezierTo(
                size.width * 0.21f, size.height * 0.67f,
                size.width * 0.5f, size.height * 0.67f // 5,16 -> 12,16
            )
            quadraticBezierTo(
                size.width * 0.79f, size.height * 0.67f,
                size.width * 0.79f, size.height * 0.46f // 12,16 -> 19,11
            )
        }
        drawPath(
            path = arcPath,
            color = color,
            style = Stroke(width = strokeWidth)
        )

        // Vertical stand
        drawLine(
            color = color,
            start = Offset(size.width * 0.5f, size.height * 0.67f), // 12,16
            end = Offset(size.width * 0.5f, size.height * 0.83f),   // 12,20
            strokeWidth = strokeWidth
        )

        // Base horizontal line
        drawLine(
            color = color,
            start = Offset(size.width * 0.375f, size.height * 0.83f), // 9,20
            end = Offset(size.width * 0.625f, size.height * 0.83f),   // 15,20
            strokeWidth = strokeWidth
        )
    }
}

@Composable
fun OwlReverseIcon(color: Color) {
    // Reverse/Loop Icon - Circular Rune (exact from HTML)
    Canvas(modifier = Modifier.size(32.dp)) {
        val strokeWidth = 2.dp.toPx()

        // Left arc
        val leftArc = Path().apply {
            moveTo(size.width * 0.5f, size.height * 0.17f) // 12,4
            cubicTo(
                size.width * 0.29f, size.height * 0.17f,
                size.width * 0.17f, size.height * 0.29f,
                size.width * 0.17f, size.height * 0.5f // C7,4 4,7 4,12
            )
            cubicTo(
                size.width * 0.17f, size.height * 0.71f,
                size.width * 0.29f, size.height * 0.83f,
                size.width * 0.5f, size.height * 0.83f // C4,17 7,20 12,20
            )
        }
        drawPath(
            path = leftArc,
            color = color,
            style = Stroke(width = strokeWidth)
        )

        // Arrow at top
        val arrowPath = Path().apply {
            moveTo(size.width * 0.5f, size.height * 0.17f)   // 12,4
            lineTo(size.width * 0.5f, size.height * 0.33f)   // 12,8
            lineTo(size.width * 0.33f, size.height * 0.17f)  // 8,4
            lineTo(size.width * 0.5f, size.height * 0.17f)   // 12,4
        }
        drawPath(path = arrowPath, color = color, style = Fill)

        // Right arc
        val rightArc = Path().apply {
            moveTo(size.width * 0.5f, size.height * 0.83f) // 12,20
            cubicTo(
                size.width * 0.71f, size.height * 0.83f,
                size.width * 0.83f, size.height * 0.71f,
                size.width * 0.83f, size.height * 0.5f // C17,20 20,17 20,12
            )
            cubicTo(
                size.width * 0.83f, size.height * 0.29f,
                size.width * 0.71f, size.height * 0.17f,
                size.width * 0.5f, size.height * 0.17f // C20,7 17,4 12,4
            )
        }
        drawPath(
            path = rightArc,
            color = color,
            style = Stroke(width = strokeWidth)
        )
    }
}

@Composable
fun OwlLoopIcon(color: Color) {
    // Loop/Rev Icon - Circle opens at 6 o'clock, chevron points LEFT
    Canvas(modifier = Modifier.size(32.dp)) {
        val strokeWidth = 2.5.dp.toPx()
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension / 2.5f

        // Circle opens at 6 o'clock (bottom) - 20% gap = 72°
        // Arc from ~4:30 position (216°) clockwise to ~7:30 position (288°) = 288° sweep
        val arcRect = Rect(
            center.x - radius,
            center.y - radius,
            center.x + radius,
            center.y + radius
        )

        // Draw arc (opens at 6 o'clock with 20% gap)
        drawArc(
            color = color,
            startAngle = 216f,  // Start at ~4:30 position
            sweepAngle = 288f,   // Sweep 288° (360° - 72° gap)
            useCenter = false,
            topLeft = arcRect.topLeft,
            size = arcRect.size,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Chevron pointing LEFT (90° anticlockwise from down)
        // Upper gap point is around 4:30, lower gap point around 7:30
        val gapRadius = radius * 1.0f

        // Calculate gap points (where circle opens)
        val upperGapAngle = Math.toRadians(216.0) // 4:30 position
        val lowerGapAngle = Math.toRadians(504.0) // 7:30 position (216 + 288)

        val upperGapX = center.x + cos(upperGapAngle).toFloat() * radius
        val upperGapY = center.y + sin(upperGapAngle).toFloat() * radius
        val lowerGapX = center.x + cos(lowerGapAngle).toFloat() * radius
        val lowerGapY = center.y + sin(lowerGapAngle).toFloat() * radius

        // Chevron tip point (to the left of center)
        val chevronTipX = center.x - radius * 1.4f
        val chevronTipY = center.y

        // Draw chevron arms with tangent alignment
        val chevronPath = Path().apply {
            // Top arm: from upper gap point to chevron tip (left)
            moveTo(upperGapX, upperGapY)
            lineTo(chevronTipX, chevronTipY)
            // Bottom arm: from chevron tip to lower gap point
            lineTo(lowerGapX, lowerGapY)
        }
        drawPath(
            path = chevronPath,
            color = color,
            style = Stroke(width = strokeWidth * 1.2f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
fun OwlDeleteIcon(color: Color) {
    // Delete Icon - Breaking Rune (X with base, exact from HTML)
    Canvas(modifier = Modifier.size(24.dp)) {
        val strokeWidth = 2.5.dp.toPx()

        // X marks
        drawLine(
            color = color,
            start = Offset(size.width * 0.33f, size.height * 0.33f), // 8,8
            end = Offset(size.width * 0.67f, size.height * 0.67f),   // 16,16
            strokeWidth = strokeWidth,
            cap = StrokeCap.Square
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.67f, size.height * 0.33f), // 16,8
            end = Offset(size.width * 0.33f, size.height * 0.67f),   // 8,16
            strokeWidth = strokeWidth,
            cap = StrokeCap.Square
        )

        // Base platform
        val basePath = Path().apply {
            moveTo(size.width * 0.25f, size.height * 0.75f)  // 6,18
            lineTo(size.width * 0.75f, size.height * 0.75f)  // 18,18
            lineTo(size.width * 0.79f, size.height * 0.83f)  // 19,20
            lineTo(size.width * 0.21f, size.height * 0.83f)  // 5,20
            close()
        }
        drawPath(
            path = basePath,
            color = color,
            style = Stroke(width = 2.dp.toPx())
        )
    }
}

@Composable
fun OwlHomeIcon(color: Color) {
    // Home Icon - Shelter Rune (exact from HTML)
    Canvas(modifier = Modifier.size(24.dp)) {
        val strokeWidth = 2.dp.toPx()

        // Roof
        val roofPath = Path().apply {
            moveTo(size.width * 0.125f, size.height * 0.5f) // 3,12
            lineTo(size.width * 0.5f, size.height * 0.125f) // 12,3
            lineTo(size.width * 0.875f, size.height * 0.5f) // 21,12
        }
        drawPath(
            path = roofPath,
            color = color,
            style = Stroke(width = strokeWidth, join = StrokeJoin.Miter)
        )

        // Walls
        val wallsPath = Path().apply {
            moveTo(size.width * 0.21f, size.height * 0.42f)  // 5,10
            lineTo(size.width * 0.21f, size.height * 0.83f)  // 5,20
            lineTo(size.width * 0.79f, size.height * 0.83f)  // 19,20
            lineTo(size.width * 0.79f, size.height * 0.42f)  // 19,10
        }
        drawPath(
            path = wallsPath,
            color = color,
            style = Stroke(width = strokeWidth)
        )

        // Door
        drawRect(
            color = color,
            topLeft = Offset(size.width * 0.42f, size.height * 0.58f), // 10,14
            size = Size(size.width * 0.17f, size.height * 0.25f), // 4,6
            style = Stroke(width = strokeWidth)
        )
    }
}
// ============================================
// 🎤 COMPOSITE ICONS - MIC + ARROW
// ============================================

@Composable
fun OwlMicRightArrowIcon(color: Color) {
    // Mic on left + Right arrow on right (for Fwd challenge)
    Canvas(modifier = Modifier.size(32.dp)) {
        val strokeWidth = 2.5.dp.toPx()

        // Mic body (left side) - scaled to match large chevron height
        drawRoundRect(
            color = color,
            topLeft = Offset(size.width * 0.10f, size.height * 0.15f),
            size = Size(size.width * 0.22f, size.height * 0.45f),
            cornerRadius = CornerRadius(4.dp.toPx()),
            style = Stroke(width = strokeWidth)
        )

        // Mic stand
        drawLine(
            color = color,
            start = Offset(size.width * 0.21f, size.height * 0.60f),
            end = Offset(size.width * 0.21f, size.height * 0.75f),
            strokeWidth = strokeWidth
        )

        // Mic base
        drawLine(
            color = color,
            start = Offset(size.width * 0.14f, size.height * 0.75f),
            end = Offset(size.width * 0.28f, size.height * 0.75f),
            strokeWidth = strokeWidth
        )

        // Right double chevron (75% larger - height matches mic)
        val arrow1 = Path().apply {
            moveTo(size.width * 0.48f, size.height * 0.15f)
            lineTo(size.width * 0.68f, size.height * 0.375f)
            lineTo(size.width * 0.48f, size.height * 0.60f)
        }
        val arrow2 = Path().apply {
            moveTo(size.width * 0.68f, size.height * 0.15f)
            lineTo(size.width * 0.88f, size.height * 0.375f)
            lineTo(size.width * 0.68f, size.height * 0.60f)
        }
        drawPath(arrow1, color, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawPath(arrow2, color, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
fun OwlMicLeftArrowIcon(color: Color) {
    // Mic on right + Left arrow on left (for Rev challenge)
    Canvas(modifier = Modifier.size(32.dp)) {
        val strokeWidth = 2.5.dp.toPx()

        // Mic body (right side) - scaled to match large chevron height
        drawRoundRect(
            color = color,
            topLeft = Offset(size.width * 0.68f, size.height * 0.15f),
            size = Size(size.width * 0.22f, size.height * 0.45f),
            cornerRadius = CornerRadius(4.dp.toPx()),
            style = Stroke(width = strokeWidth)
        )

        // Mic stand
        drawLine(
            color = color,
            start = Offset(size.width * 0.79f, size.height * 0.60f),
            end = Offset(size.width * 0.79f, size.height * 0.75f),
            strokeWidth = strokeWidth
        )

        // Mic base
        drawLine(
            color = color,
            start = Offset(size.width * 0.72f, size.height * 0.75f),
            end = Offset(size.width * 0.86f, size.height * 0.75f),
            strokeWidth = strokeWidth
        )

        // Left double chevron (75% larger - height matches mic)
        val arrow1 = Path().apply {
            moveTo(size.width * 0.52f, size.height * 0.15f)
            lineTo(size.width * 0.32f, size.height * 0.375f)
            lineTo(size.width * 0.52f, size.height * 0.60f)
        }
        val arrow2 = Path().apply {
            moveTo(size.width * 0.32f, size.height * 0.15f)
            lineTo(size.width * 0.12f, size.height * 0.375f)
            lineTo(size.width * 0.32f, size.height * 0.60f)
        }
        drawPath(arrow1, color, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawPath(arrow2, color, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}