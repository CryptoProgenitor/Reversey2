package com.quokkalabs.thehunt.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quokkalabs.thehunt.HuntViewModel
import com.quokkalabs.thehunt.R
import com.quokkalabs.thehunt.logic.HuntEngine
import com.quokkalabs.thehunt.ui.components.ProgressCoffins
import com.quokkalabs.thehunt.ui.components.PulsingButton
import com.quokkalabs.thehunt.ui.components.ResetDialog
import com.quokkalabs.thehunt.ui.components.longHold
import com.quokkalabs.thehunt.ui.theme.Bone
import com.quokkalabs.thehunt.ui.theme.InkRaised
import com.quokkalabs.thehunt.ui.theme.Mist

@Composable
fun MainScreen(vm: HuntViewModel) {
    var showReset by remember { mutableStateOf(false) }
    val nextClueNumber = (vm.completed + 1).coerceAtMost(HuntEngine.TOTAL_CODES)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(28.dp))
        Text(
            text = stringResource(R.string.title_display),
            style = MaterialTheme.typography.headlineMedium,
            color = Bone,
            modifier = Modifier.longHold { showReset = true },
        )
        Spacer(Modifier.height(22.dp))

        ProgressCoffins(completed = vm.completed, total = HuntEngine.TOTAL_CODES)

        Spacer(Modifier.height(30.dp))
        Text(
            text = stringResource(
                R.string.clue_of,
                HuntEngine.romanNumeral(nextClueNumber),
                HuntEngine.romanNumeral(HuntEngine.TOTAL_CODES),
            ),
            style = MaterialTheme.typography.labelLarge,
            color = Bone.copy(alpha = 0.6f),
        )
        Spacer(Modifier.height(12.dp))

        // The current clue stays visible — she will refer back to it.
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = InkRaised,
            border = BorderStroke(1.dp, Mist),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = vm.currentClueText,
                fontFamily = FontFamily.Serif,
                fontStyle = FontStyle.Italic,
                fontSize = 20.sp,
                lineHeight = 34.sp,
                color = Bone,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 30.dp),
            )
        }

        Spacer(Modifier.height(48.dp))
        PulsingButton(
            text = stringResource(R.string.scan),
            onClick = vm::openScanner,
            big = true,
        )
        Spacer(Modifier.height(40.dp))
    }

    if (showReset) {
        ResetDialog(
            title = stringResource(R.string.reset_title),
            body = stringResource(R.string.reset_body),
            confirm = stringResource(R.string.reset_confirm),
            cancel = stringResource(R.string.reset_cancel),
            onConfirm = {
                showReset = false
                vm.reset()
            },
            onDismiss = { showReset = false },
        )
    }
}
