package ru.bpo.norn.jwmMain.screens.bottombarscreens.settings.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.bpo.norn.jwmMain.viewmodel.NornViewModel
import java.io.File
import javax.swing.JFileChooser

/**
 * Компонент для управления базовой директорией приложения
 * Позволяет пользователю выбрать папку, которая будет использоваться
 * для поиска файлов и сохранения создаваемых документов
 */
@Composable
fun BaseDirectorySection(
    viewModel: NornViewModel,
    modifier: Modifier = Modifier
) {
    // Получаем текущую базовую директорию из ViewModel
    val baseDirectory by viewModel.baseDirectory.collectAsState()

    Card(
        modifier = modifier.fillMaxWidth(),
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

            Text(
                "Установите папку, откуда будут начинаться поиски файлов во всех экранах, а также куда будут сохраняться создаваемые документы (отчеты, направления, приказы)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Отображение текущей директории
            CurrentDirectoryDisplay(baseDirectory = baseDirectory)

            // Кнопки управления
            DirectoryActionButtons(
                viewModel = viewModel,
                baseDirectory = baseDirectory
            )
        }
    }
}

/**
 * Компонент для отображения текущей установленной директории
 */
@Composable
private fun CurrentDirectoryDisplay(baseDirectory: File?) {
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
 * Компонент с кнопками для выбора и сброса директории
 */
@Composable
private fun DirectoryActionButtons(
    viewModel: NornViewModel,
    baseDirectory: File?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Кнопка выбора папки
        Button(
            onClick = {
                selectBaseDirectory(viewModel, baseDirectory)
            },
            modifier = Modifier.weight(1f)
        ) {
            Text("📂 Выбрать папку")
        }

        // Кнопка сброса настроек
        OutlinedButton(
            onClick = {
                viewModel.selectBaseDirectory(null)
            },
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
 * Функция для выбора базовой директории через диалог файлового менеджера
 */
private fun selectBaseDirectory(viewModel: NornViewModel, baseDirectory: File?) {
    val fileChooser = JFileChooser().apply {
        fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
        dialogTitle = "Выберите базовую директорию"
    }

    // Устанавливаем текущую директорию отдельно, чтобы избежать конфликта имен
    fileChooser.currentDirectory = baseDirectory ?: File(System.getProperty("user.home"))

    if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
        viewModel.selectBaseDirectory(fileChooser.selectedFile)
    }
}