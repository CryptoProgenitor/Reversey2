package com.quokkalabs.reversey.di

import android.content.Context
import com.quokkalabs.reversey.asr.SpeechRecognitionService
import com.quokkalabs.reversey.asr.TranscriptionManager
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

    @Provides
    @Singleton
    fun provideSpeechRecognitionService(
        @ApplicationContext context: Context
    ): SpeechRecognitionService {
        return SpeechRecognitionService(context)
    }

    @Provides
    @Singleton
    fun provideTranscriptionManager(
        @ApplicationContext context: Context,
        speechRecognitionService: SpeechRecognitionService
    ): TranscriptionManager {
        return TranscriptionManager(context, speechRecognitionService)
    }

    // 🎤 VOSK: Provide VoskTranscriptionHelper
    @Provides
    @Singleton
    fun provideVoskTranscriptionHelper(
        @ApplicationContext context: Context
    ): VoskTranscriptionHelper {
        return VoskTranscriptionHelper(context)
    }
}