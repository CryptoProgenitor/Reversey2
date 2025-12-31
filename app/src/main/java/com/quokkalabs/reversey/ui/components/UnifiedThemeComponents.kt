package com.quokkalabs.reversey.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.quokkalabs.reversey.ui.theme.SharedDefaultComponents

/**
 * ⚠️ DEPRECATED: This file is a compatibility shim.
 *
 * All logic has been moved to:
 * - SharedDefaultComponents (for Material styles)
 * - ScrapbookThemeComponents (for Scrapbook styles)
 *
 * This remains only to support themes that haven't fully migrated their RecordButton yet.
 */

@Composable
fun UnifiedRecordButton(
    isRecording: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Redirect to the shared Material implementation
    SharedDefaultComponents.MaterialRecordButton(
        isRecording = isRecording,
        onClick = onClick
    )
}
