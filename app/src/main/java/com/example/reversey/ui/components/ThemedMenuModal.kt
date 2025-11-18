package com.example.reversey.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.reversey.ui.theme.AestheticTheme
import com.example.reversey.ui.theme.MaterialColors
import com.example.reversey.ui.theme.AestheticThemeData
import com.example.reversey.ui.viewmodels.ThemeViewModel
//import com.example.reversey.scoring.ScoringEngine
import com.example.reversey.scoring.DifficultyLevel
import com.example.reversey.scoring.DifficultyConfig
import com.example.reversey.scoring.ScoringPresets
import com.example.reversey.ui.viewmodels.AudioViewModel
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import android.media.MediaPlayer
import android.os.Build.VERSION.SDK_INT
import android.util.Log
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.example.reversey.BuildConfig
import com.example.reversey.R
import kotlinx.coroutines.delay
import com.example.reversey.testing.BITRunner
import android.widget.Toast
import com.example.reversey.testing.VocalModeDetectorTuner
import com.example.reversey.audio.processing.AudioProcessor
import androidx.compose.material.icons.filled.Tune
import kotlinx.coroutines.withContext
import com.example.reversey.testing.ScoringStressTester
import kotlinx.coroutines.launch
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.Button
import androidx.compose.runtime.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
//import com.example.reversey.testing.ScoringStressTesterPanel


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 🎨 MULTI-SCREEN THEMED MENU MODAL
 * Contains About, Settings, and Themes INSIDE the modal
 * - 90% screen size with blur/dim background
 * - Fully themed with Egg 🥚 and Scrapbook support
 * - Internal navigation (doesn't leave modal)
 */

sealed class ModalScreen {
    object Menu : ModalScreen()
    object About : ModalScreen()
    object Settings : ModalScreen()
    object Themes : ModalScreen()
}

@Composable
fun ThemedMenuModal(
    visible: Boolean,
    currentRoute: String?,
    onDismiss: () -> Unit,
    onNavigateHome: () -> Unit,
    onClearAll: () -> Unit,
    themeViewModel: ThemeViewModel,
    //scoringEngine: ScoringEngine,
    audioViewModel: AudioViewModel,
    //showDebugPanel: Boolean,
    //onShowDebugPanelChange: (Boolean) -> Unit,
    initialScreen: ModalScreen = ModalScreen.Menu  // ← NEW: Allow opening at specific screen
) {

    var currentScreen by remember(visible, initialScreen) { mutableStateOf<ModalScreen>(if (visible) initialScreen else ModalScreen.Menu) }

    // Reset to menu when modal closes
    LaunchedEffect(visible) {
        if (!visible) {
            currentScreen = ModalScreen.Menu
        }
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
            // Modal Card
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .fillMaxHeight(0.9f)
                    .clickable(enabled = false) { } // Prevent click-through
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
                    // Header with back button or close button
                    ModalHeader(
                        currentScreen = currentScreen,
                        aesthetic = aesthetic,
                        colors = colors,
                        onBack = { currentScreen = ModalScreen.Menu },
                        onClose = onDismiss
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Content area - switches based on current screen
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        when (currentScreen) {
                            ModalScreen.Menu -> MenuContent(
                                currentRoute = currentRoute,
                                aesthetic = aesthetic,
                                colors = colors,
                                onNavigateHome = {
                                    onNavigateHome()
                                    onDismiss()
                                },
                                onNavigateAbout = { currentScreen = ModalScreen.About },
                                onNavigateSettings = { currentScreen = ModalScreen.Settings },
                                onNavigateThemes = { currentScreen = ModalScreen.Themes },
                                onClearAll = {
                                    onClearAll()
                                    onDismiss()
                                }
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
                                //scoringEngine = scoringEngine,
                                audioViewModel = audioViewModel,
                                //showDebugPanel = showDebugPanel,
                                //onShowDebugPanelChange = onShowDebugPanelChange
                            )

                            ModalScreen.Themes -> ThemesContent(
                                aesthetic = aesthetic,
                                colors = colors,
                                themeViewModel = themeViewModel
                            )
                        }
                    }
                }  // ← Column closing brace

                // Easter egg overlay with fade animations
                val uiState by audioViewModel.uiState.collectAsState()
                val context = LocalContext.current
                val imageLoader = remember {
                    ImageLoader.Builder(context)
                        .components {
                            if (SDK_INT >= 28) {
                                add(ImageDecoderDecoder.Factory())
                            } else {
                                add(GifDecoder.Factory())
                            }
                        }
                        .build()
                }

                AnimatedVisibility(
                    visible = uiState.showEasterEgg,
                    enter = fadeIn(animationSpec = tween(500)),  // ✅ 1000ms fade in
                    exit = fadeOut(animationSpec = tween(500))   // ✅ 1000ms fade out
                ) {
                    LaunchedEffect(Unit) {
                        delay(1500L)
                        audioViewModel.dismissEasterEgg()
                    }

                    AsyncImage(
                        model = R.drawable.cracking_egg,
                        contentDescription = "Easter Egg GIF",
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
            // Back button or spacer
            if (showBackButton) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = when (aesthetic.id) {
                            "egg" -> Color(0xFF2E2E2E)
                            "scrapbook" -> Color(0xFFFFF9E6)
                            "cyberpunk" -> Color.Black
                            else -> aesthetic.primaryTextColor
                        }
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(32.dp))
            }

            // Title
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = if (aesthetic.useWideLetterSpacing) 3.sp else 1.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Close button
            IconButton(
                onClick = onClose,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close",
                    tint = when (aesthetic.id) {
                        "egg" -> Color(0xFF2E2E2E)
                        "scrapbook" -> Color(0xFFFFF9E6)
                        "cyberpunk" -> Color.Black
                        else -> aesthetic.primaryTextColor
                    }
                )
            }
        }
    }
}

