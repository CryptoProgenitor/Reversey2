package com.quokkalabs.reversey.ui.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quokkalabs.reversey.R
import com.quokkalabs.reversey.data.models.ChallengeType
import com.quokkalabs.reversey.data.models.PlayerAttempt
import com.quokkalabs.reversey.data.models.Recording
import com.quokkalabs.reversey.ui.components.DifficultySquircle
import com.quokkalabs.reversey.ui.components.ScoreExplanationDialog
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * 🌸 PetalShape - Scalloped edges like cherry blossom petals
 * Creates soft, rounded indentations along each edge
 */
class PetalShape(
    private val petalDepth: Dp,
    private val petalsPerEdge: Int = 5,
    private val cornerRadius: Dp = 12.dp,
    private val seed: Int = 0,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val path = Path()
        val depthPx = with(density) { petalDepth.toPx() }
        val cornerPx = with(density) { cornerRadius.toPx() }
        val w = size.width
        val h = size.height

        // Slight randomness for organic feel
        val random = Random(seed)
        val variance = 0.15f

        // Start top-left after corner
        path.moveTo(cornerPx, 0f)

        // Top edge - petals bulging UP (negative Y)
        val topPetalWidth = (w - 2 * cornerPx) / petalsPerEdge
        for (i in 0 until petalsPerEdge) {
            val startX = cornerPx + i * topPetalWidth
            val midX = startX + topPetalWidth / 2
            val endX = startX + topPetalWidth
            val depth = depthPx * (1f + (random.nextFloat() - 0.5f) * variance)
            path.quadraticTo(midX, -depth, endX, 0f)
        }

        // Top-right corner
        path.quadraticTo(w, 0f, w, cornerPx)

        // Right edge - petals bulging RIGHT (beyond w)
        val rightPetalHeight = (h - 2 * cornerPx) / petalsPerEdge
        for (i in 0 until petalsPerEdge) {
            val startY = cornerPx + i * rightPetalHeight
            val midY = startY + rightPetalHeight / 2
            val endY = startY + rightPetalHeight
            val depth = depthPx * (1f + (random.nextFloat() - 0.5f) * variance)
            path.quadraticTo(w + depth, midY, w, endY)
        }

        // Bottom-right corner
        path.quadraticTo(w, h, w - cornerPx, h)

        // Bottom edge - petals bulging DOWN (beyond h)
        val bottomPetalWidth = (w - 2 * cornerPx) / petalsPerEdge
        for (i in 0 until petalsPerEdge) {
            val startX = w - cornerPx - i * bottomPetalWidth
            val midX = startX - bottomPetalWidth / 2
            val endX = startX - bottomPetalWidth
            val depth = depthPx * (1f + (random.nextFloat() - 0.5f) * variance)
            path.quadraticTo(midX, h + depth, endX, h)
        }

        // Bottom-left corner
        path.quadraticTo(0f, h, 0f, h - cornerPx)

        // Left edge - petals bulging LEFT (negative X)
        val leftPetalHeight = (h - 2 * cornerPx) / petalsPerEdge
        for (i in 0 until petalsPerEdge) {
            val startY = h - cornerPx - i * leftPetalHeight
            val midY = startY - leftPetalHeight / 2
            val endY = startY - leftPetalHeight
            val depth = depthPx * (1f + (random.nextFloat() - 0.5f) * variance)
            path.quadraticTo(-depth, midY, 0f, endY)
        }

        // Top-left corner (close)
        path.quadraticTo(0f, 0f, cornerPx, 0f)
        path.close()

        return Outline.Generic(path)
    }
}

/** Helper to create PetalShape with remembered instance */
@Composable
fun rememberPetalShape(
    petalDepth: Dp = 4.dp,
    petalsPerEdge: Int = 5,
    cornerRadius: Dp = 12.dp,
    seed: Int = 0,
): PetalShape {
    return remember(petalDepth, petalsPerEdge, cornerRadius, seed) {
        PetalShape(petalDepth, petalsPerEdge, cornerRadius, seed)
    }
}

/** Variegated sakura border brush */
@Composable
fun sakuraBorderBrush(): Brush {
    return Brush.sweepGradient(
        colors = listOf(
            Color(0xFFFFB7C5),  // Light sakura
            Color(0xFFFF8FA3),  // Medium pink
            Color(0xFFFFB7C5),  // Light sakura
            Color(0xFFE75480),  // Dark sakura
            Color(0xFFFFB7C5),  // Light sakura
            Color(0xFFFF8FA3),  // Medium pink
            Color(0xFFFFB7C5),  // Light sakura
        )
    )
}

/**
 * 🌸 SAKURA SERENITY THEME
 * Cherry blossoms, bamboo, and sunset gradients.
 */
object SakuraSerenityTheme {
    const val THEME_ID = "sakura_serenity"

    val data = AestheticThemeData(
        id = THEME_ID,
        name = "Sakura Serenity",
        description = "🌸 Cherry blossoms and sunset gradients",
        components = SakuraSerenityComponents(),

        // Visuals
        primaryTextColor = Color.White,
        secondaryTextColor = Color(0xFFFFC0CB),
        primaryGradient = Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFFB6C1), // Light pink
                Color(0xFFFF69B4), // Hot pink
                Color(0xFFFFA07A), // Light coral sunset
                Color(0xFFFF7F50)  // Coral orange
            )
        ),
        accentColor = Color(0xFFFF69B4),
        useGlassmorphism = false,
        glowIntensity = 0.3f,
        recordButtonEmoji = "🌸",
        scoreEmojis = mapOf(
            90 to "⭐",
            80 to "🌸",
            70 to "🌙",
            60 to "✨",
            0 to "🌱"
        ),

        // M3 Overrides
        cardAlpha = 0.95f,
        shadowElevation = 8f,
        useHandDrawnBorders = false,
        borderWidth = 2f,
        maxCardRotation = 0f,

        // Interaction
        dialogCopy = DialogCopy(
            deleteTitle = { type -> if (type == DeletableItemType.RECORDING) "Prune this branch?" else "Scatter petals?" },
            deleteMessage = { type, name -> "Shall we let '$name' fade away? It cannot bloom again." },
            deleteConfirmButton = "Prune",
            deleteCancelButton = "Preserve",
            shareTitle = "Share the Bloom 🌸",
            shareMessage = "Spread these petals to:",
            renameTitle = { "Name this Blossom" },
            renameHint = "New Name"
        ),
        scoreFeedback = ScoreFeedback(
            getTitle = { score ->
                when {
                    score >= 90 -> "Full Bloom!"
                    score >= 80 -> "Blossoming Beautifully"
                    score >= 70 -> "Budding Potential"
                    score >= 60 -> "Sprouting"
                    else -> "Planting Seeds"
                }
            },
            getMessage = { score ->
                when {
                    score >= 90 -> "A perfect spring day!"
                    score >= 80 -> "The garden is proud."
                    score >= 70 -> "Growing stronger every day."
                    score >= 60 -> "Needs a little more sunlight."
                    else -> "Patience is the key to growth."
                }
            },
            getEmoji = { score ->
                when {
                    score >= 90 -> "🌸"
                    score >= 80 -> "🌺"
                    score >= 70 -> "🌷"
                    score >= 60 -> "🌱"
                    else -> "🍂"
                }
            }
        ),
        menuColors = MenuColors.fromColors(
            primaryText = Color.White,
            secondaryText = Color(0xFFFFC0CB),
            border = Color(0xFFFF69B4),
            gradient = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFFF69B4),
                    Color(0xFFFFA07A)
                )
            )
        ),
        isPro = true
    )
}

