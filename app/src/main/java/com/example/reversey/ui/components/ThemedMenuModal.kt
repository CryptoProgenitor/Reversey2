package com.example.reversey.ui.components

import android.os.Build.VERSION.SDK_INT
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.example.reversey.R
import com.example.reversey.ui.menu.*
import com.example.reversey.ui.theme.AestheticTheme
import com.example.reversey.ui.theme.AestheticThemeData
import com.example.reversey.ui.theme.MaterialColors
import com.example.reversey.ui.viewmodels.AudioViewModel
import com.example.reversey.ui.viewmodels.ThemeViewModel
import kotlinx.coroutines.delay

@Composable
fun ThemedMenuModal(
    visible: Boolean,
    currentRoute: String?,
    onDismiss: () -> Unit,
    onNavigateHome: () -> Unit,
    onClearAll: () -> Unit,
    themeViewModel: ThemeViewModel,
    audioViewModel: AudioViewModel,
    initialScreen: ModalScreen = ModalScreen.Menu
) {
    var currentScreen by remember(visible, initialScreen) { mutableStateOf(if (visible) initialScreen else ModalScreen.Menu) }

    LaunchedEffect(visible) {
        if (!visible) currentScreen = ModalScreen.Menu
    }

    val aesthetic = AestheticTheme()
    val colors = MaterialColors()

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(300)),
        exit = fadeOut(animationSpec = tween(200))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.75f))
                .blur(if (visible) 0.dp else 8.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .fillMaxHeight(0.9f)
                    .clickable(enabled = false) { }
                    .then(getCardModifier(aesthetic, colors))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            horizontal = if (aesthetic.id == "egg") 16.dp else 24.dp,
                            vertical = if (aesthetic.id == "egg") 12.dp else 20.dp
                        )
                ) {
                    ModalHeader(
                        currentScreen = currentScreen,
                        aesthetic = aesthetic,
                        colors = colors,
                        onBack = { currentScreen = ModalScreen.Menu },
                        onClose = onDismiss
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        when (currentScreen) {
                            ModalScreen.Menu -> MenuContent(
                                currentRoute = currentRoute,
                                aesthetic = aesthetic,
                                colors = colors,
                                onNavigateHome = { onNavigateHome(); onDismiss() },
                                onNavigateAbout = { currentScreen = ModalScreen.About },
                                onNavigateSettings = { currentScreen = ModalScreen.Settings },
                                onNavigateThemes = { currentScreen = ModalScreen.Themes },
                                onClearAll = { onClearAll(); onDismiss() }
                            )

                            ModalScreen.About -> AboutContent(
                                aesthetic = aesthetic,
                                colors = colors,
                                audioViewModel = audioViewModel
                            )

                            ModalScreen.Settings -> SettingsContent(
                                aesthetic = aesthetic,
                                colors = colors,
                                themeViewModel = themeViewModel,
                                audioViewModel = audioViewModel
                            )

                            ModalScreen.Themes -> ThemesContent(
                                aesthetic = aesthetic,
                                colors = colors,
                                themeViewModel = themeViewModel
                            )
                        }
                    }
                }

                // Easter Egg Overlay
                val uiState by audioViewModel.uiState.collectAsState()
                val context = LocalContext.current
                val imageLoader = remember {
                    ImageLoader.Builder(context).components {
                        if (SDK_INT >= 28) add(ImageDecoderDecoder.Factory()) else add(GifDecoder.Factory())
                    }.build()
                }

                AnimatedVisibility(
                    visible = uiState.showEasterEgg,
                    enter = fadeIn(animationSpec = tween(500)),
                    exit = fadeOut(animationSpec = tween(500))
                ) {
                    LaunchedEffect(Unit) {
                        delay(1500L)
                        audioViewModel.dismissEasterEgg()
                    }
                    AsyncImage(
                        model = R.drawable.cracking_egg,
                        contentDescription = "Easter Egg",
                        imageLoader = imageLoader,
                        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.75f))
                    )
                }
            }
        }
    }
}

