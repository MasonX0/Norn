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

    //?
    val group: String,

    val isForeign:Boolean,

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