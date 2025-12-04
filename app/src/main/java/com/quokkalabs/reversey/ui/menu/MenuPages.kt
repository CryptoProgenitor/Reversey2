package com.quokkalabs.reversey.ui.menu

import android.media.MediaPlayer
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quokkalabs.reversey.BuildConfig
import com.quokkalabs.reversey.R
import com.quokkalabs.reversey.ui.theme.AestheticThemes
import com.quokkalabs.reversey.ui.viewmodels.AudioViewModel
import com.quokkalabs.reversey.ui.viewmodels.ThemeViewModel
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// Navigation State
sealed class ModalScreen {
    object Menu : ModalScreen()
    object About : ModalScreen()
    object Settings : ModalScreen()
    object Themes : ModalScreen()
}

// ========== MENU SCREEN ==========
@Composable
fun MenuContent(
    currentRoute: String?,
    onNavigateHome: () -> Unit,
    onNavigateAbout: () -> Unit,
    onNavigateSettings: () -> Unit,
    onNavigateThemes: () -> Unit,
    onNavigateToBackupTests: () -> Unit,  // ← ADD THIS LINE
    onClearAll: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FloatingMenuItem(
            icon = Icons.Default.Home,
            label = "Home",
            selected = currentRoute == "home",
            onClick = onNavigateHome
        )

        FloatingMenuItem(
            icon = Icons.Default.Settings,
            label = "Settings",
            selected = false,
            onClick = onNavigateSettings
        )

        FloatingMenuItem(
            icon = Icons.Default.AutoAwesome,
            label = "Themes",
            selected = false,
            onClick = onNavigateThemes
        )

        FloatingMenuItem(
            icon = Icons.Default.Info,
            label = "About",
            selected = false,
            onClick = onNavigateAbout
        )

        // 🧪 Backup Tests Button
        FloatingMenuItem(
            icon = Icons.Default.Settings,  // Or use a better icon if you have one
            label = "🧪 Backup Tests",
            selected = false,
            onClick = onNavigateToBackupTests
        )

        Spacer(modifier = Modifier.weight(1f))

        // Divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(StaticMenuColors.divider)
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Delete Button - Special Danger Style
        DangerMenuItem(
            icon = Icons.Default.Delete,
            label = "Clear All Recordings",
            onClick = onClearAll
        )
    }
}

// ========== ABOUT SCREEN ==========
@Composable
fun AboutContent(
    audioViewModel: AudioViewModel
) {
    val context = LocalContext.current
    val uiState by audioViewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // About Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(StaticMenuColors.cardSelected)
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "ReVerseY",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = StaticMenuColors.textOnCard
                )

                Text(
                    text = "Version ${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = StaticMenuColors.textOnCard.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Challenge yourself by recording audio and matching it forwards or backwards!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = StaticMenuColors.textOnCard.copy(alpha = 0.85f),
                    modifier = Modifier.clickable {
                        if (uiState.cpdTaps + 1 == 5) {
                            val mediaPlayer = MediaPlayer.create(context, R.raw.egg_crack)
                            mediaPlayer?.start()
                            mediaPlayer?.setOnCompletionListener { mp -> mp.release() }
                        }
                        audioViewModel.onCpdTapped()
                    }
                )

                Text(
                    text = "Built by Ed Dark © 2025\nInspired by CPD!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = StaticMenuColors.textOnCard.copy(alpha = 0.7f)
                )

                Text(
                    text = "Created with ❤️ for 🐣🐣🐣🦉🦉🦉",
                    style = MaterialTheme.typography.bodyLarge,
                    color = StaticMenuColors.textOnCard
                )
            }
        }
    }
}

// ========== THEMES SCREEN ==========
@Composable
fun ThemesContent(
    themeViewModel: ThemeViewModel,
    onDismiss: () -> Unit = {}
) {
    val currentThemeId by themeViewModel.currentThemeId.collectAsState()
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        val themes = AestheticThemes.allThemes

        themes.forEach { (id, themeData) ->
            FloatingThemeButton(
                themeName = themeData.name,
                themeEmoji = themeData.recordButtonEmoji,
                isPro = themeData.isPro,
                isSelected = currentThemeId == id,
                onClick = {
                    scope.launch {
                        themeViewModel.setTheme(id)
                    }
                    onDismiss()
                }
            )
        }
    }
}

// ========== FLOATING MENU ITEM ==========
@Composable
private fun FloatingMenuItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (selected) {
                    Modifier.shadow(
                        elevation = 12.dp,
                        shape = shape,
                        ambientColor = Color.Black.copy(alpha = 0.15f),
                        spotColor = Color.Black.copy(alpha = 0.2f)
                    )
                } else {
                    Modifier.shadow(
                        elevation = 4.dp,
                        shape = shape,
                        ambientColor = Color.Black.copy(alpha = 0.1f),
                        spotColor = Color.Black.copy(alpha = 0.1f)
                    )
                }
            )
            .clip(shape)
            .background(
                if (selected) StaticMenuColors.cardSelected
                else StaticMenuColors.cardUnselected
            )
            .then(
                if (selected) {
                    Modifier.border(2.dp, Color.White.copy(alpha = 0.8f), shape)
                } else {
                    Modifier.border(1.dp, Color.White.copy(alpha = 0.2f), shape)
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) StaticMenuColors.textOnCard else StaticMenuColors.textOnGradient,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold
                ),
                color = if (selected) StaticMenuColors.textOnCard else StaticMenuColors.textOnGradient
            )
        }
    }
}

