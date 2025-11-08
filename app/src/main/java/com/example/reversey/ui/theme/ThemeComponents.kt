package com.example.reversey.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.reversey.data.models.ChallengeType
import com.example.reversey.data.models.PlayerAttempt
import com.example.reversey.data.models.Recording
import com.example.reversey.ui.components.UnifiedRecordButton

/**
 * 🎨 THEME COMPONENTS INTERFACE
 *
 * Core of the Component Composition architecture.
 * Each theme provides its own implementation of these components.
 *
 * GLUTE Principle: Themes compose their own UI without central routing logic.
 */
interface ThemeComponents {

    /**
     * Recording Item Component
     * Displays a single recording with playback controls
     */
    @Composable
    fun RecordingItem(
        recording: Recording,
        aesthetic: AestheticThemeData,
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
    )

    /**
     * Attempt Item Component
     * Displays a player's attempt with score and controls
     */
    @Composable
    fun AttemptItem(
        attempt: PlayerAttempt,
        aesthetic: AestheticThemeData,
        currentlyPlayingPath: String?,
        isPaused: Boolean,
        progress: Float,
        onPlay: (String) -> Unit,
        onPause: () -> Unit,
        onStop: () -> Unit,
        onRenamePlayer: ((PlayerAttempt, String) -> Unit)? = null,
        onDeleteAttempt: ((PlayerAttempt) -> Unit)? = null,
        onShareAttempt: ((String) -> Unit)? = null,
        onJumpToParent: (() -> Unit)? = null
    )

    /**
     * Record Button Component
     * Main recording button with theme-specific styling
     */
    @Composable
    fun RecordButton(
        isRecording: Boolean,
        isProcessing: Boolean,
        aesthetic: AestheticThemeData,
        onStartRecording: () -> Unit,
        onStopRecording: () -> Unit
    )

    /**
     * App Background Component
     * Full-screen background with decorations, animations, and theme-specific elements
     * This enables immersive theming beyond just gradients
     */
    @Composable
    fun AppBackground(
        aesthetic: AestheticThemeData,
        content: @Composable () -> Unit
    )
}

/**
 * 🎨 DEFAULT THEME COMPONENTS
 *
 * Material 3 implementation that works for most themes.
 * Used by: Y2K, Cottagecore, Dark Academia, Vaporwave, Jeoseung, Steampunk, Cyberpunk, Graphite
 *
 * Themes can use this directly or create their own custom implementation.
 */
class DefaultThemeComponents : ThemeComponents {

    @Composable
    override fun RecordingItem(
        recording: Recording,
        aesthetic: AestheticThemeData,
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
        // Call UnifiedRecordingItem which will route to default implementation
        // (UnifiedRecordingItem handles routing internally)
        com.example.reversey.ui.components.UnifiedRecordingItem(
            recording = recording,
            aesthetic = aesthetic,
            isPlaying = isPlaying,
            isPaused = isPaused,
            progress = progress,
            onPlay = onPlay,
            onPause = onPause,
            onStop = onStop,
            onDelete = onDelete,
            onShare = onShare,
            onRename = onRename,
            isGameModeEnabled = isGameModeEnabled,
            onStartAttempt = onStartAttempt
        )
    }

    @Composable
    override fun AttemptItem(
        attempt: PlayerAttempt,
        aesthetic: AestheticThemeData,
        currentlyPlayingPath: String?,
        isPaused: Boolean,
        progress: Float,
        onPlay: (String) -> Unit,
        onPause: () -> Unit,
        onStop: () -> Unit,
        onRenamePlayer: ((PlayerAttempt, String) -> Unit)?,
        onDeleteAttempt: ((PlayerAttempt) -> Unit)?,
        onShareAttempt: ((String) -> Unit)?,
        onJumpToParent: (() -> Unit)?
    ) {
        // Delegate to UnifiedAttemptItem which handles default routing
        com.example.reversey.ui.components.UnifiedAttemptItem(
            attempt = attempt,
            currentlyPlayingPath = currentlyPlayingPath,
            isPaused = isPaused,
            progress = progress,
            onPlay = onPlay,
            onPause = onPause,
            onStop = onStop,
            onRenamePlayer = onRenamePlayer,
            onDeleteAttempt = onDeleteAttempt,
            onShareAttempt = onShareAttempt,
            onJumpToParent = onJumpToParent
        )
    }

    @Composable
    override fun RecordButton(
        isRecording: Boolean,
        isProcessing: Boolean,
        aesthetic: AestheticThemeData,
        onStartRecording: () -> Unit,
        onStopRecording: () -> Unit
    ) {
        // Delegate to existing UnifiedRecordButton
        // UnifiedRecordButton has a simpler signature, so we adapt it here
        UnifiedRecordButton(
            isRecording = isRecording,
            onClick = {
                if (isRecording) {
                    onStopRecording()
                } else {
                    onStartRecording()
                }
            }
        )
    }

    @Composable
    override fun AppBackground(
        aesthetic: AestheticThemeData,
        content: @Composable () -> Unit
    ) {
        // Default: Simple gradient background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(aesthetic.primaryGradient)
        ) {
            content()
        }
    }
}