class SakuraSerenityComponents : ThemeComponents {

    // 🌸 DEFINE THE CUSTOM FONT FAMILY
    private val electroHarmonixFont = FontFamily(Font(R.font.electroharmonix))

    @Composable
    override fun RecordingItem(
        recording: Recording,
        aesthetic: AestheticThemeData,
        isPaused: Boolean,
        progress: Float,
        currentlyPlayingPath: String?,
        onPlay: (String) -> Unit,
        onPause: () -> Unit,
        onStop: () -> Unit,
        onDelete: (Recording) -> Unit,
        onShare: (String) -> Unit,
        onRename: (String, String) -> Unit,
        isGameModeEnabled: Boolean,
        onStartAttempt: (Recording, ChallengeType) -> Unit,
        activeAttemptRecordingPath: String?,
        onStopAttempt: (() -> Unit)?,
    ) {
        // 🌸 THEME COLORS
        val bgPink = Color(0xFFFFF0F5) // Lavender Blush
        val borderPink = Color(0xFFFF69B4) // Hot Pink
        val textDarkPink = Color(0xFFC71585) // Medium Violet Red
        val buttonBg = Color(0xFFFFE4E1) // Misty Rose

        var showRenameDialog by remember { mutableStateOf(false) }
        var showDeleteDialog by remember { mutableStateOf(false) }
        var showShareDialog by remember { mutableStateOf(false) }

        // 🔧 POLYMORPHIC: Track which button owns the current playback
        val isPlayingForward = currentlyPlayingPath == recording.originalPath
        val isPlayingReversed = currentlyPlayingPath == recording.reversedPath

        // LAYOUT: Adapted from Guitar Theme
        // 🌸 Petal-edged card with variegated border
        val stableId = recording.originalPath.hashCode()
        val petalShape = rememberPetalShape(
            petalDepth = 8.dp,
            petalsPerEdge = 9,
            cornerRadius = 16.dp,
            seed = stableId
        )
        val borderBrush = sakuraBorderBrush()

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    // 🌸 Petal shape with variegated gradient border
                    .background(bgPink.copy(alpha = 0.75f), petalShape)
                    .border(3.dp, borderBrush, petalShape)
                    .padding(16.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                            .clickable { showRenameDialog = true }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "🌸 ${recording.name}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = electroHarmonixFont, // 🌸 CUSTOM FONT
                            color = textDarkPink,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Progress Bar (Sakura Style) - 🔧 POLYMORPHIC (only show when playing)
                if (isPlayingForward || isPlayingReversed) {
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = borderPink,
                        trackColor = Color(0xFFFFC0CB).copy(alpha = 0.3f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 🌸 JAPANESE BUTTON ROW
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Share (Fan)
                    SakuraControlButton(
                        color = buttonBg,
                        label = "Share",
                        textColor = textDarkPink,
                        onClick = { showShareDialog = true }) {
                        SakuraShareIcon(borderPink)
                    }

                    // Play OR Stop (when Reversed is playing)
                    if (isPlayingReversed) {
                        SakuraControlButton(
                            color = buttonBg,
                            label = "Stop",
                            textColor = textDarkPink,
                            onClick = onStop
                        ) {
                            SakuraStopIcon(textDarkPink)
                        }
                    } else {
                        SakuraControlButton(
                            color = Color(0xFFFFB6C1),
                            label = when {
                                isPlayingForward && !isPaused -> "Pause"
                                isPlayingForward && isPaused -> "Resume"
                                else -> "Play"
                            },
                            textColor = textDarkPink,
                            onClick = {
                                if (isPlayingForward) onPause()
                                else onPlay(recording.originalPath)
                            }
                        ) {
                            if (isPlayingForward && !isPaused) {
                                SakuraPauseIcon(textDarkPink)
                            } else {
                                SakuraPlayIcon(textDarkPink)
                            }
                        }
                    }

                    // Rev OR Stop (when Forward is playing)
                    if (isPlayingForward) {
                        SakuraControlButton(
                            color = buttonBg,
                            label = "Stop",
                            textColor = textDarkPink,
                            onClick = onStop
                        ) {
                            SakuraStopIcon(textDarkPink)
                        }
                    } else {
                        SakuraControlButton(
                            color = buttonBg,
                            label = when {
                                isPlayingReversed && !isPaused -> "Pause"
                                isPlayingReversed && isPaused -> "Resume"
                                else -> "Rev"
                            },
                            textColor = textDarkPink,
                            onClick = {
                                if (isPlayingReversed) onPause()
                                else recording.reversedPath?.let { onPlay(it) }
                            }
                        ) {
                            if (isPlayingReversed && !isPaused) SakuraPauseIcon(textDarkPink) else SakuraRewindIcon(
                                borderPink
                            )
                        }
                    }

                    // 🎯 POLYMORPHIC: Try → Stop when recording attempt
                    if (isGameModeEnabled && recording.reversedPath != null) {
                        val isAttemptingThis = activeAttemptRecordingPath == recording.originalPath

                        if (isAttemptingThis && onStopAttempt != null) {
                            // 🛑 STOP with Y-axis spin animation
                            val infiniteTransition =
                                rememberInfiniteTransition(label = "sakuraSpin")
                            val spin by infiniteTransition.animateFloat(
                                initialValue = 1f,
                                targetValue = -1f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(600, easing = LinearEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "spin"
                            )

                            SakuraControlButton(
                                color = buttonBg,
                                label = "Stop",
                                textColor = textDarkPink,
                                onClick = { onStopAttempt() }
                            ) {
                                Box(modifier = Modifier.graphicsLayer(scaleX = spin)) {
                                    SakuraStopIcon(textDarkPink)
                                }
                            }
                        } else {
                            SakuraControlButton(
                                color = buttonBg,
                                label = "Try",
                                textColor = textDarkPink,
                                onClick = { onStartAttempt(recording, ChallengeType.REVERSE) }) {
                                Icon(
                                    Icons.Default.ArrowUpward,
                                    "Try",
                                    tint = textDarkPink,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .rotate(180f)
                                )
                            }
                        }
                    }
                    // 🌸 STYLED DELETE BUTTON (Consistent with control row)
                    SakuraControlButton(
                        color = buttonBg,
                        label = "DEL",
                        textColor = textDarkPink,
                        onClick = { showDeleteDialog = true }
                    ) {
                        SakuraDeleteIcon(textDarkPink) // Torii Gate
                    }
                }
            }
        }

        if (showRenameDialog) RenameDialog(
            RenamableItemType.RECORDING,
            recording.name,
            aesthetic,
            { onRename(recording.originalPath, it) },
            { showRenameDialog = false })
        if (showDeleteDialog) DeleteDialog(
            DeletableItemType.RECORDING,
            recording,
            aesthetic,
            { onDelete(recording) },
            { showDeleteDialog = false })
        if (showShareDialog) ShareDialog(
            recording,
            null,
            aesthetic,
            onShare,
            { showShareDialog = false })
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
        onOverrideScore: ((Int) -> Unit)?,
        onResetScore: (() -> Unit)?,
    ) {
        // 🌸 THEME COLORS
        val cardBg = Color(0xFFFFFAFC)
        val borderPink = Color(0xFFFF69B4)
        val textDarkPink = Color(0xFFC71585)
        val buttonBg = Color(0xFFFFE4E1)

        // 🔧 POLYMORPHIC: Track which specific file is playing
        val isPlayingForward = currentlyPlayingPath == attempt.attemptFilePath
        val isPlayingReversed = currentlyPlayingPath == attempt.reversedAttemptFilePath
        val isPlayingThis = isPlayingForward || isPlayingReversed

        var showRenameDialog by remember { mutableStateOf(false) }
        var showDeleteDialog by remember { mutableStateOf(false) }
        var showShareDialog by remember { mutableStateOf(false) }
        var showScoreDialog by remember { mutableStateOf(false) }

        // 🔧 FIX: Use finalScore override if present
        val displayScore = (attempt.finalScore ?: attempt.score).toInt()
        val scoreEmoji = aesthetic.scoreEmojis.entries
            .sortedByDescending { it.key }
            .firstOrNull { displayScore >= it.key }?.value ?: "🌸"

        // 🌸 Petal-edged card with variegated border
        val stableId = attempt.attemptFilePath.hashCode()
        val petalShape = rememberPetalShape(
            petalDepth = 6.dp,
            petalsPerEdge = 8,
            cornerRadius = 14.dp,
            seed = stableId
        )
        val borderBrush = sakuraBorderBrush()

        // LAYOUT: Adapted from Guitar Theme (Split View with Squircle)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 34.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    // 🌸 Petal shape with variegated gradient border
                    .background(
                        color = cardBg.copy(alpha = 0.75f),
                        shape = petalShape
                    )
                    .border(width = 2.dp, brush = borderBrush, shape = petalShape)
                    .padding(12.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                        // LEFT COLUMN: Info & Controls
                        Column(modifier = Modifier.weight(1f)) {
                            Spacer(modifier = Modifier.height(8.dp))

                            // Name Row
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (onJumpToParent != null) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowUpward,
                                        contentDescription = "Jump",
                                        tint = borderPink,
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clickable { onJumpToParent() })
                                }
                                Box(
                                    modifier = Modifier
                                        .background(
                                            Color(0xFFFFC0CB).copy(alpha = 0.2f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { showRenameDialog = true }
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = attempt.playerName,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = electroHarmonixFont, // 🌸 CUSTOM FONT
                                        color = textDarkPink,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Controls Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (onShareAttempt != null) {
                                    SakuraControlButton(
                                        onClick = { showShareDialog = true },
                                        color = buttonBg,
                                        label = "Share",
                                        textColor = textDarkPink
                                    ) {
                                        SakuraShareIcon(borderPink)
                                    }
                                }
                                // Play OR Stop (when Reversed is playing)
                                if (isPlayingReversed) {
                                    SakuraControlButton(
                                        onClick = onStop,
                                        color = buttonBg,
                                        label = "Stop",
                                        textColor = textDarkPink
                                    ) {
                                        SakuraStopIcon(textDarkPink)
                                    }
                                } else {
                                    SakuraControlButton(
                                        onClick = {
                                            if (isPlayingForward) onPause() else onPlay(attempt.attemptFilePath)
                                        },
                                        color = Color(0xFFFFB6C1),
                                        label = when {
                                            isPlayingForward && !isPaused -> "Pause"
                                            isPlayingForward && isPaused -> "Resume"
                                            else -> "Play"
                                        },
                                        textColor = textDarkPink
                                    ) {
                                        if (isPlayingForward && !isPaused) SakuraPauseIcon(
                                            textDarkPink
                                        ) else SakuraPlayIcon(textDarkPink)
                                    }
                                }
                                // Rev OR Stop (when Forward is playing)
                                attempt.reversedAttemptFilePath?.let { reversedPath ->
                                    if (isPlayingForward) {
                                        SakuraControlButton(
                                            onClick = onStop,
                                            color = buttonBg,
                                            label = "Stop",
                                            textColor = textDarkPink
                                        ) {
                                            SakuraStopIcon(textDarkPink)
                                        }
                                    } else {
                                        SakuraControlButton(
                                            onClick = {
                                                if (isPlayingReversed) onPause() else onPlay(
                                                    reversedPath
                                                )
                                            },
                                            color = buttonBg,
                                            label = when {
                                                isPlayingReversed && !isPaused -> "Pause"
                                                isPlayingReversed && isPaused -> "Resume"
                                                else -> "Rev"
                                            },
                                            textColor = textDarkPink
                                        ) {
                                            if (isPlayingReversed && !isPaused) SakuraPauseIcon(
                                                textDarkPink
                                            ) else SakuraRewindIcon(borderPink)
                                        }
                                    }
                                }
                                if (onDeleteAttempt != null) {
                                    SakuraControlButton(
                                        onClick = { showDeleteDialog = true },
                                        color = buttonBg,
                                        label = "Del",
                                        textColor = textDarkPink
                                    ) {
                                        SakuraDeleteIcon(borderPink)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // RIGHT COLUMN: The Difficulty Squircle
                        DifficultySquircle(
                            score = displayScore,
                            difficulty = attempt.difficulty,
                            challengeType = attempt.challengeType,
                            emoji = scoreEmoji,
                            isOverridden = attempt.finalScore != null,
                            width = 85.dp,
                            height = 110.dp,
                            onClick = { showScoreDialog = true }
                        )
                    }

                    if (isPlayingThis) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = borderPink,
                            trackColor = Color(0xFFFFC0CB).copy(alpha = 0.3f)
                        )
                    }
                }
            }
        }

        if (showRenameDialog && onRenamePlayer != null) RenameDialog(
            RenamableItemType.PLAYER,
            attempt.playerName,
            aesthetic,
            { onRenamePlayer(attempt, it) },
            { showRenameDialog = false })
        if (showDeleteDialog && onDeleteAttempt != null) DeleteDialog(
            DeletableItemType.ATTEMPT,
            attempt,
            aesthetic,
            { onDeleteAttempt(attempt) },
            { showDeleteDialog = false })
        if (showShareDialog && onShareAttempt != null) ShareDialog(
            null,
            attempt,
            aesthetic,
            onShareAttempt,
            { showShareDialog = false })
        if (showScoreDialog) ScoreExplanationDialog(
            attempt,
            { showScoreDialog = false },
            onOverrideScore = onOverrideScore ?: { },
            onResetScore = onResetScore ?: { })
    }

