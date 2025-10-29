// desktopMain/kotlin/ru/bpo/norn/App.kt
package ru.bpo.norn.jwmMain.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.bpo.norn.jwmMain.viewmodel.NornViewModel
import ru.bpo.norn.commonMain.models.Group
import ru.bpo.norn.commonMain.models.Student
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

@Composable
fun MainScreen() {
    val viewModel = remember { NornViewModel() }

    val groups by viewModel.groups.collectAsState()
    val reportFile by viewModel.reportFile.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Norn Desktop App", style = androidx.compose.material3.MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(24.dp))

        // Кнопка выбора файла через диалог
        Button(onClick = {
            val fileChooser = JFileChooser().apply{
                dialogTitle = "Выберите отчетный файл"
                // Добавляем фильтры сразу в цепочке
                addChoosableFileFilter(FileNameExtensionFilter("PDF файлы (*.pdf)", "pdf"))
                addChoosableFileFilter(FileNameExtensionFilter("Excel файлы", "xlsx", "xls"))
                addChoosableFileFilter(FileNameExtensionFilter("Word документы", "docx", "doc"))
                fileFilter = FileNameExtensionFilter("Все документы", "pdf", "xlsx", "xls", "docx", "doc")
                //Показывать ли опцию все файлы? isAcceptAllFileFilterUsed = false
            }

            if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                viewModel.selectReportFile(fileChooser.selectedFile)
            }
        }) {
            Text("Выбрать файл отчета")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Кнопка добавления группы
        Button(onClick = {
            val testStudent = Student(
                name = "Иван Иванов",
                group = "ИТ-21",
                gradeForPractice = "Отлично",
                nameOfPracticeBase = "БПО",
                typeOfPractice = "Производственная",
                periodOfPractice = "01.09.2024 - 30.11.2024",
                formOfPractice = "Очная",
                withPayment = true,
                cityOfPractice = "Москва",
                nameOfSpeciality = "Информационные технологии",
                codeOfSpeciality = "09.03.01",
                headOfPractice = "Петров П.П.",
                directorName = "Сидоров С.С."
            )
            val testGroup = Group(listOf(testStudent))
            viewModel.addGroup(testGroup)
        }) {
            Text("Добавить тестовую группу")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Отображение состояния
        Column(horizontalAlignment = Alignment.Start) {
            Text("Состояние приложения:")
            Text("Файл: ${reportFile?.name ?: "не выбран"}")
            reportFile?.let { file ->
                Text("Путь: ${file.absolutePath}")
                Text("Размер: ${file.length()} байт")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Групп: ${groups.size}")

            groups.forEachIndexed { index, group ->
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text("Группа ${index + 1}: ${group.getStudentsCount()} студентов")
                    group.students.forEach { student ->
                        Text("  - ${student.name} (${student.gradeForPractice})",
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}