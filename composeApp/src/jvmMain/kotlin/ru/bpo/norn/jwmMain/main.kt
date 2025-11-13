package ru.bpo.norn.jwmMain

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import ru.bpo.norn.jwmMain.screens.MainScreen
import ru.bpo.norn.jwmMain.viewmodel.NornViewModel

private val DarkColorScheme = darkColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFFBB86FC),
    secondary = androidx.compose.ui.graphics.Color(0xFF03DAC6),
    background = androidx.compose.ui.graphics.Color(0xFF121212)
)

private val LightColorScheme = lightColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF6200EE),
    secondary = androidx.compose.ui.graphics.Color(0xFF03DAC6),
    background = androidx.compose.ui.graphics.Color(0xFFFFFFFF)
)

fun main() = application {
    val viewModel = remember { NornViewModel() }
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()

    Window(
        onCloseRequest = {
            // Сохраняем настройки перед закрытием приложения
            println("💾 Сохранение настроек перед выходом...")
            viewModel.saveSettingsOnExit()
            exitApplication()
        },
        title = "Norn - Система управления практикой студентов"
    ) {
        MaterialTheme(
            colorScheme = if (isDarkTheme) DarkColorScheme else LightColorScheme
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}