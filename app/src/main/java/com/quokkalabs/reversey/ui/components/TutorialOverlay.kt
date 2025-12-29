package com.quokkalabs.reversey.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ═══════════════════════════════════════════════════════════════════════════════
//  Y2K CYBER POP TUTORIAL THEME
// ═══════════════════════════════════════════════════════════════════════════════

private object TutorialTheme {
    // Y2K Cyber Pop colors
    val hotPink = Color(0xFFFF6EC7)
    val purple = Color(0xFF7873F5)
    val blue = Color(0xFF4FACFE)
    val white = Color.White
    val black = Color.Black
    val surface = Color(0xFF1A1A2E)
    val cardSurface = Color(0xFF16213E)

    // Gradients
    val primaryGradient = Brush.verticalGradient(listOf(hotPink, purple, blue))
    val horizontalGradient = Brush.horizontalGradient(listOf(hotPink, purple, blue))
    val overlayBackground = black.copy(alpha = 0.95f)

    // Difficulty colors (matching DifficultyConfig)
    val easyGreen = Color(0xFF4ADE80)
    val normalYellow = Color(0xFFFBBF24)
    val hardRed = Color(0xFFF87171)
}

// ═══════════════════════════════════════════════════════════════════════════════
//  MAIN TUTORIAL OVERLAY
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun TutorialOverlay(
    onDismiss: () -> Unit,
    onComplete: () -> Unit
) {
    var currentStep by remember { mutableIntStateOf(0) }
    val totalSteps = 13

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TutorialTheme.overlayBackground)
    ) {
        // Main content area
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .padding(top = 40.dp)
                .navigationBarsPadding()
                .padding(bottom = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Step content with animations
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        // Moving forward
                        slideInHorizontally(
                            initialOffsetX = { it },
                            animationSpec = tween(350)
                        ) + fadeIn(tween(350)) togetherWith
                                slideOutHorizontally(
                                    targetOffsetX = { -it },
                                    animationSpec = tween(350)
                                ) + fadeOut(tween(350))
                    } else {
                        // Moving backward
                        slideInHorizontally(
                            initialOffsetX = { -it },
                            animationSpec = tween(350)
                        ) + fadeIn(tween(350)) togetherWith
                                slideOutHorizontally(
                                    targetOffsetX = { it },
                                    animationSpec = tween(350)
                                ) + fadeOut(tween(350))
                    }
                },
                label = "step_transition",
                modifier = Modifier.weight(1f)
            ) { step ->
                TutorialStep(step = step)
            }
        }

        // Bottom navigation bar
        TutorialNavBar(
            currentStep = currentStep,
            totalSteps = totalSteps,
            onBack = { if (currentStep > 0) currentStep-- },
            onNext = {
                if (currentStep < totalSteps - 1) {
                    currentStep++
                } else {
                    onComplete()
                }
            },
            onQuit = onDismiss,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
//  NAVIGATION BAR
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun TutorialNavBar(
    currentStep: Int,
    totalSteps: Int,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onQuit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(TutorialTheme.surface.copy(alpha = 0.7f))
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back button
        NavButton(
            icon = Icons.Default.ArrowBack,
            label = "Back",
            enabled = currentStep > 0,
            onClick = onBack
        )

        // Step indicator
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Step ${currentStep + 1} of $totalSteps",
                color = TutorialTheme.white,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            // Progress dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(top = 6.dp)
            ) {
                repeat(totalSteps) { index ->
                    Box(
                        modifier = Modifier
                            .size(if (index == currentStep) 8.dp else 6.dp)
                            .background(
                                color = if (index == currentStep) TutorialTheme.hotPink
                                else if (index < currentStep) TutorialTheme.purple.copy(alpha = 0.7f)
                                else TutorialTheme.white.copy(alpha = 0.3f),
                                shape = CircleShape
                            )
                    )
                }
            }
        }

        // Quit button
        NavButton(
            icon = Icons.Default.Close,
            label = "Quit",
            enabled = true,
            onClick = onQuit,
            isDestructive = true
        )

        // Next button
        NavButton(
            icon = Icons.Default.ArrowForward,
            label = if (currentStep == totalSteps - 1) "Done" else "Next",
            enabled = true,
            onClick = onNext,
            isPrimary = true
        )
    }
}

