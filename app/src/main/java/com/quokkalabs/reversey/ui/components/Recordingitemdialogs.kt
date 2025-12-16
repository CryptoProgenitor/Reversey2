package com.quokkalabs.reversey.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quokkalabs.reversey.data.models.Recording
import com.quokkalabs.reversey.ui.theme.AestheticThemeData

/**
 * 🎨 SHARED RECORDING ITEM DIALOGS
 * Eliminates code duplication across theme-specific recording items
 * Applies theme-specific styling while maintaining consistent behavior
 */

/**
 * Shared Rename Dialog
 * Works for all themes with theme-specific styling
 */
@Composable
fun RecordingRenameDialog(
    recording: Recording,
    aesthetic: AestheticThemeData,
    onDismiss: () -> Unit,
    onRename: (String, String) -> Unit
) {
    var newName by remember {
        mutableStateOf(
            recording.originalPath?.substringAfterLast("/")?.removeSuffix(".wav") ?: "Recording"
        )
    }

    // Theme-specific colors
    val titleColor = when (aesthetic.id) {
        "egg" -> Color(0xFF2E2E2E)
        else -> MaterialTheme.colorScheme.onSurface
    }

    val accentColor = when (aesthetic.id) {
        "egg" -> Color(0xFFFF8A65)
        "scrapbook" -> Color(0xFF8D6E63)
        else -> MaterialTheme.colorScheme.primary
    }

    // Theme-specific title text
    val titleText = when (aesthetic.id) {
        "egg" -> "Rename Egg Recording 🥚"
        "scrapbook" -> "Rename Recording 📝"
        else -> "Rename Recording"
    }

    // Theme-specific background color (egg theme needs light background)
    val containerColor = when (aesthetic.id) {
        "egg" -> Color(0xFFFFFBF0)  // Light cream background like egg cards
        else -> MaterialTheme.colorScheme.surface
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = containerColor,
        title = {
            Text(
                titleText,
                color = titleColor,
                fontWeight = if (aesthetic.id == "egg") FontWeight.Bold else FontWeight.Normal
            )
        },
        text = {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text("New Name") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentColor,
                    focusedLabelColor = accentColor
                )
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (newName.isNotBlank()) {
                        val finalName = if (newName.endsWith(".wav")) newName else "$newName.wav"
                        onRename(recording.originalPath ?: "", finalName)
                    }
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Text(
                    "Rename",
                    color = Color.White,
                    fontWeight = if (aesthetic.id == "egg") FontWeight.Bold else FontWeight.Normal
                )
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = when (aesthetic.id) {
                        "egg" -> Color(0xFF2E2E2E)
                        else -> MaterialTheme.colorScheme.secondary
                    }
                )
            ) {
                Text(
                    "Cancel",
                    color = Color.White,
                    fontWeight = if (aesthetic.id == "egg") FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    )
}

/**
 * Shared Delete Dialog
 * Works for all themes with theme-specific styling
 */
