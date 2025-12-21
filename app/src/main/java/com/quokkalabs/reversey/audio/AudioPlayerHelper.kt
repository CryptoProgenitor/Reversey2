package com.quokkalabs.reversey.audio

import android.media.MediaPlayer
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thread-safe audio player helper.
 * CRITICAL FIX: Added @Synchronized to prevent race conditions when multiple
 * threads call play/pause/resume/stop simultaneously. Previous implementation
 * could crash if two threads accessed mediaPlayer at the same time.
 */
@Singleton
class AudioPlayerHelper @Inject constructor() {

    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // UI State exposed as Flows
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _progress = MutableStateFlow(0f)
    val progress = _progress.asStateFlow()

    private val _currentPath = MutableStateFlow<String?>(null)
    val currentPath = _currentPath.asStateFlow()

    @Synchronized
    fun play(path: String, onCompletion: () -> Unit = {}) {
        stop() // Clean up previous

        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(path)
                prepare()
                start()
                setOnCompletionListener {
                    stop()
                    onCompletion()
                }
            }

            _currentPath.value = path
            _isPlaying.value = true

            startProgressPolling()

        } catch (e: Exception) {
            Log.e("AudioPlayerHelper", "Error playing file: $path", e)
            stop()
        }
    }

    @Synchronized
    fun pause() {
        try {
            mediaPlayer?.let { mp ->
                if (mp.isPlaying) {
                    mp.pause()
                    _isPlaying.value = false
                    progressJob?.cancel()
                }
            }
        } catch (e: Exception) {
            Log.e("AudioPlayerHelper", "Error pausing player", e)
        }
    }

    @Synchronized
    fun resume() {
        try {
            mediaPlayer?.let { mp ->
                if (!mp.isPlaying) {
                    mp.start()
                    _isPlaying.value = true
                    startProgressPolling()
                }
            }
        } catch (e: Exception) {
            Log.e("AudioPlayerHelper", "Error resuming player", e)
        }
    }

    @Synchronized
    fun stop() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.stop()
            }
            mediaPlayer?.release()
        } catch (e: Exception) {
            Log.e("AudioPlayerHelper", "Error stopping player", e)
        } finally {
            mediaPlayer = null
            progressJob?.cancel()
            _isPlaying.value = false
            _progress.value = 0f
            _currentPath.value = null
        }
    }

    fun cleanup() {
        stop()
        scope.cancel()
    }

    private fun startProgressPolling() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive && mediaPlayer?.isPlaying == true) {
                val current = mediaPlayer?.currentPosition ?: 0
                val total = mediaPlayer?.duration ?: 1
                if (total > 0) {
                    _progress.value = current.toFloat() / total.toFloat()
                }
                delay(100) // 10fps update rate - reduces recomposition
            }
        }
    }
}