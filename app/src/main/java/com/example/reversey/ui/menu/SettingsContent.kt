package com.example.reversey.ui.menu

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.reversey.BuildConfig
import com.example.reversey.audio.processing.AudioProcessor
import com.example.reversey.scoring.DifficultyConfig
import com.example.reversey.testing.ScoringStressTester
import com.example.reversey.testing.VocalModeDetectorTuner
import com.example.reversey.ui.components.DifficultyButton
import com.example.reversey.ui.theme.AestheticThemeData
import com.example.reversey.ui.theme.SharedDefaultComponents
import com.example.reversey.ui.viewmodels.AudioViewModel
import com.example.reversey.ui.viewmodels.ThemeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SettingsContent(
    aesthetic: AestheticThemeData,
    colors: ColorScheme,
    themeViewModel: ThemeViewModel,
    audioViewModel: AudioViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // State collection
    val currentDifficulty by audioViewModel.currentDifficultyFlow.collectAsState()
    val isGameModeEnabled by themeViewModel.gameModeEnabled.collectAsState()
    val darkModePreference by themeViewModel.darkModePreference.collectAsState()
    val backupRecordingsEnabled by themeViewModel.backupRecordingsEnabled.collectAsState()
    val customAccentColor by themeViewModel.customAccentColor.collectAsState()

    // 🎨 Theme Colors from the new MenuColors object
    val menuColors = aesthetic.menuColors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ========== GAMEPLAY SECTION ==========
        SectionTitle("GAMEPLAY", aesthetic)

        SharedDefaultComponents.ThemedSettingsCard(aesthetic) {
            SharedDefaultComponents.ThemedToggle(
                aesthetic = aesthetic,
                label = "Enable Game Mode",
                checked = isGameModeEnabled,
                onCheckedChange = { scope.launch { themeViewModel.setGameMode(it) } }
            )
        }

        HorizontalDivider(color = menuColors.menuDivider)

        // ========== DIFFICULTY SECTION ==========
        SectionTitle("SCORING DIFFICULTY", aesthetic)

        Text(
            text = "Choose your challenge level",
            style = MaterialTheme.typography.bodyMedium,
            color = menuColors.menuItemText,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

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

        HorizontalDivider(color = menuColors.menuDivider)

        // ========== APPEARANCE SECTION ==========
        SectionTitle("APPEARANCE", aesthetic)

        SharedDefaultComponents.ThemedSettingsCard(aesthetic, title = "Dark Mode") {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf("Light", "Dark", "System").forEach { mode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { scope.launch { themeViewModel.setDarkModePreference(mode) } }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = darkModePreference == mode,
                            onClick = { scope.launch { themeViewModel.setDarkModePreference(mode) } },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = menuColors.toggleActive,
                                unselectedColor = menuColors.toggleInactive
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = mode,
                            style = MaterialTheme.typography.bodyLarge,
                            color = menuColors.menuItemText
                        )
                    }
                }
            }
        }

        // Custom Accent Color
        SharedDefaultComponents.ThemedSettingsCard(aesthetic, title = "Custom Accent Color") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (customAccentColor != null) "Custom color active" else "Using theme default",
                    style = MaterialTheme.typography.bodyMedium,
                    color = menuColors.menuItemText
                )

                if (customAccentColor != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .border(1.5.dp, menuColors.menuBorder, RoundedCornerShape(6.dp))
                            .background(menuColors.menuBorder.copy(alpha = 0.1f))
                            .clickable { scope.launch { themeViewModel.setCustomAccentColor(null) } }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Reset",
                            style = MaterialTheme.typography.bodySmall,
                            color = menuColors.menuTitleText,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            var showColorPicker by remember { mutableStateOf(false) }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(menuColors.menuItemBackground)
                    .clickable { showColorPicker = true }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Palette,
                        contentDescription = "Color Picker",
                        tint = menuColors.toggleActive
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Pick Color",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = menuColors.menuTitleText
                    )
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(customAccentColor ?: colors.primary)
                        .border(2.dp, menuColors.menuBorder, CircleShape)
                )
            }

            if (showColorPicker) {
                ARGBColorPickerDialog(
                    currentColor = customAccentColor ?: colors.primary,
                    onColorSelected = { color ->
                        scope.launch { themeViewModel.setCustomAccentColor(color) }
                        showColorPicker = false
                    },
                    onDismiss = { showColorPicker = false }
                )
            }
        }

        HorizontalDivider(color = menuColors.menuDivider)

        // ========== STORAGE SECTION ==========
        SectionTitle("STORAGE", aesthetic)

        SharedDefaultComponents.ThemedSettingsCard(aesthetic) {
            SharedDefaultComponents.ThemedToggle(
                aesthetic = aesthetic,
                label = "Backup Recordings to Drive",
                checked = backupRecordingsEnabled,
                onCheckedChange = { scope.launch { themeViewModel.setBackupRecordingsEnabled(it) } }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "ℹ️ Settings and scores are always backed up. Audio files are only backed up if enabled.",
                style = MaterialTheme.typography.bodySmall,
                color = menuColors.menuItemText.copy(alpha = 0.7f)
            )
        }

        HorizontalDivider(color = menuColors.menuDivider)

        SectionTitle("DEVELOPER OPTIONS", aesthetic)

        // ========== STRESS TESTER ==========
        var showStressTester by remember { mutableStateOf(false) }
        var progress by remember { mutableStateOf<ScoringStressTester.Progress?>(null) }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = menuColors.menuCardBackground),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showStressTester = true }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Scoring Stress Tester",
                    style = MaterialTheme.typography.titleMedium,
                    color = menuColors.menuTitleText
                )
            }
        }

        if (showStressTester) {
            StressTesterPanel(
                progress = progress,
                onClose = { showStressTester = false },
                audioViewModel = audioViewModel
            )
        }

        // ========== VOCAL TUNER (Debug Only) ==========
        if (BuildConfig.DEBUG) {
            var tunerRunning by remember { mutableStateOf(false) }
            var tunerProgress by remember { mutableStateOf("Ready") }
            var currentTest by remember { mutableStateOf(0) }
            var totalTests by remember { mutableStateOf(4000) }
            var progressPercentage by remember { mutableStateOf(0f) }

            SharedDefaultComponents.ThemedSettingsCard(aesthetic) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !tunerRunning) {
                            if (!tunerRunning) {
                                tunerRunning = true
                                scope.launch {
                                    try {
                                        Log.d("VocalTuner", "=== VOCAL TUNER START ===")
                                        tunerProgress = "Initializing..."
                                        val audioProcessor = AudioProcessor()
                                        val tuner = VocalModeDetectorTuner(audioProcessor)
                                        val dataLoaded = tuner.loadTrainingDataFromAssets(context)
                                        if (!dataLoaded) throw Exception("Failed to load training data")
                                        tunerProgress = "Pre-processing..."
                                        val preprocessed = tuner.preProcessTrainingData { tunerProgress = it }
                                        if (!preprocessed) throw Exception("Failed to pre-process")
                                        tunerProgress = "Optimizing..."
                                        val result = withContext(Dispatchers.Default) {
                                            tuner.findOptimalParameters { progressString ->
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
                                        }
                                        if (result == null) throw Exception("Optimization failed")
                                        tunerProgress = "Complete!"
                                        Toast.makeText(context, "Tuning Complete", Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        Log.e("VocalTuner", "ERROR: ${e.message}", e)
                                        Toast.makeText(context, "Failed: ${e.message}", Toast.LENGTH_LONG).show()
                                    } finally {
                                        tunerRunning = false
                                    }
                                }
                            }
                        }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Tune,
                            contentDescription = "Tune",
                            tint = if (tunerRunning) menuColors.toggleActive else menuColors.menuTitleText
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                if (tunerRunning) "Tuning..." else "Auto-Tune Detector",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = menuColors.menuTitleText
                            )
                            Text(
                                tunerProgress,
                                style = MaterialTheme.typography.bodySmall,
                                color = menuColors.menuItemText
                            )
                            if (tunerRunning && currentTest > 0) {
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { progressPercentage / 100f },
                                    modifier = Modifier.fillMaxWidth(),
                                    color = menuColors.toggleActive,
                                    trackColor = menuColors.toggleInactive,
                                )
                            }
                        }
                    }
                    if (tunerRunning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = menuColors.toggleActive
                        )
                    }
                }
            }
        }
    }
}

