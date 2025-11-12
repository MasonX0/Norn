package ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.bpo.norn.commonMain.models.SummaryReportData
import ru.bpo.norn.commonMain.models.GroupStatistics
import ru.bpo.norn.jwmMain.viewmodel.NornViewModel
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter


/**
 * Экран генерации сводного отчета по практике
 * Отображает статистику по группам и позволяет генерировать отчет в формате Word
 * @param viewModel ViewModel для управления данными отчета
 */
@Composable
fun SummaryReport(viewModel: NornViewModel) {
    // Подписка на состояния из ViewModel
    val reportFile by viewModel.reportFile.collectAsState()

    // Локальные состояния для процесса генерации
    var generationResult by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // Получаем данные отчета и статистику групп
    val summaryReportData by viewModel.summaryReportData.collectAsState()
    val groupStatistics = remember(
        viewModel.groups.collectAsState().value,
        viewModel.enterprisesList.collectAsState().value
    ) {
        viewModel.getGroupStatistics()
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(15.dp)
            .verticalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        // Левая часть - основная таблица и управление
        MainReportSection(
            summaryReportData = summaryReportData,
            groupStatistics = groupStatistics,
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

/**
 * Основная секция с таблицей отчета и управлением
 */
@Composable
private fun MainReportSection(
    summaryReportData: SummaryReportData,
    groupStatistics: Map<String, GroupStatistics>,
    viewModel: NornViewModel,
    generationResult: String?,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
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
                // Таблица статистики групп
                SummaryTable(groupStatistics)
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
            groupStatistics = groupStatistics
        )

        // Статус генерации документа
        DocumentGenerationStatus(viewModel)

        // Информация о состоянии
        ReportStatusInfo(
            viewModel = viewModel,
            groupStatistics = groupStatistics,
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
    groupStatistics: Map<String, GroupStatistics>
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Button(
            onClick = {
                val reportData = summaryReportData
                val statistics = viewModel.getGroupStatistics()
                viewModel.generateSummaryReportWithoutTemplate(reportData, statistics)
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
    groupStatistics: Map<String, GroupStatistics>,
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
        val totalStudents = groupStatistics.values.sumOf { it.totalStudents }
        val totalGroups = groupStatistics.size

        Text("Загружено групп: $totalGroups")
        Text("Всего студентов: $totalStudents")

        // Статус готовности
        if (totalGroups > 0) {
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
 * Основная таблица сводного отчета с статистикой по группам
 * Содержит две секции: статистика по местам практики и результаты защиты
 */
@Composable
fun SummaryTable(groupStatistics: Map<String, GroupStatistics>) {
    if (groupStatistics.isEmpty()) {
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

            // Таблица статистики по местам практики
            PracticeStatisticsTable(groupStatistics)

            Spacer(modifier = Modifier.height(16.dp))

            // Заголовок второй таблицы
            Text(
                "7. Результаты защиты отчетов по практике:",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Таблица результатов защиты
            DefenseResultsTable(groupStatistics)
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
private fun PracticeStatisticsTable(groupStatistics: Map<String, GroupStatistics>) {
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

    // Строки данных для каждой группы
    groupStatistics.forEach { (groupName, stats) ->
        PracticeStatisticsRow(groupName, stats)

        // Дополнительная строка для иностранных студентов (если есть)
        if (stats.foreignStudents > 0) {
            ForeignStudentsRow(stats)
        }

        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    }
}

/**
 * Строка данных по статистике практики для группы
 */
@Composable
private fun PracticeStatisticsRow(groupName: String, stats: GroupStatistics) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TableDataCell(groupName, Modifier.weight(1.5f))
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
private fun DefenseResultsTable(groupStatistics: Map<String, GroupStatistics>) {
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
        Box(modifier = Modifier.weight(2f)) // Пустое место под "Группа"
        Box(modifier = Modifier.weight(1f)) // Пустое место под "Количество студентов"
        Row(modifier = Modifier.weight(3f)) {
            TableHeaderCell("отлично", Modifier.weight(1f))
            TableHeaderCell("хорошо", Modifier.weight(1f))
            TableHeaderCell("удовлетворит.", Modifier.weight(1f))
        }
        Box(modifier = Modifier.weight(1f)) // Пустое место под "Не защитили в срок"
    }

    Divider()

    // Данные результатов защиты по группам
    groupStatistics.forEach { (groupName, stats) ->
        DefenseResultsRow(groupName, stats)
        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    }
}

/**
 * Строка результатов защиты для группы
 */
@Composable
private fun DefenseResultsRow(groupName: String, stats: GroupStatistics) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TableDataCell(groupName, Modifier.weight(2f))
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
        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
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