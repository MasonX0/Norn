// commonMain/data/coursework/mock/mockData.kt
package ru.bpo.norn.commonMain.data.coursework.mock.mockData

import ru.bpo.norn.commonMain.models.Student

val mockStudent1 = Student(
    name = "Воронков Денис Викторович",
    course = 4,
    group = "БПО-21-01",
    isForeign = false,
    gradeForPractice = "отлично",
    nameOfPracticeBase = "ООО \"РЕКОД Технологии\", в г. Уфа",
    typeOfPractice = "преддипломная",
    periodOfPractice = "с 07.04.2025 г. по 18.05.2025 г.",
    formOfStudy = "Платная",
    withPayment = true,
    cityOfPractice = "Уфа",
    nameOfSpeciality = "Информатика и вычислительная техника",
    codeOfSpeciality = "09.03.01",
    headOfPracticeFromDepartment = "М.А. Салихова",
    headOfPracticeFromPracticeBase = "Э. Р. Читахян",
    postOfHeadOfPracticeFromPracticeBase = "руководитель практики",
    postOfHeadOfPracticeFromDepartment = "ст. преподаватель",
    directorName = "З. Х. Павлова"
)

val mockStudent2 = Student(
    name = "Иванов Иван Иванович",
    course = 4,
    group = "БПО-21-02",
    isForeign = false,
    gradeForPractice = "хорошо",
    nameOfPracticeBase = "ООО \"ТехноПарк\", в г. Москва",
    typeOfPractice = "преддипломная",
    periodOfPractice = "с 07.04.2025 г. по 18.05.2025 г.",
    formOfStudy = "Бюджетная",
    withPayment = false,
    cityOfPractice = "Москва",
    nameOfSpeciality = "Информатика и вычислительная техника",
    codeOfSpeciality = "09.03.01",
    headOfPracticeFromDepartment = "П.С. Петров",
    headOfPracticeFromPracticeBase = "А.В. Сидоров",
    postOfHeadOfPracticeFromPracticeBase = "руководитель отдела",
    postOfHeadOfPracticeFromDepartment = "доцент",
    directorName = "З. Х. Павлова"
)