package com.example.reversey.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.reversey.ui.viewmodels.ThemeViewModel
import com.example.reversey.ui.theme.AestheticThemeData
import com.example.reversey.ui.theme.AestheticThemes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSelectionScreen(
    navController: NavController,
    themeViewModel: ThemeViewModel = hiltViewModel()
) {
    // 🎨 CLEAN: Single source of truth for current theme
    val currentThemeId by themeViewModel.currentThemeId.collectAsState()
    val listState = rememberLazyListState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Choose Your Vibe ✨") },
                actions = {  // ✅ MOVED FROM navigationIcon TO actions
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.Close, "Close")  // ✅ CHANGED TO Close icon
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier.Companion
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            val showTopFade by remember { derivedStateOf { listState.canScrollBackward } }
            val showBottomFade by remember { derivedStateOf { listState.canScrollForward } }

            LazyColumn(
                state = listState,
                modifier = Modifier.Companion.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(AestheticThemes.allThemes.values.toList()) { theme ->
                    ThemeCard(
                        theme = theme,
                        isSelected = theme.id == currentThemeId, // 🎨 CLEAN: Simple comparison
                        onClick = { themeViewModel.setTheme(theme.id) } // 🎨 CLEAN: Single method
                    )
                }
            }

            // Scroll fade gradients
            val topGradient = Brush.Companion.verticalGradient(
                0.0f to MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                1.0f to Color.Companion.Transparent
            )
            val bottomGradient = Brush.Companion.verticalGradient(
                0.0f to Color.Companion.Transparent,
                1.0f to MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )

            if (showTopFade) {
                Box(
                    modifier = Modifier.Companion
                        .fillMaxWidth()
                        .height(24.dp)
                        .align(Alignment.Companion.TopCenter)
                        .clip(MaterialTheme.shapes.medium)
                        .background(topGradient)
                )
            }
            if (showBottomFade) {
                Box(
                    modifier = Modifier.Companion
                        .fillMaxWidth()
                        .height(24.dp)
                        .align(Alignment.Companion.BottomCenter)
                        .clip(MaterialTheme.shapes.medium)
                        .background(bottomGradient)
                )
            }
        }
    }
}

@Composable
fun ThemeCard(
    theme: AestheticThemeData,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier.Companion
            .fillMaxWidth()
            .height(150.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(theme.primaryGradient)
            .then(
                if (isSelected) {
                    Modifier.Companion.border(
                        4.dp,
                        Color.Companion.White,
                        androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
                    )
                } else {
                    Modifier.Companion
                }
            )
            .clickable(onClick = onClick)
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.Companion.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.Companion.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Companion.CenterVertically
            ) {
                Text(
                    text = theme.name,
                    style = MaterialTheme.typography.headlineSmall,
                    color = theme.primaryTextColor // 🎨 GLUTE FIX: Use theme's contrast-aware color
                )
                if (isSelected) {
                    Text(
                        "✓",
                        style = MaterialTheme.typography.headlineMedium,
                        color = theme.primaryTextColor
                    )
                }
            }

            Text(
                text = theme.description,
                style = MaterialTheme.typography.bodyMedium,
                color = theme.secondaryTextColor  // 🎨 GLUTE FIX: Use theme's secondary text color
            )

            // Preview buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier.Companion
                        .size(40.dp)
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Companion.Center
                ) {
                    Text(
                        "REC",
                        color = Color.Companion.White,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                Box(
                    modifier = Modifier.Companion
                        .size(40.dp)
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(
                            1.dp,
                            theme.cardBorder,
                            androidx.compose.foundation.shape.RoundedCornerShape(10.dp)
                        )
                )
            }
        }
    }
}