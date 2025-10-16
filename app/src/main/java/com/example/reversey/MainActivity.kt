package com.example.reversey

import coil.ImageLoader
import android.os.Build.VERSION.SDK_INT



import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.example.reversey.ui.theme.ReVerseYTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val themeViewModel: ThemeViewModel = viewModel()
            val currentTheme by themeViewModel.theme.collectAsState()
            val darkModePreference by themeViewModel.darkModePreference.collectAsState()

            val useDarkTheme = when (darkModePreference) {
                "Light" -> false
                "Dark" -> true
                else -> isSystemInDarkTheme()
            }

            ReVerseYTheme(themeName = currentTheme, darkTheme = useDarkTheme) {
                MainApp(themeViewModel = themeViewModel)
            }
        }
    }
}

@Composable
fun MainApp(themeViewModel: ThemeViewModel) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val currentTheme by themeViewModel.theme.collectAsState()
    val darkModePreference by themeViewModel.darkModePreference.collectAsState()
    val isGameModeEnabled by themeViewModel.gameModeEnabled.collectAsState()

    var showClearAllDialog by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawerContent(
                navController = navController,
                closeDrawer = { scope.launch { drawerState.close() } },
                onClearAll = { showClearAllDialog = true }
            )
        }
    ) {
        NavHost(navController = navController, startDestination = "home") {
            composable("home") {
                // Create an instance of the ViewModel.
                // It will be automatically retained across screen rotations.
                val audioViewModel: AudioViewModel = viewModel()
                AudioReverserApp(
                    viewModel = audioViewModel, // Pass the ViewModel down
                    openDrawer = { scope.launch { drawerState.open() } },
                    showClearAllDialog = showClearAllDialog,
                    onClearAllDialogDismiss = { showClearAllDialog = false },
                    isGameModeEnabled = isGameModeEnabled // <-- PASS THE FLAG
                )
            }
            composable("about") {
                AboutScreen(navController = navController)
            }
            composable("settings") {
                SettingsScreen(
                    navController = navController,
                    currentTheme = currentTheme,
                    onThemeChange = { themeName -> themeViewModel.setTheme(themeName) },
                    currentDarkModePreference = darkModePreference,
                    onDarkModePreferenceChange = { preference -> themeViewModel.setDarkModePreference(preference) },
                    isGameModeEnabled = isGameModeEnabled,
                    onGameModeChange = { isEnabled -> themeViewModel.setGameMode(isEnabled) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDrawerContent(
    navController: NavController,
    closeDrawer: () -> Unit,
    onClearAll: () -> Unit
) {
    ModalDrawerSheet {
        Text("ReVerseY Menu", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium)
        HorizontalDivider()
        NavigationDrawerItem(
            label = { Text("Home") },
            selected = navController.currentDestination?.route == "home",
            onClick = {
                navController.navigate("home") { popUpTo("home") { inclusive = true } }
                closeDrawer()
            }
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Info, contentDescription = "About") },
            label = { Text("About") },
            selected = navController.currentDestination?.route == "about",
            onClick = {
                navController.navigate("about")
                closeDrawer()
            }
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text("Settings") },
            selected = navController.currentDestination?.route == "settings",
            onClick = {
                navController.navigate("settings")
                closeDrawer()
            }
        )
        HorizontalDivider()
        NavigationDrawerItem(
            label = { Text("Clear All Recordings", color = MaterialTheme.colorScheme.error) },
            icon = { Icon(Icons.Default.Delete, contentDescription = "Clear All", tint = MaterialTheme.colorScheme.error) },
            selected = false,
            onClick = {
                onClearAll()
                closeDrawer()
            }
        )
    }
}

// PASTE THIS ENTIRE, CORRECTED AboutScreen FUNCTION

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(navController: NavController) {
    val context = LocalContext.current
    val audioViewModel: AudioViewModel = viewModel()
    val uiState by audioViewModel.uiState.collectAsState()

    // --- ONE ImageLoader, defined once. This is correct. ---
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

    // This Box will contain both the screen content AND the GIF overlay
    Box(modifier = Modifier.fillMaxSize()) {

        // --- The Original Screen Content (This part is fine) ---
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("About ReVerseY") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("ReVerseY", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(8.dp))
                // You can bump this to your final version number when you commit
                Text("Version 2.0.3.claude", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "A fun audio recording and reversing game built by Ed Dark (c) 2025. Inspired by CPD!",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.clickable {
                        // Play the sound directly here, as it needs the context.
                        // We check if the *next* tap will be the 5th one.
                        if (uiState.cpdTaps + 1 == 5) {
                            val mediaPlayer = MediaPlayer.create(context, R.raw.egg_crack)
                            mediaPlayer?.start()
                            mediaPlayer?.setOnCompletionListener { mp -> mp.release() }
                        }
                        // Tell the ViewModel that a tap occurred. The ViewModel handles ALL state changes.
                        audioViewModel.onCpdTapped()
                    }
                )
            }
        }
        // --- The CORRECTED GIF Overlay ---
        if (uiState.showEasterEgg) {
            // Use a LaunchedEffect to give the UI a moment to compose before dismissing
            LaunchedEffect(Unit) {
                // Wait for the animation to play. A simple delay works well here.
                // We'll use a duration slightly longer than the GIF's animation.
                // Let's assume the GIF is about 1.5 seconds long.
                delay(1500L) // 1.5 seconds
                audioViewModel.dismissEasterEgg()
            }

            AsyncImage(
                model = R.drawable.cracking_egg, // The model can be simple
                contentDescription = "Easter Egg GIF",
                // Use the imageLoader you already defined at the top of the function
                imageLoader = imageLoader,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.75f))
            )
        }
    }
}


