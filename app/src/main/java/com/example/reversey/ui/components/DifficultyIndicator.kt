package com.example.reversey.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.reversey.scoring.DifficultyLevel

/**
 * Difficulty indicator for the top-right corner of the home screen
 * Shows current difficulty level with emoji and text
 */
@Composable
fun DifficultyIndicator(
    difficulty: DifficultyLevel,
    modifier: Modifier = Modifier
) {
    val indicatorColor = when (difficulty) {
        DifficultyLevel.EASY -> Color(0xFF4CAF50)      // Green
        DifficultyLevel.NORMAL -> Color(0xFF2196F3)    // Blue
        DifficultyLevel.HARD -> Color(0xFFFF9800)      // Orange
        DifficultyLevel.EXPERT -> Color(0xFF9C27B0)    // Purple
        DifficultyLevel.MASTER -> Color(0xFFFFD700)    // Gold
    }

    Card(
        modifier = modifier
            .border(1.dp, indicatorColor, RoundedCornerShape(16.dp))
            .shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = indicatorColor.copy(alpha = 0.3f)),
        colors = CardDefaults.cardColors(
            containerColor = indicatorColor.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = difficulty.emoji,
                fontSize = 16.sp
            )
            Text(
                text = difficulty.displayName,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = indicatorColor
            )
        }
    }
}

/**
 * Compact difficulty indicator for smaller spaces
 */
@Composable
fun CompactDifficultyIndicator(
    difficulty: DifficultyLevel,
    modifier: Modifier = Modifier
) {
    val indicatorColor = when (difficulty) {
        DifficultyLevel.EASY -> Color(0xFF4CAF50)      // Green
        DifficultyLevel.NORMAL -> Color(0xFF2196F3)    // Blue
        DifficultyLevel.HARD -> Color(0xFFFF9800)      // Orange
        DifficultyLevel.EXPERT -> Color(0xFF9C27B0)    // Purple
        DifficultyLevel.MASTER -> Color(0xFFFFD700)    // Gold
    }

    Box(
        modifier = modifier
            .size(32.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(indicatorColor.copy(alpha = 0.2f))
            .border(1.dp, indicatorColor, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = difficulty.emoji,
            fontSize = 16.sp
        )
    }
}

/**
 * Detailed difficulty card for settings or info screens
 */
@Composable
fun DetailedDifficultyCard(
    difficulty: DifficultyLevel,
    modifier: Modifier = Modifier
) {
    val indicatorColor = when (difficulty) {
        DifficultyLevel.EASY -> Color(0xFF4CAF50)      // Green
        DifficultyLevel.NORMAL -> Color(0xFF2196F3)    // Blue
        DifficultyLevel.HARD -> Color(0xFFFF9800)      // Orange
        DifficultyLevel.EXPERT -> Color(0xFF9C27B0)    // Purple
        DifficultyLevel.MASTER -> Color(0xFFFFD700)    // Gold
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(2.dp, indicatorColor, RoundedCornerShape(12.dp))
            .shadow(6.dp, RoundedCornerShape(12.dp), ambientColor = indicatorColor.copy(alpha = 0.3f)),
        colors = CardDefaults.cardColors(
            containerColor = indicatorColor.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = difficulty.emoji,
                fontSize = 32.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = difficulty.displayName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = indicatorColor
            )
            Text(
                text = difficulty.description,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}