package com.quokkalabs.thehunt.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quokkalabs.thehunt.ui.theme.Bone
import com.quokkalabs.thehunt.ui.theme.Ink
import com.quokkalabs.thehunt.ui.theme.InkRaised
import com.quokkalabs.thehunt.ui.theme.Mist
import com.quokkalabs.thehunt.ui.theme.Typewriter
import com.quokkalabs.thehunt.ui.theme.Violet
import com.quokkalabs.thehunt.ui.theme.VioletDeep
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.min

/** Hold for [durationMillis] without releasing to trigger [onHeld]. Dad's hidden reset. */
fun Modifier.longHold(durationMillis: Long = 5000L, onHeld: () -> Unit): Modifier =
    pointerInput(Unit) {
        detectTapGestures(
            onPress = {
                val released = withTimeoutOrNull(durationMillis) { tryAwaitRelease() }
                if (released == null) onHeld()
            }
        )
    }

/** Faintly pulsing gothic button. */
@Composable
fun PulsingButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    big: Boolean = false,
) {
    val pulse by rememberInfiniteTransition(label = "buttonPulse")
        .animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                tween(2200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "pulse",
        )
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.scale(1f + 0.012f * pulse),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, Violet.copy(alpha = 0.45f + 0.45f * pulse)),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = VioletDeep.copy(alpha = 0.22f + 0.10f * pulse),
            contentColor = Bone,
        ),
    ) {
        Text(
            text = text,
            fontFamily = Typewriter,
            fontSize = if (big) 20.sp else 16.sp,
            letterSpacing = 4.sp,
            modifier = Modifier.padding(
                horizontal = if (big) 28.dp else 12.dp,
                vertical = if (big) 10.dp else 4.dp,
            ),
        )
    }
}

/** Dismissible deadpan message card over a dim scrim. */
@Composable
fun MessageCard(text: String, dismissLabel: String, onDismiss: () -> Unit) {
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Ink.copy(alpha = 0.72f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss,
            ),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedVisibility(
            visible = shown,
            enter = fadeIn(tween(350)) + slideInVertically(tween(350)) { it / 6 },
        ) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = InkRaised,
                border = BorderStroke(1.dp, Mist),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 36.dp),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp),
                ) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Bone,
                        textAlign = TextAlign.Center,
                    )
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = dismissLabel,
                            fontFamily = Typewriter,
                            color = Violet,
                            letterSpacing = 2.sp,
                        )
                    }
                }
            }
        }
    }
}

/** Themed confirm dialog for the hidden reset. */
@Composable
fun ResetDialog(title: String, body: String, confirm: String, cancel: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = InkRaised,
        titleContentColor = Bone,
        textContentColor = Bone.copy(alpha = 0.8f),
        title = { Text(title, style = MaterialTheme.typography.titleMedium) },
        text = { Text(body, style = MaterialTheme.typography.bodyMedium) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirm, fontFamily = Typewriter, color = Violet, letterSpacing = 2.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(cancel, fontFamily = Typewriter, color = Bone.copy(alpha = 0.7f), letterSpacing = 2.sp)
            }
        },
    )
}

/** Ten little coffins that fill and glow violet as codes are completed. */
@Composable
fun ProgressCoffins(completed: Int, total: Int, modifier: Modifier = Modifier) {
    val pulse by rememberInfiniteTransition(label = "coffinPulse")
        .animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                tween(1300, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "coffin",
        )
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(46.dp),
    ) {
        val slot = size.width / total
        val coffinW = min(slot * 0.62f, 30.dp.toPx())
        val coffinH = size.height * 0.82f
        val top = (size.height - coffinH) / 2f

        for (i in 0 until total) {
            val cx = slot * (i + 0.5f)
            val path = Path().apply {
                moveTo(cx - coffinW * 0.31f, top)
                lineTo(cx + coffinW * 0.31f, top)
                lineTo(cx + coffinW * 0.5f, top + coffinH * 0.30f)
                lineTo(cx + coffinW * 0.22f, top + coffinH)
                lineTo(cx - coffinW * 0.22f, top + coffinH)
                lineTo(cx - coffinW * 0.5f, top + coffinH * 0.30f)
                close()
            }
            when {
                i < completed -> {
                    // glow halo behind the filled coffin
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Violet.copy(alpha = 0.30f), Color.Transparent),
                            center = Offset(cx, top + coffinH / 2f),
                            radius = coffinH * 0.85f,
                        ),
                        radius = coffinH * 0.85f,
                        center = Offset(cx, top + coffinH / 2f),
                    )
                    drawPath(path, Violet)
                    drawPath(path, Bone.copy(alpha = 0.20f), style = Stroke(width = 1f))
                }
                i == completed -> {
                    drawPath(path, Violet.copy(alpha = 0.10f + 0.12f * pulse))
                    drawPath(
                        path,
                        Violet.copy(alpha = 0.35f + 0.55f * pulse),
                        style = Stroke(width = 1.5.dp.toPx()),
                    )
                }
                else -> {
                    drawPath(path, Mist.copy(alpha = 0.55f), style = Stroke(width = 1.dp.toPx()))
                }
            }
        }
    }
}
