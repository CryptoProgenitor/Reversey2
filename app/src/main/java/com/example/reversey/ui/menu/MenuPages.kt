package com.example.reversey.ui.menu

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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.reversey.BuildConfig
import com.example.reversey.R
import com.example.reversey.ui.theme.AestheticThemeData
import com.example.reversey.ui.theme.AestheticThemes
import com.example.reversey.ui.viewmodels.AudioViewModel
import com.example.reversey.ui.viewmodels.ThemeViewModel
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
    aesthetic: AestheticThemeData,
    colors: ColorScheme,
    onNavigateHome: () -> Unit,
    onNavigateAbout: () -> Unit,
    onNavigateSettings: () -> Unit,
    onNavigateThemes: () -> Unit,
    onClearAll: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MenuItem(
            icon = Icons.Default.Home,
            label = "Home",
            selected = currentRoute == "home",
            aesthetic = aesthetic,
            colors = colors,
            onClick = onNavigateHome
        )

        MenuItem(
            icon = Icons.Default.Settings,
            label = "Settings",
            selected = false,
            aesthetic = aesthetic,
            colors = colors,
            onClick = onNavigateSettings
        )

        MenuItem(
            icon = Icons.Default.AutoAwesome,
            label = "Themes",
            selected = false,
            aesthetic = aesthetic,
            colors = colors,
            onClick = onNavigateThemes
        )

        MenuItem(
            icon = Icons.Default.Info,
            label = "About",
            selected = false,
            aesthetic = aesthetic,
            colors = colors,
            onClick = onNavigateAbout
        )

        Spacer(modifier = Modifier.weight(1f))

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = aesthetic.cardBorder.copy(alpha = 0.3f)
        )

        MenuItem(
            icon = Icons.Default.Delete,
            label = "Clear All Recordings",
            selected = false,
            aesthetic = aesthetic,
            colors = colors,
            onClick = onClearAll,
            customColor = Color(0xFFE53935)
        )
    }
}

// ========== ABOUT SCREEN ==========
@Composable
fun AboutContent(
    aesthetic: AestheticThemeData,
    colors: ColorScheme,
    audioViewModel: AudioViewModel
) {
    val context = LocalContext.current
    val uiState by audioViewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "ReVerseY",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = "Version ${BuildConfig.VERSION_NAME}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Challenge yourself by recording audio and matching it forwards or backwards!\nBuilt by Ed Dark (c) 2025.\nInspired by CPD!",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
            text = "Created with ❤️ for 🐣🐣🐣🦉🦉🦉",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ========== THEMES SCREEN ==========
@Composable
fun ThemesContent(
    aesthetic: AestheticThemeData,
    colors: ColorScheme,
    themeViewModel: ThemeViewModel
) {
    val currentThemeId by themeViewModel.currentThemeId.collectAsState()
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val themes = AestheticThemes.allThemes

        themes.forEach { (id, themeData) ->
            ThemeButton(
                themeName = themeData.name,
                themeId = id,
                isSelected = currentThemeId == id,
                aesthetic = aesthetic,
                colors = colors,
                onClick = {
                    scope.launch {
                        themeViewModel.setTheme(id)
                    }
                }
            )
        }
    }
}

// --- Private Helpers for Menu Items ---

@Composable
private fun MenuItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    aesthetic: AestheticThemeData,
    colors: ColorScheme,
    onClick: () -> Unit,
    customColor: Color? = null
) {
    val itemRotation = if (aesthetic.id == "scrapbook") {
        remember { (0..1).random() * if ((0..1).random() == 0) -1f else 1f * 0.5f }
    } else 0f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .rotate(itemRotation)
            .then(getItemModifier(aesthetic, colors, selected))
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = customColor ?: getItemColor(aesthetic, colors, selected),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    letterSpacing = if (aesthetic.useWideLetterSpacing) 2.sp else 0.5.sp
                ),
                color = customColor ?: getItemColor(aesthetic, colors, selected)
            )
        }
    }
}

@Composable
private fun ThemeButton(
    themeName: String,
    themeId: String,
    isSelected: Boolean,
    aesthetic: AestheticThemeData,
    colors: ColorScheme,
    onClick: () -> Unit
) {
    val itemRotation = if (aesthetic.id == "scrapbook") {
        remember { (0..1).random() * if ((0..1).random() == 0) -1f else 1f * 0.5f }
    } else 0f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .rotate(itemRotation)
            .then(getItemModifier(aesthetic, colors, isSelected))
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = themeName.uppercase(),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                letterSpacing = if (aesthetic.useWideLetterSpacing) 2.sp else 0.5.sp
            ),
            color = getItemColor(aesthetic, colors, isSelected)
        )
    }
}

// Shared Modifiers
fun getItemModifier(
    aesthetic: AestheticThemeData,
    colors: ColorScheme,
    selected: Boolean
): Modifier {
    return when (aesthetic.id) {
        "egg" -> Modifier
            .background(
                color = if (selected) Color(0xFFFFE0B2) else Color.White,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = if (selected) 3.dp else 2.dp,
                color = if (selected) Color(0xFFFF8A65) else Color(0xFF2E2E2E).copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )

        "scrapbook" -> Modifier
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(
                width = if (selected) 4.dp else 3.dp,
                color = if (selected) Color(0xFF8B4513) else Color(0xFFD2691E),
                shape = RoundedCornerShape(12.dp)
            )
            .then(if (selected) Modifier.shadow(4.dp, RoundedCornerShape(12.dp)) else Modifier)

        "cyberpunk" -> Modifier
            .background(
                color = if (selected) Color(0xFFFF00FF).copy(alpha = 0.2f)
                else Color(0xFF00FFFF).copy(alpha = 0.05f),
                shape = RoundedCornerShape(4.dp)
            )
            .border(
                width = 2.dp,
                color = if (selected) Color(0xFFFF00FF) else Color(0xFF00FFFF).copy(alpha = 0.3f),
                shape = RoundedCornerShape(4.dp)
            )

        "vaporwave" -> Modifier
            .background(
                color = Color.White.copy(alpha = if (selected) 0.4f else 0.15f),
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 2.dp,
                color = Color.White.copy(alpha = if (selected) 0.6f else 0.4f),
                shape = RoundedCornerShape(16.dp)
            )

        else -> Modifier
            .background(
                color = if (selected) colors.primary.copy(alpha = 0.2f) else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = if (selected) colors.primary else colors.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
    }
}

fun getItemColor(
    aesthetic: AestheticThemeData,
    colors: ColorScheme,
    selected: Boolean
): Color {
    return when (aesthetic.id) {
        "egg" -> if (selected) Color(0xFFFF8A65) else Color(0xFF2E2E2E)
        "scrapbook" -> if (selected) Color(0xFF8B4513) else Color(0xFF5D4037)
        "cyberpunk" -> if (selected) Color(0xFFFF00FF) else Color(0xFF00FFFF)
        "vaporwave" -> Color.White
        else -> if (selected) colors.primary else colors.onSurface
    }
}