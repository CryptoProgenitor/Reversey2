package com.example.reversey


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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.reversey.scoring.DifficultyLevel
import com.example.reversey.scoring.ScoringEngine
import com.example.reversey.scoring.ScoringPresets
import com.example.reversey.scoring.applyPreset
import com.example.reversey.ui.components.ColorCirclePicker
import com.example.reversey.ui.debug.DebugPanel
import kotlinx.coroutines.launch
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    currentTheme: String,
    onThemeChange: (String) -> Unit,
    currentDarkModePreference: String,
    onDarkModePreferenceChange: (String) -> Unit,
    isGameModeEnabled: Boolean,
    onGameModeChange: (Boolean) -> Unit,
    backupRecordingsEnabled: Boolean,
    onBackupRecordingsChange: (Boolean) -> Unit,
    scoringEngine: ScoringEngine, // <-- ADD THIS //ClaudeGeminiNewCodeV5
    audioViewModel: AudioViewModel,  // <-- ADD THIS LINE to sync difficulty levels with audioviewmodel
    showDebugPanel: Boolean, // <-- ADD THIS //ClaudeGeminiNewCodeV5
    onShowDebugPanelChange: (Boolean) -> Unit, // <-- ADD THIS //ClaudeGeminiNewCodeV5
    themeViewModel: ThemeViewModel // 🎨 NEW: Add ThemeViewModel for accent color functionality
) {
    val themes = listOf("Purple", "Blue", "Green", "Orange")
    val darkModeOptions = listOf("Light", "Dark", "System")
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // 🎨 NEW: Get custom accent color states from ThemeViewModel
    val customAccentColor by themeViewModel.customAccentColor.collectAsState()
    val defaultAccentColor by themeViewModel.defaultAccentColor.collectAsState()

    // Get current difficulty for UI feedback with state management
    var currentDifficulty by remember { mutableStateOf(scoringEngine.getCurrentDifficulty()) }

    // Update local state when difficulty changes

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val showTopFade by remember { derivedStateOf { listState.canScrollBackward } }
            val showBottomFade by remember { derivedStateOf { listState.canScrollForward } }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(MaterialTheme.shapes.medium)
            ) {
                item {
                    Text(
                        "Gameplay",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onGameModeChange(!isGameModeEnabled) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Enable Game Mode", style = MaterialTheme.typography.bodyLarge)
                        Switch(
                            checked = isGameModeEnabled,
                            onCheckedChange = onGameModeChange
                        )
                    }
                }
// ENHANCED Scoring Difficulty Section
                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                    Text(
                        "Scoring Difficulty",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
                    )

                    Text(
                        "Choose how strict the app is when scoring your singing",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
                    )

                    // Current Difficulty Indicator
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Current: ${currentDifficulty.emoji} ${currentDifficulty.displayName}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    // Difficulty Selection Grid
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        // First row: Easy, Normal, Hard
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DifficultyButton(
                                difficulty = DifficultyLevel.EASY,
                                preset = ScoringPresets.easyMode(),
                                isSelected = currentDifficulty == DifficultyLevel.EASY,
                                scoringEngine = scoringEngine,
                                audioViewModel = audioViewModel,  // <-- ADD THIS
                                onDifficultyChanged = { currentDifficulty = it },
                                modifier = Modifier.weight(1f)
                            )

                            DifficultyButton(
                                difficulty = DifficultyLevel.NORMAL,
                                preset = ScoringPresets.normalMode(),
                                isSelected = currentDifficulty == DifficultyLevel.NORMAL,
                                scoringEngine = scoringEngine,
                                audioViewModel = audioViewModel,  // <-- ADD THIS
                                onDifficultyChanged = { currentDifficulty = it },
                                modifier = Modifier.weight(1f)
                            )

                            DifficultyButton(
                                difficulty = DifficultyLevel.HARD,
                                preset = ScoringPresets.hardMode(),
                                isSelected = currentDifficulty == DifficultyLevel.HARD,
                                scoringEngine = scoringEngine,
                                audioViewModel = audioViewModel,  // <-- ADD THIS
                                onDifficultyChanged = { currentDifficulty = it },
                                modifier = Modifier.weight(1f)
                            )

                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Second row: Expert, Master (centered)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                        ) {
                            DifficultyButton(
                                difficulty = DifficultyLevel.EXPERT,
                                preset = ScoringPresets.expertMode(),
                                isSelected = currentDifficulty == DifficultyLevel.EXPERT,
                                scoringEngine = scoringEngine,
                                audioViewModel = audioViewModel,  // <-- ADD THIS
                                onDifficultyChanged = { currentDifficulty = it },
                                modifier = Modifier.weight(1f)
                            )

                            DifficultyButton(
                                difficulty = DifficultyLevel.MASTER,
                                preset = ScoringPresets.masterMode(),
                                isSelected = currentDifficulty == DifficultyLevel.MASTER,
                                scoringEngine = scoringEngine,
                                audioViewModel = audioViewModel,  // <-- ADD THIS
                                onDifficultyChanged = { currentDifficulty = it },
                                modifier = Modifier.weight(1f)
                            )

                        }
                    }

                    // Difficulty Description Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = currentDifficulty.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                // 🎨 NEW: Custom Accent Color Section (replaces old radio buttons)
                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                    Text(
                        "Appearance",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        ColorCirclePicker(
                            selectedColor = customAccentColor,
                            defaultColor = defaultAccentColor,
                            onColorSelected = { color ->
                                themeViewModel.setCustomAccentColor(color)
                            },
                            onResetToDefault = {
                                themeViewModel.clearCustomAccentColor()
                            },
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                // Dark Mode Section
                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                    Text(
                        "Dark Mode",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
                    )
                }

                // Dark Mode Options
                items(darkModeOptions) { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onDarkModePreferenceChange(option) }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentDarkModePreference == option,
                            onClick = { onDarkModePreferenceChange(option) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                // Storage Section
                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                    Text(
                        "Storage",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onBackupRecordingsChange(!backupRecordingsEnabled) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Backup Recordings to Google Drive", style = MaterialTheme.typography.bodyLarge)
                        Switch(
                            checked = backupRecordingsEnabled,
                            onCheckedChange = onBackupRecordingsChange
                        )
                    }

                    if (backupRecordingsEnabled) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Text(
                                text = "⚠️ Experimental Feature\n\n" +
                                        "Your recordings will be automatically saved to Google Drive when you finish a challenge. " +
                                        "This feature is currently in beta testing.\n\n" +
                                        "Privacy: Your voice recordings are stored securely on your personal Google Drive account.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }

                // About Section
                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                    Text(
                        "About",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
                    )
                    Button(
                        onClick = {
                            scope.launch {
                                val settingsDataStore = SettingsDataStore(context)
                                settingsDataStore.setTutorialCompleted(false)
                                navController.navigate("home")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("View Tutorial")
                    }
                }

                // --- ADD THIS NEW ITEM AT THE END OF THE LAZYCOLUMN ---
                item { //ClaudeGeminiNewCodeV5
                    // Only show this section in debug builds //ClaudeGeminiNewCodeV5
                    if (BuildConfig.DEBUG) { //ClaudeGeminiNewCodeV5
                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp)) //ClaudeGeminiNewCodeV5
                        Text( //ClaudeGeminiNewCodeV5
                            "Developer Options", //ClaudeGeminiNewCodeV5
                            style = MaterialTheme.typography.titleLarge, //ClaudeGeminiNewCodeV5
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp) //ClaudeGeminiNewCodeV5
                        ) //ClaudeGeminiNewCodeV5
                        Card( //ClaudeGeminiNewCodeV5
                            modifier = Modifier //ClaudeGeminiNewCodeV5
                                .fillMaxWidth() //ClaudeGeminiNewCodeV5
                                .padding(horizontal = 16.dp, vertical = 8.dp) //ClaudeGeminiNewCodeV5
                        ) { //ClaudeGeminiNewCodeV5
                            Row( //ClaudeGeminiNewCodeV5
                                modifier = Modifier //ClaudeGeminiNewCodeV5
                                    .fillMaxWidth() //ClaudeGeminiNewCodeV5
                                    .clickable { onShowDebugPanelChange(!showDebugPanel) } //ClaudeGeminiNewCodeV5
                                    .padding(16.dp), //ClaudeGeminiNewCodeV5
                                horizontalArrangement = Arrangement.SpaceBetween, //ClaudeGeminiNewCodeV5
                                verticalAlignment = Alignment.CenterVertically //ClaudeGeminiNewCodeV5
                            ) { //ClaudeGeminiNewCodeV5
                                Text("Advanced Settings Panel") //ClaudeGeminiNewCodeV5
                                Switch( //ClaudeGeminiNewCodeV5
                                    checked = showDebugPanel, //ClaudeGeminiNewCodeV5
                                    onCheckedChange = onShowDebugPanelChange //ClaudeGeminiNewCodeV5
                                ) //ClaudeGeminiNewCodeV5
                            } //ClaudeGeminiNewCodeV5
                        } //ClaudeGeminiNewCodeV5
                    } //ClaudeGeminiNewCodeV5
                } //ClaudeGeminiNewCodeV5
            }

            // Scroll glow effects
            val topGradient = Brush.verticalGradient(
                0.0f to MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                1.0f to Color.Transparent
            )
            val bottomGradient = Brush.verticalGradient(
                0.0f to Color.Transparent,
                1.0f to MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )

            if (showTopFade) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .align(Alignment.TopCenter)
                        .clip(MaterialTheme.shapes.medium)
                        .background(topGradient)
                )
            }

            if (showBottomFade) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .align(Alignment.BottomCenter)
                        .clip(MaterialTheme.shapes.medium)
                        .background(bottomGradient)
                )
            }
            // --- ADD THIS AT THE END OF THE COMPOSABLE, AFTER THE LAZYCOLUMN ---
            DebugPanel( //ClaudeGeminiNewCodeV5
                scoringEngine = scoringEngine, //ClaudeGeminiNewCodeV5
                isVisible = showDebugPanel, //ClaudeGeminiNewCodeV5
                onDismiss = { onShowDebugPanelChange(false) } //ClaudeGeminiNewCodeV5
            ) //ClaudeGeminiNewCodeV5
        }
    }
}

@Composable
private fun DifficultyButton(
    difficulty: DifficultyLevel,
    preset: com.example.reversey.scoring.Presets,
    isSelected: Boolean,
    scoringEngine: ScoringEngine,
    audioViewModel: AudioViewModel,  // <-- ADD THIS LINE
    onDifficultyChanged: (DifficultyLevel) -> Unit,
    modifier: Modifier = Modifier
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
                    Modifier
                        .border(2.dp, glowColor, RoundedCornerShape(12.dp))
                        .shadow(8.dp, RoundedCornerShape(12.dp), ambientColor = glowColor, spotColor = glowColor)
                } else {
                    Modifier
                }
            )
            .clickable {
                Log.d("BEFORE_PRESET", "Before: ${scoringEngine.getCurrentDifficulty().displayName}")
                // Update both ScoringEngine instances
                scoringEngine.applyPreset(preset)
                audioViewModel.updateScoringEngine(preset)
                Log.d("AFTER_PRESET", "After: ${scoringEngine.getCurrentDifficulty().displayName}")
                onDifficultyChanged(difficulty)
            },
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = difficulty.emoji,
                fontSize = 24.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = difficulty.displayName,
                fontWeight = FontWeight.Bold,
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