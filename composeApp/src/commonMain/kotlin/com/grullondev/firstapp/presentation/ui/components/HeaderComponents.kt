package com.grullondev.firstapp.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ChatHeader(name: String, status: String, contentColor: Color = Color.White, onClick: () -> Unit = {}) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(contentColor.copy(alpha = 0.25f)),
            contentAlignment = Alignment.Center
        ) {
            Text(name.take(1), color = contentColor, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(name, style = MaterialTheme.typography.titleMedium, color = contentColor)
            Text(status, style = MaterialTheme.typography.bodySmall, color = contentColor.copy(alpha = 0.8f))
        }
    }
}

@Composable
fun ReactionMenu(onDismiss: () -> Unit, onReaction: (String) -> Unit) {
    Box(modifier = Modifier.fillMaxSize().clickable { onDismiss() }) {
        Surface(
            modifier = Modifier.padding(8.dp).align(Alignment.TopCenter).offset(y = (-40).dp),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(24.dp),
            shadowElevation = 8.dp
        ) {
            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                listOf("❤️", "😂", "😮", "😢", "🙏", "👍").forEach { emoji ->
                    Text(
                        text = emoji,
                        modifier = Modifier
                            .clickable { onReaction(emoji) }
                            .padding(horizontal = 8.dp),
                        fontSize = 24.sp
                    )
                }
            }
        }
    }
}
