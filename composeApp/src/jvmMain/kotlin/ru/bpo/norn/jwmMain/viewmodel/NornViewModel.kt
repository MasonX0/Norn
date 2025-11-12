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
import ru.bpo.norn.commonMain.models.OrderData
import ru.bpo.norn.commonMain.repository.NornRepository
import viewmodel.Screen
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.charset.Charset

class NornViewModel {

    /**
     * Настраивает кодировку консоли для корректного отображения русских символов
     */
    init {
        try {
            System.setProperty("file.encoding", "UTF-8")
            System.setProperty("console.encoding", "UTF-8")
            // Для Windows - устанавливаем кодовую страницу
            if (System.getProperty("os.name").lowercase().contains("windows")) {
                Runtime.getRuntime().exec("chcp 65001")
            }
        } catch (e: Exception) {
            println("⚠️ Не удалось настроить кодировку консоли: ${e.message}")
        }
    }

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

    fun updateGroupStudents(groupName: String, students: List<Student>) {
        println("🔄 [ViewModel] Обновление студентов в группе: $groupName")
        repository.updateGroupStudents(groupName, students)
        // Обновляем выбранную группу если она та же самая
        if (_selectedGroup.value?.name == groupName) {
            _selectedGroup.value = repository.getGroupByName(groupName)
        }
        println("✅ [ViewModel] Студенты обновлены, всего: ${students.size}")
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
                                    practiceForm = "стационарная",
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
        println("📝 Выбран студент для редактирования: ${student.name}")
        _selectedStudent.value = student
        _showStudentEditDialog.value = true
    }

    fun updateStudentData(updatedStudent: Student) {
        println("🔄 Обновление данных студента: ${updatedStudent.name}")
        val currentGroup = _selectedGroup.value
        val originalStudent = _selectedStudent.value

        if (currentGroup != null && originalStudent != null) {
            println("📋 Текущая группа: ${currentGroup.name}")
            println("👤 Исходный студент: ${originalStudent.name}")

            val updatedStudents = currentGroup.students.map { student ->
                if (student == originalStudent) { // Сравниваем объекты, а не имена
                    println("✅ Найден студент для обновления: ${originalStudent.name} -> ${updatedStudent.name}")
                    updatedStudent
                } else {
                    student
                }
            }

            println("🔄 Обновление группы с ${updatedStudents.size} студентами")
            repository.updateGroupStudents(currentGroup.name, updatedStudents)

            // Обновляем выбранную группу после изменения состава студентов
            _selectedGroup.value = repository.getGroupByName(currentGroup.name)

            // Если редактировался тот же студент (по имени и/или номеру зачетки), обновим и его в selectedStudent
            if (_selectedStudent.value != null && _selectedStudent.value?.name == originalStudent.name) {
                _selectedStudent.value = updatedStudent
            }

            // После успешного обновления закрыть диалог редактирования
            _showStudentEditDialog.value = false

            println("✅ Данные студента обновлены: ${updatedStudent.name}")
        } else {
            println("❌ Отсутствуют данные: группа=${currentGroup?.name}, студент=${originalStudent?.name}")
        }
    }

    fun closeStudentEditDialog() {
        println("🔒 Закрытие диалога редактирования студента")
        println("🔒 Состояние до: showDialog=${_showStudentEditDialog.value}, selectedStudent=${_selectedStudent.value?.name}")
        _showStudentEditDialog.value = false
        _selectedStudent.value = null
        println("🔒 Состояние после: showDialog=${_showStudentEditDialog.value}, selectedStudent=${_selectedStudent.value?.name}")
    }

    // ==================== 5. ДАННЫЕ ОТЧЕТА ПО ПРАКТИКЕ ====================

    private val _summaryReportData = MutableStateFlow(SummaryReportData())
    val summaryReportData: StateFlow<SummaryReportData> = _summaryReportData.asStateFlow()

