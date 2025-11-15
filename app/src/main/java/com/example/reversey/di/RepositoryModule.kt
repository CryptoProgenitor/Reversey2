package com.example.reversey.di

import android.content.Context
import com.example.reversey.data.repositories.AttemptsRepository
import com.example.reversey.data.repositories.RecordingNamesRepository
import com.example.reversey.data.repositories.RecordingRepository
import com.example.reversey.data.repositories.SettingsDataStore
import com.example.reversey.scoring.VocalModeDetector
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

    @Provides
    @Singleton
    fun provideRecordingRepository(
        @ApplicationContext context: Context,
        vocalModeDetector: VocalModeDetector
    ): RecordingRepository {
        return RecordingRepository(context, vocalModeDetector)
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