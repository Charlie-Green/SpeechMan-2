package by.vadim_churun.ordered.speechman2.remote.lack

import by.vadim_churun.ordered.speechman2.db.entities.Person


class PersonNameLack(val typeID: Int?): DataLack<String, Person>()
{
    override fun validate(potentialData: String): Boolean
        = potentialData.isNotEmpty()

    override fun buildObject(missedData: String): Person
        = Person(null, missedData, typeID)
}