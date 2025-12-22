package com.quokkalabs.reversey

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
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
import androidx.compose.runtime.snapshotFlow
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
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.quokkalabs.reversey.data.backup.BackupManager
import com.quokkalabs.reversey.data.models.ChallengeType
import com.quokkalabs.reversey.data.models.Recording
import com.quokkalabs.reversey.scoring.PhonemeUtils
import com.quokkalabs.reversey.ui.components.AnalysisToast
import com.quokkalabs.reversey.ui.components.DifficultyIndicator
import com.quokkalabs.reversey.ui.components.ThemedMenuModal
import com.quokkalabs.reversey.ui.components.TutorialOverlay
import com.quokkalabs.reversey.ui.constants.UiConstants
import com.quokkalabs.reversey.ui.menu.FilesContent
import com.quokkalabs.reversey.ui.menu.ModalScreen
import com.quokkalabs.reversey.ui.theme.AestheticTheme
import com.quokkalabs.reversey.ui.theme.MaterialColors
import com.quokkalabs.reversey.ui.theme.ReVerseYTheme
import com.quokkalabs.reversey.ui.theme.ScrapbookThemeComponents
import com.quokkalabs.reversey.ui.viewmodels.AudioViewModel
import com.quokkalabs.reversey.ui.viewmodels.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var backupManager: BackupManager

    // StateFlow for incoming WAV URIs - survives hot restart
    private val _incomingWavUri = MutableStateFlow<Uri?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check for incoming WAV file intent on cold start
        _incomingWavUri.value = handleIncomingIntent(intent)

        setContent {
            val themeViewModel: ThemeViewModel = hiltViewModel()
            val darkModePreference by themeViewModel.darkModePreference.collectAsState()
            val currentThemeId by themeViewModel.currentThemeId.collectAsState()
            val customAccentColor by themeViewModel.customAccentColor.collectAsState()

            // Observe incoming WAV URI as state
            val incomingWavUri by _incomingWavUri.collectAsState()

            val useDarkTheme = when (darkModePreference) {
                "Light" -> false
                "Dark" -> true
                else -> isSystemInDarkTheme()
            }

            ReVerseYTheme(
                aestheticThemeId = currentThemeId,
                customAccentColor = customAccentColor,
                darkTheme = useDarkTheme
            ) {
                MainApp(
                    themeViewModel = themeViewModel,
                    backupManager = backupManager,
                    incomingWavUri = incomingWavUri,
                    onWavUriConsumed = { _incomingWavUri.value = null }
                )
            }
        }
        // Initialize phoneme dictionary for scoring (background thread - avoids 10s main thread block)
        lifecycleScope.launch(Dispatchers.IO) {
            val success = PhonemeUtils.initialize(applicationContext)
            Log.d("PhonemeUtils", "CMU dictionary loaded: $success (${PhonemeUtils.dictionarySize()} words)")
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Update the intent property so subsequent checks use the new one
        setIntent(intent)
        // Push new URI to StateFlow - composables will recompose automatically
        _incomingWavUri.value = handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?): Uri? {
        if (intent == null) return null

        return when (intent.action) {
            Intent.ACTION_VIEW -> intent.data
            Intent.ACTION_SEND -> {
                if (intent.type?.startsWith("audio/") == true) {
                    // FIX: Handle API 33+ (Tiramisu) type-safe intent extras
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(Intent.EXTRA_STREAM) as? Uri
                    }
                } else null
            }
            else -> null
        }
    }
}

// 🎯 CRITICAL FIX: Deleted duplicate AudioConstants object to prevent shadowing


