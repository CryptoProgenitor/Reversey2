package com.quokkalabs.reversey.di

import android.content.Context
import com.quokkalabs.reversey.audio.processing.AudioProcessor
import com.quokkalabs.reversey.data.repositories.SettingsDataStore
import com.quokkalabs.reversey.scoring.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoreModule {

    @Provides
    @Singleton
    fun provideAudioProcessor(): AudioProcessor = AudioProcessor()

    @Provides
    @Singleton
    fun provideVocalModeDetector(
        audioProcessor: AudioProcessor
    ): VocalModeDetector = VocalModeDetector(audioProcessor)

    @Provides
    @Singleton
    fun provideVocalModeRouter(): VocalModeRouter = VocalModeRouter()

    @Provides
    @Singleton
    fun provideSpeechScoringEngine(
        @ApplicationContext context: Context,
        settingsDataStore: SettingsDataStore
    ): SpeechScoringEngine = SpeechScoringEngine(context, settingsDataStore)

    @Provides
    @Singleton
    fun provideSingingScoringEngine(
        @ApplicationContext context: Context,
        settingsDataStore: SettingsDataStore
    ): SingingScoringEngine = SingingScoringEngine(context, settingsDataStore)

    @Provides
    @Singleton
    fun provideScoreAcquisitionDataConcentrator(): ScoreAcquisitionDataConcentrator =
        ScoreAcquisitionDataConcentrator()

    @Provides
    @Singleton
    fun provideVocalScoringOrchestrator(
        detector: VocalModeDetector,
        router: VocalModeRouter,
        speechEngine: SpeechScoringEngine,
        singingEngine: SingingScoringEngine
    ): VocalScoringOrchestrator {
        return VocalScoringOrchestrator(
            detector,
            router,
            speechEngine,
            singingEngine
        )
    }
}
