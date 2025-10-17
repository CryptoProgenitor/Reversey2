package com.example.reversey

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSelectionScreen(
    navController: NavController,
    themeViewModel: ThemeViewModel = viewModel()
) {
    val currentAestheticTheme by themeViewModel.aestheticTheme.collectAsState()
    val listState = rememberLazyListState()  // ADD THIS

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Choose Your Vibe ✨") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(  // WRAP LazyColumn in Box
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            val showTopFade by remember { derivedStateOf { listState.canScrollBackward } }
            val showBottomFade by remember { derivedStateOf { listState.canScrollForward } }

            LazyColumn(
                state = listState,  // ADD THIS
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(ThemeRepository.allThemes) { theme ->
                    ThemeCard(
                        theme = theme,
                        isSelected = theme.id == currentAestheticTheme.id,
                        onClick = { themeViewModel.setAestheticTheme(theme.id) }
                    )
                }
            }

            // ADD GRADIENTS
            val topGradient = Brush.verticalGradient(
                0.0f to MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                1.0f to Color.Transparent
            )
            val bottomGradient = Brush.verticalGradient(
                0.0f to Color.Transparent,
                1.0f to MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )

            if (showTopFade) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .align(Alignment.TopCenter)
                        .clip(MaterialTheme.shapes.medium)  // Rounds corners
                        .background(topGradient)
                )
            }
            if (showBottomFade) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .align(Alignment.BottomCenter)
                        .clip(MaterialTheme.shapes.medium)  // Rounds corners
                        .background(bottomGradient)
                )
            }
        }
    }
}

@Composable
fun ThemeCard(
    theme: AppTheme,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(theme.primaryGradient)
            .then(
                if (isSelected) {
                    Modifier.border(4.dp, Color.White, RoundedCornerShape(20.dp))
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick)
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = theme.name,
                    style = MaterialTheme.typography.headlineSmall,
                    color = theme.textPrimary
                )
                if (isSelected) {
                    Text("✓", style = MaterialTheme.typography.headlineMedium, color = Color.White)
                }
            }

            Text(
                text = theme.description,
                style = MaterialTheme.typography.bodyMedium,
                color = theme.textSecondary
            )

            // Preview buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(theme.buttonGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Text("REC", color = Color.White, style = MaterialTheme.typography.labelSmall)
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(theme.cardBackground)
                        .border(1.dp, theme.cardBorder, RoundedCornerShape(10.dp))
                )
            }
        }
    }
}