package by.vadim_churun.ordered.speechman2.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import by.vadim_churun.ordered.speechman2.R
import by.vadim_churun.ordered.speechman2.db.entities.SemCost
import by.vadim_churun.ordered.speechman2.model.objects.SeminarBuilder
import by.vadim_churun.ordered.speechman2.repo.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject


class SpeechManViewModel(app: Application): AndroidViewModel(app)
{
    private val LOGTAG = SpeechManViewModel::class.java.simpleName

    //////////////////////////////////////////////////////////////////////////////////////////////////
    // WRAPPING PEOPLE REPOSITORY:

    private val peopleRepo = PeopleRepository(super.getApplication())

    fun createPersonObservable(personID: Int)
        = peopleRepo.createPersonObservable(personID)

    fun createPeopleObservable()
        = peopleRepo.createPeopleObservable()

    fun createPersonInfoObservable()
        = peopleRepo.createInfoObservable()

    fun createAppointedSeminarObservable(personID: Int, seminarID: Int)
        = peopleRepo.createAppointedSeminarObservable(personID, seminarID)

    fun createAppointedSeminarsObservable(personID: Int)
        = peopleRepo.createAppointedSeminarsObservable(personID)

    fun createSemHeadersNotForPersonObservable(excludedPersonID: Int)
        = peopleRepo.createSemHeadersNotForPersonObservable(excludedPersonID)

    fun validatePersonName(name: CharSequence)
        = peopleRepo.validateName(name)


    //////////////////////////////////////////////////////////////////////////////////////////////////
    // WRAPPING SEMINARS REPOSITORY:

    private val semsRepo = SeminarsRepository(super.getApplication())

    fun createSeminarObservable(seminarID: Int)
        = semsRepo.createSeminarObservable(seminarID)

    fun createSemDaysObservable(seminarID: Int)
        = semsRepo.createDaysObservable(seminarID)

    /** [SemCost]s are sorted just by their [Money] component.
     * For a smarter sort the received list can be passed to a [SeminarBuilder]. **/
    fun createSemCostsObservable(seminarID: Int)
        = semsRepo.createCostsObservable(seminarID)

    fun createSeminarHeaderObservable(seminarID: Int)
        = semsRepo.createHeaderObservable(seminarID)

    fun createSeminarHeadersObservable()
        = semsRepo.createHeadersObservable()

    fun createSeminarInfosObservable()
        = semsRepo.createInfosObservable()

    fun createParticipantsObservable(seminarID: Int)
        = semsRepo.createParticipantsObservable(seminarID)

    fun createSeminarBuilderObservable()
        = semsRepo.createBuilderObservable()

    fun createSemAppointsBuilderObservable()
        = semsRepo.appointsBuilderSubject
            .observeOn(AndroidSchedulers.mainThread())


    //////////////////////////////////////////////////////////////////////////////////////////////////
    // WRAPPING REMOTE REPOSITORY:

    private val remoteRepo = RemoteRepository(super.getApplication())
    private val ipValidationSubject = PublishSubject.create<Boolean>()

    val nextSyncRequestID: Int
        = RemoteRepository.nextRequestID

    fun createRemoteDataObservable()
         = remoteRepo.createRemoteDataObservable()

    fun createSyncResponseObservable()
        = remoteRepo.createSyncResponseObservable()

    fun createLackInfosObservable()
        = remoteRepo.createLackInfosObservable()

    fun createPersistedIpMaybe()
        = remoteRepo.createPersistedIpMaybe()

    fun createIpValidationObservable()
        = ipValidationSubject.observeOn(AndroidSchedulers.mainThread())


    //////////////////////////////////////////////////////////////////////////////////////////////////
    // WRAPPING BITMAP REPOSITORY:

    private val bitmapRepo = BitmapRepository(super.getApplication())

    val nextImageDecodeID: Int
        get() = BitmapRepository.nextRequestID

    fun createDecodedImagesObservable()
        = bitmapRepo.createDecodedImagesObservable()

    fun cancelImageDecodeRequest(requestID: Int)
        = BitmapRepository.cancelRequest(requestID)


    //////////////////////////////////////////////////////////////////////////////////////////////////
    // REACTIVE:

    private val disposable = CompositeDisposable()

