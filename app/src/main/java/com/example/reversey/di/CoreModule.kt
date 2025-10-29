package com.example.reversey.di

import android.content.Context
import com.example.reversey.scoring.ScoringEngine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing core application dependencies
 * This ensures a single instance of ScoringEngine throughout the app
 */
@Module
@InstallIn(SingletonComponent::class)
object CoreModule {

    @Provides
    @Singleton
    fun provideScoringEngine(@ApplicationContext context: Context): ScoringEngine {
        return ScoringEngine(context)
    }
}