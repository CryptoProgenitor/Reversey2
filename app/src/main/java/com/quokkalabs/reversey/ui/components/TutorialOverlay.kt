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
            TutorialSlide(
                id = 1,
                title = "Gaming Mode",
                subtitle = "Challenge Your Friends!",
                icon = "🎮"
            ) {
                GameModeContent()
            },
            TutorialSlide(
                id = 2,
                title = "Here's an Example",
                subtitle = "",
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
fun GameModeContent() {
    Column(
        modifier = Modifier.Companion
            .fillMaxWidth()
            .border(
                width = 2.dp,
                brush = TutorialDefaults.magentaBrush,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
            )
            .background(
                color = TutorialDefaults.magenta.copy(alpha = 0.1f),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
            )
            .padding(25.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        GameStep("1️⃣", "Record anything you want")
        GameStep("2️⃣", "It gets automatically reversed")
        GameStep("3️⃣", "Say it backwards so it sounds right!")
        GameStep("🏆", "Get scored on your accuracy")
    }
}

@Composable
fun GameStep(icon: String, text: String) {
    Row(
        verticalAlignment = Alignment.Companion.CenterVertically,
        modifier = Modifier.Companion.fillMaxWidth()
    ) {
        Text(
            text = icon,
            fontSize = 24.sp,
            modifier = Modifier.Companion
                .width(40.dp)
                .padding(end = 12.dp)
        )
        Text(
            text = text,
            fontSize = 15.sp,
            color = TutorialDefaults.white,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

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
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(15.dp)
                )
                .background(
                    color = TutorialDefaults.cyan.copy(alpha = 0.1f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(15.dp)
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
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
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
fun ExampleText(label: String, value: String) {
    Row(
        modifier = Modifier.Companion
            .fillMaxWidth()
            .background(
                color = TutorialDefaults.black.copy(alpha = 0.3f),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
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