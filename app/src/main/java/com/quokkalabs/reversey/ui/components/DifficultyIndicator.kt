package com.quokkalabs.reversey.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quokkalabs.reversey.scoring.DifficultyConfig
import com.quokkalabs.reversey.scoring.DifficultyLevel
import com.quokkalabs.reversey.ui.theme.AestheticThemeData

/**
 * Difficulty indicator for the top-right corner of the home screen
 * Shows current difficulty level with emoji and text
 * Recording progress shown as draining border! 🎯
 *
 * @param difficulty Current difficulty level
 * @param onClick Click handler for navigation to settings
 * @param recordingProgress Optional progress (1.0 → 0.0) shown as draining border during recording
 * @param modifier Modifier for the component
 */
@Composable
fun DifficultyIndicator(
    difficulty: DifficultyLevel,
    aesthetic: AestheticThemeData,
    onClick: () -> Unit = {},
    recordingProgress: Float? = null,
    modifier: Modifier = Modifier
) {
    val difficultyColor = DifficultyConfig.getColorForDifficulty(difficulty)
    val arcColor = aesthetic.accentColor.copy(alpha = 0.8f)  // Theme's accent color for progress arc
    val shape = RoundedCornerShape(16.dp)

    // Determine border based on recording state
    val isRecording = recordingProgress != null && recordingProgress < 1f

    val borderModifier = if (isRecording) {
        // Progress border: sweeps clockwise from top, draining as time runs out
        // Offset shifts gradient center to align sweep with top of badge
        Modifier.border(
            width = 3.dp,
            brush = Brush.sweepGradient(
                colorStops = arrayOf(
                    0.0f to arcColor,
                    recordingProgress!! to arcColor,
                    recordingProgress to Color.Transparent,
                    1.0f to Color.Transparent
                ),
                center = Offset.Unspecified  // Centers on the shape
            ),
            shape = shape
        )
    } else {
        // Normal border
        Modifier.border(width = 1.dp, color = difficultyColor, shape = shape)
    }

    Card(
        modifier = modifier
            .clickable { onClick() }
            .then(borderModifier)
            .shadow(4.dp, shape, ambientColor = difficultyColor.copy(alpha = 0.55f)),
        colors = CardDefaults.cardColors(
            containerColor = difficultyColor.copy(alpha = 0.55f)
        ),
        shape = shape
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = DifficultyConfig.getEmojiForDifficulty(difficulty),
                fontSize = 16.sp
            )
            Text(
                text = difficulty.displayName,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black.copy(alpha = 0.8f)
            )
        }
    }
}