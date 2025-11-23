package com.quokkalabs.reversey.ui.menu

import android.media.MediaPlayer
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
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
    themeViewModel: ThemeViewModel
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
                isSelected = currentThemeId == id,
                onClick = {
                    scope.launch {
                        themeViewModel.setTheme(id)
                    }
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
