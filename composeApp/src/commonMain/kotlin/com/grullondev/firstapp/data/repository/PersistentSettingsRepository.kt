package com.grullondev.firstapp.data.repository

import com.grullondev.firstapp.domain.repository.SettingsRepository
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PersistentSettingsRepository(private val settings: Settings = Settings()) : SettingsRepository {
    
    private val _isDarkMode = MutableStateFlow<Boolean?>(
        if (settings.hasKey("is_dark_mode")) settings.getBoolean("is_dark_mode", false) else null
    )
    private val _isLiquidGlass = MutableStateFlow(settings.getBoolean("is_liquid_glass", false))
    private val _themeColor = MutableStateFlow(settings.getLong("theme_color", 0xFF008069))

    override fun isDarkMode(): StateFlow<Boolean?> = _isDarkMode.asStateFlow()
    override fun isLiquidGlassEnabled(): StateFlow<Boolean> = _isLiquidGlass.asStateFlow()
    override fun getThemeColor(): StateFlow<Long> = _themeColor.asStateFlow()

    override suspend fun setDarkMode(enabled: Boolean?) {
        if (enabled == null) {
            settings.remove("is_dark_mode")
        } else {
            settings.putBoolean("is_dark_mode", enabled)
        }
        _isDarkMode.value = enabled
    }

    override suspend fun setLiquidGlassEnabled(enabled: Boolean) {
        settings.putBoolean("is_liquid_glass", enabled)
        _isLiquidGlass.value = enabled
    }

    override suspend fun setThemeColor(color: Long) {
        settings.putLong("theme_color", color)
        _themeColor.value = color
    }
}
