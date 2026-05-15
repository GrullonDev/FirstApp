package com.grullondev.firstapp.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import com.grullondev.firstapp.di.initKoin
import org.koin.android.ext.koin.androidContext

class WearMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            WearApp()
        }
    }
}

@Composable
fun WearApp() {
    MaterialTheme {
        ScreenScaffold {
            ScalingLazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Text("FirstApp Wear OS")
                }
                item {
                    Text("Bienvenido al reloj")
                }
            }
        }
    }
}
