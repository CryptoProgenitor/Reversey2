package com.example.reversey

// update from v. XXX

import android.Manifest
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.experimental.and
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI
import kotlin.math.min


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    // NEW: App now starts at the MainApp composable which handles navigation
                    MainApp()
                }
            }
        }
    }
}

// NEW: A top-level composable to manage navigation state
@Composable
fun MainApp() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawerContent(navController = navController, closeDrawer = {
                scope.launch { drawerState.close() }
            })
        }
    ) {
        // NavHost contains all the screens your app can navigate to
        NavHost(navController = navController, startDestination = "home") {
            composable("home") {
                AudioReverserApp(openDrawer = {
                    scope.launch { drawerState.open() }
                })
            }
            composable("about") {
                AboutScreen(navController = navController)
            }
        }
    }
}

// NEW: Content for the slide-out navigation drawer
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDrawerContent(navController: NavController, closeDrawer: () -> Unit) {
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
    }
}

// NEW: A simple screen for the "About" page
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About ReVerseY") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack, // This line will now work
                            contentDescription = "Back",
                            modifier = Modifier.size(24.dp)
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
            Text("Version 1.1.3", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "A fun audio recording and reversing game built by Ed Dark (c) 2025. Inspired by CPD!",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

// CHANGED: Added @OptIn annotation
// This is the complete, correct, and final version of AudioReverserApp.
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AudioReverserApp(openDrawer: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // All `remember` calls are correctly placed here, at the top level of the composable.
    val listState = rememberLazyListState()
    var recordings by remember { mutableStateOf<List<Recording>>(emptyList()) }
    var isRecording by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("Ready to record") }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var recordingJob by remember { mutableStateOf<Job?>(null) }
    var currentlyPlayingPath by remember { mutableStateOf<String?>(null) }
    var playbackProgress by remember { mutableStateOf(0f) }
    var playbackJob by remember { mutableStateOf<Job?>(null) }
    var isPaused by remember { mutableStateOf(false) }

    // Derived state declarations for the fading edge effect, correctly placed.
    val showTopFade by remember {
        derivedStateOf {
            // Use the simple boolean property from the state
            listState.canScrollBackward
        }
    }
    val showBottomFade by remember {
        derivedStateOf {
            // Use the simple boolean property from the state
            listState.canScrollForward
        }
    }

    val waveformAmplitudes = remember { mutableStateListOf<Float>() }
    val recordAudioPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)

    fun updateRecordingsList() {
        coroutineScope.launch {
            recordings = loadRecordings(context)
        }
    }

    LaunchedEffect(Unit) {
        updateRecordingsList()
    }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
            mediaPlayer = null
            recordingJob?.cancel()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ReVerseY") },
                navigationIcon = {
                    IconButton(onClick = openDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
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

            // THIS IS THE FULL, CORRECT RecordButton CALL
            RecordButton(
                isRecording = isRecording,
                hasPermission = recordAudioPermissionState.status.isGranted,
                onRequestPermission = { recordAudioPermissionState.launchPermissionRequest() },
                onStartRecording = {
                    isRecording = true
                    statusText = "Recording..."
                    waveformAmplitudes.clear()
                    val file = createAudioFile(context)
                    recordingJob = coroutineScope.launch(Dispatchers.IO) {
                        startRecording(context, file, waveformAmplitudes)
                    }
                },
                onStopRecording = {
                    isRecording = false
                    statusText = "Processing..."
                    coroutineScope.launch(Dispatchers.IO) {
                        recordingJob?.cancelAndJoin()
                        val lastRecording = getLatestFile(context)
                        val reversedFile = reverseWavFile(lastRecording)
                        withContext(Dispatchers.Main) {
                            statusText = if (reversedFile != null) "Reversed successfully!" else "Error: Could not reverse audio."
                            updateRecordingsList()
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // THIS IS THE FULL, CORRECT WaveformVisualizer / Text BLOCK
            if (isRecording) {
                WaveformVisualizer(
                    amplitudes = waveformAmplitudes,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                )
            } else {
                Text(text = statusText, style = MaterialTheme.typography.bodyLarge)
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider()

            // This Box contains the list and the fading edge overlays.
            Box(modifier = Modifier.fillMaxSize()) {
                // The LazyColumn with its full, correct content.XXX
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(MaterialTheme.shapes.medium) // THIS IS THE FIX
                ) {
                    items(recordings, key = { it.originalPath }) { recording ->
                        val isCurrentlyPlayingThisItem = currentlyPlayingPath == recording.originalPath || currentlyPlayingPath == recording.reversedPath
                        RecordingItem(
                            recording = recording,
                            isPlaying = isCurrentlyPlayingThisItem,
                            isPaused = isPaused,
                            progress = if (isCurrentlyPlayingThisItem) playbackProgress else 0f,
                            onPlay = { path ->
                                mediaPlayer?.release()
                                playbackJob?.cancel()
                                mediaPlayer = MediaPlayer().apply {
                                    try {
                                        setDataSource(path)
                                        prepare()
                                        start()
                                        currentlyPlayingPath = path
                                        isPaused = false
                                        playbackProgress = 0f
                                        setOnCompletionListener {
                                            playbackJob?.cancel()
                                            currentlyPlayingPath = null
                                            isPaused = false
                                        }
                                        playbackJob = coroutineScope.launch {
                                            while (isActive) {
                                                withContext(Dispatchers.Main) {
                                                    val currentPos = mediaPlayer?.currentPosition?.toFloat() ?: 0f
                                                    val totalDuration = mediaPlayer?.duration?.toFloat() ?: 0f
                                                    playbackProgress = if (totalDuration > 0) currentPos / totalDuration else 0f
                                                }
                                                delay(100)
                                            }
                                        }
                                    } catch (e: Exception) {
                                        Log.e("MediaPlayer", "Error playing", e)
                                        currentlyPlayingPath = null
                                    }
                                }
                            },
                            onPause = {
                                if (mediaPlayer?.isPlaying == true) {
                                    mediaPlayer?.pause()
                                    isPaused = true
                                    playbackJob?.cancel()
                                } else {
                                    mediaPlayer?.start()
                                    isPaused = false
                                    playbackJob = coroutineScope.launch {
                                        while (isActive) {
                                            withContext(Dispatchers.Main) {
                                                val currentPos = mediaPlayer?.currentPosition?.toFloat() ?: 0f
                                                val totalDuration = mediaPlayer?.duration?.toFloat() ?: 0f
                                                playbackProgress = if (totalDuration > 0) currentPos / totalDuration else 0f
                                            }
                                            delay(100)
                                        }
                                    }
                                }
                            },
                            onStop = {
                                mediaPlayer?.stop()
                                mediaPlayer?.release()
                                mediaPlayer = null
                                playbackJob?.cancel()
                                currentlyPlayingPath = null
                                isPaused = false
                                playbackProgress = 0f
                            },
                            onDelete = { originalPath, reversedPath ->
                                coroutineScope.launch {
                                    deleteRecording(originalPath, reversedPath)
                                    updateRecordingsList()
                                }
                            },
                            onShare = { path ->
                                shareRecording(context, File(path))
                            },
                            onRename = { oldPath, newName ->
                                coroutineScope.launch {
                                    renameRecording(oldPath, newName)
                                    updateRecordingsList()
                                }
                            }
                        )
                    }
                }

                // --- FADING EDGE GRADIENTS (Subtler Version) ---

                // This gradient will be used for the top fade
                val topGradient = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), // Start with 50% transparent purple
                        Color.Transparent
                    )
                )
                // This gradient will be used for the bottom fade
                val bottomGradient = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) // End with 50% transparent purple
                    )
                )


                // Top fading edge overlay, shown only when not at the top
                if (showTopFade) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                            .align(Alignment.TopCenter)
                            .clip(MaterialTheme.shapes.medium) // THIS IS THE FIX
                            .background(brush = topGradient)
                    )
                }
                // Bottom fading edge overlay, shown only when not at the bottom
                if (showBottomFade) {    Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .align(Alignment.BottomCenter)
                        .clip(MaterialTheme.shapes.medium) // THIS IS THE FIX
                        .background(brush = bottomGradient)
                )
                }
            }
        }
    }
}







