// desktopMain/kotlin/ru/bpo/norn/NornViewModel.kt
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
import ru.bpo.norn.commonMain.repository.NornRepository
import viewmodel.Screen
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.charset.Charset

class NornViewModel {

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

    private val _enterprisesList = MutableStateFlow<List<String>>(emptyList())
    val enterprisesList: StateFlow<List<String>> = _enterprisesList.asStateFlow()

    private val _selectedEnterprise = MutableStateFlow<Pair<String, String?>?>(null)
    val selectedEnterprise: StateFlow<Pair<String, String?>?> = _selectedEnterprise.asStateFlow()

    private val _showEditDialog = MutableStateFlow(false)
    val showEditDialog: StateFlow<Boolean> = _showEditDialog.asStateFlow()

    fun selectEnterprisesFile(file: File?) {
        _enterprisesFile.value = file
    }

    /**
     * Читает список предприятий из TXT файла с правильной кодировкой
     */
    fun readEnterprisesFromTxt(file: File): List<String> {
        return try {
            file.readLines(Charset.forName("Windows-1251"))
                .map { it.trim() }
                .filter { it.isNotBlank() }
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
     * Извлекает город из названия предприятия
     */
    fun extractCityFromEnterpriseSmart(enterpriseName: String): String? {
        val cleanName = enterpriseName.trim()
        if (cleanName.contains(",")) {
            val parts = cleanName.split(",")
            val lastPart = parts.last().trim()
            val city = lastPart
                .removePrefix("г.")
                .removePrefix("г")
                .removePrefix("с.")
                .removePrefix("с")
                .removePrefix("д.")
                .removePrefix("д")
                .trim()
            if (city.isNotBlank() && city.split("\\s+".toRegex()).size <= 3) {
                return city
            }
        }
        return null
    }

    fun clearEnterprisesList() {
        _enterprisesList.value = emptyList()
    }

    fun selectEnterpriseForEditing(enterprise: String) {
        val currentCity = extractCityFromEnterpriseSmart(enterprise)
        _selectedEnterprise.value = enterprise to currentCity
        _showEditDialog.value = true
    }

    fun updateEnterpriseCity(newCity: String) {
        val current = _selectedEnterprise.value
        if (current != null) {
            val (enterprise, _) = current
            val updatedList = _enterprisesList.value.map { item ->
                if (item == enterprise) {
                    if (item.contains(",")) {
                        val parts = item.split(",")
                        val baseName = parts.dropLast(1).joinToString(",")
                        "$baseName, $newCity"
                    } else {
                        "$item, $newCity"
                    }
                } else {
                    item
                }
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

    // Data class для файла студентов
    data class StudentFile(
        val file: File,
        val students: List<Student>,
        val groupName: String,
        val loadTime: Long = System.currentTimeMillis()
    )

    // StateFlow для работы со студентами
    private val _studentFiles = MutableStateFlow<List<StudentFile>>(emptyList())
    val studentFiles: StateFlow<List<StudentFile>> = _studentFiles.asStateFlow()

    private val _selectedStudentFile = MutableStateFlow<StudentFile?>(null)
    val selectedStudentFile: StateFlow<StudentFile?> = _selectedStudentFile.asStateFlow()

    private val _currentStudentsList = MutableStateFlow<List<Student>>(emptyList())
    val currentStudentsList: StateFlow<List<Student>> = _currentStudentsList.asStateFlow()

    private val _selectedStudent = MutableStateFlow<Student?>(null)
    val selectedStudent: StateFlow<Student?> = _selectedStudent.asStateFlow()

    private val _showStudentEditDialog = MutableStateFlow(false)
    val showStudentEditDialog: StateFlow<Boolean> = _showStudentEditDialog.asStateFlow()

    // Mock студент для тестирования
    private val mockStudent: Student = mockStudent1

    /**
     * Загружает студентов из Excel файла и добавляет в список файлов
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
            val studentFile = StudentFile(
                file = file,
                students = students,
                groupName = groupName
            )

            val currentFiles = _studentFiles.value.toMutableList()
            val existingIndex = currentFiles.indexOfFirst { it.file.absolutePath == file.absolutePath }

            if (existingIndex != -1) {
                currentFiles[existingIndex] = studentFile
                println("🔄 Файл обновлен: ${file.name}")
            } else {
                currentFiles.add(studentFile)
                println("✅ Файл добавлен: ${file.name}")
            }

            _studentFiles.value = currentFiles
            selectStudentFile(studentFile)
            println("🎯 Файл выбран: ${file.name}")

        } catch (e: Exception) {
            println("💥 Ошибка загрузки: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * Выбирает файл студентов для просмотра
     */
    fun selectStudentFile(studentFile: StudentFile) {
        _selectedStudentFile.value = studentFile
        _currentStudentsList.value = studentFile.students
    }

    /**
     * Удаляет файл студентов из списка
     */
    fun removeStudentFile(studentFile: StudentFile) {
        val currentFiles = _studentFiles.value.toMutableList()
        currentFiles.removeAll { it.file.absolutePath == studentFile.file.absolutePath }
        _studentFiles.value = currentFiles

        if (_selectedStudentFile.value?.file?.absolutePath == studentFile.file.absolutePath) {
            _selectedStudentFile.value = currentFiles.firstOrNull()
            _currentStudentsList.value = currentFiles.firstOrNull()?.students ?: emptyList()
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
                                    nameOfPracticeBase = "не назначено",
                                    typeOfPractice = "производственная",
                                    periodOfPractice = "с 07.04.2025 г. по 18.05.2025 г.",
                                    formOfStudy = fundingType.ifBlank { "бюджетная" },
                                    withPayment = false,
                                    cityOfPractice = branch.ifBlank { "Уфа" },
                                    nameOfSpeciality = "Технологии искусственного интеллекта",
                                    codeOfSpeciality = "БПО09",
                                    headOfPracticeFromDepartment = "Иванов И.И.",
                                    headOfPracticeFromPracticeBase = "Петров П.П.",
                                    postOfHeadOfPracticeFromPracticeBase = "руководитель",
                                    postOfHeadOfPracticeFromDepartment = "доцент",
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
        val currentFile = _selectedStudentFile.value

        if (currentFile != null) {
            val updatedStudents = currentFile.students.map { student ->
                if (student.name == updatedStudent.name) {
                    updatedStudent
                } else {
                    student
                }
            }

            val updatedFile = currentFile.copy(students = updatedStudents)
            val updatedFiles = _studentFiles.value.map { file ->
                if (file.file.absolutePath == currentFile.file.absolutePath) {
                    updatedFile
                } else {
                    file
                }
            }

            _studentFiles.value = updatedFiles
            _currentStudentsList.value = updatedStudents

            if (_selectedStudentFile.value?.file?.absolutePath == currentFile.file.absolutePath) {
                _selectedStudentFile.value = updatedFile
            }

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

    // ==================== 5. ГЕНЕРАЦИЯ ДОКУМЕНТОВ ====================

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
}