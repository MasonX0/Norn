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

/**
 * Экран управления студентами и группами
 * Позволяет загружать Excel файлы со студентами и редактировать их данные
 * @param viewModel ViewModel для управления данными студентов и групп
 */
@Composable
fun StudentsListScreen(viewModel: NornViewModel) {
    // Подписка на состояния из ViewModel
    val groups by viewModel.groups.collectAsState()
    val selectedGroup by viewModel.selectedGroup.collectAsState()
    val selectedStudent by viewModel.selectedStudent.collectAsState()
    val showEditDialog by viewModel.showStudentEditDialog.collectAsState()

    // Локальные состояния для генерации и результатов
    var generationResult by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // Локальные состояния для редактирования данных студента
    var editedFullName by remember { mutableStateOf("") }
    var editedRecordBook by remember { mutableStateOf("") }
    var editedFundingType by remember { mutableStateOf("") }
    var editedBranch by remember { mutableStateOf("") }
    var editedFaculty by remember { mutableStateOf("") }
    var editedGroup by remember { mutableStateOf("") }
    var editedIsPaidPractice by remember { mutableStateOf("") }
    var editedPracticeForm by remember { mutableStateOf("") }

    // Обновляем поля редактирования когда выбираем студента
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
        // Заголовок экрана
        Text(
            "Управление группами студентов",
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium
        )

        // Информационная подсказка
        Text(
            "ℹ️ Студенты загружаются непосредственно в группы",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall
        )

        // Кнопка загрузки Excel файлов
        LoadExcelFilesButton(viewModel)

        // Список загруженных групп
        if (groups.isNotEmpty()) {
            GroupsListCard(
                groups = groups,
                selectedGroup = selectedGroup,
                onGroupSelect = { viewModel.selectGroup(it) },
                onGroupRemove = { viewModel.removeGroup(it) }
            )
        }

        // Информация о выбранной группе и списке студентов
        selectedGroup?.let { group ->
            // Карточка с информацией о группе
            GroupInfoCard(group)

            // Список студентов выбранной группы
            StudentsListCard(
                group = group,
                onStudentSelect = { viewModel.selectStudentForEditing(it) }
            )
        }

        // Заглушка когда нет групп
        if (groups.isEmpty()) {
            NoGroupsPlaceholder()
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Тестовая кнопка для обновления студента (для отладки)
        if (selectedStudent != null) {
            TestUpdateStudentButton(selectedStudent, viewModel)
        }

        // Отображение результата генерации
        generationResult?.let { result ->
            GenerationResultText(result)
        }

        // Индикатор загрузки
        if (isLoading) {
            LoadingIndicator()
        }
    }

    // Диалог редактирования студента
    if (showEditDialog) {
        StudentEditDialog(
            editedFullName = editedFullName,
            onFullNameChange = { editedFullName = it },
            editedGroup = editedGroup,
            onGroupChange = { editedGroup = it },
            editedFundingType = editedFundingType,
            onFundingTypeChange = { editedFundingType = it },
            editedBranch = editedBranch,
            onBranchChange = { editedBranch = it },
            editedFaculty = editedFaculty,
            onFacultyChange = { editedFaculty = it },
            editedIsPaidPractice = editedIsPaidPractice,
            onIsPaidPracticeChange = { editedIsPaidPractice = it },
            editedPracticeForm = editedPracticeForm,
            onPracticeFormChange = { editedPracticeForm = it },
            selectedStudent = selectedStudent,
            onSave = { updatedStudent -> viewModel.updateStudentData(updatedStudent) },
            onDismiss = { viewModel.closeStudentEditDialog() }
        )
    }
}

/**
 * Кнопка для загрузки Excel файлов со студентами
 */
@Composable
private fun LoadExcelFilesButton(viewModel: NornViewModel) {
    Button(
        onClick = {
            val fileChooser = JFileChooser().apply {
                currentDirectory = viewModel.getStartDirectory()
                dialogTitle = "Выберите Excel файл со студентами"
                addChoosableFileFilter(
                    FileNameExtensionFilter(
                        "Excel файлы (*.xlsx, *.xls)",
                        "xlsx",
                        "xls"
                    )
                )
                fileFilter = FileNameExtensionFilter("Excel файлы", "xlsx", "xls")
                isAcceptAllFileFilterUsed = false
                isMultiSelectionEnabled = true // Разрешаем выбор нескольких файлов
            }

            if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                val selectedFiles = fileChooser.selectedFiles
                selectedFiles.forEach { file ->
                    if (file.exists()) {
                        viewModel.loadStudentsFromExcel(file)
                }
            }
        }
    }, colors = ButtonColors(
        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
        disabledContentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Text("📁 Загрузить Excel файл(ы) в группы")
    }
}

/**
 * Карточка со списком загруженных групп
 */
@Composable
private fun GroupsListCard(
    groups: List<ru.bpo.norn.commonMain.models.Group>,
    selectedGroup: ru.bpo.norn.commonMain.models.Group?,
    onGroupSelect: (ru.bpo.norn.commonMain.models.Group) -> Unit,
    onGroupRemove: (ru.bpo.norn.commonMain.models.Group) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, color = MaterialTheme.colorScheme.primary)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "📚 Группы (${groups.size}):",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 10.dp)
            )
            Divider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                thickness = 1.dp,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            LazyColumn(modifier = Modifier.height(120.dp)) {
                items(groups) { group ->
                    GroupCard(
                        group = group,
                        isSelected = selectedGroup?.name == group.name,
                        onSelect = { onGroupSelect(group) },
                        onRemove = { onGroupRemove(group) }
                    )
                }
            }
        }
    }
}

