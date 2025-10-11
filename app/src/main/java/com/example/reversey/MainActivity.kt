package com.example.reversey

import android.Manifest
import android.content.Context
import androidx.compose.animation.core.copy
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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
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
        Divider()
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
            Text("Version 1.0", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "A fun audio recording and reversing game built by Ed Dark (c) 2025.",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}


@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AudioReverserApp(openDrawer: () -> Unit) { // CHANGED: Now takes a function to open the drawer
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var recordings by remember { mutableStateOf<List<Recording>>(emptyList()) }
    var isRecording by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("Ready to record") }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var recordingJob by remember { mutableStateOf<Job?>(null) }

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
            recordingJob?.cancel()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ReVerseY") },
                // NEW: Navigation icon to open the drawer
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
            RecordButton(
                isRecording = isRecording,
                hasPermission = recordAudioPermissionState.status.isGranted,
                onRequestPermission = { recordAudioPermissionState.launchPermissionRequest() },
                onStartRecording = {
                    isRecording = true
                    statusText = "Recording..."
                    recordingJob = coroutineScope.launch(Dispatchers.IO) {
                        val file = createAudioFile(context)
                        startRecording(context, file)
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
            Text(text = statusText, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(20.dp))
            Divider()
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(recordings) { recording ->
                    RecordingItem(
                        recording = recording,
                        onPlay = { path ->
                            mediaPlayer?.release()
                            mediaPlayer = MediaPlayer().apply {
                                try {
                                    setDataSource(path)
                                    prepare()
                                    start()
                                    setOnCompletionListener { it.release() }
                                } catch (e: IOException) {
                                    Log.e("MediaPlayer", "Failed to play file", e)
                                }
                            }
                        },
                        onDelete = { originalPath, reversedPath ->
                            coroutineScope.launch {
                                deleteRecording(originalPath, reversedPath)
                                updateRecordingsList()
                            }
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun RecordingItem(
    recording: Recording,
    onPlay: (String) -> Unit,
    onDelete: (originalPath: String, reversedPath: String?) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

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

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = { onPlay(recording.originalPath) },
                    shape = CircleShape,
                    modifier = Modifier.size(50.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play Original", tint = Color.White)
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = { recording.reversedPath?.let { onPlay(it) } },
                    enabled = recording.reversedPath != null,
                    shape = CircleShape,
                    modifier = Modifier.size(50.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(imageVector = Icons.Default.Replay, contentDescription = "Play Reversed", tint = Color.White)
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
                modifier = Modifier.align(Alignment.Start)
            )
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

private suspend fun startRecording(context: Context, file: File) {
    val sampleRate = 44100
    val channelConfig = AudioFormat.CHANNEL_IN_MONO
    val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
        return
    }

    val audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channelConfig, audioFormat, bufferSize)
    val buffer = ByteArray(bufferSize)

    try {
        FileOutputStream(file).use { fos ->
            audioRecord.startRecording()
            while (currentCoroutineContext().isActive) {
                val read = audioRecord.read(buffer, 0, buffer.size)
                if (read > 0) {
                    fos.write(buffer, 0, read)
                }
            }
        }
    } catch (e: Exception) {
        Log.e("Recording", "Error during recording", e)
    } finally {
        if (audioRecord.state == AudioRecord.STATE_INITIALIZED) {
            audioRecord.stop()
        }
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
                name = formatFileName(file.name),
                originalPath = file.absolutePath,
                reversedPath = if (reversedFile.exists()) reversedFile.absolutePath else null
            )
        }
}

private fun getLatestFile(context: Context): File? {
    val dir = getRecordingsDir(context)
    return dir.listFiles { _, name -> name.endsWith(".wav") && !name.contains("_reversed") }?.maxByOrNull { it.lastModified() }
}

@Composable
fun RecordButton(isRecording: Boolean, hasPermission: Boolean, onRequestPermission: () -> Unit, onStartRecording: () -> Unit, onStopRecording: () -> Unit) {
    Button(
        onClick = { if (hasPermission) { if (isRecording) onStopRecording() else onStartRecording() } else { onRequestPermission() } },
        modifier = Modifier.size(100.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(containerColor = if (isRecording) Color.Red else MaterialTheme.colorScheme.primary)
    ) {
        Text(text = if (isRecording) "STOP" else "REC", fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
}

data class Recording(val name: String, val originalPath: String, val reversedPath: String?)

fun getRecordingsDir(context: Context): File {
    return File(context.filesDir, "recordings").apply { mkdirs() }
}

fun createAudioFile(context: Context): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    return File(getRecordingsDir(context), "REC_${timeStamp}.wav")
}

private fun formatFileName(fileName: String): String {
    val parser = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
    val formatter = SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.US)

    return try {
        val dateTimeString = fileName.substring(4, fileName.length - 4)
        val date = parser.parse(dateTimeString)
        if (date != null) formatter.format(date) else "Invalid Date"
    } catch (e: Exception) {
        fileName
    }
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

