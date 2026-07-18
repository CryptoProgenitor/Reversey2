package com.quokkalabs.thehunt.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.quokkalabs.thehunt.HuntViewModel
import com.quokkalabs.thehunt.R
import com.quokkalabs.thehunt.Screen
import com.quokkalabs.thehunt.logic.HuntEngine
import com.quokkalabs.thehunt.ui.components.MessageCard
import com.quokkalabs.thehunt.ui.effects.SparkleOverlay
import com.quokkalabs.thehunt.ui.screens.ClueRevealOverlay
import com.quokkalabs.thehunt.ui.screens.FinaleScreen
import com.quokkalabs.thehunt.ui.screens.MainScreen
import com.quokkalabs.thehunt.ui.screens.ScannerScreen
import com.quokkalabs.thehunt.ui.screens.WelcomeScreen
import com.quokkalabs.thehunt.ui.theme.HuntTheme
import com.quokkalabs.thehunt.ui.theme.Ink

@Composable
fun HuntApp(vm: HuntViewModel) {
    HuntTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Ink),
        ) {
            Crossfade(
                targetState = vm.screen,
                animationSpec = tween(500),
                label = "screens",
            ) { screen ->
                when (screen) {
                    Screen.WELCOME -> WelcomeScreen(onBegin = vm::begin, onResetConfirmed = vm::reset)
                    Screen.MAIN -> MainScreen(vm)
                    Screen.SCANNER -> ScannerScreen(onResult = vm::onScanned, onBack = vm::closeScanner)
                    Screen.FINALE -> FinaleScreen(vm)
                }
            }

            // ambient sparkles drift across every screen
            SparkleOverlay(Modifier.fillMaxSize())

            vm.reveal?.let { reveal ->
                ClueRevealOverlay(
                    reveal = reveal,
                    totalClues = HuntEngine.TOTAL_CODES,
                    onDismiss = vm::dismissReveal,
                )
            }

            vm.message?.let { text ->
                MessageCard(
                    text = text,
                    dismissLabel = stringResource(R.string.dismiss_message),
                    onDismiss = vm::dismissMessage,
                )
            }
        }
    }
}
