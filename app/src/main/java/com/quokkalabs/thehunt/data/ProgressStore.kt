package com.quokkalabs.thehunt.data

import android.content.Context

/** SharedPreferences persistence: highest completed code index + whether "Begin" was pressed. */
class ProgressStore(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var completed: Int
        get() = prefs.getInt(KEY_COMPLETED, 0)
        set(value) = prefs.edit().putInt(KEY_COMPLETED, value).apply()

    var begun: Boolean
        get() = prefs.getBoolean(KEY_BEGUN, false)
        set(value) = prefs.edit().putBoolean(KEY_BEGUN, value).apply()

    fun reset() = prefs.edit().clear().apply()

    private companion object {
        const val PREFS_NAME = "hunt_progress"
        const val KEY_COMPLETED = "highest_completed_index"
        const val KEY_BEGUN = "begun"
    }
}
