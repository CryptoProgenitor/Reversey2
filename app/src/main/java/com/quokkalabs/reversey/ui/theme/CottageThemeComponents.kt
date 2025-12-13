package com.quokkalabs.reversey.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.quokkalabs.reversey.data.models.ChallengeType
import com.quokkalabs.reversey.data.models.PlayerAttempt
import com.quokkalabs.reversey.data.models.Recording

/**
 * 🌸 COTTAGECORE DREAMS THEME
 * Soft pastels, florals, and cozy vibes.
 */
object CottageTheme {
    const val THEME_ID = "cottagecore"

    val data = AestheticThemeData(
        id = THEME_ID,
        name = "Cottagecore Dreams",
        description = "🌸 Soft pastels, florals, and cozy vibes",
        components = CottageThemeComponents(),

        // Visuals
        primaryGradient = Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFEEEF8),
                Color(0xFFFFF4E6),
                Color(0xFFE8F5E9)
            )
        ),
        cardBorder = Color(0xFFF8BBD0),
        primaryTextColor = Color(0xFF2E2E2E),
        secondaryTextColor = Color(0xFF424242),
        useGlassmorphism = false,
        glowIntensity = 0f,
        useSerifFont = true,
        recordButtonEmoji = "🌸",
        scoreEmojis = mapOf(
            90 to "🦋",
            80 to "🌷",
            70 to "🌼",
            60 to "🌿",
            0 to "🌱"
        ),

        // Interaction
        dialogCopy = DialogCopy.default(),
        scoreFeedback = ScoreFeedback.default(),
        menuColors = MenuColors.fromColors(
            primaryText = Color(0xFF2E2E2E),
            secondaryText = Color(0xFF424242),
            border = Color(0xFFF8BBD0),
            gradient = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFFEEEF8),
                    Color(0xFFFFF4E6),
                    Color(0xFFE8F5E9)
                )
            )
        )
    )
}

class CottageThemeComponents : ThemeComponents {

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
        onJumpToParent: (() -> Unit)?,
        onOverrideScore: ((Int) -> Unit)?
    ) {
        SharedDefaultComponents.MaterialAttemptCard(
            attempt, aesthetic, currentlyPlayingPath, isPaused, progress, onPlay, onPause, onStop,
            onRenamePlayer, onDeleteAttempt, onShareAttempt, onJumpToParent, onOverrideScore
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
        onDismiss: () -> Unit,
        onOverrideScore: ((Int) -> Unit)
    ) {
        SharedDefaultComponents.MaterialScoreCard(attempt, aesthetic, onDismiss, onOverrideScore)
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