package ru.bpo.norn.commonMain.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.bpo.norn.commonMain.models.Group
import ru.bpo.norn.commonMain.models.Student

class NornRepository {
    private val _groups = MutableStateFlow<List<Group>>(emptyList())
    val groups: StateFlow<List<Group>> = _groups

    private val _selectedStudent = MutableStateFlow<Student?>(null)
    val selectedStudent: StateFlow<Student?> = _selectedStudent

    fun addGroup(group: Group) {
        val currentGroups = _groups.value
        if (currentGroups.none { it.name == group.name }) {
            _groups.value = currentGroups + group
        }
    }

    fun addStudentsToGroup(groupName: String, students: List<Student>) {
        val currentGroups = _groups.value.toMutableList()
        val groupIndex = currentGroups.indexOfFirst { it.name == groupName }

        if (groupIndex != -1) {
            // Обновляем существующую группу
            val existingGroup = currentGroups[groupIndex]
            val updatedStudents = existingGroup.students + students
            currentGroups[groupIndex] = existingGroup.copy(students = updatedStudents)
        } else {
            // Создаем новую группу
            val newGroup = Group(
                name = groupName,
                students = students,
                codeOfDirection = "09.03.01",
                nameOfDirection = "Информатика и вычислительная техника"
            )
            currentGroups.add(newGroup)
        }

        _groups.value = currentGroups
    }

    fun updateGroupStudents(groupName: String, students: List<Student>) {
        println("🔄 [Repository] Обновление студентов в группе: $groupName")
        val currentGroups = _groups.value.toMutableList()
        val groupIndex = currentGroups.indexOfFirst { it.name == groupName }

        if (groupIndex != -1) {
            val existingGroup = currentGroups[groupIndex]
            println("📋 [Repository] Найдена группа: ${existingGroup.name}, было студентов: ${existingGroup.students.size}")
            currentGroups[groupIndex] = existingGroup.copy(students = students)
            _groups.value = currentGroups
            println("✅ [Repository] Группа обновлена, стало студентов: ${students.size}")
        } else {
            println("❌ [Repository] Группа не найдена: $groupName")
        }
    }

    fun setSelectedStudent(student: Student?) {
        _selectedStudent.value = student
    }

    fun getGroupByName(groupName: String): Group? {
        return _groups.value.find { it.name == groupName }
    }
    fun removeGroup(group: Group) {
        val currentGroups = _groups.value.toMutableList()
        currentGroups.removeAll { it.name == group.name }
        _groups.value = currentGroups
    }
    fun updateGroup(updatedGroup: Group) {
        val currentGroups = _groups.value.toMutableList()
        val groupIndex = currentGroups.indexOfFirst { it.name == updatedGroup.name }

        if (groupIndex != -1) {
            currentGroups[groupIndex] = updatedGroup
            _groups.value = currentGroups
            println("✅ Группа обновлена: ${updatedGroup.name}")
        }
    }
}