package com.quokkalabs.reversey.ui.components

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quokkalabs.reversey.scoring.DifficultyConfig
import com.quokkalabs.reversey.scoring.DifficultyLevel
import com.quokkalabs.reversey.ui.viewmodels.AudioViewModel

/**
 * 🎮 DIFFICULTY BUTTON COMPONENT - OPTION B (CLEAN ARCHITECTURE)
 * - Single responsibility: UI display + trigger AudioViewModel
 * - AudioViewModel handles all business logic (presets, persistence, scoring engines)
 * - DRY: No duplicate scoring logic
 * - Long-press shows scoring explanation dialog
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DifficultyButton(
    difficulty: DifficultyLevel,
    isSelected: Boolean,
    audioViewModel: AudioViewModel,
    modifier: Modifier = Modifier
) {
    var showExplanationDialog by remember { mutableStateOf(false) }

    val glowColor = DifficultyConfig.getColorForDifficulty(difficulty)
    val containerColor = if (isSelected) {
        glowColor.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    // Show explanation dialog on long-press
    if (showExplanationDialog) {
        DifficultyExplanationDialog(
            difficulty = difficulty,
            onDismiss = { showExplanationDialog = false }
        )
    }

    Card(
        modifier = modifier
            .then(
                if (isSelected) {
                    Modifier
                        .border(2.dp, glowColor, RoundedCornerShape(12.dp))
                        .shadow(8.dp, RoundedCornerShape(12.dp), ambientColor = glowColor, spotColor = glowColor)
                } else {
                    Modifier
                }
            )
            .combinedClickable(
                onClick = {
                    Log.d("DIFFICULTY_UI", "User selected: ${difficulty.displayName}")
                    // 🎯 CLEAN ARCHITECTURE: Single call to AudioViewModel handles everything
                    audioViewModel.updateDifficulty(difficulty)
                },
                onLongClick = {
                    Log.d("DIFFICULTY_UI", "Long-press: showing ${difficulty.displayName} explanation")
                    showExplanationDialog = true
                }
            ),
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
                text = DifficultyConfig.getEmojiForDifficulty(difficulty),
                fontSize = 24.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = difficulty.displayName,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = DifficultyConfig.getDescriptionForDifficulty(difficulty),
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}