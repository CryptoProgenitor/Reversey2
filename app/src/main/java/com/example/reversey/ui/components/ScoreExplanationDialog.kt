package com.example.reversey.ui.components

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.reversey.data.models.ChallengeType
import com.example.reversey.data.models.PlayerAttempt
import com.example.reversey.ui.theme.AestheticTheme
import com.example.reversey.ui.theme.MaterialColors

/**
 * 🎨 UNIFIED Score Explanation Dialog
 * Single composable that adapts to all aesthetic themes
 * Replaces: Y2KScoreCard, ScrapbookScoreCard, SteampunkScoreCard, CyberpunkScoreCard, GraphiteSketchScoreCard
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ScoreExplanationDialog(
    attempt: PlayerAttempt,
    onDismiss: () -> Unit
) {
    // ✅ Access theme data through unified system
    val aesthetic = AestheticTheme()
    val colors = MaterialColors()

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

    val scanLineOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scan_line"
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
                    .fillMaxWidth(0.9f)
                    .clickable(enabled = false) { }
            ) {
                // ✅ Theme-aware background glow (unified approach)
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

                // ✅ Single unified score card with theme-aware styling
                UnifiedScoreCard(
                    attempt = attempt,
                    aesthetic = aesthetic,
                    colors = colors,
                    rotation = rotation,
                    glowAlpha = glowAlpha,
                    gearRotation = gearRotation,
                    scanLineOffset = scanLineOffset,
                    onDismiss = onDismiss
                )
            }
        }
    }
}

/**
 * ✅ Single Score Card that adapts to all themes
 * Replaces 5+ separate theme-specific cards with unified approach
 */
