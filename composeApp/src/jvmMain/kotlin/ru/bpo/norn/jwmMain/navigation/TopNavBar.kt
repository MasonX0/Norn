// ui/Navigation.kt
package ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import viewmodel.Screen

@Composable
fun TopNavBar(
    currentScreen: Screen,
    onNavigationClick: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 8.dp
    ) {
        // Кнопка экрана 1
        NavigationBarItem(
            icon = { Text("ℹ️", style = MaterialTheme.typography.titleMedium) },
            label = { Text("Загруженные данные") },
            selected = currentScreen is Screen.Screen1,
            onClick = { onNavigationClick(Screen.Screen1) },
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

        // Кнопка экрана 2
        NavigationBarItem(
            icon = { Text("📋", style = MaterialTheme.typography.titleMedium) },
            label = { Text("Сводный отчет") },
            selected = currentScreen is Screen.Screen2,
            onClick = { onNavigationClick(Screen.Screen2) },
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

        // Кнопка экрана 3
        NavigationBarItem(
            icon = { Text("🗺️", style = MaterialTheme.typography.titleMedium) },
            label = { Text("Направления") },
            selected = currentScreen is Screen.Screen3,
            onClick = { onNavigationClick(Screen.Screen3) },
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

        // Кнопка экрана 4
        NavigationBarItem(
            icon = { Text("📜", style = MaterialTheme.typography.titleMedium) },
            label = { Text("Приказ") },
            selected = currentScreen is Screen.Screen4,
            onClick = { onNavigationClick(Screen.Screen4) },
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