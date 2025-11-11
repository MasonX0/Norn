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
}