package com.grullondev.firstapp.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grullondev.firstapp.domain.model.Chat
import com.grullondev.firstapp.presentation.ui.theme.avatarColorFromName

@OptIn(ExperimentalMaterial3Api::class)
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
                modifier = Modifier.size(120.dp).clip(CircleShape).background(avatarColorFromName(chat.name)),
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
private fun ProfileActionButton(icon: ImageVector, label: String, themeColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = themeColor)
        Text(label, style = MaterialTheme.typography.labelSmall, color = themeColor)
    }
}
