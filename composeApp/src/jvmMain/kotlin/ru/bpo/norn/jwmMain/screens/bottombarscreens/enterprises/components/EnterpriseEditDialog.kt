package ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.bpo.norn.commonMain.models.Enterprise
import ru.bpo.norn.commonMain.models.PracticeSupervisor

/**
 * Расширенный диалог редактирования предприятия
 * Позволяет редактировать все свойства предприятия включая руководителей
 */
@Composable
fun EnterpriseEditDialog(
    enterprise: Enterprise?,
    showDialog: Boolean,
    onSave: (Enterprise) -> Unit,
    onDismiss: () -> Unit
) {
    // Локальные состояния для редактирования
    var cityText by remember { mutableStateOf("") }
    var supervisorsList by remember { mutableStateOf<List<PracticeSupervisor>>(emptyList()) }
    var isForeign by remember { mutableStateOf(false) }
    var isSoluniTyulyukInzer by remember { mutableStateOf(false) }
    var isDepartment by remember { mutableStateOf(false) }
    var isUniversitySubdivision by remember { mutableStateOf(false) }
    var isBaseDepartment by remember { mutableStateOf(false) }

    // Обновляем локальные состояния при изменении предприятия
    LaunchedEffect(enterprise) {
        enterprise?.let {
            cityText = it.city ?: ""
            supervisorsList = it.supervisors
            isForeign = it.isForeign
            isSoluniTyulyukInzer = it.isSoluniTyulyukInzer
            isDepartment = it.isDepartment
            isUniversitySubdivision = it.isUniversitySubdivision
            isBaseDepartment = it.isBaseDepartment
        }
    }

    if (showDialog && enterprise != null) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    "✏️ Редактировать предприятие",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .width(800.dp)
                        .heightIn(min = 400.dp, max = 700.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Название предприятия (только для просмотра)
                    EnterpriseInfoCard(enterprise = enterprise)

                    // Редактирование города
                    CityEditSection(
                        cityText = cityText,
                        onCityTextChange = { cityText = it }
                    )

                    // Булевы поля предприятия
                    EnterpriseTypeSection(
                        isForeign = isForeign,
                        onIsForeignChange = { isForeign = it },
                        isSoluniTyulyukInzer = isSoluniTyulyukInzer,
                        onIsSoluniTyulyukInzerChange = { isSoluniTyulyukInzer = it },
                        isDepartment = isDepartment,
                        onIsDepartmentChange = { isDepartment = it },
                        isUniversitySubdivision = isUniversitySubdivision,
                        onIsUniversitySubdivisionChange = { isUniversitySubdivision = it },
                        isBaseDepartment = isBaseDepartment,
                        onIsBaseDepartmentChange = { isBaseDepartment = it }
                    )

                    // Управление руководителями
                    SupervisorsManagementSection(
                        supervisorsList = supervisorsList,
                        onSupervisorsListChange = { supervisorsList = it }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val updatedEnterprise = enterprise.copy(
                            city = cityText.takeIf { it.isNotBlank() },
                            supervisors = supervisorsList,
                            isForeign = isForeign,
                            isSoluniTyulyukInzer = isSoluniTyulyukInzer,
                            isDepartment = isDepartment,
                            isUniversitySubdivision = isUniversitySubdivision,
                            isBaseDepartment = isBaseDepartment
                        )
                        onSave(updatedEnterprise)
                    },
                    enabled = cityText.isNotBlank()
                ) {
                    Text("💾 Сохранить изменения")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("❌ Отмена")
                }
            }
        )
    }
}

/**
 * Карточка с информацией о предприятии (только чтение)
 */
@Composable
private fun EnterpriseInfoCard(enterprise: Enterprise) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "🏢 Предприятие:",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                enterprise.name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

/**
 * Секция редактирования города
 */
