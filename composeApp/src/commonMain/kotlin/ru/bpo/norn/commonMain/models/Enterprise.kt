package ru.bpo.norn.commonMain.models

/**
 * Модель предприятия с информацией о руководителях практики
 */
data class Enterprise(
    /** Полное название предприятия */
    val name: String,

    /** Город, где находится предприятие */
    val city: String?,

    /** Список руководителей практики от базы практики */
    val supervisors: List<PracticeSupervisor> = emptyList(),

    /** Предприятие зарубежное */
    val isForeign: Boolean = false,

    /** Солуни, Тюлюк, Инзер */
    val isSoluniTyulyukInzer: Boolean = false,

    /** Кафедра */
    val isDepartment: Boolean = false,

    /** Структурное подразделение вуза */
    val isUniversitySubdivision: Boolean = false,

    /** Базовая кафедра */
    val isBaseDepartment: Boolean = false
) {
    /**
     * Форматированное название с городом для отображения
     */
    fun getDisplayName(): String {
        return if (city != null) {
            "$name, $city"
        } else {
            name
        }
    }
}

/**
 * Руководитель практики от базы практики
 */
data class PracticeSupervisor(
    /** ФИО руководителя */
    val fullName: String,

    /** Должность руководителя */
    val position: String
) {
    /**
     * Форматированное представление руководителя
     */
    fun getDisplayText(): String {
        return "$position $fullName"
    }
}