package com.grullondev.firstapp.presentation.ui

import androidx.compose.runtime.Composable

@Composable
actual fun CommonBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // iOS no tiene botón físico de back; mantenemos no-op.
}