// ========== MENU CONTENT ==========

@Composable
private fun MenuContent(
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
            colors = colors,  // ✅ FIXED
            onClick = onClearAll,
            customColor = Color(0xFFE53935)  // ✅ RED COLOR!
        )
    }
}

// ========== ABOUT CONTENT ==========

@Composable
private fun AboutContent(
    aesthetic: AestheticThemeData,
    colors: ColorScheme,
    audioViewModel: AudioViewModel
) {
    val context = LocalContext.current  // ✅ YOU WERE MISSING THIS
    val uiState by audioViewModel.uiState.collectAsState()  // ✅ YOU WERE MISSING THIS

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
            modifier = Modifier.clickable {  // ✅ YOU WERE MISSING THIS CLICK HANDLER
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

// ========== SETTINGS CONTENT (REAL CONTROLS) ==========

@Composable
private fun SettingsContent(
    aesthetic: AestheticThemeData,
    colors: ColorScheme,
    themeViewModel: ThemeViewModel,
    //scoringEngine: ScoringEngine,
    audioViewModel: AudioViewModel,
    //showDebugPanel: Boolean,
    //onShowDebugPanelChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // State collection
    val currentDifficulty by audioViewModel.currentDifficultyFlow.collectAsState()
    val isGameModeEnabled by themeViewModel.gameModeEnabled.collectAsState()
    val darkModePreference by themeViewModel.darkModePreference.collectAsState()
    val backupRecordingsEnabled by themeViewModel.backupRecordingsEnabled.collectAsState()
    val currentThemeId by themeViewModel.currentThemeId.collectAsState()
    val customAccentColor by themeViewModel.customAccentColor.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ========== GAMEPLAY SECTION ==========
        SectionTitle("GAMEPLAY", aesthetic)

        SettingRow(
            label = "Enable Game Mode",
            aesthetic = aesthetic,
            colors = colors
        ) {
            Switch(
                checked = isGameModeEnabled,
                onCheckedChange = {
                    scope.launch {
                        themeViewModel.setGameMode(it)
                    }
                }
            )
        }

        HorizontalDivider(color = aesthetic.cardBorder.copy(alpha = 0.3f))

        // ========== DIFFICULTY SECTION ==========
        SectionTitle("SCORING DIFFICULTY", aesthetic)

        Text(
            text = "Choose your challenge level",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        // Difficulty buttons in single row for 3 levels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DifficultyConfig.supportedLevels.forEach { difficulty ->
                DifficultyButton(
                    difficulty = difficulty,
                    isSelected = currentDifficulty == difficulty,
                    audioViewModel = audioViewModel,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        HorizontalDivider(color = aesthetic.cardBorder.copy(alpha = 0.3f))

        // ========== APPEARANCE SECTION ==========
        SectionTitle("APPEARANCE", aesthetic)

        // Dark Mode
        Text(
            text = "Dark Mode",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf("Light", "Dark", "System").forEach { mode ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            scope.launch {
                                themeViewModel.setDarkModePreference(mode)
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = darkModePreference == mode,
                        onClick = {
                            scope.launch {
                                themeViewModel.setDarkModePreference(mode)
                            }
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = mode,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Custom Accent Color
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Custom Accent Color",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        // Color preview and reset button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (customAccentColor != null) "Custom color active" else "Using theme default",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }//closes row

        // 🎨 Reset button - styled bordered text below status
        if (customAccentColor != null) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .border(
                        width = 1.5.dp,
                        color = colors.primary.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .background(colors.primary.copy(alpha = 0.1f))
                    .clickable {
                        scope.launch {
                            themeViewModel.setCustomAccentColor(null)
                        }
                    }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Reset to theme's colours",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }//closes if

        // Color preview and button to open picker
        var showColorPicker by remember { mutableStateOf(false) }


        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clickable { showColorPicker = true },
            colors = CardDefaults.cardColors(
                containerColor = colors.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Palette,
                        contentDescription = "Color Picker",
                        tint = colors.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "Open Color Picker",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Choose any ARGB color",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (customAccentColor != null) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(customAccentColor!!)
                            .border(2.dp, colors.outline, CircleShape)
                    )
                }
            }
        }

        // ARGB Color Picker Dialog
        if (showColorPicker) {
            ARGBColorPickerDialog(
                currentColor = customAccentColor ?: colors.primary,
                onColorSelected = { color ->
                    scope.launch {
                        themeViewModel.setCustomAccentColor(color)
                    }
                    showColorPicker = false
                },
                onDismiss = { showColorPicker = false }
            )
        }

        HorizontalDivider(color = aesthetic.cardBorder.copy(alpha = 0.3f))

        // ========== STORAGE SECTION ==========
        SectionTitle("STORAGE", aesthetic)

        SettingRow(
            label = "Backup Recordings to Drive",
            aesthetic = aesthetic,
            colors = colors
        ) {
            Switch(
                checked = backupRecordingsEnabled,
                onCheckedChange = {
                    scope.launch {
                        themeViewModel.setBackupRecordingsEnabled(it)
                    }
                }
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = colors.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "ℹ️ Backup Info",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Settings and scores always backed up. Audio files only backed up if enabled.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 🐛 DEVELOPER OPTIONS - Simple callback pattern (no ViewModel state)

        HorizontalDivider(color = aesthetic.cardBorder.copy(alpha = 0.3f))

        SectionTitle("DEVELOPER OPTIONS", aesthetic)

        ////////////////////SCORE STRESS TESTER START//////////////////////////////////////////

        var showStressTester by remember { mutableStateOf(false) }
        var progress by remember { mutableStateOf<ScoringStressTester.Progress?>(null) }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .clickable { showStressTester = true }
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Scoring Stress Tester",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

// Panel shown *inside* SettingsContent, but no inner scroll now
        if (showStressTester) {
            StressTesterPanel(
                progress = progress,
                onClose = { showStressTester = false },
                audioViewModel = audioViewModel
            )
        }


        //====================SCORING TESTER END======================================================

        /*SettingRow(
            label = "Show Debug Panel",
            aesthetic = aesthetic,
            colors = colors
        ) {
            Switch(
                checked = showDebugPanel,
                onCheckedChange = onShowDebugPanelChange
            )
        }

        Text(
            text = "Enable advanced scoring diagnostics and parameter inspection",
            style = MaterialTheme.typography.bodySmall,
            color = aesthetic.secondaryTextColor,
            modifier = Modifier.padding(horizontal = 8.dp)
        )*/
        // ADD THIS AFTER LINE 802 in ThemedMenuModal.kt
        // Right after the "Enable advanced scoring diagnostics" text

        // 🔧 Vocal Detector Tuning - Only in DEBUG builds (add this before your BITrunner block)
        // 🎵 Vocal Mode Detector Tuner - Only in DEBUG builds
        // 🎵 Vocal Mode Detector Tuner - Only in DEBUG builds
        if (BuildConfig.DEBUG) {
            var tunerRunning by remember { mutableStateOf(false) }
            var tunerProgress by remember { mutableStateOf("Ready") }
            var currentTest by remember { mutableStateOf(0) }
            var totalTests by remember { mutableStateOf(4000) }
            var progressPercentage by remember { mutableStateOf(0f) }

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !tunerRunning) {
                        if (!tunerRunning) {
                            tunerRunning = true
                            scope.launch {
                                try {
                                    Log.d("VocalTuner", "=== VOCAL TUNER START ===")

                                    // Create tuner with AudioProcessor
                                    tunerProgress = "Initializing..."
                                    Log.d("VocalTuner", "Creating AudioProcessor...")
                                    val audioProcessor = AudioProcessor()
                                    Log.d("VocalTuner", "Creating VocalModeDetectorTuner...")
                                    val tuner = VocalModeDetectorTuner(audioProcessor)

                                    tunerProgress = "Loading training data..."
                                    Log.d("VocalTuner", "Loading training data from assets...")
                                    val dataLoaded = tuner.loadTrainingDataFromAssets(context)
                                    Log.d("VocalTuner", "Training data loaded: $dataLoaded")

                                    if (!dataLoaded) {
                                        throw Exception("Failed to load training data from assets")
                                    }
// FROM HERE====================================
                                    tunerProgress = "Pre-processing training data..."
                                    Log.d("VocalTuner", "Pre-processing training data...")
                                    val preprocessed = tuner.preProcessTrainingData { status ->
                                        tunerProgress = status
                                    }

                                    if (!preprocessed) {
                                        throw Exception("Failed to pre-process training data")
                                    }

                                    tunerProgress = "Running optimization..."
                                    Log.d("VocalTuner", "Starting optimization on background thread...")

                                    // Run heavy optimization on background thread
                                    val result = withContext(kotlinx.coroutines.Dispatchers.Default) {
                                        Log.d("VocalTuner", "Background optimization started...")

                                        // 🔥 FIXED: Use correct method name and callback
                                        val optimizationResult = tuner.findOptimalParameters { progressString ->
                                            // Parse progress string to extract numbers for UI
                                            val regex = """Tested (\d+)/(\d+) \((\d+)%\)""".toRegex()
                                            val match = regex.find(progressString)
                                            if (match != null) {
                                                currentTest = match.groupValues[1].toIntOrNull() ?: 0
                                                totalTests = match.groupValues[2].toIntOrNull() ?: 4000
                                                progressPercentage = match.groupValues[3].toFloatOrNull() ?: 0f
                                                tunerProgress = progressString
                                            } else {
                                                tunerProgress = progressString
                                            }
                                        }

                                        Log.d("VocalTuner", "Optimization complete! Accuracy: ${optimizationResult?.accuracy}")
                                        optimizationResult
                                    }

                                    // Check if optimization succeeded
                                    if (result == null) {
                                        throw Exception("Optimization failed - no result returned")
                                    }

                                    // Write results to Downloads (back on main thread)
                                    tunerProgress = "Writing results file..."
                                    Log.d("VocalTuner", "Writing results to Downloads...")
                                    val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(
                                        android.os.Environment.DIRECTORY_DOWNLOADS
                                    )
                                    val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.US)
                                        .format(java.util.Date())
                                    val outputFile = java.io.File(downloadsDir, "ReVerseY_VocalTuner_${timestamp}.txt")

                                    // 🔥 FIXED: Use correct method for generating code
                                    val updateCode = tuner.generateOptimizedCode(result)

                                    outputFile.writeText(updateCode)
                                    Log.d("VocalTuner", "Results written to: ${outputFile.absolutePath}")

                                    android.widget.Toast.makeText(
                                        context,
                                        "Optimization complete! Accuracy: ${(result.accuracy * 100).toInt()}% - Results saved to ${outputFile.name}",
                                        android.widget.Toast.LENGTH_LONG
                                    ).show()
                                    Log.d("VocalTuner", "=== VOCAL TUNER SUCCESS ===")
//TO HERE=========================================================================================================
                                } catch (e: Exception) {
                                    Log.e("VocalTuner", "ERROR: ${e.message}", e)
                                    android.widget.Toast.makeText(
                                        context,
                                        "Tuner Failed: ${e.message}",
                                        android.widget.Toast.LENGTH_LONG
                                    ).show()
                                } finally {
                                    tunerRunning = false
                                    tunerProgress = "Ready"
                                    Log.d("VocalTuner", "Tuner finished - UI reset")
                                }
                            }
                            /* ```

                             **🔧 FIXES:**
                             - **Background thread:** `withContext(Dispatchers.Default)` for heavy optimization
                                     - **Extensive logging:** Every step logged with "VocalTuner" tag
                             - **UI thread safety:** File I/O back on main thread

                             **Test and check logcat:** `adb logcat | grep VocalTuner`*/
                        }
                    },
                colors = CardDefaults.cardColors(
                    containerColor = if (tunerRunning) colors.secondaryContainer else colors.surfaceVariant.copy(
                        alpha = 0.5f
                    )
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Tune,
                            contentDescription = "Tune Vocal Detector",
                            tint = if (tunerRunning) colors.onSecondaryContainer else colors.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                if (tunerRunning) "Tuning Vocal Detector..." else "Auto-Tune Vocal Detector",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                tunerProgress,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            // Add after line 985 (after the tunerProgress Text closing parenthesis):

                            // Progress bar when running
                            if (tunerRunning && currentTest > 0) {
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { progressPercentage / 100f },
                                    modifier = Modifier.fillMaxWidth(),
                                    color = colors.primary,
                                    trackColor = colors.surfaceVariant,
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    "${progressPercentage.toInt()}% complete",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                    if (tunerRunning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
            }
        }





        // 🔧 BIT (Built-In Test) Button - Only in DEBUG builds
        if (BuildConfig.DEBUG) {
            var bitRunning by remember { mutableStateOf(false) }
            var bitProgress by remember { mutableStateOf<Pair<Int, Int>?>(null) }

            Spacer(modifier = Modifier.height(8.dp))

            /*Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !bitRunning) {
                        if (!bitRunning) {
                            bitRunning = true
                            scope.launch {
                                // Get BITRunner from Hilt or create manually
                                val bitRunner = BITRunner(context, scoringEngine)

                                bitRunner.runAllTests { current, total ->
                                    bitProgress = Pair(current, total)
                                }.onSuccess { message ->
                                    android.widget.Toast.makeText(
                                        context,
                                        message,
                                        android.widget.Toast.LENGTH_LONG
                                    ).show()
                                    bitRunning = false
                                    bitProgress = null
                                }.onFailure { error ->
                                    android.widget.Toast.makeText(
                                        context,
                                        "BIT Failed: ${error.message}",
                                        android.widget.Toast.LENGTH_LONG
                                    ).show()
                                    bitRunning = false
                                    bitProgress = null
                                }
                            }
                        }
                    },
                colors = CardDefaults.cardColors(
                    containerColor = if (bitRunning) colors.secondaryContainer else colors.surfaceVariant.copy(
                        alpha = 0.5f
                    )
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.BugReport,
                            contentDescription = "Run BIT",
                            tint = if (bitRunning) colors.onSecondaryContainer else colors.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                if (bitRunning) "Running BIT..." else "Run Alignment Test (BIT)",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                bitProgress?.let { (current, total) ->
                                    "Test $current/$total"
                                } ?: "Tests forward + reverse (50 tests)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (bitRunning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
            }*/
        }
    }
}

@Composable
private fun SectionTitle(text: String, aesthetic: AestheticThemeData) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = if (aesthetic.useWideLetterSpacing) 2.sp else 0.5.sp
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 8.dp)
    )
}

@Composable
private fun SettingRow(
    label: String,
    aesthetic: AestheticThemeData,
    colors: ColorScheme,
    control: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        control()
    }
}

@Composable
fun StressTesterPanel(
    progress: ScoringStressTester.Progress?,
    onClose: () -> Unit,
    audioViewModel: AudioViewModel
) {
    val ctx = LocalContext.current
    val orchestrator = audioViewModel.getOrchestrator()

    // 🔁 Local progress state so we can update it from the tester
    var localProgress by remember { mutableStateOf(progress) }

    LaunchedEffect(Unit) {
        ScoringStressTester.runAll(
            context = ctx,
            orchestrator = orchestrator,
            onProgress = { p -> localProgress = p }
        )
        // when it finishes you’ll have the CSV in Downloads
        // you can add a Toast here if you want
    }

    // Simple card-style panel, NO verticalScroll
    Surface(
        tonalElevation = 8.dp,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                "SCORING STRESS TEST",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(12.dp))

            if (localProgress != null) {
                val p = localProgress!!
                val frac = p.current.toFloat() / p.total.toFloat()

                LinearProgressIndicator(
                    progress = { frac },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                Text("File: ${p.file}")
                Text("Difficulty: ${p.difficulty.displayName}")
                Text("Pass: ${p.pass}")
                Text("${p.current} / ${p.total}")
            } else {
                Text("Preparing tests…")
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onClose) {
                    Text("Close")
                }
            }
        }
    }
}




// ========== THEMES CONTENT ==========

@Composable
private fun ThemesContent(
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
        // 🎨 DYNAMIC THEME LIST: Get from AestheticThemes.allThemes instead of hardcoded list
        val themes = com.example.reversey.ui.theme.AestheticThemes.allThemes

        themes.forEach { (id, themeData) ->
            val name = themeData.name
            ThemeButton(
                themeName = name,
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

// ========== MENU ITEM ==========

@Composable
private fun MenuItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    aesthetic: AestheticThemeData,
    colors: ColorScheme,
    onClick: () -> Unit,
    customColor: Color? = null  // ✅ ADD THIS
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
                tint = customColor ?: getItemColor(aesthetic, colors, selected),  // ✅ USE CUSTOM COLOR IF PROVIDED
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    letterSpacing = if (aesthetic.useWideLetterSpacing) 2.sp else 0.5.sp
                ),
                color = customColor ?: getItemColor(aesthetic, colors, selected)  // ✅ USE CUSTOM COLOR IF PROVIDED
            )
        }
    }
}

