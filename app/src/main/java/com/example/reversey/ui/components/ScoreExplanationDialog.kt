package com.example.reversey.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.reversey.AppTheme
import com.example.reversey.ChallengeType
import com.example.reversey.scoring.ScoringResult

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ScoreExplanationDialog(
    score: ScoringResult,
    challengeType: ChallengeType,
    currentTheme: AppTheme,
    onDismiss: () -> Unit
) {
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
            // Main dialog content
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .clickable(enabled = false) { } // Prevent dismissing when clicking content
            ) {
                // Background glow effect for Y2K theme
                if (currentTheme.id == "y2k_cyber") {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .blur(20.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        currentTheme.accentColor.copy(alpha = glowAlpha),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                }

                // Main content card
                when (currentTheme.id) {
                    "scrapbook" -> ScrapbookScoreCard(
                        score = score,
                        challengeType = challengeType,
                        theme = currentTheme,
                        rotation = rotation
                    )
                    "y2k_cyber" -> Y2KScoreCard(
                        score = score,
                        challengeType = challengeType,
                        theme = currentTheme,
                        glowAlpha = glowAlpha
                    )
                    else -> DefaultScoreCard(
                        score = score,
                        challengeType = challengeType,
                        theme = currentTheme
                    )
                }
            }

            // FIXED CLOSE BUTTON - Always visible on small screens
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(40.dp)
                    .background(
                        color = Color.Black.copy(alpha = 0.8f),
                        shape = CircleShape
                    )
                    .border(
                        width = 2.dp,
                        color = currentTheme.accentColor,
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun ScrapbookScoreCard(
    score: ScoringResult,
    challengeType: ChallengeType,
    theme: AppTheme,
    rotation: Float
) {
    val stickyColors = listOf(
        Color(0xFFFFEB3B), // Yellow
        Color(0xFFFFCDD2), // Light Pink
        Color(0xFFC8E6C9), // Light Green
        Color(0xFFBBDEFB), // Light Blue
        Color(0xFFD1C4E9) // Light Purple
    )

    // Remember the color so it doesn't flash on recomposition
    val stickyNoteColor = remember { stickyColors.random() }


    Card(
        modifier = Modifier
            .rotate(rotation * 2f)
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = stickyNoteColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Tape effect at top
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(20.dp)
                    .background(
                        Color.Gray.copy(alpha = 0.6f),
                        RoundedCornerShape(4.dp)
                    )
                    .rotate(-5f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Main score with hand-drawn style
            Text(
                text = getScoreEmoji(score.score),
                fontSize = 48.sp
            )
            Text(
                text = "${score.score}%",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = theme.accentColor,
                modifier = Modifier.rotate(-1f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Encouraging header
            Text(
                text = getEncouragingHeader(score.score, challengeType),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = theme.textPrimary,
                modifier = Modifier.rotate(0.5f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Score breakdown (simplified for scrapbook)
            ScrapbookScoreBreakdown(score, theme)

            Spacer(modifier = Modifier.height(20.dp))

            // Tips section
            ScrapbookTips(score, challengeType, theme)
        }
    }
}

@Composable
private fun Y2KScoreCard(
    score: ScoringResult,
    challengeType: ChallengeType,
    theme: AppTheme,
    glowAlpha: Float
) {
    Card(
        modifier = Modifier
            .padding(16.dp)
            .graphicsLayer {
                shadowElevation = 20.dp.toPx()
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.9f)
        ),
        border = BorderStroke(
            2.dp,
            Brush.linearGradient(
                colors = listOf(
                    theme.accentColor.copy(alpha = glowAlpha),
                    theme.accentColor.copy(alpha = glowAlpha),
                    theme.accentColor.copy(alpha = glowAlpha)
                )
            )
        )
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Glowing score display
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                theme.accentColor.copy(alpha = glowAlpha),
                                Color.Transparent
                            )
                        ),
                        shape = RoundedCornerShape(60.dp)
                    )
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${score.score}%",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.accentColor
                    )
                    Text(
                        text = getScoreEmoji(score.score),
                        fontSize = 24.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Cyber-style header
            Text(
                text = "║ ${getEncouragingHeader(score.score, challengeType).uppercase()} ║",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = theme.accentColor,
                textAlign = TextAlign.Center,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Tech-style breakdown
            Y2KScoreBreakdown(score, theme)

            Spacer(modifier = Modifier.height(20.dp))

            // Cyber tips
            Y2KTips(score, challengeType, theme)
        }
    }
}

@Composable
private fun DefaultScoreCard(
    score: ScoringResult,
    challengeType: ChallengeType,
    theme: AppTheme
) {
    Card(
        modifier = Modifier.padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = theme.cardBackground
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Score display
            Text(
                text = "${score.score}%",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = theme.accentColor
            )
            Text(
                text = getScoreEmoji(score.score),
                fontSize = 32.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = getEncouragingHeader(score.score, challengeType),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = theme.textPrimary
            )

            Spacer(modifier = Modifier.height(20.dp))

            DefaultScoreBreakdown(score, theme)

            Spacer(modifier = Modifier.height(20.dp))

            DefaultTips(score, challengeType, theme)
        }
    }
}

@Composable
private fun ScrapbookScoreBreakdown(score: ScoringResult, theme: AppTheme) {
    Column {
        Text(
            text = "Your Performance:",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = theme.textPrimary,
            modifier = Modifier.rotate(-0.5f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        // Simplified breakdown for scrapbook
        ScrapbookMetricRow("Melody Match", (score.metrics.pitch * 100).toInt(), "🎵")
        ScrapbookMetricRow("Voice Match", (score.metrics.mfcc * 100).toInt(), "🎤")
    }
}

@Composable
private fun ScrapbookMetricRow(label: String, value: Int, emoji: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = emoji, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 14.sp,
                color = Color.Black.copy(alpha = 0.8f)
            )
        }
        Text(
            text = "$value%",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun Y2KScoreBreakdown(score: ScoringResult, theme: AppTheme) {
    Column {
        Text(
            text = "> PERFORMANCE ANALYSIS",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = theme.accentColor,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Y2KMetricRow("PITCH_SYNC", (score.metrics.pitch * 100).toInt(), theme)
        Y2KMetricRow("VOICE_MATCH", (score.metrics.mfcc * 100).toInt(), theme)
        Y2KMetricRow("OVERALL", score.score, theme)
    }
}

@Composable
private fun Y2KMetricRow(label: String, value: Int, theme: AppTheme) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = theme.textPrimary,
            letterSpacing = 1.sp
        )
        Text(
            text = "${value}%",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = theme.accentColor
        )
    }
}

@Composable
private fun DefaultScoreBreakdown(score: ScoringResult, theme: AppTheme) {
    Column {
        Text(
            text = "Performance Breakdown:",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = theme.textPrimary
        )
        Spacer(modifier = Modifier.height(12.dp))
        DefaultMetricRow("Melody Matching", (score.metrics.pitch * 100).toInt(), theme)
        DefaultMetricRow("Voice Similarity", (score.metrics.mfcc * 100).toInt(), theme)
    }
}

@Composable
private fun DefaultMetricRow(label: String, value: Int, theme: AppTheme) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = theme.textPrimary
        )
        Text(
            text = "$value%",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = theme.accentColor
        )
    }
}

