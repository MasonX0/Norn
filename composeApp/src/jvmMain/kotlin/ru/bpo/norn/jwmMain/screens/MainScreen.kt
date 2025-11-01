// desktopMain/kotlin/ru/bpo/norn/App.kt
package ru.bpo.norn.jwmMain.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.bpo.norn.jwmMain.viewmodel.NornViewModel
import ru.bpo.norn.commonMain.models.Group
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter
import java.io.File

@Composable
fun MainScreen() {
    val viewModel = remember { NornViewModel() }

    val groups by viewModel.groups.collectAsState()
    val reportFile by viewModel.reportFile.collectAsState()

    var generationResult by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Norn Desktop App", style = androidx.compose.material3.MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(24.dp))

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
                viewModel.selectReportFile(fileChooser.selectedFile)
            }
        }) {
            Text("Выбрать шаблон Word документа")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Кнопка генерации документа
        Button(
            onClick = {
                isLoading = true
                generationResult = null
                try {
                    val templateFile = reportFile
                    if (templateFile != null) {
                        val success = viewModel.generatePracticeDocument(templateFile)
                        generationResult = if (success) {
                            "✅ Документ успешно создан в той же папке!"
                        } else {
                            "❌ Ошибка при создании документа"
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
            enabled = reportFile != null && !isLoading
        ) {
            Text(if (isLoading) "Генерация..." else "Сгенерировать документ")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Информация о mock студенте
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text("Данные студента для заполнения:",
                style = androidx.compose.material3.MaterialTheme.typography.titleSmall)
            Text("ФИО: ${viewModel.getMockStudent().name}")
            Text("Группа: ${viewModel.getMockStudent().group}")
            Text("Курс: ${viewModel.getMockStudent().course}")
            Text("База практики: ${viewModel.getMockStudent().nameOfPracticeBase}")
            Text("Город: ${viewModel.getMockStudent().cityOfPractice}")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Отображение состояния
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text("Состояние приложения:",
                style = androidx.compose.material3.MaterialTheme.typography.titleMedium)

            Text("Шаблон: ${reportFile?.name ?: "не выбран"}")
            reportFile?.let { file ->
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