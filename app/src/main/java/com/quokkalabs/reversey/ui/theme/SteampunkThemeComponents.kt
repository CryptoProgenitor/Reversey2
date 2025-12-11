package com.quokkalabs.reversey.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.quokkalabs.reversey.data.models.ChallengeType
import com.quokkalabs.reversey.data.models.PlayerAttempt
import com.quokkalabs.reversey.data.models.Recording

/**
 * ⚙️ STEAMPUNK VICTORIAN THEME
 * Brass gears, copper pipes, and steam power.
 */
object SteampunkTheme {
    const val THEME_ID = "steampunk"

    val data = AestheticThemeData(
        id = THEME_ID,
        name = "Steampunk Victorian",
        description = "⚙️ Brass gears, copper pipes, and steam power",
        components = SteampunkThemeComponents(),

        // Visuals
        primaryGradient = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF2C1810),
                Color(0xFF8B4513),
                Color(0xFFCD7F32)
            )
        ),
        cardBorder = Color(0xFFD4AF37),
        primaryTextColor = Color(0xFFD4AF37),
        secondaryTextColor = Color(0xFFF5E6D3),
        useGlassmorphism = false,
        glowIntensity = 0.4f,
        useSerifFont = true,
        useWideLetterSpacing = true,
        recordButtonEmoji = "⚙️",
        scoreEmojis = mapOf(
            90 to "🏆",
            80 to "⚗️",
            70 to "🎩",
            60 to "⚙️",
            0 to "🔧"
        ),

        // M3 Overrides
        shadowElevation = 6f,

        // Interaction
        dialogCopy = DialogCopy(
            deleteTitle = { "Dismantle Mechanism?" },
            deleteMessage = { type, name -> "Are you certain you wish to scrap '$name'? The gears cannot be reassembled." },
            deleteConfirmButton = "Dismantle",
            deleteCancelButton = "Maintain",
            shareTitle = "Transmit via Telegraph",
            shareMessage = "Select frequency for transmission:",
            renameTitle = { "Re-label Blueprint" },
            renameHint = "New Specification"
        ),
        scoreFeedback = ScoreFeedback(
            getTitle = { score ->
                when {
                    score >= 90 -> "Master Engineer!"
                    score >= 80 -> "Splendid Invention!"
                    score >= 70 -> "Functional Prototype!"
                    score >= 60 -> "Needs Calibration!"
                    else -> "Steam Leak Detected!"
                }
            },
            getMessage = { score ->
                when {
                    score >= 90 -> "Precision engineering at its finest."
                    score >= 80 -> "The gears are turning smoothly."
                    score >= 70 -> "Operational, but noisy."
                    score >= 60 -> "Tighten the bolts and try again."
                    else -> "Back to the drawing board, old chap."
                }
            },
            getEmoji = { score ->
                when {
                    score >= 90 -> "🏆"
                    score >= 80 -> "🎩"
                    score >= 70 -> "⚙️"
                    score >= 60 -> "🔧"
                    else -> "💨"
                }
            }
        ),
        menuColors = MenuColors.fromColors(
            primaryText = Color(0xFFD4AF37),
            secondaryText = Color(0xFFF5E6D3),
            border = Color(0xFFD4AF37),
            gradient = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF2C1810),
                    Color(0xFF8B4513),
                    Color(0xFFCD7F32)
                )
            )
        )
    )
}

class SteampunkThemeComponents : ThemeComponents {

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
        SharedDefaultComponents.MaterialRecordingCard(
            recording, aesthetic, isPlaying, isPaused, progress, onPlay, onPause, onStop, onDelete, onShare, onRename, isGameModeEnabled, onStartAttempt,

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
        SharedDefaultComponents.MaterialAttemptCard(
            attempt, aesthetic, currentlyPlayingPath, isPaused, progress, onPlay, onPause, onStop,
            onRenamePlayer, onDeleteAttempt, onShareAttempt, onJumpToParent
        )
    }

    @Composable
    override fun RecordButton(
        isRecording: Boolean,
        isProcessing: Boolean,
        aesthetic: AestheticThemeData,
        onStartRecording: () -> Unit,
        onStopRecording: () -> Unit,
        countdownProgress: Float  // 🎯 PHASE 3
    ) {
        SharedDefaultComponents.MaterialRecordButton(isRecording, countdownProgress) {
            if (isRecording) onStopRecording() else onStartRecording()
        }
    }

    @Composable
    override fun AppBackground(
        aesthetic: AestheticThemeData,
        content: @Composable () -> Unit
    ) {
        SharedDefaultComponents.GradientBackground(aesthetic, content)
    }

    @Composable
    override fun ScoreCard(
        attempt: PlayerAttempt,
        aesthetic: AestheticThemeData,
        onDismiss: () -> Unit
    ) {
        SharedDefaultComponents.MaterialScoreCard(attempt, aesthetic, onDismiss)
    }

    @Composable
    override fun DeleteDialog(
        itemType: DeletableItemType,
        item: Any,
        aesthetic: AestheticThemeData,
        onConfirm: () -> Unit,
        onDismiss: () -> Unit
    ) {
        SharedDefaultComponents.MaterialDeleteDialog(itemType, item, aesthetic, onConfirm, onDismiss)
    }

    @Composable
    override fun ShareDialog(
        recording: Recording?,
        attempt: PlayerAttempt?,
        aesthetic: AestheticThemeData,
        onShare: (String) -> Unit,
        onDismiss: () -> Unit
    ) {
        SharedDefaultComponents.MaterialShareDialog(recording, attempt, aesthetic, onShare, onDismiss)
    }

    @Composable
    override fun RenameDialog(
        itemType: RenamableItemType,
        currentName: String,
        aesthetic: AestheticThemeData,
        onRename: (String) -> Unit,
        onDismiss: () -> Unit
    ) {
        SharedDefaultComponents.MaterialRenameDialog(itemType, currentName, aesthetic, onRename, onDismiss)
    }
}