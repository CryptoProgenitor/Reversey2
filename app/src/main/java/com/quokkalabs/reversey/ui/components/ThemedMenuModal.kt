package com.quokkalabs.reversey.ui.components

import android.os.Build.VERSION.SDK_INT
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.quokkalabs.reversey.data.backup.BackupManager
import com.quokkalabs.reversey.ui.menu.AboutContent
import com.quokkalabs.reversey.ui.menu.HelpContent
import com.quokkalabs.reversey.ui.menu.MenuContent
import com.quokkalabs.reversey.ui.menu.ModalScreen
import com.quokkalabs.reversey.ui.menu.SettingsContent
import com.quokkalabs.reversey.ui.menu.StaticMenuColors
import com.quokkalabs.reversey.ui.menu.ThemesContent
import com.quokkalabs.reversey.ui.viewmodels.AudioViewModel
import com.quokkalabs.reversey.ui.viewmodels.ThemeViewModel
import kotlinx.coroutines.delay

// 🔧 Constants for animation timing
private const val ANIM_DURATION = 200
private const val EXIT_BUFFER = 50

@Composable
fun ThemedMenuModal(
    visible: Boolean,
    currentRoute: String?,
    onDismiss: () -> Unit,
    onNavigateHome: () -> Unit,
    onClearAll: () -> Unit,
    onNavigateToFiles: () -> Unit,
    onShowTutorial: () -> Unit,
    themeViewModel: ThemeViewModel,
    audioViewModel: AudioViewModel,
    backupManager: BackupManager,
    initialScreen: ModalScreen = ModalScreen.Menu
) {
    // 🔧 SAFETY: Custom Saver to handle Sealed Class serialization
    // This maps the objects to strings and back, avoiding .name/.entries calls
    val stackSaver = remember {
        listSaver<List<ModalScreen>, String>(
            save = { stack -> stack.map { it.toScreenId() } },
            restore = { savedList ->
                savedList.map { it.toModalScreen() }
            }
        )
    }

    // 🔧 Stack-based navigation
    var screenStack by rememberSaveable(visible, initialScreen, stateSaver = stackSaver) {
        mutableStateOf(
            if (visible) {
                if (initialScreen != ModalScreen.Menu)
                    listOf(ModalScreen.Menu, initialScreen)
                else
                    listOf(ModalScreen.Menu)
            } else {
                listOf(ModalScreen.Menu)
            }
        )
    }

    // Safe access to current screen (default to Menu if stack somehow empties)
    val currentScreen = screenStack.lastOrNull() ?: ModalScreen.Menu

    // Reset stack AFTER exit animation completes
    LaunchedEffect(visible) {
        if (!visible) {
            delay((ANIM_DURATION + EXIT_BUFFER).toLong())
            screenStack = listOf(ModalScreen.Menu)
        }
    }

    // 🔧 Back button handler
    BackHandler(enabled = visible) {
        if (screenStack.size > 1) {
            screenStack = screenStack.dropLast(1)
        } else {
            onDismiss()
        }
    }

    // Helper to navigate (prevents duplicates, caps stack size)
    fun navigateTo(screen: ModalScreen) {
        if (screenStack.lastOrNull() != screen && screenStack.size < 10) {
            screenStack = screenStack + screen
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(300)),
        exit = fadeOut(animationSpec = tween(ANIM_DURATION))
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
                        onBack = {
                            if (screenStack.size > 1) {
                                screenStack = screenStack.dropLast(1)
                            }
                        },
                        onClose = onDismiss
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Content Area
                    Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        when (currentScreen) {
                            ModalScreen.Menu -> MenuContent(
                                currentRoute = currentRoute,
                                onNavigateHome = { onNavigateHome(); onDismiss() },
                                onNavigateAbout = { navigateTo(ModalScreen.About) },
                                onNavigateSettings = { navigateTo(ModalScreen.Settings) },
                                onNavigateThemes = { navigateTo(ModalScreen.Themes) },
                                onNavigateHelp = { navigateTo(ModalScreen.Help) },
                                onNavigateToFiles = { onNavigateToFiles(); onDismiss() },
                                onShowTutorial = { onShowTutorial(); onDismiss() },
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

                            ModalScreen.Help -> HelpContent()
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
        ModalScreen.Help -> "HELP"
        else -> "MENU"
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

// -----------------------------------------------------------
// 🔧 HELPERS: Sealed Class Serialization
// -----------------------------------------------------------

private fun ModalScreen.toScreenId(): String = when (this) {
    ModalScreen.Menu -> "Menu"
    ModalScreen.About -> "About"
    ModalScreen.Settings -> "Settings"
    ModalScreen.Themes -> "Themes"
    ModalScreen.Help -> "Help"
    else -> "Menu"
}

private fun String.toModalScreen(): ModalScreen = when (this) {
    "Menu" -> ModalScreen.Menu
    "About" -> ModalScreen.About
    "Settings" -> ModalScreen.Settings
    "Themes" -> ModalScreen.Themes
    "Help" -> ModalScreen.Help
    else -> ModalScreen.Menu
}