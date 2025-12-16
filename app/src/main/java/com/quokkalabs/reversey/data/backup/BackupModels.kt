package com.quokkalabs.reversey.data.backup

import com.quokkalabs.reversey.data.models.ChallengeType
import com.quokkalabs.reversey.scoring.DebuggingData
import com.quokkalabs.reversey.scoring.DifficultyLevel
import com.quokkalabs.reversey.scoring.PerformanceInsights
import com.quokkalabs.reversey.scoring.VocalAnalysis
import com.quokkalabs.reversey.scoring.WordPhonemes
import java.io.File

/**
 * BACKUP MANIFEST v2.2 - Path-Agnostic, Cross-Device Compatible Format
 *
 * CRITICAL DESIGN PRINCIPLE:
 * - Keys are FILENAMES, not absolute paths
 * - Paths are reconstructed on import using device's local directories
 * - This survives cross-device restore (different user IDs, storage paths)
 *
 * CHANGES FROM v2.1:
 * - Added full scorecard data to AttemptMetadataBackup:
 *   - finalScore (player override)
 *   - attemptTranscription (ASR text)
 *   - targetPhonemes, attemptPhonemes, phonemeMatches (phoneme comparison)
 *   - targetWordPhonemes, attemptWordPhonemes (word-grouped for UI)
 *   - durationRatio, wordAccuracy
 * - Added WordPhonemesBackup data class
 *
 * CHANGES FROM v2.0:
 * - Added creationTimestampMs to RecordingBackupEntry (no more brittle filename parsing)
 * - Added fileHash to all entries (for collision detection)
 * - Added customNames map (backup display names)
 * - Added exportTimestampMs for backup versioning
 */

/**
 * Root manifest containing all backup data.
 *
 * STORED AS: manifest.json inside the backup zip
 *
 * PATH-AGNOSTIC DESIGN:
 * - recordings: List of metadata (no paths, just filenames)
 * - attempts: Map keyed by FILENAME (e.g., "song.wav" → list of attempts)
 * - customNames: Map of FILENAME → display name
 */
data class BackupManifestV2(
    val version: String = "2.2",

    /** When this backup was created (epoch milliseconds) */
    val exportTimestampMs: Long,

    /** App version that created this backup */
    val appVersionName: String,
    val appVersionCode: Int,

    /** Date range filter used during export (null = all time) */
    val dateRange: DateRange? = null,

    /** Quick stats for preview */
    val summary: BackupSummary,

    /** List of recordings in this backup */
    val recordings: List<RecordingBackupEntry>,

    /**
     * Attempts map - KEY IS FILENAME, NOT PATH
     * Example: "song.wav" → [attempt1, attempt2, attempt3]
     *
     * CRITICAL: This allows path remapping on import.
     */
    val attempts: Map<String, List<AttemptBackupEntry>>,

    /**
     * Custom display names map - KEY IS FILENAME, NOT PATH
     * Example: "song.wav" → "Birthday Song"
     *
     * NEW IN v2.1: Prevents loss of custom names on restore.
     */
    val customNames: Map<String, String> = emptyMap()
)

/**
 * Date range for selective backup.
 */
data class DateRange(
    val fromMs: Long,      // Start of range (epoch milliseconds)
    val toMs: Long,        // End of range (epoch milliseconds)
    val fromIso: String,   // ISO 8601: "2025-11-01T00:00:00Z"
    val toIso: String      // ISO 8601: "2025-11-30T23:59:59Z"
)

/**
 * Quick summary for import preview.
 */
data class BackupSummary(
    val recordingCount: Int,
    val attemptCount: Int,
    val totalAudioFileSizeBytes: Long,
    val oldestRecordingTimestampMs: Long?,
    val newestRecordingTimestampMs: Long?,
    val hasCustomNames: Boolean = false
)

/**
 * Recording entry in backup - PATH-AGNOSTIC.
 *
 * CRITICAL FIELDS:
 * - filename: JUST the filename (e.g., "song.wav"), NOT full path
 * - hash: xxHash64 for deduplication and collision detection
 * - creationTimestampMs: NEW IN v2.1 - explicit timestamp (no parsing needed)
 */
data class RecordingBackupEntry(
    /** JUST the filename - no paths! */
    val filename: String,              // e.g., "2025-11-23_13-00-00.wav"

    /** Reversed audio filename (if exists) */
    val reversedFilename: String?,     // e.g., "2025-11-23_13-00-00_reversed.wav"

    /**
     * xxHash64 of original file (for deduplication).
     * NEW IN v2.1: Pre-computed during export.
     */
    val hash: String,

    /**
     * When this recording was created (epoch milliseconds).
     * NEW IN v2.1: Explicit timestamp, no more parsing filenames!
     */
    val creationTimestampMs: Long,

    /** Last modified time of the file */
    val lastModified: Long,

    /** File size in bytes */
    val fileSizeBytes: Long,

    /** Vocal mode classification */
    val vocalMode: String?,            // "SPEECH", "SINGING", or "UNKNOWN"
    val vocalConfidence: Float?,
    val vocalFeatures: VocalFeaturesBackup?
)

