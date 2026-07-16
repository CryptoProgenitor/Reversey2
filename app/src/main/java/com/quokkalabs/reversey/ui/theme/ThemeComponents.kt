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
 *
 * Implementation options:
 * - Vanilla themes: Use DefaultThemeComponents() for zero-boilerplate delegation
 * - Pro themes: Implement this interface directly with custom UI
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
        onShare: (String) -> Unit,
        onRename: (String, String) -> Unit,
        isGameModeEnabled: Boolean,
        onStartAttempt: (Recording, ChallengeType) -> Unit,
        activeAttemptRecordingPath: String? = null,  // 🎯 Which recording has active attempt
        onStopAttempt: (() -> Unit)? = null          // 🎯 Stop attempt callback
    )

    /**
     * Family Container Component
     * Wraps a recording ("challenge") card together with all of its attempt
     * ("try") cards in a single visual unit: an outer border that mimics the
     * recording tile's border, plus a collapse/expand button anchored at the
     * top-right corner of the recording tile.
     *
     * The default implementation delegates to
     * [SharedDefaultComponents.MaterialFamilyContainer]; pro themes can
     * override to draw the envelope in their own visual language.
     *
     * The border and button are only shown when the recording has attempts —
     * a family of one has nothing to collapse.
     */
    @Composable
    fun FamilyContainer(
        recording: Recording,
        aesthetic: AestheticThemeData,
        isExpanded: Boolean,
        onToggleExpanded: () -> Unit,
        recordingContent: @Composable () -> Unit,
        attemptsContent: @Composable () -> Unit
    ) {
        SharedDefaultComponents.MaterialFamilyContainer(
            recording = recording,
            aesthetic = aesthetic,
            isExpanded = isExpanded,
            onToggleExpanded = onToggleExpanded,
            recordingContent = recordingContent,
            attemptsContent = attemptsContent
        )
    }

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
     */
    @Composable
    fun ShareDialog(
        recording: Recording?,
        attempt: PlayerAttempt?,
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
