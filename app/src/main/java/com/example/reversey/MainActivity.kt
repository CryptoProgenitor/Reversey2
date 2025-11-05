package com.example.reversey

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.reversey.data.models.ChallengeType
import com.example.reversey.data.models.Recording
import com.example.reversey.scoring.ScoringEngine
import com.example.reversey.ui.components.DifficultyIndicator
import com.example.reversey.ui.components.EnhancedGlowButton
import com.example.reversey.ui.components.ThemedMenuModal
import com.example.reversey.ui.components.TutorialOverlay
import com.example.reversey.ui.components.UnifiedAttemptItem
import com.example.reversey.ui.components.UnifiedRecordingButton
import com.example.reversey.ui.components.UnifiedRecordingItem
import com.example.reversey.ui.screens.SettingsScreen
import com.example.reversey.ui.theme.AestheticTheme
import com.example.reversey.ui.theme.MaterialColors
import com.example.reversey.ui.theme.ReVerseYTheme
import com.example.reversey.ui.viewmodels.AudioViewModel
import com.example.reversey.ui.viewmodels.ThemeViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin


@AndroidEntryPoint  // ← ADD THIS ANNOTATION
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val themeViewModel: ThemeViewModel = hiltViewModel()
            val darkModePreference by themeViewModel.darkModePreference.collectAsState()
            val currentThemeId by themeViewModel.currentThemeId.collectAsState()
            val customAccentColor by themeViewModel.customAccentColor.collectAsState()  // ⚡ ADD THIS


            val useDarkTheme = when (darkModePreference) {
                "Light" -> false
                "Dark" -> true
                else -> isSystemInDarkTheme()
            }

            ReVerseYTheme(
                aestheticThemeId = currentThemeId,
                customAccentColor = customAccentColor,  // ⚡ ADD THIS LINE
                darkTheme = useDarkTheme
            ) {
                MainApp(themeViewModel = themeViewModel)
            }
        }
    }
}

// Audio constants
object AudioConstants {
    const val SAMPLE_RATE = 44100
    const val CHANNEL_CONFIG = android.media.AudioFormat.CHANNEL_IN_MONO
    const val AUDIO_FORMAT = android.media.AudioFormat.ENCODING_PCM_16BIT
    const val MAX_WAVEFORM_SAMPLES = 200
}

// REPLACE YOUR MainApp FUNCTION (lines 177-272) WITH THIS:

@Composable
fun MainApp(themeViewModel: ThemeViewModel) {
    val navController = rememberNavController()
    var showMenuModal by remember { mutableStateOf(false) }
    val currentThemeId by themeViewModel.currentThemeId.collectAsState()
    val darkModePreference by themeViewModel.darkModePreference.collectAsState()
    val isGameModeEnabled by themeViewModel.gameModeEnabled.collectAsState()
    val audioViewModel: AudioViewModel = hiltViewModel()
    var showClearAllDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scoringEngine = audioViewModel.scoringEngine
    var showDebugPanel by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(navController = navController, startDestination = "home") {
            composable("home") {
                val currentThemeId by themeViewModel.currentThemeId.collectAsState()
                val customAccentColor by themeViewModel.customAccentColor.collectAsState()
                AudioReverserApp(
                    viewModel = audioViewModel,
                    openMenu = { showMenuModal = true },
                    openMenuToSettings = { navController.navigate("settings") },
                    onNavigateToSettings = { navController.navigate("settings") },
                    showClearAllDialog = showClearAllDialog,
                    onClearAllDialogDismiss = { showClearAllDialog = false },
                    isGameModeEnabled = isGameModeEnabled,
                    scoringEngine = scoringEngine,
                    navController = navController
                )
            }
            composable("settings") {
                val backupRecordingsEnabled by themeViewModel.backupRecordingsEnabled.collectAsState()
                SettingsScreen(
                    navController = navController,
                    themeViewModel = themeViewModel,
                    scoringEngine = scoringEngine,
                    audioViewModel = audioViewModel,
                    showDebugPanel = showDebugPanel,
                    onShowDebugPanelChange = { showDebugPanel = it }
                )
            }
        }
    }  // ← End of Box

    // Themed menu modal (replaces drawer)
    ThemedMenuModal(
        visible = showMenuModal,
        currentRoute = navController.currentDestination?.route,
        onDismiss = { showMenuModal = false },
        onNavigateHome = {
            navController.navigate("home") {
                popUpTo("home") { inclusive = true }
            }
        },
        onClearAll = { showClearAllDialog = true },
        themeViewModel = themeViewModel,
        scoringEngine = scoringEngine,
        audioViewModel = audioViewModel,
        showDebugPanel = showDebugPanel,
        onShowDebugPanelChange = { showDebugPanel = it }
    )
}  // ← End of MainApp