@Composable
fun MainApp(
    themeViewModel: ThemeViewModel,
    backupManager: BackupManager,
    incomingWavUri: Uri? = null,
    onWavUriConsumed: () -> Unit = {}
) {
    val navController = rememberNavController()
    var showMenuModal by remember { mutableStateOf(false) }
    var modalInitialScreen by remember { mutableStateOf<ModalScreen>(ModalScreen.Menu) }

    val isGameModeEnabled by themeViewModel.gameModeEnabled.collectAsState()
    val audioViewModel: AudioViewModel = hiltViewModel()
    var showClearAllDialog by remember { mutableStateOf(false) }

    var showDebugPanel by remember { mutableStateOf(false) }
    var showTutorial by remember { mutableStateOf(false) }

    // Handle incoming WAV file - navigate to files screen
    LaunchedEffect(incomingWavUri) {
        if (incomingWavUri != null) {
            navController.navigate("files?wavUri=${Uri.encode(incomingWavUri.toString())}")
            onWavUriConsumed() // Clear the URI so it doesn't re-trigger on recomposition
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(navController = navController, startDestination = "home") {
            composable("home") {
                AudioReverserApp(
                    viewModel = audioViewModel,
                    openMenu = { showMenuModal = true },
                    openMenuToSettings = { modalInitialScreen = ModalScreen.Settings; showMenuModal = true },
                    showClearAllDialog = showClearAllDialog,
                    onClearAllDialogDismiss = { showClearAllDialog = false },
                    isGameModeEnabled = isGameModeEnabled,
                    navController = navController,
                    showDebugPanel = showDebugPanel,
                    onShowDebugPanelChange = { showDebugPanel = it }
                )
            }

            // Files Screen (Backup/Restore/Import)
            composable("files?wavUri={wavUri}") { backStackEntry ->
                val wavUriString = backStackEntry.arguments?.getString("wavUri")
                val wavUri = wavUriString?.let { Uri.parse(Uri.decode(it)) }

                FilesContent(
                    backupManager = backupManager,
                    audioViewModel = audioViewModel,
                    incomingWavUri = wavUri,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateHome = {
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                )
            }

            // Simple route without params
            composable("files") {
                FilesContent(
                    backupManager = backupManager,
                    audioViewModel = audioViewModel,
                    incomingWavUri = null,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateHome = {
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                )
            }
        }
    }

    // Themed menu modal
    ThemedMenuModal(
        visible = showMenuModal,
        currentRoute = navController.currentDestination?.route,
        onNavigateHome = {
            navController.navigate("home") {
                popUpTo("home") { inclusive = true }
            }
        },
        onClearAll = { showClearAllDialog = true },
        onNavigateToFiles = {
            navController.navigate("files")
            showMenuModal = false
        },
        onShowTutorial = { showTutorial = true },
        themeViewModel = themeViewModel,
        audioViewModel = audioViewModel,
        backupManager = backupManager,
        initialScreen = modalInitialScreen,

        onDismiss = { showMenuModal = false; modalInitialScreen = ModalScreen.Menu },
    )

    // Tutorial overlay
    if (showTutorial) {
        TutorialOverlay(
            onDismiss = { showTutorial = false },
            onComplete = { showTutorial = false }
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AudioReverserApp(
    viewModel: AudioViewModel,
    openMenu: () -> Unit,
    showClearAllDialog: Boolean,
    onClearAllDialogDismiss: () -> Unit,
    isGameModeEnabled: Boolean,
    navController: NavController,
    openMenuToSettings: () -> Unit,
    showDebugPanel: Boolean,
    onShowDebugPanelChange: (Boolean) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val recordAudioPermissionState = rememberPermissionState(android.Manifest.permission.RECORD_AUDIO)
    val listState = rememberLazyListState()
    LaunchedEffect(listState) {
        var lastIndex = listState.firstVisibleItemIndex
        var lastOffset = listState.firstVisibleItemScrollOffset

        snapshotFlow {
            listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset
        }.collect { (index, offset) ->
            val indexDelta = index - lastIndex
            val offsetDelta = offset - lastOffset

            // crude "how much did we scroll?"
            val totalDelta = indexDelta * 1000 + offsetDelta

            if (abs(totalDelta) > 150) {
                // Scale velocity a bit for fun, but keep it in [0.4, 1.0]
                val velocity = (abs(totalDelta) / 2000f).coerceIn(0.4f, 1f)
                ScrapbookThemeComponents.triggerScrollPop(velocity)
            }

            lastIndex = index
            lastOffset = offset
        }
    }

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
            title = { Text("Clear All Recordings?", style = MaterialTheme.typography.headlineSmall) },
            text = { Text("This will permanently delete all of your recordings. This action cannot be undone.", style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllRecordings()
                        onClearAllDialogDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete All", style = MaterialTheme.typography.labelLarge) }
            },
            dismissButton = {
                Button(onClick = { onClearAllDialogDismiss() }) { Text("Cancel", style = MaterialTheme.typography.labelLarge) }
            }
        )
    }

    val aesthetic = AestheticTheme()

    aesthetic.components.AppBackground(aesthetic = aesthetic) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
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
                        IconButton(onClick = openMenu) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = aesthetic.primaryTextColor)
                        }
                    },
                    actions = {
                        val currentDifficulty by viewModel.currentDifficultyFlow.collectAsState()
                        DifficultyIndicator(
                            difficulty = currentDifficulty,
                            onClick = openMenuToSettings,
                            modifier = Modifier.padding(end = UiConstants.DIFFICULTY_INDICATOR_END_PADDING)
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(UiConstants.CONTENT_PADDING),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(UiConstants.SPACER_ABOVE_RECORD_BUTTON))

                EnhancedRecordButton(
                    isRecording = uiState.isRecording.also { recording ->
                        Log.d("RECORD_BUG", "🖥️ UI READS: isRecording=$recording, isRecordingAttempt=${uiState.isRecordingAttempt}")
                    },
                    hasPermission = recordAudioPermissionState.status.isGranted,
                    onRequestPermission = { recordAudioPermissionState.launchPermissionRequest() },
                    onStartRecording = { viewModel.startRecording() },
                    onStopRecording = {
                        if (uiState.isRecordingAttempt) {
                            viewModel.stopAttempt()
                        } else {
                            viewModel.stopRecording()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(UiConstants.SPACER_BELOW_RECORD_BUTTON))

                if (uiState.isRecording) {
                    EnhancedWaveformVisualizer(
                        amplitudes = uiState.amplitudes,
                        modifier = Modifier.fillMaxWidth().height(UiConstants.WAVEFORM_HEIGHT)
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
                                letterSpacing = if (aesthetic.useWideLetterSpacing) 1.sp else 0.sp
                            ),
                            color = aesthetic.primaryTextColor,
                            modifier = Modifier.padding(vertical = UiConstants.STATUS_TEXT_VERTICAL_PADDING)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(UiConstants.SPACER_ABOVE_LIST))

                Box(modifier = Modifier.fillMaxSize()) {
                    val showTopFade by remember { derivedStateOf { listState.canScrollBackward } }
                    val showBottomFade by remember { derivedStateOf { listState.canScrollForward } }

                    LazyColumn(
                        state = listState,
                        verticalArrangement = Arrangement.spacedBy(UiConstants.RECORDING_LIST_ITEM_SPACING),
                        modifier = Modifier.fillMaxSize().clip(MaterialTheme.shapes.medium)
                    ) {
                        uiState.recordings.forEach { recording ->
                            item(key = "parent_${recording.originalPath}") {
                                aesthetic.components.RecordingItem(
                                    recording = recording,
                                    aesthetic = aesthetic,
                                    isPaused = uiState.isPaused,
                                    progress = if (uiState.currentlyPlayingPath == recording.originalPath ||
                                        uiState.currentlyPlayingPath == recording.reversedPath) uiState.playbackProgress else 0f,
                                    currentlyPlayingPath = uiState.currentlyPlayingPath,
                                    onPlay = { path: String -> viewModel.play(path) },
                                    onPause = { viewModel.pause() },
                                    onStop = { viewModel.stopPlayback() },
                                    onDelete = { viewModel.deleteRecording(recording) },
                                    onShare = { path: String ->
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "audio/wav"
                                            putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(context, "${context.packageName}.provider", File(path)))
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Share Recording"))
                                    },
                                    onRename = { oldPath: String, newName: String -> viewModel.renameRecording(oldPath, newName) },
                                    isGameModeEnabled = isGameModeEnabled,
                                    onStartAttempt = { rec: Recording, type: ChallengeType ->
                                        viewModel.startAttemptRecording(rec, type)
                                    },
                                    activeAttemptRecordingPath = if (uiState.isRecordingAttempt) uiState.parentRecordingPath else null,
                                    onStopAttempt = { viewModel.stopAttempt() }
                                )
                            }

                            items(
                                count = recording.attempts.size,
                                key = { index -> "attempt_${recording.originalPath}_${index}" }
                            ) { index ->
                                val attempt = recording.attempts[index]
                                // 🎯 POLYMORPHIC CALL 2: Attempt Item
                                aesthetic.components.AttemptItem(
                                    attempt = attempt,
                                    aesthetic = aesthetic,
                                    currentlyPlayingPath = uiState.currentlyPlayingPath,
                                    isPaused = uiState.isPaused,
                                    progress = if (uiState.currentlyPlayingPath == attempt.attemptFilePath ||
                                        uiState.currentlyPlayingPath == attempt.reversedAttemptFilePath
                                    ) uiState.playbackProgress else 0f,
                                    onPlay = { path -> viewModel.play(path) },
                                    onPause = { viewModel.pause() },
                                    onStop = { viewModel.stopPlayback() },
                                    onRenamePlayer = { oldAttempt, newName ->
                                        viewModel.renamePlayer(recording.originalPath, oldAttempt, newName)
                                    },
                                    onDeleteAttempt = { attemptToDelete ->
                                        viewModel.deleteAttempt(recording.originalPath, attemptToDelete)
                                    },
                                    onShareAttempt = { path ->
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "audio/wav"
                                            putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(context, "${context.packageName}.provider", File(path)))
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Share Attempt"))
                                    },
                                    onJumpToParent = {
                                        scope.launch {
                                            var flatIndex = 0
                                            for (i in uiState.recordings.indices) {
                                                val rec = uiState.recordings[i]
                                                if (rec.originalPath == recording.originalPath) {
                                                    listState.animateScrollToItem(index = flatIndex, scrollOffset = 0)
                                                    break
                                                }
                                                flatIndex += 1 + rec.attempts.size
                                            }
                                        }
                                    },
                                    onOverrideScore = { score ->
                                        viewModel.overrideAttemptScore(recording.originalPath, attempt, score)
                                    },
                                    onResetScore = {
                                        viewModel.resetAttemptScore(recording.originalPath, attempt)
                                    }
                                )
                            }
                        }
                    }

                    val topGradient = Brush.verticalGradient(0.0f to MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), 1.0f to Color.Transparent)
                    val bottomGradient = Brush.verticalGradient(0.0f to Color.Transparent, 1.0f to MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))

                    if (showTopFade) {
                        Box(modifier = Modifier.fillMaxWidth().height(UiConstants.GRADIENT_OVERLAY_HEIGHT).align(Alignment.TopCenter).clip(MaterialTheme.shapes.medium).background(topGradient))
                    }
                    if (showBottomFade) {
                        Box(modifier = Modifier.fillMaxWidth().height(UiConstants.GRADIENT_OVERLAY_HEIGHT).align(Alignment.BottomCenter).clip(MaterialTheme.shapes.medium).background(bottomGradient))
                    }
                }
            }
        }

        uiState.attemptToRename?.let { (parentPath, attempt) ->
            var newPlayerName by remember { mutableStateOf(attempt.playerName) }
            val copy = aesthetic.dialogCopy
            AlertDialog(
                onDismissRequest = { viewModel.clearAttemptToRename() },
                title = { Text(copy.renameTitle(com.quokkalabs.reversey.ui.theme.RenamableItemType.PLAYER)) },
                text = { OutlinedTextField(value = newPlayerName, onValueChange = { newPlayerName = it }, label = { Text(copy.renameHint) }) },
                confirmButton = { Button(onClick = { if (newPlayerName.isNotBlank()) viewModel.renamePlayer(parentPath, attempt, newPlayerName); viewModel.clearAttemptToRename() }) { Text("Save") } },
                dismissButton = { Button(onClick = { viewModel.clearAttemptToRename() }) { Text("Cancel") } }
            )
        }

        if (uiState.showTutorial) {
            TutorialOverlay(onDismiss = { viewModel.dismissTutorial() }, onComplete = { viewModel.completeTutorial() })
        }

        if (uiState.showQualityWarning) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissQualityWarning() },
                title = { Text("Poor Quality Recording") },
                text = { Text(uiState.qualityWarningMessage) },
                confirmButton = { Button(onClick = { viewModel.dismissQualityWarning() }) { Text("Got it") } },
                dismissButton = { Button(onClick = { viewModel.dismissQualityWarning() }) { Text("Re-record") } },
                icon = { Icon(Icons.Default.Info, contentDescription = "Warning", tint = MaterialTheme.colorScheme.primary) }
            )
        }

        AnalysisToast(isVisible = uiState.showAnalysisToast)
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
        Button(
            onClick = onRequestPermission,
            modifier = Modifier.size(UiConstants.PERMISSION_BUTTON_SIZE),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Mic, "Request Permission", tint = Color.White, modifier = Modifier.size(UiConstants.MIC_ICON_SIZE))
                Text("Grant\nPermission", color = Color.White, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
            }
        }
    } else {
        Box(
            modifier = Modifier.size(UiConstants.RECORD_BUTTON_SIZE),
            contentAlignment = Alignment.Center
        ) {
            aesthetic.components.RecordButton(
                isRecording = isRecording,
                isProcessing = false,
                aesthetic = aesthetic,
                onStartRecording = onStartRecording,
                onStopRecording = onStopRecording
            )
        }
    }
}

@Composable
fun EnhancedWaveformVisualizer(
    amplitudes: List<Float>,
    modifier: Modifier = Modifier,
    barWidth: Dp = UiConstants.WAVEFORM_BAR_WIDTH,
    barGap: Dp = UiConstants.WAVEFORM_BAR_GAP
) {
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

            val gradient = Brush.verticalGradient(
                colors = listOf(
                    materialColors.primary.copy(alpha = 0.3f),
                    materialColors.primary.copy(alpha = 0.8f),
                    materialColors.primary
                ),
                startY = y,
                endY = y + barHeight
            )

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