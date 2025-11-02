package ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
fun Enterprises(viewModel: NornViewModel) {
    val enterprisesFile by viewModel.enterprisesFile.collectAsState()
    val enterprisesList by viewModel.enterprisesList.collectAsState()
    val selectedEnterprise by viewModel.selectedEnterprise.collectAsState()
    val showEditDialog by viewModel.showEditDialog.collectAsState()

    // Локальное состояние для текстового поля
    var newCityText by remember { mutableStateOf("") }

    // Обновляем текстовое поле когда выбираем предприятие
    LaunchedEffect(selectedEnterprise) {
        newCityText = selectedEnterprise?.second ?: ""
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(15.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        Text(
            "Чтение списка предприятий из TXT файла",
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium
        )
        // Кнопка выбора TXT файла
        Row(modifier=Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(15.dp)){

            Button(onClick = {
                val fileChooser = JFileChooser().apply {
                    currentDirectory = File(System.getProperty("user.home"), "Desktop")
                    dialogTitle = "Выберите текстовый документ с предприятиями"
                    addChoosableFileFilter(FileNameExtensionFilter("Текстовые файлы (*.txt)", "txt"))
                    fileFilter = FileNameExtensionFilter("Текстовые файлы", "txt")
                    isAcceptAllFileFilterUsed = false
                }

                if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    viewModel.selectEnterprisesFile(fileChooser.selectedFile)
                    val file = fileChooser.selectedFile
                    if (file.exists()) {
                        viewModel.readEnterprisesFromTxt(file)
                    }
                }
            }, colors = ButtonColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                disabledContentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ), border = BorderStroke(1.dp,MaterialTheme.colorScheme.outline)
            ) {
                Text("📁 Выбрать TXT документ\n(список предприятий)")
            }
            // Кнопка очистки списка
            if (enterprisesList.isNotEmpty()) {
                Button(
                    onClick = { viewModel.clearEnterprisesList()
                        viewModel.selectEnterprisesFile(null)},
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("🗑️ Очистить список")
                }
            }

        }



        // Отображение списка предприятий с городами
        if (enterprisesList.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border= BorderStroke(width = 2.dp, color = MaterialTheme.colorScheme.primary)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "📋 Список предприятий (${enterprisesList.size}):",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 10.dp)
                    )
                    Divider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                    LazyColumn(modifier = Modifier.height(400.dp)) {
                        items(enterprisesList) { enterprise ->
                            val city = viewModel.extractCityFromEnterpriseSmart(enterprise)
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                onClick = {
                                    // При нажатии на карточку - открываем диалог редактирования
                                    viewModel.selectEnterpriseForEditing(enterprise)
                                },
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                                , border = BorderStroke(1.dp, color = MaterialTheme.colorScheme.tertiary)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        enterprise,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.weight(1f)
                                    )

                                    Text(
                                        city ?: "❓",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (city != null) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }



        // Отображение информации о файле
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                "Информация о файле:",
                style = MaterialTheme.typography.titleMedium
            )

            Text("TXT документ: ${enterprisesFile?.name ?: "не выбран"}")
            enterprisesFile?.let { file ->
                Text("Путь: ${file.absolutePath}")
                Text("Размер: ${file.length() / 1024} KB")
                Text("Строк в файле: ${countLines(file)}")
            }
        }
    }

    // Диалог редактирования города
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.closeEditDialog() },
            title = { Text("✏️ Изменить город") },
            text = {
                Column {
                    Text(
                        "Предприятие: ${selectedEnterprise?.first ?: ""}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = newCityText,
                        onValueChange = { newCityText = it },
                        label = { Text("Новый город") },
                        placeholder = { Text("Например: Уфа, Москва, Санкт-Петербург") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCityText.isNotBlank()) {
                            viewModel.updateEnterpriseCity(newCityText)
                        }
                    },
                    enabled = newCityText.isNotBlank()
                ) {
                    Text("Сохранить")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.closeEditDialog() }
                ) {
                    Text("Отмена")
                }
            }
        )
    }
}

// Функция для подсчета строк в TXT файле (локальная, только для UI)
private fun countLines(file: File): Int {
    return try {
        file.useLines { lines -> lines.count() }
    } catch (e: Exception) {
        -1
    }
}