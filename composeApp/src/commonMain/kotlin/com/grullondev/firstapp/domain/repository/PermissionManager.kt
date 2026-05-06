package com.grullondev.firstapp.domain.repository

import com.grullondev.firstapp.domain.model.Permission
import com.grullondev.firstapp.domain.model.PermissionState
import kotlinx.coroutines.flow.StateFlow

interface PermissionManager {
    fun getPermissionState(permission: Permission): StateFlow<PermissionState>
    suspend fun requestPermission(permission: Permission): PermissionState
}
