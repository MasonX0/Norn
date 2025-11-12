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
import androidx.compose.ui.unit.sp
import ru.bpo.norn.jwmMain.viewmodel.NornViewModel
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter
import java.io.File

/**
 * Экран генерации направлений на практику
 * Позволяет выбрать шаблон Word документа и сгенерировать направления для студентов группы
 * @param viewModel ViewModel для управления данными направлений
 */
@Composable
fun DestinationScreen(viewModel: NornViewModel) {
    // Подписка на состояния из ViewModel
    val documentGenerationStatus by viewModel.directionsGenerationStatus.collectAsState()
    val directionTemplateFile by viewModel.directionTemplateFile.collectAsState()
    val groups by viewModel.groups.collectAsState()
    val selectedGroup by viewModel.selectedGroupForDirections.collectAsState()
    val outputFolder by viewModel.directionsOutputFolder.collectAsState()
    val dateOfDirectionIssue by viewModel.dateOfDirectionIssue.collectAsState()
    val dateOfTaskReceived by viewModel.dateOfTaskReceived.collectAsState()
    val dateOfDepartmentReview by viewModel.dateOfDepartmentReview.collectAsState()
    val baseDirectory by viewModel.baseDirectory.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(15.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        // Заголовок экрана
        Text(
            "Генерация направлений на практику",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )

        // Секция загрузки шаблона Word документа
        TemplateSelectionSection(
            directionTemplateFile = directionTemplateFile,
            onTemplateSelect = { viewModel.selectDirectionTemplateFile(it) },
            onTemplateClear = { viewModel.selectDirectionTemplateFile(null) },
            viewModel = viewModel
        )

        // Секция выбора группы студентов
        GroupSelectionSection(
            groups = groups,
            selectedGroup = selectedGroup,
            onGroupSelect = { viewModel.selectGroupForDirections(it) }
        )

        // Секция настройки папки для сохранения файлов
        OutputFolderSection(
            baseDirectory = baseDirectory,
            outputFolder = outputFolder,
            onFolderSelect = { viewModel.selectDirectionsOutputFolder(it) },
            viewModel = viewModel
        )

        // Секция ввода дат для направлений
        DatesInputSection(
            dateOfDirectionIssue = dateOfDirectionIssue,
            onDirectionIssueChange = { viewModel.updateDateOfDirectionIssue(it) },
            dateOfTaskReceived = dateOfTaskReceived,
            onTaskReceivedChange = { viewModel.updateDateOfTaskReceived(it) },
            dateOfDepartmentReview = dateOfDepartmentReview,
            onDepartmentReviewChange = { viewModel.updateDateOfDepartmentReview(it) }
        )

        // Информационная секция о процессе генерации
        GenerationInfoSection()

        // Кнопка генерации направлений
        GenerateDirectionsButton(
            viewModel = viewModel,
            selectedGroup = selectedGroup,
            directionTemplateFile = directionTemplateFile,
            baseDirectory = baseDirectory,
            outputFolder = outputFolder
        )

        // Статус генерации документов
        if (documentGenerationStatus.isNotEmpty()) {
            DocumentGenerationStatus(documentGenerationStatus)
        }

        // Превью выбранной группы студентов
        if (selectedGroup != null) {
            GroupPreviewSection(selectedGroup!!)
        }
    }
}

/**
 * Секция выбора шаблона Word документа
 */
