package ru.bpo.norn.commonMain.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.bpo.norn.commonMain.models.Group
import ru.bpo.norn.commonMain.models.Student

/**
 * Центральный репозиторий для управления данными приложения Norn
 *
 * Этот класс является единственным источником истины (Single Source of Truth)
 * для всех данных в приложении. Он обеспечивает:
 *
 * - Хранение и управление группами студентов
 * - Отслеживание текущего выбранного студента
 * - Реактивное обновление UI через StateFlow
 * - Валидацию и целостность данных
 * - Логирование операций для отладки
 *
 * Репозиторий использует паттерн Repository для абстракции источника данных
 * и предоставляет простой API для ViewModel'ей и других компонентов.
 *
 * Все данные хранятся в памяти и сбрасываются при перезапуске приложения.
 * В будущих версиях планируется добавить персистентное хранение.
 */
class NornRepository {

    // ========== СОСТОЯНИЕ ГРУПП СТУДЕНТОВ ==========

    /**
     * Внутреннее изменяемое состояние списка всех групп студентов
     *
     * Содержит полную коллекцию групп, каждая из которых включает:
     * - Название группы (например, "БПО09-24-01")
     * - Список студентов в группе
     * - Код и название направления подготовки
     * - Метаданные группы
     */
    private val _groups = MutableStateFlow<List<Group>>(emptyList())

    /**
     * Публичное неизменяемое состояние списка групп
     *
     * UI компоненты подписываются на это состояние для получения
     * актуальной информации о группах и автоматического обновления
     * при изменении данных.
     *
     * @return StateFlow со списком всех групп в системе
     */
    val groups: StateFlow<List<Group>> = _groups

    // ========== СОСТОЯНИЕ ВЫБРАННОГО СТУДЕНТА ==========

    /**
     * Внутреннее изменяемое состояние текущего выбранного студента
     *
     * Отслеживает студента, который выбран пользователем для просмотра
     * или редактирования. Используется для:
     * - Отображения детальной информации о студенте
     * - Открытия диалогов редактирования
     * - Выполнения операций над конкретным студентом
     */
    private val _selectedStudent = MutableStateFlow<Student?>(null)

    /**
     * Публичное неизменяемое состояние выбранного студента
     *
     * UI компоненты используют это состояние для определения,
     * какой студент в данный момент активен, и соответствующего
     * обновления интерфейса.
     *
     * @return StateFlow с выбранным студентом или null, если никто не выбран
     */
    val selectedStudent: StateFlow<Student?> = _selectedStudent

    // ========== ОПЕРАЦИИ С ГРУППАМИ ==========

    /**
     * Добавляет новую группу в репозиторий
     *
     * Проверяет уникальность названия группы перед добавлением.
     * Если группа с таким названием уже существует, операция игнорируется
     * для предотвращения дублирования данных.
     *
     * Операция атомарна - либо группа добавляется полностью, либо не добавляется вовсе.
     *
     * @param group Группа для добавления в репозиторий
     *
     * Пример использования:
     * ```kotlin
     * val newGroup = Group(
     *     name = "БПО09-24-01",
     *     students = listOf(student1, student2),
     *     codeOfDirection = "09.03.01",
     *     nameOfDirection = "Информатика и вычислительная техника"
     * )
     * repository.addGroup(newGroup)
     * ```
     */
    fun addGroup(group: Group) {
        val currentGroups = _groups.value
        // Проверяем уникальность названия группы
        if (currentGroups.none { it.name == group.name }) {
            _groups.value = currentGroups + group
            println("✅ [Repository] Добавлена новая группа: ${group.name}")
        } else {
            println("⚠️ [Repository] Группа ${group.name} уже существует, пропускаем добавление")
        }
    }

    /**
     * Добавляет студентов в существующую группу или создает новую группу
     *
     * Эта функция является ключевой для обработки данных из Excel файлов.
     * Она выполняет следующую логику:
     *
     * 1. Если группа с указанным названием существует:
     *    - Добавляет новых студентов к существующим
     *    - Сохраняет все метаданные группы
     *
     * 2. Если группа не существует:
     *    - Создает новую группу с указанными студентами
     *    - Устанавливает значения по умолчанию для метаданных
     *
     * @param groupName Название группы (например, "БПО09-24-01")
     * @param students Список студентов для добавления
     *
     * Пример использования:
     * ```kotlin
     * val studentsFromExcel = listOf(
     *     Student(name = "Иванов И.И.", group = "БПО09-24-01", ...),
     *     Student(name = "Петров П.П.", group = "БПО09-24-01", ...)
     * )
     * repository.addStudentsToGroup("БПО09-24-01", studentsFromExcel)
     * ```
     */
    fun addStudentsToGroup(groupName: String, students: List<Student>) {
        val currentGroups = _groups.value.toMutableList()
        val groupIndex = currentGroups.indexOfFirst { it.name == groupName }

        if (groupIndex != -1) {
            // Обновляем существующую группу
            val existingGroup = currentGroups[groupIndex]
            val updatedStudents = existingGroup.students + students
            currentGroups[groupIndex] = existingGroup.copy(students = updatedStudents)
            println("📝 [Repository] Добавлено ${students.size} студентов в группу ${groupName}. Всего стало: ${updatedStudents.size}")
        } else {
            // Создаем новую группу с значениями по умолчанию
            val newGroup = Group(
                name = groupName,
                students = students,
                codeOfDirection = "09.03.01", // Код направления по умолчанию
                nameOfDirection = "Информатика и вычислительная техника" // Название по умолчанию
            )
            currentGroups.add(newGroup)
            println("🆕 [Repository] Создана новая группа ${groupName} с ${students.size} студентами")
        }

        _groups.value = currentGroups
    }

