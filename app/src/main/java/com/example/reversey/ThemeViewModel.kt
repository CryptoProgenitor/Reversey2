package com.example.reversey

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ThemeViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsDataStore = SettingsDataStore(application)

    // Expose the theme color as a StateFlow
    val theme: StateFlow<String> = settingsDataStore.getTheme.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "Purple"
    )

    // Function to change the theme color
    fun setTheme(themeName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsDataStore.saveTheme(themeName)
        }
    }

    // Expose the dark mode preference as a StateFlow
    val darkModePreference: StateFlow<String> = settingsDataStore.getDarkModePreference.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "System"
    )

    // Function to change the dark mode preference
    fun setDarkModePreference(preference: String) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsDataStore.saveDarkModePreference(preference)
        }
    }

    // Expose the game mode setting as a StateFlow
    val gameModeEnabled: StateFlow<Boolean> = settingsDataStore.getGameModeEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    // Function to change the game mode setting
    fun setGameMode(isEnabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsDataStore.saveGameMode(isEnabled)
        }
    }

    // 🎨 NEW: Custom accent color override
    val customAccentColor: StateFlow<Color?> = settingsDataStore.getCustomAccentColor
        .map { colorInt ->
            if (colorInt != null) {
                Color(colorInt)
            } else {
                null
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // 🎨 NEW: Function to set custom accent color
    fun setCustomAccentColor(color: Color) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsDataStore.saveCustomAccentColor(color.toArgb())
        }
    }

    // 🎨 NEW: Function to clear custom accent (use theme default)
    fun clearCustomAccentColor() {
        viewModelScope.launch(Dispatchers.IO) {
            settingsDataStore.clearCustomAccentColor()
        }
    }

    // 🎨 UPDATED: Combined aesthetic theme with custom accent override
    val aestheticTheme: StateFlow<AppTheme> = combine(
        settingsDataStore.getAestheticTheme,
        customAccentColor
    ) { themeId, customAccent ->
        val baseTheme = ThemeRepository.getThemeById(themeId)
        if (customAccent != null) {
            // Override the accent color while keeping everything else
            baseTheme.copy(accentColor = customAccent)
        } else {
            // Use the original theme as-is
            baseTheme
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ThemeRepository.y2kCyberTheme
    )

    // Function to change the aesthetic theme
    fun setAestheticTheme(themeId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsDataStore.saveAestheticTheme(themeId)
        }
    }

    // 🎨 NEW: Get the original (default) accent color for current theme
    val defaultAccentColor: StateFlow<Color> = settingsDataStore.getAestheticTheme
        .map { themeId -> ThemeRepository.getThemeById(themeId).accentColor }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemeRepository.y2kCyberTheme.accentColor
        )

    // Expose the backup recordings setting as a StateFlow
    val backupRecordingsEnabled: StateFlow<Boolean> = settingsDataStore.backupRecordingsEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    // Function to change the backup recordings setting
    fun setBackupRecordingsEnabled(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsDataStore.setBackupRecordingsEnabled(enabled)
        }
    }
}