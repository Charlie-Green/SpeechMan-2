package by.vadim_churun.ordered.speechman2.repo

import android.content.Context
import android.os.Looper
import by.vadim_churun.ordered.speechman2.db.entities.*
import by.vadim_churun.ordered.speechman2.db.objs.*
import by.vadim_churun.ordered.speechman2.model.filters.*
import by.vadim_churun.ordered.speechman2.model.objects.PersonInfo
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import kotlin.concurrent.thread


class PeopleRepository(appContext: Context): SpeechManRepository(appContext)
{
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    // LIST OF PEOPLE:

    val filterSubject = BehaviorSubject.create<PeopleFilter>()

    fun createPeopleObservable(): Observable< List<Person> >
        = SpeechManRepository.FiilteredItemsStreamBuilder<Person, PeopleFilter>()
            .create(super.peopleDAO.get(), this.filterSubject) { person, filter ->
                ( person.name.contains(filter.nameSubstring, true) ) &&
                ( filter.typeID == null || person.personTypeID == filter.typeID )
            }.map { people ->
                if(Looper.myLooper() == Looper.getMainLooper())
                    throw Exception("Filtering people on UI thread")
                people.sortedBy { it.name.toLowerCase() }
            }.observeOn(AndroidSchedulers.mainThread())


    //////////////////////////////////////////////////////////////////////////////////////////////////////
    // PERSON INFOS:

    val infoRequestSubject = BehaviorSubject.create< List<Person> >()

    fun createInfoObservable(): Observable<PersonInfo>
        = infoRequestSubject.switchMap { people ->
            Observable.create<PersonInfo> { emitter ->
                for(j in 0..people.lastIndex)
                {
                    PersonInfo(j,
                        super.associationsDAO.countAppointmentsForPerson(people[j].ID!!),
                        super.associationsDAO.countOrdersForPerson(people[j].ID!!)
                    ).also {
                        emitter.onNext(it)
                    }
                }

                emitter.onComplete()
            }.subscribeOn(Schedulers.single())
        }.subscribeOn(Schedulers.single())
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
                if(Looper.myLooper() == Looper.getMainLooper())
                    throw Exception("Searching on the UI thread")
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
                    if(Looper.myLooper() == Looper.getMainLooper())
                        throw Exception("Sorting on the UI thread")
                    semheaders.sortedBy { it.start?.timeInMillis ?: Long.MIN_VALUE }
                }.observeOn(AndroidSchedulers.mainThread())


    //////////////////////////////////////////////////////////////////////////////////////////////////////
    // OTHER:

    fun validateName(name: CharSequence): Boolean
        = name.isNotEmpty()

    fun createPersonObservable(personID: Int): Observable<Person>
        = super.peopleDAO.get(personID)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

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