/**
 * Карточка отдельной группы в списке
 */
@Composable
private fun GroupCard(
    group: ru.bpo.norn.commonMain.models.Group,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, color = MaterialTheme.colorScheme.tertiary)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Информация о группе
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

            // Кнопки управления группой
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onSelect,
                    border = BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.tertiary)
                ) {
                    Text("👁️ Просмотр")
                }
                OutlinedButton(
                    onClick = onRemove,
                    border = BorderStroke(width = 1.dp, color = Color.Red)
                ) {
                    Text("🗑️ Удалить")
                }
            }
        }
    }
}

/**
 * Карточка с информацией о выбранной группе
 */
@Composable
private fun GroupInfoCard(group: ru.bpo.norn.commonMain.models.Group) {
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
}

/**
 * Карточка со списком студентов выбранной группы
 */
@Composable
private fun StudentsListCard(
    group: ru.bpo.norn.commonMain.models.Group,
    onStudentSelect: (Student) -> Unit
) {
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
                modifier = Modifier.padding(bottom = 10.dp)
            )
            Divider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                thickness = 1.dp,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            LazyColumn(modifier = Modifier.height(400.dp)) {
                items(group.students) { student ->
                    StudentCard(
                        student = student,
                        onSelect = { onStudentSelect(student) }
                    )
                }
            }
        }
    }
}

/**
 * Карточка отдельного студента в списке
 */
@Composable
private fun StudentCard(
    student: Student,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onSelect,
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
            // Информация о студенте
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

            // Индикатор для клика
            Text(
                "👆",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * Заглушка когда нет загруженных групп
 */
@Composable
private fun NoGroupsPlaceholder() {
    Text(
        "📝 Нет загруженных групп. Нажмите кнопку выше чтобы загрузить Excel файл со студентами.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

/**
 * Тестовая кнопка для обновления студента (для отладки)
 */
@Composable
private fun TestUpdateStudentButton(
    selectedStudent: Student?,
    viewModel: NornViewModel
) {
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

/**
 * Отображение результата генерации
 */
@Composable
private fun GenerationResultText(result: String) {
    Text(
        result,
        style = MaterialTheme.typography.bodyMedium,
        color = if (result.startsWith("✅")) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.error
    )
}

/**
 * Индикатор загрузки
 */
@Composable
private fun LoadingIndicator() {
    Text("⏳ Идет генерация документа...")
}

/**
 * Диалог редактирования данных студента
 */
@Composable
private fun StudentEditDialog(
    editedFullName: String,
    onFullNameChange: (String) -> Unit,
    editedGroup: String,
    onGroupChange: (String) -> Unit,
    editedFundingType: String,
    onFundingTypeChange: (String) -> Unit,
    editedBranch: String,
    onBranchChange: (String) -> Unit,
    editedFaculty: String,
    onFacultyChange: (String) -> Unit,
    editedIsPaidPractice: String,
    onIsPaidPracticeChange: (String) -> Unit,
    editedPracticeForm: String,
    onPracticeFormChange: (String) -> Unit,
    selectedStudent: Student?,
    onSave: (Student) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("✏️ Редактировать данные студента") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Поля для редактирования данных студента
                StudentEditTextField(
                    value = editedFullName,
                    onValueChange = onFullNameChange,
                    label = "ФИО студента"
                )

                StudentEditTextField(
                    value = editedGroup,
                    onValueChange = onGroupChange,
                    label = "Группа"
                )

                StudentEditTextField(
                    value = editedFundingType,
                    onValueChange = onFundingTypeChange,
                    label = "Направление (бюджет/платное/льготный)",
                    placeholder = "Например: бюджет, платное, льготный прием"
                )

                StudentEditTextField(
                    value = editedBranch,
                    onValueChange = onBranchChange,
                    label = "Филиал"
                )

                StudentEditTextField(
                    value = editedFaculty,
                    onValueChange = onFacultyChange,
                    label = "Факультет"
                )

                StudentEditTextField(
                    value = editedIsPaidPractice,
                    onValueChange = onIsPaidPracticeChange,
                    label = "Платная практика"
                )

                StudentEditTextField(
                    value = editedPracticeForm,
                    onValueChange = onPracticeFormChange,
                    label = "Форма практики"
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (editedFullName.isNotBlank()) {
                        selectedStudent?.let { currentStudent ->
                            // Создаем обновленного студента с новыми данными
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
                            onSave(updatedStudent)
                        }
                    }
                },
                enabled = editedFullName.isNotBlank()
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

/**
 * Поле ввода для редактирования данных студента
 */
@Composable
private fun StudentEditTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = ""
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        placeholder = if (placeholder.isNotEmpty()) {
            { Text(placeholder) }
        } else null
    )
}

/**
 * Вспомогательная функция для определения цвета типа финансирования
 * @param fundingType Тип финансирования студента
 * @return Цвет для отображения типа финансирования
 */
private fun getFundingTypeColor(fundingType: String): Color {
    return when {
        fundingType.contains("бюджет", ignoreCase = true) -> Color(0xFF2E7D32) // Зеленый
        fundingType.contains("платн", ignoreCase = true) -> Color(0xFFD32F2F) // Красный
        fundingType.contains("льгот", ignoreCase = true) -> Color(0xFFED6C02) // Оранжевый
        else -> Color(0xFF666666) // Серый
    }
}