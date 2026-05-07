package com.grullondev.firstapp.presentation.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grullondev.firstapp.domain.model.Chat
import com.grullondev.firstapp.domain.model.ChatType
import com.grullondev.firstapp.presentation.viewmodel.ChatViewModel

@Composable
fun ChatListContent(
    viewModel: ChatViewModel,
    paddingValues: PaddingValues
) {
    val chats by viewModel.chats.collectAsState()
    val themeColor by viewModel.themeColor.collectAsState()
    val isLiquidGlassEnabled by viewModel.isLiquidGlassEnabled.collectAsState()
    val isDarkModeState by viewModel.isDarkMode.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isDarkMode = isDarkModeState ?: androidx.compose.foundation.isSystemInDarkTheme()
    
    var searchQuery by remember { mutableStateOf("") }
    val filteredChats = if (searchQuery.isEmpty()) chats else {
        chats.filter { it.name.contains(searchQuery, ignoreCase = true) || it.lastMessage.contains(searchQuery, ignoreCase = true) }
    }

    Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
        if (isLiquidGlassEnabled) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                themeColor.copy(alpha = if (isDarkMode) 0.1f else 0.3f),
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
            )
        }
        
        Column {
            SearchBarBelowAppBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                themeColor = themeColor
            )
            
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (isLiquidGlassEnabled) Color.Transparent else MaterialTheme.colorScheme.background)
            ) {
                if (isLoading) {
                    items(8) {
                        SkeletonChatItem(isLiquidGlass = isLiquidGlassEnabled)
                    }
                } else {
                    items(filteredChats, key = { it.id }) { chat ->
                        ChatItem(
                            chat = chat, 
                            onClick = { viewModel.onChatSelected(chat.id) },
                            isLiquidGlass = isLiquidGlassEnabled
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SearchBarBelowAppBar(
    query: String,
    onQueryChange: (String) -> Unit,
    themeColor: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🔍", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            TextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Buscar...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), fontSize = 14.sp) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = themeColor,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                ),
                singleLine = true
            )
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Text("✕", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun SettingsTabContent(viewModel: ChatViewModel) {
    val themeColor by viewModel.themeColor.collectAsState()
    val isLiquidGlassEnabled by viewModel.isLiquidGlassEnabled.collectAsState()
    val isDarkModeState by viewModel.isDarkMode.collectAsState()
    val isDarkMode = isDarkModeState ?: androidx.compose.foundation.isSystemInDarkTheme()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item { ProfileSection(themeColor = themeColor) }
        item {
            SettingsCategoryTitle("Ajustes de chat")
            PersonalizationSection(
                themeColor = themeColor,
                isLiquidGlassEnabled = isLiquidGlassEnabled,
                isDarkMode = isDarkMode,
                onColorSelected = { viewModel.updateThemeColor(it) },
                onToggleLiquidGlass = { viewModel.toggleLiquidGlass() },
                onToggleDarkMode = { viewModel.toggleDarkMode() }
            )
        }
        item {
            SettingsCategoryTitle("Ajustes")
            SettingsMenuItem(icon = "🔔", title = "Notificaciones y sonidos", subtitle = "Activado")
            SettingsMenuItem(icon = "🔐", title = "Privacidad y seguridad", subtitle = "Dos pasos, bloqueos")
            SettingsMenuItem(icon = "📊", title = "Datos y almacenamiento", subtitle = "Uso de red")
            SettingsMenuItem(icon = "📂", title = "Carpetas de chats", subtitle = "Personaliza tus pestañas")
            SettingsMenuItem(icon = "🌐", title = "Idioma", subtitle = "Español")
        }
        item {
            SettingsCategoryTitle("Ayuda")
            SettingsMenuItem(icon = "❓", title = "Preguntas frecuentes")
            SettingsMenuItem(icon = "📧", title = "Soporte técnico")
            SettingsMenuItem(icon = "🌟", title = "Clone Premium", themeColor = themeColor)
        }
    }
}

@Composable
fun ProfileSection(themeColor: Color) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(64.dp).clip(CircleShape).background(themeColor), contentAlignment = Alignment.Center) {
                Text("JD", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("Julián Grullón", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text("+1 829 123 4567", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("@jgrullon", style = MaterialTheme.typography.bodySmall, color = themeColor)
            }
        }
    }
}

@Composable
fun SettingsCategoryTitle(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
        style = MaterialTheme.typography.labelLarge,
        color = Color(0xFF2196F3),
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun SettingsMenuItem(icon: String, title: String, subtitle: String? = null, themeColor: Color? = null) {
    Surface(modifier = Modifier.fillMaxWidth(), color = Color.Transparent, onClick = { }) {
        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 24.sp)
            Spacer(modifier = Modifier.width(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, color = themeColor ?: MaterialTheme.colorScheme.onSurface)
                if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("›", fontSize = 24.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
        }
    }
}

@Composable
fun PersonalizationSection(
    themeColor: Color, 
    isLiquidGlassEnabled: Boolean, 
    isDarkMode: Boolean,
    onColorSelected: (Color) -> Unit, 
    onToggleLiquidGlass: () -> Unit,
    onToggleDarkMode: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(), 
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Color de acento", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    val colors = listOf(Color(0xFF008069), Color(0xFF2196F3), Color(0xFFE91E63), Color(0xFF9C27B0), Color(0xFF607D8B))
                    colors.forEach { color ->
                        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(color).clickable { onColorSelected(color) }.let { 
                            if (themeColor == color) it.background(color).padding(4.dp).background(if (isDarkMode) Color.Black else Color.White, CircleShape).padding(2.dp).background(color, CircleShape) else it
                        })
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Modo Oscuro", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        Text("Cambiar tema de la aplicación", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = isDarkMode, onCheckedChange = { onToggleDarkMode() }, colors = SwitchDefaults.colors(checkedThumbColor = themeColor, checkedTrackColor = themeColor.copy(alpha = 0.5f)))
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Efecto Liquid Glass", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        Text("Transparencias inmersivas", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = isLiquidGlassEnabled, onCheckedChange = { onToggleLiquidGlass() }, colors = SwitchDefaults.colors(checkedThumbColor = themeColor, checkedTrackColor = themeColor.copy(alpha = 0.5f)))
                }
            }
        }
    }
}