/**
 * Attempt entry in backup - PATH-AGNOSTIC.
 *
 * CRITICAL:
 * - parentRecordingFilename: Links to parent via FILENAME (not path)
 * - attemptFilename: JUST the filename (not path)
 * - hash: For deduplication
 */
data class AttemptBackupEntry(
    /** Parent recording FILENAME (e.g., "song.wav") */
    val parentRecordingFilename: String,

    /** Attempt audio FILENAME */
    val attemptFilename: String,

    /** Reversed attempt FILENAME (if exists) */
    val reversedAttemptFilename: String?,

    /**
     * xxHash64 of attempt file (for deduplication).
     * NEW IN v2.1: Pre-computed during export.
     */
    val hash: String,

    /** All the attempt metadata */
    val metadata: AttemptMetadataBackup
)

/**
 * Vocal features backup - matches VocalFeatures from VocalModeDetector.kt
 */
data class VocalFeaturesBackup(
    val pitchStability: Float,
    val pitchContour: Float,
    val mfccSpread: Float,
    val voicedRatio: Float
)

/**
 * Vocal analysis backup - matches VocalAnalysis from VocalModeDetector.kt
 */
data class VocalAnalysisBackup(
    val mode: String,              // "SPEECH", "SINGING", or "UNKNOWN"
    val confidence: Float,
    val features: VocalFeaturesBackup
)


/**
 * Performance insights backup - matches PerformanceInsights from Scoreacquisitiondataconcentrator.kt
 */
data class PerformanceInsightsBackup(
    val feedback: List<String>
)

/**
 * Debugging data backup - matches DebuggingData from Scoreacquisitiondataconcentrator.kt
 */
data class DebuggingDataBackup(
    val debugInfo: String
)

/**
 * Word-grouped phonemes for scorecard UI visualization.
 * NEW IN v2.2: Enables phoneme chip display on restored attempts.
 */
data class WordPhonemesBackup(
    val word: String,
    val phonemes: List<String>
)

/**
 * Attempt metadata for backup - matches ALL fields from PlayerAttempt.kt
 *
 * v2.2 ADDITIONS (Scorecard fields):
 * - finalScore: Player-overridden score
 * - attemptTranscription: ASR transcription text
 * - targetPhonemes / attemptPhonemes / phonemeMatches: Phoneme comparison data
 * - targetWordPhonemes / attemptWordPhonemes: Word-grouped for UI chips
 * - durationRatio: Timing comparison
 * - wordAccuracy: Content match score
 */
data class AttemptMetadataBackup(
    val playerName: String,
    val score: Int,
    val pitchSimilarity: Float,
    val mfccSimilarity: Float,
    val rawScore: Float,
    val challengeType: String,          // "REVERSE"
    val difficulty: String,             // "EASY", "NORMAL", or "HARD"

    // Rich metadata
    val feedback: List<String>,
    val isGarbage: Boolean,
    val vocalAnalysis: VocalAnalysisBackup?,
    val performanceInsights: PerformanceInsightsBackup?,
    val debuggingData: DebuggingDataBackup?,

    // ═══════════════════════════════════════════════════════════════
    // v2.2: SCORECARD FIELDS - Full phoneme/transcription data
    // ═══════════════════════════════════════════════════════════════

    /** Player-overridden score (null = use algorithmic score) */
    val finalScore: Int? = null,

    /** ASR transcription of what player said */
    val attemptTranscription: String? = null,

    /** Flat list of target phonemes (for matching) */
    val targetPhonemes: List<String> = emptyList(),

    /** Flat list of attempt phonemes (for matching) */
    val attemptPhonemes: List<String> = emptyList(),

    /** Per-phoneme match results (true = match, false = miss) */
    val phonemeMatches: List<Boolean> = emptyList(),

    /** Word-grouped target phonemes for UI visualization */
    val targetWordPhonemes: List<WordPhonemesBackup> = emptyList(),

    /** Word-grouped attempt phonemes for UI visualization */
    val attemptWordPhonemes: List<WordPhonemesBackup> = emptyList(),

    /** Duration ratio (e.g., 1.1 = attempt was 10% longer than target) */
    val durationRatio: Float? = null,

    /** Word accuracy / content match (0.0-1.0) */
    val wordAccuracy: Float? = null
)

/**
 * Result of a backup export operation.
 */
data class BackupResult(
    val success: Boolean,
    val zipFile: File?,
    val recordingsExported: Int,
    val attemptsExported: Int,
    val totalSizeBytes: Long,
    val error: String? = null
)

/**
 * Result of a backup restore operation.
 */
data class RestoreResult(
    val success: Boolean,
    val recordingsImported: Int,
    val recordingsSkipped: Int,
    val attemptsImported: Int,
    val customNamesRestored: Int,
    val error: String? = null
)

/**
 * Conflict resolution strategy when importing duplicates.
 */
enum class ConflictStrategy {
    /**
     * Skip importing duplicate recordings, but MERGE their attempts.
     */
    SKIP_DUPLICATES,

