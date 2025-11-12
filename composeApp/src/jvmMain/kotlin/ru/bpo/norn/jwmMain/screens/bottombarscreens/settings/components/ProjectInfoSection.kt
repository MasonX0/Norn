package ru.bpo.norn.jwmMain.screens.bottombarscreens.settings.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import java.awt.Desktop
import java.net.URI

/**
 * Компонент для отображения информации о проекте
 * Включает ссылки на GitHub репозиторий, контакты разработчика
 * и полезную информацию для пользователя
 */
@Composable
fun ProjectInfoSection(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "📚 Информация о проекте",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                "1) Весь код + примеры входных файлов находятся на GitHub:",
                style = MaterialTheme.typography.bodyMedium
            )

            // GitHub ссылка
            ClickableLink(
                text = "https://github.com/MasonX0/Norn",
                url = "https://github.com/MasonX0/Norn",
                color = Color(0xFF1976D2)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Контакты:",
                style = MaterialTheme.typography.bodyMedium
            )

            // Telegram ссылка
            ClickableLink(
                text = "Telegram: @masonrb",
                url = "https://t.me/masonrb",
                color = Color(0xFF0088CC)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Дополнительная информация
            UsefulInfoCard()
        }
    }
}

/**
 * Компонент для кликабельных ссылок
 */
@Composable
private fun ClickableLink(
    text: String,
    url: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = color,
        textDecoration = TextDecoration.Underline,
        modifier = modifier.clickable {
            try {
                Desktop.getDesktop().browse(URI(url))
            } catch (e: Exception) {
                println("❌ Ошибка открытия ссылки: ${e.message}")
            }
        }
    )
}

/**
 * Карточка с полезной информацией о проекте
 */
@Composable
private fun UsefulInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                "💡 Полезная информация:",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )

            // Список полезной информации
            val infoItems = listOf(
                "В репозитории находятся примеры входных файлов Excel",
                "Документация по форматам данных",
                "Инструкция по настройке и использованию",
                "Исходный код всех модулей приложения"
            )

            infoItems.forEach { item ->
                Text(
                    "• $item",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}