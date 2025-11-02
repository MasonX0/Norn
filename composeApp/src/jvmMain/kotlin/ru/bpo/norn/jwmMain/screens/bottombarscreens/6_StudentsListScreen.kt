package ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ru.bpo.norn.commonMain.models.Student
import ru.bpo.norn.jwmMain.viewmodel.NornViewModel
import java.io.File
import java.text.SimpleDateFormat
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

@Composable
fun StudentsListScreen(viewModel: NornViewModel) {
    val studentFiles by viewModel.studentFiles.collectAsState()
    val selectedStudentFile by viewModel.selectedStudentFile.collectAsState()
    val currentStudentsList by viewModel.currentStudentsList.collectAsState()
    val selectedStudent by viewModel.selectedStudent.collectAsState()
    val showEditDialog by viewModel.showStudentEditDialog.collectAsState()

    var generationResult by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // Локальные состояния для текстовых полей редактирования
    var editedFullName by remember { mutableStateOf("") }
    var editedRecordBook by remember { mutableStateOf("") }
    var editedFundingType by remember { mutableStateOf("") }
    var editedBranch by remember { mutableStateOf("") }
    var editedFaculty by remember { mutableStateOf("") }
    var editedGroup by remember { mutableStateOf("") }

    // Обновляем поля когда выбираем студента
    LaunchedEffect(selectedStudent) {
        selectedStudent?.let { student ->
            editedFullName = student.name
            editedFundingType = student.formOfStudy
            editedBranch = student.cityOfPractice
            editedFaculty = student.nameOfDirection
            editedGroup = student.group
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(15.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        Text(
            "👥 Управление списками студентов",
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            "ℹ️ Можно загрузить несколько файлов и переключаться между ними",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall
        )

        // Кнопка выбора Excel файла
        Button(onClick = {
            val fileChooser = JFileChooser().apply {
                currentDirectory = File(System.getProperty("user.home"), "Desktop")
                dialogTitle = "Выберите Excel файл со студентами"
                addChoosableFileFilter(FileNameExtensionFilter("Excel файлы (*.xlsx, *.xls)", "xlsx", "xls"))
                fileFilter = FileNameExtensionFilter("Excel файлы", "xlsx", "xls")
                isAcceptAllFileFilterUsed = false
                isMultiSelectionEnabled = true // Разрешаем множественный выбор
            }

            if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                val selectedFiles = fileChooser.selectedFiles
                selectedFiles.forEach { file ->
                    if (file.exists()) {
                        viewModel.loadStudentsFromExcel(file)
                    }
                }
            }
        }) {
            Text("📁 Загрузить Excel файл(ы)\n(списки студентов)")
        }

        // Список загруженных файлов
        if (studentFiles.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "📚 Загруженные файлы (${studentFiles.size}):",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    LazyColumn(modifier = Modifier.height(120.dp)) {
                        items(studentFiles) { studentFile ->
                            val isSelected = selectedStudentFile?.file?.absolutePath == studentFile.file.absolutePath

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            studentFile.file.name,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            "Группа: ${studentFile.groupName} • Студентов: ${studentFile.students.size}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        TextButton(
                                            onClick = { viewModel.selectStudentFile(studentFile) }
                                        ) {
                                            Text("👁️ Просмотр")
                                        }
                                        TextButton(
                                            onClick = { viewModel.removeStudentFile(studentFile) }
                                        ) {
                                            Text("🗑️ Удалить")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Информация о текущем файле
        selectedStudentFile?.let { studentFile ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "📊 Текущий файл: ${studentFile.file.name}",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Группа: ${studentFile.groupName}")
                        Text("Студентов: ${studentFile.students.size}")
                    }

                    Text("Тип: ${getExcelFileType(studentFile.file)}")
                    Text("Загружен: ${SimpleDateFormat("dd.MM.yyyy HH:mm").format(studentFile.loadTime)}")
                }
            }
        }

        // Кнопка генерации документа
        Button(
            onClick = {
                isLoading = true
                generationResult = null
                try {
                    val templateFile = selectedStudentFile?.file
                    if (templateFile != null) {
                        val success = viewModel.generatePracticeDocument(templateFile)
                        generationResult = if (success) {
                            "✅ Документ успешно создан в той же папке!"
                        } else {
                            "❌ Ошибка при создании документа!"
                        }
                    } else {
                        generationResult = "⚠️ Сначала выберите файл студентов"
                    }
                } catch (e: Exception) {
                    generationResult = "❌ Исключение: ${e.message}"
                    e.printStackTrace()
                } finally {
                    isLoading = false
                }
            },
            enabled = selectedStudentFile != null && !isLoading
        ) {
            Text(if (isLoading) "Генерация..." else "📄 Сгенерировать документ")
        }

        // Отображение списка студентов текущего файла
        if (currentStudentsList.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "🎓 Список студентов (${currentStudentsList.size}):",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Информация о группе и направлении
                    currentStudentsList.firstOrNull()?.let { firstStudent ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            if (firstStudent.group.isNotEmpty()) {
                                Text("Группа: ${firstStudent.group}")
                            }
                            if (firstStudent.formOfStudy.isNotEmpty()) {
                                Text("Направление: ${firstStudent.formOfStudy}")
                            }
                        }
                    }

                    LazyColumn(modifier = Modifier.height(400.dp)) {
                        items(currentStudentsList) { student ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                onClick = {
                                    viewModel.selectStudentForEditing(student)
                                },
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            student.name,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Text(
                                                "Курс: ${student.course}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            if (student.formOfStudy.isNotEmpty()) {
                                                Text(
                                                    "• ${student.formOfStudy}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = getFundingTypeColor(student.formOfStudy)
                                                )
                                            }
                                        }
                                    }

                                    Text(
                                        "👆",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else if (studentFiles.isEmpty()) {
            Text(
                "📝 Нет загруженных файлов. Нажмите кнопку выше чтобы загрузить Excel файл со студентами.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        generationResult?.let { result ->
            Text(
                result,
                style = MaterialTheme.typography.bodyMedium,
                color = if (result.startsWith("✅")) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.error
            )
        }

        if (isLoading) {
            Text("⏳ Идет генерация документа...")
        }
    }

    // Диалог редактирования студента
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.closeStudentEditDialog() },
            title = { Text("✏️ Редактировать данные студента") },
            text = {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = editedFullName,
                        onValueChange = { editedFullName = it },
                        label = { Text("ФИО студента") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editedGroup,
                        onValueChange = { editedGroup = it },
                        label = { Text("Группа") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editedFundingType,
                        onValueChange = { editedFundingType = it },
                        label = { Text("Направление (бюджет/платное/льготный)") },
                        placeholder = { Text("Например: бюджет, платное, льготный прием") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editedBranch,
                        onValueChange = { editedBranch = it },
                        label = { Text("Филиал") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editedFaculty,
                        onValueChange = { editedFaculty = it },
                        label = { Text("Факультет") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editedFullName.isNotBlank()) {
                            selectedStudent?.let { currentStudent ->
                                // Создаем нового студента с обновленными данными
                                val updatedStudent = Student(
                                    name = editedFullName,
                                    course = currentStudent.course,
                                    codeOfDirection = currentStudent.codeOfDirection,
                                    nameOfDirection = editedFaculty,
                                    group = editedGroup,
                                    isForeign = currentStudent.isForeign,
                                    gradeForPractice = currentStudent.gradeForPractice,
                                    nameOfPracticeBase = currentStudent.nameOfPracticeBase,
                                    typeOfPractice = currentStudent.typeOfPractice,
                                    periodOfPractice = currentStudent.periodOfPractice,
                                    formOfStudy = editedFundingType,
                                    withPayment = currentStudent.withPayment,
                                    cityOfPractice = editedBranch,
                                    nameOfSpeciality = currentStudent.nameOfSpeciality,
                                    codeOfSpeciality = currentStudent.codeOfSpeciality,
                                    headOfPracticeFromDepartment = currentStudent.headOfPracticeFromDepartment,
                                    headOfPracticeFromPracticeBase = currentStudent.headOfPracticeFromPracticeBase,
                                    postOfHeadOfPracticeFromPracticeBase = currentStudent.postOfHeadOfPracticeFromPracticeBase,
                                    postOfHeadOfPracticeFromDepartment = currentStudent.postOfHeadOfPracticeFromDepartment,
                                    directorName = currentStudent.directorName,
                                )
                                viewModel.updateStudentData(updatedStudent)
                            }
                        }
                    },
                    enabled = editedFullName.isNotBlank()
                ) {
                    Text("Сохранить")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.closeStudentEditDialog() }
                ) {
                    Text("Отмена")
                }
            }
        )
    }
}

// Функция для определения цвета типа финансирования
private fun getFundingTypeColor(fundingType: String): Color {
    return when {
        fundingType.contains("бюджет", ignoreCase = true) -> Color(0xFF2E7D32) // Зеленый
        fundingType.contains("платн", ignoreCase = true) -> Color(0xFFD32F2F) // Красный
        fundingType.contains("льгот", ignoreCase = true) -> Color(0xFFED6C02) // Оранжевый
        else -> Color(0xFF666666) // Серый
    }
}

// Функция для определения типа Excel файла
private fun getExcelFileType(file: File): String {
    return when {
        file.name.endsWith(".xlsx") -> "XLSX (Excel 2007+)"
        file.name.endsWith(".xls") -> "XLS (Excel 97-2003)"
        else -> "Неизвестный формат"
    }
}