package com.quokkalabs.reversey.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.quokkalabs.reversey.data.models.ChallengeType
import com.quokkalabs.reversey.data.models.PlayerAttempt
import com.quokkalabs.reversey.scoring.CalculationStep
import com.quokkalabs.reversey.scoring.DifficultyLevel
import com.quokkalabs.reversey.scoring.ScoreCalculationBreakdown
import com.quokkalabs.reversey.scoring.ScoringEngineType
import com.quokkalabs.reversey.scoring.VocalMode
import com.quokkalabs.reversey.scoring.getKeyModifiers
import com.quokkalabs.reversey.scoring.toDisplaySteps
import com.quokkalabs.reversey.scoring.toQuickSummary
import com.quokkalabs.reversey.ui.theme.AestheticTheme
import com.quokkalabs.reversey.ui.theme.AestheticThemeData
import com.quokkalabs.reversey.ui.theme.MaterialColors

/**
 * 🎨 UNIFIED Score Explanation Dialog v8 - SINGLE SOURCE OF TRUTH
 *
 * NO THEME ID SNIFFING.
 * NO HARDCODED WEIGHTS.
 * All weights pulled from ScoreCalculationBreakdown.
 */

private data class TierStyle(
    val bgColor: Color,
    val bordColor: Color,
    val primColor: Color,
    val txtSize: TextUnit,
    val bordWidth: Dp,
    val hasGlow: Boolean
)

private enum class MetricCategory { WEIGHTED, BONUS, MULTIPLIER }

private fun deriveTierStyle(
    category: MetricCategory,
    isHighWeight: Boolean,
    themeAccent: Color,
    themeSurface: Color
): TierStyle {
    return when (category) {
        MetricCategory.WEIGHTED -> if (isHighWeight) {
            TierStyle(
                bgColor = themeAccent.copy(alpha = 0.25f),
                bordColor = themeAccent,
                primColor = themeAccent,
                txtSize = 19.sp,
                bordWidth = 3.dp,
                hasGlow = true
            )
        } else {
            TierStyle(
                bgColor = themeAccent.copy(alpha = 0.12f),
                bordColor = themeAccent.copy(alpha = 0.5f),
                primColor = themeAccent.copy(alpha = 0.7f),
                txtSize = 15.sp,
                bordWidth = 2.dp,
                hasGlow = false
            )
        }
        MetricCategory.BONUS -> TierStyle(
            bgColor = themeAccent.copy(alpha = 0.15f),
            bordColor = themeAccent.copy(alpha = 0.6f),
            primColor = themeAccent.copy(alpha = 0.8f),
            txtSize = 16.sp,
            bordWidth = 2.dp,
            hasGlow = false
        )
        MetricCategory.MULTIPLIER -> TierStyle(
            bgColor = themeSurface.copy(alpha = 0.1f),
            bordColor = themeAccent.copy(alpha = 0.25f),
            primColor = themeAccent.copy(alpha = 0.45f),
            txtSize = 13.sp,
            bordWidth = 1.dp,
            hasGlow = false
        )
    }
}

