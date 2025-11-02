package ui

import androidx.compose.foundation.layout.*
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
fun OrderScreen(viewModel: NornViewModel) {
    val orderFile by viewModel.orderFile.collectAsState()
    var generationResult by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(15.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        Text(
            "Загрузите шаблон, чтобы получить приказ",
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyLarge
        )
        // Кнопка выбора шаблона
        Button(onClick = {
            val fileChooser = JFileChooser().apply {
                // Путь по умолчанию - рабочий стол
                currentDirectory = File(System.getProperty("user.home"), "Desktop")
                dialogTitle = "Выберите шаблон документа"
                addChoosableFileFilter(FileNameExtensionFilter("Word документы (*.docx)", "docx"))
                fileFilter = FileNameExtensionFilter("Word документы", "docx")
            }

            if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                viewModel.selectOrderFile(fileChooser.selectedFile)
            }
        }) {
            Text("Выбрать шаблон Word документа\n(приказа)")
        }

        // Кнопка генерации документа
        Button(
            onClick = {
                isLoading = true
                generationResult = null
                try {
                    val templateFile = orderFile
                    if (templateFile != null) {
                        val success = viewModel.generatePracticeDocument(templateFile)
                        generationResult = if (success) {
                            "✅ Документ успешно создан в той же папке!"
                        } else {
                            "❌ Ошибка при создании документа, закройте используемые word' файлы!"
                        }
                    } else {
                        generationResult = "⚠️ Сначала выберите шаблон документа"
                    }
                } catch (e: Exception) {
                    generationResult = "❌ Исключение: ${e.message}"
                    e.printStackTrace()
                } finally {
                    isLoading = false
                }
            },
            enabled = orderFile != null && !isLoading
        ) {
            Text(if (isLoading) "Генерация..." else "Сгенерировать документ")
        }




        // Отображение состояния
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text("Состояние приложения:",
                style = androidx.compose.material3.MaterialTheme.typography.titleMedium)

            Text("Шаблон: ${orderFile?.name ?: "не выбран"}")
            orderFile?.let { file ->
                Text("Путь: ${file.absolutePath}")
            }

            Spacer(modifier = Modifier.height(16.dp))

            generationResult?.let { result ->
                Text(result,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
            }

            if (isLoading) {
                Text("⏳ Идет генерация документа...")
            }
        }
    }
}