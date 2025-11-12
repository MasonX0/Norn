package ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.bpo.norn.commonMain.models.Group

/**
 * Компонент для отображения информации о выбранной группе
 * Показывает основные данные группы: название, количество студентов,
 * направление и код направления
 */
@Composable
fun GroupInfoCard(
    group: Group,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, color = MaterialTheme.colorScheme.tertiary)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Название группы
            Text(
                "Текущая группа: ${group.name}",
                style = MaterialTheme.typography.titleMedium
            )

            // Статистика группы
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Студентов: ${group.students.size}")
                Text("Направление: ${group.nameOfDirection}")
            }

            // Код направления
            Text("Код направления: ${group.codeOfDirection}")
        }
    }
}