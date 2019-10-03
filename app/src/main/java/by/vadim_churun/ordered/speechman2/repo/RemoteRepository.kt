package by.vadim_churun.ordered.speechman2.repo

import android.content.Context
import android.os.Looper
import android.util.Log
import by.vadim_churun.ordered.speechman2.db.entities.*
import by.vadim_churun.ordered.speechman2.model.objects.*
import by.vadim_churun.ordered.speechman2.model.warning.PersonNameExistsWarning
import by.vadim_churun.ordered.speechman2.model.warning.ProductNameExistsWarning
import by.vadim_churun.ordered.speechman2.model.warning.SeminarNameAndCityExistWarning
import by.vadim_churun.ordered.speechman2.remote.*
import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject


class RemoteRepository(appContext: Context):
SpeechManRepository(appContext)
{
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


    val dataPullSubject = BehaviorSubject.create<SyncRequest>()
    val databaseFulfillSubject = BehaviorSubject.create<RemoteData>()

    /** Emits responses for requests supplied via [dataPullSubject] and [databaseFulfillSubject]. **/
    fun createRequestSubject(): Observable<RemoteData>
        = dataPullSubject
            .observeOn(Schedulers.io())
            .map { request ->
                if(Looper.myLooper() == Looper.getMainLooper())
                    throw Exception("Connecting to the server on the UI thread.")

                val rdb = RemoteData.Builder(request.requestID)
                val connection = SpeechManRemoteConnector.openConnection(request.ip, false)
                connection.getInputStream().use { instream ->
                    SpeechManXmlParser.parse(instream, rdb.entities, rdb.lacks)
                }

                rdb
            }.mergeWith(databaseFulfillSubject
                .observeOn(Schedulers.computation())
                .map { rd ->
                    if(Looper.myLooper() == Looper.getMainLooper())
                        throw Exception("Creating Builder from RemoteData on the UI thread")
                    rd.toBuilder()
                }
            ).observeOn(Schedulers.io())
            .map { oldBuilder ->
                if(Looper.myLooper() == Looper.getMainLooper())
                    throw Exception("Handling RemoteData on the UI thread")

                // Map XML pseudo-IDs to real database IDs:
                val personIdMap = HashMap<Int, Int>()
                val seminarIdMap = HashMap<Int, Int>()
                val productIdMap = HashMap<Int, Int>()

                val newBuilder = RemoteData.Builder(
                    requestID = oldBuilder.requestID,
                    lacks = oldBuilder.lacks
                )

                // Insert PersonType's, so that People's foreign key constraints don't fail.
                for(entity in oldBuilder.entities)
                {
                    if(entity !is PersonType) continue
                    // TODO: Insert this PersonType.
                }

                // Fill in the ID-maps:
                for(entity in oldBuilder.entities)
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
                            }
                        }
                    }
                }

                // Map IDs of remaining entities and optionally insert them:
                val insertedAppoints = mutableListOf<Appointment>()
                val insertedOrders = mutableListOf<Order>()
                val insertedDays = mutableListOf<SemDay>()
                val insertedCosts = mutableListOf<SemCost>()
                for(entity in oldBuilder.entities)
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

                // TODO: Handle lacks.
                // TODO: Handle warnings.

                return@map newBuilder.build()
            }.observeOn(AndroidSchedulers.mainThread())
}