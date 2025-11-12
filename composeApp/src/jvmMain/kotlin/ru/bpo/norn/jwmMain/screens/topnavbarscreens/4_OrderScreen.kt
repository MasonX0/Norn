package ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ru.bpo.norn.commonMain.models.OrderData
import ru.bpo.norn.commonMain.models.Student
import ru.bpo.norn.jwmMain.viewmodel.NornViewModel

/**
 * Экран генерации приказа по практике
 * Позволяет настроить данные приказа и сгенерировать документ Word
 * @param viewModel ViewModel для управления данными приказа и студентов
 */
@Composable
fun OrderScreen(viewModel: NornViewModel) {
    // Подписка на состояния из ViewModel
    val orderData by viewModel.orderData.collectAsState()
    val groups by viewModel.groups.collectAsState()

    // Получаем всех студентов из всех групп для статистики и группировки
    val allStudents = groups.flatMap { it.students }
    
    // Группировка студентов по типу финансирования для разных секций приказа
    val budgetStudents = allStudents.filter {
        it.formOfStudy.contains("бюджет", ignoreCase = true) ||
                (!it.withPayment && !it.formOfStudy.contains("платн", ignoreCase = true) && 
                 !it.formOfStudy.contains("целев", ignoreCase = true))
    }
    val targetStudents = allStudents.filter {
        it.formOfStudy.contains("целев", ignoreCase = true)
    }
    val paidStudents = allStudents.filter {
        it.withPayment || it.formOfStudy.contains("платн", ignoreCase = true)
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Левая часть - основная таблица и управление
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Генерация приказа по практике",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )

            // Статистика студентов
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Статистика загруженных студентов:",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("📊 Всего студентов:")
                        Text("${allStudents.size}", style = MaterialTheme.typography.titleMedium)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("💰 Бюджетная основа:")
                        Text("${budgetStudents.size}", color = MaterialTheme.colorScheme.primary)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("🎯 Целевая основа:")
                        Text("${targetStudents.size}", color = MaterialTheme.colorScheme.secondary)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("💳 Платная основа:")
                        Text("${paidStudents.size}", color = MaterialTheme.colorScheme.tertiary)
                    }

                    if (allStudents.isEmpty()) {
                        Text(
                            "⚠️ Нет загруженных студентов. Загрузите списки студентов в разделе 'Студенты'",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // Кнопка генерации приказа
            Button(
                onClick = {
                    viewModel.generateOrderDocument(orderData)
                },
                enabled = allStudents.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("📄 Сгенерировать приказ")
            }

            // Информация о месте сохранения
            if (allStudents.isNotEmpty()) {
                Text(
                    "💾 Приказ будет сохранен: ${viewModel.getOutputDirectory("Приказы").absolutePath}/Приказ_по_практике.docx",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Статус генерации документа
            val documentStatus by viewModel.orderGenerationStatus.collectAsState()
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

            // Превью приказа
            OrderPreview(
                orderData = orderData,
                budgetStudents = budgetStudents,
                targetStudents = targetStudents,
                paidStudents = paidStudents
            )
        }

        // Правая часть - форма редактирования данных приказа
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Редактирование данных приказа",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                OrderEditForm(
                    orderData = orderData,
                    onDataChange = { viewModel.updateOrderData(it) }
                )
            }
        }
    }
}

/**
 * Форма редактирования полей приказа
 * Содержит поля для настройки заголовков, оснований и подписей
 */
