package ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.bpo.norn.commonMain.models.Group
import ru.bpo.norn.commonMain.models.Student
import ru.bpo.norn.jwmMain.viewmodel.NornViewModel

/**
 * Компонент для отображения списка студентов в выбранной группе
 * Позволяет выбрать студента для редактирования
 */
@Composable
fun StudentsList(
    group: Group,
    viewModel: NornViewModel,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, color = MaterialTheme.colorScheme.tertiary)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Заголовок списка студентов
            Text(
                "🎓 Список студентов (${group.students.size}):",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                thickness = 1.dp,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            // Список студентов в ограниченном контейнере
            LazyColumn(modifier = Modifier.height(400.dp)) {
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
 * Компонент для одной карточки студента
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

            // Индикатор клика
            Text(
                "👆",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * Вспомогательная функция для определения цвета типа финансирования
 */
private fun getFundingTypeColor(fundingType: String): Color {
    return when {
        fundingType.contains("бюджет", ignoreCase = true) -> Color(0xFF2E7D32) // Зеленый
        fundingType.contains("платн", ignoreCase = true) -> Color(0xFFD32F2F) // Красный
        fundingType.contains("льгот", ignoreCase = true) -> Color(0xFFED6C02) // Оранжевый
        else -> Color(0xFF666666) // Серый
    }
}