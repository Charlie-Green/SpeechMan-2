package by.vadim_churun.ordered.speechman2.model.filters


class PeopleFilter(
    /** Person's name must contain this substring. **/
    val nameSubstring: String,

    /** Person must be of that type (pass null to discard the check). **/
    val typeID: Int? )
{
    override fun equals(other: Any?): Boolean
    {
        other ?: return false
        if(other !is PeopleFilter)
            throw IllegalArgumentException("Comparing PeopleFilter to something else.")
        return (typeID == other.typeID) && (nameSubstring == other.nameSubstring)
    }
}