// NEW, "DUMB" UI COMPOSABLE
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AudioReverserApp(
    viewModel: AudioViewModel,
    openDrawer: () -> Unit,
    showClearAllDialog: Boolean,
    onClearAllDialogDismiss: () -> Unit,
    isGameModeEnabled: Boolean // <-- ADD THIS

) {
    val uiState by viewModel.uiState.collectAsState()
    val recordAudioPermissionState = rememberPermissionState(android.Manifest.permission.RECORD_AUDIO)
    val listState = rememberLazyListState()
    val context = LocalContext.current

    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { onClearAllDialogDismiss() },
            title = { Text("Clear All Recordings?") },
            text = { Text("This will permanently delete all of your recordings. This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllRecordings() // Call ViewModel function
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ReVerseY") },
                navigationIcon = { IconButton(onClick = openDrawer) { Icon(Icons.Default.Menu, "Menu") } },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            RecordButton(
                isRecording = uiState.isRecording,
                hasPermission = recordAudioPermissionState.status.isGranted,
                onRequestPermission = { recordAudioPermissionState.launchPermissionRequest() },
                onStartRecording = { viewModel.startRecording() },
                onStopRecording = { viewModel.stopRecording() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isRecording) {
                WaveformVisualizer(
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
                    Text(text = uiState.statusText, style = MaterialTheme.typography.bodyLarge)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Box(modifier = Modifier.fillMaxSize()) {
                val showTopFade by remember { derivedStateOf { listState.canScrollBackward } }
                val showBottomFade by remember { derivedStateOf { listState.canScrollForward } }

                // Start LazyColumn block
                LazyColumn(
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(MaterialTheme.shapes.medium)
                ) {
                    // Use forEach to have access to the 'recording' variable for both items
                    uiState.recordings.forEach { recording ->
                        // 1. Show the parent recording item
                        item(key = "parent_${recording.originalPath}") {
                            RecordingItem(
                                recording = recording,
                                isPlaying = uiState.currentlyPlayingPath == recording.originalPath || uiState.currentlyPlayingPath == recording.reversedPath,
                                isPaused = uiState.isPaused,
                                progress = if (uiState.currentlyPlayingPath == recording.originalPath || uiState.currentlyPlayingPath == recording.reversedPath) uiState.playbackProgress else 0f,
                                onPlay = { path -> viewModel.play(path) },
                                onPause = { viewModel.pause() },
                                onStop = { viewModel.stopPlayback() },
                                onDelete = { viewModel.deleteRecording(recording) },
                                onShare = { path ->
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "audio/wav"
                                        putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(context, "${context.packageName}.provider", File(path)))
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share Recording"))
                                },
                                onRename = { oldPath, newName -> viewModel.renameRecording(oldPath, newName) },
                                isGameModeEnabled = isGameModeEnabled,
                                onStartAttempt = { rec -> viewModel.startAttemptRecording(rec) }
                            )
                        }

                        // 2. Show all the child attempts for THIS recording
                        // 2. Show all the child attempts for THIS recording
                        // 2. Show all the child attempts for THIS recording
                        items(
                            count = recording.attempts.size,
                            key = { index -> "attempt_${recording.originalPath}_${index}" }
                        ) { index ->
                            val attempt = recording.attempts[index]
                            val attemptPlaybackPath = attempt.reversedAttemptFilePath ?: attempt.attemptFilePath
                            AttemptItem(
                                attempt = attempt,
                                currentlyPlayingPath = uiState.currentlyPlayingPath,
                                isPaused = uiState.isPaused,
                                progress = if (uiState.currentlyPlayingPath == attemptPlaybackPath) uiState.playbackProgress else 0f,
                                onPlay = { path -> viewModel.play(path) },
                                onPause = { viewModel.pause() },
                                onStop = { viewModel.stopPlayback() },
                                onRenamePlayer = { oldAttempt, newName ->
                                    viewModel.renamePlayer(recording.originalPath, oldAttempt, newName)
                                },
                                onDeleteAttempt = { attemptToDelete ->
                                    viewModel.deleteAttempt(recording.originalPath, attemptToDelete)
                                },
                                onShareAttempt = { path ->  // ADD THIS BLOCK
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "audio/wav"
                                        putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(context, "${context.packageName}.provider", File(path)))
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share Attempt"))
                                }
                            )
                        }
                    }
                }

                val topGradient = Brush.verticalGradient(0.0f to MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), 1.0f to Color.Transparent)
                val bottomGradient = Brush.verticalGradient(0.0f to Color.Transparent, 1.0f to MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))

                if (showTopFade) {
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .align(Alignment.TopCenter)
                        .clip(MaterialTheme.shapes.medium)
                        .background(topGradient))
                }
                if (showBottomFade) {
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .align(Alignment.BottomCenter)
                        .clip(MaterialTheme.shapes.medium)
                        .background(bottomGradient))
                }
            }
        }
    }
}

