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

/**
 * Экран управления предприятиями и базами практики
 * Позволяет загружать списки предприятий из TXT файлов и редактировать их данные
 * @param viewModel ViewModel для управления данными предприятий
 */
@Composable
fun Enterprises(viewModel: NornViewModel) {
    // Подписка на состояния из ViewModel
    val enterprisesFile by viewModel.enterprisesFile.collectAsState()
    val enterprisesList by viewModel.enterprisesList.collectAsState()
    val selectedEnterprise by viewModel.selectedEnterprise.collectAsState()
    val showEditDialog by viewModel.showEditDialog.collectAsState()

    // Локальные состояния для редактирования предприятия
    var newCityText by remember { mutableStateOf("") }
    var supervisorsList by remember { mutableStateOf<List<PracticeSupervisor>>(emptyList()) }

    // Обновляем локальные состояния когда выбираем предприятие для редактирования
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

        // Информационная карточка о новом формате файла
        FileFormatInfoCard()

        // Кнопки управления файлом предприятий
        FileManagementButtons(
            viewModel = viewModel,
            enterprisesList = enterprisesList
        )

        // Список загруженных предприятий
        if (enterprisesList.isNotEmpty()) {
            EnterprisesListCard(
                enterprisesList = enterprisesList,
                selectedEnterprise = selectedEnterprise,
                onEnterpriseSelect = { viewModel.selectEnterpriseForEditing(it) }
            )
        }

        // Информация о загруженном файле
        FileInfoSection(enterprisesFile = enterprisesFile)
    }

    // Диалог редактирования предприятия
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

/**
 * Информационная карточка о формате TXT файла
 */
@Composable
private fun FileFormatInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
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
}

/**
 * Кнопки для управления файлом предприятий
 */
@Composable
private fun FileManagementButtons(
    viewModel: NornViewModel,
    enterprisesList: List<Enterprise>
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        // Кнопка выбора TXT файла с предприятиями
        Button(
            onClick = {
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
            },
            colors = ButtonColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                disabledContentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Text("📁 Выбрать TXT документ\n(список предприятий)")
        }

        // Кнопка очистки списка предприятий
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
}

/**
 * Карточка со списком загруженных предприятий
 */
@Composable
private fun EnterprisesListCard(
    enterprisesList: List<Enterprise>,
    selectedEnterprise: Enterprise?,
    onEnterpriseSelect: (Enterprise) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(width = 2.dp, color = MaterialTheme.colorScheme.primary)
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

            // Прокручиваемый список предприятий
            LazyColumn(modifier = Modifier.height(400.dp)) {
                items(enterprisesList) { enterprise ->
                    EnterpriseCard(
                        enterprise = enterprise,
                        isSelected = selectedEnterprise?.name == enterprise.name,
                        onSelect = { onEnterpriseSelect(enterprise) }
                    )
                }
            }
        }
    }
}

/**
 * Карточка отдельного предприятия в списке
 */
@Composable
private fun EnterpriseCard(
    enterprise: Enterprise,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onSelect,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = BorderStroke(1.dp, color = MaterialTheme.colorScheme.tertiary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Название предприятия и город
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

            // Список руководителей практики
            SupervisorsList(enterprise.supervisors)

            // Типы предприятия (флаги)
            EnterpriseTypesList(enterprise)
        }
    }
}

/**
 * Отображение списка руководителей практики
 */
@Composable
private fun SupervisorsList(supervisors: List<PracticeSupervisor>) {
    if (supervisors.isNotEmpty()) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "👨‍💼 Руководители практики:",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        supervisors.forEach { supervisor ->
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
}

/**
 * Отображение типов предприятия
 */
@Composable
private fun EnterpriseTypesList(enterprise: Enterprise) {
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

/**
 * Секция с информацией о загруженном файле
 */
@Composable
private fun FileInfoSection(enterprisesFile: File?) {
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

/**
 * Расширенный диалог редактирования предприятия
 * Позволяет изменить город, руководителей и типы предприятия
 */
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
    // Локальные состояния для булевых полей типов предприятия
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
                // Название предприятия (только для чтения)
                EnterpriseNameCard(enterprise?.name ?: "")

                // Редактирование города
                CityEditCard(cityText, onCityTextChange)

                // Редактирование типов предприятия
                EnterpriseTypesCard(
                    isForeign = isForeign,
                    onForeignChange = { isForeign = it },
                    isSoluniTyulyukInzer = isSoluniTyulyukInzer,
                    onSoluniTyulyukInzerChange = { isSoluniTyulyukInzer = it },
                    isDepartment = isDepartment,
                    onDepartmentChange = { isDepartment = it },
                    isUniversitySubdivision = isUniversitySubdivision,
                    onUniversitySubdivisionChange = { isUniversitySubdivision = it },
                    isBaseDepartment = isBaseDepartment,
                    onBaseDepartmentChange = { isBaseDepartment = it }
                )

                // Управление руководителями
                SupervisorsManagementCard(supervisorsList, onSupervisorsListChange)
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
            TextButton(onClick = onDismiss) {
                Text("❌ Отмена")
            }
        }
    )
}

