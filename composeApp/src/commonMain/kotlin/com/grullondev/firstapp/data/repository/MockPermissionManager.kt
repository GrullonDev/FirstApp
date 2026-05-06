package com.grullondev.firstapp.data.repository

import com.grullondev.firstapp.domain.model.Permission
import com.grullondev.firstapp.domain.model.PermissionState
import com.grullondev.firstapp.domain.repository.PermissionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MockPermissionManager : PermissionManager {
    private val states = Permission.entries.associateWith { 
        MutableStateFlow(PermissionState.NOT_DETERMINED) 
    }

    override fun getPermissionState(permission: Permission): StateFlow<PermissionState> {
        return states[permission]?.asStateFlow() ?: MutableStateFlow(PermissionState.DENIED).asStateFlow()
    }

    override suspend fun requestPermission(permission: Permission): PermissionState {
        // En una app real, aquí se invocaría el diálogo del sistema
        // Para este clon, simularemos que el usuario acepta
        states[permission]?.value = PermissionState.GRANTED
        return PermissionState.GRANTED
    }
}
