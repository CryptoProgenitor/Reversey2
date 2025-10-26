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
import kotlin.math.sin

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

    // New animation for steampunk gears
    val gearRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gear_rotation"
    )

    // New animation for cyberpunk scan lines
    val scanLineOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scan_line"
    )

    // New animation for graphite pencil strokes
    val pencilStroke by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pencil_stroke"
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
                // Background glow effects based on theme
                when (currentTheme.id) {
                    "y2k_cyber" -> {
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
                    "steampunk" -> {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .blur(15.dp)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFFFFD700).copy(alpha = glowAlpha * 0.6f), // Golden glow
                                            Color(0xFFCD7F32).copy(alpha = glowAlpha * 0.3f), // Bronze
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                    }
                    "cyberpunk" -> {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .blur(25.dp)
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xFF00FFFF).copy(alpha = glowAlpha), // Neon cyan
                                            Color(0xFFFF0080).copy(alpha = glowAlpha * 0.7f), // Neon magenta
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                    }
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
                    "steampunk" -> SteampunkScoreCard(
                        score = score,
                        challengeType = challengeType,
                        theme = currentTheme,
                        gearRotation = gearRotation
                    )
                    "cyberpunk" -> CyberpunkScoreCard(
                        score = score,
                        challengeType = challengeType,
                        theme = currentTheme,
                        scanLineOffset = scanLineOffset,
                        glowAlpha = glowAlpha
                    )
                    "graphite_sketch" -> GraphiteSketchScoreCard(
                        score = score,
                        challengeType = challengeType,
                        theme = currentTheme,
                        pencilStroke = pencilStroke
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
            brush = Brush.linearGradient(
                colors = listOf(
                    theme.accentColor.copy(alpha = glowAlpha),
                    Color.Cyan.copy(alpha = glowAlpha),
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
            // Futuristic header
            Text(
                text = ">> VOCAL.ANALYSIS.EXE",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = theme.accentColor,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Glowing score
            Box(
                modifier = Modifier
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                theme.accentColor.copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
                    .padding(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = getScoreEmoji(score.score),
                        fontSize = 48.sp
                    )
                    Text(
                        text = "${score.score}%",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.accentColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Status message
            Text(
                text = ">> ${getEncouragingHeader(score.score, challengeType).uppercase()}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color.White,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Performance breakdown
            Y2KScoreBreakdown(score, theme)

            Spacer(modifier = Modifier.height(20.dp))

            // Tips
            Y2KTips(score, challengeType, theme)
        }
    }
}

@Composable
private fun SteampunkScoreCard(
    score: ScoringResult,
    challengeType: ChallengeType,
    theme: AppTheme,
    gearRotation: Float
) {
    Card(
        modifier = Modifier
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2C1810) // Dark mahogany
        ),
        border = BorderStroke(
            3.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFFCD7F32), // Bronze
                    Color(0xFFD4AF37), // Brass gold
                    Color(0xFFCD7F32)
                )
            )
        )
    ) {
        Box {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Victorian header with brass corners
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    // Decorative brass corners
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFFD4AF37))
                            .align(Alignment.TopStart)
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFFD4AF37))
                            .align(Alignment.TopEnd)
                    )

                    Text(
                        text = "⚙️ VOCAL APPARATUS ANALYSIS ⚙️",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD4AF37),
                        textAlign = TextAlign.Center,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Ornate trophy section with gears
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    // Background gear animation
                    Text(
                        text = "⚙️",
                        fontSize = 40.sp,
                        color = Color(0xFFCD7F32).copy(alpha = 0.3f),
                        modifier = Modifier
                            .offset((-20).dp, (-10).dp)
                            .rotate(gearRotation)
                    )
                    Text(
                        text = "⚙️",
                        fontSize = 30.sp,
                        color = Color(0xFFD4AF37).copy(alpha = 0.4f),
                        modifier = Modifier
                            .offset(25.dp, 15.dp)
                            .rotate(-gearRotation * 0.7f)
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = getSteampunkEmoji(score.score),
                            fontSize = 48.sp
                        )
                        Text(
                            text = "${score.score}%",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD4AF37)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Victorian proclamation
                Text(
                    text = getSteampunkHeader(score.score, challengeType),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = Color(0xFFF4A460), // Sandy brown
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Mechanical breakdown
                SteampunkScoreBreakdown(score, theme)

                Spacer(modifier = Modifier.height(20.dp))

                // Victorian tips
                SteampunkTips(score, challengeType, theme)
            }

            // Steam effects
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Text(
                    text = "💨",
                    fontSize = 20.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier
                        .offset(
                            x = (sin(gearRotation * 0.01f) * 5).dp,
                            y = (sin(gearRotation * 0.015f) * 3).dp
                        )
                )
            }
        }
    }
}

