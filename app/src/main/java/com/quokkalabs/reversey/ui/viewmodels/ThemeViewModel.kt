package com.quokkalabs.reversey.ui.viewmodels

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.quokkalabs.reversey.data.repositories.SettingsDataStore
import com.quokkalabs.reversey.ui.theme.ThemeSoundGate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    application: Application,
    val settingsDataStore: SettingsDataStore
) : AndroidViewModel(application) {

    // 🎨 UNIFIED THEME SYSTEM: Current aesthetic theme ID (no more AppTheme)
    val currentThemeId: StateFlow<String> = settingsDataStore.getAestheticTheme.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Companion.WhileSubscribed(5000),
        initialValue = "y2k_cyber"
    )

    // 🥚 Available themes for UI selection
    val availableThemes = mapOf(
        "sakura_serenity" to "Sakura Serenity", // 🌸 ADD THIS LINE!
        "y2k_cyber" to "Y2K Cyber Pop",
        "scrapbook" to "Scrapbook Vibes", // ← ADD THIS LINE!
        "cottagecore" to "Cottagecore Dreams",
        "dark_academia" to "Dark Academia",
        "vaporwave" to "Neon Vaporwave",
        "egg" to "Egg Theme" // 🥚

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
        started = SharingStarted.Companion.WhileSubscribed(5000),
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
        started = SharingStarted.Companion.WhileSubscribed(5000),
        initialValue = true
    )

    fun setGameMode(isEnabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsDataStore.saveGameMode(isEnabled)
        }
    }

    // 🔇 Theme sound effects setting — one switch to rule them all
    val themeSoundsEnabled: StateFlow<Boolean> = settingsDataStore.getThemeSoundsEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Companion.WhileSubscribed(5000),
        initialValue = true
    )

    init {
        // Keep the global gate in sync so theme sound managers (plain
        // classes, no composition access) can check it directly.
        viewModelScope.launch {
            settingsDataStore.getThemeSoundsEnabled.collect { enabled ->
                ThemeSoundGate.enabled = enabled
            }
        }
    }

    fun setThemeSoundsEnabled(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsDataStore.saveThemeSoundsEnabled(enabled)
        }
    }

    // 📱 Backup recordings setting (separate from theme system)
    val backupRecordingsEnabled: StateFlow<Boolean> = settingsDataStore.backupRecordingsEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Companion.WhileSubscribed(5000),
        initialValue = false
    )

    fun setBackupRecordingsEnabled(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsDataStore.setBackupRecordingsEnabled(enabled)
        }
    }

    // 🎨 Custom accent color for Material 3 theming
    val customAccentColor: StateFlow<Color?> = settingsDataStore.getCustomAccentColor
        .map { colorInt ->
            if (colorInt != null) Color(colorInt) else null
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(5000),
            initialValue = null
        )

    fun setCustomAccentColor(color: Color?) {
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
