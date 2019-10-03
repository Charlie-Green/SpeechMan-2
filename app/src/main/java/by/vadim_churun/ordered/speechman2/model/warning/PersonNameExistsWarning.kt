package by.vadim_churun.ordered.speechman2.model.warning

import by.vadim_churun.ordered.speechman2.db.entities.Person


class PersonNameExistsWarning(val ID: Int?,
    val name: String,
    val personTypeID: Int?
): DataWarning<Person>()
{
    override fun createObject(): Person
        = Person(ID, name, personTypeID)
}