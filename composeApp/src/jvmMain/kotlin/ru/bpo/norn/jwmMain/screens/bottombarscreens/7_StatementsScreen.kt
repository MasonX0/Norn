package ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.bpo.norn.jwmMain.viewmodel.NornViewModel
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

@Composable
fun StatementsScreen(viewModel: NornViewModel) {
    val statementsFile by viewModel.statementsFile.collectAsState()
    var generationResult by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(15.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        Text(
            "В РАЗРАБОТКЕ",
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyLarge
        )

        // Кнопка выбора PDF файла
        Button(onClick = {
            val fileChooser = JFileChooser().apply {
                currentDirectory = File(System.getProperty("user.home"), "Desktop")
                dialogTitle = "Выберите PDF документ"
                addChoosableFileFilter(FileNameExtensionFilter("PDF документы (*.pdf)", "pdf"))
                fileFilter = FileNameExtensionFilter("PDF документы", "pdf")
                // Запрещаем выбор других типов файлов
                isAcceptAllFileFilterUsed = false
            }

            if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                viewModel.selectStatementsFile(fileChooser.selectedFile)
            }
        },colors = ButtonColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
            disabledContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ), border = BorderStroke(1.dp,MaterialTheme.colorScheme.outline)
        ) {
            Text("Выбрать PDF документ\n(ведомости)")
        }

        // Кнопка генерации документа
        Button(
            onClick = {
                isLoading = true
                generationResult = null
                try {
                    val templateFile = statementsFile
                    if (templateFile != null) {
                        val success = viewModel.generatePracticeDocument(templateFile)
                        generationResult = if (success) {
                            "✅ Документ успешно создан в той же папке!"
                        } else {
                            "❌ Ошибка при создании документа!"
                        }
                    } else {
                        generationResult = "⚠️ Сначала выберите PDF документ"
                    }
                } catch (e: Exception) {
                    generationResult = "❌ Исключение: ${e.message}"
                    e.printStackTrace()
                } finally {
                    isLoading = false
                }
            },
            enabled = statementsFile != null && !isLoading
        ) {
            Text(if (isLoading) "Генерация..." else "Сгенерировать документ")
        }

        // Отображение состояния
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                "Состояние приложения:",
                style = MaterialTheme.typography.titleMedium
            )

            Text("PDF документ: ${statementsFile?.name ?: "не выбран"}")
            statementsFile?.let { file ->
                Text("Путь: ${file.absolutePath}")
                Text("Размер: ${file.length() / 1024} KB")
            }

            Spacer(modifier = Modifier.height(16.dp))

            generationResult?.let { result ->
                Text(
                    result,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (isLoading) {
                Text("⏳ Идет генерация документа...")
            }
        }
    }
}