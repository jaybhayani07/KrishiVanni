package com.example.krishivani

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.example.krishivani.ui.theme.KrishiVaniTheme

import com.example.krishivani.ui.screens.HomeScreen
import com.example.krishivani.ui.screens.MarketScreen
import com.example.krishivani.ui.screens.CameraScreen
import com.example.krishivani.ui.screens.AdvisoryScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KrishiVaniTheme {
                val currentDensity = LocalDensity.current
                val clampedFontScale = currentDensity.fontScale.coerceIn(0.85f, 1.15f)
                CompositionLocalProvider(
                    LocalDensity provides Density(
                        density = currentDensity.density,
                        fontScale = clampedFontScale
                    )
                ) {
                    var currentRoute by remember { mutableStateOf("home") }
                    when (currentRoute) {
                        "home" -> HomeScreen(onNavigate = { currentRoute = it })
                        "market" -> MarketScreen(onNavigate = { currentRoute = it })
                        "camera" -> CameraScreen(onNavigate = { currentRoute = it })
                        "advisory" -> AdvisoryScreen(onNavigate = { currentRoute = it })
                        else -> HomeScreen(onNavigate = { currentRoute = it })
                    }
                }
            }
        }
    }
}