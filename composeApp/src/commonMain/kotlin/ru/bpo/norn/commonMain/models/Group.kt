package ru.bpo.norn.commonMain.models

// ru.bpo.norn.commonMain.models.Group
data class Group(
    val name: String,
    val students: List<Student>,
    var codeOfDirection: String,
    var nameOfDirection: String
) {
    fun getStudentsCount(): Int = students.size
    fun getCountOfStudentsWithGrade(grade: String): Int =
        students.count { it.gradeForPractice == grade }

    constructor(name: String) : this(
        name = name,
        students = emptyList(),
        codeOfDirection = "09.03.01",
        nameOfDirection = "Технологии искусственного интеллекта в нефтегазовой отрасли"
    )

    // Методы для подсчета статистики по предприятиям
    fun calculateStatistics(enterprises: List<Enterprise>): GroupStatistics {
        val totalStudents = students.size
        val foreignStudents = students.count { it.isForeign }
        val paidPracticeStudents = students.count { it.isPaidPractice }

        // Подсчитываем по типам предприятий на основе nameOfPracticeBase
        var foreignEnterprises = 0
        var rfEnterprises = 0
        var soluniTyulyukInzer = 0
        var departmentStudents = 0
        var universitySubdivisions = 0
        var baseDepartments = 0

        students.forEach { student ->
            val enterprise =
                enterprises.find { it.name.contains(student.nameOfPracticeBase, ignoreCase = true) }
            when {
                enterprise?.isForeign == true -> foreignEnterprises++
                enterprise?.isSoluniTyulyukInzer == true -> soluniTyulyukInzer++
                enterprise?.isDepartment == true -> departmentStudents++
                enterprise?.isUniversitySubdivision == true -> universitySubdivisions++
                enterprise?.isBaseDepartment == true -> baseDepartments++
                else -> rfEnterprises++ // По умолчанию считаем РФ предприятием
            }
        }

        // Извлекаем сроки и тип практики из данных студентов группы
        val firstStudent = students.firstOrNull()
        val practiceStartDate =
            firstStudent?.periodOfPractice?.split("-")?.firstOrNull()?.trim() ?: "23.06.2025"
        val practiceEndDate =
            firstStudent?.periodOfPractice?.split("-")?.lastOrNull()?.trim() ?: "06.07.2025"
        val practiceType = firstStudent?.typeOfPractice ?: "учебная"

        return GroupStatistics(
            totalStudents = totalStudents,
            foreignEnterprises = foreignEnterprises,
            rfEnterprises = rfEnterprises,
            soluniTyulyukInzer = soluniTyulyukInzer,
            departmentStudents = departmentStudents,
            universitySubdivisions = universitySubdivisions,
            baseDepartments = baseDepartments,
            paidPracticeStudents = paidPracticeStudents,
            foreignStudents = foreignStudents,
            excellentGrades = getCountOfStudentsWithGrade("отлично"),
            goodGrades = getCountOfStudentsWithGrade("хорошо"),
            satisfactoryGrades = getCountOfStudentsWithGrade("удовлетворительно"),
            notDefended = getCountOfStudentsWithGrade("не защитили в срок") + getCountOfStudentsWithGrade(
                "неудов"
            ),
            practiceStartDate = practiceStartDate,
            practiceEndDate = practiceEndDate,
            practiceType = practiceType
        )
    }
}

// Временно оставляем data class для совместимости, потом удалим
data class GroupStatistics(
    val totalStudents: Int,
    val foreignEnterprises: Int,
    val rfEnterprises: Int,
    val soluniTyulyukInzer: Int,
    val departmentStudents: Int,
    val universitySubdivisions: Int,
    val baseDepartments: Int,
    val paidPracticeStudents: Int,
    val foreignStudents: Int,
    val excellentGrades: Int,
    val goodGrades: Int,
    val satisfactoryGrades: Int,
    val notDefended: Int,
    val practiceStartDate: String,
    val practiceEndDate: String,
    val practiceType: String
)