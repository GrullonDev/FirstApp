package com.grullondev.firstapp.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grullondev.firstapp.presentation.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val themeColor by viewModel.themeColor.collectAsState()
    val isLiquidGlass by viewModel.isLiquidGlassEnabled.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item { ProfileSection(themeColor) }
        item { SettingsCategoryTitle("Apariencia", themeColor) }
        item {
            PersonalizationSection(
                themeColor = themeColor,
                isLiquidGlass = isLiquidGlass,
                isDarkMode = isDarkMode ?: false,
                onColorSelected = { viewModel.updateThemeColor(it) },
                onToggleLiquidGlass = { viewModel.toggleLiquidGlass() },
                onToggleDarkMode = { viewModel.toggleDarkMode() }
            )
        }
        item { SettingsCategoryTitle("Privacidad", themeColor) }
        item { SettingsActionMenuItem("Cuenta", "Seguridad, cambiar número", "🔒") {} }
        item { SettingsActionMenuItem("Privacidad", "Bloqueo de pantalla", "👁️") {} }
        item { SettingsCategoryTitle("Ayuda", themeColor) }
        item { SettingsActionMenuItem("Ayuda", "Centro de ayuda, contáctanos", "❓") {} }
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun ProfileSection(themeColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(64.dp).clip(CircleShape).background(themeColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text("TU", style = MaterialTheme.typography.headlineSmall, color = themeColor, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text("Tú (Nombre de Usuario)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Disponible", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SettingsCategoryTitle(title: String, themeColor: Color) {
    Text(
        text = title,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
        style = MaterialTheme.typography.labelLarge,
        color = themeColor,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun SettingsActionMenuItem(title: String, subtitle: String, icon: String, onClick: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth().clickable { onClick() }) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 24.sp)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun SettingsToggleMenuItem(title: String, subtitle: String, icon: String, checked: Boolean, onToggle: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 24.sp)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = checked, onCheckedChange = { onToggle() })
        }
    }
}

@Composable
private fun PersonalizationSection(
    themeColor: Color,
    isLiquidGlass: Boolean,
    isDarkMode: Boolean,
    onColorSelected: (Color) -> Unit,
    onToggleLiquidGlass: () -> Unit,
    onToggleDarkMode: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        SettingsToggleMenuItem("Modo Oscuro", "Tema visual de la aplicación", "🌙", isDarkMode, onToggleDarkMode)
        SettingsToggleMenuItem("Efecto Liquid Glass", "Gradientes y transparencias", "✨", isLiquidGlass, onToggleLiquidGlass)
        
        Spacer(modifier = Modifier.height(8.dp))
        Text("Color del Tema", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        
        val colors = listOf(
            Color(0xFF008069), Color(0xFF2196F3), Color(0xFFFF9800),
            Color(0xFF4CAF50), Color(0xFFE91E63), Color(0xFF9C27B0),
            Color(0xFF795548), Color(0xFF607D8B)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            colors.forEach { color ->
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(color)
                        .clickable { onColorSelected(color) }
                        .let {
                            if (color == themeColor) it.border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape).padding(4.dp)
                            else it
                        }
                )
            }
        }
    }
}