// ========== DANGER MENU ITEM ==========
@Composable
private fun DangerMenuItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(StaticMenuColors.deleteBackground)
            .border(1.dp, StaticMenuColors.deleteBorder, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = StaticMenuColors.deleteText,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = StaticMenuColors.deleteText
            )
        }
    }
}

// ========== FLOATING THEME BUTTON ==========
@Composable
private fun FloatingThemeButton(
    themeName: String,
    themeEmoji: String,
    isPro: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(14.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isSelected) {
                    Modifier.shadow(
                        elevation = 12.dp,
                        shape = shape,
                        ambientColor = Color.Black.copy(alpha = 0.15f),
                        spotColor = Color.Black.copy(alpha = 0.2f)
                    )
                } else {
                    Modifier.shadow(
                        elevation = 2.dp,
                        shape = shape,
                        ambientColor = Color.Black.copy(alpha = 0.05f),
                        spotColor = Color.Black.copy(alpha = 0.05f)
                    )
                }
            )
            .clip(shape)
            .background(
                if (isSelected) StaticMenuColors.cardSelected
                else StaticMenuColors.cardUnselected
            )
            .then(
                if (isSelected) {
                    Modifier.border(2.dp, Color.White.copy(alpha = 0.8f), shape)
                } else {
                    Modifier.border(1.dp, Color.White.copy(alpha = 0.15f), shape)
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = themeEmoji,
                fontSize = 22.sp
            )
            Text(
                text = themeName,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                ),
                color = if (isSelected) StaticMenuColors.textOnCard else StaticMenuColors.textOnGradient,
                modifier = Modifier.weight(1f)
            )

            // PRO Badge (Animated Sparkle)
            if (isPro) {
                AnimatedSparkleProBadge(isSelected)
            }

            if (isSelected) {
                Text(
                    text = "✓",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = StaticMenuColors.checkmark
                )
            }
        }
    }
}

// ========== ANIMATED SPARKLE PRO BADGE ==========
@Composable
private fun AnimatedSparkleProBadge(isSelected: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "sparkle")

    // Pulse animation (scale)
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    // Color pulse animation
    val colorPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "color"
    )

    // Calculate pulsing pink color
    val greenValue = (105 + 30 * sin(colorPhase * 2 * PI)).toInt().coerceIn(0, 255)
    val blueValue = (180 + 30 * sin(colorPhase * 2 * PI)).toInt().coerceIn(0, 255)
    val badgeColor = Color(255, greenValue, blueValue)

    Box(
        modifier = Modifier
            .width(60.dp)
            .height(28.dp)
    ) {
        // Background sparkle glow
        Box(
            modifier = Modifier
                .matchParentSize()
                .blur(8.dp)
                .alpha(0.3f * (0.5f + 0.5f * sin(colorPhase * 2 * PI).toFloat()))
                .background(
                    color = if (isSelected) Color(0xFFFF69B4) else Color(0xFFFFB6C1),
                    shape = RoundedCornerShape(8.dp)
                )
        )

        // Main badge with scale animation
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clip(RoundedCornerShape(8.dp))
                .background(badgeColor)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = "✨PRO",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                ),
                color = Color.White,
                fontSize = 11.sp
            )
        }

        // Sparkle particles
        SparkleParticles(colorPhase)
    }
}

@Composable
private fun SparkleParticles(phase: Float) {
    // Sparkle positions around badge (relative to center)
    val sparkleData = remember {
        listOf(
            Pair(Offset(-10f, -15f), 0f),    // Top left
            Pair(Offset(50f, -15f), 0.3f),   // Top right
            Pair(Offset(-10f, 15f), 0.6f),   // Bottom left
            Pair(Offset(50f, 15f), 0.2f),    // Bottom right
            Pair(Offset(20f, -20f), 0.8f),   // Top center
            Pair(Offset(20f, 20f), 0.4f),    // Bottom center
        )
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val centerX = size.width / 2
        val centerY = size.height / 2

        sparkleData.forEach { (offset, phaseOffset) ->
            val sparklePhase = (phase + phaseOffset) % 1f
            val opacity = if (sparklePhase < 0.5f) {
                sparklePhase * 2f  // Fade in
            } else {
                (1f - sparklePhase) * 2f  // Fade out
            }

            drawSparkle(
                center = Offset(centerX + offset.x, centerY + offset.y),
                size = 6.dp.toPx(),
                opacity = opacity
            )
        }
    }
}

private fun DrawScope.drawSparkle(center: Offset, size: Float, opacity: Float) {
    val color = Color.White.copy(alpha = opacity)

    // Draw 4-point star
    val path = Path().apply {
        val points = 8
        for (i in 0 until points) {
            val angle = (i * PI / 4) - PI / 2
            val radius = if (i % 2 == 0) size else size * 0.4f

            val x = center.x + (radius * cos(angle)).toFloat()
            val y = center.y + (radius * sin(angle)).toFloat()

            if (i == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }

    drawPath(path, color)
}