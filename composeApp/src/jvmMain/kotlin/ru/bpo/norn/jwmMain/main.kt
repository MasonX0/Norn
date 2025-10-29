package ru.bpo.norn.jwmMain

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import ru.bpo.norn.jwmMain.screens.MainScreen

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Norn",
    ) {
        MainScreen()
    }
}