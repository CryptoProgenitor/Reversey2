package com.quokkalabs.reversey.di

import android.content.Context
import com.quokkalabs.reversey.audio.processing.AudioProcessor
import com.quokkalabs.reversey.data.backup.BackupManager
import com.quokkalabs.reversey.data.repositories.AttemptsRepository
import com.quokkalabs.reversey.data.repositories.RecordingNamesRepository
import com.quokkalabs.reversey.data.repositories.RecordingRepository
import com.quokkalabs.reversey.data.repositories.ThreadSafeJsonRepository
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