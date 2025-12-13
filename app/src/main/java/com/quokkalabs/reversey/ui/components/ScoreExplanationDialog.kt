package com.quokkalabs.reversey.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.quokkalabs.reversey.data.models.PlayerAttempt
import com.quokkalabs.reversey.scoring.WordPhonemes

/**
 * ScoreExplanationDialog v2 - Phase 4 UI
 *
 * Variant 1: Stacked layout with word-grouped phonemes
 * Shows target/attempt transcriptions, phoneme visualization, and override controls
 *
 * Backward compatible: onDismiss stays in position 2 for old call sites
 */
@Composable
fun ScoreExplanationDialog(
    attempt: PlayerAttempt,
    onDismiss: () -> Unit,
    targetTranscription: String = "",  // Empty = will derive from word phonemes
    onAccept: () -> Unit = onDismiss,  // Default: just dismiss
    onOverrideScore: (Int) -> Unit = { }  // Default: no-op (override not wired up yet)
) {
    var showOverridePanel by remember { mutableStateOf(false) }
    var selectedOverrideScore by remember { mutableIntStateOf(50) }

    val displayScore = attempt.finalScore ?: attempt.score
    val isOverridden = attempt.finalScore != null

    // Derive target transcription from word phonemes if not provided
    val effectiveTargetTranscription = targetTranscription.ifEmpty {
        attempt.targetWordPhonemes.joinToString(" ") { it.word }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                DialogHeader(onDismiss = onDismiss)

                Spacer(modifier = Modifier.height(12.dp))

                // Score Display
                ScoreDisplay(
                    score = displayScore,
                    originalScore = if (isOverridden) attempt.score else null,
                    matchedCount = attempt.phonemeMatches.count { it },
                    totalCount = attempt.targetPhonemes.size
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Target Section
                TargetSection(
                    transcription = effectiveTargetTranscription,
                    wordPhonemes = attempt.targetWordPhonemes,
                    phonemeMatches = attempt.phonemeMatches
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Attempt Section
                AttemptSection(
                    transcription = attempt.attemptTranscription ?: "(no transcription)",
                    wordPhonemes = attempt.attemptWordPhonemes
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Breakdown (Duration + Difficulty)
                BreakdownSection(attempt = attempt)

                // Override Panel (conditional)
                if (showOverridePanel) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OverridePanel(
                        selectedScore = selectedOverrideScore,
                        onScoreSelected = { selectedOverrideScore = it }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                ActionButtons(
                    showOverridePanel = showOverridePanel,
                    selectedOverrideScore = selectedOverrideScore,
                    currentScore = displayScore,
                    onAccept = {
                        if (showOverridePanel) {
                            onOverrideScore(selectedOverrideScore)
                        } else {
                            onAccept()
                        }
                    },
                    onToggleOverride = { showOverridePanel = !showOverridePanel }
                )
            }
        }
    }
}

@Composable
private fun DialogHeader(onDismiss: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "🎯 Score Breakdown",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Close",
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun ScoreDisplay(
    score: Int,
    originalScore: Int?,
    matchedCount: Int,
    totalCount: Int
) {
    val scoreColor = when {
        score >= 70 -> Color(0xFF4ADE80)  // Green
        score >= 40 -> Color(0xFFFBBF24)  // Yellow
        else -> Color(0xFFF87171)          // Red
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Show strikethrough original if overridden
            if (originalScore != null) {
                Text(
                    text = "$originalScore",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    fontStyle = FontStyle.Italic
                )
            }

            Text(
                text = "$score",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = scoreColor
            )

            Text(
                text = if (originalScore != null) "PLAYER OVERRIDE" else "$matchedCount/$totalCount phonemes matched",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                letterSpacing = 1.sp
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TargetSection(
    transcription: String,
    wordPhonemes: List<WordPhonemes>,
    phonemeMatches: List<Boolean>
) {
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val targetColor = Color(0xFFFBBF24)  // Yellow/gold

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(surfaceVariant.copy(alpha = 0.5f))
            .padding(14.dp)
    ) {
        // Section header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "🎯", fontSize = 16.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "TARGET (Reversed Phonemes of...)",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Transcription text
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(
                    width = 2.dp,
                    color = targetColor.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(6.dp)
                )
                .padding(10.dp)
        ) {
            Text(
                text = "\"$transcription\"",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = targetColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Word-grouped phonemes with match coloring
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            var phonemeIndex = 0

            wordPhonemes.forEach { wordPhoneme ->
                WordPhonemeGroup(
                    word = wordPhoneme.word,
                    phonemes = wordPhoneme.phonemes,
                    matches = phonemeMatches.subListSafe(
                        phonemeIndex,
                        phonemeIndex + wordPhoneme.phonemes.size
                    ),
                    isTarget = true
                )
                phonemeIndex += wordPhoneme.phonemes.size
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AttemptSection(
    transcription: String,
    wordPhonemes: List<WordPhonemes>
) {
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val attemptColor = Color(0xFF60A5FA)  // Blue

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(surfaceVariant.copy(alpha = 0.5f))
            .padding(14.dp)
    ) {
        // Section header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "🎤", fontSize = 16.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "YOU SAID",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Transcription text
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(
                    width = 2.dp,
                    color = attemptColor.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(6.dp)
                )
                .padding(10.dp)
        ) {
            Text(
                text = "\"$transcription\"",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = attemptColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Word-grouped phonemes (all blue, no match coloring)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            wordPhonemes.forEach { wordPhoneme ->
                WordPhonemeGroup(
                    word = wordPhoneme.word,
                    phonemes = wordPhoneme.phonemes,
                    matches = null,  // No match info for attempt
                    isTarget = false
                )
            }
        }
    }
}

@Composable
private fun WordPhonemeGroup(
    word: String,
    phonemes: List<String>,
    matches: List<Boolean>?,
    isTarget: Boolean
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Phoneme chips
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            phonemes.forEachIndexed { index, phoneme ->
                val isMatched = matches?.getOrNull(index) ?: true
                PhonemeChip(
                    phoneme = phoneme,
                    isMatched = isMatched,
                    isTarget = isTarget
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Word label
        Text(
            text = word,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            fontStyle = FontStyle.Italic
        )
    }
}

@Composable
private fun PhonemeChip(
    phoneme: String,
    isMatched: Boolean,
    isTarget: Boolean
) {
    val backgroundColor = when {
        !isTarget -> Color(0xFF1E3A5F)  // Blue for attempt
        isMatched -> Color(0xFF166534)   // Green for matched
        else -> Color(0xFF7F1D1D)        // Red for missed
    }

    val textColor = when {
        !isTarget -> Color(0xFF60A5FA)
        isMatched -> Color(0xFF4ADE80)
        else -> Color(0xFFF87171)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .padding(horizontal = 7.dp, vertical = 5.dp)
    ) {
        Text(
            text = phoneme,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
        )
    }
}

@Composable
private fun BreakdownSection(attempt: PlayerAttempt) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        // Phoneme match percentage row
        val matchPercent = if (attempt.targetPhonemes.isNotEmpty()) {
            (attempt.phonemeMatches.count { it } * 100) / attempt.targetPhonemes.size
        } else 0

        BreakdownRow(
            icon = "🔤",
            label = "Phoneme Match",
            value = "$matchPercent%",
            isGood = matchPercent >= 60
        )

        // Duration row (if available)
        attempt.durationRatio?.let { ratio ->
            BreakdownRow(
                icon = "⏱️",
                label = "Duration",
                value = String.format("%.1fx", ratio),
                isGood = ratio in 0.66f..1.33f
            )
        }

        // Difficulty row
        BreakdownRow(
            icon = "🎚️",
            label = "Difficulty",
            value = attempt.difficulty.name,
            isGood = null  // Neutral
        )
    }
}

@Composable
private fun BreakdownRow(
    icon: String,
    label: String,
    value: String,
    isGood: Boolean?
) {
    val valueColor = when (isGood) {
        true -> Color(0xFF4ADE80)
        false -> Color(0xFFF87171)
        null -> Color(0xFFFBBF24)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = icon, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Text(
            text = value + if (isGood == true) " ✓" else if (isGood == false) " ✗" else "",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = valueColor
        )
    }
}

@Composable
private fun OverridePanel(
    selectedScore: Int,
    onScoreSelected: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(14.dp)
    ) {
        Text(
            text = "SET SCORE",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Preset buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(0, 25, 50, 75, 100).forEach { score ->
                PresetButton(
                    score = score,
                    isSelected = selectedScore == score,
                    onClick = { onScoreSelected(score) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun PresetButton(
    score: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(
                width = if (isSelected) 0.dp else 2.dp,
                color = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$score",
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}

@Composable
private fun ActionButtons(
    showOverridePanel: Boolean,
    selectedOverrideScore: Int,
    currentScore: Int,
    onAccept: () -> Unit,
    onToggleOverride: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Accept button
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF4ADE80))
                .clickable { onAccept() }
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (showOverridePanel) "✓ SAVE $selectedOverrideScore" else "✓ ACCEPT",
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF052E16)
            )
        }

        // Override/Cancel button
        Box(
            modifier = Modifier
                .weight(if (showOverridePanel) 0.5f else 1f)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(10.dp)
                )
                .clickable { onToggleOverride() }
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (showOverridePanel) "CANCEL" else "OVERRIDE",
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// Safe sublist helper
private fun <T> List<T>.subListSafe(fromIndex: Int, toIndex: Int): List<T> {
    val safeFrom = fromIndex.coerceIn(0, size)
    val safeTo = toIndex.coerceIn(safeFrom, size)
    return subList(safeFrom, safeTo)
}