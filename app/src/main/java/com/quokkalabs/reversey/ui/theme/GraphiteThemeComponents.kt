package com.quokkalabs.reversey.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.quokkalabs.reversey.data.models.ChallengeType
import com.quokkalabs.reversey.data.models.PlayerAttempt
import com.quokkalabs.reversey.data.models.Recording

/**
 * ✏️ GRAPHITE SKETCH THEME
 * Hand-drawn art, pencil textures, paper vibes.
 */
object GraphiteTheme {
    const val THEME_ID = "graphite_sketch"

    val data = AestheticThemeData(
        id = THEME_ID,
        name = "Graphite Sketch",
        description = "✏️ Hand-drawn art, pencil textures, paper vibes",
        components = GraphiteThemeComponents(),

        // Visuals
        primaryGradient = Brush.verticalGradient(
            colors = listOf(
                Color(0xFFF8F8F8),
                Color(0xFFEEEEEE),
                Color(0xFFE0E0E0)
            )
        ),
        cardBorder = Color(0xFF2A2A2A),
        primaryTextColor = Color(0xFF2A2A2A),
        secondaryTextColor = Color(0xFF505050),
        useGlassmorphism = false,
        glowIntensity = 0f,
        useSerifFont = false,
        useWideLetterSpacing = false,
        recordButtonEmoji = "✏️",
        scoreEmojis = mapOf(
            90 to "⭐",
            80 to "😊",
            70 to "👍",
            60 to "😐",
            0 to "😔"
        ),

        // M3 Overrides
        shadowElevation = 4f,

        // Interaction
        dialogCopy = DialogCopy(
            deleteTitle = { "Erase Sketch?" },
            deleteMessage = { type, name -> "Are you sure you want to erase '$name'? This artwork cannot be restored." },
            deleteConfirmButton = "Erase",
            deleteCancelButton = "Keep Art",
            shareTitle = "Exhibit Work",
            shareMessage = "Choose a gallery for your sketch:",
            renameTitle = { "Title Artwork" },
            renameHint = "Untitled Sketch"
        ),
        scoreFeedback = ScoreFeedback(
            getTitle = { score ->
                when {
                    score >= 90 -> "Masterpiece!"
                    score >= 80 -> "Beautiful Composition!"
                    score >= 70 -> "Strong Outline!"
                    score >= 60 -> "Rough Sketch"
                    else -> "Back to Basics"
                }
            },
            getMessage = { score ->
                when {
                    score >= 90 -> "Frame this immediately. It's perfect."
                    score >= 80 -> "Excellent shading and depth."
                    score >= 70 -> "Good form, needs a bit more detail."
                    score >= 60 -> "Keep sharpening your pencils."
                    else -> "Every artist starts somewhere. Try again!"
                }
            },
            getEmoji = { score ->
                when {
                    score >= 90 -> "🎨"
                    score >= 80 -> "🖼️"
                    score >= 70 -> "✏️"
                    score >= 60 -> "📝"
                    else -> "🗑️"
                }
            }
        ),
        menuColors = MenuColors.fromColors(
            primaryText = Color(0xFF2A2A2A),
            secondaryText = Color(0xFF505050),
            border = Color(0xFF2A2A2A),
            gradient = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFF8F8F8),
                    Color(0xFFEEEEEE),
                    Color(0xFFE0E0E0)
                )
            )
        )
    )
}

class GraphiteThemeComponents : ThemeComponents {

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