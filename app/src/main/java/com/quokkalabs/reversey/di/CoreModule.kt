package com.quokkalabs.reversey.di

import android.content.Context
import com.quokkalabs.reversey.audio.processing.AudioProcessor
import com.quokkalabs.reversey.data.backup.BackupManager
import com.quokkalabs.reversey.data.repositories.AttemptsRepository
import com.quokkalabs.reversey.data.repositories.RecordingNamesRepository
import com.quokkalabs.reversey.data.repositories.RecordingRepository
import com.quokkalabs.reversey.data.repositories.SettingsDataStore
import com.quokkalabs.reversey.data.repositories.ThreadSafeJsonRepository
import com.quokkalabs.reversey.scoring.ScoreAcquisitionDataConcentrator
import com.quokkalabs.reversey.scoring.SingingScoringEngine
import com.quokkalabs.reversey.scoring.SpeechScoringEngine
import com.quokkalabs.reversey.scoring.VocalModeDetector
import com.quokkalabs.reversey.scoring.VocalModeRouter
import com.quokkalabs.reversey.scoring.VocalScoringOrchestrator
import com.quokkalabs.reversey.testing.BackupIntegrationTest
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

    // 🎤 PHASE 3: Removed SpeechRecognitionService - live transcription handled upstream
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

    // ============================================================
    //  BACKUP SYSTEM PROVIDERS
    // ============================================================

    @Provides
    @Singleton
    fun provideThreadSafeJsonRepository(
        @ApplicationContext context: Context
    ): ThreadSafeJsonRepository = ThreadSafeJsonRepository(context)

    @Provides
    @Singleton
    fun provideBackupManager(
        @ApplicationContext context: Context,
        recordingRepository: RecordingRepository,
        attemptsRepository: AttemptsRepository,
        recordingNamesRepository: RecordingNamesRepository,
        threadSafeJsonRepo: ThreadSafeJsonRepository
    ): BackupManager {
        return BackupManager(
            context,
            recordingRepository,
            attemptsRepository,
            recordingNamesRepository,
            threadSafeJsonRepo
        )
    }

    @Provides
    @Singleton
    fun provideBackupIntegrationTest(
        @ApplicationContext context: Context,
        backupManager: BackupManager,
        recordingRepository: RecordingRepository,
        attemptsRepository: AttemptsRepository,
        recordingNamesRepository: RecordingNamesRepository,
        threadSafeJsonRepo: ThreadSafeJsonRepository
    ): BackupIntegrationTest {
        return BackupIntegrationTest(
            context,
            backupManager,
            recordingRepository,
            attemptsRepository,
            recordingNamesRepository,
            threadSafeJsonRepo
        )
    }
}