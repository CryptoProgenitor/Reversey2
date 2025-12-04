package com.quokkalabs.reversey.testing

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quokkalabs.reversey.testing.BackupIntegrationTest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 🎯 BACKUP TEST RUNNER
 * 
 * ViewModel for triggering and monitoring Backup Integration Tests.
 * 
 * USAGE:
 * 1. Inject into your UI/Settings screen
 * 2. Call runBackupTests() from a button
 * 3. Observe testStatus for results
 */
@HiltViewModel
class BackupTestRunner @Inject constructor(
    private val backupBIT: BackupIntegrationTest
) : ViewModel() {
    
    companion object {
        private const val TAG = "BackupTestRunner"
    }
    
    // Test status
    data class TestStatus(
        val isRunning: Boolean = false,
        val currentTest: String = "",
        val progress: String = "",
        val testsRun: Int = 0,
        val testsPassed: Int = 0,
        val testsFailed: Int = 0,
        val failures: List<String> = emptyList(),
        val completed: Boolean = false
    )
    
    private val _testStatus = MutableStateFlow(TestStatus())
    val testStatus: StateFlow<TestStatus> = _testStatus.asStateFlow()
    
    /**
     * Run all backup integration tests.
     * 
     * Results will be emitted to testStatus StateFlow.
     */
    fun runBackupTests() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "🎯 Starting Backup Integration Tests...")
                
                _testStatus.value = TestStatus(
                    isRunning = true,
                    currentTest = "Initializing...",
                    progress = "Starting BIT suite"
                )
                
                // Run all tests
                val results = backupBIT.runAllTests()
                
                // Update final status
                _testStatus.value = TestStatus(
                    isRunning = false,
                    currentTest = "Completed",
                    progress = if (results.testsFailed == 0) {
                        "✅ All tests passed!"
                    } else {
                        "❌ ${results.testsFailed} test(s) failed"
                    },
                    testsRun = results.testsRun,
                    testsPassed = results.testsPassed,
                    testsFailed = results.testsFailed,
                    failures = results.failures,
                    completed = true
                )
                
                Log.d(TAG, "🎯 BIT Complete: ${results.testsPassed}/${results.testsRun} passed")
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ BIT crashed", e)
                
                _testStatus.value = TestStatus(
                    isRunning = false,
                    currentTest = "ERROR",
                    progress = "Test suite crashed: ${e.message}",
                    testsFailed = 1,
                    failures = listOf("Test suite crash: ${e.message}"),
                    completed = true
                )
            }
        }
    }
    
    /**
     * Reset test status.
     */
    fun resetTests() {
        _testStatus.value = TestStatus()
    }
}
