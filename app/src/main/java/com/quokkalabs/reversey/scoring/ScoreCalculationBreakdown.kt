package com.quokkalabs.reversey.scoring

import com.quokkalabs.reversey.data.models.ChallengeType

/**
 * 🧮 SCORE CALCULATION BREAKDOWN
 *
 * Captures the complete calculation journey for the scorecard tooltip.
 * Supports all 4 scoring paths:
 *   - Speech Forward
 *   - Speech Reverse
 *   - Singing Forward
 *   - Singing Reverse
 *
 * Usage: Built in scoring engines, passed through ScoringResult → PlayerAttempt → UI
 *
 * NOTE: This class is also defined in ScoringCommonModels.kt
 *       This standalone file provides extension functions for UI display.
 */

// ============================================================
// DISPLAY EXTENSION FUNCTIONS
// ============================================================

/**
 * Single calculation step for display
 */
data class CalculationStep(
    val stepNumber: Int,
    val title: String,
    val formula: String,
    val calculation: String,
    val result: String,
    val isBonus: Boolean = false,
    val isPenalty: Boolean = false,
    val isFinal: Boolean = false
)

/**
 * Convert breakdown to displayable calculation steps
 */
fun ScoreCalculationBreakdown.toDisplaySteps(): List<CalculationStep> {
    val steps = mutableListOf<CalculationStep>()

    // Step 1: Input Similarities
    steps.add(CalculationStep(
        stepNumber = 1,
        title = "Input Similarities",
        formula = "From DTW alignment",
        calculation = "Pitch: ${formatPercent(pitchSimilarity)}, MFCC: ${formatPercent(mfccSimilarity)}",
        result = "✓ Features extracted"
    ))

    // Step 2: Weighted Combination
    steps.add(CalculationStep(
        stepNumber = 2,
        title = "Weighted Combination",
        formula = "pitch × ${formatWeight(pitchWeight)} + mfcc × ${formatWeight(mfccWeight)}",
        calculation = "${formatPercent(pitchSimilarity)} × ${formatWeight(pitchWeight)} + ${formatPercent(mfccSimilarity)} × ${formatWeight(mfccWeight)}",
        result = formatPercent(baseWeightedScore)
    ))

    // Step 3: Musical Bonuses (Singing only)
    musicalBonuses?.let { bonuses ->
        steps.add(CalculationStep(
            stepNumber = 3,
            title = "Musical Bonuses",
            formula = "complexity×0.10 + intervals×0.15 + harmonics×0.05",
            calculation = "${formatPercent(bonuses.complexityBonus)}×0.10 + ${formatPercent(bonuses.intervalAccuracy)}×0.15 + ${formatPercent(bonuses.harmonicRichness)}×0.05",
            result = "+${formatPercent(bonuses.totalMusicalBonus)}",
            isBonus = true
        ))
    }

    // Step 4: Content Detection Penalty
    val contentStepNumber = if (musicalBonuses != null) 4 else 3
    if (contentPenaltyTriggered) {
        steps.add(CalculationStep(
            stepNumber = contentStepNumber,
            title = "Content Penalty",
            formula = if (reverseHandicap > 0) "mfcc < (threshold - handicap)" else "mfcc < threshold",
            calculation = "${formatPercent(mfccSimilarity)} < ${formatPercent(contentThresholdEffective)} → ×${formatWeight(contentPenaltyMultiplier)}",
            result = formatPercent(scoreAfterContentPenalty),
            isPenalty = true
        ))
    } else {
        steps.add(CalculationStep(
            stepNumber = contentStepNumber,
            title = "Content Check",
            formula = "mfcc ≥ threshold",
            calculation = "${formatPercent(mfccSimilarity)} ≥ ${formatPercent(contentThresholdEffective)}",
            result = "✓ Passed"
        ))
    }

    // Step 5: Variance Penalty
    val varianceStepNumber = contentStepNumber + 1
    if (variancePenaltyTriggered) {
        steps.add(CalculationStep(
            stepNumber = varianceStepNumber,
            title = "Monotone Penalty",
            formula = "Flat delivery detected",
            calculation = "Score × ${formatWeight(variancePenaltyMultiplier)}",
            result = "Applied",
            isPenalty = true
        ))
    }

    // Step 6: Performance Adjustments
    val perfStepNumber = varianceStepNumber + 1
    val hasAdjustments = consistencyBonus > 0.001f || confidenceBonus > 0.001f || hummingDetected
    if (hasAdjustments) {
        val adjustmentParts = mutableListOf<String>()
        if (consistencyBonus > 0.001f) adjustmentParts.add("consistency +${formatPercent(consistencyBonus)}")
        if (confidenceBonus > 0.001f) adjustmentParts.add("confidence +${formatPercent(confidenceBonus)}")
        if (hummingDetected) adjustmentParts.add("humming ×${formatWeight(hummingPenaltyMultiplier)}")

        steps.add(CalculationStep(
            stepNumber = perfStepNumber,
            title = "Performance Adjustments",
            formula = "× (1 + bonuses) × humming",
            calculation = adjustmentParts.joinToString(", "),
            result = "×${formatWeight(totalPerformanceMultiplier * hummingPenaltyMultiplier)}",
            isBonus = consistencyBonus + confidenceBonus > 0.01f && !hummingDetected,
            isPenalty = hummingDetected
        ))
    }

    // Step 7: Threshold Normalization
    val normStepNumber = perfStepNumber + 1
    val thresholdNote = if (challengeType == ChallengeType.REVERSE) " (reverse handicap)" else ""
    steps.add(CalculationStep(
        stepNumber = normStepNumber,
        title = "Normalize$thresholdNote",
        formula = "(raw - min) / (perfect - min)",
        calculation = "(${formatPercent(rawScoreFinal)} - ${formatPercent(minThresholdEffective)}) / (${formatPercent(perfectThresholdEffective)} - ${formatPercent(minThresholdEffective)})",
        result = formatPercent(normalizedScore)
    ))

    // Step 8: Score Curve
    val curveStepNumber = normStepNumber + 1
    val curveNote = if (scoreCurve == 1.0f) " (linear)" else ""
    steps.add(CalculationStep(
        stepNumber = curveStepNumber,
        title = "Apply Curve$curveNote",
        formula = "normalized ^ (1/${formatWeight(scoreCurve)})",
        calculation = "${formatPercent(normalizedScore)} ^ ${formatWeight(1f / scoreCurve)}",
        result = formatPercent(curvedScore)
    ))

    // Step 9: Final Score
    steps.add(CalculationStep(
        stepNumber = curveStepNumber + 1,
        title = "Final Score",
        formula = "curved × 100",
        calculation = "${formatPercent(curvedScore)} × 100",
        result = "${finalScore}%",
        isFinal = true
    ))

    return steps
}