@Composable
fun ScoreExplanationDialog(
    attempt: PlayerAttempt,
    onDismiss: () -> Unit
) {
    val aesthetic = AestheticTheme()
    val colors = MaterialColors()

    var tooltipType by remember { mutableStateOf<TooltipType?>(null) }

    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(animation = tween(2000), repeatMode = RepeatMode.Reverse),
        label = "glow_alpha"
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(3000), repeatMode = RepeatMode.Reverse),
        label = "rotation"
    )

    val gearRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(10000, easing = LinearEasing), repeatMode = RepeatMode.Restart),
        label = "gear_rotation"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true, usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)).clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .clickable(onClick = {}, indication = null, interactionSource = remember { MutableInteractionSource() })
            ) {
                if (aesthetic.useGlassmorphism && aesthetic.glowIntensity > 0) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .blur((aesthetic.glowIntensity * 25).dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(colors.primary.copy(alpha = glowAlpha * aesthetic.glowIntensity), Color.Transparent)
                                )
                            )
                    )
                }

                UnifiedScoreCard(
                    attempt = attempt,
                    aesthetic = aesthetic,
                    colors = colors,
                    rotation = rotation,
                    glowAlpha = glowAlpha,
                    gearRotation = gearRotation,
                    onDismiss = onDismiss,
                    onTooltipRequest = { tooltipType = it }
                )
            }
        }
    }

    tooltipType?.let { type ->
        if (type == TooltipType.SCORE_BREAKDOWN) {
            attempt.calculationBreakdown?.let { breakdown ->
                BreakdownTooltipDialog(breakdown = breakdown, onDismiss = { tooltipType = null })
            } ?: MetricTooltipDialog(tooltipType = type, breakdown = attempt.calculationBreakdown, onDismiss = { tooltipType = null })
        } else {
            MetricTooltipDialog(tooltipType = type, breakdown = attempt.calculationBreakdown, onDismiss = { tooltipType = null })
        }
    }
}

