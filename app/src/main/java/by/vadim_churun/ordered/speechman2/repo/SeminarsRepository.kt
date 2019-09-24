package by.vadim_churun.ordered.speechman2.repo

import android.content.Context
import android.os.Looper
import by.vadim_churun.ordered.speechman2.R
import by.vadim_churun.ordered.speechman2.db.entities.*
import by.vadim_churun.ordered.speechman2.db.objs.*
import by.vadim_churun.ordered.speechman2.model.filters.SeminarsFilter
import by.vadim_churun.ordered.speechman2.model.objects.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.*
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.*
import java.util.Calendar
import kotlin.concurrent.thread


class SeminarsRepository(appContext: Context): SpeechManRepository(appContext)
{
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    // SELECT QUERIES:

    val filterSubject = PublishSubject.create<SeminarsFilter>()

    fun createSeminarObservable(seminarID: Int): Observable<Seminar>
        = super.seminarsDAO.getRx(seminarID)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

    fun createDaysObservable(seminarID: Int): Observable< List<SemDay> >
        = super.seminarsDAO.getDays(seminarID)
            .subscribeOn(Schedulers.io())
            .map { days ->
                if(Looper.myLooper() == Looper.getMainLooper())
                    throw Exception("Sorting SemDays on the UI thread")
                days.sortedBy { it.start }
            }.observeOn(AndroidSchedulers.mainThread())

    /** [SemCost]s are sorted just by their [Money] component.
      * For a smarter sort the received list can be passed to a [SeminarBuilder]. **/
    fun createCostsObservable(seminarID: Int): Observable< List<SemCost> >
        = super.seminarsDAO.getCosts(seminarID)
            .subscribeOn(Schedulers.io())
            .map { costs ->
                costs.sortedWith( object: Comparator<SemCost> {
                    override fun compare(c1: SemCost, c2: SemCost): Int
                    {
                        if(c1.cost.currency == c2.cost.currency)
                            return c1.cost.amount.compareTo(c2.cost.amount)
                        return c1.cost.currency.compareTo(c2.cost.currency)
                    }
                } )
            }.observeOn(AndroidSchedulers.mainThread())

