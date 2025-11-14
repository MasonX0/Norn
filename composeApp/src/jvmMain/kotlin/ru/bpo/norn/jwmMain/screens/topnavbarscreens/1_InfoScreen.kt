package ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.bpo.norn.commonMain.models.Group
import ru.bpo.norn.commonMain.models.Student
import ru.bpo.norn.commonMain.models.Enterprise
import ru.bpo.norn.commonMain.models.PracticeSupervisor
import ru.bpo.norn.jwmMain.viewmodel.NornViewModel

/**
 * Основной экран информации и управления данными студентов
 * Позволяет выбирать группы, просматривать студентов и редактировать их данные
 * @param viewModel ViewModel для управления данными студентов и групп
 */
@Composable
fun InfoScreen(viewModel: NornViewModel) {
    // Подписка на состояния из ViewModel
    val groups by viewModel.groups.collectAsState()
    val selectedGroup by viewModel.selectedGroup.collectAsState()
    val selectedStudent by viewModel.selectedStudent.collectAsState()
    val showEditDialog by viewModel.showStudentEditDialog.collectAsState()
    val enterprisesList by viewModel.enterprisesList.collectAsState()

    // Локальные состояния для диалогов
    var showGroupDialog by remember { mutableStateOf(false) }
    var showGroupEditDialog by remember { mutableStateOf(false) }

    // Собираем статистику по всем студентам во всех группах
    val allStudents = groups.flatMap { it.students }
    val budgetStudents = allStudents.filter { it.formOfStudy.contains("Бюджет", ignoreCase = true) }
    val paidStudents = allStudents.filter { it.formOfStudy.contains("Платн", ignoreCase = true) }
    val targetStudents = allStudents.filter { it.formOfStudy.contains("Целев", ignoreCase = true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(15.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        // Заголовок экрана
        Text("Панель управления данными студентов", style = MaterialTheme.typography.headlineSmall)

        // Секция выбора группы
        if (groups.isNotEmpty()) {
            GroupSelectionSection(
                groups = groups,
                selectedGroup = selectedGroup,
                onGroupSelect = { viewModel.selectGroup(it) }
            )
        }

        // Секция информации о выбранной группе
        selectedGroup?.let { group ->
            GroupInfoSection(
                group = group,
                onGroupFillClick = { showGroupDialog = true },
                onGroupEditClick = { showGroupEditDialog = true }
            )

            // Список студентов выбранной группы
            StudentsListSection(
                group = group,
                onStudentSelect = { viewModel.selectStudentForEditing(it) }
            )
        }

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
                } else {
                    // Кнопка для создания статистики по базам практики
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            viewModel.generatePracticeBasesStatisticsExcel()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text("📊 Создать Excel со статистикой по базам практики")
                    }

                    Text(
                        "💡 Создаст Excel файл с распределением студентов по базам практики",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Статус генерации статистики
                    val statisticsStatus by viewModel.statisticsGenerationStatus.collectAsState()
                    if (statisticsStatus.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = when {
                                    statisticsStatus.startsWith("✅") -> androidx.compose.ui.graphics.Color(
                                        0xFF4CAF50
                                    ).copy(alpha = 0.1f)

                                    statisticsStatus.startsWith("❌") -> androidx.compose.ui.graphics.Color(
                                        0xFFF44336
                                    ).copy(alpha = 0.1f)

                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            )
                        ) {
                            Text(
                                text = statisticsStatus,
                                modifier = Modifier.padding(12.dp),
                                color = when {
                                    statisticsStatus.startsWith("✅") -> androidx.compose.ui.graphics.Color(
                                        0xFF2E7D32
                                    )

                                    statisticsStatus.startsWith("❌") -> androidx.compose.ui.graphics.Color(
                                        0xFFD32F2F
                                    )

                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }

        // Заглушка когда нет групп
        if (groups.isEmpty()) {
            NoGroupsPlaceholder()
        }
    }

    // Диалог массового заполнения данных группы
    if (showGroupDialog) {
        selectedGroup?.let { group ->
            GroupFillDialog(
                enterprisesList = enterprisesList,
                viewModel = viewModel,
                currentGroup = group,
                onDismiss = { showGroupDialog = false }
            )
        }
    }

    // Диалог редактирования параметров группы
    if (showGroupEditDialog) {
        selectedGroup?.let { group ->
            GroupParametersEditDialog(
                currentGroup = group,
                viewModel = viewModel,
                onDismiss = { showGroupEditDialog = false }
            )
        }
    }

    // Диалог редактирования отдельного студента
    if (showEditDialog) {
        StudentEditDialog(
            student = selectedStudent,
            enterprisesList = enterprisesList,
            viewModel = viewModel,
            onDismiss = { viewModel.closeStudentEditDialog() },
            onSave = { updatedStudent ->
                viewModel.updateStudentData(updatedStudent)
            }
        )
    }
}

/**
 * Диалог массового заполнения данных для всей группы
 * Позволяет заполнить одинаковые поля для всех студентов группы
 */
@Composable
private fun GroupFillDialog(
    enterprisesList: List<Enterprise>,
    viewModel: NornViewModel,
    currentGroup: Group,
    onDismiss: () -> Unit
) {
    // Состояния для всех полей, которые можно заполнить
    var selectedEnterprise by remember { mutableStateOf<Enterprise?>(null) }
    var nameOfPracticeBase by remember { mutableStateOf("") }
    var typeOfPractice by remember { mutableStateOf("") }
    var periodOfPractice by remember { mutableStateOf("") }
    var cityOfPractice by remember { mutableStateOf("") }
    var headOfPracticeFromDepartment by remember { mutableStateOf("") }
    var postOfHeadOfPracticeFromDepartment by remember { mutableStateOf("") }
    var headOfPracticeFromPracticeBase by remember { mutableStateOf("") }
    var postOfHeadOfPracticeFromPracticeBase by remember { mutableStateOf("") }
    var directorName by remember { mutableStateOf("") }
    var codeOfDirection by remember { mutableStateOf(currentGroup.codeOfDirection) }
    var nameOfDirection by remember { mutableStateOf(currentGroup.nameOfDirection) }
    var nameOfSpeciality by remember { mutableStateOf("") }
    var codeOfSpeciality by remember { mutableStateOf("") }
    var practiceForm by remember { mutableStateOf("") }
    var formOfStudy by remember { mutableStateOf("") }
    var isForeign by remember { mutableStateOf(false) }
    var isPaidPractice by remember { mutableStateOf(false) }
    var gradeForPractice by remember { mutableStateOf("") }

    // Флаги для отслеживания измененных полей (только измененные поля будут применены)
    var isNameOfPracticeBaseSet by remember { mutableStateOf(false) }
    var isTypeOfPracticeSet by remember { mutableStateOf(false) }
    var isPeriodOfPracticeSet by remember { mutableStateOf(false) }
    var isCityOfPracticeSet by remember { mutableStateOf(false) }
    var isHeadOfPracticeFromDepartmentSet by remember { mutableStateOf(false) }
    var isPostOfHeadOfPracticeFromDepartmentSet by remember { mutableStateOf(false) }
    var isHeadOfPracticeFromPracticeBaseSet by remember { mutableStateOf(false) }
    var isPostOfHeadOfPracticeFromPracticeBaseSet by remember { mutableStateOf(false) }
    var isDirectorNameSet by remember { mutableStateOf(false) }
    var isCodeOfDirectionSet by remember { mutableStateOf(true) }
    var isNameOfDirectionSet by remember { mutableStateOf(true) }
    var isNameOfSpecialitySet by remember { mutableStateOf(false) }
    var isCodeOfSpecialitySet by remember { mutableStateOf(false) }
    var isPracticeFormSet by remember { mutableStateOf(false) }
    var isFormOfStudySet by remember { mutableStateOf(false) }
    var isForeignSet by remember { mutableStateOf(false) }
    var isPaidPracticeSet by remember { mutableStateOf(false) }
    var isGradeForPracticeSet by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("🏫 Заполнить данные для всей группы") },
        text = {
            Divider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                thickness = 1.dp,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            // Универсальная форма редактирования
            EditFormContent(
                selectedEnterprise = selectedEnterprise,
                onEnterpriseSelected = { enterprise ->
                    selectedEnterprise = enterprise
                    nameOfPracticeBase = enterprise.name
                    isNameOfPracticeBaseSet = true
                    cityOfPractice = enterprise.city ?: ""
                    isCityOfPracticeSet = true
                },
                nameOfPracticeBase = nameOfPracticeBase,
                onNameOfPracticeBaseChange = {
                    nameOfPracticeBase = it
                    isNameOfPracticeBaseSet = true
                },
                typeOfPractice = typeOfPractice,
                onTypeOfPracticeChange = {
                    typeOfPractice = it
                    isTypeOfPracticeSet = true
                },
                periodOfPractice = periodOfPractice,
                onPeriodOfPracticeChange = {
                    periodOfPractice = it
                    isPeriodOfPracticeSet = true
                },
                cityOfPractice = cityOfPractice,
                onCityOfPracticeChange = {
                    cityOfPractice = it
                    isCityOfPracticeSet = true
                },
                headOfPracticeFromDepartment = headOfPracticeFromDepartment,
                onHeadOfPracticeFromDepartmentChange = {
                    headOfPracticeFromDepartment = it
                    isHeadOfPracticeFromDepartmentSet = true
                },
                postOfHeadOfPracticeFromDepartment = postOfHeadOfPracticeFromDepartment,
                onPostOfHeadOfPracticeFromDepartmentChange = {
                    postOfHeadOfPracticeFromDepartment = it
                    isPostOfHeadOfPracticeFromDepartmentSet = true
                },
                headOfPracticeFromPracticeBase = headOfPracticeFromPracticeBase,
                onHeadOfPracticeFromPracticeBaseChange = {
                    headOfPracticeFromPracticeBase = it
                    isHeadOfPracticeFromPracticeBaseSet = true
                },
                postOfHeadOfPracticeFromPracticeBase = postOfHeadOfPracticeFromPracticeBase,
                onPostOfHeadOfPracticeFromPracticeBaseChange = {
                    postOfHeadOfPracticeFromPracticeBase = it
                    isPostOfHeadOfPracticeFromPracticeBaseSet = true
                },
                directorName = directorName,
                onDirectorNameChange = {
                    directorName = it
                    isDirectorNameSet = true
                },
                codeOfDirection = codeOfDirection,
                onCodeOfDirectionChange = {
                    codeOfDirection = it
                    isCodeOfDirectionSet = true
                },
                nameOfDirection = nameOfDirection,
                onNameOfDirectionChange = {
                    nameOfDirection = it
                    isNameOfDirectionSet = true
                },
                nameOfSpeciality = nameOfSpeciality,
                onNameOfSpecialityChange = {
                    nameOfSpeciality = it
                    isNameOfSpecialitySet = true
                },
                codeOfSpeciality = codeOfSpeciality,
                onCodeOfSpecialityChange = {
                    codeOfSpeciality = it
                    isCodeOfSpecialitySet = true
                },
                practiceForm = practiceForm,
                onPracticeFormChange = {
                    practiceForm = it
                    isPracticeFormSet = true
                },
                formOfStudy = formOfStudy,
                onFormOfStudyChange = {
                    formOfStudy = it
                    isFormOfStudySet = true
                },
                isForeign = isForeign,
                onIsForeignChange = {
                    isForeign = it
                    isForeignSet = true
                },
                isPaidPractice = isPaidPractice,
                onIsPaidPracticeChange = {
                    isPaidPractice = it
                    isPaidPracticeSet = true
                },
                gradeForPractice = gradeForPractice,
                onGradeForPracticeChange = {
                    gradeForPractice = it
                    isGradeForPracticeSet = true
                },
                enterprisesList = enterprisesList,
                isStudentDialog = false
            )
        },
        confirmButton = {
            Button(onClick = {
                // Применяем изменения только к измененным полям
                ApplyGroupChanges(
                    viewModel = viewModel,
                    currentGroup = currentGroup,
                    changes = GroupChanges(
                        nameOfPracticeBase = if (isNameOfPracticeBaseSet) nameOfPracticeBase else null,
                        typeOfPractice = if (isTypeOfPracticeSet) typeOfPractice else null,
                        periodOfPractice = if (isPeriodOfPracticeSet) periodOfPractice else null,
                        cityOfPractice = if (isCityOfPracticeSet) cityOfPractice else null,
                        headOfPracticeFromDepartment = if (isHeadOfPracticeFromDepartmentSet) headOfPracticeFromDepartment else null,
                        postOfHeadOfPracticeFromDepartment = if (isPostOfHeadOfPracticeFromDepartmentSet) postOfHeadOfPracticeFromDepartment else null,
                        headOfPracticeFromPracticeBase = if (isHeadOfPracticeFromPracticeBaseSet) headOfPracticeFromPracticeBase else null,
                        postOfHeadOfPracticeFromPracticeBase = if (isPostOfHeadOfPracticeFromPracticeBaseSet) postOfHeadOfPracticeFromPracticeBase else null,
                        directorName = if (isDirectorNameSet) directorName else null,
                        codeOfDirection = if (isCodeOfDirectionSet) codeOfDirection else null,
                        nameOfDirection = if (isNameOfDirectionSet) nameOfDirection else null,
                        nameOfSpeciality = if (isNameOfSpecialitySet) nameOfSpeciality else null,
                        codeOfSpeciality = if (isCodeOfSpecialitySet) codeOfSpeciality else null,
                        practiceForm = if (isPracticeFormSet) practiceForm else null,
                        formOfStudy = if (isFormOfStudySet) formOfStudy else null,
                        isForeign = if (isForeignSet) isForeign else null,
                        isPaidPractice = if (isPaidPracticeSet) isPaidPractice else null,
                        gradeForPractice = if (isGradeForPracticeSet) gradeForPractice else null
                    )
                )
                onDismiss()
            }) {
                Text("Применить ко всей группе")
            }
        }
    )
}

/**
 * Вспомогательная функция для применения изменений к группе
 */
private fun ApplyGroupChanges(
    viewModel: NornViewModel,
    currentGroup: Group,
    changes: GroupChanges
) {
    // Обновляем группу если изменились её параметры
    if (changes.codeOfDirection != null || changes.nameOfDirection != null) {
        val updatedGroup = currentGroup.copy(
            codeOfDirection = changes.codeOfDirection ?: currentGroup.codeOfDirection,
            nameOfDirection = changes.nameOfDirection ?: currentGroup.nameOfDirection
        )
        viewModel.updateGroupData(updatedGroup)
    }

    // Обновляем всех студентов в группе
    val updatedStudents = currentGroup.students.map { student ->
        Student(
            name = student.name,
            course = student.course,
            codeOfDirection = changes.codeOfDirection ?: student.codeOfDirection,
            nameOfDirection = changes.nameOfDirection ?: student.nameOfDirection,
            group = student.group,
            isForeign = changes.isForeign ?: student.isForeign,
            gradeForPractice = changes.gradeForPractice ?: student.gradeForPractice,
            nameOfPracticeBase = changes.nameOfPracticeBase ?: student.nameOfPracticeBase,
            typeOfPractice = changes.typeOfPractice ?: student.typeOfPractice,
            periodOfPractice = changes.periodOfPractice ?: student.periodOfPractice,
            practiceForm = changes.practiceForm ?: student.practiceForm,
            formOfStudy = changes.formOfStudy ?: student.formOfStudy,
            isPaidPractice = changes.isPaidPractice ?: student.isPaidPractice,
            cityOfPractice = changes.cityOfPractice ?: student.cityOfPractice,
            nameOfSpeciality = changes.nameOfSpeciality ?: student.nameOfSpeciality,
            codeOfSpeciality = changes.codeOfSpeciality ?: student.codeOfSpeciality,
            headOfPracticeFromDepartment = changes.headOfPracticeFromDepartment
                ?: student.headOfPracticeFromDepartment,
            headOfPracticeFromPracticeBase = changes.headOfPracticeFromPracticeBase
                ?: student.headOfPracticeFromPracticeBase,
            postOfHeadOfPracticeFromPracticeBase = changes.postOfHeadOfPracticeFromPracticeBase
                ?: student.postOfHeadOfPracticeFromPracticeBase,
            postOfHeadOfPracticeFromDepartment = changes.postOfHeadOfPracticeFromDepartment
                ?: student.postOfHeadOfPracticeFromDepartment,
            directorName = changes.directorName ?: student.directorName,
            withPayment = changes.isPaidPractice ?: student.withPayment
        )
    }

    // Обновляем студентов в группе одним вызовом
    viewModel.updateGroupStudents(currentGroup.name, updatedStudents)
}

/**
 * Класс для хранения изменений, которые нужно применить к группе
 */
private data class GroupChanges(
    val nameOfPracticeBase: String? = null,
    val typeOfPractice: String? = null,
    val periodOfPractice: String? = null,
    val cityOfPractice: String? = null,
    val headOfPracticeFromDepartment: String? = null,
    val postOfHeadOfPracticeFromDepartment: String? = null,
    val headOfPracticeFromPracticeBase: String? = null,
    val postOfHeadOfPracticeFromPracticeBase: String? = null,
    val directorName: String? = null,
    val codeOfDirection: String? = null,
    val nameOfDirection: String? = null,
    val nameOfSpeciality: String? = null,
    val codeOfSpeciality: String? = null,
    val practiceForm: String? = null,
    val formOfStudy: String? = null,
    val isForeign: Boolean? = null,
    val isPaidPractice: Boolean? = null,
    val gradeForPractice: String? = null
)

@Composable
private fun GroupParametersEditDialog(
    currentGroup: Group,
    viewModel: NornViewModel,
    onDismiss: () -> Unit
) {
    var groupName by remember { mutableStateOf(currentGroup.name) }
    var codeOfDirection by remember { mutableStateOf(currentGroup.codeOfDirection) }
    var nameOfDirection by remember { mutableStateOf(currentGroup.nameOfDirection) }
    var course by remember { mutableStateOf(currentGroup.course.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("⚙️ Редактировать параметры группы") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = groupName,
                    onValueChange = { groupName = it },
                    label = { Text("Название группы") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = codeOfDirection,
                        onValueChange = { codeOfDirection = it },
                        label = { Text("Код направления") },
                        placeholder = { Text("09.03.01") },
                        modifier = Modifier.weight(2f)
                    )
                    OutlinedTextField(
                        value = course,
                        onValueChange = { newValue ->
                            // Разрешаем только цифры от 1 до 6
                            if (newValue.isEmpty() || (newValue.toIntOrNull()
                                    ?.let { it in 1..6 } == true)
                            ) {
                                course = newValue
                            }
                        },
                        label = { Text("Курс") },
                        placeholder = { Text("1-6") },
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = nameOfDirection,
                    onValueChange = { nameOfDirection = it },
                    label = { Text("Название направления") },
                    placeholder = { Text("Информатика и вычислительная техника") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "💡 Изменения применятся к группе и всем её студентам",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "📚 Курс рассчитывается автоматически из названия группы, но можно изменить вручную",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                // Создаем обновленную группу
                val updatedGroup = currentGroup.copy(
                    name = groupName,
                    codeOfDirection = codeOfDirection,
                    nameOfDirection = nameOfDirection,
                    course = course.toIntOrNull() ?: currentGroup.course
                )

                // Обновляем группу
                viewModel.updateGroupData(updatedGroup)

                // Обновляем всех студентов в группе с новыми данными направления
                currentGroup.students.forEach { student ->
                    val updatedStudent = Student(
                        name = student.name,
                        course = course.toIntOrNull() ?: student.course,
                        codeOfDirection = codeOfDirection,
                        nameOfDirection = nameOfDirection,
                        group = groupName,
                        isForeign = student.isForeign,
                        gradeForPractice = student.gradeForPractice,
                        nameOfPracticeBase = student.nameOfPracticeBase,
                        typeOfPractice = student.typeOfPractice,
                        periodOfPractice = student.periodOfPractice,
                        formOfStudy = student.formOfStudy,
                        withPayment = student.withPayment,
                        isPaidPractice = student.isPaidPractice,
                        practiceForm = student.practiceForm,
                        cityOfPractice = student.cityOfPractice,
                        nameOfSpeciality = student.nameOfSpeciality,
                        codeOfSpeciality = student.codeOfSpeciality,
                        headOfPracticeFromDepartment = student.headOfPracticeFromDepartment,
                        headOfPracticeFromPracticeBase = student.headOfPracticeFromPracticeBase,
                        postOfHeadOfPracticeFromPracticeBase = student.postOfHeadOfPracticeFromPracticeBase,
                        postOfHeadOfPracticeFromDepartment = student.postOfHeadOfPracticeFromDepartment,
                        directorName = student.directorName
                    )
                    viewModel.updateStudentData(updatedStudent)
                }

                onDismiss()
            }) {
                Text("Сохранить изменения")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

@Composable
private fun StudentEditDialog(
    student: Student?,
    enterprisesList: List<Enterprise>,
    viewModel: NornViewModel,
    onDismiss: () -> Unit,
    onSave: (Student) -> Unit
) {
    var name by remember { mutableStateOf(student?.name ?: "") }
    var course by remember { mutableStateOf(student?.course?.toString() ?: "") }
    var group by remember { mutableStateOf(student?.group ?: "") }
    var selectedEnterprise by remember { mutableStateOf<Enterprise?>(null) }
    var nameOfPracticeBase by remember { mutableStateOf(student?.nameOfPracticeBase ?: "") }
    var typeOfPractice by remember { mutableStateOf(student?.typeOfPractice ?: "") }
    var periodOfPractice by remember { mutableStateOf(student?.periodOfPractice ?: "") }
    var cityOfPractice by remember { mutableStateOf(student?.cityOfPractice ?: "") }
    var headOfPracticeFromDepartment by remember { mutableStateOf(student?.headOfPracticeFromDepartment ?: "") }
    var postOfHeadOfPracticeFromDepartment by remember { mutableStateOf(student?.postOfHeadOfPracticeFromDepartment ?: "") }
    var headOfPracticeFromPracticeBase by remember { mutableStateOf(student?.headOfPracticeFromPracticeBase ?: "") }
    var postOfHeadOfPracticeFromPracticeBase by remember { mutableStateOf(student?.postOfHeadOfPracticeFromPracticeBase ?: "") }
    var directorName by remember { mutableStateOf(student?.directorName ?: "") }
    // Получаем данные о направлении из выбранной группы через viewModel
    val selectedGroup by viewModel.selectedGroup.collectAsState()
    var codeOfDirection by remember {
        mutableStateOf(
            selectedGroup?.codeOfDirection ?: student?.codeOfDirection ?: ""
        )
    }
    var nameOfDirection by remember {
        mutableStateOf(
            selectedGroup?.nameOfDirection ?: student?.nameOfDirection ?: ""
        )
    }
    var nameOfSpeciality by remember { mutableStateOf(student?.nameOfSpeciality ?: "") }
    var codeOfSpeciality by remember { mutableStateOf(student?.codeOfSpeciality ?: "") }
    var practiceForm by remember { mutableStateOf(student?.practiceForm ?: "") }
    var formOfStudy by remember { mutableStateOf(student?.formOfStudy ?: "") }
    var isForeign by remember { mutableStateOf(student?.isForeign ?: false) }
    var isPaidPractice by remember { mutableStateOf(student?.isPaidPractice ?: false) }
    var gradeForPractice by remember { mutableStateOf(student?.gradeForPractice ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("✏️ Редактировать студента") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("ФИО студента *") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = course,
                        onValueChange = { course = it },
                        label = { Text("Курс *") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = group,
                        onValueChange = { group = it },
                        label = { Text("Группа *") },
                        modifier = Modifier.weight(1f)
                    )
                }
                EditFormContent(
                    selectedEnterprise = selectedEnterprise,
                    onEnterpriseSelected = { enterprise ->
                        selectedEnterprise = enterprise
                        nameOfPracticeBase = enterprise.name
                        cityOfPractice = enterprise.city ?: ""
                    },
                    nameOfPracticeBase = nameOfPracticeBase,
                    onNameOfPracticeBaseChange = { nameOfPracticeBase = it },
                    typeOfPractice = typeOfPractice,
                    onTypeOfPracticeChange = { typeOfPractice = it },
                    periodOfPractice = periodOfPractice,
                    onPeriodOfPracticeChange = { periodOfPractice = it },
                    cityOfPractice = cityOfPractice,
                    onCityOfPracticeChange = { cityOfPractice = it },
                    headOfPracticeFromDepartment = headOfPracticeFromDepartment,
                    onHeadOfPracticeFromDepartmentChange = { headOfPracticeFromDepartment = it },
                    postOfHeadOfPracticeFromDepartment = postOfHeadOfPracticeFromDepartment,
                    onPostOfHeadOfPracticeFromDepartmentChange = { postOfHeadOfPracticeFromDepartment = it },
                    headOfPracticeFromPracticeBase = headOfPracticeFromPracticeBase,
                    onHeadOfPracticeFromPracticeBaseChange = { headOfPracticeFromPracticeBase = it },
                    postOfHeadOfPracticeFromPracticeBase = postOfHeadOfPracticeFromPracticeBase,
                    onPostOfHeadOfPracticeFromPracticeBaseChange = { postOfHeadOfPracticeFromPracticeBase = it },
                    directorName = directorName,
                    onDirectorNameChange = { directorName = it },
                    codeOfDirection = codeOfDirection,
                    onCodeOfDirectionChange = { codeOfDirection = it },
                    nameOfDirection = nameOfDirection,
                    onNameOfDirectionChange = { nameOfDirection = it },
                    nameOfSpeciality = nameOfSpeciality,
                    onNameOfSpecialityChange = { nameOfSpeciality = it },
                    codeOfSpeciality = codeOfSpeciality,
                    onCodeOfSpecialityChange = { codeOfSpeciality = it },
                    practiceForm = practiceForm,
                    onPracticeFormChange = { practiceForm = it },
                    formOfStudy = formOfStudy,
                    onFormOfStudyChange = { formOfStudy = it },
                    isForeign = isForeign,
                    onIsForeignChange = { isForeign = it },
                    isPaidPractice = isPaidPractice,
                    onIsPaidPracticeChange = { isPaidPractice = it },
                    gradeForPractice = gradeForPractice,
                    onGradeForPracticeChange = { gradeForPractice = it },
                    enterprisesList = enterprisesList,
                    isStudentDialog = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    student?.let {
                        onSave(
                            Student(
                                name = name,
                                course = course.toIntOrNull() ?: it.course,
                                codeOfDirection = codeOfDirection,
                                nameOfDirection = nameOfDirection,
                                group = group,
                                isForeign = isForeign,
                                gradeForPractice = gradeForPractice,
                                nameOfPracticeBase = nameOfPracticeBase,
                                typeOfPractice = typeOfPractice,
                                periodOfPractice = periodOfPractice,
                                practiceForm = practiceForm,
                                formOfStudy = formOfStudy,
                                isPaidPractice = isPaidPractice,
                                cityOfPractice = cityOfPractice,
                                nameOfSpeciality = nameOfSpeciality,
                                codeOfSpeciality = codeOfSpeciality,
                                headOfPracticeFromDepartment = headOfPracticeFromDepartment,
                                headOfPracticeFromPracticeBase = headOfPracticeFromPracticeBase,
                                postOfHeadOfPracticeFromPracticeBase = postOfHeadOfPracticeFromPracticeBase,
                                postOfHeadOfPracticeFromDepartment = postOfHeadOfPracticeFromDepartment,
                                directorName = directorName,
                                withPayment = isPaidPractice
                            )
                        )
                    }
                },
                enabled = name.isNotBlank() && course.isNotBlank() && group.isNotBlank()
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}

/**
 * Секция выбора группы из выпадающего списка
 */
@Composable
private fun GroupSelectionSection(
    groups: List<Group>,
    selectedGroup: Group?,
    onGroupSelect: (Group) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("🎓 Выберите группу:", style = MaterialTheme.typography.titleMedium)

            var expanded by remember { mutableStateOf(false) }
            var selectedGroupName by remember {
                mutableStateOf(selectedGroup?.name ?: "Выберите группу")
            }

            // Выпадающий список групп
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    value = selectedGroupName,
                    onValueChange = {},
                    label = { Text("Группа") },
                    trailingIcon = {
                        TextButton(onClick = { expanded = !expanded }) {
                            Text(if (expanded) "▲" else "▼")
                        }
                    }
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    groups.forEach { group ->
                        DropdownMenuItem(
                            text = {
                                Text("${group.name} (${group.students.size} студентов)")
                            },
                            onClick = {
                                onGroupSelect(group)
                                selectedGroupName = group.name
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Секция с информацией о выбранной группе и кнопками управления
 */
@Composable
private fun GroupInfoSection(
    group: Group,
    onGroupFillClick: () -> Unit,
    onGroupEditClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Информация о группе
                Column {
                    Text("🎓 Группа: ${group.name}", style = MaterialTheme.typography.titleMedium)
                    Text("Студентов: ${group.students.size}")
                    Text("Курс: ${group.course}")
                    Text("Направление: ${group.nameOfDirection}")
                    Text("Код направления: ${group.codeOfDirection}")
                }

                // Кнопки управления группой
                Column {
                    Button(onClick = onGroupFillClick) {
                        Text("📝 Заполнить для группы")
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = onGroupEditClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text("⚙️ Изменить группу")
                    }
                }
            }
        }
    }
}

/**
 * Секция со списком студентов выбранной группы
 */
@Composable
private fun StudentsListSection(
    group: Group,
    onStudentSelect: (Student) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(width = 2.dp, color = MaterialTheme.colorScheme.primary)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "👤 Список студентов:",
                modifier = Modifier.padding(bottom = 10.dp),
                style = MaterialTheme.typography.titleMedium
            )
            Divider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                thickness = 1.dp,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            // Прокручиваемый список студентов
            LazyColumn(modifier = Modifier.height(300.dp)) {
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
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        onClick = onSelect,
        border = BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.tertiary)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(student.name, style = MaterialTheme.typography.bodyMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Курс: ${student.course}")
                Text("Группа: ${student.group}")
            }
            Text("База: ${student.nameOfPracticeBase}")
            Text("Город: ${student.cityOfPractice}")
            Text("Оценка: ${student.gradeForPractice}")
        }
    }
}

/**
 * Заглушка когда нет загруженных групп
 */
@Composable
private fun NoGroupsPlaceholder() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "📝 Нет загруженных групп",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Перейдите на экран 'Списки студентов' чтобы загрузить группы",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Универсальная форма редактирования данных студента/группы
 * Содержит все поля для редактирования информации о практике
 */
@Composable
private fun EditFormContent(
    selectedEnterprise: Enterprise?,
    onEnterpriseSelected: (Enterprise) -> Unit,
    nameOfPracticeBase: String,
    onNameOfPracticeBaseChange: (String) -> Unit,
    typeOfPractice: String,
    onTypeOfPracticeChange: (String) -> Unit,
    periodOfPractice: String,
    onPeriodOfPracticeChange: (String) -> Unit,
    cityOfPractice: String,
    onCityOfPracticeChange: (String) -> Unit,
    headOfPracticeFromDepartment: String,
    onHeadOfPracticeFromDepartmentChange: (String) -> Unit,
    postOfHeadOfPracticeFromDepartment: String,
    onPostOfHeadOfPracticeFromDepartmentChange: (String) -> Unit,
    headOfPracticeFromPracticeBase: String,
    onHeadOfPracticeFromPracticeBaseChange: (String) -> Unit,
    postOfHeadOfPracticeFromPracticeBase: String,
    onPostOfHeadOfPracticeFromPracticeBaseChange: (String) -> Unit,
    directorName: String,
    onDirectorNameChange: (String) -> Unit,
    codeOfDirection: String,
    onCodeOfDirectionChange: (String) -> Unit,
    nameOfDirection: String,
    onNameOfDirectionChange: (String) -> Unit,
    nameOfSpeciality: String,
    onNameOfSpecialityChange: (String) -> Unit,
    codeOfSpeciality: String,
    onCodeOfSpecialityChange: (String) -> Unit,
    practiceForm: String,
    onPracticeFormChange: (String) -> Unit,
    formOfStudy: String,
    onFormOfStudyChange: (String) -> Unit,
    isForeign: Boolean,
    onIsForeignChange: (Boolean) -> Unit,
    isPaidPractice: Boolean,
    onIsPaidPracticeChange: (Boolean) -> Unit,
    gradeForPractice: String,
    onGradeForPracticeChange: (String) -> Unit,
    enterprisesList: List<Enterprise>,
    isStudentDialog: Boolean
) {
    // Состояние для выбранного руководителя практики от предприятия
    var selectedSupervisor by remember { mutableStateOf<PracticeSupervisor?>(null) }

    // Состояния для выпадающих списков (формы, формы обучения, оплаты, оценки)
    var practiceFormExpanded by remember { mutableStateOf(false) }
    var formOfStudyExpanded by remember { mutableStateOf(false) }
    var practicePaymentExpanded by remember { mutableStateOf(false) }
    var gradeExpanded by remember { mutableStateOf(false) }

    // Варианты для выпадающих списков
    val practiceFormOptions = listOf("стационарная", "выездная")
    val formOfStudyOptions = listOf("Бюджетная", "Платная", "Целевая")
    val practicePaymentOptions = listOf("Оплачиваемая", "Неоплачиваемая")
    val gradeOptions = listOf("отлично", "хорошо", "удовлетворительно", "неудов")

    /**
     * Функция для определения кастомной цветовой схемы для заполненных полей формы
     */
    @Composable
    fun fieldColors(value: String): TextFieldColors {
        return if (value.isNotEmpty()) {
            OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.background.copy(alpha = 1.0f),
                unfocusedContainerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.9f)
            )
        } else {
            OutlinedTextFieldDefaults.colors()
        }
    }

    LazyColumn(
        modifier = if (isStudentDialog) Modifier.height(350.dp) else Modifier.height(400.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Блок выбора места практики, предприятия и руководителя от базы
        item {
            Text("Место практики:", style = MaterialTheme.typography.titleSmall)
            // Выпадающий список предприятий
            EnterpriseDropdown(
                enterprisesList = enterprisesList,
                selectedEnterprise = selectedEnterprise,
                onEnterpriseSelected = onEnterpriseSelected
            )
            OutlinedTextField(
                value = nameOfPracticeBase,
                onValueChange = onNameOfPracticeBaseChange,
                label = { Text("База практики") },
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors(nameOfPracticeBase)
            )

            // Выпадающий список руководителей практики от предприятия
            if (selectedEnterprise != null && selectedEnterprise.supervisors.isNotEmpty()) {
                SupervisorDropdown(
                    supervisors = selectedEnterprise.supervisors,
                    selectedSupervisor = selectedSupervisor,
                    onSupervisorSelected = { supervisor ->
                        selectedSupervisor = supervisor
                        onHeadOfPracticeFromPracticeBaseChange(supervisor.fullName)
                        onPostOfHeadOfPracticeFromPracticeBaseChange(supervisor.position)
                    }
                )
            }
        }
        // Блок направления подготовки
        item {
            Text("Направление подготовки:", style = MaterialTheme.typography.titleSmall)
            OutlinedTextField(
                value = codeOfDirection,
                onValueChange = onCodeOfDirectionChange,
                label = { Text("Код направления") },
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors(codeOfDirection)
            )
            OutlinedTextField(
                value = nameOfDirection,
                onValueChange = onNameOfDirectionChange,
                label = { Text("Название направления") },
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors(nameOfDirection)
            )
        }
        // Блок информации о практике: тип, период, город, оценка, оплата
        item {
            Text("Информация о практике:", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = typeOfPractice,
                    onValueChange = onTypeOfPracticeChange,
                    label = { Text("Тип практики") },
                    modifier = Modifier.weight(1f),
                    colors = fieldColors(typeOfPractice)
                )
                OutlinedTextField(
                    value = periodOfPractice,
                    onValueChange = onPeriodOfPracticeChange,
                    label = { Text("Период практики") },
                    modifier = Modifier.weight(1f),
                    colors = fieldColors(periodOfPractice)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = cityOfPractice,
                    onValueChange = onCityOfPracticeChange,
                    label = { Text("Город практики") },
                    modifier = Modifier.weight(1f),
                    colors = fieldColors(cityOfPractice)
                )
                // Выпадающий список для оценки практики
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = gradeForPractice,
                        onValueChange = onGradeForPracticeChange,
                        label = { Text("Оценка") },
                        trailingIcon = {
                            TextButton(onClick = { gradeExpanded = !gradeExpanded }) {
                                Text(if (gradeExpanded) "▲" else "▼")
                            }
                        },
                        colors = fieldColors(gradeForPractice)
                    )
                    DropdownMenu(
                        expanded = gradeExpanded,
                        onDismissRequest = { gradeExpanded = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        gradeOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    onGradeForPracticeChange(option)
                                    gradeExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Выпадающий список для указания, оплачиваемая или нет практика, с возможностью ручного ввода
            Text("Оплата практики:", style = MaterialTheme.typography.bodySmall)
            var practicePaymentText by remember { mutableStateOf(if (isPaidPractice) "Оплачиваемая" else "Неоплачиваемая") }
            
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = practicePaymentText,
                    onValueChange = { 
                        practicePaymentText = it
                        onIsPaidPracticeChange(it.contains("оплач", ignoreCase = true) || it.contains("платн", ignoreCase = true))
                    },
                    label = { Text("Оплата практики") },
                    trailingIcon = {
                        TextButton(onClick = {
                            practicePaymentExpanded = !practicePaymentExpanded
                        }) {
                            Text(if (practicePaymentExpanded) "▲" else "▼")
                        }
                    },
                    colors = if (isPaidPractice) fieldColors("оплачиваемая") else OutlinedTextFieldDefaults.colors()
                )
                DropdownMenu(
                    expanded = practicePaymentExpanded,
                    onDismissRequest = { practicePaymentExpanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    practicePaymentOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                practicePaymentText = option
                                onIsPaidPracticeChange(option == "Оплачиваемая")
                                practicePaymentExpanded = false
                            }
                        )
                    }
                }
            }
        }
        // Блок информации о руководителях (от кафедры и от базы)
        item {
            Text("Руководители:", style = MaterialTheme.typography.titleSmall)
            OutlinedTextField(
                value = headOfPracticeFromDepartment,
                onValueChange = onHeadOfPracticeFromDepartmentChange,
                label = { Text("Руководитель от кафедры") },
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors(headOfPracticeFromDepartment)
            )
            OutlinedTextField(
                value = postOfHeadOfPracticeFromDepartment,
                onValueChange = onPostOfHeadOfPracticeFromDepartmentChange,
                label = { Text("Должность руков. от кафедры") },
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors(postOfHeadOfPracticeFromDepartment)
            )
            OutlinedTextField(
                value = headOfPracticeFromPracticeBase,
                onValueChange = onHeadOfPracticeFromPracticeBaseChange,
                label = { Text("Руководитель от базы") },
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors(headOfPracticeFromPracticeBase)
            )
            OutlinedTextField(
                value = postOfHeadOfPracticeFromPracticeBase,
                onValueChange = onPostOfHeadOfPracticeFromPracticeBaseChange,
                label = { Text("Должность руков. от базы") },
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors(postOfHeadOfPracticeFromPracticeBase)
            )
        }
        // Блок дополнительной информации и формы обучения/практики
        item {
            Text("Дополнительная информация:", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = nameOfSpeciality,
                    onValueChange = onNameOfSpecialityChange,
                    label = { Text("Специальность") },
                    modifier = Modifier.weight(1f),
                    colors = fieldColors(nameOfSpeciality)
                )
                OutlinedTextField(
                    value = codeOfSpeciality,
                    onValueChange = onCodeOfSpecialityChange,
                    label = { Text("Код специальности") },
                    modifier = Modifier.weight(1f),
                    colors = fieldColors(codeOfSpeciality)
                )
            }
            OutlinedTextField(
                value = directorName,
                onValueChange = onDirectorNameChange,
                label = { Text("Директор/декан") },
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors(directorName)
            )

            // Выпадающий список для формы практики
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = practiceForm,
                    onValueChange = onPracticeFormChange,
                    label = { Text("Форма практики") },
                    trailingIcon = {
                        TextButton(onClick = { practiceFormExpanded = !practiceFormExpanded }) {
                            Text(if (practiceFormExpanded) "▲" else "▼")
                        }
                    },
                    colors = fieldColors(practiceForm)
                )
                DropdownMenu(
                    expanded = practiceFormExpanded,
                    onDismissRequest = { practiceFormExpanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    practiceFormOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                onPracticeFormChange(option)
                                practiceFormExpanded = false
                            }
                        )
                    }
                }
            }
            // Выпадающий список для формы обучения
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = formOfStudy,
                    onValueChange = onFormOfStudyChange,
                    label = { Text("Форма обучения") },
                    trailingIcon = {
                        TextButton(onClick = { formOfStudyExpanded = !formOfStudyExpanded }) {
                            Text(if (formOfStudyExpanded) "▲" else "▼")
                        }
                    },
                    colors = fieldColors(formOfStudy)
                )
                DropdownMenu(
                    expanded = formOfStudyExpanded,
                    onDismissRequest = { formOfStudyExpanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    formOfStudyOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                onFormOfStudyChange(option)
                                formOfStudyExpanded = false
                            }
                        )
                    }
                }
            }

            // Чекбокс для иностранного студента
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isForeign, onCheckedChange = onIsForeignChange)
                Text("Иностранный студент")
            }
        }
    }
}