    val actionSubject = PublishSubject.create<SpeechManAction>()
    private fun observeActionSubject()
        = actionSubject.observeOn(AndroidSchedulers.mainThread())
            .doOnNext { action ->
                when(action)
                {
                    is SpeechManAction.AddPerson -> {
                        val msgAddingPerson = super
                            .getApplication<Application>().getString(R.string.msg_adding_person)
                        actionSubject.onNext( SpeechManAction.ShowMessage(false, msgAddingPerson) )
                        peopleRepo.addOrUpdate(action.person)
                    }

                    is SpeechManAction.ApplySemCostParticipants -> {
                        val msgID = semsRepo.validateCostParticipants(action.input)
                        if(msgID == null) {
                            action.costBuilder.minParticipants = action.input.toInt()
                            semsRepo.builderSubject.onNext(action.fullBuilder)
                            return@doOnNext
                        }

                        SpeechManAction.ShowMessage(true,
                            super.getApplication<Application>().getString(msgID)
                        ).also {
                            actionSubject.onNext(it)
                        }
                    }

                    is SpeechManAction.DecodeImages -> {
                        bitmapRepo.sourceSubject.onNext(
                            BitmapRepository.DecodeRequest(action.requestID, action.imagesSource) )
                    }

                    is SpeechManAction.DeleteAppointment -> {
                        val msgDeletingAppoint = super.getApplication<Application>()
                            .resources.getString(R.string.msg_deleting_appoint)
                        actionSubject.onNext( SpeechManAction.ShowMessage(false, msgDeletingAppoint) )
                        peopleRepo.deleteAppointment(action.appointment)
                    }

                    is SpeechManAction.DeletePerson -> {
                        val msgDeletingPerson = super.getApplication<Application>()
                            .resources.getString(R.string.msg_deleting_person)
                        actionSubject.onNext( SpeechManAction.ShowMessage(false, msgDeletingPerson) )
                        peopleRepo.delete(action.person)
                    }

                    is SpeechManAction.DeleteSeminar -> {
                        SpeechManAction.ShowMessage(
                            false,
                            super.getApplication<Application>().resources, R.string.msg_deleting_seminar
                        ).also {
                            actionSubject.onNext(it)
                        }
                        semsRepo.deleteSeminar(action.seminarID)
                    }

                    is SpeechManAction.MigrateSeminarCosting -> {
                        semsRepo.migrateCosting(action.builder, action.newCosting)
                    }

                    is SpeechManAction.PublishSemAppointsBuilder -> {
                        semsRepo.appointsBuilderSubject.onNext(action.builder)
                    }

                    is SpeechManAction.PublishSeminarBuilder -> {
                        semsRepo.builderSubject.onNext(action.builder)
                        sbuilderUptodateSubject.onNext(true)
                    }

                    is SpeechManAction.RetrieveSeminarAppointsBuilder -> {
                        semsRepo.retrieveAppointsBuilder(action.seminarID)
                    }

                    is SpeechManAction.RequestDataLackInfos -> {
                        remoteRepo.lackInfosSubject.onNext(action.request)
                    }

                    is SpeechManAction.RequestPersonInfos -> {
                        peopleRepo.infoRequestSubject.onNext(action.people)
                    }

                    is SpeechManAction.RequestSeminarInfo -> {
                        semsRepo.infoSubject.onNext(action.seminar)
                    }

                    is SpeechManAction.RequestSeminarInfos -> {
                        semsRepo.infoSubjectHeaders.onNext(action.semHeaders)
                    }

                    is SpeechManAction.RequestSync -> {
                        val isIpGood = remoteRepo.validateIP(action.request.ip)
                        ipValidationSubject.onNext(isIpGood)
                        if(isIpGood) {
                            remoteRepo.persistIP(action.request.ip)
                            remoteRepo.requestSubject.onNext(action.request)
                        }
                    }

                    is SpeechManAction.SaveAppointment -> {
                        val msg = super.getApplication<Application>().getString(R.string.msg_saving_changes)
                        actionSubject.onNext(SpeechManAction.ShowMessage(false, msg))
                        peopleRepo.addOrUpdateAppointments(listOf(action.appoint))
                    }

                    is SpeechManAction.SaveSeminar -> {
                        semsRepo.builderSubject.onNext(action.builder)
                        semsRepo.save(action.builder)?.also {
                            actionSubject.onNext( SpeechManAction.NavigateError(it) )
                            actionSubject.onNext( SpeechManAction.ShowMessage(
                                true, super.getApplication<Application>().resources, it) )
                        } ?: actionSubject.onNext(SpeechManAction.NavigateBack)
                    }

                    is SpeechManAction.SaveSeminarAppoints -> {
                        for(appoint in action.builder.addedAppoints)
                        {
                            Log.i(LOGTAG, "Saving cost: ${appoint.cost}")
                        }

                        super.getApplication<Application>().getString(R.string.msg_saving_changes).also {
                            actionSubject.onNext( SpeechManAction.ShowMessage(false, it) )
                        }
                        semsRepo.saveAppoints(action.builder)
                    }

                    is SpeechManAction.SetAppointedSeminarsFilter -> {
                        peopleRepo.appointedSeminarsFilterSubject.onNext(action.filter)
                    }

                    is SpeechManAction.SetPeopleFilter -> {
                        peopleRepo.filterSubject.onNext(action.filter)
                    }

                    is SpeechManAction.SetSeminarsFilter -> {
                        semsRepo.filterSubject.onNext(action.filter)
                        peopleRepo.semheadersFilterSubject.onNext(action.filter)
                    }

                    is SpeechManAction.UpdatePerson -> {
                        val msgUpdating = super
                            .getApplication<Application>().getString(R.string.msg_saving_changes)
                        actionSubject.onNext( SpeechManAction.ShowMessage(false, msgUpdating) )
                        peopleRepo.addOrUpdate(action.person)
                    }

                    is SpeechManAction.UpdateSeminar -> {
                        semsRepo.save(action.seminar)
                    }
                }
            }.subscribe()

    private var keptAction: SpeechManAction? = null
    fun keepAction(action: SpeechManAction)
    { keptAction = action }
    fun consumeKeptAction(): SpeechManAction?
        = keptAction?.also { keptAction = null }


    //////////////////////////////////////////////////////////////////////////////////////////////////
    // COMMUNICATION BETWEEN FRAGMENTS:

    /** Value emitted by this Subject determines whether a {@link SeminarBuilder}
      * currently stored within the application model is up-to-date.
      * It emits true on each [SpeechManAction.PublishSeminarBuilder] action. **/
    val sbuilderUptodateSubject = BehaviorSubject.create<Boolean>()


    //////////////////////////////////////////////////////////////////////////////////////////////////
    // VIEWMODEL LIFECYCLE:

    init
    {
        disposable.add(observeActionSubject())
    }

    override fun onCleared()
        = disposable.clear()
}