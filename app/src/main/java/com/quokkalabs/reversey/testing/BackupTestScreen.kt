package com.quokkalabs.reversey.ui.testing

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.quokkalabs.reversey.testing.BackupTestRunner

/**
 * 🧪 Dedicated screen for running Backup Integration Tests
 *
 * ACCESS: Add "BIT Tests" menu item → navigates here
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupTestScreen(
    onNavigateBack: () -> Unit = {},
    backupTestRunner: BackupTestRunner = hiltViewModel()
) {
    // Observe test status
    val testStatus by backupTestRunner.testStatus.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🧪 Backup Tests") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Title
            Text(
                text = "Backup Integration Tests",
                style = MaterialTheme.typography.headlineMedium
            )

            Text(
                text = "Verifies backup/restore system",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Run button
            Button(
                onClick = { backupTestRunner.runBackupTests() },
                enabled = !testStatus.isRunning,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (testStatus.isRunning) "⏳ Running Tests..."
                    else "▶️ Run All Tests"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress indicator while running
            if (testStatus.isRunning) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text("Current: ${testStatus.currentTest}")
                Text(
                    text = testStatus.progress,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Results card (only shown after completion)
            if (testStatus.completed) {
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (testStatus.testsFailed == 0) {
                            Color(0xFF4CAF50) // Green for success
                        } else {
                            Color(0xFFF44336) // Red for failure
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        // Main result
                        Text(
                            text = testStatus.progress,
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Stats
                        Text("Tests Run: ${testStatus.testsRun}", color = Color.White)
                        Text("Passed: ${testStatus.testsPassed} ✅", color = Color.White)
                        Text("Failed: ${testStatus.testsFailed} ❌", color = Color.White)

                        // Failures list (if any)
                        if (testStatus.failures.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = Color.White.copy(alpha = 0.3f))
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Failures:",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )

                            testStatus.failures.forEach { failure ->
                                Text(
                                    text = "• $failure",
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Reset button
                OutlinedButton(
                    onClick = { backupTestRunner.resetTests() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Reset")
                }
            }
        }
    }
}