/**
 * Выпадающий список предприятий с информацией о городе и числе руководителей
 */
@Composable
private fun EnterpriseDropdown(
    enterprisesList: List<Enterprise>,
    selectedEnterprise: Enterprise?,
    onEnterpriseSelected: (Enterprise) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Text(
            "Выберите предприятие из списка:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.weight(1f))
            Box(modifier = Modifier.fillMaxWidth(0.7f)) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    value = selectedEnterprise?.getDisplayName() ?: "Не выбрано",
                    onValueChange = {},
                    label = { Text("Предприятие") },
                    trailingIcon = {
                        TextButton(onClick = { expanded = !expanded }) {
                            Text(if (expanded) "▲" else "▼")
                        }
                    }
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    enterprisesList.forEach { enterprise ->
                        DropdownMenuItem(
                            text = { 
                                Column {
                                    Text(enterprise.name, maxLines = 2)
                                    if (enterprise.city != null) {
                                        Text(
                                            enterprise.city,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (enterprise.supervisors.isNotEmpty()) {
                                        Text(
                                            "${enterprise.supervisors.size} руководителей",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            },
                            onClick = {
                                onEnterpriseSelected(enterprise)
                                expanded = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

/**
 * Выпадающий список руководителей практики от предприятия (ФИО + должность)
 */
@Composable
private fun SupervisorDropdown(
    supervisors: List<PracticeSupervisor>,
    selectedSupervisor: PracticeSupervisor?,
    onSupervisorSelected: (PracticeSupervisor) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Column {
        Text(
            "Выберите руководителя практики:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.weight(1f))
            Box(modifier = Modifier.fillMaxWidth(0.7f)) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    value = selectedSupervisor?.getDisplayText() ?: "Не выбрано",
                    onValueChange = {},
                    label = { Text("Руководитель от базы") },
                    trailingIcon = {
                        TextButton(onClick = { expanded = !expanded }) {
                            Text(if (expanded) "▲" else "▼")
                        }
                    }
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    supervisors.forEach { supervisor ->
                        DropdownMenuItem(
                            text = { 
                                Column {
                                    Text(supervisor.fullName)
                                    Text(
                                        supervisor.position,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            onClick = {
                                onSupervisorSelected(supervisor)
                                expanded = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}