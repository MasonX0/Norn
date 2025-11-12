// desktopMain/kotlin/ru/bpo/norn/App.kt
package ru.bpo.norn.jwmMain.screens.topnavbarscreens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.bpo.norn.jwmMain.viewmodel.NornViewModel
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter
import java.io.File

@Composable
fun DestinationScreen(viewModel: NornViewModel) {
    val documentGenerationStatus by viewModel.documentGenerationStatus.collectAsState()
    val directionTemplateFile by viewModel.directionTemplateFile.collectAsState()
    val groups by viewModel.groups.collectAsState()
    val selectedGroup by viewModel.selectedGroupForDirections.collectAsState()
    val outputFolder by viewModel.directionsOutputFolder.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(15.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        Text(
            "Генерация направлений на практику",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )

        // Загрузка шаблона
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "📄 Шаблон направления:",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            val fileChooser = JFileChooser().apply {
                                fileSelectionMode = JFileChooser.FILES_ONLY
                                fileFilter =
                                    FileNameExtensionFilter("Word документы", "docx", "doc")
                                dialogTitle = "Выберите шаблон направления"
                                currentDirectory = File(System.getProperty("user.home"), "Desktop")
                            }

                            if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                                viewModel.selectDirectionTemplateFile(fileChooser.selectedFile)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("📁 Выбрать шаблон")
                    }

                    if (directionTemplateFile != null) {
                        Button(
                            onClick = { viewModel.selectDirectionTemplateFile(null) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("✖")
                        }
                    }
                }

                if (directionTemplateFile != null) {
                    Text(
                        "Выбран файл: ${directionTemplateFile!!.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Путь: ${directionTemplateFile!!.absolutePath}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        "⚠️ Выберите шаблон Word документа для генерации направлений",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        // Выбор группы
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "👥 Выбор группы:",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (groups.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 200.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        itemsIndexed(groups) { index: Int, group: ru.bpo.norn.commonMain.models.Group ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.selectGroupForDirections(group) },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selectedGroup?.name == group.name) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surface
                                    }
                                ),
                                border = if (selectedGroup?.name == group.name) {
                                    BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                                } else null
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    Text(
                                        group.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = if (selectedGroup?.name == group.name) {
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        }
                                    )
                                    Text(
                                        "Студентов: ${group.students.size}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (selectedGroup?.name == group.name) {
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Text(
                        "⚠️ Нет загруженных групп. Загрузите студентов в разделе 'Студенты'",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // Выбор папки для сохранения
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "📂 Папка для сохранения:",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            val folderChooser = JFileChooser().apply {
                                fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                                dialogTitle = "Выберите папку для сохранения направлений"
                                currentDirectory = File(System.getProperty("user.home"), "Desktop")
                            }

                            if (folderChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                                viewModel.selectDirectionsOutputFolder(folderChooser.selectedFile)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("📁 Выбрать папку")
                    }

                    if (outputFolder != null) {
                        Button(
                            onClick = { viewModel.selectDirectionsOutputFolder(null) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("✖")
                        }
                    }
                }

                if (outputFolder != null) {
                    Text(
                        "Выбрана папка: ${outputFolder!!.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Путь: ${outputFolder!!.absolutePath}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        "⚠️ Выберите папку для сохранения документов",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        // Инструкция по шаблону
        Card(
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "📝 Информация о генерации:",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    "• Для каждого студента будет создан отдельный файл",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    "• Номер направления соответствует порядковому номеру студента в группе",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    "• Используется проверенный метод заполнения плейсхолдеров",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Кнопка генерации направлений
        Button(
            onClick = {
                viewModel.generateDirectionsFromTemplate()
            },
            enabled = selectedGroup != null &&
                    selectedGroup!!.students.isNotEmpty() &&
                    directionTemplateFile != null &&
                    outputFolder != null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("📄 Сгенерировать направления для группы ${selectedGroup?.name ?: ""}")
        }

        // Статус генерации документа
        if (documentGenerationStatus.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        documentGenerationStatus.startsWith("✅") -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                        documentGenerationStatus.startsWith("❌") -> Color(0xFFF44336).copy(alpha = 0.1f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                )
            ) {
                Text(
                    text = documentGenerationStatus,
                    modifier = Modifier.padding(12.dp),
                    color = when {
                        documentGenerationStatus.startsWith("✅") -> Color(0xFF2E7D32)
                        documentGenerationStatus.startsWith("❌") -> Color(0xFFD32F2F)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Превью выбранной группы
        if (selectedGroup != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "📋 Студенты группы ${selectedGroup!!.name}:",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        "Будет создано ${selectedGroup!!.students.size} файлов:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    LazyColumn(
                        modifier = Modifier.heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        itemsIndexed(selectedGroup!!.students) { index: Int, student: ru.bpo.norn.commonMain.models.Student ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                        alpha = 0.3f
                                    )
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "№${index + 1}",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.width(40.dp)
                                    )

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            student.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                        Text(
                                            "Файл: ${
                                                student.name.replace(
                                                    " ",
                                                    "_"
                                                )
                                            }_Napravlenie.docx",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}