    @Composable
    override fun RecordButton(
        isRecording: Boolean,
        isProcessing: Boolean,
        aesthetic: AestheticThemeData,
        onStartRecording: () -> Unit,
        onStopRecording: () -> Unit,

        ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clickable { if (isRecording) onStopRecording() else onStartRecording() },
            contentAlignment = Alignment.Center
        ) {
            ToriiRecordIcon(
                isRecording = isRecording,
                baseColor = Color(0xFFC71585),
                recordingColor = Color(0xFFFF69B4),
                iconSize = 64.dp
            )
        }
    }

    @Composable
    override fun AppBackground(aesthetic: AestheticThemeData, content: @Composable () -> Unit) {
        Box(modifier = Modifier.fillMaxSize()) {
            EnhancedSakuraGradient()
            EnhancedFallingSakuraPetals()
            SakuraSparkles()
            EnhancedCalmSakuraTrees()
            SakuraBloomEffects()
            content()
        }
    }

    // --- HELPER COMPONENTS ---

    @Composable
    fun SakuraControlButton(
        color: Color,
        label: String,
        textColor: Color,
        onClick: () -> Unit,
        icon: @Composable () -> Unit,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable(onClick = onClick)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(color, RoundedCornerShape(12.dp))
                    .border(2.dp, Color(0xFFFF69B4).copy(0.5f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }
            Spacer(modifier = Modifier.height(2.dp))
            // 🌸 Use Real Electroharmonix Font
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = electroHarmonixFont, // 🌸 CUSTOM FONT
                color = textColor,
                textAlign = TextAlign.Center
            )
        }
    }

    // --- JAPANESE ICONS (CANVAS) ---

    // 🌸 TORII RECORD BUTTON ICON
    @Composable
    fun ToriiRecordIcon(
        isRecording: Boolean,
        baseColor: Color = Color(0xFFC71585), // Dark Pink (Medium Violet Red) from your theme
        recordingColor: Color = Color(0xFFFF69B4), // Hot Pink
        iconSize: Dp = 48.dp,
    ) {
        val currentTintColor = if (isRecording) recordingColor else baseColor

        val infiniteTransition = rememberInfiniteTransition(label = "waveAnimation")
        val waveOffset by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "waveOffset"
        )

        Canvas(modifier = Modifier.size(iconSize)) {
            val w = size.width
            val h = size.height
            val stroke = 3.6.dp.toPx() * (iconSize / 48.dp)  // 20% thicker strokes

            // Calculate THINNER crossbeam dimensions (no text inside)
            val crossbeamInternalHeight =
                2.sp.toPx() * (iconSize / 48.dp)  // Reduced from 10sp to 6sp
            val crossbeamTotalHeight = crossbeamInternalHeight + (stroke * 2)

            // Position crossbeam
            val crossbeamCenterY = h * 0.5f
            val crossbeamTop = crossbeamCenterY - (crossbeamTotalHeight / 2f)
            val crossbeamBottom = crossbeamCenterY + (crossbeamTotalHeight / 2f)
            val crossbeamLeft = w * 0.015f
            val crossbeamRight = w * 0.985f

            // --- TORII GATE STRUCTURE ---
            // INVERTED CONCAVE Top Bar (Kasagi)
            val topPath = Path().apply {
                moveTo(w * 0.00f, crossbeamTop - (stroke * 2))
                quadraticTo(
                    w * 0.5f,
                    crossbeamTop + (stroke * 1),
                    w * 1.0f,
                    crossbeamTop - (stroke * 2)
                )
            }
            drawPath(
                topPath,
                currentTintColor,
                style = Stroke(stroke * 1.2f, cap = StrokeCap.Round)
            )

            // THINNER Crossbeam (Nuki)
            drawRect(
                color = currentTintColor,
                topLeft = Offset(crossbeamLeft, crossbeamTop),
                size = Size(crossbeamRight - crossbeamLeft, crossbeamBottom - crossbeamTop),
                style = Stroke(width = stroke)
            )

            // TALLER SLANTED PILLARS (Hashira)
            val leftPillarTop = Offset(w * 0.12f, crossbeamTop - (stroke * 1f))
            val leftPillarBottom =
                Offset(w * 0.02f, h * 1.2f)  // Shortened to make room for text below
            val rightPillarTop = Offset(w * 0.88f, crossbeamTop - (stroke * 1f))
            val rightPillarBottom =
                Offset(w * 0.98f, h * 1.2f)  // Shortened to make room for text below

            drawLine(
                currentTintColor,
                leftPillarTop,
                leftPillarBottom,
                stroke * 1.4f,
                StrokeCap.Round
            )
            drawLine(
                currentTintColor,
                rightPillarTop,
                rightPillarBottom,
                stroke * 1.4f,
                StrokeCap.Round
            )

            // --- HANGING GONG SYMBOL ---
            val gongCenterX = w * 0.5f
            val gongCenterY = crossbeamBottom + (h * 0.12f)  // Hanging below crossbeam
            val gongRadius = w * 0.12f

            // Suspension cord
            drawLine(
                color = currentTintColor,
                start = Offset(gongCenterX, crossbeamBottom),
                end = Offset(gongCenterX, gongCenterY - gongRadius),
                strokeWidth = stroke * 0.3f,
                cap = StrokeCap.Round
            )

            // Gong circle
            drawCircle(
                color = currentTintColor,
                radius = gongRadius,
                center = Offset(gongCenterX, gongCenterY),
                style = Stroke(width = stroke * 0.8f)
            )

            // Inner gong detail (smaller circle)
            drawCircle(
                color = currentTintColor,
                radius = gongRadius * 0.6f,
                center = Offset(gongCenterX, gongCenterY),
                style = Stroke(width = stroke * 0.4f)
            )

            // --- REC/STOP TEXT BELOW GATE ---
            val labelText = if (isRecording) "STOP" else "REC"
            val textSize = 12.sp.toPx() * (iconSize / 48.dp)

            drawIntoCanvas { canvas ->
                val paint = android.graphics.Paint().apply {
                    color = currentTintColor.toArgb()
                    this.textSize = textSize
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                }

                // Position text below the pillars
                val textY = h * 1.3f

                canvas.nativeCanvas.drawText(
                    labelText,
                    w * 0.5f,
                    textY,
                    paint
                )
            }

            // --- CONCENTRIC ARC SOUND WAVES ( ( ( [GATE] ) ) ) ---
            if (isRecording) {
                val waveColor = currentTintColor
                val waveStroke = 3.dp.toPx() * (iconSize / 48.dp)

                val centerY = crossbeamCenterY

                // Left side arcs ( ( ( - positioned LEFT of left pillar
                for (i in 0 until 3) {
                    val leftX = w * (-0.6f - i * 0.04f)  // NEW: -15%, -19%, -23%
                    val radius = 15.dp.toPx() * (1 + i * 0.5f) * (iconSize / 48.dp)

                    val animatedAlpha =
                        0.5f + 0.3f * sin((waveOffset + i * 0.5f) * 2 * PI).toFloat()

                    drawArc(
                        color = waveColor.copy(alpha = animatedAlpha),
                        startAngle = -60f,
                        sweepAngle = 120f,
                        useCenter = false,
                        topLeft = Offset(leftX - radius, centerY - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = waveStroke, cap = StrokeCap.Round)
                    )
                }

                // Right side arcs ) ) ) - positioned RIGHT of right pillar
                for (i in 0 until 3) {
                    val rightX = w * (1.6f + i * 0.04f)  // NEW: 115%, 119%, 123%
                    val radius = 15.dp.toPx() * (1 + i * 0.5f) * (iconSize / 48.dp)

                    val animatedAlpha =
                        0.5f + 0.3f * sin((waveOffset + i * 0.5f + 0.3f) * 2 * PI).toFloat()

                    drawArc(
                        color = waveColor.copy(alpha = animatedAlpha),
                        startAngle = 120f,
                        sweepAngle = 120f,
                        useCenter = false,
                        topLeft = Offset(rightX - radius, centerY - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = waveStroke, cap = StrokeCap.Round)
                    )
                }
            }
        }
    }

    // --- JAPANESE ICONS (CANVAS) ---

    // 🌸 PLAY: A spinning Sakura Flower
    @Composable
    fun SakuraPlayIcon(color: Color) {
        val infiniteTransition = rememberInfiniteTransition(label = "spin")
        val angle by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(animation = tween(8000, easing = LinearEasing)),
            label = "rotation"
        )

        Canvas(
            modifier = Modifier
                .size(24.dp)
                .rotate(angle)
        ) {
            val center = Offset(size.width / 2, size.height / 2)
            val petalRadius = size.width / 4

            // Draw 5 petals
            for (i in 0 until 5) {
                val theta = (i * 72) * (Math.PI / 180)
                val petalCenter = Offset(
                    center.x + (petalRadius * 0.6f) * cos(theta).toFloat(),
                    center.y + (petalRadius * 0.6f) * sin(theta).toFloat()
                )
                drawCircle(color = color, radius = petalRadius * 0.7f, center = petalCenter)
            }
            // Center dot
            drawCircle(color = Color.White, radius = petalRadius * 0.3f, center = center)
        }
    }

    // 🎋 PAUSE: Bamboo Stalks
    @Composable
    fun SakuraPauseIcon(color: Color) {
        Canvas(modifier = Modifier.size(24.dp)) {
            val w = size.width
            val h = size.height
            val stroke = 3.dp.toPx()

            // Left Stalk
            drawLine(
                color,
                Offset(w * 0.35f, h * 0.2f),
                Offset(w * 0.35f, h * 0.8f),
                stroke,
                StrokeCap.Round
            )
            // Right Stalk
            drawLine(
                color,
                Offset(w * 0.65f, h * 0.2f),
                Offset(w * 0.65f, h * 0.8f),
                stroke,
                StrokeCap.Round
            )

            // Bamboo Nodes (Horizontal lines)
            drawLine(color, Offset(w * 0.25f, h * 0.5f), Offset(w * 0.45f, h * 0.5f), 1.dp.toPx())
            drawLine(color, Offset(w * 0.55f, h * 0.5f), Offset(w * 0.75f, h * 0.5f), 1.dp.toPx())
        }
    }

    // ⭕ STOP: Ensō (Zen Circle of Enlightenment)
    @Composable
    fun SakuraStopIcon(color: Color) {
        Canvas(modifier = Modifier.size(24.dp)) {
            val w = size.width
            val h = size.height
            val stroke = 3.dp.toPx()
            val radius = w * 0.35f
            drawCircle(
                color = color,
                radius = radius,
                center = Offset(w * 0.5f, h * 0.5f),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
    }

    // 🪭 SHARE: Japanese Folding Fan (Sensu)
    @Composable
    fun SakuraShareIcon(color: Color) {
        Canvas(modifier = Modifier.size(24.dp)) {
            val w = size.width
            val h = size.height

            val fanPath = Path().apply {
                moveTo(w * 0.5f, h * 0.8f) // Pivot point
                lineTo(w * 0.1f, h * 0.3f) // Top Left
                quadraticTo(w * 0.5f, h * 0.1f, w * 0.9f, h * 0.3f) // Arc top
                lineTo(w * 0.5f, h * 0.8f) // Back to pivot
                close()
            }
            drawPath(fanPath, color = color, style = Stroke(width = 2.dp.toPx()))

            // Ribs of the fan
            drawLine(color, Offset(w * 0.5f, h * 0.8f), Offset(w * 0.5f, h * 0.2f), 1.dp.toPx())
            drawLine(color, Offset(w * 0.5f, h * 0.8f), Offset(w * 0.3f, h * 0.25f), 1.dp.toPx())
            drawLine(color, Offset(w * 0.5f, h * 0.8f), Offset(w * 0.7f, h * 0.25f), 1.dp.toPx())
        }
    }

    // ⛩️ DELETE: Torii Gate
    @Composable
    fun SakuraDeleteIcon(color: Color) {
        Canvas(modifier = Modifier.size(24.dp)) {
            val w = size.width
            val h = size.height
            val stroke = 2.5.dp.toPx()

            // Top Bar (Kasagi) - Curved
            val topPath = Path().apply {
                moveTo(w * 0.1f, h * 0.2f)
                quadraticTo(w * 0.5f, h * 0.1f, w * 0.9f, h * 0.2f)
            }
            drawPath(topPath, color, style = Stroke(stroke, cap = StrokeCap.Round))

            // Second Bar (Nuki)
            drawLine(color, Offset(w * 0.2f, h * 0.35f), Offset(w * 0.8f, h * 0.35f), stroke)

            // Pillars (Hashira)
            drawLine(color, Offset(w * 0.3f, h * 0.25f), Offset(w * 0.25f, h * 0.9f), stroke)
            drawLine(color, Offset(w * 0.7f, h * 0.25f), Offset(w * 0.75f, h * 0.9f), stroke)
        }
    }

    // 🍃 REWIND: Wind Gust
    @Composable
    fun SakuraRewindIcon(color: Color) {
        Canvas(modifier = Modifier.size(24.dp)) {
            val w = size.width
            val h = size.height
            val stroke = 2.dp.toPx()

            val windPath = Path().apply {
                moveTo(w * 0.8f, h * 0.3f)
                quadraticTo(w * 0.4f, h * 0.3f, w * 0.3f, h * 0.5f)
                quadraticTo(w * 0.2f, h * 0.7f, w * 0.5f, h * 0.7f)
            }
            drawPath(windPath, color, style = Stroke(stroke, cap = StrokeCap.Round))

            // Arrow head
            val arrowPath = Path().apply {
                moveTo(w * 0.4f, h * 0.4f)
                lineTo(w * 0.25f, h * 0.5f) // Point
                lineTo(w * 0.35f, h * 0.65f)
            }
            drawPath(arrowPath, color, style = Stroke(stroke, cap = StrokeCap.Round))
        }
    }

    // --- DIALOG INTERFACE IMPLEMENTATION ---

    @Composable
    override fun ScoreCard(
        attempt: PlayerAttempt,
        aesthetic: AestheticThemeData,
        onDismiss: () -> Unit,
        onOverrideScore: ((Int) -> Unit),
    ) {
        ScoreExplanationDialog(attempt, onDismiss, onOverrideScore = onOverrideScore)
    }

    @Composable
    override fun DeleteDialog(
        itemType: DeletableItemType,
        item: Any,
        aesthetic: AestheticThemeData,
        onConfirm: () -> Unit,
        onDismiss: () -> Unit,
    ) {
        val copy = aesthetic.dialogCopy
        val name =
            if (item is Recording) item.name else if (item is PlayerAttempt) item.playerName else "Item"

        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = Color(0xFFFFF0F5),
            title = {
                Text(
                    copy.deleteTitle(itemType),
                    color = Color(0xFFC71585),
                    fontWeight = FontWeight.Bold
                )
            },
            text = { Text(copy.deleteMessage(itemType, name), color = Color(0xFF8B008B)) },
            confirmButton = {
                Button(
                    onClick = { onConfirm(); onDismiss() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF1493))
                ) {
                    Text(copy.deleteConfirmButton)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(
                        copy.deleteCancelButton,
                        color = Color(0xFFC71585)
                    )
                }
            }
        )
    }

    @Composable
    override fun ShareDialog(
        recording: Recording?,
        attempt: PlayerAttempt?,
        aesthetic: AestheticThemeData,
        onShare: (String) -> Unit,
        onDismiss: () -> Unit,
    ) {
        val copy = aesthetic.dialogCopy
        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = Color(0xFFFFF0F5),
            title = {
                Text(
                    copy.shareTitle,
                    color = Color(0xFFC71585),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(copy.shareMessage, color = Color(0xFF8B008B))
                    Spacer(modifier = Modifier.height(16.dp))
                    val path = recording?.originalPath ?: attempt?.attemptFilePath ?: ""
                    Button(
                        onClick = { onShare(path); onDismiss() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF69B4))
                    ) {
                        Text("Share Blossom 🌸▶️")
                    }
                    val revPath = recording?.reversedPath ?: attempt?.reversedAttemptFilePath
                    if (revPath != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { onShare(revPath); onDismiss() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDB7093))
                        ) {
                            Text("Share Petals 🍃◀️")
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(
                        "Cancel",
                        color = Color(0xFFC71585)
                    )
                }
            }
        )
    }

    @Composable
    override fun RenameDialog(
        itemType: RenamableItemType,
        currentName: String,
        aesthetic: AestheticThemeData,
        onRename: (String) -> Unit,
        onDismiss: () -> Unit,
    ) {
        var name by remember { mutableStateOf(currentName) }
        val copy = aesthetic.dialogCopy
        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = Color(0xFFF0F5),
            title = {
                Text(
                    copy.renameTitle(itemType),
                    color = Color(0xFFC71585),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                OutlinedTextField(
                    value = name, onValueChange = { name = it }, singleLine = true,
                    label = { Text(copy.renameHint) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFF69B4),
                        focusedLabelColor = Color(0xFFFF69B4)
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = { onRename(name); onDismiss() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF69B4))
                ) {
                    Text("Bloom")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(
                        "Cancel",
                        color = Color(0xFFC71585)
                    )
                }
            }
        )
    }

    // 🌸 VISUAL HELPERS (Stars and Trees)
    @Composable
    private fun TwinklingStars() {
        val infiniteTransition = rememberInfiniteTransition(label = "starTwinkle")
        val twinkle1 by infiniteTransition.animateFloat(
            0.3f,
            1f,
            infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = "twinkle1"
        )
        val twinkle2 by infiniteTransition.animateFloat(
            0.5f,
            1f,
            infiniteRepeatable(tween(1500, 200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = "twinkle2"
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            val stars = listOf(
                Triple(
                    Offset(size.width * 0.15f, size.height * 0.08f),
                    12.dp.toPx(),
                    Color(0xFFFFD700) to twinkle1
                ),
                Triple(
                    Offset(size.width * 0.85f, size.height * 0.07f),
                    11.dp.toPx(),
                    Color(0xFFFFFFFF) to twinkle2
                ),
                Triple(
                    Offset(size.width * 0.50f, size.height * 0.20f),
                    10.dp.toPx(),
                    Color(0xFFFFFFFF) to twinkle1
                )
            )
            stars.forEach { (pos, baseSize, pair) ->
                val (color, alpha) = pair
                val size = baseSize * (0.7f + alpha * 0.3f)
                drawCircle(color.copy(alpha = alpha), size / 2, pos)
            }
        }
    }

    @Composable
    private fun CherryBlossomTrees() {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val trees = listOf(
                Triple(0.12f, 0.25f, 40.dp.toPx()),
                Triple(0.88f, 0.28f, 38.dp.toPx()),
                Triple(0.48f, 0.52f, 41.dp.toPx())
            )
            trees.forEach { (xPct, yPct, size) ->
                val x = this.size.width * xPct
                val y = this.size.height * yPct
                drawRect(
                    Color(0xFF8B4513),
                    topLeft = Offset(x - 8.dp.toPx(), y),
                    size = Size(16.dp.toPx(), 70.dp.toPx())
                )
                drawCircle(Color(0xFFFFB6C1), size, Offset(x, y - size * 0.3f))
                drawCircle(Color(0xFFFF69B4), size * 0.7f, Offset(x - size * 0.4f, y - size * 0.5f))
            }
        }
    }

    // 🌸 FALLING PETALS (Adapted from Owl Theme)
    private data class PetalData(
        var x: Float,
        var y: Float,
        val size: Float,
        val speed: Float,
        var angle: Float,
        val spinSpeed: Float,
        val color: Color,
    )

    @Composable
    private fun FallingSakuraPetals() {
        var petals by remember { mutableStateOf(listOf<PetalData>()) }

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val width = constraints.maxWidth.toFloat()
            val height = constraints.maxHeight.toFloat()
            val colors = listOf(Color(0xFFFFB7C5), Color(0xFFFFC0CB), Color(0xFFFF69B4))

            LaunchedEffect(width, height) {
                petals = List(35) {
                    PetalData(
                        x = Random.nextFloat() * width,
                        y = Random.nextFloat() * height,
                        size = Random.nextFloat() * 25f + 22f,
                        speed = Random.nextFloat() * 2f + 1f,
                        angle = Random.nextFloat() * 360f,
                        spinSpeed = Random.nextFloat() * 4f - 2f,
                        color = colors.random().copy(alpha = 0.8f)
                    )
                }
            }

            LaunchedEffect(Unit) {
                while (true) {
                    withFrameMillis {
                        petals = petals.map { petal ->
                            var newY = petal.y + petal.speed
                            var newX = petal.x + sin(newY * 0.01f) * 1.5f // Swaying motion
                            var newAngle = petal.angle + petal.spinSpeed

                            if (newY > height) {
                                newY = -petal.size
                                newX = Random.nextFloat() * width
                            }
                            petal.copy(x = newX, y = newY, angle = newAngle)
                        }
                    }
                }
            }

            Canvas(modifier = Modifier.fillMaxSize()) {
                petals.forEach { petal ->
                    rotate(degrees = petal.angle, pivot = Offset(petal.x, petal.y)) {
                        drawOval(
                            color = petal.color,
                            topLeft = Offset(petal.x, petal.y),
                            size = Size(petal.size, petal.size * 0.6f) // Oval shape for petal
                        )
                    }
                }
            }
        }
    }
}
// 🌸🌸🌸 ENHANCED VISUAL COMPONENTS 🌸🌸🌸

