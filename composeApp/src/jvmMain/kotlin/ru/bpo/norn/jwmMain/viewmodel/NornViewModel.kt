// desktopMain/kotlin/ru/bpo/norn/NornViewModel.kt
package ru.bpo.norn.jwmMain.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.bpo.norn.commonMain.models.Group
import ru.bpo.norn.commonMain.models.Student
import ru.bpo.norn.commonMain.repository.NornRepository
// работает только в этом модуле!!
import java.io.File

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

    private fun processReportFile(file: File) {
        println("Обрабатываем файл: ${file.name}")
        println("Путь: ${file.absolutePath}")
        println("Размер: ${file.length()} байт")
    }
}