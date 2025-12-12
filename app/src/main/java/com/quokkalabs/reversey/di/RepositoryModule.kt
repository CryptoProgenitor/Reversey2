package com.quokkalabs.reversey.di

import android.content.Context
import com.quokkalabs.reversey.data.repositories.AttemptsRepository
import com.quokkalabs.reversey.data.repositories.RecordingNamesRepository
import com.quokkalabs.reversey.data.repositories.RecordingRepository
import com.quokkalabs.reversey.data.repositories.SettingsDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing repository and data store dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    // 🎤 PHASE 3: Removed SpeechRecognitionService - live transcription handled by AudioRecorderHelper
    // 🔄 REFACTOR: Removed VocalModeDetector - dual pipeline eliminated (Dec 2025)
    @Provides
    @Singleton
    fun provideRecordingRepository(
        @ApplicationContext context: Context
    ): RecordingRepository {
        return RecordingRepository(context)
    }

    @Provides
    @Singleton
    fun provideAttemptsRepository(@ApplicationContext context: Context): AttemptsRepository {
        return AttemptsRepository(context)
    }

    @Provides
    @Singleton
    fun provideSettingsDataStore(@ApplicationContext context: Context): SettingsDataStore {
        return SettingsDataStore(context)
    }

    @Provides
    @Singleton
    fun provideRecordingNamesRepository(@ApplicationContext context: Context): RecordingNamesRepository {
        return RecordingNamesRepository(context)
    }
}