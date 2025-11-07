package com.example.reversey



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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

import com.example.reversey.utils.*



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
            title = {
                Text(
                    "Clear All Recordings?",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Text(
                    "This will permanently delete all of your recordings. This action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllRecordings()
                        onClearAllDialogDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(
                        "Delete All",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            },
            dismissButton = {
                Button(onClick = { onClearAllDialogDismiss() }) {
                    Text(
                        "Cancel",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        )
    }

    val aesthetic = AestheticTheme()

    aesthetic.components.AppBackground(
        aesthetic = aesthetic
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        val aesthetic = AestheticTheme()
                        Text(
                            "ReVerseY",
                            color = aesthetic.primaryTextColor,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                letterSpacing = if (aesthetic.useWideLetterSpacing) 2.sp else 0.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    },

                    navigationIcon = {
                        val aesthetic = AestheticTheme()
                        IconButton(onClick = openMenu) {
                            Icon(
                                Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = aesthetic.primaryTextColor
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
                        val aesthetic = AestheticTheme()
                        Text(
                            text = uiState.statusText,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                letterSpacing = if (aesthetic.useWideLetterSpacing) 1.sp else 0.sp
                            ),
                            color = aesthetic.primaryTextColor
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
        // Dialogs and overlays
        uiState.attemptToRename?.let { (parentPath, attempt) ->
            var newPlayerName by remember { mutableStateOf(attempt.playerName) }
            AlertDialog(
                onDismissRequest = { viewModel.clearAttemptToRename() },
                title = {
                    Text(
                        "Name This Player",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                text = {
                    OutlinedTextField(
                        value = newPlayerName,
                        onValueChange = { newPlayerName = it },
                        label = {
                            Text(
                                "Player Name",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        if (newPlayerName.isNotBlank()) {
                            viewModel.renamePlayer(parentPath, attempt, newPlayerName)
                        }
                        viewModel.clearAttemptToRename()
                    }) {
                        Text(
                            "Save",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                },
                dismissButton = {
                    Button(onClick = { viewModel.clearAttemptToRename() }) {
                        Text(
                            "Keep Default",
                            style = MaterialTheme.typography.labelLarge
                        )
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
        // 🎨 Component composition architecture!
        aesthetic.components.RecordButton(
            isRecording = isRecording,
            isProcessing = false,
            aesthetic = aesthetic,
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