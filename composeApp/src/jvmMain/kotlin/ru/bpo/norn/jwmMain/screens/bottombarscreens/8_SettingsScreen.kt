package ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import ru.bpo.norn.jwmMain.viewmodel.NornViewModel
import java.awt.Desktop
import java.io.File
import java.net.URI
import javax.swing.JFileChooser

/**
 * Экран настроек приложения
 * Содержит настройки базовой директории, темы и информацию о проекте
 * @param viewModel ViewModel для управления настройками приложения
 */
@Composable
fun SettingsScreen(viewModel: NornViewModel) {
    // Подписка на текущую базовую директорию
    val baseDirectory by viewModel.baseDirectory.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(15.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        // Заголовок экрана настроек
        Text(
            "Настройки приложения",
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.headlineSmall
        )

        // Секция управления базовой директорией
        BaseDirectorySection(
            baseDirectory = baseDirectory,
            onDirectorySelect = { viewModel.selectBaseDirectory(it) },
            onDirectoryReset = { viewModel.selectBaseDirectory(null) }
        )

        // Секция управления внешним видом (темой)
        AppearanceSection(
            onThemeSwitch = { viewModel.switchTheme() }
        )

        // Секция информации о проекте с ссылками
        ProjectInfoSection()

        // Секция информации о приложении
        ApplicationInfoSection()
    }
}

/**
 * Секция управления базовой директорией
 */
@Composable
private fun BaseDirectorySection(
    baseDirectory: File?,
    onDirectorySelect: (File) -> Unit,
    onDirectoryReset: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "📁 Базовая директория",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            // Описание назначения базовой директории
            Text(
                "Установите папку, откуда будут начинаться поиски файлов во всех экранах, а также куда будут сохраняться создаваемые документы (отчеты, направления, приказы)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Отображение текущей выбранной директории
            CurrentDirectoryCard(baseDirectory)

            // Кнопки управления директорией
            DirectoryManagementButtons(
                baseDirectory = baseDirectory,
                onDirectorySelect = onDirectorySelect,
                onDirectoryReset = onDirectoryReset
            )
        }
    }
}

/**
 * Карточка с информацией о текущей директории
 */
@Composable
private fun CurrentDirectoryCard(baseDirectory: File?) {
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
                "Текущая базовая директория:",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                baseDirectory?.absolutePath ?: "Не установлена (используется домашняя папка)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Кнопки управления базовой директорией
 */
@Composable
private fun DirectoryManagementButtons(
    baseDirectory: File?,
    onDirectorySelect: (File) -> Unit,
    onDirectoryReset: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Кнопка выбора новой директории
        Button(
            onClick = {
                val fileChooser = JFileChooser().apply {
                    fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                    dialogTitle = "Выберите базовую директорию"
                    currentDirectory = baseDirectory ?: File(System.getProperty("user.home"))
                }

                if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    onDirectorySelect(fileChooser.selectedFile)
                }
            },
            modifier = Modifier.weight(1f)
        ) {
            Text("📂 Выбрать папку")
        }

        // Кнопка сброса к дефолтной директории
        OutlinedButton(
            onClick = onDirectoryReset,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
        ) {
            Text("🗑️ Сбросить")
        }
    }
}

/**
 * Секция управления внешним видом приложения
 */
@Composable
private fun AppearanceSection(onThemeSwitch: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "🎨 Внешний вид",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            // Кнопка переключения темы
            Button(
                onClick = onThemeSwitch,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Text("Сменить тему")
            }
        }
    }
}

/**
 * Секция информации о проекте с ссылками
 */
@Composable
private fun ProjectInfoSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
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

            // Информация о GitHub репозитории
            Text(
                "1) Весь код + примеры входных файлов находятся на GitHub:",
                style = MaterialTheme.typography.bodyMedium
            )

            // Кликабельная GitHub ссылка
            ClickableLink(
                text = "https://github.com/MasonX0/Norn",
                url = "https://github.com/MasonX0/Norn",
                color = Color(0xFF1976D2)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Контактная информация
            Text(
                "Контакты:",
                style = MaterialTheme.typography.bodyMedium
            )

            // Кликабельная Telegram ссылка
            ClickableLink(
                text = "Telegram: @masonrb",
                url = "https://t.me/masonrb",
                color = Color(0xFF0088CC)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Дополнительная полезная информация
            AdditionalProjectInfo()
        }
    }
}

/**
 * Кликабельная ссылка
 */
@Composable
private fun ClickableLink(
    text: String,
    url: String,
    color: Color
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = color,
        textDecoration = TextDecoration.Underline,
        modifier = Modifier.clickable {
            try {
                Desktop.getDesktop().browse(URI(url))
            } catch (e: Exception) {
                println("❌ Ошибка открытия ссылки: ${e.message}")
            }
        }
    )
}

/**
 * Дополнительная информация о проекте
 */
@Composable
private fun AdditionalProjectInfo() {
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
                "• В репозитории находятся примеры входных файлов Excel",
                "• Документация по форматам данных",
                "• Инструкция по настройке и использованию",
                "• Исходный код всех модулей приложения"
            )

            infoItems.forEach { item ->
                Text(
                    item,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

/**
 * Секция информации о приложении (версия, технологии)
 */
@Composable
private fun ApplicationInfoSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "ℹ️ О приложении",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            // Основная информация о приложении
            Text(
                "Norn - система управления практикой студентов",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )

            // Версия приложения
            Text(
                "Версия: 1.0.0",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Используемые технологии
            Text(
                "Технологии: Kotlin, Compose Multiplatform, Apache POI",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
