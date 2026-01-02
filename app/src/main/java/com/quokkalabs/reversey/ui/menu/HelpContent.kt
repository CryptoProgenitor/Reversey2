package com.quokkalabs.reversey.ui.menu

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quokkalabs.reversey.ui.theme.LocalAestheticTheme
import com.quokkalabs.reversey.ui.theme.MenuColors

// ========== HELP SECTIONS ==========
enum class HelpSection(val title: String, val emoji: String) {
    HOW_TO_PLAY("How to Play", "🎮"),
    CONTROLS("Controls", "🎛️"),
    THEMES("Themes", "🎨"),
    SCORING("How Scoring Works", "📊"),
    TIPS("Tips", "💡")
}

// ========== HELP SCREEN ==========
@Composable
fun HelpContent() {
    val menuColors = LocalAestheticTheme.current.menuColors
    var expandedSection by rememberSaveable { mutableStateOf<HelpSection?>(HelpSection.HOW_TO_PLAY) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        HelpSection.entries.forEach { section ->
            HelpSectionCard(
                section = section,
                expanded = expandedSection == section,
                menuColors = menuColors,
                onToggle = {
                    expandedSection = if (expandedSection == section) null else section
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

// ========== EXPANDABLE SECTION CARD ==========
@Composable
private fun HelpSectionCard(
    section: HelpSection,
    expanded: Boolean,
    menuColors: MenuColors,
    onToggle: () -> Unit
) {
    val shape = RoundedCornerShape(14.dp)
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "expandRotation"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (expanded) 8.dp else 2.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.1f),
                spotColor = Color.Black.copy(alpha = 0.15f)
            )
            .clip(shape)
            .background(
                if (expanded) menuColors.menuCardBackground
                else menuColors.menuItemBackground
            )
            .animateContentSize()
    ) {
        Column {
            // Header Row (always visible)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = section.emoji,
                        fontSize = 22.sp
                    )
                    Text(
                        text = section.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = if (expanded) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (expanded) menuColors.menuItemText
                        else menuColors.menuTitleText
                    )
                }

                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = if (expanded) menuColors.menuItemText
                    else menuColors.menuTitleText,
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(rotation)
                )
            }

            // Content (only when expanded)
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(
                        start = 18.dp,
                        end = 18.dp,
                        bottom = 18.dp
                    )
                ) {
                    HorizontalDivider(
                        color = menuColors.menuItemText.copy(alpha = 0.15f),
                        modifier = Modifier.padding(bottom = 14.dp)
                    )

                    when (section) {
                        HelpSection.HOW_TO_PLAY -> HowToPlayContent(menuColors)
                        HelpSection.CONTROLS -> ControlsContent(menuColors)
                        HelpSection.THEMES -> ThemesHelpContent(menuColors)
                        HelpSection.SCORING -> ScoringContent(menuColors)
                        HelpSection.TIPS -> TipsContent(menuColors)
                    }
                }
            }
        }
    }
}

// ========== HOW TO PLAY ==========
@Composable
private fun HowToPlayContent(menuColors: MenuColors) {
    val steps = listOf(
        "1️⃣" to "Record — Tap the record button and speak a phrase",
        "2️⃣" to "Listen — The app reverses your audio into gibberish",
        "3️⃣" to "Attempt — Try to speak those reversed sounds back",
        "4️⃣" to "Score — See how close you got"
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        steps.forEach { (num, text) ->
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(text = num, fontSize = 18.sp)
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = menuColors.menuItemText.copy(alpha = 0.85f)
                )
            }
        }
    }
}

// ========== CONTROLS ==========
@Composable
private fun ControlsContent(menuColors: MenuColors) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Main Screen Controls
        HelpSubsection("Main Screen", menuColors) {
            HelpTable(
                menuColors = menuColors,
                rows = listOf(
                    "Record button" to "Tap to start/stop recording",
                    "Play ▶️" to "Play original audio",
                    "Rev ⏪" to "Play reversed audio",
                    "Pause ⏸️" to "Pause playback",
                    "Try 🎙️" to "Start a player attempt"
                )
            )
        }

        // Card Controls
        HelpSubsection("Recording Cards", menuColors) {
            HelpTable(
                menuColors = menuColors,
                rows = listOf(
                    "Tap 🏠" to "Jump to challenge",
                    "Press filename" to "Rename recording",
                    "Press score" to "See score breakdown"
                )
            )
        }
    }
}

// ========== THEMES HELP ==========
@Composable
private fun ThemesHelpContent(menuColors: MenuColors) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "ReVerseY comes with multiple themes to personalize your experience.",
            style = MaterialTheme.typography.bodyMedium,
            color = menuColors.menuItemText.copy(alpha = 0.85f)
        )

        HelpSubsection("Changing Themes", menuColors) {
            Text(
                text = "Go to Menu → Themes to browse and select a theme.",
                style = MaterialTheme.typography.bodyMedium,
                color = menuColors.menuItemText.copy(alpha = 0.85f)
            )
        }

        HelpSubsection("Pro Themes", menuColors) {
            Text(
                text = "Some themes are marked as Pro and include custom animations, sounds, and visual effects.",
                style = MaterialTheme.typography.bodyMedium,
                color = menuColors.menuItemText.copy(alpha = 0.85f)
            )
        }
    }
}

