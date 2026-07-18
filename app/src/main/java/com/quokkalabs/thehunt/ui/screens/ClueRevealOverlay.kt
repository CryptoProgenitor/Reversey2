package com.quokkalabs.thehunt.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quokkalabs.thehunt.ClueReveal
import com.quokkalabs.thehunt.R
import com.quokkalabs.thehunt.logic.HuntEngine
import com.quokkalabs.thehunt.ui.components.PulsingButton
import com.quokkalabs.thehunt.ui.theme.Bone
import com.quokkalabs.thehunt.ui.theme.Ink
import com.quokkalabs.thehunt.ui.theme.InkRaised
import com.quokkalabs.thehunt.ui.theme.Violet
import com.quokkalabs.thehunt.ui.theme.VioletDeep

/** The showpiece: the clue card flips into view like a tarot card being turned. */
@Composable
fun ClueRevealOverlay(reveal: ClueReveal, totalClues: Int, onDismiss: () -> Unit) {
    val rotation = remember { Animatable(-90f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(reveal) {
        rotation.snapTo(-90f)
        alpha.snapTo(0f)
        alpha.animateTo(1f, tween(150))
        rotation.animateTo(0f, tween(750, easing = FastOutSlowInEasing))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Ink.copy(alpha = 0.90f * alpha.value))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}, // consume taps behind the card
            ),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = InkRaised,
            border = BorderStroke(1.dp, VioletDeep),
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp)
                .graphicsLayer {
                    rotationY = rotation.value
                    cameraDistance = 18f * density
                },
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 26.dp, vertical = 34.dp),
            ) {
                Text(
                    text = "✦", // four-pointed star ornament
                    color = Violet,
                    fontSize = 22.sp,
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    text = stringResource(
                        R.string.clue_of,
                        HuntEngine.romanNumeral(reveal.clueNumber),
                        HuntEngine.romanNumeral(totalClues),
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    color = Bone.copy(alpha = 0.6f),
                )
                Spacer(Modifier.height(24.dp))
                Text(
                    text = reveal.text,
                    fontFamily = FontFamily.Serif,
                    fontStyle = FontStyle.Italic,
                    fontSize = 21.sp,
                    lineHeight = 36.sp,
                    color = Bone,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(34.dp))
                PulsingButton(
                    text = stringResource(R.string.onward),
                    onClick = onDismiss,
                )
            }
        }
    }
}
