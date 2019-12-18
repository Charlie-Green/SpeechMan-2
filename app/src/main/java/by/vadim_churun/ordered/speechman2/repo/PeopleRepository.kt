package by.vadim_churun.ordered.speechman2.repo

import android.content.Context
import by.vadim_churun.ordered.speechman2.db.entities.*
import by.vadim_churun.ordered.speechman2.db.objs.*
import by.vadim_churun.ordered.speechman2.model.filters.*
import by.vadim_churun.ordered.speechman2.model.objects.PersonHeader
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread


class PeopleRepository(appContext: Context): SpeechManRepository(appContext)
{
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    // SELECT PEOPLE:

    private var lastPeople: List<Person>? = null
    private var lastFilter: PeopleFilter? = null
    val filterSubject = BehaviorSubject.create<PeopleFilter>()
    val infoRequestSubject = BehaviorSubject.create< List<Person> >()

    private fun Person.toHeader()
        = PersonHeader(
            this,
            this@PeopleRepository.associationsDAO.countAppointmentsForPerson(this.ID!!),
            this@PeopleRepository.associationsDAO.countOrdersForPerson(this.ID!!)
        )

    private fun List<Person>.filter(filt: PeopleFilter)
        = this.filter { person ->
            (filt.typeID == null || person.personTypeID == filt.typeID) &&
            (person.name.contains(filt.nameSubstring, true))
        }


    fun createPeopleHeadersObservable(): Observable< List<PersonHeader> >
        = super.peopleDAO.get().map { people ->
            lastPeople = people
            Pair< List<Person>?, PeopleFilter? >(people, lastFilter)
        }.subscribeOn(Schedulers.io())
        .mergeWith(
            filterSubject.debounce(256, TimeUnit.MILLISECONDS)
                .map { filter ->
                    lastFilter = filter
                    Pair< List<Person>?, PeopleFilter? >(lastPeople, filter)
                }.subscribeOn(Schedulers.computation())
        ).observeOn(Schedulers.computation())
        .switchMap { pair ->
            val filter = pair.second
            val people = pair.first?.let { allPeople ->
                filter?.let {
                    // Apply filter if one is provided.
                    allPeople.filter(it)
                } ?: allPeople  // Otherwise, pass all people.
            } ?: return@switchMap Observable.empty< List<PersonHeader> >()

            Observable.create< List<PersonHeader> > { emitter ->
                // First, provide UI with basic information about people.
                val headers = MutableList(people.size) { index ->
                    PersonHeader(people[index], null, null)
                }
                emitter.onNext(headers)

                // Now, provide additional information.
                val EMIT_FREQUENCY = 64
                for(index in 0 until headers.size) {
                    headers[index] = people[index].toHeader()
                    if((index % EMIT_FREQUENCY) == EMIT_FREQUENCY - 1)
                        emitter.onNext(headers)
                }
                if((headers.size % EMIT_FREQUENCY) != 0)
                    emitter.onNext(headers)
            }
        }.observeOn(AndroidSchedulers.mainThread())

    fun createPeopleObservable()
        = super.peopleDAO.get().map { people ->
            lastFilter?.let {
                people.filter(it)
            } ?: people
        }.subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())

    fun createPersonObservable(personID: Int): Observable<Person>
        = super.peopleDAO.get(personID)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

    fun createPersonHeaderObservable(personID: Int)
        = super.peopleDAO.get(personID).map { person ->
            person.toHeader()
        }.subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())


    //////////////////////////////////////////////////////////////////////////////////////////////////////
    // FOR APPOINTMENTS:

    val appointedSeminarsSubject = BehaviorSubject.create< List<AppointedSeminar> >()
    val appointedSeminarsFilterSubject = BehaviorSubject.create< SeminarsFilter >()
    val semheadersFilterSubject = BehaviorSubject.create<SeminarsFilter>()

    fun createAppointedSeminarObservable(personID: Int, seminarID: Int)
        = super.peopleDAO.getAppointedSeminars(personID)
            .subscribeOn(Schedulers.io())
            .map { appsems ->
                appsems.find { it.appoint.seminarID == seminarID }!!
            }.observeOn(AndroidSchedulers.mainThread())

    fun createAppointedSeminarsObservable(personID: Int)
        = SpeechManRepository.FiilteredItemsStreamBuilder<AppointedSeminar, SeminarsFilter>()
            .create(super.peopleDAO.getAppointedSeminars(personID),
                appointedSeminarsFilterSubject ) { appsem, filter ->
                    (appsem.seminar.name.contains(filter.nameSubstring, true)) &&
                    (!filter.forbidLogicallyDeleted || !appsem.seminar.isLogicallyDeleted) &&
                    (!filter.forbidLogicallyDeleted || !appsem.appoint.isLogicallyDeleted)
                }
            .map { appsems ->
                appsems.sortedBy { it.start?.timeInMillis ?: Long.MIN_VALUE }
            }.observeOn(AndroidSchedulers.mainThread())

    fun createSemHeadersNotForPersonObservable(excludedPersonID: Int)
        = SpeechManRepository.FiilteredItemsStreamBuilder<SeminarHeader, SeminarsFilter>()
            .create(super.peopleDAO.getSemHeadersNotForPerson(excludedPersonID),
                semheadersFilterSubject ) { semheader, filter ->
                    (!semheader.isLogicallyDeleted || !filter.forbidLogicallyDeleted) &&
                    (semheader.name.contains(filter.nameSubstring, true))
                }.map { semheaders ->
                    semheaders.sortedBy { it.start?.timeInMillis ?: Long.MIN_VALUE }
                }.observeOn(AndroidSchedulers.mainThread())


    //////////////////////////////////////////////////////////////////////////////////////////////////////
    // OTHER:

    fun validateName(name: CharSequence): Boolean
        = name.isNotEmpty()

    fun addOrUpdate(person: Person)
    {
        thread(start = true) {
            super.peopleDAO.addOrUpdate(person)
        }
    }

    fun addOrUpdateAppointments(appointments: List<Appointment>)
    {
        thread(start = true) {
            super.associationsDAO.addOrUpdateAppointments(appointments)
        }
    }

    fun delete(person: Person)
    {
        thread(start = true) {
            // Physically delete all associated Appointments:
            super.associationsDAO.deleteAppointments(
                super.associationsDAO.getAllAppointmentsForPerson(person.ID!!) )

            // TODO: Physically delete all associated Orders.

            // Physically delete the person themself:
            super.peopleDAO.delete(person)
        }
    }

    fun deleteAppointment(appoint: Appointment)
    {
        thread(start = true) {
            val app = if(appoint.isLogicallyDeleted) appoint
            else Appointment(
                appoint.personID, appoint.seminarID,
                appoint.purchase, appoint.cost,
                appoint.historyStatus, true )
            super.associationsDAO.addOrUpdateAppointments(listOf(app))
        }
    }
}