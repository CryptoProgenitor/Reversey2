package com.quokkalabs.reversey.ui.icons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * EggIcons.kt - Custom icons for Egg Theme v12.1.0
 * Part of GLUTE (Grand Luxurious Unified Theme Engine)
 */
object EggIcons {

    @Composable
    fun WholeEggIcon(
        size: Dp = 24.dp,
        tint: Color = Color.Black
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val canvasSize = size.toPx()
            val strokeWidth = 2.5.dp.toPx()

            // Draw egg shape - oval with pointed top
            val eggPath = Path().apply {
                moveTo(canvasSize * 0.5f, canvasSize * 0.1f) // Top point
                cubicTo(
                    canvasSize * 0.75f, canvasSize * 0.1f,
                    canvasSize * 0.9f, canvasSize * 0.3f,
                    canvasSize * 0.85f, canvasSize * 0.5f
                )
                cubicTo(
                    canvasSize * 0.9f, canvasSize * 0.7f,
                    canvasSize * 0.75f, canvasSize * 0.9f,
                    canvasSize * 0.5f, canvasSize * 0.9f
                )
                cubicTo(
                    canvasSize * 0.25f, canvasSize * 0.9f,
                    canvasSize * 0.1f, canvasSize * 0.7f,
                    canvasSize * 0.15f, canvasSize * 0.5f
                )
                cubicTo(
                    canvasSize * 0.1f, canvasSize * 0.3f,
                    canvasSize * 0.25f, canvasSize * 0.1f,
                    canvasSize * 0.5f, canvasSize * 0.1f
                )
                close()
            }

            drawPath(
                path = eggPath,
                color = tint,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
    }

    @Composable
    fun FriedEggIcon(
        size: Dp = 24.dp,
        tint: Color = Color.Black
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val canvasSize = size.toPx()

            // Irregular egg white
            val eggWhitePath = Path().apply {
                moveTo(canvasSize * 0.2f, canvasSize * 0.5f)
                quadraticTo(canvasSize * 0.1f, canvasSize * 0.3f, canvasSize * 0.3f, canvasSize * 0.25f)
                quadraticTo(canvasSize * 0.5f, canvasSize * 0.2f, canvasSize * 0.7f, canvasSize * 0.25f)
                quadraticTo(canvasSize * 0.9f, canvasSize * 0.3f, canvasSize * 0.85f, canvasSize * 0.5f)
                quadraticTo(canvasSize * 0.9f, canvasSize * 0.7f, canvasSize * 0.7f, canvasSize * 0.8f)
                quadraticTo(canvasSize * 0.5f, canvasSize * 0.85f, canvasSize * 0.3f, canvasSize * 0.8f)
                quadraticTo(canvasSize * 0.1f, canvasSize * 0.7f, canvasSize * 0.2f, canvasSize * 0.5f)
                close()
            }

            // Draw egg white
            drawPath(
                path = eggWhitePath,
                color = tint,
                style = Stroke(width = 3.dp.toPx())
            )

            // Draw yolk
            drawCircle(
                color = tint,
                radius = canvasSize * 0.15f,
                center = center.copy(y = center.y - canvasSize * 0.05f),
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }

    @Composable
    fun CrackedEggIcon(
        size: Dp = 24.dp,
        tint: Color = Color.Black
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val canvasSize = size.toPx()

            // Left half of egg
            val leftHalf = Path().apply {
                moveTo(canvasSize * 0.5f, canvasSize * 0.1f)
                cubicTo(
                    canvasSize * 0.25f, canvasSize * 0.1f,
                    canvasSize * 0.1f, canvasSize * 0.3f,
                    canvasSize * 0.15f, canvasSize * 0.5f
                )
                cubicTo(
                    canvasSize * 0.1f, canvasSize * 0.7f,
                    canvasSize * 0.25f, canvasSize * 0.9f,
                    canvasSize * 0.45f, canvasSize * 0.9f
                )
                lineTo(canvasSize * 0.5f, canvasSize * 0.1f)
                close()
            }

            // Right half of egg
            val rightHalf = Path().apply {
                moveTo(canvasSize * 0.5f, canvasSize * 0.1f)
                cubicTo(
                    canvasSize * 0.75f, canvasSize * 0.1f,
                    canvasSize * 0.9f, canvasSize * 0.3f,
                    canvasSize * 0.85f, canvasSize * 0.5f
                )
                cubicTo(
                    canvasSize * 0.9f, canvasSize * 0.7f,
                    canvasSize * 0.75f, canvasSize * 0.9f,
                    canvasSize * 0.55f, canvasSize * 0.9f
                )
                lineTo(canvasSize * 0.5f, canvasSize * 0.1f)
                close()
            }

            // Draw separated halves
            drawPath(
                path = leftHalf,
                color = tint,
                style = Stroke(width = 2.dp.toPx())
            )

            drawPath(
                path = rightHalf,
                color = tint,
                style = Stroke(width = 2.dp.toPx())
            )

            // Draw crack line
            val crackPath = Path().apply {
                moveTo(canvasSize * 0.45f, canvasSize * 0.2f)
                lineTo(canvasSize * 0.55f, canvasSize * 0.35f)
                lineTo(canvasSize * 0.42f, canvasSize * 0.5f)
                lineTo(canvasSize * 0.58f, canvasSize * 0.65f)
                lineTo(canvasSize * 0.48f, canvasSize * 0.8f)
            }

            drawPath(
                path = crackPath,
                color = tint,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }

    @Composable
    fun ChickIcon(
        size: Dp = 24.dp,
        tint: Color = Color.Black
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val canvasSize = size.toPx()
            val strokeWidth = 2.dp.toPx()

            // Chick body (circle)
            drawCircle(
                color = tint,
                radius = canvasSize * 0.25f,
                center = center.copy(y = center.y + canvasSize * 0.1f),
                style = Stroke(width = strokeWidth)
            )

            // Chick head (smaller circle)
            drawCircle(
                color = tint,
                radius = canvasSize * 0.18f,
                center = center.copy(y = center.y - canvasSize * 0.2f),
                style = Stroke(width = strokeWidth)
            )

            // Beak (triangle)
            val beakPath = Path().apply {
                moveTo(canvasSize * 0.6f, canvasSize * 0.35f)
                lineTo(canvasSize * 0.75f, canvasSize * 0.4f)
                lineTo(canvasSize * 0.6f, canvasSize * 0.45f)
                close()
            }

            drawPath(
                path = beakPath,
                color = tint,
                style = Stroke(width = strokeWidth)
            )

            // Eye (small circle)
            drawCircle(
                color = tint,
                radius = canvasSize * 0.03f,
                center = center.copy(
                    x = center.x + canvasSize * 0.05f,
                    y = center.y - canvasSize * 0.25f
                )
            )
        }
    }

    @Composable
    fun EggMicIcon(
        size: Dp = 24.dp,
        tint: Color = Color.Black
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val canvasSize = size.toPx()
            val strokeWidth = 2.5.dp.toPx()

            // Microphone body (egg shape)
            val micPath = Path().apply {
                moveTo(canvasSize * 0.5f, canvasSize * 0.15f)
                cubicTo(
                    canvasSize * 0.7f, canvasSize * 0.15f,
                    canvasSize * 0.8f, canvasSize * 0.3f,
                    canvasSize * 0.75f, canvasSize * 0.45f
                )
                cubicTo(
                    canvasSize * 0.8f, canvasSize * 0.6f,
                    canvasSize * 0.7f, canvasSize * 0.7f,
                    canvasSize * 0.5f, canvasSize * 0.7f
                )
                cubicTo(
                    canvasSize * 0.3f, canvasSize * 0.7f,
                    canvasSize * 0.2f, canvasSize * 0.6f,
                    canvasSize * 0.25f, canvasSize * 0.45f
                )
                cubicTo(
                    canvasSize * 0.2f, canvasSize * 0.3f,
                    canvasSize * 0.3f, canvasSize * 0.15f,
                    canvasSize * 0.5f, canvasSize * 0.15f
                )
                close()
            }

            drawPath(
                path = micPath,
                color = tint,
                style = Stroke(width = strokeWidth)
            )

            // Mic stand
            drawLine(
                color = tint,
                start = Offset(canvasSize * 0.5f, canvasSize * 0.7f),
                end = Offset(canvasSize * 0.5f, canvasSize * 0.85f),
                strokeWidth = strokeWidth
            )

            // Base
            drawLine(
                color = tint,
                start = Offset(canvasSize * 0.35f, canvasSize * 0.85f),
                end = Offset(canvasSize * 0.65f, canvasSize * 0.85f),
                strokeWidth = strokeWidth
            )

            // Sound waves
            drawArc(
                color = tint,
                startAngle = -45f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(canvasSize * 0.75f, canvasSize * 0.25f),
                size = androidx.compose.ui.geometry.Size(canvasSize * 0.2f, canvasSize * 0.2f),
                style = Stroke(width = strokeWidth * 0.7f)
            )
        }
    }
}