@Composable
private fun ModalHeader(
    currentScreen: ModalScreen,
    aesthetic: AestheticThemeData,
    colors: ColorScheme,
    onBack: () -> Unit,
    onClose: () -> Unit
) {
    val title = when (currentScreen) {
        ModalScreen.Menu -> if (aesthetic.id == "egg") "MENU 🥚" else "MENU"
        ModalScreen.About -> "ABOUT"
        ModalScreen.Settings -> "SETTINGS"
        ModalScreen.Themes -> "THEMES"
    }

    val showBackButton = currentScreen != ModalScreen.Menu

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(getHeaderModifier(aesthetic, colors))
            .padding(
                horizontal = 16.dp,
                vertical = if (aesthetic.id == "egg") 8.dp else 12.dp
            )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showBackButton) {
                IconButton(onClick = onBack, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back",
                        tint = getIconTint(aesthetic)
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(32.dp))
            }

            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = if (aesthetic.useWideLetterSpacing) 3.sp else 1.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.Close, contentDescription = "Close",
                    tint = getIconTint(aesthetic)
                )
            }
        }
    }
}

private fun getIconTint(aesthetic: AestheticThemeData): Color {
    return when (aesthetic.id) {
        "egg" -> Color(0xFF2E2E2E)
        "scrapbook" -> Color(0xFFFFF9E6)
        "cyberpunk" -> Color.Black
        else -> aesthetic.primaryTextColor
    }
}

private fun getCardModifier(aesthetic: AestheticThemeData, colors: ColorScheme): Modifier {
    return when (aesthetic.id) {
        "egg" -> Modifier.background(Color(0xFFFFF8E1), RoundedCornerShape(20.dp))
            .border(4.dp, Color(0xFF2E2E2E), RoundedCornerShape(20.dp)).shadow(6.dp, RoundedCornerShape(20.dp))
        "scrapbook" -> Modifier.rotate(-0.5f).background(Color(0xFFFFF9E6), RoundedCornerShape(20.dp))
            .border(4.dp, Color(0xFF8B4513), RoundedCornerShape(20.dp)).shadow(8.dp, RoundedCornerShape(20.dp), spotColor = Color(0xFF8B4513))
        "cyberpunk" -> Modifier.background(Brush.verticalGradient(listOf(Color(0xFF0A0A0A), Color(0xFF1A0033))), RoundedCornerShape(8.dp))
            .border(3.dp, Color(0xFF00FFFF), RoundedCornerShape(8.dp)).shadow(20.dp, RoundedCornerShape(8.dp), spotColor = Color(0xFF00FFFF))
        "vaporwave" -> Modifier.background(aesthetic.primaryGradient, RoundedCornerShape(24.dp))
            .border(3.dp, Color(0xFFB967FF), RoundedCornerShape(24.dp)).shadow(20.dp, RoundedCornerShape(24.dp), spotColor = Color(0xFFB967FF))
        else -> Modifier.background(colors.surface, RoundedCornerShape(16.dp))
            .border(1.dp, colors.outline.copy(alpha = 0.3f), RoundedCornerShape(16.dp)).shadow(16.dp, RoundedCornerShape(16.dp))
    }
}

private fun getHeaderModifier(aesthetic: AestheticThemeData, colors: ColorScheme): Modifier {
    return when (aesthetic.id) {
        "egg" -> Modifier.background(Brush.horizontalGradient(listOf(Color(0xFFFFE0B2), Color(0xFFFFD54F))), RoundedCornerShape(12.dp))
            .border(2.dp, Color(0xFF2E2E2E), RoundedCornerShape(12.dp))
        "scrapbook" -> Modifier.rotate(0.5f).background(Brush.linearGradient(listOf(Color(0xFFD2691E), Color(0xFF8B4513))), RoundedCornerShape(12.dp))
            .shadow(3.dp, RoundedCornerShape(12.dp))
        "cyberpunk" -> Modifier.background(Brush.horizontalGradient(listOf(Color(0xFF00FFFF), Color(0xFFFF00FF))), RoundedCornerShape(4.dp))
        "vaporwave" -> Modifier.background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(16.dp))
        else -> Modifier.background(colors.surfaceVariant, RoundedCornerShape(12.dp))
    }
}