@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RecordingItem(
    recording: Recording,
    isPlaying: Boolean,
    isPaused: Boolean,
    progress: Float,
    onPlay: (String) -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit,
    onDelete: (originalPath: String, reversedPath: String?) -> Unit,
    onShare: (path: String) -> Unit,
    onRename: (oldPath: String, newName: String) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var editableName by remember { mutableStateOf("") }

    // --- RENAME DIALOG (Restored) ---
    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Recording") },
            text = {
                OutlinedTextField(
                    value = editableName,
                    onValueChange = { editableName = it },
                    label = { Text("Filename") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRename(recording.originalPath, editableName)
                        showRenameDialog = false
                    },
                    enabled = editableName.isNotBlank() && editableName.endsWith(".wav")
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                Button(onClick = { showRenameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // --- SHARE DIALOG (Restored) ---
    if (showShareDialog) {
        AlertDialog(
            onDismissRequest = { showShareDialog = false },
            title = { Text("Share Which Version?") },
            text = { Text("Which version of the recording would you like to share?") },
            confirmButton = {
                Button(
                    onClick = {
                        recording.reversedPath?.let { onShare(it) }
                        showShareDialog = false
                    },
                    enabled = recording.reversedPath != null
                ) {
                    Text("Reversed")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        onShare(recording.originalPath)
                        showShareDialog = false
                    }
                ) {
                    Text("Original")
                }
            }
        )
    }

    // --- DELETE DIALOG (Restored) ---
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Recording") },
            text = { Text("Are you sure you want to permanently delete this recording?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete(recording.originalPath, recording.reversedPath)
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // --- CARD UI ---
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { showShareDialog = true }) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "Share Recording", tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.weight(1f))

                if (isPlaying) {
                    Button(onClick = { onPause() }, shape = CircleShape, modifier = Modifier.size(50.dp), contentPadding = PaddingValues(0.dp)) {
                        val pauseIcon = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause
                        Icon(imageVector = pauseIcon, contentDescription = "Pause/Resume", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { onStop() }, shape = CircleShape, modifier = Modifier.size(50.dp), contentPadding = PaddingValues(0.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                        Icon(imageVector = Icons.Default.Stop, contentDescription = "Stop", tint = Color.White)
                    }
                } else {
                    Button(onClick = { onPlay(recording.originalPath) }, shape = CircleShape, modifier = Modifier.size(50.dp), contentPadding = PaddingValues(0.dp)) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play Original", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { onPlay(recording.reversedPath!!) }, enabled = recording.reversedPath != null, shape = CircleShape, modifier = Modifier.size(50.dp), contentPadding = PaddingValues(0.dp)) {
                        Icon(imageVector = Icons.Default.Replay, contentDescription = "Play Reversed", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Recording", tint = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = recording.name,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = {},
                        onLongClick = {
                            editableName = File(recording.originalPath).name
                            showRenameDialog = true
                        }
                    )
            )

            if (isPlaying) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}



// All other helper functions (startRecording, loadRecordings, deleteRecording, etc.) remain the same.
// ... (The rest of your file from RecordButton downwards is unchanged and correct)
private suspend fun deleteRecording(originalPath: String, reversedPath: String?) = withContext(Dispatchers.IO) {
    try {
        File(originalPath).let { if (it.exists()) it.delete() }
        reversedPath?.let { File(it).let { f -> if (f.exists()) f.delete() } }
    } catch (e: Exception) {
        Log.e("Delete", "Error deleting files for $originalPath", e)
    }
}

private suspend fun startRecording(
    context: Context,
    file: File,
    amplitudes: MutableList<Float> // NEW parameter
) {
    val sampleRate = 44100
    val channelConfig = AudioFormat.CHANNEL_IN_MONO
    val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
        return
    }

    val audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channelConfig, audioFormat, bufferSize)
    val buffer = ShortArray(bufferSize / 2) // Use ShortArray for 16-bit audio

    try {
        FileOutputStream(file).use { fos ->
            audioRecord.startRecording()
            while (currentCoroutineContext().isActive) {
                val readSize = audioRecord.read(buffer, 0, buffer.size)
                if (readSize > 0) {
                    // Convert ShortArray to ByteArray to write to file
                    val byteBuffer = ByteArray(readSize * 2)
                    for (i in 0 until readSize) {
                        byteBuffer[i * 2] = buffer[i].and(0xFF).toByte()

                        byteBuffer[i * 2 + 1] = (buffer[i].toInt() shr 8).toByte()
                    }
                    fos.write(byteBuffer)

                    // NEW: Calculate amplitude and update state
                    val maxAmplitude = buffer.maxOfOrNull { kotlin.math.abs(it.toFloat()) } ?: 0f
                    val normalizedAmplitude = maxAmplitude / Short.MAX_VALUE

                    // Update the list on the Main thread to be safe for UI
                    withContext(Dispatchers.Main) {
                        amplitudes.add(normalizedAmplitude)
                        // Keep the list at a reasonable size
                        if (amplitudes.size > 200) {
                            amplitudes.removeAt(0)
                        }
                    }
                }
            }
        }
    } catch (e: Exception) {
        Log.e("Recording", "Error during recording", e)
    } finally {
        if (audioRecord.state == AudioRecord.STATE_INITIALIZED) audioRecord.stop()
        audioRecord.release()
        addWavHeader(file)
    }
}

private fun addWavHeader(file: File) {
    if (!file.exists() || file.length() == 0L) return

    val rawData = file.readBytes()
    try {
        val tempFile = File.createTempFile("temp_wav", ".tmp", file.parentFile)
        FileOutputStream(tempFile).use { fos ->
            writeWavHeader(fos, rawData, 1, 44100, 16)
        }
        file.delete()
        tempFile.renameTo(file)
    } catch (e: IOException) {
        Log.e("WavHeader", "Failed to add WAV header", e)
    }
}

private suspend fun reverseWavFile(originalFile: File?): File? = withContext(Dispatchers.IO) {
    if (originalFile == null || !originalFile.exists() || originalFile.length() < 44) {
        return@withContext null
    }

    try {
        val fileBytes = originalFile.readBytes()
        val rawPcmData = fileBytes.drop(44).toByteArray()
        if (rawPcmData.isEmpty()) return@withContext null

        val reversedPcmData = ByteArray(rawPcmData.size)
        var i = 0
        while (i < rawPcmData.size - 1) {
            reversedPcmData[i] = rawPcmData[rawPcmData.size - 2 - i]
            reversedPcmData[i + 1] = rawPcmData[rawPcmData.size - 1 - i]
            i += 2
        }

        val reversedFile = File(originalFile.parent, originalFile.name.replace(".wav", "_reversed.wav"))
        FileOutputStream(reversedFile).use { fos ->
            writeWavHeader(fos, reversedPcmData, 1, 44100, 16)
        }
        return@withContext reversedFile
    } catch (e: Exception) {
        Log.e("Reverser", "Failed to reverse file", e)
        return@withContext null
    }
}

private suspend fun loadRecordings(context: Context): List<Recording> = withContext(Dispatchers.IO) {
    val dir = getRecordingsDir(context)
    val originalFiles = dir.listFiles { _, name -> name.endsWith(".wav") && !name.contains("_reversed") } ?: emptyArray()

    originalFiles
        .sortedByDescending { it.lastModified() }
        .map { file ->
            val reversedFile = File(dir, file.name.replace(".wav", "_reversed.wav"))
            Recording(
                name = formatFileName(file.name), // This will now just remove ".wav"
                originalPath = file.absolutePath,
                reversedPath = if (reversedFile.exists()) reversedFile.absolutePath else null
            )
        }
}




// CHANGED: This function is now much simpler.
private suspend fun renameRecording(
    oldPath: String,
    newName: String // This is the full new name, e.g., "My Cat Purring.wav"
): Boolean = withContext(Dispatchers.IO) {
    // Basic validation for the new name. Still important.
    if (newName.isBlank() || !newName.endsWith(".wav")) return@withContext false

    try {
        val oldFile = File(oldPath)
        if (!oldFile.exists()) return@withContext false

        // Create the new file object directly with the user-provided new name.
        val newFile = File(oldFile.parent, newName)

        // Prevent overwriting an existing file, which renameTo() can do silently on some systems.
        if (newFile.exists()) return@withContext false

        // Perform the rename.
        val renameSuccess = oldFile.renameTo(newFile)

        if (renameSuccess) {
            // If the original file was renamed, find and rename the reversed version too.
            val oldReversedPath = oldPath.replace(".wav", "_reversed.wav")
            val oldReversedFile = File(oldReversedPath)
            if (oldReversedFile.exists()) {
                val newReversedName = newName.replace(".wav", "_reversed.wav")
                val newReversedFile = File(oldReversedFile.parent, newReversedName)
                oldReversedFile.renameTo(newReversedFile)
            }
        }
        return@withContext renameSuccess
    } catch (e: Exception) {
        Log.e("Rename", "Error renaming file", e)
        return@withContext false
    }
}




private fun getLatestFile(context: Context): File? {
    val dir = getRecordingsDir(context)
    return dir.listFiles { _, name -> name.endsWith(".wav") && !name.contains("_reversed") }?.maxByOrNull { it.lastModified() }
}

@Composable
fun RecordButton(
    isRecording: Boolean,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit
) {// Define shape and color based on the recording state
    val buttonShape = if (isRecording) OctagonShape() else CircleShape
    val buttonColor = if (isRecording) Color.Red else Color(0xFF00C853) // A nice Material Green

    Button(
        onClick = {
            if (!hasPermission) {
                onRequestPermission()
                return@Button
            }
            if (isRecording) onStopRecording() else onStartRecording()
        },
        modifier = Modifier.size(100.dp),
        shape = buttonShape, // Use the dynamic shape
        colors = ButtonDefaults.buttonColors(containerColor = buttonColor) // Use the dynamic color
    ) {
        // We still use a Box to ensure alignment, but the crucial change is in the Text composable
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = if (isRecording) "STOP" else "REC",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                // THIS IS THE FIX: Prevent the text from breaking into a new line
                softWrap = false
            )
        }
    }
}

// NEW: Composable to draw the waveform
@Composable
fun WaveformVisualizer(
    amplitudes: List<Float>,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary,    barWidth: Float = 4f,
    barGap: Float = 2f
) {
    Canvas(modifier = modifier) {
        val canvasHeight = size.height
        val maxAmplitude = 1.0f // Normalized max amplitude

        val totalBarWidth = barWidth + barGap
        val maxBars = (size.width / totalBarWidth).toInt()
        val barsToDraw = amplitudes.takeLast(maxBars)

        barsToDraw.forEachIndexed { index, amplitude ->
            val barHeight = (amplitude / maxAmplitude) * canvasHeight
            val x = index * totalBarWidth
            val y = (canvasHeight - barHeight) / 2

            drawRect(
                color = barColor,
                topLeft = androidx.compose.ui.geometry.Offset(x, y),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
            )
        }
    }
}

data class Recording(
    val name: String,         // The human-readable name for display
    val originalPath: String,
    val reversedPath: String?
)

fun getRecordingsDir(context: Context): File {
    return File(context.filesDir, "recordings").apply { mkdirs() }
}

fun createAudioFile(context: Context): File {
    // THIS IS THE FIX: We use the human-readable format for the filename itself.
    // The format "dd MMM yy - HH.mm.ss" is filename-safe (no colons).
    val timeStamp = SimpleDateFormat("dd MMM yy - HH.mm.ss", Locale.US).format(Date())
    val fileName = "REC-$timeStamp.wav" // e.g., "REC-12 Oct 25 - 19.35.15.wav"

    return File(getRecordingsDir(context), fileName)
}


private fun formatFileName(fileName: String): String {
    // Since the filename is now always human-readable,
    // we just need to remove the ".wav" extension for display.
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

// NEW: Helper function to launch the Android Share Sheet
fun shareRecording(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "audio/wav" // Set the MIME type for WAV files
        putExtra(Intent.EXTRA_STREAM, uri)
        // Grant temporary read permission to the app that handles the share
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    // Use a chooser to let the user pick how to share
    val chooser = Intent.createChooser(intent, "Share Recording")
    context.startActivity(chooser)
}


// NEW: Custom Shape class for the octagon button
class OctagonShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val radius = min(centerX, centerY)
            val angle = (2 * PI / 8).toFloat() // 8 sides for an octagon
            val startAngle = -angle / 2f // Start at the top middle point

            // Move to the first point
            moveTo(
                centerX + radius * cos(startAngle),
                centerY + radius * sin(startAngle)
            )
            // Draw lines to the other 7 points
            for (i in 1 until 8) {
                lineTo(
                    centerX + radius * cos(startAngle + angle * i),
                    centerY + radius * sin(startAngle + angle * i)
                )
            }
            close() // Close the path to form the octagon
        }
        return Outline.Generic(path)
    }
}

