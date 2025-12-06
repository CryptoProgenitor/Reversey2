package com.quokkalabs.reversey.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
// Centralised styling for consistency
private object TutorialDefaults {
    val magenta = Color(0xFFFF00FF)
    val cyan = Color(0xFF00FFFF)
    val white = Color.Companion.White
    val black = Color.Companion.Black

    val primaryGradient = Brush.Companion.horizontalGradient(listOf(magenta, cyan))
    val titleGradient = Brush.Companion.linearGradient(listOf(magenta, cyan, magenta))
    val cyanBrush = Brush.Companion.linearGradient(listOf(cyan, cyan))
    val magentaBrush = Brush.Companion.linearGradient(listOf(magenta, magenta))
}

// Tutorial Step Data Class
data class TutorialSlide(
    val id: Int,
    val title: String,
    val subtitle: String = "",
    val icon: String,
    val content: @Composable () -> Unit
)

@Composable
fun TutorialOverlay(
    onDismiss: () -> Unit,
    onComplete: () -> Unit
) {
    var currentSlide by remember { mutableStateOf(0) }

    val slides = remember {
        listOf(
            // SLIDE 0: Welcome
            TutorialSlide(
                id = 0,
                title = "Welcome to\nReVerseY!",
                subtitle = "Record. Reverse. Challenge.",
                icon = "⚡"
            ) {
                Text(
                    text = "Your voice backwards has\nnever been this fun!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TutorialDefaults.white.copy(alpha = 0.9f),
                    textAlign = TextAlign.Companion.Center
                )
            },

            // SLIDE 1: Recording Basics
            TutorialSlide(
                id = 1,
                title = "Recordings",
                subtitle = "The Heart of the Game",
                icon = "🎙️"
            ) {
                RecordingsContent()
            },

            // SLIDE 2: Speech vs Singing
            TutorialSlide(
                id = 2,
                title = "Speech vs Singing",
                subtitle = "Smart Detection",
                icon = "🎤"
            ) {
                VocalModeContent()
            },

            // SLIDE 3: Making Attempts
            TutorialSlide(
                id = 3,
                title = "Attempts",
                subtitle = "Challenge Yourself!",
                icon = "🎯"
            ) {
                AttemptsContent()
            },

            // SLIDE 4: Scorecards
            TutorialSlide(
                id = 4,
                title = "Scorecards",
                subtitle = "Track Your Performance",
                icon = "📊"
            ) {
                ScorecardContent()
            },

            // SLIDE 5: File Management
            TutorialSlide(
                id = 5,
                title = "Backup & Restore",
                subtitle = "Protect Your Progress",
                icon = "💾"
            ) {
                FileManagementContent()
            },

            // SLIDE 6: Themes
            TutorialSlide(
                id = 6,
                title = "Themes",
                subtitle = "Make It Your Own",
                icon = "🎨"
            ) {
                ThemesContent()
            },

            // SLIDE 7: Tips & Example
            TutorialSlide(
                id = 7,
                title = "Quick Example",
                subtitle = "Here's How It Works",
                icon = ""
            ) {
                ExampleContent()
            }
        )
    }

    // Total slides is now derived from the list, making it more robust.
    val totalSlides = slides.size

    Box(
        modifier = Modifier.Companion
            .fillMaxSize()
            .background(TutorialDefaults.black.copy(alpha = 0.92f))
    ) {
        Column(
            modifier = Modifier.Companion
                .fillMaxSize()
                .padding(horizontal = 30.dp)
                .padding(top = 60.dp, bottom = 100.dp),
            horizontalAlignment = Alignment.Companion.CenterHorizontally
        ) {
            // Carousel Container
            Box(
                modifier = Modifier.Companion
                    .weight(1f) // This pushes the controls group down
                    .fillMaxWidth(),
                contentAlignment = Alignment.Companion.Center
            ) {
                AnimatedContent(
                    targetState = currentSlide,
                    transitionSpec = {
                        slideInHorizontally(
                            initialOffsetX = { it },
                            animationSpec = tween(400)
                        ) + fadeIn(animationSpec = tween(400)) togetherWith
                                slideOutHorizontally(
                                    targetOffsetX = { -it },
                                    animationSpec = tween(400)
                                ) + fadeOut(animationSpec = tween(400))
                    },
                    label = "slide_transition"
                ) { slideIndex ->
                    SlideContent(slide = slides[slideIndex])
                }
            }

            // Group for navigation dots and buttons
            Column(
                horizontalAlignment = Alignment.Companion.CenterHorizontally,
                modifier = Modifier.Companion.fillMaxWidth()
            ) {
                // Navigation Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.Companion.padding(vertical = 20.dp)
                ) {
                    repeat(totalSlides) { index ->
                        NavigationDot(
                            isActive = index == currentSlide,
                            onClick = { currentSlide = index }
                        )
                    }
                }

                // Button Group
                Row(
                    horizontalArrangement = Arrangement.spacedBy(15.dp),
                    modifier = Modifier.Companion.fillMaxWidth(),
                    verticalAlignment = Alignment.Companion.CenterVertically
                ) {
                    // Skip Button
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.Companion.weight(1f),
                        shape = RoundedCornerShape(30.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = TutorialDefaults.cyan
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            width = 2.dp,
                            brush = TutorialDefaults.cyanBrush
                        )
                    ) {
                        Text(
                            "SKIP",
                            fontWeight = FontWeight.Companion.Bold,
                            letterSpacing = 1.sp,
                            modifier = Modifier.Companion.padding(vertical = 6.dp)
                        )
                    }

                    // Next/Start Button
                    Button(
                        onClick = {
                            if (currentSlide < totalSlides - 1) {
                                currentSlide++
                            } else {
                                onComplete()
                            }
                        },
                        modifier = Modifier.Companion
                            .weight(1f)
                            .shadow(
                                elevation = 20.dp,
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(30.dp),
                                spotColor = TutorialDefaults.magenta
                            ),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(30.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Companion.Transparent
                        ),
                        contentPadding = PaddingValues(0.dp) // Remove default padding
                    ) {
                        Box(
                            modifier = Modifier.Companion
                                .fillMaxWidth()
                                .background(
                                    brush = TutorialDefaults.primaryGradient,
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(30.dp)
                                )
                                .padding(vertical = 8.dp), // Adjust padding on inner box
                            contentAlignment = Alignment.Companion.Center
                        ) {
                            Text(
                                text = if (currentSlide == totalSlides - 1) "LET'S GO! 🚀" else "NEXT",
                                color = TutorialDefaults.black,
                                fontWeight = FontWeight.Companion.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SlideContent(slide: TutorialSlide) {
    Column(
        horizontalAlignment = Alignment.Companion.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.Companion.fillMaxWidth()
    ) {
        // Floating Icon
        if (slide.icon.isNotEmpty()) {
            val infiniteTransition = rememberInfiniteTransition(label = "float")
            val offsetY by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = -15f,
                animationSpec = infiniteRepeatable(
                    animation = tween(3000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "float_animation"
            )

            Text(
                text = slide.icon,
                fontSize = 80.sp,
                modifier = Modifier.Companion
                    .offset(y = offsetY.dp)
                    .padding(bottom = 20.dp)
            )
        }

        // Neon Title with Gradient
        Text(
            text = slide.title,
            fontSize = 36.sp,
            fontWeight = FontWeight.Companion.Bold,
            textAlign = TextAlign.Companion.Center,
            style = MaterialTheme.typography.headlineLarge.copy(
                brush = TutorialDefaults.titleGradient
            ),
            modifier = Modifier.Companion.padding(bottom = 10.dp)
        )

        // Subtitle
        if (slide.subtitle.isNotEmpty()) {
            Text(
                text = slide.subtitle,
                fontSize = 18.sp,
                color = TutorialDefaults.cyan,
                textAlign = TextAlign.Companion.Center,
                modifier = Modifier.Companion.padding(bottom = 30.dp)
            )
        }

        // Content
        slide.content()
    }
}

@Composable
fun NavigationDot(
    isActive: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isActive) 1.3f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "dot_scale"
    )

    Box(
        modifier = Modifier.size(10.dp * scale)
            .background(
                color = if (isActive) TutorialDefaults.magenta else TutorialDefaults.white.copy(
                    alpha = 0.3f
                ),
                shape = CircleShape
            )
            .border(
                width = 2.dp,
                color = TutorialDefaults.magenta.copy(alpha = 0.5f),
                shape = CircleShape
            )
            .then(
                if (isActive) {
                    Modifier.Companion.shadow(
                        elevation = 15.dp,
                        shape = CircleShape,
                        spotColor = TutorialDefaults.magenta
                    )
                } else Modifier.Companion
            )
    )
}

// ============================================================
//  TUTORIAL CONTENT SECTIONS
// ============================================================

/**
 * SLIDE: Recordings - Explains how to create and manage recordings
 */
@Composable
fun RecordingsContent() {
    Column(
        modifier = Modifier.Companion
            .fillMaxWidth()
            .border(
                width = 2.dp,
                brush = TutorialDefaults.cyanBrush,
                shape = RoundedCornerShape(20.dp)
            )
            .background(
                color = TutorialDefaults.cyan.copy(alpha = 0.1f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        TutorialBullet("🎙️", "Tap the record button to capture audio")
        TutorialBullet("⏱️", "Recording auto-stops, or tap to end early")
        TutorialBullet("🔄", "Audio is instantly reversed for you")
        TutorialBullet("📝", "Long-press any recording to rename it")
        TutorialBullet("🗑️", "Swipe left on a recording to delete")
    }
}

/**
 * SLIDE: Speech vs Singing - Explains the dual pipeline
 */
@Composable
fun VocalModeContent() {
    Column(
        modifier = Modifier.Companion
            .fillMaxWidth()
            .border(
                width = 2.dp,
                brush = TutorialDefaults.primaryGradient,
                shape = RoundedCornerShape(20.dp)
            )
            .background(
                color = TutorialDefaults.magenta.copy(alpha = 0.1f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "ReVerseY auto-detects your vocal style:",
            style = MaterialTheme.typography.bodyMedium,
            color = TutorialDefaults.white.copy(alpha = 0.9f),
            modifier = Modifier.Companion.padding(bottom = 4.dp)
        )

        // Speech section
        Row(
            verticalAlignment = Alignment.Companion.CenterVertically,
            modifier = Modifier.Companion.fillMaxWidth()
        ) {
            Text("🗣️", fontSize = 28.sp)
            Spacer(modifier = Modifier.Companion.width(12.dp))
            Column {
                Text(
                    "Speech Mode",
                    fontWeight = FontWeight.Companion.Bold,
                    color = TutorialDefaults.cyan,
                    fontSize = 16.sp
                )
                Text(
                    "Focuses on rhythm & phonetics",
                    color = TutorialDefaults.white.copy(alpha = 0.7f),
                    fontSize = 13.sp
                )
            }
        }

        // Singing section
        Row(
            verticalAlignment = Alignment.Companion.CenterVertically,
            modifier = Modifier.Companion.fillMaxWidth()
        ) {
            Text("🎵", fontSize = 28.sp)
            Spacer(modifier = Modifier.Companion.width(12.dp))
            Column {
                Text(
                    "Singing Mode",
                    fontWeight = FontWeight.Companion.Bold,
                    color = TutorialDefaults.magenta,
                    fontSize = 16.sp
                )
                Text(
                    "Emphasizes pitch & melody",
                    color = TutorialDefaults.white.copy(alpha = 0.7f),
                    fontSize = 13.sp
                )
            }
        }

        Text(
            text = "💡 The scoring adapts to each mode!",
            style = MaterialTheme.typography.bodySmall,
            color = TutorialDefaults.cyan,
            modifier = Modifier.Companion.padding(top = 6.dp)
        )
    }
}

/**
 * SLIDE: Attempts - Explains how to make attempts and challenge recordings
 */
@Composable
fun AttemptsContent() {
    Column(
        modifier = Modifier.Companion
            .fillMaxWidth()
            .border(
                width = 2.dp,
                brush = TutorialDefaults.magentaBrush,
                shape = RoundedCornerShape(20.dp)
            )
            .background(
                color = TutorialDefaults.magenta.copy(alpha = 0.1f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TutorialBullet("▶️", "Tap a recording to select it")
        TutorialBullet("🔊", "Listen to the reversed audio")
        TutorialBullet("🎙️", "Hit record to make your attempt")
        TutorialBullet("🔁", "Try to say/sing it backwards!")
        TutorialBullet("📈", "Make unlimited attempts to improve")

        // Difficulty tip
        Box(
            modifier = Modifier.Companion
                .fillMaxWidth()
                .background(
                    color = TutorialDefaults.cyan.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(10.dp)
                )
                .padding(10.dp)
        ) {
            Text(
                text = "💡 Choose Easy, Normal, or Hard difficulty\nto adjust scoring sensitivity!",
                fontSize = 12.sp,
                color = TutorialDefaults.white.copy(alpha = 0.9f),
                textAlign = TextAlign.Companion.Center,
                modifier = Modifier.Companion.fillMaxWidth()
            )
        }
    }
}

/**
 * SLIDE: Scorecards - Explains the scoring breakdown
 */
@Composable
fun ScorecardContent() {
    Column(
        modifier = Modifier.Companion
            .fillMaxWidth()
            .border(
                width = 2.dp,
                brush = TutorialDefaults.cyanBrush,
                shape = RoundedCornerShape(20.dp)
            )
            .background(
                color = TutorialDefaults.cyan.copy(alpha = 0.1f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Tap any attempt to see detailed stats:",
            style = MaterialTheme.typography.bodyMedium,
            color = TutorialDefaults.white.copy(alpha = 0.9f),
            modifier = Modifier.Companion.padding(bottom = 4.dp)
        )

        ScoreMetricRow("🎼", "Pitch Similarity", "How close your notes match")
        ScoreMetricRow("🗣️", "Voice Matching", "Tonal & timber accuracy")
        ScoreMetricRow("⭐", "Overall Score", "Combined performance grade")

        Spacer(modifier = Modifier.Companion.height(6.dp))

        Text(
            text = "📋 Get personalized tips based on\nyour performance!",
            fontSize = 12.sp,
            color = TutorialDefaults.magenta,
            textAlign = TextAlign.Companion.Center,
            modifier = Modifier.Companion.fillMaxWidth()
        )
    }
}

@Composable
private fun ScoreMetricRow(emoji: String, title: String, description: String) {
    Row(
        verticalAlignment = Alignment.Companion.CenterVertically,
        modifier = Modifier.Companion
            .fillMaxWidth()
            .background(
                color = TutorialDefaults.black.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(10.dp)
    ) {
        Text(emoji, fontSize = 20.sp)
        Spacer(modifier = Modifier.Companion.width(10.dp))
        Column {
            Text(
                title,
                fontWeight = FontWeight.Companion.Bold,
                color = TutorialDefaults.cyan,
                fontSize = 14.sp
            )
            Text(
                description,
                color = TutorialDefaults.white.copy(alpha = 0.7f),
                fontSize = 11.sp
            )
        }
    }
}

/**
 * SLIDE: File Management - Backup & Restore
 */
@Composable
fun FileManagementContent() {
    Column(
        modifier = Modifier.Companion
            .fillMaxWidth()
            .border(
                width = 2.dp,
                brush = TutorialDefaults.primaryGradient,
                shape = RoundedCornerShape(20.dp)
            )
            .background(
                color = TutorialDefaults.magenta.copy(alpha = 0.1f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Backup section
        Row(
            verticalAlignment = Alignment.Companion.CenterVertically,
            modifier = Modifier.Companion.fillMaxWidth()
        ) {
            Text("📦", fontSize = 26.sp)
            Spacer(modifier = Modifier.Companion.width(12.dp))
            Column {
                Text(
                    "Back It Up",
                    fontWeight = FontWeight.Companion.Bold,
                    color = TutorialDefaults.cyan,
                    fontSize = 15.sp
                )
                Text(
                    "Export all recordings & attempts\nto a single .zip file",
                    color = TutorialDefaults.white.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }
        }

        // Restore section
        Row(
            verticalAlignment = Alignment.Companion.CenterVertically,
            modifier = Modifier.Companion.fillMaxWidth()
        ) {
            Text("📥", fontSize = 26.sp)
            Spacer(modifier = Modifier.Companion.width(12.dp))
            Column {
                Text(
                    "Restore",
                    fontWeight = FontWeight.Companion.Bold,
                    color = TutorialDefaults.magenta,
                    fontSize = 15.sp
                )
                Text(
                    "Import a backup with the wizard\nSkip duplicates or merge attempts",
                    color = TutorialDefaults.white.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }
        }

        // Add recording tip
        Box(
            modifier = Modifier.Companion
                .fillMaxWidth()
                .background(
                    color = TutorialDefaults.cyan.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(10.dp)
                )
                .padding(10.dp)
        ) {
            Text(
                text = "🎵 Import .wav files directly from\nMenu → Files → Add Recording",
                fontSize = 12.sp,
                color = TutorialDefaults.white.copy(alpha = 0.9f),
                textAlign = TextAlign.Companion.Center,
                modifier = Modifier.Companion.fillMaxWidth()
            )
        }
    }
}

/**
 * SLIDE: Themes - Visual customization options
 */
@Composable
fun ThemesContent() {
    Column(
        modifier = Modifier.Companion
            .fillMaxWidth()
            .border(
                width = 2.dp,
                brush = TutorialDefaults.primaryGradient,
                shape = RoundedCornerShape(20.dp)
            )
            .background(
                color = TutorialDefaults.magenta.copy(alpha = 0.1f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Choose from stunning visual styles:",
            style = MaterialTheme.typography.bodyMedium,
            color = TutorialDefaults.white.copy(alpha = 0.9f),
            modifier = Modifier.Companion.padding(bottom = 4.dp)
        )

        // Theme grid - 2 columns
        Row(
            modifier = Modifier.Companion.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(
                modifier = Modifier.Companion.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThemeChip("💿", "Y2K Cyber")
                ThemeChip("⚙️", "Steampunk")
                ThemeChip("✏️", "Graphite Sketch")
            }
            Column(
                modifier = Modifier.Companion.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThemeChip("📒", "Scrapbook")
                ThemeChip("🌆", "Cyberpunk")
                ThemeChip("🌴", "Vaporwave")
            }
        }

        Spacer(modifier = Modifier.Companion.height(4.dp))

        // Access tip
        Box(
            modifier = Modifier.Companion
                .fillMaxWidth()
                .background(
                    color = TutorialDefaults.cyan.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(10.dp)
                )
                .padding(10.dp)
        ) {
            Text(
                text = "🎭 Access themes from Menu → Themes\nEach theme changes colors, cards & animations!",
                fontSize = 12.sp,
                color = TutorialDefaults.white.copy(alpha = 0.9f),
                textAlign = TextAlign.Companion.Center,
                modifier = Modifier.Companion.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ThemeChip(emoji: String, name: String) {
    Row(
        verticalAlignment = Alignment.Companion.CenterVertically,
        modifier = Modifier.Companion
            .fillMaxWidth()
            .background(
                color = TutorialDefaults.black.copy(alpha = 0.3f),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(emoji, fontSize = 18.sp)
        Spacer(modifier = Modifier.Companion.width(8.dp))
        Text(
            name,
            color = TutorialDefaults.white,
            fontSize = 12.sp,
            fontWeight = FontWeight.Companion.Medium
        )
    }
}

/**
 * SLIDE: Quick Example - Shows the game flow
 */
@Composable
fun ExampleContent() {
    Column(
        modifier = Modifier.Companion.fillMaxWidth(),
        horizontalAlignment = Alignment.Companion.CenterHorizontally
    ) {
        // Example Box
        Column(
            modifier = Modifier.Companion
                .fillMaxWidth()
                .border(
                    width = 2.dp,
                    color = TutorialDefaults.cyan,
                    shape = RoundedCornerShape(15.dp)
                )
                .background(
                    color = TutorialDefaults.cyan.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(15.dp)
                )
                .padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ExampleText("🎤 Record:", "\"Hello world!\"")
            ExampleText("🔄 Reversed:", "\"!dlrow olleH\"")
            ExampleText("🎯 Challenge:", "Say it backwards\nto match the original")

            // Score Badge
            Box(
                modifier = Modifier.Companion
                    .align(Alignment.Companion.CenterHorizontally)
                    .padding(top = 10.dp)
                    .background(
                        brush = TutorialDefaults.primaryGradient,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Your Score: 87% 🌟",
                    color = TutorialDefaults.black,
                    fontWeight = FontWeight.Companion.Bold,
                    fontSize = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.Companion.height(25.dp))

        Text(
            text = "The better your backwards attempt,\nthe higher your score!",
            fontSize = 14.sp,
            color = TutorialDefaults.white.copy(alpha = 0.9f),
            textAlign = TextAlign.Companion.Center,
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun ExampleText(label: String, value: String) {
    Row(
        modifier = Modifier.Companion
            .fillMaxWidth()
            .background(
                color = TutorialDefaults.black.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp)
    ) {
        Text(
            text = "$label ",
            color = TutorialDefaults.cyan,
            fontWeight = FontWeight.Companion.Bold,
            fontSize = 14.sp
        )
        Text(
            text = value,
            color = TutorialDefaults.white,
            fontSize = 14.sp
        )
    }
}

/**
 * Reusable bullet point for tutorial slides
 */
@Composable
private fun TutorialBullet(emoji: String, text: String) {
    Row(
        verticalAlignment = Alignment.Companion.CenterVertically,
        modifier = Modifier.Companion.fillMaxWidth()
    ) {
        Text(
            text = emoji,
            fontSize = 22.sp,
            modifier = Modifier.Companion.width(36.dp)
        )
        Text(
            text = text,
            fontSize = 14.sp,
            color = TutorialDefaults.white,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}