package com.quokkalabs.reversey.ui.theme

import androidx.compose.runtime.Composable
import com.quokkalabs.reversey.data.models.ChallengeType
import com.quokkalabs.reversey.data.models.PlayerAttempt
import com.quokkalabs.reversey.data.models.Recording

/**
 * 🎨 THEME COMPONENTS INTERFACE
 *
 * Core of the Component Composition architecture.
 * Each theme provides its own implementation of these components.
 *
 * GLUTE Principle: Themes compose their own UI without central routing logic.
 */
interface ThemeComponents {

    // --- CORE DISPLAY COMPONENTS ---

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
     * 🎯 PHASE 3: Added countdownProgress for timed recording arc
     */
    @Composable
    fun RecordButton(
        isRecording: Boolean,
        isProcessing: Boolean,
        aesthetic: AestheticThemeData,
        onStartRecording: () -> Unit,
        onStopRecording: () -> Unit,
        countdownProgress: Float = 1f  // 🎯 PHASE 3: 1.0 → 0.0 for arc timer
    )

    /**
     * App Background Component
     * Full-screen background with decorations, animations, and theme-specific elements
     */
    @Composable
    fun AppBackground(
        aesthetic: AestheticThemeData,
        content: @Composable () -> Unit
    )

    // --- NEW: DIALOG & INTERACTION COMPONENTS ---

    /**
     * Score Card Component
     * Displays the result of a challenge
     */
    @Composable
    fun ScoreCard(
        attempt: PlayerAttempt,
        aesthetic: AestheticThemeData,
        onDismiss: () -> Unit
    )

    /**
     * Delete Confirmation Dialog
     */
    @Composable
    fun DeleteDialog(
        itemType: DeletableItemType,
        item: Any, // Recording or PlayerAttempt
        aesthetic: AestheticThemeData,
        onConfirm: () -> Unit,
        onDismiss: () -> Unit
    )

    /**
     * Share Dialog
     */
    @Composable
    fun ShareDialog(
        recording: Recording?, // Null if sharing attempt
        attempt: PlayerAttempt?, // Null if sharing recording
        aesthetic: AestheticThemeData,
        onShare: (String) -> Unit,
        onDismiss: () -> Unit
    )

    /**
     * Rename Dialog
     */
    @Composable
    fun RenameDialog(
        itemType: RenamableItemType,
        currentName: String,
        aesthetic: AestheticThemeData,
        onRename: (String) -> Unit,
        onDismiss: () -> Unit
    )
}

// --- SUPPORTING ENUMS ---

enum class DeletableItemType { RECORDING, ATTEMPT }
enum class RenamableItemType { RECORDING, PLAYER }