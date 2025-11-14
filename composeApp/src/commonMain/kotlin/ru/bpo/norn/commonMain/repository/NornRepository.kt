package ru.bpo.norn.commonMain.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.bpo.norn.commonMain.models.Group
import ru.bpo.norn.commonMain.models.Student

/**
 * Репозиторий для управления данными приложения Norn
 * Содержит состояние групп студентов и выбранного студента
 */
class NornRepository {
    // Приватное изменяемое состояние списка групп
    private val _groups = MutableStateFlow<List<Group>>(emptyList())

    // Публичное только для чтения состояние списка групп
    val groups: StateFlow<List<Group>> = _groups

    // Приватное изменяемое состояние выбранного студента
    private val _selectedStudent = MutableStateFlow<Student?>(null)

    // Публичное только для чтения состояние выбранного студента
    val selectedStudent: StateFlow<Student?> = _selectedStudent

    /**
     * Добавляет новую группу в список, если группы с таким именем еще не существует
     * @param group Группа для добавления
     */
    fun addGroup(group: Group) {
        val currentGroups = _groups.value
        if (currentGroups.none { it.name == group.name }) {
            _groups.value = currentGroups + group
        }
    }

    /**
     * Добавляет студентов в существующую группу или создает новую группу
     * @param groupName Название группы
     * @param students Список студентов для добавления
     */
    fun addStudentsToGroup(groupName: String, students: List<Student>) {
        val currentGroups = _groups.value.toMutableList()
        val groupIndex = currentGroups.indexOfFirst { it.name == groupName }

        if (groupIndex != -1) {
            // Обновляем существующую группу
            val existingGroup = currentGroups[groupIndex]
            val updatedStudents = existingGroup.students + students
            currentGroups[groupIndex] = existingGroup.copy(students = updatedStudents)
        } else {
            // Создаем новую группу с автоматическим расчетом курса из названия
            val newGroup = Group(groupName)
            currentGroups.add(newGroup.copy(students = students))
        }

        _groups.value = currentGroups
    }

    /**
     * Полностью заменяет список студентов в указанной группе
     * @param groupName Название группы для обновления
     * @param students Новый список студентов группы
     */
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

    /**
     * Устанавливает выбранного студента для редактирования
     * @param student Студент для выбора или null для сброса выбора
     */
    fun setSelectedStudent(student: Student?) {
        _selectedStudent.value = student
    }

    /**
     * Находит группу по имени
     * @param groupName Название группы для поиска
     * @return Найденная группа или null если не найдена
     */
    fun getGroupByName(groupName: String): Group? {
        return _groups.value.find { it.name == groupName }
    }

    /**
     * Удаляет группу из списка
     * @param group Группа для удаления
     */
    fun removeGroup(group: Group) {
        val currentGroups = _groups.value.toMutableList()
        currentGroups.removeAll { it.name == group.name }
        _groups.value = currentGroups
    }

    /**
     * Обновляет данные существующей группы
     * @param updatedGroup Группа с обновленными данными
     */
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