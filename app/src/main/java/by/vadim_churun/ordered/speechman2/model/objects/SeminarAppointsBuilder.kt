package by.vadim_churun.ordered.speechman2.model.objects

import by.vadim_churun.ordered.speechman2.db.entities.Appointment
import by.vadim_churun.ordered.speechman2.db.entities.*
import by.vadim_churun.ordered.speechman2.db.objs.*


class SeminarAppointsBuilder private constructor(
    val seminarID: Int,
    val allPeople: List<Person> )
{
    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    // INSTANCE MEMBERS:

    private val appointMap = HashMap<Int, Appointment>()
    private val added = emptyList<Appointment>().toMutableList()
    private val removed = emptyList<Appointment>().toMutableList()

    val initialAppointsCount: Int
        get() = appointMap.size

    val addedAppoints: List<Appointment>
        get() = added

    val removedAppoints: List<Appointment>
        get() = removed

    fun appointForPerson(personID: Int)
        = appointMap.get(personID)

    fun willBeAppointed(personID: Int): Boolean
    {
        if(appointMap.containsKey(personID))
            return removed.find { it.personID == personID } == null
        return addedAppoints.find { it.personID == personID } != null
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    // COMPANION OBJECT:

    companion object
    {
        private fun <T> create(semID: Int,
            listPeople: List<Person>,
            collectionAppoints: Collection<T>,
            getAppoint: (T) -> Appointment
        ): SeminarAppointsBuilder
        {
            val builder = SeminarAppointsBuilder(semID, listPeople)

            collectionAppoints.forEach { t ->
                val appoint = getAppoint(t)
                if(appoint.seminarID != semID) {
                    throw IllegalArgumentException(
                        "Seminar IDs don't match. Expected: $semID. Got: ${appoint.seminarID}")
                }
                builder.appointMap.put(appoint.personID, appoint)
            }

            return builder
        }

        fun createFromAppointments
        (semID: Int, listPeople: List<Person>, collectionAppoints: Collection<Appointment>)
            = create(semID, listPeople, collectionAppoints) { it }

        fun createFromParticipants
        (semID: Int, listPeople: List<Person>, collectionAppoints: Collection<Participant>)
            = create(semID, listPeople, collectionAppoints) { it.appoint }
    }

    fun updateAppointment(newAppoint: Appointment)
    {
        if(newAppoint.seminarID != seminarID)
            throw IllegalArgumentException(
                "Wrong seminar ID. Expected $seminarID, got ${newAppoint.seminarID}" )

        val targetList = if(newAppoint.isLogicallyDeleted) removed else added
        val index = targetList.indexOfFirst {
            it.personID == newAppoint.personID
        }
        if(index < 0)
            throw IllegalArgumentException("Illegal personID ${newAppoint.personID}")
        targetList[index] = newAppoint
    }


    fun swapAppointingStatus(personID: Int)
    {
        for(j in 0.until(added.size))
        {
            if(added[j].personID == personID) {
                // This person was a candidate to be added, but they're not.
                added.removeAt(j)
                return
            }
        }

        for(j in 0.until(removed.size))
        {
            if(removed[j].personID == personID) {
                // This person was a candidate to be removed, but now they're not.
                removed.removeAt(j)
                return
            }
        }

        // This person wasn't a candidate to be added/removed, but now they are.
        val swappedAppoint = appointMap.get(personID)
        if(swappedAppoint == null) {
            Appointment(personID, seminarID,
                Money(0f, "USD"), Money(90f, "USD"),
                HistoryStatus.USUAL, false
            ).also {
                added.add(it)
            }
        } else {
            if(swappedAppoint.personID != personID || swappedAppoint.seminarID != seminarID)
                throw Exception("Something went wrong with Appointment IDs")

            Appointment(personID, seminarID,
                swappedAppoint.purchase, swappedAppoint.cost,
                HistoryStatus.USUAL, true
            ).also {
                removed.add(it)
            }
        }
    }
}