@Composable
private fun UnifiedScoreCard(
    attempt: PlayerAttempt,
    aesthetic: com.example.reversey.ui.theme.AestheticThemeData,
    colors: ColorScheme,
    rotation: Float,
    glowAlpha: Float,
    gearRotation: Float,
    scanLineOffset: Float,
    onDismiss: () -> Unit
) {
    // ✅ Card styling adapts to aesthetic theme
    val cardModifier = when (aesthetic.id) {
        "scrapbook" -> Modifier
            .rotate(rotation * 2f)  // Scrapbook rotation effect
            .background(
                color = colors.surface,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 2.dp,
                color = aesthetic.cardBorder,
                shape = RoundedCornerShape(12.dp)
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

        else -> Modifier  // Y2K, Vaporwave, etc. - glassmorphism style
            .background(
                color = colors.surface.copy(alpha = aesthetic.cardAlpha),
                shape = RoundedCornerShape(20.dp)
            )
            .border(
                width = 1.dp,
                color = colors.primary.copy(alpha = 0.3f),
                shape = RoundedCornerShape(20.dp)
            )
    }

    Card(
        modifier = cardModifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ✅ Close button using Material 3 colors
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = colors.onSurface
                    )
                }
            }

            // ✅ Theme-aware decorative effects
            when (aesthetic.id) {
                "steampunk" -> {
                    // Rotating gear decoration
                    Icon(
                        imageVector = Icons.Default.Settings, // Placeholder for gear
                        contentDescription = null,
                        modifier = Modifier
                            .size(40.dp)
                            .rotate(gearRotation),
                        tint = aesthetic.cardBorder
                    )
                }
                "cyberpunk" -> {
                    // Scan line effect
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        colors.primary.copy(alpha = scanLineOffset),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                }
                "scrapbook" -> {
                    // "Tape" decoration
                    Box(
                        modifier = Modifier
                            .size(60.dp, 20.dp)
                            .background(
                                color = Color(0xFFFFEB3B).copy(alpha = 0.7f),
                                shape = RoundedCornerShape(4.dp)
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ✅ Score display using theme emoji
            val rawScore = attempt.score
            val emoji = aesthetic.scoreEmojis[
                when {
                    rawScore >= 90 -> 90
                    rawScore >= 80 -> 80
                    rawScore >= 70 -> 70
                    rawScore >= 60 -> 60
                    else -> 0
                }
            ] ?: "🎤"

            Text(
                text = emoji,
                fontSize = 48.sp,
                modifier = Modifier.padding(8.dp)
            )

            // ✅ Score text using Material 3 colors
            Text(
                text = "${attempt.score}%",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = colors.primary,
                style = if (aesthetic.useSerifFont) {
                    MaterialTheme.typography.headlineLarge
                } else {
                    MaterialTheme.typography.headlineLarge
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // DEBUG: Check what feedback we have
            android.util.Log.d("ScoreDialog", "Feedback items: ${attempt.feedback}")
            android.util.Log.d("ScoreDialog", "Feedback size: ${attempt.feedback.size}")

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                attempt.feedback.forEach { feedbackLine ->
                    Text(
                        text = feedbackLine,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.onSurface,
                        textAlign = TextAlign.Center,
                        letterSpacing = if (aesthetic.useWideLetterSpacing) 2.sp else 0.sp,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ✅ Metrics using Material 3 styling (from PlayerAttempt)
            MetricsSection(attempt, colors)

            Spacer(modifier = Modifier.height(16.dp))

            // ✅ Tips using theme-aware content
            TipsSection(attempt, aesthetic, colors)
        }
    }
}

@Composable
private fun MetricsSection(attempt: PlayerAttempt, colors: ColorScheme) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = colors.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Performance Breakdown",
                style = MaterialTheme.typography.titleMedium,
                color = colors.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            MetricRow("Pitch Similarity", attempt.pitchSimilarity, colors)
            MetricRow("Voice Matching", attempt.mfccSimilarity, colors)
        }
    }
}

@Composable
private fun MetricRow(label: String, value: Float, colors: ColorScheme) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            LinearProgressIndicator(
                progress = value.coerceIn(0f, 1f),
                modifier = Modifier
                    .width(80.dp)
                    .padding(end = 8.dp),
                color = colors.primary
            )
            Text(
                text = "${(value * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TipsSection(
    attempt: PlayerAttempt,
    aesthetic: com.example.reversey.ui.theme.AestheticThemeData,
    colors: ColorScheme
) {
    val tips = generateThemeAwareTips(
        score = attempt.score,
        challengeType = attempt.challengeType,
        themeId = aesthetic.id
    ).take(3)

    LazyColumn(
        modifier = Modifier.height(120.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(tips) { tip ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = colors.primaryContainer.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = tip,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onPrimaryContainer,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

/**
 * ✅ Theme-aware header generation (still available if you hook it into UI)
 */
private fun getThemeAwareHeader(
    score: Int,
    challengeType: ChallengeType,
    themeId: String
): String {
    val challengeText =
        if (challengeType == ChallengeType.REVERSE) "reverse singing" else "vocal mimicry"

    return when (themeId) {
        "steampunk" -> {
            val steamChallenge =
                if (challengeType == ChallengeType.REVERSE) "phonographic reversal" else "vocal apparatus mimicry"
            when {
                score >= 95 -> "EXTRAORDINARY MASTERY of $steamChallenge!"
                score >= 90 -> "Most splendid $steamChallenge performance!"
                score >= 80 -> "Admirable $steamChallenge craftsmanship!"
                score >= 70 -> "Decent $steamChallenge endeavour!"
                score >= 60 -> "Respectable $steamChallenge attempt!"
                score >= 50 -> "Continue perfecting your $steamChallenge!"
                else -> "$steamChallenge requires mechanical precision!"
            }
        }

        "cyberpunk" -> {
            val cyberChallenge =
                if (challengeType == ChallengeType.REVERSE) "audio_reverse_hack" else "voice_pattern_clone"
            when {
                score >= 95 -> "NEURAL_LINK_PERFECTED: $cyberChallenge"
                score >= 90 -> "SYSTEM_HACKED: Elite $cyberChallenge"
                score >= 80 -> "ACCESS_GRANTED: Good $cyberChallenge"
                score >= 70 -> "FIREWALL_BYPASSED: $cyberChallenge"
                score >= 60 -> "CONNECTION_ESTABLISHED: $cyberChallenge"
                score >= 50 -> "UPLOADING_SKILLS: $cyberChallenge"
                else -> "SYSTEM_ERROR: $cyberChallenge failed"
            }
        }

        "graphite_sketch" -> {
            val artChallenge =
                if (challengeType == ChallengeType.REVERSE) "reverse melody sketch" else "voice drawing"
            when {
                score >= 95 -> "Perfect $artChallenge - frame worthy!"
                score >= 90 -> "Beautiful $artChallenge artwork!"
                score >= 80 -> "Nice $artChallenge drawing!"
                score >= 70 -> "Good $artChallenge sketch!"
                score >= 60 -> "Decent $artChallenge attempt!"
                score >= 50 -> "Keep sketching that $artChallenge!"
                else -> "Every artist starts with rough $artChallenge!"
            }
        }

        else -> {
            // Default encouraging headers for Y2K, Scrapbook, etc.
            when {
                score >= 95 -> "LEGENDARY $challengeText mastery!"
                score >= 90 -> "Incredible $challengeText skills!"
                score >= 80 -> "Great job with $challengeText!"
                score >= 70 -> "Nice $challengeText progress!"
                score >= 60 -> "Good $challengeText effort!"
                score >= 50 -> "Keep practicing $challengeText!"
                else -> "$challengeText is tough - you're learning!"
            }
        }
    }
}

/**
 * ✅ Theme-aware tips generation using INT score (no ScoringResult dependency)
 */
private fun generateThemeAwareTips(
    score: Int,
    challengeType: ChallengeType,
    themeId: String
): List<String> {
    return when (themeId) {
        "steampunk" -> generateSteampunkTips(score, challengeType)
        "cyberpunk" -> generateCyberpunkTips(score, challengeType)
        "graphite_sketch" -> generateGraphiteTips(score, challengeType)
        else -> generateEncouragingTips(score, challengeType)
    }
}

private fun generateEncouragingTips(
    score: Int,
    challengeType: ChallengeType
): List<String> {
    // TODO: replace placeholders with your full logic if you had it before
    return listOf(
        "Practice makes perfect!",
        "Keep trying!",
        "You're improving!"
    )
}

private fun generateSteampunkTips(
    score: Int,
    challengeType: ChallengeType
): List<String> {
    // TODO: replace placeholders with your full logic if you had it before
    return listOf(
        "Calibrate your vocal apparatus.",
        "Check steam pressure in your delivery.",
        "Oil the gears of your timing."
    )
}

private fun generateCyberpunkTips(
    score: Int,
    challengeType: ChallengeType
): List<String> {
    // TODO: replace placeholders with your full logic if you had it before
    return listOf(
        "Upgrade your neural interface.",
        "Boost your signal clarity.",
        "Debug your rhythm subroutine."
    )
}

private fun generateGraphiteTips(
    score: Int,
    challengeType: ChallengeType
): List<String> {
    // TODO: replace placeholders with your full logic if you had it before
    return listOf(
        "Sharpen your vocal outline.",
        "Smooth out your tonal shading.",
        "Study the reference recording closely."
    )
}