@Composable
private fun CyberpunkScoreCard(
    score: ScoringResult,
    challengeType: ChallengeType,
    theme: AppTheme,
    scanLineOffset: Float,
    glowAlpha: Float
) {
    Card(
        modifier = Modifier
            .padding(16.dp),
        shape = RoundedCornerShape(4.dp), // Sharp edges for cyberpunk
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0A0A0A) // Pure black
        ),
        border = BorderStroke(
            2.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF00FFFF), // Neon cyan
                    Color(0xFFFF0080), // Neon magenta
                    Color(0xFF00FFFF)
                )
            )
        )
    ) {
        Box {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Matrix-style header
                Text(
                    text = "◤ NEURAL_VOICE_SCAN.exe ◥",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00FFFF),
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Holographic score display
                Box(
                    modifier = Modifier
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF00FFFF).copy(alpha = 0.1f),
                                    Color.Transparent
                                )
                            )
                        )
                        .border(
                            1.dp,
                            Color(0xFF00FFFF).copy(alpha = glowAlpha),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(16.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = getCyberpunkEmoji(score.score),
                            fontSize = 48.sp
                        )
                        Text(
                            text = "${score.score}%",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00FFFF),
                            modifier = Modifier.graphicsLayer {
                                shadowElevation = 10.dp.toPx()
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Digital underground message
                Text(
                    text = getCyberpunkHeader(score.score, challengeType),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = Color(0xFFFF0080),
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Data matrix breakdown
                CyberpunkScoreBreakdown(score, theme)

                Spacer(modifier = Modifier.height(20.dp))

                // Hacker tips
                CyberpunkTips(score, challengeType, theme)
            }

            // Animated scan lines
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(Color(0xFF00FFFF).copy(alpha = 0.8f))
                    .offset(y = (scanLineOffset * 300).dp)
            )
        }
    }
}

@Composable
private fun GraphiteSketchScoreCard(
    score: ScoringResult,
    challengeType: ChallengeType,
    theme: AppTheme,
    pencilStroke: Float
) {
    Card(
        modifier = Modifier
            .padding(16.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF8F8F8) // Paper white
        ),
        border = BorderStroke(
            1.dp,
            Color(0xFF2A2A2A) // Graphite gray
        )
    ) {
        Box {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Hand-drawn header
                Text(
                    text = "✏️ Voice Sketch Analysis ✏️",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2A2A2A),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Sketchy score circle
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(
                            Color.Transparent,
                            CircleShape
                        )
                        .border(
                            width = 3.dp,
                            color = Color(0xFF666666),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = getGraphiteEmoji(score.score),
                            fontSize = 36.sp
                        )
                        Text(
                            text = "${score.score}%",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2A2A2A)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Hand-written message
                Text(
                    text = getGraphiteHeader(score.score, challengeType),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF666666)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Sketchy breakdown
                GraphiteScoreBreakdown(score, theme)

                Spacer(modifier = Modifier.height(20.dp))

                // Penciled tips
                GraphiteTips(score, challengeType, theme)
            }

            // Animated pencil strokes
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = 0.3f }
            ) {
                // You could add custom drawing here for pencil stroke effects
            }
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
        border = BorderStroke(1.dp, theme.cardBorder)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = getScoreEmoji(score.score),
                fontSize = 48.sp
            )
            Text(
                text = "${score.score}%",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = theme.accentColor
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
            text = "How you did:",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = theme.textPrimary,
            modifier = Modifier.rotate(-0.5f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        ScrapbookMetricRow("Melody", (score.metrics.pitch * 100).toInt(), "🎵")
        ScrapbookMetricRow("Voice", (score.metrics.mfcc * 100).toInt(), "🎤")
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
private fun SteampunkScoreBreakdown(score: ScoringResult, theme: AppTheme) {
    Column {
        Text(
            text = "⚙️ MECHANICAL PRECISION ⚙️",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFD4AF37),
            letterSpacing = 1.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        SteampunkMetricRow("Harmonic Resonance", (score.metrics.pitch * 100).toInt())
        SteampunkMetricRow("Vocal Apparatus", (score.metrics.mfcc * 100).toInt())
        SteampunkMetricRow("Overall Mastery", score.score)
    }
}

@Composable
private fun SteampunkMetricRow(label: String, value: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFFF4A460),
            letterSpacing = 0.5.sp
        )
        Text(
            text = "${value}%",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFD4AF37)
        )
    }
}

