package ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import ru.bpo.norn.jwmMain.screens.topnavbarscreens.DestinationScreen

import ru.bpo.norn.jwmMain.viewmodel.NornViewModel
import viewmodel.Screen

/**
 * Главный экран приложения с навигацией и контентом
 * Управляет отображением различных экранов через верхнюю и нижнюю панели навигации
 * @param viewModel ViewModel приложения для управления состоянием
 */
@Composable
fun MainScreen(viewModel: NornViewModel) {
    // Подписка на текущий экран из ViewModel
    val currentScreen by viewModel.currentScreen.collectAsState()

    // Основная структура экрана с панелями навигации
    Scaffold(
        bottomBar = {
            // Нижняя панель навигации (Предприятия, Студенты, Ведомости, Настройки)
            BottomNavBar(
                currentScreen = currentScreen,
                onNavigationClick = { screen ->
                    viewModel.navigateTo(screen)
                }
            )
        },
        topBar = {
            // Верхняя панель навигации (Информация, Отчеты, Направления, Приказы)
            TopNavBar(
                currentScreen = currentScreen,
                onNavigationClick = { screen ->
                    viewModel.navigateTo(screen)
                }
            )
        },
        content = { paddingValues ->
            // Основной контент с учетом отступов от панелей навигации
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Маршрутизация - отображение нужного экрана в зависимости от выбранного
                when (currentScreen) {
                    is Screen.Screen1 -> InfoScreen(viewModel = viewModel)           // Информация о практике
                    is Screen.Screen2 -> SummaryReport(viewModel = viewModel)       // Сводный отчет
                    is Screen.Screen3 -> DestinationScreen(viewModel = viewModel)   // Направления на практику
                    is Screen.Screen4 -> OrderScreen(viewModel = viewModel)         // Приказы по практике
                    is Screen.Screen5 -> Enterprises(viewModel = viewModel)         // Управление предприятиями
                    is Screen.Screen6 -> StudentsListScreen(viewModel = viewModel)  // Списки студентов
                    is Screen.Screen7 -> StatementsScreen(viewModel = viewModel)    // Ведомости (в разработке)
                    is Screen.Screen8 -> SettingsScreen(viewModel = viewModel)      // Настройки приложения
                }
            }
        }
    )
}