package ui.topnav.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.bpo.norn.commonMain.models.Group
import ru.bpo.norn.commonMain.models.Student
import ru.bpo.norn.jwmMain.viewmodel.NornViewModel

/**
 * Компонент для отображения списка студентов в группе
 * Показывает карточки студентов с их основной информацией
 * Используется в различных top navigation экранах
 */
@Composable
fun StudentsListCard(
    group: Group,
    viewModel: NornViewModel,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        border = BorderStroke(width = 2.dp, color = MaterialTheme.colorScheme.primary)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Заголовок списка
            Text(
                "👤 Список студентов:",
                modifier = Modifier.padding(bottom = 10.dp),
                style = MaterialTheme.typography.titleMedium
            )
            
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                thickness = 1.dp,
                modifier = Modifier.padding(bottom = 10.dp)
            )
            
            // Список студентов в ограниченном контейнере
            LazyColumn(modifier = Modifier.height(300.dp)) {
                items(group.students) { student ->
                    StudentCard(
                        student = student,
                        onStudentClick = { viewModel.selectStudentForEditing(student) }
                    )
                }
            }
        }
    }
}

/**
 * Карточка одного студента
 */
@Composable
private fun StudentCard(
    student: Student,
    onStudentClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onStudentClick,
        border = BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.tertiary)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Основная информация о студенте
            Text(
                student.name,
                style = MaterialTheme.typography.bodyMedium
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Курс: ${student.course}")
                Text("Группа: ${student.group}")
            }
            
            // Дополнительная информация
            if (student.nameOfPracticeBase.isNotEmpty()) {
                Text("База: ${student.nameOfPracticeBase}")
            }
            
            if (student.cityOfPractice.isNotEmpty()) {
                Text("Город: ${student.cityOfPractice}")
            }
            
            if (student.gradeForPractice.isNotEmpty()) {
                Text("Оценка: ${student.gradeForPractice}")
            }
        }
    }
}