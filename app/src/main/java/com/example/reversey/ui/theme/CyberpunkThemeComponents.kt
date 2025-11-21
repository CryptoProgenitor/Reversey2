package com.example.reversey.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.reversey.data.models.ChallengeType
import com.example.reversey.data.models.PlayerAttempt
import com.example.reversey.data.models.Recording

/**
 * 🤖 CYBERPUNK 2099 THEME
 * Neon lights, digital underground, matrix vibes.
 */
object CyberpunkTheme {
    const val THEME_ID = "cyberpunk"

    val data = AestheticThemeData(
        id = THEME_ID,
        name = "Cyberpunk 2099",
        description = "🤖 Neon lights, digital underground, matrix vibes",
        components = CyberpunkThemeComponents(),

        // Visuals
        primaryGradient = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF0A0A0A),
                Color(0xFF1A0033),
                Color(0xFF000A1A)
            )
        ),
        cardBorder = Color(0xFF00FFFF),
        primaryTextColor = Color(0xFF00FFFF),
        secondaryTextColor = Color(0xFF80FF80),
        useGlassmorphism = true,
        glowIntensity = 0.9f,
        useSerifFont = false,
        useWideLetterSpacing = true,
        recordButtonEmoji = "🤖",
        scoreEmojis = mapOf(
            90 to "👑",
            80 to "🤖",
            70 to "⚡",
            60 to "🔥",
            0 to "💻"
        ),

        // M3 Overrides
        cardAlpha = 0f,
        shadowElevation = 18f,

        // Interaction
        dialogCopy = DialogCopy(
            deleteTitle = { "ERASE_DATA_FRAGMENT?" },
            deleteMessage = { type, name -> "Confirm deletion of '$name'. Data recovery will be impossible." },
            deleteConfirmButton = "[CONFIRM_DELETION]",
            deleteCancelButton = "[ABORT]",
            shareTitle = "UPLINK_ESTABLISHED",
            shareMessage = "Select network protocol for transmission:",
            renameTitle = { "MODIFY_METADATA" },
            renameHint = "Enter_New_ID"
        ),
        scoreFeedback = ScoreFeedback(
            getTitle = { score ->
                when {
                    score >= 90 -> "SYSTEM_HACKED!"
                    score >= 80 -> "ACCESS_GRANTED"
                    score >= 70 -> "FIREWALL_BYPASSED"
                    score >= 60 -> "CONNECTION_UNSTABLE"
                    else -> "ACCESS_DENIED"
                }
            },
            getMessage = { score ->
                when {
                    score >= 90 -> "Root access obtained. Flawless execution."
                    score >= 80 -> "Security protocols neutralized."
                    score >= 70 -> "Data packet integrity acceptable."
                    score >= 60 -> "Signal noise detected. Optimize algorithm."
                    else -> "Critical failure. Reboot and retry."
                }
            },
            getEmoji = { score ->
                when {
                    score >= 90 -> "👑"
                    score >= 80 -> "🤖"
                    score >= 70 -> "⚡"
                    score >= 60 -> "⚠️"
                    else -> "🚫"
                }
            }
        ),
        menuColors = MenuColors.fromColors(
            primaryText = Color(0xFF00FFFF),
            secondaryText = Color(0xFF80FF80),
            border = Color(0xFF00FFFF),
            gradient = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF0A0A0A),
                    Color(0xFF1A0033),
                    Color(0xFF000A1A)
                )
            )
        )
    )
}

class CyberpunkThemeComponents : ThemeComponents {

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