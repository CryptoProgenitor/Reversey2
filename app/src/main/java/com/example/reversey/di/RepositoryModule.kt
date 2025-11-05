package com.example.reversey.di

import android.content.Context
import com.example.reversey.data.repositories.AttemptsRepository
import com.example.reversey.data.repositories.RecordingRepository
import com.example.reversey.data.repositories.SettingsDataStore
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
    fun provideRecordingRepository(@ApplicationContext context: Context): RecordingRepository {
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
}