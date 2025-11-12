package ru.bpo.norn.jwmMain.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.apache.poi.xwpf.usermodel.XWPFParagraph
import ru.bpo.norn.commonMain.data.coursework.mock.mockData.mockStudent1
import ru.bpo.norn.commonMain.models.Group
import ru.bpo.norn.commonMain.models.Student
import ru.bpo.norn.commonMain.models.Enterprise
import ru.bpo.norn.commonMain.models.PracticeSupervisor
import ru.bpo.norn.commonMain.models.SummaryReportData
import ru.bpo.norn.commonMain.repository.NornRepository
import viewmodel.Screen
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.charset.Charset

class NornViewModel {

    private val _isDarkTheme = MutableStateFlow<Boolean>(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()


    fun switchTheme(): Unit{
        _isDarkTheme.value=!_isDarkTheme.value
    }

    // ==================== 1. ОСНОВНЫЕ НАСТРОЙКИ И НАВИГАЦИЯ ====================
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Screen1)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _screen1Data = MutableStateFlow("Привет от экрана 1!")
    val screen1Data: StateFlow<String> = _screen1Data.asStateFlow()

    private val _screen2Data = MutableStateFlow("Привет от экрана 2!")
    val screen2Data: StateFlow<String> = _screen2Data.asStateFlow()

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun updateScreen1Data(newData: String) {
        _screen1Data.value = newData
    }

    fun updateScreen2Data(newData: String) {
        _screen2Data.value = newData
    }

    // ==================== 2. РЕПОЗИТОРИЙ И БАЗОВЫЕ ДАННЫЕ ====================
    private val repository = NornRepository()
    val groups: StateFlow<List<Group>> = repository.groups

    fun addGroup(group: Group) {
        repository.addGroup(group)
    }

    fun addStudentsToGroup(groupName: String, students: List<Student>) {
        repository.addStudentsToGroup(groupName, students)
    }

    fun selectStudent(student: Student?) {
        repository.setSelectedStudent(student)
    }

    // ==================== 3. ФАЙЛЫ ДЛЯ РАЗНЫХ ЭКРАНОВ ====================

    // 3.1 InfoScreen файлы
    private val _reportFile = MutableStateFlow<File?>(null)
    val reportFile: StateFlow<File?> = _reportFile.asStateFlow()

    private val _directionFile = MutableStateFlow<File?>(null)
    val directionFile: StateFlow<File?> = _directionFile.asStateFlow()

    fun selectReportFile(file: File) {
        _reportFile.value = file
    }

    fun selectDirectionFile(file: File) {
        _directionFile.value = file
    }

    // 3.2 SummaryReport файлы
    // (добавьте при необходимости)

    // 3.3 Destination файлы
    // (добавьте при необходимости)

    // 3.4 Order файлы
    private val _orderFile = MutableStateFlow<File?>(null)
    val orderFile: StateFlow<File?> = _orderFile.asStateFlow()

    fun selectOrderFile(file: File) {
        _orderFile.value = file
    }

    // 3.5 Enterprises файлы
    private val _enterprisesFile = MutableStateFlow<File?>(null)
    val enterprisesFile: StateFlow<File?> = _enterprisesFile.asStateFlow()

    private val _enterprisesList = MutableStateFlow<List<Enterprise>>(emptyList())
    val enterprisesList: StateFlow<List<Enterprise>> = _enterprisesList.asStateFlow()

    private val _selectedEnterprise = MutableStateFlow<Enterprise?>(null)
    val selectedEnterprise: StateFlow<Enterprise?> = _selectedEnterprise.asStateFlow()

    private val _selectedSupervisor = MutableStateFlow<PracticeSupervisor?>(null)
    val selectedSupervisor: StateFlow<PracticeSupervisor?> = _selectedSupervisor.asStateFlow()

    private val _showEditDialog = MutableStateFlow(false)
    val showEditDialog: StateFlow<Boolean> = _showEditDialog.asStateFlow()

    fun selectEnterprisesFile(file: File?) {
        _enterprisesFile.value = file
    }

    /**
     * Читает список предприятий из TXT файла с новым форматом
     * Формат: ООО Газпром Межрегионгаз Уфа, г. Уфа // ст. преподаватель ! М.А. Салихова, доц. ! А.И.Сидоров
     */
    fun readEnterprisesFromTxt(file: File): List<Enterprise> {
        return try {
            file.readLines(Charset.forName("Windows-1251"))
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .mapNotNull { line -> parseEnterpriseLine(line) }
                .toList()
                .also { list ->
                    _enterprisesList.value = list
                }
        } catch (e: Exception) {
            println("❌ Ошибка чтения файла: ${e.message}")
            emptyList()
        }
    }

