// desktopMain/kotlin/ru/bpo/norn/NornViewModel.kt
package ru.bpo.norn.jwmMain.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.bpo.norn.commonMain.models.Group
import ru.bpo.norn.commonMain.models.Student
import ru.bpo.norn.commonMain.repository.NornRepository
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.apache.poi.xwpf.usermodel.XWPFParagraph
import org.apache.poi.xwpf.usermodel.XWPFRun
import ru.bpo.norn.commonMain.data.coursework.mock.mockData.mockStudent1

class NornViewModel {
    private val repository = NornRepository()

    private val _reportFile = MutableStateFlow<File?>(null)
    val reportFile: StateFlow<File?> = _reportFile.asStateFlow()

    private val _directionFile = MutableStateFlow<File?>(null)
    val directionFile: StateFlow<File?> = _directionFile.asStateFlow()

    private val _orderFile = MutableStateFlow<File?>(null)
    val orderFile: StateFlow<File?> = _orderFile.asStateFlow()

    private val _statementsFile = MutableStateFlow<File?>(null)
    val statementsFile: StateFlow<File?> = _statementsFile.asStateFlow()

    val groups: StateFlow<List<Group>> = repository.groups
    val selectedStudent: StateFlow<Student?> = repository.selectedStudent

    // Mock студент прямо в коде
    private val mockStudent:Student = mockStudent1

    fun selectReportFile(file: File) {
        _reportFile.value = file
        processReportFile(file)
    }

    fun selectDirectionFile(file: File) {
        _directionFile.value = file
    }

    fun selectOrderFile(file: File) {
        _orderFile.value = file
    }

    fun selectStatementsFile(file: File) {
        _statementsFile.value = file
    }

    fun addGroup(group: Group) {
        repository.addGroup(group)
    }

    fun selectStudent(student: Student?) {
        repository.setSelectedStudent(student)
    }

    // Основная функция для генерации документов
    fun generatePracticeDocument(templateFile: File): Boolean {
        return try {
            // Создаем копию файла в той же директории
            val originalName = templateFile.nameWithoutExtension
            val extension = templateFile.extension
            val outputFile = File(
                templateFile.parent,
                "${originalName}_заполненный_.$extension"
            )

            // Заменяем поля в Word документе
            val success = replacePlaceholdersInWord(templateFile, mockStudent, outputFile)

            if (success) {
                println("✅ Документ создан: ${outputFile.absolutePath}")
                true
            } else {
                println("❌ Ошибка при создании документа, закройте используемые word' файлы!")
                false
            }
        } catch (e: Exception) {
            println("❌ Исключение при создании документа: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    // Функция для замены плейсхолдеров в Word документе
    private fun replacePlaceholdersInWord(templateFile: File, student: Student, outputFile: File): Boolean {
        return try {
            FileInputStream(templateFile).use { fis ->
                XWPFDocument(fis).use { document ->

                    // Заменяем поля в параграфах
                    for (paragraph in document.paragraphs) {
                        replaceInParagraph(paragraph, student)
                    }

                    // Заменяем поля в таблицах
                    for (table in document.tables) {
                        for (row in table.rows) {
                            for (cell in row.tableCells) {
                                for (cellParagraph in cell.paragraphs) {
                                    replaceInParagraph(cellParagraph, student)
                                }
                            }
                        }
                    }

                    // Сохраняем измененный документ
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

            // Создаем карту: плейсхолдер -> был ли он подчеркнут
            val placeholderUnderlineMap = mutableMapOf<String, Boolean>()

            // Проверяем каждый плейсхолдер в оригинальном тексте
            replacements.keys.forEach { placeholder ->
                if (text.contains(placeholder)) {
                    val startIndex = text.indexOf(placeholder)
                    val endIndex = startIndex + placeholder.length
                    placeholderUnderlineMap[placeholder] = isTextUnderlined(paragraph, startIndex, endIndex)
                }
            }

            // Удаляем все существующие runs БЕЗ итерации
            while (paragraph.runs.isNotEmpty()) {
                paragraph.removeRun(0)
            }

            var remainingText = text

            // Обрабатываем текст с плейсхолдерами
            while (remainingText.isNotEmpty()) {
                val openBraceIndex = remainingText.indexOf("{")
                val closeBraceIndex = remainingText.indexOf("}")

                if (openBraceIndex != -1 && closeBraceIndex != -1 && openBraceIndex < closeBraceIndex) {
                    // Текст до плейсхолдера
                    val beforePlaceholder = remainingText.substring(0, openBraceIndex)
                    val placeholder = remainingText.substring(openBraceIndex, closeBraceIndex + 1)
                    val afterPlaceholder = remainingText.substring(closeBraceIndex + 1)

                    // Добавляем текст до плейсхолдера
                    if (beforePlaceholder.isNotEmpty()) {
                        val run = paragraph.createRun()
                        run.setText(beforePlaceholder)
                        run.setFontSize(12)
                        run.setFontFamily("Times New Roman")
                    }

                    // Добавляем замененное значение
                    val value = replacements[placeholder] ?: placeholder
                    val run = paragraph.createRun()
                    run.setText(value)
                    run.setFontSize(12)
                    run.setFontFamily("Times New Roman")

                    // Подчеркиваем только если оригинальный плейсхолдер был подчеркнут
                    if (placeholderUnderlineMap[placeholder] == true) {
                        run.setUnderline(org.apache.poi.xwpf.usermodel.UnderlinePatterns.SINGLE)
                    }

                    remainingText = afterPlaceholder
                } else {
                    // Остаток текста
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

            // Проверяем, пересекается ли этот run с нужным текстом
            if (runStart <= endIndex && runEnd >= startIndex) {
                if (run.getUnderline() != org.apache.poi.xwpf.usermodel.UnderlinePatterns.NONE) {
                    return true
                }
            }
            currentPos = runEnd
        }
        return false
    }

    private fun processReportFile(file: File) {
        println("Обрабатываем файл: ${file.name}")
        println("Путь: ${file.absolutePath}")
        println("Размер: ${file.length()} байт")
        println("Расширение: ${file.extension}")
    }


    // Функция для получения mock студента
    fun getMockStudent(): Student {
        return mockStudent
    }
}