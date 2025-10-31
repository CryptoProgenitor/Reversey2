package com.example.reversey.ui.components.egg

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.reversey.ui.components.egg.HandDrawnEggButton
import com.example.reversey.ui.components.egg.HandDrawnShareIcon
import com.example.reversey.ui.components.egg.HandDrawnPlayIcon
import com.example.reversey.ui.components.egg.HandDrawnReverseIcon
import com.example.reversey.ui.components.egg.CrackedEggIcon

/**
 * EGG-THEMED PLAYER CARD - MATCHING THE BEAUTIFUL RECORDING CARD STYLE! 🥚🍳
 */
@Composable
fun EggStylePlayerCard(
    playerName: String = "Player 1",
    score: String = "100%",
    eggEmoji: String = "🥚", // The egg emoji to show in score circle
    onShare: () -> Unit,
    onPlay: () -> Unit,
    onReverse: () -> Unit,
    //onDelete: () -> Unit,  // ← Change "onDeleteClick" to "onDelete"
    onScoreClick: () -> Unit = {},  // ✅ ADD THIS LINE
    onShowDeleteDialog: (Boolean) -> Unit = {},  // ← ADD THIS LINE TO THE FUNCTION
    onNavigateToParent: () -> Unit, // House icon combined with player name action button
    onShowRenameDialog: () -> Unit, // ← ADD THIS LINE
    modifier: Modifier = Modifier
) {
    // Hand-drawn card style matching the recording card exactly!
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(
                elevation = 0.dp,
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 4.dp,
                color = Color(0xFF2E2E2E), // Same thick black border!
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBF0)), // Same cream background!
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left side: Sticky note label + buttons
            Column {
                // Yellow sticky note "Player 1" label (rotated like mockup)
                Box(
                    modifier = Modifier
                        .rotate(-8f) // Slight rotation for hand-drawn feel
                        .background(
                            Color(0xFFFFF176), // Yellow sticky note color
                            RoundedCornerShape(4.dp)
                        )
                        .border(
                            2.dp,
                            Color(0xFF2E2E2E),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.combinedClickable(
                            onClick = onNavigateToParent, // Short press
                            onLongClick = onShowRenameDialog // Long press
                        )
                    ) {
                        HandDrawnHouseIcon(
                            modifier = Modifier.size(16.dp),
                            onClick = {} // Empty - click handled by parent Row
                        )
                        Text(
                            text = playerName,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            ),
                            color = Color(0xFF2E2E2E),
                            maxLines = 1,                                    // ← ADD THIS
                            overflow = TextOverflow.Ellipsis,                // ← ADD THIS
                            modifier = Modifier.widthIn(max = 120.dp)        // ← ADD THIS
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 4 Hand-drawn buttons in a row (matching mockup layout)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. SHARE BUTTON (purple like mockup)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        HandDrawnEggButton(
                            onClick = onShare,
                            backgroundColor = Color(0xFF9C27B0), // Same purple from recording card
                            size = 45.dp // Slightly smaller for player card
                        ) {
                            HandDrawnShareIcon()
                        }
                        Text(
                            text = "Share",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF2E2E2E)
                        )
                    }

                    // 2. PLAY BUTTON (orange like mockup)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        HandDrawnEggButton(
                            onClick = onPlay,
                            backgroundColor = Color(0xFFFF8A65), // Same orange from recording card
                            size = 45.dp
                        ) {
                            HandDrawnPlayIcon()
                        }
                        Text(
                            text = "Play",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF2E2E2E)
                        )
                    }

                    // 3. REV BUTTON (orange like mockup)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        HandDrawnEggButton(
                            onClick = onReverse,
                            backgroundColor = Color(0xFFFF8A65), // Same orange
                            size = 45.dp
                        ) {
                            HandDrawnReverseIcon()
                        }
                        Text(
                            text = "Rev",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF2E2E2E)
                        )
                    }

                    // 4. DEL BUTTON (red-orange like mockup)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        HandDrawnEggButton(
                            onClick = { onShowDeleteDialog(true) },  // ← CHANGED THIS LINE!
                            backgroundColor = Color(0xFFFF5722), // Slightly more red for delete
                            size = 45.dp
                        ) {
                            CrackedEggIcon() // Use the cracked egg for delete!
                        }
                        Text(
                            text = "Del",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF2E2E2E)
                        )
                    }
                }
            }

            // Right side: Score circle with egg emoji (matching user request)
            EggScoreCircle(
                score = score,
                eggEmoji = eggEmoji,
                size = 80.dp,
                onClick = onScoreClick  // ✅ ADD THIS LINE
            )
        }
    }
}

/**
 * Score circle with egg emoji and thick hand-drawn border
 */
@Composable
fun EggScoreCircle(
    score: String,
    eggEmoji: String,
    size: Dp,
    onClick: () -> Unit = {},  // ← ADD THIS
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clickable { onClick() }  // ← ADD THIS
            .border(
                width = 3.dp,
                color = Color(0xFF9C27B0), // Purple border matching share button
                shape = CircleShape
            )
            .background(
                Color(0xFFFFFBF0), // Same cream background
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = eggEmoji,
                style = MaterialTheme.typography.titleMedium,
                fontSize = 20.sp
            )
            Text(
                text = score,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                ),
                color = Color(0xFF2E2E2E)
            )
        }
    }
}

/**
 * Hand-drawn house icon for navigation to parent
 */
@Composable
fun HandDrawnHouseIcon(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Canvas(modifier = modifier) {  // ← REMOVE .clickable { onClick() }
        val strokeWidth = 2.dp.toPx()
        val color = Color(0xFF2E2E2E)

        // House roof (triangle)
        val roofPath = Path().apply {
            moveTo(size.width * 0.5f, size.height * 0.1f)
            lineTo(size.width * 0.1f, size.height * 0.5f)
            lineTo(size.width * 0.9f, size.height * 0.5f)
            close()
        }
        drawPath(roofPath, color, style = Stroke(strokeWidth))

        // House body (rectangle)
        drawRect(
            color = color,
            topLeft = Offset(size.width * 0.2f, size.height * 0.45f),
            size = androidx.compose.ui.geometry.Size(size.width * 0.6f, size.height * 0.45f),
            style = Stroke(strokeWidth)
        )

        // Door
        drawRect(
            color = color,
            topLeft = Offset(size.width * 0.4f, size.height * 0.65f),
            size = androidx.compose.ui.geometry.Size(size.width * 0.2f, size.height * 0.25f),
            style = Stroke(strokeWidth)
        )
    }
}