@Composable
private fun ScrapbookTips(score: ScoringResult, challengeType: ChallengeType, theme: AppTheme) {
    Column {
        Text(
            text = "Tips to improve:",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = theme.textPrimary,
            modifier = Modifier.rotate(0.5f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        val tips = generateEncouragingTips(score, challengeType)
        tips.forEach { tip ->
            Text(
                text = "• $tip",
                fontSize = 12.sp,
                color = Color.Black.copy(alpha = 0.8f),
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun Y2KTips(score: ScoringResult, challengeType: ChallengeType, theme: AppTheme) {
    Column {
        Text(
            text = "> IMPROVEMENT_PROTOCOL",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = theme.accentColor,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        val tips = generateEncouragingTips(score, challengeType)
        tips.forEach { tip ->
            Text(
                text = ">> $tip",
                fontSize = 11.sp,
                color = theme.textPrimary,
                modifier = Modifier.padding(vertical = 2.dp),
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
private fun DefaultTips(score: ScoringResult, challengeType: ChallengeType, theme: AppTheme) {
    Column {
        Text(
            text = "Tips for next time:",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = theme.textPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        val tips = generateEncouragingTips(score, challengeType)
        tips.forEach { tip ->
            Text(
                text = "• $tip",
                fontSize = 12.sp,
                color = theme.textPrimary,
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }
    }
}

// Helper functions
private fun getScoreEmoji(score: Int): String {
    return when {
        score >= 95 -> "🏆"
        score >= 90 -> "🥇"
        score >= 80 -> "🥈"
        score >= 70 -> "🥉"
        score >= 60 -> "⭐"
        score >= 50 -> "👍"
        score >= 40 -> "😊"
        else -> "💪"
    }
}

private fun getEncouragingHeader(score: Int, challengeType: ChallengeType): String {
    val challengeText = if (challengeType == ChallengeType.REVERSE) "reverse singing" else "vocal mimicry"
    return when {
        score >= 95 -> "LEGENDARY $challengeText mastery!"
        score >= 90 -> "Incredible $challengeText skills!"
        score >= 80 -> "Great job with $challengeText!"
        score >= 70 -> "Nice $challengeText progress!"
        score >= 60 -> "Good $challengeText effort!"
        score >= 50 -> "Keep practicing $challengeText!"
        else -> "$challengeText is tough - you're learning!"
    }
}

private fun generateEncouragingTips(score: ScoringResult, challengeType: ChallengeType): List<String> {
    val tips = mutableListOf<String>()
    when {
        score.score >= 90 -> {
            tips.add("You're almost perfect! Try tiny adjustments to nail it 100%")
            tips.add("Your technique is excellent - keep it up!")
        }
        score.score >= 70 -> {
            if (score.metrics.pitch < 0.8f) {
                tips.add("Focus on matching the melody shape more closely")
            }
            if (score.metrics.mfcc < 0.8f) {
                tips.add("Try to match the vocal tone and style")
            }
            tips.add("You're doing great! Small tweaks will boost your score")
        }
        score.score >= 50 -> {
            tips.add("Listen carefully to the melody before attempting")
            if (challengeType == ChallengeType.REVERSE) {
                tips.add("Reverse singing is hard! Focus on the rhythm first")
            } else {
                tips.add("Try humming along first to learn the tune")
            }
            tips.add("Practice makes perfect - keep going!")
        }
        else -> {
            tips.add("Start by listening to the original a few times")
            tips.add("Try singing louder and clearer")
            if (challengeType == ChallengeType.REVERSE) {
                tips.add("Reverse is super challenging - every attempt counts!")
            }
            tips.add("You're brave for trying - improvement comes with practice!")
        }
    }
    return tips.take(3) // Limit to 3 tips max
}