@Composable
private fun EnhancedSakuraGradient() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        // Multi-layer gradient for depth
        val gradientBrush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFFFE4E6), // Soft pink dawn
                Color(0xFFFFB6C1), // Light pink
                Color(0xFFFF69B4), // Hot pink
                Color(0xFFFFA07A), // Light coral sunset
                Color(0xFFFF7F50), // Coral orange
                Color(0xFFCD5C5C)  // Indian red dusk
            ),
            center = Offset(size.width * 0.3f, size.height * 0.2f),
            radius = size.width * 1.2f
        )

        drawRect(gradientBrush)

        // Subtle overlay gradient for atmosphere
        val atmosphereGradient = Brush.verticalGradient(
            colors = listOf(
                Color(0x22FFB6C1), // Light pink mist
                Color(0x00FFB6C1), // Transparent
                Color(0x11FF69B4)  // Subtle pink at bottom
            )
        )
        drawRect(atmosphereGradient)
    }
}

@Composable
private fun EnhancedFallingSakuraPetals() {
    var petals by remember { mutableStateOf(listOf<EnhancedPetalData>()) }
    val windPhase by rememberInfiniteTransition("wind").animateFloat(
        0f, 2 * PI.toFloat(),
        infiniteRepeatable(tween(8000, easing = LinearEasing)),
        label = "wind"
    )

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val width = constraints.maxWidth.toFloat()
        val height = constraints.maxHeight.toFloat()
        val colors = listOf(
            Color(0xFFFFB7C5),
            Color(0xFFFFC0CB),
            Color(0xFFFF69B4),
            Color(0xFFFFE4E6),
            Color(0xFFFFA07A)
        )

        LaunchedEffect(width, height) {
            petals = List(15) { // REDUCED FROM 45 TO 15 AS REQUESTED
                EnhancedPetalData(
                    x = Random.nextFloat() * width,
                    y = Random.nextFloat() * height,
                    size = Random.nextFloat() * 20f + 15f,
                    speed = Random.nextFloat() * 1.5f + 0.8f,
                    angle = Random.nextFloat() * 360f,
                    spinSpeed = Random.nextFloat() * 3f - 1.5f,
                    color = colors.random().copy(alpha = Random.nextFloat() * 0.4f + 0.6f),
                    swayAmplitude = Random.nextFloat() * 30f + 20f,
                    swaySpeed = Random.nextFloat() * 0.02f + 0.01f
                )
            }
        }

        LaunchedEffect(windPhase) {
            withFrameMillis {
                petals = petals.map { petal ->
                    var newY = petal.y + petal.speed
                    val windEffect = sin(windPhase + petal.y * 0.01f) * 2f
                    var newX =
                        petal.x + sin(newY * petal.swaySpeed) * petal.swayAmplitude * 0.1f + windEffect
                    var newAngle = petal.angle + petal.spinSpeed

                    if (newY > height + petal.size) {
                        newY = -petal.size
                        newX = Random.nextFloat() * width
                    }

                    if (newX > width + petal.size) newX = -petal.size
                    if (newX < -petal.size) newX = width + petal.size

                    petal.copy(x = newX, y = newY, angle = newAngle)
                }
            }
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            petals.forEach { petal ->
                rotate(degrees = petal.angle, pivot = Offset(petal.x, petal.y)) {
                    // Draw more realistic petal shape
                    val path = Path().apply {
                        moveTo(petal.x, petal.y)
                        quadraticTo(
                            petal.x + petal.size * 0.3f, petal.y - petal.size * 0.2f,
                            petal.x + petal.size * 0.5f, petal.y
                        )
                        quadraticTo(
                            petal.x + petal.size * 0.3f, petal.y + petal.size * 0.4f,
                            petal.x, petal.y + petal.size * 0.3f
                        )
                        quadraticTo(
                            petal.x - petal.size * 0.3f, petal.y + petal.size * 0.4f,
                            petal.x - petal.size * 0.5f, petal.y
                        )
                        quadraticTo(
                            petal.x - petal.size * 0.3f, petal.y - petal.size * 0.2f,
                            petal.x, petal.y
                        )
                        close()
                    }
                    drawPath(path, petal.color)
                }
            }
        }
    }
}

