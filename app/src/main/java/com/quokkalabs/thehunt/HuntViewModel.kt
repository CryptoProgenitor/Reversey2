package com.quokkalabs.thehunt

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.quokkalabs.thehunt.data.HuntData
import com.quokkalabs.thehunt.data.HuntLoader
import com.quokkalabs.thehunt.data.ProgressStore
import com.quokkalabs.thehunt.logic.HuntEngine
import com.quokkalabs.thehunt.logic.ScanOutcome
import com.quokkalabs.thehunt.sound.SoundPlayer
import kotlin.random.Random

enum class Screen { WELCOME, MAIN, SCANNER, FINALE }

data class ClueReveal(val clueNumber: Int, val text: String)

class HuntViewModel(app: Application) : AndroidViewModel(app) {

    val hunt: HuntData = HuntLoader.load(app)
    private val store = ProgressStore(app)
    private val sounds = SoundPlayer(app)

    var completed by mutableIntStateOf(store.completed)
        private set

    var screen by mutableStateOf(initialScreen())
        private set

    var reveal by mutableStateOf<ClueReveal?>(null)
        private set

    var message by mutableStateOf<String?>(null)
        private set

    private var finaleJustReached = false

    private fun initialScreen(): Screen = when {
        store.completed >= HuntEngine.TOTAL_CODES -> Screen.FINALE
        store.begun -> Screen.MAIN
        else -> Screen.WELCOME
    }

    /** The clue she is currently working on (the one leading to code completed + 1). */
    val currentClueText: String
        get() = if (completed == 0) hunt.startClue
        else hunt.codes[completed - 1].nextClue.orEmpty()

    fun begin() {
        store.begun = true
        reveal = ClueReveal(1, hunt.startClue)
        screen = Screen.MAIN
    }

    fun openScanner() {
        message = null
        screen = Screen.SCANNER
    }

    fun closeScanner() {
        screen = Screen.MAIN
    }

    fun dismissReveal() {
        reveal = null
    }

    fun dismissMessage() {
        message = null
    }

    fun onScanned(payload: String?) {
        if (completed >= HuntEngine.TOTAL_CODES) return
        when (val outcome = HuntEngine.evaluate(payload, completed)) {
            is ScanOutcome.Success -> {
                completed = outcome.codeNumber
                store.completed = outcome.codeNumber
                if (outcome.isFinale) {
                    finaleJustReached = true
                    screen = Screen.FINALE
                } else {
                    sounds.snap()
                    reveal = ClueReveal(
                        clueNumber = outcome.codeNumber + 1,
                        text = hunt.codes[outcome.codeNumber - 1].nextClue.orEmpty(),
                    )
                    screen = Screen.MAIN
                }
            }
            ScanOutcome.AlreadyScanned -> {
                message = hunt.alreadyScannedMessage
                screen = Screen.MAIN
            }
            is ScanOutcome.OutOfOrder -> {
                val template = hunt.outOfOrderMessages[Random.nextInt(hunt.outOfOrderMessages.size)]
                message = template.format(outcome.neededClue)
                screen = Screen.MAIN
            }
            ScanOutcome.Unknown -> {
                message = hunt.unknownCodeMessage
                screen = Screen.MAIN
            }
        }
    }

    fun playFanfare() = sounds.fanfare()

    /** True exactly once, when the finale was just reached by a live scan (not a cold start). */
    fun consumeFinaleAutoplay(): Boolean {
        val value = finaleJustReached
        finaleJustReached = false
        return value
    }

    fun reset() {
        store.reset()
        completed = 0
        reveal = null
        message = null
        finaleJustReached = false
        screen = Screen.WELCOME
    }

    override fun onCleared() {
        sounds.release()
    }
}
