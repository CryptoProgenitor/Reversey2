package com.example.reversey.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.reversey.data.models.ChallengeType
import com.example.reversey.scoring.DifficultyLevel

/**
 * Difficulty-colored squircle score display with progress border
 *
 * Features:
 * - Full background in difficulty color
 * - Border shows score progress (clockwise from 12 o'clock)
 * - 3 rows: [icon+emoji] [score%] [difficulty]
 *
 * @param score The score percentage (0-100)
 * @param difficulty The difficulty level for this attempt
 * @param challengeType Forward or Reverse challenge
 * @param emoji The emoji to display based on score
 * @param width The width of the squircle
 * @param height The height of the squircle
 * @param onClick Callback when squircle is clicked
 */

/**
 * Calculate if we need light or dark text based on background color luminance
 * Returns true if background is dark (needs light text)
 */
private fun Color.isDark(): Boolean {
    val luminance = (0.299 * red + 0.587 * green + 0.114 * blue)
    return luminance < 0.5
}
@Composable
fun DifficultySquircle(
    score: Int,
    difficulty: DifficultyLevel,
    challengeType: ChallengeType,
    emoji: String,
    width: Dp = 100.dp,
    height: Dp = 130.dp,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Difficulty colors
    val difficultyColor = when (difficulty) {
        DifficultyLevel.EASY -> Color(0xFF4CAF50)    // Green
        DifficultyLevel.NORMAL -> Color(0xFF2196F3)  // Blue
        DifficultyLevel.HARD -> Color(0xFFFF9800)    // Orange
        DifficultyLevel.EXPERT -> Color(0xFF9C27B0)  // Purple
        DifficultyLevel.MASTER -> Color(0xFFFFD700)  // Gold
    }

    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // Draw squircle with full background and progress border
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasSize = this.size
            val cornerRadius = canvasSize.minDimension * 0.35f // ROUNDER corners
            val strokeWidth = 6.dp.toPx()

            // 1. Draw full background in difficulty color
            drawRoundRect(
                color = difficultyColor.copy(alpha = 0.5f),
                size = canvasSize,
                cornerRadius = CornerRadius(cornerRadius, cornerRadius)
            )

            // 2. Draw gray background border (full outline)
            drawRoundRect(
                color = Color.Gray.copy(alpha = 0.3f),
                size = canvasSize,
                cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                style = Stroke(width = strokeWidth)
            )

            // 3. Draw progress border (colored portion based on score)
            if (score > 0) {
                val progressPath = Path()
                val rect = Rect(
                    left = strokeWidth / 2,
                    top = strokeWidth / 2,
                    right = canvasSize.width - strokeWidth / 2,
                    bottom = canvasSize.height - strokeWidth / 2
                )

                // Calculate how much of the perimeter to draw
                val perimeter = 2 * (rect.width + rect.height - 4 * cornerRadius) + 2 * Math.PI.toFloat() * cornerRadius
                val progressLength = (score / 100f) * perimeter

                // Create path starting from top center (12 o'clock position)
                progressPath.addRoundRect(
                    androidx.compose.ui.geometry.RoundRect(
                        rect = rect,
                        cornerRadius = CornerRadius(cornerRadius, cornerRadius)
                    )
                )

                // Draw the progress border with pathEffect to show only the progress portion
                drawPath(
                    path = progressPath,
                    color = difficultyColor,
                    style = Stroke(
                        width = strokeWidth,
                        cap = StrokeCap.Round,
                        pathEffect = PathEffect.dashPathEffect(
                            intervals = floatArrayOf(progressLength, perimeter - progressLength),
                            phase = perimeter * 0.18f // Start from top (12 o'clock) was 0.25 - wrong value.
                        )
                    )
                )
            }
        }

        // Content: 3 rows
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(8.dp)
        ) {
            // ROW 1: Challenge type icon + emoji (side by side)
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (challengeType == ChallengeType.REVERSE) "🔄" else "▶️",
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = emoji,
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 22.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // ROW 2: Score with %
            // ROW 2: Score with %
            val textColor = if (difficultyColor.isDark()) Color.White else Color.Black
            val shadowColor = if (difficultyColor.isDark()) Color.Black else Color.White

            Text(
                text = "$score%",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = textColor,
                    shadow = androidx.compose.ui.graphics.Shadow(
                        color = shadowColor.copy(alpha = 0.6f),
                        offset = Offset(0f, 2f),
                        blurRadius = 4f
                    )
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            // ROW 3: Difficulty text
            // ROW 3: Difficulty text
            Text(
                text = difficulty.displayName.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    color = textColor,
                    letterSpacing = 0.5.sp,
                    shadow = androidx.compose.ui.graphics.Shadow(
                        color = shadowColor.copy(alpha = 0.6f),
                        offset = Offset(0f, 1f),
                        blurRadius = 2f
                    )
                )
            )
        }
    }
}