package ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.io.File

/**
 * Общий компонент для отображения информации о загруженном файле
 * Используется в разных экранах для единообразного отображения статуса файлов
 */
@Composable
fun FileStatusCard(
    file: File?,
    fileTypeName: String,
    additionalInfo: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            "Информация о файле:",
            style = MaterialTheme.typography.titleMedium
        )

        Text("$fileTypeName: ${file?.name ?: "не выбран"}")

        file?.let { selectedFile ->
            Text("Путь: ${selectedFile.absolutePath}")
            Text("Размер: ${selectedFile.length() / 1024} KB")

            // Дополнительная информация (например, количество строк)
            additionalInfo?.let { info ->
                Text(info)
            }
        }
    }
}

/**
 * Функция для подсчета строк в текстовом файле
 */
fun countLinesInFile(file: File): Int {
    return try {
        file.useLines { lines -> lines.count() }
    } catch (e: Exception) {
        -1
    }
}