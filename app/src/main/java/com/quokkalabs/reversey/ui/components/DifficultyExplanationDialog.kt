package com.quokkalabs.reversey.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.quokkalabs.reversey.scoring.DifficultyConfig
import com.quokkalabs.reversey.scoring.DifficultyLevel

/**
 * Dialog explaining the scoring methodology for a specific difficulty level.
 * Triggered by long-pressing a difficulty selector button.
 */
@Composable
fun DifficultyExplanationDialog(
    difficulty: DifficultyLevel,
    onDismiss: () -> Unit
) {
    val difficultyColor = DifficultyConfig.getColorForDifficulty(difficulty)
    val emoji = DifficultyConfig.getEmojiForDifficulty(difficulty)

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
                .padding(horizontal = 16.dp),
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = emoji, fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "${difficulty.displayName} Mode",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = difficultyColor
                        )
                        Text(
                            text = "Scoring Explained",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Content based on difficulty
                when (difficulty) {
                    DifficultyLevel.EASY -> EasyExplanation(difficultyColor)
                    DifficultyLevel.NORMAL -> NormalExplanation(difficultyColor)
                    DifficultyLevel.HARD -> HardExplanation(difficultyColor)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Common formula section
                FormulaSection()

                Spacer(modifier = Modifier.height(16.dp))

                // Close button
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = "GOT IT",
                        fontWeight = FontWeight.Bold,
                        color = difficultyColor
                    )
                }
            }
        }
    }
}

@Composable
private fun EasyExplanation(accentColor: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ExplanationCard(
            title = "🔤 Phoneme Matching: FUZZY",
            accentColor = accentColor
        ) {
            Text(
                text = "Similar sounds get partial credit!",
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "• \"AH\" matches \"AE\" → 0.7 points\n" +
                        "• \"AH\" matches \"AH\" → 1.0 points\n" +
                        "• Close enough counts!",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                lineHeight = 18.sp
            )
        }

        ExplanationCard(
            title = "⏱️ Duration Window: 50% – 150%",
            accentColor = accentColor
        ) {
            Text(
                text = "Very forgiving timing",
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "If the original was 2 seconds, you can take anywhere from 1 to 3 seconds.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
        }

        ExplanationCard(
            title = "📊 Duration Bonus Width: 0.3",
            accentColor = accentColor
        ) {
            Text(
                text = "Gaussian bell curve is wide — even imperfect timing gets good bonus points.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun NormalExplanation(accentColor: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ExplanationCard(
            title = "🔤 Phoneme Matching: EXACT",
            accentColor = accentColor
        ) {
            Text(
                text = "Bag overlap — counts must match!",
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "• Order doesn't matter\n" +
                        "• Uses Jaccard similarity\n" +
                        "• Formula: intersection ÷ union",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                lineHeight = 18.sp
            )
        }

        ExplanationCard(
            title = "⏱️ Duration Window: 66% – 133%",
            accentColor = accentColor
        ) {
            Text(
                text = "Balanced timing",
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "If the original was 3 seconds, you should be between 2 and 4 seconds.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
        }

        ExplanationCard(
            title = "📊 Duration Bonus Width: 0.2",
            accentColor = accentColor
        ) {
            Text(
                text = "Gaussian bell curve is moderate — timing matters but isn't brutal.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun HardExplanation(accentColor: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ExplanationCard(
            title = "🔤 Phoneme Matching: ORDERED",
            accentColor = accentColor
        ) {
            Text(
                text = "Longest Common Subsequence!",
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "• Phonemes must appear in order\n" +
                        "• D before L before R before OW\n" +
                        "• Skipped sounds hurt your score",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                lineHeight = 18.sp
            )
        }

        ExplanationCard(
            title = "⏱️ Duration Window: 80% – 120%",
            accentColor = accentColor
        ) {
            Text(
                text = "Tight timing required",
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "If the original was 5 seconds, you must be between 4 and 6 seconds.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
        }

        ExplanationCard(
            title = "📊 Duration Bonus Width: 0.1",
            accentColor = accentColor
        ) {
            Text(
                text = "Gaussian bell curve is narrow — only near-perfect timing gets the bonus!",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun FormulaSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF1E1E2E))
            .padding(12.dp)
    ) {
        Text(
            text = "📐 SCORE FORMULA",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF89B4FA),
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "score = (√phonemeOverlap × 0.85) + durationBonus",
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFFA6E3A1)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "durationBonus = 0.15 × e^(-(ratio-1)²/width)",
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFFF9E2AF)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "85% phonemes",
                fontSize = 10.sp,
                color = Color(0xFFCDD6F4)
            )
            Text(
                text = "15% timing bonus",
                fontSize = 10.sp,
                color = Color(0xFFCDD6F4)
            )
        }
    }
}

@Composable
private fun ExplanationCard(
    title: String,
    accentColor: Color,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .border(
                width = 1.dp,
                color = accentColor.copy(alpha = 0.3f),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(12.dp)
    ) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = accentColor
        )
        Spacer(modifier = Modifier.height(6.dp))
        content()
    }
}
