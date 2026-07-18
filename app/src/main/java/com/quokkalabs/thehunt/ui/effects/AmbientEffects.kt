package com.quokkalabs.thehunt.ui.effects

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.quokkalabs.thehunt.ui.theme.Bone
import com.quokkalabs.thehunt.ui.theme.Ink
import com.quokkalabs.thehunt.ui.theme.Violet
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.sin
import kotlin.random.Random

private class Sparkle(random: Random) {
    val x = random.nextFloat()
    val y = random.nextFloat()
    val driftX = (random.nextFloat() - 0.5f) * 0.25f
    val driftY = (random.nextFloat() - 0.5f) * 0.18f
    val phase = random.nextFloat()
    val twinkle = 6 + random.nextInt(14) // blinks per cycle
    val sizePx = 2.5f + random.nextFloat() * 4.5f
    val maxAlpha = 0.25f + random.nextFloat() * 0.55f
    val color = if (random.nextFloat() < 0.45f) Violet else Bone
}

private fun DrawScope.drawGlint(x: Float, y: Float, r: Float, alpha: Float, color: Color) {
    val c = color.copy(alpha = alpha)
    drawLine(c, Offset(x - r, y), Offset(x + r, y), strokeWidth = 1.4f)
    drawLine(c, Offset(x, y - r), Offset(x, y + r), strokeWidth = 1.4f)
    drawCircle(c.copy(alpha = alpha * 0.55f), radius = r * 0.3f, center = Offset(x, y))
}

/** Sparse, elegant drifting star-glints. Sits over any screen without consuming input. */
@Composable
fun SparkleOverlay(modifier: Modifier = Modifier, count: Int = 14) {
    val sparkles = remember { List(count) { Sparkle(Random(it * 7919 + 13)) } }
    val t by rememberInfiniteTransition(label = "sparkles")
        .animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(40000, easing = LinearEasing)),
            label = "sparkleTime",
        )
    Canvas(modifier) {
        val twoPi = 2f * PI.toFloat()
        sparkles.forEach { s ->
            val x = (((s.x + s.driftX * t) % 1f) + 1f) % 1f * size.width
            val y = (((s.y + s.driftY * t) % 1f) + 1f) % 1f * size.height
            val tw = sin((t * s.twinkle + s.phase) * twoPi) * 0.5f + 0.5f
            val alpha = tw * s.maxAlpha
            if (alpha > 0.02f) {
                drawGlint(x, y, s.sizePx * (0.6f + 0.4f * tw), alpha, s.color)
            }
        }
    }
}

/** Slow-drifting violet fog blobs plus a dark vignette. Welcome-screen backdrop. */
@Composable
fun FogVignette(modifier: Modifier = Modifier) {
    val t by rememberInfiniteTransition(label = "fog")
        .animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(60000, easing = LinearEasing)),
            label = "fogTime",
        )
    Canvas(modifier) {
        val twoPi = 2f * PI.toFloat()
        val blobs = listOf(
            Triple(0.30f, 0.35f, 0.0f),
            Triple(0.72f, 0.60f, 2.1f),
            Triple(0.45f, 0.85f, 4.2f),
        )
        blobs.forEach { (bx, by, phase) ->
            val cx = size.width * (bx + 0.12f * sin(twoPi * t + phase))
            val cy = size.height * (by + 0.08f * sin(twoPi * t * 2f + phase * 1.7f))
            val radius = size.minDimension * (0.45f + 0.08f * sin(twoPi * t * 3f + phase))
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Violet.copy(alpha = 0.07f), Color.Transparent),
                    center = Offset(cx, cy),
                    radius = max(radius, 1f),
                ),
                radius = radius,
                center = Offset(cx, cy),
            )
        }
        // vignette
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color.Transparent, Ink.copy(alpha = 0.85f)),
                center = center,
                radius = size.maxDimension * 0.72f,
            ),
        )
    }
}

/** Flickering candle-glow, violet flavoured, meant to sit behind the title. */
@Composable
fun CandleGlow(modifier: Modifier = Modifier) {
    val t by rememberInfiniteTransition(label = "candle")
        .animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(5000, easing = LinearEasing)),
            label = "candleTime",
        )
    Canvas(modifier) {
        val twoPi = 2f * PI.toFloat()
        // layered sines make an organic flicker
        val flicker = 0.72f +
            0.16f * sin(twoPi * t * 7f) +
            0.08f * sin(twoPi * t * 13f + 1.7f) +
            0.04f * sin(twoPi * t * 29f + 0.4f)
        val radius = size.minDimension * 0.5f * (0.85f + 0.1f * flicker)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Violet.copy(alpha = 0.30f * flicker),
                    Violet.copy(alpha = 0.10f * flicker),
                    Color.Transparent,
                ),
                center = center,
                radius = max(radius, 1f),
            ),
            radius = radius,
            center = center,
        )
    }
}