    fun createHeaderObservable(seminarID: Int): Observable<SeminarHeader>
            = super.seminarsDAO.getHeader(seminarID)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())

    fun createHeadersObservable(): Observable<List<SeminarHeader>>
        = SpeechManRepository.FiilteredItemsStreamBuilder<SeminarHeader, SeminarsFilter>()
            .create(super.seminarsDAO.getHeaders(), filterSubject) { header, filter ->
                (!header.isLogicallyDeleted || !filter.forbidLogicallyDeleted) &&
                (header.name.contains(filter.nameSubstring, true))
            }.map { headers ->
                // Sort by start time. Nulls first, then ascending.
                headers.sortedBy { it.start?.timeInMillis ?: Long.MIN_VALUE }
            }.observeOn(AndroidSchedulers.mainThread())


    val infoSubject = PublishSubject.create<Seminar>()
    val infoSubjectHeaders = PublishSubject.create<List<SeminarHeader>>()

    fun createInfosObservable(): Observable<List<SeminarInfo>>
        = infoSubject.observeOn(Schedulers.computation())
            .map { seminar ->
                listOf(
                    seminar.ID ?: throw NullPointerException(
                        "Attempt to retrieve SeminarInfo for a Seminar with null ID" )
                )
            }.mergeWith(infoSubjectHeaders.observeOn(Schedulers.computation())
                .map { headers ->
                    if (Looper.myLooper() == Looper.getMainLooper())
                        throw Exception("Mapping infoSubjectHeaders on the UI thread!")
                    headers.map { header -> header.ID }
                }
            ).map { semIDs ->
                if (Looper.myLooper() == Looper.getMainLooper())
                    throw Exception("Mapping infoSubjectHeaders on the UI thread!")
                semIDs.map { semID ->
                    SeminarInfo(super.associationsDAO.countAppointmentsForSeminar(semID))
                }
            }.subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())

    fun createParticipantsObservable(seminarID: Int): Observable< List<Participant> >
        = super.seminarsDAO.getParticipants(seminarID)
            .subscribeOn(Schedulers.io())
            .map { particips ->
                if(Looper.myLooper() == Looper.getMainLooper())
                    throw Exception("Sorting on the UI thread")
                particips.sortedBy { it.person.name.toLowerCase() }
            }.observeOn(AndroidSchedulers.mainThread())


    //////////////////////////////////////////////////////////////////////////////////////////////////////
    // INSERT, UPDATE, DELETE QUERIES

    /** Asynchronously performs logical deletion of the specified [Seminar]. **/
    fun deleteSeminar(seminarID: Int)
    {
        thread(start = true) {
            // Logically delete the Seminar itself:
            val sem = super.seminarsDAO.get(seminarID)
            val deletedSem = Seminar(sem.ID, sem.name, sem.city,
                sem.address, sem.content, sem.imageUri, sem.costing,
                true
            )
            super.seminarsDAO.addOrUpdate(deletedSem)

            // Logically delete all associated appointments:
            val appoints = super.associationsDAO.getAppointmentsForSeminar(seminarID)
            appoints.map { appoint ->
                Appointment(appoint.personID, appoint.seminarID,
                    appoint.purchase, appoint.cost,
                    appoint.historyStatus, true)
            }.also {
                super.associationsDAO.addOrUpdateAppointments(it)
            }

            // TODO: Logically delete all associated tutor works.

            // Delete all associated SemDays:
            var disposableDays: Disposable? = null
            disposableDays = super.seminarsDAO.getDays(seminarID)
                .doOnNext { days ->
                    super.seminarsDAO.deleteDays(days)
                    disposableDays!!.dispose()
                }.subscribe()

            // Delete all SemCosts:
            var disposableCosts: Disposable? = null
            disposableCosts = super.seminarsDAO.getCosts(seminarID)
                .doOnNext { costs ->
                    super.seminarsDAO.deleteCosts(costs)
                    disposableCosts!!.dispose()
                }.subscribe()
        }
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////////
    // SEMINAR BUILDER:

    val builderSubject = BehaviorSubject.create<SeminarBuilder>()

    fun createBuilderObservable(): Observable<SeminarBuilder>
        = builderSubject
            .observeOn(Schedulers.single())
            .map { builder ->
                builder.dayBuilders.sortBy { it.start }
                builder.costBuilders.sortWith(object : Comparator<SeminarBuilder.CostBuilder> {
                    override fun compare
                    (cb1: SeminarBuilder.CostBuilder, cb2: SeminarBuilder.CostBuilder): Int
                    {
                        if(builder.costing == Seminar.CostingStrategy.DATE
                            || builder.costing == Seminar.CostingStrategy.DATE_PARTICIPANTS) {

                            // Appointment date is the most important.
                            if(cb1.minDate.timeInMillis == cb2.minDate.timeInMillis)
                                return cb1.minParticipants.compareTo(cb2.minParticipants)
                            return cb1.minDate.timeInMillis.compareTo(cb2.minDate.timeInMillis)

                        } else if(builder.costing == Seminar.CostingStrategy.PARTICIPANTS
                            || builder.costing == Seminar.CostingStrategy.PARTICIPANTS_DATE) {

                            // Number of participants is the most important.
                            if(cb1.minParticipants == cb2.minParticipants)
                                return cb1.minDate.timeInMillis.compareTo(cb2.minDate.timeInMillis)
                            return cb1.minParticipants.compareTo(cb2.minParticipants)

                        }

                        // Costing is fixed. No sort is needed.
                        return 0
                    }
                })
                builder
            }.observeOn(AndroidSchedulers.mainThread())

    /** Asynchronously migrates the given [SeminarBuilder] to respect
      * the specified [Seminar.CostingStrategy].
      * As done, the instance is emitted by any stream created by [createBuilderObservable] method. **/
    fun migrateCosting(builder: SeminarBuilder, newCosting: Seminar.CostingStrategy)
    {
        // TODO: Implement a smarter migrating strategy.

        thread(start = true) {
            if(builder.costBuilders.isEmpty()) {
                builder.costing = newCosting
                if(newCosting == Seminar.CostingStrategy.FIXED)
                    builder.costBuilders.add( SeminarBuilder.CostBuilder() )
            } else if(newCosting == Seminar.CostingStrategy.FIXED) {
                val firstCost = builder.costBuilders[0]
                builder.costBuilders.clear()
                builder.costBuilders.add(firstCost)
            } else {
                val addDateDependency =
                    !Seminar.doesCostDependOnDate(builder.costing)
                            && Seminar.doesCostDependOnDate(newCosting)
                val addParticipsDependency =
                    !Seminar.doesCostDependOnParticipants(builder.costing)
                            && Seminar.doesCostDependOnParticipants(newCosting)

                val firstCostBuilder = builder.costBuilders[0]
                if(addDateDependency)
                    firstCostBuilder.minDate = Calendar.getInstance()
                if(addParticipsDependency)
                    firstCostBuilder.minParticipants = 0
                for(j in 1.until(builder.costBuilders.size))
                {
                    val previous = builder.costBuilders[j-1]
                    val cur = builder.costBuilders[j]
                    if(addDateDependency) {
                        cur.minDate.timeInMillis = previous.minDate.timeInMillis
                        cur.minDate.add(Calendar.DAY_OF_MONTH, 7)
                    }
                    if(addParticipsDependency)
                        cur.minParticipants = 2 * previous.minParticipants
                }

            }

            builder.costing = newCosting
            builderSubject.onNext(builder)
        }
    }

    /** Asynchronously inserts a new or updates an existing [Seminar]
     * with associated [SemDay]s and [SemCost]s.
     * Important: further modifications of supplied [SeminarBuilder] object are unsafe.
     * @return ID of the string to describe the reason [builder] is not validated for,
     * or null if it is validated.  **/
    fun save(builder: SeminarBuilder): Int?
    {
        if (builder.name.isEmpty())
            return R.string.msg_no_seminar_name
        if (builder.city.isEmpty())
            return R.string.msg_no_seminar_city

        super.seminarsDAO.addOrUpdateRx(builder.buildSeminar())
            .subscribeOn(Schedulers.io())
            .doOnSuccess { semIdLong ->
                // Now ID of the Seminar is known, so use it to build SemDays and SemCosts:
                builder.ID = semIdLong.toInt()

                // Insert/Update all associated SemDays:
                var subscriptionDays: Disposable? = null
                subscriptionDays = super.seminarsDAO.getDays(builder.ID!!)
                    .subscribeOn(Schedulers.io())
                    .doOnNext { oldDays ->
                        super.seminarsDAO.deleteDays(oldDays)
                        super.seminarsDAO.addOrUpdateDays(builder.buildDays())
                        subscriptionDays!!.dispose()
                    }.subscribe()

                // Insert/Update all associated SemCosts:
                var subscriptionCosts: Disposable? = null
                subscriptionCosts = super.seminarsDAO.getCosts(builder.ID!!)
                    .subscribeOn(Schedulers.io())
                    .doOnNext { oldCosts ->
                        super.seminarsDAO.deleteCosts(oldCosts)
                        super.seminarsDAO.addOrUpdateCosts(builder.buildCosts())
                        subscriptionCosts!!.dispose()
                    }.subscribe()
            }.subscribe()

        return null
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////////
    // SEMINAR APPOINTMENTS BUILDER:

    val appointsBuilderSubject = BehaviorSubject.create<SeminarAppointsBuilder>()

    /** Asynchronously creates a [SeminarAppointsBuilder] from the specified seminar
      * and emits it via [appointsBuilderSubject]. **/
    fun retrieveAppointsBuilder(seminarID: Int)
    {
        val disposable = CompositeDisposable()
        var mPeople: List<Person>? = null
        var mParticips: List<Participant>? = null

        /** Emits [SeminarAppointsBuilder] from the variables if they all have been initialized. **/
        fun tryEmitBuilder()
        {
            if(mPeople == null || mParticips == null)
                return
            disposable.clear()
            SeminarAppointsBuilder.createFromParticipants(seminarID, mPeople!!, mParticips!!).also {
                appointsBuilderSubject.onNext(it)
            }
        }

        // Retrieve all People from within the database, so to initialize mPeople:
        super.peopleDAO.get()
            .subscribeOn(Schedulers.io())
            .map { people ->
                people.sortedBy { it.name.toLowerCase() }
            }.doOnNext { people ->
                mPeople = people
                tryEmitBuilder()
            }.subscribe()
            .also { disposable.add(it) }

        // Retrieve Participants for the specified seminar, so to initialize mParticipsIDs:
        super.seminarsDAO.getParticipants(seminarID)
            .subscribeOn(Schedulers.io())
            .doOnNext { particips ->
                mParticips = particips
                tryEmitBuilder()
            }.subscribe()
            .also { disposable.add(it) }
    }

    /** Asynchronously adds and deletes [Appointment]s as specified by the given [SeminarAppointsBuilder].
      * The deletion is logical. **/
    fun saveAppoints(builder: SeminarAppointsBuilder)
    {
        thread(start = true) {
            super.associationsDAO.addOrUpdateAppointments(builder.addedAppoints)
            super.associationsDAO.addOrUpdateAppointments(builder.removedAppoints)
        }
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////////
    // OTHER QUERIES:

    fun save(seminar: Seminar)
    {
        thread(start = true) {
            super.seminarsDAO.addOrUpdate(seminar)
        }
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////////
    // INPUT VALIDATION

    /** Validates value for [SemCost.minParticipants] field.
      * @return ID of the string to show to the user, if not validated; null, otherwise. **/
    fun validateCostParticipants(input: String): Int?
    {
        if(input.isEmpty())
            return R.string.msg_semcost_particips_empty

        var value: Int
        try {
            value = input.toInt()
        } catch(exc: NumberFormatException) {
            return R.string.msg_semcost_particips_notint
        }

        if(value < 0)
            return R.string.msg_semcost_particips_negative

        return null
    }
}