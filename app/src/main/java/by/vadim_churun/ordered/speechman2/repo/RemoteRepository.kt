package by.vadim_churun.ordered.speechman2.repo

import android.content.Context
import android.util.Log
import by.vadim_churun.ordered.speechman2.db.entities.*
import by.vadim_churun.ordered.speechman2.model.exceptions.UnknownResponseSpeechManException
import by.vadim_churun.ordered.speechman2.model.lack_info.*
import by.vadim_churun.ordered.speechman2.model.objects.*
import by.vadim_churun.ordered.speechman2.model.warning.*
import by.vadim_churun.ordered.speechman2.remote.connect.*
import by.vadim_churun.ordered.speechman2.remote.lack.*
import by.vadim_churun.ordered.speechman2.remote.xml.SpeechManXmlParser
import by.vadim_churun.ordered.speechman2.remote.xml.SpeechManXmlWriter
import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.io.InputStream


class RemoteRepository(appContext: Context):
SpeechManRepository(appContext)
{
    //////////////////////////////////////////////////////////////////////////////////////////////////
    // COMPANION OBJECTS:

    companion object
    {
        private val LOGTAG = RemoteRepository::class.java.simpleName
        private val ID_LOCK = Any()
        private var nRequestID = 0

        val nextRequestID: Int
            get() = synchronized(ID_LOCK) {
                nRequestID.also { ++nRequestID }
            }
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////
    // IP:

    fun validateIP(ip: String): Boolean
        = SpeechManRemoteConnector.validateIP(ip)

    fun persistIP(ip: String)
        = SpeechManRemoteConnector.persistIP(super.appContext, ip)

    fun createPersistedIpMaybe(): Maybe<String>
        = Maybe.create<String> { emitter ->
            SpeechManRemoteConnector
                .getPersistedIP(super.appContext)
                ?.also { emitter.onSuccess(it) }
            emitter.onComplete()
        }.subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())


    //////////////////////////////////////////////////////////////////////////////////////////////////
    // INTERACTION WITH THE SERVER:

    private fun assertResponseOk(instream: InputStream)
    {
        val response = instream.read()
        when(response.toChar())
        {
            'O' ->  return
            'E' ->  throw SpeechManServerException(instream.read().toByte())
            else -> throw UnknownResponseSpeechManException(response.toByte())
        }
    }


    private fun fetchRemoteData(request: SyncRequest): RemoteData.Builder
    {
        Log.v(LOGTAG, "fetchRemoteData")

        val rdb = RemoteData.Builder(request.requestID)
        val connection = SpeechManRemoteConnector.openConnection(request.ip)
        SyncResponse(request.requestID, SyncResponse.ProgressStatus.CONNECTION_OPENED).also {
            responseSubject.onNext(it)
        }

        connection.getOutputStream().use { outstream ->
            outstream.write('I'.toInt())
            connection.getInputStream().use { instream ->
                assertResponseOk(instream)
                SpeechManXmlParser.parse(instream, rdb.entities, rdb.lacks )
            }
        }
        SyncResponse(request.requestID, SyncResponse.ProgressStatus.XML_PARSED).also {
            responseSubject.onNext(it)
        }

        return rdb
    }


    private fun pushRemoteData(request: SyncRequest)
    {
        val connection = SpeechManRemoteConnector.openConnection(request.ip)
        responseSubject.onNext( SyncResponse(request.requestID, SyncResponse.ProgressStatus.CONNECTION_OPENED) )

        connection.getOutputStream().use { outstream ->
            outstream.write('E'.toInt())
            SpeechManXmlWriter(outstream).use { writer ->
                writer.writeHead()

                // writer.writePersonTypes( ... )
                writer.writePeople( super.peopleDAO.rawGet() )
                writer.writeSeminars( super.seminarsDAO.rawGetAll() )
                writer.writeSemDays( super.seminarsDAO.rawGetDays() )
                writer.writeSemCosts( super.seminarsDAO.rawGetCosts() )
                writer.writeProducts( super.productsDAO.rawGetAll() )
                writer.writeAppointments( super.associationsDAO.getAllAppointments() )
                writer.writeOrders( super.associationsDAO.getAllOrders() )

                writer.writeEnding()
            }
        }

        connection.getInputStream().use { instream ->
            assertResponseOk(instream)
        }
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////
    // WARNING GENERATORS:

    private fun warningOrNull(entity: Any): DataWarning<*>?
    {
        fun personNameExistsWarningOrNull
                    (p: Person): PersonNameExistsWarning?
        {
            if(super.peopleDAO.getByName(p.name).isEmpty())
                return null
            return PersonNameExistsWarning(p.ID, p.name, p.personTypeID)
        }

        fun seminarNameAndCityExistWarningOrNull(sem: Seminar): SeminarNameAndCityExistWarning?
        {
            if(super.seminarsDAO.getByNameAndCity(sem.name, sem.city).isEmpty())
                return null
            return SeminarNameAndCityExistWarning(
                sem.ID, sem.name, sem.city, sem.address, sem.content, sem.costing, sem.isLogicallyDeleted )
        }

        fun productNameExistsWarningOrNull(pr: Product): ProductNameExistsWarning?
        {
            if(super.productsDAO.getByName(pr.name).isEmpty())
                return null
            return ProductNameExistsWarning(
                pr.ID, pr.name, pr.cost, pr.countBoxes, pr.countCase, pr.isLogicallyDeleted )
        }

        return when(entity) {
            is Person  -> personNameExistsWarningOrNull(entity)
            is Seminar -> seminarNameAndCityExistWarningOrNull(entity)
            is Product -> productNameExistsWarningOrNull(entity)
            else       -> null
        }
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////
    // CLEARING DATA:

    private fun removeDiscardedLacks
    (rdb: RemoteData.Builder, removedSeminarIDs: HashSet<Int>, removedProductIDs: HashSet<Int>)
    {
        val newLacks = mutableListOf<DataLack<*,*>>()

        for(lack in rdb.lacks)
        {
            if(!lack.isDiscarded) {
                newLacks.add(lack)
                continue
            }

            when(lack) {
                is SeminarNameLack -> lack.ID?.also { removedSeminarIDs.add(it) }
                is SeminarCityLack -> lack.ID?.also { removedSeminarIDs.add(it) }
                is ProductCostLack -> lack.ID?.also { removedProductIDs.add(it) }
            }
        }

        rdb.lacks = newLacks
    }

    private fun removeDroppedWarnings(
        rdb: RemoteData.Builder,
        removedPersonIDs: HashSet<Int>,
        removedSeminarIDs: HashSet<Int>,
        removedProductIDs: HashSet<Int> )
    {
        val newWarnings = mutableListOf<DataWarning<*>>()

        for(w in rdb.warnings)
        {
            if(w.action != DataWarning.Action.DROP) {
                newWarnings.add(w)
                continue
            }

            when(w) {
                is PersonNameExistsWarning -> w.ID?.also { removedPersonIDs.add(it) }
                is SeminarNameAndCityExistWarning -> w.ID?.also { removedSeminarIDs.add(it) }
                is ProductNameExistsWarning -> w.ID?.also { removedProductIDs.add(it) }
            }
        }

        rdb.warnings = newWarnings
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////
    // DATA DEPENDENCIES:

    private fun removeDependentLacks(
        lacks: MutableList<DataLack<*,*>>,
        removedPersonTypeIDs: HashSet<Int>,
        removedPeopleIDs: HashSet<Int>,
        removedSeminarIDs: HashSet<Int>,
        removedProductIDs: HashSet<Int> )
    {
        if(removedPersonTypeIDs.isNotEmpty()) {
            // TODO: Null People.typeIDs referring removed PersonType IDs.
        }

        if(removedPeopleIDs.isNotEmpty() ||
            removedSeminarIDs.isNotEmpty() ||
            removedProductIDs.isNotEmpty() ) {
            var index = 0

            var personID: Int?
            var seminarID: Int?
            var productID: Int?
            while(index < lacks.size)
            {
                personID = null; seminarID = null; productID = null
                val lack = lacks[index]

                when(lack)
                {
                    is AppointmentPurchaseLack -> {
                        personID = lack.personID
                        seminarID = lack.seminarID
                    }
                    is AppointmentCostLack -> {
                        personID = lack.personID
                        seminarID = lack.seminarID
                    }
                    is AppointmentMoneyLack -> {
                        personID = lack.personID
                        seminarID = lack.seminarID
                    }
                    is OrderPurchaseLack -> {
                        personID = lack.personID
                        productID = lack.productID
                    }
                    is SemCostMoneyLack -> {
                        seminarID = lack.seminarID
                    }
                }

                if( (personID?.let { removedPeopleIDs.contains(it) } == true) ||
                    (seminarID?.let { removedSeminarIDs.contains(it) } == true) ||
                    (productID?.let { removedProductIDs.contains(it) } == true) ) {
                    // Constraint failed. Remove this Lack.
                    lacks[index] = lacks.last()
                    lacks.removeAt(lacks.lastIndex)
                } else {
                    // OK, move on.
                    ++index
                }
            }
        }
    }

    private fun removeDependentEntities(
        entities: MutableList<Any>,
        entityActions: MutableList<RemoteData.EntityAction>,
        removedPersonTypeIDs: HashSet<Int>,
        removedPeopleIDs: HashSet<Int>,
        removedSeminarIDs: HashSet<Int>,
        removedProductIDs: HashSet<Int> )
    {
        if(removedPersonTypeIDs.isNotEmpty()) {
            // TODO: Find People dependent on removed PersonTypes and null their typeIDs.
        }

        var personID: Int?
        var seminarID: Int?
        var productID: Int?
        for(index in 0 until entities.size)
        {
            personID = null; seminarID = null; productID = null

            if(entityActions[index] == RemoteData.EntityAction.DELETE)
                continue

            val entity = entities[index]
            when(entity)
            {
                is Appointment -> {
                    personID = entity.personID
                    seminarID = entity.seminarID
                }
                is Order -> {
                    personID = entity.personID
                    productID = entity.productID
                }
                is SemDay -> {
                    seminarID = entity.seminarID
                }
                is SemCost -> {
                    seminarID = entity.seminarID
                }
            }

            if( (personID?.let { removedPeopleIDs.contains(it) } == true) ||
                (seminarID?.let { removedSeminarIDs.contains(it) } == true) ||
                (productID?.let { removedProductIDs.contains(it) } == true) )
                entityActions[index] = RemoteData.EntityAction.DELETE
        }
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////
    // DATA REFACTORING:

    private fun mapEntitiesToWarnings(rdBuilder: RemoteData.Builder)
    {
        val newEntities = mutableListOf<Any>()

        for(entity in rdBuilder.entities)
        {
            warningOrNull(entity)?.also {
                rdBuilder.warnings.add(it)
            } ?: newEntities.add(entity)
        }

        rdBuilder.entities = newEntities
        rdBuilder.entityActions = MutableList(newEntities.size) { RemoteData.EntityAction.INSERT }
        Log.v(LOGTAG, "mapEntitiesToWarnings: E=${newEntities.size}, W=${rdBuilder.warnings.size}")
    }

    private fun mapFilledLacks(rdBuilder: RemoteData.Builder)
    {
        val newLacks = mutableListOf<DataLack<*,*>>()

        for(lack in rdBuilder.lacks)
        {
            if(!lack.isFilled) {
                newLacks.add(lack)
                continue
            }

            val builtObject = lack.buildObject()!!
            val warning = warningOrNull(builtObject)
            if(warning == null) {
                rdBuilder.entities.add(builtObject)
                rdBuilder.entityActions.add(RemoteData.EntityAction.INSERT)
            } else {
                rdBuilder.warnings.add(warning)
            }
        }

        rdBuilder.lacks = newLacks
        Log.v(LOGTAG, "mapFilledLacks: generated ${newLacks.size} lacks.")
    }

    private fun mapWarningsWithDefinedAction(rdBuilder: RemoteData.Builder)
    {
        val newWarnings = mutableListOf<DataWarning<*>>()

        for(w in rdBuilder.warnings)
        {
            if(w.action == DataWarning.Action.NOT_DEFINED) {
                newWarnings.add(w)
            } else {
                rdBuilder.entities.add(w.produceObject()!!)
                rdBuilder.entityActions.add(
                    when(w.action)
                    {
                        DataWarning.Action.UPDATE    -> RemoteData.EntityAction.UPDATE
                        DataWarning.Action.DROP      -> RemoteData.EntityAction.DELETE
                        DataWarning.Action.DUPLICATE -> RemoteData.EntityAction.INSERT
                        else -> throw Exception("Unknown DataWarning.Action")
                    }
                )
            }
        }

        rdBuilder.warnings = newWarnings
        Log.v(LOGTAG, "mapWarningsWithDefinedAction: generated ${newWarnings.size} warnings")
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////
    // DATA INSERTION:

    private fun flushEntities(rdb: RemoteData.Builder)
    {
        // Map pseudo-IDs to real database IDs:
        val personTypeIdsMap = HashMap<Int, Int>()
        val personIdsMap = HashMap<Int, Int>()
        val seminarIdMap = HashMap<Int, Int>()
        val productIdMap = HashMap<Int, Int>()

        // TODO: Have a loop for PersonTypes.

        for(index in 0 until rdb.entities.size)
        {
            val ent = rdb.entities[index]
            val action = rdb.entityActions[index]
            if(action == RemoteData.EntityAction.DELETE)
                continue

            when(ent)
            {
                is Person -> {
                    var mID = ent.ID; var mName = ent.name
                    var mType = ent.personTypeID?.let { personTypeIdsMap[it]!! }

                    if(action == RemoteData.EntityAction.INSERT) {
                        val realID = Person(null, mName, mType)
                            .let { super.peopleDAO.addOrUpdate(it).toInt() }
                        mID?.also { personIdsMap[it] = realID }
                    } else {
                        for(oldPerson in super.peopleDAO.getByName(mName))
                        {
                            super.peopleDAO.addOrUpdate( Person(oldPerson.ID, mName, mType) )
                            mID?.also { personIdsMap[it] = oldPerson.ID!! }
                        }
                    }
                }

                is Seminar -> {
                    if(action == RemoteData.EntityAction.INSERT) {
                        val realSem = Seminar(
                            null,
                            ent.name,
                            ent.city,
                            ent.address,
                            ent.content,
                            ent.imageUri,
                            ent.costing,
                            ent.isLogicallyDeleted
                        )
                        val realID = super.seminarsDAO.addOrUpdate(realSem).toInt()
                        ent.ID?.also { seminarIdMap[it] = realID }
                    } else {
                        for(oldSem in super.seminarsDAO.getByNameAndCity(ent.name, ent.city))
                        {
                            val newSem = Seminar(
                                oldSem.ID,
                                ent.name,
                                ent.city,
                                ent.address,
                                ent.content,
                                ent.imageUri,
                                ent.costing,
                                ent.isLogicallyDeleted
                            )
                            super.seminarsDAO.addOrUpdate(newSem)
                            ent.ID?.also { seminarIdMap[it] = oldSem.ID!! }
                        }
                    }
                }

                is Product -> {
                    if(action == RemoteData.EntityAction.INSERT) {
                        val realProd = Product(
                            null, ent.name, ent.cost, ent.countBoxes, ent.countCase, ent.isLogicallyDeleted )
                        val realID = super.productsDAO.addOrUpdate(realProd).toInt()
                        ent.ID?.also { productIdMap[it] = realID }
                    } else {
                        for(oldProd in super.productsDAO.getByName(ent.name))
                        {
                            val newProd = Product(
                                oldProd.ID, ent.name, ent.cost, ent.countBoxes, ent.countCase, ent.isLogicallyDeleted )
                            super.productsDAO.addOrUpdate(newProd)
                            ent.ID?.also { productIdMap[it] = oldProd.ID!! }
                        }
                    }
                }
            }
        }

        val appoints = mutableListOf<Appointment>()
        val orders = mutableListOf<Order>()
        val semdays = mutableListOf<SemDay>()
        val semcosts = mutableListOf<SemCost>()
        for(index in 0 until rdb.entities.size)
        {
            val ent = rdb.entities[index]
            if(rdb.entityActions[index] == RemoteData.EntityAction.DELETE)
                continue

            // No warnings are generated for these types of entities,
            // so further in this loop all actions are INSERT.
            when(ent)
            {
                is Appointment -> {
                    appoints.add( Appointment(
                        personIdsMap[ent.personID]!!,
                        seminarIdMap[ent.seminarID]!!,
                        ent.purchase,
                        ent.cost,
                        ent.historyStatus,
                        ent.isLogicallyDeleted
                    ) )
                }

                is Order -> {
                    val personID = personIdsMap[ent.personID]!!
                    val productID = productIdMap[ent.productID]!!
                    val order = Order(
                        personID, productID, ent.purchase, ent.historyStatus, ent.isLogicallyDeleted )
                    orders.add(order)
                }

                is SemDay -> {
                    val semID = seminarIdMap[ent.seminarID]!!
                    val day = SemDay(null, semID, ent.start, ent.duration)
                    semdays.add(day)
                }

                is SemCost -> {
                    val semID = seminarIdMap[ent.seminarID]!!
                    val cost = SemCost(null, semID, ent.minParticipants, ent.minDate, ent.cost)
                    semcosts.add(cost)
                }
            }
        }
        super.associationsDAO.addOrUpdateAppointments(appoints)
        super.associationsDAO.addOrUpdateOrders(orders)
        super.seminarsDAO.addOrUpdateDays(semdays)
        super.seminarsDAO.addOrUpdateCosts(semcosts)
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////
    // LACK INFOS:

    private fun getLackInfos(request: DataLackInfosRequest): List<DataLackInfo?>
    {
        val peopleMap   = HashMap<Int, String?>()                                 // ID -> name?
        val seminarsMap = HashMap<Int, Pair<String, Seminar.CostingStrategy>?>()  // ID -> (name; costing)?
        val productsMap = HashMap<Int, String?>()                                 // ID -> name?

        // Fill the maps:
        for(lack in request.lacks)
        {
            when(lack)
            {
                is SeminarNameLack ->
                    lack.ID?.also { seminarsMap[it] = Pair("", lack.costing) }
                is SeminarCityLack ->
                    lack.ID?.also { seminarsMap[it] = Pair(lack.name, lack.costing) }
                is ProductCostLack ->
                    lack.ID?.also { productsMap[it] = lack.name }
            }
        }

        for(w in request.warnings)
        {
            when(w)
            {
                is PersonNameExistsWarning ->
                    w.ID?.also { peopleMap[it] = w.name }
                is SeminarNameAndCityExistWarning ->
                    w.ID?.also { seminarsMap[it] = Pair(w.name, w.costing) }
                is ProductNameExistsWarning ->
                    w.ID?.also { productsMap[it] = w.name }
            }
        }

        for(ent in request.entities)
        {
            when(ent)
            {
                is Person ->
                    ent.ID?.also { peopleMap[it] = ent.name }
                is Seminar ->
                    ent.ID?.also { seminarsMap[it] = Pair(ent.name, ent.costing) }
                is Product ->
                    ent.ID?.also { productsMap[it] = ent.name }
            }
        }

        // Help functions:
        fun appointLackInfo(personID: Int, seminarID: Int)
            = DataLackInfo.AppointmentInfo(
                peopleMap[personID]!!, seminarsMap[seminarID]!!.first )
        fun orderLackInfo(personID: Int, productID: Int)
            = DataLackInfo.OrderInfo(
                peopleMap[personID]!!, productsMap[productID]!! )
        fun semCostLackInfo(seminarID: Int)
            = seminarsMap[seminarID]!!.let {
                DataLackInfo.SemCostInfo(it.first, it.second)
            }

        return MutableList<DataLackInfo?>(request.lacks.size) { index ->
            val lack = request.lacks[index]
            when(lack)
            {
                is AppointmentPurchaseLack -> {
                    appointLackInfo(lack.personID, lack.seminarID)
                }
                is AppointmentCostLack -> {
                    appointLackInfo(lack.personID, lack.seminarID)
                }
                is AppointmentMoneyLack -> {
                    appointLackInfo(lack.personID, lack.seminarID)
                }
                is OrderPurchaseLack -> {
                    orderLackInfo(lack.personID, lack.productID)
                }
                is SemCostMoneyLack -> {
                    semCostLackInfo(lack.seminarID).also {
                        Log.v("Import UI", "SemCostMoneyLack -> seminarName=\"${it.seminarName}\"")
                    }
                }
                else -> null
            }
        }
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////
    // CREATING OBSERVABLES:

    private val responseSubject = BehaviorSubject.create<SyncResponse>()
    val requestSubject = PublishSubject.create<SyncRequest>()
    val databaseFulfillSubject = PublishSubject.create<RemoteData>()
    val lackInfosSubject = PublishSubject.create<DataLackInfosRequest>()

    fun createRemoteDataObservable(): Observable<RemoteData>
        = requestSubject
            .observeOn(Schedulers.io())
            .filter { request ->
                request.action == SyncRequest.RemoteAction.IMPORT
            }.map { request ->
                // Data comes from the server with all constraints fulfilled
                // (otherwise SpeechManXmlParser throws an exception), but there may be a duplicated data,
                // so we possibly need to generate warnings.
                fetchRemoteData(request).also {
                    val response = SyncResponse(
                        request.requestID, SyncResponse.ProgressStatus.XML_PARSED )
                    responseSubject.onNext(response)
                    mapEntitiesToWarnings(it)
                }
            }.mergeWith( databaseFulfillSubject
                .observeOn(Schedulers.computation())
                .map { rd ->
                    if(rd.entities.size != rd.entityActions.size)
                        throw Exception("${rd.entities.size} entities VS ${rd.entityActions.size} actions")

                    // RemoteData comes from UI with changes.
                    // Lacks may have been filled/discarded and DataWarning.Actions may have been defined.

                    val rdb = rd.toBuilder()

                    // Learn which IDs entities cannot depend on anymore:
                    val removedPersonTypeIDs = HashSet<Int>()
                    val removedPersonIDs = HashSet<Int>()
                    val removedSeminarIDs = HashSet<Int>()
                    val removedProductIDs = HashSet<Int>()
                    removeDiscardedLacks(rdb, removedSeminarIDs, removedProductIDs)
                    removeDroppedWarnings(rdb, removedPersonIDs, removedSeminarIDs, removedProductIDs)

                    // Remove the dependencies:
                    removeDependentLacks(
                        rdb.lacks, removedPersonTypeIDs, removedPersonIDs, removedSeminarIDs, removedProductIDs )
                    removeDependentEntities(
                        rdb.entities,
                        rdb.entityActions,
                        removedPersonTypeIDs,
                        removedPersonIDs,
                        removedSeminarIDs,
                        removedProductIDs
                    )

                    // Apply user changes, generate new entities or warnings:
                    mapWarningsWithDefinedAction(rdb)
                    mapFilledLacks(rdb)

                    rdb
                }
            ).map { rdb ->
                // Data comes here with all constraints fulfilled.
                // No filled lacks, no warnings with a defined action, no duplicated data.

                if(rdb.lacks.isEmpty() && rdb.warnings.isEmpty()) {
                    flushEntities(rdb)
                    return@map RemoteData(rdb.requestID, listOf(), listOf(), listOf(), listOf())
                }
                return@map rdb.build()
            }.observeOn(AndroidSchedulers.mainThread())

    fun createLackInfosObservable(): Observable<DataLackInfosResponse>
        = lackInfosSubject
            .observeOn(Schedulers.computation())
            .map { request ->
                DataLackInfosResponse(request.requestID, getLackInfos(request))
            }.observeOn(AndroidSchedulers.mainThread())

    fun createSyncResponseObservable(): Observable<SyncResponse>
        = requestSubject
            .observeOn(Schedulers.io())
            .filter { request ->
                request.action == SyncRequest.RemoteAction.EXPORT
            }.map { request ->
                pushRemoteData(request)
                SyncResponse(request.requestID, SyncResponse.ProgressStatus.DATA_PUSHED)
            }.mergeWith(responseSubject)
            .observeOn(AndroidSchedulers.mainThread())
}