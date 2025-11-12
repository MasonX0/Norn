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
fun OrderScreen(viewModel: NornViewModel) {
    val orderFile by viewModel.orderFile.collectAsState()
    val groups by viewModel.groups.collectAsState()
    var generationResult by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // Получаем всех студентов из всех групп
    val allStudents = groups.flatMap { it.students }
    
    // Группируем студентов по типам финансирования
    val budgetStudents = allStudents.filter {
        it.formOfStudy.contains("бюджет", ignoreCase = true) ||
                (!it.withPayment && !it.formOfStudy.contains("платн", ignoreCase = true) && 
                 !it.formOfStudy.contains("целев", ignoreCase = true))
    }
    
    val targetStudents = allStudents.filter {
        it.formOfStudy.contains("целев", ignoreCase = true)
    }
    
    val paidStudents = allStudents.filter {
        it.withPayment || it.formOfStudy.contains("платн", ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(15.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        Text(
            "Генерация приказа по практике",
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.headlineSmall
        )

        // Статистика студентов
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Статистика загруженных студентов:",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("📊 Всего студентов:")
                    Text("${allStudents.size}", style = MaterialTheme.typography.titleMedium)
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("💰 Бюджетная основа:")
                    Text("${budgetStudents.size}", color = MaterialTheme.colorScheme.primary)
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("🎯 Целевая основа:")
                    Text("${targetStudents.size}", color = MaterialTheme.colorScheme.secondary)
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("💳 Платная основа:")
                    Text("${paidStudents.size}", color = MaterialTheme.colorScheme.tertiary)
                }
                
                if (allStudents.isEmpty()) {
                    Text(
                        "⚠️ Нет загруженных студентов. Загрузите списки студентов в разделе 'Студенты'",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // Кнопка генерации приказа (без шаблона)
        Button(
            onClick = {
                isLoading = true
                generationResult = null
                try {
                    val success = viewModel.generateOrderDocument()
                    generationResult = if (success) {
                        "✅ Приказ успешно создан на рабочем столе!"
                    } else {
                        "❌ Ошибка при создании приказа. Проверьте, что загружены студенты."
                    }
                } catch (e: Exception) {
                    generationResult = "❌ Исключение: ${e.message}"
                    e.printStackTrace()
                } finally {
                    isLoading = false
                }
            },
            enabled = allStudents.isNotEmpty() && !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(if (isLoading) "Генерация приказа..." else "Сгенерировать приказ")
        }

        // Разделитель
        HorizontalDivider()

        Text(
            "Или загрузите свой шаблон приказа:",
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
        },colors = ButtonColors(
            containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f),
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            disabledContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f),
            disabledContentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ), border = BorderStroke(1.dp,MaterialTheme.colorScheme.outline)
        ) {
            Text("Выбрать шаблон Word документа")
        }

        // Кнопка генерации документа по шаблону
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
                            "❌ Ошибка при создании документа, закройте используемые word файлы!"
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
            enabled = orderFile != null && !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            )
        ) {
            Text(if (isLoading) "Генерация..." else "Сгенерировать по шаблону")
        }

        // Отображение состояния
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text("Состояние:",
                style = MaterialTheme.typography.titleMedium)

            Text("Шаблон: ${orderFile?.name ?: "не выбран"}")
            orderFile?.let { file ->
                Text("Путь: ${file.absolutePath}", 
                     style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(8.dp))

            generationResult?.let { result ->
                Text(result,
                    style = MaterialTheme.typography.bodyMedium)
            }

            if (isLoading) {
                Text("⏳ Идет генерация документа...")
            }
        }
    }
}