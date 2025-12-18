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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.quokkalabs.reversey.data.models.PlayerAttempt
import com.quokkalabs.reversey.scoring.WordPhonemes
import kotlinx.coroutines.launch

// ═══════════════════════════════════════════════════════════════
//  SPACING CONFIG - Change ACTIVE_SPACING to switch layouts
// ═══════════════════════════════════════════════════════════════

private enum class SpacingMode { TIGHT, NORMAL, LOOSE }

private data class SpacingConfig(
    val cardPadding: Dp,
    val sectionGap: Dp,
    val innerPadding: Dp,
    val smallGap: Dp
)

private val SPACING_CONFIGS = mapOf(
    SpacingMode.TIGHT to SpacingConfig(
        cardPadding = 10.dp,
        sectionGap = 6.dp,
        innerPadding = 10.dp,
        smallGap = 4.dp
    ),
    SpacingMode.NORMAL to SpacingConfig(
        cardPadding = 12.dp,
        sectionGap = 10.dp,
        innerPadding = 14.dp,
        smallGap = 6.dp
    ),
    SpacingMode.LOOSE to SpacingConfig(
        cardPadding = 16.dp,
        sectionGap = 16.dp,
        innerPadding = 20.dp,
        smallGap = 10.dp
    )
)

// 👇 CHANGE THIS TO SWITCH SPACING 👇
private val ACTIVE_SPACING = SpacingMode.TIGHT
private val S = SPACING_CONFIGS[ACTIVE_SPACING] ?: SpacingConfig(
    cardPadding = 10.dp,
    sectionGap = 6.dp,
    innerPadding = 10.dp,
    smallGap = 4.dp
)

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
    onOverrideScore: (Int) -> Unit = { },  // Override to specific score
    onResetScore: () -> Unit = { }  // Reset to algo score (clears finalScore)
) {
    var showOverridePanel by remember { mutableStateOf(false) }
    var selectedOverrideScore by remember { mutableIntStateOf(50) }
    var showFormulaToast by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    val displayScore = attempt.finalScore ?: attempt.score
    val isOverridden = attempt.finalScore != null

    // Auto-scroll to bottom when override panel opens
    LaunchedEffect(showOverridePanel) {
        if (showOverridePanel) {
            coroutineScope.launch {
                scrollState.animateScrollTo(scrollState.maxValue)
            }
        }
    }

    // Derive target transcription from word phonemes if not provided
    val effectiveTargetTranscription = targetTranscription.ifEmpty {
        attempt.targetWordPhonemes.joinToString(" ") { it.word }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(S.cardPadding)
                    .verticalScroll(scrollState)
            ) {
                // Header
                DialogHeader(onDismiss = onDismiss)

                Spacer(modifier = Modifier.height(S.sectionGap))

                // Score Display (tap for formula)
                ScoreDisplay(
                    score = displayScore,
                    originalScore = if (isOverridden) attempt.score else null,
                    matchedCount = attempt.phonemeMatches.count { it },
                    totalCount = attempt.targetPhonemes.size,
                    onTap = { showFormulaToast = true }
                )

                // Formula Toast (dismissable overlay)
                if (showFormulaToast) {
                    FormulaToast(
                        attempt = attempt,
                        onDismiss = { showFormulaToast = false }
                    )
                }

                Spacer(modifier = Modifier.height(S.sectionGap))

                // Target Section
                TargetSection(
                    transcription = effectiveTargetTranscription,
                    wordPhonemes = attempt.targetWordPhonemes,
                    phonemeMatches = attempt.phonemeMatches
                )

                Spacer(modifier = Modifier.height(S.sectionGap))

                // Attempt Section
                AttemptSection(
                    transcription = attempt.attemptTranscription ?: "(no transcription)",
                    wordPhonemes = attempt.attemptWordPhonemes
                )

                Spacer(modifier = Modifier.height(S.sectionGap))

                // Breakdown (Duration + Difficulty)
                BreakdownSection(attempt = attempt)

                // Override Panel (conditional)
                if (showOverridePanel) {
                    Spacer(modifier = Modifier.height(S.sectionGap))
                    OverridePanel(
                        selectedScore = selectedOverrideScore,
                        onScoreSelected = { selectedOverrideScore = it }
                    )
                }

                Spacer(modifier = Modifier.height(S.sectionGap))

                // Action Buttons
                ActionButtons(
                    showOverridePanel = showOverridePanel,
                    selectedOverrideScore = selectedOverrideScore,
                    currentScore = displayScore,
                    isOverridden = isOverridden,
                    algoScore = attempt.score,
                    onAccept = {
                        if (showOverridePanel) {
                            onOverrideScore(selectedOverrideScore)
                            onDismiss()  // Close dialog after override
                        } else {
                            onAccept()
                        }
                    },
                    onToggleOverride = { showOverridePanel = !showOverridePanel },
                    onReset = { onResetScore() }
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
    totalCount: Int,
    onTap: () -> Unit = {}
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
            .clickable { onTap() }
            .padding(S.innerPadding),
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

            // Hint to tap
            if (originalScore == null) {
                Text(
                    text = "tap for formula",
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    fontStyle = FontStyle.Italic
                )
            }
        }
    }
}

