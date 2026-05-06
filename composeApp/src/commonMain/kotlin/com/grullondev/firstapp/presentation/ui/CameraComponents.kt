package com.grullondev.firstapp.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CameraSimulation(onClose: () -> Unit, onCapture: () -> Unit, themeColor: Color) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Simulación de Preview de Cámara
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 100.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("📷", fontSize = 80.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Cámara Activa", color = Color.White, style = MaterialTheme.typography.headlineMedium)
                Text("Simulación Premium", color = Color.White.copy(alpha = 0.6f))
            }
        }

        // Controles de Cámara
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Text("✕", color = Color.White, fontSize = 28.sp)
            }
            
            // Botón de Captura
            Surface(
                modifier = Modifier
                    .size(75.dp)
                    .clickable { onCapture() },
                shape = CircleShape,
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(5.dp, themeColor.copy(alpha = 0.5f))
            ) {
                Box(modifier = Modifier.padding(8.dp).background(Color.White, CircleShape))
            }

            IconButton(onClick = { /* Girar cámara */ }) {
                Text("🔄", color = Color.White, fontSize = 28.sp)
            }
        }
    }
}
