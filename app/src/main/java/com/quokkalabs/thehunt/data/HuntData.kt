package com.quokkalabs.thehunt.data

import android.content.Context
import org.json.JSONObject

data class HuntCode(val id: String, val nextClue: String?)

data class FinaleText(val heading: String, val body: String, val footer: String)

data class HuntData(
    val title: String,
    val startClue: String,
    val codes: List<HuntCode>,
    val finale: FinaleText,
    val outOfOrderMessages: List<String>,
    val alreadyScannedMessage: String,
    val unknownCodeMessage: String,
)

object HuntLoader {

    fun load(context: Context): HuntData =
        fromJson(context.assets.open("hunt.json").bufferedReader().use { it.readText() })

    // The "hideAt" field exists in the JSON for the printed sheet only; it is deliberately ignored.
    fun fromJson(text: String): HuntData {
        val root = JSONObject(text)
        val codesArray = root.getJSONArray("codes")
        val codes = (0 until codesArray.length()).map { i ->
            val obj = codesArray.getJSONObject(i)
            HuntCode(
                id = obj.getString("id"),
                nextClue = if (obj.isNull("nextClue")) null else obj.getString("nextClue"),
            )
        }
        val finale = root.getJSONObject("finale")
        val outOfOrder = root.getJSONArray("outOfOrderMessages")
        return HuntData(
            title = root.getString("title"),
            startClue = root.getString("startClue"),
            codes = codes,
            finale = FinaleText(
                heading = finale.getString("heading"),
                body = finale.getString("body"),
                footer = finale.getString("footer"),
            ),
            outOfOrderMessages = (0 until outOfOrder.length()).map { outOfOrder.getString(it) },
            alreadyScannedMessage = root.getString("alreadyScannedMessage"),
            unknownCodeMessage = root.getString("unknownCodeMessage"),
        )
    }
}
