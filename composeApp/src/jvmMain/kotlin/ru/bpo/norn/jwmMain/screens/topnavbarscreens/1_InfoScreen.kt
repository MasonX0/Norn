package ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.bpo.norn.commonMain.models.Group
import ru.bpo.norn.commonMain.models.Student
import ru.bpo.norn.jwmMain.viewmodel.NornViewModel

@Composable
fun InfoScreen(viewModel: NornViewModel) {
    val groups by viewModel.groups.collectAsState()
    val selectedGroup by viewModel.selectedGroup.collectAsState()
    val selectedStudent by viewModel.selectedStudent.collectAsState()
    val showEditDialog by viewModel.showStudentEditDialog.collectAsState()
    val enterprisesList by viewModel.enterprisesList.collectAsState()
    var showGroupDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(15.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        Text("Панель управления данными студентов", style = MaterialTheme.typography.headlineSmall)

        // Выбор группы
        if (groups.isNotEmpty()) {
            Card(modifier = Modifier.fillMaxWidth(),
                border= BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🎓 Выберите группу:", style = MaterialTheme.typography.titleMedium)

                    var expanded by remember { mutableStateOf(false) }
                    var selectedGroupName by remember {
                        mutableStateOf(selectedGroup?.name ?: "Выберите группу")
                    }

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
                                        viewModel.selectGroup(group)
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

        selectedGroup?.let { group ->
            Card(modifier = Modifier.fillMaxWidth(),
                border= BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("🎓 Группа: ${group.name}", style = MaterialTheme.typography.titleMedium)
                            Text("Студентов: ${group.students.size}")
                            Text("Направление: ${group.nameOfDirection}")
                        }
                        Button(onClick = { showGroupDialog = true }) {
                            Text("📝 Заполнить для группы")
                        }
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth(),
                border= BorderStroke(width = 2.dp, color = MaterialTheme.colorScheme.primary)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("👤 Список студентов:", modifier=Modifier.padding(bottom=10.dp),style = MaterialTheme.typography.titleMedium)
                    Divider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                    LazyColumn(modifier = Modifier.height(400.dp)) {
                        items(group.students) { student ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                onClick = { viewModel.selectStudentForEditing(student) },
                                border= BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.tertiary)
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
                    }
                }
            }
        }
        if (groups.isEmpty()) {
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
    }

    if (showGroupDialog) {
        selectedGroup?.let { group ->
            GroupEditDialog(
                enterprisesList = enterprisesList,
                viewModel = viewModel,
                currentGroup = group,
                onDismiss = { showGroupDialog = false }
            )
        }
    }

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

@Composable
private fun GroupEditDialog(
    enterprisesList: List<String>,
    viewModel: NornViewModel,
    currentGroup: Group,
    onDismiss: () -> Unit
) {
    var selectedEnterprise by remember { mutableStateOf("") }
    var nameOfPracticeBase by remember { mutableStateOf("") }
    var typeOfPractice by remember { mutableStateOf("") }
    var periodOfPractice by remember { mutableStateOf("") }
    var cityOfPractice by remember { mutableStateOf("") }
    var headOfPracticeFromDepartment by remember { mutableStateOf("") }
    var postOfHeadOfPracticeFromDepartment by remember { mutableStateOf("") }
    var headOfPracticeFromPracticeBase by remember { mutableStateOf("") }
    var postOfHeadOfPracticeFromPracticeBase by remember { mutableStateOf("") }
    var directorName by remember { mutableStateOf("") }
    var codeOfDirection by remember { mutableStateOf("") }
    var nameOfDirection by remember { mutableStateOf("") }
    var nameOfSpeciality by remember { mutableStateOf("") }
    var codeOfSpeciality by remember { mutableStateOf("") }
    var formOfStudy by remember { mutableStateOf("") }
    var isForeign by remember { mutableStateOf(false) }
    var withPayment by remember { mutableStateOf(false) }
    var gradeForPractice by remember { mutableStateOf("") }

    // Флаги: поле было задано (даже если стало пустым)
    var isNameOfPracticeBaseSet by remember { mutableStateOf(false) }
    var isTypeOfPracticeSet by remember { mutableStateOf(false) }
    var isPeriodOfPracticeSet by remember { mutableStateOf(false) }
    var isCityOfPracticeSet by remember { mutableStateOf(false) }
    var isHeadOfPracticeFromDepartmentSet by remember { mutableStateOf(false) }
    var isPostOfHeadOfPracticeFromDepartmentSet by remember { mutableStateOf(false) }
    var isHeadOfPracticeFromPracticeBaseSet by remember { mutableStateOf(false) }
    var isPostOfHeadOfPracticeFromPracticeBaseSet by remember { mutableStateOf(false) }
    var isDirectorNameSet by remember { mutableStateOf(false) }
    var isCodeOfDirectionSet by remember { mutableStateOf(false) }
    var isNameOfDirectionSet by remember { mutableStateOf(false) }
    var isNameOfSpecialitySet by remember { mutableStateOf(false) }
    var isCodeOfSpecialitySet by remember { mutableStateOf(false) }
    var isFormOfStudySet by remember { mutableStateOf(false) }
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
            EditFormContent(
                selectedEnterprise = selectedEnterprise,
                onEnterpriseSelected = { enterprise ->
                    selectedEnterprise = enterprise
                    nameOfPracticeBase = enterprise
                    isNameOfPracticeBaseSet = true
                    val city = viewModel.extractCityFromEnterpriseSmart(enterprise)
                    cityOfPractice = city ?: ""
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
                formOfStudy = formOfStudy,
                onFormOfStudyChange = {
                    formOfStudy = it
                    isFormOfStudySet = true
                },
                isForeign = isForeign,
                onIsForeignChange = { isForeign = it },
                withPayment = withPayment,
                onWithPaymentChange = { withPayment = it },
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
                // Создаем обновленную группу с новыми данными
                val updatedGroup = currentGroup.copy(
                    codeOfDirection = if (isCodeOfDirectionSet) codeOfDirection else currentGroup.codeOfDirection,
                    nameOfDirection = if (isNameOfDirectionSet) nameOfDirection else currentGroup.nameOfDirection
                )

                // Обновляем информацию о группе
                viewModel.updateGroupData(updatedGroup)

                // Обновляем всех студентов в группе
                currentGroup.students.forEach { student ->
                    viewModel.updateStudentData(
                        Student(
                            name = student.name,
                            course = student.course,
                            codeOfDirection = if (isCodeOfDirectionSet) codeOfDirection else student.codeOfDirection,
                            nameOfDirection = if (isNameOfDirectionSet) nameOfDirection else student.nameOfDirection,
                            group = student.group,
                            isForeign = isForeign,
                            gradeForPractice = if (isGradeForPracticeSet) gradeForPractice else student.gradeForPractice,
                            nameOfPracticeBase = if (isNameOfPracticeBaseSet) nameOfPracticeBase else student.nameOfPracticeBase,
                            typeOfPractice = if (isTypeOfPracticeSet) typeOfPractice else student.typeOfPractice,
                            periodOfPractice = if (isPeriodOfPracticeSet) periodOfPractice else student.periodOfPractice,
                            formOfStudy = if (isFormOfStudySet) formOfStudy else student.formOfStudy,
                            withPayment = withPayment,
                            cityOfPractice = if (isCityOfPracticeSet) cityOfPractice else student.cityOfPractice,
                            nameOfSpeciality = if (isNameOfSpecialitySet) nameOfSpeciality else student.nameOfSpeciality,
                            codeOfSpeciality = if (isCodeOfSpecialitySet) codeOfSpeciality else student.codeOfSpeciality,
                            headOfPracticeFromDepartment = if (isHeadOfPracticeFromDepartmentSet) headOfPracticeFromDepartment else student.headOfPracticeFromDepartment,
                            headOfPracticeFromPracticeBase = if (isHeadOfPracticeFromPracticeBaseSet) headOfPracticeFromPracticeBase else student.headOfPracticeFromPracticeBase,
                            postOfHeadOfPracticeFromPracticeBase = if (isPostOfHeadOfPracticeFromPracticeBaseSet) postOfHeadOfPracticeFromPracticeBase else student.postOfHeadOfPracticeFromPracticeBase,
                            postOfHeadOfPracticeFromDepartment = if (isPostOfHeadOfPracticeFromDepartmentSet) postOfHeadOfPracticeFromDepartment else student.postOfHeadOfPracticeFromDepartment,
                            directorName = if (isDirectorNameSet) directorName else student.directorName
                        )
                    )
                }
                onDismiss()
            }) {
                Text("Применить ко всей группе")
            }
        }
    )
}

