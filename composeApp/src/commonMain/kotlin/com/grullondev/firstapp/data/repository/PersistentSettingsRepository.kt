package com.grullondev.firstapp.data.repository

import com.grullondev.firstapp.domain.repository.SettingsRepository
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PersistentSettingsRepository(private val settings: Settings = Settings()) : SettingsRepository {
    private companion object {
        const val KEY_DARK_MODE = "is_dark_mode"
        const val KEY_LIQUID_GLASS = "is_liquid_glass"
        const val KEY_THEME_COLOR = "theme_color"
        const val DEFAULT_THEME_COLOR = 0xFF008069.toLong()
    }
    
    private val _isDarkMode = MutableStateFlow<Boolean?>(
        if (settings.hasKey(KEY_DARK_MODE)) settings.getBoolean(KEY_DARK_MODE, false) else null
    )
    private val _isLiquidGlass = MutableStateFlow(settings.getBoolean(KEY_LIQUID_GLASS, false))
    private val _themeColor = MutableStateFlow(settings.getLong(KEY_THEME_COLOR, DEFAULT_THEME_COLOR))

    override fun isDarkMode(): StateFlow<Boolean?> = _isDarkMode.asStateFlow()
    override fun isLiquidGlassEnabled(): StateFlow<Boolean> = _isLiquidGlass.asStateFlow()
    override fun getThemeColor(): StateFlow<Long> = _themeColor.asStateFlow()

    override suspend fun setDarkMode(enabled: Boolean?) {
        if (enabled == null) {
            settings.remove(KEY_DARK_MODE)
        } else {
            settings.putBoolean(KEY_DARK_MODE, enabled)
        }
        _isDarkMode.value = enabled
    }

    override suspend fun setLiquidGlassEnabled(enabled: Boolean) {
        settings.putBoolean(KEY_LIQUID_GLASS, enabled)
        _isLiquidGlass.value = enabled
    }

    override suspend fun setThemeColor(color: Long) {
        settings.putLong(KEY_THEME_COLOR, color)
        _themeColor.value = color
    }
}
