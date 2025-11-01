package ru.bpo.norn.commonMain.models



class Practice (
    val nameOfOrganization:String,
    val nameOfHead: String,
    val city:String,
    val countOfPlaces:Int
)

{
    //private count :Int = countOfPlaces
    init {



    }

}


class Student(
    val name: String,

    val course: Int,

    val group: String,

    val isForeign:Boolean,

    val gradeForPractice: String,

    val nameOfPracticeBase: String,

    val typeOfPractice: String,

    val periodOfPractice: String,

    val formOfStudy: String,

    val withPayment: Boolean,

    val cityOfPractice: String?,

    val nameOfSpeciality: String,

    val codeOfSpeciality: String,

    val headOfPracticeFromDepartment: String,

    val headOfPracticeFromPracticeBase: String,

    val postOfHeadOfPracticeFromPracticeBase: String,

    val postOfHeadOfPracticeFromDepartment: String,

    val directorName: String = "Павлова",
) {
    //val result2 = str.filterNot { it in " -" }
}

// TODO: Сделать мок группу/студента