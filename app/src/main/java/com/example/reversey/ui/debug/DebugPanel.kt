package com.example.reversey.ui.debug

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.reversey.scoring.*

@Composable
fun DebugPanel(
    scoringEngine: ScoringEngine,
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
    if (!isVisible) return

    var parameters by remember { mutableStateOf(scoringEngine.getParameters()) }
    var audioParams by remember { mutableStateOf(scoringEngine.getAudioParameters()) }
    var contentParams by remember { mutableStateOf(scoringEngine.getContentParameters()) }
    var melodicParams by remember { mutableStateOf(scoringEngine.getMelodicParameters()) }
    var musicalParams by remember { mutableStateOf(scoringEngine.getMusicalParameters()) }
    var scalingParams by remember { mutableStateOf(scoringEngine.getScalingParameters()) }

    var selectedTab by remember { mutableStateOf(0) }

    fun refreshParams() {
        parameters = scoringEngine.getParameters()
        audioParams = scoringEngine.getAudioParameters()
        contentParams = scoringEngine.getContentParameters()
        melodicParams = scoringEngine.getMelodicParameters()
        musicalParams = scoringEngine.getMusicalParameters()
        scalingParams = scoringEngine.getScalingParameters()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Advanced Tuning\nOptions", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                TextButton(onClick = onDismiss) { Text("Close") }
            }

            Text(
                "⚠️ These settings affect how the app scores your singing. Changes apply immediately!",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Tab Bar
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Basic", fontSize = 9.sp) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Content", fontSize = 9.sp) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Musical", fontSize = 9.sp) }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("Technical", fontSize = 8.sp) }
                )
            }

            // Tab Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(top = 16.dp)
            ) {
                when (selectedTab) {
                    0 -> BasicParametersTab(parameters, scoringEngine, ::refreshParams)
                    1 -> ContentDetectionTab(contentParams, scoringEngine, ::refreshParams)
                    2 -> MusicalAnalysisTab(melodicParams, musicalParams, scoringEngine, ::refreshParams)
                    3 -> TechnicalParametersTab(audioParams, scalingParams, scoringEngine, ::refreshParams)
                }
            }

            // Reset All Button
            Button(
                onClick = {
                    scoringEngine.updateParameters(ScoringParameters())
                    scoringEngine.updateAudioParameters(AudioProcessingParameters())
                    scoringEngine.updateContentParameters(ContentDetectionParameters())
                    scoringEngine.updateMelodicParameters(MelodicAnalysisParameters())
                    scoringEngine.updateMusicalParameters(MusicalSimilarityParameters())
                    scoringEngine.updateScalingParameters(ScoreScalingParameters())
                    refreshParams()
                },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                Text("🔄 Reset ALL to Defaults")
            }
        }
    }
}

@Composable
private fun BasicParametersTab(
    parameters: ScoringParameters,
    scoringEngine: ScoringEngine,
    refreshParams: () -> Unit
) {
    SectionHeader("🎯 Core Settings")

    ParameterSlider(
        label = "Content vs Voice Priority",
        description = "Left = voice similarity, Right = content accuracy",
        value = parameters.pitchWeight,
        valueRange = 0f..1f,
        onValueChange = {
            scoringEngine.updateParameters(parameters.copy(pitchWeight = it, mfccWeight = 1f - it))
            refreshParams()
        }
    )

    ParameterSlider(
        label = "Pitch Forgiveness",
        description = "How off-key you can be and still score well",
        value = parameters.pitchTolerance,
        valueRange = 5f..25f,
        onValueChange = {
            scoringEngine.updateParameters(parameters.copy(pitchTolerance = it))
            refreshParams()
        }
    )

    ParameterSlider(
        label = "Minimum Score Threshold",
        description = "How easy it is to get above 0%",
        value = parameters.minScoreThreshold,
        valueRange = 0.1f..0.4f,
        onValueChange = {
            scoringEngine.updateParameters(parameters.copy(minScoreThreshold = it))
            refreshParams()
        }
    )

    ParameterSlider(
        label = "Perfect Score Threshold",
        description = "How hard it is to get 100%",
        value = parameters.perfectScoreThreshold,
        valueRange = 0.6f..0.95f,
        onValueChange = {
            scoringEngine.updateParameters(parameters.copy(perfectScoreThreshold = it))
            refreshParams()
        }
    )

    ParameterSlider(
        label = "Scoring Generosity",
        description = "How generous the scoring curve is",
        value = parameters.scoreCurve,
        valueRange = 1f..3f,
        onValueChange = {
            scoringEngine.updateParameters(parameters.copy(scoreCurve = it))
            refreshParams()
        }
    )
}