@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AudioReverserApp(
    viewModel: AudioViewModel,
    //openDrawer: () -> Unit,
    openMenu: () -> Unit,  // ✅ NEW NAME
    onNavigateToSettings: () -> Unit = {},  // ← ADD THIS
    showClearAllDialog: Boolean,
    onClearAllDialogDismiss: () -> Unit,
    isGameModeEnabled: Boolean,
    scoringEngine: ScoringEngine,  // <-- ADD THIS LINE
    navController: NavController, // 🔧 ADD THIS - so DifficultyIndicator can navigate
    openMenuToSettings: () -> Unit,  // ← ADD THIS
) {
    val uiState by viewModel.uiState.collectAsState()
    val recordAudioPermissionState = rememberPermissionState(android.Manifest.permission.RECORD_AUDIO)
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Auto-scroll handling
    LaunchedEffect(uiState.scrollToIndex) {
        uiState.scrollToIndex?.let { targetIndex ->
            scope.launch {
                delay(300)
                if (targetIndex == 0) {
                    listState.animateScrollToItem(0)
                } else {
                    val layoutInfo = listState.layoutInfo
                    val visibleItems = layoutInfo.visibleItemsInfo
                    val lastVisibleIndex = visibleItems.lastOrNull()?.index ?: -1

                    if (targetIndex > lastVisibleIndex) {
                        val firstVisibleIndex = visibleItems.firstOrNull()?.index ?: 0
                        val visibleCount = lastVisibleIndex - firstVisibleIndex + 1
                        val scrollToIndex = (targetIndex - visibleCount + 1).coerceAtLeast(0)

                        listState.animateScrollToItem(
                            index = scrollToIndex,
                            scrollOffset = +300
                        )
                    }
                }
                viewModel.clearScrollToIndex()
            }
        }
    }

    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { onClearAllDialogDismiss() },
            title = { Text("Clear All Recordings?") },
            text = { Text("This will permanently delete all of your recordings. This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllRecordings()
                        onClearAllDialogDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete All") }
            },
            dismissButton = {
                Button(onClick = { onClearAllDialogDismiss() }) { Text("Cancel") }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AestheticTheme().primaryGradient)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "ReVerseY",
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                letterSpacing = if (AestheticTheme().useWideLetterSpacing) 2.sp else 0.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    },
                    /*navigationIcon = {
                        EnhancedGlowButton(
                            onClick = openDrawer,
                            isPrimary = true,
                            size = 48.dp
                        ) {
                            Icon(Icons.Default.Menu, "Menu", tint = Color.White)
                        }
                    },*/
                    navigationIcon = {
                        IconButton(onClick = openMenu) {  // ✅ NEW: Simple IconButton
                            Icon(
                                Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    },
                    actions = {
                        // NEW: Difficulty indicator in top-right
                        DifficultyIndicator(
                            difficulty = scoringEngine.getCurrentDifficulty(),
                            onClick = openMenuToSettings,  // ← CHANGE THIS
                            //onClick = openMenu,
                            //onClick = { navController.navigate("settings") }, // 🔧 ADD THIS - navigate to settings
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Enhanced Record Button with theme integration
                EnhancedRecordButton(
                    isRecording = uiState.isRecording,
                    hasPermission = recordAudioPermissionState.status.isGranted,
                    onRequestPermission = { recordAudioPermissionState.launchPermissionRequest() },
                    onStartRecording = { viewModel.startRecording() },
                    onStopRecording = { viewModel.stopRecording() },
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (uiState.isRecording) {
                    EnhancedWaveformVisualizer(
                        amplitudes = uiState.amplitudes,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                    )
                } else {
                    AnimatedVisibility(
                        visible = uiState.statusText.isNotEmpty(),
                        enter = fadeIn(animationSpec = tween(600, 100, LinearOutSlowInEasing)),
                        exit = fadeOut(animationSpec = tween(600, easing = LinearOutSlowInEasing))
                    ) {
                        Text(
                            text = uiState.statusText,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                letterSpacing = if (AestheticTheme().useWideLetterSpacing) 1.sp else 0.sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Box(modifier = Modifier.fillMaxSize()) {
                    val showTopFade by remember { derivedStateOf { listState.canScrollBackward } }
                    val showBottomFade by remember { derivedStateOf { listState.canScrollForward } }

                    LazyColumn(
                        state = listState,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(MaterialTheme.shapes.medium)
                    ) {//keep me
                        uiState.recordings.forEach { recording ->
                            item(key = "parent_${recording.originalPath}") {
                                UnifiedRecordingItem(
                                    recording = recording,
                                    aesthetic = AestheticTheme(),
                                    isPlaying = uiState.currentlyPlayingPath != null &&
                                            (uiState.currentlyPlayingPath == recording.originalPath ||
                                                    uiState.currentlyPlayingPath == recording.reversedPath),
                                    isPaused = uiState.isPaused,
                                    progress = if (uiState.currentlyPlayingPath == recording.originalPath ||
                                        uiState.currentlyPlayingPath == recording.reversedPath) uiState.playbackProgress else 0f,
                                    onPlay = { path: String -> viewModel.play(path) },
                                    onPause = { viewModel.pause() },
                                    onStop = { viewModel.stopPlayback() },
                                    onDelete = { viewModel.deleteRecording(recording) },
                                    onShare = { path: String ->
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "audio/wav"
                                            putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(context,
                                                "${context.packageName}.provider", File(path)))
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Share Recording"))
                                    },
                                    onRename = { oldPath: String, newName: String -> viewModel.renameRecording(oldPath, newName) },
                                    isGameModeEnabled = isGameModeEnabled,
                                    onStartAttempt = { rec: Recording, type: ChallengeType ->
                                        viewModel.startAttemptRecording(rec, type)
                                    }
                                )
                            }//keep me

                            // Use the new EnhancedAttemptItem instead of AttemptItem
                            // Use the new EnhancedAttemptItem instead of AttemptItem
                            items(
                                count = recording.attempts.size,
                                key = { index -> "attempt_${recording.originalPath}_${index}" }
                            ) { index ->
                                val attempt = recording.attempts[index]
//START HERE
                                // 🎯 GLUTE: Unified component for all themes
                                UnifiedAttemptItem(
                                    attempt = attempt,
                                    currentlyPlayingPath = uiState.currentlyPlayingPath,
                                    isPaused = uiState.isPaused,
                                    progress = if (uiState.currentlyPlayingPath == attempt.attemptFilePath ||
                                        uiState.currentlyPlayingPath == attempt.reversedAttemptFilePath
                                    ) uiState.playbackProgress else 0f,
                                    onPlay = { path -> viewModel.play(path) },
                                    onPause = { viewModel.pause() },
                                    onStop = { viewModel.stopPlayback() },
                                    onRenamePlayer = { oldAttempt, newName ->
                                        viewModel.renamePlayer(
                                            recording.originalPath,
                                            oldAttempt,
                                            newName
                                        )
                                    },
                                    onDeleteAttempt = { attemptToDelete ->
                                        viewModel.deleteAttempt(
                                            recording.originalPath,
                                            attemptToDelete
                                        )
                                    },
                                    onShareAttempt = { path ->
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "audio/wav"
                                            putExtra(
                                                Intent.EXTRA_STREAM, FileProvider.getUriForFile(
                                                    context,
                                                    "${context.packageName}.provider", File(path)
                                                )
                                            )
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(
                                            Intent.createChooser(
                                                shareIntent,
                                                "Share Attempt"
                                            )
                                        )
                                    },
                                    onJumpToParent = {
                                        scope.launch {
                                            // Calculate the correct index in the flattened LazyColumn
                                            var flatIndex = 0
                                            for (i in uiState.recordings.indices) {
                                                val rec = uiState.recordings[i]
                                                if (rec.originalPath == recording.originalPath) {
                                                    // Found the parent recording - scroll to its position
                                                    listState.animateScrollToItem(
                                                        index = flatIndex,
                                                        scrollOffset = 0  // Position at top of view
                                                    )
                                                    break
                                                }
                                                // Add 1 for the recording item + its attempts count
                                                flatIndex += 1 + rec.attempts.size
                                            }
                                        }
                                    }
                                )
                            }//KEEP ME! for GLUE drop in
                        }//KEEP ME!
                    }

                    // Fade gradients
                    val topGradient = Brush.verticalGradient(
                        0.0f to MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        1.0f to Color.Transparent
                    )
                    val bottomGradient = Brush.verticalGradient(
                        0.0f to Color.Transparent,
                        1.0f to MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )

                    if (showTopFade) {
                        Box(modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                            .align(Alignment.TopCenter)
                            .clip(MaterialTheme.shapes.medium)
                            .background(topGradient))
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
                }
            }
        }

        // Dialogs and overlays
        uiState.attemptToRename?.let { (parentPath, attempt) ->
            var newPlayerName by remember { mutableStateOf(attempt.playerName) }
            AlertDialog(
                onDismissRequest = { viewModel.clearAttemptToRename() },
                title = { Text("Name This Player") },
                text = {
                    OutlinedTextField(
                        value = newPlayerName,
                        onValueChange = { newPlayerName = it },
                        label = { Text("Player Name") }
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        if (newPlayerName.isNotBlank()) {
                            viewModel.renamePlayer(parentPath, attempt, newPlayerName)
                        }
                        viewModel.clearAttemptToRename()
                    }) { Text("Save") }
                },
                dismissButton = {
                    Button(onClick = { viewModel.clearAttemptToRename() }) {
                        Text("Keep Default")
                    }
                }
            )
        }

        // Tutorial Overlay
        if (uiState.showTutorial) {
            TutorialOverlay(
                onDismiss = { viewModel.dismissTutorial() },
                onComplete = { viewModel.completeTutorial() }
            )
        }

        // Quality Warning Dialog
        if (uiState.showQualityWarning) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissQualityWarning() },
                title = {
                    Text(
                        "Recording Too Quiet",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                text = {
                    Text(
                        uiState.qualityWarningMessage,
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    Button(onClick = { viewModel.dismissQualityWarning() }) {
                        Text("Got it")
                    }
                },
                dismissButton = {
                    Button(onClick = { viewModel.dismissQualityWarning() }) {
                        Text("Re-record")
                    }
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Warning",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            )
        }
    }
}

/**
 * Enhanced Record Button with theme integration and glow effects
 */
/**
 * ALSO REPLACE: The EnhancedRecordButton function - Remove theme parameter
 */
@Composable
fun EnhancedRecordButton(
    isRecording: Boolean,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit
) {
    val aesthetic = AestheticTheme()
    val colors = MaterialColors()

    if (!hasPermission) {
        // Keep existing permission button code exactly as-is
        Button(
            onClick = onRequestPermission,
            modifier = Modifier.size(120.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Mic, "Request Permission", tint = Color.White, modifier = Modifier.size(32.dp))
                Text("Grant\nPermission", color = Color.White, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
            }
        }
    } else {
        // 🥚 BEAUTIFUL EGG BUTTON!
        UnifiedRecordingButton(
            isRecording = isRecording,
            onStartRecording = onStartRecording,
            onStopRecording = onStopRecording
        )
    }
}

/**
 * Enhanced Waveform Visualizer with standardized theming
 */
@Composable
fun EnhancedWaveformVisualizer(
    amplitudes: List<Float>,
    modifier: Modifier = Modifier,
    barWidth: Dp = 8.dp,
    barGap: Dp = 4.dp
) {
    val aesthetic = AestheticTheme()
    val materialColors = MaterialColors()
    Canvas(modifier = modifier) {
        val canvasHeight = size.height
        val maxAmplitude = 1.0f
        val barWidthPx = barWidth.toPx()
        val barGapPx = barGap.toPx()
        val totalBarWidth = barWidthPx + barGapPx
        val maxBars = (size.width / totalBarWidth).toInt()
        val barsToDraw = amplitudes.takeLast(maxBars)

        barsToDraw.forEachIndexed { index, amplitude ->
            val barHeight = (amplitude / maxAmplitude) * canvasHeight * 0.8f
            val x = index * totalBarWidth
            val y = (canvasHeight - barHeight) / 2

            // Enhanced gradient with more vibrant colors using standardized theming
            val gradient = Brush.verticalGradient(
                colors = listOf(
                    materialColors.primary.copy(alpha = 0.3f),
                    materialColors.primary.copy(alpha = 0.8f),
                    materialColors.primary
                ),
                startY = y,
                endY = y + barHeight
            )

            // Rounded bars with gradient and glow effect
            drawRoundRect(
                brush = gradient,
                topLeft = Offset(x, y),
                size = Size(barWidthPx, barHeight),
                cornerRadius = CornerRadius(barWidthPx / 2, barWidthPx / 2)
            )
        }
    }
}

/**
 * Enhanced Recording Item - FIXED VERSION with Material 3 theming
 * Replace the EnhancedRecordingItem function in your MainActivity.kt with this version
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EnhancedRecordingItem(
    recording: Recording,
    isPlaying: Boolean,
    isPaused: Boolean,
    progress: Float,
    onPlay: (String) -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit,
    onDelete: (Recording) -> Unit,
    onShare: (String) -> Unit,
    onRename: (String, String) -> Unit,
    isGameModeEnabled: Boolean,
    onStartAttempt: (Recording, ChallengeType) -> Unit
) {
    // Use Material 3 theming system consistently
    val aesthetic = AestheticTheme()
    val colors = MaterialColors()

    // 🐛 TEMPORARY DEBUG - ADD THESE LINES
    println("🎨 Theme ID: ${aesthetic.id}")
    println("🎨 Border Width: ${aesthetic.borderWidth}")
    println("🎨 Card Border: ${aesthetic.cardBorder}")
    println("🎨 Use Glassmorphism: ${aesthetic.useGlassmorphism}")


    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .rotate(aesthetic.maxCardRotation)  // ✅ GEMINI-APPROVED: Property-driven styling
            .then(
                if (aesthetic.useGlassmorphism && aesthetic.glowIntensity > 0) {
                    Modifier.shadow(
                        elevation = (aesthetic.glowIntensity * 15).dp,
                        shape = RoundedCornerShape(20.dp),
                        spotColor = colors.primary
                    )
                } else Modifier
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (aesthetic.useGlassmorphism) {
                colors.surface.copy(alpha = 0.8f)
            } else colors.surface
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (aesthetic.useGlassmorphism) {
                        Modifier
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.15f),
                                        Color.Transparent
                                    )
                                ),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .border(
                                width = 2.dp,
                                color = Color.White.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(20.dp)
                            )
                    } else Modifier.border(
                        width = aesthetic.borderWidth.dp,  // ✅ Uses 3f for scrapbook, 2f for others
                        color = aesthetic.cardBorder,      // ✅ Brown for scrapbook
                        shape = RoundedCornerShape(20.dp)
                    )
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = recording.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = if (aesthetic.useWideLetterSpacing) 1.sp else 0.sp
                        ),
                        color = colors.onSurface,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showRenameDialog = true }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { if (isPlaying) progress else 0f },
                    modifier = Modifier.fillMaxWidth(),
                    color = colors.primary,
                    trackColor = colors.surfaceVariant.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    EnhancedGlowButton(
                        onClick = { showShareDialog = true },
                        isPrimary = true,
                        size = 40.dp,
                        label = "Share"
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Recording",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isPlaying) {
                            EnhancedGlowButton(
                                onClick = { onPause() },
                                isPrimary = true,
                                size = 50.dp,
                                label = if (isPaused) "Resume" else "Pause"
                            ) {
                                val pauseIcon =
                                    if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause
                                Icon(
                                    imageVector = pauseIcon,
                                    contentDescription = "Pause/Resume",
                                    tint = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            EnhancedGlowButton(
                                onClick = { onStop() },
                                isDestructive = true,
                                size = 50.dp,
                                label = "Stop"
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Stop,
                                    contentDescription = "Stop",
                                    tint = Color.White
                                )
                            }
                        } else {
                            EnhancedGlowButton(
                                onClick = { onPlay(recording.originalPath) },
                                isPrimary = true,
                                size = 50.dp,
                                label = "Play"
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play Original",
                                    tint = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            EnhancedGlowButton(
                                onClick = { onPlay(recording.reversedPath!!) },
                                enabled = recording.reversedPath != null,
                                isPrimary = true,
                                size = 50.dp,
                                label = "Rewind"
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Replay,
                                    contentDescription = "Play Reversed",
                                    tint = Color.White
                                )
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isGameModeEnabled) {
                            EnhancedGlowButton(
                                onClick = { onStartAttempt(recording, ChallengeType.FORWARD) },
                                isPrimary = true,
                                size = 50.dp,
                                label = "FWD"
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Forward Challenge",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            EnhancedGlowButton(
                                onClick = { onStartAttempt(recording, ChallengeType.REVERSE) },
                                isPrimary = true,
                                size = 50.dp,
                                label = "REV"
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Reverse Challenge",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        EnhancedGlowButton(
                            onClick = { showDeleteDialog = true },
                            isDestructive = true,
                            size = 40.dp,
                            label = "Delete"
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Recording",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialogs (keeping existing functionality)
    if (showRenameDialog) {
        var newName by remember { mutableStateOf(recording.name) }
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Recording") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("New Name") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (newName.isNotBlank()) {
                        val finalName = if (newName.endsWith(".wav")) newName else "$newName.wav"
                        onRename(recording.originalPath, finalName)
                    }
                    showRenameDialog = false
                }) { Text("Rename") }
            },
            dismissButton = {
                Button(onClick = { showRenameDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Recording?", color = colors.error) },
            text = { Text("Are you sure you want to delete '${recording.name}'? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete(recording)
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.error)
                ) { Text("Delete", color = Color.White) }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showShareDialog) {
        AlertDialog(
            onDismissRequest = { showShareDialog = false },
            title = { Text("Share Recording") },
            text = {
                Column {
                    Text("Which version would you like to share?")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            onShare(recording.originalPath)
                            showShareDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                    ) {
                        Text("Share Original", color = Color.White)
                    }
                    if (recording.reversedPath != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                onShare(recording.reversedPath!!)
                                showShareDialog = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                        ) {
                            Text("Share Reversed", color = Color.White)
                        }
                    }
                }
            },
            confirmButton = { },
            dismissButton = {
                Button(onClick = { showShareDialog = false }) { Text("Cancel") }
            }
        )
    }
}

// Utility functions (keeping existing ones)
fun getRecordingsDir(context: Context): File {
    return File(context.filesDir, "recordings").apply { mkdirs() }
}

fun formatFileName(fileName: String): String {
    return fileName.removeSuffix(".wav")
}

@Throws(IOException::class)
fun writeWavHeader(out: FileOutputStream, audioData: ByteArray, channels: Int, sampleRate: Int, bitDepth: Int) {
    val audioDataLength = audioData.size
    val totalDataLength = audioDataLength + 36
    val byteRate = sampleRate * channels * bitDepth / 8
    val blockAlign = channels * bitDepth / 8
    val header = ByteArray(44)

    header[0] = 'R'.code.toByte(); header[1] = 'I'.code.toByte(); header[2] = 'F'.code.toByte(); header[3] = 'F'.code.toByte()
    header[4] = (totalDataLength and 0xff).toByte(); header[5] = (totalDataLength shr 8 and 0xff).toByte(); header[6] = (totalDataLength shr 16 and 0xff).toByte(); header[7] = (totalDataLength shr 24 and 0xff).toByte()
    header[8] = 'W'.code.toByte(); header[9] = 'A'.code.toByte(); header[10] = 'V'.code.toByte(); header[11] = 'E'.code.toByte()
    header[12] = 'f'.code.toByte(); header[13] = 'm'.code.toByte(); header[14] = 't'.code.toByte(); header[15] = ' '.code.toByte()
    header[16] = 16; header[17] = 0; header[18] = 0; header[19] = 0
    header[20] = 1; header[21] = 0
    header[22] = channels.toByte(); header[23] = 0
    header[24] = (sampleRate and 0xff).toByte(); header[25] = (sampleRate shr 8 and 0xff).toByte(); header[26] = (sampleRate shr 16 and 0xff).toByte(); header[27] = (sampleRate shr 24 and 0xff).toByte()
    header[28] = (byteRate and 0xff).toByte(); header[29] = (byteRate shr 8 and 0xff).toByte(); header[30] = (byteRate shr 16 and 0xff).toByte(); header[31] = (byteRate shr 24 and 0xff).toByte()
    header[32] = blockAlign.toByte(); header[33] = 0
    header[34] = bitDepth.toByte(); header[35] = 0
    header[36] = 'd'.code.toByte(); header[37] = 'a'.code.toByte(); header[38] = 't'.code.toByte(); header[39] = 'a'.code.toByte()
    header[40] = (audioDataLength and 0xff).toByte(); header[41] = (audioDataLength shr 8 and 0xff).toByte(); header[42] = (audioDataLength shr 16 and 0xff).toByte(); header[43] = (audioDataLength shr 24 and 0xff).toByte()

    out.write(header, 0, 44)
    out.write(audioData)
}

class OctagonShape : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path().apply {
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val radius = min(centerX, centerY)
            val angle = (2 * PI / 8).toFloat()
            val startAngle = -angle / 2f

            moveTo(centerX + radius * cos(startAngle), centerY + radius * sin(startAngle))
            for (i in 1 until 8) {
                lineTo(centerX + radius * cos(startAngle + angle * i), centerY + radius * sin(startAngle + angle * i))
            }
            close()
        }
        return Outline.Generic(path)
    }
}