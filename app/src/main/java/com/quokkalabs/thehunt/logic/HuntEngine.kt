package com.quokkalabs.thehunt.logic

/**
 * Pure sequence logic for the hunt. No Android dependencies so it is unit-testable.
 *
 * State is simply `completed`: the number of codes already scanned in order (0..10).
 */
sealed class ScanOutcome {
    /** Payload was exactly the next expected code. */
    data class Success(val codeNumber: Int, val isFinale: Boolean) : ScanOutcome()

    /** Payload was a hunt code she has already completed. */
    object AlreadyScanned : ScanOutcome()

    /** Payload was a valid hunt code but further ahead than the next expected one. */
    data class OutOfOrder(val neededClue: Int) : ScanOutcome()

    /** Not a hunt code at all (or a WOE number outside 1..10). */
    object Unknown : ScanOutcome()
}

object HuntEngine {
    const val TOTAL_CODES = 10

    private val CODE_PATTERN = Regex("""WOE-(\d{1,2})""")

    fun evaluate(payload: String?, completed: Int): ScanOutcome {
        val text = payload?.trim()?.uppercase() ?: return ScanOutcome.Unknown
        val match = CODE_PATTERN.matchEntire(text) ?: return ScanOutcome.Unknown
        val number = match.groupValues[1].toIntOrNull() ?: return ScanOutcome.Unknown
        if (number < 1 || number > TOTAL_CODES) return ScanOutcome.Unknown
        return when {
            number == completed + 1 -> ScanOutcome.Success(number, number == TOTAL_CODES)
            number <= completed -> ScanOutcome.AlreadyScanned
            else -> ScanOutcome.OutOfOrder(completed + 1)
        }
    }

    fun romanNumeral(n: Int): String {
        val numerals = listOf("I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X")
        return if (n in 1..numerals.size) numerals[n - 1] else n.toString()
    }
}