@Composable
private fun ContentDetectionTab(
    contentParams: ContentDetectionParameters,
    scoringEngine: ScoringEngine,
    refreshParams: () -> Unit
) {
    SectionHeader("🧠 Smart Content Detection (v8.0.0)")

    ParameterSlider(
        label = "Content Recognition Sensitivity",
        description = "How easily app detects correct words",
        value = contentParams.contentDetectionBestThreshold,
        valueRange = 0.1f..0.7f,
        onValueChange = {
            scoringEngine.updateContentParameters(contentParams.copy(contentDetectionBestThreshold = it))
            refreshParams()
        }
    )

    ParameterSlider(
        label = "Average Content Threshold",
        description = "Secondary content detection threshold",
        value = contentParams.contentDetectionAvgThreshold,
        valueRange = 0.1f..0.5f,
        onValueChange = {
            scoringEngine.updateContentParameters(contentParams.copy(contentDetectionAvgThreshold = it))
            refreshParams()
        }
    )

    SectionHeader("🎵 Melodic Requirements")

    ParameterSlider(
        label = "High Melodic Threshold",
        description = "What counts as very melodic singing",
        value = contentParams.highMelodicThreshold,
        valueRange = 0.4f..0.8f,
        onValueChange = {
            scoringEngine.updateContentParameters(contentParams.copy(highMelodicThreshold = it))
            refreshParams()
        }
    )

    ParameterSlider(
        label = "Low Melodic Threshold",
        description = "Below this = flat/monotone speech",
        value = contentParams.lowMelodicThreshold,
        valueRange = 0.1f..0.5f,
        onValueChange = {
            scoringEngine.updateContentParameters(contentParams.copy(lowMelodicThreshold = it))
            refreshParams()
        }
    )

    SectionHeader("⚖️ Smart Penalties")

    ParameterSlider(
        label = "Right Content, Flat Penalty",
        description = "Penalty for correct words but no melody",
        value = contentParams.rightContentFlatPenalty,
        valueRange = 0f..0.5f,
        onValueChange = {
            scoringEngine.updateContentParameters(contentParams.copy(rightContentFlatPenalty = it))
            refreshParams()
        }
    )

    ParameterSlider(
        label = "Right Content, Different Melody",
        description = "Penalty for correct words, different tune",
        value = contentParams.rightContentDifferentMelodyPenalty,
        valueRange = 0f..0.3f,
        onValueChange = {
            scoringEngine.updateContentParameters(contentParams.copy(rightContentDifferentMelodyPenalty = it))
            refreshParams()
        }
    )

    ParameterSlider(
        label = "Wrong Content Standard Penalty",
        description = "Base penalty for wrong words",
        value = contentParams.wrongContentStandardPenalty,
        valueRange = 0.2f..0.8f,
        onValueChange = {
            scoringEngine.updateContentParameters(contentParams.copy(wrongContentStandardPenalty = it))
            refreshParams()
        }
    )

    ParameterSlider(
        label = "Wrong Content Flat Penalty",
        description = "Harsh penalty for wrong words + no melody",
        value = contentParams.wrongContentFlatPenalty,
        valueRange = 0.5f..0.9f,
        onValueChange = {
            scoringEngine.updateContentParameters(contentParams.copy(wrongContentFlatPenalty = it))
            refreshParams()
        }
    )
}

