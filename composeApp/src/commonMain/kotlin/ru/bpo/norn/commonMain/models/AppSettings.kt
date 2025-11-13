package ru.bpo.norn.commonMain.models

import kotlinx.serialization.Serializable

/**
 * Модель настроек приложения для сохранения в файл
 * Содержит все настройки, которые должны сохраняться между сессиями
 */
@Serializable
data class AppSettings(
    // Основные настройки
    val baseDirectory: String? = null,
    val isDarkTheme: Boolean = false,

    // Последние выбранные файлы
    val lastReportFile: String? = null,
    val lastDirectionFile: String? = null,
    val lastDirectionTemplateFile: String? = null,
    val lastOrderFile: String? = null,
    val lastEnterprisesFile: String? = null,
    val lastStudentsListFile: String? = null,
    val lastStatementsFile: String? = null,
    val lastDirectionsOutputFolder: String? = null,

    // Загруженные данные студентов
    val loadedGroups: List<Group> = emptyList(),
    val loadedEnterprises: List<Enterprise> = emptyList(),

    // Настройки направлений
    val dateOfDirectionIssue: String = "",
    val dateOfTaskReceived: String = "",
    val dateOfDepartmentReview: String = "",

    // Данные отчета
    val departmentName: String = "Кафедра Вычислительная техника и инженерная кибернетика",
    val academicYear: String = "2024-2025 учебный год",
    val field2_excursions: String = "Экскурсии не проводились",
    val field3_teachers: String = "Салихова М.А., Дружинская Е.В., Зайдуллина С. Г., Кондратьев Д.В., Мурзина Г.Р.",
    val field4_absentStudents: String = "Все студенты прошли практику",
    val field5_additionalInfo: String = "Дополнительных сведений нет",
    val field6_preliminaryEvents: String = "Проведены собрания со студентами до начала практики. При необходимости были проведены инструктажи по технике безопасности",
    val field8_shortcomings: String = "не выявлено",
    val field9_improvements: String = "автоматизировать работу по оформлению договоров, направлений, приказов, всех видов отчетов",
    val field10_conclusion: String = "Считаем, что все цели и задачи были выполнены",

    // Данные приказа
    val orderHeaderText: String = "Проект приказа -4п от _________",
    val orderTitleText: String = "Об учебной практике (ознакомительной практике)",
    val orderInstituteText: String = "По институту цифровых систем, автоматизации и энергетики процессов",
    val orderStreamName: String = "",
    val orderBasisText: String = "Представление и.о. зав. кафедрой «Вычислительная техника и инженерная кибернетика» Зарипова Д.М.,\n\tвиза согласования директора института цифровых систем, автоматизации и энергетики процессов Павловой З.Х.",
    val orderAgreeText: String = "СОГЛАСОВАНО",
    val orderProposerText: String = "Проект вносит:\nИ.о. зав. кафедрой ВТИК\t\t\t\t\t\t\t\t\t_________ Д.М. Зарипов",
    val orderProrectorName: String = "А.А. Иванов",
    val orderStudyDepartmentHead: String = "Б.Б. Петров",
    val orderPartnershipDepartmentHead: String = "В.В. Сидоров",
    val orderLegalDepartmentDeputy: String = "Г.Г. Козлов",
    val orderPracticeManager: String = "Д.Д. Морозов",
    val orderInstituteDirector: String = "З.Х. Павлова",
    val orderDepartmentHead: String = "Д.М. Зарипов",

    // Версия конфигурации для будущих миграций
    val configVersion: Int = 1
)