// --- Private Components ---

@Composable
private fun SectionTitle(text: String, aesthetic: AestheticThemeData) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = if (aesthetic.useWideLetterSpacing) 2.sp else 0.5.sp
        ),
        color = aesthetic.menuColors.menuTitleText,
        modifier = Modifier.padding(horizontal = 8.dp)
    )
}

@Composable
fun StressTesterPanel(
    progress: ScoringStressTester.Progress?,
    onClose: () -> Unit,
    audioViewModel: AudioViewModel
) {
    val ctx = LocalContext.current
    val orchestrator = audioViewModel.getOrchestrator()
    var localProgress by remember { mutableStateOf(progress) }

    LaunchedEffect(Unit) {
        ScoringStressTester.runAll(
            context = ctx,
            orchestrator = orchestrator,
            onProgress = { p -> localProgress = p }
        )
    }

    Surface(
        tonalElevation = 8.dp,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(20.dp)) {
            Text("SCORING STRESS TEST", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(12.dp))

            if (localProgress != null) {
                val p = localProgress!!
                val frac = p.current.toFloat() / p.total.toFloat()
                LinearProgressIndicator(progress = { frac }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                Text("File: ${p.file}")
                Text("Difficulty: ${p.difficulty.displayName}")
                Text("Pass: ${p.pass}")
                Text("${p.current} / ${p.total}")
            } else {
                Text("Preparing tests…")
            }

            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onClose) { Text("Close") }
            }
        }
    }
}

