package com.example.reversey

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
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

    suspend fun saveDarkModePreference(preference: String) {
        dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = preference
        }
    }

    val getGameModeEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[GAME_MODE_KEY] ?: false
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
}