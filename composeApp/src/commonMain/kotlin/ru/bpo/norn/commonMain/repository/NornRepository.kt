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
        if (currentGroups.none { it.students == group.students }) {
            _groups.value = currentGroups + group
        }
    }

    fun setSelectedStudent(student: Student?) {
        _selectedStudent.value = student
    }
}