// StudentEditDialog и EditFormContent остаются без изменений, так как они уже работают со Student
@Composable
private fun StudentEditDialog(
    student: Student?,
    enterprisesList: List<String>,
    viewModel: NornViewModel,
    onDismiss: () -> Unit,
    onSave: (Student) -> Unit
) {
    var name by remember { mutableStateOf(student?.name ?: "") }
    var course by remember { mutableStateOf(student?.course?.toString() ?: "") }
    var group by remember { mutableStateOf(student?.group ?: "") }
    var selectedEnterprise by remember { mutableStateOf("") }
    var nameOfPracticeBase by remember { mutableStateOf(student?.nameOfPracticeBase ?: "") }
    var typeOfPractice by remember { mutableStateOf(student?.typeOfPractice ?: "") }
    var periodOfPractice by remember { mutableStateOf(student?.periodOfPractice ?: "") }
    var cityOfPractice by remember { mutableStateOf(student?.cityOfPractice ?: "") }
    var headOfPracticeFromDepartment by remember { mutableStateOf(student?.headOfPracticeFromDepartment ?: "") }
    var postOfHeadOfPracticeFromDepartment by remember { mutableStateOf(student?.postOfHeadOfPracticeFromDepartment ?: "") }
    var headOfPracticeFromPracticeBase by remember { mutableStateOf(student?.headOfPracticeFromPracticeBase ?: "") }
    var postOfHeadOfPracticeFromPracticeBase by remember { mutableStateOf(student?.postOfHeadOfPracticeFromPracticeBase ?: "") }
    var directorName by remember { mutableStateOf(student?.directorName ?: "") }
    var codeOfDirection by remember { mutableStateOf(student?.codeOfDirection ?: "") }
    var nameOfDirection by remember { mutableStateOf(student?.nameOfDirection ?: "") }
    var nameOfSpeciality by remember { mutableStateOf(student?.nameOfSpeciality ?: "") }
    var codeOfSpeciality by remember { mutableStateOf(student?.codeOfSpeciality ?: "") }
    var formOfStudy by remember { mutableStateOf(student?.formOfStudy ?: "") }
    var isForeign by remember { mutableStateOf(student?.isForeign ?: false) }
    var withPayment by remember { mutableStateOf(student?.withPayment ?: false) }
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
                        nameOfPracticeBase = enterprise
                        val city = viewModel.extractCityFromEnterpriseSmart(enterprise)
                        cityOfPractice = city ?: ""
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
                    formOfStudy = formOfStudy,
                    onFormOfStudyChange = { formOfStudy = it },
                    isForeign = isForeign,
                    onIsForeignChange = { isForeign = it },
                    withPayment = withPayment,
                    onWithPaymentChange = { withPayment = it },
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
                                formOfStudy = formOfStudy,
                                withPayment = withPayment,
                                cityOfPractice = cityOfPractice,
                                nameOfSpeciality = nameOfSpeciality,
                                codeOfSpeciality = codeOfSpeciality,
                                headOfPracticeFromDepartment = headOfPracticeFromDepartment,
                                headOfPracticeFromPracticeBase = headOfPracticeFromPracticeBase,
                                postOfHeadOfPracticeFromPracticeBase = postOfHeadOfPracticeFromPracticeBase,
                                postOfHeadOfPracticeFromDepartment = postOfHeadOfPracticeFromDepartment,
                                directorName = directorName
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

@Composable
private fun EditFormContent(
    selectedEnterprise: String,
    onEnterpriseSelected: (String) -> Unit,
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
    formOfStudy: String,
    onFormOfStudyChange: (String) -> Unit,
    isForeign: Boolean,
    onIsForeignChange: (Boolean) -> Unit,
    withPayment: Boolean,
    onWithPaymentChange: (Boolean) -> Unit,
    gradeForPractice: String,
    onGradeForPracticeChange: (String) -> Unit,
    enterprisesList: List<String>,
    isStudentDialog: Boolean
) {
    @Composable
    fun fieldColors(value: String): TextFieldColors {
        return if (value.isNotEmpty()) {
            OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        } else {
            OutlinedTextFieldDefaults.colors()
        }
    }

    LazyColumn(
        modifier = if (isStudentDialog) Modifier.height(400.dp) else Modifier.height(500.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text("Место практики:", style = MaterialTheme.typography.titleSmall)
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
        }
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
                OutlinedTextField(
                    value = gradeForPractice,
                    onValueChange = onGradeForPracticeChange,
                    label = { Text("Оценка") },
                    modifier = Modifier.weight(1f),
                    colors = fieldColors(gradeForPractice)
                )
            }
        }
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
            OutlinedTextField(
                value = formOfStudy,
                onValueChange = onFormOfStudyChange,
                label = { Text("Форма обучения") },
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors(formOfStudy)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isForeign, onCheckedChange = onIsForeignChange)
                    Text("Иностранный студент")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = withPayment, onCheckedChange = onWithPaymentChange)
                    Text("Платное обучение")
                }
            }
        }
    }
}
@Composable
private fun EnterpriseDropdown(
    enterprisesList: List<String>,
    selectedEnterprise: String,
    onEnterpriseSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Text(
            "Выберите предприятие из списка:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))

        // Просто оборачиваем в Row с весом
        Row(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.weight(1f))
            Box(modifier = Modifier.fillMaxWidth(0.7f)) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    value = selectedEnterprise.ifEmpty { "Не выбрано" },
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
                    DropdownMenuItem(
                        text = { Text("Не выбрано") },
                        onClick = {
                            onEnterpriseSelected("")
                            expanded = false
                        }
                    )
                    enterprisesList.forEach { enterprise ->
                        DropdownMenuItem(
                            text = { Text(enterprise, maxLines = 2) },
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