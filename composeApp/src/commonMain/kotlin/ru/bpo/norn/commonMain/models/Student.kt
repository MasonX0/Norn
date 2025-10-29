package ru.bpo.norn.commonMain.models


class Student(
    val name: String,

    //?
    val group: String,

    val gradeForPractice: String,
    val nameOfPracticeBase: String,
    val typeOfPractice: String,
    val periodOfPractice: String,
    val formOfPractice: String,
    val withPayment: Boolean,


    //?
    val cityOfPractice: String?,


    //?
    val nameOfSpeciality: String,
    val codeOfSpeciality: String,
    //?
    val headOfPractice: String,
    val directorName: String,
) {
    //val result2 = str.filterNot { it in " -" }
}

// TODO: Сделать мок группу/студента