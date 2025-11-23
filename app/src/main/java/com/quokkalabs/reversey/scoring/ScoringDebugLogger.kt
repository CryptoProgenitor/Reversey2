package com.quokkalabs.reversey.scoring

import android.util.Log
import com.quokkalabs.reversey.BuildConfig

/**
 * Centralised debug logger for the dual-stream scoring pipeline.
 *
 * GOALS:
 *  - Prove which mode was detected (speech / singing)
 *  - Prove which engine was routed (SpeechScoringEngine / SingingScoringEngine)
 *  - Prove which engine actually ran
 *  - Prove the full Presets bundle for speech vs singing
 *  - Provide a human-readable "audit" summary for each preset load
 *  - Log garbage-detector thresholds + runtime metrics
 */
object ScoringDebugLogger {

    private const val TAG_DETECTOR = "VocalModeDetector"
    private const val TAG_ROUTER = "VocalModeRouter"
    private const val TAG_ORCH = "VocalScoringOrch"
    private const val TAG_SPEECH = "SpeechEngine"
    private const val TAG_SINGING = "SingingEngine"
    private const val TAG_GARBAGE = "GarbageDetector"
    private const val TAG_PRESET = "ScoringPreset"

    // region Correlation helpers

    /**
     * Use filename or whatever path we have as a short correlation ID.
     */
    fun formatRecordingId(sourcePath: String?): String {
        if (sourcePath.isNullOrBlank()) return "unknown"
        return sourcePath.substringAfterLast('/').substringAfterLast('\\')
    }

    // endregion

    // region Detector → Router → Orchestrator chain

    fun logDetectorDecision(recordingId: String, mode: VocalMode) {
        if (!BuildConfig.DEBUG) return
        Log.d(
            TAG_DETECTOR,
            "[DETECTOR] recording=$recordingId mode=$mode"
        )
    }

    fun logRouterSelection(
        recordingId: String,
        detectedMode: VocalMode,
        selectedEngine: ScoringEngineType
    ) {
        if (!BuildConfig.DEBUG) return
        Log.d(
            TAG_ROUTER,
            "[ROUTER] recording=$recordingId detectedMode=$detectedMode selectedEngine=$selectedEngine"
        )
    }

    fun logOrchestratorEngine(
        recordingId: String,
        engineType: ScoringEngineType
    ) {
        if (!BuildConfig.DEBUG) return
        Log.d(
            TAG_ORCH,
            "[ORCHESTRATOR] recording=$recordingId Using engine=$engineType"
        )
    }

    // endregion

    // region Engine entry

    fun logSpeechEngineEnter(recordingId: String) {
        if (!BuildConfig.DEBUG) return
        Log.d(
            TAG_SPEECH,
            "[SPEECH_ENGINE] recording=$recordingId ScoreAttempt() ENTER"
        )
    }

    fun logSingingEngineEnter(recordingId: String) {
        if (!BuildConfig.DEBUG) return
        Log.d(
            TAG_SINGING,
            "[SINGING_ENGINE] recording=$recordingId ScoreAttempt() ENTER"
        )
    }

    // endregion

    // region Preset logging (FULL RAW + HUMAN AUDIT)

    fun logSpeechPresetApplied(
        presetName: String,
        preset: Presets,
        scoring: ScoringParameters,
        content: ContentDetectionParameters,
        melodic: MelodicAnalysisParameters,
        musical: MusicalSimilarityParameters,
        audio: AudioProcessingParameters,
        scaling: ScoreScalingParameters,
        garbage: GarbageDetectionParameters
    ) {
        logPresetApplied(
            streamLabel = "SPEECH",
            presetName = presetName,
            preset = preset,
            scoring = scoring,
            content = content,
            melodic = melodic,
            musical = musical,
            audio = audio,
            scaling = scaling,
            garbage = garbage
        )
    }

    fun logSingingPresetApplied(
        presetName: String,
        preset: Presets,
        scoring: ScoringParameters,
        content: ContentDetectionParameters,
        melodic: MelodicAnalysisParameters,
        musical: MusicalSimilarityParameters,
        audio: AudioProcessingParameters,
        scaling: ScoreScalingParameters,
        garbage: GarbageDetectionParameters
    ) {
        logPresetApplied(
            streamLabel = "SINGING",
            presetName = presetName,
            preset = preset,
            scoring = scoring,
            content = content,
            melodic = melodic,
            musical = musical,
            audio = audio,
            scaling = scaling,
            garbage = garbage
        )
    }