@Composable
fun OrderEditForm(
    orderData: OrderData,
    onDataChange: (OrderData) -> Unit
) {
    LazyColumn(
        modifier = Modifier.heightIn(max = 600.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                "Заголовки приказа",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }

        item {
            OutlinedTextField(
                value = orderData.headerText,
                onValueChange = { onDataChange(orderData.copy(headerText = it)) },
                label = { Text("Заголовок приказа") },
                placeholder = { Text("Проект приказа -4п от _________") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = orderData.titleText,
                onValueChange = { onDataChange(orderData.copy(titleText = it)) },
                label = { Text("Название приказа") },
                placeholder = { Text("Об учебной практике (ознакомительной практике)") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = orderData.instituteText,
                onValueChange = { onDataChange(orderData.copy(instituteText = it)) },
                label = { Text("Институт") },
                placeholder = { Text("По институту цифровых систем, автоматизации и энергетики процессов") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = orderData.streamName,
                onValueChange = { onDataChange(orderData.copy(streamName = it)) },
                label = { Text("Название потока") },
                placeholder = { Text("БПО09-24 и БПО09и-24 (автоматически вычисляется из групп)") },
                modifier = Modifier.fillMaxWidth(),
                supportingText = {
                    Text(
                        "💡 Оставьте пустым для автоматического вычисления на основе групп студентов",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
        }

        item {
            Text(
                "Основание и подписи",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }

        item {
            OutlinedTextField(
                value = orderData.basisText,
                onValueChange = { onDataChange(orderData.copy(basisText = it)) },
                label = { Text("Основание") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )
        }

        item {
            OutlinedTextField(
                value = orderData.agreeText,
                onValueChange = { onDataChange(orderData.copy(agreeText = it)) },
                label = { Text("Текст согласования") },
                placeholder = { Text("СОГЛАСОВАНО") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = orderData.proposerText,
                onValueChange = { onDataChange(orderData.copy(proposerText = it)) },
                label = { Text("Проект вносит") },
                placeholder = { Text("Проект вносит:\nИ.о. зав. кафедрой ВТИК_________ Д.М. Зарипов") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 3
            )
        }

        item {
            Text(
                "Подписанты",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }

        item {
            OutlinedTextField(
                value = orderData.prorectorName,
                onValueChange = { onDataChange(orderData.copy(prorectorName = it)) },
                label = { Text("Проректор по учебной работе") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = orderData.studyDepartmentHead,
                onValueChange = { onDataChange(orderData.copy(studyDepartmentHead = it)) },
                label = { Text("Начальник учебного отдела") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = orderData.partnershipDepartmentHead,
                onValueChange = { onDataChange(orderData.copy(partnershipDepartmentHead = it)) },
                label = { Text("Начальник отдела взаимодействия с организациями-партнёрами") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = orderData.legalDepartmentDeputy,
                onValueChange = { onDataChange(orderData.copy(legalDepartmentDeputy = it)) },
                label = { Text("Зам. начальника юридического отдела") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = orderData.practiceManager,
                onValueChange = { onDataChange(orderData.copy(practiceManager = it)) },
                label = { Text("Руководитель учебно-производственной практики") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = orderData.instituteDirector,
                onValueChange = { onDataChange(orderData.copy(instituteDirector = it)) },
                label = { Text("Директор IT-института") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = orderData.departmentHead,
                onValueChange = { onDataChange(orderData.copy(departmentHead = it)) },
                label = { Text("Зав. кафедрой ВТИК") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Text(
                "💡 Подсказка: Оставьте поля пустыми чтобы использовать значения по умолчанию",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun OrderPreview(
    orderData: OrderData,
    budgetStudents: List<Student>,
    targetStudents: List<Student>,
    paidStudents: List<Student>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "📄 Превью приказа",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            Text(
                "💡 Предварительный просмотр генерируемого приказа с актуальными данными",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            HorizontalDivider()
            
            // Заголовок приказа
            Text(
                orderData.headerText.ifBlank { "Проект приказа -4п от _________" },
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.End
            )
            
            Text(
                orderData.titleText.ifBlank { "Об учебной практике (ознакомительной практике)" },
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Text(
                orderData.instituteText.ifBlank { "По институту цифровых систем, автоматизации и энергетики процессов" },
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Секции студентов
            if (budgetStudents.isNotEmpty()) {
                OrderSection(
                    sectionNumber = 1,
                    fundingType = "бюджетной основе",
                    students = budgetStudents,
                    startIndex = 1,
                    streamName = orderData.streamName.ifBlank { calculateStreamName(budgetStudents) }
                )
            }
            
            if (targetStudents.isNotEmpty()) {
                OrderSection(
                    sectionNumber = 2,
                    fundingType = "целевой основе", 
                    students = targetStudents,
                    startIndex = budgetStudents.size + 1,
                    streamName = orderData.streamName.ifBlank { calculateStreamName(targetStudents) }
                )
            }
            
            if (paidStudents.isNotEmpty()) {
                OrderSection(
                    sectionNumber = 3,
                    fundingType = "платной основе",
                    students = paidStudents,
                    startIndex = budgetStudents.size + targetStudents.size + 1,
                    streamName = orderData.streamName.ifBlank { calculateStreamName(paidStudents) }
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))

            Spacer(modifier = Modifier.height(8.dp))

            // Основание
            Text(
                "Основание:",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                )
            )
            Text(
                orderData.basisText.ifBlank { "Представление и.о. зав. кафедрой «Вычислительная техника и инженерная кибернетика» Зарипова Д.М., виза согласования директора института цифровых систем, автоматизации и энергетики процессов Павловой З.Х." }
                    .removePrefix("Основание:").trim(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))

            // Все подписи
            Text(
                "Подписи:",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )

            val allSignatures = listOf(
                "Проректор по учебной работе" to orderData.prorectorName,
                "Начальник учебного отдела" to orderData.studyDepartmentHead,
                "Начальник отдела взаимодействия с организациями-партнёрами" to orderData.partnershipDepartmentHead,
                "Зам. начальника юридического отдела" to orderData.legalDepartmentDeputy,
                "Руководитель учебно-производственной практики" to orderData.practiceManager,
                "Директор IT-института" to orderData.instituteDirector
            )

            allSignatures.forEach { (position, name) ->
                Text(
                    "$position _________ ${name.ifBlank { "NULL" }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Согласование
            Text(
                orderData.agreeText.ifBlank { "СОГЛАСОВАНО" },
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Проект вносит
            Text(
                orderData.proposerText.ifBlank { "Проект вносит:\nИ.о. зав. кафедрой ВТИК _________ ${orderData.departmentHead.ifBlank { "NULL" }}" },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Общая статистика
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        "📊 Общая статистика приказа:",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )

                    val totalStudents =
                        budgetStudents.size + targetStudents.size + paidStudents.size

                    Text(
                        "• Всего студентов в приказе: $totalStudents",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        "• Бюджетников: ${budgetStudents.size}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        "• Целевиков: ${targetStudents.size}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        "• Платников: ${paidStudents.size}",
                        style = MaterialTheme.typography.bodySmall
                    )

                    val paidPracticeCount =
                        (budgetStudents + targetStudents + paidStudents).count { it.isPaidPractice }
                    Text(
                        "• С оплачиваемой практикой: $paidPracticeCount",
                        style = MaterialTheme.typography.bodySmall
                    )

                    val stationaryCount =
                        (budgetStudents + targetStudents + paidStudents).count { it.practiceForm == "стационарная" }
                    val fieldCount =
                        (budgetStudents + targetStudents + paidStudents).count { it.practiceForm == "выездная" }
                    Text(
                        "• Стационарная практика: $stationaryCount",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        "• Выездная практика: $fieldCount",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun OrderSection(
    sectionNumber: Int,
    fundingType: String,
    students: List<Student>,
    startIndex: Int,
    streamName: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            "$sectionNumber Нижеперечисленных студентов потока $streamName направления 09.03.01 Информатика и вычислительная техника, профиля «Технологии искусственного интеллекта в нефтегазовой отрасли», обучающихся на $fundingType, направить для прохождения практики на следующие базы практик:",
            style = MaterialTheme.typography.bodyMedium
        )
        
        // Показываем всех студентов с актуальными данными
        students.forEachIndexed { index, student ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "${startIndex + index}.",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                            modifier = Modifier.width(30.dp)
                        )
                        Text(
                            student.name.ifBlank { "NULL" },
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    // Актуальные данные студента
                    Text(
                        "База практики: ${student.nameOfPracticeBase.ifBlank { "NULL" }}, ${student.cityOfPractice.ifBlank { "NULL" }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        "Вид практики: ${student.typeOfPractice.ifBlank { "NULL" }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        "Сроки: ${student.periodOfPractice.ifBlank { "NULL" }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Форма: ${student.practiceForm.ifBlank { "NULL" }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Text(
                            if (student.isPaidPractice) "с оплатой" else "без оплаты",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (student.isPaidPractice) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Text(
                        "Руководитель: ${student.postOfHeadOfPracticeFromDepartment.ifBlank { "NULL" }} ${student.headOfPracticeFromDepartment.ifBlank { "NULL" }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            "Всего в секции: ${students.size} студентов",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}

// Helper function to calculate the stream name
fun calculateStreamName(students: List<Student>): String {
    val streams = students
        .map { it.group }
        .distinct()
        .mapNotNull { groupName ->
            // БПО09-24-01 -> БПО09-24, БПО09и-24-02 -> БПО09и-24
            val parts = groupName.split('-')
            if (parts.size > 1) {
                parts.dropLast(1).joinToString("-")
            } else {
                groupName // если нет тире, возвращаем как есть
            }
        }
        .distinct()
        .sorted()

    return if (streams.isNotEmpty()) {
        streams.joinToString(" и ")
    } else {
        "БПО09-24 и БПО09и-24" // fallback к дефолтному значению
    }
}