package ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import ru.bpo.norn.jwmMain.viewmodel.NornViewModel
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

@Composable
fun Enterprises(viewModel: NornViewModel) {
    val enterprisesFile by viewModel.enterprisesFile.collectAsState()
    val enterprisesList by viewModel.enterprisesList.collectAsState()
    val selectedEnterprise by viewModel.selectedEnterprise.collectAsState()
    val showEditDialog by viewModel.showEditDialog.collectAsState()

    // Локальные состояния для редактирования
    var newCityText by remember { mutableStateOf("") }
    var supervisorsList by remember { mutableStateOf<List<PracticeSupervisor>>(emptyList()) }

    // Обновляем локальные состояния когда выбираем предприятие
    LaunchedEffect(selectedEnterprise) {
        newCityText = selectedEnterprise?.city ?: ""
        supervisorsList = selectedEnterprise?.supervisors ?: emptyList()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(15.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        // Заголовок экрана
        Text(
            "Чтение списка предприятий из TXT файла с руководителями",
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium
        )

        // Информация о новом формате
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(
                    alpha = 0.3f
                )
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    "📋 Новый формат файла:",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "ООО Газпром Межрегионгаз Уфа, г. Уфа // ст. преподаватель ! М.А. Салихова, доц. ! А.И.Сидоров",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "• Предприятие и город разделены запятой\n• Руководители отделены '//' от предприятия\n• Должность и ФИО разделены '!'",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Кнопка выбора TXT файла
        Row(modifier=Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(15.dp)){

            Button(onClick = {
                val fileChooser = JFileChooser().apply {
                    currentDirectory = viewModel.getStartDirectory()
                    dialogTitle = "Выберите файл с данными предприятий"
                    addChoosableFileFilter(FileNameExtensionFilter("TXT файлы", "txt"))
                    fileFilter = FileNameExtensionFilter("TXT файлы", "txt")
                    isAcceptAllFileFilterUsed = false
                }

                if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    viewModel.selectEnterprisesFile(fileChooser.selectedFile)
                    val file = fileChooser.selectedFile
                    if (file.exists()) {
                        viewModel.readEnterprisesFromTxt(file)
                    }
                }
            }, colors = ButtonColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                disabledContentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ), border = BorderStroke(1.dp,MaterialTheme.colorScheme.outline)
            ) {
                Text("📁 Выбрать TXT документ\n(список предприятий)")
            }
            // Кнопка очистки списка
            if (enterprisesList.isNotEmpty()) {
                Button(
                    onClick = {
                        viewModel.clearEnterprisesList()
                        viewModel.selectEnterprisesFile(null)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("🗑️ Очистить список")
                }
            }

        }

        // Отображение списка предприятий с руководителями
        if (enterprisesList.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border= BorderStroke(width = 2.dp, color = MaterialTheme.colorScheme.primary)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "📋 Список предприятий (${enterprisesList.size}):",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                    Text(
                        "Нажмите на карточку предприятия для редактирования",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                    Divider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                    LazyColumn(modifier = Modifier.height(400.dp)) {
                        items(enterprisesList) { enterprise ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                onClick = {
                                    // При нажатии на карточку - открываем диалог редактирования
                                    viewModel.selectEnterpriseForEditing(enterprise)
                                },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selectedEnterprise?.name == enterprise.name) {
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                    } else {
                                        MaterialTheme.colorScheme.surface
                                    }
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    // Название и город
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            enterprise.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            enterprise.city ?: "❓",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (enterprise.city != null) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.error
                                        )
                                    }

                                    // Руководители практики
                                    if (enterprise.supervisors.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            "👨‍💼 Руководители практики:",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        enterprise.supervisors.forEach { supervisor ->
                                            Text(
                                                "• ${supervisor.getDisplayText()}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.padding(start = 8.dp)
                                            )
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            "👨‍💼 Нет руководителей",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }

                                    // Отображение типов предприятия
                                    val enterpriseTypes = mutableListOf<String>()
                                    if (enterprise.isForeign) enterpriseTypes.add("🌍 Зарубежное")
                                    if (enterprise.isSoluniTyulyukInzer) enterpriseTypes.add("🏔️ Солуни/Тюлюк/Инзер")
                                    if (enterprise.isDepartment) enterpriseTypes.add("🎓 Кафедра")
                                    if (enterprise.isUniversitySubdivision) enterpriseTypes.add("🏛️ Структурное подразделение")
                                    if (enterprise.isBaseDepartment) enterpriseTypes.add("🏭 Базовая кафедра")

                                    if (enterpriseTypes.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            "🏢 Тип предприятия:",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        enterpriseTypes.forEach { type ->
                                            Text(
                                                "• $type",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.tertiary,
                                                modifier = Modifier.padding(start = 8.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Информация о загруженном файле
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                "Информация о файле:",
                style = MaterialTheme.typography.titleMedium
            )

            Text("TXT документ: ${enterprisesFile?.name ?: "не выбран"}")
            enterprisesFile?.let { file ->
                Text("Путь: ${file.absolutePath}")
                Text("Размер: ${file.length() / 1024} KB")
                Text("Строк в файле: ${countLines(file)}")
            }
        }
    }

    // Расширенный диалог редактирования предприятия
    if (showEditDialog) {
        EnterpriseEditDialog(
            enterprise = selectedEnterprise,
            cityText = newCityText,
            onCityTextChange = { newCityText = it },
            supervisorsList = supervisorsList,
            onSupervisorsListChange = { supervisorsList = it },
            onSave = { updatedEnterprise ->
                viewModel.updateEnterpriseData(updatedEnterprise)
            },
            onDismiss = { viewModel.closeEditDialog() }
        )
    }
}

@Composable
private fun EnterpriseEditDialog(
    enterprise: Enterprise?,
    cityText: String,
    onCityTextChange: (String) -> Unit,
    supervisorsList: List<PracticeSupervisor>,
    onSupervisorsListChange: (List<PracticeSupervisor>) -> Unit,
    onSave: (Enterprise) -> Unit,
    onDismiss: () -> Unit
) {
    // Локальные состояния для булевых полей
    var isForeign by remember { mutableStateOf(enterprise?.isForeign ?: false) }
    var isSoluniTyulyukInzer by remember { mutableStateOf(enterprise?.isSoluniTyulyukInzer ?: false) }
    var isDepartment by remember { mutableStateOf(enterprise?.isDepartment ?: false) }
    var isUniversitySubdivision by remember { mutableStateOf(enterprise?.isUniversitySubdivision ?: false) }
    var isBaseDepartment by remember { mutableStateOf(enterprise?.isBaseDepartment ?: false) }

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
                // Название предприятия
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
                            enterprise?.name ?: "",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                // Редактирование города
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

                // Булевые поля предприятия
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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isForeign,
                                    onCheckedChange = { isForeign = it }
                                )
                                Text(
                                    "🌍 Зарубежное предприятие",
                                    modifier = Modifier.padding(start = 8.dp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isSoluniTyulyukInzer,
                                    onCheckedChange = { isSoluniTyulyukInzer = it }
                                )
                                Text(
                                    "🏔️ Солуни/Тюлюк/Инзер",
                                    modifier = Modifier.padding(start = 8.dp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isDepartment,
                                    onCheckedChange = { isDepartment = it }
                                )
                                Text(
                                    "🎓 Кафедра",
                                    modifier = Modifier.padding(start = 8.dp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isUniversitySubdivision,
                                    onCheckedChange = { isUniversitySubdivision = it }
                                )
                                Text(
                                    "🏛️ Структурное подразделение вуза",
                                    modifier = Modifier.padding(start = 8.dp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isBaseDepartment,
                                    onCheckedChange = { isBaseDepartment = it }
                                )
                                Text(
                                    "🏭 Базовая кафедра",
                                    modifier = Modifier.padding(start = 8.dp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                // Управление руководителями
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

                        // Список руководителей в ограниченном LazyColumn
                        if (supervisorsList.isNotEmpty()) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp), // Увеличена высота
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
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    enterprise?.let { ent ->
                        val updatedEnterprise = ent.copy(
                            city = cityText.takeIf { it.isNotBlank() },
                            supervisors = supervisorsList,
                            isForeign = isForeign,
                            isSoluniTyulyukInzer = isSoluniTyulyukInzer,
                            isDepartment = isDepartment,
                            isUniversitySubdivision = isUniversitySubdivision,
                            isBaseDepartment = isBaseDepartment
                        )
                        onSave(updatedEnterprise)
                    }
                },
                enabled = cityText.isNotBlank()
            ) {
                Text("💾 Сохранить изменения")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("❌ Отмена")
            }
        }
    )
}

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

// Функция для подсчета строк в TXT файле (локальная, только для UI)
private fun countLines(file: File): Int {
    return try {
        file.useLines { lines -> lines.count() }
    } catch (e: Exception) {
        -1
    }
}