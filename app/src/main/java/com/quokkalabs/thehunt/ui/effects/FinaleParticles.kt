package com.quokkalabs.thehunt.ui.effects

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.runtime.withFrameNanos
import com.quokkalabs.thehunt.ui.theme.Bone
import com.quokkalabs.thehunt.ui.theme.Violet
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private class ConfettiPiece(random: Random) {
    val x = random.nextFloat()
    val startY = random.nextFloat()
    val fallSpeed = 0.035f + random.nextFloat() * 0.05f // screen-heights per second
    val sway = 0.015f + random.nextFloat() * 0.03f
    val swaySpeed = 0.4f + random.nextFloat() * 0.8f
    val phase = random.nextFloat() * 2f * PI.toFloat()
    val rotSpeed = (random.nextFloat() - 0.5f) * 220f
    val w = 6f + random.nextFloat() * 8f
    val h = 10f + random.nextFloat() * 8f
    val white = random.nextBoolean()
}

private class BurstStar(index: Int, random: Random) {
    val angle = (index / 60f) * 2f * PI.toFloat() + (random.nextFloat() - 0.5f) * 0.25f
    val speed = 0.25f + random.nextFloat() * 0.75f
    val size = 3f + random.nextFloat() * 6f
    val violet = random.nextFloat() < 0.6f
}

/**
 * Finale celebration: slow black & white confetti forever, plus a purple/white
 * sparkle burst each time [burstKey] increments.
 */
@Composable
fun FinaleParticles(burstKey: Int, modifier: Modifier = Modifier) {
    val confetti = remember { List(36) { ConfettiPiece(Random(it * 31 + 7)) } }
    val burstStars = remember { List(60) { BurstStar(it, Random(it * 17 + 3)) } }

    var timeSec by remember { mutableFloatStateOf(0f) }
    var burstStart by remember { mutableStateOf(-100f) }

    LaunchedEffect(Unit) {
        val start = withFrameNanos { it }
        while (true) {
            withFrameNanos { now -> timeSec = (now - start) / 1_000_000_000f }
        }
    }
    LaunchedEffect(burstKey) {
        if (burstKey > 0) burstStart = timeSec
    }

    val charcoal = Color(0xFF2A2A30)

    Canvas(modifier) {
        // Slow-falling black & white confetti; wraps a little above/below the screen
        confetti.forEach { p ->
            val progress = (p.startY + timeSec * p.fallSpeed) % 1.15f
            val y = (progress - 0.075f) * size.height
            val x = (p.x + p.sway * sin(timeSec * p.swaySpeed * 2f * PI.toFloat() + p.phase)) * size.width
            val rotation = timeSec * p.rotSpeed + p.phase * 57f
            val color = if (p.white) Bone.copy(alpha = 0.8f) else charcoal
            rotate(degrees = rotation, pivot = Offset(x, y)) {
                drawRect(color, topLeft = Offset(x - p.w / 2f, y - p.h / 2f), size = Size(p.w, p.h))
            }
        }

        // Sparkle burst
        val life = 2.6f
        val u = (timeSec - burstStart) / life
        if (u in 0f..1f) {
            val eased = 1f - (1f - u) * (1f - u) // ease-out
            val maxR = size.minDimension * 0.55f
            val cx = size.width / 2f
            val cy = size.height * 0.42f
            burstStars.forEach { star ->
                val r = eased * maxR * star.speed
                val x = cx + cos(star.angle) * r
                val y = cy + sin(star.angle) * r + eased * eased * 60f
                val alpha = (1f - u).coerceIn(0f, 1f)
                val color = if (star.violet) Violet else Bone
                drawStarGlint(x, y, star.size * (1f - 0.5f * u), alpha, color)
            }
        }
    }
}

private fun DrawScope.drawStarGlint(x: Float, y: Float, r: Float, alpha: Float, color: Color) {
    val c = color.copy(alpha = alpha)
    drawLine(c, Offset(x - r, y), Offset(x + r, y), strokeWidth = 1.6f)
    drawLine(c, Offset(x, y - r), Offset(x, y + r), strokeWidth = 1.6f)
    drawCircle(c.copy(alpha = alpha * 0.6f), radius = r * 0.32f, center = Offset(x, y))
}
