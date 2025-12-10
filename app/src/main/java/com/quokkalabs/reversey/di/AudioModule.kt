package com.quokkalabs.reversey.di

import android.content.Context
import com.quokkalabs.reversey.audio.AudioPlayerHelper
import com.quokkalabs.reversey.audio.AudioRecorderHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AudioModule {

    @Provides
    @Singleton
    fun provideAudioPlayerHelper(): AudioPlayerHelper {
        return AudioPlayerHelper()
    }

    // 🎤 SIMPLIFIED: AudioRecorderHelper no longer needs LiveTranscriptionHelper
    // Vosk transcription is now handled in AudioViewModel after recording stops
    @Provides
    @Singleton
    fun provideAudioRecorderHelper(
        @ApplicationContext context: Context
    ): AudioRecorderHelper {
        return AudioRecorderHelper(context)
    }

    // 🎤 REMOVED: LiveTranscriptionHelper - no longer needed (replaced by Vosk)
}