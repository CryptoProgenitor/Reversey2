package com.example.reversey

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
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
        // NEW: Key for the dark mode preference
        val DARK_MODE_KEY = stringPreferencesKey("dark_mode_preference")
    }

    // Flow for the theme color
    val getTheme: Flow<String> = dataStore.data.map { preferences ->
        preferences[THEME_KEY] ?: "Purple"
    }

    // Suspend function to save the theme color
    suspend fun saveTheme(themeName: String) {
        dataStore.edit { preferences ->
            preferences[THEME_KEY] = themeName
        }
    }

    // NEW: Flow for the dark mode preference. Defaults to "System".
    val getDarkModePreference: Flow<String> = dataStore.data.map { preferences ->
        preferences[DARK_MODE_KEY] ?: "System"
    }

    // NEW: Suspend function to save the dark mode preference
    suspend fun saveDarkModePreference(preference: String) {
        dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = preference
        }
    }
}
