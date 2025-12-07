package com.quokkalabs.reversey.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlin.math.sin

/**
 * Universal animated analysis toast - works with any theme
 * Shows during the 1000ms audio processing delay
 *
 * Usage:
 * AnalysisToast(isVisible = showingAnalysis)
 */
@Composable
fun AnalysisToast(
    isVisible: Boolean,
    onDismiss: () -> Unit = {}
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = 300,
                easing = LinearOutSlowInEasing
            )
        ),
        exit = fadeOut(
            animationSpec = tween(
                durationMillis = 200,
                easing = FastOutSlowInEasing
            )
        )
    ) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                AnalysisCard {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Analysing audio...",
                            style = MaterialTheme.typography.titleMedium,
                            color = colorScheme.onSurface,
                            fontWeight = FontWeight.Medium
                        )

                        AnimatedWaveform()
                    }
                }
            }
        }
    }
}

@Composable
private fun AnalysisCard(
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colorScheme.surface.copy(alpha = 0.95f))
            .padding(2.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(colorScheme.primary.copy(alpha = 0.1f))
    ) {
        content()
    }
}

@Composable
private fun AnimatedWaveform() {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")

    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val amplitude by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "amplitude"
    )

    val waveColor = MaterialTheme.colorScheme.primary // Move this OUTSIDE Canvas

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
    ) {
        val centerY = size.height / 2
        val waveWidth = size.width
        val strokeWidth = 4.dp.toPx()

        // Draw animated waveform - OPTIMIZED: draw inline without allocating List
        var prevOffset: Offset? = null
        for (x in 0..waveWidth.toInt() step 8) {
            val progress = x / waveWidth
            val radians = Math.toRadians((progress * 720 + phase).toDouble())
            val yOffset = sin(radians).toFloat() * amplitude * centerY * 0.6f
            val currentOffset = Offset(x.toFloat(), centerY + yOffset)

            // Draw line from previous point to current (skip first point)
            prevOffset?.let { prev ->
                drawLine(
                    color = waveColor,
                    start = prev,
                    end = currentOffset,
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
            }
            prevOffset = currentOffset
        }

        // Draw center line
        drawLine(
            color = waveColor.copy(alpha = 0.3f),
            start = Offset(0f, centerY),
            end = Offset(waveWidth, centerY),
            strokeWidth = strokeWidth / 2,
            cap = StrokeCap.Round
        )
    }
}