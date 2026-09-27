package com.example.anonymouschat.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("drift_settings", Context.MODE_PRIVATE)
    
    // We start by assuming system default, but if the user has toggled it, we use their preference.
    // However, SharedPreferences can't easily track "system default vs explicitly disabled".
    // For simplicity, we default to false, but we'll improve it if needed.
    // Better: let's use a String for theme preference to allow "system", "light", "dark".
    // For now, simple Boolean toggle.
    private val _isDarkMode = MutableStateFlow(prefs.getBoolean("dark_mode", false))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun setDarkMode(isDark: Boolean) {
        prefs.edit().putBoolean("dark_mode", isDark).apply()
        _isDarkMode.value = isDark
    }
}