    // Статус генерации документов
    private val _documentGenerationStatus = MutableStateFlow("")
    val documentGenerationStatus: StateFlow<String> = _documentGenerationStatus.asStateFlow()

    // ==================== OrderData StateFlow ====================
    private val _orderData = MutableStateFlow(OrderData())
    val orderData: StateFlow<OrderData> = _orderData.asStateFlow()

    fun updateOrderData(data: OrderData) {
        _orderData.value = data
    }

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
    fun generateOrderDocument(orderData: OrderData = _orderData.value): Boolean {
        return try {
            _documentGenerationStatus.value = "🔄 Создание приказа..."

            val outputFile =
                File(System.getProperty("user.home"), "Desktop/Приказ_по_практике.docx")

            // Получаем всех студентов из всех групп
            val allStudents = repository.groups.value.flatMap { it.students }

            if (allStudents.isEmpty()) {
                _documentGenerationStatus.value = "❌ Нет загруженных студентов для создания приказа"
                println("❌ Нет загруженных студентов для создания приказа")
                return false
            }

            val success = createOrderDocument(allStudents, orderData, outputFile)

            if (success) {
                _documentGenerationStatus.value = "✅ Приказ создан: ${outputFile.absolutePath}"
                println("✅ Приказ создан: ${outputFile.absolutePath}")
                true
            } else {
                _documentGenerationStatus.value = "❌ Ошибка при создании приказа"
                println("❌ Ошибка при создании приказа")
                false
            }
        } catch (e: Exception) {
            _documentGenerationStatus.value = "❌ Ошибка: ${e.message}"
            println("❌ Исключение при создании приказа: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    /**
     * Создает Word документ с приказом, разделенным по формам обучения
     */
    private fun createOrderDocument(
        students: List<Student>,
        orderData: OrderData,
        outputFile: File
    ): Boolean {
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
                addOrderHeader(document, orderData)

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
                addOrderFooter(document, orderData)

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
    private fun addOrderHeader(document: XWPFDocument, orderData: OrderData) {
        // Заголовок справа
        val headerParagraph = document.createParagraph()
        headerParagraph.alignment = org.apache.poi.xwpf.usermodel.ParagraphAlignment.RIGHT
        val headerRun = headerParagraph.createRun()
        headerRun.setText(orderData.headerText.ifBlank { "Проект приказа -4п от _________" })
        headerRun.setFontSize(12)
        headerRun.setFontFamily("Times New Roman")
        headerRun.isBold = false

        val titleParagraph = document.createParagraph()
        titleParagraph.alignment = org.apache.poi.xwpf.usermodel.ParagraphAlignment.CENTER
        val titleRun = titleParagraph.createRun()
        titleRun.setText(orderData.titleText.ifBlank { "Об учебной практике (ознакомительной практике)" })
        titleRun.setFontSize(12)
        titleRun.setFontFamily("Times New Roman")
        titleRun.isBold = false

        // Пустая строка
        document.createParagraph()

        val instituteParagraph = document.createParagraph()
        instituteParagraph.alignment = org.apache.poi.xwpf.usermodel.ParagraphAlignment.CENTER
        val instituteRun = instituteParagraph.createRun()
        instituteRun.setText(orderData.instituteText.ifBlank { "По институту цифровых систем, автоматизации и энергетики процессов" })
        instituteRun.setFontSize(12)
        instituteRun.setFontFamily("Times New Roman")
        instituteRun.isBold = false

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
        // Заголовок секции с красной строкой
        val sectionParagraph = document.createParagraph()
        sectionParagraph.indentationFirstLine = 250 // 1.25 см красная строка
        sectionParagraph.indentationLeft = 0 // левая граница на нуле

        val sectionRun = sectionParagraph.createRun()
        sectionRun.setText("$sectionNumber Нижеперечисленных студентов потока БПО09-24 и БПО09и-24 направления 09.03.01 Информатика и вычислительная техника, профиля «Технологии искусственного интеллекта в нефтегазовой отрасли», обучающихся на ")
        sectionRun.setFontSize(12)
        sectionRun.setFontFamily("Times New Roman")
        sectionRun.isBold = false

        // Добавляем тип обучения жирным и подчеркнутым
        val fundingRun = sectionParagraph.createRun()
        fundingRun.setText(fundingType)
        fundingRun.setFontSize(12)
        fundingRun.setFontFamily("Times New Roman")
        fundingRun.isBold = true
        fundingRun.setUnderline(org.apache.poi.xwpf.usermodel.UnderlinePatterns.SINGLE)

        // Завершение предложения
        val endRun = sectionParagraph.createRun()
        endRun.setText(", направить для прохождения практики на следующие базы практик:")
        endRun.setFontSize(12)
        endRun.setFontFamily("Times New Roman")
        endRun.isBold = false

        // Создаем таблицу
        val table = document.createTable()
        table.width = 10000

        // Настраиваем свойства таблицы приказа
        val ctTbl = table.ctTbl
        val tblPr = ctTbl.tblPr ?: ctTbl.addNewTblPr()
        val tblW = tblPr.tblW ?: tblPr.addNewTblW()
        tblW.type = org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblWidth.DXA
        tblW.w = java.math.BigInteger.valueOf(10000)
        val tblLayout = tblPr.tblLayout ?: tblPr.addNewTblLayout()
        tblLayout.type =
            org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblLayoutType.FIXED

        // Заголовок таблицы
        val headerRow = table.getRow(0)

        // Ширины колонок для таблицы приказа
        val columnWidths = intArrayOf(
            500,   // №пп
            2500,  // Ф. И. О. практиканта
            2000,  // Наименование база практики
            1200,  // Вид и тип практики
            1200,  // Сроки практики
            800,   // Форма практики
            800,   // с оплатой/без оплаты
            1000   // Руководитель по практике на кафедре  
        )

        val headers = listOf(
            "№пп",
            "Ф. И. О. практиканта\n(в именительном падеже)",
            "Наименование база практики, населенный пункт",
            "Вид и тип практики",
            "Сроки практики",
            "Форма практики",
            "с оплатой/ без оплаты",
            "Руководитель по практике на кафедре"
        )

        // Добавляем недостающие ячейки
        while (headerRow.tableCells.size < headers.size) {
            headerRow.addNewTableCell()
        }

        headers.forEachIndexed { index, header ->
            val cell = headerRow.getCell(index)

            // Устанавливаем ширину ячейки заголовка
            val ctTc = cell.ctTc
            val tcPr = ctTc.tcPr ?: ctTc.addNewTcPr()
            val tcW = tcPr.tcW ?: tcPr.addNewTcW()
            tcW.type = org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblWidth.DXA
            tcW.w = java.math.BigInteger.valueOf(columnWidths[index].toLong())

            // Заполняем содержимое
            cell.removeParagraph(0)
            val p = cell.addParagraph()
            p.alignment = org.apache.poi.xwpf.usermodel.ParagraphAlignment.CENTER
            val run = p.createRun()
            run.setText(header)
            run.setFontSize(12)
            run.setFontFamily("Times New Roman")
            run.isBold = false
        }

        // Добавляем строки со студентами
        var currentIndex = startIndex
        students.forEach { student ->
            val row = table.createRow()

            val values = listOf(
                currentIndex.toString(),
                student.name,
                "${student.nameOfPracticeBase}, ${student.cityOfPractice}",
                student.typeOfPractice,
                student.periodOfPractice,
                student.practiceForm,
                if (student.isPaidPractice) "с оплатой" else "без оплаты",
                "${student.postOfHeadOfPracticeFromDepartment}\n${student.headOfPracticeFromDepartment}"
            )

            // Заполняем ячейки и устанавливаем ширины
            values.forEachIndexed { index, value ->
                val cell = row.getCell(index)

                val ctTc = cell.ctTc
                val tcPr = ctTc.tcPr ?: ctTc.addNewTcPr()
                val tcW = tcPr.tcW ?: tcPr.addNewTcW()
                tcW.type = org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblWidth.DXA
                tcW.w = java.math.BigInteger.valueOf(columnWidths[index].toLong())

                cell.removeParagraph(0)
                val p = cell.addParagraph()
                val run = p.createRun()
                run.setText(value)
                run.setFontSize(12)
                run.setFontFamily("Times New Roman")
            }

            currentIndex++
        }

        // Пустая строка после таблицы
        document.createParagraph()

        // Пустая строка после таблицы с красной строкой
        val emptyParagraph = document.createParagraph()
        emptyParagraph.indentationFirstLine = 250 // красная строка
        emptyParagraph.indentationLeft = 0

        return currentIndex
    }

    /**
     * Добавляет подписи в конец приказа
     */
    private fun addOrderFooter(document: XWPFDocument, orderData: OrderData) {
        // Основание с подчеркиванием
        val basisParagraph = document.createParagraph()
        basisParagraph.indentationLeft = 0

        // Слово "Основание:" подчеркнутое
        val basisLabelRun = basisParagraph.createRun()
        basisLabelRun.setText("Основание:")
        basisLabelRun.setFontSize(12)
        basisLabelRun.setFontFamily("Times New Roman")
        basisLabelRun.isBold = false
        basisLabelRun.setUnderline(org.apache.poi.xwpf.usermodel.UnderlinePatterns.SINGLE)

        // Остальной текст основания
        val basisTextRun = basisParagraph.createRun()
        val basisText = orderData.basisText.ifBlank {
            "Основание: Представление и.о. зав. кафедрой «Вычислительная техника и инженерная кибернетика» Зарипова Д.М.,\n\tвиза согласования директора института цифровых систем, автоматизации и энергетики процессов Павловой З.Х."
        }
        // Убираем "Основание:" из текста если оно есть
        val cleanBasisText = basisText.removePrefix("Основание:").trim()
        basisTextRun.setText(" $cleanBasisText")
        basisTextRun.setFontSize(12)
        basisTextRun.setFontFamily("Times New Roman")
        basisTextRun.isBold = false

        // Пустые строки
        document.createParagraph()
        document.createParagraph()

        // Подписи
        val signaturesList = if (orderData.signatures.isNotEmpty()) {
            orderData.signatures
        } else {
            listOf(
                "Проректор по учебной работе" to "_________  ${orderData.prorectorName}",
                "Начальник учебного отдела" to "_________  ${orderData.studyDepartmentHead}",
                "Начальник отдела взаимодействия с организациями-партнёрами" to "_________  ${orderData.partnershipDepartmentHead}",
                "Зам. начальника юридического отдела" to "_________ ${orderData.legalDepartmentDeputy}",
                "Руководитель учебно-производственной практики" to "_________ ${orderData.practiceManager}",
                "Директор IT-института" to "_________ ${orderData.instituteDirector}"
            )
        }

        signaturesList.forEach { (position, signature) ->
            val signatureParagraph = document.createParagraph()
            signatureParagraph.indentationLeft = 0
            val signatureRun = signatureParagraph.createRun()
            signatureRun.setText("$position\t\t\t\t\t\t\t\t\t$signature")
            signatureRun.setFontSize(12)
            signatureRun.setFontFamily("Times New Roman")
            signatureRun.isBold = false
        }

        // Согласовано
        val agreeParagraph = document.createParagraph()
        agreeParagraph.alignment = org.apache.poi.xwpf.usermodel.ParagraphAlignment.LEFT
        agreeParagraph.indentationLeft = 0
        val agreeRun = agreeParagraph.createRun()
        agreeRun.setText(orderData.agreeText.ifBlank { "СОГЛАСОВАНО" })
        agreeRun.setFontSize(12)
        agreeRun.setFontFamily("Times New Roman")
        agreeRun.isBold = false

        // Проект вносит
        val proposerParagraph = document.createParagraph()
        proposerParagraph.indentationLeft = 0
        val proposerRun = proposerParagraph.createRun()
        proposerRun.setText(orderData.proposerText.ifBlank { "Проект вносит:\nИ.о. зав. кафедрой ВТИК\t\t\t\t\t\t\t\t\t_________ ${orderData.departmentHead}" })
        proposerRun.setFontSize(12)
        proposerRun.setFontFamily("Times New Roman")
        proposerRun.isBold = false
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
     * Генерирует отчет по практике без шаблона (создает новый документ)
     */
    fun generateSummaryReportWithoutTemplate(
        reportData: SummaryReportData,
        groupStatistics: Map<String, ru.bpo.norn.commonMain.models.GroupStatistics>
    ): Boolean {
        return try {
            _documentGenerationStatus.value = "🔄 Создание документа..."

            val outputFile = File(
                System.getProperty("user.home"),
                "Desktop/Сводный_отчет_по_практике.docx"
            )

            val success = createSummaryReportFromScratch(reportData, groupStatistics, outputFile)

            if (success) {
                _documentGenerationStatus.value = "✅ Отчет создан: ${outputFile.absolutePath}"
                println("✅ Сводный отчет создан: ${outputFile.absolutePath}")
                true
            } else {
                _documentGenerationStatus.value = "❌ Ошибка при создании отчета"
                println("❌ Ошибка при создании сводного отчета")
                false
            }
        } catch (e: Exception) {
            _documentGenerationStatus.value = "❌ Ошибка: ${e.message}"
            println("❌ Исключение при создании сводного отчета: ${e.message}")
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
     * Создает Word документ с отчетом по практике без шаблона
     */
    private fun createSummaryReportFromScratch(
        reportData: SummaryReportData,
        groupStatistics: Map<String, ru.bpo.norn.commonMain.models.GroupStatistics>,
        outputFile: File
    ): Boolean {
        return try {
            XWPFDocument().use { document ->
                // Заголовок отчета - одна строка по центру
                val titleParagraph = document.createParagraph()
                titleParagraph.alignment = org.apache.poi.xwpf.usermodel.ParagraphAlignment.CENTER
                val titleRun = titleParagraph.createRun()
                titleRun.setText("${reportData.departmentName.ifBlank { "Кафедра Вычислительная техника и инженерная кибернетика" }} ${reportData.academicYear.ifBlank { "2024-2025 учебный год" }}")
                titleRun.setFontSize(14)
                titleRun.setBold(false)
                titleRun.setFontFamily("Times New Roman")

                // Пустая строка
                document.createParagraph()

                // 1. Таблица со статистикой
                val numberParagraph = document.createParagraph()
                numberParagraph.alignment = org.apache.poi.xwpf.usermodel.ParagraphAlignment.LEFT
                val numberRun = numberParagraph.createRun()
                numberRun.setText("1.")
                numberRun.setFontSize(14)
                numberRun.setBold(false)
                numberRun.setFontFamily("Times New Roman")

                val table = document.createTable()

                // Устанавливаем ширину таблицы
                table.width = 10000 // Ширина в twentieths of a point (1440 = 1 inch)

                // Получаем CTTbl для настройки свойств таблицы
                val ctTbl = table.ctTbl
                val tblPr = ctTbl.tblPr ?: ctTbl.addNewTblPr()

                // Устанавливаем тип ширины таблицы
                val tblW = tblPr.tblW ?: tblPr.addNewTblW()
                tblW.type = org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblWidth.DXA
                tblW.w = java.math.BigInteger.valueOf(10000)

                // Устанавливаем поведение таблицы - фиксированные размеры колонок
                val tblLayout = tblPr.tblLayout ?: tblPr.addNewTblLayout()
                tblLayout.type =
                    org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblLayoutType.FIXED

                // Заголовок таблицы
                val headerRow = table.getRow(0)

                // Определяем ширины колонок (в twentieths of a point)
                val columnWidths = intArrayOf(
                    1200,  // Группа
                    1500,  // сроки практики
                    2000,  // вид практики
                    800,   // всего чел
                    800,   // предприятия заруб
                    800,   // предприятия РФ
                    1000,  // Солуни, Тюлюк Инзер
                    800,   // кафедра
                    1200,  // Структурные Подразделения вуза
                    800,   // Базовые кафедры
                    1200   // кол-во студ. на оплачиваемых местах
                )

                val headers = listOf(
                    "Группа",
                    "сроки практики",
                    "вид практики (учебная производственная, преддипломная, НИР)",
                    "всего чел",
                    "предприятия заруб",
                    "предприятия РФ",
                    "Солуни, Тюлюк Инзер",
                    "кафедра",
                    "Структурные Подразделения вуза",
                    "Базовые кафедры",
                    "кол-во студ. Прошедших практику на оплачиваемых местах"
                )

                // Добавляем недостающие ячейки и устанавливаем ширины
                while (headerRow.tableCells.size < headers.size) {
                    headerRow.addNewTableCell()
                }

                headers.forEachIndexed { index, header ->
                    val cell = headerRow.getCell(index)

                    // Устанавливаем ширину ячейки
                    val ctTc = cell.ctTc
                    val tcPr = ctTc.tcPr ?: ctTc.addNewTcPr()
                    val tcW = tcPr.tcW ?: tcPr.addNewTcW()
                    tcW.type = org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblWidth.DXA
                    tcW.w = java.math.BigInteger.valueOf(columnWidths[index].toLong())

                    // Очищаем и заполняем содержимое
                    cell.removeParagraph(0)
                    val p = cell.addParagraph()
                    p.alignment = org.apache.poi.xwpf.usermodel.ParagraphAlignment.CENTER
                    val run = p.createRun()
                    run.setText(header)
                    run.setFontSize(12)
                    run.setBold(false)
                    run.setFontFamily("Times New Roman")
                }

                // Добавляем данные групп
                groupStatistics.forEach { (groupName, stats) ->
                    val row = table.createRow()
                    val values = listOf(
                        groupName,
                        "${stats.practiceStartDate}-${stats.practiceEndDate}",
                        stats.practiceType,
                        stats.totalStudents.toString(),
                        stats.foreignEnterprises.toString(),
                        stats.rfEnterprises.toString(),
                        stats.soluniTyulyukInzer.toString(),
                        stats.departmentStudents.toString(),
                        stats.universitySubdivisions.toString(),
                        stats.baseDepartments.toString(),
                        stats.paidPracticeStudents.toString()
                    )

                    values.forEachIndexed { index, value ->
                        val cell = row.getCell(index)

                        // Устанавливаем ширину ячейки для данных
                        val ctTc = cell.ctTc
                        val tcPr = ctTc.tcPr ?: ctTc.addNewTcPr()
                        val tcW = tcPr.tcW ?: tcPr.addNewTcW()
                        tcW.type =
                            org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblWidth.DXA
                        tcW.w = java.math.BigInteger.valueOf(columnWidths[index].toLong())

                        cell.removeParagraph(0)
                        val p = cell.addParagraph()
                        val run = p.createRun()
                        run.setText(value)
                        run.setFontSize(12)
                        run.setBold(false)
                        run.setFontFamily("Times New Roman")
                    }

                    // Добавляем строку с иностранными студентами если есть
                    if (stats.foreignStudents > 0) {
                        val foreignRow = table.createRow()
                        val foreignValues = listOf(
                            "из них иностранных студентов",
                            "", // сроки
                            "", // вид практики
                            stats.foreignStudents.toString(),
                            "0",
                            "0",
                            "0",
                            stats.foreignStudents.toString(),
                            "0",
                            "0",
                            "0"
                        )
                        foreignValues.forEachIndexed { index, value ->
                            val cell = foreignRow.getCell(index)
                            // Устанавливаем ширину ячейки для данных
                            val ctTc = cell.ctTc
                            val tcPr = ctTc.tcPr ?: ctTc.addNewTcPr()
                            val tcW = tcPr.tcW ?: tcPr.addNewTcW()
                            tcW.type =
                                org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblWidth.DXA
                            tcW.w = java.math.BigInteger.valueOf(columnWidths[index].toLong())
                            cell.removeParagraph(0)
                            val p = cell.addParagraph()
                            val run = p.createRun()
                            run.setText(value)
                            run.setFontSize(14)
                            run.setBold(false)
                            run.setFontFamily("Times New Roman")
                        }
                    }
                }

                // Пустые строки
                document.createParagraph()
                document.createParagraph()

                // Дополнительные поля отчета (2-6, 8-10)
                addReportField(
                    document,
                    "2.",
                    reportData.field2_excursions.ifBlank { "Экскурсии не проводились" })
                addReportField(
                    document,
                    "3.",
                    reportData.field3_teachers.ifBlank { "Салихова М.А., Дружинская Е.В., Зайдуллина С. Г., Кондратьев Д.В., Мурзина Г.Р." })
                addReportField(
                    document,
                    "4.",
                    reportData.field4_absentStudents.ifBlank { "Все студенты прошли практику" })
                addReportField(
                    document,
                    "5.",
                    reportData.field5_additionalInfo.ifBlank { "Дополнительных сведений нет" })
                addReportField(
                    document,
                    "6.",
                    reportData.field6_preliminaryEvents.ifBlank { "Проведены собрания со студентами до начала практики. При необходимости были проведены инструктажи по технике безопасности" })

                // 7. Таблица результатов защиты
                val resultsParagraph = document.createParagraph()
                val resultsRun = resultsParagraph.createRun()
                resultsRun.setText("7. Результаты защиты отчетов по практике:")
                resultsRun.setFontSize(14)
                resultsRun.setBold(false)
                resultsRun.setFontFamily("Times New Roman")

                // Таблица результатов
                val resultsTable = document.createTable()
                resultsTable.width = 10000

                // Настраиваем свойства таблицы результатов
                val ctTblResults = resultsTable.ctTbl
                val tblPrResults = ctTblResults.tblPr ?: ctTblResults.addNewTblPr()
                val tblWResults = tblPrResults.tblW ?: tblPrResults.addNewTblW()
                tblWResults.type =
                    org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblWidth.DXA
                tblWResults.w = java.math.BigInteger.valueOf(10000)
                val tblLayoutResults = tblPrResults.tblLayout ?: tblPrResults.addNewTblLayout()
                tblLayoutResults.type =
                    org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblLayoutType.FIXED

                // Заголовок таблицы результатов
                val resultsHeaderRow = resultsTable.getRow(0)

                // Ширины колонок для таблицы результатов 
                val resultsColumnWidths = intArrayOf(
                    2000,  // Группа
                    1500,  // Количество студентов
                    1500,  // отлично
                    1500,  // хорошо  
                    1500,  // удовлетворительно
                    2000   // Не защитили в срок
                )

                val resultsHeaders = listOf(
                    "Группа",
                    "Количество студентов",
                    "Из них с оценкой",
                    "",
                    "",
                    "Не защитили в срок"
                )

                // Добавляем недостающие ячейки
                while (resultsHeaderRow.tableCells.size < resultsHeaders.size) {
                    resultsHeaderRow.addNewTableCell()
                }

                resultsHeaders.forEachIndexed { index, header ->
                    val cell = resultsHeaderRow.getCell(index)

                    // Устанавливаем ширину ячейки
                    val ctTc = cell.ctTc
                    val tcPr = ctTc.tcPr ?: ctTc.addNewTcPr()
                    val tcW = tcPr.tcW ?: tcPr.addNewTcW()
                    tcW.type = org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblWidth.DXA
                    tcW.w = java.math.BigInteger.valueOf(resultsColumnWidths[index].toLong())

                    // Заполняем содержимое
                    cell.removeParagraph(0)
                    val p = cell.addParagraph()
                    p.alignment = org.apache.poi.xwpf.usermodel.ParagraphAlignment.CENTER
                    val run = p.createRun()
                    run.setText(header)
                    run.setFontSize(12)
                    run.setBold(false)
                    run.setFontFamily("Times New Roman")
                }

                // Подзаголовки для оценок
                val subHeaderRow = resultsTable.createRow()
                val subHeaders = listOf("", "", "отлично", "хорошо", "удовлетворительно", "")

                subHeaders.forEachIndexed { index, header ->
                    val cell = subHeaderRow.getCell(index)

                    // Устанавливаем ширину ячейки для подзаголовков
                    val ctTc = cell.ctTc
                    val tcPr = ctTc.tcPr ?: ctTc.addNewTcPr()
                    val tcW = tcPr.tcW ?: tcPr.addNewTcW()
                    tcW.type = org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblWidth.DXA
                    tcW.w = java.math.BigInteger.valueOf(resultsColumnWidths[index].toLong())

                    cell.removeParagraph(0)
                    val p = cell.addParagraph()
                    p.alignment = org.apache.poi.xwpf.usermodel.ParagraphAlignment.CENTER
                    val run = p.createRun()
                    run.setText(header)
                    run.setFontSize(14)
                    run.setBold(false)
                    run.setFontFamily("Times New Roman")
                }


                // Данные результатов по группам
                groupStatistics.forEach { (groupName, stats) ->
                    val row = resultsTable.createRow()
                    val values = listOf(
                        groupName,
                        stats.totalStudents.toString(),
                        stats.excellentGrades.toString(),
                        stats.goodGrades.toString(),
                        stats.satisfactoryGrades.toString(),
                        stats.notDefended.toString()
                    )

                    values.forEachIndexed { index, value ->
                        val cell = row.getCell(index)

                        // Устанавливаем ширину ячейки для данных результатов
                        val ctTc = cell.ctTc
                        val tcPr = ctTc.tcPr ?: ctTc.addNewTcPr()
                        val tcW = tcPr.tcW ?: tcPr.addNewTcW()
                        tcW.type =
                            org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblWidth.DXA
                        tcW.w = java.math.BigInteger.valueOf(resultsColumnWidths[index].toLong())

                        cell.removeParagraph(0)
                        val p = cell.addParagraph()
                        val run = p.createRun()
                        run.setText(value)
                        run.setFontSize(12)
                        run.setBold(false)
                        run.setFontFamily("Times New Roman")
                    }
                }

                // Завершающие поля
                document.createParagraph()
                addReportField(
                    document,
                    "8.",
                    reportData.field8_shortcomings.ifBlank { "не выявлено" })
                addReportField(
                    document,
                    "9.",
                    reportData.field9_improvements.ifBlank { "автоматизировать работу по оформлению договоров, направлений, приказов, всех видов отчетов" })
                addReportField(
                    document,
                    "10.",
                    reportData.field10_conclusion.ifBlank { "Считаем, что все цели и задачи были выполнены" })

                // Сохраняем документ
                FileOutputStream(outputFile).use { fos ->
                    document.write(fos)
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
     * Добавляет поле отчета с номером
     */
    private fun addReportField(document: XWPFDocument, number: String, text: String) {
        val paragraph = document.createParagraph()
        val run = paragraph.createRun()
        run.setText("$number $text")
        run.setFontSize(14)
        run.setBold(false)
        run.setFontFamily("Times New Roman")
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

