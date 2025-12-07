package ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.bpo.norn.commonMain.models.Group
import ru.bpo.norn.commonMain.models.SummaryReportData
import ru.bpo.norn.commonMain.models.GroupStatistics
import ru.bpo.norn.jwmMain.viewmodel.NornViewModel
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter
import kotlin.collections.forEach


/**
 * Экран генерации сводного отчета по практике
 * Отображает статистику по потокам и позволяет генерировать отчет в формате Word
 * @param viewModel ViewModel для управления данными отчета
 */
@Composable
fun SummaryReport(viewModel: NornViewModel) {
    // Подписка на состояния из ViewModel
    val reportFile by viewModel.reportFile.collectAsState()
    val groups by viewModel.groups.collectAsState()
    val selectedGroupsForOrder by viewModel.selectedGroupsForReport.collectAsState()

    // Локальные состояния для процесса генерации
    var generationResult by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // Получаем данные отчета и статистику по группам
    val summaryReportData by viewModel.summaryReportData.collectAsState()
//    val groupStatistics = remember(
//        viewModel.groups.collectAsState().value,
//        viewModel.enterprisesList.collectAsState().value
//    ) {
//        viewModel.getGroupStatistics1(groups)
//    }
    val groupStatistics = viewModel.getGroupStatistics1(selectedGroupsForOrder)
    Column(modifier = Modifier.fillMaxSize()) {
        if (groups.isNotEmpty()) {
            GroupSelectionForOrderCard(
                groups = groups,
                selectedGroups = selectedGroupsForOrder,
                onToggleGroup = { groupName -> viewModel.toggleGroupForReport(groupName) },
                onSelectAll = { viewModel.selectAllGroupsForReport() },
                onClearAll = { viewModel.clearGroupsForReport() }
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(15.dp)
                .verticalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(15.dp)

        )
        {
            // Левая часть - основная таблица и управление
            MainReportSection(
                summaryReportData = summaryReportData,
                streamStatistics = groupStatistics,
                viewModel = viewModel,
                generationResult = generationResult,
                isLoading = isLoading,
                modifier = Modifier.weight(2f)
            )


            // Правая часть - компактная форма редактирования (всегда видна)
            EditFormSection(
                summaryReportData = summaryReportData,
                onDataChanged = { newData -> viewModel.updateSummaryReportData(newData) },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )

        }
    }

}

@Composable
private fun GroupSelectionForOrderCard(
    groups: List<Group>,
    selectedGroups: List<Group>,
    onToggleGroup: (Group) -> Unit,
    onSelectAll: () -> Unit,
    onClearAll: () -> Unit
) {
    // Локальное состояние для фильтра по курсу
    var selectedCourseFilter by remember { mutableStateOf<Int?>(null) }

    // Получаем уникальные курсы из всех групп
    val availableCourses = groups.map { it.course }.toSet().sorted()

    // Фильтруем группы по выбранному курсу
    val filteredGroups = if (selectedCourseFilter == null) {
        groups
    } else {
        groups.filter { it.course == selectedCourseFilter }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "🎓 Выбор групп для приказа:",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(onClick = {
                        // Выбираем все отфильтрованные группы
                        filteredGroups.forEach { group ->
                            if (!selectedGroups.contains(group)) {
                                onToggleGroup(group)
                            }
                        }
                    }) {
                        Text("Выбрать ${if (selectedCourseFilter != null) "отфильтрованные" else "все"}")
                    }
                    TextButton(onClick = onClearAll) {
                        Text("Очистить")
                    }
                }
            }

            // Фильтр по курсу
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "📚 Фильтр по курсу:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Кнопка "Все курсы"
                        FilterChip(
                            onClick = { selectedCourseFilter = null },
                            label = { Text("Все курсы") },
                            selected = selectedCourseFilter == null,
                            leadingIcon = if (selectedCourseFilter == null) {
                                {
                                    Text("V")
                                }
                            } else null
                        )

                        // Кнопки для каждого курса
                        availableCourses.forEach { course ->
                            val groupsForCourse = groups.filter { it.course == course }
                            FilterChip(
                                onClick = {
                                    selectedCourseFilter =
                                        if (selectedCourseFilter == course) null else course
                                },
                                label = { Text("${course} курс (${groupsForCourse.size})") },
                                selected = selectedCourseFilter == course,
                                leadingIcon = if (selectedCourseFilter == course) {
                                    {
                                        Text("V")
                                    }
                                } else null
                            )
                        }
                    }

                    // Информация о фильтрации
                    if (selectedCourseFilter != null) {
                        Text(
                            "🔍 Показаны группы ${selectedCourseFilter} курса: ${filteredGroups.size} из ${groups.size}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            // Информация о выборе
            if (selectedGroups.isEmpty()) {
                Text(
                    "💡 Группы не выбраны - будут использованы все загруженные группы",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val selectedGroupsInFilter =
                    selectedGroups.intersect(filteredGroups.map { it.name }.toSet()).size
                Text(
                    "✅ Выбрано групп: ${selectedGroups.size} из ${groups.size}" +
                            if (selectedCourseFilter != null) " (в фильтре: $selectedGroupsInFilter из ${filteredGroups.size})" else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Список групп с чекбоксами (отфильтрованный)
            LazyColumn(
                modifier = Modifier.heightIn(max = 250.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(filteredGroups) { group ->
                    GroupCheckboxItem(
                        group = group,
                        isSelected = selectedGroups.contains(group),
                        onToggle = { onToggleGroup(group) }
                    )
                }

                // Показываем сообщение если нет групп в фильтре
                if (filteredGroups.isEmpty() && selectedCourseFilter != null) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                            )
                        ) {
                            Text(
                                text = "🔍 Нет групп ${selectedCourseFilter} курса",
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
@Composable
private fun GroupSelectionSection(
    groups: List<ru.bpo.norn.commonMain.models.Group>,
    selectedGroup: ru.bpo.norn.commonMain.models.Group?,
    onGroupSelect: (ru.bpo.norn.commonMain.models.Group) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "👥 Выбор группы:",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (groups.isNotEmpty()) {
                // Список групп для выбора
                LazyColumn(
                    modifier = Modifier.heightIn(max = 200.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    itemsIndexed(groups) { index: Int, group: ru.bpo.norn.commonMain.models.Group ->
                        GroupSelectionCard(
                            group = group,
                            isSelected = selectedGroup?.name == group.name,
                            onSelect = { onGroupSelect(group) }
                        )
                    }
                }
            } else {
                // Заглушка когда нет групп
                Text(
                    "⚠️ Нет загруженных групп. Загрузите студентов в разделе 'Студенты'",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
@Composable
private fun GroupCheckboxItem(
    group: Group,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() }
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = group.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "${group.students.size} студентов",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "${group.course} курс",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            if (isSelected) {
                Text(
                    text = "V",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
@Composable
private fun GroupSelectionCard(
    group: ru.bpo.norn.commonMain.models.Group,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else null
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                group.name,
                style = MaterialTheme.typography.titleSmall,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
            Text(
                "Студентов: ${group.students.size}",
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}
/**
 * Основная секция с таблицей отчета и управлением
 */
@Composable
private fun MainReportSection(
    summaryReportData: SummaryReportData,
    streamStatistics: Map<String, GroupStatistics>,
    viewModel: NornViewModel,
    generationResult: String?,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    // Группируем статистику по потокам
    val groupedStreamStatistics = groupStatisticsByStream(streamStatistics)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        // Заголовок отчета
        ReportHeader(summaryReportData)

        Spacer(modifier = Modifier.height(8.dp))

        // Главная таблица с прокруткой
        LazyColumn(
            modifier = Modifier.height(400.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                // Таблица статистики по потокам
                SummaryTable(groupedStreamStatistics)
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                // Дополнительные поля отчета
                SummaryReportFields(summaryReportData)
            }
        }

        // Кнопка генерации отчета
        GenerateReportButton(
            viewModel = viewModel,
            summaryReportData = summaryReportData,
            streamStatistics = groupedStreamStatistics
        )

        // Статус генерации документа
        DocumentGenerationStatus(viewModel)

        // Информация о состоянии
        ReportStatusInfo(
            viewModel = viewModel,
            streamStatistics = groupedStreamStatistics,
            generationResult = generationResult,
            isLoading = isLoading
        )
    }
}

/**
 * Заголовок отчета с названием кафедры и учебным годом
 */
@Composable
private fun ReportHeader(summaryReportData: SummaryReportData) {
    Column {
        Text(
            summaryReportData.departmentName,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            summaryReportData.academicYear,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Кнопка генерации отчета
 */
@Composable
private fun GenerateReportButton(
    viewModel: NornViewModel,
    summaryReportData: SummaryReportData,
    streamStatistics: Map<String, GroupStatistics>
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Button(
            onClick = {
                val reportData = summaryReportData
                val statistics = viewModel.getGroupStatistics()
                viewModel.generateSummaryReportWithoutTemplate(reportData, streamStatistics)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("📄 Сгенерировать отчет")
        }
    }
}

/**
 * Отображение статуса генерации документа
 */
@Composable
private fun DocumentGenerationStatus(viewModel: NornViewModel) {
    val documentStatus by viewModel.reportGenerationStatus.collectAsState()

    if (documentStatus.isNotEmpty()) {
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    documentStatus.startsWith("✅") -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                    documentStatus.startsWith("❌") -> Color(0xFFF44336).copy(alpha = 0.1f)
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
            )
        ) {
            Text(
                text = documentStatus,
                modifier = Modifier.padding(12.dp),
                color = when {
                    documentStatus.startsWith("✅") -> Color(0xFF2E7D32)
                    documentStatus.startsWith("❌") -> Color(0xFFD32F2F)
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/**
 * Информация о состоянии отчета и готовности к генерации
 */
@Composable
private fun ReportStatusInfo(
    viewModel: NornViewModel,
    streamStatistics: Map<String, GroupStatistics>,
    generationResult: String?,
    isLoading: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            "Информация о генерации:",
            style = MaterialTheme.typography.titleMedium
        )

        // Статистика по загруженным данным
        val totalStudents = streamStatistics.values.sumOf { it.totalStudents }
        val totalStreams = streamStatistics.size

        Text("Загружено потоков: $totalStreams")
        Text("Всего студентов: $totalStudents")

        // Статус готовности
        if (totalStreams > 0) {
            Text(
                "✅ Готов к генерации отчета",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            Text(
                "⚠️ Загрузите данные студентов для генерации",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // Путь сохранения
        Text(
            "💾 Отчет будет сохранен: ${viewModel.getOutputDirectory("Отчеты").absolutePath}/Сводный_отчет_по_практике.docx",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Результат генерации
        generationResult?.let { result ->
            Text(
                result,
                style = MaterialTheme.typography.bodyMedium,
                color = if (result.contains("✅")) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.error
            )
        }

        // Индикатор загрузки
        if (isLoading) {
            Text("⏳ Идет генерация отчета...")
        }
    }
}

/**
 * Секция с формой редактирования данных отчета
 */
@Composable
private fun EditFormSection(
    summaryReportData: SummaryReportData,
    onDataChanged: (SummaryReportData) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        CompactEditForm(
            summaryReportData = summaryReportData,
            onDataChanged = onDataChanged,
            modifier = Modifier.padding(12.dp)
        )
    }
}

/**
 * Основная таблица сводного отчета с статистикой по потокам
 * Содержит две секции: статистика по местам практики и результаты защиты
 */
@Composable
fun SummaryTable(streamStatistics: Map<String, GroupStatistics>) {
    if (streamStatistics.isEmpty()) {
        // Заглушка когда нет данных
        EmptyDataPlaceholder()
        return
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Заголовок первой таблицы
            Text(
                "1. Количество студентов на практике",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Таблица статистики по местам практики (по потокам)
            PracticeStatisticsTable(streamStatistics)

            Spacer(modifier = Modifier.height(16.dp))

            // Заголовок второй таблицы
            Text(
                "7. Результаты защиты отчетов по практике:",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Таблица результатов защиты (по потокам)
            DefenseResultsTable(streamStatistics)
        }
    }
}

/**
 * Заглушка для отображения когда нет данных
 */
@Composable
private fun EmptyDataPlaceholder() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Нет данных для отображения.\nЗагрузите студентов и предприятия.",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

/**
 * Таблица статистики по местам практики
 */
@Composable
private fun PracticeStatisticsTable(streamStatistics: Map<String, GroupStatistics>) {
    // Заголовки колонок
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TableHeaderCell("Группа", Modifier.weight(1.5f))
        TableHeaderCell("Сроки практики", Modifier.weight(1.5f))
        TableHeaderCell(
            "Вид практики\n(учебная, произв.,\nпреддипломная, НИР)",
            Modifier.weight(1.5f)
        )
        TableHeaderCell("Всего\nчел", Modifier.weight(1f))
        TableHeaderCell("Предприятия\nзаруб", Modifier.weight(1f))
        TableHeaderCell("Предприятия\nРФ", Modifier.weight(1f))
        TableHeaderCell("Солуни,\nТюлюк Инзер", Modifier.weight(1f))
        TableHeaderCell("Кафедра", Modifier.weight(1f))
        TableHeaderCell("Структурные\nподразделения\nвуза", Modifier.weight(1f))
        TableHeaderCell("Базовые\nкафедры", Modifier.weight(1f))
        TableHeaderCell("Кол-во студ.\nна оплачиваемых\nместах", Modifier.weight(1f))
    }

    Divider()

    // Строки данных для каждого потока
    var foreignStats = GroupStatistics(0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, "", "", "",
        emptyList())
    streamStatistics.forEach { (streamName, stats) ->
        PracticeStatisticsRow(streamName, stats)

        // Дополнительная строка для иностранных студентов (если есть)
        if (stats.foreignStudents > 0) {
            foreignStats.foreignStudents+=stats.foreignStudents
        }

        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    }
    ForeignStudentsRow(foreignStats)
}

/**
 * Строка данных по статистике практики для потока
 */
@Composable
private fun PracticeStatisticsRow(streamName: String, stats: GroupStatistics) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TableDataCell(stats.groupNames.joinToString(separator = ", "), Modifier.weight(1.5f))
        TableDataCell(
            stats.practiceStartDate + "-" + stats.practiceEndDate,
            Modifier.weight(1.5f)
        )
        TableDataCell(stats.practiceType, Modifier.weight(1.5f))
        TableDataCell(stats.totalStudents.toString(), Modifier.weight(1f))
        TableDataCell(stats.foreignEnterprises.toString(), Modifier.weight(1f))
        TableDataCell(stats.rfEnterprises.toString(), Modifier.weight(1f))
        TableDataCell(stats.soluniTyulyukInzer.toString(), Modifier.weight(1f))
        TableDataCell(stats.departmentStudents.toString(), Modifier.weight(1f))
        TableDataCell(stats.universitySubdivisions.toString(), Modifier.weight(1f))
        TableDataCell(stats.baseDepartments.toString(), Modifier.weight(1f))
        TableDataCell(stats.paidPracticeStudents.toString(), Modifier.weight(1f))
    }
}

/**
 * Дополнительная строка для иностранных студентов
 */
@Composable
private fun ForeignStudentsRow(stats: GroupStatistics) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(vertical = 2.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TableDataCell("из них иностранных\nстудентов", Modifier.weight(1.5f))
        TableDataCell("", Modifier.weight(1.5f)) // Сроки практики
        TableDataCell("", Modifier.weight(1.5f)) // Вид практики
        TableDataCell(stats.foreignStudents.toString(), Modifier.weight(1f))
        TableDataCell("0", Modifier.weight(1f))
        TableDataCell("0", Modifier.weight(1f))
        TableDataCell("0", Modifier.weight(1f))
        TableDataCell(stats.foreignStudents.toString(), Modifier.weight(1f))
        TableDataCell("0", Modifier.weight(1f))
        TableDataCell("0", Modifier.weight(1f))
        TableDataCell("0", Modifier.weight(1f))
    }
}

/**
 * Таблица результатов защиты отчетов
 */
@Composable
private fun DefenseResultsTable(streamStatistics: Map<String, GroupStatistics>) {
    // Заголовки для результатов защиты
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TableHeaderCell("Группа", Modifier.weight(2f))
        TableHeaderCell("Количество\nстудентов", Modifier.weight(1f))
        TableHeaderCell("Из них с оценкой", Modifier.weight(3f))
        TableHeaderCell("Не защитили\nв срок", Modifier.weight(1f))
    }

    // Подзаголовки для оценок
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(2f)) // Пустое место под "Поток"
        Box(modifier = Modifier.weight(1f)) // Пустое место под "Количество студентов"
        Row(modifier = Modifier.weight(3f)) {
            TableHeaderCell("отлично", Modifier.weight(1f))
            TableHeaderCell("хорошо", Modifier.weight(1f))
            TableHeaderCell("удовлетворит.", Modifier.weight(1f))
        }
        Box(modifier = Modifier.weight(1f)) // Пустое место под "Не защитили в срок"
    }

    Divider()

    // Данные результатов защиты по потокам
    streamStatistics.forEach { (streamName, stats) ->
        DefenseResultsRow(streamName, stats)
        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    }
}

/**
 * Строка результатов защиты для потока
 */
@Composable
private fun DefenseResultsRow(streamName: String, stats: GroupStatistics) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TableDataCell(stats.groupNames.joinToString(separator = ", "), Modifier.weight(2f))
        TableDataCell(stats.totalStudents.toString(), Modifier.weight(1f))
        Row(modifier = Modifier.weight(3f)) {
            TableDataCell(stats.excellentGrades.toString(), Modifier.weight(1f))
            TableDataCell(stats.goodGrades.toString(), Modifier.weight(1f))
            TableDataCell(stats.satisfactoryGrades.toString(), Modifier.weight(1f))
        }
        TableDataCell(stats.notDefended.toString(), Modifier.weight(1f))
    }
}

/**
 * Дополнительные поля отчета (текстовые секции)
 */
@Composable
fun SummaryReportFields(summaryReportData: SummaryReportData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Все дополнительные поля отчета
            ReportField("2. Экскурсии:", summaryReportData.field2_excursions)
            ReportField("3. Преподаватели:", summaryReportData.field3_teachers)
            ReportField(
                "4. Студенты, не прошедшие практику:",
                summaryReportData.field4_absentStudents
            )
            ReportField("5. Дополнительные сведения:", summaryReportData.field5_additionalInfo)
            ReportField(
                "6. Организационные мероприятия:",
                summaryReportData.field6_preliminaryEvents
            )
            ReportField("8. Недостатки:", summaryReportData.field8_shortcomings)
            ReportField("9. Предложения:", summaryReportData.field9_improvements)
            ReportField("10. Заключение:", summaryReportData.field10_conclusion)
        }
    }
}

/**
 * Отдельное поле отчета с меткой и значением
 */
@Composable
fun ReportField(label: String, value: String) {
    Column {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
        )
    }
}

/**
 * Компактная форма редактирования данных отчета
 * Располагается в правой части экрана
 */
@Composable
fun CompactEditForm(
    summaryReportData: SummaryReportData,
    onDataChanged: (SummaryReportData) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.heightIn(max = 600.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                "Редактирование данных отчета",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                fontSize = 14.sp
            )
        }

        // Основные поля отчета
        item {
            CompactEditTextField(
                label = "Название кафедры",
                value = summaryReportData.departmentName,
                onValueChange = { onDataChanged(summaryReportData.copy(departmentName = it)) }
            )
        }

        item {
            CompactEditTextField(
                label = "Учебный год",
                value = summaryReportData.academicYear,
                onValueChange = { onDataChanged(summaryReportData.copy(academicYear = it)) }
            )
        }

        // Дополнительные поля отчета
        item {
            CompactEditTextField(
                label = "Поле 2: Экскурсии",
                value = summaryReportData.field2_excursions,
                onValueChange = { onDataChanged(summaryReportData.copy(field2_excursions = it)) },
                maxLines = 2
            )
        }

        item {
            CompactEditTextField(
                label = "Поле 3: Преподаватели",
                value = summaryReportData.field3_teachers,
                onValueChange = { onDataChanged(summaryReportData.copy(field3_teachers = it)) },
                maxLines = 3
            )
        }

        item {
            CompactEditTextField(
                label = "Поле 4: Студенты, не прошедшие практику",
                value = summaryReportData.field4_absentStudents,
                onValueChange = { onDataChanged(summaryReportData.copy(field4_absentStudents = it)) },
                maxLines = 2
            )
        }

        item {
            CompactEditTextField(
                label = "Поле 5: Дополнительные сведения",
                value = summaryReportData.field5_additionalInfo,
                onValueChange = { onDataChanged(summaryReportData.copy(field5_additionalInfo = it)) },
                maxLines = 2
            )
        }

        item {
            CompactEditTextField(
                label = "Поле 6: Организационные мероприятия",
                value = summaryReportData.field6_preliminaryEvents,
                onValueChange = { onDataChanged(summaryReportData.copy(field6_preliminaryEvents = it)) },
                maxLines = 3
            )
        }

        item {
            CompactEditTextField(
                label = "Поле 8: Недостатки",
                value = summaryReportData.field8_shortcomings,
                onValueChange = { onDataChanged(summaryReportData.copy(field8_shortcomings = it)) },
                maxLines = 2
            )
        }

        item {
            CompactEditTextField(
                label = "Поле 9: Предложения",
                value = summaryReportData.field9_improvements,
                onValueChange = { onDataChanged(summaryReportData.copy(field9_improvements = it)) },
                maxLines = 2
            )
        }

        item {
            CompactEditTextField(
                label = "Поле 10: Заключение",
                value = summaryReportData.field10_conclusion,
                onValueChange = { onDataChanged(summaryReportData.copy(field10_conclusion = it)) },
                maxLines = 2
            )
        }
    }
}

/**
 * Компактное текстовое поле для формы редактирования
 */
@Composable
fun CompactEditTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    maxLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                label,
                fontSize = 11.sp,
                maxLines = 1
            )
        },
        modifier = Modifier.fillMaxWidth(),
        maxLines = maxLines,
        minLines = 1,
        textStyle = TextStyle(fontSize = 12.sp)
    )
}

/**
 * Заголовок ячейки таблицы
 */
@Composable
fun TableHeaderCell(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier.padding(4.dp),
        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
        textAlign = TextAlign.Center,
        fontSize = 11.sp
    )
}

/**
 * Ячейка данных таблицы
 */
@Composable
fun TableDataCell(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier.padding(4.dp),
        style = MaterialTheme.typography.bodySmall,
        textAlign = TextAlign.Center,
        fontSize = 11.sp
    )
}

/**
 * Группирует статистику групп по потокам
 * Например, БПО09и-23-01 и БПО09и-23-02 становятся БПО09и-23
 * @param groupStatistics Исходная статистика по группам
 * @return Статистика, сгруппированная по потокам
 */
private fun groupStatisticsByStream(groupStatistics: Map<String, GroupStatistics>): Map<String, GroupStatistics> {

    val res = groupStatistics.entries
        .groupBy { entry ->
            // Извлекаем поток из названия группы (убираем последний сегмент после дефиса)
            extractStreamFromGroupName(entry.key)
        }
        .mapValues { (streamName, groupEntries) ->
            // Объединяем статистику всех групп в потоке
            val allStats = groupEntries.map { entry -> entry.value }
            val originalGroupNames = groupEntries.map {it.key}
            combineGroupStatistics(streamName, allStats, originalGroupNames)
        }
    return res
}

/**
 * Извлекает название потока из названия группы
 * Объединяет потоки с "и" в конце с обычными потоками
 *
 * Примеры работы:
 * - "БПО09-24-01" → "БПО09-24"
 * - "БПО09и-24-01" → "БПО09-24" (убирается "и")
 * - "БПО09и-24-02" → "БПО09-24" (убирается "и")
 * - "ИВТ03-24-01" → "ИВТ03-24"
 * - "ПИ09и-25-03" → "ПИ09-25" (убирается "и")
 * - "МатМод-22-01" → "МатМод-22"
 *
 * В результате группы с одинаковыми потоками будут объединены:
 * - БПО09-24-01 + БПО09и-24-01 + БПО09и-24-02 → БПО09-24 (суммарная статистика)
 */
private fun extractStreamFromGroupName(groupName: String): String {
    val parts = groupName.split("-")
    return if (parts.size >= 2) {
        parts.dropLast(1).drop(1).joinToString("-")
    } else {
        groupName // Если формат не соответствует ожидаемому, возвращаем как есть
    }
}

/**
 * Объединяет статистику нескольких групп в один поток
 */
private fun combineGroupStatistics(streamName: String, statsList: List<GroupStatistics>, originalGroupNames: List<String>): GroupStatistics {
    if (statsList.isEmpty()) {
        // Возвращаем пустую статистику если список пуст
        return GroupStatistics(
            totalStudents = 0,
            foreignStudents = 0,
            paidPracticeStudents = 0,
            foreignEnterprises = 0,
            rfEnterprises = 0,
            soluniTyulyukInzer = 0,
            departmentStudents = 0,
            universitySubdivisions = 0,
            baseDepartments = 0,
            excellentGrades = 0,
            goodGrades = 0,
            satisfactoryGrades = 0,
            notDefended = 0,
            practiceStartDate = "",
            practiceEndDate = "",
            practiceType = "",
            groupNames = emptyList()
        )
    }
    
    // Берем первую статистику как базу для временных данных
    val firstStats = statsList.first()
    val res = mutableListOf<String>()
    val seen = mutableSetOf<String>()
    originalGroupNames.forEach { original ->
        val processed = original.substringBeforeLast("-")
        if (seen.add(processed)) {
            res.add(processed)
        }
    }
    val stats = GroupStatistics(
        totalStudents = statsList.sumOf { it.totalStudents },
        foreignStudents = statsList.sumOf { it.foreignStudents },
        paidPracticeStudents = statsList.sumOf { it.paidPracticeStudents },
        foreignEnterprises = statsList.sumOf { it.foreignEnterprises },
        rfEnterprises = statsList.sumOf { it.rfEnterprises },
        soluniTyulyukInzer = statsList.sumOf { it.soluniTyulyukInzer },
        departmentStudents = statsList.sumOf { it.departmentStudents },
        universitySubdivisions = statsList.sumOf { it.universitySubdivisions },
        baseDepartments = statsList.sumOf { it.baseDepartments },
        excellentGrades = statsList.sumOf { it.excellentGrades },
        goodGrades = statsList.sumOf { it.goodGrades },
        satisfactoryGrades = statsList.sumOf { it.satisfactoryGrades },
        notDefended = statsList.sumOf { it.notDefended },
        // Для дат и типа практики берем данные из первой группы
        // (предполагается, что в рамках потока они одинаковые)
        practiceStartDate = firstStats.practiceStartDate,
        practiceEndDate = firstStats.practiceEndDate,
        practiceType = firstStats.practiceType,
        groupNames = res.toList()
    )
    return stats
}