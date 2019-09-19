package by.vadim_churun.ordered.speechman2.model.filters


class SeminarsFilter(
    /** The [Seminar]'s name must contain this substring. **/
    val nameSubstring: String,

    /** Whether logically deleted [Seminar]s must be filtered out. **/
    val forbidLogicallyDeleted: Boolean )
{
    override fun equals(other: Any?): Boolean
    {
        other ?: return false
        if(other !is SeminarsFilter)
            throw IllegalArgumentException("Comparing SeminarsFilter to something else")
        return (forbidLogicallyDeleted == other.forbidLogicallyDeleted) &&
            (nameSubstring == other.nameSubstring)
    }
}