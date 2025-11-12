package ui.topnav.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.bpo.norn.commonMain.models.Group

/**
 * Компонент с кнопками действий для выбранной группы
 * Показывает информацию о группе и предоставляет действия
 */
@Composable
fun GroupActionButtons(
    group: Group,
    onFillForGroup: () -> Unit,
    onEditGroup: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        border = BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Информация о группе
                GroupInfo(group = group)

                // Кнопки действий
                ActionButtons(
                    onFillForGroup = onFillForGroup,
                    onEditGroup = onEditGroup
                )
            }
        }
    }
}

/**
 * Компонент с информацией о группе
 */
@Composable
private fun GroupInfo(group: Group) {
    Column {
        Text(
            "🎓 Группа: ${group.name}",
            style = MaterialTheme.typography.titleMedium
        )
        Text("Студентов: ${group.students.size}")
        Text("Направление: ${group.nameOfDirection}")
        Text("Код направления: ${group.codeOfDirection}")
    }
}

/**
 * Кнопки действий
 */
@Composable
private fun ActionButtons(
    onFillForGroup: () -> Unit,
    onEditGroup: () -> Unit
) {
    Column {
        Button(onClick = onFillForGroup) {
            Text("📝 Заполнить для группы")
        }
        Spacer(modifier = Modifier.height(4.dp))
        Button(
            onClick = onEditGroup,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Text("⚙️ Изменить группу")
        }
    }
}