@Composable
fun RecordingDeleteDialog(
    recording: Recording,
    aesthetic: AestheticThemeData,
    onDismiss: () -> Unit,
    onDelete: (Recording) -> Unit
) {
    // Theme-specific colors
    val titleColor = when (aesthetic.id) {
        "egg" -> Color(0xFF2E2E2E)
        else -> MaterialTheme.colorScheme.error
    }

    val deleteColor = when (aesthetic.id) {
        "egg" -> Color(0xFFFF5722)
        else -> MaterialTheme.colorScheme.error
    }

    val cancelColor = when (aesthetic.id) {
        "egg" -> Color(0xFFFF8A65)
        else -> MaterialTheme.colorScheme.secondary
    }

    // Theme-specific title and button text
    val titleText = when (aesthetic.id) {
        "egg" -> "Crack this egg? 🥚💥"
        "scrapbook" -> "Delete Recording? 📝"
        else -> "Delete Recording?"
    }

    val messageText = when (aesthetic.id) {
        "egg" -> "Are you sure you want to crack this egg? This action cannot be undone!"
        else -> "Are you sure you want to delete this recording? This action cannot be undone."
    }

    val confirmText = when (aesthetic.id) {
        "egg" -> "Crack It!"
        "scrapbook" -> "Delete"
        else -> "Delete"
    }

    val cancelText = when (aesthetic.id) {
        "egg" -> "Keep Safe"
        else -> "Cancel"
    }

    // Theme-specific background color (egg theme needs light background)
    val containerColor = when (aesthetic.id) {
        "egg" -> Color(0xFFFFFBF0)  // Light cream background like egg cards
        else -> MaterialTheme.colorScheme.surface
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = containerColor,
        title = {
            Text(
                titleText,
                color = titleColor,
                fontWeight = if (aesthetic.id == "egg") FontWeight.Bold else FontWeight.Normal
            )
        },
        text = {
            Text(
                messageText,
                fontWeight = if (aesthetic.id == "egg") FontWeight.Bold else FontWeight.Normal
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onDelete(recording)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = deleteColor)
            ) {
                Text(
                    confirmText,
                    color = Color.White,
                    fontWeight = if (aesthetic.id == "egg") FontWeight.Bold else FontWeight.Normal
                )
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = cancelColor)
            ) {
                Text(
                    cancelText,
                    color = Color.White,
                    fontWeight = if (aesthetic.id == "egg") FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    )
}

/**
 * Shared Share Dialog
 * Works for all themes with theme-specific styling
 */
@Composable
fun RecordingShareDialog(
    recording: Recording,
    aesthetic: AestheticThemeData,
    onDismiss: () -> Unit,
    onShare: (String) -> Unit
) {
    // Theme-specific colors
    val titleColor = when (aesthetic.id) {
        "egg" -> Color(0xFF2E2E2E)
        else -> MaterialTheme.colorScheme.onSurface
    }

    val primaryColor = when (aesthetic.id) {
        "egg" -> Color(0xFFFF8A65)
        "scrapbook" -> Color(0xFF8D6E63)
        else -> MaterialTheme.colorScheme.primary
    }

    val secondaryColor = when (aesthetic.id) {
        "egg" -> Color(0xFFFFB74D)
        else -> MaterialTheme.colorScheme.primary
    }

    val cancelColor = when (aesthetic.id) {
        "egg" -> Color(0xFF2E2E2E)
        else -> MaterialTheme.colorScheme.secondary
    }

    // Theme-specific text
    val titleText = when (aesthetic.id) {
        "egg" -> "Share Your Egg! 🥚"
        "scrapbook" -> "Share Recording 📝"
        else -> "Share Recording"
    }

    val promptText = when (aesthetic.id) {
        "egg" -> "Which egg would you like to share?"
        else -> "Which version would you like to share?"
    }

    val originalText = when (aesthetic.id) {
        "egg" -> "Share Fresh Egg 🥚"
        "scrapbook" -> "Share Original 📝"
        else -> "Share Original"
    }

    val reversedText = when (aesthetic.id) {
        "egg" -> "Share Scrambled Egg 🍳"
        "scrapbook" -> "Share Reversed 🔄"
        else -> "Share Reversed"
    }

    // Theme-specific background color (egg theme needs light background)
    val containerColor = when (aesthetic.id) {
        "egg" -> Color(0xFFFFFBF0)  // Light cream background like egg cards
        else -> MaterialTheme.colorScheme.surface
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = containerColor,
        title = {
            Text(
                titleText,
                color = titleColor,
                fontWeight = if (aesthetic.id == "egg") FontWeight.Bold else FontWeight.Normal
            )
        },
        text = {
            Column {
                Text(
                    promptText,
                    fontWeight = if (aesthetic.id == "egg") FontWeight.Bold else FontWeight.Normal
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Share Original button
                Button(
                    onClick = {
                        onShare(recording.originalPath ?: "")
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    Text(
                        originalText,
                        color = Color.White,
                        fontWeight = if (aesthetic.id == "egg") FontWeight.Bold else FontWeight.Normal
                    )
                }

                // Share Reversed button (if reversed exists)
                recording.reversedPath?.let { reversedPath ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            onShare(reversedPath)
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = secondaryColor)
                    ) {
                        Text(
                            reversedText,
                            color = Color.White,
                            fontWeight = if (aesthetic.id == "egg") FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        },
        confirmButton = { },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = cancelColor)
            ) {
                Text(
                    "Cancel",
                    color = Color.White,
                    fontWeight = if (aesthetic.id == "egg") FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    )
}