package com.example.reversey

import android.app.Application
import androidx.lifecycle.AndroidViewModel    import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ThemeViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsDataStore = SettingsDataStore(application)

    // Expose the theme as a StateFlow that the UI can collect.
    // It survives configuration changes and always has the latest value.
    val theme: StateFlow<String> = settingsDataStore.getTheme.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "Purple" // Default value while loading
    )

    // Function called from the UI to change the theme.
    fun setTheme(themeName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsDataStore.saveTheme(themeName)
        }
    }
}
