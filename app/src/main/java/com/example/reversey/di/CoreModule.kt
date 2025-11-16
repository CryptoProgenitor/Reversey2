package com.example.reversey.di

import android.content.Context
import com.example.reversey.data.repositories.SettingsDataStore
import com.example.reversey.scoring.ScoringEngine
import com.example.reversey.scoring.VocalModeDetector
import com.example.reversey.audio.processing.AudioProcessor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.example.reversey.scoring.SpeechScoringEngine
import com.example.reversey.scoring.SingingScoringEngine
import com.example.reversey.scoring.ScoreAcquisitionDataConcentrator
import com.example.reversey.scoring.VocalModeRouter

/**
 * Hilt module providing core application dependencies
 * This ensures single instances of major components throughout the app
 */
@Module
@InstallIn(SingletonComponent::class)
object CoreModule {

    @Provides
    @Singleton
    fun provideScoringEngine(
        @ApplicationContext context: Context,
        settingsDataStore: SettingsDataStore
    ): ScoringEngine {
        return ScoringEngine(context, settingsDataStore)
    }

    @Provides
    @Singleton
    fun provideAudioProcessor(): AudioProcessor {
        return AudioProcessor()
    }

    @Provides
    @Singleton
    fun provideVocalModeDetector(audioProcessor: AudioProcessor): VocalModeDetector {
        return VocalModeDetector(audioProcessor)
    }


    // AFTER the existing provideVocalModeDetector() method
// ADD these 4 new provider methods:

    @Provides
    @Singleton
    fun provideSpeechScoringEngine(
        @ApplicationContext context: Context,
        settingsDataStore: SettingsDataStore
    ): SpeechScoringEngine {
        return SpeechScoringEngine(context, settingsDataStore)
    }

    @Provides
    @Singleton
    fun provideSingingScoringEngine(
        @ApplicationContext context: Context,
        settingsDataStore: SettingsDataStore
    ): SingingScoringEngine {
        return SingingScoringEngine(context, settingsDataStore)
    }

    @Provides
    @Singleton
    fun provideVocalModeRouter(): VocalModeRouter {
        return VocalModeRouter()
    }

    @Provides
    @Singleton
    fun provideScoreAcquisitionDataConcentrator(
        speechEngine: SpeechScoringEngine,
        singingEngine: SingingScoringEngine,
        vocalRouter: VocalModeRouter
    ): ScoreAcquisitionDataConcentrator {
        return ScoreAcquisitionDataConcentrator()
    }

}