package com.quokkalabs.thehunt.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class HuntLoaderTest {

    @Test
    fun `parses structure and ignores hideAt`() {
        val json = """
            {
              "title": "The Hunt",
              "startClue": "line one\nline two",
              "codes": [
                { "id": "WOE-01", "hideAt": "secret place", "nextClue": "clue two" },
                { "id": "WOE-02", "hideAt": "another place", "nextClue": null }
              ],
              "finale": { "heading": "h", "body": "b", "footer": "f" },
              "outOfOrderMessages": ["seek %d"],
              "alreadyScannedMessage": "again?",
              "unknownCodeMessage": "what?"
            }
        """.trimIndent()

        val hunt = HuntLoader.fromJson(json)

        assertEquals("The Hunt", hunt.title)
        assertEquals("line one\nline two", hunt.startClue)
        assertEquals(2, hunt.codes.size)
        assertEquals("WOE-01", hunt.codes[0].id)
        assertEquals("clue two", hunt.codes[0].nextClue)
        assertNull(hunt.codes[1].nextClue)
        assertEquals("h", hunt.finale.heading)
        assertEquals(listOf("seek %d"), hunt.outOfOrderMessages)
        assertEquals("again?", hunt.alreadyScannedMessage)
        assertEquals("what?", hunt.unknownCodeMessage)
    }

    @Test
    fun `shipped asset has ten sequential codes and a null final clue`() {
        // Unit tests run with the module directory as the working directory.
        val asset = File("src/main/assets/hunt.json")
        assertTrue("hunt.json asset missing at ${asset.absolutePath}", asset.exists())

        val hunt = HuntLoader.fromJson(asset.readText())

        assertEquals(10, hunt.codes.size)
        hunt.codes.forEachIndexed { index, code ->
            assertEquals("WOE-%02d".format(index + 1), code.id)
        }
        // codes 1..9 carry the clue to the next spot; the tenth is the finale
        hunt.codes.dropLast(1).forEach { code ->
            assertTrue("code ${code.id} should have a nextClue", !code.nextClue.isNullOrBlank())
        }
        assertNull(hunt.codes.last().nextClue)
        assertEquals(3, hunt.outOfOrderMessages.size)
        hunt.outOfOrderMessages.forEach { assertTrue(it.contains("%d")) }
    }
}
