package ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.bpo.norn.commonMain.models.Student
import ru.bpo.norn.jwmMain.viewmodel.NornViewModel

/**
 * Общий компонент диалога редактирования студента
 * Используется в разных экранах для единообразного редактирования данных студентов
 */
@Composable
fun StudentEditDialog(
    student: Student?,
    viewModel: NornViewModel,
    onDismiss: () -> Unit,
    onSave: (Student) -> Unit
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
                            val updatedStudent = Student(
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
                            onSave(updatedStudent)
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