    private fun logPresetApplied(
        streamLabel: String,
        presetName: String,
        preset: Presets,
        scoring: ScoringParameters,
        content: ContentDetectionParameters,
        melodic: MelodicAnalysisParameters,
        musical: MusicalSimilarityParameters,
        audio: AudioProcessingParameters,
        scaling: ScoreScalingParameters,
        garbage: GarbageDetectionParameters
    ) {
        if (!BuildConfig.DEBUG) return

        // -------- HUMAN-READABLE AUDIT SUMMARY --------
        val scoringMatch = (preset.scoring === scoring)
        val contentMatch = (preset.content === content)
        val melodicMatch = (preset.melodic === melodic)
        val musicalMatch = (preset.musical === musical)
        val audioMatch = (preset.audio === audio)
        val scalingMatch = (preset.scaling === scaling)
        val garbageMatch = (preset.garbage === garbage)

        Log.d(
            TAG_PRESET,
            "[PRESET_SUMMARY][$streamLabel] name=$presetName difficulty=${preset.difficulty.displayName}"
        )
        Log.d(
            TAG_PRESET,
            "  Ref integrity:" +
                    " scoringRefMatch=$scoringMatch" +
                    " contentRefMatch=$contentMatch" +
                    " melodicRefMatch=$melodicMatch" +
                    " musicalRefMatch=$musicalMatch" +
                    " audioRefMatch=$audioMatch" +
                    " scalingRefMatch=$scalingMatch" +
                    " garbageRefMatch=$garbageMatch"
        )

        Log.d(
            TAG_PRESET,
            "  Scoring snapshot: pitchWeight=${scoring.pitchWeight}, mfccWeight=${scoring.mfccWeight}, " +
                    "pitchTolerance=${scoring.pitchTolerance}, minScoreThreshold=${scoring.minScoreThreshold}, " +
                    "perfectScoreThreshold=${scoring.perfectScoreThreshold}, scoreCurve=${scoring.scoreCurve}"
        )

        Log.d(
            TAG_PRESET,
            "  Content snapshot: best=${content.contentDetectionBestThreshold}, avg=${content.contentDetectionAvgThreshold}, " +
                    "rightFlatPenalty=${content.rightContentFlatPenalty}, wrongStdPenalty=${content.wrongContentStandardPenalty}"
        )

        Log.d(
            TAG_PRESET,
            "  Melodic snapshot: rangeSemitones=${melodic.melodicRangeSemitones}, " +
                    "varianceThreshold=${melodic.melodicVarianceThreshold}, monotoneDetection=${melodic.monotoneDetectionThreshold}, " +
                    "flatSpeechThreshold=${melodic.flatSpeechThreshold}, monotonePenalty=${melodic.monotonePenalty}"
        )

        Log.d(
            TAG_PRESET,
            "  Garbage snapshot: mfccVarThresh=${garbage.mfccVarianceThreshold}, " +
                    "pitchMonotoneThresh=${garbage.pitchMonotoneThreshold}, oscRate=${garbage.pitchOscillationRate}, " +
                    "entropyThresh=${garbage.spectralEntropyThreshold}, zcrMin=${garbage.zcrMinThreshold}, " +
                    "zcrMax=${garbage.zcrMaxThreshold}, silenceRatioMin=${garbage.silenceRatioMin}, " +
                    "silenceThreshold=${garbage.silenceThreshold}, garbageScoreMax=${garbage.garbageScoreMax}"
        )

        // Quick integrity verdict
        val allMatch = scoringMatch && contentMatch && melodicMatch &&
                musicalMatch && audioMatch && scalingMatch && garbageMatch

        Log.d(
            TAG_PRESET,
            "  Integrity verdict: ${if (allMatch) "OK ✅ (all bundles wired to preset)" else "⚠️ MISMATCH — check applyPreset() wiring"}"
        )

        // -------- FULL RAW DUMP (ALL FIELDS) --------
        logPresetDetailsInternal(streamLabel, preset)
    }

