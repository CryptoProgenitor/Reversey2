// TutorialOverlay.kt
package com.example.reversey

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
    val totalSlides = 3

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
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.92f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 30.dp)
                .padding(top = 60.dp, bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Carousel Container
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
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

            // Navigation Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(vertical = 20.dp)
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
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Skip Button
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(30.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF00FFFF)
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        width = 2.dp,
                        brush = Brush.linearGradient(
                            listOf(Color(0xFF00FFFF), Color(0xFF00FFFF))
                        )
                    )
                ) {
                    Text(
                        "SKIP",
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(vertical = 6.dp)
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
                    modifier = Modifier
                        .weight(1f)
                        .shadow(
                            elevation = 20.dp,
                            shape = RoundedCornerShape(30.dp),
                            spotColor = Color(0xFFFF00FF)
                        ),
                    shape = RoundedCornerShape(30.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.horizontalGradient(
                                    listOf(Color(0xFFFF00FF), Color(0xFF00FFFF))
                                ),
                                shape = RoundedCornerShape(30.dp)
                            )
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (currentSlide == totalSlides - 1) "LET'S GO! 🚀" else "NEXT",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SlideContent(slide: TutorialSlide) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
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
                modifier = Modifier
                    .offset(y = offsetY.dp)
                    .padding(bottom = 20.dp)
            )
        }

        // Neon Title with Gradient
        Text(
            text = slide.title,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineLarge.copy(
                brush = Brush.linearGradient(
                    listOf(
                        Color(0xFFFF00FF),
                        Color(0xFF00FFFF),
                        Color(0xFFFF00FF)
                    )
                )
            ),
            modifier = Modifier.padding(bottom = 10.dp)
        )

        // Subtitle
        if (slide.subtitle.isNotEmpty()) {
            Text(
                text = slide.subtitle,
                fontSize = 18.sp,
                color = Color(0xFF00FFFF),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 30.dp)
            )
        }

        // Content
        slide.content()
    }
}

@Composable
fun GameModeContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(
                    listOf(Color(0xFFFF00FF), Color(0xFFFF00FF))
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .background(
                color = Color(0xFFFF00FF).copy(alpha = 0.1f),
                shape = RoundedCornerShape(20.dp)
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
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = icon,
            fontSize = 24.sp,
            modifier = Modifier
                .width(40.dp)
                .padding(end = 12.dp)
        )
        Text(
            text = text,
            fontSize = 15.sp,
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun ExampleContent() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Example Box
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 2.dp,
                    color = Color(0xFF00FFFF),
                    shape = RoundedCornerShape(15.dp)
                )
                .background(
                    color = Color(0xFF00FFFF).copy(alpha = 0.1f),
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
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 10.dp)
                    .background(
                        brush = Brush.linearGradient(
                            listOf(Color(0xFFFF00FF), Color(0xFF00FFFF))
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Your Score: 87% 🌟",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(25.dp))

        Text(
            text = "The better your backwards attempt,\nthe higher your score!",
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.9f),
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
    }
}

@Composable
fun ExampleText(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color.Black.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp)
    ) {
        Text(
            text = "$label ",
            color = Color(0xFF00FFFF),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Text(
            text = value,
            color = Color.White,
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
        modifier = Modifier
            .size(10.dp * scale)
            .background(
                color = if (isActive) Color(0xFFFF00FF) else Color.White.copy(alpha = 0.3f),
                shape = CircleShape
            )
            .border(
                width = 2.dp,
                color = Color(0xFFFF00FF).copy(alpha = 0.5f),
                shape = CircleShape
            )
            .then(
                if (isActive) {
                    Modifier.shadow(
                        elevation = 15.dp,
                        shape = CircleShape,
                        spotColor = Color(0xFFFF00FF)
                    )
                } else Modifier
            )
    )
}