@Composable
fun SkeletonChatItem(isLiquidGlass: Boolean) {
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        )
    )

    Surface(
        color = if (isLiquidGlass) Color.White.copy(alpha = 0.1f * alpha) else Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .let { if (isLiquidGlass) it.clip(RoundedCornerShape(12.dp)) else it }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray.copy(alpha = alpha))
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(14.dp)
                        .background(Color.LightGray.copy(alpha = alpha), RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(12.dp)
                        .background(Color.LightGray.copy(alpha = alpha), RoundedCornerShape(4.dp))
                )
            }
        }
    }
}

@Composable
fun ChatItem(chat: Chat, onClick: () -> Unit, isLiquidGlass: Boolean = false) {
    Surface(
        color = if (isLiquidGlass) MaterialTheme.colorScheme.surface.copy(alpha = 0.3f) else Color.Transparent, 
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 8.dp, vertical = 4.dp).let { if (isLiquidGlass) it.clip(RoundedCornerShape(12.dp)) else it }, 
        shadowElevation = if (isLiquidGlass) 1.dp else 0.dp
    ) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(52.dp).clip(CircleShape).background(when (chat.type) { ChatType.INDIVIDUAL -> Color.LightGray; ChatType.GROUP -> Color(0xFF2196F3).copy(alpha = 0.6f); ChatType.FAMILY -> Color(0xFFFF9800).copy(alpha = 0.6f); ChatType.WORK -> Color(0xFF4CAF50).copy(alpha = 0.6f); ChatType.TOPIC -> Color(0xFF9C27B0).copy(alpha = 0.6f) }), contentAlignment = Alignment.Center) {
                Text(text = when (chat.type) { ChatType.INDIVIDUAL -> chat.name.take(1); ChatType.GROUP -> "👥"; ChatType.FAMILY -> "🏠"; ChatType.WORK -> "💼"; ChatType.TOPIC -> "💡" }, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = chat.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurface)
                        if (chat.type != ChatType.INDIVIDUAL) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Surface(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp)) {
                                Text(text = when (chat.type) { ChatType.GROUP -> "Grupo"; ChatType.FAMILY -> "Familia"; ChatType.WORK -> "Trabajo"; ChatType.TOPIC -> "Temas"; ChatType.INDIVIDUAL -> "" }, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    Text(text = chat.lastMessageTime, style = MaterialTheme.typography.bodySmall, color = if (chat.unreadCount > 0) Color(0xFF00A884) else MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = chat.lastMessage, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    if (chat.unreadCount > 0) {
                        Box(modifier = Modifier.size(22.dp).clip(CircleShape).background(Color(0xFF00A884)), contentAlignment = Alignment.Center) {
                            Text(text = chat.unreadCount.toString(), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
