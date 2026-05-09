@file:OptIn(ExperimentalMaterial3Api::class)

package com.grullondev.firstapp.presentation.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grullondev.firstapp.domain.model.Chat
import com.grullondev.firstapp.domain.model.ChatType
import com.grullondev.firstapp.domain.model.MessageType
import com.grullondev.firstapp.presentation.viewmodel.ChatViewModel
import kotlin.math.absoluteValue

private fun getAvatarColor(name: String): Color {
    val avatarColors = listOf(
        Color(0xFFEF5350), Color(0xFFEC407A), Color(0xFFAB47BC), Color(0xFF7E57C2),
        Color(0xFF5C6BC0), Color(0xFF42A5F5), Color(0xFF29B6F6), Color(0xFF26C6DA),
        Color(0xFF26A69A), Color(0xFF66BB6A), Color(0xFF9CCC65), Color(0xFFD4E157),
        Color(0xFFFFEE58), Color(0xFFFFCA28), Color(0xFFFFA726), Color(0xFFFF7043)
    )
    return avatarColors[name.hashCode().absoluteValue % avatarColors.size]
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(viewModel: ChatViewModel) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val themeColor by viewModel.themeColor.collectAsState()
    
    val tabs = listOf(
        Triple("Llamadas", Icons.Outlined.Call, Icons.Default.Call),
        Triple("Chats", Icons.Outlined.Chat, Icons.Default.Chat),
        Triple("Ajustes", Icons.Outlined.Settings, Icons.Default.Settings)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Clone WhatsApp",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = themeColor,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { viewModel.onTabSelected(index) },
                        icon = { 
                            Icon(
                                if (selectedTab == index) tab.third else tab.second,
                                contentDescription = tab.first
                            )
                        },
                        label = { Text(tab.first) },
                        alwaysShowLabel = true
                    )
                }
            }
        },
        floatingActionButton = {
            when (selectedTab) {
                0 -> { // Calls tab
                    FloatingActionButton(
                        onClick = { /* TODO: New Call */ },
                        containerColor = themeColor,
                        contentColor = Color.White,
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.Call, contentDescription = "Nueva Llamada")
                    }
                }
                1 -> { // Chats tab
                    FloatingActionButton(
                        onClick = { /* TODO: New Chat */ },
                        containerColor = themeColor,
                        contentColor = Color.White,
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Nuevo Chat")
                    }
                }
            }
        }
    ) { paddingValues ->
        when (selectedTab) {
            0 -> Box(modifier = Modifier.padding(paddingValues)) {
                CallsScreen(themeColor = themeColor)
            }
            2 -> Box(modifier = Modifier.padding(paddingValues)) {
                SettingsTabContent(viewModel)
            }
            else -> ChatListContent(viewModel = viewModel, paddingValues = paddingValues)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
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
    val onlyUnread by viewModel.onlyUnread.collectAsState()
    val isDarkMode = isDarkModeState ?: androidx.compose.foundation.isSystemInDarkTheme()

    var searchQuery by remember { mutableStateOf("") }
    val filteredChats = chats.filter { 
        val matchesSearch = it.name.contains(searchQuery, ignoreCase = true) || 
                          it.lastMessage.contains(searchQuery, ignoreCase = true)
        val matchesUnread = !onlyUnread || it.unreadCount > 0
        matchesSearch && matchesUnread
    }

    val pinnedChats = filteredChats.filter { it.isPinned }
    val unpinnedChats = filteredChats.filter { !it.isPinned }
    val groupedChats = unpinnedChats.groupBy { chat ->
        when {
            chat.lastMessageTime.contains("AM") || chat.lastMessageTime.contains("PM") -> "Hoy"
            chat.lastMessageTime == "Ayer" -> "Ayer"
            else -> "Esta semana"
        }
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
                themeColor = themeColor,
                onlyUnread = onlyUnread,
                onToggleUnread = { viewModel.toggleUnreadFilter() }
            )
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                if (isLoading) {
                    items(8) {
                        SkeletonChatItem(isLiquidGlass = isLiquidGlassEnabled)
                    }
                } else if (filteredChats.isEmpty()) {
                    item {
                        EmptyChatsState(searchQuery, themeColor)
                    }
                } else {
                    if (searchQuery.isEmpty()) {
                        val favorites = chats.filter { it.type == ChatType.INDIVIDUAL }.take(5)
                        if (favorites.isNotEmpty()) {
                            item {
                                QuickAccessRow(
                                    chats = favorites,
                                    onChatClick = { viewModel.onChatSelected(it) }
                                )
                            }
                        }
                    }

                    // Seccion de Chats Fijados
                    if (pinnedChats.isNotEmpty()) {
                        stickyHeader {
                            HeaderSection("Fijados", themeColor)
                        }
                        items(pinnedChats, key = { "pinned_${it.id}" }) { chat ->
                            ChatItem(
                                chat = chat,
                                onClick = { viewModel.onChatSelected(chat.id) },
                                onLongClick = { viewModel.togglePin(chat.id) },
                                isLiquidGlass = isLiquidGlassEnabled,
                                themeColor = themeColor,
                                showPin = true,
                                modifier = Modifier.animateItem()
                            )
                        }
                    }

                    // Secciones por fecha
                    listOf("Hoy", "Ayer", "Esta semana").forEach { day ->
                        val dayChats = groupedChats[day]
                        if (!dayChats.isNullOrEmpty()) {
                            stickyHeader {
                                HeaderSection(day, themeColor)
                            }
                            items(dayChats, key = { it.id }) { chat ->
                                ChatItem(
                                    chat = chat,
                                    onClick = { viewModel.onChatSelected(chat.id) },
                                    onLongClick = { viewModel.togglePin(chat.id) },
                                    isLiquidGlass = isLiquidGlassEnabled,
                                    themeColor = themeColor,
                                    modifier = Modifier.animateItem()
                                )
                                if (!isLiquidGlassEnabled) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(start = 80.dp, end = 16.dp),
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyChatsState(searchQuery: String, themeColor: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 100.dp, start = 32.dp, end = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(themeColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text(if (searchQuery.isEmpty()) "💬" else "🔍", fontSize = 60.sp)
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = if (searchQuery.isEmpty()) "¡Tu bandeja está tranquila!" else "No encontramos nada para \"$searchQuery\"",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (searchQuery.isEmpty()) 
                "Inicia una nueva conversación y conecta con el mundo hoy." 
            else "Intenta con otras palabras clave o revisa la ortografía.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun HeaderSection(title: String, themeColor: Color) {
    Surface(
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.95f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = themeColor,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun ContactProfileScreen(chat: Chat, themeColor: Color, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Info. del contacto", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = themeColor)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Box(
                modifier = Modifier.size(120.dp).clip(CircleShape).background(getAvatarColor(chat.name)),
                contentAlignment = Alignment.Center
            ) {
                Text(chat.name.take(1), fontSize = 48.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(chat.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("+1 809 555 0123", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            
            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                ProfileActionButton(Icons.Default.Call, "Llamar", themeColor)
                ProfileActionButton(Icons.Default.VideoCall, "Video", themeColor)
                ProfileActionButton(Icons.Default.Search, "Buscar", themeColor)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
            
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text("Archivos, enlaces y documentos", style = MaterialTheme.typography.labelLarge, color = themeColor)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(3) {
                        Box(modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)).background(Color.LightGray))
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileActionButton(icon: ImageVector, label: String, themeColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = themeColor)
        Text(label, style = MaterialTheme.typography.labelSmall, color = themeColor)
    }
}

@Composable
fun QuickAccessRow(
    chats: List<Chat>,
    onChatClick: (String) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = "Acceso rápido",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp, bottom = 6.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(chats, key = { it.id }) { chat ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onChatClick(chat.id) }
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(getAvatarColor(chat.name)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = chat.name.take(1),
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (chat.unreadCount > 0) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFF3B30))
                                    .align(Alignment.TopEnd),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = chat.unreadCount.toString(),
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        // Indicador de presencia (punto verde)
                        if (chat.isOnline) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surface)
                                    .align(Alignment.BottomEnd),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF25D366))
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = chat.name.split(" ").first(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                }
            }
        }
        HorizontalDivider(
            modifier = Modifier.padding(top = 8.dp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f)
        )
    }
}

@Composable
fun SearchBarBelowAppBar(
    query: String,
    onQueryChange: (String) -> Unit,
    themeColor: Color,
    onlyUnread: Boolean,
    onToggleUnread: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                TextField(
                    value = query,
                    onValueChange = onQueryChange,
                    placeholder = { Text("Buscar...", style = MaterialTheme.typography.bodyMedium) },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    singleLine = true
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        FilterChip(
            selected = onlyUnread,
            onClick = onToggleUnread,
            label = { Text("No leídos", fontSize = 12.sp) },
            leadingIcon = {
                if (onlyUnread) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = themeColor.copy(alpha = 0.2f),
                selectedLabelColor = themeColor
            ),
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
fun SettingsTabContent(viewModel: ChatViewModel) {
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
fun ProfileSection(themeColor: Color) {
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
fun SettingsCategoryTitle(title: String, themeColor: Color) {
    Text(
        text = title,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
        style = MaterialTheme.typography.labelLarge,
        color = themeColor,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun SettingsActionMenuItem(title: String, subtitle: String, icon: String, onClick: () -> Unit) {
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
fun SettingsToggleMenuItem(title: String, subtitle: String, icon: String, checked: Boolean, onToggle: () -> Unit) {
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
fun PersonalizationSection(
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

    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(52.dp).clip(CircleShape).background(Color.LightGray.copy(alpha = alpha)))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Box(modifier = Modifier.fillMaxWidth(0.4f).height(16.dp).background(Color.LightGray.copy(alpha = alpha)))
            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth(0.8f).height(12.dp).background(Color.LightGray.copy(alpha = alpha)))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ChatItem(
    chat: Chat,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    isLiquidGlass: Boolean = false,
    themeColor: Color = Color(0xFF008069),
    showPin: Boolean = false,
    modifier: Modifier = Modifier
) {
    val dismissState = rememberSwipeToDismissBoxState()
    val haptic = LocalHapticFeedback.current

    SwipeToDismissBox(
        modifier = modifier.combinedClickable(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            },
            onLongClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onLongClick()
            }
        ),
        state = dismissState,
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val color = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Color(0xFFFFCC00) // Archivar (Amarillo)
                SwipeToDismissBoxValue.EndToStart -> Color(0xFFFF3B30) // Eliminar (Rojo)
                else -> Color.Transparent
            }
            val icon = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Icons.Default.Archive
                SwipeToDismissBoxValue.EndToStart -> Icons.Default.Delete
                else -> Icons.Default.Clear
            }
            val alignment = if (direction == SwipeToDismissBoxValue.StartToEnd) Alignment.CenterStart else Alignment.CenterEnd
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(horizontal = 24.dp),
                contentAlignment = alignment
            ) {
                Icon(icon, contentDescription = null, tint = Color.White)
            }
        }
    ) {
        Surface(
            color = if (isLiquidGlass)
                MaterialTheme.colorScheme.surface.copy(alpha = 0.45f)
            else
                MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .let {
                    if (isLiquidGlass) it.padding(horizontal = 8.dp, vertical = 4.dp).clip(RoundedCornerShape(12.dp))
                    else it
                },
            shadowElevation = 0.dp
        ) {
            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(52.dp).clip(CircleShape).background(
                        if (chat.type == ChatType.INDIVIDUAL) getAvatarColor(chat.name)
                        else when (chat.type) {
                            ChatType.GROUP -> Color(0xFF2196F3).copy(alpha = 0.6f)
                            ChatType.FAMILY -> Color(0xFFFF9800).copy(alpha = 0.6f)
                            ChatType.WORK -> Color(0xFF4CAF50).copy(alpha = 0.6f)
                            ChatType.TOPIC -> Color(0xFF9C27B0).copy(alpha = 0.6f)
                        }
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (chat.type) {
                            ChatType.INDIVIDUAL -> chat.name.take(1)
                            ChatType.GROUP -> "👥"
                            ChatType.FAMILY -> "🏠"
                            ChatType.WORK -> "💼"
                            ChatType.TOPIC -> "💡"
                        },
                        color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold
                    )
                    
                    if (showPin) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                                .align(Alignment.TopStart),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.PushPin,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = themeColor
                            )
                        }
                    }

                    // Indicador de presencia en la lista principal
                    if (chat.isOnline && chat.type == ChatType.INDIVIDUAL) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                                .align(Alignment.BottomEnd),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF25D366))
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = chat.name, style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold, maxLines = 1,
                                overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurface
                            )
                            if (chat.type != ChatType.INDIVIDUAL) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Surface(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp)) {
                                    Text(
                                        text = when (chat.type) {
                                            ChatType.GROUP -> "Grupo"; ChatType.FAMILY -> "Familia"
                                            ChatType.WORK -> "Trabajo"; ChatType.TOPIC -> "Temas"
                                            ChatType.INDIVIDUAL -> ""
                                        },
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        Text(
                            text = chat.lastMessageTime,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (chat.unreadCount > 0) themeColor else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            if (chat.typingStatus != null) {
                                Text(
                                    text = chat.typingStatus,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF25D366),
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            } else {
                                // Vista previa de multimedia
                                when (chat.lastMessageType) {
                                    MessageType.IMAGE -> {
                                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    MessageType.AUDIO -> {
                                        Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    MessageType.FILE -> {
                                        Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    else -> {}
                                }
                                Text(
                                    text = chat.lastMessage, style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        if (chat.unreadCount > 0) {
                            Box(modifier = Modifier.size(22.dp).clip(CircleShape).background(themeColor), contentAlignment = Alignment.Center) {
                                Text(text = chat.unreadCount.toString(), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}