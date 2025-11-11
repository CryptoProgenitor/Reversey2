package com.example.reversey.data.repositories

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(context: Context) {

    private val dataStore = context.dataStore

    companion object {
        val THEME_KEY = stringPreferencesKey("app_theme")
        val DARK_MODE_KEY = stringPreferencesKey("dark_mode_preference")
        val GAME_MODE_KEY = booleanPreferencesKey("game_mode_enabled")
        val AESTHETIC_THEME_KEY = stringPreferencesKey("aesthetic_theme") // NEW
        val TUTORIAL_COMPLETED_KEY = booleanPreferencesKey("tutorial_completed")
        val BACKUP_RECORDINGS_KEY = booleanPreferencesKey("backup_recordings_enabled")
        val CUSTOM_ACCENT_COLOR_KEY = intPreferencesKey("custom_accent_color") // 🎨 NEW
        val DIFFICULTY_LEVEL_KEY = stringPreferencesKey("difficulty_level") // 🎯 NEW - ADD THIS LINE
    }

    val getTheme: Flow<String> = dataStore.data.map { preferences ->
        preferences[THEME_KEY] ?: "Purple"
    }

    suspend fun saveTheme(themeName: String) {
        dataStore.edit { preferences ->
            preferences[THEME_KEY] = themeName
        }
    }

    val getDarkModePreference: Flow<String> = dataStore.data.map { preferences ->
        preferences[DARK_MODE_KEY] ?: "System"
    }

    val backupRecordingsEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[BACKUP_RECORDINGS_KEY] ?: false  // Default: don't backup recordings
    }

    suspend fun saveDarkModePreference(preference: String) {
        dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = preference
        }
    }

    val getGameModeEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[GAME_MODE_KEY] ?: true
    }

    val tutorialCompleted: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[TUTORIAL_COMPLETED_KEY] ?: false
    }

    suspend fun saveGameMode(isEnabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[GAME_MODE_KEY] = isEnabled
        }
    }

    // NEW: Aesthetic theme
    val getAestheticTheme: Flow<String> = dataStore.data.map { preferences ->
        preferences[AESTHETIC_THEME_KEY] ?: "y2k_cyber" // Default to Y2K
    }

    suspend fun saveAestheticTheme(themeId: String) {
        dataStore.edit { preferences ->
            preferences[AESTHETIC_THEME_KEY] = themeId
        }
    }

    // 🎨 NEW: Custom accent color functions
    val getCustomAccentColor: Flow<Int?> = dataStore.data.map { preferences ->
        preferences[CUSTOM_ACCENT_COLOR_KEY]
    }

    suspend fun saveCustomAccentColor(colorInt: Int) {
        dataStore.edit { preferences ->
            preferences[CUSTOM_ACCENT_COLOR_KEY] = colorInt
        }
    }

    suspend fun clearCustomAccentColor() {
        dataStore.edit { preferences ->
            preferences.remove(CUSTOM_ACCENT_COLOR_KEY)
        }
    }

    suspend fun setBackupRecordingsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[BACKUP_RECORDINGS_KEY] = enabled
        }
    }

    suspend fun setTutorialCompleted(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[TUTORIAL_COMPLETED_KEY] = completed
        }
    }
    // 🎯 NEW: Difficulty level persistence
    val getDifficultyLevel: Flow<String> = dataStore.data.map { preferences ->
        preferences[DIFFICULTY_LEVEL_KEY] ?: "NORMAL" // Default to NORMAL difficulty
    }

    suspend fun saveDifficultyLevel(difficultyLevel: String) {
        dataStore.edit { preferences ->
            preferences[DIFFICULTY_LEVEL_KEY] = difficultyLevel
        }
    }
}