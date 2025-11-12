// ui/Navigation.kt
package ru.bpo.norn.jwmMain.navigation

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Компонент верхней панели навигации приложения
 *
 * Предоставляет доступ к аналитическим и административным функциям.
 * Содержит 4 кнопки для работы с отчетами, анализом данных и документооборотом.
 *
 * Архитектурные особенности:
 * - Использует Material Design 3 NavigationBar с адаптацией для верхнего размещения
 * - Цветовая схема синхронизирована с общей темой приложения
 * - Рамка в цвете основной темы (в отличие от черной рамки в BottomNavBar)
 * - Единообразная типографика и стиль с нижней панелью навигации
 *
 * @param currentScreen Текущий активный экран для визуальной индикации
 * @param onNavigationClick Callback функция для обработки навигационных переходов
 * @param modifier Модификатор для кастомизации внешнего вида
 *
 * @see Screen.Screen1 Экран информации о загруженных данных
 * @see Screen.Screen2 Экран сводных отчетов и аналитики
 * @see Screen.Screen3 Экран управления направлениями практики
 * @see Screen.Screen4 Экран формирования приказов
 */
@Composable
fun TopNavBar(
    currentScreen: Screen,
    onNavigationClick: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    /**
     * Основной контейнер верхней навигационной панели
     *
     * Стилевые отличия от BottomNavBar:
     * - Рамка в цвете основной темы (primary) вместо черного
     * - Идентичные цвет фона и тональная высота для консистентности
     * - Размещение в верхней части интерфейса над основным контентом
     */
    NavigationBar(
        modifier = modifier.border(width = 1.dp, color = MaterialTheme.colorScheme.primary),
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 8.dp
    ) {
        /**
         * Кнопка навигации "Загруженные данные" (Screen1)
         *
         * Информационный экран-дашборд для анализа загруженных данных:
         * - Отображение всех загруженных групп студентов
         * - Статистическая информация по каждой группе
         * - Возможность выбора конкретной группы для дальнейшей работы
         * - Кнопки быстрого доступа к операциям с данными группы
         * - Визуализация общей структуры данных в системе
         *
         * UI элементы:
         * - Иконка: ℹ️ (информационный символ)
         * - Текст: "Загруженные данные"
         * - Стандартная цветовая схема навигации
         */
        NavigationBarItem(
            icon = { Text("ℹ️", style = MaterialTheme.typography.titleMedium) },
            label = { Text("Загруженные данные") },
            selected = currentScreen is Screen.Screen1,
            onClick = { onNavigationClick(Screen.Screen1) },
            colors = NavigationBarItemColors(
                // Цвет иконки ВЫБРАННОГО элемента - высокий контраст для активного состояния
                selectedIconColor = MaterialTheme.colorScheme.onPrimary,

                // Цвет текста ВЫБРАННОГО элемента - основной акцентный цвет
                selectedTextColor = MaterialTheme.colorScheme.primary,

                // Цвет индикатора (подсветки) ВЫБРАННОГО элемента - подчеркивает активность
                selectedIndicatorColor = MaterialTheme.colorScheme.primary,

                // Цвет иконки НЕВЫБРАННОГО элемента - приглушенный для фонового восприятия
                unselectedIconColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.38f),

                // Цвет текста НЕВЫБРАННОГО элемента - полупрозрачный для иерархии
                unselectedTextColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.38f),

                // Цвет иконки ОТКЛЮЧЕННОГО элемента - минимальная видимость
                disabledIconColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.38f),

                // Цвет текста ОТКЛЮЧЕННОГО элемента - минимальная видимость  
                disabledTextColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.38f),
            )
        )

        /**
         * Кнопка навигации "Сводный отчет" (Screen2)
         *
         * Аналитический экран для генерации комплексных отчетов:
         * - Общая статистика по всем студентам в системе
         * - Анализ распределения студентов по предприятиям
         * - Сводные данные по направлениям и специальностям практики
         * - Графическая визуализация данных и трендов
         * - Экспорт отчетов в различные форматы (Excel, PDF, CSV)
         * - Настраиваемые фильтры и параметры отчетности
         *
         * UI элементы:
         * - Иконка: 📋 (символ отчета/документа)
         * - Текст: "Сводный отчет"
         * - Аналогичные цветовые настройки для консистентности
         */
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

        /**
         * Кнопка навигации "Направления" (Screen3)
         *
         * Административный экран для управления практикой:
         * - Создание и редактирование направлений на практику
         * - Назначение студентов на конкретные направления
         * - Связывание направлений с предприятиями-партнерами
         * - Контроль количества доступных мест на предприятиях
         * - Автоматическое распределение студентов по критериям
         * - Отслеживание статуса направлений (отправлено/принято/отклонено)
         *
         * UI элементы:
         * - Иконка: 🗺️ (символ карты/направлений)
         * - Текст: "Направления"
         * - Стандартная схема цветов навигационной панели
         */
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

        /**
         * Кнопка навигации "Приказ" (Screen4)
         *
         * Документооборотный экран для формирования официальных документов:
         * - Генерация приказов о направлении студентов на практику
         * - Использование шаблонов документов для разных типов практики
         * - Предварительный просмотр документов с возможностью редактирования
         * - Автоматическое заполнение данных студентов и предприятий
         * - Экспорт готовых приказов в PDF формат для печати
         * - Архивирование и учет созданных документов
         *
         * UI элементы:
         * - Иконка: 📜 (символ свитка/официального документа)
         * - Текст: "Приказ"
         * - Единообразная цветовая схема с другими кнопками панели
         */
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