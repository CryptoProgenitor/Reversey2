package com.quokkalabs.reversey.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.quokkalabs.reversey.scoring.DifficultyConfig
import com.quokkalabs.reversey.scoring.DifficultyLevel

/**
 * Difficulty indicator for the top-right corner of the home screen
 * Shows current difficulty level with emoji and text
 * Now clickable to navigate to difficulty settings! 🔧
 */
@Composable
fun DifficultyIndicator(
    difficulty: DifficultyLevel,
    onClick: () -> Unit = {}, // 🔧 ADD THIS - click handler for navigation
    modifier: Modifier = Modifier
) {
    val difficultyColor = DifficultyConfig.getColorForDifficulty(difficulty)


    Card(
        modifier = modifier
            .clickable { onClick() } // 🔧 ADD THIS - make it clickable
            .border(width = 1.dp, color = difficultyColor, shape = RoundedCornerShape(16.dp))
            .shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = difficultyColor.copy(alpha = 0.55f)),
        colors = CardDefaults.cardColors(
            containerColor = difficultyColor.copy(alpha = 0.55f)
        ),
        shape = RoundedCornerShape(16.dp)
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