    /**
     * Парсит строку с предприятием в новом формате
     * Формат: ООО Газпром Межрегионгаз Уфа, г. Уфа // ст. преподаватель ! М.А. Салихова, доц. ! А.И.Сидоров
     */
    private fun parseEnterpriseLine(line: String): Enterprise? {
        return try {
            // Разделяем по "//" для отделения предприятия от руководителей
            val parts = line.split("//").map { it.trim() }
            
            if (parts.isEmpty()) return null
            
            val enterprisePart = parts[0]
            val supervisorsPart = if (parts.size > 1) parts[1] else ""
            
            // Парсим предприятие и город
            val (name, city) = parseEnterpriseNameAndCity(enterprisePart)
            
            // Парсим руководителей
            val supervisors = if (supervisorsPart.isNotEmpty()) {
                parseSupervisors(supervisorsPart)
            } else {
                emptyList()
            }
            
            // Определяем тип предприятия по названию
            val isForeign = isEnterpriseTypeMatch(name, city, "зарубежное")
            val isSoluniTyulyukInzer = isEnterpriseTypeMatch(name, city, "солуни_тюлюк_инзер")
            val isDepartment = isEnterpriseTypeMatch(name, city, "кафедра")
            val isUniversitySubdivision = isEnterpriseTypeMatch(name, city, "структурное_подразделение")
            val isBaseDepartment = isEnterpriseTypeMatch(name, city, "базовая_кафедра")
            
            Enterprise(
                name = name,
                city = city,
                supervisors = supervisors,
                isForeign = isForeign,
                isSoluniTyulyukInzer = isSoluniTyulyukInzer,
                isDepartment = isDepartment,
                isUniversitySubdivision = isUniversitySubdivision,
                isBaseDepartment = isBaseDepartment
            )
        } catch (e: Exception) {
            println("❌ Ошибка парсинга строки '$line': ${e.message}")
            null
        }
    }

    /**
     * Определяет тип предприятия по ключевым словам
     */
    private fun isEnterpriseTypeMatch(name: String, city: String?, type: String): Boolean {
        val fullText = "$name ${city ?: ""}".lowercase()
        
        return when (type) {
            "зарубежное" -> {
                // Примеры зарубежных предприятий или городов
                listOf("германия", "китай", "сша", "франция", "япония", "корея", "индия", "турция", "казахстан", "беларусь")
                    .any { fullText.contains(it) }
            }
            "солуни_тюлюк_инзер" -> {
                listOf("солуни", "тюлюк", "инзер").any { fullText.contains(it) }
            }
            "кафедра" -> {
                listOf("кафедра", "каф.", "втик", "вычислительная техника", "инженерная кибернетика")
                    .any { fullText.contains(it) }
            }
            "структурное_подразделение" -> {
                listOf("фгбоу", "угнту", "университет", "институт", "филиал", "факультет")
                    .any { fullText.contains(it) } && !fullText.contains("кафедра")
            }
            "базовая_кафедра" -> {
                listOf("базовая кафедра", "базовая каф").any { fullText.contains(it) }
            }
            else -> false
        }
    }

    /**
     * Парсит название предприятия и город
     */
    private fun parseEnterpriseNameAndCity(enterprisePart: String): Pair<String, String?> {
        val cleanName = enterprisePart.trim()
        
        if (cleanName.contains(",")) {
            val parts = cleanName.split(",")
            val name = parts.dropLast(1).joinToString(",").trim()
            val cityPart = parts.last().trim()
            val city = cityPart
                .removePrefix("г.")
                .removePrefix("г")
                .removePrefix("с.")
                .removePrefix("с")
                .removePrefix("д.")
                .removePrefix("д")
                .trim()
            
            return name to if (city.isNotBlank()) city else null
        }
        
        return cleanName to null
    }

    /**
     * Парсит руководителей из строки
     * Формат: ст. преподаватель ! М.А. Салихова, доц. ! А.И.Сидоров
     */
    private fun parseSupervisors(supervisorsPart: String): List<PracticeSupervisor> {
        return supervisorsPart.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .mapNotNull { supervisorStr ->
                // Разделяем по "!" для отделения должности от ФИО
                val supervisorParts = supervisorStr.split("!").map { it.trim() }
                if (supervisorParts.size >= 2) {
                    val position = supervisorParts[0].trim()
                    val fullName = supervisorParts[1].trim()
                    if (position.isNotEmpty() && fullName.isNotEmpty()) {
                        PracticeSupervisor(fullName = fullName, position = position)
                    } else null
                } else null
            }
    }

    /**
     * Извлекает город из названия предприятия (для обратной совместимости)
     */
    fun extractCityFromEnterpriseSmart(enterpriseName: String): String? {
        val (_, city) = parseEnterpriseNameAndCity(enterpriseName)
        return city
    }

    fun clearEnterprisesList() {
        _enterprisesList.value = emptyList()
    }

    fun selectEnterpriseForEditing(enterprise: Enterprise) {
        _selectedEnterprise.value = enterprise
        _showEditDialog.value = true
    }

    fun updateEnterpriseCity(newCity: String) {
        val current = _selectedEnterprise.value
        if (current != null) {
            val updatedEnterprise = current.copy(city = newCity.takeIf { it.isNotBlank() })
            val updatedList = _enterprisesList.value.map { item ->
                if (item.name == current.name) updatedEnterprise else item
            }
            _enterprisesList.value = updatedList
            _showEditDialog.value = false
            _selectedEnterprise.value = null
        }
    }

    fun closeEditDialog() {
        _showEditDialog.value = false
        _selectedEnterprise.value = null
    }

