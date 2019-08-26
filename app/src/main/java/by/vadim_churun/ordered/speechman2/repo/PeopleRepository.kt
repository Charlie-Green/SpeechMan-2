package by.vadim_churun.ordered.speechman2.repo

import android.content.Context
import android.os.Looper
import by.vadim_churun.ordered.speechman2.db.entities.Person
import by.vadim_churun.ordered.speechman2.model.filters.PeopleFilter
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
    // OTHER:

    fun validateName(name: CharSequence): Boolean
        = name.isNotEmpty()

    fun addOrUpdate(person: Person)
    {
        thread(start = true) {
            super.peopleDAO.addOrUpdate(person)
        }
    }
}