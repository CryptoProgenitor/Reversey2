package com.example.reversey

import android.app.Application
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    application: Application,
    val settingsDataStore: SettingsDataStore
) : AndroidViewModel(application) {

    // 🎨 UNIFIED THEME SYSTEM: Current aesthetic theme ID (no more AppTheme)
    val currentThemeId: StateFlow<String> = settingsDataStore.getAestheticTheme.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "y2k_cyber"
    )

    // 🎨 Function to change theme
    fun setTheme(themeId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsDataStore.saveAestheticTheme(themeId)
        }
    }

    // 🌙 Dark mode preference (separate from theme system)
    val darkModePreference: StateFlow<String> = settingsDataStore.getDarkModePreference.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "System"
    )

    fun setDarkModePreference(preference: String) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsDataStore.saveDarkModePreference(preference)
        }
    }

    // 🎮 Game mode setting (separate from theme system)
    val gameModeEnabled: StateFlow<Boolean> = settingsDataStore.getGameModeEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    fun setGameMode(isEnabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsDataStore.saveGameMode(isEnabled)
        }
    }

    // 📱 Backup recordings setting (separate from theme system)
    val backupRecordingsEnabled: StateFlow<Boolean> = settingsDataStore.backupRecordingsEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    fun setBackupRecordingsEnabled(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsDataStore.setBackupRecordingsEnabled(enabled)
        }
    }

    // 🎨 Custom accent color for Material 3 theming
    val customAccentColor: StateFlow<androidx.compose.ui.graphics.Color?> = settingsDataStore.getCustomAccentColor
        .map { colorInt ->
            if (colorInt != null) androidx.compose.ui.graphics.Color(colorInt) else null
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun setCustomAccentColor(color: androidx.compose.ui.graphics.Color?) {
        viewModelScope.launch(Dispatchers.IO) {
            if (color != null) {
                settingsDataStore.saveCustomAccentColor(color.toArgb())
            } else {
                // Use the proper clear function from DataStore
                settingsDataStore.clearCustomAccentColor()
            }
        }
    }

    fun clearCustomAccentColor() {
        viewModelScope.launch(Dispatchers.IO) {
            settingsDataStore.clearCustomAccentColor()
        }
    }
}