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
        }

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
        }

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

        HorizontalDivider(color = aesthetic.cardBorder.copy(alpha = 0.3f))

        SectionTitle("DEVELOPER OPTIONS", aesthetic)

        // ========== STRESS TESTER ==========
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
                                    tunerProgress = "Initializing..."
                                    val audioProcessor = AudioProcessor()
                                    val tuner = VocalModeDetectorTuner(audioProcessor)

                                    tunerProgress = "Loading training data..."
                                    val dataLoaded = tuner.loadTrainingDataFromAssets(context)

                                    if (!dataLoaded) throw Exception("Failed to load training data")

                                    tunerProgress = "Pre-processing data..."
                                    val preprocessed = tuner.preProcessTrainingData { status -> tunerProgress = status }

                                    if (!preprocessed) throw Exception("Failed to pre-process data")

                                    tunerProgress = "Running optimization..."
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

                                    tunerProgress = "Writing results..."
                                    val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(
                                        android.os.Environment.DIRECTORY_DOWNLOADS
                                    )
                                    val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.US).format(java.util.Date())
                                    val outputFile = java.io.File(downloadsDir, "ReVerseY_VocalTuner_${timestamp}.txt")
                                    outputFile.writeText(tuner.generateOptimizedCode(result))

                                    Toast.makeText(context, "Optimization complete! Saved to Downloads", Toast.LENGTH_LONG).show()

                                } catch (e: Exception) {
                                    Log.e("VocalTuner", "ERROR: ${e.message}", e)
                                    Toast.makeText(context, "Tuner Failed: ${e.message}", Toast.LENGTH_LONG).show()
                                } finally {
                                    tunerRunning = false
                                    tunerProgress = "Ready"
                                }
                            }
                        }
                    },
                colors = CardDefaults.cardColors(
                    containerColor = if (tunerRunning) colors.secondaryContainer else colors.surfaceVariant.copy(alpha = 0.5f)
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
                            if (tunerRunning && currentTest > 0) {
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { progressPercentage / 100f },
                                    modifier = Modifier.fillMaxWidth(),
                                    color = colors.primary,
                                    trackColor = colors.surfaceVariant,
                                )
                            }
                        }
                    }
                    if (tunerRunning) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
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