package com.quokkalabs.reversey.ui.menu

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quokkalabs.reversey.data.backup.BackupProgress
import com.quokkalabs.reversey.data.backup.ConflictStrategy
import com.quokkalabs.reversey.data.backup.ImportAnalysis
import com.quokkalabs.reversey.data.backup.RecordingBackupEntry
import com.quokkalabs.reversey.data.backup.RestoreResult
import com.quokkalabs.reversey.ui.viewmodels.DateChipOption
import com.quokkalabs.reversey.ui.theme.LocalAestheticTheme
import com.quokkalabs.reversey.ui.theme.MenuColors


// ============================================================
//  STEP 1: FILE PICKER
// ============================================================

@Composable
fun RestoreStep1_FilePicker(
    onFileSelected: (android.net.Uri, String?) -> Unit,
    onBack: () -> Unit
) {
    val menuColors = LocalAestheticTheme.current.menuColors
    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { onFileSelected(it, uri.lastPathSegment) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        WizardGlassHeader(
            title = "RESTORE BACKUP",
            subtitle = "Step 1 of 4",
            onBack = onBack
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Big tap target card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .shadow(12.dp, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .background(menuColors.menuCardBackground)
                .border(2.dp, menuColors.toggleActive.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                .clickable { filePicker.launch("application/zip") },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.FolderOpen,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = menuColors.toggleActive
                )
                Text(
                    "Tap to pick your backup",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = menuColors.menuItemText
                )
                Text(
                    "Select a .zip file",
                    style = MaterialTheme.typography.bodyMedium,
                    color = menuColors.menuItemText.copy(alpha = 0.6f)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Info text
        Text(
            text = "📦 Looking for a ReVerseY backup file",
            style = MaterialTheme.typography.bodyMedium,
            color = menuColors.menuItemText.copy(alpha = 0.6f),
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}

// ============================================================
//  STEP 1.5: ANALYZING (Loading State)
// ============================================================

@Composable
fun RestoreStep_Analyzing() {
    val menuColors = LocalAestheticTheme.current.menuColors
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(64.dp),
                color = menuColors.toggleActive,
                strokeWidth = 4.dp
            )
            Text(
                "Analyzing backup...",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = menuColors.menuTitleText
            )
            Text(
                "Let's See →",
                style = MaterialTheme.typography.bodyLarge,
                color = menuColors.menuItemText.copy(alpha = 0.6f)
            )
        }
    }
}

// ============================================================
//  STEP 2: ANALYSIS RESULTS
// ============================================================

@Composable
fun RestoreStep2_Analysis(
    analysis: ImportAnalysis,
    filteredAnalysis: ImportAnalysis,
    dateChipOptions: List<DateChipOption>,
    activeDateChips: Set<String>,
    selectedNewRecordings: Set<String>,
    selectedConflicts: Set<String>,
    customNames: Map<String, String>,
    onDateChipClick: (DateChipOption) -> Unit,
    onToggleNewRecording: (String) -> Unit,
    onToggleConflict: (String) -> Unit,
    onProceed: () -> Unit,
    onBack: () -> Unit,
    formatDate: (Long) -> String,
    formatSize: (Long) -> String
) {
    val menuColors = LocalAestheticTheme.current.menuColors
    val scrollState = rememberScrollState()

    // Helper to get display name or fallback to filename
    fun getDisplayName(filename: String): String = customNames[filename] ?: filename

    Column(modifier = Modifier.fillMaxSize()) {
        WizardGlassHeader(
            title = "WHAT'S INSIDE",
            subtitle = "Step 2 of 4",
            onBack = onBack
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Date filter chips - always show if we have data
            Text(
                "FILTER BY DATE",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = menuColors.menuItemText.copy(alpha = 0.6f),
                modifier = Modifier.padding(start = 4.dp)
            )

            var showDatePicker by remember { mutableStateOf(false) }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                dateChipOptions.forEach { option ->
                    DateFilterChip(
                        label = option.label,
                        isSelected = option.label in activeDateChips || (activeDateChips.isEmpty() && option.label == "All"),
                        onClick = { onDateChipClick(option) }
                    )
                }

                // Custom date picker chip
                DateFilterChip(
                    label = "Custom...",
                    isSelected = "Custom" in activeDateChips,
                    onClick = { showDatePicker = true }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Date range picker dialog
            if (showDatePicker) {
                DateRangePickerDialog(
                    initialStartMs = analysis.dateRange?.first,
                    initialEndMs = analysis.dateRange?.second,
                    onConfirm = { startMs, endMs ->
                        onDateChipClick(DateChipOption("Custom", startMs, endMs))
                        showDatePicker = false
                    },
                    onDismiss = { showDatePicker = false }
                )
            }

            // Summary card
            // Summary card
            SummaryCard(
                totalRecordings = filteredAnalysis.newRecordings.size + filteredAnalysis.duplicateRecordings.size + filteredAnalysis.conflictingRecordings.size,
                newCount = filteredAnalysis.newRecordings.size,
                duplicateCount = filteredAnalysis.duplicateRecordings.size,
                conflictCount = filteredAnalysis.conflictingRecordings.size,
                attemptCount = filteredAnalysis.newAttempts.size + filteredAnalysis.duplicateAttempts.size + filteredAnalysis.conflictingAttempts.size,
                orphanCount = filteredAnalysis.orphanedAttempts.size,
                totalSize = formatSize(filteredAnalysis.totalSizeBytes),
                dateRange = filteredAnalysis.dateRange?.let {
                    "${formatDate(it.first)} → ${formatDate(it.second)}"
                }
            )

            // THE BREAKDOWN
            Text(
                "THE BREAKDOWN",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = menuColors.menuItemText.copy(alpha = 0.6f),
                modifier = Modifier.padding(start = 4.dp, top = 8.dp)
            )

            // New Stuff section
            // New Recordings section
            if (filteredAnalysis.newRecordings.isNotEmpty()) {
                val newAttemptCount = filteredAnalysis.newAttempts.size
                ExpandableSection(
                    emoji = "✨",
                    title = "New Recordings",
                    subtitle = if (newAttemptCount > 0) "includes $newAttemptCount attempts" else null,
                    count = filteredAnalysis.newRecordings.size,
                    selectedCount = selectedNewRecordings.size,
                    badgeColor = Color(0xFF10B981)
                ) {
                    filteredAnalysis.newRecordings.forEach { recording ->
                        CheckboxItem(
                            label = getDisplayName(recording.filename),
                            subtitle = "${formatSize(recording.fileSizeBytes)} • ${formatDate(recording.creationTimestampMs)}",
                            isChecked = recording.filename in selectedNewRecordings,
                            onCheckedChange = { onToggleNewRecording(recording.filename) }
                        )
                    }
                }
            }

            // Already Got These section
            if (filteredAnalysis.duplicateRecordings.isNotEmpty()) {
                ExpandableSection(
                    emoji = "✓",
                    title = "Already Got These",
                    count = filteredAnalysis.duplicateRecordings.size,
                    selectedCount = null,
                    badgeColor = Color(0xFF6B7280),
                    defaultExpanded = false
                ) {
                    filteredAnalysis.duplicateRecordings.forEach { recording ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = Color(0xFF6B7280),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                getDisplayName(recording.filename),
                                style = MaterialTheme.typography.bodyMedium,
                                color = menuColors.menuItemText.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }

            // Same Name, Different Vibes (Conflicts)
            if (filteredAnalysis.conflictingRecordings.isNotEmpty()) {
                ExpandableSection(
                    emoji = "⚠️",
                    title = "Same Name, Different Vibes",
                    count = filteredAnalysis.conflictingRecordings.size,
                    selectedCount = selectedConflicts.size,
                    badgeColor = Color(0xFFF59E0B)
                ) {
                    filteredAnalysis.conflictingRecordings.forEach { recording ->
                        CheckboxItem(
                            label = getDisplayName(recording.filename),
                            subtitle = "${formatSize(recording.fileSizeBytes)} • Needs resolution",
                            isChecked = recording.filename in selectedConflicts,
                            onCheckedChange = { onToggleConflict(recording.filename) },
                            accentColor = Color(0xFFF59E0B)
                        )
                    }
                }
            }

            // Lost Attempts (Orphaned)
            if (filteredAnalysis.orphanedAttempts.isNotEmpty()) {
                ExpandableSection(
                    emoji = "👻",
                    title = "Lost Attempts",
                    count = filteredAnalysis.orphanedAttempts.size,
                    selectedCount = null,
                    badgeColor = Color(0xFF8B5CF6),
                    defaultExpanded = false
                ) {
                    Text(
                        "These attempts don't have a parent recording. They'll be skipped.",
                        style = MaterialTheme.typography.bodySmall,
                        color = menuColors.menuItemText.copy(alpha = 0.6f),
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Bottom button logic
        val hasNewStuff = selectedNewRecordings.isNotEmpty()
        val hasConflicts = selectedConflicts.isNotEmpty()
        val hasAnythingToImport = hasNewStuff || hasConflicts
        val onlyDuplicates = filteredAnalysis.newRecordings.isEmpty() &&
                filteredAnalysis.conflictingRecordings.isEmpty() &&
                filteredAnalysis.duplicateRecordings.isNotEmpty()

        if (onlyDuplicates) {
            // Nothing new to import - show friendly message
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF10B981).copy(alpha = 0.15f))
                    .border(1.dp, Color(0xFF10B981).copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("✨", fontSize = 32.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "You're all caught up!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981)
                    )
                    Text(
                        "Everything in this backup is already on your device",
                        style = MaterialTheme.typography.bodySmall,
                        color = menuColors.menuTitleText.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            WizardPrimaryButton(
                text = "Sweet! 🎉",
                enabled = true,
                onClick = onBack
            )
        } else {
            WizardPrimaryButton(
                text = if (hasConflicts) "Sort It Out →" else "Let's Go!",
                enabled = hasAnythingToImport,
                onClick = onProceed
            )
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

// ============================================================
//  STEP 3: CONFLICT RESOLUTION
// ============================================================

@Composable
fun RestoreStep3_Conflicts(
    conflicts: List<RecordingBackupEntry>,
    selectedConflicts: Set<String>,
    globalStrategy: ConflictStrategy,
    onSetGlobalStrategy: (ConflictStrategy) -> Unit,
    onProceed: () -> Unit,
    onBack: () -> Unit,
    formatSize: (Long) -> String
) {
    val menuColors = LocalAestheticTheme.current.menuColors
    Column(modifier = Modifier.fillMaxSize()) {
        WizardGlassHeader(
            title = "SORT IT OUT",
            subtitle = "Step 3 of 4",
            onBack = onBack
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "These files have the same name but different content. What should we do?",
                style = MaterialTheme.typography.bodyLarge,
                color = menuColors.menuTitleText
            )

            // Strategy options
            ConflictStrategyCard(
                emoji = "🛡️",
                title = "Keep Mine",
                subtitle = "Skip backup files, keep what's on your device",
                isSelected = globalStrategy == ConflictStrategy.SKIP_DUPLICATES,
                onClick = { onSetGlobalStrategy(ConflictStrategy.SKIP_DUPLICATES) }
            )

            ConflictStrategyCard(
                emoji = "🤝",
                title = "Keep Both",
                subtitle = "Import backup as new files (adds numbers)",
                isSelected = globalStrategy == ConflictStrategy.KEEP_BOTH,
                onClick = { onSetGlobalStrategy(ConflictStrategy.KEEP_BOTH) }
            )

            ConflictStrategyCard(
                emoji = "📦",
                title = "Use Backup",
                subtitle = "Replace device files with backup versions",
                isSelected = globalStrategy == ConflictStrategy.MERGE_ATTEMPTS_ONLY,
                onClick = { onSetGlobalStrategy(ConflictStrategy.MERGE_ATTEMPTS_ONLY) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Affected files preview
            GlassCard {
                Column {
                    Text(
                        "AFFECTED FILES",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = menuColors.menuItemText.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    conflicts.filter { it.filename in selectedConflicts }.forEach { recording ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("⚠️", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                recording.filename,
                                style = MaterialTheme.typography.bodyMedium,
                                color = menuColors.menuItemText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                formatSize(recording.fileSizeBytes),
                                style = MaterialTheme.typography.bodySmall,
                                color = menuColors.menuItemText.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }

        WizardPrimaryButton(
            text = "Let's Go! ✨",
            enabled = true,
            onClick = onProceed
        )
    }
}

// ============================================================
//  STEP 4: IMPORTING PROGRESS
// ============================================================

@Composable
fun RestoreStep4_Importing(
    progress: BackupProgress
) {
    val menuColors = LocalAestheticTheme.current.menuColors
    val messages = listOf(
        "Unpacking...",
        "Moving recordings...",
        "Syncing attempts...",
        "Almost there..."
    )

    val progressData = progress as? BackupProgress.InProgress
    val percentage = progressData?.percentage ?: 0
    val currentMessage = when {
        percentage < 25 -> messages[0]
        percentage < 50 -> messages[1]
        percentage < 75 -> messages[2]
        else -> messages[3]
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            // Animated progress ring
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { percentage / 100f },
                    modifier = Modifier.size(120.dp),
                    color = menuColors.toggleActive,
                    strokeWidth = 8.dp,
                    trackColor = menuColors.toggleActive.copy(alpha = 0.3f)
                )
                Text(
                    "$percentage%",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = menuColors.menuTitleText
                )
            }

            Text(
                "Doing the thing...",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = menuColors.menuTitleText
            )

            Text(
                currentMessage,
                style = MaterialTheme.typography.bodyLarge,
                color = menuColors.menuItemText.copy(alpha = 0.6f)
            )

            if (progressData != null) {
                Text(
                    progressData.currentFileName,
                    style = MaterialTheme.typography.bodySmall,
                    color = menuColors.menuItemText.copy(alpha = 0.6f).copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ============================================================
//  STEP 5: COMPLETE
// ============================================================

@Composable
fun RestoreStep5_Complete(
    result: RestoreResult,
    onDone: () -> Unit
) {
    val menuColors = LocalAestheticTheme.current.menuColors
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            // Success icon with animation
            val scale by rememberInfiniteTransition(label = "pulse").animateFloat(
                initialValue = 1f,
                targetValue = 1.1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale"
            )

            Text(
                "✨",
                fontSize = (64 * scale).sp
            )

            Text(
                "All Done!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = menuColors.menuTitleText
            )

            // Stats card
            GlassCard {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatRow("Recordings imported", result.recordingsImported.toString())
                    StatRow("Attempts restored", result.attemptsImported.toString())
                    if (result.recordingsSkipped > 0) {
                        StatRow("Skipped (duplicates)", result.recordingsSkipped.toString())
                    }
                    if (result.customNamesRestored > 0) {
                        StatRow("Names restored", result.customNamesRestored.toString())
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            WizardPrimaryButton(
                text = "Sweet! 🎉",
                enabled = true,
                onClick = onDone
            )
        }
    }
}

// ============================================================
//  ERROR STATE
// ============================================================

@Composable
fun RestoreStep_Error(
    message: String,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    val menuColors = LocalAestheticTheme.current.menuColors
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text("😕", fontSize = 64.sp)

            Text(
                "Something went wrong",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = menuColors.menuTitleText
            )

            Text(
                message,
                style = MaterialTheme.typography.bodyLarge,
                color = menuColors.menuItemText.copy(alpha = 0.6f)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                WizardSecondaryButton(
                    text = "Go Back",
                    onClick = onBack
                )
                WizardPrimaryButton(
                    text = "Try Again",
                    enabled = true,
                    onClick = onRetry
                )
            }
        }
    }
}

// ============================================================
//  REUSABLE COMPONENTS
// ============================================================

@Composable
private fun WizardGlassHeader(
    title: String,
    subtitle: String,
    onBack: () -> Unit
) {
    val menuColors = LocalAestheticTheme.current.menuColors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(menuColors.menuItemBackground)
            .border(1.dp, menuColors.menuBorder, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = menuColors.menuTitleText
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    ),
                    color = menuColors.menuTitleText
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = menuColors.menuItemText.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.width(32.dp))
        }
    }
}

@Composable
private fun WizardPrimaryButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val menuColors = LocalAestheticTheme.current.menuColors
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = menuColors.toggleActive,
            disabledContainerColor = menuColors.toggleActive.copy(alpha = 0.3f)
        )
    ) {
        Text(
            text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun WizardSecondaryButton(
    text: String,
    onClick: () -> Unit
) {
    val menuColors = LocalAestheticTheme.current.menuColors
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = menuColors.menuTitleText
        ),
        border = BorderStroke(1.dp, menuColors.menuTitleText.copy(alpha = 0.5f))
    ) {
        Text(
            text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun DateFilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val menuColors = LocalAestheticTheme.current.menuColors
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (isSelected) menuColors.toggleActive
                else Color.White.copy(alpha = 0.2f)
            )
            .border(
                1.dp,
                if (isSelected) menuColors.toggleActive else Color.White.copy(alpha = 0.3f),
                RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else menuColors.menuTitleText
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SummaryCard(
    totalRecordings: Int,
    newCount: Int,
    duplicateCount: Int,
    conflictCount: Int,
    attemptCount: Int,
    orphanCount: Int,
    totalSize: String,
    dateRange: String?
) {
    val menuColors = LocalAestheticTheme.current.menuColors
    GlassCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryBadge("🎤 $totalRecordings recordings", Color(0xFF8B5CF6))
                SummaryBadge("✨ $newCount new", Color(0xFF10B981))
                if (duplicateCount > 0) {
                    SummaryBadge("✓ $duplicateCount skipped", Color(0xFF6B7280))
                }
                if (conflictCount > 0) {
                    SummaryBadge("⚠️ $conflictCount conflict", Color(0xFFF59E0B))
                }
                SummaryBadge("🎯 $attemptCount attempts", Color(0xFF3B82F6))
                SummaryBadge("📊 $totalSize", Color(0xFF6B7280))
            }

            if (dateRange != null) {
                HorizontalDivider(color = menuColors.menuItemText.copy(alpha = 0.2f))
                Text(
                    "📅 $dateRange",
                    style = MaterialTheme.typography.bodySmall,
                    color = menuColors.menuItemText.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun SummaryBadge(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun ExpandableSection(
    emoji: String,
    title: String,
    subtitle: String? = null,
    count: Int,
    selectedCount: Int?,
    badgeColor: Color,
    defaultExpanded: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val menuColors = LocalAestheticTheme.current.menuColors
    var expanded by remember { mutableStateOf(defaultExpanded) }

    GlassCard {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(emoji, fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = menuColors.menuItemText
                        )
                    }
                    if (subtitle != null) {
                        Text(
                            subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = menuColors.menuItemText.copy(alpha = 0.6f),
                            modifier = Modifier.padding(start = 32.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(badgeColor.copy(alpha = 0.2f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            if (selectedCount != null) "$selectedCount/$count" else count.toString(),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = menuColors.menuItemText.copy(alpha = 0.5f)
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun CheckboxItem(
    label: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: () -> Unit,
    accentColor: Color? = null
) {
    val menuColors = LocalAestheticTheme.current.menuColors
    val resolvedAccent = accentColor ?: menuColors.toggleActive
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isChecked) resolvedAccent.copy(alpha = 0.1f) else Color.Transparent)
            .clickable(onClick = onCheckedChange)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = { onCheckedChange() },
            colors = CheckboxDefaults.colors(
                checkedColor = resolvedAccent,
                uncheckedColor = menuColors.menuItemText.copy(alpha = 0.4f)
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = menuColors.menuItemText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = menuColors.menuItemText.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun ConflictStrategyCard(
    emoji: String,
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val menuColors = LocalAestheticTheme.current.menuColors
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(if (isSelected) 8.dp else 2.dp, shape)
            .clip(shape)
            .background(
                if (isSelected) menuColors.toggleActive.copy(alpha = 0.15f)
                else menuColors.menuCardBackground
            )
            .border(
                2.dp,
                if (isSelected) menuColors.toggleActive else Color.Transparent,
                shape
            )
            .clickable(onClick = onClick)
            .padding(18.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(emoji, fontSize = 28.sp)

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) menuColors.toggleActive else menuColors.menuItemText
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = menuColors.menuItemText.copy(alpha = 0.7f)
                )
            }

            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = menuColors.toggleActive,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun GlassCard(
    content: @Composable ColumnScope.() -> Unit
) {
    val menuColors = LocalAestheticTheme.current.menuColors
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, shape)
            .clip(shape)
            .background(menuColors.menuCardBackground)
            .border(1.dp, Color.White.copy(alpha = 0.3f), shape)
            .padding(16.dp)
    ) {
        Column { content() }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    val menuColors = LocalAestheticTheme.current.menuColors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = menuColors.menuItemText.copy(alpha = 0.7f)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = menuColors.menuItemText
        )
    }
}

// ============================================================
//  DATE RANGE PICKER DIALOG
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateRangePickerDialog(
    initialStartMs: Long?,
    initialEndMs: Long?,
    onConfirm: (Long, Long) -> Unit,
    onDismiss: () -> Unit
) {
    val menuColors = LocalAestheticTheme.current.menuColors
    val dateRangePickerState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = initialStartMs,
        initialSelectedEndDateMillis = initialEndMs
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val start = dateRangePickerState.selectedStartDateMillis
                    val end = dateRangePickerState.selectedEndDateMillis
                    if (start != null && end != null) {
                        // Add 23:59:59 to end date to include the full day
                        val endOfDay = end + (24 * 60 * 60 * 1000 - 1)
                        onConfirm(start, endOfDay)
                    }
                },
                enabled = dateRangePickerState.selectedStartDateMillis != null &&
                        dateRangePickerState.selectedEndDateMillis != null
            ) {
                Text("OK", color = menuColors.toggleActive)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DateRangePicker(
            state = dateRangePickerState,
            title = {
                Text(
                    "Pick Date Range",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            headline = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val startText = dateRangePickerState.selectedStartDateMillis?.let {
                        java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.US).format(java.util.Date(it))
                    } ?: "Start"
                    val endText = dateRangePickerState.selectedEndDateMillis?.let {
                        java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.US).format(java.util.Date(it))
                    } ?: "End"

                    Text(startText, style = MaterialTheme.typography.bodyLarge)
                    Text("→", style = MaterialTheme.typography.bodyLarge)
                    Text(endText, style = MaterialTheme.typography.bodyLarge)
                }
            },
            showModeToggle = false,
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
        )
    }
}