package com.grullondev.firstapp.presentation.ui

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(viewModel: ChatViewModel) {
    val chats by viewModel.chats.collectAsState()
    val themeColor by viewModel.themeColor.collectAsState()
    val isLiquidGlassEnabled by viewModel.isLiquidGlassEnabled.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    
    var showMenu by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showCameraSim by remember { mutableStateOf(false) }

    val filteredChats = if (searchQuery.isEmpty()) chats else {
        chats.filter { it.name.contains(searchQuery, ignoreCase = true) || it.lastMessage.contains(searchQuery, ignoreCase = true) }
    }

    if (showCameraSim) {
        CameraSimulation(
            onClose = { showCameraSim = false },
            onCapture = { showCameraSim = false },
            themeColor = themeColor
        )
    } else {
        Scaffold(
            topBar = {
                Column(modifier = Modifier.background(if (isLiquidGlassEnabled) themeColor.copy(alpha = 0.8f) else themeColor)) {
                    TopAppBar(
                        title = { Text("Clone WhatsApp", fontWeight = FontWeight.Bold) },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            titleContentColor = Color.White
                        ),
                        actions = {
                            IconButton(onClick = { showCameraSim = true }) {
                                Text("📷", fontSize = 20.sp, color = Color.White)
                            }
                            Box {
                                IconButton(onClick = { showMenu = true }) {
                                    Text("⋮", fontSize = 24.sp, color = Color.White)
                                }
                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Nueva Comunidad") },
                                        onClick = { showMenu = false }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Destacado") },
                                        onClick = { showMenu = false }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Marcar todas como leidas") },
                                        onClick = { 
                                            viewModel.markAllAsRead()
                                            showMenu = false 
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Ajustes") },
                                        onClick = { 
                                            viewModel.onTabSelected(4) 
                                            showMenu = false 
                                        }
                                    )
                                }
                            }
                        }
                    )
                    
                    if (selectedTab == 3) {
                        SearchBarBelowAppBar(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            themeColor = themeColor
                        )
                    }
                }
            },
            bottomBar = {
                WhatsAppBottomNavigation(
                    selectedTab = selectedTab,
                    onTabSelected = { viewModel.onTabSelected(it) },
                    themeColor = themeColor,
                    isLiquidGlass = isLiquidGlassEnabled
                )
            },
            floatingActionButton = {
                if (selectedTab == 3) {
                    FloatingActionButton(
                        onClick = { /* New Chat */ },
                        containerColor = if (isLiquidGlassEnabled) Color.White.copy(alpha = 0.5f) else Color(0xFF00A884),
                        contentColor = if (isLiquidGlassEnabled) Color.Black else Color.White,
                        shape = CircleShape
                    ) {
                        Text("💬", fontSize = 24.sp)
                    }
                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                if (isLiquidGlassEnabled) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(themeColor.copy(alpha = 0.3f), Color.White)
                                )
                            )
                    )
                }
                
                when (selectedTab) {
                    3 -> { // Chats Tab
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(if (isLiquidGlassEnabled) Color.Transparent else Color.White)
                        ) {
                            items(filteredChats, key = { it.id }) { chat ->
                                ChatItem(
                                    chat = chat, 
                                    onClick = { viewModel.onChatSelected(chat.id) },
                                    isLiquidGlass = isLiquidGlassEnabled
                                )
                            }
                        }
                    }
                    4 -> { // Ajustes Tab
                        SettingsTabContent(viewModel = viewModel)
                    }
                    else -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Próximamente", color = Color.Gray)
                        }
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
        color = Color.White.copy(alpha = 0.2f),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🔍", fontSize = 16.sp, color = Color.White.copy(alpha = 0.7f))
            TextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Buscar...", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = true
            )
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Text("✕", fontSize = 16.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun SettingsTabContent(viewModel: ChatViewModel) {
    val themeColor by viewModel.themeColor.collectAsState()
    val isLiquidGlassEnabled by viewModel.isLiquidGlassEnabled.collectAsState()

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
                onColorSelected = { viewModel.updateThemeColor(it) },
                onToggleLiquidGlass = { viewModel.toggleLiquidGlass() }
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
        color = Color.White.copy(alpha = 0.1f),
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(64.dp).clip(CircleShape).background(themeColor), contentAlignment = Alignment.Center) {
                Text("JD", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("Julián Grullón", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("+1 829 123 4567", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
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
                Text(title, style = MaterialTheme.typography.bodyLarge, color = themeColor ?: Color.Unspecified)
                if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Text("›", fontSize = 24.sp, color = Color.LightGray)
        }
    }
}

@Composable
fun PersonalizationSection(themeColor: Color, isLiquidGlassEnabled: Boolean, onColorSelected: (Color) -> Unit, onToggleLiquidGlass: () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.5f))) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Color de acento", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    val colors = listOf(Color(0xFF008069), Color(0xFF2196F3), Color(0xFFE91E63), Color(0xFF9C27B0), Color(0xFF607D8B))
                    colors.forEach { color ->
                        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(color).clickable { onColorSelected(color) }.let { 
                            if (themeColor == color) it.background(color).padding(4.dp).background(Color.White, CircleShape).padding(2.dp).background(color, CircleShape) else it
                        })
                    }
                }
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Efecto Liquid Glass", style = MaterialTheme.typography.titleMedium)
                        Text("Transparencias inmersivas", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                    Switch(checked = isLiquidGlassEnabled, onCheckedChange = { onToggleLiquidGlass() }, colors = SwitchDefaults.colors(checkedThumbColor = themeColor, checkedTrackColor = themeColor.copy(alpha = 0.5f)))
                }
            }
        }
    }
}

