package com.maaz.befit.viewmodel

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val prefs: SharedPreferences = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)

    private val _isDarkTheme = MutableStateFlow(prefs.getBoolean("dark_theme", false))
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _isHighContrast = MutableStateFlow(prefs.getBoolean("high_contrast", false))
    val isHighContrast: StateFlow<Boolean> = _isHighContrast.asStateFlow()

    private val _isLargeText = MutableStateFlow(prefs.getBoolean("large_text", false))
    val isLargeText: StateFlow<Boolean> = _isLargeText.asStateFlow()

    private val _soundEnabled = MutableStateFlow(prefs.getBoolean("sound_enabled", true))
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(prefs.getBoolean("notifications_enabled", true))
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    fun toggleDarkTheme() {
        viewModelScope.launch {
            _isDarkTheme.value = !_isDarkTheme.value
        }
    }

    fun setDarkTheme(isDark: Boolean) {
        viewModelScope.launch {
            _isDarkTheme.value = isDark
            prefs.edit().putBoolean("dark_theme", isDark).apply()
        }
    }

    fun toggleHighContrast() {
        viewModelScope.launch {
            _isHighContrast.value = !_isHighContrast.value
        }
    }

    fun setHighContrast(isHighContrast: Boolean) {
        viewModelScope.launch {
            _isHighContrast.value = isHighContrast
            prefs.edit().putBoolean("high_contrast", isHighContrast).apply()
        }
    }

    fun toggleLargeText() {
        viewModelScope.launch {
            _isLargeText.value = !_isLargeText.value
        }
    }

    fun setLargeText(isLargeText: Boolean) {
        viewModelScope.launch {
            _isLargeText.value = isLargeText
            prefs.edit().putBoolean("large_text", isLargeText).apply()
        }
    }

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch {
            _soundEnabled.value = enabled
            prefs.edit().putBoolean("sound_enabled", enabled).apply()
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            _notificationsEnabled.value = enabled
            prefs.edit().putBoolean("notifications_enabled", enabled).apply()
        }
    }
}