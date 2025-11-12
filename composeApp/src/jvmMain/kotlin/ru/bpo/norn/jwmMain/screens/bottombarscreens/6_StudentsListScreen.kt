package ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import ru.bpo.norn.commonMain.models.Student
import ru.bpo.norn.jwmMain.viewmodel.NornViewModel
import java.io.File
import java.text.SimpleDateFormat
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

@Composable
fun StudentsListScreen(viewModel: NornViewModel) {
    val groups by viewModel.groups.collectAsState()
    val selectedGroup by viewModel.selectedGroup.collectAsState()
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
    var editedIsPaidPractice by remember { mutableStateOf("") }
    var editedPracticeForm by remember { mutableStateOf("") }

    // Обновляем поля когда выбираем студента
    LaunchedEffect(selectedStudent) {
        selectedStudent?.let { student ->
            editedFullName = student.name
            editedFundingType = student.formOfStudy
            editedBranch = student.cityOfPractice
            editedFaculty = student.nameOfDirection
            editedGroup = student.group
            editedIsPaidPractice = if (student.isPaidPractice) "Да" else "Нет"
            editedPracticeForm = student.practiceForm
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(15.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        Text(
            "Управление группами студентов",
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            "ℹ️ Студенты загружаются непосредственно в группы",
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
        },colors = ButtonColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
            disabledContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ), border = BorderStroke(1.dp,MaterialTheme.colorScheme.outline)) {
            Text("📁 Загрузить Excel файл(ы) в группы")
        }

        // Список групп
        if (groups.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = BorderStroke(1.dp, color = MaterialTheme.colorScheme.primary)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "📚 Группы (${groups.size}):",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 10.dp)
                    )
                    Divider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                    LazyColumn(modifier = Modifier.height(120.dp)) {
                        items(groups) { group ->
                            val isSelected = selectedGroup?.name == group.name

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surface
                                ), border = BorderStroke(1.dp, color = MaterialTheme.colorScheme.tertiary)
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
                                            group.name,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            "Студентов: ${group.students.size} • Направление: ${group.nameOfDirection}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { viewModel.selectGroup(group) },
                                            border = BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.tertiary)
                                        ) {
                                            Text("👁️ Просмотр")
                                        }
                                        OutlinedButton(
                                            onClick = { viewModel.removeGroup(group) },
                                            border = BorderStroke(width = 1.dp, color = Color.Red)
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

        // Информация о текущей группе
        selectedGroup?.let { group ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, color = MaterialTheme.colorScheme.tertiary)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Текущая группа: ${group.name}",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Студентов: ${group.students.size}")
                        Text("Направление: ${group.nameOfDirection}")
                    }
                    Text("Код направления: ${group.codeOfDirection}")
                }
            }

            // Отображение списка студентов текущей группы
            Card(
                modifier = Modifier,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = BorderStroke(1.dp, color = MaterialTheme.colorScheme.tertiary)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "🎓 Список студентов (${group.students.size}):",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier=Modifier.padding(bottom=10.dp)
                    )
                    Divider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                    LazyColumn(modifier = Modifier.height(400.dp)) {
                        items(group.students) { student ->
                            Card(
                                modifier = Modifier

                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    ,
                                onClick = {
                                    viewModel.selectStudentForEditing(student)
                                },
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                border = BorderStroke(1.dp, color = MaterialTheme.colorScheme.tertiary)
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
        }
        if (groups.isEmpty()) {
        Text(
            "📝 Нет загруженных групп. Нажмите кнопку выше чтобы загрузить Excel файл со студентами.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

        Spacer(modifier = Modifier.height(16.dp))

        // Кнопка для теста
        if (selectedStudent != null) {
            Button(
                onClick = {
                    println("🧪 Тестирование обновления студента: ${selectedStudent?.name}")
                    selectedStudent?.let { student ->
                        val testStudent = student.copy(
                            gradeForPractice = "ТЕСТ: ${System.currentTimeMillis()}"
                        )
                        viewModel.updateStudentData(testStudent)
                    }
                }
            ) {
                Text("🧪 Тест обновления студента")
            }
        }

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

                    OutlinedTextField(
                        value = editedIsPaidPractice,
                        onValueChange = { editedIsPaidPractice = it },
                        label = { Text("Платная практика") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editedPracticeForm,
                        onValueChange = { editedPracticeForm = it },
                        label = { Text("Форма практики") },
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
                                    isPaidPractice = editedIsPaidPractice == "Да",
                                    practiceForm = editedPracticeForm,
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

// Остальные вспомогательные функции остаются без изменений
private fun getFundingTypeColor(fundingType: String): Color {
    return when {
        fundingType.contains("бюджет", ignoreCase = true) -> Color(0xFF2E7D32) // Зеленый
        fundingType.contains("платн", ignoreCase = true) -> Color(0xFFD32F2F) // Красный
        fundingType.contains("льгот", ignoreCase = true) -> Color(0xFFED6C02) // Оранжевый
        else -> Color(0xFF666666) // Серый
    }
}