@Composable
private fun ARGBColorPickerDialog(
    currentColor: Color,
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    var alpha by remember { mutableFloatStateOf(currentColor.alpha) }
    var red by remember { mutableFloatStateOf(currentColor.red) }
    var green by remember { mutableFloatStateOf(currentColor.green) }
    var blue by remember { mutableFloatStateOf(currentColor.blue) }

    var hexInput by remember {
        mutableStateOf(currentColor.toArgb().toUInt().toString(16).uppercase().padStart(8, '0'))
    }

    val previewColor = Color(red = red, green = green, blue = blue, alpha = alpha)
    val hexFromSliders = previewColor.toArgb().toUInt().toString(16).uppercase().padStart(8, '0')

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose Custom Accent Color", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(previewColor)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = hexInput,
                    onValueChange = { input ->
                        hexInput = input.take(8)
                        try {
                            if (input.length == 8) {
                                val colorValue = input.toULong(16).toLong()
                                val color = Color(colorValue)
                                alpha = color.alpha; red = color.red; green = color.green; blue = color.blue
                            }
                        } catch (e: Exception) { }
                    },
                    label = { Text("ARGB Hex (8 digits)") },
                    placeholder = { Text("FFFFFFFF") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Text("#") }
                )
                Spacer(modifier = Modifier.height(16.dp))
                ColorSlider(label = "Alpha", value = alpha, onValueChange = { alpha = it; hexInput = hexFromSliders }, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                ColorSlider(label = "Red", value = red, onValueChange = { red = it; hexInput = hexFromSliders }, color = Color.Red)
                Spacer(modifier = Modifier.height(8.dp))
                ColorSlider(label = "Green", value = green, onValueChange = { green = it; hexInput = hexFromSliders }, color = Color.Green)
                Spacer(modifier = Modifier.height(8.dp))
                ColorSlider(label = "Blue", value = blue, onValueChange = { blue = it; hexInput = hexFromSliders }, color = Color.Blue)
            }
        },
        confirmButton = { Button(onClick = { onColorSelected(previewColor) }) { Text("Apply Color") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun ColorSlider(label: String, value: Float, onValueChange: (Float) -> Unit, color: Color) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = "${(value * 255).toInt()}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Slider(value = value, onValueChange = onValueChange, valueRange = 0f..1f, modifier = Modifier.fillMaxWidth())
    }
}