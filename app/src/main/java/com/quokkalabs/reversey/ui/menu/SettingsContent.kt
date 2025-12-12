package com.quokkalabs.reversey.ui.menu

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quokkalabs.reversey.BuildConfig
import com.quokkalabs.reversey.audio.processing.AudioProcessor
import com.quokkalabs.reversey.data.backup.BackupManager
import com.quokkalabs.reversey.scoring.DifficultyConfig


import com.quokkalabs.reversey.ui.components.DifficultyButton
import com.quokkalabs.reversey.ui.viewmodels.AudioViewModel
import com.quokkalabs.reversey.ui.viewmodels.ThemeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.material.icons.filled.BugReport
import com.quokkalabs.reversey.asr.DualMicTest
import com.quokkalabs.reversey.asr.VoskTranscriptionHelper
import java.io.File

@Composable
fun SettingsContent(
    themeViewModel: ThemeViewModel,
    audioViewModel: AudioViewModel,
    backupManager: BackupManager,
    onBackupComplete: () -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val colors = MaterialTheme.colorScheme

    // State collection
    val currentDifficulty by audioViewModel.currentDifficultyFlow.collectAsState()
    val isGameModeEnabled by themeViewModel.gameModeEnabled.collectAsState()
    val darkModePreference by themeViewModel.darkModePreference.collectAsState()
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

        // ========== DEVELOPER OPTIONS SECTION ==========
        if (BuildConfig.DEBUG) {
            SectionTitle("DEVELOPER OPTIONS")

            //START==========hacky test buttons!============
            val context = LocalContext.current
            val scope = rememberCoroutineScope()

            // Create helper directly for testing
            val voskHelper = remember { VoskTranscriptionHelper(context) }

            Button(onClick = { DualMicTest(context).runSpeechOnlyTest() }) {
                Text("Run Dual Mic Test")
            }

            // Button 1: Init (do once)
            Button(onClick = {
                scope.launch {
                    Log.d("TEST", "Vosk init: ${voskHelper.initialize()}")
                }
            }) { Text("Init Vosk") }

// Button 2: Transcribe last recording
            Button(onClick = {
                scope.launch {
                    if (!voskHelper.isReady()) {
                        Log.d("TEST", "Initializing Vosk first...")
                        voskHelper.initialize()
                    }

                    // Find the most recent FORWARD recording (not reversed)
                    val recordingsDir = File(context.filesDir, "recordings")
                    val latestFile = recordingsDir.listFiles()
                        ?.filter { it.extension == "wav" && !it.name.contains("reversed") }
                        ?.maxByOrNull { it.lastModified() }

                    if (latestFile != null) {
                        Log.d("TEST", "Testing with: ${latestFile.name}")
                        val result = voskHelper.transcribeFile(latestFile)
                        Log.d("TEST", "Vosk: '${result.text}' (${result.status})")
                    } else {
                        Log.d("TEST", "No recordings found!")
                    }
                }
            }) { Text("Test Vosk") }

            //END==========hacky test buttons!============

            // ========== BIT RUNNER (Synthetic Tests) ==========
            /*var bitRunning by remember { mutableStateOf(false) }
            var bitProgress by remember { mutableStateOf("Ready") }
            var bitCurrent by remember { mutableStateOf(0) }
            var bitTotal by remember { mutableStateOf(15) }

            GlassCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !bitRunning) {
                            if (!bitRunning) {
                                bitRunning = true
                                bitProgress = "Starting..."
                                scope.launch {
                                    try {
                                        // Get BITRunner from AudioViewModel
                                        val bitRunner = audioViewModel.getBITRunner()

                                        val result = bitRunner.runAllTests { current, total ->
                                            bitCurrent = current
                                            bitTotal = total
                                            bitProgress = "Test $current/$total"
                                        }

                                        result.fold(
                                            onSuccess = { message ->
                                                bitProgress = "Complete!"
                                                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                            },
                                            onFailure = { error ->
                                                bitProgress = "Failed"
                                                Toast.makeText(
                                                    context,
                                                    "BIT Failed: ${error.message}",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        )
                                    } catch (e: Exception) {
                                        bitProgress = "Error"
                                        Toast.makeText(context, "BIT Error: ${e.message}", Toast.LENGTH_LONG)
                                            .show()
                                    } finally {
                                        bitRunning = false
                                    }
                                }
                            }
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.BugReport,
                            contentDescription = "BIT",
                            tint = if (bitRunning) StaticMenuColors.toggleActive else StaticMenuColors.textOnCard
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                if (bitRunning) "Running BIT..." else "Built-In Test (Synthetic)",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = StaticMenuColors.textOnCard
                            )
                            Text(
                                bitProgress,
                                style = MaterialTheme.typography.bodySmall,
                                color = StaticMenuColors.textOnCard.copy(alpha = 0.6f)
                            )
                            if (bitRunning && bitCurrent > 0) {
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { bitCurrent.toFloat() / bitTotal.toFloat() },
                                    modifier = Modifier.fillMaxWidth(),
                                    color = StaticMenuColors.toggleActive,
                                    trackColor = StaticMenuColors.toggleInactive,
                                )
                            }
                        }
                    }
                    if (bitRunning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = StaticMenuColors.toggleActive
                        )
                    }
                }
            }*/
        }
        // Bottom spacing
        Spacer(modifier = Modifier.height(16.dp))
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
    content: @Composable ColumnScope.() -> Unit,
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
    onCheckedChange: (Boolean) -> Unit,
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
private fun ARGBColorPickerDialog(
    currentColor: Color,
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit,
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
        title = {
            Text(
                "Choose Custom Accent Color",
                style = MaterialTheme.typography.headlineSmall
            )
        },
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
                                alpha = color.alpha; red = color.red; green = color.green; blue =
                                    color.blue
                            }
                        } catch (e: Exception) {
                        }
                    },
                    label = { Text("ARGB Hex (8 digits)") },
                    placeholder = { Text("FFFFFFFF") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Text("#") }
                )
                Spacer(modifier = Modifier.height(16.dp))
                ColorSlider(
                    label = "Alpha",
                    value = alpha,
                    onValueChange = { alpha = it; hexInput = hexFromSliders },
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                ColorSlider(
                    label = "Red",
                    value = red,
                    onValueChange = { red = it; hexInput = hexFromSliders },
                    color = Color.Red
                )
                Spacer(modifier = Modifier.height(8.dp))
                ColorSlider(
                    label = "Green",
                    value = green,
                    onValueChange = { green = it; hexInput = hexFromSliders },
                    color = Color.Green
                )
                Spacer(modifier = Modifier.height(8.dp))
                ColorSlider(
                    label = "Blue",
                    value = blue,
                    onValueChange = { blue = it; hexInput = hexFromSliders },
                    color = Color.Blue
                )
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
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${(value * 255).toInt()}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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