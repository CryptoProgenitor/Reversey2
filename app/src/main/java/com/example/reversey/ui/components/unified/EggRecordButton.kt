// EggRecordButton.kt - GLUTE Compliant Recording Button
package com.example.reversey.ui.components.unified

import android.graphics.Paint
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.reversey.ui.theme.AestheticTheme

/**
 * GLUTE-compliant recording button that displays as a beautiful fried egg
 * when egg theme is active, or falls back to standard Material Design
 */
@Composable
fun UnifiedRecordButton(
    isRecording: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val aesthetic = AestheticTheme()

    // Route to appropriate button style based on theme ID
    when (aesthetic.id) {
        "egg" -> {
            EggRecordButton(
                isRecording = isRecording,
                onClick = onClick,
                modifier = modifier
            )
        }
        "scrapbook" -> {
            ScrapbookRecordButton(
                isRecording = isRecording,
                onClick = onClick,
                modifier = modifier
            )
        }
        else -> {
            ModernRecordButton(
                isRecording = isRecording,
                onClick = onClick,
                modifier = modifier
            )
        }
    }
}

/**
 * Beautiful fried egg recording button for your daughter's egg theme! 🥚🍳
 */
@Composable
fun EggRecordButton(
    isRecording: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val aesthetic = AestheticTheme()

    // Animation for recording state
    val scale by animateFloatAsState(
        targetValue = if (isRecording) 1.02f else 1f,
        animationSpec = tween(200),
        label = "scale"
    )

    // Pulsing animation for recording ring
    val pulseAlpha by animateFloatAsState(
        targetValue = if (isRecording) 0.8f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = modifier
            .size(182.dp, 156.dp)  // 30% bigger!
            .scale(scale)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // Pulsing ring when recording
        if (isRecording) {
            Canvas(
                modifier = Modifier
                    .size(208.dp, 182.dp)  // Pulsing ring 30% bigger too
            ) {
                drawOval(
                    color = Color(0xFFFF6B6B).copy(alpha = pulseAlpha * 0.6f),
                    style = Stroke(width = 4.dp.toPx()),
                    topLeft = Offset(8.dp.toPx(), 8.dp.toPx()),
                    size = Size(
                        size.width - 16.dp.toPx(),
                        size.height - 16.dp.toPx()
                    )
                )
            }
        }

        // Main fried egg button
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // Create the irregular fried egg white shape
            val eggWhitePath = Path().apply {
                // Starting from left side, creating organic curves
                moveTo(canvasWidth * 0.18f, canvasHeight * 0.5f)

                // Top curve - irregular and organic
                quadraticBezierTo(
                    canvasWidth * 0.14f, canvasHeight * 0.33f,
                    canvasWidth * 0.29f, canvasHeight * 0.29f
                )
                quadraticBezierTo(
                    canvasWidth * 0.39f, canvasHeight * 0.25f,
                    canvasWidth * 0.46f, canvasHeight * 0.28f
                )
                quadraticBezierTo(
                    canvasWidth * 0.57f, canvasHeight * 0.23f,
                    canvasWidth * 0.68f, canvasHeight * 0.27f
                )
                quadraticBezierTo(
                    canvasWidth * 0.79f, canvasHeight * 0.29f,
                    canvasWidth * 0.84f, canvasHeight * 0.4f
                )

                // Right side curve
                quadraticBezierTo(
                    canvasWidth * 0.89f, canvasHeight * 0.5f,
                    canvasWidth * 0.82f, canvasHeight * 0.6f
                )
                quadraticBezierTo(
                    canvasWidth * 0.84f, canvasHeight * 0.71f,
                    canvasWidth * 0.75f, canvasHeight * 0.75f
                )

                // Bottom curve - wider and flatter
                quadraticBezierTo(
                    canvasWidth * 0.64f, canvasHeight * 0.8f,
                    canvasWidth * 0.54f, canvasHeight * 0.77f
                )
                quadraticBezierTo(
                    canvasWidth * 0.43f, canvasHeight * 0.8f,
                    canvasWidth * 0.32f, canvasHeight * 0.75f
                )

                // Left side return
                quadraticBezierTo(
                    canvasWidth * 0.21f, canvasHeight * 0.71f,
                    canvasWidth * 0.18f, canvasHeight * 0.62f
                )
                quadraticBezierTo(
                    canvasWidth * 0.13f, canvasHeight * 0.54f,
                    canvasWidth * 0.18f, canvasHeight * 0.5f
                )

                close()
            }

            // Draw the egg white
            drawPath(
                path = eggWhitePath,
                color = Color(0xFFFFF8E1) // Creamy white
            )

            // Draw thick hand-drawn border around egg white
            drawPath(
                path = eggWhitePath,
                color = Color(0xFF2E2E2E),
                style = Stroke(
                    width = if (aesthetic.useHandDrawnBorders) 4.dp.toPx() else 2.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // Draw the yolk (perfect circle in center-top area)
            val yolkColor = if (isRecording) Color(0xFFFF6B6B) else Color(0xFFFFD700)
            val yolkCenter = Offset(canvasWidth * 0.5f, canvasHeight * 0.46f)
            val yolkRadius = canvasWidth * 0.13f

            drawCircle(
                color = yolkColor,
                radius = yolkRadius,
                center = yolkCenter
            )

            // Yolk border
            drawCircle(
                color = Color(0xFF2E2E2E),
                radius = yolkRadius,
                center = yolkCenter,
                style = Stroke(
                    width = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )

            // Draw text in the egg white area below yolk
            val text = if (isRecording) "STOP" else "REC"
            val textY = canvasHeight * 0.71f

            drawContext.canvas.nativeCanvas.drawText(
                text,
                canvasWidth * 0.5f,
                textY,
                Paint().apply {
                    color = Color(0xFF2E2E2E).toArgb()
                    textSize = 16.sp.toPx()
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                    textAlign = Paint.Align.CENTER
                }
            )
        }
    }
}

/**
 * Scrapbook-style recording button for scrapbook theme
 */
@Composable
fun ScrapbookRecordButton(
    isRecording: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val aesthetic = AestheticTheme()

    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.size(72.dp),
        containerColor = if (isRecording)
            Color(0xFFFF6B6B) else Color(0xFFFF8C00), // Use orange instead of primaryAccentColor
        contentColor = Color.White
    ) {
        if (isRecording) {
            Icon(
                imageVector = Icons.Default.Stop,
                contentDescription = "Stop Recording",
                modifier = Modifier.size(32.dp)
            )
        } else {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "Start Recording",
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

/**
 * Modern Material Design recording button fallback
 */
@Composable
fun ModernRecordButton(
    isRecording: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = if (isRecording)
            MaterialTheme.colorScheme.error
        else
            MaterialTheme.colorScheme.primary
    ) {
        if (isRecording) {
            Icon(
                imageVector = Icons.Default.Stop,
                contentDescription = "Stop Recording"
            )
        } else {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "Start Recording"
            )
        }
    }
}