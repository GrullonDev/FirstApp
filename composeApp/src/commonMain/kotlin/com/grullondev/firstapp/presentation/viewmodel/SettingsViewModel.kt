package com.grullondev.firstapp.presentation.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grullondev.firstapp.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val themeColor: StateFlow<Color> = settingsRepository.getThemeColor()
        .map { Color(it.toULong()) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, Color(0xFF008069))

    val isLiquidGlassEnabled = settingsRepository.isLiquidGlassEnabled()
    val isDarkMode = settingsRepository.isDarkMode()

    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled = _notificationsEnabled.asStateFlow()

    private val _privacyLockEnabled = MutableStateFlow(true)
    val privacyLockEnabled = _privacyLockEnabled.asStateFlow()

    private val _saveMediaOnMobileData = MutableStateFlow(false)
    val saveMediaOnMobileData = _saveMediaOnMobileData.asStateFlow()

    private val _useCompactFolders = MutableStateFlow(false)
    val useCompactFolders = _useCompactFolders.asStateFlow()

    private val _selectedLanguage = MutableStateFlow("Español")
    val selectedLanguage = _selectedLanguage.asStateFlow()

    fun toggleLiquidGlass() {
        viewModelScope.launch { settingsRepository.setLiquidGlassEnabled(!isLiquidGlassEnabled.value) }
    }

    fun toggleDarkMode() {
        viewModelScope.launch { settingsRepository.setDarkMode(!(isDarkMode.value ?: false)) }
    }

    fun updateThemeColor(color: Color) {
        viewModelScope.launch { settingsRepository.setThemeColor(color.value.toLong()) }
    }

    fun toggleNotifications() { _notificationsEnabled.value = !_notificationsEnabled.value }
    fun togglePrivacyLock() { _privacyLockEnabled.value = !_privacyLockEnabled.value }
    fun toggleSaveMediaOnMobileData() { _saveMediaOnMobileData.value = !_saveMediaOnMobileData.value }
    fun toggleCompactFolders() { _useCompactFolders.value = !_useCompactFolders.value }
    fun toggleLanguage() {
        _selectedLanguage.value = if (_selectedLanguage.value == "Español") "English" else "Español"
    }
}
