package com.example.reversey.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*

// Extension function to detect drag gestures
import androidx.compose.foundation.gestures.detectDragGestures

@Composable
fun ColorCirclePicker(
    selectedColor: Color?,
    defaultColor: Color,
    onColorSelected: (Color) -> Unit,
    onResetToDefault: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val circleRadius = with(density) { 100.dp.toPx() }
    val centerRadius = with(density) { 20.dp.toPx() }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title
        Text(
            text = "Custom Accent Color",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Color circle picker
        Box(
            modifier = Modifier
                .size(220.dp)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures { change, _ ->
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val offset = change.position - center
                            val distance = sqrt(offset.x * offset.x + offset.y * offset.y)

                            if (distance <= circleRadius && distance >= centerRadius) {
                                val angle = atan2(offset.y, offset.x)
                                val normalizedDistance = (distance - centerRadius) / (circleRadius - centerRadius)
                                val saturation = normalizedDistance.coerceIn(0f, 1f)

                                // Convert angle to hue (0-360 degrees)
                                val hue = ((angle * 180f / PI + 360f) % 360f).toFloat()

                                // Create HSV color with full brightness
                                val hsvColor = android.graphics.Color.HSVToColor(
                                    floatArrayOf(hue, saturation, 1f)
                                )
                                onColorSelected(Color(hsvColor))
                            }
                        }
                    }
            ) {
                drawColorWheel(circleRadius, centerRadius)

                // Draw selected color indicator
                selectedColor?.let { color ->
                    val hsv = FloatArray(3)
                    android.graphics.Color.colorToHSV(color.toArgb(), hsv)
                    val hue = hsv[0]
                    val saturation = hsv[1]

                    val angle = hue * PI / 180.0
                    val radius = centerRadius + saturation * (circleRadius - centerRadius)
                    val indicatorX = center.x + (radius * cos(angle)).toFloat()
                    val indicatorY = center.y + (radius * sin(angle)).toFloat()

                    // Draw outer ring
                    drawCircle(
                        color = Color.White,
                        radius = 12.dp.toPx(),
                        center = Offset(indicatorX, indicatorY)
                    )
                    // Draw inner circle with selected color
                    drawCircle(
                        color = color,
                        radius = 8.dp.toPx(),
                        center = Offset(indicatorX, indicatorY)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Current color preview
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Default color preview
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(defaultColor, CircleShape)
                        .border(2.dp, Color.Gray, CircleShape)
                )
                Text(
                    text = "Default",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Arrow or vs
            Text(
                text = "→",
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Current/Selected color preview
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(selectedColor ?: defaultColor, CircleShape)
                        .border(
                            2.dp,
                            if (selectedColor != null) MaterialTheme.colorScheme.primary else Color.Gray,
                            CircleShape
                        )
                )
                Text(
                    text = if (selectedColor != null) "Custom" else "Default",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Reset button
        OutlinedButton(
            onClick = onResetToDefault,
            enabled = selectedColor != null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Reset to Theme Default")
        }
    }
}

private fun DrawScope.drawColorWheel(outerRadius: Float, innerRadius: Float) {
    val center = size.center

    // Number of segments for smooth color transition
    val segments = 360
    val angleStep = 360f / segments

    for (i in 0 until segments) {
        val startAngle = i * angleStep
        val endAngle = (i + 1) * angleStep

        // Create HSV color for this segment
        val hue = startAngle
        val color1 = Color.hsv(hue, 0f, 1f) // White center
        val color2 = Color.hsv(hue, 1f, 1f) // Full saturation edge

        // Draw gradient from center to edge
        val brush = Brush.radialGradient(
            colors = listOf(color1, color2),
            center = center,
            radius = outerRadius
        )

        // Draw arc segment
        drawArc(
            brush = brush,
            startAngle = startAngle,
            sweepAngle = angleStep,
            useCenter = true,
            topLeft = Offset(
                center.x - outerRadius,
                center.y - outerRadius
            ),
            size = androidx.compose.ui.geometry.Size(outerRadius * 2, outerRadius * 2)
        )
    }

    // Draw white center hole
    drawCircle(
        color = Color.White,
        radius = innerRadius,
        center = center
    )
}
