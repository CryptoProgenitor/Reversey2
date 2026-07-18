package com.quokkalabs.thehunt.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quokkalabs.thehunt.R
import com.quokkalabs.thehunt.ui.components.PulsingButton
import com.quokkalabs.thehunt.ui.components.ResetDialog
import com.quokkalabs.thehunt.ui.components.longHold
import com.quokkalabs.thehunt.ui.effects.CandleGlow
import com.quokkalabs.thehunt.ui.effects.FogVignette
import com.quokkalabs.thehunt.ui.theme.Bone

@Composable
fun WelcomeScreen(onBegin: () -> Unit, onResetConfirmed: () -> Unit) {
    var showReset by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        FogVignette(Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(contentAlignment = Alignment.Center) {
                CandleGlow(Modifier.size(320.dp))
                Text(
                    text = stringResource(R.string.title_display),
                    style = MaterialTheme.typography.displayLarge,
                    color = Bone,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.longHold { showReset = true },
                )
            }
            Spacer(Modifier.height(18.dp))
            Text(
                text = stringResource(R.string.welcome_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = Bone.copy(alpha = 0.72f),
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(72.dp))
            PulsingButton(
                text = stringResource(R.string.begin),
                onClick = onBegin,
                big = true,
            )
        }
    }

    if (showReset) {
        ResetDialog(
            title = stringResource(R.string.reset_title),
            body = stringResource(R.string.reset_body),
            confirm = stringResource(R.string.reset_confirm),
            cancel = stringResource(R.string.reset_cancel),
            onConfirm = {
                showReset = false
                onResetConfirmed()
            },
            onDismiss = { showReset = false },
        )
    }
}
