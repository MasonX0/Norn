// ui/Navigation.kt
package ru.bpo.norn.jwmMain.navigation

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Компонент нижней панели навигации приложения
 *
 * Отвечает за навигацию между основными рабочими экранами приложения.
 * Содержит 4 кнопки для доступа к функциональным экранам управления данными.
 *
 * Архитектурные особенности:
 * - Использует Material Design 3 NavigationBar
 * - Адаптивная цветовая схема на основе темы
 * - Визуальная индикация текущего активного экрана
 * - Единообразный стиль для всех кнопок навигации
 *
 * @param currentScreen Текущий активный экран для подсветки соответствующей кнопки
 * @param onNavigationClick Callback функция, вызываемая при нажатии на кнопку навигации
 * @param modifier Модификатор для настройки внешнего вида компонента
 *
 * @see Screen.Screen5 Экран управления предприятиями
 * @see Screen.Screen6 Экран управления студентами
 * @see Screen.Screen7 Экран работы с ведомостями
 * @see Screen.Screen8 Экран настроек приложения
 */
@Composable
fun BottomNavBar(
    currentScreen: Screen,
    onNavigationClick: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    /**
     * Основной контейнер навигационной панели
     *
     * Настройки стиля:
     * - Черная рамка толщиной 1dp для четкого разделения
     * - Цвет фона из цветовой схемы темы (primaryContainer)
     * - Тональная высота 8dp для создания эффекта поднятия
     */
    NavigationBar(
        modifier = modifier.border(width = 1.dp, color = Color.Black),
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 8.dp
    ) {
        // Кнопка экрана 5
        NavigationBarItem(
            icon = { Text("🏭", style = MaterialTheme.typography.titleMedium) },
            label = { Text("Списки предприятий") },
            selected = currentScreen is Screen.Screen5,
            onClick = { onNavigationClick(Screen.Screen5) },
            colors = NavigationBarItemColors(
                // Цвет иконки ВЫБРАННОГО элемента - контрастный для читаемости
                selectedIconColor = MaterialTheme.colorScheme.onPrimary,

                // Цвет текста ВЫБРАННОГО элемента - основной цвет темы
                selectedTextColor = MaterialTheme.colorScheme.primary,

                // Цвет индикатора (подсветки) ВЫБРАННОГО элемента
                selectedIndicatorColor = MaterialTheme.colorScheme.primary,

                // Цвет иконки НЕВЫБРАННОГО элемента - полупрозрачный для снижения акцента
                unselectedIconColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.38f),

                // Цвет текста НЕВЫБРАННОГО элемента - полупрозрачный основной цвет
                unselectedTextColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.38f),

                // Цвет иконки ОТКЛЮЧЕННОГО элемента - максимально приглушенный
                disabledIconColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.38f),

                // Цвет текста ОТКЛЮЧЕННОГО элемента - максимально приглушенный
                disabledTextColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.38f),
            )
        )

        // Кнопка экрана 6
        NavigationBarItem(
            icon = { Text("👨‍🎓", style = MaterialTheme.typography.titleMedium) },
            label = { Text("Списки студентов") },
            selected = currentScreen is Screen.Screen6,
            onClick = { onNavigationClick(Screen.Screen6) },
            colors = NavigationBarItemColors(
                // Цвет иконки ВЫБРАННОГО элемента
                selectedIconColor = MaterialTheme.colorScheme.onPrimary,

                // Цвет текста ВЫБРАННОГО элемента
                selectedTextColor = MaterialTheme.colorScheme.primary,

                // Цвет индикатора (подсветки) ВЫБРАННОГО элемента
                selectedIndicatorColor = MaterialTheme.colorScheme.primary,

                // Цвет иконки НЕВЫБРАННОГО элемента
                unselectedIconColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.38f),

                // Цвет текста НЕВЫБРАННОГО элемента
                unselectedTextColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.38f),

                // Цвет иконки ОТКЛЮЧЕННОГО элемента
                disabledIconColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.38f),

                // Цвет текста ОТКЛЮЧЕННОГО элемента
                disabledTextColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.38f),
            )
        )

        // Кнопка экрана 7


        // Кнопка экрана 8
        NavigationBarItem(
            icon = { Text("⚙️", style = MaterialTheme.typography.titleMedium) },
            label = { Text("Настройки") },
            selected = currentScreen is Screen.Screen8,
            onClick = { onNavigationClick(Screen.Screen8) },
            colors = NavigationBarItemColors(
                // Цвет иконки ВЫБРАННОГО элемента
                selectedIconColor = MaterialTheme.colorScheme.onPrimary,

                // Цвет текста ВЫБРАННОГО элемента
                selectedTextColor = MaterialTheme.colorScheme.primary,

                // Цвет индикатора (подсветки) ВЫБРАННОГО элемента
                selectedIndicatorColor = MaterialTheme.colorScheme.primary,

                // Цвет иконки НЕВЫБРАННОГО элемента
                unselectedIconColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.38f),

                // Цвет текста НЕВЫБРАННОГО элемента
                unselectedTextColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.38f),

                // Цвет иконки ОТКЛЮЧЕННОГО элемента
                disabledIconColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.38f),

                // Цвет текста ОТКЛЮЧЕННОГО элемента
                disabledTextColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.38f),
            )
        )
    }
}