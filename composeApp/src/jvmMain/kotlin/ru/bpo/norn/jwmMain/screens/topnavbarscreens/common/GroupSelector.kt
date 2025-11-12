package ui.topnav.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.bpo.norn.commonMain.models.Group
import ru.bpo.norn.jwmMain.viewmodel.NornViewModel

/**
 * Общий компонент для выбора группы в top navigation экранах
 * Предоставляет выпадающий список доступных групп
 */
@Composable
fun GroupSelector(
    groups: List<Group>,
    selectedGroup: Group?,
    viewModel: NornViewModel,
    modifier: Modifier = Modifier
) {
    if (groups.isEmpty()) {
        NoGroupsMessage(modifier = modifier)
        return
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        border = BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "🎓 Выберите группу:",
                style = MaterialTheme.typography.titleMedium
            )

            // Выпадающий список групп
            GroupDropdown(
                groups = groups,
                selectedGroup = selectedGroup,
                onGroupSelected = { group -> viewModel.selectGroup(group) }
            )
        }
    }
}

/**
 * Выпадающий список групп
 */
@Composable
private fun GroupDropdown(
    groups: List<Group>,
    selectedGroup: Group?,
    onGroupSelected: (Group) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedGroupName by remember {
        mutableStateOf(selectedGroup?.name ?: "Выберите группу")
    }

    // Обновляем отображаемое имя при изменении выбранной группы
    LaunchedEffect(selectedGroup) {
        selectedGroupName = selectedGroup?.name ?: "Выберите группу"
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
                        onGroupSelected(group)
                        selectedGroupName = group.name
                        expanded = false
                    }
                )
            }
        }
    }
}

/**
 * Сообщение об отсутствии групп
 */
@Composable
private fun NoGroupsMessage(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
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