// THE RecordingItem composable remains here as it's part of the UI layer
// This is the NEW, COMPLETE, and CORRECT version of the RecordingItem composable
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RecordingItem(
    recording: Recording, isPlaying: Boolean,
    isPaused: Boolean,
    progress: Float,
    onPlay: (String) -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit,
    onDelete: (Recording) -> Unit,
    onShare: (String) -> Unit,
    onRename: (String, String) -> Unit,
    isGameModeEnabled: Boolean,
    onStartAttempt: (Recording) -> Unit
) {
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }

    // Re-introduce the Column to structure the card correctly
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // --- TOP ROW: Name and Rename Logic ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = recording.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showRenameDialog = true } // Click name to rename
                )
            }

            // This is the Rename Dialog for WAV files
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
                                // Make sure the name ends with .wav
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

            Spacer(modifier = Modifier.height(8.dp))

            // --- MIDDLE: Progress Bar ---
            LinearProgressIndicator(
                progress = { if (isPlaying) progress else 0f },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // --- BOTTOM ROW: All Action Buttons ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left Group: Share
                IconButton(onClick = { showShareDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share Recording",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                // Center Group: Playback
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isPlaying) {
                        Button(
                            onClick = { onPause() },
                            shape = CircleShape,
                            modifier = Modifier.size(50.dp),
                            contentPadding = PaddingValues(0.dp)
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
                        Button(
                            onClick = { onStop() },
                            shape = CircleShape,
                            modifier = Modifier.size(50.dp),
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Stop",
                                tint = Color.White
                            )
                        }
                    } else {
                        Button(
                            onClick = { onPlay(recording.originalPath) },
                            shape = CircleShape,
                            modifier = Modifier.size(50.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play Original",
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { onPlay(recording.reversedPath!!) },
                            enabled = recording.reversedPath != null,
                            shape = CircleShape,
                            modifier = Modifier.size(50.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Replay,
                                contentDescription = "Play Reversed",
                                tint = Color.White
                            )
                        }
                    }
                }

                // Right Group: Game and Delete
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isGameModeEnabled) {
                        Button(
                            onClick = { onStartAttempt(recording) },
                            shape = CircleShape,
                            modifier = Modifier.size(50.dp),
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Sing Along",
                                tint = Color.White
                            )
                        }
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Recording",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // This is the Delete Dialog
            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Delete Recording?") },
                    text = { Text("Are you sure you want to delete '${recording.name}'? This action cannot be undone.") },
                    confirmButton = {
                        Button(
                            onClick = {
                                onDelete(recording)
                                showDeleteDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) { Text("Delete") }
                    },
                    dismissButton = {
                        Button(onClick = { showDeleteDialog = false }) { Text("Cancel") }
                    }
                )
            }

            // This is the Share Dialog (which just launches the intent)
            // This is the Share Dialog - let user choose which version to share-CLAUDE
            // This is the Share Dialog - let user choose which version to share
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
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Share Original")
                            }
                            if (recording.reversedPath != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        onShare(recording.reversedPath!!)
                                        showShareDialog = false
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Share Reversed")
                                }
                            }
                        }
                    },
                    confirmButton = { },
                    dismissButton = {
                        Button(onClick = { showShareDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

// NEW, SIMPLER RecordButton that is just UI
@Composable
fun RecordButton(
    isRecording: Boolean,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit
) {
    val buttonShape = if (isRecording) OctagonShape() else CircleShape
    val buttonColor = if (isRecording) Color.Red else Color(0xFF00C853)

    Button(
        onClick = {
            if (!hasPermission) {
                onRequestPermission()
                return@Button
            }
            if (isRecording) onStopRecording() else onStartRecording()
        },
        modifier = Modifier.size(100.dp),
        shape = buttonShape,
        colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = if (isRecording) "STOP" else "REC", fontSize = 16.sp, fontWeight = FontWeight.Bold, softWrap = false)
        }
    }
}

@Composable
fun WaveformVisualizer(
    amplitudes: List<Float>,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary,
    barWidth: Dp = 4.dp, // Changed from Float
    barGap: Dp = 2.dp     // Changed from Float
) {
    Canvas(modifier = modifier) {
        val canvasHeight = size.height
        val maxAmplitude = 1.0f

        // FIX: Convert Dp to raw pixels (Float) before using them
        val barWidthPx = barWidth.toPx()
        val barGapPx = barGap.toPx()

        val totalBarWidth = barWidthPx + barGapPx
        val maxBars = (size.width / totalBarWidth).toInt()
        val barsToDraw = amplitudes.takeLast(maxBars)

        barsToDraw.forEachIndexed { index, amplitude ->
            val barHeight = (amplitude / maxAmplitude) * canvasHeight
            val x = index * totalBarWidth
            val y = (canvasHeight - barHeight) / 2
            drawRect(
                color = barColor,
                topLeft = Offset(x, y),
                size = Size(barWidthPx, barHeight)
            )
        }
    }
}

// --- GLOBAL HELPER FUNCTIONS can remain here ---
fun getRecordingsDir(context: Context): File {
    return File(context.filesDir, "recordings").apply { mkdirs() }
}

fun createAudioFile(context: Context): File {
    val timeStamp = SimpleDateFormat("dd MMM yy - HH.mm.ss", Locale.US).format(Date())
    val fileName = "REC-$timeStamp.wav"
    return File(getRecordingsDir(context), fileName)
}

fun formatFileName(fileName: String): String {
    return fileName.removeSuffix(".wav")
}

fun shareRecording(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_STREAM, uri)
        type = "audio/wav"
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share Recording"))
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

// Global constants object
object AudioConstants {
    const val SAMPLE_RATE = 44100
    const val CHANNEL_CONFIG = android.media.AudioFormat.CHANNEL_IN_MONO
    const val AUDIO_FORMAT = android.media.AudioFormat.ENCODING_PCM_16BIT
    const val MAX_WAVEFORM_SAMPLES = 200
}

// This is the NEW, UPGRADED Composable for renaming (from 2.0.2.e) a player's attempt
@Composable
fun AttemptItem(
    attempt: PlayerAttempt,
    currentlyPlayingPath: String?,
    isPaused: Boolean,
    progress: Float,
    onPlay: (String) -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit,
    onRenamePlayer: ((PlayerAttempt, String) -> Unit)? = null,
    onDeleteAttempt: ((PlayerAttempt) -> Unit)? = null,
    onShareAttempt: ((String) -> Unit)? = null
) {
    val isPlayingThis = currentlyPlayingPath == (attempt.reversedAttemptFilePath ?: attempt.attemptFilePath)
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 40.dp, end = 8.dp, top = 4.dp, bottom = 4.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(12.dp)
    ) {
        // Top row for Player Name and Score ONLY
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = attempt.playerName,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.clickable { showRenameDialog = true }
            )
            Text(text = "${attempt.score}%", style = MaterialTheme.typography.headlineSmall)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Progress bar
        if (isPlayingThis) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Bottom row: Share, Play controls, Delete (matching parent card layout)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Share button
            IconButton(onClick = { showShareDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share Attempt",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Center: Play controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (isPlayingThis) {
                    Button(
                        onClick = onPause,
                        shape = CircleShape,
                        modifier = Modifier.size(40.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        val pauseIcon = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause
                        Icon(
                            imageVector = pauseIcon,
                            contentDescription = "Pause/Resume Attempt",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onStop,
                        shape = CircleShape,
                        modifier = Modifier.size(40.dp),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop",
                            tint = Color.White
                        )
                    }
                } else {
                    Button(
                        onClick = {
                            val pathToPlay = attempt.reversedAttemptFilePath ?: attempt.attemptFilePath
                            onPlay(pathToPlay)
                        },
                        shape = CircleShape,
                        modifier = Modifier.size(40.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play Attempt",
                            tint = Color.White
                        )
                    }
                }
            }

            // Right: Delete button
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Attempt",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    // Rename player dialog
    if (showRenameDialog) {
        var newPlayerName by remember { mutableStateOf(attempt.playerName) }
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Player") },
            text = {
                OutlinedTextField(
                    value = newPlayerName,
                    onValueChange = { newPlayerName = it },
                    label = { Text("Player Name") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (newPlayerName.isNotBlank() && onRenamePlayer != null) {
                        onRenamePlayer(attempt, newPlayerName)
                    }
                    showRenameDialog = false
                }) { Text("Rename") }
            },
            dismissButton = {
                Button(onClick = { showRenameDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Delete dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Attempt?") },
            text = { Text("Are you sure you want to delete ${attempt.playerName}'s attempt? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        if (onDeleteAttempt != null) {
                            onDeleteAttempt(attempt)
                        }
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Share dialog
    if (showShareDialog) {
        AlertDialog(
            onDismissRequest = { showShareDialog = false },
            title = { Text("Share Attempt") },
            text = {
                Column {
                    Text("Which version would you like to share?")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            if (onShareAttempt != null) {
                                onShareAttempt(attempt.attemptFilePath)
                            }
                            showShareDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Share Original (What ${attempt.playerName} Sang)")
                    }
                    if (attempt.reversedAttemptFilePath != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                if (onShareAttempt != null) {
                                    onShareAttempt(attempt.reversedAttemptFilePath!!)
                                }
                                showShareDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Share Reversed (How It Sounds)")
                        }
                    }
                }
            },
            confirmButton = { },
            dismissButton = {
                Button(onClick = { showShareDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}