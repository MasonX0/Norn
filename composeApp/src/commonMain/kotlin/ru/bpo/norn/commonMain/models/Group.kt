package ru.bpo.norn.commonMain.models


class Group(
    val students: List<Student>,
) {
    fun getStudentsCount(): Int = students.size
    fun getCountOfStudentsWithGrade(grade: String): Int = students.count{it.gradeForPractice == grade}
}
// TODO: Сделать мок группу/студента