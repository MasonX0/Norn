package ru.bpo.norn.commonMain.models


class Group(
    val name:String,
    val students: List<Student>,

    var codeOfDirection: String,

    var nameOfDirection: String

) {
    fun getStudentsCount(): Int = students.size
    fun getCountOfStudentsWithGrade(grade: String): Int = students.count { it.gradeForPractice == grade }

    // Вторичный конструктор по умолчанию - для БПО09-24-02
    constructor(name:String) : this(
        name=name,
        students = emptyList(),
        codeOfDirection = "09.03.01 Информатика и вычислительная техника",
        nameOfDirection = "Технологии искусственного интеллекта в нефтегазовой отрасли"
    )
}
// TODO: Сделать мок группу/студента