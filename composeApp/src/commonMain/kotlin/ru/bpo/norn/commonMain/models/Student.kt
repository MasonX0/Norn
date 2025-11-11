package ru.bpo.norn.commonMain.models


data class Student(
    /** Полное ФИО студента */
    val name: String,

    /** Курс обучения (1-6) */
    val course: Int,

    /** Код направления подготовки (например, "09.03.01 Информатика и вычислительная техника") */
    var codeOfDirection: String,

    /** Название направления подготовки (Технологии искусственного интеллекта в нефтегазовой отрасли) */
    var nameOfDirection: String,

    /** Учебная группа (например, "БПО-21-01") */
    val group: String,

    /** Является ли иностранным студентом */
    val isForeign: Boolean,

    /** Оценка за практику ("отлично", "хорошо", "удовлетворительно", "неудовлетворительно") */
    val gradeForPractice: String,

    /** Наименование базы практики */
    val nameOfPracticeBase: String,

    /** Тип практики (учебная, производственная, преддипломная) */
    val typeOfPractice: String,

    /** Период прохождения практики  аля с 07.04.2025 г. по 18.05.2025 г.*/
    val periodOfPractice: String,

    /** Форма обучения (бюджетная/платная/целевая) */
    val formOfStudy: String,

    /** Обучается на платной основе */
    val withPayment: Boolean,

    /** Оплачиваемая ли практика */
    val isPaidPractice: Boolean = false,

    /** Город прохождения практики  */
    val cityOfPractice: String,

    /** Наименование специальности */
    val nameOfSpeciality: String,

    /** Код специальности аля БПО09*/
    val codeOfSpeciality: String,

    /** ФИО руководителя практики от кафедры */
    val headOfPracticeFromDepartment: String,

    /** ФИО руководителя практики от базы практики */
    val headOfPracticeFromPracticeBase: String,

    /** Должность руководителя от базы практики */
    val postOfHeadOfPracticeFromPracticeBase: String,

    /** Должность руководителя от кафедры */
    val postOfHeadOfPracticeFromDepartment: String,

    /** ФИО директора института/декана */
    val directorName: String,

    )
