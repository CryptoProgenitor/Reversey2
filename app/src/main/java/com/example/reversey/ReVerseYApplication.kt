package com.example.reversey

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class required for Hilt dependency injection
 * This enables Hilt to provide dependencies throughout the app
 */
@HiltAndroidApp
class ReVerseYApplication : Application()