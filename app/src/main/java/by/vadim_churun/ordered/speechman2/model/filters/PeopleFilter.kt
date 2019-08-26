package by.vadim_churun.ordered.speechman2.model.filters


class PeopleFilter(
    /** Person's name must contain this substring. **/
    val nameSubstring: String,

    /** Person must be of that type (pass null to discard the check). **/
    val typeID: Int?
)