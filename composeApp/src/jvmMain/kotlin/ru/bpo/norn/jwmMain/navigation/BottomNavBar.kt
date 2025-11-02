// ui/Navigation.kt
package ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import viewmodel.Screen

@Composable
fun BottomNavBar(
    currentScreen: Screen,
    onNavigationClick: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
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
        NavigationBarItem(
            icon = { Text("📊", style = MaterialTheme.typography.titleMedium) },
            label = { Text("Ведомости") },
            selected = currentScreen is Screen.Screen7,
            onClick = { onNavigationClick(Screen.Screen7) },
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