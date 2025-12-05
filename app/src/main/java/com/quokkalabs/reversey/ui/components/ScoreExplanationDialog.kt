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
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.quokkalabs.reversey.data.models.ChallengeType
import com.quokkalabs.reversey.data.models.PlayerAttempt
import com.quokkalabs.reversey.scoring.DifficultyLevel
import com.quokkalabs.reversey.scoring.VocalMode
import com.quokkalabs.reversey.ui.theme.AestheticTheme
import com.quokkalabs.reversey.ui.theme.AestheticThemeData
import com.quokkalabs.reversey.ui.theme.MaterialColors

/**
 * 🎨 UNIFIED Score Explanation Dialog v3
 *
 * Features:
 * - Compact header: Emoji + Score + Difficulty badge (single row)
 * - Tags row: Challenge type + Vocal mode (tappable with tooltip)
 * - Rich metrics grid: 6 metrics with tooltips
 * - Theme-aware styling
 * - Metrics adapt based on Speech/Singing mode
 * - NEW: Clickable score shows calculation breakdown
 */
@Composable
fun ScoreExplanationDialog(
    attempt: PlayerAttempt,
    onDismiss: () -> Unit
) {
    val aesthetic = AestheticTheme()
    val colors = MaterialColors()

    // Tooltip state
    var tooltipType by remember { mutableStateOf<TooltipType?>(null) }

    // Theme-aware animations
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )

    val gearRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gear_rotation"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
            ) {
                // Theme-aware background glow
                if (aesthetic.useGlassmorphism && aesthetic.glowIntensity > 0) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .blur((aesthetic.glowIntensity * 25).dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        colors.primary.copy(alpha = glowAlpha * aesthetic.glowIntensity),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                }

                // Main score card
                UnifiedScoreCardV3(
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

    // Tooltip dialog
    tooltipType?.let { type ->
        MetricTooltipDialog(
            tooltipType = type,
            vocalMode = attempt.vocalAnalysis?.mode,
            onDismiss = { tooltipType = null }
        )
    }
}

/**
 * Main score card with v3 layout
 */
@Composable
private fun UnifiedScoreCardV3(
    attempt: PlayerAttempt,
    aesthetic: AestheticThemeData,
    colors: ColorScheme,
    rotation: Float,
    glowAlpha: Float,
    gearRotation: Float,
    onDismiss: () -> Unit,
    onTooltipRequest: (TooltipType) -> Unit
) {
    // Card styling adapts to aesthetic theme
    val cardModifier = when (aesthetic.id) {
        "scrapbook" -> Modifier
            .rotate(rotation * 2f)
            .background(
                color = colors.surface,
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 2.dp,
                color = aesthetic.cardBorder,
                shape = RoundedCornerShape(16.dp)
            )

        "steampunk" -> Modifier
            .background(
                brush = aesthetic.primaryGradient,
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 3.dp,
                color = aesthetic.cardBorder,
                shape = RoundedCornerShape(16.dp)
            )

        "cyberpunk" -> Modifier
            .background(
                color = colors.surface.copy(alpha = aesthetic.cardAlpha),
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 1.dp,
                color = colors.primary.copy(alpha = glowAlpha),
                shape = RoundedCornerShape(8.dp)
            )

        else -> Modifier
            .background(
                color = colors.surface.copy(alpha = aesthetic.cardAlpha),
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 1.dp,
                color = colors.primary.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            )
    }

    // Interaction source for the "click sink"
    val interactionSource = remember { MutableInteractionSource() }

    Card(
        modifier = cardModifier
            .fillMaxWidth()
            .padding(8.dp)
            // CRITICAL FIX: "Click Sink"
            // We use a clickable that IS enabled but has NO indication (ripple).
            // This actively consumes the click event so it does not bubble up to the background.
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {}
            ),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box {
            // Content Column
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Theme-specific decoration
                when (aesthetic.id) {
                    "steampunk" -> {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            modifier = Modifier
                                .size(28.dp)
                                .rotate(gearRotation),
                            tint = aesthetic.cardBorder
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // === ROW 1: Emoji + Score + Difficulty ===
                ScoreHeaderRow(
                    attempt = attempt,
                    aesthetic = aesthetic,
                    colors = colors,
                    onScoreClick = { onTooltipRequest(TooltipType.SCORE_BREAKDOWN) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // === ROW 2: Challenge Type + Vocal Mode ===
                TagsRow(
                    attempt = attempt,
                    colors = colors,
                    onVocalModeClick = {
                        val mode = attempt.vocalAnalysis?.mode
                        onTooltipRequest(
                            if (mode == VocalMode.SINGING) TooltipType.SINGING_MODE
                            else TooltipType.SPEECH_MODE
                        )
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Feedback text
                if (attempt.feedback.isNotEmpty()) {
                    Text(
                        text = attempt.feedback.firstOrNull() ?: "",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.onSurface.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // === METRICS GRID ===
                RichMetricsGrid(
                    attempt = attempt,
                    colors = colors,
                    onMetricClick = onTooltipRequest
                )

                Spacer(modifier = Modifier.height(12.dp))

                // === TIPS SECTION ===
                TipsSection(
                    attempt = attempt,
                    aesthetic = aesthetic,
                    colors = colors
                )
            }

            // Close button (floating overlay)
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(32.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close",
                    tint = colors.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * ROW 1: Emoji + Score + Difficulty badge (all inline)
 */
@Composable
private fun ScoreHeaderRow(
    attempt: PlayerAttempt,
    aesthetic: AestheticThemeData,
    colors: ColorScheme,
    onScoreClick: () -> Unit
) {
    val emoji = aesthetic.scoreEmojis[
        when {
            attempt.score >= 90 -> 90
            attempt.score >= 80 -> 80
            attempt.score >= 70 -> 70
            attempt.score >= 60 -> 60
            else -> 0
        }
    ] ?: "🎤"

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Emoji
        Text(
            text = emoji,
            fontSize = 40.sp,
            modifier = Modifier.padding(end = 12.dp)
        )

        // Score (Clickable)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable { onScoreClick() }
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = "${attempt.score}%",
                fontSize = 48.sp,
                fontWeight = FontWeight.ExtraBold,
                color = colors.primary,
                letterSpacing = if (aesthetic.useWideLetterSpacing) 2.sp else 0.sp
            )
            // Small hint icon to show it's clickable
            Text(
                text = " ⓘ",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = colors.primary.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 16.dp) // Align to top-ish
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Difficulty badge
        DifficultyBadge(
            difficulty = attempt.difficulty,
            colors = colors
        )
    }
}

/**
 * Difficulty badge with icon and label
 */
@Composable
private fun DifficultyBadge(
    difficulty: DifficultyLevel,
    colors: ColorScheme
) {
    val (icon, label, bgColor) = when (difficulty) {
        DifficultyLevel.EASY -> Triple("🌱", "Easy", Color(0xFF4ADE80))
        DifficultyLevel.NORMAL -> Triple("⚡", "Medium", Color(0xFFFBBF24))
        DifficultyLevel.HARD -> Triple("💀", "Hard", Color(0xFFF87171))
    }

    val textColor = if (difficulty == DifficultyLevel.HARD) Color.White else Color.Black

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = icon,
            fontSize = 18.sp
        )
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            letterSpacing = 0.5.sp
        )
    }
}

/**
 * ROW 2: Challenge type chip + Vocal mode chip
 */
@Composable
private fun TagsRow(
    attempt: PlayerAttempt,
    colors: ColorScheme,
    onVocalModeClick: () -> Unit
) {
    val vocalMode = attempt.vocalAnalysis?.mode
    val confidence = attempt.vocalAnalysis?.confidence

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Challenge type chip
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

        // Vocal mode chip (tappable)
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

/**
 * Reusable tag chip component
 */
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
            .then(
                if (borderColor != null) Modifier.border(1.dp, borderColor, RoundedCornerShape(16.dp))
                else Modifier
            )
            .background(backgroundColor, RoundedCornerShape(16.dp))
            .then(
                if (onClick != null) Modifier.clickable { onClick() }
                else Modifier
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text = icon, fontSize = 14.sp)
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = contentColor
        )
        if (confidence != null) {
            Text(
                text = " ${(confidence * 100).toInt()}%",
                fontSize = 10.sp,
                color = contentColor.copy(alpha = 0.7f)
            )
        }
        if (onClick != null) {
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                Icons.Default.Info,
                contentDescription = "Info",
                modifier = Modifier.size(12.dp),
                tint = contentColor.copy(alpha = 0.5f)
            )
        }
    }
}

/**
 * Rich metrics grid - 6 metrics in 2x3 layout
 * Primary metrics highlighted based on vocal mode
 */
@Composable
private fun RichMetricsGrid(
    attempt: PlayerAttempt,
    colors: ColorScheme,
    onMetricClick: (TooltipType) -> Unit
) {
    val vocalMode = attempt.vocalAnalysis?.mode ?: VocalMode.SPEECH
    val vocalFeatures = attempt.vocalAnalysis?.features

    // Determine which metrics are primary based on mode
    val singingPrimary = setOf(TooltipType.PITCH_MATCH, TooltipType.MELODY_FLOW, TooltipType.STEADINESS)
    val speechPrimary = setOf(TooltipType.VOICE_MATCH, TooltipType.CLARITY, TooltipType.PITCH_MATCH)
    val primaryMetrics = if (vocalMode == VocalMode.SINGING) singingPrimary else speechPrimary

    // Build metrics list with values
    val metrics = listOf(
        MetricData(
            type = TooltipType.PITCH_MATCH,
            label = "Pitch Match",
            icon = "🎯",
            value = attempt.pitchSimilarity,
            isPrimary = TooltipType.PITCH_MATCH in primaryMetrics
        ),
        MetricData(
            type = TooltipType.VOICE_MATCH,
            label = "Voice Match",
            icon = "🗣️",
            value = attempt.mfccSimilarity,
            isPrimary = TooltipType.VOICE_MATCH in primaryMetrics
        ),
        MetricData(
            type = TooltipType.STEADINESS,
            label = "Steadiness",
            icon = "📊",
            value = vocalFeatures?.pitchStability ?: 0f,
            isPrimary = TooltipType.STEADINESS in primaryMetrics
        ),
        MetricData(
            type = TooltipType.MELODY_FLOW,
            label = "Melody Flow",
            icon = "🎼",
            value = vocalFeatures?.pitchContour ?: 0f,
            isPrimary = TooltipType.MELODY_FLOW in primaryMetrics
        ),
        MetricData(
            type = TooltipType.CLARITY,
            label = "Clarity",
            icon = "🎤",
            value = vocalFeatures?.voicedRatio ?: 0f,
            isPrimary = TooltipType.CLARITY in primaryMetrics
        ),
        MetricData(
            type = TooltipType.AUDIO_QUALITY,
            label = "Audio Quality",
            icon = "📡",
            value = attempt.audioQualityMetrics?.snr?.coerceIn(0f, 1f) ?: 0.5f,
            isPrimary = false
        )
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Row 1
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            MetricCell(
                metric = metrics[0],
                colors = colors,
                onClick = { onMetricClick(metrics[0].type) },
                modifier = Modifier.weight(1f)
            )
            MetricCell(
                metric = metrics[1],
                colors = colors,
                onClick = { onMetricClick(metrics[1].type) },
                modifier = Modifier.weight(1f)
            )
        }
        // Row 2
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            MetricCell(
                metric = metrics[2],
                colors = colors,
                onClick = { onMetricClick(metrics[2].type) },
                modifier = Modifier.weight(1f)
            )
            MetricCell(
                metric = metrics[3],
                colors = colors,
                onClick = { onMetricClick(metrics[3].type) },
                modifier = Modifier.weight(1f)
            )
        }
        // Row 3
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            MetricCell(
                metric = metrics[4],
                colors = colors,
                onClick = { onMetricClick(metrics[4].type) },
                modifier = Modifier.weight(1f)
            )
            MetricCell(
                metric = metrics[5],
                colors = colors,
                onClick = { onMetricClick(metrics[5].type) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Single metric cell
 */
@Composable
private fun MetricCell(
    metric: MetricData,
    colors: ColorScheme,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (metric.isPrimary)
        colors.primaryContainer.copy(alpha = 0.3f)
    else
        colors.surfaceVariant.copy(alpha = 0.2f)

    val borderModifier = if (metric.isPrimary)
        Modifier.border(1.dp, colors.primary.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
    else
        Modifier

    Column(
        modifier = modifier
            .then(borderModifier)
            .background(backgroundColor, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(10.dp)
    ) {
        // Header: Icon + Label + Info icon
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = metric.icon, fontSize = 12.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = metric.label,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.onSurface.copy(alpha = 0.8f),
                    letterSpacing = 0.3.sp
                )
            }
            Text(
                text = "ⓘ",
                fontSize = 10.sp,
                color = colors.onSurface.copy(alpha = 0.4f)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Progress bar
        LinearProgressIndicator(
            progress = { metric.value.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = colors.primary,
            trackColor = colors.onSurface.copy(alpha = 0.1f)
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Value
        Text(
            text = "${(metric.value * 100).toInt()}%",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = if (metric.isPrimary) colors.primary else colors.onSurface
        )
    }
}

/**
 * Tips section
 */
@Composable
private fun TipsSection(
    attempt: PlayerAttempt,
    aesthetic: AestheticThemeData,
    colors: ColorScheme
) {
    // Ensuring the tooltip data is remembered to prevent unnecessary recalculations
    val tips = remember(attempt.score, attempt.challengeType, aesthetic.id) {
        generateThemeAwareTips(
            score = attempt.score,
            challengeType = attempt.challengeType,
            themeId = aesthetic.id
        ).take(2)
    }

    if (tips.isEmpty()) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                colors.surfaceVariant.copy(alpha = 0.15f),
                RoundedCornerShape(10.dp)
            )
            .padding(10.dp)
    ) {
        Text(
            text = getTipsHeader(aesthetic.id),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = colors.onSurface.copy(alpha = 0.5f),
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        tips.forEach { tip ->
            Text(
                text = tip,
                fontSize = 11.sp,
                color = colors.onSurface.copy(alpha = 0.8f),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        colors.surfaceVariant.copy(alpha = 0.1f),
                        RoundedCornerShape(6.dp)
                    )
                    .padding(8.dp),
                lineHeight = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

// ============================================================
// TOOLTIP DIALOG
// ============================================================

/**
 * Tooltip types for metrics and vocal modes
 */
enum class TooltipType {
    PITCH_MATCH,
    VOICE_MATCH,
    STEADINESS,
    MELODY_FLOW,
    CLARITY,
    AUDIO_QUALITY,
    SINGING_MODE,
    SPEECH_MODE,
    SCORE_BREAKDOWN
}

/**
 * Data class for metric display
 */
private data class MetricData(
    val type: TooltipType,
    val label: String,
    val icon: String,
    val value: Float,
    val isPrimary: Boolean
)

/**
 * Tooltip dialog with explanation
 */
@Composable
fun MetricTooltipDialog(
    tooltipType: TooltipType,
    vocalMode: VocalMode?,
    onDismiss: () -> Unit
) {
    // Using remember to ensure tooltipData is only calculated when tooltipType or vocalMode changes
    val tooltipData = remember(tooltipType, vocalMode) {
        getTooltipData(tooltipType, vocalMode)
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
                .fillMaxWidth(0.85f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon
                Text(
                    text = tooltipData.icon,
                    fontSize = 40.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Title
                Text(
                    text = tooltipData.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Description
                Text(
                    text = tooltipData.description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Technical detail
                Text(
                    text = tooltipData.technicalDetail,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    fontStyle = FontStyle.Italic
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Close button
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.primary,
                            RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "Got it!",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

/**
 * Tooltip content data
 */
private data class TooltipContent(
    val icon: String,
    val title: String,
    val description: String,
    val technicalDetail: String
)

/**
 * Get tooltip content for each type
 */
private fun getTooltipData(type: TooltipType, vocalMode: VocalMode? = null): TooltipContent {
    return when (type) {
        TooltipType.SCORE_BREAKDOWN -> {
            val isSinging = vocalMode == VocalMode.SINGING
            TooltipContent(
                icon = "🧮",
                title = "Score Calculation",
                description = if (isSinging)
                    "Singing detected! Your score is primarily based on how well you matched the pitch (90%) and the tone of the voice (10%)."
                else
                    "Speech detected! Your score is weighted between matching the pitch contour (85%) and the voice texture (15%).",
                technicalDetail = if (isSinging)
                    "Weights: Pitch 0.9 + Voice 0.1 | Penalty applied for timing errors."
                else
                    "Weights: Pitch 0.85 + Voice 0.15 | Penalty applied for silence/pauses."
            )
        }
        TooltipType.PITCH_MATCH -> TooltipContent(
            icon = "🎯",
            title = "Pitch Match",
            description = "How accurately your notes match the original recording. High scores mean you're hitting the right notes!",
            technicalDetail = "Technical: pitchSimilarity via DTW alignment"
        )
        TooltipType.VOICE_MATCH -> TooltipContent(
            icon = "🗣️",
            title = "Voice Match",
            description = "How similar your voice tone and character sound to the original. This measures the 'color' and texture of your voice.",
            technicalDetail = "Technical: mfccSimilarity (Mel-frequency cepstral coefficients)"
        )
        TooltipType.STEADINESS -> TooltipContent(
            icon = "📊",
            title = "Steadiness",
            description = "How consistently you hold notes without wavering. Singing typically shows high stability (0.7-0.9), speech shows lower (0.2-0.4).",
            technicalDetail = "Technical: pitchStability from vocal analysis"
        )
        TooltipType.MELODY_FLOW -> TooltipContent(
            icon = "🎼",
            title = "Melody Flow",
            description = "How well your pitch rises and falls match the original melody shape. Singing shows high contour (0.6-0.8), speech is flatter.",
            technicalDetail = "Technical: pitchContour matching"
        )
        TooltipType.CLARITY -> TooltipContent(
            icon = "🎤",
            title = "Clarity",
            description = "Percentage of voiced (audible) frames vs silence/consonants. Singing typically has high voiced ratio, speech has more pauses.",
            technicalDetail = "Technical: voicedRatio (voiced frames / total frames)"
        )
        TooltipType.AUDIO_QUALITY -> TooltipContent(
            icon = "📡",
            title = "Audio Quality",
            description = "How clean your recording is. Background noise, mic issues, echo, or distortion will lower this score.",
            technicalDetail = "Technical: SNR (Signal-to-Noise Ratio)"
        )
        TooltipType.SINGING_MODE -> TooltipContent(
            icon = "🎵",
            title = "Singing Mode Detected",
            description = "Musical content detected! Scoring emphasizes pitch accuracy and melody. Primary metrics: Pitch Match, Melody Flow, Steadiness.",
            technicalDetail = "Pitch weight: 90% • Voice weight: 10% • Tolerance: ±20 semitones"
        )
        TooltipType.SPEECH_MODE -> TooltipContent(
            icon = "🗣️",
            title = "Speech Mode Detected",
            description = "Spoken content detected! Scoring is more forgiving on pitch and focuses on voice matching. Primary metrics: Voice Match, Clarity, Pitch.",
            technicalDetail = "Pitch weight: 85% • Voice weight: 15% • Tolerance: ±40 semitones"
        )
    }
}

// ============================================================
// TIPS GENERATION
// ============================================================

private fun getTipsHeader(themeId: String): String {
    return when (themeId) {
        "steampunk" -> "⚙️ CALIBRATION NOTES"
        "cyberpunk" -> "// SYSTEM_ANALYSIS"
        "cottage", "sakura_serenity" -> "🌸 SUGGESTIONS"
        else -> "💡 TIPS"
    }
}

private fun generateThemeAwareTips(
    score: Int,
    challengeType: ChallengeType,
    themeId: String
): List<String> {
    return when (themeId) {
        "steampunk" -> generateSteampunkTips(score, challengeType)
        "cyberpunk" -> generateCyberpunkTips(score, challengeType)
        else -> generateEncouragingTips(score, challengeType)
    }
}

private fun generateEncouragingTips(score: Int, challengeType: ChallengeType): List<String> {
    val challengeText = if (challengeType == ChallengeType.REVERSE) "reverse" else "forward"
    return when {
        score >= 90 -> listOf("Amazing $challengeText performance!", "You've mastered this!")
        score >= 80 -> listOf("Great job matching the melody!", "Almost perfect!")
        score >= 70 -> listOf("Good progress on $challengeText!", "Keep practicing!")
        score >= 60 -> listOf("Nice effort!", "Try matching the rhythm more closely")
        else -> listOf("Keep trying!", "Listen to the original again")
    }
}

private fun generateSteampunkTips(score: Int, challengeType: ChallengeType): List<String> {
    return when {
        score >= 90 -> listOf("Phonographic apparatus calibrated perfectly!", "Steam pressure optimal")
        score >= 80 -> listOf("Most admirable vocal machinery!", "Minor gear adjustments needed")
        score >= 70 -> listOf("Respectable craftsmanship!", "Oil the timing gears")
        score >= 60 -> listOf("Adequate endeavour!", "Check valve pressure")
        else -> listOf("Recalibration required!", "Consult the engineering manual")
    }
}

private fun generateCyberpunkTips(score: Int, challengeType: ChallengeType): List<String> {
    return when {
        score >= 90 -> listOf("NEURAL_LINK: PERFECTED", "Signal strength: MAXIMUM")
        score >= 80 -> listOf("ACCESS_GRANTED: Elite performance", "Minor latency detected")
        score >= 70 -> listOf("FIREWALL_BYPASSED", "Upgrade audio codec")
        score >= 60 -> listOf("CONNECTION_ESTABLISHED", "Debug rhythm subroutine")
        else -> listOf("SYSTEM_ERROR: Retry", "Boost signal clarity")
    }
}