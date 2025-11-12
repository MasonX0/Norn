package ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import ru.bpo.norn.jwmMain.screens.topnavbarscreens.DestinationScreen

import ru.bpo.norn.jwmMain.viewmodel.NornViewModel
import viewmodel.Screen
@Composable
fun MainScreen(viewModel: NornViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    Scaffold(
        bottomBar = {
            BottomNavBar(
                currentScreen = currentScreen,
                onNavigationClick = { screen ->
                    viewModel.navigateTo(screen)
                }
            )
        },
        topBar = {
            TopNavBar(
                currentScreen = currentScreen,
                onNavigationClick = { screen ->
                    viewModel.navigateTo(screen)
                }
            )
        },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (currentScreen) {
                    is Screen.Screen1 -> InfoScreen(viewModel = viewModel)
                    is Screen.Screen2 -> SummaryReport(viewModel = viewModel)
                    is Screen.Screen3 -> DestinationScreen(viewModel = viewModel)
                    is Screen.Screen4 -> OrderScreen(viewModel = viewModel)
                    is Screen.Screen5 -> Enterprises(viewModel = viewModel)
                    is Screen.Screen6 -> StudentsListScreen(viewModel = viewModel)
                    is Screen.Screen7 -> StatementsScreen(viewModel = viewModel)
                    is Screen.Screen8 -> SettingsScreen(viewModel = viewModel)
                }
            }
        }
    )
}