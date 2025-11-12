package ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.bpo.norn.jwmMain.viewmodel.NornViewModel
import java.io.File

/**
 * Компонент для генерации документов из загруженных заявлений
 * Позволяет запустить процесс обработки PDF файла и создания выходных документов
 */
@Composable
fun DocumentGenerator(
    viewModel: NornViewModel,
    statementsFile: File?,
    modifier: Modifier = Modifier
) {
    var generationResult by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        // Кнопка генерации документа
        Button(
            onClick = {
                generateDocument(
                    viewModel = viewModel,
                    templateFile = statementsFile,
                    onResult = { result -> generationResult = result },
                    onLoadingChange = { loading -> isLoading = loading }
                )
            },
            enabled = statementsFile != null && !isLoading
        ) {
            Text(if (isLoading) "Генерация..." else "Сгенерировать документ")
        }

        // Результат генерации
        generationResult?.let { result ->
            Text(
                result,
                style = MaterialTheme.typography.bodyMedium,
                color = if (result.startsWith("✅")) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.error
            )
        }

        // Индикатор загрузки
        if (isLoading) {
            Text(
                "⏳ Идет генерация документа...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Функция для запуска генерации документа
 */
private fun generateDocument(
    viewModel: NornViewModel,
    templateFile: File?,
    onResult: (String) -> Unit,
    onLoadingChange: (Boolean) -> Unit
) {
    onLoadingChange(true)
    onResult("")

    try {
        if (templateFile != null) {
            // Запускаем генерацию через ViewModel
            val success = viewModel.generatePracticeDocument(templateFile)
            onResult(
                if (success) {
                    "✅ Документ успешно создан в той же папке!"
                } else {
                    "❌ Ошибка при создании документа!"
                }
            )
        } else {
            onResult("⚠️ Сначала выберите PDF документ")
        }
    } catch (e: Exception) {
        onResult("❌ Исключение: ${e.message}")
        e.printStackTrace()
    } finally {
        onLoadingChange(false)
    }
}