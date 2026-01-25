package com.quokkalabs.reversey.ui.theme

import androidx.compose.runtime.Composable
import com.quokkalabs.reversey.data.models.ChallengeType
import com.quokkalabs.reversey.data.models.PlayerAttempt
import com.quokkalabs.reversey.data.models.Recording
import java.io.File

/**
 * 🎨 THEME COMPONENTS INTERFACE
 *
 * Core of the Component Composition architecture.
 * Each theme provides its own implementation of these components.
 *
 * GLUTE Principle: Themes compose their own UI without central routing logic.
 *
 * Implementation options:
 * - Vanilla themes: Use DefaultThemeComponents() for zero-boilerplate delegation
 * - Pro themes: Implement this interface directly with custom UI
 *
 * v3.0 BREAKING CHANGE:
 * - onShare now takes (Recording) instead of (String) for Remote Play support
 * - onShareAttempt now takes (PlayerAttempt) instead of (String)
 * - Caller is responsible for ZIP generation via ViewModel
 */
interface ThemeComponents {

    // --- CORE DISPLAY COMPONENTS ---

    /**
     * Recording Item Component
     * Displays a single recording with playback controls
     *
     * 🔧 POLYMORPHIC BUTTONS: currentlyPlayingPath enables each button
     * (Play/Rewind) to independently track if IT is the one playing,
     * allowing proper Pause/Resume state per button.
     *
     * 🎮 REMOTE PLAY: onShare now receives the full Recording object
     * so the ViewModel can generate a Challenge ZIP package.
     */
    @Composable
    fun RecordingItem(
        recording: Recording,
        aesthetic: AestheticThemeData,
        isPaused: Boolean,
        progress: Float,
        currentlyPlayingPath: String?,  // 🔧 Which specific file is playing
        onPlay: (String) -> Unit,
        onPause: () -> Unit,
        onStop: () -> Unit,
        onDelete: (Recording) -> Unit,
        onShare: (Recording) -> Unit,   // 🎮 v3.0: Changed from (String) for Remote Play
        onRename: (String, String) -> Unit,
        isGameModeEnabled: Boolean,
        onStartAttempt: (Recording, ChallengeType) -> Unit,
        activeAttemptRecordingPath: String? = null,  // 🎯 Which recording has active attempt
        onStopAttempt: (() -> Unit)? = null          // 🎯 Stop attempt callback
    )

    /**
     * Attempt Item Component
     * Displays a player's attempt with score and controls
     *
     * 🎮 REMOTE PLAY: onShareAttempt now receives the full PlayerAttempt object.
     * The caller captures the parent Recording in the closure to generate
     * a Response ZIP package via ViewModel.
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
        onShareAttempt: ((PlayerAttempt) -> Unit)? = null,  // 🎮 v3.0: Changed from (String)
        onJumpToParent: (() -> Unit)? = null,
        onOverrideScore: ((Int) -> Unit)? = null,
        onResetScore: (() -> Unit)? = null
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
     */
    @Composable
    fun AppBackground(
        aesthetic: AestheticThemeData,
        content: @Composable () -> Unit
    )

    // --- DIALOG & INTERACTION COMPONENTS ---

    /**
     * Score Card Component
     * Displays the result of a challenge
     */
    @Composable
    fun ScoreCard(
        attempt: PlayerAttempt,
        aesthetic: AestheticThemeData,
        onDismiss: () -> Unit,
        onOverrideScore: (Int) -> Unit = { }
    )

    /**
     * Delete Confirmation Dialog
     */
    @Composable
    fun DeleteDialog(
        itemType: DeletableItemType,
        item: Any,
        aesthetic: AestheticThemeData,
        onConfirm: () -> Unit,
        onDismiss: () -> Unit
    )

    /**
     * Share Dialog
     *
     * 🎮 REMOTE PLAY: onShare now takes the file and MIME type directly.
     * The ViewModel generates the ZIP before showing the dialog.
     */
    @Composable
    fun ShareDialog(
        recording: Recording?,
        attempt: PlayerAttempt?,
        aesthetic: AestheticThemeData,
        onShare: (file: File, mimeType: String) -> Unit,  // 🎮 v3.0: Changed for Remote Play
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