@Composable
private fun MusicalAnalysisTab(
    melodicParams: MelodicAnalysisParameters,
    musicalParams: MusicalSimilarityParameters,
    scoringEngine: ScoringEngine,
    refreshParams: () -> Unit
) {
    SectionHeader("🎼 Melodic Analysis Weights")

    ParameterSlider(
        label = "Range Weight",
        description = "How much pitch range matters",
        value = melodicParams.melodicRangeWeight,
        valueRange = 0f..1f,
        onValueChange = {
            scoringEngine.updateMelodicParameters(melodicParams.copy(melodicRangeWeight = it))
            refreshParams()
        }
    )

    ParameterSlider(
        label = "Transition Weight",
        description = "How much pitch movement matters",
        value = melodicParams.melodicTransitionWeight,
        valueRange = 0f..1f,
        onValueChange = {
            scoringEngine.updateMelodicParameters(melodicParams.copy(melodicTransitionWeight = it))
            refreshParams()
        }
    )

    ParameterSlider(
        label = "Variance Weight",
        description = "How much overall variation matters",
        value = melodicParams.melodicVarianceWeight,
        valueRange = 0f..1f,
        onValueChange = {
            scoringEngine.updateMelodicParameters(melodicParams.copy(melodicVarianceWeight = it))
            refreshParams()
        }
    )

    SectionHeader("🎯 Musical Similarity Scoring")

    ParameterSlider(
        label = "Same Interval Threshold",
        description = "How close intervals need to be to match",
        value = musicalParams.sameIntervalThreshold,
        valueRange = 0.1f..1f,
        onValueChange = {
            scoringEngine.updateMusicalParameters(musicalParams.copy(sameIntervalThreshold = it))
            refreshParams()
        }
    )

    ParameterSlider(
        label = "Different Interval Score",
        description = "Score for very different intervals",
        value = musicalParams.differentIntervalScore,
        valueRange = 0f..0.5f,
        onValueChange = {
            scoringEngine.updateMusicalParameters(musicalParams.copy(differentIntervalScore = it))
            refreshParams()
        }
    )

    ParameterSlider(
        label = "Empty Phrases Penalty",
        description = "Penalty when phrase structures don't match",
        value = musicalParams.emptyPhrasesPenalty,
        valueRange = 0f..0.5f,
        onValueChange = {
            scoringEngine.updateMusicalParameters(musicalParams.copy(emptyPhrasesPenalty = it))
            refreshParams()
        }
    )
}

@Composable
private fun TechnicalParametersTab(
    audioParams: AudioProcessingParameters,
    scalingParams: ScoreScalingParameters,
    scoringEngine: ScoringEngine,
    refreshParams: () -> Unit
) {
    SectionHeader("🔧 Audio Processing")

    ParameterSlider(
        label = "Pitch Frame Size",
        description = "Audio samples analyzed for pitch",
        value = audioParams.pitchFrameSize.toFloat(),
        valueRange = 1024f..8192f,
        onValueChange = {
            scoringEngine.updateAudioParameters(audioParams.copy(pitchFrameSize = it.toInt()))
            refreshParams()
        }
    )

    ParameterSlider(
        label = "Audio Alignment Threshold",
        description = "Volume level that counts as audio start",
        value = audioParams.audioAlignmentThreshold,
        valueRange = 0.001f..0.02f,
        onValueChange = {
            scoringEngine.updateAudioParameters(audioParams.copy(audioAlignmentThreshold = it))
            refreshParams()
        }
    )

    SectionHeader("📊 Score Scaling")

    ParameterSlider(
        label = "Reverse Challenge Adjustment",
        description = "Make reverse challenges easier",
        value = scalingParams.reverseMinScoreAdjustment,
        valueRange = 0.7f..1f,
        onValueChange = {
            scoringEngine.updateScalingParameters(scalingParams.copy(reverseMinScoreAdjustment = it))
            refreshParams()
        }
    )

    ParameterSlider(
        label = "Confidence Multiplier",
        description = "How much volume affects confidence bonus",
        value = scalingParams.rmsConfidenceMultiplier,
        valueRange = 1f..10f,
        onValueChange = {
            scoringEngine.updateScalingParameters(scalingParams.copy(rmsConfidenceMultiplier = it))
            refreshParams()
        }
    )

    SectionHeader("💬 Feedback Thresholds")

    ParameterSlider(
        label = "Incredible Threshold",
        description = "Score needed for 'Incredible!' message",
        value = scalingParams.incredibleFeedbackThreshold.toFloat(),
        valueRange = 80f..100f,
        onValueChange = {
            scoringEngine.updateScalingParameters(scalingParams.copy(incredibleFeedbackThreshold = it.toInt()))
            refreshParams()
        }
    )
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        title,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun ParameterSlider(
    label: String,
    description: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Text(
                    description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Text(
                "%.2f".format(value),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
        )
    }
}