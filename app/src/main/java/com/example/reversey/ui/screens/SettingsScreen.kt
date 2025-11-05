package com.example.reversey.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.reversey.ui.viewmodels.AudioViewModel
import com.example.reversey.ui.viewmodels.ThemeViewModel
import com.example.reversey.scoring.DifficultyLevel
import com.example.reversey.scoring.ScoringEngine
import com.example.reversey.scoring.ScoringPresets
import com.example.reversey.ui.debug.DebugPanel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    themeViewModel: ThemeViewModel,
    scoringEngine: ScoringEngine,
    audioViewModel: AudioViewModel,
    showDebugPanel: Boolean,
    onShowDebugPanelChange: (Boolean) -> Unit
) {
    // 🎨 Get current theme state safely
    val currentDarkModePreference by themeViewModel.darkModePreference.collectAsState()
    val customAccentColor by themeViewModel.customAccentColor.collectAsState()

    // Use local state for gameMode to avoid compilation issues
    var isGameModeEnabled by remember { mutableStateOf(false) }
    val backupRecordingsEnabled = false // Simplified for now

    val darkModeOptions = listOf("Light", "Dark", "System")
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Get current difficulty for UI feedback with state management
    var currentDifficulty by remember { mutableStateOf(scoringEngine.getCurrentDifficulty()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                actions = {  // ✅ MOVED FROM navigationIcon TO actions
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.Close, "Close")  // ✅ CHANGED TO Close icon
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier.Companion
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val showTopFade by remember { derivedStateOf { listState.canScrollBackward } }
            val showBottomFade by remember { derivedStateOf { listState.canScrollForward } }

            LazyColumn(
                state = listState,
                modifier = Modifier.Companion
                    .fillMaxSize()
                    .clip(MaterialTheme.shapes.medium)
            ) {
                item {
                    Text(
                        "Gameplay",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.Companion.padding(
                            start = 16.dp,
                            end = 16.dp,
                            top = 16.dp,
                            bottom = 8.dp
                        )
                    )
                    Row(
                        modifier = Modifier.Companion
                            .fillMaxWidth()
                            .clickable { isGameModeEnabled = !isGameModeEnabled }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.Companion.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Enable Game Mode", style = MaterialTheme.typography.bodyLarge)
                        Switch(
                            checked = isGameModeEnabled,
                            onCheckedChange = { isGameModeEnabled = it }
                        )
                    }
                }

                // 🎯 KEEP ORIGINAL WORKING SCORING DIFFICULTY SECTION
                item {
                    HorizontalDivider(modifier = Modifier.Companion.padding(vertical = 16.dp))
                    Text(
                        "Scoring Difficulty",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.Companion.padding(
                            start = 16.dp,
                            end = 16.dp,
                            top = 8.dp,
                            bottom = 8.dp
                        )
                    )

                    Text(
                        "Choose how strict the app is when scoring your singing",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.Companion.padding(
                            start = 16.dp,
                            end = 16.dp,
                            top = 8.dp,
                            bottom = 8.dp
                        )
                    )

                    // Current Difficulty Indicator
                    Card(
                        modifier = Modifier.Companion
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.Companion
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.Companion.CenterVertically
                        ) {
                            Text(
                                "Current: ${currentDifficulty.emoji} ${currentDifficulty.displayName}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Companion.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    // 🎯 SIMPLIFIED DIFFICULTY BUTTONS - Use working approach
                    Column {
                        // First row: Easy, Normal, Hard
                        Row(
                            modifier = Modifier.Companion.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SimpleDifficultyButton(
                                difficulty = DifficultyLevel.EASY,
                                isSelected = currentDifficulty == DifficultyLevel.EASY,
                                onSelect = {
                                    // Use the applyPreset function that already works
                                    scoringEngine.applyPreset(ScoringPresets.easyMode())
                                    audioViewModel.updateScoringEngine(ScoringPresets.easyMode())
                                    currentDifficulty = DifficultyLevel.EASY
                                },
                                modifier = Modifier.Companion.weight(1f).padding(horizontal = 16.dp)
                            )
                            SimpleDifficultyButton(
                                difficulty = DifficultyLevel.NORMAL,
                                isSelected = currentDifficulty == DifficultyLevel.NORMAL,
                                onSelect = {
                                    scoringEngine.applyPreset(ScoringPresets.normalMode())
                                    audioViewModel.updateScoringEngine(ScoringPresets.normalMode())
                                    currentDifficulty = DifficultyLevel.NORMAL
                                },
                                modifier = Modifier.Companion.weight(1f).padding(horizontal = 16.dp)
                            )
                            SimpleDifficultyButton(
                                difficulty = DifficultyLevel.HARD,
                                isSelected = currentDifficulty == DifficultyLevel.HARD,
                                onSelect = {
                                    scoringEngine.applyPreset(ScoringPresets.hardMode())
                                    audioViewModel.updateScoringEngine(ScoringPresets.hardMode())
                                    currentDifficulty = DifficultyLevel.HARD
                                },
                                modifier = Modifier.Companion.weight(1f).padding(horizontal = 16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.Companion.height(8.dp))

                        // Second row: Expert, Master (centered)
                        Row(
                            modifier = Modifier.Companion.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(
                                8.dp,
                                Alignment.Companion.CenterHorizontally
                            )
                        ) {
                            SimpleDifficultyButton(
                                difficulty = DifficultyLevel.EXPERT,
                                isSelected = currentDifficulty == DifficultyLevel.EXPERT,
                                onSelect = {
                                    scoringEngine.applyPreset(ScoringPresets.expertMode())
                                    audioViewModel.updateScoringEngine(ScoringPresets.expertMode())
                                    currentDifficulty = DifficultyLevel.EXPERT
                                },
                                modifier = Modifier.Companion.weight(0.5f)
                                    .padding(horizontal = 32.dp)
                            )
                            SimpleDifficultyButton(
                                difficulty = DifficultyLevel.MASTER,
                                isSelected = currentDifficulty == DifficultyLevel.MASTER,
                                onSelect = {
                                    scoringEngine.applyPreset(ScoringPresets.masterMode())
                                    audioViewModel.updateScoringEngine(ScoringPresets.masterMode())
                                    currentDifficulty = DifficultyLevel.MASTER
                                },
                                modifier = Modifier.Companion.weight(0.5f)
                                    .padding(horizontal = 32.dp)
                            )
                        }
                    }
                }

                // 🎨 NEW: FIXED APPEARANCE SECTION
                item {
                    HorizontalDivider(modifier = Modifier.Companion.padding(vertical = 16.dp))
                    Text(
                        "Appearance",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.Companion.padding(
                            start = 16.dp,
                            end = 16.dp,
                            top = 8.dp,
                            bottom = 8.dp
                        )
                    )

                    // 🎨 CUSTOM ACCENT COLOR PICKER - PROPER ARGB IMPLEMENTATION
                    Text(
                        "Custom Accent Color",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.Companion.padding(
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 8.dp
                        )
                    )

                    Text(
                        "Choose a custom accent color that works across all themes",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.Companion.padding(
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 12.dp
                        )
                    )

                    // Color picker section with dialog
                    var showColorPicker by remember { mutableStateOf(false) }

                    Card(
                        modifier = Modifier.Companion
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clickable { showColorPicker = true },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.Companion
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.Companion.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.Companion.CenterVertically) {
                                Icon(
                                    Icons.Default.Palette,
                                    contentDescription = "Color Picker",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.Companion.width(12.dp))
                                Column {
                                    Text(
                                        "Open Color Picker",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Companion.Medium
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
                                    modifier = Modifier.Companion
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(customAccentColor!!)
                                        .border(
                                            1.dp,
                                            MaterialTheme.colorScheme.outline,
                                            CircleShape
                                        )
                                )
                            }
                        }
                    }

                    // Current color indicator and reset
                    if (customAccentColor != null) {
                        Spacer(modifier = Modifier.Companion.height(12.dp))
                        Row(
                            modifier = Modifier.Companion
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.Companion.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.Companion.CenterVertically) {
                                Text(
                                    "Current accent: ",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Box(
                                    modifier = Modifier.Companion
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(customAccentColor!!)
                                        .border(
                                            1.dp,
                                            MaterialTheme.colorScheme.outline,
                                            CircleShape
                                        )
                                )
                                Spacer(modifier = Modifier.Companion.width(8.dp))
                                Text(
                                    "#${
                                        customAccentColor!!.toArgb().toUInt().toString(16)
                                            .uppercase().padStart(8, '0')
                                    }",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            TextButton(
                                onClick = { themeViewModel.clearCustomAccentColor() }
                            ) {
                                Text("Reset")
                            }
                        }
                    }

                    // Color Picker Dialog
                    if (showColorPicker) {
                        ARGBColorPickerDialog(
                            currentColor = customAccentColor ?: Color.Companion.Blue,
                            onColorSelected = { color ->
                                themeViewModel.setCustomAccentColor(color)
                                showColorPicker = false
                            },
                            onDismiss = { showColorPicker = false }
                        )
                    }

                    // Dark Mode Section
                    Spacer(modifier = Modifier.Companion.height(24.dp))
                    Text(
                        "Dark Mode",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.Companion.padding(
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 8.dp
                        )
                    )

                    Column(modifier = Modifier.Companion.fillMaxWidth()) {
                        darkModeOptions.forEach { option ->
                            Row(
                                modifier = Modifier.Companion
                                    .fillMaxWidth()
                                    .clickable { themeViewModel.setDarkModePreference(option) }
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.Companion.CenterVertically
                            ) {
                                RadioButton(
                                    selected = currentDarkModePreference == option,
                                    onClick = { themeViewModel.setDarkModePreference(option) }
                                )
                                Spacer(modifier = Modifier.Companion.width(8.dp))
                                Text(
                                    text = option,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }

                // About Section
                item {
                    HorizontalDivider(modifier = Modifier.Companion.padding(vertical = 16.dp))
                    Text(
                        "About",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.Companion.padding(
                            start = 16.dp,
                            end = 16.dp,
                            top = 8.dp,
                            bottom = 8.dp
                        )
                    )
                    Button(
                        onClick = {
                            scope.launch {
                                themeViewModel.settingsDataStore.setTutorialCompleted(false)
                                navController.navigate("home")
                            }
                        },
                        modifier = Modifier.Companion
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.Companion.padding(end = 8.dp)
                        )
                        Text("View Tutorial")
                    }
                }

                // Developer Options
                item {
                    if (true) { // Simplified check instead of BuildConfig.DEBUG
                        HorizontalDivider(modifier = Modifier.Companion.padding(vertical = 16.dp))
                        Text(
                            "Developer Options",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.Companion.padding(
                                start = 16.dp,
                                end = 16.dp,
                                top = 8.dp,
                                bottom = 8.dp
                            )
                        )
                        Card(
                            modifier = Modifier.Companion
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.Companion
                                    .fillMaxWidth()
                                    .clickable { onShowDebugPanelChange(!showDebugPanel) }
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Companion.CenterVertically
                            ) {
                                Text("Advanced Settings Panel")
                                Switch(
                                    checked = showDebugPanel,
                                    onCheckedChange = onShowDebugPanelChange
                                )
                            }
                        }
                    }
                }
            }

            // Scroll glow effects
            val topGradient = Brush.Companion.verticalGradient(
                0.0f to MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                1.0f to Color.Companion.Transparent
            )
            val bottomGradient = Brush.Companion.verticalGradient(
                0.0f to Color.Companion.Transparent,
                1.0f to MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )

            if (showTopFade) {
                Box(
                    modifier = Modifier.Companion
                        .fillMaxWidth()
                        .height(24.dp)
                        .align(Alignment.Companion.TopCenter)
                        .clip(MaterialTheme.shapes.medium)
                        .background(topGradient)
                )
            }

            if (showBottomFade) {
                Box(
                    modifier = Modifier.Companion
                        .fillMaxWidth()
                        .height(24.dp)
                        .align(Alignment.Companion.BottomCenter)
                        .clip(MaterialTheme.shapes.medium)
                        .background(bottomGradient)
                )
            }

            // Debug Panel
            DebugPanel(
                scoringEngine = scoringEngine,
                isVisible = showDebugPanel,
                onDismiss = { onShowDebugPanelChange(false) }
            )
        }
    }
}

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
    val previewColor =
        androidx.compose.ui.graphics.Color(red = red, green = green, blue = blue, alpha = alpha)

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
                modifier = Modifier.Companion.fillMaxWidth()
            ) {
                // Color preview
                Box(
                    modifier = Modifier.Companion
                        .fillMaxWidth()
                        .height(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(previewColor)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline,
                            androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                        )
                )

                Spacer(modifier = Modifier.Companion.height(16.dp))

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
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Companion.Text),
                    modifier = Modifier.Companion.fillMaxWidth(),
                    leadingIcon = { Text("#") }
                )

                Spacer(modifier = Modifier.Companion.height(16.dp))

                // Alpha slider
                ColorSlider(
                    label = "Alpha",
                    value = alpha,
                    onValueChange = {
                        alpha = it
                        hexInput = hexFromSliders
                    },
                    color = Color.Companion.Gray
                )

                Spacer(modifier = Modifier.Companion.height(8.dp))

                // Red slider
                ColorSlider(
                    label = "Red",
                    value = red,
                    onValueChange = {
                        red = it
                        hexInput = hexFromSliders
                    },
                    color = Color.Companion.Red
                )

                Spacer(modifier = Modifier.Companion.height(8.dp))

                // Green slider
                ColorSlider(
                    label = "Green",
                    value = green,
                    onValueChange = {
                        green = it
                        hexInput = hexFromSliders
                    },
                    color = Color.Companion.Green
                )

                Spacer(modifier = Modifier.Companion.height(8.dp))

                // Blue slider
                ColorSlider(
                    label = "Blue",
                    value = blue,
                    onValueChange = {
                        blue = it
                        hexInput = hexFromSliders
                    },
                    color = Color.Companion.Blue
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
            modifier = Modifier.Companion.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Companion.CenterVertically
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
                fontFamily = FontFamily.Monospace
            )
        }

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..1f,
            modifier = Modifier.Companion.fillMaxWidth()
        )
    }
}