    private fun logPresetDetailsInternal(
        streamLabel: String,
        params: Presets
    ) {
        if (!BuildConfig.DEBUG) return

        val s = params.scoring
        val c = params.content
        val m = params.melodic
        val ms = params.musical
        val a = params.audio
        val scale = params.scaling
        val g = params.garbage

        // ----- ScoringParameters -----
        Log.d(
            TAG_PRESET,
            "[PRESET_RAW][$streamLabel][ScoringParameters]\n" +
                    "  pitchWeight=${s.pitchWeight}\n" +
                    "  mfccWeight=${s.mfccWeight}\n" +
                    "  pitchTolerance=${s.pitchTolerance}\n" +
                    "  variancePenalty=${s.variancePenalty}\n" +
                    "  dtwNormalizationFactor=${s.dtwNormalizationFactor}\n" +
                    "  silenceThreshold=${s.silenceThreshold}\n" +
                    "  minScoreThreshold=${s.minScoreThreshold}\n" +
                    "  perfectScoreThreshold=${s.perfectScoreThreshold}\n" +
                    "  scoreCurve=${s.scoreCurve}\n" +
                    "  consistencyBonus=${s.consistencyBonus}\n" +
                    "  confidenceBonus=${s.confidenceBonus}\n" +
                    "  effortWeight=${s.effortWeight}\n" +
                    "  intensityWeight=${s.intensityWeight}\n" +
                    "  rangeWeight=${s.rangeWeight}\n" +
                    "  intensityPenaltyThreshold=${s.intensityPenaltyThreshold}\n" +
                    "  intensityPenaltyMultiplier=${s.intensityPenaltyMultiplier}"
        )

        // ----- ContentDetectionParameters -----
        Log.d(
            TAG_PRESET,
            "[PRESET_RAW][$streamLabel][ContentDetectionParameters]\n" +
                    "  contentDetectionBestThreshold=${c.contentDetectionBestThreshold}\n" +
                    "  contentDetectionAvgThreshold=${c.contentDetectionAvgThreshold}\n" +
                    "  highMelodicThreshold=${c.highMelodicThreshold}\n" +
                    "  mediumMelodicThreshold=${c.mediumMelodicThreshold}\n" +
                    "  lowMelodicThreshold=${c.lowMelodicThreshold}\n" +
                    "  insufficientMelodyThreshold=${c.insufficientMelodyThreshold}\n" +
                    "  rightContentFlatPenalty=${c.rightContentFlatPenalty}\n" +
                    "  rightContentDifferentMelodyPenalty=${c.rightContentDifferentMelodyPenalty}\n" +
                    "  wrongContentFlatPenalty=${c.wrongContentFlatPenalty}\n" +
                    "  wrongContentInsufficientPenalty=${c.wrongContentInsufficientPenalty}\n" +
                    "  wrongContentStandardPenalty=${c.wrongContentStandardPenalty}"
        )

        // ----- MelodicAnalysisParameters -----
        Log.d(
            TAG_PRESET,
            "[PRESET_RAW][$streamLabel][MelodicAnalysisParameters]\n" +
                    "  melodicRangeWeight=${m.melodicRangeWeight}\n" +
                    "  melodicTransitionWeight=${m.melodicTransitionWeight}\n" +
                    "  melodicVarianceWeight=${m.melodicVarianceWeight}\n" +
                    "  melodicRangeSemitones=${m.melodicRangeSemitones}\n" +
                    "  melodicVarianceThreshold=${m.melodicVarianceThreshold}\n" +
                    "  melodicTransitionThreshold=${m.melodicTransitionThreshold}\n" +
                    "  pitchDifferenceDecayRate=${m.pitchDifferenceDecayRate}\n" +
                    "  silenceToSilenceScore=${m.silenceToSilenceScore}\n" +
                    "  monotoneDetectionThreshold=${m.monotoneDetectionThreshold}\n" +
                    "  flatSpeechThreshold=${m.flatSpeechThreshold}\n" +
                    "  monotonePenalty=${m.monotonePenalty}"
        )

        // ----- MusicalSimilarityParameters -----
        Log.d(
            TAG_PRESET,
            "[PRESET_RAW][$streamLabel][MusicalSimilarityParameters]\n" +
                    "  sameIntervalThreshold=${ms.sameIntervalThreshold}\n" +
                    "  sameIntervalScore=${ms.sameIntervalScore}\n" +
                    "  closeIntervalThreshold=${ms.closeIntervalThreshold}\n" +
                    "  closeIntervalScore=${ms.closeIntervalScore}\n" +
                    "  similarIntervalThreshold=${ms.similarIntervalThreshold}\n" +
                    "  similarIntervalScore=${ms.similarIntervalScore}\n" +
                    "  differentIntervalScore=${ms.differentIntervalScore}\n" +
                    "  emptyPhrasesPenalty=${ms.emptyPhrasesPenalty}\n" +
                    "  phraseCountDifferenceThreshold=${ms.phraseCountDifferenceThreshold}\n" +
                    "  phraseCountPenaltyMultiplier=${ms.phraseCountPenaltyMultiplier}\n" +
                    "  phraseWeightBalance=${ms.phraseWeightBalance}\n" +
                    "  emptyRhythmPenalty=${ms.emptyRhythmPenalty}\n" +
                    "  rhythmDifferenceSoftening=${ms.rhythmDifferenceSoftening}\n" +
                    "  segmentCountSoftening=${ms.segmentCountSoftening}"
        )

        // ----- AudioProcessingParameters -----
        Log.d(
            TAG_PRESET,
            "[PRESET_RAW][$streamLabel][AudioProcessingParameters]\n" +
                    "  pitchFrameSize=${a.pitchFrameSize}\n" +
                    "  pitchHopSize=${a.pitchHopSize}\n" +
                    "  mfccFrameSize=${a.mfccFrameSize}\n" +
                    "  mfccHopSize=${a.mfccHopSize}\n" +
                    "  semitonesPerOctave=${a.semitonesPerOctave}\n" +
                    "  pitchReferenceFreq=${a.pitchReferenceFreq}\n" +
                    "  audioAlignmentThreshold=${a.audioAlignmentThreshold}"
        )

        // ----- ScoreScalingParameters -----
        Log.d(
            TAG_PRESET,
            "[PRESET_RAW][$streamLabel][ScoreScalingParameters]\n" +
                    "  reverseMinScoreAdjustment=${scale.reverseMinScoreAdjustment}\n" +
                    "  reversePerfectScoreAdjustment=${scale.reversePerfectScoreAdjustment}\n" +
                    "  reverseCurveAdjustment=${scale.reverseCurveAdjustment}\n" +
                    "  minimumCurveProtection=${scale.minimumCurveProtection}\n" +
                    "  incredibleFeedbackThreshold=${scale.incredibleFeedbackThreshold}\n" +
                    "  greatJobFeedbackThreshold=${scale.greatJobFeedbackThreshold}\n" +
                    "  goodEffortFeedbackThreshold=${scale.goodEffortFeedbackThreshold}\n" +
                    "  additionalFeedbackThreshold=${scale.additionalFeedbackThreshold}\n" +
                    "  rmsConfidenceMultiplier=${scale.rmsConfidenceMultiplier}"
        )

        // ----- GarbageDetectionParameters -----
        Log.d(
            TAG_PRESET,
            "[PRESET_RAW][$streamLabel][GarbageDetectionParameters]\n" +
                    "  enableGarbageDetection=${g.enableGarbageDetection}\n" +
                    "  noiseThreshold=${g.noiseThreshold}\n" +
                    "  garbageEnergyRatioThreshold=${g.garbageEnergyRatioThreshold}\n" +
                    "  penaltyMultiplier=${g.penaltyMultiplier}\n" +
                    "  mfccVarianceThreshold=${g.mfccVarianceThreshold}\n" +
                    "  pitchMonotoneThreshold=${g.pitchMonotoneThreshold}\n" +
                    "  pitchOscillationRate=${g.pitchOscillationRate}\n" +
                    "  spectralEntropyThreshold=${g.spectralEntropyThreshold}\n" +
                    "  zcrMinThreshold=${g.zcrMinThreshold}\n" +
                    "  zcrMaxThreshold=${g.zcrMaxThreshold}\n" +
                    "  silenceRatioMin=${g.silenceRatioMin}\n" +
                    "  silenceRatioMax=${g.silenceRatioMax}\n" +
                    "  silenceThreshold=${g.silenceThreshold}\n" +
                    "  garbageScorePenalty=${g.garbageScorePenalty}\n" +
                    "  garbageScoreMax=${g.garbageScoreMax}"
        )
    }

