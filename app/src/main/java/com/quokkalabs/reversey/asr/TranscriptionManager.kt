package com.quokkalabs.reversey.asr

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 🗣️ PHASE 3: Manages transcription queue and automatic offline retry.
 * 
 * When a recording is made offline:
 * 1. Recording saved with transcriptionPending = true
 * 2. Audio path added to pending queue
 * 3. When device comes online, queue is processed automatically
 * 4. Recording updated with transcription
 * 
 * GLUTE Principle: Automatic, invisible to user, robust to failures
 */
@Singleton
class TranscriptionManager @Inject constructor(
    private val context: Context,
    private val speechRecognitionService: SpeechRecognitionService
) {
    companion object {
        private const val TAG = "TranscriptionMgr"
        private const val QUEUE_FILE = "transcription_queue.json"
    }

    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val gson = Gson()
    
    // Pending transcription queue
    private val _pendingQueue = MutableStateFlow<List<PendingTranscription>>(emptyList())
    val pendingQueue: StateFlow<List<PendingTranscription>> = _pendingQueue.asStateFlow()
    
    // Processing state
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()
    
    // Callback for when transcription completes (to update Recording)
    private var onTranscriptionComplete: ((String, TranscriptionResult) -> Unit)? = null
    
    // Network callback for auto-retry
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    init {
        loadQueue()
        registerNetworkCallback()
    }

    /**
     * Set callback for transcription completion
     * Called when a pending transcription finishes (to update Recording model)
     */
    fun setOnTranscriptionComplete(callback: (audioPath: String, result: TranscriptionResult) -> Unit) {
        onTranscriptionComplete = callback
    }

    /**
     * Add a recording to the pending transcription queue
     * Called when recording is made offline
     */
    fun queueForTranscription(audioPath: String, recordingName: String) {
        Log.d(TAG, "Queueing for transcription: $audioPath")
        
        val pending = PendingTranscription(
            audioPath = audioPath,
            recordingName = recordingName,
            queuedAt = System.currentTimeMillis(),
            retryCount = 0
        )
        
        _pendingQueue.value = _pendingQueue.value + pending
        saveQueue()
        
        // Try immediately if online
        if (speechRecognitionService.isOnline()) {
            processQueue()
        }
    }

    /**
     * Process all pending transcriptions
     * Called automatically when device comes online
     */
    fun processQueue() {
        if (_isProcessing.value) {
            Log.d(TAG, "Already processing queue")
            return
        }
        
        if (_pendingQueue.value.isEmpty()) {
            Log.d(TAG, "Queue empty")
            return
        }
        
        if (!speechRecognitionService.isOnline()) {
            Log.d(TAG, "Device offline, skipping queue processing")
            return
        }
        
        managerScope.launch {
            _isProcessing.value = true
            Log.d(TAG, "Processing ${_pendingQueue.value.size} pending transcriptions")
            
            val completed = mutableListOf<String>()
            
            for (pending in _pendingQueue.value.toList()) {
                try {
                    val audioFile = File(pending.audioPath)
                    if (!audioFile.exists()) {
                        Log.w(TAG, "Audio file not found, removing from queue: ${pending.audioPath}")
                        completed.add(pending.audioPath)
                        continue
                    }
                    
                    Log.d(TAG, "Transcribing: ${pending.recordingName}")
                    val result = speechRecognitionService.transcribeFile(audioFile)
                    
                    if (result.isSuccess) {
                        Log.d(TAG, "Transcription complete: ${result.text?.take(50)}...")
                        onTranscriptionComplete?.invoke(pending.audioPath, result)
                        completed.add(pending.audioPath)
                    } else if (result.isOffline) {
                        Log.d(TAG, "Still offline, will retry later")
                        // Don't remove, will retry when online
                        break  // Stop processing, wait for network
                    } else {
                        // Error - increment retry count
                        val updated = pending.copy(retryCount = pending.retryCount + 1)
                        if (updated.retryCount >= 3) {
                            Log.w(TAG, "Max retries reached, removing: ${pending.audioPath}")
                            completed.add(pending.audioPath)
                        } else {
                            // Update retry count in queue
                            _pendingQueue.value = _pendingQueue.value.map {
                                if (it.audioPath == pending.audioPath) updated else it
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error transcribing ${pending.audioPath}", e)
                }
            }
            
            // Remove completed items
            _pendingQueue.value = _pendingQueue.value.filter { it.audioPath !in completed }
            saveQueue()
            
            _isProcessing.value = false
            Log.d(TAG, "Queue processing complete. ${_pendingQueue.value.size} remaining")
        }
    }

    /**
     * Register for network connectivity changes
     * Automatically process queue when device comes online
     */
    private fun registerNetworkCallback() {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return
        
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Log.d(TAG, "Network available - checking pending transcriptions")
                // Small delay to let connection stabilize
                managerScope.launch {
                    kotlinx.coroutines.delay(2000)
                    if (speechRecognitionService.isOnline()) {
                        processQueue()
                    }
                }
            }
            
            override fun onLost(network: Network) {
                Log.d(TAG, "Network lost")
            }
        }
        
        try {
            cm.registerNetworkCallback(request, networkCallback!!)
            Log.d(TAG, "Network callback registered")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register network callback", e)
        }
    }

    /**
     * Unregister network callback (call on app destroy)
     */
    fun cleanup() {
        networkCallback?.let { callback ->
            try {
                val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                cm?.unregisterNetworkCallback(callback)
                Log.d(TAG, "Network callback unregistered")
            } catch (e: Exception) {
                Log.e(TAG, "Error unregistering network callback", e)
            }
        }
        networkCallback = null
    }

    /**
     * Load pending queue from disk
     */
    private fun loadQueue() {
        try {
            val file = File(context.filesDir, QUEUE_FILE)
            if (file.exists()) {
                val json = file.readText()
                val type = object : TypeToken<List<PendingTranscription>>() {}.type
                _pendingQueue.value = gson.fromJson(json, type) ?: emptyList()
                Log.d(TAG, "Loaded ${_pendingQueue.value.size} pending transcriptions")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading transcription queue", e)
            _pendingQueue.value = emptyList()
        }
    }

    /**
     * Save pending queue to disk
     */
    private fun saveQueue() {
        try {
            val file = File(context.filesDir, QUEUE_FILE)
            val json = gson.toJson(_pendingQueue.value)
            file.writeText(json)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving transcription queue", e)
        }
    }

    /**
     * Get count of pending transcriptions
     */
    fun getPendingCount(): Int = _pendingQueue.value.size

    /**
     * Check if a specific recording is pending transcription
     */
    fun isPending(audioPath: String): Boolean {
        return _pendingQueue.value.any { it.audioPath == audioPath }
    }

    /**
     * Manually retry all pending transcriptions
     */
    fun retryAll() {
        if (speechRecognitionService.isOnline()) {
            processQueue()
        } else {
            Log.w(TAG, "Cannot retry - device offline")
        }
    }

    /**
     * Clear the pending queue (use with caution)
     */
    fun clearQueue() {
        _pendingQueue.value = emptyList()
        saveQueue()
        Log.d(TAG, "Queue cleared")
    }
}

/**
 * A recording pending transcription
 */
data class PendingTranscription(
    val audioPath: String,
    val recordingName: String,
    val queuedAt: Long,
    val retryCount: Int = 0
)
