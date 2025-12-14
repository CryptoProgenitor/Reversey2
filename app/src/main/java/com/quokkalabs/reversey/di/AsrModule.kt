package com.quokkalabs.reversey.di

import android.content.Context
import com.quokkalabs.reversey.asr.VoskTranscriptionHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 🗣️ PHASE 3: Hilt module for ASR dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object AsrModule {


    // 🎤 VOSK: Provide VoskTranscriptionHelper
    @Provides
    @Singleton
    fun provideVoskTranscriptionHelper(
        @ApplicationContext context: Context,
    ): VoskTranscriptionHelper {
        return VoskTranscriptionHelper(context)
    }
}