    // endregion

    // region Garbage detection runtime logging

    data class GarbageMetrics(
        val pitchMonotone: Float,
        val pitchOscillationRate: Float,
        val spectralEntropy: Float,
        val mfccVariance: Float,
        val silenceRatio: Float
    )

    fun logGarbageDecision(
        recordingId: String,
        params: GarbageDetectionParameters,
        metrics: GarbageMetrics,
        isGarbage: Boolean
    ) {
        if (!BuildConfig.DEBUG) return

        Log.d(
            TAG_GARBAGE,
            "[GARBAGE] recording=$recordingId verdict=$isGarbage\n" +
                    "  pitchMonotone=${metrics.pitchMonotone}\n" +
                    "  pitchOscRate=${metrics.pitchOscillationRate}\n" +
                    "  spectralEntropy=${metrics.spectralEntropy}\n" +
                    "  mfccVariance=${metrics.mfccVariance}\n" +
                    "  silenceRatio=${metrics.silenceRatio}"
        )

        Log.d(
            TAG_GARBAGE,
            "[GARBAGE_THRESHOLDS]\n" +
                    "  mfccVarianceThreshold=${params.mfccVarianceThreshold}\n" +
                    "  pitchMonotoneThreshold=${params.pitchMonotoneThreshold}\n" +
                    "  pitchOscillationRate=${params.pitchOscillationRate}\n" +
                    "  spectralEntropyThreshold=${params.spectralEntropyThreshold}\n" +
                    "  zcrMinThreshold=${params.zcrMinThreshold}\n" +
                    "  zcrMaxThreshold=${params.zcrMaxThreshold}\n" +
                    "  silenceRatioMin=${params.silenceRatioMin}\n" +
                    "  silenceRatioMax=${params.silenceRatioMax}\n" +
                    "  silenceThreshold=${params.silenceThreshold}\n" +
                    "  garbageScorePenalty=${params.garbageScorePenalty}\n" +
                    "  garbageScoreMax=${params.garbageScoreMax}"
        )
    }

    // endregion
}
