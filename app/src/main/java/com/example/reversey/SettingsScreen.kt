package com.example.reversey

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    currentTheme: String,
    onThemeChange: (String) -> Unit,
    // NEW PARAMETERS
    currentDarkModePreference: String,
    onDarkModePreferenceChange: (String) -> Unit
) {
    val themes = listOf("Purple", "Blue", "Green", "Orange")
    // NEW: Options for the dark mode toggle
    val darkModeOptions = listOf("Light", "Dark", "System")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Theme Color Section (Unchanged)
            item {
                Text(
                    "Theme Color",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
                )
            }
            items(themes) { themeName ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onThemeChange(themeName) }
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (themeName == currentTheme),
                        onClick = { onThemeChange(themeName) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = themeName, style = MaterialTheme.typography.bodyLarge)
                }
            }

            // NEW: Dark Mode Section
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                Text(
                    "Dark Mode",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
                )
            }
            items(darkModeOptions) { preference ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onDarkModePreferenceChange(preference) }
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (preference == currentDarkModePreference),
                        onClick = { onDarkModePreferenceChange(preference) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = preference, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}
