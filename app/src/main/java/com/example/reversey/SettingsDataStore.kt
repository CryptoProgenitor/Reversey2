package com.example.reversey

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Creates a DataStore instance, available application-wide
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(context: Context) {

    private val dataStore = context.dataStore

    // Define a key for storing our theme name as a String
    companion object {
        val THEME_KEY = stringPreferencesKey("app_theme")
    }

    // A Flow that emits the currently saved theme name. Defaults to "Purple".
    val getTheme: Flow<String> = dataStore.data.map { preferences ->
        preferences[THEME_KEY] ?: "Purple"
    }

    // A suspend function to save the chosen theme name
    suspend fun saveTheme(themeName: String) {
        dataStore.edit { preferences ->
            preferences[THEME_KEY] = themeName
        }
    }
}
