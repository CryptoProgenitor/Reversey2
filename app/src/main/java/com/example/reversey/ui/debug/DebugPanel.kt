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
import com.example.reversey.scoring.ScoringEngine
import com.example.reversey.scoring.ScoringParameters
import kotlin.math.abs

@Composable
fun DebugPanel(
    scoringEngine: ScoringEngine,
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
    if (!isVisible) return

    var parameters by remember { mutableStateOf(scoringEngine.getParameters()) }

    fun refreshParams() {
        parameters = scoringEngine.getParameters()
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
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Scoring Parameters", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                TextButton(onClick = onDismiss) { Text("Close") }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text("Weights (must sum to 1.0)", fontWeight = FontWeight.Medium)

            ParameterSlider(
                label = "Pitch Weight",
                value = parameters.pitchWeight,
                onValueChange = {
                    scoringEngine.updateParameters(parameters.copy(pitchWeight = it)); refreshParams()
                }
            )
            ParameterSlider(
                label = "MFCC (Timbre) Weight",
                value = parameters.mfccWeight,
                onValueChange = {
                    scoringEngine.updateParameters(parameters.copy(mfccWeight = it)); refreshParams()
                }
            )

            val totalWeight = parameters.pitchWeight + parameters.mfccWeight
            Text(
                "Total Weight: %.2f".format(totalWeight),
                color = if (abs(totalWeight - 1f) < 0.01f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text("Pitch Analysis", fontWeight = FontWeight.Medium)
            ParameterSlider(
                label = "Pitch Contour Weight",
                value = parameters.pitchContourWeight,
                onValueChange = {
                    scoringEngine.updateParameters(parameters.copy(pitchContourWeight = it)); refreshParams()
                }
            )
            ParameterSlider(
                label = "Variance Penalty",
                value = parameters.variancePenalty,
                onValueChange = {
                    scoringEngine.updateParameters(parameters.copy(variancePenalty = it)); refreshParams()
                }
            )
            ParameterSlider(
                label = "DTW Normalization\n(lower=stricter on gibberish)",
                value = parameters.dtwNormalizationFactor,
                valueRange = 10f..80f,
                onValueChange = {
                    scoringEngine.updateParameters(parameters.copy(dtwNormalizationFactor = it)); refreshParams()
                }
            )
            ParameterSlider(
                label = "Pitch Tolerance (semitones)",
                value = parameters.pitchTolerance,
                valueRange = 0.5f..5f,
                onValueChange = {
                    scoringEngine.updateParameters(parameters.copy(pitchTolerance = it)); refreshParams()
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text("Detection", fontWeight = FontWeight.Medium)
            ParameterSlider(
                label = "Silence Threshold",
                value = parameters.silenceThreshold,
                valueRange = 0.0f..0.1f,
                onValueChange = {
                    scoringEngine.updateParameters(parameters.copy(silenceThreshold = it)); refreshParams()
                }
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text("Score Scaling", fontWeight = FontWeight.Medium)
            ParameterSlider(
                label = "Min Threshold",
                value = parameters.minScoreThreshold,
                valueRange = 0f..0.5f,
                onValueChange = {
                    scoringEngine.updateParameters(parameters.copy(minScoreThreshold = it)); refreshParams()
                }
            )
            ParameterSlider(
                label = "Perfect Threshold",
                value = parameters.perfectScoreThreshold,
                valueRange = 0.5f..1f,
                onValueChange = {
                    scoringEngine.updateParameters(parameters.copy(perfectScoreThreshold = it)); refreshParams()
                }
            )
            ParameterSlider(
                label = "Score Curve",
                value = parameters.scoreCurve,
                valueRange = 0.5f..3f,
                onValueChange = {
                    scoringEngine.updateParameters(parameters.copy(scoreCurve = it)); refreshParams()
                }
            )

            Button(
                onClick = {
                    scoringEngine.updateParameters(ScoringParameters()); refreshParams()
                },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                Text("Reset to Defaults")
            }
        }
    }
}

@Composable
private fun ParameterSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 14.sp)
            Text("%.2f".format(value), fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