    /**
     * Полностью заменяет список студентов в указанной группе
     *
     * В отличие от addStudentsToGroup, эта функция полностью заменяет
     * существующий список студентов новым. Используется для:
     * - Массового обновления данных группы
     * - Применения изменений после редактирования
     * - Синхронизации с внешними источниками данных
     *
     * Функция включает подробное логирование для отслеживания операций
     * и помощи в отладке проблем с данными.
     *
     * @param groupName Название группы для обновления
     * @param students Новый полный список студентов группы
     *
     * Пример использования:
     * ```kotlin
     * // После массового редактирования данных группы
     * val updatedStudents = originalStudents.map { student ->
     *     student.copy(cityOfPractice = "Уфа") // Обновляем город практики для всех
     * }
     * repository.updateGroupStudents("БПО09-24-01", updatedStudents)
     * ```
     */
    fun updateGroupStudents(groupName: String, students: List<Student>) {
        println("🔄 [Repository] Обновление студентов в группе: $groupName")
        val currentGroups = _groups.value.toMutableList()
        val groupIndex = currentGroups.indexOfFirst { it.name == groupName }

        if (groupIndex != -1) {
            val existingGroup = currentGroups[groupIndex]
            println("📋 [Repository] Найдена группа: ${existingGroup.name}, было студентов: ${existingGroup.students.size}")

            // Заменяем список студентов новым
            currentGroups[groupIndex] = existingGroup.copy(students = students)
            _groups.value = currentGroups

            println("✅ [Repository] Группа обновлена, стало студентов: ${students.size}")
        } else {
            println("❌ [Repository] Группа не найдена: $groupName")
        }
    }

    /**
     * Удаляет группу из репозитория
     *
     * Полностью удаляет группу и всех её студентов из системы.
     * Операция необратима - восстановить данные после удаления невозможно.
     *
     * Удаление происходит по названию группы, что обеспечивает
     * корректное удаление даже при наличии измененных ссылок.
     *
     * @param group Группа для удаления
     *
     * Пример использования:
     * ```kotlin
     * // Удаление группы после подтверждения пользователя
     * repository.removeGroup(selectedGroup)
     * ```
     */
    fun removeGroup(group: Group) {
        val currentGroups = _groups.value.toMutableList()
        val initialSize = currentGroups.size
        currentGroups.removeAll { it.name == group.name }

        if (currentGroups.size < initialSize) {
            _groups.value = currentGroups
            println("🗑️ [Repository] Удалена группа: ${group.name} (${group.students.size} студентов)")
        } else {
            println("⚠️ [Repository] Группа для удаления не найдена: ${group.name}")
        }
    }

    /**
     * Обновляет метаданные группы (название, направление подготовки)
     *
     * Позволяет изменить основные параметры группы, сохраняя при этом
     * всех студентов и их данные. Используется для:
     * - Исправления ошибок в названии группы
     * - Обновления информации о направлении подготовки
     * - Изменения кода направления
     *
     * @param updatedGroup Группа с обновленными данными
     *
     * Пример использования:
     * ```kotlin
     * val updatedGroup = originalGroup.copy(
     *     nameOfDirection = "Новое название направления",
     *     codeOfDirection = "09.03.02"
     * )
     * repository.updateGroup(updatedGroup)
     * ```
     */
    fun updateGroup(updatedGroup: Group) {
        val currentGroups = _groups.value.toMutableList()
        val groupIndex = currentGroups.indexOfFirst { it.name == updatedGroup.name }

        if (groupIndex != -1) {
            currentGroups[groupIndex] = updatedGroup
            _groups.value = currentGroups
            println("✅ [Repository] Группа обновлена: ${updatedGroup.name}")
        } else {
            println("❌ [Repository] Группа для обновления не найдена: ${updatedGroup.name}")
        }
    }

    // ========== ОПЕРАЦИИ С ВЫБРАННЫМ СТУДЕНТОМ ==========

    /**
     * Устанавливает выбранного студента
     *
     * Эта функция используется UI для отслеживания того, какой студент
     * в данный момент выбран пользователем. Выбранный студент может быть:
     * - Отображен в детальном виде
     * - Отредактирован в диалоге
     * - Использован для генерации документов
     *
     * @param student Студент для выбора или null для сброса выбора
     *
     * Пример использования:
     * ```kotlin
     * // При клике на студента в списке
     * repository.setSelectedStudent(clickedStudent)
     *
     * // При закрытии диалога редактирования
     * repository.setSelectedStudent(null)
     * ```
     */
    fun setSelectedStudent(student: Student?) {
        _selectedStudent.value = student
        if (student != null) {
            println("👤 [Repository] Выбран студент: ${student.name}")
        } else {
            println("👤 [Repository] Снят выбор студента")
        }
    }

    // ========== ВСПОМОГАТЕЛЬНЫЕ ФУНКЦИИ ==========

    /**
     * Ищет группу по названию
     *
     * Выполняет поиск группы в текущем состоянии репозитория.
     * Используется для проверки существования группы и получения
     * её данных без необходимости перебора всего списка.
     *
     * @param groupName Точное название группы для поиска
     * @return Найденная группа или null, если группа не существует
     *
     * Пример использования:
     * ```kotlin
     * val group = repository.getGroupByName("БПО09-24-01")
     * if (group != null) {
     *     println("Группа найдена, студентов: ${group.students.size}")
     * } else {
     *     println("Группа не найдена")
     * }
     * ```
     */
    fun getGroupByName(groupName: String): Group? {
        return _groups.value.find { it.name == groupName }
    }
}