/**
 * 🎯 SIMPLE DIFFICULTY BUTTON - Uses lowercase preset names that actually exist
 */
@Composable
private fun SimpleDifficultyButton(
    difficulty: DifficultyLevel,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier.Companion
) {
    val glowColor = when (difficulty) {
        DifficultyLevel.EASY -> Color(0xFF4CAF50)      // Green
        DifficultyLevel.NORMAL -> Color(0xFF2196F3)    // Blue
        DifficultyLevel.HARD -> Color(0xFFFF9800)      // Orange
        DifficultyLevel.EXPERT -> Color(0xFF9C27B0)    // Purple
        DifficultyLevel.MASTER -> Color(0xFFFFD700)    // Gold
    }

    val containerColor = if (isSelected) {
        glowColor.copy(alpha = 0.2f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = modifier
            .then(
                if (isSelected) {
                    Modifier.Companion
                        .border(
                            2.dp,
                            glowColor,
                            androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                        )
                        .shadow(
                            8.dp,
                            androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                            ambientColor = glowColor,
                            spotColor = glowColor
                        )
                } else {
                    Modifier.Companion
                }
            )
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.Companion.CenterHorizontally,
            modifier = Modifier.Companion
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = difficulty.emoji,
                fontSize = 24.sp,
                modifier = Modifier.Companion.padding(bottom = 4.dp)
            )
            Text(
                text = difficulty.displayName,
                fontWeight = FontWeight.Companion.Bold,
                fontSize = 14.sp,
                color = if (isSelected) glowColor else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = when (difficulty) {
                    DifficultyLevel.EASY -> "Forgiving"
                    DifficultyLevel.NORMAL -> "Balanced"
                    DifficultyLevel.HARD -> "Strict"
                    DifficultyLevel.EXPERT -> "Very Strict"
                    DifficultyLevel.MASTER -> "Perfection"
                },
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}