@Composable
private fun CyberpunkScoreBreakdown(score: ScoringResult, theme: AppTheme) {
    Column {
        Text(
            text = "◤ DATA_MATRIX_ANALYSIS ◥",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF00FFFF),
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        CyberpunkMetricRow("NEURAL_PITCH", (score.metrics.pitch * 100).toInt())
        CyberpunkMetricRow("VOICE_PATTERN", (score.metrics.mfcc * 100).toInt())
        CyberpunkMetricRow("TOTAL_HACK", score.score)
    }
}

@Composable
private fun CyberpunkMetricRow(label: String, value: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color(0xFFFF0080),
            letterSpacing = 1.sp
        )
        Text(
            text = "${value}%",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF00FFFF)
        )
    }
}

@Composable
private fun GraphiteScoreBreakdown(score: ScoringResult, theme: AppTheme) {
    Column {
        Text(
            text = "Sketch Notes:",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2A2A2A)
        )
        Spacer(modifier = Modifier.height(12.dp))
        GraphiteMetricRow("Melody Line", (score.metrics.pitch * 100).toInt())
        GraphiteMetricRow("Voice Shade", (score.metrics.mfcc * 100).toInt())
        GraphiteMetricRow("Overall", score.score)
    }
}

@Composable
private fun GraphiteMetricRow(label: String, value: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "• $label",
            fontSize = 12.sp,
            color = Color(0xFF666666)
        )
        Text(
            text = "$value%",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2A2A2A)
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
private fun SteampunkTips(score: ScoringResult, challengeType: ChallengeType, theme: AppTheme) {
    Column {
        Text(
            text = "⚗️ ALCHEMICAL IMPROVEMENTS ⚗️",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFD4AF37),
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        val tips = generateSteampunkTips(score, challengeType)
        tips.forEach { tip ->
            Text(
                text = "• $tip",
                fontSize = 11.sp,
                color = Color(0xFFF4A460),
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun CyberpunkTips(score: ScoringResult, challengeType: ChallengeType, theme: AppTheme) {
    Column {
        Text(
            text = "◤ UPGRADE_PROTOCOLS ◥",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF00FFFF),
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        val tips = generateCyberpunkTips(score, challengeType)
        tips.forEach { tip ->
            Text(
                text = ">> $tip",
                fontSize = 10.sp,
                color = Color(0xFFFF0080),
                modifier = Modifier.padding(vertical = 2.dp),
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
private fun GraphiteTips(score: ScoringResult, challengeType: ChallengeType, theme: AppTheme) {
    Column {
        Text(
            text = "Sketch Notes:",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2A2A2A)
        )
        Spacer(modifier = Modifier.height(8.dp))
        val tips = generateGraphiteTips(score, challengeType)
        tips.forEach { tip ->
            Text(
                text = "• $tip",
                fontSize = 12.sp,
                color = Color(0xFF666666),
                modifier = Modifier.padding(vertical = 2.dp)
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

private fun getSteampunkEmoji(score: Int): String {
    return when {
        score >= 95 -> "🏆"
        score >= 90 -> "⚗️"
        score >= 80 -> "🎩"
        score >= 70 -> "⚙️"
        score >= 60 -> "🔧"
        score >= 50 -> "📜"
        score >= 40 -> "🕰️"
        else -> "⚡"
    }
}

private fun getCyberpunkEmoji(score: Int): String {
    return when {
        score >= 95 -> "👑"
        score >= 90 -> "🤖"
        score >= 80 -> "⚡"
        score >= 70 -> "🔥"
        score >= 60 -> "💎"
        score >= 50 -> "🎮"
        score >= 40 -> "💻"
        else -> "🔌"
    }
}

private fun getGraphiteEmoji(score: Int): String {
    return when {
        score >= 95 -> "⭐"
        score >= 90 -> "😊"
        score >= 80 -> "👍"
        score >= 70 -> "😐"
        score >= 60 -> "📝"
        score >= 50 -> "✏️"
        score >= 40 -> "📋"
        else -> "😔"
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

private fun getSteampunkHeader(score: Int, challengeType: ChallengeType): String {
    val challengeText = if (challengeType == ChallengeType.REVERSE) "phonographic reversal" else "vocal apparatus mimicry"
    return when {
        score >= 95 -> "EXTRAORDINARY MASTERY of $challengeText!"
        score >= 90 -> "Most splendid $challengeText performance!"
        score >= 80 -> "Admirable $challengeText craftsmanship!"
        score >= 70 -> "Decent $challengeText endeavour!"
        score >= 60 -> "Respectable $challengeText attempt!"
        score >= 50 -> "Continue perfecting your $challengeText!"
        else -> "$challengeText requires mechanical precision!"
    }
}

private fun getCyberpunkHeader(score: Int, challengeType: ChallengeType): String {
    val challengeText = if (challengeType == ChallengeType.REVERSE) "audio_reverse_hack" else "voice_pattern_clone"
    return when {
        score >= 95 -> "NEURAL_LINK_PERFECTED: $challengeText"
        score >= 90 -> "SYSTEM_HACKED: Elite $challengeText"
        score >= 80 -> "ACCESS_GRANTED: Good $challengeText"
        score >= 70 -> "FIREWALL_BYPASSED: $challengeText"
        score >= 60 -> "CONNECTION_ESTABLISHED: $challengeText"
        score >= 50 -> "UPLOADING_SKILLS: $challengeText"
        else -> "SYSTEM_ERROR: $challengeText failed"
    }
}

private fun getGraphiteHeader(score: Int, challengeType: ChallengeType): String {
    val challengeText = if (challengeType == ChallengeType.REVERSE) "reverse melody sketch" else "voice drawing"
    return when {
        score >= 95 -> "Perfect $challengeText - frame worthy!"
        score >= 90 -> "Beautiful $challengeText artwork!"
        score >= 80 -> "Nice $challengeText drawing!"
        score >= 70 -> "Good $challengeText sketch!"
        score >= 60 -> "Decent $challengeText attempt!"
        score >= 50 -> "Keep sketching that $challengeText!"
        else -> "Every artist starts with rough $challengeText!"
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

private fun generateSteampunkTips(score: ScoringResult, challengeType: ChallengeType): List<String> {
    val tips = mutableListOf<String>()
    when {
        score.score >= 90 -> {
            tips.add("Your vocal apparatus operates at peak efficiency!")
            tips.add("Fine-tune the mechanical precision for perfection")
        }
        score.score >= 70 -> {
            if (score.metrics.pitch < 0.8f) {
                tips.add("Adjust harmonic resonance calibration")
            }
            if (score.metrics.mfcc < 0.8f) {
                tips.add("Refine vocal apparatus settings")
            }
            tips.add("Your steam engine needs minor adjustments")
        }
        score.score >= 50 -> {
            tips.add("Study the mechanical blueprints carefully")
            tips.add("Oil your vocal gears for smoother operation")
            tips.add("Practice builds steam pressure!")
        }
        else -> {
            tips.add("Inspect the original phonographic recording")
            tips.add("Ensure proper boiler pressure for projection")
            tips.add("Every great inventor faces initial setbacks!")
        }
    }
    return tips.take(3)
}

private fun generateCyberpunkTips(score: ScoringResult, challengeType: ChallengeType): List<String> {
    val tips = mutableListOf<String>()
    when {
        score.score >= 90 -> {
            tips.add("Neural interface operating at maximum efficiency")
            tips.add("Minor code optimization will achieve perfection")
        }
        score.score >= 70 -> {
            if (score.metrics.pitch < 0.8f) {
                tips.add("Recalibrate pitch detection algorithms")
            }
            if (score.metrics.mfcc < 0.8f) {
                tips.add("Update voice pattern recognition matrix")
            }
            tips.add("System requires debugging for optimal performance")
        }
        score.score >= 50 -> {
            tips.add("Analyze source code before execution")
            tips.add("Increase bandwidth for clearer signal")
            tips.add("Practice enhances neural network training")
        }
        else -> {
            tips.add("Download original audio files for reference")
            tips.add("Boost signal strength for better transmission")
            tips.add("Every hacker starts with basic code!")
        }
    }
    return tips.take(3)
}

private fun generateGraphiteTips(score: ScoringResult, challengeType: ChallengeType): List<String> {
    val tips = mutableListOf<String>()
    when {
        score.score >= 90 -> {
            tips.add("Your sketch is nearly photorealistic!")
            tips.add("Add subtle shading for perfection")
        }
        score.score >= 70 -> {
            if (score.metrics.pitch < 0.8f) {
                tips.add("Work on melody line accuracy")
            }
            if (score.metrics.mfcc < 0.8f) {
                tips.add("Practice voice texture and shading")
            }
            tips.add("Your drawing skills are developing nicely")
        }
        score.score >= 50 -> {
            tips.add("Study the reference image carefully")
            tips.add("Use different pencil pressures for variation")
            tips.add("Every sketch improves your technique")
        }
        else -> {
            tips.add("Look at the original artwork closely")
            tips.add("Press harder for darker, clearer lines")
            tips.add("All great artists started with simple sketches!")
        }
    }
    return tips.take(3)
}