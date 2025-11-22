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
import androidx.compose.ui.draw.shadow
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
import com.example.reversey.ui.viewmodels.AudioViewModel
import com.example.reversey.ui.viewmodels.ThemeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SettingsContent(
    themeViewModel: ThemeViewModel,
    audioViewModel: AudioViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val colors = MaterialTheme.colorScheme

    // State collection
    val currentDifficulty by audioViewModel.currentDifficultyFlow.collectAsState()
    val isGameModeEnabled by themeViewModel.gameModeEnabled.collectAsState()
    val darkModePreference by themeViewModel.darkModePreference.collectAsState()
    val backupRecordingsEnabled by themeViewModel.backupRecordingsEnabled.collectAsState()
    val customAccentColor by themeViewModel.customAccentColor.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ========== GAMEPLAY SECTION ==========
        SectionTitle("GAMEPLAY")

        GlassCard {
            GlassToggle(
                label = "Enable Game Mode",
                checked = isGameModeEnabled,
                onCheckedChange = { scope.launch { themeViewModel.setGameMode(it) } }
            )
        }

        GlassDivider()

        // ========== DIFFICULTY SECTION ==========
        SectionTitle("SCORING DIFFICULTY")

        Text(
            text = "Choose your challenge level",
            style = MaterialTheme.typography.bodyMedium,
            color = StaticMenuColors.textSecondary,
            modifier = Modifier.padding(horizontal = 4.dp)
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

        GlassDivider()

        // ========== APPEARANCE SECTION ==========
        SectionTitle("APPEARANCE")

        GlassCard(title = "Dark Mode") {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf("Light", "Dark", "System").forEach { mode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { scope.launch { themeViewModel.setDarkModePreference(mode) } }
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = darkModePreference == mode,
                            onClick = { scope.launch { themeViewModel.setDarkModePreference(mode) } },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = StaticMenuColors.toggleActive,
                                unselectedColor = StaticMenuColors.textOnCard.copy(alpha = 0.5f)
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = mode,
                            style = MaterialTheme.typography.bodyLarge,
                            color = StaticMenuColors.textOnCard
                        )
                    }
                }
            }
        }

        // Custom Accent Color
        GlassCard(title = "Custom Accent Color") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (customAccentColor != null) "Custom color active" else "Using theme default",
                    style = MaterialTheme.typography.bodyMedium,
                    color = StaticMenuColors.textOnCard.copy(alpha = 0.8f)
                )

                if (customAccentColor != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(StaticMenuColors.toggleActive.copy(alpha = 0.15f))
                            .clickable { scope.launch { themeViewModel.setCustomAccentColor(null) } }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Reset",
                            style = MaterialTheme.typography.bodySmall,
                            color = StaticMenuColors.toggleActive,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            var showColorPicker by remember { mutableStateOf(false) }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(StaticMenuColors.settingsInputBackground)
                    .clickable { showColorPicker = true }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Palette,
                        contentDescription = "Color Picker",
                        tint = StaticMenuColors.toggleActive
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Pick Color",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = StaticMenuColors.textOnCard
                    )
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(customAccentColor ?: colors.primary)
                        .border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape)
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

        GlassDivider()

        // ========== STORAGE SECTION ==========
        SectionTitle("STORAGE")

        GlassCard {
            GlassToggle(
                label = "Backup Recordings to Drive",
                checked = backupRecordingsEnabled,
                onCheckedChange = { scope.launch { themeViewModel.setBackupRecordingsEnabled(it) } }
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "ℹ️ Settings and scores are always backed up. Audio files are only backed up if enabled.",
                style = MaterialTheme.typography.bodySmall,
                color = StaticMenuColors.textOnCard.copy(alpha = 0.6f)
            )
        }

        GlassDivider()

        SectionTitle("DEVELOPER OPTIONS")

        // ========== STRESS TESTER ==========
        var showStressTester by remember { mutableStateOf(false) }
        var progress by remember { mutableStateOf<ScoringStressTester.Progress?>(null) }

        GlassCard {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showStressTester = true },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Scoring Stress Tester",
                    style = MaterialTheme.typography.titleMedium,
                    color = StaticMenuColors.textOnCard,
                    fontWeight = FontWeight.Medium
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

            GlassCard {
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
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Tune,
                            contentDescription = "Tune",
                            tint = if (tunerRunning) StaticMenuColors.toggleActive else StaticMenuColors.textOnCard
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                if (tunerRunning) "Tuning..." else "Auto-Tune Detector",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = StaticMenuColors.textOnCard
                            )
                            Text(
                                tunerProgress,
                                style = MaterialTheme.typography.bodySmall,
                                color = StaticMenuColors.textOnCard.copy(alpha = 0.6f)
                            )
                            if (tunerRunning && currentTest > 0) {
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { progressPercentage / 100f },
                                    modifier = Modifier.fillMaxWidth(),
                                    color = StaticMenuColors.toggleActive,
                                    trackColor = StaticMenuColors.toggleInactive,
                                )
                            }
                        }
                    }
                    if (tunerRunning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = StaticMenuColors.toggleActive
                        )
                    }
                }
            }
        }

        // Bottom spacing
        Spacer(modifier = Modifier.height(8.dp))
    }
}

// ========== REUSABLE COMPONENTS ==========

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.dp.value.sp
        ),
        color = StaticMenuColors.textOnGradient,
        modifier = Modifier.padding(horizontal = 4.dp)
    )
}

@Composable
private fun GlassCard(
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(16.dp)

    Column(modifier = Modifier.fillMaxWidth()) {
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = StaticMenuColors.textOnGradient,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, shape)
                .clip(shape)
                .background(StaticMenuColors.settingsCardBackground)
                .border(1.dp, Color.White.copy(alpha = 0.3f), shape)
                .padding(18.dp)
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
private fun GlassToggle(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = StaticMenuColors.textOnCard
        )

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = StaticMenuColors.toggleActive,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = StaticMenuColors.toggleInactive
            )
        )
    }
}

@Composable
private fun GlassDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(StaticMenuColors.divider)
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

    GlassCard {
        Column(Modifier.fillMaxWidth()) {
            Text(
                "SCORING STRESS TEST",
                style = MaterialTheme.typography.titleLarge,
                color = StaticMenuColors.textOnCard,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))

            if (localProgress != null) {
                val p = localProgress!!
                val frac = p.current.toFloat() / p.total.toFloat()
                LinearProgressIndicator(
                    progress = { frac },
                    modifier = Modifier.fillMaxWidth(),
                    color = StaticMenuColors.toggleActive
                )
                Spacer(Modifier.height(10.dp))
                Text("File: ${p.file}", color = StaticMenuColors.textOnCard)
                Text("Difficulty: ${p.difficulty.displayName}", color = StaticMenuColors.textOnCard)
                Text("Pass: ${p.pass}", color = StaticMenuColors.textOnCard)
                Text("${p.current} / ${p.total}", color = StaticMenuColors.textOnCard)
            } else {
                Text("Preparing tests…", color = StaticMenuColors.textOnCard)
            }

            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onClose) {
                    Text("Close", color = StaticMenuColors.toggleActive)
                }
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
                        .clip(RoundedCornerShape(12.dp))
                        .background(previewColor)
                        .border(2.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
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
        confirmButton = {
            Button(
                onClick = { onColorSelected(previewColor) },
                colors = ButtonDefaults.buttonColors(containerColor = StaticMenuColors.toggleActive)
            ) { Text("Apply Color") }
        },
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
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..1f,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = color,
                activeTrackColor = color
            )
        )
    }
}