    /**
     * Import duplicate with renamed filename.
     */
    KEEP_BOTH,

    /**
     * Only merge attempts, never import recordings.
     */
    MERGE_ATTEMPTS_ONLY
}

// ============================================================
//  PROGRESS TRACKING
// ============================================================

/**
 * Progress tracking for backup/restore operations.
 * Used with StateFlow for real-time UI updates.
 */
sealed class BackupProgress {
    /** No operation in progress */
    object Idle : BackupProgress()

    /** Operation in progress */
    data class InProgress(
        val phase: BackupPhase,
        val currentItem: Int,
        val totalItems: Int,
        val currentFileName: String,
        val message: String
    ) : BackupProgress() {
        val percentage: Int
            get() = if (totalItems > 0) ((currentItem * 100) / totalItems) else 0
    }

    /** Operation completed successfully */
    data class Complete(
        val message: String,
        val recordingsProcessed: Int,
        val attemptsProcessed: Int
    ) : BackupProgress()

    /** Operation failed */
    data class Error(val message: String) : BackupProgress()
}

/**
 * Phases of backup/restore operations.
 */
enum class BackupPhase {
    ANALYZING,              // Analyzing files to backup/restore
    HASHING,                // Computing file hashes
    EXPORTING_RECORDINGS,   // Writing recordings to zip
    EXPORTING_ATTEMPTS,     // Writing attempts to zip
    CREATING_MANIFEST,      // Creating manifest.json
    EXTRACTING,             // Extracting zip contents
    IMPORTING_RECORDINGS,   // Copying recordings to device
    IMPORTING_ATTEMPTS,     // Copying attempts to device
    MERGING_METADATA,       // Updating attempts.json
    COMPLETE,               // Operation finished
    FAILED                  // Operation failed
}

// ============================================================
//  CONVERSION EXTENSIONS
// ============================================================

/**
 * Convert ChallengeType enum to string for backup.
 */
fun ChallengeType.toBackupString(): String = this.name

/**
 * Convert DifficultyLevel enum to string for backup.
 */
fun DifficultyLevel.toBackupString(): String = this.name

/**
 * Convert ScoringEngineType enum to string for backup.
 */

/**
 * Convert VocalAnalysis to backup format.
 */
fun VocalAnalysis.toBackup(): VocalAnalysisBackup =
    VocalAnalysisBackup(
        mode = this.mode.name,
        confidence = this.confidence,
        features = VocalFeaturesBackup(
            pitchStability = this.features.pitchStability,
            pitchContour = this.features.pitchContour,
            mfccSpread = this.features.mfccSpread,
            voicedRatio = this.features.voicedRatio
        )
    )

/**
 * Convert PerformanceInsights to backup format.
 */
fun PerformanceInsights.toBackup(): PerformanceInsightsBackup =
    PerformanceInsightsBackup(
        feedback = this.feedback
    )

/**
 * Convert DebuggingData to backup format.
 */
fun DebuggingData.toBackup(): DebuggingDataBackup =
    DebuggingDataBackup(
        debugInfo = this.debugInfo
    )

/**
 * Convert WordPhonemes to backup format.
 * NEW IN v2.2: For scorecard phoneme visualization.
 */
fun WordPhonemes.toBackup(): WordPhonemesBackup =
    WordPhonemesBackup(
        word = this.word,
        phonemes = this.phonemes
    )

/**
 * Convert WordPhonemesBackup back to WordPhonemes.
 * NEW IN v2.2: For scorecard phoneme visualization restore.
 */
fun WordPhonemesBackup.toWordPhonemes(): WordPhonemes =
    WordPhonemes(
        word = this.word,
        phonemes = this.phonemes
    )

// ============================================================
//  WIZARD FRAMEWORK
// ============================================================

/**
 * Import analysis - categorizes what will happen on import.
 */
data class ImportAnalysis(
    val manifest: BackupManifestV2,
    val newRecordings: List<RecordingBackupEntry>,
    val duplicateRecordings: List<RecordingBackupEntry>,
    val conflictingRecordings: List<RecordingBackupEntry>,
    val newAttempts: List<AttemptBackupEntry>,
    val duplicateAttempts: List<AttemptBackupEntry>,
    val conflictingAttempts: List<AttemptBackupEntry>,
    val orphanedAttempts: List<AttemptBackupEntry>,
    val totalSizeBytes: Long,
    val dateRange: Pair<Long, Long>?
)

/**
 * Date range presets for filtering.
 */
enum class DatePreset(val displayName: String, val daysBack: Int) {
    LAST_7_DAYS("Last 7 Days", 7),
    LAST_30_DAYS("Last Month", 30),
    LAST_90_DAYS("Last 3 Months", 90),
    THIS_YEAR("This Year", 365),
    ALL_TIME("All Time", Int.MAX_VALUE)
}

/**
 * Item selection state for wizard checkboxes.
 */
data class SelectableItem<T>(
    val item: T,
    val isSelected: Boolean = true,
    val isConflict: Boolean = false,
    val statusMessage: String? = null
)