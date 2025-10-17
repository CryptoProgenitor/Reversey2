package com.example.reversey

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info  // ADD THIS
import androidx.compose.material3.Button  // ADD THIS
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember  // ADD THIS
import androidx.compose.runtime.rememberCoroutineScope  // ADD THIS
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext  // ADD THIS
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch  // ADD THIS

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    currentTheme: String,
    onThemeChange: (String) -> Unit,
    // NEW PARAMETERS
    currentDarkModePreference: String,
    onDarkModePreferenceChange: (String) -> Unit,
    // ADD THESE TWO NEW PARAMETERS
    isGameModeEnabled: Boolean,
    onGameModeChange: (Boolean) -> Unit
    ) {
    val themes = listOf("Purple", "Blue", "Green", "Orange")
    // NEW: Options for the dark mode toggle
    val darkModeOptions = listOf("Light", "Dark", "System")
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

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
            item {
                Text(
                    "Gameplay",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onGameModeChange(!isGameModeEnabled) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Enable Game Mode", style = MaterialTheme.typography.bodyLarge)
                    Switch(
                        checked = isGameModeEnabled,
                        onCheckedChange = onGameModeChange
                    )
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            }
            // Theme Color Section (Unchanged)
            item {
                Text(
                    "Icon Colour",
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

            // ADD THIS - Tutorial Section
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                Text(
                    "Help",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
                )
                Button(
                    onClick = {
                        scope.launch {
                            val settingsDataStore = SettingsDataStore(context)
                            settingsDataStore.setTutorialCompleted(false)
                            navController.navigate("home")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("View Tutorial")
                }
            }
        }  // ← This closes LazyColumn
    }  // ← This closes Scaffold
}  // ← This closes SettingsScreen function