private data class EnhancedPetalData(
    var x: Float,
    var y: Float,
    val size: Float,
    val speed: Float,
    var angle: Float,
    val spinSpeed: Float,
    val color: Color,
    val swayAmplitude: Float,
    val swaySpeed: Float,
)

@Composable
private fun SakuraSparkles() {
    var sparkles by remember { mutableStateOf(listOf<SparkleData>()) }
    val shimmer by rememberInfiniteTransition("sparkle").animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "shimmer"
    )

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val width = constraints.maxWidth.toFloat()
        val height = constraints.maxHeight.toFloat()

        LaunchedEffect(width, height) {
            sparkles = List(15) { // REDUCED FROM 25 TO 15 AS REQUESTED
                SparkleData(
                    x = Random.nextFloat() * width,
                    y = Random.nextFloat() * height,
                    size = Random.nextFloat() * 4f + 2f,
                    alpha = Random.nextFloat() * 0.8f + 0.2f,
                    phase = Random.nextFloat() * 2 * PI.toFloat()
                )
            }
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            sparkles.forEach { sparkle ->
                val sparkleAlpha =
                    sparkle.alpha * (0.5f + 0.5f * sin(shimmer * 2 * PI + sparkle.phase).toFloat())

                // Draw sparkle as a star
                repeat(8) { i ->
                    val angle = i * PI / 4
                    val length = if (i % 2 == 0) sparkle.size else sparkle.size * 0.4f

                    drawLine(
                        color = Color(0xFFFFFFFF).copy(alpha = sparkleAlpha),
                        start = Offset(sparkle.x, sparkle.y),
                        end = Offset(
                            sparkle.x + cos(angle).toFloat() * length,
                            sparkle.y + sin(angle).toFloat() * length
                        ),
                        strokeWidth = 1.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}

private data class SparkleData(
    val x: Float,
    val y: Float,
    val size: Float,
    val alpha: Float,
    val phase: Float,
)

@Composable
private fun SakuraBloomEffects() {
    val bloomPulse by rememberInfiniteTransition("bloom").animateFloat(
        0.8f, 1.2f,
        infiniteRepeatable(tween(3000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bloom"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        // Soft glowing orbs around the scene - ENHANCED VISIBILITY BY 200% (0.1f -> 0.3f)
        val glowPositions = listOf(
            Offset(size.width * 0.2f, size.height * 0.3f),
            Offset(size.width * 0.8f, size.height * 0.6f),
            Offset(size.width * 0.5f, size.height * 0.8f)
        )

        glowPositions.forEach { position ->
            val radius = 80.dp.toPx() * bloomPulse

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFFB6C1).copy(alpha = 0.3f), // ENHANCED FROM 0.1f TO 0.3f AS REQUESTED
                        Color(0x00FFB6C1)
                    ),
                    radius = radius
                ),
                radius = radius,
                center = position
            )
        }
    }
}

@Composable
private fun EnhancedCalmSakuraTrees() {
    val calmPulse by rememberInfiniteTransition("tree_pulse").animateFloat(
        0.8f, 1.1f,
        infiniteRepeatable(tween(4000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "calm_pulse"
    )

    // FIXED: Generate random blob positions ONCE and store them
    val staticBlobPositions = remember {
        listOf(
            // Tree 0 blobs (8)
            Offset(-80f, -30f), Offset(-60f, -50f), Offset(-40f, -35f), Offset(-70f, -60f),
            Offset(-55f, -25f), Offset(-85f, -45f), Offset(-45f, -55f), Offset(-65f, -40f),
            // Tree 1 blobs (8)
            Offset(70f, -25f), Offset(85f, -45f), Offset(95f, -30f), Offset(75f, -55f),
            Offset(60f, -40f), Offset(90f, -35f), Offset(80f, -60f), Offset(65f, -50f),
            // Tree 2 blobs (8)
            Offset(-10f, -20f), Offset(15f, -40f), Offset(0f, -55f), Offset(-20f, -45f),
            Offset(25f, -30f), Offset(5f, -35f), Offset(-15f, -60f), Offset(10f, -50f),
            // Tree 3 blobs (8)
            Offset(40f, -15f), Offset(-25f, -60f), Offset(30f, -50f), Offset(45f, -40f),
            Offset(35f, -55f), Offset(50f, -30f), Offset(25f, -45f), Offset(55f, -35f)
        )
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val trees = listOf(
            // Left tree - prominent
            CalmTreeData(0.15f, 0.75f, 0.85f, true),
            // Right tree - medium
            CalmTreeData(0.85f, 0.70f, 0.75f, false),
            // Background trees
            CalmTreeData(0.45f, 0.80f, 0.6f, true),
            CalmTreeData(0.75f, 0.85f, 0.5f, false)
        )

        trees.forEachIndexed { treeIndex, tree ->
            drawCalmSakuraTree(tree, calmPulse, staticBlobPositions, treeIndex)
        }
    }
}

private data class CalmTreeData(
    val xPos: Float,
    val yPos: Float,
    val scale: Float,
    val leanRight: Boolean,
)

private data class CalmBranchData(
    val angle: Float,
    val length: Float,
    val thickness: Float,
)

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCalmSakuraTree(
    tree: CalmTreeData,
    pulse: Float,
    staticBlobPositions: List<Offset>,
    treeIndex: Int,
) {
    val baseX = size.width * tree.xPos
    val baseY = size.height * tree.yPos
    val scale = tree.scale

    // Tree trunk - using drunk Claude's realistic trunk design
    val trunkWidth = 25.dp.toPx() * scale
    val trunkHeight = 180.dp.toPx() * scale
    val leanOffset = if (tree.leanRight) 15.dp.toPx() * scale else -15.dp.toPx() * scale

    drawRoundRect(
        color = Color(0xFF8B4513), // Saddle brown
        topLeft = Offset(baseX - trunkWidth / 2, baseY - trunkHeight),
        size = Size(trunkWidth, trunkHeight),
        cornerRadius = CornerRadius(trunkWidth * 0.3f, trunkWidth * 0.3f)
    )

    // Trunk shadow/depth - drunk Claude's technique
    drawRoundRect(
        color = Color(0xFF654321), // Darker brown for shadow
        topLeft = Offset(baseX - trunkWidth / 2 + 4.dp.toPx(), baseY - trunkHeight),
        size = Size(trunkWidth * 0.3f, trunkHeight),
        cornerRadius = CornerRadius(trunkWidth * 0.3f, trunkWidth * 0.3f)
    )

    // Main branches - drunk Claude's realistic branching pattern
    val branches = listOf(
        CalmBranchData(-45f, 120.dp.toPx() * scale, 15.dp.toPx() * scale),
        CalmBranchData(45f, 100.dp.toPx() * scale, 12.dp.toPx() * scale),
        CalmBranchData(-20f, 80.dp.toPx() * scale, 10.dp.toPx() * scale),
        CalmBranchData(30f, 90.dp.toPx() * scale, 10.dp.toPx() * scale),
        CalmBranchData(0f, 70.dp.toPx() * scale, 8.dp.toPx() * scale),
        CalmBranchData(-60f, 60.dp.toPx() * scale, 6.dp.toPx() * scale),
        CalmBranchData(60f, 55.dp.toPx() * scale, 6.dp.toPx() * scale)
    )

    branches.forEach { branch ->
        val startX = baseX + leanOffset * 0.3f
        val startY = baseY - trunkHeight * 0.7f
        val endX = startX + cos((branch.angle - 90) * PI / 180).toFloat() * branch.length
        val endY = startY + sin((branch.angle - 90) * PI / 180).toFloat() * branch.length

        // Draw branch - drunk Claude's technique
        drawLine(
            color = Color(0xFF8B4513),
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = branch.thickness,
            cap = StrokeCap.Round
        )

        // 🌸 PETAL CLOUDS - performance optimized
        val blobCount = (branch.length / 50.dp.toPx()).toInt().coerceIn(2, 4)
        val random = Random(treeIndex * 100 + branch.angle.toInt())

        repeat(blobCount) { i ->
            val t = (i + 1).toFloat() / blobCount
            val blobX = startX + (endX - startX) * t
            val blobY = startY + (endY - startY) * t

            // Draw small cluster of big petals
            val clusterSize = random.nextInt(2, 4)
            repeat(clusterSize) { j ->
                val scatter = 30.dp.toPx() * scale
                val offsetX = (random.nextFloat() - 0.5f) * scatter * 2
                val offsetY = (random.nextFloat() - 0.5f) * scatter * 2
                val petalScale = 0.9f + random.nextFloat() * 0.6f // Big: 0.9-1.5

                drawCherryBlossomEmoji(
                    center = Offset(blobX + offsetX, blobY + offsetY),
                    scale = scale * pulse * petalScale,
                    baseSize = 40.dp.toPx(), // Double size
                    colorPulse = pulse
                )
            }
        }
    }
    // 🌸 CANOPY CLUSTERS - performance optimized
    val blobsPerTree = 3
    val startIdx = treeIndex * blobsPerTree
    val canopyRandom = Random(treeIndex * 999)

    repeat(blobsPerTree) { i ->
        val blobIdx = startIdx + i
        if (blobIdx < staticBlobPositions.size) {
            val staticOffset = staticBlobPositions[blobIdx]
            val centerX = baseX + staticOffset.x * scale
            val centerY = baseY - trunkHeight * 0.6f + staticOffset.y * scale

            // Draw small cluster of big petals
            val clusterSize = canopyRandom.nextInt(1, 3)
            repeat(clusterSize) { j ->
                val scatter = 25.dp.toPx() * scale
                val offsetX = (canopyRandom.nextFloat() - 0.5f) * scatter * 2
                val offsetY = (canopyRandom.nextFloat() - 0.5f) * scatter * 2
                val petalScale = 0.8f + canopyRandom.nextFloat() * 0.8f // Big: 0.8-1.6

                drawCherryBlossomEmoji(
                    center = Offset(centerX + offsetX, centerY + offsetY),
                    scale = scale * pulse * petalScale,
                    baseSize = 35.dp.toPx(), // Double size
                    colorPulse = pulse
                )
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCherryBlossomEmoji(
    center: Offset,
    scale: Float,
    baseSize: Float,
    colorPulse: Float,
) {
    val emojiSize = baseSize * scale

    // SUBTLE GLOW EFFECT BEHIND EMOJI
    val glowRadius = emojiSize * 0.8f
    val glowAlpha = 0.3f + (colorPulse - 0.8f) * 0.7f // Pulse between 0.3f and 1.0f

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFFF69B4).copy(alpha = glowAlpha), // Hot pink center
                Color(0xFFFFB6C1).copy(alpha = glowAlpha * 0.7f), // Light pink
                Color(0x00FF69B4) // Transparent edge
            ),
            radius = glowRadius
        ),
        radius = glowRadius,
        center = center
    )

    // CHERRY BLOSSOM EMOJI WITH COLOR PULSE
    drawIntoCanvas { canvas ->
        val paint = android.graphics.Paint().apply {
            textSize = emojiSize
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER

            // PULSING COLOR EFFECT
            val colorIntensity = 0.7f + (colorPulse - 0.8f) * 1.0f // Pulse between 0.7f and 1.7f
            alpha = (255 * colorIntensity.coerceIn(0.5f, 1.0f)).toInt()

            // Subtle color tint that pulses
            colorFilter = android.graphics.ColorMatrixColorFilter(
                floatArrayOf(
                    colorIntensity,
                    0f,
                    0.2f,
                    0f,
                    20f, // Red channel with pink tint
                    0.1f,
                    colorIntensity,
                    0.3f,
                    0f,
                    10f, // Green channel
                    0.2f,
                    0.1f,
                    colorIntensity * 0.8f,
                    0f,
                    30f, // Blue channel (less blue for warmer tone)
                    0f,
                    0f,
                    0f,
                    1f,
                    0f // Alpha channel
                )
            )
        }

        // Draw the emoji
        canvas.nativeCanvas.drawText(
            "🌸",
            center.x,
            center.y + emojiSize * 0.35f, // Adjust vertical centering
            paint
        )
    }

    // ADDITIONAL SOFT SPARKLE EFFECT
    val sparkleAlpha = (colorPulse - 0.8f) * 2f // More intense pulsing for sparkles
    if (sparkleAlpha > 0f) {
        repeat(3) { i ->
            val sparkleAngle = i * 120f * (PI / 180f)
            val sparkleDistance = emojiSize * 0.6f
            val sparklePos = Offset(
                center.x + cos(sparkleAngle).toFloat() * sparkleDistance,
                center.y + sin(sparkleAngle).toFloat() * sparkleDistance
            )

            drawCircle(
                color = Color(0xFFFFFFFF).copy(alpha = sparkleAlpha.coerceIn(0f, 0.8f)),
                radius = 2.dp.toPx() * scale,
                center = sparklePos
            )
        }
    }
}
