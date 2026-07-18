package com.quokkalabs.thehunt.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HuntEngineTest {

    // --- correct codes ---

    @Test
    fun `first code succeeds from fresh start`() {
        val outcome = HuntEngine.evaluate("WOE-01", completed = 0)
        assertEquals(ScanOutcome.Success(1, isFinale = false), outcome)
    }

    @Test
    fun `full sequence succeeds in order`() {
        for (n in 0 until HuntEngine.TOTAL_CODES) {
            val payload = "WOE-%02d".format(n + 1)
            val outcome = HuntEngine.evaluate(payload, completed = n)
            assertTrue("expected Success for $payload at completed=$n", outcome is ScanOutcome.Success)
            assertEquals(n + 1, (outcome as ScanOutcome.Success).codeNumber)
        }
    }

    @Test
    fun `tenth code is the finale`() {
        val outcome = HuntEngine.evaluate("WOE-10", completed = 9)
        assertEquals(ScanOutcome.Success(10, isFinale = true), outcome)
    }

    @Test
    fun `codes before the tenth are not the finale`() {
        val outcome = HuntEngine.evaluate("WOE-09", completed = 8)
        assertEquals(ScanOutcome.Success(9, isFinale = false), outcome)
    }

    @Test
    fun `payload is trimmed and case-insensitive`() {
        assertEquals(ScanOutcome.Success(3, false), HuntEngine.evaluate("  woe-03 \n", completed = 2))
    }

    @Test
    fun `single-digit payload is accepted`() {
        assertEquals(ScanOutcome.Success(3, false), HuntEngine.evaluate("WOE-3", completed = 2))
    }

    // --- duplicates ---

    @Test
    fun `scanning the just-completed code again is AlreadyScanned`() {
        assertEquals(ScanOutcome.AlreadyScanned, HuntEngine.evaluate("WOE-04", completed = 4))
    }

    @Test
    fun `scanning a much older code is AlreadyScanned`() {
        assertEquals(ScanOutcome.AlreadyScanned, HuntEngine.evaluate("WOE-01", completed = 7))
    }

    // --- skipped ahead ---

    @Test
    fun `skipping ahead reports the needed clue number`() {
        val outcome = HuntEngine.evaluate("WOE-07", completed = 2)
        assertEquals(ScanOutcome.OutOfOrder(neededClue = 3), outcome)
    }

    @Test
    fun `skipping to the finale from the start is out of order`() {
        assertEquals(ScanOutcome.OutOfOrder(neededClue = 1), HuntEngine.evaluate("WOE-10", completed = 0))
    }

    // --- unknown ---

    @Test
    fun `arbitrary text is Unknown`() {
        assertEquals(ScanOutcome.Unknown, HuntEngine.evaluate("https://example.com/menu", completed = 0))
    }

    @Test
    fun `null payload is Unknown`() {
        assertEquals(ScanOutcome.Unknown, HuntEngine.evaluate(null, completed = 0))
    }

    @Test
    fun `empty payload is Unknown`() {
        assertEquals(ScanOutcome.Unknown, HuntEngine.evaluate("", completed = 0))
    }

    @Test
    fun `WOE-00 is Unknown`() {
        assertEquals(ScanOutcome.Unknown, HuntEngine.evaluate("WOE-00", completed = 0))
    }

    @Test
    fun `WOE-11 is Unknown`() {
        assertEquals(ScanOutcome.Unknown, HuntEngine.evaluate("WOE-11", completed = 0))
    }

    @Test
    fun `WOE with letters is Unknown`() {
        assertEquals(ScanOutcome.Unknown, HuntEngine.evaluate("WOE-AB", completed = 0))
    }

    @Test
    fun `WOE code embedded in longer text is Unknown`() {
        assertEquals(ScanOutcome.Unknown, HuntEngine.evaluate("prefix WOE-01 suffix", completed = 0))
    }

    // --- roman numerals ---

    @Test
    fun `roman numerals cover one to ten`() {
        val expected = listOf("I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X")
        assertEquals(expected, (1..10).map { HuntEngine.romanNumeral(it) })
    }

    // --- out-of-order message formatting (the %d contract from hunt.json) ---

    @Test
    fun `out of order message template formats with needed clue`() {
        val template = "How ambitious. That code comes later. Find clue %d first."
        assertEquals(
            "How ambitious. That code comes later. Find clue 4 first.",
            template.format(4),
        )
    }
}
