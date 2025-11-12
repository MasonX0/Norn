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
import androidx.compose.ui.unit.dp
import ru.bpo.norn.commonMain.models.Group
import ru.bpo.norn.jwmMain.viewmodel.NornViewModel

/**
 * Компонент для отображения списка загруженных групп
 * Показывает информацию о каждой группе и позволяет выбрать/удалить группу
 */
@Composable
fun GroupsList(
    groups: List<Group>,
    selectedGroup: Group?,
    viewModel: NornViewModel,
    modifier: Modifier = Modifier
) {
    if (groups.isEmpty()) {
        // Показываем сообщение, если нет групп
        NoGroupsMessage(modifier = modifier)
        return
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, color = MaterialTheme.colorScheme.primary)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Заголовок с количеством групр
            Text(
                "📚 Группы (${groups.size}):",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                thickness = 1.dp,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            // Список групп в ограниченном контейнере
            LazyColumn(modifier = Modifier.height(120.dp)) {
                items(groups) { group ->
                    GroupCard(
                        group = group,
                        isSelected = selectedGroup?.name == group.name,
                        onSelectGroup = { viewModel.selectGroup(group) },
                        onRemoveGroup = { viewModel.removeGroup(group) }
                    )
                }
            }
        }
    }
}

/**
 * Компонент для одной карточки группы
 */
@Composable
private fun GroupCard(
    group: Group,
    isSelected: Boolean,
    onSelectGroup: () -> Unit,
    onRemoveGroup: () -> Unit
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

            // Кнопки действий
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onSelectGroup,
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Text("👁️ Просмотр")
                }

                OutlinedButton(
                    onClick = onRemoveGroup,
                    border = BorderStroke(width = 1.dp, color = Color.Red)
                ) {
                    Text("🗑️ Удалить")
                }
            }
        }
    }
}

/**
 * Компонент для отображения сообщения об отсутствии групп
 */
@Composable
private fun NoGroupsMessage(modifier: Modifier = Modifier) {
    Text(
        "📝 Нет загруженных групп. Нажмите кнопку выше чтобы загрузить Excel файл со студентами.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}