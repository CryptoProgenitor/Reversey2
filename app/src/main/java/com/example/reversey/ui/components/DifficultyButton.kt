package com.example.reversey.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.util.Log
import com.example.reversey.scoring.DifficultyLevel
import com.example.reversey.scoring.Presets
import com.example.reversey.scoring.ScoringEngine
import com.example.reversey.ui.viewmodels.AudioViewModel

/**
 * 🎮 DIFFICULTY BUTTON COMPONENT
 * - Clickable card showing difficulty level with emoji
 * - Glow effect when selected
 * - Updates both ScoringEngine and AudioViewModel
 */
@Composable
fun DifficultyButton(
    difficulty: DifficultyLevel,
    preset: Presets,
    isSelected: Boolean,
    scoringEngine: ScoringEngine,
    audioViewModel: AudioViewModel,
    onDifficultyChanged: (DifficultyLevel) -> Unit,
    modifier: Modifier = Modifier
) {
    val glowColor = when (difficulty) {
        DifficultyLevel.EASY -> Color(0xFF4CAF50)      // Green
        DifficultyLevel.NORMAL -> Color(0xFF2196F3)    // Blue
        DifficultyLevel.HARD -> Color(0xFFFF9800)      // Orange
        DifficultyLevel.EXPERT -> Color(0xFF9C27B0)    // Purple
        DifficultyLevel.MASTER -> Color(0xFFFFD700)    // Gold
    }
    val containerColor = if (isSelected) {
        glowColor.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    /* =========================================================
    val indicatorColor = when (difficulty) {
        DifficultyLevel.EASY -> Color(0xFF4CAF50)      // Green
        DifficultyLevel.NORMAL -> Color(0xFF2196F3)    // Blue
        DifficultyLevel.HARD -> Color(0xFFFF9800)      // Orange
        DifficultyLevel.EXPERT -> Color(0xFF9C27B0)    // Purple
        DifficultyLevel.MASTER -> Color(0xFFFFD700)    // Gold
    }

    Card(
        modifier = modifier
            .clickable { onClick() } // 🔧 ADD THIS - make it clickable
            .border(width = 1.dp, color = indicatorColor, shape = RoundedCornerShape(16.dp))
            .shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = indicatorColor.copy(alpha = 0.3f)),
        colors = CardDefaults.cardColors(
            containerColor = indicatorColor.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
    =============================================================
    */

    Card(
        modifier = modifier
            .height(100.dp)
            .then(
                if (isSelected) {
                    Modifier
                        .border(2.dp, glowColor, RoundedCornerShape(12.dp))
                        .shadow(8.dp, RoundedCornerShape(12.dp), ambientColor = glowColor, spotColor = glowColor)
                } else {
                    Modifier
                }
            )
            .clickable {
                Log.d("BEFORE_PRESET", "Before: ${scoringEngine.getCurrentDifficulty().displayName}")
                // Apply preset once (singleton means both use same instance)
                scoringEngine.applyPreset(preset)
                Log.d("AFTER_PRESET", "After: ${scoringEngine.getCurrentDifficulty().displayName}")
                onDifficultyChanged(difficulty)
            },
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = difficulty.emoji,
                fontSize = 24.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = difficulty.displayName,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
                //color = if (isSelected) glowColor else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = when (difficulty) {
                    DifficultyLevel.EASY -> "Forgiving"
                    DifficultyLevel.NORMAL -> "Balanced"
                    DifficultyLevel.HARD -> "Strict"
                    DifficultyLevel.EXPERT -> "Very Strict"
                    DifficultyLevel.MASTER -> "Perfection"
                },
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}