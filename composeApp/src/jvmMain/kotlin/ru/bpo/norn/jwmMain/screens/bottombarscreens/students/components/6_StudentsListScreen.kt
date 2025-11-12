package ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.bpo.norn.commonMain.models.Student
import ru.bpo.norn.jwmMain.viewmodel.NornViewModel
import ui.components.StudentsFileLoader
import ui.components.GroupsList
import ui.components.GroupInfoCard
import ui.components.StudentsList
import ui.common.StudentEditDialog

/**
 * Основной экран управления группами студентов
 * Позволяет загружать Excel файлы, просматривать группы и редактировать студентов
 */
@Composable
fun StudentsListScreen(viewModel: NornViewModel) {
    val groups by viewModel.groups.collectAsState()
    val selectedGroup by viewModel.selectedGroup.collectAsState()
    val selectedStudent by viewModel.selectedStudent.collectAsState()
    val showEditDialog by viewModel.showStudentEditDialog.collectAsState()
    val enterprisesList by viewModel.enterprisesList.collectAsState()

    var generationResult by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

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

        // Компонент загрузки файлов
        StudentsFileLoader(viewModel = viewModel)

        // Список групп
        GroupsList(
            groups = groups,
            selectedGroup = selectedGroup,
            viewModel = viewModel
        )

        // Информация о выбранной группе
        selectedGroup?.let { group ->
            GroupInfoCard(group = group)
            
            // Список студентов в группе
            StudentsList(
                group = group,
                viewModel = viewModel
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Кнопка для тестирования (временная функциональность)
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

        // Результат генерации (если есть)
        generationResult?.let { result ->
            Text(
                result,
                style = MaterialTheme.typography.bodyMedium,
                color = if (result.startsWith("✅")) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.error
            )
        }

        // Индикатор загрузки
        if (isLoading) {
            Text("⏳ Идет генерация документа...")
        }
    }

    // Диалог редактирования студента
    if (showEditDialog) {
        SimpleStudentEditDialog(
            student = selectedStudent,
            viewModel = viewModel,
            onDismiss = { viewModel.closeStudentEditDialog() }
        )
    }
}

/**
 * Упрощенный диалог редактирования студента для экрана списка студентов
 */
@Composable
private fun SimpleStudentEditDialog(
    student: Student?,
    viewModel: NornViewModel,
    onDismiss: () -> Unit
) {
    // Локальные состояния для редактирования
    var editedFullName by remember { mutableStateOf("") }
    var editedGroup by remember { mutableStateOf("") }
    var editedFundingType by remember { mutableStateOf("") }
    var editedBranch by remember { mutableStateOf("") }
    var editedFaculty by remember { mutableStateOf("") }
    var editedIsPaidPractice by remember { mutableStateOf("") }
    var editedPracticeForm by remember { mutableStateOf("") }

    // Обновляем поля когда выбираем студента
    LaunchedEffect(student) {
        student?.let {
            editedFullName = it.name
            editedFundingType = it.formOfStudy
            editedBranch = it.cityOfPractice
            editedFaculty = it.nameOfDirection
            editedGroup = it.group
            editedIsPaidPractice = if (it.isPaidPractice) "Да" else "Нет"
            editedPracticeForm = it.practiceForm
        }
    }

    if (student != null) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("✏️ Редактировать данные студента") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // ФИО студента
                    OutlinedTextField(
                        value = editedFullName,
                        onValueChange = { editedFullName = it },
                        label = { Text("ФИО студента") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Группа
                    OutlinedTextField(
                        value = editedGroup,
                        onValueChange = { editedGroup = it },
                        label = { Text("Группа") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Форма обучения
                    OutlinedTextField(
                        value = editedFundingType,
                        onValueChange = { editedFundingType = it },
                        label = { Text("Направление (бюджет/платное/льготный)") },
                        placeholder = { Text("Например: бюджет, платное, льготный прием") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Филиал/город
                    OutlinedTextField(
                        value = editedBranch,
                        onValueChange = { editedBranch = it },
                        label = { Text("Филиал") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Факультет
                    OutlinedTextField(
                        value = editedFaculty,
                        onValueChange = { editedFaculty = it },
                        label = { Text("Факультет") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Платная практика
                    OutlinedTextField(
                        value = editedIsPaidPractice,
                        onValueChange = { editedIsPaidPractice = it },
                        label = { Text("Платная практика") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Форма практики
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
                            // Создаем обновленного студента с новыми данными
                            val updatedStudent = ru.bpo.norn.commonMain.models.Student(
                                name = editedFullName,
                                course = student.course,
                                codeOfDirection = student.codeOfDirection,
                                nameOfDirection = editedFaculty,
                                group = editedGroup,
                                isForeign = student.isForeign,
                                gradeForPractice = student.gradeForPractice,
                                nameOfPracticeBase = student.nameOfPracticeBase,
                                typeOfPractice = student.typeOfPractice,
                                periodOfPractice = student.periodOfPractice,
                                formOfStudy = editedFundingType,
                                withPayment = student.withPayment,
                                isPaidPractice = editedIsPaidPractice == "Да",
                                practiceForm = editedPracticeForm,
                                cityOfPractice = editedBranch,
                                nameOfSpeciality = student.nameOfSpeciality,
                                codeOfSpeciality = student.codeOfSpeciality,
                                headOfPracticeFromDepartment = student.headOfPracticeFromDepartment,
                                headOfPracticeFromPracticeBase = student.headOfPracticeFromPracticeBase,
                                postOfHeadOfPracticeFromPracticeBase = student.postOfHeadOfPracticeFromPracticeBase,
                                postOfHeadOfPracticeFromDepartment = student.postOfHeadOfPracticeFromDepartment,
                                directorName = student.directorName,
                            )
                            viewModel.updateStudentData(updatedStudent)
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
}