@Composable
private fun CityEditSection(
    cityText: String,
    onCityTextChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "🏙️ Город:",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = cityText,
                onValueChange = onCityTextChange,
                label = { Text("Город") },
                placeholder = { Text("Например: Уфа, Москва, Санкт-Петербург") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Секция выбора типов предприятия
 */
@Composable
private fun EnterpriseTypeSection(
    isForeign: Boolean,
    onIsForeignChange: (Boolean) -> Unit,
    isSoluniTyulyukInzer: Boolean,
    onIsSoluniTyulyukInzerChange: (Boolean) -> Unit,
    isDepartment: Boolean,
    onIsDepartmentChange: (Boolean) -> Unit,
    isUniversitySubdivision: Boolean,
    onIsUniversitySubdivisionChange: (Boolean) -> Unit,
    isBaseDepartment: Boolean,
    onIsBaseDepartmentChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "🏢 Тип предприятия:",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                EnterpriseTypeCheckbox(
                    checked = isForeign,
                    onCheckedChange = onIsForeignChange,
                    text = "🌍 Зарубежное предприятие"
                )

                EnterpriseTypeCheckbox(
                    checked = isSoluniTyulyukInzer,
                    onCheckedChange = onIsSoluniTyulyukInzerChange,
                    text = "🏔️ Солуни/Тюлюк/Инзер"
                )

                EnterpriseTypeCheckbox(
                    checked = isDepartment,
                    onCheckedChange = onIsDepartmentChange,
                    text = "🎓 Кафедра"
                )

                EnterpriseTypeCheckbox(
                    checked = isUniversitySubdivision,
                    onCheckedChange = onIsUniversitySubdivisionChange,
                    text = "🏛️ Структурное подразделение вуза"
                )

                EnterpriseTypeCheckbox(
                    checked = isBaseDepartment,
                    onCheckedChange = onIsBaseDepartmentChange,
                    text = "🏭 Базовая кафедра"
                )
            }
        }
    }
}

/**
 * Компонент чекбокса для типа предприятия
 */
@Composable
private fun EnterpriseTypeCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    text: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
        Text(
            text,
            modifier = Modifier.padding(start = 8.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/**
 * Секция управления руководителями практики
 */
@Composable
private fun SupervisorsManagementSection(
    supervisorsList: List<PracticeSupervisor>,
    onSupervisorsListChange: (List<PracticeSupervisor>) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "👨‍💼 Руководители практики:",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Button(
                    onClick = {
                        val newSupervisor = PracticeSupervisor(
                            fullName = "Новый руководитель",
                            position = "должность"
                        )
                        onSupervisorsListChange(supervisorsList + newSupervisor)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("➕ Добавить")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Список руководителей
            if (supervisorsList.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(supervisorsList) { index, supervisor ->
                        SupervisorEditCard(
                            supervisor = supervisor,
                            onSupervisorChange = { updatedSupervisor ->
                                val updatedList = supervisorsList.toMutableList()
                                updatedList[index] = updatedSupervisor
                                onSupervisorsListChange(updatedList)
                            },
                            onDelete = {
                                val updatedList = supervisorsList.toMutableList()
                                updatedList.removeAt(index)
                                onSupervisorsListChange(updatedList)
                            }
                        )
                    }
                }
            } else {
                NoSupervisorsCard()
            }
        }
    }
}

/**
 * Карточка редактирования одного руководителя
 */
@Composable
private fun SupervisorEditCard(
    supervisor: PracticeSupervisor,
    onSupervisorChange: (PracticeSupervisor) -> Unit,
    onDelete: () -> Unit
) {
    var fullName by remember { mutableStateOf(supervisor.fullName) }
    var position by remember { mutableStateOf(supervisor.position) }

    // Обновляем родительский компонент при изменениях
    LaunchedEffect(fullName, position) {
        onSupervisorChange(supervisor.copy(fullName = fullName, position = position))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Руководитель",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(
                    onClick = onDelete,
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("🗑️")
                }
            }

            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("ФИО") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Например: И.И. Иванов") }
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = position,
                onValueChange = { position = it },
                label = { Text("Должность") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Например: ст. преподаватель") }
            )
        }
    }
}

/**
 * Карточка отсутствия руководителей
 */
@Composable
private fun NoSupervisorsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "❌ Нет руководителей",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                "Добавьте хотя бы одного руководителя практики",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}