/**
 * Quick summary for tooltip header
 */
fun ScoreCalculationBreakdown.toQuickSummary(): String {
    val engineName = when (scoringEngineType) {
        ScoringEngineType.SPEECH_ENGINE -> "Speech"
        ScoringEngineType.SINGING_ENGINE -> "Singing"
    }
    val challengeName = when (challengeType) {
        ChallengeType.FORWARD -> "Forward"
        ChallengeType.REVERSE -> "Reverse"
    }
    return "$engineName • $challengeName • ${difficultyLevel.displayName}"
}

/**
 * Get key penalties/bonuses applied
 */
fun ScoreCalculationBreakdown.getKeyModifiers(): List<String> {
    val modifiers = mutableListOf<String>()

    musicalBonuses?.let {
        if (it.totalMusicalBonus > 0.05f) {
            modifiers.add("🎵 Musical bonus +${(it.totalMusicalBonus * 100).toInt()}%")
        }
    }

    if (contentPenaltyTriggered) {
        modifiers.add("⚠️ Content mismatch penalty")
    }

    if (variancePenaltyTriggered) {
        modifiers.add("📉 Monotone penalty")
    }

    if (hummingDetected) {
        modifiers.add("🎤 Humming detected")
    }

    if (challengeType == ChallengeType.REVERSE) {
        modifiers.add("🔄 Reverse handicap applied")
    }

    return modifiers
}

// ============================================================
// FORMATTING HELPERS
// ============================================================

private fun formatPercent(value: Float): String {
    return "%.1f%%".format(value * 100)
}

private fun formatWeight(value: Float): String {
    return "%.2f".format(value)
}
