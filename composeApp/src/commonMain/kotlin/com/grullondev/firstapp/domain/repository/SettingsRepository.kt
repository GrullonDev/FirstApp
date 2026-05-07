package com.grullondev.firstapp.domain.repository

import kotlinx.coroutines.flow.StateFlow

interface SettingsRepository {
    fun isDarkMode(): StateFlow<Boolean?>
    fun isLiquidGlassEnabled(): StateFlow<Boolean>
    fun getThemeColor(): StateFlow<Long>
    
    suspend fun setDarkMode(enabled: Boolean?)
    suspend fun setLiquidGlassEnabled(enabled: Boolean)
    suspend fun setThemeColor(color: Long)
}
