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
import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject


class RemoteRepository(appContext: Context):
SpeechManRepository(appContext)
{
    //////////////////////////////////////////////////////////////////////////////////////////////////
    // COMPANION OBJECTS:

    companion object
    {
        private val LOGTAG = RemoteRepository::class.java.simpleName
        private val ID_LOCK = Any()
        private val REMOTE_LOCK = Any()
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
    // IMPLEMENTATION OF THE TRANSFORMATIONS:

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
                val response = instream.read()
                when(response.toChar())
                {
                    'O' -> {
                        // OK. Parse the response.
                        SpeechManXmlParser.parse(
                            instream, rdb.entities, rdb.lacks )
                    }

                    'E' -> {
                        // Error. Get the error code.
                        throw SpeechManServerException(instream.read().toByte())
                    }

                    else -> {
                        // Unknown response.
                        throw UnknownResponseSpeechManException(response.toByte())
                    }
                }
            }
        }
        SyncResponse(request.requestID, SyncResponse.ProgressStatus.XML_PARSED).also {
            responseSubject.onNext(it)
        }

        return rdb
    }

    private fun refactorRemoteData(rd: RemoteData): RemoteData.Builder
    {
        Log.v(LOGTAG, "refactorRemoteData. Entities: ${rd.entities.size}. Lacks: ${rd.lacks.size}")

        val newBuilder = RemoteData.Builder(rd.requestID, rd.entities.toMutableList())

        // Check for filled data which was previously lacked:
        for(lack in rd.lacks)
        {
            if(lack.isFilled)
                newBuilder.entities.add(lack.buildObject()!!)
            else if(!lack.isDiscarded)
                newBuilder.lacks.add(lack)
        }

        // Check user's response to warnings:
        for(warning in rd.warnings)
        {
            when(warning.action)
            {
                DataWarning.Action.DUPLICATE -> {
                    newBuilder.entities.add(warning.produceObject()!!)
                }

                DataWarning.Action.UPDATE -> {
                    when(warning)
                    {
                        is PersonNameExistsWarning -> {
                            for(oldPerson in super.peopleDAO.getByName(warning.name))
                            {
                                val newPerson = Person(
                                    oldPerson.ID, warning.name, warning.personTypeID )
                                super.peopleDAO.addOrUpdate(newPerson)
                            }
                        }

                        is SeminarNameAndCityExistWarning -> {
                            val oldSems = super.seminarsDAO.getByNameAndCity(warning.name, warning.city)
                            for(oldSeminar in oldSems)
                            {
                                val newSeminar = Seminar(oldSeminar.ID,
                                    warning.name,
                                    warning.city,
                                    warning.address,
                                    warning.content,
                                    oldSeminar.imageUri,    // Because images are never exported.
                                    warning.costing,
                                    warning.isLogicallyDeleted
                                )
                                super.seminarsDAO.addOrUpdate(newSeminar)
                            }
                        }

                        is ProductNameExistsWarning -> {
                            for(oldProduct in super.productsDAO.getByName(warning.name))
                            {
                                val newProduct = Product(oldProduct.ID,
                                    warning.name,
                                    warning.cost,
                                    warning.countBoxes,
                                    warning.countCase,
                                    warning.isLogicallyDeleted
                                )
                                super.productsDAO.addOrUpdate(newProduct)
                            }
                        }
                    }
                }

                DataWarning.Action.NOT_DEFINED -> {
                    newBuilder.warnings.add(warning)
                }
            }
        }

        return newBuilder
    }


    private fun handleRemoteData(builder: RemoteData.Builder): RemoteData
    {
        Log.i(LOGTAG, "handleRemoteData. Entities: ${builder.entities.size}. Lacks: ${builder.lacks.size}")

        // Map XML pseudo-IDs to real database IDs:
        val personIdMap = HashMap<Int, Int>()
        val seminarIdMap = HashMap<Int, Int>()
        val productIdMap = HashMap<Int, Int>()

        val newBuilder = RemoteData.Builder(
            requestID = builder.requestID,
            lacks = builder.lacks,
            warnings = builder.warnings
        )

        // Insert PersonType's, so that People's foreign key constraints don't fail.
        for(entity in builder.entities)
        {
            if(entity !is PersonType) continue
            // TODO: Insert this PersonType.
        }

        // Fill in the ID-maps:
        for(entity in builder.entities)
        {
            when(entity)
            {
                is Person -> {
                    if(super.peopleDAO.getByName(entity.name).isEmpty()) {
                        // No person with such name. Add one.
                        val realPerson = Person(null, entity.name, entity.personTypeID)
                        val realID = super.peopleDAO.addOrUpdate(realPerson).toInt()
                        Log.i(LOGTAG, "Map person ID: ${entity.ID} -> $realID")
                        entity.ID?.also { personIdMap[it] = realID }
                    } else {
                        // A person with such name already exists. Add another one?
                        PersonNameExistsWarning(entity.ID, entity.name, entity.personTypeID).also {
                            newBuilder.warnings.add(it)
                        }
                    }
                }

                is Seminar -> {
                    if(super.seminarsDAO
                            .getByNameAndCity(entity.name, entity.city)
                            .isEmpty() ) {
                        val realSem = Seminar(null,
                            entity.name,
                            entity.city,
                            entity.address,
                            entity.content,
                            entity.imageUri,
                            entity.costing,
                            entity.isLogicallyDeleted )
                        val realID = super.seminarsDAO.addOrUpdate(realSem).toInt()
                        Log.i(LOGTAG, "Map seminar ID: ${entity.ID} -> $realID")
                        entity.ID?.also { seminarIdMap[it] = realID }
                    } else {
                        SeminarNameAndCityExistWarning(entity.ID,
                            entity.name,
                            entity.city,
                            entity.address,
                            entity.content,
                            entity.costing,
                            entity.isLogicallyDeleted
                        ).also { newBuilder.warnings.add(it) }
                        Log.i(LOGTAG, "Generate SeminarNameAndCityExistWarning" +
                            "(name=\"${entity.name}\", city=\"${entity.city}\"" )
                    }
                }

                is Product -> {
                    if(super.productsDAO.getByName(entity.name).isEmpty()) {
                        val realProduct = Product(null,
                            entity.name,
                            entity.cost,
                            entity.countBoxes,
                            entity.countCase,
                            entity.isLogicallyDeleted )
                        val realID = super.productsDAO.addOrUpdate(realProduct).toInt()
                        Log.i(LOGTAG, "Map product ID: ${entity.ID} -> $realID")
                        entity.ID?.also { productIdMap[it] = realID }
                    } else {
                        ProductNameExistsWarning(entity.ID,
                            entity.name,
                            entity.cost,
                            entity.countBoxes,
                            entity.countCase,
                            entity.isLogicallyDeleted
                        ).also {
                            newBuilder.warnings.add(it)
                        }
                        Log.i(LOGTAG, "Generate ProductNameExistWarning" +
                            "(ID=${entity.ID}, name=${entity.name}" )
                    }
                }
            }
        }

        // Map IDs of remaining entities and optionally insert them:
        val insertedAppoints = mutableListOf<Appointment>()
        val insertedOrders = mutableListOf<Order>()
        val insertedDays = mutableListOf<SemDay>()
        val insertedCosts = mutableListOf<SemCost>()
        for(entity in builder.entities)
        {
            when(entity)
            {
                is Appointment -> {
                    val personRealID = personIdMap[entity.personID]
                    val seminarRealID = seminarIdMap[entity.seminarID]
                    if(personRealID != null && seminarRealID != null) {
                        Appointment(
                            personRealID,
                            seminarRealID,
                            entity.purchase,
                            entity.cost,
                            entity.historyStatus,
                            entity.isLogicallyDeleted
                        ).let { insertedAppoints.add(it) }
                    } else {
                        // Leave this Appointment for later, when the user fills required lacks.
                        newBuilder.entities.add(entity)
                    }
                }

                is Order -> {
                    val personRealID = personIdMap[entity.personID]
                    val productRealID = productIdMap[entity.productID]
                    if(personRealID != null && productRealID != null) {
                        Order(
                            personRealID,
                            productRealID,
                            entity.purchase,
                            entity.historyStatus,
                            entity.isLogicallyDeleted
                        ).let { insertedOrders.add(it) }
                    } else {
                        newBuilder.entities.add(entity)
                    }
                }

                is SemDay -> {
                    val seminarRealID = seminarIdMap[entity.seminarID]
                    seminarRealID?.also {
                        SemDay(null, seminarRealID, entity.start, entity.duration).let {
                            insertedDays.add(it)
                        }
                    } ?: newBuilder.entities.add(entity)
                }

                is SemCost -> {
                    val seminarRealID = seminarIdMap[entity.seminarID]
                    seminarRealID?.also {
                        SemCost(null,
                            seminarRealID,
                            entity.minParticipants,
                            entity.minDate,
                            entity.cost
                        ).let { insertedCosts.add(it) }
                    } ?: newBuilder.entities.add(entity)
                }
            }
        }
        super.associationsDAO.addOrUpdateAppointments(insertedAppoints)
        super.associationsDAO.addOrUpdateOrders(insertedOrders)
        super.seminarsDAO.addOrUpdateDays(insertedDays)
        super.seminarsDAO.addOrUpdateCosts(insertedCosts)

        return newBuilder.build()
    }


    private fun getLackInfos(request: DataLackInfosRequest): List<DataLackInfo?>
    {
        // Define methods to extract information from the maps and the database:
        fun getPersonName(personID: Int): String
        {
            try {
                return super.peopleDAO.rawGet(personID).name
            } catch(exc: Exception) {
                throw Exception("Cannot retrieve a Person with ID $personID", exc)
            }
        }
        fun getSeminarPair(seminarID: Int): Pair<String, Seminar.CostingStrategy>
        {
            try {
                val sem = super.seminarsDAO.get(seminarID)
                return Pair(sem.name, sem.costing)
            } catch(exc: Exception) {
                throw Exception("Failed to retrieve a Seminar with ID $seminarID", exc)
            }
        }
        fun getProductName(productID: Int): String
        {
            try {
                return super.productsDAO.rawGet(productID).name
            } catch(exc: Exception) {
                throw Exception("Failed to retrieve a Product with ID $productID", exc)
            }
        }

        // Obtain the infos and return:
        return MutableList<DataLackInfo?>(request.lacks.size) { index ->
            val lack = request.lacks[index]
            when(lack)
            {
                is AppointmentPurchaseLack -> {
                    return@MutableList DataLackInfo.AppointmentInfo(
                        getPersonName(lack.personID),
                        getSeminarPair(lack.seminarID).first
                    )
                }

                is AppointmentCostLack -> {
                    return@MutableList DataLackInfo.AppointmentInfo(
                        getPersonName(lack.personID),
                        getSeminarPair(lack.seminarID).first
                    )
                }

                is AppointmentMoneyLack -> {
                    return@MutableList DataLackInfo.AppointmentInfo(
                        getPersonName(lack.personID),
                        getSeminarPair(lack.seminarID).first
                    )
                }

                is OrderPurchaseLack -> {
                    return@MutableList  DataLackInfo.OrderInfo(
                        getPersonName(lack.personID),
                        getProductName(lack.productID)
                    )
                }

                is SemCostMoneyLack -> {
                    val sempair = getSeminarPair(lack.seminarID)
                    return@MutableList DataLackInfo.SemCostInfo(
                        sempair.first, sempair.second)
                }
            }

            // No additional information is needed for other types of DataLack.
            return@MutableList null
        }
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////
    // CREATING OBSERVABLES:

    private var lastRequestID: Int? = null
    private val responseSubject = BehaviorSubject.create<SyncResponse>()
    val requestSubject = PublishSubject.create<SyncRequest>()
    val databaseFulfillSubject = PublishSubject.create<RemoteData>()
    val lackInfosSubject = PublishSubject.create<DataLackInfosRequest>()

    fun createSyncResponseObservable(): Observable<SyncResponse>
        = requestSubject
            .observeOn(Schedulers.io())
            .filter { request ->
                request.action == SyncRequest.RemoteAction.EXPORT
            }.map { request ->
                // TODO: Export data
                SyncResponse(request.requestID, SyncResponse.ProgressStatus.DATA_PUSHED)
            }.mergeWith(responseSubject)
            .observeOn(AndroidSchedulers.mainThread())


    /** Emits responses for requests supplied via [requestSubject] and [databaseFulfillSubject]. **/
    fun createRemoteDataObservable(): Observable<RemoteData>
        = requestSubject                   // A publish subject.
            .observeOn(Schedulers.io())    // Won't be triggered unless the user requests it explicitly.
            .filter { request ->
                Log.v(LOGTAG,
                    "filter: request.requestID=${request.requestID}. lastRequestID=$lastRequestID" )
                request.requestID != lastRequestID &&
                request.action == SyncRequest.RemoteAction.IMPORT
            }.map { request ->
                lastRequestID = request.requestID
                fetchRemoteData(request)
            }.mergeWith(databaseFulfillSubject
                .observeOn(Schedulers.computation())
                .map { rd ->
                    refactorRemoteData(rd)
                }
            ).observeOn(Schedulers.io())
            .map { builder ->
                handleRemoteData(builder)
            }.observeOn(AndroidSchedulers.mainThread())

    fun createLackInfosObservable(): Observable<DataLackInfosResponse>
        = lackInfosSubject
            .observeOn(Schedulers.computation())
            .map { request ->
                DataLackInfosResponse(request.requestID, getLackInfos(request))
            }.observeOn(AndroidSchedulers.mainThread())
}