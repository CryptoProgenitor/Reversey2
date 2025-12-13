package com.quokkalabs.reversey.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.quokkalabs.reversey.data.models.ChallengeType
import com.quokkalabs.reversey.data.models.PlayerAttempt
import com.quokkalabs.reversey.data.models.Recording

/**
 * 💀 JEOSEUNG SHADOWS THEME
 * Dark reaper energy, golden souls and mysteries.
 */
object JeoseungTheme {
    const val THEME_ID = "jeoseung_shadows"

    val data = AestheticThemeData(
        id = THEME_ID,
        name = "Jeoseung Shadows",
        description = "💀 Dark reaper energy, golden souls and mysteries",
        components = JeoseungThemeComponents(),

        // Visuals
        primaryGradient = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF0A0A0A),
                Color(0xFF1C1C1C),
                Color(0xFF2D2D2D)
            )
        ),
        cardBorder = Color(0x66FFD700),
        primaryTextColor = Color(0xFFFFD700),
        secondaryTextColor = Color(0xFFCCCCCC),
        useGlassmorphism = true,
        glowIntensity = 0.7f,
        useSerifFont = true,
        useWideLetterSpacing = true,
        recordButtonEmoji = "💀",
        scoreEmojis = mapOf(
            90 to "🔥",
            80 to "👻",
            70 to "🌙",
            60 to "⚡",
            0 to "💀"
        ),

        // M3 Overrides
        cardAlpha = 0.1f,
        shadowElevation = 14f,

        // Interaction
        dialogCopy = DialogCopy.default(),
        scoreFeedback = ScoreFeedback.default(),
        menuColors = MenuColors.fromColors(
            primaryText = Color(0xFFFFD700),
            secondaryText = Color(0xFFCCCCCC),
            border = Color(0x66FFD700),
            gradient = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF0A0A0A),
                    Color(0xFF1C1C1C),
                    Color(0xFF2D2D2D)
                )
            )
        )
    )
}

class JeoseungThemeComponents : ThemeComponents {

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