// ========== SCORING ==========
@Composable
private fun ScoringContent(menuColors: MenuColors) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Your score is calculated by comparing your attempt to the reversed audio.",
            style = MaterialTheme.typography.bodyMedium,
            color = menuColors.menuItemText.copy(alpha = 0.85f)
        )

        HelpSubsection("Score Components", menuColors) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ScoringItem(
                    label = "Phonetic Match",
                    value = "85%",
                    description = "How well your sounds match the target",
                    menuColors = menuColors
                )
                ScoringItem(
                    label = "Timing",
                    value = "15%",
                    description = "How close your duration is to the target",
                    menuColors = menuColors
                )
            }
        }

        // Difficulty Levels
        HelpSubsection("Difficulty Levels", menuColors) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Difficulty affects both sound matching AND timing strictness.",
                    style = MaterialTheme.typography.bodySmall,
                    color = menuColors.menuItemText.copy(alpha = 0.7f)
                )

                DifficultyRow("Easy 💚", "Similar sounds accepted (T≈D, P≈B)", "50-150%", menuColors)
                DifficultyRow("Normal 💎", "Exact sounds required", "66-133%", menuColors)
                DifficultyRow("Hard 🔥", "Exact sounds in order", "80-120%", menuColors)

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Easy is forgiving — if you say \"D\" but the target was \"T\", " +
                            "it counts because they're made the same way in your mouth.",
                    style = MaterialTheme.typography.bodySmall,
                    color = menuColors.menuItemText.copy(alpha = 0.65f)
                )
            }
        }

        // Score Override
        HelpSubsection("Score Override", menuColors) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "If you disagree with the algorithm, tap the score to see the breakdown. " +
                            "Use the slider to set your own score.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = menuColors.menuItemText.copy(alpha = 0.85f)
                )

                HelpTable(
                    menuColors = menuColors,
                    rows = listOf(
                        "⚙️" to "Algorithm's score",
                        "✋" to "Human override"
                    )
                )

                Text(
                    text = "You can reset to the algorithm's score at any time.",
                    style = MaterialTheme.typography.bodySmall,
                    color = menuColors.menuItemText.copy(alpha = 0.65f)
                )
            }
        }
    }
}

// ========== TIPS ==========
@Composable
private fun TipsContent(menuColors: MenuColors) {
    val tips = listOf(
        "🎧" to "Listen to the reversed audio several times before attempting",
        "🔊" to "Focus on matching sounds, not making real words",
        "🌱" to "Start on Easy to get the hang of it",
        "⚖️" to "Use override if the algorithm seems unfair",
        "🎉" to "This is a party game — have fun with it!"
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        tips.forEach { (emoji, tip) ->
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(text = emoji, fontSize = 18.sp)
                Text(
                    text = tip,
                    style = MaterialTheme.typography.bodyMedium,
                    color = menuColors.menuItemText.copy(alpha = 0.85f)
                )
            }
        }
    }
}

// ========== HELPER COMPOSABLES ==========

@Composable
private fun HelpSubsection(
    title: String,
    menuColors: MenuColors,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = menuColors.menuItemText
        )
        content()
    }
}

@Composable
private fun HelpTable(
    menuColors: MenuColors,
    rows: List<Pair<String, String>>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(menuColors.menuItemText.copy(alpha = 0.08f))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        rows.forEachIndexed { index, (control, action) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = control,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = menuColors.menuItemText,
                    modifier = Modifier.weight(0.4f)
                )
                Text(
                    text = action,
                    style = MaterialTheme.typography.bodyMedium,
                    color = menuColors.menuItemText.copy(alpha = 0.75f),
                    modifier = Modifier.weight(0.6f)
                )
            }
            if (index < rows.lastIndex) {
                HorizontalDivider(
                    color = menuColors.menuItemText.copy(alpha = 0.1f),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun ScoringItem(
    label: String,
    value: String,
    description: String,
    menuColors: MenuColors
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = menuColors.menuItemText
                )
                Text(
                    text = "($value)",
                    style = MaterialTheme.typography.bodySmall,
                    color = menuColors.menuItemText.copy(alpha = 0.6f)
                )
            }
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = menuColors.menuItemText.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun DifficultyRow(
    level: String,
    matching: String,
    timing: String,
    menuColors: MenuColors
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(menuColors.menuItemText.copy(alpha = 0.06f))
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = level,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = menuColors.menuItemText,
            modifier = Modifier.width(80.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = matching,
                style = MaterialTheme.typography.bodySmall,
                color = menuColors.menuItemText.copy(alpha = 0.8f)
            )
            Text(
                text = "Timing: $timing of target",
                style = MaterialTheme.typography.labelSmall,
                color = menuColors.menuItemText.copy(alpha = 0.55f)
            )
        }
    }
}