// ========== THEME-SPECIFIC MODIFIERS ==========

private fun getCardModifier(aesthetic: AestheticThemeData, colors: ColorScheme): Modifier {
    return when (aesthetic.id) {
        "egg" -> Modifier
            .background(Color(0xFFFFF8E1), RoundedCornerShape(20.dp))
            .border(4.dp, Color(0xFF2E2E2E), RoundedCornerShape(20.dp))
            .shadow(6.dp, RoundedCornerShape(20.dp))

        "scrapbook" -> Modifier
            .rotate(-0.5f)
            .background(Color(0xFFFFF9E6), RoundedCornerShape(20.dp))
            .border(4.dp, Color(0xFF8B4513), RoundedCornerShape(20.dp))
            .shadow(8.dp, RoundedCornerShape(20.dp), spotColor = Color(0xFF8B4513))

        "cyberpunk" -> Modifier
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF0A0A0A), Color(0xFF1A0033))
                ),
                shape = RoundedCornerShape(8.dp)
            )
            .border(3.dp, Color(0xFF00FFFF), RoundedCornerShape(8.dp))
            .shadow(20.dp, RoundedCornerShape(8.dp), spotColor = Color(0xFF00FFFF))

        "vaporwave" -> Modifier
            .background(
                brush = aesthetic.primaryGradient,
                shape = RoundedCornerShape(24.dp)
            )
            .border(3.dp, Color(0xFFB967FF), RoundedCornerShape(24.dp))
            .shadow(20.dp, RoundedCornerShape(24.dp), spotColor = Color(0xFFB967FF))

        else -> Modifier
            .background(colors.surface, RoundedCornerShape(16.dp))
            .border(1.dp, colors.outline.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .shadow(16.dp, RoundedCornerShape(16.dp))
    }
}