    fun updateEnterpriseData(updatedEnterprise: Enterprise) {
        val currentList = _enterprisesList.value
        val updatedList = currentList.map { enterprise ->
            if (enterprise.name == updatedEnterprise.name) {
                updatedEnterprise
            } else {
                enterprise
            }
        }
        _enterprisesList.value = updatedList
        _showEditDialog.value = false
        _selectedEnterprise.value = null
        println("✅ Данные предприятия обновлены: ${updatedEnterprise.name}")
    }

    fun selectEnterpriseForDisplay(enterprise: Enterprise) {
        _selectedEnterprise.value = enterprise
        // Сбрасываем выбранного руководителя при смене предприятия
        _selectedSupervisor.value = null
    }

    fun selectSupervisor(supervisor: PracticeSupervisor) {
        _selectedSupervisor.value = supervisor
    }

    // 3.6 StudentsList файлы
    private val _studentsListFile = MutableStateFlow<File?>(null)
    val studentsListFile: StateFlow<File?> = _studentsListFile.asStateFlow()

    fun selectStudentsListFile(file: File) {
        _studentsListFile.value = file
    }

    // 3.7 Statements файлы
    private val _statementsFile = MutableStateFlow<File?>(null)
    val statementsFile: StateFlow<File?> = _statementsFile.asStateFlow()

    fun selectStatementsFile(file: File) {
        _statementsFile.value = file
    }

    // ==================== 4. СТУДЕНТЫ И ГРУППЫ ====================

    // StateFlow для работы со студентами
    private val _selectedGroup = MutableStateFlow<Group?>(null)
    val selectedGroup: StateFlow<Group?> = _selectedGroup.asStateFlow()

    private val _selectedStudent = MutableStateFlow<Student?>(null)
    val selectedStudent: StateFlow<Student?> = _selectedStudent.asStateFlow()

    private val _showStudentEditDialog = MutableStateFlow(false)
    val showStudentEditDialog: StateFlow<Boolean> = _showStudentEditDialog.asStateFlow()

    // Mock студент для тестирования
    private val mockStudent: Student = mockStudent1