@Composable
private fun NavButton(
    icon: ImageVector,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    isPrimary: Boolean = false,
    isDestructive: Boolean = false
) {
    val backgroundColor = when {
        !enabled -> Color.Transparent
        isPrimary -> TutorialTheme.hotPink
        isDestructive -> Color.Transparent
        else -> Color.Transparent
    }

    val contentColor = when {
        !enabled -> TutorialTheme.white.copy(alpha = 0.3f)
        isPrimary -> TutorialTheme.black
        isDestructive -> TutorialTheme.hardRed
        else -> TutorialTheme.white
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .then(
                if (isPrimary && enabled) Modifier.background(backgroundColor)
                else Modifier
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .alpha(if (enabled) 1f else 0.4f)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            color = contentColor,
            fontSize = 10.sp,
            fontWeight = if (isPrimary) FontWeight.Bold else FontWeight.Medium
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
//  TUTORIAL STEPS
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun TutorialStep(step: Int) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        when (step) {
            0 -> StepWelcome()
            1 -> StepEmptyHome()
            2 -> StepRecording()
            3 -> StepRecordingCard()
            4 -> StepStartChallenge()
            5 -> StepMakingAttempt()
            6 -> StepAttemptCard()
            7 -> StepScorecard()
            8 -> StepMenuOverview()
            9 -> StepFilesMenu()
            10 -> StepThemesMenu()
            11 -> StepSettingsMenu()
            12 -> StepComplete()
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  STEP 0: Welcome
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StepWelcome() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        // Floating logo emoji
        FloatingEmoji(emoji = "🔄", size = 80.sp)

        Spacer(modifier = Modifier.height(24.dp))

        GradientText(
            text = "Welcome to\nReVerseY!",
            fontSize = 36.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Record. Reverse. Challenge.",
            color = TutorialTheme.blue,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Tap Next to learn how to play",
            color = TutorialTheme.white.copy(alpha = 0.7f),
            fontSize = 14.sp
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  STEP 1: Empty Home Screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StepEmptyHome() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        StepTitle("Your Home Screen")
        StepSubtitle("This is where it all begins")

        Spacer(modifier = Modifier.height(24.dp))

        // Mock home screen
        MockHomeScreen(
            showRecording = false,
            highlightRecordButton = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        CalloutBox(
            text = "Tap the record button to capture a phrase.\nSpeak clearly for best results!"
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  STEP 2: Recording
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StepRecording() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        StepTitle("Recording")
        StepSubtitle("Capturing your voice")

        Spacer(modifier = Modifier.height(24.dp))

        // Mock recording state
        MockHomeScreen(
            showRecording = false,
            isRecording = true,
            highlightRecordButton = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        CalloutBox(
            text = "While recording, you'll see a waveform.\nTap again to stop, or it auto-stops after ~2 minutes."
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  STEP 3: Recording Card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StepRecordingCard() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        StepTitle("Recording Card")
        StepSubtitle("Your recording appears here")

        Spacer(modifier = Modifier.height(20.dp))

        // Mock recording card with hotspots
        MockRecordingCard(
            name = "Hello world",
            showHotspots = true
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Legend
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            LegendItem(icon = Icons.Default.Share, label = "Share", description = "Share the recording")
            LegendItem(icon = Icons.Default.PlayArrow, label = "Play", description = "Play original audio")
            LegendItem(icon = Icons.Default.Replay, label = "Rewind", description = "Play reversed audio")
            LegendItem(icon = Icons.Default.Mic, label = "Try", description = "Start a challenge attempt")
            LegendItem(icon = Icons.Default.Delete, label = "Del", description = "Delete recording")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "💡 Long-press the title to rename",
            color = TutorialTheme.blue,
            fontSize = 12.sp
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  STEP 4: Start Challenge
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StepStartChallenge() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        StepTitle("Start a Challenge")
        StepSubtitle("Listen, then speak backwards!")

        Spacer(modifier = Modifier.height(24.dp))

        // Mock card with Try highlighted
        MockRecordingCard(
            name = "Hello world",
            showHotspots = false,
            highlightTry = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            StepBullet("1️⃣", "Tap Rewind to hear the reversed audio")
            StepBullet("2️⃣", "Tap Try to start recording your attempt")
            StepBullet("3️⃣", "Speak what you heard backwards!")
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  STEP 5: Making Attempt
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StepMakingAttempt() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        StepTitle("Making an Attempt")
        StepSubtitle("Recording your backwards speech")

        Spacer(modifier = Modifier.height(24.dp))

        // Mock card in attempt mode
        MockRecordingCard(
            name = "Hello world",
            showHotspots = false,
            isAttempting = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        CalloutBox(
            text = "The Try button becomes Stop while recording.\nTap it when you're done speaking."
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  STEP 6: Attempt Card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StepAttemptCard() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        StepTitle("Attempt Card")
        StepSubtitle("Your attempt appears below the recording")

        Spacer(modifier = Modifier.height(20.dp))

        // Mock attempt card
        MockAttemptCard(showHotspots = true)

        Spacer(modifier = Modifier.height(20.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            LegendItem(icon = Icons.Default.Home, label = "Home", description = "Jump to parent recording")
            LegendItem(icon = Icons.Default.PlayArrow, label = "Play", description = "Play your attempt")
            LegendItem(icon = Icons.Default.Replay, label = "Rev", description = "Play attempt reversed")
            Text(
                text = "🏆 Tap the score badge to see detailed breakdown",
                color = TutorialTheme.hotPink,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  STEP 7: Scorecard
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StepScorecard() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        StepTitle("Scorecard")
        StepSubtitle("See how well you matched")

        Spacer(modifier = Modifier.height(12.dp))

        // Mock scorecard
        MockScorecard()

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            StepBullet("🎯", "Target: What you should have said")
            StepBullet("🎤", "Attempt: What you actually said")
            StepBullet("🔤", "Phonemes: Sound-by-sound breakdown")
            StepBullet("✏️", "Override: Manually adjust score if needed")
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  STEP 8: Menu Overview
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StepMenuOverview() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        StepTitle("The Menu")
        StepSubtitle("Access more features")

        Spacer(modifier = Modifier.height(16.dp))

        // Mock menu icon with pulse
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(TutorialTheme.cardSurface)
                .padding(16.dp)
        ) {
            PulsingHotspot(size = 48.dp) {
                Icon(
                    Icons.Default.Menu,
                    contentDescription = null,
                    tint = TutorialTheme.white,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Tap the menu icon in the top-left",
                color = TutorialTheme.white,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Mock menu items
        MockMenuList()
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  STEP 9: Files Menu
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StepFilesMenu() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        StepTitle("Files")
        StepSubtitle("Backup & Restore")

        Spacer(modifier = Modifier.height(24.dp))

        MockMenuItem(
            icon = Icons.Default.FolderOpen,
            label = "Files",
            isHighlighted = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            StepBullet("📦", "Backup: Export all recordings & attempts to .zip")
            StepBullet("📥", "Restore: Import a backup, skip duplicates")
            StepBullet("🎵", "Add Recording: Import external .wav files")
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  STEP 10: Themes Menu
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StepThemesMenu() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        StepTitle("Themes")
        StepSubtitle("Make it your own")

        Spacer(modifier = Modifier.height(24.dp))

        MockMenuItem(
            icon = Icons.Default.AutoAwesome,
            label = "Themes",
            isHighlighted = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Theme grid preview
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThemeChip("⚡", "Y2K Cyber")
                ThemeChip("⚙️", "Steampunk")
                ThemeChip("🌆", "Cyberpunk")
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThemeChip("📒", "Scrapbook")
                ThemeChip("🌴", "Vaporwave")
                ThemeChip("✨", "PRO themes")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Each theme changes colors, cards & animations!",
            color = TutorialTheme.white.copy(alpha = 0.8f),
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  STEP 11: Settings Menu
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StepSettingsMenu() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        StepTitle("Settings")
        StepSubtitle("Configure your experience")

        Spacer(modifier = Modifier.height(24.dp))

        MockMenuItem(
            icon = Icons.Default.Settings,
            label = "Settings",
            isHighlighted = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Difficulty highlight
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(TutorialTheme.cardSurface)
                    .border(2.dp, TutorialTheme.hotPink, RoundedCornerShape(12.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🎯", fontSize = 24.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Difficulty",
                        color = TutorialTheme.hotPink,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Easy • Normal • Hard",
                        color = TutorialTheme.white.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }

            StepBullet("🌙", "Dark mode preferences")
            StepBullet("🔊", "Audio settings")
            StepBullet("🎮", "Game mode toggle")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Mock difficulty indicator
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Tap the difficulty badge →",
                color = TutorialTheme.white.copy(alpha = 0.6f),
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            MockDifficultyBadge()
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  STEP 12: Complete
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StepComplete() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        // Celebration emoji
        FloatingEmoji(emoji = "🎉", size = 80.sp)

        Spacer(modifier = Modifier.height(24.dp))

        GradientText(
            text = "You're Ready!",
            fontSize = 36.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Time to challenge yourself",
            color = TutorialTheme.blue,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(32.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Quick tips:",
                color = TutorialTheme.white,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text("• Short phrases work best", color = TutorialTheme.white.copy(alpha = 0.8f), fontSize = 13.sp)
            Text("• Speak clearly and at normal speed", color = TutorialTheme.white.copy(alpha = 0.8f), fontSize = 13.sp)
            Text("• Make unlimited attempts to improve!", color = TutorialTheme.white.copy(alpha = 0.8f), fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Tap Done to start playing",
            color = TutorialTheme.hotPink,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
//  MOCK UI COMPONENTS
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun MockHomeScreen(
    showRecording: Boolean = false,
    isRecording: Boolean = false,
    highlightRecordButton: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(TutorialTheme.cardSurface)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top bar mock
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Menu,
                contentDescription = null,
                tint = TutorialTheme.white.copy(alpha = 0.6f),
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "ReVerseY",
                color = TutorialTheme.white,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            MockDifficultyBadge()
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Record button
        if (highlightRecordButton) {
            PulsingHotspot(size = 80.dp) {
                MockRecordButton(isRecording = isRecording)
            }
        } else {
            MockRecordButton(isRecording = isRecording)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Status text or waveform
        if (isRecording) {
            // Mock waveform
            Row(
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                modifier = Modifier.height(40.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(15) { index ->
                    val infiniteTransition = rememberInfiniteTransition(label = "wave$index")
                    val height by infiniteTransition.animateFloat(
                        initialValue = 10f,
                        targetValue = 35f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(300 + index * 50, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "waveHeight$index"
                    )
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(height.dp)
                            .background(
                                TutorialTheme.hotPink.copy(alpha = 0.8f),
                                RoundedCornerShape(2.dp)
                            )
                    )
                }
            }
        } else if (!showRecording) {
            Text(
                text = "Tap to record",
                color = TutorialTheme.white.copy(alpha = 0.5f),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun MockRecordButton(isRecording: Boolean = false) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .background(
                brush = if (isRecording)
                    Brush.radialGradient(listOf(TutorialTheme.hardRed, TutorialTheme.hardRed.copy(alpha = 0.7f)))
                else
                    TutorialTheme.primaryGradient,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
            contentDescription = null,
            tint = TutorialTheme.white,
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
private fun MockDifficultyBadge() {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(TutorialTheme.normalYellow.copy(alpha = 0.6f))
            .border(1.dp, TutorialTheme.normalYellow, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text("🎯", fontSize = 12.sp)
        Text(
            text = "Normal",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = TutorialTheme.black.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun MockRecordingCard(
    name: String,
    showHotspots: Boolean = false,
    highlightTry: Boolean = false,
    isAttempting: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = TutorialTheme.cardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Title
            Text(
                text = name,
                color = TutorialTheme.white,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { 0f },
                modifier = Modifier.fillMaxWidth(),
                color = TutorialTheme.hotPink,
                trackColor = TutorialTheme.white.copy(alpha = 0.2f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                if (showHotspots) {
                    MockCardButton(Icons.Default.Share, "Share", showPulse = true)
                    MockCardButton(Icons.Default.PlayArrow, "Play", showPulse = true)
                    MockCardButton(Icons.Default.Replay, "Rewind", showPulse = true)
                    MockCardButton(Icons.Default.Mic, "Try", showPulse = true)
                    MockCardButton(Icons.Default.Delete, "Del", showPulse = true)
                } else if (highlightTry) {
                    MockCardButton(Icons.Default.Share, "Share")
                    MockCardButton(Icons.Default.PlayArrow, "Play")
                    MockCardButton(Icons.Default.Replay, "Rewind")
                    MockCardButton(Icons.Default.Mic, "Try", isHighlighted = true, showPulse = true)
                    MockCardButton(Icons.Default.Delete, "Del")
                } else if (isAttempting) {
                    MockCardButton(Icons.Default.Share, "Share")
                    MockCardButton(Icons.Default.PlayArrow, "Play")
                    MockCardButton(Icons.Default.Replay, "Rewind")
                    MockCardButton(
                        Icons.Default.Stop,
                        "Stop",
                        isHighlighted = true,
                        highlightColor = TutorialTheme.hardRed,
                        showPulse = true
                    )
                    MockCardButton(Icons.Default.Delete, "Del")
                } else {
                    MockCardButton(Icons.Default.Share, "Share")
                    MockCardButton(Icons.Default.PlayArrow, "Play")
                    MockCardButton(Icons.Default.Replay, "Rewind")
                    MockCardButton(Icons.Default.Mic, "Try")
                    MockCardButton(Icons.Default.Delete, "Del")
                }
            }
        }
    }
}

@Composable
private fun MockCardButton(
    icon: ImageVector,
    label: String,
    isHighlighted: Boolean = false,
    highlightColor: Color = TutorialTheme.hotPink,
    showPulse: Boolean = false
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (showPulse) {
            PulsingHotspot(size = 44.dp) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            if (isHighlighted) highlightColor else TutorialTheme.purple,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = TutorialTheme.white, modifier = Modifier.size(20.dp))
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (isHighlighted) highlightColor else TutorialTheme.purple,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = TutorialTheme.white, modifier = Modifier.size(20.dp))
            }
        }
        Text(
            text = label,
            color = TutorialTheme.white.copy(alpha = 0.8f),
            fontSize = 10.sp
        )
    }
}

@Composable
private fun MockAttemptCard(
    playerName: String = "Player 1",
    showHotspots: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = TutorialTheme.cardSurface.copy(alpha = 0.8f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Left: Controls
            Column(modifier = Modifier.weight(1f)) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (showHotspots) {
                        PulsingHotspot(size = 28.dp) {
                            Icon(
                                Icons.Default.Home,
                                null,
                                tint = TutorialTheme.hotPink,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else {
                        Icon(
                            Icons.Default.Home,
                            null,
                            tint = TutorialTheme.hotPink,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = playerName,
                        color = TutorialTheme.white,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Buttons
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MockCardButton(Icons.Default.Share, "Share", showPulse = showHotspots)
                    MockCardButton(Icons.Default.PlayArrow, "Play", showPulse = showHotspots)
                    MockCardButton(Icons.Default.Replay, "Rev", showPulse = showHotspots)
                    MockCardButton(Icons.Default.Delete, "Del", showPulse = showHotspots)
                }
            }

            // Right: Score badge (no pulse - it constrains the squircle)
            MockSquircleBadge()
        }
    }
}

@Composable
private fun MockSquircleBadge(
    width: Dp = 77.dp,
    height: Dp = 115.dp
) {
    // Fixed to Normal difficulty (yellow) for tutorial
    val color = TutorialTheme.normalYellow
    val textColor = Color.Black

    Box(
        modifier = Modifier
            .width(width)
            .height(height),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cornerRadius = size.minDimension * 0.35f
            val strokeWidth = 5.dp.toPx()

            // Background fill
            drawRoundRect(
                color = color.copy(alpha = 0.4f),
                size = size,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius)
            )

            // Gray track
            drawRoundRect(
                color = Color.Gray.copy(alpha = 0.3f),
                size = size,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius),
                style = Stroke(width = strokeWidth)
            )

            // Progress border
            val rect = androidx.compose.ui.geometry.Rect(
                left = strokeWidth / 2,
                top = strokeWidth / 2,
                right = size.width - strokeWidth / 2,
                bottom = size.height - strokeWidth / 2
            )
            val perimeter = 2 * (rect.width + rect.height - 4 * cornerRadius) + 2 * Math.PI.toFloat() * cornerRadius
            val progressLength = (78 / 100f) * perimeter

            val progressPath = Path().apply {
                addRoundRect(
                    androidx.compose.ui.geometry.RoundRect(
                        rect = rect,
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius)
                    )
                )
            }

            drawPath(
                path = progressPath,
                color = color,
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(
                        intervals = floatArrayOf(progressLength, perimeter - progressLength),
                        phase = perimeter * 0.18f
                    )
                )
            )
        }

        // Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "⚙️ 👍", fontSize = 16.sp)
            Text(
                text = "78%",
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp
            )
            Text(
                text = "NORMAL",
                color = textColor.copy(alpha = 0.7f),
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MockScorecard() {
    val targetColor = Color(0xFFFBBF24)  // Yellow/gold
    val attemptColor = Color(0xFF60A5FA)  // Blue

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = TutorialTheme.cardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Header
            Text(
                text = "🎯 Score Breakdown",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TutorialTheme.hotPink
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Score Display
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(TutorialTheme.surface)
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "78",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = TutorialTheme.easyGreen
                    )
                    Text(
                        text = "6/8 phonemes matched",
                        fontSize = 10.sp,
                        color = TutorialTheme.white.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "tap for formula",
                        fontSize = 9.sp,
                        color = TutorialTheme.hotPink.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Target Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(TutorialTheme.surface.copy(alpha = 0.5f))
                    .padding(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🎯", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "TARGET (Reversed Phonemes of...)",
                        fontSize = 10.sp,
                        color = TutorialTheme.white.copy(alpha = 0.6f)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Transcription box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(TutorialTheme.cardSurface)
                        .border(2.dp, targetColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "\"dlrow olleh\"",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = targetColor
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Word-grouped phonemes
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // "dlrow" word group
                    MockWordPhonemeGroup(
                        word = "dlrow",
                        phonemes = listOf("D" to true, "L" to true, "R" to true, "OW" to true),
                        isTarget = true
                    )
                    // "olleh" word group
                    MockWordPhonemeGroup(
                        word = "olleh",
                        phonemes = listOf("AA" to true, "L" to true, "EH" to false, "HH" to false),
                        isTarget = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Attempt Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(TutorialTheme.surface.copy(alpha = 0.5f))
                    .padding(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🎤", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "YOU SAID",
                        fontSize = 10.sp,
                        color = TutorialTheme.white.copy(alpha = 0.6f)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Transcription box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(TutorialTheme.cardSurface)
                        .border(2.dp, attemptColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "\"derlo oleh\"",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = attemptColor
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Word-grouped phonemes (all blue)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MockWordPhonemeGroup(
                        word = "derlo",
                        phonemes = listOf("D" to true, "ER" to true, "L" to true, "OW" to true),
                        isTarget = false
                    )
                    MockWordPhonemeGroup(
                        word = "oleh",
                        phonemes = listOf("OW" to true, "L" to true, "EH" to true, "HH" to true),
                        isTarget = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Breakdown rows
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                MockBreakdownRow(icon = "🔤", label = "Phoneme Match", value = "78%", isGood = true)
                MockBreakdownRow(icon = "⏱️", label = "Duration", value = "1.1x", isGood = true)
                MockBreakdownRow(icon = "🎚️", label = "Difficulty", value = "NORMAL", isGood = null)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Accept button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(TutorialTheme.easyGreen)
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✓ ACCEPT",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = Color(0xFF052E16)
                    )
                }

                // Override button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(TutorialTheme.surface)
                        .border(2.dp, TutorialTheme.hotPink, RoundedCornerShape(8.dp))
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "OVERRIDE",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = TutorialTheme.white
                    )
                }
            }
        }
    }
}

@Composable
private fun MockWordPhonemeGroup(
    word: String,
    phonemes: List<Pair<String, Boolean>>,
    isTarget: Boolean
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            phonemes.forEach { (phoneme, isMatched) ->
                MockPhonemeChip(
                    phoneme = phoneme,
                    isMatched = isMatched,
                    isTarget = isTarget
                )
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = word,
            fontSize = 9.sp,
            color = TutorialTheme.white.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun MockPhonemeChip(
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
        isMatched -> TutorialTheme.easyGreen
        else -> TutorialTheme.hardRed
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .padding(horizontal = 5.dp, vertical = 3.dp)
    ) {
        Text(
            text = phoneme,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}

@Composable
private fun MockBreakdownRow(
    icon: String,
    label: String,
    value: String,
    isGood: Boolean?
) {
    val valueColor = when (isGood) {
        true -> TutorialTheme.easyGreen
        false -> TutorialTheme.hardRed
        null -> TutorialTheme.normalYellow
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(TutorialTheme.surface.copy(alpha = 0.3f))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = icon, fontSize = 14.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                color = TutorialTheme.white
            )
        }

        Text(
            text = value + if (isGood == true) " ✓" else "",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = valueColor
        )
    }
}

@Composable
private fun MockMenuList() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        MockMenuItem(Icons.Default.Home, "Home")
        MockMenuItem(Icons.Default.FolderOpen, "Files")
        MockMenuItem(Icons.Default.Settings, "Settings")
        MockMenuItem(Icons.Default.AutoAwesome, "Themes")
        MockMenuItem(Icons.Default.Info, "About")
        MockMenuItem(Icons.Default.Lightbulb, "Tutorial")
        MockMenuItem(Icons.Default.HelpOutline, "Help")
    }
}

@Composable
private fun MockMenuItem(
    icon: ImageVector,
    label: String,
    isHighlighted: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isHighlighted) TutorialTheme.hotPink.copy(alpha = 0.2f)
                else TutorialTheme.cardSurface
            )
            .then(
                if (isHighlighted)
                    Modifier.border(2.dp, TutorialTheme.hotPink, RoundedCornerShape(12.dp))
                else Modifier
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (isHighlighted) {
            PulsingHotspot(size = 32.dp) {
                Icon(icon, null, tint = TutorialTheme.hotPink, modifier = Modifier.size(24.dp))
            }
        } else {
            Icon(icon, null, tint = TutorialTheme.white.copy(alpha = 0.7f), modifier = Modifier.size(24.dp))
        }
        Text(
            text = label,
            color = if (isHighlighted) TutorialTheme.hotPink else TutorialTheme.white,
            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Medium,
            fontSize = 15.sp
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
//  REUSABLE TUTORIAL COMPONENTS
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun StepTitle(text: String) {
    GradientText(text = text, fontSize = 28.sp)
}

@Composable
private fun StepSubtitle(text: String) {
    Text(
        text = text,
        color = TutorialTheme.blue,
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium
    )
}

@Composable
private fun GradientText(text: String, fontSize: androidx.compose.ui.unit.TextUnit) {
    Text(
        text = text,
        fontSize = fontSize,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        style = androidx.compose.ui.text.TextStyle(
            brush = TutorialTheme.horizontalGradient
        )
    )
}

@Composable
private fun FloatingEmoji(emoji: String, size: androidx.compose.ui.unit.TextUnit) {
    val infiniteTransition = rememberInfiniteTransition(label = "float")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -12f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatY"
    )

    Text(
        text = emoji,
        fontSize = size,
        modifier = Modifier.offset(y = offsetY.dp)
    )
}

@Composable
private fun PulsingHotspot(
    size: Dp,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Pulsing ring
        Box(
            modifier = Modifier
                .size(size)
                .scale(scale)
                .background(
                    TutorialTheme.hotPink.copy(alpha = alpha),
                    CircleShape
                )
        )
        // Content
        content()
    }
}

@Composable
private fun CalloutBox(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(TutorialTheme.blue.copy(alpha = 0.15f))
            .border(1.dp, TutorialTheme.blue.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text(
            text = text,
            color = TutorialTheme.white,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun StepBullet(emoji: String, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(emoji, fontSize = 20.sp, modifier = Modifier.width(32.dp))
        Text(
            text = text,
            color = TutorialTheme.white,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun LegendItem(
    icon: ImageVector,
    label: String,
    description: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(TutorialTheme.surface.copy(alpha = 0.5f))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = TutorialTheme.purple,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            color = TutorialTheme.hotPink,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            modifier = Modifier.width(60.dp)
        )
        Text(
            text = description,
            color = TutorialTheme.white.copy(alpha = 0.8f),
            fontSize = 12.sp
        )
    }
}

@Composable
private fun ThemeChip(emoji: String, name: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(TutorialTheme.surface)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(emoji, fontSize = 16.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = name,
            color = TutorialTheme.white,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}