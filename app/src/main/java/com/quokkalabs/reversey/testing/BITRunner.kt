package com.quokkalabs.reversey.testing

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * BITRunner stub - test assets removed to reduce APK size.
 * Re-add test WAVs to res/raw/ to restore functionality.
 */
@Singleton
class BITRunner @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun runAllTests(onResult: (String) -> Unit) {
        onResult("BIT disabled - test assets not included")
    }

    fun runSingleTest(testName: String, onResult: (String) -> Unit) {
        onResult("BIT disabled - test assets not included")
    }
}