@Composable
fun WhatsAppBottomNavigation(selectedTab: Int, onTabSelected: (Int) -> Unit, themeColor: Color, isLiquidGlass: Boolean) {
    NavigationBar(containerColor = if (isLiquidGlass) Color.White.copy(alpha = 0.4f) else Color.White, contentColor = Color.Gray, tonalElevation = if (isLiquidGlass) 0.dp else 8.dp) {
        val items = listOf("Novedades" to "⭕", "Llamadas" to "📞", "Comunidades" to "👥", "Chats" to "💬", "Ajustes" to "⚙️")
        items.forEachIndexed { index, item ->
            NavigationBarItem(selected = selectedTab == index, onClick = { onTabSelected(index) }, icon = { Text(item.second, fontSize = 20.sp) }, label = { Text(item.first, fontSize = 10.sp) }, colors = NavigationBarItemDefaults.colors(selectedIconColor = themeColor, selectedTextColor = themeColor, unselectedIconColor = Color.Gray, unselectedTextColor = Color.Gray, indicatorColor = Color.Transparent))
        }
    }
}

@Composable
fun ChatItem(chat: Chat, onClick: () -> Unit, isLiquidGlass: Boolean = false) {
    Surface(color = if (isLiquidGlass) Color.White.copy(alpha = 0.3f) else Color.Transparent, modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 8.dp, vertical = 4.dp).let { if (isLiquidGlass) it.clip(RoundedCornerShape(12.dp)) else it }, shadowElevation = if (isLiquidGlass) 1.dp else 0.dp) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(52.dp).clip(CircleShape).background(when (chat.type) { ChatType.INDIVIDUAL -> Color.LightGray; ChatType.GROUP -> Color(0xFF2196F3).copy(alpha = 0.6f); ChatType.FAMILY -> Color(0xFFFF9800).copy(alpha = 0.6f); ChatType.WORK -> Color(0xFF4CAF50).copy(alpha = 0.6f); ChatType.TOPIC -> Color(0xFF9C27B0).copy(alpha = 0.6f) }), contentAlignment = Alignment.Center) {
                Text(text = when (chat.type) { ChatType.INDIVIDUAL -> chat.name.take(1); ChatType.GROUP -> "👥"; ChatType.FAMILY -> "🏠"; ChatType.WORK -> "💼"; ChatType.TOPIC -> "💡" }, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = chat.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        if (chat.type != ChatType.INDIVIDUAL) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Surface(color = Color.LightGray.copy(alpha = 0.3f), shape = RoundedCornerShape(4.dp)) {
                                Text(text = when (chat.type) { ChatType.GROUP -> "Grupo"; ChatType.FAMILY -> "Familia"; ChatType.WORK -> "Trabajo"; ChatType.TOPIC -> "Temas"; ChatType.INDIVIDUAL -> "" }, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            }
                        }
                    }
                    Text(text = chat.lastMessageTime, style = MaterialTheme.typography.bodySmall, color = if (chat.unreadCount > 0) Color(0xFF00A884) else Color.Gray)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = chat.lastMessage, style = MaterialTheme.typography.bodyMedium, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
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
