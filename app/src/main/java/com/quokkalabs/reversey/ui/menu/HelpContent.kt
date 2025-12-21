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
                if (expanded) StaticMenuColors.cardSelected
                else StaticMenuColors.cardUnselected
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
                        color = if (expanded) StaticMenuColors.textOnCard
                        else StaticMenuColors.textOnGradient
                    )
                }

                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = if (expanded) StaticMenuColors.textOnCard
                    else StaticMenuColors.textOnGradient,
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
                        color = StaticMenuColors.textOnCard.copy(alpha = 0.15f),
                        modifier = Modifier.padding(bottom = 14.dp)
                    )

                    when (section) {
                        HelpSection.HOW_TO_PLAY -> HowToPlayContent()
                        HelpSection.CONTROLS -> ControlsContent()
                        HelpSection.THEMES -> ThemesHelpContent()
                        HelpSection.SCORING -> ScoringContent()
                        HelpSection.TIPS -> TipsContent()
                    }
                }
            }
        }
    }
}

// ========== HOW TO PLAY ==========
@Composable
private fun HowToPlayContent() {
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
                    color = StaticMenuColors.textOnCard.copy(alpha = 0.85f)
                )
            }
        }
    }
}

// ========== CONTROLS ==========
@Composable
private fun ControlsContent() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Main Screen Controls
        HelpSubsection("Main Screen") {
            HelpTable(
                rows = listOf(
                    "Record button" to "Tap to start/stop recording",
                    "Play ▶️" to "Play original audio",
                    "Rev ⏪" to "Play reversed audio",
                    "Pause ⏸️" to "Pause playback",
                    "Rev 🎙️" to "Start a player attempt"
                )
            )
        }

        // Recording Cards
        HelpSubsection("Recording Cards") {
            HelpTable(
                rows = listOf(
                    "Tap filename" to "Rename recording",
                    "Share" to "Share recording",
                    "Delete" to "Delete recording",
                    "Swipe" to "Delete (if enabled)"
                )
            )
        }

        // Attempt Cards
        HelpSubsection("Attempt Cards") {
            HelpTable(
                rows = listOf(
                    "🏚️ icon" to "Jump to recording",
                    "Tap score" to "View score breakdown",
                    "Play buttons" to "Listen to your attempt",
                    "Score %" to "Show score breakdown"
                )
            )
        }
    }
}

// ========== THEMES HELP ==========
@Composable
private fun ThemesHelpContent() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "ReVerseY includes 14 visual themes. Each theme changes:",
            style = MaterialTheme.typography.bodyMedium,
            color = StaticMenuColors.textOnCard.copy(alpha = 0.85f)
        )

        val changes = listOf(
            "• Background colors and gradients",
            "• Card styling and borders",
            "• Button appearance",
            "• Score emojis",
            "• Dialog text and personality",
            "• Pro themes have animations, sounds and Easter eggs"
        )

        changes.forEach { item ->
            Text(
                text = item,
                style = MaterialTheme.typography.bodyMedium,
                color = StaticMenuColors.textOnCard.copy(alpha = 0.75f),
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "To change theme: Menu → Themes",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = StaticMenuColors.textOnCard
        )

        Text(
            text = "Themes are purely cosmetic and do not affect gameplay or scoring.",
            style = MaterialTheme.typography.bodySmall,
            color = StaticMenuColors.textOnCard.copy(alpha = 0.6f)
        )
    }
}

// ========== SCORING ==========
@Composable
private fun ScoringContent() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // The Basics
        HelpSubsection("The Basics") {
            Text(
                text = "The app breaks down speech into individual sounds called phonemes. " +
                        "For example, \"cat\" has three sounds: K - AE - T.\n\n" +
                        "Your attempt is compared to the target sounds. " +
                        "The more sounds you match, the higher your score.",
                style = MaterialTheme.typography.bodyMedium,
                color = StaticMenuColors.textOnCard.copy(alpha = 0.85f)
            )
        }

        // Score Calculation
        HelpSubsection("Score Calculation") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ScoringItem(
                    label = "Sound Matching",
                    value = "85% of score",
                    description = "How many target sounds appear in your attempt"
                )
                ScoringItem(
                    label = "Timing Bonus",
                    value = "up to 15%",
                    description = "Bonus if attempt length matches target length"
                )
            }
        }

        // Difficulty Levels
        HelpSubsection("Difficulty Levels") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Difficulty affects both sound matching AND timing strictness.",
                    style = MaterialTheme.typography.bodySmall,
                    color = StaticMenuColors.textOnCard.copy(alpha = 0.7f)
                )

                DifficultyRow("Easy 💚", "Similar sounds accepted (T≈D, P≈B)", "50-150%")
                DifficultyRow("Normal 💎", "Exact sounds required", "66-133%")
                DifficultyRow("Hard 🔥", "Exact sounds in order", "80-120%")

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Easy is forgiving — if you say \"D\" but the target was \"T\", " +
                            "it counts because they're made the same way in your mouth.",
                    style = MaterialTheme.typography.bodySmall,
                    color = StaticMenuColors.textOnCard.copy(alpha = 0.65f)
                )
            }
        }

        // Score Override
        HelpSubsection("Score Override") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "If you disagree with the algorithm, tap the score to see the breakdown. " +
                            "Use the slider to set your own score.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = StaticMenuColors.textOnCard.copy(alpha = 0.85f)
                )

                HelpTable(
                    rows = listOf(
                        "⚙️" to "Algorithm's score",
                        "✋" to "Human override"
                    )
                )

                Text(
                    text = "You can reset to the algorithm's score at any time.",
                    style = MaterialTheme.typography.bodySmall,
                    color = StaticMenuColors.textOnCard.copy(alpha = 0.65f)
                )
            }
        }
    }
}

// ========== TIPS ==========
@Composable
private fun TipsContent() {
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
                    color = StaticMenuColors.textOnCard.copy(alpha = 0.85f)
                )
            }
        }
    }
}

// ========== HELPER COMPOSABLES ==========

@Composable
private fun HelpSubsection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = StaticMenuColors.textOnCard
        )
        content()
    }
}

@Composable
private fun HelpTable(
    rows: List<Pair<String, String>>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(StaticMenuColors.textOnCard.copy(alpha = 0.08f))
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
                    color = StaticMenuColors.textOnCard,
                    modifier = Modifier.weight(0.4f)
                )
                Text(
                    text = action,
                    style = MaterialTheme.typography.bodyMedium,
                    color = StaticMenuColors.textOnCard.copy(alpha = 0.75f),
                    modifier = Modifier.weight(0.6f)
                )
            }
            if (index < rows.lastIndex) {
                HorizontalDivider(
                    color = StaticMenuColors.textOnCard.copy(alpha = 0.1f),
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
    description: String
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
                    color = StaticMenuColors.textOnCard
                )
                Text(
                    text = "($value)",
                    style = MaterialTheme.typography.bodySmall,
                    color = StaticMenuColors.textOnCard.copy(alpha = 0.6f)
                )
            }
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = StaticMenuColors.textOnCard.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun DifficultyRow(
    level: String,
    matching: String,
    timing: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(StaticMenuColors.textOnCard.copy(alpha = 0.06f))
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = level,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = StaticMenuColors.textOnCard,
            modifier = Modifier.width(80.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = matching,
                style = MaterialTheme.typography.bodySmall,
                color = StaticMenuColors.textOnCard.copy(alpha = 0.8f)
            )
            Text(
                text = "Timing: $timing of target",
                style = MaterialTheme.typography.labelSmall,
                color = StaticMenuColors.textOnCard.copy(alpha = 0.55f)
            )
        }
    }
}