private fun getHeaderModifier(aesthetic: AestheticThemeData, colors: ColorScheme): Modifier {
    return when (aesthetic.id) {
        "egg" -> Modifier
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFFFFE0B2), Color(0xFFFFD54F))
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .border(2.dp, Color(0xFF2E2E2E), RoundedCornerShape(12.dp))

        "scrapbook" -> Modifier
            .rotate(0.5f)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFD2691E), Color(0xFF8B4513))
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .shadow(3.dp, RoundedCornerShape(12.dp))

        "cyberpunk" -> Modifier
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF00FFFF), Color(0xFFFF00FF))
                ),
                shape = RoundedCornerShape(4.dp)
            )

        "vaporwave" -> Modifier
            .background(
                color = Color.White.copy(alpha = 0.9f),
                shape = RoundedCornerShape(16.dp)
            )

        else -> Modifier
            .background(colors.surfaceVariant, RoundedCornerShape(12.dp))
    }
}

private fun getItemModifier(
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

private fun getItemColor(
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

// ========== ARGB COLOR PICKER DIALOG ==========

/**
 * 🎨 ARGB COLOR PICKER DIALOG - Full spectrum color picker with sliders
 */
@Composable
private fun ARGBColorPickerDialog(
    currentColor: Color,
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    // State for ARGB values
    var alpha by remember { mutableFloatStateOf(currentColor.alpha) }
    var red by remember { mutableFloatStateOf(currentColor.red) }
    var green by remember { mutableFloatStateOf(currentColor.green) }
    var blue by remember { mutableFloatStateOf(currentColor.blue) }

    // Hex input state
    var hexInput by remember {
        mutableStateOf(
            currentColor.toArgb().toUInt().toString(16).uppercase().padStart(8, '0')
        )
    }

    // Current preview color
    val previewColor = Color(red = red, green = green, blue = blue, alpha = alpha)

    // Update hex when sliders change
    val hexFromSliders = previewColor.toArgb().toUInt().toString(16).uppercase().padStart(8, '0')

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Choose Custom Accent Color",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Color preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(previewColor)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Hex input
                OutlinedTextField(
                    value = hexInput,
                    onValueChange = { input ->
                        hexInput = input.take(8) // Limit to 8 chars
                        // Try to parse and update sliders
                        try {
                            if (input.length == 8) {
                                val colorValue = input.toULong(16).toLong()
                                val color = Color(colorValue)
                                alpha = color.alpha
                                red = color.red
                                green = color.green
                                blue = color.blue
                            }
                        } catch (e: Exception) {
                            // Invalid hex, ignore
                        }
                    },
                    label = { Text("ARGB Hex (8 digits)") },
                    placeholder = { Text("FFFFFFFF") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Text("#") }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Alpha slider
                ColorSlider(
                    label = "Alpha",
                    value = alpha,
                    onValueChange = {
                        alpha = it
                        hexInput = hexFromSliders
                    },
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Red slider
                ColorSlider(
                    label = "Red",
                    value = red,
                    onValueChange = {
                        red = it
                        hexInput = hexFromSliders
                    },
                    color = Color.Red
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Green slider
                ColorSlider(
                    label = "Green",
                    value = green,
                    onValueChange = {
                        green = it
                        hexInput = hexFromSliders
                    },
                    color = Color.Green
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Blue slider
                ColorSlider(
                    label = "Blue",
                    value = blue,
                    onValueChange = {
                        blue = it
                        hexInput = hexFromSliders
                    },
                    color = Color.Blue
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onColorSelected(previewColor) }
            ) {
                Text("Apply Color")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * 🎨 COLOR SLIDER COMPONENT - Individual ARGB component slider
 */
@Composable
private fun ColorSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    color: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${(value * 255).toInt()}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )
        }

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..1f,
            modifier = Modifier.fillMaxWidth()
        )
    }
}