@Composable
private fun UnifiedScoreCard(
    attempt: PlayerAttempt,
    aesthetic: AestheticThemeData,
    colors: ColorScheme,
    rotation: Float,
    glowAlpha: Float,
    gearRotation: Float,
    onDismiss: () -> Unit,
    onTooltipRequest: (TooltipType) -> Unit
) {
    val cardModifier = Modifier
        .then(
            if (aesthetic.maxCardRotation > 0f) Modifier.rotate(rotation * aesthetic.maxCardRotation)
            else Modifier
        )
        .background(
            brush = aesthetic.primaryGradient,
            shape = RoundedCornerShape(16.dp)
        )
        .border(
            width = aesthetic.borderWidth.dp,
            color = if (aesthetic.glowIntensity > 0f)
                aesthetic.cardBorder.copy(alpha = glowAlpha)
            else
                aesthetic.cardBorder,
            shape = RoundedCornerShape(16.dp)
        )

    Card(
        modifier = cardModifier.fillMaxWidth().padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box {
            Column(
                modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (aesthetic.shadowElevation > 5f) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp).rotate(gearRotation),
                        tint = aesthetic.cardBorder
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                ScoreHeaderRow(
                    attempt = attempt,
                    aesthetic = aesthetic,
                    colors = colors,
                    onScoreClick = { onTooltipRequest(TooltipType.SCORE_BREAKDOWN) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                TagsRow(
                    attempt = attempt,
                    colors = colors,
                    onVocalModeClick = {
                        val mode = attempt.vocalAnalysis?.mode
                        onTooltipRequest(if (mode == VocalMode.SINGING) TooltipType.SINGING_MODE else TooltipType.SPEECH_MODE)
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = aesthetic.scoreFeedback.getMessage(attempt.score),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = aesthetic.primaryTextColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                RichMetricsGrid(
                    attempt = attempt,
                    aesthetic = aesthetic,
                    colors = colors,
                    onMetricClick = onTooltipRequest
                )

                Spacer(modifier = Modifier.height(12.dp))

                TipsSection(attempt = attempt, aesthetic = aesthetic)
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(32.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close",
                    tint = aesthetic.secondaryTextColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun ScoreHeaderRow(
    attempt: PlayerAttempt,
    aesthetic: AestheticThemeData,
    colors: ColorScheme,
    onScoreClick: () -> Unit = {}
) {
    val emoji = aesthetic.scoreEmojis.entries
        .sortedByDescending { it.key }
        .firstOrNull { attempt.score >= it.key }
        ?.value ?: "🎤"

    val hasBreakdown = attempt.calculationBreakdown != null

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = emoji, fontSize = 40.sp, modifier = Modifier.padding(end = 12.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = if (hasBreakdown) {
                Modifier
                    .clickable { onScoreClick() }
                    .background(aesthetic.cardBorder.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            } else Modifier
        ) {
            Text(
                text = "${attempt.score}%",
                fontSize = 48.sp,
                fontWeight = FontWeight.ExtraBold,
                color = aesthetic.primaryTextColor,
                letterSpacing = if (aesthetic.useWideLetterSpacing) 2.sp else 0.sp
            )
            if (hasBreakdown) {
                Text(text = "tap for details", fontSize = 9.sp, color = aesthetic.secondaryTextColor)
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        DifficultyBadge(difficulty = attempt.difficulty)
    }
}

@Composable
private fun DifficultyBadge(difficulty: DifficultyLevel) {
    val (icon, label, bgColor) = when (difficulty) {
        DifficultyLevel.EASY -> Triple("🌱", "Easy", Color(0xFF4ADE80))
        DifficultyLevel.NORMAL -> Triple("⚡", "Medium", Color(0xFFFBBF24))
        DifficultyLevel.HARD -> Triple("💀", "Hard", Color(0xFFF87171))
    }
    val textColor = if (bgColor.luminance() > 0.5f) Color.Black else Color.White

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.background(bgColor, RoundedCornerShape(10.dp)).padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(text = icon, fontSize = 18.sp)
        Text(text = label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = textColor, letterSpacing = 0.5.sp)
    }
}

@Composable
private fun TagsRow(attempt: PlayerAttempt, colors: ColorScheme, onVocalModeClick: () -> Unit) {
    val vocalMode = attempt.vocalAnalysis?.mode ?: when (attempt.calculationBreakdown?.scoringEngineType) {
        ScoringEngineType.SINGING_ENGINE -> VocalMode.SINGING
        ScoringEngineType.SPEECH_ENGINE -> VocalMode.SPEECH
        else -> null
    }
    val confidence = attempt.vocalAnalysis?.confidence

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        val (challengeIcon, challengeLabel) = when (attempt.challengeType) {
            ChallengeType.FORWARD -> "▶️" to "Forward"
            ChallengeType.REVERSE -> "🔄" to "Reverse"
        }

        TagChip(
            icon = challengeIcon,
            label = challengeLabel,
            backgroundColor = colors.surfaceVariant.copy(alpha = 0.5f),
            contentColor = colors.onSurfaceVariant
        )

        Spacer(modifier = Modifier.width(8.dp))

        val (modeIcon, modeLabel, modeColor) = when (vocalMode) {
            VocalMode.SINGING -> Triple("🎵", "Singing", Color(0xFFEC4899))
            VocalMode.SPEECH -> Triple("🗣️", "Speech", Color(0xFF9333EA))
            else -> Triple("❓", "Unknown", Color.Gray)
        }

        TagChip(
            icon = modeIcon,
            label = modeLabel,
            confidence = confidence,
            backgroundColor = modeColor.copy(alpha = 0.3f),
            contentColor = colors.onSurface,
            borderColor = modeColor.copy(alpha = 0.5f),
            onClick = onVocalModeClick
        )
    }
}

@Composable
private fun TagChip(
    icon: String,
    label: String,
    confidence: Float? = null,
    backgroundColor: Color,
    contentColor: Color,
    borderColor: Color? = null,
    onClick: (() -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .then(if (borderColor != null) Modifier.border(1.dp, borderColor, RoundedCornerShape(16.dp)) else Modifier)
            .background(backgroundColor, RoundedCornerShape(16.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text = icon, fontSize = 14.sp)
        Spacer(modifier = Modifier.width(5.dp))
        Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = contentColor)
        if (confidence != null) {
            Text(text = " ${(confidence * 100).toInt()}%", fontSize = 10.sp, color = contentColor.copy(alpha = 0.7f))
        }
        if (onClick != null) {
            Spacer(modifier = Modifier.width(4.dp))
            Icon(Icons.Default.Info, contentDescription = "Info", modifier = Modifier.size(12.dp), tint = contentColor.copy(alpha = 0.5f))
        }
    }
}

@Composable
private fun RichMetricsGrid(
    attempt: PlayerAttempt,
    aesthetic: AestheticThemeData,
    colors: ColorScheme,
    onMetricClick: (TooltipType) -> Unit
) {
    val vocalMode = attempt.vocalAnalysis?.mode ?: VocalMode.SPEECH
    val breakdown = attempt.calculationBreakdown

    val consistencyRaw = breakdown?.let {
        if (it.maxConsistencyBonus > 0f) (it.consistencyBonus / it.maxConsistencyBonus).coerceIn(0f, 1f) else 0f
    } ?: 0f
    val confidenceRaw = breakdown?.let {
        if (it.maxConfidenceBonus > 0f) (it.confidenceBonus / it.maxConfidenceBonus).coerceIn(0f, 1f) else 0f
    } ?: 0f

    val metrics = if (vocalMode == VocalMode.SINGING) {
        listOf(
            MetricData(TooltipType.PITCH_MATCH, "Pitch Match", "🎯", attempt.pitchSimilarity, MetricCategory.WEIGHTED, true),
            MetricData(TooltipType.VOICE_MATCH, "Voice Match", "🗣️", attempt.mfccSimilarity, MetricCategory.WEIGHTED, false),
            MetricData(TooltipType.LEAPS, "Leaps", "🦘", breakdown?.musicalBonuses?.intervalAccuracy ?: 0f, MetricCategory.BONUS, false),
            MetricData(TooltipType.RICHNESS, "Richness", "✨", breakdown?.musicalBonuses?.harmonicRichness ?: 0f, MetricCategory.BONUS, false),
            MetricData(TooltipType.CONTROL, "Control", "🎛️", consistencyRaw, MetricCategory.MULTIPLIER, false),
            MetricData(TooltipType.PRESENCE, "Presence", "🌟", confidenceRaw, MetricCategory.MULTIPLIER, false)
        )
    } else {
        listOf(
            MetricData(TooltipType.INTONATION, "Intonation", "🎯", attempt.pitchSimilarity, MetricCategory.WEIGHTED, true),
            MetricData(TooltipType.PRONUNCIATION, "Diction", "🗣️", attempt.mfccSimilarity, MetricCategory.WEIGHTED, false),
            MetricData(TooltipType.FLOW, "Flow", "🌊", consistencyRaw, MetricCategory.MULTIPLIER, false),
            MetricData(TooltipType.CLARITY, "Clarity", "💎", confidenceRaw, MetricCategory.MULTIPLIER, false)
        )
    }

    val themeAccent = aesthetic.cardBorder
    val themeSurface = colors.surface

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MetricCell(metric = metrics[0], breakdown = breakdown, aesthetic = aesthetic, themeAccent = themeAccent, themeSurface = themeSurface, onClick = { onMetricClick(metrics[0].type) }, modifier = Modifier.weight(1f))
            MetricCell(metric = metrics[1], breakdown = breakdown, aesthetic = aesthetic, themeAccent = themeAccent, themeSurface = themeSurface, onClick = { onMetricClick(metrics[1].type) }, modifier = Modifier.weight(1f))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MetricCell(metric = metrics[2], breakdown = breakdown, aesthetic = aesthetic, themeAccent = themeAccent, themeSurface = themeSurface, onClick = { onMetricClick(metrics[2].type) }, modifier = Modifier.weight(1f))
            MetricCell(metric = metrics[3], breakdown = breakdown, aesthetic = aesthetic, themeAccent = themeAccent, themeSurface = themeSurface, onClick = { onMetricClick(metrics[3].type) }, modifier = Modifier.weight(1f))
        }
        if (metrics.size > 4) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricCell(metric = metrics[4], breakdown = breakdown, aesthetic = aesthetic, themeAccent = themeAccent, themeSurface = themeSurface, onClick = { onMetricClick(metrics[4].type) }, modifier = Modifier.weight(1f))
                MetricCell(metric = metrics[5], breakdown = breakdown, aesthetic = aesthetic, themeAccent = themeAccent, themeSurface = themeSurface, onClick = { onMetricClick(metrics[5].type) }, modifier = Modifier.weight(1f))
            }
        }
    }
}

/**
 * Derives weight description from ScoreCalculationBreakdown - SINGLE SOURCE OF TRUTH
 */
private fun getWeightDescription(metric: MetricData, breakdown: ScoreCalculationBreakdown?): String {
    return when (metric.type) {
        TooltipType.PITCH_MATCH, TooltipType.INTONATION ->
            breakdown?.pitchWeight?.let { "${(it * 100).toInt()}% weighting" } ?: "primary metric"
        TooltipType.VOICE_MATCH, TooltipType.PRONUNCIATION ->
            breakdown?.mfccWeight?.let { "${(it * 100).toInt()}% weighting" } ?: "secondary metric"
        TooltipType.LEAPS ->
            breakdown?.musicalBonuses?.intervalWeight?.let { "up to +${(it * 100).toInt()}% bonus" } ?: "bonus"
        TooltipType.RICHNESS ->
            breakdown?.musicalBonuses?.harmonicWeight?.let { "up to +${(it * 100).toInt()}% bonus" } ?: "bonus"
        TooltipType.CONTROL, TooltipType.FLOW ->
            breakdown?.maxConsistencyBonus?.let { "up to +${(it * 100).toInt()}% bonus" } ?: "bonus"
        TooltipType.PRESENCE, TooltipType.CLARITY ->
            breakdown?.maxConfidenceBonus?.let { "up to +${(it * 100).toInt()}% bonus" } ?: "bonus"
        else -> ""
    }
}

@Composable
private fun MetricCell(
    metric: MetricData,
    breakdown: ScoreCalculationBreakdown?,
    aesthetic: AestheticThemeData,
    themeAccent: Color,
    themeSurface: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val style = deriveTierStyle(metric.category, metric.isHighWeight, themeAccent, themeSurface)
    val weightDescription = getWeightDescription(metric, breakdown)

    val glowModifier = if (style.hasGlow) {
        Modifier.shadow(elevation = 8.dp, shape = RoundedCornerShape(12.dp), ambientColor = style.primColor.copy(alpha = 0.4f), spotColor = style.primColor.copy(alpha = 0.4f))
    } else Modifier

    Column(
        modifier = modifier
            .then(glowModifier)
            .border(style.bordWidth, style.bordColor, RoundedCornerShape(12.dp))
            .background(style.bgColor, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        // Row 1: Icon + Label
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = metric.icon, fontSize = 14.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = metric.label,
                fontSize = 12.sp,
                fontWeight = if (style.hasGlow) FontWeight.Bold else FontWeight.Medium,
                color = aesthetic.primaryTextColor,
                letterSpacing = 0.3.sp
            )
        }

        // Row 2: Weight description (from breakdown - single source of truth)
        Text(
            text = weightDescription,
            fontSize = 10.sp,
            color = aesthetic.secondaryTextColor
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (style.hasGlow) 6.dp else 4.dp)
                .background(style.primColor.copy(alpha = 0.15f), RoundedCornerShape(3.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(metric.value.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(
                        brush = Brush.horizontalGradient(listOf(style.primColor, style.primColor.copy(alpha = 0.7f))),
                        shape = RoundedCornerShape(3.dp)
                    )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Percentage value
        Text(
            text = "${(metric.value * 100).toInt()}%",
            fontSize = style.txtSize,
            fontWeight = if (style.hasGlow) FontWeight.ExtraBold else FontWeight.Bold,
            color = aesthetic.primaryTextColor
        )
    }
}

@Composable
private fun TipsSection(attempt: PlayerAttempt, aesthetic: AestheticThemeData) {
    val title = aesthetic.scoreFeedback.getTitle(attempt.score)
    val emoji = aesthetic.scoreFeedback.getEmoji(attempt.score)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(aesthetic.cardBorder.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = emoji, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = aesthetic.primaryTextColor
            )
        }
    }
}

// ============================================================
// TOOLTIP TYPES AND DATA
// ============================================================

enum class TooltipType {
    PITCH_MATCH, VOICE_MATCH, RANGE, LEAPS, RICHNESS, CONTROL, PRESENCE,
    INTONATION, PRONUNCIATION, FLOW, CLARITY,
    SINGING_MODE, SPEECH_MODE, SCORE_BREAKDOWN
}

private data class MetricData(
    val type: TooltipType,
    val label: String,
    val icon: String,
    val value: Float,
    val category: MetricCategory,
    val isHighWeight: Boolean
)

@Composable
fun MetricTooltipDialog(tooltipType: TooltipType, breakdown: ScoreCalculationBreakdown?, onDismiss: () -> Unit) {
    val tooltipData = getTooltipData(tooltipType, breakdown)

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)) {
        Card(
            modifier = Modifier.fillMaxWidth(0.85f).padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = tooltipData.icon, fontSize = 40.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = tooltipData.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = tooltipData.description, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center, lineHeight = 20.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = tooltipData.technicalDetail, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), textAlign = TextAlign.Center, fontStyle = FontStyle.Italic)
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.background(MaterialTheme.colorScheme.primary, RoundedCornerShape(20.dp)).padding(horizontal = 16.dp)
                ) {
                    Text(text = "Got it!", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

private data class TooltipContent(val icon: String, val title: String, val description: String, val technicalDetail: String)

private fun getTooltipData(type: TooltipType, breakdown: ScoreCalculationBreakdown?): TooltipContent {
    val pitchPct = breakdown?.let { ((it.pitchWeight) * 100).toInt() }
    val mfccPct = breakdown?.let { ((it.mfccWeight) * 100).toInt() }

    return when (type) {
        TooltipType.PITCH_MATCH -> TooltipContent("🎯", "Pitch Match", "How well you hit the notes.", "Technical: pitchSimilarity (DTW-aligned pitch comparison)")
        TooltipType.VOICE_MATCH -> TooltipContent("🗣️", "Voice Match", "How well your voice matched the original.", "Technical: mfccSimilarity (MFCC cosine distance)")
        TooltipType.RANGE -> TooltipContent("🎼", "Range", "The vocal range complexity of the phrase.", "Technical: complexity (pitch range analysis) - not displayed")
        TooltipType.LEAPS -> TooltipContent("🦘", "Leaps", "Accuracy of melodic jumps between notes.", "Technical: intervals (interval matching accuracy)")
        TooltipType.RICHNESS -> TooltipContent("✨", "Richness", "Tonal quality and resonance of your voice.", "Technical: harmonics (harmonic content analysis)")
        TooltipType.CONTROL -> TooltipContent("🎛️", "Control", "How balanced your pitch and voice were.", "Technical: consistency (1 - |pitch - mfcc|)")
        TooltipType.PRESENCE -> TooltipContent("🌟", "Presence", "Vocal projection and strength.", "Technical: confidence (RMS-based vocal strength)")
        TooltipType.INTONATION -> TooltipContent("🎯", "Intonation", "How well you matched the rise and fall of speech.", "Technical: pitchSimilarity (DTW-aligned pitch comparison)")
        TooltipType.PRONUNCIATION -> TooltipContent("🗣️", "Pronunciation", "How accurately you pronounced the words.", "Technical: mfccSimilarity (MFCC cosine distance)")
        TooltipType.FLOW -> TooltipContent("🌊", "Flow", "Smoothness and natural rhythm.", "Technical: consistency (pitch/voice balance)")
        TooltipType.CLARITY -> TooltipContent("💎", "Clarity", "How clear and projected your voice was.", "Technical: confidence (RMS-based vocal strength)")
        TooltipType.SINGING_MODE -> {
            val description = if (pitchPct != null && mfccPct != null)
                "Detected melodic content with sustained pitches. Scoring emphasizes pitch accuracy ($pitchPct%) over voice timbre ($mfccPct%)."
            else
                "Detected melodic content with sustained pitches. Scoring emphasizes pitch accuracy over voice timbre."
            TooltipContent("🎵", "Singing Mode", description, "Technical: VocalModeDetector classified this as singing based on pitch stability and melodic patterns.")
        }
        TooltipType.SPEECH_MODE -> {
            val description = if (pitchPct != null && mfccPct != null)
                "Detected spoken content with natural intonation. Scoring balances pitch ($pitchPct%) and pronunciation ($mfccPct%)."
            else
                "Detected spoken content with natural intonation. Scoring balances pitch and pronunciation."
            TooltipContent("🗣️", "Speech Mode", description, "Technical: VocalModeDetector classified this as speech based on pitch variation and rhythmic patterns.")
        }
        TooltipType.SCORE_BREAKDOWN -> TooltipContent("🧮", "Score Breakdown", "Detailed calculation steps showing how your score was computed.", "Technical: Full scoring pipeline breakdown")
    }
}

// ============================================================
// BREAKDOWN TOOLTIP DIALOG
// ============================================================

@Composable
fun BreakdownTooltipDialog(breakdown: ScoreCalculationBreakdown, onDismiss: () -> Unit) {
    val steps = breakdown.toDisplaySteps()
    val summary = breakdown.toQuickSummary()
    val modifiers = breakdown.getKeyModifiers()

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)) {
        Card(
            modifier = Modifier.fillMaxWidth(0.95f).padding(8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🧮 Score Breakdown", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(16.dp))
                    }
                }
                Text(
                    text = summary,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                if (modifiers.isNotEmpty()) {
                    Text(text = "KEY FACTORS", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    modifiers.forEach { mod -> Text(text = mod, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)) }
                    Spacer(modifier = Modifier.height(12.dp))
                }
                Text(text = "CALCULATION STEPS", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(8.dp))
                steps.forEach { step ->
                    CalculationStepRow(step = step, colors = MaterialTheme.colorScheme)
                    Spacer(modifier = Modifier.height(6.dp))
                }
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.CenterHorizontally).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(20.dp)).padding(horizontal = 16.dp)
                ) {
                    Text(text = "Got it!", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun CalculationStepRow(step: CalculationStep, colors: ColorScheme) {
    val backgroundColor = when {
        step.isFinal -> colors.primaryContainer.copy(alpha = 0.3f)
        step.isBonus -> Color(0xFF4ADE80).copy(alpha = 0.15f)
        step.isPenalty -> Color(0xFFF87171).copy(alpha = 0.15f)
        else -> colors.surfaceVariant.copy(alpha = 0.2f)
    }
    val borderColor = when {
        step.isFinal -> colors.primary.copy(alpha = 0.5f)
        step.isBonus -> Color(0xFF4ADE80).copy(alpha = 0.3f)
        step.isPenalty -> Color(0xFFF87171).copy(alpha = 0.3f)
        else -> Color.Transparent
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (borderColor != Color.Transparent) Modifier.border(1.dp, borderColor, RoundedCornerShape(8.dp)) else Modifier)
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "${step.stepNumber}. ${step.title}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colors.onSurface)
            Text(
                text = step.result,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = when { step.isFinal -> colors.primary; step.isBonus -> Color(0xFF22C55E); step.isPenalty -> Color(0xFFEF4444); else -> colors.onSurface }
            )
        }
        Text(text = step.formula, fontSize = 9.sp, color = colors.onSurface.copy(alpha = 0.5f), fontStyle = FontStyle.Italic)
        Text(text = step.calculation, fontSize = 10.sp, color = colors.onSurface.copy(alpha = 0.7f))
    }
}