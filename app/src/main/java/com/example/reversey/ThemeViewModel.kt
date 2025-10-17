package com.example.reversey

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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

    // NEW: Expose the dark mode preference as a StateFlow
    val darkModePreference: StateFlow<String> = settingsDataStore.getDarkModePreference.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "System"
    )

    // NEW: Function to change the dark mode preference
    fun setDarkModePreference(preference: String) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsDataStore.saveDarkModePreference(preference)
        }
    }

    // --- Add this block for Game Mode ---

    // Expose the game mode setting as a StateFlow
    val gameModeEnabled: StateFlow<Boolean> = settingsDataStore.getGameModeEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false // Default to false
    )

    // Function to change the game mode setting
    fun setGameMode(isEnabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsDataStore.saveGameMode(isEnabled)
        }
    }
    // Expose the aesthetic theme as a StateFlow
    val aestheticTheme: StateFlow<AppTheme> = settingsDataStore.getAestheticTheme
        .map { themeId -> ThemeRepository.getThemeById(themeId) }
        .stateIn(
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

}