/**
 * Карточка с названием предприятия (только для чтения)
 */
@Composable
private fun EnterpriseNameCard(name: String) {
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
                name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

/**
 * Карточка редактирования города
 */
@Composable
private fun CityEditCard(
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
 * Карточка для редактирования типов предприятия
 */
@Composable
private fun EnterpriseTypesCard(
    isForeign: Boolean,
    onForeignChange: (Boolean) -> Unit,
    isSoluniTyulyukInzer: Boolean,
    onSoluniTyulyukInzerChange: (Boolean) -> Unit,
    isDepartment: Boolean,
    onDepartmentChange: (Boolean) -> Unit,
    isUniversitySubdivision: Boolean,
    onUniversitySubdivisionChange: (Boolean) -> Unit,
    isBaseDepartment: Boolean,
    onBaseDepartmentChange: (Boolean) -> Unit
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
                // Чекбоксы для всех типов предприятий
                EnterpriseTypeCheckbox("🌍 Зарубежное предприятие", isForeign) { checked ->
                    if (checked) {
                        // Uncheck all others when this is checked
                        onForeignChange(true)
                        onSoluniTyulyukInzerChange(false)
                        onDepartmentChange(false)
                        onUniversitySubdivisionChange(false)
                        onBaseDepartmentChange(false)
                    }
                }
                EnterpriseTypeCheckbox("🏔️ Солуни/Тюлюк/Инзер", isSoluniTyulyukInzer) { checked ->
                    if (checked) {
                        onForeignChange(false)
                        onSoluniTyulyukInzerChange(true)
                        onDepartmentChange(false)
                        onUniversitySubdivisionChange(false)
                        onBaseDepartmentChange(false)
                    }
                }
                EnterpriseTypeCheckbox("🎓 Кафедра", isDepartment) { checked ->
                    if (checked) {
                        onForeignChange(false)
                        onSoluniTyulyukInzerChange(false)
                        onDepartmentChange(true)
                        onUniversitySubdivisionChange(false)
                        onBaseDepartmentChange(false)
                    }
                }
                EnterpriseTypeCheckbox("🏛️ Структурное подразделение вуза", isUniversitySubdivision) { checked ->
                    if (checked) {
                        onForeignChange(false)
                        onSoluniTyulyukInzerChange(false)
                        onDepartmentChange(false)
                        onUniversitySubdivisionChange(true)
                        onBaseDepartmentChange(false)
                    }
                }
                EnterpriseTypeCheckbox("🏭 Базовая кафедра", isBaseDepartment) { checked ->
                    if (checked) {
                        onForeignChange(false)
                        onSoluniTyulyukInzerChange(false)
                        onDepartmentChange(false)
                        onUniversitySubdivisionChange(false)
                        onBaseDepartmentChange(true)
                    }
                }
                /*
                EnterpriseTypeCheckbox("🌍 Зарубежное предприятие", isForeign, onForeignChange)
                EnterpriseTypeCheckbox(
                    "🏔️ Солуни/Тюлюк/Инзер",
                    isSoluniTyulyukInzer,
                    onSoluniTyulyukInzerChange
                )
                EnterpriseTypeCheckbox("🎓 Кафедра", isDepartment, onDepartmentChange)
                EnterpriseTypeCheckbox(
                    "🏛️ Структурное подразделение вуза",
                    isUniversitySubdivision,
                    onUniversitySubdivisionChange
                )
                EnterpriseTypeCheckbox(
                    "🏭 Базовая кафедра",
                    isBaseDepartment,
                    onBaseDepartmentChange
                )
                 */
            }
        }
    }
}

/**
 * Чекбокс для типа предприятия
 */
@Composable
private fun EnterpriseTypeCheckbox(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
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
            label,
            modifier = Modifier.padding(start = 8.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/**
 * Карточка управления руководителями практики
 */
@Composable
private fun SupervisorsManagementCard(
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
            // Заголовок и кнопка добавления
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

            // Список руководителей или заглушка
            if (supervisorsList.isNotEmpty()) {
                SupervisorsEditList(supervisorsList, onSupervisorsListChange)
            } else {
                NoSupervisorsPlaceholder()
            }
        }
    }
}

/**
 * Список редактируемых руководителей
 */
@Composable
private fun SupervisorsEditList(
    supervisorsList: List<PracticeSupervisor>,
    onSupervisorsListChange: (List<PracticeSupervisor>) -> Unit
) {
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
}

/**
 * Заглушка когда нет руководителей
 */
@Composable
private fun NoSupervisorsPlaceholder() {
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

/**
 * Карточка для редактирования отдельного руководителя
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
            // Заголовок и кнопка удаления
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

            // Поля для редактирования ФИО и должности
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
 * Вспомогательная функция для подсчета строк в TXT файле
 * Используется только в UI для отображения информации о файле
 * @param file Файл для подсчета строк
 * @return Количество строк или -1 в случае ошибки
 */
private fun countLines(file: File): Int {
    return try {
        file.useLines { lines -> lines.count() }
    } catch (e: Exception) {
        -1
    }
}