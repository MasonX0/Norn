package ru.bpo.norn.commonMain.models

/**
 * Модель данных для отчета по практике
 */
data class SummaryReportData(
    /** Название кафедры */
    val departmentName: String = "Кафедра Вычислительная техника и инженерная кибернетика",

    /** Учебный год */
    val academicYear: String = "2024-2025 учебный год",

    /** Поле 2: Экскурсии */
    val field2_excursions: String = "не проводились",

    /** Поле 3: Перечень преподавателей, принимавших участие в организации и проведении практики */
    val field3_teachers: String = "Салихова М.А., Дружинская Е.В., Зайдуллина С. Г., Кондратьев Д.В., Мурзина Г.Р.",

    /** Поле 4: Перечень студентов, не прошедших практику в установленные сроки */
    val field4_absentStudents: String = "Ильин К.О., Богданов Д.Р.",

    /** Поле 5: Дополнительные сведения об организации и проведении практики */
    val field5_additionalInfo: String = "Дополнительных сведений нет",

    /** Поле 6: Организационные мероприятия, проведенные перед началом практики */
    val field6_preliminaryEvents: String = "Проведены собрания со студентами до начала практики. При необходимости были проведены инструктажи по технике безопасности",

    /** Поле 8: Недостатки при организации и проведении практики */
    val field8_shortcomings: String = "не выявлено",

    /** Поле 9: Предложения по улучшению качества проведения практики */
    val field9_improvements: String = "автоматизировать работу по оформлению договоров, направлений, приказов, всех видов отчетов",

    /** Поле 10: Заключение о выполнении программы практики */
    val field10_conclusion: String = "Считаем, что все цели и задачи были выполнены"
)

/**
 * Модель данных для приказа по практике
 */
data class OrderData(
    val orderNumber: String = "",
    val orderDate: String = "",
    val headerText: String = "",
    val titleText: String = "",
    val instituteText: String = "",
    val streamName: String = "", // Название потока (автоматически вычисляется, но можно переопределить)
    val practiceType: String = "учебной практике (ознакомительной практике)",
    val institute: String = "По институту цифровых систем, автоматизации и энергетики процессов",
    val direction: String = "09.03.01 Информатика и вычислительная техника",
    val profile: String = "Технологии искусственного интеллекта в нефтегазовой отрасли",
    val practiceCredits: String = "3 з.е.",
    val practiceForm: String = "стационарная",
    val basisText: String = "Основание: Представление и.о. зав. кафедрой «Вычислительная техника и инженерная кибернетика» Зарипова Д.М.,\n\tвиза согласования директора института цифровых систем, автоматизации и энергетики процессов Павловой З.Х.",
    val agreeText: String = "",
    val proposerText: String = "",

    // Подписи как список пар (должность, подпись) 
    val signatures: List<Pair<String, String>> = emptyList(),

    // Подписи (отдельные поля для удобства редактирования)
    val prorectorName: String = "А.И. Могучев",
    val studyDepartmentHead: String = "Н.В. Заиченко",
    val partnershipDepartmentHead: String = "Р.Р. Даминов",
    val legalDepartmentDeputy: String = "Р.Ф. Хуснулина",
    val practiceManager: String = "Э.Р. Читахян",
    val instituteDirector: String = "З.Х. Павлова",
    val departmentHead: String = "Д.М. Зарипов"
)