@Composable
private fun TemplateSelectionSection(
    directionTemplateFile: File?,
    onTemplateSelect: (File) -> Unit,
    onTemplateClear: () -> Unit,
    viewModel: NornViewModel
) {
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

            // Кнопки управления шаблоном
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Кнопка выбора файла шаблона
                Button(
                    onClick = {
                        val fileChooser = JFileChooser().apply {
                            fileSelectionMode = JFileChooser.FILES_ONLY
                            dialogTitle = "Выберите файл шаблона направления"
                            addChoosableFileFilter(
                                FileNameExtensionFilter(
                                    "Word документы (*.docx)",
                                    "docx"
                                )
                            )
                            fileFilter = FileNameExtensionFilter("Word документы", "docx")
                            isAcceptAllFileFilterUsed = false
                            currentDirectory = viewModel.getStartDirectory()
                        }

                        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                            onTemplateSelect(fileChooser.selectedFile)
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("📁 Выбрать шаблон")
                }

                // Кнопка очистки выбранного шаблона
                if (directionTemplateFile != null) {
                    Button(
                        onClick = onTemplateClear,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("✖")
                    }
                }
            }

            // Информация о выбранном файле
            if (directionTemplateFile != null) {
                TemplateFileInfo(directionTemplateFile)
            } else {
                Text(
                    "⚠️ Выберите шаблон Word документа для генерации направлений",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/**
 * Информация о выбранном файле шаблона
 */
@Composable
private fun TemplateFileInfo(templateFile: File) {
    Text(
        "Выбран файл: ${templateFile.name}",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.primary
    )
    Text(
        "Путь: ${templateFile.absolutePath}",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

/**
 * Секция выбора группы студентов
 */
@Composable
private fun GroupSelectionSection(
    groups: List<ru.bpo.norn.commonMain.models.Group>,
    selectedGroup: ru.bpo.norn.commonMain.models.Group?,
    onGroupSelect: (ru.bpo.norn.commonMain.models.Group) -> Unit
) {
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
                // Список групп для выбора
                LazyColumn(
                    modifier = Modifier.heightIn(max = 200.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    itemsIndexed(groups) { index: Int, group: ru.bpo.norn.commonMain.models.Group ->
                        GroupSelectionCard(
                            group = group,
                            isSelected = selectedGroup?.name == group.name,
                            onSelect = { onGroupSelect(group) }
                        )
                    }
                }
            } else {
                // Заглушка когда нет групп
                Text(
                    "⚠️ Нет загруженных групп. Загрузите студентов в разделе 'Студенты'",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

/**
 * Карточка группы для выбора
 */
@Composable
private fun GroupSelectionCard(
    group: ru.bpo.norn.commonMain.models.Group,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else null
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                group.name,
                style = MaterialTheme.typography.titleSmall,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
            Text(
                "Студентов: ${group.students.size}",
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

/**
 * Секция настройки папки для сохранения файлов
 */
@Composable
private fun OutputFolderSection(
    baseDirectory: File?,
    outputFolder: File?,
    onFolderSelect: (File) -> Unit,
    viewModel: NornViewModel
) {
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
                "📂 Папка для сохранения направлений:",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Информация о текущей настройке папки
            if (baseDirectory != null) {
                BaseDirectoryInfo(outputFolder, viewModel)
            } else {
                NoBaseDirectoryWarning(outputFolder)
            }

            // Кнопка выбора папки
            FolderSelectionButton(
                baseDirectory = baseDirectory,
                onFolderSelect = onFolderSelect,
                viewModel = viewModel
            )
        }
    }
}

/**
 * Информация когда установлена базовая директория
 */
@Composable
private fun BaseDirectoryInfo(outputFolder: File?, viewModel: NornViewModel) {
    Text(
        "✅ Базовая директория установлена. Направления будут сохранены:",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.primary
    )

    outputFolder?.let { folder ->
        Text(
            "📁 Выбранная папка: ${folder.absolutePath}",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Green
        )
    } ?: run {
        Text(
            "📁 По умолчанию: ${viewModel.getOutputDirectory("Направления").absolutePath}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Предупреждение когда нет базовой директории
 */
@Composable
private fun NoBaseDirectoryWarning(outputFolder: File?) {
    Text(
        "⚠️ Базовая директория не установлена. Необходимо выбрать папку для сохранения:",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.error
    )

    outputFolder?.let { folder ->
        Text(
            "📁 Выбранная папка: ${folder.absolutePath}",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Green
        )
    } ?: run {
        Text(
            "❌ Папка не выбрана",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error
        )
    }
}

/**
 * Кнопка выбора папки для сохранения
 */
@Composable
private fun FolderSelectionButton(
    baseDirectory: File?,
    onFolderSelect: (File) -> Unit,
    viewModel: NornViewModel
) {
    Button(
        onClick = {
            val folderChooser = JFileChooser().apply {
                fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                dialogTitle = "Выберите папку для сохранения направлений"
                currentDirectory = viewModel.getOutputDirectory()
            }

            val result = folderChooser.showOpenDialog(null)
            if (result == JFileChooser.APPROVE_OPTION) {
                onFolderSelect(folderChooser.selectedFile)
            }
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(if (baseDirectory != null) "📂 Изменить папку (необязательно)" else "📂 Выбрать папку (обязательно)")
    }
}

/**
 * Секция ввода дат для направлений
 */
@Composable
private fun DatesInputSection(
    dateOfDirectionIssue: String,
    onDirectionIssueChange: (String) -> Unit,
    dateOfTaskReceived: String,
    onTaskReceivedChange: (String) -> Unit,
    dateOfDepartmentReview: String,
    onDepartmentReviewChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "📅 Даты для направлений (общие для всей группы)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Поле даты выдачи направления
            DateInputField(
                label = "Дата выдачи направления (dataIaV):",
                value = dateOfDirectionIssue,
                onValueChange = onDirectionIssueChange
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Поле даты получения индивидуального задания
            DateInputField(
                label = "Дата получения индивидуального задания (dataIaP):",
                value = dateOfTaskReceived,
                onValueChange = onTaskReceivedChange
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Поле даты отзыва руководителя от кафедры
            DateInputField(
                label = "Дата отзыва руководителя от кафедры (dataOtz):",
                value = dateOfDepartmentReview,
                onValueChange = onDepartmentReviewChange
            )
        }
    }
}

/**
 * Поле ввода даты
 */
@Composable
private fun DateInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Text(label, fontSize = 12.sp)
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text("Например: 04.04.2025") },
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        singleLine = true
    )
}

/**
 * Информационная секция о процессе генерации
 */
@Composable
private fun GenerationInfoSection() {
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

            val infoItems = listOf(
                "• Для каждого студента будет создан отдельный файл",
                "• Номер направления соответствует порядковому номеру студента в группе",
                "• Используется проверенный метод заполнения плейсхолдеров"
            )

            infoItems.forEach { item ->
                Text(
                    item,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

/**
 * Кнопка генерации направлений
 */
@Composable
private fun GenerateDirectionsButton(
    viewModel: NornViewModel,
    selectedGroup: ru.bpo.norn.commonMain.models.Group?,
    directionTemplateFile: File?,
    baseDirectory: File?,
    outputFolder: File?
) {
    Button(
        onClick = {
            viewModel.generateDirectionsFromTemplate()
        },
        enabled = selectedGroup != null &&
                selectedGroup.students.isNotEmpty() &&
                directionTemplateFile != null &&
                (baseDirectory != null || outputFolder != null),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("📄 Сгенерировать направления для группы ${selectedGroup?.name ?: ""}")
    }
}

/**
 * Отображение статуса генерации документов
 */
@Composable
private fun DocumentGenerationStatus(status: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                status.startsWith("✅") -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                status.startsWith("❌") -> Color(0xFFF44336).copy(alpha = 0.1f)
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(12.dp),
            color = when {
                status.startsWith("✅") -> Color(0xFF2E7D32)
                status.startsWith("❌") -> Color(0xFFD32F2F)
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/**
 * Превью выбранной группы студентов
 * Показывает список студентов и имена файлов, которые будут созданы
 */
@Composable
private fun GroupPreviewSection(selectedGroup: ru.bpo.norn.commonMain.models.Group) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "📋 Студенты группы ${selectedGroup.name}:",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                "Будет создано ${selectedGroup.students.size} файлов:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Список студентов с превью имен файлов
            LazyColumn(
                modifier = Modifier.heightIn(max = 300.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                itemsIndexed(selectedGroup.students) { index: Int, student: ru.bpo.norn.commonMain.models.Student ->
                    StudentPreviewCard(
                        studentIndex = index + 1,
                        student = student
                    )
                }
            }
        }
    }
}

/**
 * Карточка превью студента
 */
@Composable
private fun StudentPreviewCard(
    studentIndex: Int,
    student: ru.bpo.norn.commonMain.models.Student
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Номер студента
            Text(
                "№$studentIndex",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.width(40.dp)
            )

            // Информация о студенте и файле
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    student.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    "Файл: ${student.name.replace(" ", "_")}_Napravlenie.docx",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}