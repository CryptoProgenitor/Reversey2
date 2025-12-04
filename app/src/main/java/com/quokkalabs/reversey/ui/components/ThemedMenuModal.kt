package com.quokkalabs.reversey.ui.components

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.quokkalabs.reversey.R
import com.quokkalabs.reversey.ui.menu.*
import com.quokkalabs.reversey.ui.viewmodels.AudioViewModel
import com.quokkalabs.reversey.ui.viewmodels.ThemeViewModel
import kotlinx.coroutines.delay
import com.quokkalabs.reversey.data.backup.BackupManager

@Composable
fun ThemedMenuModal(
    visible: Boolean,
    currentRoute: String?,
    onDismiss: () -> Unit,
    onNavigateHome: () -> Unit,
    onClearAll: () -> Unit,
    onNavigateToFiles: () -> Unit,
    themeViewModel: ThemeViewModel,
    audioViewModel: AudioViewModel,
    backupManager: BackupManager,
    initialScreen: ModalScreen = ModalScreen.Menu
) {
    var currentScreen by remember(visible, initialScreen) {
        mutableStateOf(if (visible) initialScreen else ModalScreen.Menu)
    }

    LaunchedEffect(visible) {
        if (!visible) currentScreen = ModalScreen.Menu
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(300)),
        exit = fadeOut(animationSpec = tween(200))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            // Modal Card with Gradient Background
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .fillMaxHeight(0.88f)
                    .clickable(enabled = false) { }
                    .shadow(
                        elevation = 24.dp,
                        shape = RoundedCornerShape(24.dp),
                        ambientColor = Color(0xFF667EEA).copy(alpha = 0.3f),
                        spotColor = Color(0xFF764BA2).copy(alpha = 0.3f)
                    )
                    .clip(RoundedCornerShape(24.dp))
                    .background(StaticMenuColors.backgroundGradient)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    // Glassmorphism Header
                    GlassHeader(
                        currentScreen = currentScreen,
                        onBack = { currentScreen = ModalScreen.Menu },
                        onClose = onDismiss
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Content Area
                    Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        when (currentScreen) {
                            ModalScreen.Menu -> MenuContent(
                                currentRoute = currentRoute,
                                onNavigateHome = { onNavigateHome(); onDismiss() },
                                onNavigateAbout = { currentScreen = ModalScreen.About },
                                onNavigateSettings = { currentScreen = ModalScreen.Settings },
                                onNavigateThemes = { currentScreen = ModalScreen.Themes },
                                onNavigateToFiles = { onNavigateToFiles(); onDismiss() },
                                onClearAll = { onClearAll(); onDismiss() }
                            )

                            ModalScreen.About -> AboutContent(
                                audioViewModel = audioViewModel
                            )

                            ModalScreen.Settings -> SettingsContent(
                                themeViewModel = themeViewModel,
                                audioViewModel = audioViewModel,
                                backupManager = backupManager,
                                onBackupComplete = {
                                    audioViewModel.loadRecordings()
                                }
                            )

                            ModalScreen.Themes -> ThemesContent(
                                themeViewModel = themeViewModel,
                                onDismiss = onDismiss
                            )
                        }
                    }
                }

                // Easter Egg Overlay
                val uiState by audioViewModel.uiState.collectAsState()
                val context = LocalContext.current
                val imageLoader = remember {
                    ImageLoader.Builder(context).components {
                        if (SDK_INT >= 28) add(ImageDecoderDecoder.Factory())
                        else add(GifDecoder.Factory())
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
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.75f))
                    )
                }
            }
        }
    }
}

@Composable
private fun GlassHeader(
    currentScreen: ModalScreen,
    onBack: () -> Unit,
    onClose: () -> Unit
) {
    val title = when (currentScreen) {
        ModalScreen.Menu -> "MENU"
        ModalScreen.About -> "ABOUT"
        ModalScreen.Settings -> "SETTINGS"
        ModalScreen.Themes -> "THEMES"
    }

    val showBackButton = currentScreen != ModalScreen.Menu

    // Glassmorphism effect
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(StaticMenuColors.headerBackground)
            .border(
                width = 1.dp,
                color = StaticMenuColors.headerBorder,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back Button
            if (showBackButton) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = StaticMenuColors.textOnGradient
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(32.dp))
            }

            // Title
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                ),
                color = StaticMenuColors.textOnGradient
            )

            // Close Button
            IconButton(
                onClick = onClose,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close",
                    tint = StaticMenuColors.textOnGradient
                )
            }
        }
    }
}