    /**
     * Загружает студентов из Excel файла и добавляет в группу
     */
    fun loadStudentsFromExcel(file: File) {
        println("🚀 Загрузка файла: ${file.name}")
        try {
            val students = parseStudentsFromExcel(file)
            println("📊 Найдено студентов: ${students.size}")

            if (students.isEmpty()) {
                println("⚠️ В файле не найдено студентов")
                return
            }

            val groupName = file.nameWithoutExtension
            addStudentsToGroup(groupName, students)

            // Выбираем загруженную группу
            val group = repository.getGroupByName(groupName)
            _selectedGroup.value = group

            println("✅ Студенты добавлены в группу: $groupName")

        } catch (e: Exception) {
            println("💥 Ошибка загрузки: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * Выбирает группу для просмотра
     */
    fun selectGroup(group: Group) {
        _selectedGroup.value = group
    }

    /**
     * Удаляет группу из списка
     */
    fun removeGroup(group: Group) {
        repository.removeGroup(group)

        if (_selectedGroup.value?.name == group.name) {
            _selectedGroup.value = repository.groups.value.firstOrNull()
        }
    }
    fun updateGroupData(updatedGroup: Group) {
        repository.updateGroup(updatedGroup)
        // Обновляем выбранную группу если она та же самая
        if (_selectedGroup.value?.name == updatedGroup.name) {
            _selectedGroup.value = updatedGroup
        }
    }
    /**
     * Парсит студентов из Excel файла
     */
    private fun parseStudentsFromExcel(file: File): List<Student> {
        val students = mutableListOf<Student>()
        try {
            println("🔄 === НАЧАЛО ПАРСИНГА ===")
            println("📄 Файл: ${file.name}")

            WorkbookFactory.create(file).use { workbook ->
                val sheet = workbook.getSheetAt(0)
                println("📋 Лист: '${sheet.sheetName}'")
                println("📏 Всего строк: ${sheet.lastRowNum + 1}")

                var branch = ""
                var faculty = ""
                var group = file.nameWithoutExtension
                var fundingType = ""

                for (rowIndex in 0..sheet.lastRowNum) {
                    val row = sheet.getRow(rowIndex)
                    if (row == null) {
                        println("➖ Строка $rowIndex: ПУСТАЯ")
                        continue
                    }

                    val cells = (0..10).map { index ->
                        row.getCell(index)?.toString()?.trim() ?: ""
                    }

                    val cell0 = cells[0]
                    val cell1 = cells[1]
                    val cell2 = cells[2]
                    val cell3 = cells[3]

                    println("📝 Строка $rowIndex: '$cell0' | '$cell1' | '$cell2' | '$cell3'")

                    // Парсим информацию о филиале, факультете
                    when {
                        cell0.startsWith("Филиал:") -> {
                            branch = cell0.removePrefix("Филиал:").trim()
                            println("📍 Филиал: $branch")
                        }
                        cell0.startsWith("Факультет:") -> {
                            faculty = cell0.removePrefix("Факультет:").trim()
                            println("🎓 Факультет: $faculty")
                        }
                        cell0.startsWith("Направление:") -> {
                            fundingType = cell0.removePrefix("Направление:").trim()
                            println("💰 Направление: $fundingType")
                        }
                        cell0.startsWith("Список студентов группы") -> {
                            val extractedGroup = cell0.removePrefix("Список студентов группы").trim()
                            if (extractedGroup.isNotBlank()) {
                                group = extractedGroup
                            }
                            println("👥 Группа из заголовка: $group")
                        }
                    }

                    // Проверка для студентов
                    val isStudentRow = when {
                        cell0.matches(Regex("\\d+")) -> true
                        cell0.matches(Regex("\\d+\\.0")) -> true
                        cell1.matches(Regex("\\d+")) -> true
                        cell1.matches(Regex("\\d+\\.0")) -> true
                        cell2.isNotBlank() && cell3.matches(Regex("\\d+")) -> true
                        else -> false
                    }

                    if (isStudentRow) {
                        println("🎯 ВОЗМОЖНЫЙ СТУДЕНТ в строке $rowIndex")

                        val id = when {
                            cell0.matches(Regex("\\d+")) -> cell0
                            cell0.matches(Regex("\\d+\\.0")) -> cell0.removeSuffix(".0")
                            else -> ""
                        }

                        val number = when {
                            cell1.matches(Regex("\\d+")) -> cell1.toIntOrNull() ?: 0
                            cell1.matches(Regex("\\d+\\.0")) -> cell1.removeSuffix(".0").toIntOrNull() ?: 0
                            else -> 0
                        }

                        val fullName = cell2
                        val recordBook = when {
                            cell3.matches(Regex("\\d+")) -> cell3
                            cell3.matches(Regex("\\d+\\.0")) -> cell3.removeSuffix(".0")
                            else -> ""
                        }

                        if (fullName.isNotBlank() && recordBook.isNotBlank()) {
                            students.add(
                                Student(
                                    name = fullName,
                                    course = 2,
                                    codeOfDirection = "09.03.01",
                                    nameOfDirection = faculty.ifBlank { "Информатика и вычислительная техника" },
                                    group = group,
                                    isForeign = false,
                                    gradeForPractice = "не установлено",
                                    nameOfPracticeBase = "ФГБОУ ВО УГНТУ каф. ВТИК",
                                    typeOfPractice = "учебная",
                                    periodOfPractice = "23.06.2025 -06.07.2025",
                                    formOfStudy = fundingType.ifBlank { "бюджетная" },
                                    withPayment = fundingType.contains("платн", ignoreCase = true),
                                    isPaidPractice = fundingType.contains(
                                        "платн",
                                        ignoreCase = true
                                    ), // Обновлено поле isPaidPractice
                                    cityOfPractice = branch.ifBlank { "Уфа" },
                                    nameOfSpeciality = "Технологии искусственного интеллекта",
                                    codeOfSpeciality = "БПО09",
                                    headOfPracticeFromDepartment = "Кондратьев Д.В.",
                                    headOfPracticeFromPracticeBase = "Петров П.П.",
                                    postOfHeadOfPracticeFromPracticeBase = "руководитель",
                                    postOfHeadOfPracticeFromDepartment = "доц.",
                                    directorName = "Сидоров С.С."
                                )
                            )
                            println("✅ ДОБАВЛЕН СТУДЕНТ: $number. $fullName ($recordBook)")
                        } else {
                            println("❌ НЕДОСТАТОЧНО ДАННЫХ: Имя='$fullName' Зачетка='$recordBook'")
                        }
                    }
                }
            }

            println("🎯 === РЕЗУЛЬТАТ ПАРСИНГА ===")
            println("📊 Найдено студентов: ${students.size}")
            students.forEachIndexed { index, student ->
                println("   ${index + 1}. ${student.name} (${student.group})")
            }

        } catch (e: Exception) {
            println("❌ Ошибка парсинга: ${e.message}")
            e.printStackTrace()
        }
        return students
    }

    fun selectStudentForEditing(student: Student) {
        _selectedStudent.value = student
        _showStudentEditDialog.value = true
    }

    fun updateStudentData(updatedStudent: Student) {
        val currentGroup = _selectedGroup.value

        if (currentGroup != null) {
            val updatedStudents = currentGroup.students.map { student ->
                if (student.name == updatedStudent.name) {
                    updatedStudent
                } else {
                    student
                }
            }

            repository.updateGroupStudents(currentGroup.name, updatedStudents)

            // Обновляем выбранную группу
            _selectedGroup.value = repository.getGroupByName(currentGroup.name)

            // Не сбрасываем выбранного студента при групповом обновлении
            if (_selectedStudent.value?.name != updatedStudent.name) {
                _showStudentEditDialog.value = false
                _selectedStudent.value = null
            }

            println("✅ Данные студента обновлены: ${updatedStudent.name}")
        }
    }

    fun closeStudentEditDialog() {
        _showStudentEditDialog.value = false
        _selectedStudent.value = null
    }

    // ==================== 5. ДАННЫЕ ОТЧЕТА ПО ПРАКТИКЕ ====================
    
    private val _summaryReportData = MutableStateFlow(SummaryReportData())
    val summaryReportData: StateFlow<SummaryReportData> = _summaryReportData.asStateFlow()
    
    fun updateSummaryReportData(data: SummaryReportData) {
        _summaryReportData.value = data
    }

    /**
     * Подсчитывает статистику студентов по группам и типам предприятий
     */
    fun getGroupStatistics(): Map<String, ru.bpo.norn.commonMain.models.GroupStatistics> {
        val allGroups = repository.groups.value
        val allEnterprises = _enterprisesList.value
        
        return allGroups.associate { group ->
            group.name to group.calculateStatistics(allEnterprises)
        }
    }

    // ==================== 6. ГЕНЕРАЦИЯ ДОКУМЕНТОВ ====================
    /**
     * Основная функция для генерации документов
     */
    fun generatePracticeDocument(templateFile: File): Boolean {
        return try {
            val originalName = templateFile.nameWithoutExtension
            val extension = templateFile.extension
            val outputFile = File(
                templateFile.parent,
                "${originalName}_заполненный_.$extension"
            )

            val success = replacePlaceholdersInWord(templateFile, mockStudent, outputFile)

            if (success) {
                println("✅ Документ создан: ${outputFile.absolutePath}")
                true
            } else {
                println("❌ Ошибка при создании документа")
                false
            }
        } catch (e: Exception) {
            println("❌ Исключение при создании документа: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    /**
     * Генерирует приказ по всем загруженным группам с разделением по форме обучения
     */
    fun generateOrderDocument(): Boolean {
        return try {
            val outputFile =
                File(System.getProperty("user.home"), "Desktop/Приказ_по_практике.docx")

            // Получаем всех студентов из всех групп
            val allStudents = repository.groups.value.flatMap { it.students }

            if (allStudents.isEmpty()) {
                println("❌ Нет загруженных студентов для создания приказа")
                return false
            }

            val success = createOrderDocument(allStudents, outputFile)

            if (success) {
                println("✅ Приказ создан: ${outputFile.absolutePath}")
                true
            } else {
                println("❌ Ошибка при создании приказа")
                false
            }
        } catch (e: Exception) {
            println("❌ Исключение при создании приказа: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    /**
     * Создает Word документ с приказом, разделенным по формам обучения
     */
    private fun createOrderDocument(students: List<Student>, outputFile: File): Boolean {
        return try {
            // Группируем студентов по форме обучения с улучшенной логикой
            val budgetStudents = students.filter {
                it.formOfStudy.contains("бюджет", ignoreCase = true) ||
                        (!it.withPayment && !it.formOfStudy.contains("платн", ignoreCase = true) &&
                                !it.formOfStudy.contains("целев", ignoreCase = true))
            }.sortedBy { it.name }

            val targetStudents = students.filter {
                it.formOfStudy.contains("целев", ignoreCase = true)
            }.sortedBy { it.name }

            val paidStudents = students.filter {
                it.withPayment || it.formOfStudy.contains("платн", ignoreCase = true)
            }.sortedBy { it.name }

            // Создаем новый документ
            XWPFDocument().use { document ->
                // Заголовок документа
                addOrderHeader(document)

                var globalIndex = 1

                // Секция для бюджетных студентов
                if (budgetStudents.isNotEmpty()) {
                    globalIndex = addStudentsSection(
                        document,
                        budgetStudents,
                        1,
                        "бюджетной основе",
                        globalIndex
                    )
                }

                // Секция для целевых студентов
                if (targetStudents.isNotEmpty()) {
                    globalIndex = addStudentsSection(
                        document,
                        targetStudents,
                        2,
                        "целевой основе",
                        globalIndex
                    )
                }

                // Секция для платных студентов
                if (paidStudents.isNotEmpty()) {
                    addStudentsSection(document, paidStudents, 3, "платной основе", globalIndex)
                }

                // Подписи
                addOrderFooter(document)

                // Сохраняем документ
                FileOutputStream(outputFile).use { fos ->
                    document.write(fos)
                }
            }

            true
        } catch (e: Exception) {
            println("❌ Ошибка создания приказа: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    /**
     * Добавляет заголовок приказа
     */
    private fun addOrderHeader(document: XWPFDocument) {
        // Заголовок
        val headerParagraph = document.createParagraph()
        headerParagraph.alignment = org.apache.poi.xwpf.usermodel.ParagraphAlignment.CENTER
        val headerRun = headerParagraph.createRun()
        headerRun.setText("Проект приказа -4п от _________")
        headerRun.setFontSize(12)
        headerRun.setFontFamily("Times New Roman")
        headerRun.isBold = true

        val titleParagraph = document.createParagraph()
        titleParagraph.alignment = org.apache.poi.xwpf.usermodel.ParagraphAlignment.CENTER
        val titleRun = titleParagraph.createRun()
        titleRun.setText("Об учебной практике (ознакомительной практике)")
        titleRun.setFontSize(12)
        titleRun.setFontFamily("Times New Roman")
        titleRun.isBold = true

        val instituteParagraph = document.createParagraph()
        instituteParagraph.alignment = org.apache.poi.xwpf.usermodel.ParagraphAlignment.CENTER
        val instituteRun = instituteParagraph.createRun()
        instituteRun.setText("По институту цифровых систем, автоматизации и энергетики процессов")
        instituteRun.setFontSize(12)
        instituteRun.setFontFamily("Times New Roman")

        // Пустая строка
        document.createParagraph()
    }

    /**
     * Добавляет секцию со студентами определенной формы обучения
     */
    private fun addStudentsSection(
        document: XWPFDocument,
        students: List<Student>,
        sectionNumber: Int,
        fundingType: String,
        startIndex: Int
    ): Int {
        // Заголовок секции
        val sectionParagraph = document.createParagraph()
        val sectionRun = sectionParagraph.createRun()
        sectionRun.setText("$sectionNumber Нижеперечисленных студентов потока БПО09-24 и БПО09и-24 направления 09.03.01 Информатика и вычислительная техника, профиля «Технологии искусственного интеллекта в нефтегазовой отрасли», обучающихся на $fundingType, направить для прохождения практики на следующие базы практик:")
        sectionRun.setFontSize(12)
        sectionRun.setFontFamily("Times New Roman")

        // Создаем таблицу
        val table = document.createTable()
        table.width = 10000

        // Заголовок таблицы
        val headerRow = table.getRow(0)
        headerRow.getCell(0).setText("№пп")
        headerRow.addNewTableCell().setText("Ф. И. О. практиканта\n(в именительном падеже)")
        headerRow.addNewTableCell().setText("Наименование база практики, населенный пункт")
        headerRow.addNewTableCell().setText("Вид и тип практики")
        headerRow.addNewTableCell().setText("Сроки практики")
        headerRow.addNewTableCell().setText("Форма практики")
        headerRow.addNewTableCell().setText("с оплатой/ без оплаты")
        headerRow.addNewTableCell().setText("Руководитель по практике на кафедре")

        // Применяем стиль к заголовку
        for (cell in headerRow.tableCells) {
            val paragraph = cell.paragraphs[0]
            paragraph.alignment = org.apache.poi.xwpf.usermodel.ParagraphAlignment.CENTER
            val run = paragraph.runs[0]
            run.setFontSize(11)
            run.setFontFamily("Times New Roman")
            run.isBold = true
        }

        // Добавляем строки со студентами
        var currentIndex = startIndex
        students.forEach { student ->
            val row = table.createRow()

            row.getCell(0).setText(currentIndex.toString())
            row.getCell(1).setText(student.name)
            row.getCell(2).setText("${student.nameOfPracticeBase}, ${student.cityOfPractice}")
            row.getCell(3).setText("${student.typeOfPractice} (тип: ознакомительная, 3 з.е.)")
            row.getCell(4).setText(student.periodOfPractice)
            row.getCell(5).setText("стационарная")
            row.getCell(6).setText(if (student.isPaidPractice) "с оплатой" else "без оплаты")
            row.getCell(7)
                .setText("${student.postOfHeadOfPracticeFromDepartment}\n${student.headOfPracticeFromDepartment}")

            // Применяем стиль к строке
            for (cell in row.tableCells) {
                val paragraph = cell.paragraphs[0]
                val run =
                    if (paragraph.runs.isNotEmpty()) paragraph.runs[0] else paragraph.createRun()
                run.setFontSize(10)
                run.setFontFamily("Times New Roman")
            }

            currentIndex++
        }

        // Пустая строка после таблицы
        document.createParagraph()

        return currentIndex
    }

    /**
     * Добавляет подписи в конец приказа
     */
    private fun addOrderFooter(document: XWPFDocument) {
        // Основание
        val basisParagraph = document.createParagraph()
        val basisRun = basisParagraph.createRun()
        basisRun.setText("Основание: Представление и.о. зав. кафедрой «Вычислительная техника и инженерная кибернетика» Зарипова Д.М.,\n\tвиза согласования директора института цифровых систем, автоматизации и энергетики процессов Павловой З.Х.")
        basisRun.setFontSize(12)
        basisRun.setFontFamily("Times New Roman")

        // Пустые строки
        document.createParagraph()
        document.createParagraph()

        // Подписи
        val signatures = listOf(
            "Проректор по учебной работе" to "_________  А.И. Могучев",
            "Начальник учебного отдела" to "_________  Н.В. Заиченко",
            "Начальник отдела взаимодействия с организациями-партнёрами" to "_________  Р.Р. Даминов",
            "Зам. начальника юридического отдела" to "_________ Р.Ф. Хуснулина",
            "Руководитель учебно-производственной практики" to "_________ Э.Р. Читахян",
            "Директор IT-института" to "_________ З.Х. Павлова"
        )

        signatures.forEach { (position, signature) ->
            val signatureParagraph = document.createParagraph()
            val signatureRun = signatureParagraph.createRun()
            signatureRun.setText("$position\t\t\t\t\t\t\t\t\t$signature")
            signatureRun.setFontSize(12)
            signatureRun.setFontFamily("Times New Roman")
        }

        // Согласовано
        val agreeParagraph = document.createParagraph()
        agreeParagraph.alignment = org.apache.poi.xwpf.usermodel.ParagraphAlignment.LEFT
        val agreeRun = agreeParagraph.createRun()
        agreeRun.setText("СОГЛАСОВАНО")
        agreeRun.setFontSize(12)
        agreeRun.setFontFamily("Times New Roman")
        agreeRun.isBold = true

        // Проект вносит
        val proposerParagraph = document.createParagraph()
        val proposerRun = proposerParagraph.createRun()
        proposerRun.setText("Проект вносит:\nИ.о. зав. кафедрой ВТИК\t\t\t\t\t\t\t\t\t_________ Д.М. Зарипов")
        proposerRun.setFontSize(12)
        proposerRun.setFontFamily("Times New Roman")
    }

    /**
     * Заменяет плейсхолдеры в Word документе
     */
    private fun replacePlaceholdersInWord(templateFile: File, student: Student, outputFile: File): Boolean {
        return try {
            FileInputStream(templateFile).use { fis ->
                XWPFDocument(fis).use { document ->
                    for (paragraph in document.paragraphs) {
                        replaceInParagraph(paragraph, student)
                    }
                    for (table in document.tables) {
                        for (row in table.rows) {
                            for (cell in row.tableCells) {
                                for (cellParagraph in cell.paragraphs) {
                                    replaceInParagraph(cellParagraph, student)
                                }
                            }
                        }
                    }
                    FileOutputStream(outputFile).use { fos ->
                        document.write(fos)
                    }
                }
            }
            println("✅ Поля заменены в документе")
            true
        } catch (e: Exception) {
            println("❌ Ошибка при замене полей: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    private fun replaceInParagraph(paragraph: XWPFParagraph, student: Student) {
        val text = paragraph.text
        if (text.contains("{") && text.contains("}")) {
            val replacements = mapOf(
                "{name}" to student.name,
                "{group}" to student.group,
                "{c}" to student.course.toString(),
                "{typeOfPractice}" to student.typeOfPractice,
                "{nameOfPracticeBase}" to student.nameOfPracticeBase,
                "{cityOfPractice}" to (student.cityOfPractice ?: ""),
                "{periodOfPractice}" to student.periodOfPractice,
                "{headPrac}" to student.headOfPracticeFromPracticeBase,
                "{headOfPracticeFromDepartment}" to student.headOfPracticeFromDepartment,
                "{postOfHeadOfPracticeFromDepartment}" to student.postOfHeadOfPracticeFromDepartment,
                "{dataIaV}" to "04.04.2025 г.",
                "{dataIaP}" to "04.04.2025 г.",
                "{dataOtz}" to "16.05.2025 г.",
                "{codeOfDirection}" to student.codeOfDirection,
                "{nameOfDirection}" to student.nameOfDirection,
                "{directorName}" to student.directorName,
            )

            val placeholderUnderlineMap = mutableMapOf<String, Boolean>()
            replacements.keys.forEach { placeholder ->
                if (text.contains(placeholder)) {
                    val startIndex = text.indexOf(placeholder)
                    val endIndex = startIndex + placeholder.length
                    placeholderUnderlineMap[placeholder] = isTextUnderlined(paragraph, startIndex, endIndex)
                }
            }

            while (paragraph.runs.isNotEmpty()) {
                paragraph.removeRun(0)
            }

            var remainingText = text
            while (remainingText.isNotEmpty()) {
                val openBraceIndex = remainingText.indexOf("{")
                val closeBraceIndex = remainingText.indexOf("}")

                if (openBraceIndex != -1 && closeBraceIndex != -1 && openBraceIndex < closeBraceIndex) {
                    val beforePlaceholder = remainingText.substring(0, openBraceIndex)
                    val placeholder = remainingText.substring(openBraceIndex, closeBraceIndex + 1)
                    val afterPlaceholder = remainingText.substring(closeBraceIndex + 1)

                    if (beforePlaceholder.isNotEmpty()) {
                        val run = paragraph.createRun()
                        run.setText(beforePlaceholder)
                        run.setFontSize(12)
                        run.setFontFamily("Times New Roman")
                    }

                    val value = replacements[placeholder] ?: placeholder
                    val run = paragraph.createRun()
                    run.setText(value)
                    run.setFontSize(12)
                    run.setFontFamily("Times New Roman")

                    if (placeholderUnderlineMap[placeholder] == true) {
                        run.setUnderline(org.apache.poi.xwpf.usermodel.UnderlinePatterns.SINGLE)
                    }

                    remainingText = afterPlaceholder
                } else {
                    val run = paragraph.createRun()
                    run.setText(remainingText)
                    run.setFontSize(12)
                    run.setFontFamily("Times New Roman")
                    break
                }
            }
        }
    }

    private fun isTextUnderlined(paragraph: XWPFParagraph, startIndex: Int, endIndex: Int): Boolean {
        var currentPos = 0
        for (run in paragraph.runs) {
            val runText = run.getText(0) ?: ""
            val runStart = currentPos
            val runEnd = currentPos + runText.length
            if (runStart <= endIndex && runEnd >= startIndex) {
                if (run.getUnderline() != org.apache.poi.xwpf.usermodel.UnderlinePatterns.NONE) {
                    return true
                }
            }
            currentPos = runEnd
        }
        return false
    }

    // ==================== 6. УТИЛИТЫ ====================

    fun getMockStudent(): Student {
        return mockStudent
    }

    /**
     * Генерирует отчет по практике в формате Word
     */
    fun generateSummaryReport(
        templateFile: File, 
        reportData: SummaryReportData,
        groupStatistics: Map<String, ru.bpo.norn.commonMain.models.GroupStatistics>
    ): Boolean {
        return try {
            val originalName = templateFile.nameWithoutExtension
            val extension = templateFile.extension
            val outputFile = File(
                templateFile.parent,
                "${originalName}_отчет_по_практике.$extension"
            )

            val success = createSummaryReportDocument(templateFile, reportData, groupStatistics, outputFile)

            if (success) {
                println("✅ Отчет создан: ${outputFile.absolutePath}")
                true
            } else {
                println("❌ Ошибка при создании отчета")
                false
            }
        } catch (e: Exception) {
            println("❌ Исключение при создании отчета: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    /**
     * Создает Word документ с отчетом по практике
     */
    private fun createSummaryReportDocument(
        templateFile: File,
        reportData: SummaryReportData,
        groupStatistics: Map<String, ru.bpo.norn.commonMain.models.GroupStatistics>,
        outputFile: File
    ): Boolean {
        return try {
            FileInputStream(templateFile).use { fis ->
                XWPFDocument(fis).use { document ->
                    // Заменяем плейсхолдеры в тексте
                    for (paragraph in document.paragraphs) {
                        replacePlaceholdersInSummaryReport(paragraph, reportData, groupStatistics)
                    }
                    
                    // Заменяем плейсхолдеры в таблицах
                    for (table in document.tables) {
                        for (row in table.rows) {
                            for (cell in row.tableCells) {
                                for (cellParagraph in cell.paragraphs) {
                                    replacePlaceholdersInSummaryReport(cellParagraph, reportData, groupStatistics)
                                }
                            }
                        }
                    }
                    
                    FileOutputStream(outputFile).use { fos ->
                        document.write(fos)
                    }
                }
            }
            true
        } catch (e: Exception) {
            println("❌ Ошибка при создании отчета: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    /**
     * Заменяет плейсхолдеры в параграфе для отчета по практике
     */
    private fun replacePlaceholdersInSummaryReport(
        paragraph: XWPFParagraph,
        reportData: SummaryReportData,
        groupStatistics: Map<String, ru.bpo.norn.commonMain.models.GroupStatistics>
    ) {
        val text = paragraph.text
        if (text.contains("{") && text.contains("}")) {
            val replacements = mutableMapOf<String, String>()
            
            // Основные данные отчета
            replacements["{departmentName}"] = reportData.departmentName
            replacements["{academicYear}"] = reportData.academicYear
            replacements["{field2_excursions}"] = reportData.field2_excursions
            replacements["{field3_teachers}"] = reportData.field3_teachers
            replacements["{field4_absentStudents}"] = reportData.field4_absentStudents
            replacements["{field5_additionalInfo}"] = reportData.field5_additionalInfo
            replacements["{field6_preliminaryEvents}"] = reportData.field6_preliminaryEvents
            replacements["{field8_shortcomings}"] = reportData.field8_shortcomings
            replacements["{field9_improvements}"] = reportData.field9_improvements
            replacements["{field10_conclusion}"] = reportData.field10_conclusion
            
            // Статистика по группам
            groupStatistics.forEach { (groupName, stats) ->
                val groupKey = groupName.replace("-", "_").replace(",", "_")
                replacements["{${groupKey}_total}"] = stats.totalStudents.toString()
                replacements["{${groupKey}_foreign_enterprises}"] = stats.foreignEnterprises.toString()
                replacements["{${groupKey}_rf_enterprises}"] = stats.rfEnterprises.toString()
                replacements["{${groupKey}_soluni}"] = stats.soluniTyulyukInzer.toString()
                replacements["{${groupKey}_department}"] = stats.departmentStudents.toString()
                replacements["{${groupKey}_subdivisions}"] = stats.universitySubdivisions.toString()
                replacements["{${groupKey}_base_departments}"] = stats.baseDepartments.toString()
                replacements["{${groupKey}_paid}"] = stats.paidPracticeStudents.toString()
                replacements["{${groupKey}_foreign_students}"] = stats.foreignStudents.toString()
                replacements["{${groupKey}_excellent}"] = stats.excellentGrades.toString()
                replacements["{${groupKey}_good}"] = stats.goodGrades.toString()
                replacements["{${groupKey}_satisfactory}"] = stats.satisfactoryGrades.toString()
                replacements["{${groupKey}_not_defended}"] = stats.notDefended.toString()
            }

            // Заменяем текст
            var remainingText = text
            while (paragraph.runs.isNotEmpty()) {
                paragraph.removeRun(0)
            }

            while (remainingText.isNotEmpty()) {
                val openBraceIndex = remainingText.indexOf("{")
                val closeBraceIndex = remainingText.indexOf("}")

                if (openBraceIndex != -1 && closeBraceIndex != -1 && openBraceIndex < closeBraceIndex) {
                    val beforePlaceholder = remainingText.substring(0, openBraceIndex)
                    val placeholder = remainingText.substring(openBraceIndex, closeBraceIndex + 1)
                    val afterPlaceholder = remainingText.substring(closeBraceIndex + 1)

                    if (beforePlaceholder.isNotEmpty()) {
                        val run = paragraph.createRun()
                        run.setText(beforePlaceholder)
                        run.setFontSize(12)
                        run.setFontFamily("Times New Roman")
                    }

                    val value = replacements[placeholder] ?: placeholder
                    val run = paragraph.createRun()
                    run.setText(value)
                    run.setFontSize(12)
                    run.setFontFamily("Times New Roman")

                    remainingText = afterPlaceholder
                } else {
                    val run = paragraph.createRun()
                    run.setText(remainingText)
                    run.setFontSize(12)
                    run.setFontFamily("Times New Roman")
                    break
                }
            }
        }
    }

    /**
     * Основная функция для генерации документов
     */
}