/**
 * Dismissable toast showing the mathematical formula calculation.
 * Uses the same algorithm as the scoring engine for each difficulty level.
 */
@Composable
private fun FormulaToast(
    attempt: PlayerAttempt,
    onDismiss: () -> Unit
) {
    // Get breakdown from scoring engine (uses correct algorithm per difficulty)
    val breakdown = com.quokkalabs.reversey.scoring.ReverseScoringEngine.calculateFormulaBreakdown(
        targetPhonemes = attempt.targetPhonemes,
        attemptPhonemes = attempt.attemptPhonemes,
        difficulty = attempt.difficulty,
        durationRatio = attempt.durationRatio ?: 1f
    )

    val difficultyLabel = when (attempt.difficulty) {
        com.quokkalabs.reversey.scoring.DifficultyLevel.EASY -> "Easy"
        com.quokkalabs.reversey.scoring.DifficultyLevel.NORMAL -> "Normal"
        com.quokkalabs.reversey.scoring.DifficultyLevel.HARD -> "Hard"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1E1E2E))
            .clickable { onDismiss() }
            .padding(12.dp)
    ) {
        Column {
            // Header with leniency mode
            Text(
                text = "📐 ${breakdown.leniencyMode} Formula ($difficultyLabel)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF89B4FA)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Phoneme overlap calculation - varies by mode
            when (breakdown.leniencyMode) {
                "FUZZY" -> {
                    Text(
                        text = "matchScore = exact + partial credit",
                        fontSize = 10.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = Color(0xFFA6E3A1)
                    )
                    Text(
                        text = "           = ${String.format("%.1f", breakdown.matchScore ?: 0f)}",
                        fontSize = 10.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = Color(0xFFCDD6F4)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "phonemeOverlap = matchScore / target",
                        fontSize = 10.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = Color(0xFFA6E3A1)
                    )
                    Text(
                        text = "              = ${String.format("%.1f", breakdown.matchScore ?: 0f)} / ${breakdown.targetCount} = ${String.format("%.3f", breakdown.phonemeOverlap)}",
                        fontSize = 10.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = Color(0xFFCDD6F4)
                    )
                }
                "EXACT" -> {
                    Text(
                        text = "intersection = ${breakdown.intersection} matched",
                        fontSize = 10.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = Color(0xFFA6E3A1)
                    )
                    Text(
                        text = "union = target + attempt - intersection",
                        fontSize = 10.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = Color(0xFFA6E3A1)
                    )
                    Text(
                        text = "      = ${breakdown.targetCount} + ${breakdown.attemptCount} - ${breakdown.intersection} = ${breakdown.union}",
                        fontSize = 10.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = Color(0xFFCDD6F4)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "phonemeOverlap = intersection / union",
                        fontSize = 10.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = Color(0xFFA6E3A1)
                    )
                    Text(
                        text = "              = ${breakdown.intersection} / ${breakdown.union} = ${String.format("%.3f", breakdown.phonemeOverlap)}",
                        fontSize = 10.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = Color(0xFFCDD6F4)
                    )
                }
                "ORDERED" -> {
                    Text(
                        text = "lcs = ${breakdown.lcs} (longest common subsequence)",
                        fontSize = 10.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = Color(0xFFA6E3A1)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "phonemeOverlap = lcs / target",
                        fontSize = 10.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = Color(0xFFA6E3A1)
                    )
                    Text(
                        text = "              = ${breakdown.lcs} / ${breakdown.targetCount} = ${String.format("%.3f", breakdown.phonemeOverlap)}",
                        fontSize = 10.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = Color(0xFFCDD6F4)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Phoneme base (common to all)
            Text(
                text = "phonemeBase = √(${String.format("%.3f", breakdown.phonemeOverlap)}) × 0.85 = ${String.format("%.3f", breakdown.phonemeBase)}",
                fontSize = 10.sp,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                color = Color(0xFFF9E2AF)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Duration bonus calculation
            Text(
                text = "durationBonus = 0.15 × e^(-(${String.format("%.2f", breakdown.durationRatio)}-1)²/${breakdown.gaussianWidth})",
                fontSize = 10.sp,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                color = Color(0xFFF9E2AF)
            )
            Text(
                text = "             = ${String.format("%.3f", breakdown.durationBonus)}",
                fontSize = 10.sp,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                color = Color(0xFFCDD6F4)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Final score
            Text(
                text = "score = (${String.format("%.3f", breakdown.phonemeBase)} + ${String.format("%.3f", breakdown.durationBonus)}) × 100",
                fontSize = 10.sp,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                color = Color(0xFFF38BA8)
            )
            Text(
                text = "      = ${breakdown.finalScore}%",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                color = Color(0xFF4ADE80)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "tap to dismiss",
                fontSize = 9.sp,
                color = Color(0xFF6C7086),
                fontStyle = FontStyle.Italic,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
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
            .padding(S.innerPadding)
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

        Spacer(modifier = Modifier.height(S.sectionGap))

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

        Spacer(modifier = Modifier.height(S.sectionGap))

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
            .padding(S.innerPadding)
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

        Spacer(modifier = Modifier.height(S.sectionGap))

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

        Spacer(modifier = Modifier.height(S.sectionGap))

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
            .padding(S.innerPadding)
    ) {
        Text(
            text = "SET SCORE",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(S.sectionGap))

        // Slider with value display
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            androidx.compose.material3.Slider(
                value = selectedScore.toFloat(),
                onValueChange = { onScoreSelected(it.toInt()) },
                valueRange = 0f..100f,
                steps = 19,  // 0, 5, 10, 15... 100
                modifier = Modifier.weight(1f),
                colors = androidx.compose.material3.SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary
                )
            )

            Text(
                text = "$selectedScore",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.width(50.dp),
                textAlign = TextAlign.End
            )
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
    isOverridden: Boolean,
    algoScore: Int,
    onAccept: () -> Unit,
    onToggleOverride: () -> Unit,
    onReset: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(S.smallGap)
    ) {
        // Main action row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(S.sectionGap)
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

        // Reset button (only shown if score has been overridden)
        if (isOverridden && !showOverridePanel) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF87171).copy(alpha = 0.2f))
                    .border(
                        width = 1.dp,
                        color = Color(0xFFF87171),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .clickable { onReset() }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "↺ RESET TO AUTOMATIC SCORE ($algoScore%)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFF87171)
                )
            }
        }
    }
}

// Safe sublist helper
private fun <T> List<T>.subListSafe(fromIndex: Int, toIndex: Int): List<T> {
    val safeFrom = fromIndex.coerceIn(0, size)
    val safeTo = toIndex.coerceIn(safeFrom, size)
    return subList(safeFrom, safeTo)
}