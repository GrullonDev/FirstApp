package com.grullondev.firstapp.data.repository

import com.grullondev.firstapp.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class InMemorySettingsRepository : SettingsRepository {
    private val _isDarkMode = MutableStateFlow<Boolean?>(null)
    private val _isLiquidGlass = MutableStateFlow(false)
    private val _themeColor = MutableStateFlow(0xFF008069L)

    override fun isDarkMode(): StateFlow<Boolean?> = _isDarkMode.asStateFlow()
    override fun isLiquidGlassEnabled(): StateFlow<Boolean> = _isLiquidGlass.asStateFlow()
    override fun getThemeColor(): StateFlow<Long> = _themeColor.asStateFlow()

    override suspend fun setDarkMode(enabled: Boolean?) {
        _isDarkMode.value = enabled
    }

    override suspend fun setLiquidGlassEnabled(enabled: Boolean) {
        _isLiquidGlass.value = enabled
    }

    override suspend fun setThemeColor(color: Long) {
        _themeColor.value = color
    }
}
