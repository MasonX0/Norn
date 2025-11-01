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
                "${originalName}_заполненный_${mockStudent.name.replace(" ", "_")}.$extension"
            )

            // Заменяем поля в Word документе
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

            )

            // Удаляем все существующие runs
            val runsToRemove = paragraph.runs.toList()
            runsToRemove.forEach { run ->
                paragraph.removeRun(paragraph.runs.indexOf(run))
            }

            var remainingText = text
            val resultRuns = mutableListOf<XWPFRun>()

            // Обрабатываем текст пока есть плейсхолдеры
            while (remainingText.isNotEmpty()) {
                val openBraceIndex = remainingText.indexOf("{")
                val closeBraceIndex = remainingText.indexOf("}")

                if (openBraceIndex != -1 && closeBraceIndex != -1 && openBraceIndex < closeBraceIndex) {
                    // Есть плейсхолдер
                    val beforePlaceholder = remainingText.substring(0, openBraceIndex)
                    val placeholderWithBraces = remainingText.substring(openBraceIndex, closeBraceIndex + 1)
                    val afterPlaceholder = remainingText.substring(closeBraceIndex + 1)

                    // Добавляем текст до плейсхолдера
                    if (beforePlaceholder.isNotEmpty()) {
                        val normalRun = paragraph.createRun()
                        normalRun.setText(beforePlaceholder)
                        normalRun.setFontSize(12)
                        normalRun.setFontFamily("Times New Roman")
                    }

                    // Добавляем замененное значение (с подчеркиванием)
                    val value = replacements[placeholderWithBraces] ?: placeholderWithBraces
                    val underlinedRun = paragraph.createRun()
                    underlinedRun.setText(value)
                    underlinedRun.setUnderline(org.apache.poi.xwpf.usermodel.UnderlinePatterns.SINGLE)
                    underlinedRun.setFontSize(12)
                    underlinedRun.setFontFamily("Times New Roman")

                    remainingText = afterPlaceholder
                } else {
                    // Больше плейсхолдеров нет - добавляем оставшийся текст
                    val finalRun = paragraph.createRun()
                    finalRun.setText(remainingText)
                    finalRun.setFontSize(12)
                    finalRun.setFontFamily("Times New Roman")
                    break
                }
            }
        }
    }

    private fun processReportFile(file: File) {
        println("Обрабатываем файл: ${file.name}")
        println("Путь: ${file.absolutePath}")
        println("Размер: ${file.length()} байт")
        println("Расширение: ${file.extension}")
    }

    // Функция для получения информации о mock студенте
    fun getMockStudentInfo(): String {
        return """
            ФИО: ${mockStudent.name}
            Группа: ${mockStudent.group}
            Курс: ${mockStudent.course}
            База практики: ${mockStudent.nameOfPracticeBase}
            Город: ${mockStudent.cityOfPractice}
            Период: ${mockStudent.periodOfPractice}
            Руководитель от кафедры: ${mockStudent.headOfPracticeFromDepartment}
        """.trimIndent()
    }

    // Функция для получения mock студента
    fun getMockStudent(): Student {
        return mockStudent
    }
}