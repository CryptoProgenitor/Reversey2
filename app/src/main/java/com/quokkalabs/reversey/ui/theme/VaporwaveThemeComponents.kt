package com.quokkalabs.reversey.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.quokkalabs.reversey.data.models.ChallengeType
import com.quokkalabs.reversey.data.models.PlayerAttempt
import com.quokkalabs.reversey.data.models.Recording

/**
 * 🌴 NEON VAPORWAVE THEME
 * Cyan and magenta, retro-futuristic aesthetic.
 */
object VaporwaveTheme {
    const val THEME_ID = "vaporwave"

    val data = AestheticThemeData(
        id = THEME_ID,
        name = "Neon Vaporwave",
        description = "🌴 Cyan and magenta, retro-futuristic aesthetic",
        components = VaporwaveThemeComponents(),

        // Visuals
        primaryGradient = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF2E003E),
                Color(0xFF3D0066),
                Color(0xFFFF6EC7)
            )
        ),
        cardBorder = Color(0xFF00FFFF),
        primaryTextColor = Color(0xFF00FFFF),
        secondaryTextColor = Color(0xFFE0E0E0),
        useGlassmorphism = true,
        glowIntensity = 0f,
        useSerifFont = false,
        useWideLetterSpacing = true,
        recordButtonEmoji = "🌴",
        scoreEmojis = mapOf(
            90 to "💎",
            80 to "🌊",
            70 to "🎭",
            60 to "🎮",
            0 to "📼"
        ),

        // M3 Overrides
        cardAlpha = 0.1f,
        shadowElevation = 12f,

        // Interaction
        dialogCopy = DialogCopy.default(),
        scoreFeedback = ScoreFeedback.default(),
        menuColors = MenuColors.fromColors(
            primaryText = Color(0xFF00FFFF),
            secondaryText = Color(0xFFE0E0E0),
            border = Color(0xFF00FFFF),
            gradient = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF2E003E),
                    Color(0xFF3D0066),
                    Color(0xFFFF6EC7)
                )
            )
        )
    )
}

class VaporwaveThemeComponents : ThemeComponents {

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
        onStopRecording: () -> Unit
    ) {
        SharedDefaultComponents.MaterialRecordButton(isRecording) {
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