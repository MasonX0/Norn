// commonMain/data/coursework/mock/mockData.kt
package ru.bpo.norn.commonMain.data.coursework.mock.mockData

import ru.bpo.norn.commonMain.models.Student

/**
 * Мок-данные студентов для тестирования и разработки
 */

val mockStudent1 = Student(
    name = "Воронков Денис Викторович",
    course = 4,
    codeOfDirection = "09.03.01 Информатика и вычислительная техника",
    nameOfDirection = "Программное обеспечение средств вычислительной техники и автоматизированных систем",
    group = "БПО-21-01",
    isForeign = false,
    gradeForPractice = "отлично",
    nameOfPracticeBase = "ООО \"РЕКОД Технологии\", г. Уфа",
    typeOfPractice = "преддипломная",
    periodOfPractice = "с 07.04.2025 г. по 18.05.2025 г.",
    formOfStudy = "Платная",
    withPayment = true,
    isPaidPractice = false,
    cityOfPractice = "Уфа",
    nameOfSpeciality = "Информатика и вычислительная техника",
    codeOfSpeciality = "09.03.01",
    headOfPracticeFromDepartment = "М.А. Салихова",
    headOfPracticeFromPracticeBase = "Э.Р. Читахян",
    postOfHeadOfPracticeFromPracticeBase = "руководитель практики",
    postOfHeadOfPracticeFromDepartment = "ст. преподаватель",
    directorName = "З.Х. Павлова"
)

val mockStudent2 = Student(
    name = "Иванов Иван Иванович",
    course = 4,
    codeOfDirection = "09.03.01 Информатика и вычислительная техника",
    nameOfDirection = "Программное обеспечение средств вычислительной техники и автоматизированных систем",
    group = "БПО-21-02",
    isForeign = false,
    gradeForPractice = "хорошо",
    nameOfPracticeBase = "ООО \"ТехноПарк\", г. Москва",
    typeOfPractice = "преддипломная",
    periodOfPractice = "с 07.04.2025 г. по 18.05.2025 г.",
    formOfStudy = "Бюджетная",
    withPayment = false,
    isPaidPractice = false,
    cityOfPractice = "Москва",
    nameOfSpeciality = "Информатика и вычислительная техника",
    codeOfSpeciality = "09.03.01",
    headOfPracticeFromDepartment = "П.С. Петров",
    headOfPracticeFromPracticeBase = "А.В. Сидоров",
    postOfHeadOfPracticeFromPracticeBase = "руководитель отдела",
    postOfHeadOfPracticeFromDepartment = "доцент",
    directorName = "З.Х. Павлова"
)

val mockStudent3 = Student(
    name = "Смирнова Анна Сергеевна",
    course = 3,
    codeOfDirection = "09.03.01 Информатика и вычислительная техника",
    nameOfDirection = "Программное обеспечение средств вычислительной техники и автоматизированных систем",
    group = "БПО-22-01",
    isForeign = false,
    gradeForPractice = "отлично",
    nameOfPracticeBase = "ПАО \"Сбербанк\", г. Казань",
    typeOfPractice = "производственная",
    periodOfPractice = "с 01.06.2024 г. по 31.07.2024 г.",
    formOfStudy = "Бюджетная",
    withPayment = false,
    isPaidPractice = true,
    cityOfPractice = "Казань",
    nameOfSpeciality = "Информационные системы и технологии",
    codeOfSpeciality = "09.03.02",
    headOfPracticeFromDepartment = "И.Н. Козлова",
    headOfPracticeFromPracticeBase = "Д.К. Васильев",
    postOfHeadOfPracticeFromPracticeBase = "ведущий разработчик",
    postOfHeadOfPracticeFromDepartment = "доцент",
    directorName = "З.Х. Павлова"
)

val mockStudent4 = Student(
    name = "Zhang Wei",
    course = 2,
    codeOfDirection = "09.03.01 Информатика и вычислительная техника",
    nameOfDirection = "Программное обеспечение средств вычислительной техники и автоматизированных систем",
    group = "БПО-23-03",
    isForeign = true,
    gradeForPractice = "хорошо",
    nameOfPracticeBase = "ООО \"ИТ-Компания\", г. Санкт-Петербург",
    typeOfPractice = "учебная",
    periodOfPractice = "с 15.05.2024 г. по 30.06.2024 г.",
    formOfStudy = "Платная",
    withPayment = true,
    isPaidPractice = false,
    cityOfPractice = "Санкт-Петербург",
    nameOfSpeciality = "Информатика и вычислительная техника",
    codeOfSpeciality = "09.03.01",
    headOfPracticeFromDepartment = "Т.П. Новикова",
    headOfPracticeFromPracticeBase = "М.С. Кузнецов",
    postOfHeadOfPracticeFromPracticeBase = "тимлид",
    postOfHeadOfPracticeFromDepartment = "ассистент",
    directorName = "З.Х. Павлова"
)

/** Список всех мок-студентов для удобства использования */
val mockStudents = listOf(mockStudent1, mockStudent2, mockStudent3, mockStudent4)